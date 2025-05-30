/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.configuration2.event.BaseEventSource;
import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventType;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.NodeCombiner;

/**
 * <p>
 * DynamicCombinedConfiguration allows a set of CombinedConfigurations to be used.
 * </p>
 * <p>
 * Each CombinedConfiguration is referenced by a key that is dynamically constructed from a key pattern on each call.
 * The key pattern will be resolved using the configured ConfigurationInterpolator.
 * </p>
 * <p>
 * This Configuration implementation uses the configured {@code Synchronizer} to guard itself against concurrent access.
 * If there are multiple threads accessing an instance concurrently, a fully functional {@code Synchronizer}
 * implementation (for example {@code ReadWriteSynchronizer}) has to be used to ensure consistency and to avoid exceptions. The
 * {@code Synchronizer} assigned to an instance is also passed to child configuration objects when they are created.
 * </p>
 *
 * @since 1.6
 */
public class DynamicCombinedConfiguration extends CombinedConfiguration {
    /**
     * Internal class that identifies each Configuration.
     */
    static class ConfigData {
        /** Stores a reference to the configuration. */
        private final Configuration configuration;

        /** Stores the name under which the configuration is stored. */
        private final String name;

        /** Stores the at string. */
        private final String at;

        /**
         * Creates a new instance of {@code ConfigData} and initializes it.
         *
         * @param config the configuration
         * @param n the name
         * @param at the at position
         */
        public ConfigData(final Configuration config, final String n, final String at) {
            configuration = config;
            name = n;
            this.at = at;
        }

        /**
         * Gets the at position of this configuration.
         *
         * @return the at position
         */
        public String getAt() {
            return at;
        }

        /**
         * Gets the stored configuration.
         *
         * @return the configuration
         */
        public Configuration getConfiguration() {
            return configuration;
        }

        /**
         * Gets the configuration's name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

    }

    /**
     * A simple data class holding information about the current configuration while an operation for a thread is processed.
     */
    private static final class CurrentConfigHolder {
        /** Stores the current configuration of the current thread. */
        private CombinedConfiguration currentConfiguration;

        /**
         * Stores the key of the configuration evaluated for the current thread at the beginning of an operation.
         */
        private final String key;

        /** A counter for reentrant locks. */
        private int lockCount;

        /**
         * Creates a new instance of {@code CurrentConfigHolder} and initializes it with the key for the current configuration.
         *
         * @param curKey the current key
         */
        public CurrentConfigHolder(final String curKey) {
            key = curKey;
        }

        /**
         * Decrements the lock counter and checks whether it has reached 0. In this cause, the operation is complete, and the
         * lock can be released.
         *
         * @return <strong>true</strong> if the lock count reaches 0, <strong>false</strong> otherwise
         */
        public boolean decrementLockCountAndCheckRelease() {
            return --lockCount == 0;
        }

        /**
         * Gets the current configuration.
         *
         * @return the current configuration
         */
        public CombinedConfiguration getCurrentConfiguration() {
            return currentConfiguration;
        }

        /**
         * Gets the current key.
         *
         * @return the current key
         */
        public String getKey() {
            return key;
        }

        /**
         * Increments the lock counter.
         */
        public void incrementLockCount() {
            lockCount++;
        }

        /**
         * Sets the current configuration.
         *
         * @param currentConfiguration the current configuration
         */
        public void setCurrentConfiguration(final CombinedConfiguration currentConfiguration) {
            this.currentConfiguration = currentConfiguration;
        }
    }

    /**
     * Stores the current configuration for each involved thread. This value is set at the beginning of an operation and
     * removed at the end.
     */
    private static final ThreadLocal<CurrentConfigHolder> CURRENT_CONFIG = new ThreadLocal<>();

    /** The CombinedConfigurations */
    private final ConcurrentMap<String, CombinedConfiguration> configs = new ConcurrentHashMap<>();

    /** Stores a list with the contained configurations. */
    private final List<ConfigData> configurations = new ArrayList<>();

    /** Stores a map with the named configurations. */
    private final Map<String, Configuration> namedConfigurations = new HashMap<>();

    /** The key pattern for the CombinedConfiguration map. */
    private String keyPattern;

    /** Stores the combiner. */
    private NodeCombiner nodeCombiner;

    /** The name of the logger to use for each CombinedConfiguration */
    private String loggerName = DynamicCombinedConfiguration.class.getName();

    /** The object for handling variable substitution in key patterns. */
    private final ConfigurationInterpolator localSubst;

    /**
     * Creates a new instance of {@code DynamicCombinedConfiguration} that uses a union combiner.
     *
     * @see org.apache.commons.configuration2.tree.UnionCombiner
     */
    public DynamicCombinedConfiguration() {
        initLogger(new ConfigurationLogger(DynamicCombinedConfiguration.class));
        localSubst = initLocalInterpolator();
    }

    /**
     * Creates a new instance of {@code DynamicCombinedConfiguration} and initializes the combiner to be used.
     *
     * @param comb the node combiner (can be <strong>null</strong>, then a union combiner is used as default)
     */
    public DynamicCombinedConfiguration(final NodeCombiner comb) {
        setNodeCombiner(comb);
        initLogger(new ConfigurationLogger(DynamicCombinedConfiguration.class));
        localSubst = initLocalInterpolator();
    }

    /**
     * Adds a new configuration to this combined configuration. It is possible (but not mandatory) to give the new
     * configuration a name. This name must be unique, otherwise a {@code ConfigurationRuntimeException} will be thrown.
     * With the optional {@code at} argument you can specify where in the resulting node structure the content of the added
     * configuration should appear. This is a string that uses dots as property delimiters (independent on the current
     * expression engine). For instance if you pass in the string {@code "database.tables"}, all properties of the added
     * configuration will occur in this branch.
     *
     * @param config the configuration to add (must not be <strong>null</strong>)
     * @param name the name of this configuration (can be <strong>null</strong>)
     * @param at the position of this configuration in the combined tree (can be <strong>null</strong>)
     */
    @Override
    public void addConfiguration(final Configuration config, final String name, final String at) {
        beginWrite(true);
        try {
            final ConfigData cd = new ConfigData(config, name, at);
            configurations.add(cd);
            if (name != null) {
                namedConfigurations.put(name, config);
            }

            // clear cache of all child configurations
            configs.clear();
        } finally {
            endWrite();
        }
    }

    @Override
    public <T extends Event> void addEventListener(final EventType<T> eventType, final EventListener<? super T> listener) {
        configs.values().forEach(cc -> cc.addEventListener(eventType, listener));
        super.addEventListener(eventType, listener);
    }

    @Override
    protected void addNodesInternal(final String key, final Collection<? extends ImmutableNode> nodes) {
        getCurrentConfig().addNodes(key, nodes);
    }

    @Override
    protected void addPropertyInternal(final String key, final Object value) {
        getCurrentConfig().addProperty(key, value);
    }

    /**
     * {@inheritDoc} This implementation ensures that the current configuration is initialized. The lock counter is
     * increased.
     */
    @Override
    protected void beginRead(final boolean optimize) {
        final CurrentConfigHolder cch = ensureCurrentConfiguration();
        cch.incrementLockCount();
        if (!optimize && cch.getCurrentConfiguration() == null) {
            // delegate to beginWrite() which creates the child configuration
            beginWrite(false);
            endWrite();
        }

        // This actually uses our own synchronizer
        cch.getCurrentConfiguration().beginRead(optimize);
    }

    /**
     * {@inheritDoc} This implementation ensures that the current configuration is initialized. If necessary, a new child
     * configuration instance is created.
     */
    @Override
    protected void beginWrite(final boolean optimize) {
        final CurrentConfigHolder cch = ensureCurrentConfiguration();
        cch.incrementLockCount();

        super.beginWrite(optimize);
        if (!optimize && cch.getCurrentConfiguration() == null) {
            cch.setCurrentConfiguration(createChildConfiguration());
            configs.put(cch.getKey(), cch.getCurrentConfiguration());
            initChildConfiguration(cch.getCurrentConfiguration());
        }
    }

    @Override
    public void clearErrorListeners() {
        configs.values().forEach(BaseEventSource::clearErrorListeners);
        super.clearErrorListeners();
    }

    @Override
    public void clearEventListeners() {
        configs.values().forEach(CombinedConfiguration::clearEventListeners);
        super.clearEventListeners();
    }

    @Override
    protected void clearInternal() {
        getCurrentConfig().clear();
    }

    @Override
    protected void clearPropertyDirect(final String key) {
        getCurrentConfig().clearProperty(key);
    }

    @Override
    protected Object clearTreeInternal(final String key) {
        getCurrentConfig().clearTree(key);
        return Collections.emptyList();
    }

    @Override
    public HierarchicalConfiguration<ImmutableNode> configurationAt(final String key) {
        return getCurrentConfig().configurationAt(key);
    }

    @Override
    public HierarchicalConfiguration<ImmutableNode> configurationAt(final String key, final boolean supportUpdates) {
        return getCurrentConfig().configurationAt(key, supportUpdates);
    }

    @Override
    public List<HierarchicalConfiguration<ImmutableNode>> configurationsAt(final String key) {
        return getCurrentConfig().configurationsAt(key);
    }

    @Override
    protected boolean containsKeyInternal(final String key) {
        return getCurrentConfig().containsKey(key);
    }

    /**
     * Tests whether this configuration contains one or more matches to this value. This operation stops at first
     * match but may be more expensive than the containsKey method.
     * @since 2.11.0
     */
    @Override
    protected boolean containsValueInternal(final Object value) {
        return getCurrentConfig().contains(getKeys(), value);
    }

    /**
     * Creates a new, uninitialized child configuration.
     *
     * @return the new child configuration
     */
    private CombinedConfiguration createChildConfiguration() {
        return new CombinedConfiguration(getNodeCombiner());
    }

    /**
     * {@inheritDoc} This implementation clears the current configuration if necessary.
     */
    @Override
    protected void endRead() {
        CURRENT_CONFIG.get().getCurrentConfiguration().endRead();
        releaseLock();
    }

    /**
     * {@inheritDoc} This implementation clears the current configuration if necessary.
     */
    @Override
    protected void endWrite() {
        super.endWrite();
        releaseLock();
    }

    /**
     * Checks whether the current configuration is set. If not, a {@code CurrentConfigHolder} is now created and
     * initialized, and associated with the current thread. The member for the current configuration is undefined if for the
     * current key no configuration exists yet.
     *
     * @return the {@code CurrentConfigHolder} instance for the current thread
     */
    private CurrentConfigHolder ensureCurrentConfiguration() {
        CurrentConfigHolder cch = CURRENT_CONFIG.get();
        if (cch == null) {
            final String key = String.valueOf(localSubst.interpolate(keyPattern));
            cch = new CurrentConfigHolder(key);
            cch.setCurrentConfiguration(configs.get(key));
            CURRENT_CONFIG.set(cch);
        }
        return cch;
    }

    @Override
    public BigDecimal getBigDecimal(final String key) {
        return getCurrentConfig().getBigDecimal(key);
    }

    @Override
    public BigDecimal getBigDecimal(final String key, final BigDecimal defaultValue) {
        return getCurrentConfig().getBigDecimal(key, defaultValue);
    }

    @Override
    public BigInteger getBigInteger(final String key) {
        return getCurrentConfig().getBigInteger(key);
    }

    @Override
    public BigInteger getBigInteger(final String key, final BigInteger defaultValue) {
        return getCurrentConfig().getBigInteger(key, defaultValue);
    }

    @Override
    public boolean getBoolean(final String key) {
        return getCurrentConfig().getBoolean(key);
    }

    @Override
    public boolean getBoolean(final String key, final boolean defaultValue) {
        return getCurrentConfig().getBoolean(key, defaultValue);
    }

    @Override
    public Boolean getBoolean(final String key, final Boolean defaultValue) {
        return getCurrentConfig().getBoolean(key, defaultValue);
    }

    @Override
    public byte getByte(final String key) {
        return getCurrentConfig().getByte(key);
    }

    @Override
    public byte getByte(final String key, final byte defaultValue) {
        return getCurrentConfig().getByte(key, defaultValue);
    }

    @Override
    public Byte getByte(final String key, final Byte defaultValue) {
        return getCurrentConfig().getByte(key, defaultValue);
    }

    /**
     * Gets the configuration at the specified index. The contained configurations are numbered in the order they were
     * added to this combined configuration. The index of the first configuration is 0.
     *
     * @param index the index
     * @return the configuration at this index
     */
    @Override
    public Configuration getConfiguration(final int index) {
        beginRead(false);
        try {
            final ConfigData cd = configurations.get(index);
            return cd.getConfiguration();
        } finally {
            endRead();
        }
    }

    /**
     * Gets the configuration with the given name. This can be <strong>null</strong> if no such configuration exists.
     *
     * @param name the name of the configuration
     * @return the configuration with this name
     */
    @Override
    public Configuration getConfiguration(final String name) {
        beginRead(false);
        try {
            return namedConfigurations.get(name);
        } finally {
            endRead();
        }
    }

    /**
     * Gets a set with the names of all configurations contained in this combined configuration. Of course here are only
     * these configurations listed, for which a name was specified when they were added.
     *
     * @return a set with the names of the contained configurations (never <strong>null</strong>)
     */
    @Override
    public Set<String> getConfigurationNames() {
        beginRead(false);
        try {
            return namedConfigurations.keySet();
        } finally {
            endRead();
        }
    }

    /**
     * Gets the current configuration. This configuration was initialized at the beginning of an operation and stored in
     * a thread-local variable. Some methods of this class call this method directly without requesting a lock before. To
     * deal with this, we always request an additional read lock.
     *
     * @return the current configuration
     */
    private CombinedConfiguration getCurrentConfig() {
        CombinedConfiguration config;
        String key;
        beginRead(false);
        try {
            config = CURRENT_CONFIG.get().getCurrentConfiguration();
            key = CURRENT_CONFIG.get().getKey();
        } finally {
            endRead();
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Returning config for " + key + ": " + config);
        }
        return config;
    }

    @Override
    public double getDouble(final String key) {
        return getCurrentConfig().getDouble(key);
    }

    @Override
    public double getDouble(final String key, final double defaultValue) {
        return getCurrentConfig().getDouble(key, defaultValue);
    }

    @Override
    public Double getDouble(final String key, final Double defaultValue) {
        return getCurrentConfig().getDouble(key, defaultValue);
    }

    @Override
    public float getFloat(final String key) {
        return getCurrentConfig().getFloat(key);
    }

    @Override
    public float getFloat(final String key, final float defaultValue) {
        return getCurrentConfig().getFloat(key, defaultValue);
    }

    @Override
    public Float getFloat(final String key, final Float defaultValue) {
        return getCurrentConfig().getFloat(key, defaultValue);
    }

    @Override
    public int getInt(final String key) {
        return getCurrentConfig().getInt(key);
    }

    @Override
    public int getInt(final String key, final int defaultValue) {
        return getCurrentConfig().getInt(key, defaultValue);
    }

    @Override
    public Integer getInteger(final String key, final Integer defaultValue) {
        return getCurrentConfig().getInteger(key, defaultValue);
    }

    /**
     * Gets the key pattern for the CombinedConfiguration map.
     *
     * @return the key pattern for the CombinedConfiguration map.
     */
    public String getKeyPattern() {
        return this.keyPattern;
    }

    @Override
    protected Iterator<String> getKeysInternal() {
        return getCurrentConfig().getKeys();
    }

    @Override
    protected Iterator<String> getKeysInternal(final String prefix) {
        return getCurrentConfig().getKeys(prefix);
    }

    @Override
    public List<Object> getList(final String key) {
        return getCurrentConfig().getList(key);
    }

    @Override
    public List<Object> getList(final String key, final List<?> defaultValue) {
        return getCurrentConfig().getList(key, defaultValue);
    }

    @Override
    public long getLong(final String key) {
        return getCurrentConfig().getLong(key);
    }

    @Override
    public long getLong(final String key, final long defaultValue) {
        return getCurrentConfig().getLong(key, defaultValue);
    }

    @Override
    public Long getLong(final String key, final Long defaultValue) {
        return getCurrentConfig().getLong(key, defaultValue);
    }

    @Override
    protected int getMaxIndexInternal(final String key) {
        return getCurrentConfig().getMaxIndex(key);
    }

    /**
     * Gets the node combiner that is used for creating the combined node structure.
     *
     * @return the node combiner
     */
    @Override
    public NodeCombiner getNodeCombiner() {
        return nodeCombiner;
    }

    /**
     * Gets the number of configurations that are contained in this combined configuration.
     *
     * @return the number of contained configurations
     */
    @Override
    public int getNumberOfConfigurations() {
        beginRead(false);
        try {
            return configurations.size();
        } finally {
            endRead();
        }
    }

    @Override
    public Properties getProperties(final String key) {
        return getCurrentConfig().getProperties(key);
    }

    @Override
    protected Object getPropertyInternal(final String key) {
        return getCurrentConfig().getProperty(key);
    }

    @Override
    public short getShort(final String key) {
        return getCurrentConfig().getShort(key);
    }

    @Override
    public short getShort(final String key, final short defaultValue) {
        return getCurrentConfig().getShort(key, defaultValue);
    }

    @Override
    public Short getShort(final String key, final Short defaultValue) {
        return getCurrentConfig().getShort(key, defaultValue);
    }

    /**
     * Gets the configuration source, in which the specified key is defined. This method will determine the configuration
     * node that is identified by the given key. The following constellations are possible:
     * <ul>
     * <li>If no node object is found for this key, <strong>null</strong> is returned.</li>
     * <li>If the key maps to multiple nodes belonging to different configuration sources, a
     * {@code IllegalArgumentException} is thrown (in this case no unique source can be determined).</li>
     * <li>If exactly one node is found for the key, the (child) configuration object, to which the node belongs is
     * determined and returned.</li>
     * <li>For keys that have been added directly to this combined configuration and that do not belong to the namespaces
     * defined by existing child configurations this configuration will be returned.</li>
     * </ul>
     *
     * @param key the key of a configuration property
     * @return the configuration, to which this property belongs or <strong>null</strong> if the key cannot be resolved
     * @throws IllegalArgumentException if the key maps to multiple properties and the source cannot be determined, or if
     *         the key is <strong>null</strong>
     */
    @Override
    public Configuration getSource(final String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null!");
        }
        return getCurrentConfig().getSource(key);
    }

    @Override
    public String getString(final String key) {
        return getCurrentConfig().getString(key);
    }

    @Override
    public String getString(final String key, final String defaultValue) {
        return getCurrentConfig().getString(key, defaultValue);
    }

    @Override
    public String[] getStringArray(final String key) {
        return getCurrentConfig().getStringArray(key);
    }

    /**
     * Initializes a newly created child configuration. This method copies a bunch of settings from this instance to the
     * child configuration.
     *
     * @param config the child configuration to be initialized
     */
    private void initChildConfiguration(final CombinedConfiguration config) {
        if (loggerName != null) {
            config.setLogger(new ConfigurationLogger(loggerName));
        }
        config.setExpressionEngine(getExpressionEngine());
        config.setConversionExpressionEngine(getConversionExpressionEngine());
        config.setListDelimiterHandler(getListDelimiterHandler());
        copyEventListeners(config);
        configurations.forEach(data -> config.addConfiguration(data.getConfiguration(), data.getName(), data.getAt()));
        config.setSynchronizer(getSynchronizer());
    }

    /**
     * Creates a {@code ConfigurationInterpolator} instance for performing local variable substitutions. This implementation
     * returns an object which shares the prefix lookups from this configuration's {@code ConfigurationInterpolator}, but
     * does not define any other lookups.
     *
     * @return the {@code ConfigurationInterpolator}
     */
    private ConfigurationInterpolator initLocalInterpolator() {
        return new ConfigurationInterpolator() {
            @Override
            protected Lookup fetchLookupForPrefix(final String prefix) {
                return nullSafeLookup(getInterpolator().getLookups().get(prefix));
            }
        };
    }

    @Override
    public Configuration interpolatedConfiguration() {
        return getCurrentConfig().interpolatedConfiguration();
    }

    /**
     * Invalidates the current combined configuration. This means that the next time a property is accessed the combined
     * node structure must be re-constructed. Invalidation of a combined configuration also means that an event of type
     * {@code EVENT_COMBINED_INVALIDATE} is fired. Note that while other events most times appear twice (once before and
     * once after an update), this event is only fired once (after update).
     */
    @Override
    public void invalidate() {
        getCurrentConfig().invalidate();
    }

    /**
     * Invalidates all CombinedConfigurations.
     */
    public void invalidateAll() {
        configs.values().forEach(CombinedConfiguration::invalidate);
    }

    @Override
    protected boolean isEmptyInternal() {
        return getCurrentConfig().isEmpty();
    }

    /**
     * Decrements the lock count of the current configuration holder. If it reaches 0, the current configuration is removed.
     * (It is then reevaluated when the next operation starts.)
     */
    private void releaseLock() {
        final CurrentConfigHolder cch = CURRENT_CONFIG.get();
        assert cch != null : "No current configuration!";
        if (cch.decrementLockCountAndCheckRelease()) {
            CURRENT_CONFIG.remove();
        }
    }

    /**
     * Removes the specified configuration from this combined configuration.
     *
     * @param config the configuration to be removed
     * @return a flag whether this configuration was found and could be removed
     */
    @Override
    public boolean removeConfiguration(final Configuration config) {
        beginWrite(false);
        try {
            for (int index = 0; index < getNumberOfConfigurations(); index++) {
                if (configurations.get(index).getConfiguration() == config) {
                    removeConfigurationAt(index);
                    return true;
                }
            }

            return false;
        } finally {
            endWrite();
        }
    }

    /**
     * Removes the configuration with the specified name.
     *
     * @param name the name of the configuration to be removed
     * @return the removed configuration (<strong>null</strong> if this configuration was not found)
     */
    @Override
    public Configuration removeConfiguration(final String name) {
        final Configuration conf = getConfiguration(name);
        if (conf != null) {
            removeConfiguration(conf);
        }
        return conf;
    }

    /**
     * Removes the configuration at the specified index.
     *
     * @param index the index
     * @return the removed configuration
     */
    @Override
    public Configuration removeConfigurationAt(final int index) {
        beginWrite(false);
        try {
            final ConfigData cd = configurations.remove(index);
            if (cd.getName() != null) {
                namedConfigurations.remove(cd.getName());
            }
            return cd.getConfiguration();
        } finally {
            endWrite();
        }
    }

    @Override
    public <T extends Event> boolean removeEventListener(final EventType<T> eventType, final EventListener<? super T> listener) {
        configs.values().forEach(cc -> cc.removeEventListener(eventType, listener));
        return super.removeEventListener(eventType, listener);
    }

    /**
     * Sets the key pattern for the CombinedConfiguration map.
     *
     * @param pattern the key pattern for the CombinedConfiguration map.
     */
    public void setKeyPattern(final String pattern) {
        this.keyPattern = pattern;
    }

    /**
     * Sets the name of the Logger to use on each CombinedConfiguration.
     *
     * @param name The Logger name.
     */
    public void setLoggerName(final String name) {
        this.loggerName = name;
    }

    /**
     * Sets the node combiner. This object will be used when the combined node structure is to be constructed. It must not
     * be <strong>null</strong>, otherwise an {@code IllegalArgumentException} exception is thrown. Changing the node combiner causes
     * an invalidation of this combined configuration, so that the new combiner immediately takes effect.
     *
     * @param nodeCombiner the node combiner
     */
    @Override
    public void setNodeCombiner(final NodeCombiner nodeCombiner) {
        if (nodeCombiner == null) {
            throw new IllegalArgumentException("Node combiner must not be null!");
        }
        this.nodeCombiner = nodeCombiner;
        invalidateAll();
    }

    @Override
    protected void setPropertyInternal(final String key, final Object value) {
        getCurrentConfig().setProperty(key, value);
    }

    @Override
    protected int sizeInternal() {
        return getCurrentConfig().size();
    }

    @Override
    public Configuration subset(final String prefix) {
        return getCurrentConfig().subset(prefix);
    }
}
