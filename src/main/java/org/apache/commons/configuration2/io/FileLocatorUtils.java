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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
 * @since 2.0
 */
public final class FileLocatorUtils
{
    /**
     * Constant for the default {@code FileSystem}. This file system is used by
     * operations of this class if no specific file system is provided. An
     * instance of {@link DefaultFileSystem} is used.
     */
    public static final FileSystem DEFAULT_FILE_SYSTEM =
            new DefaultFileSystem();

    /**
     * Constant for the default {@code FileLocationStrategy}. This strategy is
     * used by the {@code locate()} method if the passed in {@code FileLocator}
     * does not define its own location strategy. The default location strategy
     * is roughly equivalent to the search algorithm used in version 1.x of
     * <em>Commons Configuration</em> (there it was hard-coded though). It
     * behaves in the following way when passed a {@code FileLocator}:
     * <ul>
     * <li>If the {@code FileLocator} has a defined URL, this URL is used as the
     * file's URL (without any further checks).</li>
     * <li>Otherwise, base path and file name stored in the {@code FileLocator}
     * are passed to the current {@code FileSystem}'s {@code locateFromURL()}
     * method. If this results in a URL, it is returned.</li>
     * <li>Otherwise, if the locator's file name is an absolute path to an
     * existing file, the URL of this file is returned.</li>
     * <li>Otherwise, the concatenation of base path and file name is
     * constructed. If this path points to an existing file, its URL is
     * returned.</li>
     * <li>Otherwise, a sub directory of the current user's home directory as
     * defined by the base path is searched for the referenced file. If the file
     * can be found there, its URL is returned.</li>
     * <li>Otherwise, the base path is ignored, and the file name is searched in
     * the current user's home directory. If the file can be found there, its
     * URL is returned.</li>
     * <li>Otherwise, a resource with the name of the locator's file name is
     * searched in the classpath. If it can be found, its URL is returned.</li>
     * <li>Otherwise, the strategy gives up and returns <b>null</b> indicating
     * that the file cannot be resolved.</li>
     * </ul>
     */
    public static final FileLocationStrategy DEFAULT_LOCATION_STRATEGY =
            initDefaultLocationStrategy();

    /** Constant for the file URL protocol */
    private static final String FILE_SCHEME = "file:";

    /** The logger.*/
    private static final Log LOG = LogFactory.getLog(FileLocatorUtils.class);

    /** Property key for the base path. */
    private static final String PROP_BASE_PATH = "basePath";

    /** Property key for the encoding. */
    private static final String PROP_ENCODING = "encoding";

    /** Property key for the file name. */
    private static final String PROP_FILE_NAME = "fileName";

    /** Property key for the file system. */
    private static final String PROP_FILE_SYSTEM = "fileSystem";

    /** Property key for the location strategy. */
    private static final String PROP_STRATEGY = "locationStrategy";

    /** Property key for the source URL. */
    private static final String PROP_SOURCE_URL = "sourceURL";

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
    public static File fileFromURL(final URL url)
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
    public static FileLocator.FileLocatorBuilder fileLocator()
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
    public static FileLocator.FileLocatorBuilder fileLocator(final FileLocator src)
    {
        return new FileLocator.FileLocatorBuilder(src);
    }

    /**
     * Creates a new {@code FileLocator} object with the properties defined in
     * the given map. The map must be conform to the structure generated by the
     * {@link #put(FileLocator, Map)} method; unexpected data can cause
     * {@code ClassCastException} exceptions. The map can be <b>null</b>, then
     * an uninitialized {@code FileLocator} is returned.
     *
     * @param map the map
     * @return the new {@code FileLocator}
     * @throws ClassCastException if the map contains invalid data
     */
    public static FileLocator fromMap(final Map<String, ?> map)
    {
        final FileLocator.FileLocatorBuilder builder = fileLocator();
        if (map != null)
        {
            builder.basePath((String) map.get(PROP_BASE_PATH))
                    .encoding((String) map.get(PROP_ENCODING))
                    .fileName((String) map.get(PROP_FILE_NAME))
                    .fileSystem((FileSystem) map.get(PROP_FILE_SYSTEM))
                    .locationStrategy(
                            (FileLocationStrategy) map.get(PROP_STRATEGY))
                    .sourceURL((URL) map.get(PROP_SOURCE_URL));
        }
        return builder.create();
    }

    /**
     * Stores the specified {@code FileLocator} in the given map. With the
     * {@link #fromMap(Map)} method a new {@code FileLocator} with the same
     * properties as the original one can be created.
     *
     * @param locator the {@code FileLocator} to be stored
     * @param map the map in which to store the {@code FileLocator} (must not be
     *        <b>null</b>)
     * @throws IllegalArgumentException if the map is <b>null</b>
     */
    public static void put(final FileLocator locator, final Map<String, Object> map)
    {
        if (map == null)
        {
            throw new IllegalArgumentException("Map must not be null!");
        }

        if (locator != null)
        {
            map.put(PROP_BASE_PATH, locator.getBasePath());
            map.put(PROP_ENCODING, locator.getEncoding());
            map.put(PROP_FILE_NAME, locator.getFileName());
            map.put(PROP_FILE_SYSTEM, locator.getFileSystem());
            map.put(PROP_SOURCE_URL, locator.getSourceURL());
            map.put(PROP_STRATEGY, locator.getLocationStrategy());
        }
    }

    /**
     * Checks whether the specified {@code FileLocator} contains enough
     * information to locate a file. This is the case if a file name or a URL is
     * defined. If the passed in {@code FileLocator} is <b>null</b>, result is
     * <b>false</b>.
     *
     * @param locator the {@code FileLocator} to check
     * @return a flag whether a file location is defined by this
     *         {@code FileLocator}
     */
    public static boolean isLocationDefined(final FileLocator locator)
    {
        return (locator != null)
                && (locator.getFileName() != null || locator.getSourceURL() != null);
    }

    /**
     * Returns a flag whether all components of the given {@code FileLocator}
     * describing the referenced file are defined. In order to reference a file,
     * it is not necessary that all components are filled in (for instance, the
     * URL alone is sufficient). For some use cases however, it might be of
     * interest to have different methods for accessing the referenced file.
     * Also, depending on the filled out properties, there is a subtle
     * difference how the file is accessed: If only the file name is set (and
     * optionally the base path), each time the file is accessed a
     * {@code locate()} operation has to be performed to uniquely identify the
     * file. If however the URL is determined once based on the other components
     * and stored in a fully defined {@code FileLocator}, it can be used
     * directly to identify the file. If the passed in {@code FileLocator} is
     * <b>null</b>, result is <b>false</b>.
     *
     * @param locator the {@code FileLocator} to be checked (may be <b>null</b>)
     * @return a flag whether all components describing the referenced file are
     *         initialized
     */
    public static boolean isFullyInitialized(final FileLocator locator)
    {
        if (locator == null)
        {
            return false;
        }
        return locator.getBasePath() != null && locator.getFileName() != null
                && locator.getSourceURL() != null;
    }

    /**
     * Returns a {@code FileLocator} object based on the passed in one whose
     * location is fully defined. This method ensures that all components of the
     * {@code FileLocator} pointing to the file are set in a consistent way. In
     * detail it behaves as follows:
     * <ul>
     * <li>If the {@code FileLocator} has already all components set which
     * define the file, it is returned unchanged. <em>Note:</em> It is not
     * checked whether all components are really consistent!</li>
     * <li>{@link #locate(FileLocator)} is called to determine a unique URL
     * pointing to the referenced file. If this is successful, a new
     * {@code FileLocator} is created as a copy of the passed in one, but with
     * all components pointing to the file derived from this URL.</li>
     * <li>Otherwise, result is <b>null</b>.</li>
     * </ul>
     *
     * @param locator the {@code FileLocator} to be completed
     * @return a {@code FileLocator} with a fully initialized location if
     *         possible or <b>null</b>
     */
    public static FileLocator fullyInitializedLocator(final FileLocator locator)
    {
        if (isFullyInitialized(locator))
        {
            // already fully initialized
            return locator;
        }

        final URL url = locate(locator);
        return (url != null) ? createFullyInitializedLocatorFromURL(locator,
                url) : null;
    }

    /**
     * Locates the provided {@code FileLocator}, returning a URL for accessing
     * the referenced file. This method uses a {@link FileLocationStrategy} to
     * locate the file the passed in {@code FileLocator} points to. If the
     * {@code FileLocator} contains itself a {@code FileLocationStrategy}, it is
     * used. Otherwise, the default {@code FileLocationStrategy} is applied. The
     * strategy is passed the locator and a {@code FileSystem}. The resulting
     * URL is returned. If the {@code FileLocator} is <b>null</b>, result is
     * <b>null</b>.
     *
     * @param locator the {@code FileLocator} to be resolved
     * @return the URL pointing to the referenced file or <b>null</b> if the
     *         {@code FileLocator} could not be resolved
     * @see #DEFAULT_LOCATION_STRATEGY
     */
    public static URL locate(final FileLocator locator)
    {
        if (locator == null)
        {
            return null;
        }

        return obtainLocationStrategy(locator).locate(
                obtainFileSystem(locator), locator);
    }

    /**
     * Tries to locate the file referenced by the passed in {@code FileLocator}.
     * If this fails, an exception is thrown. This method works like
     * {@link #locate(FileLocator)}; however, in case of a failed location
     * attempt an exception is thrown.
     *
     * @param locator the {@code FileLocator} to be resolved
     * @return the URL pointing to the referenced file
     * @throws ConfigurationException if the file cannot be resolved
     */
    public static URL locateOrThrow(final FileLocator locator)
            throws ConfigurationException
    {
        final URL url = locate(locator);
        if (url == null)
        {
            throw new ConfigurationException("Could not locate: " + locator);
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
    static String getBasePath(final URL url)
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
        return s.substring(0, s.lastIndexOf("/") + 1);
    }

    /**
     * Extract the file name from the specified URL.
     *
     * @param url the URL from which to extract the file name
     * @return the extracted file name
     */
    static String getFileName(final URL url)
    {
        if (url == null)
        {
            return null;
        }

        final String path = url.getPath();

        if (path.endsWith("/") || StringUtils.isEmpty(path))
        {
            return null;
        }
        return path.substring(path.lastIndexOf("/") + 1);
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
     * @param fileName the file name (must not be <b>null</b>)
     * @return the file object (<b>null</b> if no file can be obtained)
     */
    static File getFile(final String basePath, final String fileName)
    {
        // Check if the file name is absolute
        final File f = new File(fileName);
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
        catch (final MalformedURLException mex1)
        {
            try
            {
                url = new URL(fileName);
            }
            catch (final MalformedURLException mex2)
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
    static URL toURL(final File file) throws MalformedURLException
    {
        return file.toURI().toURL();
    }

    /**
     * Tries to convert the specified URI to a URL. If this causes an exception,
     * result is <b>null</b>.
     *
     * @param uri the URI to be converted
     * @return the resulting URL or <b>null</b>
     */
    static URL convertURIToURL(final URI uri)
    {
        try
        {
            return uri.toURL();
        }
        catch (final MalformedURLException e)
        {
            return null;
        }
    }

    /**
     * Tries to convert the specified file to a URL. If this causes an
     * exception, result is <b>null</b>.
     *
     * @param file the file to be converted
     * @return the resulting URL or <b>null</b>
     */
    static URL convertFileToURL(final File file)
    {
        return convertURIToURL(file.toURI());
    }

    /**
     * Tries to find a resource with the given name in the classpath.
     *
     * @param resourceName the name of the resource
     * @return the URL to the found resource or <b>null</b> if the resource
     *         cannot be found
     */
    static URL locateFromClasspath(final String resourceName)
    {
        URL url = null;
        // attempt to load from the context classpath
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
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
     * @param fileName the file name (must not be <b>null</b>)
     * @return the resulting file
     */
    static File constructFile(final String basePath, final String fileName)
    {
        File file;

        final File absolute = new File(fileName);
        if (StringUtils.isEmpty(basePath) || absolute.isAbsolute())
        {
            file = absolute;
        }
        else
        {
            file = new File(appendPath(basePath, fileName));
        }

        return file;
    }

    /**
     * Extends a path by another component. The given extension is added to the
     * already existing path adding a separator if necessary.
     *
     * @param path the path to be extended
     * @param ext the extension of the path
     * @return the extended path
     */
    static String appendPath(final String path, final String ext)
    {
        final StringBuilder fName = new StringBuilder();
        fName.append(path);

        // My best friend. Paranoia.
        if (!path.endsWith(File.separator))
        {
            fName.append(File.separator);
        }

        //
        // We have a relative path, and we have
        // two possible forms here. If we have the
        // "./" form then just strip that off first
        // before continuing.
        //
        if (ext.startsWith("." + File.separator))
        {
            fName.append(ext.substring(2));
        }
        else
        {
            fName.append(ext);
        }
        return fName.toString();
    }

    /**
     * Obtains a non-<b>null</b> {@code FileSystem} object from the passed in
     * {@code FileLocator}. If the passed in {@code FileLocator} has a
     * {@code FileSystem} object, it is returned. Otherwise, result is the
     * default {@code FileSystem}.
     *
     * @param locator the {@code FileLocator} (may be <b>null</b>)
     * @return the {@code FileSystem} to be used for this {@code FileLocator}
     */
    static FileSystem obtainFileSystem(final FileLocator locator)
    {
        return (locator != null) ? ObjectUtils.defaultIfNull(
                locator.getFileSystem(), DEFAULT_FILE_SYSTEM)
                : DEFAULT_FILE_SYSTEM;
    }

    /**
     * Obtains a non <b>null</b> {@code FileLocationStrategy} object from the
     * passed in {@code FileLocator}. If the {@code FileLocator} is not
     * <b>null</b> and has a {@code FileLocationStrategy} defined, this strategy
     * is returned. Otherwise, result is the default
     * {@code FileLocationStrategy}.
     *
     * @param locator the {@code FileLocator}
     * @return the {@code FileLocationStrategy} for this {@code FileLocator}
     */
    static FileLocationStrategy obtainLocationStrategy(final FileLocator locator)
    {
        return (locator != null) ? ObjectUtils.defaultIfNull(
                locator.getLocationStrategy(), DEFAULT_LOCATION_STRATEGY)
                : DEFAULT_LOCATION_STRATEGY;
    }

    /**
     * Creates a fully initialized {@code FileLocator} based on the specified
     * URL.
     *
     * @param src the source {@code FileLocator}
     * @param url the URL
     * @return the fully initialized {@code FileLocator}
     */
    private static FileLocator createFullyInitializedLocatorFromURL(final FileLocator src,
            final URL url)
    {
        final FileLocator.FileLocatorBuilder fileLocatorBuilder = fileLocator(src);
        if (src.getSourceURL() == null)
        {
            fileLocatorBuilder.sourceURL(url);
        }
        if (StringUtils.isBlank(src.getFileName()))
        {
            fileLocatorBuilder.fileName(getFileName(url));
        }
        if (StringUtils.isBlank(src.getBasePath()))
        {
            fileLocatorBuilder.basePath(getBasePath(url));
        }
        return fileLocatorBuilder.create();
    }

    /**
     * Creates the default location strategy. This method creates a combined
     * location strategy as described in the comment of the
     * {@link #DEFAULT_LOCATION_STRATEGY} member field.
     *
     * @return the default {@code FileLocationStrategy}
     */
    private static FileLocationStrategy initDefaultLocationStrategy()
    {
        final FileLocationStrategy[] subStrategies =
                new FileLocationStrategy[] {
                        new ProvidedURLLocationStrategy(),
                        new FileSystemLocationStrategy(),
                        new AbsoluteNameLocationStrategy(),
                        new BasePathLocationStrategy(),
                        new HomeDirectoryLocationStrategy(true),
                        new HomeDirectoryLocationStrategy(false),
                        new ClasspathLocationStrategy()
                };
        return new CombinedLocationStrategy(Arrays.asList(subStrategies));
    }
}
