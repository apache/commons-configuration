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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.Iterator;

import org.apache.commons.configuration.reloading.InvariantReloadingStrategy;
import org.apache.commons.configuration.reloading.ReloadingStrategy;

/**
 * Partial implementation of the <code>FileConfiguration</code> interface.
 * Developpers of file based configuration may wan't to extend this class,
 * the two methods left to implement are {@see AbstractFileConfiguration#load(Reader)}
 * and {@see AbstractFileConfiguration#save(Reader)}.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 1.7 $, $Date: 2004/10/19 11:44:31 $
 * @since 1.0-rc2
 */
public abstract class AbstractFileConfiguration extends BaseConfiguration implements FileConfiguration
{
    protected String fileName;
    protected String basePath;
    protected URL url;
    protected boolean autoSave;
    protected ReloadingStrategy strategy;
    private Object reloadLock = new Object();

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
     * Creates and loads the configuration from the specified file.
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
        this.fileName = fileName;

        // locate the file
        url = ConfigurationUtils.locate(fileName);

        // update the base path
        setBasePath(ConfigurationUtils.getBasePath(url));

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
        load();
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
     * Load the configuration from the underlying URL. If the URL is not
     * specified, it attempts to locate the specified file name.
     *
     * @throws ConfigurationException
     */
    public void load() throws ConfigurationException
    {
        if (url == null)
        {
            load(fileName);
        }
        else
        {
            load(url);
        }
    }

    /**
     * Locate the specified file and load the configuration.
     *
     * @param fileName the name of the file loaded
     *
     * @throws ConfigurationException
     */
    public void load(String fileName) throws ConfigurationException
    {
        try
        {
            URL url = ConfigurationUtils.locate(basePath, fileName);
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
     * Load the configuration from the specified file.
     *
     * @param file the loaded file
     *
     * @throws ConfigurationException
     */
    public void load(File file) throws ConfigurationException
    {
        try
        {
            load(file.toURL());
        }
        catch (MalformedURLException e)
        {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    /**
     * Load the configuration from the specified URL.
     *
     * @param url the URL of the file loaded
     *
     * @throws ConfigurationException
     */
    public void load(URL url) throws ConfigurationException
    {
        InputStream in = null;

        try
        {
            in = url.openStream();
            load(in);
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
     * Load the configuration from the specified stream, using the default
     * platform specific encoding.
     *
     * @param in the input stream
     *
     * @throws ConfigurationException
     */
    public void load(InputStream in) throws ConfigurationException
    {
        load(in, null);
    }

    /**
     * Load the configuration from the specified stream, using the specified
     * encoding. If the encoding is null the default encoding is used.
     *
     * @param in the input stream
     * @param encoding the encoding used. <code>null</code> to use the default encoding
     *
     * @throws ConfigurationException
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
                throw new ConfigurationException("The requested encoding is not supported, try the default encoding.", e);
            }
        }

        if (reader == null)
        {
            reader = new InputStreamReader(in);
        }

        load(reader);
    }

    /**
     * Save the configuration.
     *
     * @throws ConfigurationException
     */
    public void save() throws ConfigurationException
    {
        save(fileName);
        strategy.init();
    }

    /**
     * Save the configuration to the specified file. This doesn't change the
     * source of the configuration, use setFileName() if you need it.
     *
     * @param fileName
     *
     * @throws ConfigurationException
     */
    public void save(String fileName) throws ConfigurationException
    {
        try
        {
            // create a new file
            save(ConfigurationUtils.constructFile(basePath, fileName));
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
     * @param url
     *
     * @throws ConfigurationException
     */
    public void save(URL url) throws ConfigurationException
    {
        if ("file".equals(url.getProtocol()))
        {
            save(new File(url.getFile()));
        }
    }

    /**
     * Save the configuration to the specified file. This doesn't change the
     * source of the configuration, use setFile() if you need it.
     *
     * @param file
     *
     * @throws ConfigurationException
     */
    public void save(File file) throws ConfigurationException
    {
        OutputStream out = null;

        try
        {
            out = new FileOutputStream(file);
            save(out);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
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
     * Save the configuration to the specified stream.
     *
     * @param out
     *
     * @throws ConfigurationException
     */
    public void save(OutputStream out) throws ConfigurationException
    {
        save(out, null);
    }

    /**
     * Save the configuration to the specified stream, using the specified
     * encoding. If the encoding is null the default encoding is used.
     *
     * @param out
     * @param encoding
     * @throws ConfigurationException
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
                throw new ConfigurationException("The requested encoding is not supported, try the default encoding.", e);
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
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * Set the name of the file.
     *
     * @param fileName the name of the file
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;

        // update the URL
        url = ConfigurationUtils.locate(basePath, fileName);
    }

    /**
     * Return the base path.
     */
    public String getBasePath()
    {
        return basePath;
    }

    /**
     * Set the base path. Relative configurations are loaded from this path.
     *
     * @param basePath the base path.
     */
    public void setBasePath(String basePath)
    {
        this.basePath = basePath;

        // todo: update the url
    }

    /**
     * Return the file where the configuration is stored.
     */
    public File getFile()
    {
        if (url != null && "file".equals(url.getProtocol()))
        {
            return new File(url.getFile());
        }
        else
        {
            return ConfigurationUtils.constructFile(getBasePath(), getFileName());
        }
    }

    /**
     * Set the file where the configuration is stored.
     *
     * @param file
     */
    public void setFile(File file)
    {
        if (file != null)
        {
            try
            {
                setURL(file.toURL());
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            url = null;
        }
    }

    /**
     * Return the URL where the configuration is stored.
     */
    public URL getURL()
    {
        return url;
    }

    /**
     * The URL where the configuration is stored.
     *
     * @param url
     */
    public void setURL(URL url)
    {
        this.url = url;

        // update the base path
        basePath = ConfigurationUtils.getBasePath(url);
        if (basePath != null && basePath.startsWith("file:"))
        {
            basePath = basePath.substring(5);
        }

        // update the file name
        fileName = ConfigurationUtils.getFileName(url);
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
            if (strategy.reloadingRequired())
            {
                try
                {
                    clear();
                    load();

                    // notify the strategy
                    strategy.reloadingPerformed();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    // todo rollback the changes if the file can't be reloaded
                }
            }
        }
    }

    protected Object getPropertyDirect(String key)
    {
        reload();
        return super.getPropertyDirect(key);
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
}
