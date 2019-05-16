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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventSource;
import org.apache.commons.configuration2.event.EventType;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.sync.LockMode;
import org.apache.commons.configuration2.tree.DefaultConfigurationKey;
import org.apache.commons.configuration2.tree.DefaultExpressionEngine;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.NodeCombiner;
import org.apache.commons.configuration2.tree.NodeTreeWalker;
import org.apache.commons.configuration2.tree.QueryResult;
import org.apache.commons.configuration2.tree.TreeUtils;
import org.apache.commons.configuration2.tree.UnionCombiner;

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
 * {@link org.apache.commons.configuration2.tree.NodeCombiner NodeCombiner}
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
 * Because of the way a {@code CombinedConfiguration} is working it has more or
 * less view character: it provides a logic view on the configurations it
 * contains. In this constellation not all methods defined for hierarchical
 * configurations - especially methods that update the stored properties - can
 * be implemented in a consistent manner. Using such methods (like
 * {@code addProperty()}, or {@code clearProperty()} on a
 * {@code CombinedConfiguration} is not strictly forbidden, however, depending
 * on the current {@link NodeCombiner} and the involved properties, the results
 * may be different than expected. Some examples may illustrate this:
 * </p>
 * <ul>
 * <li>Imagine a {@code CombinedConfiguration} <em>cc</em> containing two child
 * configurations with the following content:
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
 * {@link org.apache.commons.configuration2.tree.OverrideCombiner
 * OverrideCombiner} is used. This combiner will ensure that defined user
 * settings take precedence over the default values. If the resulting
 * {@code CombinedConfiguration} is queried for the background color,
 * {@code blue} will be returned because this value is defined in
 * {@code user.properties}. Now consider what happens if the key
 * {@code gui.background} is removed from the {@code CombinedConfiguration}:
 *
 * <pre>
 * cc.clearProperty(&quot;gui.background&quot;);
 * </pre>
 *
 * Will a {@code cc.containsKey("gui.background")} now return <b>false</b>? No,
 * it won't! The {@code clearProperty()} operation is executed on the node set
 * of the combined configuration, which was constructed from the nodes of the
 * two child configurations. It causes the value of the <em>background</em> node
 * to be cleared, which is also part of the first child configuration. This
 * modification of one of its child configurations causes the
 * {@code CombinedConfiguration} to be re-constructed. This time the
 * {@code OverrideCombiner} cannot find a {@code gui.background} property in the
 * first child configuration, but it finds one in the second, and adds it to the
 * resulting combined configuration. So the property is still present (with a
 * different value now).</li>
 * <li>{@code addProperty()} can also be problematic: Most node combiners use
 * special view nodes for linking parts of the original configurations' data
 * together. If new properties are added to such a special node, they do not
 * belong to any of the managed configurations and thus hang in the air. Using
 * the same configurations as in the last example, the statement
 *
 * <pre>
 * addProperty(&quot;database.user&quot;, &quot;scott&quot;);
 * </pre>
 *
 * would cause such a hanging property. If now one of the child configurations
 * is changed and the {@code CombinedConfiguration} is re-constructed, this
 * property will disappear! (Add operations are not problematic if they result
 * in a child configuration being updated. For instance an
 * {@code addProperty("home.url", "localhost");} will alter the second child
 * configuration - because the prefix <em>home</em> is here already present;
 * when the {@code CombinedConfiguration} is re-constructed, this change is
 * taken into account.)</li>
 * </ul>
 * <p>
 * Because of such problems it is recommended to perform updates only on the
 * managed child configurations.
 * </p>
 * <p>
 * Whenever the node structure of a {@code CombinedConfiguration} becomes
 * invalid (either because one of the contained configurations was modified or
 * because the {@code invalidate()} method was directly called) an event is
 * generated. So this can be detected by interested event listeners. This also
 * makes it possible to add a combined configuration into another one.
 * </p>
 * <p>
 * Notes about thread-safety: This configuration implementation uses a
 * {@code Synchronizer} object to protect instances against concurrent access.
 * The concrete {@code Synchronizer} implementation used determines whether an
 * instance of this class is thread-safe or not. In contrast to other
 * implementations derived from {@link BaseHierarchicalConfiguration},
 * thread-safety is an issue here because the nodes structure used by this
 * configuration has to be constructed dynamically when a child configuration is
 * changed. Therefore, when multiple threads are involved which also manipulate
 * one of the child configurations, a proper {@code Synchronizer} object should
 * be set. Note that the {@code Synchronizer} objects used by the child
 * configurations do not really matter. Because immutable in-memory nodes
 * structures are used for them there is no danger that updates on child
 * configurations could interfere with read operations on the combined
 * configuration.
 * </p>
 *
 * @since 1.3
 */
public class CombinedConfiguration extends BaseHierarchicalConfiguration implements
        EventListener<ConfigurationEvent>
{
    /**
     * Constant for the event type fired when the internal node structure of a
     * combined configuration becomes invalid.
     *
     * @since 2.0
     */
    public static final EventType<ConfigurationEvent> COMBINED_INVALIDATE =
            new EventType<>(ConfigurationEvent.ANY,
                    "COMBINED_INVALIDATE");

    /** Constant for the expression engine for parsing the at path. */
    private static final DefaultExpressionEngine AT_ENGINE = DefaultExpressionEngine.INSTANCE;

    /** Constant for the default node combiner. */
    private static final NodeCombiner DEFAULT_COMBINER = new UnionCombiner();

    /** Constant for a root node for an empty configuration. */
    private static final ImmutableNode EMPTY_ROOT = new ImmutableNode.Builder()
            .create();

    /** Stores the combiner. */
    private NodeCombiner nodeCombiner;

    /** Stores a list with the contained configurations. */
    private List<ConfigData> configurations;

    /** Stores a map with the named configurations. */
    private Map<String, Configuration> namedConfigurations;

    /**
     * An expression engine used for converting child configurations to
     * hierarchical ones.
     */
    private ExpressionEngine conversionExpressionEngine;

    /** A flag whether this configuration is up-to-date. */
    private boolean upToDate;

    /**
     * Creates a new instance of {@code CombinedConfiguration} and
     * initializes the combiner to be used.
     *
     * @param comb the node combiner (can be <b>null</b>, then a union combiner
     * is used as default)
     */
    public CombinedConfiguration(final NodeCombiner comb)
    {
        nodeCombiner = (comb != null) ? comb : DEFAULT_COMBINER;
        initChildCollections();
    }

    /**
     * Creates a new instance of {@code CombinedConfiguration} that uses
     * a union combiner.
     *
     * @see org.apache.commons.configuration2.tree.UnionCombiner
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
    public void setNodeCombiner(final NodeCombiner nodeCombiner)
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
            final ExpressionEngine conversionExpressionEngine)
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
    public void addConfiguration(final Configuration config, final String name,
            final String at)
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

            final ConfigData cd = new ConfigData(config, name, at);
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
    public void addConfiguration(final Configuration config, final String name)
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
    public void addConfiguration(final Configuration config)
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
    public Configuration getConfiguration(final int index)
    {
        beginRead(true);
        try
        {
            final ConfigData cd = configurations.get(index);
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
    public Configuration getConfiguration(final String name)
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
            final List<Configuration> list =
                    new ArrayList<>(getNumberOfConfigurationsInternal());
            for (final ConfigData cd : configurations)
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
            final List<String> list = new ArrayList<>(getNumberOfConfigurationsInternal());
            for (final ConfigData cd : configurations)
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
    public boolean removeConfiguration(final Configuration config)
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
    public Configuration removeConfigurationAt(final int index)
    {
        final ConfigData cd = configurations.remove(index);
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
    public Configuration removeConfiguration(final String name)
    {
        final Configuration conf = getConfiguration(name);
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
    public void onEvent(final ConfigurationEvent event)
    {
        if (event.isBeforeUpdate())
        {
            invalidate();
        }
    }

    /**
     * Clears this configuration. All contained configurations will be removed.
     */
    @Override
    protected void clearInternal()
    {
        unregisterListenerAtChildren();
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
            final CombinedConfiguration copy = (CombinedConfiguration) super.clone();
            copy.initChildCollections();
            for (final ConfigData cd : configurations)
            {
                copy.addConfiguration(ConfigurationUtils.cloneConfiguration(cd
                        .getConfiguration()), cd.getName(), cd.getAt());
            }

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
    public Configuration getSource(final String key)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("Key must not be null!");
        }

        final Set<Configuration> sources = getSources(key);
        if (sources.isEmpty())
        {
            return null;
        }
        final Iterator<Configuration> iterator = sources.iterator();
        final Configuration source = iterator.next();
        if (iterator.hasNext())
        {
            throw new IllegalArgumentException("The key " + key
                    + " is defined by multiple sources!");
        }
        return source;
    }

    /**
     * Returns a set with the configuration sources, in which the specified key
     * is defined. This method determines the configuration nodes that are
     * identified by the given key. It then determines the configuration sources
     * to which these nodes belong and adds them to the result set. Note the
     * following points:
     * <ul>
     * <li>If no node object is found for this key, an empty set is returned.</li>
     * <li>For keys that have been added directly to this combined configuration
     * and that do not belong to the namespaces defined by existing child
     * configurations this combined configuration is contained in the result
     * set.</li>
     * </ul>
     *
     * @param key the key of a configuration property
     * @return a set with the configuration sources, which contain this property
     * @since 2.0
     */
    public Set<Configuration> getSources(final String key)
    {
        beginRead(false);
        try
        {
            final List<QueryResult<ImmutableNode>> results = fetchNodeList(key);
            final Set<Configuration> sources = new HashSet<>();

            for (final QueryResult<ImmutableNode> result : results)
            {
                final Set<Configuration> resultSources =
                        findSourceConfigurations(result.getNode());
                if (resultSources.isEmpty())
                {
                    // key must be defined in combined configuration
                    sources.add(this);
                }
                else
                {
                    sources.addAll(resultSources);
                }
            }

            return sources;
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
    protected void beginRead(final boolean optimize)
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
            super.beginRead(false);
            if (isUpToDate())
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
    protected void beginWrite(final boolean optimize)
    {
        super.beginWrite(true);
        if (optimize)
        {
            // just need a lock, don't construct configuration
            return;
        }

        try
        {
            if (!isUpToDate())
            {
                getSubConfigurationParentModel().replaceRoot(
                        constructCombinedNode(), this);
                upToDate = true;
            }
        }
        catch (final RuntimeException rex)
        {
            endWrite();
            throw rex;
        }
    }

    /**
     * Returns a flag whether this configuration has been invalidated. This
     * means that the combined nodes structure has to be rebuilt before the
     * configuration can be accessed.
     *
     * @return a flag whether this configuration is invalid
     */
    private boolean isUpToDate()
    {
        return upToDate;
    }

    /**
     * Marks this configuration as invalid. This means that the next access
     * re-creates the root node. An invalidate event is also fired. Note:
     * This implementation expects that an exclusive (write) lock is held on
     * this instance.
     */
    private void invalidateInternal()
    {
        upToDate = false;
        fireEvent(COMBINED_INVALIDATE, null, null, false);
    }

    /**
     * Initializes internal data structures for storing information about
     * child configurations.
     */
    private void initChildCollections()
    {
        configurations = new ArrayList<>();
        namedConfigurations = new HashMap<>();
    }

    /**
     * Creates the root node of this combined configuration.
     *
     * @return the combined root node
     */
    private ImmutableNode constructCombinedNode()
    {
        if (getNumberOfConfigurationsInternal() < 1)
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("No configurations defined for " + this);
            }
            return EMPTY_ROOT;
        }
        final Iterator<ConfigData> it = configurations.iterator();
        ImmutableNode node = it.next().getTransformedRoot();
        while (it.hasNext())
        {
            node = nodeCombiner.combine(node,
                    it.next().getTransformedRoot());
        }
        if (getLogger().isDebugEnabled())
        {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final PrintStream stream = new PrintStream(os);
            TreeUtils.printTree(stream, node);
            getLogger().debug(os.toString());
        }
        return node;
    }

    /**
     * Determines the configurations to which the specified node belongs. This
     * is done by inspecting the nodes structures of all child configurations.
     *
     * @param node the node
     * @return a set with the owning configurations
     */
    private Set<Configuration> findSourceConfigurations(final ImmutableNode node)
    {
        final Set<Configuration> result = new HashSet<>();
        final FindNodeVisitor<ImmutableNode> visitor =
                new FindNodeVisitor<>(node);

        for (final ConfigData cd : configurations)
        {
            NodeTreeWalker.INSTANCE.walkBFS(cd.getRootNode(), visitor,
                    getModel().getNodeHandler());
            if (visitor.isFound())
            {
                result.add(cd.getConfiguration());
                visitor.reset();
            }
        }

        return result;
    }

    /**
     * Registers this combined configuration as listener at the given child
     * configuration.
     *
     * @param configuration the child configuration
     */
    private void registerListenerAt(final Configuration configuration)
    {
        if (configuration instanceof EventSource)
        {
            ((EventSource) configuration).addEventListener(
                    ConfigurationEvent.ANY, this);
        }
    }

    /**
     * Removes this combined configuration as listener from the given child
     * configuration.
     *
     * @param configuration the child configuration
     */
    private void unregisterListenerAt(final Configuration configuration)
    {
        if (configuration instanceof EventSource)
        {
            ((EventSource) configuration).removeEventListener(
                    ConfigurationEvent.ANY, this);
        }
    }

    /**
     * Removes this combined configuration as listener from all child
     * configurations. This method is called on a clear() operation.
     */
    private void unregisterListenerAtChildren()
    {
        if (configurations != null)
        {
            for (final ConfigData child : configurations)
            {
                unregisterListenerAt(child.getConfiguration());
            }
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
        private final Configuration configuration;

        /** Stores the name under which the configuration is stored. */
        private final String name;

        /** Stores the at information as path of nodes. */
        private final Collection<String> atPath;

        /** Stores the at string.*/
        private final String at;

        /** Stores the root node for this child configuration.*/
        private ImmutableNode rootNode;

        /**
         * Creates a new instance of {@code ConfigData} and initializes
         * it.
         *
         * @param config the configuration
         * @param n the name
         * @param at the at position
         */
        public ConfigData(final Configuration config, final String n, final String at)
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
        public ImmutableNode getRootNode()
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
        public ImmutableNode getTransformedRoot()
        {
            final ImmutableNode configRoot = getRootNodeOfConfiguration();
            return (atPath == null) ? configRoot : prependAtPath(configRoot);
        }

        /**
         * Prepends the at path to the given node.
         *
         * @param node the root node of the represented configuration
         * @return the new root node including the at path
         */
        private ImmutableNode prependAtPath(final ImmutableNode node)
        {
            final ImmutableNode.Builder pathBuilder = new ImmutableNode.Builder();
            final Iterator<String> pathIterator = atPath.iterator();
            prependAtPathComponent(pathBuilder, pathIterator.next(),
                    pathIterator, node);
            return new ImmutableNode.Builder(1).addChild(pathBuilder.create())
                    .create();
        }

        /**
         * Handles a single component of the at path. A corresponding node is
         * created and added to the hierarchical path to the original root node
         * of the configuration.
         *
         * @param builder the current node builder object
         * @param currentComponent the name of the current path component
         * @param components an iterator with all components of the at path
         * @param orgRoot the original root node of the wrapped configuration
         */
        private void prependAtPathComponent(final ImmutableNode.Builder builder,
                final String currentComponent, final Iterator<String> components,
                final ImmutableNode orgRoot)
        {
            builder.name(currentComponent);
            if (components.hasNext())
            {
                final ImmutableNode.Builder childBuilder =
                        new ImmutableNode.Builder();
                prependAtPathComponent(childBuilder, components.next(),
                        components, orgRoot);
                builder.addChild(childBuilder.create());
            }
            else
            {
                builder.addChildren(orgRoot.getChildren());
                builder.addAttributes(orgRoot.getAttributes());
                builder.value(orgRoot.getValue());
            }
        }

        /**
         * Obtains the root node of the wrapped configuration. If necessary, a
         * hierarchical representation of the configuration has to be created
         * first.
         *
         * @return the root node of the associated configuration
         */
        private ImmutableNode getRootNodeOfConfiguration()
        {
            getConfiguration().lock(LockMode.READ);
            try
            {
                final ImmutableNode root =
                        ConfigurationUtils
                                .convertToHierarchical(getConfiguration(),
                                        conversionExpressionEngine).getNodeModel()
                                .getInMemoryRepresentation();
                rootNode = root;
                return root;
            }
            finally
            {
                getConfiguration().unlock(LockMode.READ);
            }
        }

        /**
         * Splits the at path into its components.
         *
         * @param at the at string
         * @return a collection with the names of the single components
         */
        private Collection<String> parseAt(final String at)
        {
            if (at == null)
            {
                return null;
            }

            final Collection<String> result = new ArrayList<>();
            final DefaultConfigurationKey.KeyIterator it = new DefaultConfigurationKey(
                    AT_ENGINE, at).iterator();
            while (it.hasNext())
            {
                result.add(it.nextKey());
            }
            return result;
        }
    }
}
