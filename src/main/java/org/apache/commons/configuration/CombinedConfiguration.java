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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.event.EventSource;
import org.apache.commons.configuration.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration.sync.LockMode;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.DefaultConfigurationKey;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.apache.commons.configuration.tree.DefaultExpressionEngine;
import org.apache.commons.configuration.tree.ExpressionEngine;
import org.apache.commons.configuration.tree.NodeCombiner;
import org.apache.commons.configuration.tree.TreeUtils;
import org.apache.commons.configuration.tree.UnionCombiner;
import org.apache.commons.configuration.tree.ViewNode;

/**
 * <p>
 * A hierarchical composite configuration class.
 * </p>
 * <p>
 * This class maintains a list of configuration objects, which can be added
 * using the diverse {@code addConfiguration()} methods. After that the
 * configurations can be accessed either by name (if one was provided when the
 * configuration was added) or by index. For the whole set of managed
 * configurations a logical node structure is constructed. For this purpose a
 * {@link org.apache.commons.configuration.tree.NodeCombiner NodeCombiner}
 * object can be set. This makes it possible to specify different algorithms for
 * the combination process.
 * </p>
 * <p>
 * The big advantage of this class is that it creates a truly hierarchical
 * structure of all the properties stored in the contained configurations - even
 * if some of them are no hierarchical configurations per se. So all enhanced
 * features provided by a hierarchical configuration (e.g. choosing an
 * expression engine) are applicable.
 * </p>
 * <p>
 * The class works by registering itself as an event listener at all added
 * configurations. So it gets notified whenever one of these configurations is
 * changed and can invalidate its internal node structure. The next time a
 * property is accessed the node structure will be re-constructed using the
 * current state of the managed configurations. Note that, depending on the used
 * {@code NodeCombiner}, this may be a complex operation.
 * </p>
 * <p>
 * Because of the way a {@code CombinedConfiguration} is working it has
 * more or less view character: it provides a logic view on the configurations
 * it contains. In this constellation not all methods defined for hierarchical
 * configurations - especially methods that update the stored properties - can
 * be implemented in a consistent manner. Using such methods (like
 * {@code addProperty()}, or {@code clearProperty()} on a
 * {@code CombinedConfiguration} is not strictly forbidden, however,
 * depending on the current {@link NodeCombiner} and the involved
 * properties, the results may be different than expected. Some examples may
 * illustrate this:
 * </p>
 * <p>
 * <ul>
 * <li>Imagine a {@code CombinedConfiguration} <em>cc</em> containing
 * two child configurations with the following content:
 * <dl>
 * <dt>user.properties</dt>
 * <dd>
 *
 * <pre>
 * gui.background = blue
 * gui.position = (10, 10, 400, 200)
 * </pre>
 *
 * </dd>
 * <dt>default.properties</dt>
 * <dd>
 *
 * <pre>
 * gui.background = black
 * gui.foreground = white
 * home.dir = /data
 * </pre>
 *
 * </dd>
 * </dl>
 * As a {@code NodeCombiner} a
 * {@link org.apache.commons.configuration.tree.OverrideCombiner OverrideCombiner}
 * is used. This combiner will ensure that defined user settings take precedence
 * over the default values. If the resulting {@code CombinedConfiguration}
 * is queried for the background color, {@code blue} will be returned
 * because this value is defined in {@code user.properties}. Now
 * consider what happens if the key {@code gui.background} is removed
 * from the {@code CombinedConfiguration}:
 *
 * <pre>cc.clearProperty("gui.background");</pre>
 *
 * Will a {@code cc.containsKey("gui.background")} now return <b>false</b>?
 * No, it won't! The {@code clearProperty()} operation is executed on the
 * node set of the combined configuration, which was constructed from the nodes
 * of the two child configurations. It causes the value of the
 * <em>background</em> node to be cleared, which is also part of the first
 * child configuration. This modification of one of its child configurations
 * causes the {@code CombinedConfiguration} to be re-constructed. This
 * time the {@code OverrideCombiner} cannot find a
 * {@code gui.background} property in the first child configuration, but
 * it finds one in the second, and adds it to the resulting combined
 * configuration. So the property is still present (with a different value now).</li>
 * <li>{@code addProperty()} can also be problematic: Most node
 * combiners use special view nodes for linking parts of the original
 * configurations' data together. If new properties are added to such a special
 * node, they do not belong to any of the managed configurations and thus hang
 * in the air. Using the same configurations as in the last example, the
 * statement
 *
 * <pre>
 * addProperty("database.user", "scott");
 * </pre>
 *
 * would cause such a hanging property. If now one of the child configurations
 * is changed and the {@code CombinedConfiguration} is re-constructed,
 * this property will disappear! (Add operations are not problematic if they
 * result in a child configuration being updated. For instance an
 * {@code addProperty("home.url", "localhost");} will alter the second
 * child configuration - because the prefix <em>home</em> is here already
 * present; when the {@code CombinedConfiguration} is re-constructed,
 * this change is taken into account.)</li>
 * </ul>
 * Because of such problems it is recommended to perform updates only on the
 * managed child configurations.
 * </p>
 * <p>
 * Whenever the node structure of a {@code CombinedConfiguration} becomes
 * invalid (either because one of the contained configurations was modified or
 * because the {@code invalidate()} method was directly called) an event
 * is generated. So this can be detected by interested event listeners. This
 * also makes it possible to add a combined configuration into another one.
 * </p>
 * <p>
 * Notes about thread-safety: This configuration implementation uses a
 * {@code Synchronizer} object to protect instances against concurrent access.
 * The concrete {@code Synchronizer} implementation used determines whether an
 * instance of this class is thread-safe or not. All methods accessing
 * configuration data or querying or altering this configuration's child
 * configurations are guarded by the {@code Synchronizer}. Because a combined
 * configuration operates on node structures partly owned by its child
 * configurations it makes sense that a single {@code Synchronizer} object is
 * used and shared between all involved configurations (including the combined
 * configuration itself). However, this is not enforced.
 * </p>
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @since 1.3
 * @version $Id$
 */
public class CombinedConfiguration extends BaseHierarchicalConfiguration implements
        ConfigurationListener, Cloneable
{
    /**
     * Constant for the invalidate event that is fired when the internal node
     * structure becomes invalid.
     */
    public static final int EVENT_COMBINED_INVALIDATE = 40;

    /**
     * The serial version ID.
     */
    private static final long serialVersionUID = 8338574525528692307L;

    /** Constant for the expression engine for parsing the at path. */
    private static final DefaultExpressionEngine AT_ENGINE = DefaultExpressionEngine.INSTANCE;

    /** Constant for the default node combiner. */
    private static final NodeCombiner DEFAULT_COMBINER = new UnionCombiner();

    /** Stores the combiner. */
    private NodeCombiner nodeCombiner;

    /** Stores the combined root node. */
    private ConfigurationNode combinedRoot;

    /** Stores a list with the contained configurations. */
    private List<ConfigData> configurations;

    /** Stores a map with the named configurations. */
    private Map<String, Configuration> namedConfigurations;

    /**
     * An expression engine used for converting child configurations to
     * hierarchical ones.
     */
    private ExpressionEngine conversionExpressionEngine;

    /**
     * Creates a new instance of {@code CombinedConfiguration} and
     * initializes the combiner to be used.
     *
     * @param comb the node combiner (can be <b>null</b>, then a union combiner
     * is used as default)
     */
    public CombinedConfiguration(NodeCombiner comb)
    {
        nodeCombiner = (comb != null) ? comb : DEFAULT_COMBINER;
        initChildCollections();
    }

    /**
     * Creates a new instance of {@code CombinedConfiguration} that uses
     * a union combiner.
     *
     * @see org.apache.commons.configuration.tree.UnionCombiner
     */
    public CombinedConfiguration()
    {
        this(null);
    }

    /**
     * Returns the node combiner that is used for creating the combined node
     * structure.
     *
     * @return the node combiner
     */
    public NodeCombiner getNodeCombiner()
    {
        beginRead(true);
        try
        {
            return nodeCombiner;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Sets the node combiner. This object will be used when the combined node
     * structure is to be constructed. It must not be <b>null</b>, otherwise an
     * {@code IllegalArgumentException} exception is thrown. Changing the
     * node combiner causes an invalidation of this combined configuration, so
     * that the new combiner immediately takes effect.
     *
     * @param nodeCombiner the node combiner
     */
    public void setNodeCombiner(NodeCombiner nodeCombiner)
    {
        if (nodeCombiner == null)
        {
            throw new IllegalArgumentException(
                    "Node combiner must not be null!");
        }

        beginWrite(true);
        try
        {
            this.nodeCombiner = nodeCombiner;
            invalidateInternal();
        }
        finally
        {
            endWrite();
        }
    }

    /**
     * Returns the {@code ExpressionEngine} for converting flat child
     * configurations to hierarchical ones.
     *
     * @return the conversion expression engine
     * @since 1.6
     */
    public ExpressionEngine getConversionExpressionEngine()
    {
        beginRead(true);
        try
        {
            return conversionExpressionEngine;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Sets the {@code ExpressionEngine} for converting flat child
     * configurations to hierarchical ones. When constructing the root node for
     * this combined configuration the properties of all child configurations
     * must be combined to a single hierarchical node structure. In this
     * process, non hierarchical configurations are converted to hierarchical
     * ones first. This can be problematic if a child configuration contains
     * keys that are no compatible with the default expression engine used by
     * hierarchical configurations. Therefore it is possible to specify a
     * specific expression engine to be used for this purpose.
     *
     * @param conversionExpressionEngine the conversion expression engine
     * @see ConfigurationUtils#convertToHierarchical(Configuration, ExpressionEngine)
     * @since 1.6
     */
    public void setConversionExpressionEngine(
            ExpressionEngine conversionExpressionEngine)
    {
        beginWrite(true);
        try
        {
            this.conversionExpressionEngine = conversionExpressionEngine;
        }
        finally
        {
            endWrite();
        }
    }

    /**
     * Adds a new configuration to this combined configuration. It is possible
     * (but not mandatory) to give the new configuration a name. This name must
     * be unique, otherwise a {@code ConfigurationRuntimeException} will
     * be thrown. With the optional {@code at} argument you can specify
     * where in the resulting node structure the content of the added
     * configuration should appear. This is a string that uses dots as property
     * delimiters (independent on the current expression engine). For instance
     * if you pass in the string {@code "database.tables"},
     * all properties of the added configuration will occur in this branch.
     *
     * @param config the configuration to add (must not be <b>null</b>)
     * @param name the name of this configuration (can be <b>null</b>)
     * @param at the position of this configuration in the combined tree (can be
     * <b>null</b>)
     */
    public void addConfiguration(Configuration config, String name,
            String at)
    {
        if (config == null)
        {
            throw new IllegalArgumentException(
                    "Added configuration must not be null!");
        }

        beginWrite(true);
        try
        {
            if (name != null && namedConfigurations.containsKey(name))
            {
                throw new ConfigurationRuntimeException(
                        "A configuration with the name '"
                                + name
                                + "' already exists in this combined configuration!");
            }

            ConfigData cd = new ConfigData(config, name, at);
            if (getLogger().isDebugEnabled())
            {
                getLogger()
                        .debug("Adding configuration " + config + " with name "
                                + name);
            }
            configurations.add(cd);
            if (name != null)
            {
                namedConfigurations.put(name, config);
            }

            invalidateInternal();
        }
        finally
        {
            endWrite();
        }
        registerListenerAt(config);
    }

    /**
     * Adds a new configuration to this combined configuration with an optional
     * name. The new configuration's properties will be added under the root of
     * the combined node structure.
     *
     * @param config the configuration to add (must not be <b>null</b>)
     * @param name the name of this configuration (can be <b>null</b>)
     */
    public void addConfiguration(Configuration config, String name)
    {
        addConfiguration(config, name, null);
    }

    /**
     * Adds a new configuration to this combined configuration. The new
     * configuration is not given a name. Its properties will be added under the
     * root of the combined node structure.
     *
     * @param config the configuration to add (must not be <b>null</b>)
     */
    public void addConfiguration(Configuration config)
    {
        addConfiguration(config, null, null);
    }

    /**
     * Returns the number of configurations that are contained in this combined
     * configuration.
     *
     * @return the number of contained configurations
     */
    public int getNumberOfConfigurations()
    {
        beginRead(true);
        try
        {
            return getNumberOfConfigurationsInternal();
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Returns the configuration at the specified index. The contained
     * configurations are numbered in the order they were added to this combined
     * configuration. The index of the first configuration is 0.
     *
     * @param index the index
     * @return the configuration at this index
     */
    public Configuration getConfiguration(int index)
    {
        beginRead(true);
        try
        {
            ConfigData cd = configurations.get(index);
            return cd.getConfiguration();
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Returns the configuration with the given name. This can be <b>null</b>
     * if no such configuration exists.
     *
     * @param name the name of the configuration
     * @return the configuration with this name
     */
    public Configuration getConfiguration(String name)
    {
        beginRead(true);
        try
        {
            return namedConfigurations.get(name);
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Returns a List of all the configurations that have been added.
     * @return A List of all the configurations.
     * @since 1.7
     */
    public List<Configuration> getConfigurations()
    {
        beginRead(true);
        try
        {
            List<Configuration> list =
                    new ArrayList<Configuration>(getNumberOfConfigurationsInternal());
            for (ConfigData cd : configurations)
            {
                list.add(cd.getConfiguration());
            }
            return list;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Returns a List of the names of all the configurations that have been
     * added in the order they were added. A NULL value will be present in
     * the list for each configuration that was added without a name.
     * @return A List of all the configuration names.
     * @since 1.7
     */
    public List<String> getConfigurationNameList()
    {
        beginRead(true);
        try
        {
            List<String> list = new ArrayList<String>(getNumberOfConfigurationsInternal());
            for (ConfigData cd : configurations)
            {
                list.add(cd.getName());
            }
            return list;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Removes the specified configuration from this combined configuration.
     *
     * @param config the configuration to be removed
     * @return a flag whether this configuration was found and could be removed
     */
    public boolean removeConfiguration(Configuration config)
    {
        for (int index = 0; index < getNumberOfConfigurations(); index++)
        {
            if (configurations.get(index).getConfiguration() == config)
            {
                removeConfigurationAt(index);
                return true;
            }
        }

        return false;
    }

    /**
     * Removes the configuration at the specified index.
     *
     * @param index the index
     * @return the removed configuration
     */
    public Configuration removeConfigurationAt(int index)
    {
        ConfigData cd = configurations.remove(index);
        if (cd.getName() != null)
        {
            namedConfigurations.remove(cd.getName());
        }
        unregisterListenerAt(cd.getConfiguration());
        invalidateInternal();
        return cd.getConfiguration();
    }

    /**
     * Removes the configuration with the specified name.
     *
     * @param name the name of the configuration to be removed
     * @return the removed configuration (<b>null</b> if this configuration
     * was not found)
     */
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
     * Returns a set with the names of all configurations contained in this
     * combined configuration. Of course here are only these configurations
     * listed, for which a name was specified when they were added.
     *
     * @return a set with the names of the contained configurations (never
     * <b>null</b>)
     */
    public Set<String> getConfigurationNames()
    {
        beginRead(true);
        try
        {
            return namedConfigurations.keySet();
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Invalidates this combined configuration. This means that the next time a
     * property is accessed the combined node structure must be re-constructed.
     * Invalidation of a combined configuration also means that an event of type
     * {@code EVENT_COMBINED_INVALIDATE} is fired. Note that while other
     * events most times appear twice (once before and once after an update),
     * this event is only fired once (after update).
     */
    public void invalidate()
    {
        beginWrite(true);
        try
        {
            invalidateInternal();
        }
        finally
        {
            endWrite();
        }
    }

    /**
     * Event listener call back for configuration update events. This method is
     * called whenever one of the contained configurations was modified. It
     * invalidates this combined configuration.
     *
     * @param event the update event
     */
    @Override
    public void configurationChanged(ConfigurationEvent event)
    {
        if (event.isBeforeUpdate())
        {
            invalidate();
        }
    }

    /**
     * Returns the configuration root node of this combined configuration. When
     * starting a read or write operation (by obtaining a corresponding lock for
     * this configuration) a combined node structure is constructed if necessary
     * using the current node combiner. This method just returns this combined
     * node. Note that this method should only be called with a lock held!
     * Otherwise, result may be <b>null</b> under certain circumstances.
     *
     * @return the combined root node
     */
    @Override
    public ConfigurationNode getRootNode()
    {
        return combinedRoot;
    }

    /**
     * Clears this configuration. All contained configurations will be removed.
     */
    @Override
    protected void clearInternal()
    {
        initChildCollections();
        invalidateInternal();
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
        beginRead(false);
        try
        {
            CombinedConfiguration copy = (CombinedConfiguration) super.clone();
            copy.initChildCollections();
            for (ConfigData cd : configurations)
            {
                copy.addConfiguration(ConfigurationUtils.cloneConfiguration(cd
                        .getConfiguration()), cd.getName(), cd.getAt());
            }

            copy.setRootNode(new DefaultConfigurationNode());
            return copy;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Returns the configuration source, in which the specified key is defined.
     * This method will determine the configuration node that is identified by
     * the given key. The following constellations are possible:
     * <ul>
     * <li>If no node object is found for this key, <b>null</b> is returned.</li>
     * <li>If the key maps to multiple nodes belonging to different
     * configuration sources, a {@code IllegalArgumentException} is
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
     * @since 1.5
     */
    public Configuration getSource(String key)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("Key must not be null!");
        }

        beginRead(false);
        try
        {
            List<ConfigurationNode> nodes = fetchNodeList(key);
            if (nodes.isEmpty())
            {
                return null;
            }

            Iterator<ConfigurationNode> it = nodes.iterator();
            Configuration source = findSourceConfiguration(it.next());
            while (it.hasNext())
            {
                Configuration src = findSourceConfiguration(it.next());
                if (src != source)
                {
                    throw new IllegalArgumentException("The key " + key
                            + " is defined by multiple sources!");
                }
            }

            return source;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * {@inheritDoc} This implementation checks whether a combined root node
     * is available. If not, it is constructed by requesting a write lock.
     */
    @Override
    protected void beginRead(boolean optimize)
    {
        if (optimize)
        {
            // just need a lock, don't construct configuration
            super.beginRead(true);
            return;
        }

        boolean lockObtained = false;
        do
        {
            super.beginRead(optimize);
            if (combinedRoot != null)
            {
                lockObtained = true;
            }
            else
            {
                // release read lock and try to obtain a write lock
                endRead();
                beginWrite(false); // this constructs the root node
                endWrite();
            }
        } while (!lockObtained);
    }

    /**
     * {@inheritDoc} This implementation checks whether a combined root node
     * is available. If not, it is constructed now.
     */
    @Override
    protected void beginWrite(boolean optimize)
    {
        super.beginWrite(true);
        if(optimize)
        {
            // just need a lock, don't construct configuration
            return;
        }

        try
        {
            if (combinedRoot == null)
            {
                combinedRoot = constructCombinedNode();
            }
        }
        catch (RuntimeException rex)
        {
            endWrite();
            throw rex;
        }
    }

    /**
     * Marks this configuration as invalid. This means that the next access
     * re-creates the root node. An invalidate event is also fired. Note:
     * This implementation expects that an exclusive (write) lock is held on
     * this instance.
     */
    private void invalidateInternal()
    {
        combinedRoot = null;
        fireEvent(EVENT_COMBINED_INVALIDATE, null, null, false);
    }

    /**
     * Initializes internal data structures for storing information about
     * child configurations.
     */
    private void initChildCollections()
    {
        configurations = new ArrayList<ConfigData>();
        namedConfigurations = new HashMap<String, Configuration>();
    }

    /**
     * Creates the root node of this combined configuration.
     *
     * @return the combined root node
     */
    private ConfigurationNode constructCombinedNode()
    {
        if (getNumberOfConfigurationsInternal() < 1)
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("No configurations defined for " + this);
            }
            return new ViewNode();
        }

        else
        {
            Iterator<ConfigData> it = configurations.iterator();
            ConfigurationNode node = it.next().getTransformedRoot();
            while (it.hasNext())
            {
                node = nodeCombiner.combine(node,
                        it.next().getTransformedRoot());
            }
            if (getLogger().isDebugEnabled())
            {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                PrintStream stream = new PrintStream(os);
                TreeUtils.printTree(stream, node);
                getLogger().debug(os.toString());
            }
            return node;
        }
    }

    /**
     * Determines the configuration that owns the specified node.
     *
     * @param node the node
     * @return the owning configuration
     */
    private Configuration findSourceConfiguration(ConfigurationNode node)
    {
        ConfigurationNode root = null;
        ConfigurationNode current = node;

        // find the root node in this hierarchy
        while (current != null)
        {
            root = current;
            current = current.getParentNode();
        }

        // Check with the root nodes of the child configurations
        for (ConfigData cd : configurations)
        {
            if (root == cd.getRootNode())
            {
                return cd.getConfiguration();
            }
        }

        return this;
    }

    /**
     * Registers this combined configuration as listener at the given child
     * configuration.
     *
     * @param configuration the child configuration
     */
    private void registerListenerAt(Configuration configuration)
    {
        if (configuration instanceof EventSource)
        {
            ((EventSource) configuration).addConfigurationListener(this);
        }
    }

    /**
     * Removes this combined configuration as listener from the given child
     * configuration.
     *
     * @param configuration the child configuration
     */
    private void unregisterListenerAt(Configuration configuration)
    {
        if (configuration instanceof EventSource)
        {
            ((EventSource) configuration).removeConfigurationListener(this);
        }
    }

    /**
     * Returns the number of child configurations in this combined
     * configuration. The internal list of child configurations is accessed
     * without synchronization.
     *
     * @return the number of child configurations
     */
    private int getNumberOfConfigurationsInternal()
    {
        return configurations.size();
    }

    /**
     * An internal helper class for storing information about contained
     * configurations.
     */
    private class ConfigData
    {
        /** Stores a reference to the configuration. */
        private Configuration configuration;

        /** Stores the name under which the configuration is stored. */
        private String name;

        /** Stores the at information as path of nodes. */
        private Collection<String> atPath;

        /** Stores the at string.*/
        private String at;

        /** Stores the root node for this child configuration.*/
        private ConfigurationNode rootNode;

        /**
         * Creates a new instance of {@code ConfigData} and initializes
         * it.
         *
         * @param config the configuration
         * @param n the name
         * @param at the at position
         */
        public ConfigData(Configuration config, String n, String at)
        {
            configuration = config;
            name = n;
            atPath = parseAt(at);
            this.at = at;
        }

        /**
         * Returns the stored configuration.
         *
         * @return the configuration
         */
        public Configuration getConfiguration()
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

        /**
         * Returns the root node for this child configuration.
         *
         * @return the root node of this child configuration
         * @since 1.5
         */
        public ConfigurationNode getRootNode()
        {
            return rootNode;
        }

        /**
         * Returns the transformed root node of the stored configuration. The
         * term &quot;transformed&quot; means that an eventually defined at path
         * has been applied.
         *
         * @return the transformed root node
         */
        public ConfigurationNode getTransformedRoot()
        {
            ViewNode result = new ViewNode();
            ViewNode atParent = result;

            if (atPath != null)
            {
                // Build the complete path
                for (String p : atPath)
                {
                    ViewNode node = new ViewNode();
                    node.setName(p);
                    atParent.addChild(node);
                    atParent = node;
                }
            }

            // Copy data of the root node to the new path
            getConfiguration().lock(LockMode.READ);
            try
            {
                ConfigurationNode root =
                        ConfigurationUtils.convertToHierarchical(
                                getConfiguration(), conversionExpressionEngine)
                                .getRootNode();
                atParent.appendChildren(root);
                atParent.appendAttributes(root);
                rootNode = root;
            }
            finally
            {
                getConfiguration().unlock(LockMode.READ);
            }

            return result;
        }

        /**
         * Splits the at path into its components.
         *
         * @param at the at string
         * @return a collection with the names of the single components
         */
        private Collection<String> parseAt(String at)
        {
            if (at == null)
            {
                return null;
            }

            Collection<String> result = new ArrayList<String>();
            DefaultConfigurationKey.KeyIterator it = new DefaultConfigurationKey(
                    AT_ENGINE, at).iterator();
            while (it.hasNext())
            {
                result.add(it.nextKey());
            }
            return result;
        }
    }
}
