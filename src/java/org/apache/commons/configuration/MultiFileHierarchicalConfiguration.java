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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Collection;
import java.util.HashMap;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.io.Reader;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.configuration.event.ConfigurationErrorListener;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationErrorEvent;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.ExpressionEngine;

/**
 * This class provides access to multiple configuration files that reside in a location that
 * can be specified by a pattern allowing applications to be multi-tenant.  For example,
 * providing a pattern of "file:///opt/config/${product}/${client}/config.xml" will result in
 * "product" and "client" being resolved on every call. The configuration resulting from the
 * resolved pattern will be saved for future access.
 * @since 1.6
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id:  $
 */
public class MultiFileHierarchicalConfiguration extends AbstractHierarchicalFileConfiguration
    implements ConfigurationListener, ConfigurationErrorListener
{
    /** FILE URL prefix */
    private static final String FILE_URL_PREFIX = "file:";

    /**
     * Prevent recursion while resolving unprefixed properties.
     */
    private static ThreadLocal recursive = new ThreadLocal()
    {
        protected synchronized Object initialValue()
        {
            return Boolean.FALSE;
        }
    };

    /** Map of configurations */
    private final Map configurationsMap = new HashMap();

    /** key pattern for configurationsMap */
    private String pattern;

    /** True if the constructor has finished */
    private boolean init;

    /**
     * Default Constructor.
     */
    public MultiFileHierarchicalConfiguration()
    {
        super();
        this.init = true;
    }

    /**
     * Construct the configuration with the specified pattern.
     * @param pathPattern The pattern to use to locate configuration files.
     */
    public MultiFileHierarchicalConfiguration(String pathPattern)
    {
        super();
        this.pattern = pathPattern;
        this.init = true;
    }

    /**
     * Set the File pattern
     * @param pathPattern The pattern for the path to the configuration.
     */
    public void setFilePattern(String pathPattern)
    {
        this.pattern = pathPattern;
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

    public void addProperty(String key, Object value)
    {
        this.getConfiguration().addProperty(key, value);
    }

    public void clear()
    {
        this.getConfiguration().clear();
    }

    public void clearProperty(String key)
    {
        this.getConfiguration().clearProperty(key);
    }

    public boolean containsKey(String key)
    {
        return this.getConfiguration().containsKey(key);
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue)
    {
        return this.getConfiguration().getBigDecimal(key, defaultValue);
    }

    public BigDecimal getBigDecimal(String key)
    {
        return this.getConfiguration().getBigDecimal(key);
    }

    public BigInteger getBigInteger(String key, BigInteger defaultValue)
    {
        return this.getConfiguration().getBigInteger(key, defaultValue);
    }

    public BigInteger getBigInteger(String key)
    {
        return this.getConfiguration().getBigInteger(key);
    }

    public boolean getBoolean(String key, boolean defaultValue)
    {
        return this.getConfiguration().getBoolean(key, defaultValue);
    }

    public Boolean getBoolean(String key, Boolean defaultValue)
    {
        return this.getConfiguration().getBoolean(key, defaultValue);
    }

    public boolean getBoolean(String key)
    {
        return this.getConfiguration().getBoolean(key);
    }

    public byte getByte(String key, byte defaultValue)
    {
        return this.getConfiguration().getByte(key, defaultValue);
    }

    public Byte getByte(String key, Byte defaultValue)
    {
        return this.getConfiguration().getByte(key, defaultValue);
    }

    public byte getByte(String key)
    {
        return this.getConfiguration().getByte(key);
    }

    public double getDouble(String key, double defaultValue)
    {
        return this.getConfiguration().getDouble(key, defaultValue);
    }

    public Double getDouble(String key, Double defaultValue)
    {
        return this.getConfiguration().getDouble(key, defaultValue);
    }

    public double getDouble(String key)
    {
        return this.getConfiguration().getDouble(key);
    }

    public float getFloat(String key, float defaultValue)
    {
        return this.getConfiguration().getFloat(key, defaultValue);
    }

    public Float getFloat(String key, Float defaultValue)
    {
        return this.getConfiguration().getFloat(key, defaultValue);
    }

    public float getFloat(String key)
    {
        return this.getConfiguration().getFloat(key);
    }

    public int getInt(String key, int defaultValue)
    {
        return this.getConfiguration().getInt(key, defaultValue);
    }

    public int getInt(String key)
    {
        return this.getConfiguration().getInt(key);
    }

    public Integer getInteger(String key, Integer defaultValue)
    {
        return this.getConfiguration().getInteger(key, defaultValue);
    }

    public Iterator getKeys()
    {
        return this.getConfiguration().getKeys();
    }

    public Iterator getKeys(String prefix)
    {
        return this.getConfiguration().getKeys(prefix);
    }

    public List getList(String key, List defaultValue)
    {
        return this.getConfiguration().getList(key, defaultValue);
    }

    public List getList(String key)
    {
        return this.getConfiguration().getList(key);
    }

    public long getLong(String key, long defaultValue)
    {
        return this.getConfiguration().getLong(key, defaultValue);
    }

    public Long getLong(String key, Long defaultValue)
    {
        return this.getConfiguration().getLong(key, defaultValue);
    }

    public long getLong(String key)
    {
        return this.getConfiguration().getLong(key);
    }

    public Properties getProperties(String key)
    {
        return this.getConfiguration().getProperties(key);
    }

    public Object getProperty(String key)
    {
        return this.getConfiguration().getProperty(key);
    }

    public short getShort(String key, short defaultValue)
    {
        return this.getConfiguration().getShort(key, defaultValue);
    }

    public Short getShort(String key, Short defaultValue)
    {
        return this.getConfiguration().getShort(key, defaultValue);
    }

    public short getShort(String key)
    {
        return this.getConfiguration().getShort(key);
    }

    public String getString(String key, String defaultValue)
    {
        return this.getConfiguration().getString(key, defaultValue);
    }

    public String getString(String key)
    {
        return this.getConfiguration().getString(key);
    }

    public String[] getStringArray(String key)
    {
        return this.getConfiguration().getStringArray(key);
    }

    public boolean isEmpty()
    {
        return this.getConfiguration().isEmpty();
    }

    public void setProperty(String key, Object value)
    {
        if (init)
        {
            this.getConfiguration().setProperty(key, value);
        }
    }

    public Configuration subset(String prefix)
    {
        return this.getConfiguration().subset(prefix);
    }

    public Node getRoot()
    {
        return this.getConfiguration().getRoot();
    }

    public void setRoot(Node node)
    {
        if (init)
        {
            this.getConfiguration().setRoot(node);
        }
        else
        {
            super.setRoot(node);
        }
    }

    public ConfigurationNode getRootNode()
    {
        return this.getConfiguration().getRootNode();
    }

    public void setRootNode(ConfigurationNode rootNode)
    {
        if (init)
        {
            this.getConfiguration().setRootNode(rootNode);
        }
        else
        {
            super.setRootNode(rootNode);
        }
    }

    public ExpressionEngine getExpressionEngine()
    {
        return super.getExpressionEngine();
    }

    public void setExpressionEngine(ExpressionEngine expressionEngine)
    {
        super.setExpressionEngine(expressionEngine);
    }

    public void addNodes(String key, Collection nodes)
    {
        this.getConfiguration().addNodes(key, nodes);
    }

    public SubnodeConfiguration configurationAt(String key, boolean supportUpdates)
    {
        return this.getConfiguration().configurationAt(key, supportUpdates);
    }

    public SubnodeConfiguration configurationAt(String key)
    {
        return this.getConfiguration().configurationAt(key);
    }

    public List configurationsAt(String key)
    {
        return this.getConfiguration().configurationsAt(key);
    }

    public void clearTree(String key)
    {
        this.getConfiguration().clearTree(key);
    }

    public int getMaxIndex(String key)
    {
        return this.getConfiguration().getMaxIndex(key);
    }

    public Configuration interpolatedConfiguration()
    {
        return this.getConfiguration().interpolatedConfiguration();
    }

    public void addConfigurationListener(ConfigurationListener l)
    {
        super.addConfigurationListener(l);
    }

    public boolean removeConfigurationListener(ConfigurationListener l)
    {
        return super.removeConfigurationListener(l);
    }

    public Collection getConfigurationListeners()
    {
        return super.getConfigurationListeners();
    }

    public void clearConfigurationListeners()
    {
        super.clearConfigurationListeners();
    }

    public void addErrorListener(ConfigurationErrorListener l)
    {
        super.addErrorListener(l);
    }

    public boolean removeErrorListener(ConfigurationErrorListener l)
    {
        return super.removeErrorListener(l);
    }

    public void clearErrorListeners()
    {
        super.clearErrorListeners();
    }

    public Collection getErrorListeners()
    {
        return super.getErrorListeners();
    }

    public void save(Writer writer) throws ConfigurationException
    {
        if (init)
        {
            this.getConfiguration().save(writer);
        }
    }

    public void load(Reader reader) throws ConfigurationException
    {
        if (init)
        {
            this.getConfiguration().load(reader);
        }
    }

    public void load() throws ConfigurationException
    {
        this.getConfiguration().load();
    }

    public void load(String fileName) throws ConfigurationException
    {
        this.getConfiguration().load(fileName);
    }

    public void load(File file) throws ConfigurationException
    {
        this.getConfiguration().load(file);
    }

    public void load(URL url) throws ConfigurationException
    {
        this.getConfiguration().load(url);
    }

    public void load(InputStream in) throws ConfigurationException
    {
        this.getConfiguration().load(in);
    }

    public void load(InputStream in, String encoding) throws ConfigurationException
    {
        this.getConfiguration().load(in, encoding);
    }

    public void save() throws ConfigurationException
    {
        this.getConfiguration().save();
    }

    public void save(String fileName) throws ConfigurationException
    {
        this.getConfiguration().save(fileName);
    }

    public void save(File file) throws ConfigurationException
    {
        this.getConfiguration().save(file);
    }

    public void save(URL url) throws ConfigurationException
    {
        this.getConfiguration().save(url);
    }

    public void save(OutputStream out) throws ConfigurationException
    {
        this.getConfiguration().save(out);
    }

    public void save(OutputStream out, String encoding) throws ConfigurationException
    {
        this.getConfiguration().save(out, encoding);
    }

    public void configurationChanged(ConfigurationEvent event)
    {
        if (event.getSource() instanceof XMLConfiguration)
        {
            Iterator iter = getConfigurationListeners().iterator();
            while (iter.hasNext())
            {
                ConfigurationListener listener = (ConfigurationListener) iter.next();
                listener.configurationChanged(event);
            }
        }
    }

    public void configurationError(ConfigurationErrorEvent event)
    {
        if (event.getSource() instanceof XMLConfiguration)
        {
            Iterator iter = getErrorListeners().iterator();
            while (iter.hasNext())
            {
                ConfigurationErrorListener listener = (ConfigurationErrorListener) iter.next();
                listener.configurationError(event);
            }
        }
    }

    /*
     * Don't allow resolveContainerStore to be called recursively.
     * @param key The key to resolve.
     * @return The value of the key.
     */
    protected Object resolveContainerStore(String key)
    {
        if (((Boolean) recursive.get()).booleanValue())
        {
            return null;
        }
        recursive.set(Boolean.TRUE);
        try
        {
            return super.resolveContainerStore(key);
        }
        finally
        {
            recursive.set(Boolean.FALSE);
        }
    }

    /**
     * Remove the current Configuration.
     */
    public void removeConfiguration()
    {
        String path = getSubstitutor().replace(pattern);
        synchronized (configurationsMap)
        {
            configurationsMap.remove(path);
        }
    }

    /**
     * First checks to see if the cache exists, if it does, get the associated Configuration.
     * If not it will load a new Configuration and save it in the cache.
     *
     * @return the Configuration associated with the current value of the path pattern.
     */
    private AbstractHierarchicalFileConfiguration getConfiguration()
    {
        if (pattern == null)
        {
            throw new ConfigurationRuntimeException("File pattern must be defined");
        }
        String path = getSubstitutor().replace(pattern);
        synchronized (configurationsMap)
        {
            if (configurationsMap.containsKey(path))
            {
                return (AbstractHierarchicalFileConfiguration) configurationsMap.get(path);
            }
        }

        if (path.equals(pattern))
        {
            XMLConfiguration configuration = new XMLConfiguration()
            {
                public void load() throws ConfigurationException
                {
                }
                public void save() throws ConfigurationException
                {
                }
            };
            synchronized (configurationsMap)
            {
                configurationsMap.put(pattern, configuration);
            }
            return configuration;
        }

        XMLConfiguration configuration = new XMLConfiguration();
        try
        {
            URL url = getURL(path);
            configuration.setURL(url);
            configuration.load();
            configuration.setExpressionEngine(getExpressionEngine());
            configuration.setReloadingStrategy(getReloadingStrategy());
            configuration.addConfigurationListener(this);
            configuration.addErrorListener(this);
            synchronized (configurationsMap)
            {
                if (!configurationsMap.containsKey(path))
                {
                    configurationsMap.put(path, configuration);
                }
            }
        }
        catch (ConfigurationException ce)
        {
            throw new ConfigurationRuntimeException(ce);
        }
        catch (FileNotFoundException fnfe)
        {
            throw new ConfigurationRuntimeException(fnfe);
        }

        return configuration;
    }

    private URL getURL(String resourceLocation) throws FileNotFoundException
    {
        if (resourceLocation == null)
        {
            throw new IllegalArgumentException("A path pattern must be configured");
        }
        try
        {
            // try URL
            return new URL(resourceLocation);
        }
        catch (MalformedURLException ex)
        {
            // no URL -> treat as file path
            try
            {
                return new URL(FILE_URL_PREFIX + resourceLocation);
            }
            catch (MalformedURLException ex2)
            {
                throw new FileNotFoundException("Resource location [" + resourceLocation
                        + "] is not a URL or a well-formed file path");
            }
        }
    }
}
