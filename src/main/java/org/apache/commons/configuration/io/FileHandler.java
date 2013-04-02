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
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.FileSystem;

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
 * <ul>
 * <li>URLs: With the method {@code setURL()} a full URL to the configuration
 * source can be specified. This is the most flexible way. Note that the
 * {@code save()} methods support only <em>file:</em> URLs.</li>
 * <li>Files: The {@code setFile()} method allows to specify the configuration
 * source as a file. This can be either a relative or an absolute file. In the
 * former case the file is resolved based on the current directory.</li>
 * <li>As file paths in string form: With the {@code setPath()} method a full
 * path to a configuration file can be provided as a string.</li>
 * <li>Separated as base path and file name: This is the native form in which
 * the location is stored. The base path is a string defining either a local
 * directory or a URL. It can be set using the {@code setBasePath()} method. The
 * file name, non surprisingly, defines the name of the configuration file.</li>
 * </ul>
 * </p>
 * <p>
 * An instance stores a location. The {@code load()} and {@code save()} methods
 * that do not take an argument make use of this internal location.
 * Alternatively, it is also possible to use overloaded variants of
 * {@code load()} and {@code save()} which expect a location. In these cases the
 * location specified takes precedence over the internal one; the internal
 * location is not changed.
 * </p>
 * <p>
 * This class is thread-safe.
 * </p>
 *
 * @version $Id$
 */
public class FileHandler
{
    /** Constant for the URI scheme for files. */
    private static final String FILE_SCHEME = "file:";

    /** Constant for the URI scheme for files with slashes. */
    private static final String FILE_SCHEME_SLASH = FILE_SCHEME + "//";

    /** The file-based object managed by this handler. */
    private final FileBased content;

    /** Stores the location of the associated file. */
    private final FileSpec fileSpec;

    /** A collection with the registered listeners. */
    private final List<FileHandlerListener> listeners =
            new CopyOnWriteArrayList<FileHandlerListener>();

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
    public FileHandler(FileBased obj)
    {
        content = obj;
        fileSpec = new FileSpec();
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
    public FileHandler(FileBased obj, FileHandler c)
    {
        content = obj;
        fileSpec = c.snapshotFileSpec();
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
    public void addFileHandlerListener(FileHandlerListener l)
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
    public void removeFileHandlerListener(FileHandlerListener l)
    {
        listeners.remove(l);
    }

    /**
     * Return the name of the file.
     *
     * @return the file name
     */
    public String getFileName()
    {
        synchronized (fileSpec)
        {
            return fileSpec.getFileName();
        }
    }

    /**
     * Set the name of the file. The passed in file name can contain a relative
     * path. It must be used when referring files with relative paths from
     * classpath. Use {@code setPath()} to set a full qualified file name.
     *
     * @param fileName the name of the file
     */
    public void setFileName(String fileName)
    {
        String name = normalizeFileURL(fileName);
        synchronized (fileSpec)
        {
            fileSpec.setFileName(name);
            fileSpec.setSourceURL(null);
        }
        fireLocationChangedEvent();
    }

    /**
     * Return the base path.
     *
     * @return the base path
     */
    public String getBasePath()
    {
        synchronized (fileSpec)
        {
            return fileSpec.getBasePath();
        }
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
     * path is automatically set.
     *
     * @param basePath the base path.
     */
    public void setBasePath(String basePath)
    {
        String path = normalizeFileURL(basePath);
        synchronized (fileSpec)
        {
            fileSpec.setBasePath(path);
            fileSpec.setSourceURL(null);
        }
        fireLocationChangedEvent();
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
        String fileName;
        String basePath;
        URL sourceURL;

        synchronized (fileSpec)
        {
            fileName = fileSpec.getFileName();
            basePath = fileSpec.getBasePath();
            sourceURL = fileSpec.getSourceURL();
        }

        if (fileName == null && sourceURL == null)
        {
            return null;
        }
        else if (sourceURL != null)
        {
            return ConfigurationUtils.fileFromURL(sourceURL);
        }
        else
        {
            return ConfigurationUtils.getFile(basePath, fileName);
        }
    }

    /**
     * Sets the location of the associated file as a {@code File} object. The
     * passed in {@code File} is made absolute if it is not yet. Then the file's
     * path component becomes the base path and its name component becomes the
     * file name.
     *
     * @param file the location of the associated file
     */
    public void setFile(File file)
    {
        synchronized (fileSpec)
        {
            fileSpec.setFileName(file.getName());
            fileSpec.setBasePath((file.getParentFile() != null) ? file
                    .getParentFile().getAbsolutePath() : null);
            fileSpec.setSourceURL(null);
        }
        fireLocationChangedEvent();
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
        FileSpec spec;
        File file;
        synchronized (fileSpec)
        {
            spec = snapshotFileSpec();
            file = getFile();
        }

        return spec.getFileSystem().getPath(file, spec.getSourceURL(),
                spec.getBasePath(), spec.getFileName());
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
    public void setPath(String path)
    {
        setFile(new File(path));
    }

    /**
     * Returns the location of the associated file as a URL.
     *
     * @return a URL to the associated file; can be <b>null</b> if the location
     *         is unspecified
     */
    public URL getURL()
    {
        URL sourceURL;
        FileSystem fileSystem;
        String basePath;
        String fileName;
        synchronized (fileSpec)
        {
            sourceURL = fileSpec.getSourceURL();
            fileSystem = fileSpec.getFileSystem();
            basePath = fileSpec.getBasePath();
            fileName = fileSpec.getFileName();
        }

        return (sourceURL != null) ? sourceURL : ConfigurationUtils.locate(
                fileSystem, basePath, fileName);
    }

    /**
     * Sets the location of the associated file as a URL. For loading this can
     * be an arbitrary URL with a supported protocol. If the file is to be
     * saved, too, a URL with the &quot;file&quot; protocol should be provided.
     *
     * @param url the location of the file as URL
     */
    public void setURL(URL url)
    {
        synchronized (fileSpec)
        {
            initFileSpecWithURL(fileSpec, url);
        }
        fireLocationChangedEvent();
    }

    /**
     * Tests whether a location is defined for this {@code FileHandler}.
     *
     * @return <b>true</b> if a location is defined, <b>false</b> otherwise
     */
    public boolean isLocationDefined()
    {
        synchronized (fileSpec)
        {
            return fileSpec.getFileName() != null;
        }
    }

    /**
     * Clears the location of this {@code FileHandler}. Afterwards this handler
     * does not point to any valid file.
     */
    public void clearLocation()
    {
        synchronized (fileSpec)
        {
            fileSpec.setBasePath(null);
            fileSpec.setFileName(null);
            fileSpec.setSourceURL(null);
        }
    }

    /**
     * Returns the encoding of the associated file. Result can be <b>null</b> if
     * no encoding has been set.
     *
     * @return the encoding of the associated file
     */
    public String getEncoding()
    {
        synchronized (fileSpec)
        {
            return fileSpec.getEncoding();
        }
    }

    /**
     * Sets the encoding of the associated file. The encoding applies if binary
     * files are loaded.
     *
     * @param encoding the encoding of the associated file
     */
    public void setEncoding(String encoding)
    {
        synchronized (fileSpec)
        {
            fileSpec.setEncoding(encoding);
        }
        fireLocationChangedEvent();
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
        synchronized (fileSpec)
        {
            return fileSpec.getFileSystem();
        }
    }

    /**
     * Sets the {@code FileSystem} to be used by this object when locating
     * files. If a <b>null</b> value is passed in, the file system is reset to
     * the default file system.
     *
     * @param fileSystem the {@code FileSystem}
     */
    public void setFileSystem(FileSystem fileSystem)
    {
        FileSystem fs =
                (fileSystem != null) ? fileSystem : FileSystem
                        .getDefaultFileSystem();
        synchronized (fileSpec)
        {
            fileSpec.setFileSystem(fs);
        }
        fireLocationChangedEvent();
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
     * Loads the associated file from the underlying location. If no location
     * has been set, an exception is thrown.
     *
     * @throws ConfigurationException if loading of the configuration fails
     */
    public void load() throws ConfigurationException
    {
        load(checkContentAndCreateSnapshotFileSpec());
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
    public void load(String fileName) throws ConfigurationException
    {
        load(fileName, checkContentAndCreateSnapshotFileSpec());
    }

    /**
     * Loads the associated file from the specified {@code File}.
     *
     * @param file the file to load
     * @throws ConfigurationException if an error occurs
     */
    public void load(File file) throws ConfigurationException
    {
        URL url;
        try
        {
            url = ConfigurationUtils.toURL(file);
        }
        catch (MalformedURLException e1)
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
    public void load(URL url) throws ConfigurationException
    {
        load(url, checkContentAndCreateSnapshotFileSpec());
    }

    /**
     * Loads the associated file from the specified stream, using the encoding
     * returned by {@link #getEncoding()}.
     *
     * @param in the input stream
     * @throws ConfigurationException if an error occurs during the load
     *         operation
     */
    public void load(InputStream in) throws ConfigurationException
    {
        load(in, checkContentAndCreateSnapshotFileSpec());
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
    public void load(InputStream in, String encoding)
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
    public void load(Reader in) throws ConfigurationException
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
        save(checkContentAndCreateSnapshotFileSpec());
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
    public void save(String fileName) throws ConfigurationException
    {
        save(fileName, checkContentAndCreateSnapshotFileSpec());
    }

    /**
     * Saves the associated file to the specified URL. This does not change the
     * location of this object (use {@link #setURL(URL)} if you need it).
     *
     * @param url the URL
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    public void save(URL url) throws ConfigurationException
    {
        save(url, checkContentAndCreateSnapshotFileSpec());
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
    public void save(File file) throws ConfigurationException
    {
        save(file, checkContentAndCreateSnapshotFileSpec());
    }

    /**
     * Saves the associated file to the specified stream using the encoding
     * returned by {@link #getEncoding()}.
     *
     * @param out the output stream
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    public void save(OutputStream out) throws ConfigurationException
    {
        save(out, checkContentAndCreateSnapshotFileSpec());
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
    public void save(OutputStream out, String encoding)
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
    public void save(Writer out) throws ConfigurationException
    {
        checkContent();
        injectNullFileLocator();
        saveToWriter(out);
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
            FileSpec spec = new FileSpec();
            spec.setEncoding(getEncoding());
            ((FileLocatorAware) getContent()).initFileLocator(spec);
        }
    }

    /**
     * Injects a {@code FileLocator} pointing to the specified URL if the
     * current {@code FileBased} object implements the {@code FileLocatorAware}
     * interface.
     *
     * @param url the URL for the locator
     */
    private void injectFileLocator(URL url)
    {
        if (url == null)
        {
            injectNullFileLocator();
        }
        else
        {
            if (getContent() instanceof FileLocatorAware)
            {
                FileSpec spec = new FileSpec();
                initFileSpecWithURL(spec, url);
                ((FileLocatorAware) getContent()).initFileLocator(spec);
            }
        }
    }

    /**
     * Internal helper method for loading the associated file from the location
     * specified in the given {@code FileSpec}.
     *
     * @param spec the current {@code FileSpec}
     * @throws ConfigurationException if an error occurs
     */
    private void load(FileSpec spec) throws ConfigurationException
    {
        if (spec.getSourceURL() != null)
        {
            load(spec.getSourceURL(), spec);
        }
        else
        {
            load(spec.getFileName(), spec);
        }
    }

    /**
     * Internal helper method for loading a file from the given URL.
     *
     * @param url the URL
     * @param spec the current {@code FileSpec}
     * @throws ConfigurationException if an error occurs
     */
    private void load(URL url, FileSpec spec) throws ConfigurationException
    {
        InputStream in = null;

        try
        {
            in = spec.getFileSystem().getInputStream(url);
            loadFromStream(in, spec.getEncoding(), url);
        }
        catch (ConfigurationException e)
        {
            throw e;
        }
        catch (Exception e)
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
     * @param spec the current {@code FileSpec}
     * @throws ConfigurationException if an error occurs
     */
    private void load(String fileName, FileSpec spec)
            throws ConfigurationException
    {
        URL url =
                ConfigurationUtils.locate(spec.getFileSystem(),
                        spec.getBasePath(), fileName);

        if (url == null)
        {
            throw new ConfigurationException(
                    "Cannot locate configuration source " + fileName);
        }
        load(url, spec);
    }

    /**
     * Internal helper method for loading a file from the given input stream.
     *
     * @param in the input stream
     * @param spec the current {@code FileSpec}
     * @throws ConfigurationException if an error occurs
     */
    private void load(InputStream in, FileSpec spec)
            throws ConfigurationException
    {
        load(in, spec.getEncoding());
    }

    /**
     * Internal helper method for loading a file from an input stream.
     *
     * @param in the input stream
     * @param encoding the encoding
     * @param url the URL of the file to be loaded (if known)
     * @throws ConfigurationException if an error occurs
     */
    private void loadFromStream(InputStream in, String encoding, URL url)
            throws ConfigurationException
    {
        checkContent();
        injectFileLocator(url);
        Reader reader = null;

        if (encoding != null)
        {
            try
            {
                reader = new InputStreamReader(in, encoding);
            }
            catch (UnsupportedEncodingException e)
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
    private void loadFromReader(Reader in) throws ConfigurationException
    {
        fireLoadingEvent();
        try
        {
            getContent().read(in);
        }
        catch (IOException ioex)
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
     * @param spec the current {@code FileSpec}
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    private void save(FileSpec spec) throws ConfigurationException
    {
        if (spec.getFileName() == null)
        {
            throw new ConfigurationException("No file name has been set!");
        }

        if (spec.getSourceURL() != null)
        {
            save(spec.getSourceURL(), spec);
        }
        else
        {
            save(spec.getFileName(), spec);
        }
    }

    /**
     * Internal helper method for saving data to the given file name.
     *
     * @param fileName the path to the target file
     * @param spec the current {@code FileSpec}
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    private void save(String fileName, FileSpec spec)
            throws ConfigurationException
    {
        URL url;
        try
        {
            url = spec.getFileSystem().getURL(spec.getBasePath(), fileName);
        }
        catch (MalformedURLException e)
        {
            throw new ConfigurationException(e);
        }

        if (url == null)
        {
            throw new ConfigurationException(
                    "Cannot locate configuration source " + fileName);
        }
        save(url, spec);
    }

    /**
     * Internal helper method for saving data to the given URL.
     *
     * @param url the target URL
     * @param spec the {@code FileSpec}
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    private void save(URL url, FileSpec spec) throws ConfigurationException
    {
        OutputStream out = null;
        try
        {
            out = spec.getFileSystem().getOutputStream(url);
            saveToStream(out, spec.getEncoding(), url);
            if(out instanceof VerifiableOutputStream)
            {
                try
                {
                    ((VerifiableOutputStream) out).verify();
                }
                catch (IOException e)
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
     * @param spec the current {@code FileSpec}
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    private void save(File file, FileSpec spec) throws ConfigurationException
    {
        OutputStream out = null;

        try
        {
            out = spec.getFileSystem().getOutputStream(file);
            saveToStream(out, spec.getEncoding(), file.toURI().toURL());
        }
        catch (MalformedURLException muex)
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
     * @param spec the current {@code FileSpec}
     * @throws ConfigurationException if an error occurs during the save
     *         operation
     */
    private void save(OutputStream out, FileSpec spec)
            throws ConfigurationException
    {
        save(out, spec.getEncoding());
    }

    /**
     * Internal helper method for saving a file to the given stream.
     *
     * @param out the output stream
     * @param encoding the encoding
     * @param url the URL of the output file if known
     * @throws ConfigurationException if an error occurs
     */
    private void saveToStream(OutputStream out, String encoding, URL url)
            throws ConfigurationException
    {
        checkContent();
        injectFileLocator(url);
        Writer writer = null;

        if (encoding != null)
        {
            try
            {
                writer = new OutputStreamWriter(out, encoding);
            }
            catch (UnsupportedEncodingException e)
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

    /**
     * Internal helper method for saving a file into the given writer.
     *
     * @param out the writer
     * @throws ConfigurationException if an error occurs
     */
    private void saveToWriter(Writer out) throws ConfigurationException
    {
        fireSavingEvent();
        try
        {
            getContent().write(out);
        }
        catch (IOException ioex)
        {
            throw new ConfigurationException(ioex);
        }
        finally
        {
            fireSavedEvent();
        }
    }

    /**
     * Creates a snapshot of the current location data of the associated file.
     * This snapshot can be used in code which does not have to synchronize on
     * the internal {@code FileSpec} object.
     *
     * @return a snapshot of the current file location
     */
    private FileSpec snapshotFileSpec()
    {
        synchronized (fileSpec)
        {
            return fileSpec.clone();
        }
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
     * Checks whether a content object is available and creates a snapshot from
     * the current file specification. If there is no content object, an
     * exception is thrown. This is a typical operation to be performed before a
     * load() or save() operation.
     *
     * @return a snapshot of the current file location
     */
    private FileSpec checkContentAndCreateSnapshotFileSpec()
            throws ConfigurationException
    {
        checkContent();
        return snapshotFileSpec();
    }

    /**
     * Notifies the registered listeners about the start of a load operation.
     */
    private void fireLoadingEvent()
    {
        for (FileHandlerListener l : listeners)
        {
            l.loading(this);
        }
    }

    /**
     * Notifies the registered listeners about a completed load operation.
     */
    private void fireLoadedEvent()
    {
        for (FileHandlerListener l : listeners)
        {
            l.loaded(this);
        }
    }

    /**
     * Notifies the registered listeners about the start of a save operation.
     */
    private void fireSavingEvent()
    {
        for (FileHandlerListener l : listeners)
        {
            l.saving(this);
        }
    }

    /**
     * Notifies the registered listeners about a completed save operation.
     */
    private void fireSavedEvent()
    {
        for (FileHandlerListener l : listeners)
        {
            l.saved(this);
        }
    }

    /**
     * Notifies the registered listeners about a property update.
     */
    private void fireLocationChangedEvent()
    {
        for (FileHandlerListener l : listeners)
        {
            l.locationChanged(this);
        }
    }

    /**
     * Initializes a {@code FileSpec} object with a URL. This method ensures
     * that base path and file name are set correctly.
     *
     * @param spec the {@code FileSpec} to be initialized
     * @param url the URL to set
     */
    private static void initFileSpecWithURL(FileSpec spec, URL url)
    {
        String basePath = ConfigurationUtils.getBasePath(url);
        String fileName = ConfigurationUtils.getFileName(url);
        spec.setBasePath(basePath);
        spec.setFileName(fileName);
        spec.setSourceURL(url);
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
    private static void closeSilent(Closeable cl)
    {
        try
        {
            if (cl != null)
            {
                cl.close();
            }
        }
        catch (IOException e)
        {
            // ignore
        }
    }

    /**
     * A bean class defining the location of a file.
     */
    private static class FileSpec implements Cloneable, FileLocator
    {
        /** Stores the file name. */
        private String fileName;

        /** Stores the base path. */
        private String basePath;

        /** Stores the URL of the associated file. */
        private URL sourceURL;

        /** Stores the encoding for binary streams. */
        private String encoding;

        /** The FileSystem being used for this Configuration */
        private FileSystem fileSystem = FileSystem.getDefaultFileSystem();

        public String getFileName()
        {
            return fileName;
        }

        public void setFileName(String fileName)
        {
            this.fileName = fileName;
        }

        public String getBasePath()
        {
            return basePath;
        }

        public void setBasePath(String basePath)
        {
            this.basePath = basePath;
        }

        public URL getSourceURL()
        {
            return sourceURL;
        }

        public void setSourceURL(URL sourceURL)
        {
            this.sourceURL = sourceURL;
        }

        public String getEncoding()
        {
            return encoding;
        }

        public void setEncoding(String encoding)
        {
            this.encoding = encoding;
        }

        public FileSystem getFileSystem()
        {
            return fileSystem;
        }

        public void setFileSystem(FileSystem fileSystem)
        {
            this.fileSystem = fileSystem;
        }

        @Override
        protected FileSpec clone()
        {
            try
            {
                return (FileSpec) super.clone();
            }
            catch (CloneNotSupportedException cex)
            {
                // should not happen
                throw new AssertionError("Could not clone!");
            }
        }
    }
}
