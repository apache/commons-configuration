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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.DefaultConfigurationKey;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.apache.commons.configuration.tree.DefaultExpressionEngine;
import org.apache.commons.configuration.tree.NodeCombiner;
import org.apache.commons.configuration.tree.UnionCombiner;
import org.apache.commons.configuration.tree.ViewNode;

/**
 * <p>
 * A hierarchical composite configuration class.
 * </p>
 * <p>
 * This class maintains a list of configuration objects, which can be added
 * using the divers <code>addConfiguration()</code> methods. After that the
 * configurations can be accessed either by name (if one was provided when the
 * configuration was added) or by index. For the whole set of managed
 * configurations a logical node structure is constructed. For this purpose a
 * <code>{@link org.apache.commons.configuration.tree.NodeCombiner NodeCombiner}</code>
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
 * <code>NodeCombiner</code>, this may be a complex operation.
 * </p>
 * <p>
 * Because of the way a <code>CombinedConfiguration</code> is working it has
 * more or less view character: it provides a logic view on the configurations
 * it contains. In this constellation not all methods defined for hierarchical
 * configurations - especially methods that update the stored properties - can
 * be implemented in a consistent manner. Using such methods (like
 * <code>addProperty()</code>, or <code>clearProperty()</code> on a
 * <code>CombinedConfiguration</code> is not strictly forbidden, however,
 * depending on the current <code>{@link NodeCombiner}</code> and the involved
 * properties, the results may be different than expected. Some examples may
 * illustrate this:
 * </p>
 * <p>
 * <ul>
 * <li>Imagine a <code>CombinedConfiguration</code> <em>cc</em> containing
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
 * As a <code>NodeCombiner</code> a
 * <code>{@link org.apache.commons.configuration.tree.OverrideCombiner OverrideCombiner}</code>
 * is used. This combiner will ensure that defined user settings take precedence
 * over the default values. If the resulting <code>CombinedConfiguration</code>
 * is queried for the background color, <code>blue</code> will be returned
 * because this value is defined in <code>user.properties</code>. Now
 * consider what happens if the key <code>gui.background</code> is removed
 * from the <code>CombinedConfiguration</code>:
 *
 * <pre>cc.clearProperty("gui.background");</pre>
 *
 * Will a <code>cc.containsKey("gui.background")</code> now return <b>false</b>?
 * No, it won't! The <code>clearProperty()</code> operation is executed on the
 * node set of the combined configuration, which was constructed from the nodes
 * of the two child configurations. It causes the value of the
 * <em>background</em> node to be cleared, which is also part of the first
 * child configuration. This modification of one of its child configurations
 * causes the <code>CombinedConfiguration</code> to be re-constructed. This
 * time the <code>OverrideCombiner</code> cannot find a
 * <code>gui.background</code> property in the first child configuration, but
 * it finds one in the second, and adds it to the resulting combined
 * configuration. So the property is still present (with a different value now).</li>
 * <li><code>addProperty()</code> can also be problematic: Most node
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
 * is changed and the <code>CombinedConfiguration</code> is re-constructed,
 * this property will disappear! (Add operations are not problematic if they
 * result in a child configuration being updated. For instance an
 * <code>addProperty("home.url", "localhost");</code> will alter the second
 * child configuration - because the prefix <em>home</em> is here already
 * present; when the <code>CombinedConfiguration</code> is re-constructed,
 * this change is taken into account.)</li>
 * </ul>
 * Because of such problems it is recommended to perform updates only on the
 * managed child configurations.
 * </p>
 * <p>
 * Whenever the node structure of a <code>CombinedConfiguration</code> becomes
 * invalid (either because one of the contained configurations was modified or
 * because the <code>invalidate()</code> method was directly called) an event
 * is generated. So this can be detected by interested event listeners. This
 * also makes it possible to add a combined configuration into another one.
 * </p>
 * <p>
 * Implementation note: Adding and removing configurations to and from a
 * combined configuration is not thread-safe. If a combined configuration is
 * manipulated by multiple threads, the developer has to take care about
 * properly synchronization.
 * </p>
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @since 1.3
 * @version $Id$
 */
public class CombinedConfiguration extends HierarchicalConfiguration implements
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
    private static final DefaultExpressionEngine AT_ENGINE = new DefaultExpressionEngine();

    /** Constant for the default node combiner. */
    private static final NodeCombiner DEFAULT_COMBINER = new UnionCombiner();

    /** Constant for the name of the property used for the reload check.*/
    private static final String PROP_RELOAD_CHECK = "CombinedConfigurationReloadCheck";

    /** Stores the combiner. */
    private NodeCombiner nodeCombiner;

    /** Stores the combined root node. */
    private ConfigurationNode combinedRoot;

    /** Stores a list with the contained configurations. */
    private List configurations;

    /** Stores a map with the named configurations. */
    private Map namedConfigurations;

    /** A flag whether an enhanced reload check is to be performed.*/
    private boolean forceReloadCheck;

    /**
     * Creates a new instance of <code>CombinedConfiguration</code> and
     * initializes the combiner to be used.
     *
     * @param comb the node combiner (can be <b>null</b>, then a union combiner
     * is used as default)
     */
    public CombinedConfiguration(NodeCombiner comb)
    {
        setNodeCombiner((comb != null) ? comb : DEFAULT_COMBINER);
        clear();
    }

    /**
     * Creates a new instance of <code>CombinedConfiguration</code> that uses
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
    public void setNodeCombiner(NodeCombiner nodeCombiner)
    {
        if (nodeCombiner == null)
        {
            throw new IllegalArgumentException(
                    "Node combiner must not be null!");
        }
        this.nodeCombiner = nodeCombiner;
        invalidate();
    }

    /**
     * Returns a flag whether an enhanced reload check must be performed.
     *
     * @return the force reload check flag
     * @since 1.4
     */
    public boolean isForceReloadCheck()
    {
        return forceReloadCheck;
    }

    /**
     * Sets the force reload check flag. If this flag is set, each property
     * access on this configuration will cause a reload check on the contained
     * configurations. This is a workaround for a problem with some reload
     * implementations that only check if a reload is required when they are
     * triggered. Per default this mode is disabled. If the force reload check
     * flag is set to <b>true</b>, accessing properties will be less
     * performant, but reloads on contained configurations will be detected.
     *
     * @param forceReloadCheck the value of the flag
     * @since 1.4
     */
    public void setForceReloadCheck(boolean forceReloadCheck)
    {
        this.forceReloadCheck = forceReloadCheck;
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
    public void addConfiguration(AbstractConfiguration config, String name,
            String at)
    {
        if (config == null)
        {
            throw new IllegalArgumentException(
                    "Added configuration must not be null!");
        }
        if (name != null && namedConfigurations.containsKey(name))
        {
            throw new ConfigurationRuntimeException(
                    "A configuration with the name '"
                            + name
                            + "' already exists in this combined configuration!");
        }

        ConfigData cd = new ConfigData(config, name, at);
        configurations.add(cd);
        if (name != null)
        {
            namedConfigurations.put(name, config);
        }

        config.addConfigurationListener(this);
        invalidate();
    }

    /**
     * Adds a new configuration to this combined configuration with an optional
     * name. The new configuration's properties will be added under the root of
     * the combined node structure.
     *
     * @param config the configuration to add (must not be <b>null</b>)
     * @param name the name of this configuration (can be <b>null</b>)
     */
    public void addConfiguration(AbstractConfiguration config, String name)
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
    public void addConfiguration(AbstractConfiguration config)
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
    public Configuration getConfiguration(int index)
    {
        ConfigData cd = (ConfigData) configurations.get(index);
        return cd.getConfiguration();
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
        return (Configuration) namedConfigurations.get(name);
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
            if (((ConfigData) configurations.get(index)).getConfiguration() == config)
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
        ConfigData cd = (ConfigData) configurations.remove(index);
        if (cd.getName() != null)
        {
            namedConfigurations.remove(cd.getName());
        }
        cd.getConfiguration().removeConfigurationListener(this);
        invalidate();
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
    public Set getConfigurationNames()
    {
        return namedConfigurations.keySet();
    }

    /**
     * Invalidates this combined configuration. This means that the next time a
     * property is accessed the combined node structure must be re-constructed.
     * Invalidation of a combined configuration also means that an event of type
     * <code>EVENT_COMBINED_INVALIDATE</code> is fired. Note that while other
     * events most times appear twice (once before and once after an update),
     * this event is only fired once (after update).
     */
    public void invalidate()
    {
        synchronized (getNodeCombiner()) // use combiner as lock
        {
            combinedRoot = null;
        }
        fireEvent(EVENT_COMBINED_INVALIDATE, null, null, false);
    }

    /**
     * Event listener call back for configuration update events. This method is
     * called whenever one of the contained configurations was modified. It
     * invalidates this combined configuration.
     *
     * @param event the update event
     */
    public void configurationChanged(ConfigurationEvent event)
    {
        invalidate();
    }

    /**
     * Returns the configuration root node of this combined configuration. This
     * method will construct a combined node structure using the current node
     * combiner if necessary.
     *
     * @return the combined root node
     */
    public ConfigurationNode getRootNode()
    {
        synchronized (getNodeCombiner())
        {
            if (combinedRoot == null)
            {
                combinedRoot = constructCombinedNode();
            }
            return combinedRoot;
        }
    }

    /**
     * Clears this configuration. All contained configurations will be removed.
     */
    public void clear()
    {
        fireEvent(EVENT_CLEAR, null, null, true);
        configurations = new ArrayList();
        namedConfigurations = new HashMap();
        fireEvent(EVENT_CLEAR, null, null, false);
        invalidate();
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
    public Object clone()
    {
        CombinedConfiguration copy = (CombinedConfiguration) super.clone();
        copy.clear();
        for (Iterator it = configurations.iterator(); it.hasNext();)
        {
            ConfigData cd = (ConfigData) it.next();
            copy.addConfiguration((AbstractConfiguration) ConfigurationUtils
                    .cloneConfiguration(cd.getConfiguration()), cd.getName(),
                    cd.getAt());
        }

        copy.setRootNode(new DefaultConfigurationNode());
        return copy;
    }

    /**
     * Returns the value of the specified property. This implementation
     * evaluates the <em>force reload check</em> flag. If it is set, all
     * contained configurations will be triggered before the value of the
     * requested property is retrieved.
     *
     * @param key the key of the desired property
     * @return the value of this property
     * @since 1.4
     */
    public Object getProperty(String key)
    {
        if (isForceReloadCheck())
        {
            for (Iterator it = configurations.iterator(); it.hasNext();)
            {
                try
                {
                    // simply retrieve a property; this is enough for
                    // triggering a reload
                    ((ConfigData) it.next()).getConfiguration().getProperty(
                            PROP_RELOAD_CHECK);
                }
                catch (Exception ex)
                {
                    // ignore all exceptions, e.g. missing property exceptions
                    ;
                }
            }
        }

        return super.getProperty(key);
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
     * @since 1.5
     */
    public Configuration getSource(String key)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("Key must not be null!");
        }

        List nodes = fetchNodeList(key);
        if (nodes.isEmpty())
        {
            return null;
        }

        Iterator it = nodes.iterator();
        Configuration source = findSourceConfiguration((ConfigurationNode) it
                .next());
        while (it.hasNext())
        {
            Configuration src = findSourceConfiguration((ConfigurationNode) it
                    .next());
            if (src != source)
            {
                throw new IllegalArgumentException("The key " + key
                        + " is defined by multiple sources!");
            }
        }

        return source;
    }

    /**
     * Creates the root node of this combined configuration.
     *
     * @return the combined root node
     */
    private ConfigurationNode constructCombinedNode()
    {
        if (getNumberOfConfigurations() < 1)
        {
            return new ViewNode();
        }

        else
        {
            Iterator it = configurations.iterator();
            ConfigurationNode node = ((ConfigData) it.next())
                    .getTransformedRoot();
            while (it.hasNext())
            {
                node = getNodeCombiner().combine(node,
                        ((ConfigData) it.next()).getTransformedRoot());
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
        for (Iterator it = configurations.iterator(); it.hasNext();)
        {
            ConfigData cd = (ConfigData) it.next();
            if (root == cd.getRootNode())
            {
                return cd.getConfiguration();
            }
        }

        return this;
    }

    /**
     * An internal helper class for storing information about contained
     * configurations.
     */
    static class ConfigData
    {
        /** Stores a reference to the configuration. */
        private AbstractConfiguration configuration;

        /** Stores the name under which the configuration is stored. */
        private String name;

        /** Stores the at information as path of nodes. */
        private Collection atPath;

        /** Stores the at string.*/
        private String at;

        /** Stores the root node for this child configuration.*/
        private ConfigurationNode rootNode;

        /**
         * Creates a new instance of <code>ConfigData</code> and initializes
         * it.
         *
         * @param config the configuration
         * @param n the name
         * @param at the at position
         */
        public ConfigData(AbstractConfiguration config, String n, String at)
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
        public AbstractConfiguration getConfiguration()
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
                for (Iterator it = atPath.iterator(); it.hasNext();)
                {
                    ViewNode node = new ViewNode();
                    node.setName((String) it.next());
                    atParent.addChild(node);
                    atParent = node;
                }
            }

            // Copy data of the root node to the new path
            HierarchicalConfiguration hc = ConfigurationUtils
                    .convertToHierarchical(getConfiguration());
            atParent.appendChildren(hc.getRootNode());
            atParent.appendAttributes(hc.getRootNode());
            rootNode = hc.getRootNode();

            return result;
        }

        /**
         * Splits the at path into its components.
         *
         * @param at the at string
         * @return a collection with the names of the single components
         */
        private Collection parseAt(String at)
        {
            if (at == null)
            {
                return null;
            }

            Collection result = new ArrayList();
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
