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
package org.apache.commons.configuration2.combined;


import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.Set;
import java.util.Properties;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.configuration2.event.ConfigurationListener;
import org.apache.commons.configuration2.event.ConfigurationErrorListener;
import org.apache.commons.configuration2.expr.ExpressionEngine;
import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.AbstractHierarchicalConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.SubConfiguration;

/**
 *
 */
public class DynamicCombinedConfiguration extends CombinedConfiguration
{
    private ConcurrentMap<String, CombinedConfiguration> configs =
            new ConcurrentHashMap<String, CombinedConfiguration>();

    /** Stores a list with the contained configurations. */
    private List<ConfigData> configurations = new CopyOnWriteArrayList<ConfigData>();

    /** Stores a map with the named configurations. */
    private ConcurrentMap<String, AbstractConfiguration> namedConfigurations =
            new ConcurrentHashMap<String, AbstractConfiguration>();

    private String keyPattern;

    /** Stores the combiner. */
    private NodeCombiner nodeCombiner;

    /**
     * Creates a new instance of <code>CombinedConfiguration</code> and
     * initializes the combiner to be used.
     *
     * @param comb the node combiner (can be <b>null</b>, then a union combiner
     * is used as default)
     */
    public DynamicCombinedConfiguration(NodeCombiner comb)
    {
        super();
        setNodeCombiner(comb);
    }

    /**
     * Creates a new instance of <code>CombinedConfiguration</code> that uses
     * a union combiner.
     *
     */
    public DynamicCombinedConfiguration()
    {
        super();
    }

    public void setKeyPattern(String pattern)
    {
        this.keyPattern = pattern;
    }

    public String getKeyPattern()
    {
        return this.keyPattern;
    }

    /**
     * Returns the node combiner that is used for creating the combined node
     * structure.
     *
     * @return the node combiner
     */
    @Override
    public NodeCombiner getNodeCombiner()
    {
        return nodeCombiner;
    }

    /**
     * Sets the node combiner. This object will be used when the combined node
     * structure is to be constructed. It must not be <b>null</b>, otherwise an
     * <code>IllegalArgumentException</code> exception is thrown. Changing the
     * node combiner causes an invalidation of this combined configuration, so
     * that the new combiner immediately takes effect.
     *
     * @param nodeCombiner the node combiner
     */
    @Override
    public void setNodeCombiner(NodeCombiner nodeCombiner)
    {
        if (nodeCombiner == null)
        {
            throw new IllegalArgumentException(
                    "Node combiner must not be null!");
        }
        this.nodeCombiner = nodeCombiner;
        invalidateAll();
    }
    /**
     * Adds a new configuration to this combined configuration. It is possible
     * (but not mandatory) to give the new configuration a name. This name must
     * be unique, otherwise a <code>ConfigurationRuntimeException</code> will
     * be thrown. With the optional <code>at</code> argument you can specify
     * where in the resulting node structure the content of the added
     * configuration should appear. This is a string that uses dots as property
     * delimiters (independent on the current expression engine). For instance
     * if you pass in the string <code>&quot;database.tables&quot;</code>,
     * all properties of the added configuration will occur in this branch.
     *
     * @param config the configuration to add (must not be <b>null</b>)
     * @param name the name of this configuration (can be <b>null</b>)
     * @param at the position of this configuration in the combined tree (can be
     * <b>null</b>)
     */
    @Override
    public void addConfiguration(AbstractHierarchicalConfiguration<?> config, String name,
            String at)
    {
        ConfigData cd = new ConfigData(config, name, at);
        configurations.add(cd);
        if (name != null)
        {
            namedConfigurations.put(name, config);
        }
    }
       /**
     * Returns the number of configurations that are contained in this combined
     * configuration.
     *
     * @return the number of contained configurations
     */
    @Override
    public int getNumberOfConfigurations()
    {
        return configurations.size();
    }

    /**
     * Returns the configuration at the specified index. The contained
     * configurations are numbered in the order they were added to this combined
     * configuration. The index of the first configuration is 0.
     *
     * @param index the index
     * @return the configuration at this index
     */
    @Override
    public Configuration getConfiguration(int index)
    {
        ConfigData cd = configurations.get(index);
        return cd.getConfiguration();
    }

    /**
     * Returns the configuration with the given name. This can be <b>null</b>
     * if no such configuration exists.
     *
     * @param name the name of the configuration
     * @return the configuration with this name
     */
    @Override
    public Configuration getConfiguration(String name)
    {
        return namedConfigurations.get(name);
    }

    /**
     * Returns a set with the names of all configurations contained in this
     * combined configuration. Of course here are only these configurations
     * listed, for which a name was specified when they were added.
     *
     * @return a set with the names of the contained configurations (never
     * <b>null</b>)
     */
    @Override
    public Set<String> getConfigurationNames()
    {
        return namedConfigurations.keySet();
    }

    /**
     * Removes the configuration with the specified name.
     *
     * @param name the name of the configuration to be removed
     * @return the removed configuration (<b>null</b> if this configuration
     * was not found)
     */
    @Override
    public Configuration removeConfiguration(String name)
    {
        Configuration conf = getConfiguration(name);
        if (conf != null)
        {
            removeConfiguration(conf);
        }
        return conf;
    }

    /**
     * Removes the specified configuration from this combined configuration.
     *
     * @param config the configuration to be removed
     * @return a flag whether this configuration was found and could be removed
     */
    @Override
    public boolean removeConfiguration(Configuration config)
    {
        for (int index = 0; index < getNumberOfConfigurations(); index++)
        {
            if (((configurations.get(index)).getConfiguration() == config))
            {
                removeConfigurationAt(index);

            }
        }

        return super.removeConfiguration(config);
    }

    /**
     * Removes the configuration at the specified index.
     *
     * @param index the index
     * @return the removed configuration
     */
    @Override
    public Configuration removeConfigurationAt(int index)
    {
        ConfigData cd = configurations.remove(index);
        if (cd.getName() != null)
        {
            namedConfigurations.remove(cd.getName());
        }
        return super.removeConfigurationAt(index);
    }
    /**
     * Returns the configuration root node of this combined configuration. This
     * method will construct a combined node structure using the current node
     * combiner if necessary.
     *
     * @return the combined root node
     */
    @Override
    public Object getRootNode()
    {
        return getCurrentConfig().getRootNode();
    }

    @Override
    public void addProperty(String key, Object value)
    {
        this.getCurrentConfig().addProperty(key, value);
    }

    @Override
    public void clear()
    {
        if (configs != null)
        {
            this.getCurrentConfig().clear();
        }
    }

    @Override
    public void clearProperty(String key)
    {
        this.getCurrentConfig().clearProperty(key);
    }

    @Override
    public boolean containsKey(String key)
    {
        return this.getCurrentConfig().containsKey(key);
    }

    @Override
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue)
    {
        return this.getCurrentConfig().getBigDecimal(key, defaultValue);
    }

    @Override
    public BigDecimal getBigDecimal(String key)
    {
        return this.getCurrentConfig().getBigDecimal(key);
    }

    @Override
    public BigInteger getBigInteger(String key, BigInteger defaultValue)
    {
        return this.getCurrentConfig().getBigInteger(key, defaultValue);
    }

    @Override
    public BigInteger getBigInteger(String key)
    {
        return this.getCurrentConfig().getBigInteger(key);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue)
    {
        return this.getCurrentConfig().getBoolean(key, defaultValue);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue)
    {
        return this.getCurrentConfig().getBoolean(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key)
    {
        return this.getCurrentConfig().getBoolean(key);
    }

    @Override
    public byte getByte(String key, byte defaultValue)
    {
        return this.getCurrentConfig().getByte(key, defaultValue);
    }

    @Override
    public Byte getByte(String key, Byte defaultValue)
    {
        return this.getCurrentConfig().getByte(key, defaultValue);
    }

    @Override
    public byte getByte(String key)
    {
        return this.getCurrentConfig().getByte(key);
    }

    @Override
    public double getDouble(String key, double defaultValue)
    {
        return this.getCurrentConfig().getDouble(key, defaultValue);
    }

    @Override
    public Double getDouble(String key, Double defaultValue)
    {
        return this.getCurrentConfig().getDouble(key, defaultValue);
    }

    @Override
    public double getDouble(String key)
    {
        return this.getCurrentConfig().getDouble(key);
    }

    @Override
    public float getFloat(String key, float defaultValue)
    {
        return this.getCurrentConfig().getFloat(key, defaultValue);
    }

    @Override
    public Float getFloat(String key, Float defaultValue)
    {
        return this.getCurrentConfig().getFloat(key, defaultValue);
    }

    @Override
    public float getFloat(String key)
    {
        return this.getCurrentConfig().getFloat(key);
    }

    @Override
    public int getInt(String key, int defaultValue)
    {
        return this.getCurrentConfig().getInt(key, defaultValue);
    }

    @Override
    public int getInt(String key)
    {
        return this.getCurrentConfig().getInt(key);
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue)
    {
        return this.getCurrentConfig().getInteger(key, defaultValue);
    }

    @Override
    public Iterator<String> getKeys()
    {
        return this.getCurrentConfig().getKeys();
    }

    @Override
    public Iterator<String> getKeys(String prefix)
    {
        return this.getCurrentConfig().getKeys(prefix);
    }

    @Override
    public <T> List<T> getList(String key, List<T> defaultValue)
    {
        return this.getCurrentConfig().getList(key, defaultValue);
    }

    @Override
    public <T> List<T> getList(String key)
    {
        return this.getCurrentConfig().getList(key);
    }

    @Override
    public long getLong(String key, long defaultValue)
    {
        return this.getCurrentConfig().getLong(key, defaultValue);
    }

    @Override
    public Long getLong(String key, Long defaultValue)
    {
        return this.getCurrentConfig().getLong(key, defaultValue);
    }

    @Override
    public long getLong(String key)
    {
        return this.getCurrentConfig().getLong(key);
    }

    @Override
    public Properties getProperties(String key)
    {
        return this.getCurrentConfig().getProperties(key);
    }

    @Override
    public Object getProperty(String key)
    {
        return this.getCurrentConfig().getProperty(key);
    }

    @Override
    public short getShort(String key, short defaultValue)
    {
        return this.getCurrentConfig().getShort(key, defaultValue);
    }

    @Override
    public Short getShort(String key, Short defaultValue)
    {
        return this.getCurrentConfig().getShort(key, defaultValue);
    }

    @Override
    public short getShort(String key)
    {
        return this.getCurrentConfig().getShort(key);
    }

    @Override
    public String getString(String key, String defaultValue)
    {
        return this.getCurrentConfig().getString(key, defaultValue);
    }

    @Override
    public String getString(String key)
    {
        return this.getCurrentConfig().getString(key);
    }

    @Override
    public String[] getStringArray(String key)
    {
        return this.getCurrentConfig().getStringArray(key);
    }

    @Override
    public boolean isEmpty()
    {
        return this.getCurrentConfig().isEmpty();
    }

    @Override
    public void setProperty(String key, Object value)
    {
        if (configs != null)
        {
            this.getCurrentConfig().setProperty(key, value);
        }
    }

    @Override
    public Configuration subset(String prefix)
    {
        return this.getCurrentConfig().subset(prefix);
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
    public SubConfiguration<Object> configurationAt(String key, boolean supportUpdates)
    {
        return this.getCurrentConfig().configurationAt(key, supportUpdates);
    }

    @Override
    public SubConfiguration<Object> configurationAt(String key)
    {
        return this.getCurrentConfig().configurationAt(key);
    }

    @Override
    public List<SubConfiguration<Object>> configurationsAt(String key)
    {
        return this.getCurrentConfig().configurationsAt(key);
    }

    @Override
    public void clearTree(String key)
    {
        this.getCurrentConfig().clearTree(key);
    }

    @Override
    public int getMaxIndex(String key)
    {
        return this.getCurrentConfig().getMaxIndex(key);
    }

    @Override
    public Configuration interpolatedConfiguration()
    {
        return this.getCurrentConfig().interpolatedConfiguration();
    }


    /**
     * Returns the configuration source, in which the specified key is defined.
     * This method will determine the configuration node that is identified by
     * the given key. The following constellations are possible:
     * <ul>
     * <li>If no node object is found for this key, <b>null</b> is returned.</li>
     * <li>If the key maps to multiple nodes belonging to different
     * configuration sources, a <code>IllegalArgumentException</code> is
     * thrown (in this case no unique source can be determined).</li>
     * <li>If exactly one node is found for the key, the (child) configuration
     * object, to which the node belongs is determined and returned.</li>
     * <li>For keys that have been added directly to this combined
     * configuration and that do not belong to the namespaces defined by
     * existing child configurations this configuration will be returned.</li>
     * </ul>
     *
     * @param key the key of a configuration property
     * @return the configuration, to which this property belongs or <b>null</b>
     * if the key cannot be resolved
     * @throws IllegalArgumentException if the key maps to multiple properties
     * and the source cannot be determined, or if the key is <b>null</b>
     */
    @Override
    public Configuration getSource(String key)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("Key must not be null!");
        }
        return getCurrentConfig().getSource(key);
    }

    @Override
    public void addConfigurationListener(ConfigurationListener l)
    {
        super.addConfigurationListener(l);

        for (CombinedConfiguration config : configs.values())
        {
            config.addConfigurationListener(l);
        }
    }

    @Override
    public boolean removeConfigurationListener(ConfigurationListener l)
    {
        for (CombinedConfiguration config : configs.values())
        {
            config.removeConfigurationListener(l);
        }
        return super.removeConfigurationListener(l);
    }

    @Override
    public Collection getConfigurationListeners()
    {
        return super.getConfigurationListeners();
    }

    @Override
    public void clearConfigurationListeners()
    {
        for (CombinedConfiguration config : configs.values())
        {
            config.clearConfigurationListeners();
        }
        super.clearConfigurationListeners();
    }

    @Override
    public void addErrorListener(ConfigurationErrorListener l)
    {
        for (CombinedConfiguration config : configs.values())
        {
            config.addErrorListener(l);
        }
        super.addErrorListener(l);
    }

    @Override
    public boolean removeErrorListener(ConfigurationErrorListener l)
    {
        for (CombinedConfiguration config : configs.values())
        {
            config.removeErrorListener(l);
        }
        return super.removeErrorListener(l);
    }

    @Override
    public void clearErrorListeners()
    {
        for (CombinedConfiguration config : configs.values())
        {
            config.clearErrorListeners();
        }
        super.clearErrorListeners();
    }

    @Override
    public Collection getErrorListeners()
    {
        return super.getErrorListeners();
    }



    /**
     * Returns a copy of this object. This implementation performs a deep clone,
     * i.e. all contained configurations will be cloned, too. For this to work,
     * all contained configurations must be cloneable. Registered event
     * listeners won't be cloned. The clone will use the same node combiner than
     * the original.
     *
     * @return the copied object
     */
    @Override
    public Object clone()
    {
        return super.clone();
    }



    /**
     * Invalidates the current combined configuration. This means that the next time a
     * property is accessed the combined node structure must be re-constructed.
     * Invalidation of a combined configuration also means that an event of type
     * <code>EVENT_COMBINED_INVALIDATE</code> is fired. Note that while other
     * events most times appear twice (once before and once after an update),
     * this event is only fired once (after update).
     */
    @Override
    public void invalidate()
    {
        getCurrentConfig().invalidate();
    }

    public void invalidateAll()
    {
        if (configs == null)
        {
            return;
        }
        for (CombinedConfiguration config : configs.values())
        {
           config.invalidate();
        }
    }

    private CombinedConfiguration getCurrentConfig()
    {
        String key = getSubstitutor().replace(keyPattern);
        CombinedConfiguration config;
        synchronized(getNodeCombiner())
        {
            config = configs.get(key);
            if (config == null)
            {
                config = new CombinedConfiguration(getNodeCombiner());
                config.setExpressionEngine(this.getExpressionEngine());
                for (ConfigurationErrorListener listener :
                        (Collection<ConfigurationErrorListener>)config.getErrorListeners())
                {
                    config.addErrorListener(listener);
                }
                for (ConfigurationListener listener :
                        (Collection<ConfigurationListener>)config.getConfigurationListeners())
                {
                    config.addConfigurationListener(listener);
                }
                config.setForceReloadCheck(isForceReloadCheck());
                for (ConfigData data : configurations)
                {
                    config.addConfiguration(data.getConfiguration(), data.getName(),
                            data.getAt());
                }
                configs.put(key, config);
            }
        }
        return config;
    }


    class ConfigData
    {
                /** Stores a reference to the configuration. */
        private AbstractHierarchicalConfiguration configuration;

        /** Stores the name under which the configuration is stored. */
        private String name;

        /** Stores the at string.*/
        private String at;

                /**
         * Creates a new instance of <code>ConfigData</code> and initializes
         * it.
         *
         * @param config the configuration
         * @param n the name
         * @param at the at position
         */
        public ConfigData(AbstractHierarchicalConfiguration config, String n, String at)
        {
            configuration = config;
            name = n;
            this.at = at;
        }

                /**
         * Returns the stored configuration.
         *
         * @return the configuration
         */
        public AbstractHierarchicalConfiguration getConfiguration()
        {
            return configuration;
        }

        /**
         * Returns the configuration's name.
         *
         * @return the name
         */
        public String getName()
        {
            return name;
        }

        /**
         * Returns the at position of this configuration.
         *
         * @return the at position
         */
        public String getAt()
        {
            return at;
        }

    }
}
