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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * FileSystem that uses java.io.File or HttpClient
 * @since 1.7
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 */
public class DefaultFileSystem extends FileSystem
{
    @Override
    public InputStream getInputStream(final URL url) throws ConfigurationException
    {
        // throw an exception if the target URL is a directory
        final File file = FileLocatorUtils.fileFromURL(url);
        if (file != null && file.isDirectory())
        {
            throw new ConfigurationException("Cannot load a configuration from a directory");
        }

        try
        {
            return url.openStream();
        }
        catch (final Exception e)
        {
            throw new ConfigurationException("Unable to load the configuration from the URL " + url, e);
        }
    }

    @Override
    public OutputStream getOutputStream(final URL url) throws ConfigurationException
    {
        // file URLs have to be converted to Files since FileURLConnection is
        // read only (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4191800)
        final File file = FileLocatorUtils.fileFromURL(url);
        if (file != null)
        {
            return getOutputStream(file);
        }
        // for non file URLs save through an URLConnection
        OutputStream out;
        try
        {
            final URLConnection connection = url.openConnection();
            connection.setDoOutput(true);

            // use the PUT method for http URLs
            if (connection instanceof HttpURLConnection)
            {
                final HttpURLConnection conn = (HttpURLConnection) connection;
                conn.setRequestMethod("PUT");
            }

            out = connection.getOutputStream();

            // check the response code for http URLs and throw an exception if an error occured
            if (connection instanceof HttpURLConnection)
            {
                out = new HttpOutputStream(out, (HttpURLConnection) connection);
            }
            return out;
        }
        catch (final IOException e)
        {
            throw new ConfigurationException("Could not save to URL " + url, e);
        }
    }

    @Override
    public OutputStream getOutputStream(final File file) throws ConfigurationException
    {
        try
        {
            // create the file if necessary
            createPath(file);
            return new FileOutputStream(file);
        }
        catch (final FileNotFoundException e)
        {
            throw new ConfigurationException("Unable to save to file " + file, e);
        }
    }

    @Override
    public String getPath(final File file, final URL url, final String basePath, final String fileName)
    {
        String path = null;
        // if resource was loaded from jar file may be null
        if (file != null)
        {
            path = file.getAbsolutePath();
        }

        // try to see if file was loaded from a jar
        if (path == null)
        {
            if (url != null)
            {
                path = url.getPath();
            }
            else
            {
                try
                {
                    path = getURL(basePath, fileName).getPath();
                }
                catch (final Exception e)
                {
                    // simply ignore it and return null
                    if (getLogger().isDebugEnabled())
                    {
                        getLogger().debug(String.format("Could not determine URL for "
                                + "basePath = %s, fileName = %s: %s", basePath,
                                fileName, e));
                    }
                }
            }
        }

        return path;
    }

    @Override
    public String getBasePath(final String path)
    {
        URL url;
        try
        {
            url = getURL(null, path);
            return FileLocatorUtils.getBasePath(url);
        }
        catch (final Exception e)
        {
            return null;
        }
    }

    @Override
    public String getFileName(final String path)
    {
        URL url;
        try
        {
            url = getURL(null, path);
            return FileLocatorUtils.getFileName(url);
        }
        catch (final Exception e)
        {
            return null;
        }
    }


    @Override
    public URL getURL(final String basePath, final String file) throws MalformedURLException
    {
        final File f = new File(file);
        if (f.isAbsolute()) // already absolute?
        {
            return FileLocatorUtils.toURL(f);
        }

        try
        {
            if (basePath == null)
            {
                return new URL(file);
            }
            final URL base = new URL(basePath);
            return new URL(base, file);
        }
        catch (final MalformedURLException uex)
        {
            return FileLocatorUtils.toURL(FileLocatorUtils.constructFile(basePath, file));
        }
    }


    @Override
    public URL locateFromURL(final String basePath, final String fileName)
    {
        try
        {
            URL url;
            if (basePath == null)
            {
                return new URL(fileName);
                //url = new URL(name);
            }
            final URL baseURL = new URL(basePath);
            url = new URL(baseURL, fileName);

            // check if the file exists
            InputStream in = null;
            try
            {
                in = url.openStream();
            }
            finally
            {
                if (in != null)
                {
                    in.close();
                }
            }
            return url;
        }
        catch (final IOException e)
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Could not locate file " + fileName + " at " + basePath + ": " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Create the path to the specified file.
     *
     * @param file the target file
     * @throws ConfigurationException if the path cannot be created
     */
    private void createPath(final File file) throws ConfigurationException
    {
        if (file != null)
        {
            // create the path to the file if the file doesn't exist
            if (!file.exists())
            {
                final File parent = file.getParentFile();
                if (parent != null && !parent.exists())
                {
                    if (!parent.mkdirs())
                    {
                        throw new ConfigurationException("Cannot create path: " + parent);
                    }
                }
            }
        }
    }
    /**
     * Wraps the output stream so errors can be detected in the HTTP response.
     * @since 1.7
     * @author <a
     * href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
     */
    private static class HttpOutputStream extends VerifiableOutputStream
    {
        /** The wrapped OutputStream */
        private final OutputStream stream;

        /** The HttpURLConnection */
        private final HttpURLConnection connection;

        public HttpOutputStream(final OutputStream stream, final HttpURLConnection connection)
        {
            this.stream = stream;
            this.connection = connection;
        }

        @Override
        public void write(final byte[] bytes) throws IOException
        {
            stream.write(bytes);
        }

        @Override
        public void write(final byte[] bytes, final int i, final int i1) throws IOException
        {
            stream.write(bytes, i, i1);
        }

        @Override
        public void flush() throws IOException
        {
            stream.flush();
        }

        @Override
        public void close() throws IOException
        {
            stream.close();
        }

        @Override
        public void write(final int i) throws IOException
        {
            stream.write(i);
        }

        @Override
        public String toString()
        {
            return stream.toString();
        }

        @Override
        public void verify() throws IOException
        {
            if (connection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST)
            {
                throw new IOException("HTTP Error " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
            }
        }
    }
}
