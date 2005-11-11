/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.configuration.reloading.InvariantReloadingStrategy;
import org.apache.commons.configuration.reloading.ReloadingStrategy;
import org.apache.commons.lang.StringUtils;

/**
 * <p>Partial implementation of the <code>FileConfiguration</code> interface.
 * Developpers of file based configuration may want to extend this class,
 * the two methods left to implement are <code>{@link FileConfiguration#load(Reader)}</code>
 * and <code>{@link FileConfiguration#save(Writer)}.</p>
 * <p>This base class already implements a couple of ways to specify the location
 * of the file this configuration is based on. The following possibilities
 * exist:
 * <ul><li>URLs: With the method <code>setURL()</code> a full URL to the
 * configuration source can be specified. This is the most flexible way. Note
 * that the <code>save()</code> methods support only <em>file:</em> URLs.</li>
 * <li>Files: The <code>setFile()</code> method allows to specify the
 * configuration source as a file. This can be either a relative or an
 * absolute file. In the former case the file is resolved based on the current
 * directory.</li>
 * <li>As file paths in string form: With the <code>setPath()</code> method a
 * full path to a configuration file can be provided as a string.</li>
 * <li>Separated as base path and file name: This is the native form in which
 * the location is stored. The base path is a string defining either a local
 * directory or a URL. It can be set using the <code>setBasePath()</code>
 * method. The file name, non surprisingly, defines the name of the configuration
 * file.</li></ul></p>
 * <p>Note that the <code>load()</code> methods do not wipe out the configuration's
 * content before the new configuration file is loaded. Thus it is very easy to
 * construct a union configuration by simply loading multiple configuration
 * files, e.g.</p>
 * <p><pre>
 * config.load(configFile1);
 * config.load(configFile2);
 * </pre></p>
 * <p>After executing this code fragment, the resulting configuration will
 * contain both the properties of configFile1 and configFile2. On the other
 * hand, if the current configuration file is to be reloaded, <code>clear()</code>
 * should be called first. Otherwise the properties are doubled. This behavior
 * is analogous to the behavior of the <code>load(InputStream)</code> method
 * in <code>java.util.Properties</code>.</p>
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 * @since 1.0-rc2
 */
public abstract class AbstractFileConfiguration extends BaseConfiguration implements FileConfiguration
{
    /** Stores the file name.*/
    protected String fileName;

    /** Stores the base path.*/
    protected String basePath;

    /** The auto save flag.*/
    protected boolean autoSave;

    /** Holds a reference to the reloading strategy.*/
    protected ReloadingStrategy strategy;

    /** A lock object for protecting reload operations.*/
    private Object reloadLock = new Object();

    /** Stores the encoding of the configuration file.*/
    private String encoding;

    /** Stores the URL from which the configuration file was loaded.*/
    private URL sourceURL;

    /** A counter that prohibits reloading.*/
    private int noReload;

    /**
     * Default constructor
     *
     * @since 1.1
     */
    public AbstractFileConfiguration()
    {
        setReloadingStrategy(new InvariantReloadingStrategy());
    }

    /**
     * Creates and loads the configuration from the specified file. The passed
     * in string must be a valid file name, either absolute or relativ.
     *
     * @param fileName The name of the file to load.
     *
     * @throws ConfigurationException Error while loading the file
     * @since 1.1
     */
    public AbstractFileConfiguration(String fileName) throws ConfigurationException
    {
        this();

        // store the file name
        setPath(fileName);

        // load the file
        load();
    }

    /**
     * Creates and loads the configuration from the specified file.
     *
     * @param file The file to load.
     * @throws ConfigurationException Error while loading the file
     * @since 1.1
     */
    public AbstractFileConfiguration(File file) throws ConfigurationException
    {
        this();

        // set the file and update the url, the base path and the file name
        setFile(file);

        // load the file
        if (file.exists())
        {
            load();
        }
    }

    /**
     * Creates and loads the configuration from the specified URL.
     *
     * @param url The location of the file to load.
     * @throws ConfigurationException Error while loading the file
     * @since 1.1
     */
    public AbstractFileConfiguration(URL url) throws ConfigurationException
    {
        this();

        // set the URL and update the base path and the file name
        setURL(url);

        // load the file
        load();
    }

    /**
     * Load the configuration from the underlying location.
     *
     * @throws ConfigurationException if loading of the configuration fails
     */
    public void load() throws ConfigurationException
    {
        if (sourceURL != null)
        {
            load(sourceURL);
        }
        else
        {
            load(getFileName());
        }
    }

    /**
     * Locate the specified file and load the configuration. This does not
     * change the source of the configuration (i.e. the internally maintained file name).
     * Use one of the setter methods for this purpose.
     *
     * @param fileName the name of the file to be loaded
     * @throws ConfigurationException if an error occurs
     */
    public void load(String fileName) throws ConfigurationException
    {
        try
        {
            URL url = ConfigurationUtils.locate(basePath, fileName);

            if (url == null)
            {
                throw new ConfigurationException("Cannot locate configuration source " + fileName);
            }
            load(url);
        }
        catch (ConfigurationException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    /**
     * Load the configuration from the specified file. This does not change
     * the source of the configuration (i.e. the internally maintained file
     * name). Use one of the setter methods for this purpose.
     *
     * @param file the file to load
     * @throws ConfigurationException if an error occurs
     */
    public void load(File file) throws ConfigurationException
    {
        try
        {
            load(file.toURL());
        }
        catch (ConfigurationException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    /**
     * Load the configuration from the specified URL. This does not change the
     * source of the configuration (i.e. the internally maintained file name).
     * Use on of the setter methods for this purpose.
     *
     * @param url the URL of the file to be loaded
     * @throws ConfigurationException if an error occurs
     */
    public void load(URL url) throws ConfigurationException
    {
        if (sourceURL == null)
        {
            if (StringUtils.isEmpty(getBasePath()))
            {
                // ensure that we have a valid base path
                setBasePath(url.toString());
            }
            sourceURL = url;
        }

        // throw an exception if the target URL is a directory
        File file = ConfigurationUtils.fileFromURL(url);
        if (file != null && file.isDirectory())
        {
            throw new ConfigurationException("Cannot load a configuration from a directory");
        }

        InputStream in = null;

        try
        {
            in = url.openStream();
            load(in);
        }
        catch (ConfigurationException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ConfigurationException(e.getMessage(), e);
        }
        finally
        {
            // close the input stream
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Load the configuration from the specified stream, using the encoding
     * returned by {@link #getEncoding()}.
     *
     * @param in the input stream
     *
     * @throws ConfigurationException if an error occurs during the load operation
     */
    public void load(InputStream in) throws ConfigurationException
    {
        load(in, getEncoding());
    }

    /**
     * Load the configuration from the specified stream, using the specified
     * encoding. If the encoding is null the default encoding is used.
     *
     * @param in the input stream
     * @param encoding the encoding used. <code>null</code> to use the default encoding
     *
     * @throws ConfigurationException if an error occurs during the load operation
     */
    public void load(InputStream in, String encoding) throws ConfigurationException
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
                        "The requested encoding is not supported, try the default encoding.", e);
            }
        }

        if (reader == null)
        {
            reader = new InputStreamReader(in);
        }

        load(reader);
    }

    /**
     * Save the configuration. Before this method can be called a valid file
     * name must have been set.
     *
     * @throws ConfigurationException if an error occurs or no file name has
     * been set yet
     */
    public void save() throws ConfigurationException
    {
        if (getFileName() == null)
        {
            throw new ConfigurationException("No file name has been set!");
        }

        if (sourceURL != null)
        {
            save(sourceURL);
        }
        else
        {
            save(fileName);
        }
        strategy.init();
    }

    /**
     * Save the configuration to the specified file. This doesn't change the
     * source of the configuration, use setFileName() if you need it.
     *
     * @param fileName the file name
     *
     * @throws ConfigurationException if an error occurs during the save operation
     */
    public void save(String fileName) throws ConfigurationException
    {
        try
        {
            File file = ConfigurationUtils.getFile(basePath, fileName);
            if (file == null)
            {
                throw new ConfigurationException("Invalid file name for save: " + fileName);
            }
            save(file);
        }
        catch (ConfigurationException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    /**
     * Save the configuration to the specified URL if it's a file URL.
     * This doesn't change the source of the configuration, use setURL()
     * if you need it.
     *
     * @param url the URL
     *
     * @throws ConfigurationException if an error occurs during the save operation
     */
    public void save(URL url) throws ConfigurationException
    {
        File file = ConfigurationUtils.fileFromURL(url);
        if (file != null)
        {
            save(file);
        }
        else
        {
            throw new ConfigurationException("Could not save to URL " + url);
        }
    }

    /**
     * Save the configuration to the specified file. The file is created
     * automatically if it doesn't exist. This doesn't change the source
     * of the configuration, use {@link #setFile} if you need it.
     *
     * @param file the target file
     *
     * @throws ConfigurationException if an error occurs during the save operation
     */
    public void save(File file) throws ConfigurationException
    {
        OutputStream out = null;

        try
        {
            // create the file if necessary
            createPath(file);
            out = new FileOutputStream(file);
            save(out);
        }
        catch (IOException e)
        {
            throw new ConfigurationException(e.getMessage(), e);
        }
        finally
        {
            // close the output stream
            try
            {
                if (out != null)
                {
                    out.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save the configuration to the specified stream, using the encoding
     * returned by {@link #getEncoding()}.
     *
     * @param out the output stream
     *
     * @throws ConfigurationException if an error occurs during the save operation
     */
    public void save(OutputStream out) throws ConfigurationException
    {
        save(out, getEncoding());
    }

    /**
     * Save the configuration to the specified stream, using the specified
     * encoding. If the encoding is null the default encoding is used.
     *
     * @param out the output stream
     * @param encoding the encoding to use
     * @throws ConfigurationException if an error occurs during the save operation
     */
    public void save(OutputStream out, String encoding) throws ConfigurationException
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
                        "The requested encoding is not supported, try the default encoding.", e);
            }
        }

        if (writer == null)
        {
            writer = new OutputStreamWriter(out);
        }

        save(writer);
    }

    /**
     * Return the name of the file.
     *
     * @return the file name
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * Set the name of the file. The passed in file name should not contain a
     * path. Use <code>{@link AbstractFileConfiguration#setPath(String)
     * setPath()}</code> to set a full qualified file name.
     *
     * @param fileName the name of the file
     */
    public void setFileName(String fileName)
    {
        sourceURL = null;
        this.fileName = fileName;
    }

    /**
     * Return the base path.
     *
     * @return the base path
     */
    public String getBasePath()
    {
        return basePath;
    }

    /**
     * Set the base path. Relative configurations are loaded from this path. The
     * base path can be either a path to a directory or a URL.
     *
     * @param basePath the base path.
     */
    public void setBasePath(String basePath)
    {
        sourceURL = null;
        this.basePath = basePath;
    }

    /**
     * Return the file where the configuration is stored. If the base path is a
     * URL with a protocol different than &quot;file&quot;, the return value
     * will not point to a valid file object.
     *
     * @return the file where the configuration is stored; this can be <b>null</b>
     */
    public File getFile()
    {
        if (getFileName() == null)
        {
            return null;
        }
        else
        {
            if (sourceURL != null)
            {
                return ConfigurationUtils.fileFromURL(sourceURL);
            }
            else
            {
                return ConfigurationUtils.getFile(getBasePath(), getFileName());
            }
        }
    }

    /**
     * Set the file where the configuration is stored. The passed in file is
     * made absolute if it is not yet. Then the file's path component becomes
     * the base path and its name component becomes the file name.
     *
     * @param file the file where the configuration is stored
     */
    public void setFile(File file)
    {
        sourceURL = null;
        setFileName(file.getName());
        setBasePath((file.getParentFile() != null) ? file.getParentFile()
                .getAbsolutePath() : null);
    }

    /**
     * Returns the full path to the file this configuration is based on. The
     * return value is valid only if this configuration is based on a file on
     * the local disk.
     *
     * @return the full path to the configuration file
     */
    public String getPath()
    {
        return getFile().getAbsolutePath();
    }

    /**
     * Sets the location of this configuration as a full path name. The passed
     * in path should represent a valid file name.
     *
     * @param path the full path name of the configuration file
     */
    public void setPath(String path)
    {
        setFile(new File(path));
    }

    /**
     * Return the URL where the configuration is stored.
     *
     * @return the configuration's location as URL
     */
    public URL getURL()
    {
        return (sourceURL != null) ? sourceURL
                : ConfigurationUtils.locate(getBasePath(), getFileName());
    }

    /**
     * Set the location of this configuration as a URL. For loading this can be
     * an arbitrary URL with a supported protocol. If the configuration is to
     * be saved, too, a URL with the &quot;file&quot; protocol should be
     * provided.
     *
     * @param url the location of this configuration as URL
     */
    public void setURL(URL url)
    {
        setBasePath(ConfigurationUtils.getBasePath(url));
        setFileName(ConfigurationUtils.getFileName(url));
    }

    public void setAutoSave(boolean autoSave)
    {
        this.autoSave = autoSave;
    }

    public boolean isAutoSave()
    {
        return autoSave;
    }

    /**
     * Save the configuration if the automatic persistence is enabled
     * and if a file is specified.
     */
    protected void possiblySave()
    {
        if (autoSave && fileName != null)
        {
            try
            {
                save();
            }
            catch (ConfigurationException e)
            {
                throw new ConfigurationRuntimeException("Failed to auto-save", e);
            }
        }
    }

    protected void addPropertyDirect(String key, Object obj)
    {
        super.addPropertyDirect(key, obj);
        possiblySave();
    }

    public void clearProperty(String key)
    {
        super.clearProperty(key);
        possiblySave();
    }

    public ReloadingStrategy getReloadingStrategy()
    {
        return strategy;
    }

    public void setReloadingStrategy(ReloadingStrategy strategy)
    {
        this.strategy = strategy;
        strategy.setConfiguration(this);
        strategy.init();
    }

    public void reload()
    {
        synchronized (reloadLock)
        {
            if (noReload == 0)
            {
                try
                {
                    enterNoReload(); // avoid reentrant calls

                    if (strategy.reloadingRequired())
                    {
                        clear();
                        load();

                        // notify the strategy
                        strategy.reloadingPerformed();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    // todo rollback the changes if the file can't be reloaded
                }
                finally
                {
                    exitNoReload();
                }
            }
        }
    }

    /**
     * Enters the &quot;No reloading mode&quot;. As long as this mode is active
     * no reloading will be performed. This is necessary for some
     * implementations of <code>save()</code> in derived classes, which may
     * cause a reload while accessing the properties to save. This may cause the
     * whole configuration to be erased. To avoid this, this method can be
     * called first. After a call to this method there always must be a
     * corresponding call of <code>{@link #exitNoReload()}</code> later! (If
     * necessary, <code>finally</code> blocks must be used to ensure this.
     */
    protected void enterNoReload()
    {
        synchronized (reloadLock)
        {
            noReload++;
        }
    }

    /**
     * Leaves the &quot;No reloading mode&quot;.
     *
     * @see #enterNoReload()
     */
    protected void exitNoReload()
    {
        synchronized (reloadLock)
        {
            if (noReload > 0) // paranoia check
            {
                noReload--;
            }
        }
    }

    public Object getProperty(String key)
    {
        reload();
        return super.getProperty(key);
    }

    public boolean isEmpty()
    {
        reload();
        return super.isEmpty();
    }

    public boolean containsKey(String key)
    {
        reload();
        return super.containsKey(key);
    }

    public Iterator getKeys()
    {
        reload();
        return super.getKeys();
    }

    /**
     * Create the path to the specified file.
     *
     * @param file the target file
     */
    private void createPath(File file)
    {
        if (file != null)
        {
            // create the path to the file if the file doesn't exist
            if (!file.exists())
            {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists())
                {
                    parent.mkdirs();
                }
            }
        }
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }
}
