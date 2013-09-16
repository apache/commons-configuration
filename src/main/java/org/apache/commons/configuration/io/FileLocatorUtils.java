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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * A utility class providing helper methods related to locating files.
 * </p>
 * <p>
 * The methods of this class are used behind the scenes when retrieving
 * configuration files based on different criteria, e.g. URLs, files, or more
 * complex search strategies. They also implement functionality required by the
 * default {@link FileSystem} implementations. Most methods are intended to be
 * used internally only by other classes in the {@code io} package.
 * </p>
 *
 * @version $Id: $
 * @since 2.0
 */
public final class FileLocatorUtils
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
    public static File fileFromURL(URL url)
    {
        return FileUtils.toFile(url);
    }

    /**
     * Returns an uninitialized {@code FileLocatorBuilder} which can be used
     * for the creation of a {@code FileLocator} object. This method provides
     * a convenient way to create file locators using a fluent API as in the
     * following example:
     * <pre>
     * FileLocator locator = FileLocatorUtils.fileLocator()
     *     .basePath(myBasePath)
     *     .fileName("test.xml")
     *     .create();
     * </pre>
     * @return a builder object for defining a {@code FileLocator}
     */
    public static FileLocatorBuilder fileLocator()
    {
        return fileLocator(null);
    }

    /**
     * Returns a {@code FileLocatorBuilder} which is already initialized with
     * the properties of the passed in {@code FileLocator}. This builder can
     * be used to create a {@code FileLocator} object which shares properties
     * of the original locator (e.g. the {@code FileSystem} or the encoding),
     * but points to a different file. An example use case is as follows:
     * <pre>
     * FileLocator loc1 = ...
     * FileLocator loc2 = FileLocatorUtils.fileLocator(loc1)
     *     .setFileName("anotherTest.xml")
     *     .create();
     * </pre>
     * @param src the source {@code FileLocator} (may be <b>null</b>)
     * @return an initialized builder object for defining a {@code FileLocator}
     */
    public static FileLocatorBuilder fileLocator(FileLocator src)
    {
        return new FileLocatorBuilder(src);
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
    public static URL locate(FileSystem fileSystem, String base, String name)
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
     * Tries to convert the specified base path and file name into a file object.
     * This method is called e.g. by the save() methods of file based
     * configurations. The parameter strings can be relative files, absolute
     * files and URLs as well. This implementation checks first whether the passed in
     * file name is absolute. If this is the case, it is returned. Otherwise
     * further checks are performed whether the base path and file name can be
     * combined to a valid URL or a valid file name. <em>Note:</em> The test
     * if the passed in file name is absolute is performed using
     * {@code java.io.File.isAbsolute()}. If the file name starts with a
     * slash, this method will return <b>true</b> on Unix, but <b>false</b> on
     * Windows. So to ensure correct behavior for relative file names on all
     * platforms you should never let relative paths start with a slash. E.g.
     * in a configuration definition file do not use something like that:
     * <pre>
     * &lt;properties fileName="/subdir/my.properties"/&gt;
     * </pre>
     * Under Windows this path would be resolved relative to the configuration
     * definition file. Under Unix this would be treated as an absolute path
     * name.
     *
     * @param basePath the base path
     * @param fileName the file name
     * @return the file object (<b>null</b> if no file can be obtained)
     */
    static File getFile(String basePath, String fileName)
    {
        // Check if the file name is absolute
        File f = new File(fileName);
        if (f.isAbsolute())
        {
            return f;
        }

        // Check if URLs are involved
        URL url;
        try
        {
            url = new URL(new URL(basePath), fileName);
        }
        catch (MalformedURLException mex1)
        {
            try
            {
                url = new URL(fileName);
            }
            catch (MalformedURLException mex2)
            {
                url = null;
            }
        }

        if (url != null)
        {
            return fileFromURL(url);
        }

        return constructFile(basePath, fileName);
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

    /**
     * A typical <em>builder</em> implementation for creating
     * {@code FileLocator} objects. An instance of this class is returned by the
     * {@code fileLocator()} method of {@code FileLocatorUtils}. It can be used
     * to define the various components of the {@code FileLocator} object. By
     * calling {@code create()} the new immutable {@code FileLocator} instance
     * is created.
     */
    public static final class FileLocatorBuilder
    {
        /** The file name. */
        private String fileName;

        /** The base path. */
        private String basePath;

        /** The source URL. */
        private URL sourceURL;

        /** The encoding. */
        private String encoding;

        /** The file system. */
        private FileSystem fileSystem;

        /**
         * Creates a new instance of {@code FileLocatorBuilder} and initializes
         * the builder's properties from the passed in {@code FileLocator}
         * object.
         *
         * @param src the source {@code FileLocator} (may be <b>null</b>)
         */
        private FileLocatorBuilder(FileLocator src)
        {
            if (src != null)
            {
                initBuilder(src);
            }
        }

        /**
         * Specifies the encoding of the new {@code FileLocator}.
         *
         * @param enc the encoding
         * @return a reference to this builder for method chaining
         */
        public FileLocatorBuilder encoding(String enc)
        {
            encoding = enc;
            return this;
        }

        /**
         * Specifies the {@code FileSystem} of the new {@code FileLocator}.
         *
         * @param fs the {@code FileSystem}
         * @return a reference to this builder for method chaining
         */
        public FileLocatorBuilder fileSystem(FileSystem fs)
        {
            fileSystem = fs;
            return this;
        }

        /**
         * Specifies the base path of the new {@code FileLocator}.
         *
         * @param path the base path
         * @return a reference to this builder for method chaining
         */
        public FileLocatorBuilder basePath(String path)
        {
            basePath = path;
            return this;
        }

        /**
         * Specifies the file name of the new {@code FileLocator}.
         *
         * @param name the file name
         * @return a reference to this builder for method chaining
         */
        public FileLocatorBuilder fileName(String name)
        {
            fileName = name;
            return this;
        }

        /**
         * Specifies the source URL of the new {@code FileLocator}.
         *
         * @param url the source URL
         * @return a reference to this builder for method chaining
         */
        public FileLocatorBuilder sourceURL(URL url)
        {
            sourceURL = url;
            return this;
        }

        /**
         * Creates a new immutable {@code FileLocator} object based on the
         * properties set so far for this builder.
         *
         * @return the newly created {@code FileLocator} object
         */
        public FileLocator create()
        {
            return new FileLocatorImpl(this);
        }

        /**
         * Initializes the properties of this builder from the passed in locator
         * object.
         *
         * @param src the source {@code FileLocator}
         */
        private void initBuilder(FileLocator src)
        {
            basePath = src.getBasePath();
            fileName = src.getFileName();
            sourceURL = src.getSourceURL();
            encoding = src.getEncoding();
            fileSystem = src.getFileSystem();
        }
    }

    /**
     * A straight-forward immutable implementation of {@code FileLocator}.
     */
    private static class FileLocatorImpl implements FileLocator
    {
        /** The file name. */
        private final String fileName;

        /** The base path. */
        private final String basePath;

        /** The source URL. */
        private final URL sourceURL;

        /** The encoding. */
        private final String encoding;

        /** The file system. */
        private final FileSystem fileSystem;

        /**
         * Creates a new instance of {@code FileLocatorImpl} and initializes it
         * from the given builder instance
         *
         * @param builder the builder
         */
        public FileLocatorImpl(FileLocatorBuilder builder)
        {
            fileName = builder.fileName;
            basePath = builder.basePath;
            sourceURL = builder.sourceURL;
            encoding = builder.encoding;
            fileSystem = builder.fileSystem;
        }

        public String getFileName()
        {
            return fileName;
        }

        public String getBasePath()
        {
            return basePath;
        }

        public URL getSourceURL()
        {
            return sourceURL;
        }

        public String getEncoding()
        {
            return encoding;
        }

        public FileSystem getFileSystem()
        {
            return fileSystem;
        }

        /**
         * Returns a hash code for this object.
         *
         * @return a hash code for this object
         */
        @Override
        public int hashCode()
        {
            return new HashCodeBuilder().append(getFileName())
                    .append(getBasePath()).append(sourceURLAsString())
                    .append(getEncoding()).append(getFileSystem()).toHashCode();
        }

        /**
         * Compares this object with another one. Two instances of
         * {@code FileLocatorImpl} are considered equal if all of their
         * properties are equal.
         *
         * @param obj the object to compare to
         * @return a flag whether these objects are equal
         */
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (!(obj instanceof FileLocatorImpl))
            {
                return false;
            }

            FileLocatorImpl c = (FileLocatorImpl) obj;
            return new EqualsBuilder().append(getFileName(), c.getFileName())
                    .append(getBasePath(), c.getBasePath())
                    .append(sourceURLAsString(), c.sourceURLAsString())
                    .append(getEncoding(), c.getEncoding())
                    .append(getFileSystem(), c.getFileSystem()).isEquals();
        }

        /**
         * Returns a string representation of this object. This string contains
         * the values of all properties.
         *
         * @return a string for this object
         */
        @Override
        public String toString()
        {
            return new ToStringBuilder(this).append("fileName", getFileName())
                    .append("basePath", getBasePath())
                    .append("sourceURL", sourceURLAsString())
                    .append("encoding", getEncoding())
                    .append("fileSystem", getFileSystem()).toString();
        }

        /**
         * Returns the source URL as a string. Result is never null. Comparisons
         * are done on this string to avoid blocking network calls.
         *
         * @return the source URL as a string (not null)
         */
        private String sourceURLAsString()
        {
            return (sourceURL != null) ? sourceURL.toExternalForm()
                    : StringUtils.EMPTY;
        }
    }
}
