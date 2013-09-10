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
package org.apache.commons.configuration.io;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.FileSystem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.FileUtils;

/**
 * <p>
 * A utility class providing helper methods related to locating files.
 * </p>
 * <p>
 * The methods of this class are used behind the scenes when retrieving
 * configuration files based on different criteria, e.g. URLs, files, or more
 * complex search strategies. They also implement functionality required by the
 * default {@link FileSystem} implementations.
 * </p>
 *
 * @version $Id: $
 * @since 2.0
 */
final class FileLocatorUtils
{
    /** Constant for the file URL protocol */
    private static final String FILE_SCHEME = "file:";

    /** The logger.*/
    private static final Log LOG = LogFactory.getLog(ConfigurationUtils.class);

    /**
     * Private constructor so that no instances can be created.
     */
    private FileLocatorUtils()
    {
    }

    /**
     * Tries to convert the specified URL to a file object. If this fails,
     * <b>null</b> is returned.
     *
     * @param url the URL
     * @return the resulting file object
     */
    static File fileFromURL(URL url)
    {
        return FileUtils.toFile(url);
    }

    /**
     * Return the path without the file name, for example http://xyz.net/foo/bar.xml
     * results in http://xyz.net/foo/
     *
     * @param url the URL from which to extract the path
     * @return the path component of the passed in URL
     */
    static String getBasePath(URL url)
    {
        if (url == null)
        {
            return null;
        }

        String s = url.toString();
        if (s.startsWith(FILE_SCHEME) && !s.startsWith("file://"))
        {
            s = "file://" + s.substring(FILE_SCHEME.length());
        }

        if (s.endsWith("/") || StringUtils.isEmpty(url.getPath()))
        {
            return s;
        }
        else
        {
            return s.substring(0, s.lastIndexOf("/") + 1);
        }
    }

    /**
     * Extract the file name from the specified URL.
     *
     * @param url the URL from which to extract the file name
     * @return the extracted file name
     */
    static String getFileName(URL url)
    {
        if (url == null)
        {
            return null;
        }

        String path = url.getPath();

        if (path.endsWith("/") || StringUtils.isEmpty(path))
        {
            return null;
        }
        else
        {
            return path.substring(path.lastIndexOf("/") + 1);
        }
    }

    /**
     * Convert the specified file into an URL. This method is equivalent
     * to file.toURI().toURL(). It was used to work around a bug in the JDK
     * preventing the transformation of a file into an URL if the file name
     * contains a '#' character. See the issue CONFIGURATION-300 for
     * more details. Now that we switched to JDK 1.4 we can directly use
     * file.toURI().toURL().
     *
     * @param file the file to be converted into an URL
     */
    static URL toURL(File file) throws MalformedURLException
    {
        return file.toURI().toURL();
    }

    /**
     * Return the location of the specified resource by searching the user home
     * directory, the current classpath and the system classpath.
     *
     * @param fileSystem the FileSystem to use.
     * @param base the base path of the resource
     * @param name the name of the resource
     *
     * @return the location of the resource
     */
    static URL locate(FileSystem fileSystem, String base, String name)
    {
        if (LOG.isDebugEnabled())
        {
            StringBuilder buf = new StringBuilder();
            buf.append("ConfigurationUtils.locate(): base is ").append(base);
            buf.append(", name is ").append(name);
            LOG.debug(buf.toString());
        }

        if (name == null)
        {
            // undefined, always return null
            return null;
        }

        // attempt to create an URL directly

        URL url = fileSystem.locateFromURL(base, name);

        // attempt to load from an absolute path
        if (url == null)
        {
            File file = new File(name);
            if (file.isAbsolute() && file.exists()) // already absolute?
            {
                try
                {
                    url = toURL(file);
                    LOG.debug("Loading configuration from the absolute path " + name);
                }
                catch (MalformedURLException e)
                {
                    LOG.warn("Could not obtain URL from file", e);
                }
            }
        }

        // attempt to load from the base directory
        if (url == null)
        {
            try
            {
                File file = constructFile(base, name);
                if (file != null && file.exists())
                {
                    url = toURL(file);
                }

                if (url != null)
                {
                    LOG.debug("Loading configuration from the path " + file);
                }
            }
            catch (MalformedURLException e)
            {
                LOG.warn("Could not obtain URL from file", e);
            }
        }

        // attempt to load from the user home directory
        if (url == null)
        {
            try
            {
                File file = constructFile(System.getProperty("user.home"), name);
                if (file != null && file.exists())
                {
                    url = toURL(file);
                }

                if (url != null)
                {
                    LOG.debug("Loading configuration from the home path " + file);
                }

            }
            catch (MalformedURLException e)
            {
                LOG.warn("Could not obtain URL from file", e);
            }
        }

        // attempt to load from classpath
        if (url == null)
        {
            url = locateFromClasspath(name);
        }
        return url;
    }

    /**
     * Tries to find a resource with the given name in the classpath.
     *
     * @param resourceName the name of the resource
     * @return the URL to the found resource or <b>null</b> if the resource
     *         cannot be found
     */
    static URL locateFromClasspath(String resourceName)
    {
        URL url = null;
        // attempt to load from the context classpath
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null)
        {
            url = loader.getResource(resourceName);

            if (url != null)
            {
                LOG.debug("Loading configuration from the context classpath (" + resourceName + ")");
            }
        }

        // attempt to load from the system classpath
        if (url == null)
        {
            url = ClassLoader.getSystemResource(resourceName);

            if (url != null)
            {
                LOG.debug("Loading configuration from the system classpath (" + resourceName + ")");
            }
        }
        return url;
    }

    /**
     * Helper method for constructing a file object from a base path and a
     * file name. This method is called if the base path passed to
     * {@code getURL()} does not seem to be a valid URL.
     *
     * @param basePath the base path
     * @param fileName the file name
     * @return the resulting file
     */
    static File constructFile(String basePath, String fileName)
    {
        File file;

        File absolute = null;
        if (fileName != null)
        {
            absolute = new File(fileName);
        }

        if (StringUtils.isEmpty(basePath) || (absolute != null && absolute.isAbsolute()))
        {
            file = new File(fileName);
        }
        else
        {
            StringBuilder fName = new StringBuilder();
            fName.append(basePath);

            // My best friend. Paranoia.
            if (!basePath.endsWith(File.separator))
            {
                fName.append(File.separator);
            }

            //
            // We have a relative path, and we have
            // two possible forms here. If we have the
            // "./" form then just strip that off first
            // before continuing.
            //
            if (fileName.startsWith("." + File.separator))
            {
                fName.append(fileName.substring(2));
            }
            else
            {
                fName.append(fileName);
            }

            file = new File(fName.toString());
        }

        return file;
    }
}
