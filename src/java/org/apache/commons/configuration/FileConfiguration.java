/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
     * @throws ConfigurationException
     */
    void load() throws ConfigurationException;

    /**
     * Locate the specified file and load the configuration.
     *
     * @param fileName the name of the file loaded
     *
     * @throws ConfigurationException
     */
    void load(String fileName) throws ConfigurationException;

    /**
     * Load the configuration from the specified file.
     *
     * @param file the loaded file
     *
     * @throws ConfigurationException
     */
    void load(File file) throws ConfigurationException;

    /**
     * Load the configuration from the specified URL.
     *
     * @param url the URL of the file loaded
     *
     * @throws ConfigurationException
     */
    void load(URL url) throws ConfigurationException;

    /**
     * Load the configuration from the specified stream, using the encoding
     * returned by {@link #getEncoding()}.
     *
     * @param in the input stream
     *
     * @throws ConfigurationException
     */
    void load(InputStream in) throws ConfigurationException;

    /**
     * Load the configuration from the specified stream, using the specified
     * encoding. If the encoding is null the default encoding is used.
     *
     * @param in the input stream
     * @param encoding the encoding used. <code>null</code> to use the default encoding
     *
     * @throws ConfigurationException
     */
    void load(InputStream in, String encoding) throws ConfigurationException;

    /**
     * Load the configuration from the specified reader.
     *
     * @param in the reader
     *
     * @throws ConfigurationException
     */
    void load(Reader in) throws ConfigurationException;

    /**
     * Save the configuration.
     *
     * @throws ConfigurationException
     */
    void save() throws ConfigurationException;

    /**
     * Save the configuration to the specified file.
     *
     * @param fileName
     *
     * @throws ConfigurationException
     */
    void save(String fileName) throws ConfigurationException;

    /**
     * Save the configuration to the specified file.
     *
     * @param file
     *
     * @throws ConfigurationException
     */
    void save(File file) throws ConfigurationException;

    /**
     * Save the configuration to the specified URL if it's a file URL.
     *
     * @param url
     *
     * @throws ConfigurationException
     */
    void save(URL url) throws ConfigurationException;

    /**
     * Save the configuration to the specified stream, using the encoding
     * returned by {@link #getEncoding()}.
     *
     * @param out
     *
     * @throws ConfigurationException
     */
    void save(OutputStream out) throws ConfigurationException;

    /**
     * Save the configuration to the specified stream, using the specified
     * encoding. If the encoding is null the default encoding is used.
     *
     * @param out
     * @param encoding
     * @throws ConfigurationException
     */
    void save(OutputStream out, String encoding) throws ConfigurationException;

    /**
     * Save the configuration to the specified writer.
     *
     * @param out the writer
     *
     * @throws ConfigurationException
     */
    void save(Writer out) throws ConfigurationException;

    /**
     * Return the name of the file.
     */
    String getFileName();

    /**
     * Set the name of the file.
     *
     * @param fileName the name of the file
     */
    void setFileName(String fileName);

    /**
     * Return the base path.
     */
    String getBasePath();

    /**
     * Set the base path. Relative configurations are loaded from this path.
     *
     * @param basePath the base path.
     */
    void setBasePath(String basePath);

    /**
     * Return the file where the configuration is stored.
     */
    File getFile();

    /**
     * Set the file where the configuration is stored.
     *
     * @param file
     */
    void setFile(File file);

    /**
     * Return the URL where the configuration is stored.
     */
    URL getURL();

    /**
     * The URL where the configuration is stored.
     *
     * @param url
     */
    void setURL(URL url);

    /**
     * Enable of disable the automatical saving of modified properties to the disk.
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
     * @since 1.1
     */
    ReloadingStrategy getReloadingStrategy();

    /**
     * Set the reloading strategy.
     *
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
     * @since 1.1
     */
    String getEncoding();

    /**
     * Set the encoding used to store the configuration file. Set the encoding
     * to null to use the default encoding.
     *
     * @since 1.1
     */
    void setEncoding(String encoding);

}
