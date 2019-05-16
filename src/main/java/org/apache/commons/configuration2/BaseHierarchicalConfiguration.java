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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.tree.ConfigurationNodeVisitorAdapter;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.InMemoryNodeModel;
import org.apache.commons.configuration2.tree.InMemoryNodeModelSupport;
import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.configuration2.tree.NodeModel;
import org.apache.commons.configuration2.tree.NodeSelector;
import org.apache.commons.configuration2.tree.NodeTreeWalker;
import org.apache.commons.configuration2.tree.QueryResult;
import org.apache.commons.configuration2.tree.ReferenceNodeHandler;
import org.apache.commons.configuration2.tree.TrackedNodeModel;
import org.apache.commons.lang3.ObjectUtils;

/**
 * <p>
 * A specialized hierarchical configuration implementation that is based on a
 * structure of {@link ImmutableNode} objects.
 * </p>
 *
 */
public class BaseHierarchicalConfiguration extends AbstractHierarchicalConfiguration<ImmutableNode>
    implements InMemoryNodeModelSupport
{
    /** A listener for reacting on changes caused by sub configurations. */
    private final EventListener<ConfigurationEvent> changeListener;

    /**
     * Creates a new instance of {@code BaseHierarchicalConfiguration}.
     */
    public BaseHierarchicalConfiguration()
    {
        this((HierarchicalConfiguration<ImmutableNode>) null);
    }

    /**
     * Creates a new instance of {@code BaseHierarchicalConfiguration} and
     * copies all data contained in the specified configuration into the new
     * one.
     *
     * @param c the configuration that is to be copied (if <b>null</b>, this
     * constructor will behave like the standard constructor)
     * @since 1.4
     */
    public BaseHierarchicalConfiguration(final HierarchicalConfiguration<ImmutableNode> c)
    {
        this(createNodeModel(c));
    }

    /**
     * Creates a new instance of {@code BaseHierarchicalConfiguration} and
     * initializes it with the given {@code NodeModel}.
     *
     * @param model the {@code NodeModel}
     */
    protected BaseHierarchicalConfiguration(final NodeModel<ImmutableNode> model)
    {
        super(model);
        changeListener = createChangeListener();
    }

    /**
     * {@inheritDoc} This implementation returns the {@code InMemoryNodeModel}
     * used by this configuration.
     */
    @Override
    public InMemoryNodeModel getNodeModel()
    {
        return (InMemoryNodeModel) super.getNodeModel();
    }

    /**
     * Creates a new {@code Configuration} object containing all keys
     * that start with the specified prefix. This implementation will return a
     * {@code BaseHierarchicalConfiguration} object so that the structure of
     * the keys will be saved. The nodes selected by the prefix (it is possible
     * that multiple nodes are selected) are mapped to the root node of the
     * returned configuration, i.e. their children and attributes will become
     * children and attributes of the new root node. However, a value of the root
     * node is only set if exactly one of the selected nodes contain a value (if
     * multiple nodes have a value, there is simply no way to decide how these
     * values are merged together). Note that the returned
     * {@code Configuration} object is not connected to its source
     * configuration: updates on the source configuration are not reflected in
     * the subset and vice versa. The returned configuration uses the same
     * {@code Synchronizer} as this configuration.
     *
     * @param prefix the prefix of the keys for the subset
     * @return a new configuration object representing the selected subset
     */
    @Override
    public Configuration subset(final String prefix)
    {
        beginRead(false);
        try
        {
            final List<QueryResult<ImmutableNode>> results = fetchNodeList(prefix);
            if (results.isEmpty())
            {
                return new BaseHierarchicalConfiguration();
            }

            final BaseHierarchicalConfiguration parent = this;
            final BaseHierarchicalConfiguration result =
                    new BaseHierarchicalConfiguration()
                    {
                        // Override interpolate to always interpolate on the parent
                        @Override
                        protected Object interpolate(final Object value)
                        {
                            return parent.interpolate(value);
                        }

                        @Override
                        public ConfigurationInterpolator getInterpolator()
                        {
                            return parent.getInterpolator();
                        }
                    };
            result.getModel().setRootNode(createSubsetRootNode(results));

            if (result.isEmpty())
            {
                return new BaseHierarchicalConfiguration();
            }
            result.setSynchronizer(getSynchronizer());
            return result;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Creates a root node for a subset configuration based on the passed in
     * query results. This method creates a new root node and adds the children
     * and attributes of all result nodes to it. If only a single node value is
     * defined, it is assigned as value of the new root node.
     *
     * @param results the collection of query results
     * @return the root node for the subset configuration
     */
    private ImmutableNode createSubsetRootNode(
            final Collection<QueryResult<ImmutableNode>> results)
    {
        final ImmutableNode.Builder builder = new ImmutableNode.Builder();
        Object value = null;
        int valueCount = 0;

        for (final QueryResult<ImmutableNode> result : results)
        {
            if (result.isAttributeResult())
            {
                builder.addAttribute(result.getAttributeName(),
                        result.getAttributeValue(getModel().getNodeHandler()));
            }
            else
            {
                if (result.getNode().getValue() != null)
                {
                    value = result.getNode().getValue();
                    valueCount++;
                }
                builder.addChildren(result.getNode().getChildren());
                builder.addAttributes(result.getNode().getAttributes());
            }
        }

        if (valueCount == 1)
        {
            builder.value(value);
        }
        return builder.create();
    }

    /**
     * {@inheritDoc} The result of this implementation depends on the
     * {@code supportUpdates} flag: If it is <b>false</b>, a plain
     * {@code BaseHierarchicalConfiguration} is returned using the selected node
     * as root node. This is suitable for read-only access to properties.
     * Because the configuration returned in this case is not connected to the
     * parent configuration, updates on properties made by one configuration are
     * not reflected by the other one. A value of <b>true</b> for this parameter
     * causes a tracked node to be created, and result is a
     * {@link SubnodeConfiguration} based on this tracked node. This
     * configuration is really connected to its parent, so that updated
     * properties are visible on both.
     *
     * @see SubnodeConfiguration
     * @throws ConfigurationRuntimeException if the key does not select a single
     *         node
     */
    @Override
    public HierarchicalConfiguration<ImmutableNode> configurationAt(final String key,
            final boolean supportUpdates)
    {
        beginRead(false);
        try
        {
            return supportUpdates ? createConnectedSubConfiguration(key)
                    : createIndependentSubConfiguration(key);
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Returns the {@code InMemoryNodeModel} to be used as parent model for a
     * new sub configuration. This method is called whenever a sub configuration
     * is to be created. This base implementation returns the model of this
     * configuration. Sub classes with different requirements for the parent
     * models of sub configurations have to override it.
     *
     * @return the parent model for a new sub configuration
     */
    protected InMemoryNodeModel getSubConfigurationParentModel()
    {
        return (InMemoryNodeModel) getModel();
    }

    /**
     * Returns the {@code NodeSelector} to be used for a sub configuration based
     * on the passed in key. This method is called whenever a sub configuration
     * is to be created. This base implementation returns a new
     * {@code NodeSelector} initialized with the passed in key. Sub classes may
     * override this method if they have a different strategy for creating a
     * selector.
     *
     * @param key the key of the sub configuration
     * @return a {@code NodeSelector} for initializing a sub configuration
     * @since 2.0
     */
    protected NodeSelector getSubConfigurationNodeSelector(final String key)
    {
        return new NodeSelector(key);
    }

    /**
     * Creates a connected sub configuration based on a selector for a tracked
     * node.
     *
     * @param selector the {@code NodeSelector}
     * @param parentModelSupport the {@code InMemoryNodeModelSupport} object for
     *        the parent node model
     * @return the newly created sub configuration
     * @since 2.0
     */
    protected SubnodeConfiguration createSubConfigurationForTrackedNode(
            final NodeSelector selector, final InMemoryNodeModelSupport parentModelSupport)
    {
        final SubnodeConfiguration subConfig =
                new SubnodeConfiguration(this, new TrackedNodeModel(
                        parentModelSupport, selector, true));
        initSubConfigurationForThisParent(subConfig);
        return subConfig;
    }

    /**
     * Initializes a {@code SubnodeConfiguration} object. This method should be
     * called for each sub configuration created for this configuration. It
     * ensures that the sub configuration is correctly connected to its parent
     * instance and that update events are correctly propagated.
     *
     * @param subConfig the sub configuration to be initialized
     * @since 2.0
     */
    protected void initSubConfigurationForThisParent(final SubnodeConfiguration subConfig)
    {
        initSubConfiguration(subConfig);
        subConfig.addEventListener(ConfigurationEvent.ANY, changeListener);
    }

    /**
     * Creates a sub configuration from the specified key which is connected to
     * this configuration. This implementation creates a
     * {@link SubnodeConfiguration} with a tracked node identified by the passed
     * in key.
     *
     * @param key the key of the sub configuration
     * @return the new sub configuration
     */
    private BaseHierarchicalConfiguration createConnectedSubConfiguration(
            final String key)
    {
        final NodeSelector selector = getSubConfigurationNodeSelector(key);
        getSubConfigurationParentModel().trackNode(selector, this);
        return createSubConfigurationForTrackedNode(selector, this);
    }

    /**
     * Creates a list of connected sub configurations based on a passed in list
     * of node selectors.
     *
     * @param parentModelSupport the parent node model support object
     * @param selectors the list of {@code NodeSelector} objects
     * @return the list with sub configurations
     */
    private List<HierarchicalConfiguration<ImmutableNode>> createConnectedSubConfigurations(
            final InMemoryNodeModelSupport parentModelSupport,
            final Collection<NodeSelector> selectors)
    {
        final List<HierarchicalConfiguration<ImmutableNode>> configs =
                new ArrayList<>(
                        selectors.size());
        for (final NodeSelector selector : selectors)
        {
            configs.add(createSubConfigurationForTrackedNode(selector,
                    parentModelSupport));
        }
        return configs;
    }

    /**
     * Creates a sub configuration from the specified key which is independent
     * on this configuration. This means that the sub configuration operates on
     * a separate node model (although the nodes are initially shared).
     *
     * @param key the key of the sub configuration
     * @return the new sub configuration
     */
    private BaseHierarchicalConfiguration createIndependentSubConfiguration(
            final String key)
    {
        final List<ImmutableNode> targetNodes = fetchFilteredNodeResults(key);
        final int size = targetNodes.size();
        if (size != 1)
        {
            throw new ConfigurationRuntimeException(
                    "Passed in key must select exactly one node (found %,d): %s", size, key);
        }
        final BaseHierarchicalConfiguration sub =
                new BaseHierarchicalConfiguration(new InMemoryNodeModel(
                        targetNodes.get(0)));
        initSubConfiguration(sub);
        return sub;
    }

    /**
     * Returns an initialized sub configuration for this configuration that is
     * based on another {@code BaseHierarchicalConfiguration}. Thus, it is
     * independent from this configuration.
     *
     * @param node the root node for the sub configuration
     * @return the initialized sub configuration
     */
    private BaseHierarchicalConfiguration createIndependentSubConfigurationForNode(
            final ImmutableNode node)
    {
        final BaseHierarchicalConfiguration sub =
                new BaseHierarchicalConfiguration(new InMemoryNodeModel(node));
        initSubConfiguration(sub);
        return sub;
    }

    /**
     * Executes a query on the specified key and filters it for node results.
     *
     * @param key the key
     * @return the filtered list with result nodes
     */
    private List<ImmutableNode> fetchFilteredNodeResults(final String key)
    {
        final NodeHandler<ImmutableNode> handler = getModel().getNodeHandler();
        return resolveNodeKey(handler.getRootNode(), key, handler);
    }

    /**
     * {@inheritDoc} This implementation creates a {@code SubnodeConfiguration}
     * by delegating to {@code configurationAt()}. Then an immutable wrapper
     * is created and returned.
     */
    @Override
    public ImmutableHierarchicalConfiguration immutableConfigurationAt(
            final String key, final boolean supportUpdates)
    {
        return ConfigurationUtils.unmodifiableConfiguration(configurationAt(
                key, supportUpdates));
    }

    /**
     * {@inheritDoc} This is a short form for {@code configurationAt(key,
     * <b>false</b>)}.
     * @throws ConfigurationRuntimeException if the key does not select a single node
     */
    @Override
    public HierarchicalConfiguration<ImmutableNode> configurationAt(final String key)
    {
        return configurationAt(key, false);
    }

    /**
     * {@inheritDoc} This implementation creates a {@code SubnodeConfiguration}
     * by delegating to {@code configurationAt()}. Then an immutable wrapper
     * is created and returned.
     * @throws ConfigurationRuntimeException if the key does not select a single node
     */
    @Override
    public ImmutableHierarchicalConfiguration immutableConfigurationAt(
            final String key)
    {
        return ConfigurationUtils.unmodifiableConfiguration(configurationAt(
                key));
    }

    /**
     * {@inheritDoc} This implementation creates sub configurations in the same
     * way as described for {@link #configurationAt(String)}.
     */
    @Override
    public List<HierarchicalConfiguration<ImmutableNode>> configurationsAt(
            final String key)
    {
        List<ImmutableNode> nodes;
        beginRead(false);
        try
        {
            nodes = fetchFilteredNodeResults(key);
        }
        finally
        {
            endRead();
        }

        final List<HierarchicalConfiguration<ImmutableNode>> results =
                new ArrayList<>(
                        nodes.size());
        for (final ImmutableNode node : nodes)
        {
            final BaseHierarchicalConfiguration sub =
                    createIndependentSubConfigurationForNode(node);
            results.add(sub);
        }

        return results;
    }

    /**
     * {@inheritDoc} This implementation creates tracked nodes for the specified
     * key. Then sub configurations for these nodes are created and returned.
     */
    @Override
    public List<HierarchicalConfiguration<ImmutableNode>> configurationsAt(
            final String key, final boolean supportUpdates)
    {
        if (!supportUpdates)
        {
            return configurationsAt(key);
        }

        InMemoryNodeModel parentModel;
        beginRead(false);
        try
        {
            parentModel = getSubConfigurationParentModel();
        }
        finally
        {
            endRead();
        }

        final Collection<NodeSelector> selectors =
                parentModel.selectAndTrackNodes(key, this);
        return createConnectedSubConfigurations(this, selectors);
    }

    /**
     * {@inheritDoc} This implementation first delegates to
     * {@code configurationsAt()} to create a list of
     * {@code SubnodeConfiguration} objects. Then for each element of this list
     * an unmodifiable wrapper is created.
     */
    @Override
    public List<ImmutableHierarchicalConfiguration> immutableConfigurationsAt(
            final String key)
    {
        return toImmutable(configurationsAt(key));
    }

    /**
     * {@inheritDoc} This implementation resolves the node(s) selected by the
     * given key. If not a single node is selected, an empty list is returned.
     * Otherwise, sub configurations for each child of the node are created.
     */
    @Override
    public List<HierarchicalConfiguration<ImmutableNode>> childConfigurationsAt(
            final String key)
    {
        List<ImmutableNode> nodes;
        beginRead(false);
        try
        {
            nodes = fetchFilteredNodeResults(key);
        }
        finally
        {
            endRead();
        }

        if (nodes.size() != 1)
        {
            return Collections.emptyList();
        }

        final ImmutableNode parent = nodes.get(0);
        final List<HierarchicalConfiguration<ImmutableNode>> subs =
                new ArrayList<>(parent
                        .getChildren().size());
        for (final ImmutableNode node : parent.getChildren())
        {
            subs.add(createIndependentSubConfigurationForNode(node));
        }

        return subs;
    }

    /**
     * {@inheritDoc} This method works like
     * {@link #childConfigurationsAt(String)}; however, depending on the value
     * of the {@code supportUpdates} flag, connected sub configurations may be
     * created.
     */
    @Override
    public List<HierarchicalConfiguration<ImmutableNode>> childConfigurationsAt(
            final String key, final boolean supportUpdates)
    {
        if (!supportUpdates)
        {
            return childConfigurationsAt(key);
        }

        final InMemoryNodeModel parentModel = getSubConfigurationParentModel();
        return createConnectedSubConfigurations(this,
                parentModel.trackChildNodes(key, this));
    }

    /**
     * {@inheritDoc} This implementation first delegates to
     * {@code childConfigurationsAt()} to create a list of mutable child
     * configurations. Then a list with immutable wrapper configurations is
     * created.
     */
    @Override
    public List<ImmutableHierarchicalConfiguration> immutableChildConfigurationsAt(
            final String key)
    {
        return toImmutable(childConfigurationsAt(key));
    }

    /**
     * This method is always called when a subnode configuration created from
     * this configuration has been modified. This implementation transforms the
     * received event into an event of type {@code SUBNODE_CHANGED}
     * and notifies the registered listeners.
     *
     * @param event the event describing the change
     * @since 1.5
     */
    protected void subnodeConfigurationChanged(final ConfigurationEvent event)
    {
        fireEvent(ConfigurationEvent.SUBNODE_CHANGED, null, event, event.isBeforeUpdate());
    }

    /**
     * Initializes properties of a sub configuration. A sub configuration
     * inherits some settings from its parent, e.g. the expression engine or the
     * synchronizer. The corresponding values are copied by this method.
     *
     * @param sub the sub configuration to be initialized
     */
    private void initSubConfiguration(final BaseHierarchicalConfiguration sub)
    {
        sub.setSynchronizer(getSynchronizer());
        sub.setExpressionEngine(getExpressionEngine());
        sub.setListDelimiterHandler(getListDelimiterHandler());
        sub.setThrowExceptionOnMissing(isThrowExceptionOnMissing());
        sub.getInterpolator().setParentInterpolator(getInterpolator());
    }

    /**
     * Creates a listener which reacts on all changes on this configuration or
     * one of its {@code SubnodeConfiguration} instances. If such a change is
     * detected, some updates have to be performed.
     *
     * @return the newly created change listener
     */
    private EventListener<ConfigurationEvent> createChangeListener()
    {
        return new EventListener<ConfigurationEvent>()
        {
            @Override
            public void onEvent(final ConfigurationEvent event)
            {
                subnodeConfigurationChanged(event);
            }
        };
    }

    /**
     * Returns a configuration with the same content as this configuration, but
     * with all variables replaced by their actual values. This implementation
     * is specific for hierarchical configurations. It clones the current
     * configuration and runs a specialized visitor on the clone, which performs
     * interpolation on the single configuration nodes.
     *
     * @return a configuration with all variables interpolated
     * @since 1.5
     */
    @Override
    public Configuration interpolatedConfiguration()
    {
        final InterpolatedVisitor visitor = new InterpolatedVisitor();
        final NodeHandler<ImmutableNode> handler = getModel().getNodeHandler();
        NodeTreeWalker.INSTANCE
                .walkDFS(handler.getRootNode(), visitor, handler);

        final BaseHierarchicalConfiguration c =
                (BaseHierarchicalConfiguration) clone();
        c.getNodeModel().setRootNode(visitor.getInterpolatedRoot());
        return c;
    }

    /**
     * {@inheritDoc} This implementation creates a new instance of
     * {@link InMemoryNodeModel}, initialized with this configuration's root
     * node. This has the effect that although the same nodes are used, the
     * original and copied configurations are independent on each other.
     */
    @Override
    protected NodeModel<ImmutableNode> cloneNodeModel()
    {
        return new InMemoryNodeModel(getModel().getNodeHandler().getRootNode());
    }

    /**
     * Creates a list with immutable configurations from the given input list.
     *
     * @param subs a list with mutable configurations
     * @return a list with corresponding immutable configurations
     */
    private static List<ImmutableHierarchicalConfiguration> toImmutable(
            final List<? extends HierarchicalConfiguration<?>> subs)
    {
        final List<ImmutableHierarchicalConfiguration> res =
                new ArrayList<>(subs.size());
        for (final HierarchicalConfiguration<?> sub : subs)
        {
            res.add(ConfigurationUtils.unmodifiableConfiguration(sub));
        }
        return res;
    }

    /**
     * Creates the {@code NodeModel} for this configuration based on a passed in
     * source configuration. This implementation creates an
     * {@link InMemoryNodeModel}. If the passed in source configuration is
     * defined, its root node also becomes the root node of this configuration.
     * Otherwise, a new, empty root node is used.
     *
     * @param c the configuration that is to be copied
     * @return the {@code NodeModel} for the new configuration
     */
    private static NodeModel<ImmutableNode> createNodeModel(
            final HierarchicalConfiguration<ImmutableNode> c)
    {
        final ImmutableNode root = (c != null) ? obtainRootNode(c) : null;
        return new InMemoryNodeModel(root);
    }

    /**
     * Obtains the root node from a configuration whose data is to be copied. It
     * has to be ensured that the synchronizer is called correctly.
     *
     * @param c the configuration that is to be copied
     * @return the root node of this configuration
     */
    private static ImmutableNode obtainRootNode(
            final HierarchicalConfiguration<ImmutableNode> c)
    {
        return c.getNodeModel().getNodeHandler().getRootNode();
    }

    /**
     * A specialized visitor base class that can be used for storing the tree of
     * configuration nodes. The basic idea is that each node can be associated
     * with a reference object. This reference object has a concrete meaning in
     * a derived class, e.g. an entry in a JNDI context or an XML element. When
     * the configuration tree is set up, the {@code load()} method is
     * responsible for setting the reference objects. When the configuration
     * tree is later modified, new nodes do not have a defined reference object.
     * This visitor class processes all nodes and finds the ones without a
     * defined reference object. For those nodes the {@code insert()}
     * method is called, which must be defined in concrete sub classes. This
     * method can perform all steps to integrate the new node into the original
     * structure.
     */
    protected abstract static class BuilderVisitor extends
            ConfigurationNodeVisitorAdapter<ImmutableNode>
    {
        @Override
        public void visitBeforeChildren(final ImmutableNode node, final NodeHandler<ImmutableNode> handler)
        {
            final ReferenceNodeHandler refHandler = (ReferenceNodeHandler) handler;
            updateNode(node, refHandler);
            insertNewChildNodes(node, refHandler);
        }

        /**
         * Inserts a new node into the structure constructed by this builder.
         * This method is called for each node that has been added to the
         * configuration tree after the configuration has been loaded from its
         * source. These new nodes have to be inserted into the original
         * structure. The passed in nodes define the position of the node to be
         * inserted: its parent and the siblings between to insert.
         *
         * @param newNode the node to be inserted
         * @param parent the parent node
         * @param sibling1 the sibling after which the node is to be inserted;
         *        can be <b>null</b> if the new node is going to be the first
         *        child node
         * @param sibling2 the sibling before which the node is to be inserted;
         *        can be <b>null</b> if the new node is going to be the last
         *        child node
         * @param refHandler the {@code ReferenceNodeHandler}
         */
        protected abstract void insert(ImmutableNode newNode,
                ImmutableNode parent, ImmutableNode sibling1,
                ImmutableNode sibling2, ReferenceNodeHandler refHandler);

        /**
         * Updates a node that already existed in the original hierarchy. This
         * method is called for each node that has an assigned reference object.
         * A concrete implementation should update the reference according to
         * the node's current value.
         *
         * @param node the current node to be processed
         * @param reference the reference object for this node
         * @param refHandler the {@code ReferenceNodeHandler}
         */
        protected abstract void update(ImmutableNode node, Object reference,
                ReferenceNodeHandler refHandler);

        /**
         * Updates the value of a node. If this node is associated with a
         * reference object, the {@code update()} method is called.
         *
         * @param node the current node to be processed
         * @param refHandler the {@code ReferenceNodeHandler}
         */
        private void updateNode(final ImmutableNode node,
                final ReferenceNodeHandler refHandler)
        {
            final Object reference = refHandler.getReference(node);
            if (reference != null)
            {
                update(node, reference, refHandler);
            }
        }

        /**
         * Inserts new children that have been added to the specified node.
         *
         * @param node the current node to be processed
         * @param refHandler the {@code ReferenceNodeHandler}
         */
        private void insertNewChildNodes(final ImmutableNode node,
                final ReferenceNodeHandler refHandler)
        {
            final Collection<ImmutableNode> subNodes =
                    new LinkedList<>(refHandler.getChildren(node));
            final Iterator<ImmutableNode> children = subNodes.iterator();
            ImmutableNode sibling1;
            ImmutableNode nd = null;

            while (children.hasNext())
            {
                // find the next new node
                do
                {
                    sibling1 = nd;
                    nd = children.next();
                } while (refHandler.getReference(nd) != null
                        && children.hasNext());

                if (refHandler.getReference(nd) == null)
                {
                    // find all following new nodes
                    final List<ImmutableNode> newNodes =
                            new LinkedList<>();
                    newNodes.add(nd);
                    while (children.hasNext())
                    {
                        nd = children.next();
                        if (refHandler.getReference(nd) == null)
                        {
                            newNodes.add(nd);
                        }
                        else
                        {
                            break;
                        }
                    }

                    // Insert all new nodes
                    final ImmutableNode sibling2 =
                            (refHandler.getReference(nd) == null) ? null : nd;
                    for (final ImmutableNode insertNode : newNodes)
                    {
                        if (refHandler.getReference(insertNode) == null)
                        {
                            insert(insertNode, node, sibling1, sibling2,
                                    refHandler);
                            sibling1 = insertNode;
                        }
                    }
                }
            }
        }
    }

    /**
     * A specialized visitor implementation which constructs the root node of a
     * configuration with all variables replaced by their interpolated values.
     */
    private class InterpolatedVisitor extends
            ConfigurationNodeVisitorAdapter<ImmutableNode>
    {
        /** A stack for managing node builder instances. */
        private final List<ImmutableNode.Builder> builderStack;

        /** The resulting root node. */
        private ImmutableNode interpolatedRoot;

        /**
         * Creates a new instance of {@code InterpolatedVisitor}.
         */
        public InterpolatedVisitor()
        {
            builderStack = new LinkedList<>();
        }

        /**
         * Returns the result of this builder: the root node of the interpolated
         * nodes hierarchy.
         *
         * @return the resulting root node
         */
        public ImmutableNode getInterpolatedRoot()
        {
            return interpolatedRoot;
        }

        @Override
        public void visitBeforeChildren(final ImmutableNode node,
                final NodeHandler<ImmutableNode> handler)
        {
            if (isLeafNode(node, handler))
            {
                handleLeafNode(node, handler);
            }
            else
            {
                final ImmutableNode.Builder builder =
                        new ImmutableNode.Builder(handler.getChildrenCount(
                                node, null))
                                .name(handler.nodeName(node))
                                .value(interpolate(handler.getValue(node)))
                                .addAttributes(
                                        interpolateAttributes(node, handler));
                push(builder);
            }
        }

        @Override
        public void visitAfterChildren(final ImmutableNode node,
                final NodeHandler<ImmutableNode> handler)
        {
            if (!isLeafNode(node, handler))
            {
                final ImmutableNode newNode = pop().create();
                storeInterpolatedNode(newNode);
            }
        }

        /**
         * Pushes a new builder on the stack.
         *
         * @param builder the builder
         */
        private void push(final ImmutableNode.Builder builder)
        {
            builderStack.add(0, builder);
        }

        /**
         * Pops the top-level element from the stack.
         *
         * @return the element popped from the stack
         */
        private ImmutableNode.Builder pop()
        {
            return builderStack.remove(0);
        }

        /**
         * Returns the top-level element from the stack without removing it.
         *
         * @return the top-level element from the stack
         */
        private ImmutableNode.Builder peek()
        {
            return builderStack.get(0);
        }

        /**
         * Returns a flag whether the given node is a leaf. This is the case if
         * it does not have children.
         *
         * @param node the node in question
         * @param handler the {@code NodeHandler}
         * @return a flag whether this is a leaf node
         */
        private boolean isLeafNode(final ImmutableNode node,
                final NodeHandler<ImmutableNode> handler)
        {
            return handler.getChildren(node).isEmpty();
        }

        /**
         * Handles interpolation for a node with no children. If interpolation
         * does not change this node, it is copied as is to the resulting
         * structure. Otherwise, a new node is created with the interpolated
         * values.
         *
         * @param node the current node to be processed
         * @param handler the {@code NodeHandler}
         */
        private void handleLeafNode(final ImmutableNode node,
                final NodeHandler<ImmutableNode> handler)
        {
            final Object value = interpolate(node.getValue());
            final Map<String, Object> interpolatedAttributes =
                    new HashMap<>();
            final boolean attributeChanged =
                    interpolateAttributes(node, handler, interpolatedAttributes);
            final ImmutableNode newNode =
                    (valueChanged(value, handler.getValue(node)) || attributeChanged) ? new ImmutableNode.Builder()
                            .name(handler.nodeName(node)).value(value)
                            .addAttributes(interpolatedAttributes).create()
                            : node;
            storeInterpolatedNode(newNode);
        }

        /**
         * Stores a processed node. Per default, the node is added to the
         * current builder on the stack. If no such builder exists, this is the
         * result node.
         *
         * @param node the node to be stored
         */
        private void storeInterpolatedNode(final ImmutableNode node)
        {
            if (builderStack.isEmpty())
            {
                interpolatedRoot = node;
            }
            else
            {
                peek().addChild(node);
            }
        }

        /**
         * Populates a map with interpolated attributes of the passed in node.
         *
         * @param node the current node to be processed
         * @param handler the {@code NodeHandler}
         * @param interpolatedAttributes a map for storing the results
         * @return a flag whether an attribute value was changed by
         *         interpolation
         */
        private boolean interpolateAttributes(final ImmutableNode node,
                final NodeHandler<ImmutableNode> handler,
                final Map<String, Object> interpolatedAttributes)
        {
            boolean attributeChanged = false;
            for (final String attr : handler.getAttributes(node))
            {
                final Object attrValue =
                        interpolate(handler.getAttributeValue(node, attr));
                if (valueChanged(attrValue,
                        handler.getAttributeValue(node, attr)))
                {
                    attributeChanged = true;
                }
                interpolatedAttributes.put(attr, attrValue);
            }
            return attributeChanged;
        }

        /**
         * Returns a map with interpolated attributes of the passed in node.
         *
         * @param node the current node to be processed
         * @param handler the {@code NodeHandler}
         * @return the map with interpolated attributes
         */
        private Map<String, Object> interpolateAttributes(final ImmutableNode node,
                final NodeHandler<ImmutableNode> handler)
        {
            final Map<String, Object> attributes = new HashMap<>();
            interpolateAttributes(node, handler, attributes);
            return attributes;
        }

        /**
         * Tests whether a value is changed because of interpolation.
         *
         * @param interpolatedValue the interpolated value
         * @param value the original value
         * @return a flag whether the value was changed
         */
        private boolean valueChanged(final Object interpolatedValue, final Object value)
        {
            return ObjectUtils.notEqual(interpolatedValue, value);
        }
    }
}
