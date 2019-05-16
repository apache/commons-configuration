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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileLocator.FileLocatorBuilder;
import org.apache.commons.configuration2.sync.LockMode;
import org.apache.commons.configuration2.sync.NoOpSynchronizer;
import org.apache.commons.configuration2.sync.Synchronizer;
import org.apache.commons.configuration2.sync.SynchronizerSupport;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * A class that manages persistence of an associated {@link FileBased} object.
 * </p>
 * <p>
 * Instances of this class can be used to load and save arbitrary objects
 * implementing the {@code FileBased} interface in a convenient way from and to
 * various locations. At construction time the {@code FileBased} object to
 * manage is passed in. Basically, this object is assigned a location from which
 * it is loaded and to which it can be saved. The following possibilities exist
 * to specify such a location:
 * </p>
 * <ul>
 * <li>URLs: With the method {@code setURL()} a full URL to the configuration
 * source can be specified. This is the most flexible way. Note that the
 * {@code save()} methods support only <em>file:</em> URLs.</li>
 * <li>Files: The {@code setFile()} method allows to specify the configuration
 * source as a file. This can be either a relative or an absolute file. In the
 * former case the file is resolved based on the current directory.</li>
 * <li>As file paths in string form: With the {@code setPath()} method a full
 * path to a configuration file can be provided as a string.</li>
 * <li>Separated as base path and file name: The base path is a string defining
 * either a local directory or a URL. It can be set using the
 * {@code setBasePath()} method. The file name, non surprisingly, defines the
 * name of the configuration file.</li>
 * </ul>
 * <p>
 * An instance stores a location. The {@code load()} and {@code save()} methods
 * that do not take an argument make use of this internal location.
 * Alternatively, it is also possible to use overloaded variants of
 * {@code load()} and {@code save()} which expect a location. In these cases the
 * location specified takes precedence over the internal one; the internal
 * location is not changed.
 * </p>
 * <p>
 * The actual position of the file to be loaded is determined by a
 * {@link FileLocationStrategy} based on the location information that has been
 * provided. By providing a custom location strategy the algorithm for searching
 * files can be adapted. Save operations require more explicit information. They
 * cannot rely on a location strategy because the file to be written may not yet
 * exist. So there may be some differences in the way location information is
 * interpreted by load and save operations. In order to avoid this, the
 * following approach is recommended:
 * </p>
 * <ul>
 * <li>Use the desired {@code setXXX()} methods to define the location of the
 * file to be loaded.</li>
 * <li>Call the {@code locate()} method. This method resolves the referenced
 * file (if possible) and fills out all supported location information.</li>
 * <li>Later on, {@code save()} can be called. This method now has sufficient
 * information to store the file at the correct location.</li>
 * </ul>
 * <p>
 * When loading or saving a {@code FileBased} object some additional
 * functionality is performed if the object implements one of the following
 * interfaces:
 * </p>
 * <ul>
 * <li>{@code FileLocatorAware}: In this case an object with the current file
 * location is injected before the load or save operation is executed. This is
 * useful for {@code FileBased} objects that depend on their current location,
 * e.g. to resolve relative path names.</li>
 * <li>{@code SynchronizerSupport}: If this interface is implemented, load and
 * save operations obtain a write lock on the {@code FileBased} object before
 * they access it. (In case of a save operation, a read lock would probably be
 * sufficient, but because of the possible injection of a {@link FileLocator}
 * object it is not allowed to perform multiple save operations in parallel;
 * therefore, by obtaining a write lock, we are on the safe side.)</li>
 * </ul>
 * <p>
 * This class is thread-safe.
 * </p>
 *
 * @since 2.0
 */
public class FileHandler
{
    /** Constant for the URI scheme for files. */
    private static final String FILE_SCHEME = "file:";

    /** Constant for the URI scheme for files with slashes. */
    private static final String FILE_SCHEME_SLASH = FILE_SCHEME + "//";

    /**
     * A dummy implementation of {@code SynchronizerSupport}. This object is
     * used when the file handler's content does not implement the
     * {@code SynchronizerSupport} interface. All methods are just empty dummy
     * implementations.
     */
    private static final SynchronizerSupport DUMMY_SYNC_SUPPORT =
            new SynchronizerSupport()
            {
                @Override
                public void unlock(final LockMode mode)
                {
                }

                @Override
                public void setSynchronizer(final Synchronizer sync)
                {
                }

                @Override
                public void lock(final LockMode mode)
                {
                }

                @Override
                public Synchronizer getSynchronizer()
                {
                    return NoOpSynchronizer.INSTANCE;
                }
            };

    /** The file-based object managed by this handler. */
    private final FileBased content;

    /** A reference to the current {@code FileLocator} object. */
    private final AtomicReference<FileLocator> fileLocator;

    /** A collection with the registered listeners. */
    private final List<FileHandlerListener> listeners =
            new CopyOnWriteArrayList<>();

    /**
     * Creates a new instance of {@code FileHandler} which is not associated
     * with a {@code FileBased} object and thus does not have a content. Objects
     * of this kind can be used to define a file location, but it is not
     * possible to actually load or save data.
     */
    public FileHandler()
    {
        this(null);
    }

    /**
     * Creates a new instance of {@code FileHandler} and sets the managed
     * {@code FileBased} object.
     *
     * @param obj the file-based object to manage
     */
    public FileHandler(final FileBased obj)
    {
        this(obj, emptyFileLocator());
    }

    /**
     * Creates a new instance of {@code FileHandler} which is associated with
     * the given {@code FileBased} object and the location defined for the given
     * {@code FileHandler} object. A copy of the location of the given
     * {@code FileHandler} is created. This constructor is a possibility to
     * associate a file location with a {@code FileBased} object.
     *
     * @param obj the {@code FileBased} object to manage
     * @param c the {@code FileHandler} from which to copy the location (must
     *        not be <b>null</b>)
     * @throws IllegalArgumentException if the {@code FileHandler} is
     *         <b>null</b>
     */
    public FileHandler(final FileBased obj, final FileHandler c)
    {
        this(obj, checkSourceHandler(c).getFileLocator());
    }

    /**
     * Creates a new instance of {@code FileHandler} based on the given
     * {@code FileBased} and {@code FileLocator} objects.
     *
     * @param obj the {@code FileBased} object to manage
     * @param locator the {@code FileLocator}
     */
    private FileHandler(final FileBased obj, final FileLocator locator)
    {
        content = obj;
        fileLocator = new AtomicReference<>(locator);
    }

    /**
     * Creates a new {@code FileHandler} instance from properties stored in a
     * map. This method tries to extract a {@link FileLocator} from the map. A
     * new {@code FileHandler} is created based on this {@code FileLocator}.
     *
     * @param map the map (may be <b>null</b>)
     * @return the newly created {@code FileHandler}
     * @see FileLocatorUtils#fromMap(Map)
     */
    public static FileHandler fromMap(final Map<String, ?> map)
    {
        return new FileHandler(null, FileLocatorUtils.fromMap(map));
    }

    /**
     * Returns the {@code FileBased} object associated with this
     * {@code FileHandler}.
     *
     * @return the associated {@code FileBased} object
     */
    public final FileBased getContent()
    {
        return content;
    }

    /**
     * Adds a listener to this {@code FileHandler}. It is notified about
     * property changes and IO operations.
     *
     * @param l the listener to be added (must not be <b>null</b>)
     * @throws IllegalArgumentException if the listener is <b>null</b>
     */
    public void addFileHandlerListener(final FileHandlerListener l)
    {
        if (l == null)
        {
            throw new IllegalArgumentException("Listener must not be null!");
        }
        listeners.add(l);
    }

    /**
     * Removes the specified listener from this object.
     *
     * @param l the listener to be removed
     */
    public void removeFileHandlerListener(final FileHandlerListener l)
    {
        listeners.remove(l);
    }

    /**
     * Return the name of the file. If only a URL is defined, the file name
     * is derived from there.
     *
     * @return the file name
     */
    public String getFileName()
    {
        final FileLocator locator = getFileLocator();
        if (locator.getFileName() != null)
        {
            return locator.getFileName();
        }

        if (locator.getSourceURL() != null)
        {
            return FileLocatorUtils.getFileName(locator.getSourceURL());
        }

        return null;
    }

    /**
     * Set the name of the file. The passed in file name can contain a relative
     * path. It must be used when referring files with relative paths from
     * classpath. Use {@code setPath()} to set a full qualified file name. The
     * URL is set to <b>null</b> as it has to be determined anew based on the
     * file name and the base path.
     *
     * @param fileName the name of the file
     */
    public void setFileName(final String fileName)
    {
        final String name = normalizeFileURL(fileName);
        new Updater()
        {
            @Override
            protected void updateBuilder(final FileLocatorBuilder builder)
            {
                builder.fileName(name);
                builder.sourceURL(null);
            }
        }
        .update();
    }

    /**
     * Return the base path. If no base path is defined, but a URL, the base
     * path is derived from there.
     *
     * @return the base path
     */
    public String getBasePath()
    {
        final FileLocator locator = getFileLocator();
        if (locator.getBasePath() != null)
        {
            return locator.getBasePath();
        }

        if (locator.getSourceURL() != null)
        {
            return FileLocatorUtils.getBasePath(locator.getSourceURL());
        }

        return null;
    }

    /**
     * Sets the base path. The base path is typically either a path to a
     * directory or a URL. Together with the value passed to the
     * {@code setFileName()} method it defines the location of the configuration
     * file to be loaded. The strategies for locating the file are quite
     * tolerant. For instance if the file name is already an absolute path or a
     * fully defined URL, the base path will be ignored. The base path can also
     * be a URL, in which case the file name is interpreted in this URL's
     * context. If other methods are used for determining the location of the
     * associated file (e.g. {@code setFile()} or {@code setURL()}), the base
     * path is automatically set. Setting the base path using this method
     * automatically sets the URL to <b>null</b> because it has to be
     * determined anew based on the file name and the base path.
     *
     * @param basePath the base path.
     */
    public void setBasePath(final String basePath)
    {
        final String path = normalizeFileURL(basePath);
        new Updater()
        {
            @Override
            protected void updateBuilder(final FileLocatorBuilder builder)
            {
                builder.basePath(path);
                builder.sourceURL(null);
            }
        }
        .update();
    }

    /**
     * Returns the location of the associated file as a {@code File} object. If
     * the base path is a URL with a protocol different than &quot;file&quot;,
     * or the file is within a compressed archive, the return value will not
     * point to a valid file object.
     *
     * @return the location as {@code File} object; this can be <b>null</b>
     */
    public File getFile()
    {
        return createFile(getFileLocator());
    }

    /**
     * Sets the location of the associated file as a {@code File} object. The
     * passed in {@code File} is made absolute if it is not yet. Then the file's
     * path component becomes the base path and its name component becomes the
     * file name.
     *
     * @param file the location of the associated file
     */
    public void setFile(final File file)
    {
        final String fileName = file.getName();
        final String basePath =
                (file.getParentFile() != null) ? file.getParentFile()
                        .getAbsolutePath() : null;
        new Updater()
        {
            @Override
            protected void updateBuilder(final FileLocatorBuilder builder)
            {
                builder.fileName(fileName).basePath(basePath).sourceURL(null);
            }
        }
        .update();
    }

    /**
     * Returns the full path to the associated file. The return value is a valid
     * {@code File} path only if this location is based on a file on the local
     * disk. If the file was loaded from a packed archive, the returned value is
     * the string form of the URL from which the file was loaded.
     *
     * @return the full path to the associated file
     */
    public String getPath()
    {
        final FileLocator locator = getFileLocator();
        final File file = createFile(locator);
        return FileLocatorUtils.obtainFileSystem(locator).getPath(file,
                locator.getSourceURL(), locator.getBasePath(), locator.getFileName());
    }

    /**
     * Sets the location of the associated file as a full or relative path name.
     * The passed in path should represent a valid file name on the file system.
     * It must not be used to specify relative paths for files that exist in
     * classpath, either plain file system or compressed archive, because this
     * method expands any relative path to an absolute one which may end in an
     * invalid absolute path for classpath references.
     *
     * @param path the full path name of the associated file
     */
    public void setPath(final String path)
    {
        setFile(new File(path));
    }

    /**
     * Returns the location of the associated file as a URL. If a URL is set,
     * it is directly returned. Otherwise, an attempt to locate the referenced
     * file is made.
     *
     * @return a URL to the associated file; can be <b>null</b> if the location
     *         is unspecified
     */
    public URL getURL()
    {
        final FileLocator locator = getFileLocator();
        return (locator.getSourceURL() != null) ? locator.getSourceURL()
                : FileLocatorUtils.locate(locator);
    }

    /**
     * Sets the location of the associated file as a URL. For loading this can
     * be an arbitrary URL with a supported protocol. If the file is to be
     * saved, too, a URL with the &quot;file&quot; protocol should be provided.
     * This method sets the file name and the base path to <b>null</b>.
     * They have to be determined anew based on the new URL.
     *
     * @param url the location of the file as URL
     */
    public void setURL(final URL url)
    {
        new Updater()
        {
            @Override
            protected void updateBuilder(final FileLocatorBuilder builder)
            {
                builder.sourceURL(url);
                builder.basePath(null).fileName(null);
            }
        }
        .update();
    }

    /**
     * Returns a {@code FileLocator} object with the specification of the file
     * stored by this {@code FileHandler}. Note that this method returns the
     * internal data managed by this {@code FileHandler} as it was defined.
     * This is not necessarily the same as the data returned by the single
     * access methods like {@code getFileName()} or {@code getURL()}: These
     * methods try to derive missing data from other values that have been set.
     *
     * @return a {@code FileLocator} with the referenced file
     */
    public FileLocator getFileLocator()
    {
        return fileLocator.get();
    }

    /**
     * Sets the file to be accessed by this {@code FileHandler} as a
     * {@code FileLocator} object.
     *
     * @param locator the {@code FileLocator} with the definition of the file to
     *        be accessed (must not be <b>null</b>
     * @throws IllegalArgumentException if the {@code FileLocator} is
     *         <b>null</b>
     */
    public void setFileLocator(final FileLocator locator)
    {
        if (locator == null)
        {
            throw new IllegalArgumentException("FileLocator must not be null!");
        }

        fileLocator.set(locator);
        fireLocationChangedEvent();
    }

    /**
     * Tests whether a location is defined for this {@code FileHandler}.
     *
     * @return <b>true</b> if a location is defined, <b>false</b> otherwise
     */
    public boolean isLocationDefined()
    {
        return FileLocatorUtils.isLocationDefined(getFileLocator());
    }

    /**
     * Clears the location of this {@code FileHandler}. Afterwards this handler
     * does not point to any valid file.
     */
    public void clearLocation()
    {
        new Updater()
        {
            @Override
            protected void updateBuilder(final FileLocatorBuilder builder)
            {
                builder.basePath(null).fileName(null).sourceURL(null);
            }
        }
        .update();
    }

    /**
     * Returns the encoding of the associated file. Result can be <b>null</b> if
     * no encoding has been set.
     *
     * @return the encoding of the associated file
     */
    public String getEncoding()
    {
        return getFileLocator().getEncoding();
    }

    /**
     * Sets the encoding of the associated file. The encoding applies if binary
     * files are loaded. Note that in this case setting an encoding is
     * recommended; otherwise the platform's default encoding is used.
     *
     * @param encoding the encoding of the associated file
     */
    public void setEncoding(final String encoding)
    {
        new Updater()
        {
            @Override
            protected void updateBuilder(final FileLocatorBuilder builder)
            {
                builder.encoding(encoding);
            }
        }
        .update();
    }

    /**
     * Returns the {@code FileSystem} to be used by this object when locating
     * files. Result is never <b>null</b>; if no file system has been set, the
     * default file system is returned.
     *
     * @return the used {@code FileSystem}
     */
    public FileSystem getFileSystem()
    {
        return FileLocatorUtils.obtainFileSystem(getFileLocator());
    }

    /**
     * Sets the {@code FileSystem} to be used by this object when locating
     * files. If a <b>null</b> value is passed in, the file system is reset to
     * the default file system.
     *
     * @param fileSystem the {@code FileSystem}
     */
    public void setFileSystem(final FileSystem fileSystem)
    {
        new Updater()
        {
            @Override
            protected void updateBuilder(final FileLocatorBuilder builder)
            {
                builder.fileSystem(fileSystem);
            }
        }
        .update();
    }

    /**
     * Resets the {@code FileSystem} used by this object. It is set to the
     * default file system.
     */
    public void resetFileSystem()
    {
        setFileSystem(null);
    }

    /**
     * Returns the {@code FileLocationStrategy} to be applied when accessing the
     * associated file. This method never returns <b>null</b>. If a
     * {@code FileLocationStrategy} has been set, it is returned. Otherwise,
     * result is the default {@code FileLocationStrategy}.
     *
     * @return the {@code FileLocationStrategy} to be used
     */
    public FileLocationStrategy getLocationStrategy()
    {
        return FileLocatorUtils.obtainLocationStrategy(getFileLocator());
    }

    /**
     * Sets the {@code FileLocationStrategy} to be applied when accessing the
     * associated file. The strategy is stored in the underlying
     * {@link FileLocator}. The argument can be <b>null</b>; this causes the
     * default {@code FileLocationStrategy} to be used.
     *
     * @param strategy the {@code FileLocationStrategy}
     * @see FileLocatorUtils#DEFAULT_LOCATION_STRATEGY
     */
    public void setLocationStrategy(final FileLocationStrategy strategy)
    {
        new Updater()
        {
            @Override
            protected void updateBuilder(final FileLocatorBuilder builder)
            {
                builder.locationStrategy(strategy);
            }

        }
        .update();
    }

    /**
     * Locates the referenced file if necessary and ensures that the associated
     * {@link FileLocator} is fully initialized. When accessing the referenced
     * file the information stored in the associated {@code FileLocator} is
     * used. If this information is incomplete (e.g. only the file name is set),
     * an attempt to locate the file may have to be performed on each access. By
     * calling this method such an attempt is performed once, and the results of
     * a successful localization are stored. Hence, later access to the
     * referenced file can be more efficient. Also, all properties pointing to
     * the referenced file in this object's {@code FileLocator} are set (i.e.
     * the URL, the base path, and the file name). If the referenced file cannot
     * be located, result is <b>false</b>. This means that the information in
     * the current {@code FileLocator} is insufficient or wrong. If the
     * {@code FileLocator} is already fully defined, it is not changed.
     *
     * @return a flag whether the referenced file could be located successfully
     * @see FileLocatorUtils#fullyInitializedLocator(FileLocator)
     */
    public boolean locate()
    {
        boolean result;
        boolean done;

        do
        {
            final FileLocator locator = getFileLocator();
            FileLocator fullLocator =
                    FileLocatorUtils.fullyInitializedLocator(locator);
            if (fullLocator == null)
            {
                result = false;
                fullLocator = locator;
            }
            else
            {
                result =
                        fullLocator != locator
                                || FileLocatorUtils.isFullyInitialized(locator);
            }
            done = fileLocator.compareAndSet(locator, fullLocator);
        } while (!done);

        return result;
    }

    /**
     * Loads the associated file from the underlying location. If no location
     * has been set, an exception is thrown.
     *
     * @throws ConfigurationException if loading of the configuration fails
     */
    public void load() throws ConfigurationException
    {
        load(checkContentAndGetLocator());
    }

    /**
     * Loads the associated file from the given file name. The file name is
     * interpreted in the context of the already set location (e.g. if it is a
     * relative file name, a base path is applied if available). The underlying
     * location is not changed.
     *
     * @param fileName the name of the file to be loaded
     * @throws ConfigurationException if an error occurs
     */
    public void load(final String fileName) throws ConfigurationException
    {
        load(fileName, checkContentAndGetLocator());
    }

    /**
     * Loads the associated file from the specified {@code File}.
     *
     * @param file the file to load
     * @throws ConfigurationException if an error occurs
     */
    public void load(final File file) throws ConfigurationException
    {
        URL url;
        try
        {
            url = FileLocatorUtils.toURL(file);
        }
        catch (final MalformedURLException e1)
        {
            throw new ConfigurationException("Cannot create URL from file "
                    + file);
        }

        load(url);
    }

    /**
     * Loads the associated file from the specified URL. The location stored in
     * this object is not changed.
     *
     * @param url the URL of the file to be loaded
     * @throws ConfigurationException if an error occurs
     */
    public void load(final URL url) throws ConfigurationException
    {
        load(url, checkContentAndGetLocator());
    }

    /**
     * Loads the associated file from the specified stream, using the encoding
     * returned by {@link #getEncoding()}.
     *
     * @param in the input stream
     * @throws ConfigurationException if an error occurs during the load
     *         operation
     */
    public void load(final InputStream in) throws ConfigurationException
    {
        load(in, checkContentAndGetLocator());
    }

    /**
     * Loads the associated file from the specified stream, using the specified
     * encoding. If the encoding is <b>null</b>, the default encoding is used.
     *
     * @param in the input stream
     * @param encoding the encoding used, {@code null} to use the default
     *        encoding
     * @throws ConfigurationException if an error occurs during the load
     *         operation
     */
    public void load(final InputStream in, final String encoding)
            throws ConfigurationException
    {
        loadFromStream(in, encoding, null);
    }

    /**
     * Loads the associated file from the specified reader.
     *
     * @param in the reader
     * @throws ConfigurationException if an error occurs during the load
     *         operation
     */
    public void load(final Reader in) throws ConfigurationException
    {
        checkContent();
        injectNullFileLocator();
        loadFromReader(in);
    }

    /**
     * Saves the associated file to the current location set for this object.
     * Before this method can be called a valid location must have been set.
     *
     * @throws ConfigurationException if an error occurs or no location has been
     *         set yet
     */
    public void save() throws ConfigurationException
    {
        save(checkContentAndGetLocator());
    }

    /**
     * Saves the associated file to the specified file name. This does not
     * change the location of this object (use {@link #setFileName(String)} if
     * you need it).
     *
     * @param fileName the file name
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    public void save(final String fileName) throws ConfigurationException
    {
        save(fileName, checkContentAndGetLocator());
    }

    /**
     * Saves the associated file to the specified URL. This does not change the
     * location of this object (use {@link #setURL(URL)} if you need it).
     *
     * @param url the URL
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    public void save(final URL url) throws ConfigurationException
    {
        save(url, checkContentAndGetLocator());
    }

    /**
     * Saves the associated file to the specified {@code File}. The file is
     * created automatically if it doesn't exist. This does not change the
     * location of this object (use {@link #setFile} if you need it).
     *
     * @param file the target file
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    public void save(final File file) throws ConfigurationException
    {
        save(file, checkContentAndGetLocator());
    }

    /**
     * Saves the associated file to the specified stream using the encoding
     * returned by {@link #getEncoding()}.
     *
     * @param out the output stream
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    public void save(final OutputStream out) throws ConfigurationException
    {
        save(out, checkContentAndGetLocator());
    }

    /**
     * Saves the associated file to the specified stream using the specified
     * encoding. If the encoding is <b>null</b>, the default encoding is used.
     *
     * @param out the output stream
     * @param encoding the encoding to be used, {@code null} to use the default
     *        encoding
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    public void save(final OutputStream out, final String encoding)
            throws ConfigurationException
    {
        saveToStream(out, encoding, null);
    }

    /**
     * Saves the associated file to the given {@code Writer}.
     *
     * @param out the {@code Writer}
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    public void save(final Writer out) throws ConfigurationException
    {
        checkContent();
        injectNullFileLocator();
        saveToWriter(out);
    }

    /**
     * Prepares a builder for a {@code FileLocator} which does not have a
     * defined file location. Other properties (e.g. encoding or file system)
     * are initialized from the {@code FileLocator} associated with this object.
     *
     * @return the initialized builder for a {@code FileLocator}
     */
    private FileLocatorBuilder prepareNullLocatorBuilder()
    {
        return FileLocatorUtils.fileLocator(getFileLocator()).sourceURL(null)
                .basePath(null).fileName(null);
    }

    /**
     * Checks whether the associated {@code FileBased} object implements the
     * {@code FileLocatorAware} interface. If this is the case, a
     * {@code FileLocator} instance is injected which returns only <b>null</b>
     * values. This method is called if no file location is available (e.g. if
     * data is to be loaded from a stream). The encoding of the injected locator
     * is derived from this object.
     */
    private void injectNullFileLocator()
    {
        if (getContent() instanceof FileLocatorAware)
        {
            final FileLocator locator = prepareNullLocatorBuilder().create();
            ((FileLocatorAware) getContent()).initFileLocator(locator);
        }
    }

    /**
     * Injects a {@code FileLocator} pointing to the specified URL if the
     * current {@code FileBased} object implements the {@code FileLocatorAware}
     * interface.
     *
     * @param url the URL for the locator
     */
    private void injectFileLocator(final URL url)
    {
        if (url == null)
        {
            injectNullFileLocator();
        }
        else
        {
            if (getContent() instanceof FileLocatorAware)
            {
                final FileLocator locator =
                        prepareNullLocatorBuilder().sourceURL(url).create();
                ((FileLocatorAware) getContent()).initFileLocator(locator);
            }
        }
    }

    /**
     * Obtains a {@code SynchronizerSupport} for the current content. If the
     * content implements this interface, it is returned. Otherwise, result is a
     * dummy object. This method is called before load and save operations. The
     * returned object is used for synchronization.
     *
     * @return the {@code SynchronizerSupport} for synchronization
     */
    private SynchronizerSupport fetchSynchronizerSupport()
    {
        if (getContent() instanceof SynchronizerSupport)
        {
            return (SynchronizerSupport) getContent();
        }
        return DUMMY_SYNC_SUPPORT;
    }

    /**
     * Internal helper method for loading the associated file from the location
     * specified in the given {@code FileLocator}.
     *
     * @param locator the current {@code FileLocator}
     * @throws ConfigurationException if an error occurs
     */
    private void load(final FileLocator locator) throws ConfigurationException
    {
        final URL url = FileLocatorUtils.locateOrThrow(locator);
        load(url, locator);
    }

    /**
     * Internal helper method for loading a file from the given URL.
     *
     * @param url the URL
     * @param locator the current {@code FileLocator}
     * @throws ConfigurationException if an error occurs
     */
    private void load(final URL url, final FileLocator locator) throws ConfigurationException
    {
        InputStream in = null;

        try
        {
            in = FileLocatorUtils.obtainFileSystem(locator).getInputStream(url);
            loadFromStream(in, locator.getEncoding(), url);
        }
        catch (final ConfigurationException e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            throw new ConfigurationException(
                    "Unable to load the configuration from the URL " + url, e);
        }
        finally
        {
            closeSilent(in);
        }
    }

    /**
     * Internal helper method for loading a file from a file name.
     *
     * @param fileName the file name
     * @param locator the current {@code FileLocator}
     * @throws ConfigurationException if an error occurs
     */
    private void load(final String fileName, final FileLocator locator)
            throws ConfigurationException
    {
        final FileLocator locFileName = createLocatorWithFileName(fileName, locator);
        final URL url = FileLocatorUtils.locateOrThrow(locFileName);
        load(url, locator);
    }

    /**
     * Internal helper method for loading a file from the given input stream.
     *
     * @param in the input stream
     * @param locator the current {@code FileLocator}
     * @throws ConfigurationException if an error occurs
     */
    private void load(final InputStream in, final FileLocator locator)
            throws ConfigurationException
    {
        load(in, locator.getEncoding());
    }

    /**
     * Internal helper method for loading a file from an input stream.
     *
     * @param in the input stream
     * @param encoding the encoding
     * @param url the URL of the file to be loaded (if known)
     * @throws ConfigurationException if an error occurs
     */
    private void loadFromStream(final InputStream in, final String encoding, final URL url)
            throws ConfigurationException
    {
        checkContent();
        final SynchronizerSupport syncSupport = fetchSynchronizerSupport();
        syncSupport.lock(LockMode.WRITE);
        try
        {
            injectFileLocator(url);

            if (getContent() instanceof InputStreamSupport)
            {
                loadFromStreamDirectly(in);
            }
            else
            {
                loadFromTransformedStream(in, encoding);
            }
        }
        finally
        {
            syncSupport.unlock(LockMode.WRITE);
        }
    }

    /**
     * Loads data from an input stream if the associated {@code FileBased}
     * object implements the {@code InputStreamSupport} interface.
     *
     * @param in the input stream
     * @throws ConfigurationException if an error occurs
     */
    private void loadFromStreamDirectly(final InputStream in)
            throws ConfigurationException
    {
        try
        {
            ((InputStreamSupport) getContent()).read(in);
        }
        catch (final IOException e)
        {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Internal helper method for transforming an input stream to a reader and
     * reading its content.
     *
     * @param in the input stream
     * @param encoding the encoding
     * @throws ConfigurationException if an error occurs
     */
    private void loadFromTransformedStream(final InputStream in, final String encoding)
            throws ConfigurationException
    {
        Reader reader = null;

        if (encoding != null)
        {
            try
            {
                reader = new InputStreamReader(in, encoding);
            }
            catch (final UnsupportedEncodingException e)
            {
                throw new ConfigurationException(
                        "The requested encoding is not supported, try the default encoding.",
                        e);
            }
        }

        if (reader == null)
        {
            reader = new InputStreamReader(in);
        }

        loadFromReader(reader);
    }

    /**
     * Internal helper method for loading a file from the given reader.
     *
     * @param in the reader
     * @throws ConfigurationException if an error occurs
     */
    private void loadFromReader(final Reader in) throws ConfigurationException
    {
        fireLoadingEvent();
        try
        {
            getContent().read(in);
        }
        catch (final IOException ioex)
        {
            throw new ConfigurationException(ioex);
        }
        finally
        {
            fireLoadedEvent();
        }
    }

    /**
     * Internal helper method for saving data to the internal location stored
     * for this object.
     *
     * @param locator the current {@code FileLocator}
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    private void save(final FileLocator locator) throws ConfigurationException
    {
        if (!FileLocatorUtils.isLocationDefined(locator))
        {
            throw new ConfigurationException("No file location has been set!");
        }

        if (locator.getSourceURL() != null)
        {
            save(locator.getSourceURL(), locator);
        }
        else
        {
            save(locator.getFileName(), locator);
        }
    }

    /**
     * Internal helper method for saving data to the given file name.
     *
     * @param fileName the path to the target file
     * @param locator the current {@code FileLocator}
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    private void save(final String fileName, final FileLocator locator)
            throws ConfigurationException
    {
        URL url;
        try
        {
            url = FileLocatorUtils.obtainFileSystem(locator).getURL(
                    locator.getBasePath(), fileName);
        }
        catch (final MalformedURLException e)
        {
            throw new ConfigurationException(e);
        }

        if (url == null)
        {
            throw new ConfigurationException(
                    "Cannot locate configuration source " + fileName);
        }
        save(url, locator);
    }

    /**
     * Internal helper method for saving data to the given URL.
     *
     * @param url the target URL
     * @param locator the {@code FileLocator}
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    private void save(final URL url, final FileLocator locator) throws ConfigurationException
    {
        OutputStream out = null;
        try
        {
            out = FileLocatorUtils.obtainFileSystem(locator).getOutputStream(url);
            saveToStream(out, locator.getEncoding(), url);
            if (out instanceof VerifiableOutputStream)
            {
                try
                {
                    ((VerifiableOutputStream) out).verify();
                }
                catch (final IOException e)
                {
                    throw new ConfigurationException(e);
                }
            }
        }
        finally
        {
            closeSilent(out);
        }
    }

    /**
     * Internal helper method for saving data to the given {@code File}.
     *
     * @param file the target file
     * @param locator the current {@code FileLocator}
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    private void save(final File file, final FileLocator locator) throws ConfigurationException
    {
        OutputStream out = null;

        try
        {
            out = FileLocatorUtils.obtainFileSystem(locator).getOutputStream(file);
            saveToStream(out, locator.getEncoding(), file.toURI().toURL());
        }
        catch (final MalformedURLException muex)
        {
            throw new ConfigurationException(muex);
        }
        finally
        {
            closeSilent(out);
        }
    }

    /**
     * Internal helper method for saving a file to the given output stream.
     *
     * @param out the output stream
     * @param locator the current {@code FileLocator}
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    private void save(final OutputStream out, final FileLocator locator)
            throws ConfigurationException
    {
        save(out, locator.getEncoding());
    }

    /**
     * Internal helper method for saving a file to the given stream.
     *
     * @param out the output stream
     * @param encoding the encoding
     * @param url the URL of the output file if known
     * @throws ConfigurationException if an error occurs
     */
    private void saveToStream(final OutputStream out, final String encoding, final URL url)
            throws ConfigurationException
    {
        checkContent();
        final SynchronizerSupport syncSupport = fetchSynchronizerSupport();
        syncSupport.lock(LockMode.WRITE);
        try
        {
            injectFileLocator(url);
            Writer writer = null;

            if (encoding != null)
            {
                try
                {
                    writer = new OutputStreamWriter(out, encoding);
                }
                catch (final UnsupportedEncodingException e)
                {
                    throw new ConfigurationException(
                            "The requested encoding is not supported, try the default encoding.",
                            e);
                }
            }

            if (writer == null)
            {
                writer = new OutputStreamWriter(out);
            }

            saveToWriter(writer);
        }
        finally
        {
            syncSupport.unlock(LockMode.WRITE);
        }
    }

    /**
     * Internal helper method for saving a file into the given writer.
     *
     * @param out the writer
     * @throws ConfigurationException if an error occurs
     */
    private void saveToWriter(final Writer out) throws ConfigurationException
    {
        fireSavingEvent();
        try
        {
            getContent().write(out);
        }
        catch (final IOException ioex)
        {
            throw new ConfigurationException(ioex);
        }
        finally
        {
            fireSavedEvent();
        }
    }

    /**
     * Creates a {@code FileLocator} which is a copy of the passed in one, but
     * has the given file name set to reference the target file.
     *
     * @param fileName the file name
     * @param locator the {@code FileLocator} to copy
     * @return the manipulated {@code FileLocator} with the file name
     */
    private FileLocator createLocatorWithFileName(final String fileName,
            final FileLocator locator)
    {
        return FileLocatorUtils.fileLocator(locator).sourceURL(null)
                .fileName(fileName).create();
    }

    /**
     * Checks whether a content object is available. If not, an exception is
     * thrown. This method is called whenever the content object is accessed.
     *
     * @throws ConfigurationException if not content object is defined
     */
    private void checkContent() throws ConfigurationException
    {
        if (getContent() == null)
        {
            throw new ConfigurationException("No content available!");
        }
    }

    /**
     * Checks whether a content object is available and returns the current
     * {@code FileLocator}. If there is no content object, an exception is
     * thrown. This is a typical operation to be performed before a load() or
     * save() operation.
     *
     * @return the current {@code FileLocator} to be used for the calling
     *         operation
     */
    private FileLocator checkContentAndGetLocator()
            throws ConfigurationException
    {
        checkContent();
        return getFileLocator();
    }

    /**
     * Notifies the registered listeners about the start of a load operation.
     */
    private void fireLoadingEvent()
    {
        for (final FileHandlerListener l : listeners)
        {
            l.loading(this);
        }
    }

    /**
     * Notifies the registered listeners about a completed load operation.
     */
    private void fireLoadedEvent()
    {
        for (final FileHandlerListener l : listeners)
        {
            l.loaded(this);
        }
    }

    /**
     * Notifies the registered listeners about the start of a save operation.
     */
    private void fireSavingEvent()
    {
        for (final FileHandlerListener l : listeners)
        {
            l.saving(this);
        }
    }

    /**
     * Notifies the registered listeners about a completed save operation.
     */
    private void fireSavedEvent()
    {
        for (final FileHandlerListener l : listeners)
        {
            l.saved(this);
        }
    }

    /**
     * Notifies the registered listeners about a property update.
     */
    private void fireLocationChangedEvent()
    {
        for (final FileHandlerListener l : listeners)
        {
            l.locationChanged(this);
        }
    }

    /**
     * Normalizes URLs to files. Ensures that file URLs start with the correct
     * protocol.
     *
     * @param fileName the string to be normalized
     * @return the normalized file URL
     */
    private static String normalizeFileURL(String fileName)
    {
        if (fileName != null && fileName.startsWith(FILE_SCHEME)
                && !fileName.startsWith(FILE_SCHEME_SLASH))
        {
            fileName =
                    FILE_SCHEME_SLASH
                            + fileName.substring(FILE_SCHEME.length());
        }
        return fileName;
    }

    /**
     * A helper method for closing a stream. Occurring exceptions will be
     * ignored.
     *
     * @param cl the stream to be closed (may be <b>null</b>)
     */
    private static void closeSilent(final Closeable cl)
    {
        try
        {
            if (cl != null)
            {
                cl.close();
            }
        }
        catch (final IOException e)
        {
            LogFactory.getLog(FileHandler.class).warn("Exception when closing " + cl, e);
        }
    }

    /**
     * Creates a {@code File} object from the content of the given
     * {@code FileLocator} object. If the locator is not defined, result is
     * <b>null</b>.
     *
     * @param loc the {@code FileLocator}
     * @return a {@code File} object pointing to the associated file
     */
    private static File createFile(final FileLocator loc)
    {
        if (loc.getFileName() == null && loc.getSourceURL() == null)
        {
            return null;
        }
        else if (loc.getSourceURL() != null)
        {
            return FileLocatorUtils.fileFromURL(loc.getSourceURL());
        }
        else
        {
            return FileLocatorUtils.getFile(loc.getBasePath(),
                    loc.getFileName());
        }
    }

    /**
     * Creates an uninitialized file locator.
     *
     * @return the locator
     */
    private static FileLocator emptyFileLocator()
    {
        return FileLocatorUtils.fileLocator().create();
    }

    /**
     * Helper method for checking a file handler which is to be copied. Throws
     * an exception if the handler is <b>null</b>.
     *
     * @param c the {@code FileHandler} from which to copy the location
     * @return the same {@code FileHandler}
     */
    private static FileHandler checkSourceHandler(final FileHandler c)
    {
        if (c == null)
        {
            throw new IllegalArgumentException(
                    "FileHandler to assign must not be null!");
        }
        return c;
    }

    /**
     * An internal class that performs all update operations of the handler's
     * {@code FileLocator} in a safe way even if there is concurrent access.
     * This class implements anon-blocking algorithm for replacing the immutable
     * {@code FileLocator} instance stored in an atomic reference by a
     * manipulated instance. (If we already had lambdas, this could be done
     * without a class in a more elegant way.)
     */
    private abstract class Updater
    {
        /**
         * Performs an update of the enclosing file handler's
         * {@code FileLocator} object.
         */
        public void update()
        {
            boolean done;
            do
            {
                final FileLocator oldLocator = fileLocator.get();
                final FileLocatorBuilder builder =
                        FileLocatorUtils.fileLocator(oldLocator);
                updateBuilder(builder);
                done = fileLocator.compareAndSet(oldLocator, builder.create());
            } while (!done);
            fireLocationChangedEvent();
        }

        /**
         * Updates the passed in builder object to apply the manipulation to be
         * performed by this {@code Updater}. The builder has been setup with
         * the former content of the {@code FileLocator} to be manipulated.
         *
         * @param builder the builder for creating an updated
         *        {@code FileLocator}
         */
        protected abstract void updateBuilder(FileLocatorBuilder builder);
    }
}
