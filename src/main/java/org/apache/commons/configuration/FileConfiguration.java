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

package org.apache.commons.configuration;

import java.net.URL;
import java.io.InputStream;
import java.io.Reader;
import java.io.OutputStream;
import java.io.Writer;
import java.io.File;

import org.apache.commons.configuration.reloading.ReloadingStrategy;

/**
 * A persistent configuration loaded and saved to a file.
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 * @since 1.0-rc2
 */
public interface FileConfiguration extends Configuration
{
    /**
     * Load the configuration from the underlying URL. If the URL is not
     * specified, it attempts to locate the specified file name.
     *
     * @throws ConfigurationException if an error occurs during the load operation
     */
    void load() throws ConfigurationException;

    /**
     * Locate the specified file and load the configuration.
     *
     * @param fileName the name of the file loaded
     *
     * @throws ConfigurationException if an error occurs during the load operation
     */
    void load(String fileName) throws ConfigurationException;

    /**
     * Load the configuration from the specified file.
     *
     * @param file the loaded file
     *
     * @throws ConfigurationException if an error occurs during the load operation
     */
    void load(File file) throws ConfigurationException;

    /**
     * Load the configuration from the specified URL.
     *
     * @param url the URL of the file loaded
     *
     * @throws ConfigurationException if an error occurs during the load operation
     */
    void load(URL url) throws ConfigurationException;

    /**
     * Load the configuration from the specified stream, using the encoding
     * returned by {@link #getEncoding()}.
     *
     * @param in the input stream
     *
     * @throws ConfigurationException if an error occurs during the load operation
     */
    void load(InputStream in) throws ConfigurationException;

    /**
     * Load the configuration from the specified stream, using the specified
     * encoding. If the encoding is null the default encoding is used.
     *
     * @param in the input stream
     * @param encoding the encoding used. <code>null</code> to use the default encoding
     *
     * @throws ConfigurationException if an error occurs during the load operation
     */
    void load(InputStream in, String encoding) throws ConfigurationException;

    /**
     * Load the configuration from the specified reader.
     *
     * @param in the reader
     *
     * @throws ConfigurationException if an error occurs during the load operation
     */
    void load(Reader in) throws ConfigurationException;

    /**
     * Save the configuration.
     *
     * @throws ConfigurationException if an error occurs during the save operation
     */
    void save() throws ConfigurationException;

    /**
     * Save the configuration to the specified file.
     *
     * @param fileName the name of the file to be saved
     *
     * @throws ConfigurationException if an error occurs during the save operation
     */
    void save(String fileName) throws ConfigurationException;

    /**
     * Save the configuration to the specified file.
     *
     * @param file specifies the file to be saved
     *
     * @throws ConfigurationException if an error occurs during the save operation
     */
    void save(File file) throws ConfigurationException;

    /**
     * Save the configuration to the specified URL.
     *
     * @param url the URL
     *
     * @throws ConfigurationException if an error occurs during the save operation
     */
    void save(URL url) throws ConfigurationException;

    /**
     * Save the configuration to the specified stream, using the encoding
     * returned by {@link #getEncoding()}.
     *
     * @param out the output stream
     *
     * @throws ConfigurationException if an error occurs during the save operation
     */
    void save(OutputStream out) throws ConfigurationException;

    /**
     * Save the configuration to the specified stream, using the specified
     * encoding. If the encoding is null the default encoding is used.
     *
     * @param out the output stream
     * @param encoding the encoding to be used
     * @throws ConfigurationException if an error occurs during the save operation
     */
    void save(OutputStream out, String encoding) throws ConfigurationException;

    /**
     * Save the configuration to the specified writer.
     *
     * @param out the writer
     *
     * @throws ConfigurationException if an error occurs during the save operation
     */
    void save(Writer out) throws ConfigurationException;

    /**
     * Return the name of the file.
     *
     * @return the file name
     */
    String getFileName();

    /**
     * Set the name of the file.
     *
     * @param fileName the name of the file
     */
    void setFileName(String fileName);

    /**
     * Returns the base path. One way to specify the location of a configuration
     * source is by setting its base path and its file name. This method returns
     * this base path. The concrete value returned by this method depends on the
     * way the location of the configuration file was set. If methods like
     * <code>setFile()</code> or <code>setURL()</code> were used, the base
     * path typically points to the parent directory of the configuration file
     * (e.g. for the URL <code>file:/temp/test.properties</code> the base path
     * will be <code>file:/temp/</code>). If the base path was explictly set
     * using <code>setBasePath()</code>, this method will return the exact
     * value specified here without further modifications.
     *
     * @return the base path
     * @see AbstractFileConfiguration#setBasePath(String)
     */
    String getBasePath();

    /**
     * Sets the base path. The methods <code>setBasePath()</code> and
     * <code>setFileName()</code> can be used together to specify the location
     * of the configuration file to be loaded. If relative file names are to
     * be resolved (e.g. for the include files supported by
     * <code>PropertiesConfiguration</code>), this base path will be used.
     *
     * @param basePath the base path.
     */
    void setBasePath(String basePath);

    /**
     * Return the file where the configuration is stored.
     *
     * @return the configuration file
     */
    File getFile();

    /**
     * Set the file where the configuration is stored.
     *
     * @param file the file
     */
    void setFile(File file);

    /**
     * Return the URL where the configuration is stored.
     *
     * @return the URL of the configuration
     */
    URL getURL();

    /**
     * The URL where the configuration is stored.
     *
     * @param url the URL
     */
    void setURL(URL url);

    /**
     * Enable or disable the automatical saving of modified properties to the disk.
     *
     * @param autoSave <code>true</code> to enable, <code>false</code> to disable
     * @since 1.1
     */
    void setAutoSave(boolean autoSave);

    /**
     * Tells if properties are automatically saved to the disk.
     *
     * @return <code>true</code> if auto-saving is enabled, <code>false</code> otherwise
     * @since 1.1
     */
    boolean isAutoSave();

    /**
     * Return the reloading strategy.
     *
     * @return the reloading strategy currently used
     * @since 1.1
     */
    ReloadingStrategy getReloadingStrategy();

    /**
     * Set the reloading strategy.
     *
     * @param strategy the reloading strategy to use
     * @since 1.1
     */
    void setReloadingStrategy(ReloadingStrategy strategy);

    /**
     * Reload the configuration.
     *
     * @since 1.1
     */
    void reload();

    /**
     * Return the encoding used to store the configuration file. If the value
     * is null the default encoding is used.
     *
     * @return the current encoding
     * @since 1.1
     */
    String getEncoding();

    /**
     * Set the encoding used to store the configuration file. Set the encoding
     * to null to use the default encoding.
     *
     * @param encoding the encoding to use
     * @since 1.1
     */
    void setEncoding(String encoding);

}
