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
package org.apache.commons.configuration2;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.configuration2.event.ConfigurationErrorEvent;
import org.apache.commons.configuration2.event.ConfigurationErrorListener;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.ConfigurationListener;
import org.apache.commons.configuration2.expr.ExpressionEngine;
import org.apache.commons.configuration2.reloading.ReloadingStrategy;
import org.apache.commons.configuration2.resolver.EntityResolverSupport;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.xml.sax.EntityResolver;

/**
 * This class provides access to multiple configuration files that reside in a location that
 * can be specified by a pattern allowing applications to be multi-tenant.  For example,
 * providing a pattern of "file:///opt/config/${product}/${client}/config.xml" will result in
 * "product" and "client" being resolved on every call. The configuration resulting from the
 * @since 1.6
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id:  $resolved pattern will be saved for future access.
 *
 */
public class MultiFileHierarchicalConfiguration extends AbstractHierarchicalFileConfiguration
    implements ConfigurationListener, ConfigurationErrorListener, EntityResolverSupport
{
    /**
     * Prevent recursion while resolving unprefixed properties.
     */
    private static ThreadLocal<Boolean> recursive = new ThreadLocal<Boolean>()
    {
        protected synchronized Boolean initialValue()
        {
            return Boolean.FALSE;
        }
    };

    /** Map of configurations */
    private ConcurrentMap<String, XMLConfiguration> configurationsMap =
            new ConcurrentHashMap<String, XMLConfiguration>();

    /** key pattern for configurationsMap */
    private String pattern;

    /** True if the constructor has finished */
    private boolean init;

    /** Return an empty configuration if loading fails */
    private boolean ignoreException = true;

    /** Capture the schema validation setting */
    private boolean schemaValidation;

    /** Stores a flag whether DTD or Schema validation should be performed.*/
    private boolean validating;

    /** A flag whether attribute splitting is disabled.*/
    private boolean attributeSplittingDisabled;

    /** The Logger name to use */
    private String loggerName = "";

    /** The Reloading strategy to use on created configurations */
    private ReloadingStrategy fileStrategy;

    /** The EntityResolver */
    private EntityResolver entityResolver;

    /**
     * Default Constructor
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

    public void setLoggerName(String name)
    {
        this.loggerName = name;
    }

    /**
     * Set the File pattern
     * @param pathPattern The pattern for the path to the configuration.
     */
    public void setFilePattern(String pathPattern)
    {
        this.pattern = pathPattern;
    }

    public boolean isSchemaValidation()
    {
        return schemaValidation;
    }

    public void setSchemaValidation(boolean schemaValidation)
    {
        this.schemaValidation = schemaValidation;
    }

    public boolean isValidating()
    {
        return validating;
    }

    public void setValidating(boolean validating)
    {
        this.validating = validating;
    }

    public boolean isAttributeSplittingDisabled()
    {
        return attributeSplittingDisabled;
    }

    public void setAttributeSplittingDisabled(boolean attributeSplittingDisabled)
    {
        this.attributeSplittingDisabled = attributeSplittingDisabled;
    }

    public ReloadingStrategy getReloadingStrategy()
    {
        return fileStrategy;
    }

    public void setReloadingStrategy(ReloadingStrategy strategy)
    {
        this.fileStrategy = strategy;
    }

    public void setEntityResolver(EntityResolver entityResolver)
    {
        this.entityResolver = entityResolver;
    }

    public EntityResolver getEntityResolver()
    {
        return this.entityResolver;
    }

    /**
     * Set to true if an empty Configuration should be returned when loading fails. If
     * false an exception will be thrown.
     * @param ignoreException The ignore value.
     */
    public void setIgnoreException(boolean ignoreException)
    {
        this.ignoreException = ignoreException;
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

    @Override
    public Iterator<String> getKeys()
    {
        return this.getConfiguration().getKeys();
    }

    @Override
    public Iterator<String> getKeys(String prefix)
    {
        return this.getConfiguration().getKeys(prefix);
    }

    @Override
    public <T> List<T> getList(String key, List<T> defaultValue)
    {
        return this.getConfiguration().getList(key, defaultValue);
    }

    @Override
    public <T> List<T> getList(String key)
    {
        return this.getConfiguration().getList(key);
    }

    @Override
    public long getLong(String key, long defaultValue)
    {
        return this.getConfiguration().getLong(key, defaultValue);
    }

    @Override
    public Long getLong(String key, Long defaultValue)
    {
        return this.getConfiguration().getLong(key, defaultValue);
    }

    @Override
    public long getLong(String key)
    {
        return this.getConfiguration().getLong(key);
    }

    @Override
    public Properties getProperties(String key)
    {
        return this.getConfiguration().getProperties(key);
    }

    @Override
    public Object getProperty(String key)
    {
        return this.getConfiguration().getProperty(key);
    }

    @Override
    public short getShort(String key, short defaultValue)
    {
        return this.getConfiguration().getShort(key, defaultValue);
    }

    @Override
    public Short getShort(String key, Short defaultValue)
    {
        return this.getConfiguration().getShort(key, defaultValue);
    }

    @Override
    public short getShort(String key)
    {
        return this.getConfiguration().getShort(key);
    }

    @Override
    public String getString(String key, String defaultValue)
    {
        return this.getConfiguration().getString(key, defaultValue);
    }

    @Override
    public String getString(String key)
    {
        return this.getConfiguration().getString(key);
    }

    @Override
    public String[] getStringArray(String key)
    {
        return this.getConfiguration().getStringArray(key);
    }

    @Override
    public boolean isEmpty()
    {
        return this.getConfiguration().isEmpty();
    }

    @Override
    public void setProperty(String key, Object value)
    {
        if (init)
        {
            this.getConfiguration().setProperty(key, value);
        }
    }

    @Override
    public Configuration subset(String prefix)
    {
        return this.getConfiguration().subset(prefix);
    }

    @Override
    public ExpressionEngine getExpressionEngine()
    {
        return super.getExpressionEngine();
    }

    @Override
    public void setExpressionEngine(ExpressionEngine expressionEngine)
    {
        super.setExpressionEngine(expressionEngine);
    }

    @Override
    public void addNodes(String key, Collection<? extends ConfigurationNode> nodes)
    {
        this.getConfiguration().addNodes(key, nodes);
    }

    @Override
    public SubConfiguration<ConfigurationNode> configurationAt(String key, boolean supportUpdates)
    {
        return this.getConfiguration().configurationAt(key, supportUpdates);
    }

    @Override
    public SubConfiguration<ConfigurationNode> configurationAt(String key)
    {
        return this.getConfiguration().configurationAt(key);
    }

    @Override
    public List<SubConfiguration<ConfigurationNode>> configurationsAt(String key)
    {
        return this.getConfiguration().configurationsAt(key);
    }

    @Override
    public void clearTree(String key)
    {
        this.getConfiguration().clearTree(key);
    }

    @Override
    public int getMaxIndex(String key)
    {
        return this.getConfiguration().getMaxIndex(key);
    }

    @Override
    public Configuration interpolatedConfiguration()
    {
        return this.getConfiguration().interpolatedConfiguration();
    }

    @Override
    public void addConfigurationListener(ConfigurationListener l)
    {
        super.addConfigurationListener(l);
    }

    @Override
    public boolean removeConfigurationListener(ConfigurationListener l)
    {
        return super.removeConfigurationListener(l);
    }

    @Override
    public Collection<ConfigurationListener> getConfigurationListeners()
    {
        return super.getConfigurationListeners();
    }

    @Override
    public void clearConfigurationListeners()
    {
        super.clearConfigurationListeners();
    }

    @Override
    public void addErrorListener(ConfigurationErrorListener l)
    {
        super.addErrorListener(l);
    }

    @Override
    public boolean removeErrorListener(ConfigurationErrorListener l)
    {
        return super.removeErrorListener(l);
    }

    @Override
    public void clearErrorListeners()
    {
        super.clearErrorListeners();
    }

    @Override
    public Collection<ConfigurationErrorListener> getErrorListeners()
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
        this.getConfiguration();
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

    @Override
    public ConfigurationNode getRootNode()
    {
        return getConfiguration().getRootNode();
    }

    @Override
    public void setRootNode(ConfigurationNode rootNode)
    {
        if (init)
        {
            getConfiguration().setRootNode(rootNode);
        }
        else
        {
            super.setRootNode(rootNode);
        }
    }

    public void configurationChanged(ConfigurationEvent event)
    {
        if (event.getSource() instanceof XMLConfiguration)
        {
            Collection<ConfigurationListener> listeners = getConfigurationListeners();
            for (ConfigurationListener listener : listeners)
            {
                listener.configurationChanged(event);
            }
        }
    }

    public void configurationError(ConfigurationErrorEvent event)
    {
        if (event.getSource() instanceof XMLConfiguration)
        {
            Collection<ConfigurationErrorListener> listeners = getErrorListeners();
            for (ConfigurationErrorListener listener : listeners)
            {
                listener.configurationError(event);
            }
        }
    }

    /*
     * Don't allow resolveContainerStore to be called recursively. This happens
     * when the file pattern does not resolve and the ConfigurationInterpolator
     * calls resolveContainerStore, which in turn calls getProperty, which then
     * calls getConfiguration. GetConfiguration then calls the interpoloator
     * which starts it all over again.
     * @param key The key to resolve.
     * @return The value of the key.
     */
    protected Object resolveContainerStore(String key)
    {
        if (recursive.get())
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
        configurationsMap.remove(path);
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

        if (configurationsMap.containsKey(path))
        {
            return configurationsMap.get(path);
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

            configurationsMap.putIfAbsent(pattern, configuration);

            return configuration;
        }

        XMLConfiguration configuration = new XMLConfiguration();
        if (loggerName != null)
        {
            Logger log = Logger.getLogger(loggerName);
            if (log != null)
            {
                configuration.setLogger(log);
            }
        }
        configuration.setBasePath(getBasePath());
        configuration.setFileName(path);
        configuration.setFileSystem(getFileSystem());
        configuration.setExpressionEngine(getExpressionEngine());
        ReloadingStrategy strategy = createReloadingStrategy();
        if (strategy != null)
        {
            configuration.setReloadingStrategy(strategy);
        }
        configuration.setDelimiterParsingDisabled(isDelimiterParsingDisabled());
        configuration.setAttributeSplittingDisabled(isAttributeSplittingDisabled());
        configuration.setValidating(validating);
        configuration.setSchemaValidation(schemaValidation);
        configuration.setEntityResolver(entityResolver);
        configuration.setListDelimiter(getListDelimiter());
        configuration.addConfigurationListener(this);
        configuration.addErrorListener(this);
        try
        {
            configuration.load();
        }
        catch (ConfigurationException ce)
        {
            if (!ignoreException)
            {
                throw new ConfigurationRuntimeException(ce);
            }
        }
        configurationsMap.putIfAbsent(path, configuration);
        return configurationsMap.get(path);
    }

    /**
     * Clone the FileReloadingStrategy since each file needs its own.
     * @return A new FileReloadingStrategy.
     */
    private ReloadingStrategy createReloadingStrategy()
    {
        if (getReloadingStrategy() == null)
        {
            return null;
        }
        try
        {
            ReloadingStrategy strategy = (ReloadingStrategy) BeanUtils.cloneBean(getReloadingStrategy());
            strategy.setConfiguration(null);
            return strategy;
        }
        catch (Exception ex)
        {
            return null;
        }

    }
}
