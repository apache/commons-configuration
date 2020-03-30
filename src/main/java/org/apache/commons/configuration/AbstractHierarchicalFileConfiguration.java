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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.event.ConfigurationErrorEvent;
import org.apache.commons.configuration.event.ConfigurationErrorListener;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.reloading.Reloadable;
import org.apache.commons.configuration.reloading.ReloadingStrategy;
import org.apache.commons.configuration.tree.ConfigurationNode;

/**
 * <p>Base class for implementing file based hierarchical configurations.</p>
 * <p>This class serves an analogous purpose as the
 * {@link AbstractFileConfiguration} class for non hierarchical
 * configurations. It behaves in exactly the same way, so please refer to the
 * documentation of {@code AbstractFileConfiguration} for further details.</p>
 *
 * @since 1.2
 *
 * @author Emmanuel Bourg
 * @version $Id$
 */
public abstract class AbstractHierarchicalFileConfiguration
extends HierarchicalConfiguration
implements FileConfiguration, ConfigurationListener, ConfigurationErrorListener, FileSystemBased,
        Reloadable
{
    /** */
    private static final long serialVersionUID = -2442591233300744836L;
    /** The global keepBackup flag.*/
    private static boolean keepBackupGlobal = false;
    /** The global backup files appendix.*/
    private static String appendixGlobal = "backup";
    /** Stores the delegate used for implementing functionality related to the
     * {@code FileConfiguration} interface.
     */
    private FileConfigurationDelegate delegate;

    /**
     * Creates a new instance of {@code AbstractHierarchicalFileConfiguration}.
     */
    protected AbstractHierarchicalFileConfiguration()
    {
        initialize();
    }

    /**
     * Creates a new instance of
     * {@code AbstractHierarchicalFileConfiguration} and copies the
     * content of the specified configuration into this object.
     *
     * @param c the configuration to copy
     * @since 1.4
     */
    protected AbstractHierarchicalFileConfiguration(HierarchicalConfiguration c)
    {
        super(c);
        initialize();
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
        delegate.setFileName(fileName);

        // load the file
        load();
    }

    /**
     * Creates and loads the configuration from the specified file.
     *
     * @param fileName The name of the plist file to load.
     * @throws ConfigurationException Error while loading the file
     */
    public AbstractHierarchicalFileConfiguration(String fileName, String backupAppendix) throws ConfigurationException
    {
        this();
        // store the file name
        delegate.setFileName(fileName);
        delegate.setBackupFileNameAppendix(backupAppendix);
        delegate.setKeepBackup(true);

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

    /**
     * Initializes this instance, mainly the internally used delegate object.
     */
    private void initialize()
    {
        delegate = createDelegate();
        initDelegate(delegate);
    }

    @Override
    protected void addPropertyDirect(String key, Object obj)
    {
        synchronized (delegate.getReloadLock())
        {
            super.addPropertyDirect(key, obj);
            delegate.possiblySave();
        }
    }

    @Override
    public void clearProperty(String key)
    {
        synchronized (delegate.getReloadLock())
        {
            super.clearProperty(key);
            delegate.possiblySave();
        }
    }

    @Override
    public void clearTree(String key)
    {
        synchronized (delegate.getReloadLock())
        {
            super.clearTree(key);
            delegate.possiblySave();
        }
    }

    @Override
    public void setProperty(String key, Object value)
    {
        synchronized (delegate.getReloadLock())
        {
            super.setProperty(key, value);
            delegate.possiblySave();
        }
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

    public void setKeepBackup(boolean keepBackup)
    {
        delegate.setKeepBackup(keepBackup);
    }

    public boolean isKeepBackup()
    {
        return delegate.isKeepBackup();
    }

    public void setBackupFileNameAppendix(String appendix)
    {
        delegate.setBackupFileNameAppendix(appendix);
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
        reload(false);
    }

    private boolean reload(boolean checkReload)
    {
        synchronized (delegate.getReloadLock())
        {
            setDetailEvents(false);
            try
            {
                return delegate.reload(checkReload);
            }
            finally
            {
                setDetailEvents(true);
            }
        }
    }

    /**
     * Reloads the associated configuration file. This method first clears the
     * content of this configuration, then the associated configuration file is
     * loaded again. Updates on this configuration which have not yet been saved
     * are lost. Calling this method is like invoking {@code reload()}
     * without checking the reloading strategy.
     *
     * @throws ConfigurationException if an error occurs
     * @since 1.7
     */
    public void refresh() throws ConfigurationException
    {
        delegate.refresh();
    }

    public String getEncoding()
    {
        return delegate.getEncoding();
    }

    public void setEncoding(String encoding)
    {
        delegate.setEncoding(encoding);
    }

    @Override
    public Object getReloadLock()
    {
        return delegate.getReloadLock();
    }

    @Override
    public boolean containsKey(String key)
    {
        reload();
        synchronized (delegate.getReloadLock())
        {
            return super.containsKey(key);
        }
    }

    @Override
    public Iterator<String> getKeys()
    {
        reload();
        synchronized (delegate.getReloadLock())
        {
            return super.getKeys();
        }
    }

    @Override
    public Iterator<String> getKeys(String prefix)
    {
        reload();
        synchronized (delegate.getReloadLock())
        {
            return super.getKeys(prefix);
        }
    }

    @Override
    public Object getProperty(String key)
    {
        if (reload(true))
        {
            // Avoid reloading again and getting the same error.
            synchronized (delegate.getReloadLock())
            {
                return super.getProperty(key);
            }
        }
        return null;
    }

    @Override
    public boolean isEmpty()
    {
        reload();
        synchronized (delegate.getReloadLock())
        {
            return super.isEmpty();
        }
    }

    /**
     * Directly adds sub nodes to this configuration. This implementation checks
     * whether auto save is necessary after executing the operation.
     *
     * @param key the key where the nodes are to be added
     * @param nodes a collection with the nodes to be added
     * @since 1.5
     */
    @Override
    public void addNodes(String key, Collection<? extends ConfigurationNode> nodes)
    {
        synchronized (delegate.getReloadLock())
        {
            super.addNodes(key, nodes);
            delegate.possiblySave();
        }
    }

    /**
     * Fetches a list of nodes, which are selected by the specified key. This
     * implementation will perform a reload if necessary.
     *
     * @param key the key
     * @return a list with the selected nodes
     */
    @Override
    protected List<ConfigurationNode> fetchNodeList(String key)
    {
        reload();
        synchronized (delegate.getReloadLock())
        {
            return super.fetchNodeList(key);
        }
    }

    /**
     * Reacts on changes of an associated subnode configuration. If the auto
     * save mechanism is active, the configuration must be saved.
     *
     * @param event the event describing the change
     * @since 1.5
     */
    @Override
    protected void subnodeConfigurationChanged(ConfigurationEvent event)
    {
        delegate.possiblySave();
        super.subnodeConfigurationChanged(event);
    }

    /**
     * Creates the file configuration delegate, i.e. the object that implements
     * functionality required by the {@code FileConfiguration} interface.
     * This base implementation will return an instance of the
     * {@code FileConfigurationDelegate} class. Derived classes may
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
        del.addConfigurationListener(this);
        del.addErrorListener(this);
        del.setLogger(getLogger());
        if (keepBackupGlobal) {
            del.setKeepBackup(keepBackupGlobal);
            del.setBackupFileNameAppendix(appendixGlobal);
        }
    }

    /**
     * Reacts on configuration change events triggered by the delegate. These
     * events are passed to the registered configuration listeners.
     *
     * @param event the triggered event
     * @since 1.3
     */
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

    public void configurationError(ConfigurationErrorEvent event)
    {
        fireError(event.getType(), event.getPropertyName(), event.getPropertyValue(),
                event.getCause());
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
        if (keepBackupGlobal) {
            this.delegate.setKeepBackup(keepBackupGlobal);
            this.delegate.setBackupFileNameAppendix(appendixGlobal);
        }
    }

    /**
     * Set the FileSystem to be used for this Configuration.
     * @param fileSystem The FileSystem to use.
     */
    public void setFileSystem(FileSystem fileSystem)
    {
        delegate.setFileSystem(fileSystem);
    }

    /**
     * Reset the FileSystem to the default;
     */
    public void resetFileSystem()
    {
        delegate.resetFileSystem();
    }

    /**
     * Retrieve the FileSystem being used.
     * @return The FileSystem.
     */
    public FileSystem getFileSystem()
    {
        return delegate.getFileSystem();
    }

    /**
     * Use this method to auto-configure all future delegate assignments.
     * <p>
     * <b>Using this feature requires at least a JDK7!</b>
     * 
     * @param keepBackup sets the global keep backup flag
     */
    public static void setKeepBackupGlobal(final boolean keepBackup) {
        keepBackupGlobal = keepBackup;
    }
    /**
     * Use this method to auto-configure all future delegate assignments.
     * 
     * @param appendix The appendix to use with all future delegates.
     */
    public static void setKeepBackupGlobal(final String appendix) {
    	appendixGlobal = appendix;
    }

    /**
     * A special implementation of the {@code FileConfiguration} interface that is
     * used internally to implement the {@code FileConfiguration} methods
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

        @Override
        public void clear()
        {
            AbstractHierarchicalFileConfiguration.this.clear();
        }
    }
}
