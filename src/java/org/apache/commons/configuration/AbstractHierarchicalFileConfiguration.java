/*
 * Copyright 2005-2006 The Apache Software Foundation.
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

import java.io.Reader;
import java.io.Writer;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.reloading.ReloadingStrategy;

/**
 * <p>Base class for implementing file based hierarchical configurations.</p>
 * <p>This class serves an analogous purpose as the
 * <code>{@link AbstractFileConfiguration}</code> class for non hierarchical
 * configurations. It behaves in exactly the same way, so please refer to the
 * documentation of <code>AbstractFileConfiguration</code> for further details.</p>
 *
 * @since 1.2
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public abstract class AbstractHierarchicalFileConfiguration
extends HierarchicalConfiguration implements FileConfiguration
{
    /** Stores the delegate used for implementing functionality related to the
     * <code>FileConfiguration</code> interface.
     */
    private FileConfigurationDelegate delegate;

    protected AbstractHierarchicalFileConfiguration()
    {
        delegate = createDelegate();
        initDelegate(delegate);
    }

    /**
     * Creates and loads the configuration from the specified file.
     *
     * @param fileName The name of the plist file to load.
     * @throws ConfigurationException Error while loading the file
     */
    public AbstractHierarchicalFileConfiguration(String fileName) throws ConfigurationException
    {
        this();
        // store the file name
        delegate.setPath(fileName);

        // load the file
        load();
    }

    /**
     * Creates and loads the configuration from the specified file.
     *
     * @param file The configuration file to load.
     * @throws ConfigurationException Error while loading the file
     */
    public AbstractHierarchicalFileConfiguration(File file) throws ConfigurationException
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
     * @param url The location of the configuration file to load.
     * @throws ConfigurationException Error while loading the file
     */
    public AbstractHierarchicalFileConfiguration(URL url) throws ConfigurationException
    {
        this();
        // set the URL and update the base path and the file name
        setURL(url);

        // load the file
        load();
    }

    protected void addPropertyDirect(String key, Object obj)
    {
        super.addPropertyDirect(key, obj);
        delegate.possiblySave();
    }

    public void clearProperty(String key)
    {
        super.clearProperty(key);
        delegate.possiblySave();
    }

    public void clearTree(String key)
    {
        super.clearTree(key);
        delegate.possiblySave();
    }

    public void setProperty(String key, Object value)
    {
        super.setProperty(key, value);
        delegate.possiblySave();
    }

    public void load() throws ConfigurationException
    {
        delegate.load();
    }

    public void load(String fileName) throws ConfigurationException
    {
        delegate.load(fileName);
    }

    public void load(File file) throws ConfigurationException
    {
        delegate.load(file);
    }

    public void load(URL url) throws ConfigurationException
    {
        delegate.load(url);
    }

    public void load(InputStream in) throws ConfigurationException
    {
        delegate.load(in);
    }

    public void load(InputStream in, String encoding) throws ConfigurationException
    {
        delegate.load(in, encoding);
    }

    public void save() throws ConfigurationException
    {
        delegate.save();
    }

    public void save(String fileName) throws ConfigurationException
    {
        delegate.save(fileName);
    }

    public void save(File file) throws ConfigurationException
    {
        delegate.save(file);
    }

    public void save(URL url) throws ConfigurationException
    {
        delegate.save(url);
    }

    public void save(OutputStream out) throws ConfigurationException
    {
        delegate.save(out);
    }

    public void save(OutputStream out, String encoding) throws ConfigurationException
    {
        delegate.save(out, encoding);
    }

    public String getFileName()
    {
        return delegate.getFileName();
    }

    public void setFileName(String fileName)
    {
        delegate.setFileName(fileName);
    }

    public String getBasePath()
    {
        return delegate.getBasePath();
    }

    public void setBasePath(String basePath)
    {
        delegate.setBasePath(basePath);
    }

    public File getFile()
    {
        return delegate.getFile();
    }

    public void setFile(File file)
    {
        delegate.setFile(file);
    }

    public URL getURL()
    {
        return delegate.getURL();
    }

    public void setURL(URL url)
    {
        delegate.setURL(url);
    }

    public void setAutoSave(boolean autoSave)
    {
        delegate.setAutoSave(autoSave);
    }

    public boolean isAutoSave()
    {
        return delegate.isAutoSave();
    }

    public ReloadingStrategy getReloadingStrategy()
    {
        return delegate.getReloadingStrategy();
    }

    public void setReloadingStrategy(ReloadingStrategy strategy)
    {
        delegate.setReloadingStrategy(strategy);
    }

    public void reload()
    {
        setDetailEvents(false);
        try
        {
            delegate.reload();
        }
        finally
        {
            setDetailEvents(true);
        }
    }

    public String getEncoding()
    {
        return delegate.getEncoding();
    }

    public void setEncoding(String encoding)
    {
        delegate.setEncoding(encoding);
    }

    public boolean containsKey(String key)
    {
        reload();
        return super.containsKey(key);
    }

    public Iterator getKeys(String prefix)
    {
        reload();
        return super.getKeys(prefix);
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

    /**
     * Creates the file configuration delegate, i.e. the object that implements
     * functionality required by the <code>FileConfiguration</code> interface.
     * This base implementation will return an instance of the
     * <code>FileConfigurationDelegate</code> class. Derived classes may
     * override it to create a different delegate object.
     *
     * @return the file configuration delegate
     */
    protected FileConfigurationDelegate createDelegate()
    {
        return new FileConfigurationDelegate();
    }

    /**
     * Helper method for initializing the file configuration delegate.
     *
     * @param del the delegate
     */
    private void initDelegate(FileConfigurationDelegate del)
    {
        del.addConfigurationListener(new ConfigurationListener()
        {
            public void configurationChanged(ConfigurationEvent event)
            {
                // deliver reload events to registered listeners
                setDetailEvents(true);
                try
                {
                    fireEvent(event.getType(), event.getPropertyName(), event
                            .getPropertyValue(), event.isBeforeUpdate());
                }
                finally
                {
                    setDetailEvents(false);
                }
            }
        });
    }

    /**
     * Returns the file configuration delegate.
     *
     * @return the delegate
     */
    protected FileConfigurationDelegate getDelegate()
    {
        return delegate;
    }

    /**
     * Allows to set the file configuration delegate.
     * @param delegate the new delegate
     */
    protected void setDelegate(FileConfigurationDelegate delegate)
    {
        this.delegate = delegate;
    }

    /**
     * A special implementation of the <code>FileConfiguration</code> interface that is
     * used internally to implement the <code>FileConfiguration</code> methods
     * for hierarchical configurations.
     */
    protected class FileConfigurationDelegate extends AbstractFileConfiguration
    {
        public void load(Reader in) throws ConfigurationException
        {
            AbstractHierarchicalFileConfiguration.this.load(in);
        }

        public void save(Writer out) throws ConfigurationException
        {
            AbstractHierarchicalFileConfiguration.this.save(out);
        }

        public void clear()
        {
            AbstractHierarchicalFileConfiguration.this.clear();
        }
    }
}
