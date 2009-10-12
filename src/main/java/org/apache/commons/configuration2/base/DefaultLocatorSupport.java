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
package org.apache.commons.configuration2.base;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.commons.configuration2.ConfigurationException;
import org.apache.commons.configuration2.fs.FileSystem;
import org.apache.commons.configuration2.fs.FileSystemBased;
import org.apache.commons.configuration2.fs.Locator;

/**
 * <p>
 * A default implementation of the {@code LocatorSupport} interface.
 * </p>
 * <p>
 * This class implements all the various load() and save() methods defined by
 * the {@code LocatorSupport} interface by delegating to a
 * {@link StreamBasedSource} instance that was passed at construction time. This
 * basically means that all load() operations are eventually mapped to a {@code
 * Reader}, and all save() operations are eventually mapped to a {@code Writer}.
 * The encoding and a default {@link Locator} are also maintained as member
 * fields.
 * </p>
 * <p>
 * {@code DefaultLocatorSupport} implements the {@link FileSystemBased}
 * interface. Thus a specific {@link FileSystem} can be set which is used for
 * resolving the URLs obtained from {@link Locator} instances. By providing
 * corresponding {@link FileSystem} implementations various types of URLs can be
 * supported.
 * </p>
 * <p>
 * Using this class greatly simplifies adding support for enhanced file
 * operations to different {@link ConfigurationSource} implementations. Because
 * the delegation principle is used no complex inheritance structures are
 * necessary.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class DefaultLocatorSupport implements LocatorSupport, FileSystemBased
{
    /** Stores the underlying stream-based source. */
    private final StreamBasedSource streamBasedSource;

    /** Stores the default locator. */
    private Locator locator;

    /** The file system used by this object. */
    private FileSystem fileSystem = FileSystem.getDefaultFileSystem();

    /** Stores the default encoding. */
    private String encoding;

    /**
     * Creates a new instance of {@code DefaultLocatorSupport} and initializes
     * it with the specified {@code StreamBasedSource}. All load() and save()
     * operations performed on this instance are eventually delegated to this
     * source.
     *
     * @param source the {@code StreamBasedSource} (must not be <b>null</b>)
     * @throws IllegalArgumentException if the {@code StreamBasedSource} is
     *         <b>null</b>
     */
    public DefaultLocatorSupport(StreamBasedSource source)
    {
        if (source == null)
        {
            throw new IllegalArgumentException(
                    "StreamBasedSource must not be null!");
        }
        streamBasedSource = source;
    }

    /**
     * Returns the underlying {@code StreamBasedSource}.
     *
     * @return the {@code StreamBasedSource}
     */
    public StreamBasedSource getStreamBasedSource()
    {
        return streamBasedSource;
    }

    /**
     * Returns the encoding.
     *
     * @return the encoding
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * Returns the default {@code Locator}.
     *
     * @return the {@code Locator}
     */
    public Locator getLocator()
    {
        return locator;
    }

    /**
     * Loads data from the default {@code Locator}. The {@link Locator} must
     * have been set before using the {@link #setLocator(Locator)} method;
     * otherwise an exception is thrown.
     *
     * @throws ConfigurationException in case of an error
     * @throws IllegalStateException if no {@link Locator} has been set
     */
    public void load() throws ConfigurationException
    {
        if (getLocator() == null)
        {
            throw new IllegalStateException(
                    "A default Locator must be set before calling load()!");
        }

        load(getLocator());
    }

    /**
     * Loads data from the specified {@code Locator}. This implementation
     * obtains the input URL from the {@code Locator}. It then uses the {@code
     * FileSystem} to obtain an input stream for this URL. This stream is then
     * loaded.
     *
     * @param loc the {@code Locator} to load from (must not be <b>null</b>)
     * @throws ConfigurationException if an error occurs
     * @throws IllegalArgumentException if the {@code Locator} is <b>null</b>
     */
    public void load(Locator loc) throws ConfigurationException
    {
        if (loc == null)
        {
            throw new IllegalArgumentException("Locator must not be null!");
        }

        InputStream in = getFileSystem().getInputStream(loc.getURL(false));
        try
        {
            load(in);
        }
        finally
        {
            close(in);
        }
    }

    /**
     * Loads data from the specified {@code Reader}. This implementation
     * directly delegates to the underlying {@code StreamBasedSource}.
     *
     * @param reader the reader to read from
     * @throws ConfigurationException if an error occurs
     */
    public void load(Reader reader) throws ConfigurationException
    {
        try
        {
            getStreamBasedSource().load(reader);
        }
        catch (IOException ioex)
        {
            throw new ConfigurationException(ioex);
        }
    }

    /**
     * Loads data from the specified {@code InputStream} using the given
     * encoding. This implementation creates a {@code Reader} for the specified
     * stream and delegates to {@link #load(Reader)}.
     *
     * @param in the input stream
     * @param encoding the encoding (can be <b>null</b>), then no specific
     *        encoding is set
     * @throws ConfigurationException if an error occurs
     */
    public void load(InputStream in, String encoding)
            throws ConfigurationException
    {
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

        load(reader);
    }

    /**
     * Loads data from the specified {@code InputStream} using the default
     * encoding. This implementation calls {@link #getEncoding()} to obtain the
     * current default encoding (which may be undefined) and then delegates to
     * {@link #load(InputStream, String)}.
     *
     * @param in the input stream
     * @throws ConfigurationException if an error occurs
     */
    public void load(InputStream in) throws ConfigurationException
    {
        load(in, getEncoding());
    }

    /**
     * Saves data to the default {@link Locator}. A {@link Locator} must have
     * been set using {@link #setLocator(Locator)}; otherwise an exception is
     * thrown.
     *
     * @throws ConfigurationException if an error occurs
     * @throws IllegalStateException if no {@link Locator} is set
     */
    public void save() throws ConfigurationException
    {
        if (getLocator() == null)
        {
            throw new IllegalStateException(
                    "A default Locator must be set before calling save()!");
        }

        save(getLocator());
    }

    /**
     * Saves data to the specified {@code Locator}. This implementation obtains
     * the output URL from the given {@code Locator} and uses the
     * {@link FileSystem} to create an output stream for it. Then it delegates
     * to {@link #save(OutputStream)}.
     *
     * @param loc the {@code Locator} (must not be <b>null</b>)
     * @throws ConfigurationException if an error occurs
     * @throws IllegalArgumentException if the {@code Locator} is <b>null</b>
     */
    public void save(Locator loc) throws ConfigurationException
    {
        if (loc == null)
        {
            throw new IllegalArgumentException("Locator must not be null!");
        }

        OutputStream out = getFileSystem().getOutputStream(loc.getURL(true));
        try
        {
            save(out);
        }
        finally
        {
            close(out);
        }
    }

    /**
     * Saves data to the specified {@code Writer}. This implementation directly
     * delegates to the underlying {@code StreamBasedSource}.
     *
     * @param writer the writer
     * @throws ConfigurationException if an error occurs
     * @see StreamBasedSource#save(Writer)
     */
    public void save(Writer writer) throws ConfigurationException
    {
        try
        {
            getStreamBasedSource().save(writer);
        }
        catch (IOException ioex)
        {
            throw new ConfigurationException(ioex);
        }
    }

    /**
     * Saves data to the specified output stream using the given encoding. This
     * implementation creates a writer for this output stream and delegates to
     * {@link #save(Writer)}.
     *
     * @param out the output stream
     * @param encoding the encoding (can be <b>null</b>, then the
     *        platform-specific default encoding is used)
     * @throws ConfigurationException if an error occurs
     */
    public void save(OutputStream out, String encoding)
            throws ConfigurationException
    {
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

        save(writer);
    }

    /**
     * Saves data to the specified output stream using the default encoding.
     * This method calls {@link #getEncoding()} to obtain the default encoding
     * (which may be undefined). Then it delegates to
     * {@link #save(OutputStream, String)}.
     *
     * @param out the output stream
     * @throws ConfigurationException if an error occurs
     */
    public void save(OutputStream out) throws ConfigurationException
    {
        save(out, getEncoding());
    }

    /**
     * Sets the default encoding. This encoding is used when reading data from
     * streams and no explicit encoding is specified.
     *
     * @param the default encoding
     */
    public void setEncoding(String enc)
    {
        encoding = enc;
    }

    /**
     * Sets the {@code Locator}. This {@code Locator} is used by the {@code
     * load()} method that takes no arguments.
     *
     * @param locator the {@code Locator}
     */
    public void setLocator(Locator locator)
    {
        this.locator = locator;
    }

    /**
     * Returns the {@code FileSystem} used by this object.
     *
     * @return the {@code FileSyste}
     */
    public FileSystem getFileSystem()
    {
        return fileSystem;
    }

    /**
     * Resets the {@code FileSystem} used by this object to the default {@code
     * FileSystem}.
     */
    public void resetFileSystem()
    {
        fileSystem = FileSystem.getDefaultFileSystem();
    }

    /**
     * Sets the {@code FileSystem} to be used by this object. The {@code
     * FileSystem} is used for resolving URLs returned by {@code Locator}
     * objects.
     *
     * @param fileSystem the new {@code FileSystem} (must not be <b>null</b>)
     * @throws IllegalArgumentException if the {@code FileSystem} is <b>null</b>
     */
    public void setFileSystem(FileSystem fileSystem)
    {
        if (fileSystem == null)
        {
            throw new IllegalArgumentException("FileSystem must not be null!");
        }
        this.fileSystem = fileSystem;
    }

    /**
     * Helper method for closing the specified object. Exceptions are rethrown
     * as configuration exceptions.
     *
     * @param c the object to be closed
     * @throws ConfigurationException if an I/O error occurs
     */
    static void close(Closeable c) throws ConfigurationException
    {
        try
        {
            c.close();
        }
        catch (IOException ioex)
        {
            throw new ConfigurationException("Error when closing stream", ioex);
        }
    }
}
