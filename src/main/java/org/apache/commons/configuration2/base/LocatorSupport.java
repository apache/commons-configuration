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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.configuration2.ConfigurationException;
import org.apache.commons.configuration2.fs.Locator;

/**
 * <p>
 * Definition of a capability interface for {@link ConfigurationSource}
 * implementations that can be loaded and stored using {@link Locator} objects.
 * </p>
 * <p>
 * Many configuration sources are loaded from files or at least from streams.
 * This interface defines operations for treating such sources in a generic way.
 * {@link Locator} objects are used to retrieve the actual configuration data.
 * An implementation can be assigned a default {@link Locator}, then the default
 * {@code load()} and {@code save()} operations refer to this {@link Locator}.
 * Alternatively a {@link Locator} can be specified as argument to {@code
 * load()} and {@code save()} operations. Finally, it is possible to use streams
 * or writers for I/O operations and to set an encoding.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public interface LocatorSupport
{
    /**
     * Returns the default {@code Locator} assigned to this object. The {@code
     * load()} and {@code save()} methods that do not take an argument use this
     * default {@code Locator}. Result can be <b>null</b> if no {@code Locator}
     * has been set yet.
     *
     * @return the default {@code Locator}
     */
    Locator getLocator();

    /**
     * Sets the default {@code Locator}. Using this method this object can be
     * assigned a default {@code Locator} that is used by methods that do not
     * explicitly define the source of a read or the target of a write
     * operation.
     *
     * @param locator the default {@code Locator}
     */
    void setLocator(Locator locator);

    /**
     * Returns the default encoding to be used for I/O operations. This can be
     * <b>null</b> if no encoding has been set so far. In this case I/O
     * operations will use the platform-specific default encoding.
     *
     * @return the default encoding
     */
    String getEncoding();

    /**
     * Sets the default encoding to be used for I/O operations. This encoding is
     * used when creating {@code Reader} or {@code Writer} objects from streams.
     * If no encoding has been set, the platform-specific default encoding is
     * used.
     *
     * @param enc the default encoding
     */
    void setEncoding(String enc);

    /**
     * Loads the data of this configuration source from the default
     * {@link Locator}. If no default {@code Locator} is set, an exception is
     * thrown.
     *
     * @throws ConfigurationException if an I/O error occurs
     * @throws IllegalStateException if no default {@code Locator} is set
     * @see #setLocator(Locator)
     */
    void load() throws ConfigurationException;

    /**
     * Loads data for this configuration source from the specified {@code
     * Locator}. The default {@code Locator} is not affected by this method.
     *
     * @param loc the {@code Locator} to load the data from
     * @throws ConfigurationException if an I/O error occurs
     */
    void load(Locator loc) throws ConfigurationException;

    /**
     * Loads data for this configuration source from the specified {@code
     * Reader}. Note: The caller is responsible for closing the reader.
     *
     * @param reader the {@code Reader} to load the data from
     * @throws ConfigurationException if an I/O error occurs
     */
    void load(Reader reader) throws ConfigurationException;

    /**
     * Loads data for this configuration source from the specified {@code
     * InputStream} using the given encoding. Note: The caller is responsible
     * for closing the stream.
     *
     * @param in the input stream to load the data from
     * @param encoding the encoding (can be <b>null</b>, then the
     *        platform-specific default encoding is used)
     * @throws ConfigurationException if an I/O error occurs
     */
    void load(InputStream in, String encoding) throws ConfigurationException;

    /**
     * Loads data for this configuration source from the specified {@code
     * InputStream} using the default encoding. Note: The caller is responsible
     * for closing the stream.
     *
     * @param in the input stream to load the data from
     * @throws ConfigurationException if an I/O error occurs
     * @see #setEncoding(String)
     */
    void load(InputStream in) throws ConfigurationException;

    /**
     * Saves the data of this configuration source to the default
     * {@link Locator}. If no default {@code Locator} is set, an exception is
     * thrown.
     *
     * @throws ConfigurationException if an I/O error occurs
     * @throws IllegalStateException if no default {@code Locator} is set
     * @see #setLocator(Locator)
     */
    void save() throws ConfigurationException;

    /**
     * Saves the data of this configuration source to the specified {@code
     * Locator}. The default {@code Locator} is not affected by this method.
     *
     * @param loc the {@code Locator} to save the data
     * @throws ConfigurationException if an I/O error occurs
     */
    void save(Locator loc) throws ConfigurationException;

    /**
     * Saves the data of this configuration source to the specified {@code
     * Writer}. Note: The caller is responsible for closing the reader.
     *
     * @param writer the {@code Writer} for storing the data
     * @throws ConfigurationException if an I/O error occurs
     */
    void save(Writer writer) throws ConfigurationException;

    /**
     * Saves the data of this configuration source to the specified {@code
     * OutputStream} using the given encoding. Note: The caller is responsible
     * for closing the stream.
     *
     * @param out the stream for storing the data
     * @param encoding the encoding (can be <b>null</b>, then the
     *        platform-specific default encoding is used)
     * @throws ConfigurationException if an I/O error occurs
     */
    void save(OutputStream out, String encoding) throws ConfigurationException;

    /**
     * Saves the data of this configuration source to the specified {@code
     * OutputStream} using the default encoding. Note: The caller is responsible
     * for closing the stream.
     *
     * @param out the stream for storing the data
     * @throws ConfigurationException if an I/O error occurs
     */
    void save(OutputStream out) throws ConfigurationException;
}
