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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.configuration.event.ConfigurationErrorEvent;
import org.apache.commons.configuration.event.ConfigurationErrorListener;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration.reloading.ReloadingStrategy;
import org.apache.commons.configuration.resolver.EntityResolverSupport;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.ExpressionEngine;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXParseException;

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
 * @version $Id$
 */
public class MultiFileHierarchicalConfiguration extends AbstractHierarchicalFileConfiguration
    implements ConfigurationListener, ConfigurationErrorListener, EntityResolverSupport
{
    /**
     * Prevent recursion while resolving unprefixed properties.
     */
    private static ThreadLocal<Boolean> recursive = new ThreadLocal<Boolean>()
    {
        @Override
        protected synchronized Boolean initialValue()
        {
            return Boolean.FALSE;
        }
    };

    /** Map of configurations */
    private final ConcurrentMap<String, XMLConfiguration> configurationsMap =
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
    private String loggerName = MultiFileHierarchicalConfiguration.class.getName();

    /** The Reloading strategy to use on created configurations */
    private ReloadingStrategy fileStrategy;

    /** The EntityResolver */
    private EntityResolver entityResolver;

    /** The internally used helper object for variable substitution. */
    private StrSubstitutor localSubst = new StrSubstitutor(new ConfigurationInterpolator());

    /**
     * Default Constructor.
     */
    public MultiFileHierarchicalConfiguration()
    {
        super();
        this.init = true;
        setLogger(LogFactory.getLog(loggerName));
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
        setLogger(LogFactory.getLog(loggerName));
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

    @Override
    public ReloadingStrategy getReloadingStrategy()
    {
        return fileStrategy;
    }

    @Override
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

    @Override
    public void addProperty(String key, Object value)
    {
        this.getConfiguration().addProperty(key, value);
    }

    @Override
    public void clear()
    {
        this.getConfiguration().clear();
    }

    @Override
    public void clearProperty(String key)
    {
        this.getConfiguration().clearProperty(key);
    }

    @Override
    public boolean containsKey(String key)
    {
        return this.getConfiguration().containsKey(key);
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue)
    {
        return this.getConfiguration().getBigDecimal(key, defaultValue);
    }

    @Override
    public BigDecimal getBigDecimal(String key)
    {
        return this.getConfiguration().getBigDecimal(key);
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue)
    {
        return this.getConfiguration().getBigInteger(key, defaultValue);
    }

    @Override
    public BigInteger getBigInteger(String key)
    {
        return this.getConfiguration().getBigInteger(key);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue)
    {
        return this.getConfiguration().getBoolean(key, defaultValue);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue)
    {
        return this.getConfiguration().getBoolean(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key)
    {
        return this.getConfiguration().getBoolean(key);
    }

    @Override
    public byte getByte(String key, byte defaultValue)
    {
        return this.getConfiguration().getByte(key, defaultValue);
    }

    @Override
    public Byte getByte(String key, Byte defaultValue)
    {
        return this.getConfiguration().getByte(key, defaultValue);
    }

    @Override
    public byte getByte(String key)
    {
        return this.getConfiguration().getByte(key);
    }

    @Override
    public double getDouble(String key, double defaultValue)
    {
        return this.getConfiguration().getDouble(key, defaultValue);
    }

    @Override
    public Double getDouble(String key, Double defaultValue)
    {
        return this.getConfiguration().getDouble(key, defaultValue);
    }

    @Override
    public double getDouble(String key)
    {
        return this.getConfiguration().getDouble(key);
    }

    @Override
    public float getFloat(String key, float defaultValue)
    {
        return this.getConfiguration().getFloat(key, defaultValue);
    }

    @Override
    public Float getFloat(String key, Float defaultValue)
    {
        return this.getConfiguration().getFloat(key, defaultValue);
    }

    @Override
    public float getFloat(String key)
    {
        return this.getConfiguration().getFloat(key);
    }

    @Override
    public int getInt(String key, int defaultValue)
    {
        return this.getConfiguration().getInt(key, defaultValue);
    }

    @Override
    public int getInt(String key)
    {
        return this.getConfiguration().getInt(key);
    }

    @Override
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
    public List<Object> getList(String key, List<Object> defaultValue)
    {
        return this.getConfiguration().getList(key, defaultValue);
    }

    @Override
    public List<Object> getList(String key)
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
    public Object getReloadLock()
    {
        return this.getConfiguration().getReloadLock();
    }

    @Override
    public ConfigurationNode getRootNode()
    {
        return this.getConfiguration().getRootNode();
    }

    @Override
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
    public SubnodeConfiguration configurationAt(String key, boolean supportUpdates)
    {
        return this.getConfiguration().configurationAt(key, supportUpdates);
    }

    @Override
    public SubnodeConfiguration configurationAt(String key)
    {
        return this.getConfiguration().configurationAt(key);
    }

    @Override
    public List<HierarchicalConfiguration> configurationsAt(String key)
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

    @Override
    public void load() throws ConfigurationException
    {
        this.getConfiguration();
    }

    @Override
    public void load(String fileName) throws ConfigurationException
    {
        this.getConfiguration().load(fileName);
    }

    @Override
    public void load(File file) throws ConfigurationException
    {
        this.getConfiguration().load(file);
    }

    @Override
    public void load(URL url) throws ConfigurationException
    {
        this.getConfiguration().load(url);
    }

    @Override
    public void load(InputStream in) throws ConfigurationException
    {
        this.getConfiguration().load(in);
    }

    @Override
    public void load(InputStream in, String encoding) throws ConfigurationException
    {
        this.getConfiguration().load(in, encoding);
    }

    @Override
    public void save() throws ConfigurationException
    {
        this.getConfiguration().save();
    }

    @Override
    public void save(String fileName) throws ConfigurationException
    {
        this.getConfiguration().save(fileName);
    }

    @Override
    public void save(File file) throws ConfigurationException
    {
        this.getConfiguration().save(file);
    }

    @Override
    public void save(URL url) throws ConfigurationException
    {
        this.getConfiguration().save(url);
    }

    @Override
    public void save(OutputStream out) throws ConfigurationException
    {
        this.getConfiguration().save(out);
    }

    @Override
    public void save(OutputStream out, String encoding) throws ConfigurationException
    {
        this.getConfiguration().save(out, encoding);
    }

    @Override
    public void configurationChanged(ConfigurationEvent event)
    {
        if (event.getSource() instanceof XMLConfiguration)
        {
            for (ConfigurationListener listener : getConfigurationListeners())
            {
                listener.configurationChanged(event);
            }
        }
    }

    @Override
    public void configurationError(ConfigurationErrorEvent event)
    {
        if (event.getSource() instanceof XMLConfiguration)
        {
            for (ConfigurationErrorListener listener : getErrorListeners())
            {
                listener.configurationError(event);
            }
        }

        if (event.getType() == AbstractFileConfiguration.EVENT_RELOAD)
        {
            if (isThrowable(event.getCause()))
            {
                throw new ConfigurationRuntimeException(event.getCause());
            }
        }
    }

    /*
     * Don't allow resolveContainerStore to be called recursively.
     * @param key The key to resolve.
     * @return The value of the key.
     */
    @Override
    protected Object resolveContainerStore(String key)
    {
        if (recursive.get().booleanValue())
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
        String path = localSubst.replace(pattern);

        if (configurationsMap.containsKey(path))
        {
            return configurationsMap.get(path);
        }

        if (path.equals(pattern))
        {
            XMLConfiguration configuration = new XMLConfiguration()
            {
                @Override
                public void load() throws ConfigurationException
                {
                }
                @Override
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
            Log log = LogFactory.getLog(loggerName);
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
            if (isThrowable(ce))
            {
                throw new ConfigurationRuntimeException(ce);
            }
        }
        configurationsMap.putIfAbsent(path, configuration);
        return configurationsMap.get(path);
    }

    private boolean isThrowable(Throwable throwable)
    {
        if (!ignoreException)
        {
            return true;
        }
        Throwable cause = throwable.getCause();
        while (cause != null && !(cause instanceof SAXParseException))
        {
            cause = cause.getCause();
        }
        return cause != null;
    }

    /**
     * Clone the FileReloadingStrategy since each file needs its own.
     * @return A new FileReloadingStrategy.
     */
    private ReloadingStrategy createReloadingStrategy()
    {
        if (fileStrategy == null)
        {
            return null;
        }
        try
        {
            ReloadingStrategy strategy = (ReloadingStrategy) BeanUtils.cloneBean(fileStrategy);
            strategy.setConfiguration(null);
            return strategy;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

}
