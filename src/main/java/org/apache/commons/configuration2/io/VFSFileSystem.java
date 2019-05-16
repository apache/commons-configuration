/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration2.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.UriParser;

/**
 * FileSystem that uses Commons VFS
 * @since 1.7
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 */
public class VFSFileSystem extends DefaultFileSystem
{
    /** The logger. */
    private final Log log = LogFactory.getLog(getClass());

    public VFSFileSystem()
    {
    }

    @Override
    public InputStream getInputStream(final URL url) throws ConfigurationException
    {
        FileObject file;
        try
        {
            final FileSystemOptions opts = getOptions(url.getProtocol());
            file = (opts == null) ? VFS.getManager().resolveFile(url.toString())
                    : VFS.getManager().resolveFile(url.toString(), opts);
            if (file.getType() != FileType.FILE)
            {
                throw new ConfigurationException("Cannot load a configuration from a directory");
            }
            final FileContent content = file.getContent();
            if (content == null)
            {
                final String msg = "Cannot access content of " + file.getName().getFriendlyURI();
                throw new ConfigurationException(msg);
            }
            return content.getInputStream();
        }
        catch (final FileSystemException fse)
        {
            final String msg = "Unable to access " + url.toString();
            throw new ConfigurationException(msg, fse);
        }
    }

    @Override
    public OutputStream getOutputStream(final URL url) throws ConfigurationException
    {
        try
        {
            final FileSystemOptions opts = getOptions(url.getProtocol());
            final FileSystemManager fsManager = VFS.getManager();
            final FileObject file = (opts == null) ? fsManager.resolveFile(url.toString())
                    : fsManager.resolveFile(url.toString(), opts);
            // throw an exception if the target URL is a directory
            if (file == null || file.getType() == FileType.FOLDER)
            {
                throw new ConfigurationException("Cannot save a configuration to a directory");
            }
            final FileContent content = file.getContent();

            if (content == null)
            {
                throw new ConfigurationException("Cannot access content of " + url);
            }
            return content.getOutputStream();
        }
        catch (final FileSystemException fse)
        {
            throw new ConfigurationException("Unable to access " + url, fse);
        }
    }

    @Override
    public String getPath(final File file, final URL url, final String basePath, final String fileName)
    {
        if (file != null)
        {
            return super.getPath(file, url, basePath, fileName);
        }
        try
        {
            final FileSystemManager fsManager = VFS.getManager();
            if (url != null)
            {
                final FileName name = fsManager.resolveURI(url.toString());
                if (name != null)
                {
                    return name.toString();
                }
            }

            if (UriParser.extractScheme(fileName) != null)
            {
                return fileName;
            }
            else if (basePath != null)
            {
                final FileName base = fsManager.resolveURI(basePath);
                return fsManager.resolveName(base, fileName).getURI();
            }
            else
            {
                final FileName name = fsManager.resolveURI(fileName);
                final FileName base = name.getParent();
                return fsManager.resolveName(base, name.getBaseName()).getURI();
            }
        }
        catch (final FileSystemException fse)
        {
            fse.printStackTrace();
            return null;
        }
    }

    @Override
    public String getBasePath(final String path)
    {
        if (UriParser.extractScheme(path) == null)
        {
            return super.getBasePath(path);
        }
        try
        {
            final FileSystemManager fsManager = VFS.getManager();
            final FileName name = fsManager.resolveURI(path);
            return name.getParent().getURI();
        }
        catch (final FileSystemException fse)
        {
            fse.printStackTrace();
            return null;
        }
    }

    @Override
    public String getFileName(final String path)
    {
        if (UriParser.extractScheme(path) == null)
        {
            return super.getFileName(path);
        }
        try
        {
            final FileSystemManager fsManager = VFS.getManager();
            final FileName name = fsManager.resolveURI(path);
            return name.getBaseName();
        }
        catch (final FileSystemException fse)
        {
            fse.printStackTrace();
            return null;
        }
    }

    @Override
    public URL getURL(final String basePath, final String file) throws MalformedURLException
    {
        if ((basePath != null && UriParser.extractScheme(basePath) == null)
            || (basePath == null && UriParser.extractScheme(file) == null))
        {
            return super.getURL(basePath, file);
        }
        try
        {
            final FileSystemManager fsManager = VFS.getManager();

            FileName path;
            if (basePath != null && UriParser.extractScheme(file) == null)
            {
                final FileName base = fsManager.resolveURI(basePath);
                path = fsManager.resolveName(base, file);
            }
            else
            {
                path = fsManager.resolveURI(file);
            }

            final URLStreamHandler handler = new VFSURLStreamHandler(path);
            return new URL(null, path.getURI(), handler);
        }
        catch (final FileSystemException fse)
        {
            throw new ConfigurationRuntimeException("Could not parse basePath: " + basePath
                + " and fileName: " + file, fse);
        }
    }

    @Override
    public URL locateFromURL(final String basePath, final String fileName)
    {
        final String fileScheme = UriParser.extractScheme(fileName);

        // Use DefaultFileSystem if basePath and fileName don't have a scheme.
        if ((basePath == null || UriParser.extractScheme(basePath) == null) && fileScheme == null)
        {
            return super.locateFromURL(basePath, fileName);
        }
        try
        {
            final FileSystemManager fsManager = VFS.getManager();

            FileObject file;
            // Only use the base path if the file name doesn't have a scheme.
            if (basePath != null && fileScheme == null)
            {
                final String scheme = UriParser.extractScheme(basePath);
                final FileSystemOptions opts = (scheme != null) ? getOptions(scheme) : null;
                FileObject base = (opts == null) ? fsManager.resolveFile(basePath)
                        : fsManager.resolveFile(basePath, opts);
                if (base.getType() == FileType.FILE)
                {
                    base = base.getParent();
                }

                file = fsManager.resolveFile(base, fileName);
            }
            else
            {
                final FileSystemOptions opts = (fileScheme != null) ? getOptions(fileScheme) : null;
                file = (opts == null) ? fsManager.resolveFile(fileName)
                        : fsManager.resolveFile(fileName, opts);
            }

            if (!file.exists())
            {
                return null;
            }
            final FileName path = file.getName();
            final URLStreamHandler handler = new VFSURLStreamHandler(path);
            return new URL(null, path.getURI(), handler);
        }
        catch (final FileSystemException fse)
        {
            return null;
        }
        catch (final MalformedURLException ex)
        {
            return null;
        }
    }

    private FileSystemOptions getOptions(final String scheme)
    {
        final FileSystemOptions opts = new FileSystemOptions();
        FileSystemConfigBuilder builder;
        try
        {
            builder = VFS.getManager().getFileSystemConfigBuilder(scheme);
        }
        catch (final Exception ex)
        {
            return null;
        }
        final FileOptionsProvider provider = getFileOptionsProvider();
        if (provider != null)
        {
            final Map<String, Object> map = provider.getOptions();
            if (map == null)
            {
                return null;
            }
            int count = 0;
            for (final Map.Entry<String, Object> entry : map.entrySet())
            {
                try
                {
                    String key = entry.getKey();
                    if (FileOptionsProvider.CURRENT_USER.equals(key))
                    {
                        key = "creatorName";
                    }
                    setProperty(builder, opts, key, entry.getValue());
                    ++count;
                }
                catch (final Exception ex)
                {
                    // Ignore an incorrect property.
                    continue;
                }
            }
            if (count > 0)
            {
                return opts;
            }
        }
        return null;

    }

    private void setProperty(final FileSystemConfigBuilder builder, final FileSystemOptions options,
                             final String key, final Object value)
    {
        final String methodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
        final Class<?>[] paramTypes = new Class<?>[2];
        paramTypes[0] = FileSystemOptions.class;
        paramTypes[1] = value.getClass();

        try
        {
            final Method method = builder.getClass().getMethod(methodName, paramTypes);
            final Object[] params = new Object[2];
            params[0] = options;
            params[1] = value;
            method.invoke(builder, params);
        }
        catch (final Exception ex)
        {
            log.warn("Cannot access property '" + key + "'! Ignoring.", ex);
        }

    }

    /**
     * Stream handler required to create URL.
     */
    private static class VFSURLStreamHandler extends URLStreamHandler
    {
        /** The Protocol used */
        private final String protocol;

        public VFSURLStreamHandler(final FileName file)
        {
            this.protocol = file.getScheme();
        }

        @Override
        protected URLConnection openConnection(final URL url) throws IOException
        {
            throw new IOException("VFS URLs can only be used with VFS APIs");
        }
    }
}
