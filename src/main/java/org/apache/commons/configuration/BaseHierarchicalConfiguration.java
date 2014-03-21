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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.ConfigurationNodeVisitorAdapter;
import org.apache.commons.configuration.tree.ImmutableNode;
import org.apache.commons.configuration.tree.InMemoryNodeModel;
import org.apache.commons.configuration.tree.NodeHandler;
import org.apache.commons.configuration.tree.NodeModel;
import org.apache.commons.configuration.tree.NodeSelector;
import org.apache.commons.configuration.tree.QueryResult;
import org.apache.commons.configuration.tree.TrackedNodeModel;

/**
 * <p>
 * A specialized hierarchical configuration implementation that is based on a
 * structure of {@link ImmutableNode} objects.
 * </p>
 *
 * @version $Id$
 */
public class BaseHierarchicalConfiguration extends AbstractHierarchicalConfiguration<ImmutableNode>
    implements Serializable, Cloneable
{
    /**
     * Constant for the subnode configuration modified event.
     * @since 1.5
     */
    public static final int EVENT_SUBNODE_CHANGED = 12;

    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 3373812230395363192L;

    /** A listener for reacting on changes caused by sub configurations. */
    private final ConfigurationListener changeListener;

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
    public BaseHierarchicalConfiguration(HierarchicalConfiguration<ImmutableNode> c)
    {
        this(createNodeModel(c));
    }

    /**
     * Creates a new instance of {@code BaseHierarchicalConfiguration} and
     * initializes it with the given {@code NodeModel}.
     *
     * @param model the {@code NodeModel}
     */
    protected BaseHierarchicalConfiguration(NodeModel<ImmutableNode> model)
    {
        super(model);
        changeListener = createChangeListener();
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
    public Configuration subset(String prefix)
    {
        beginRead(false);
        try
        {
            List<QueryResult<ImmutableNode>> results = fetchNodeList(prefix);
            if (results.isEmpty())
            {
                return new BaseHierarchicalConfiguration();
            }

            final BaseHierarchicalConfiguration parent = this;
            BaseHierarchicalConfiguration result =
                    new BaseHierarchicalConfiguration()
                    {
                        // Override interpolate to always interpolate on the parent
                        @Override
                        protected Object interpolate(Object value)
                        {
                            return parent.interpolate(value);
                        }

                        @Override
                        public ConfigurationInterpolator getInterpolator()
                        {
                            return parent.getInterpolator();
                        }
                    };
            result.setRootNode(createSubsetRootNode(results));

            if (result.isEmpty())
            {
                return new BaseHierarchicalConfiguration();
            }
            else
            {
                result.setSynchronizer(getSynchronizer());
                return result;
            }
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
            Collection<QueryResult<ImmutableNode>> results)
    {
        ImmutableNode.Builder builder = new ImmutableNode.Builder();
        Object value = null;
        int valueCount = 0;

        for (QueryResult<ImmutableNode> result : results)
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
    public HierarchicalConfiguration<ImmutableNode> configurationAt(String key,
            boolean supportUpdates)
    {
        BaseHierarchicalConfiguration sub =
                supportUpdates ? createConnectedSubConfiguration(key)
                        : createIndependentSubConfiguration(key);
        initSubConfiguration(sub);
        return sub;
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
     */
    protected NodeSelector getSubConfigurationNodeSelector(String key)
    {
        return new NodeSelector(key);
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
            String key)
    {
        InMemoryNodeModel myModel = getSubConfigurationParentModel();
        NodeSelector selector = getSubConfigurationNodeSelector(key);
        myModel.trackNode(selector, this);
        return createSubConfigurationForTrackedNode(selector, myModel);
    }

    /**
     * Creates a connected sub configuration based on a selector for a tracked
     * node.
     *
     * @param selector the {@code NodeSelector}
     * @param parentModel the parent node model
     * @return the newly created sub configuration
     */
    private SubnodeConfiguration createSubConfigurationForTrackedNode(
            NodeSelector selector, InMemoryNodeModel parentModel)
    {
        SubnodeConfiguration subConfig =
                new SubnodeConfiguration(this, new TrackedNodeModel(
                        parentModel, selector, true));
        subConfig.addConfigurationListener(changeListener);
        return subConfig;
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
            String key)
    {
        List<ImmutableNode> targetNodes = fetchFilteredNodeResults(key);
        if (targetNodes.size() != 1)
        {
            throw new ConfigurationRuntimeException(
                    "Passed in key must select exactly one node: " + key);
        }
        return new BaseHierarchicalConfiguration(new InMemoryNodeModel(
                targetNodes.get(0)));
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
            ImmutableNode node)
    {
        BaseHierarchicalConfiguration sub =
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
    private List<ImmutableNode> fetchFilteredNodeResults(String key)
    {
        NodeHandler<ImmutableNode> handler = getModel().getNodeHandler();
        return resolveNodeKey(handler.getRootNode(), key, handler);
    }

    /**
     * {@inheritDoc} This implementation creates a {@code SubnodeConfiguration}
     * by delegating to {@code configurationAt()}. Then an immutable wrapper
     * is created and returned.
     */
    public ImmutableHierarchicalConfiguration immutableConfigurationAt(
            String key, boolean supportUpdates)
    {
        return ConfigurationUtils.unmodifiableConfiguration(configurationAt(
                key, supportUpdates));
    }

    /**
     * {@inheritDoc} This is a short form for {@code configurationAt(key,
     * <b>false</b>)}.
     * @throws ConfigurationRuntimeException if the key does not select a single node
     */
    public HierarchicalConfiguration<ImmutableNode> configurationAt(String key)
    {
        return configurationAt(key, false);
    }

    /**
     * {@inheritDoc} This implementation creates a {@code SubnodeConfiguration}
     * by delegating to {@code configurationAt()}. Then an immutable wrapper
     * is created and returned.
     * @throws ConfigurationRuntimeException if the key does not select a single node
     */
    public ImmutableHierarchicalConfiguration immutableConfigurationAt(
            String key)
    {
        return ConfigurationUtils.unmodifiableConfiguration(configurationAt(
                key));
    }

    /**
     * {@inheritDoc} This implementation creates sub configurations in the same
     * way as described for {@link #configurationAt(String)}.
     */
    public List<HierarchicalConfiguration<ImmutableNode>> configurationsAt(
            String key)
    {
        List<ImmutableNode> nodes = fetchFilteredNodeResults(key);
        List<HierarchicalConfiguration<ImmutableNode>> results =
                new ArrayList<HierarchicalConfiguration<ImmutableNode>>(
                        nodes.size());
        for (ImmutableNode node : nodes)
        {
            BaseHierarchicalConfiguration sub =
                    createIndependentSubConfigurationForNode(node);
            results.add(sub);
        }

        return results;
    }

    /**
     * {@inheritDoc} This implementation creates tracked nodes for the specified
     * key. Then sub configurations for these nodes are created and returned.
     */
    public List<HierarchicalConfiguration<ImmutableNode>> configurationsAt(
            String key, boolean supportUpdates)
    {
        if (!supportUpdates)
        {
            return configurationsAt(key);
        }

        InMemoryNodeModel parentModel = getSubConfigurationParentModel();
        Collection<NodeSelector> selectors =
                parentModel.selectAndTrackNodes(key, this);
        List<HierarchicalConfiguration<ImmutableNode>> configs =
                new ArrayList<HierarchicalConfiguration<ImmutableNode>>(
                        selectors.size());
        for (NodeSelector selector : selectors)
        {
            configs.add(createSubConfigurationForTrackedNode(selector,
                    parentModel));
        }
        return configs;
    }

    /**
     * {@inheritDoc} This implementation first delegates to
     * {@code configurationsAt()} to create a list of
     * {@code SubnodeConfiguration} objects. Then for each element of this list
     * an unmodifiable wrapper is created.
     */
    public List<ImmutableHierarchicalConfiguration> immutableConfigurationsAt(
            String key)
    {
        return toImmutable(configurationsAt(key));
    }

    /**
     * {@inheritDoc} This implementation resolves the node(s) selected by the
     * given key. If not a single node is selected, an empty list is returned.
     * Otherwise, sub configurations for each child of the node are created.
     */
    public List<HierarchicalConfiguration<ImmutableNode>> childConfigurationsAt(
            String key)
    {
        List<ImmutableNode> nodes = fetchFilteredNodeResults(key);
        if (nodes.size() != 1)
        {
            return Collections.emptyList();
        }

        ImmutableNode parent = nodes.get(0);
        List<HierarchicalConfiguration<ImmutableNode>> subs =
                new ArrayList<HierarchicalConfiguration<ImmutableNode>>(parent
                        .getChildren().size());
        for (ImmutableNode node : parent.getChildren())
        {
            subs.add(createIndependentSubConfigurationForNode(node));
        }

        return subs;
    }

    public List<HierarchicalConfiguration<ImmutableNode>> childConfigurationsAt(String key, boolean supportUpdates) {
        return null;
    }

    /**
     * {@inheritDoc} This implementation first delegates to
     * {@code childConfigurationsAt()} to create a list of mutable child
     * configurations. Then a list with immutable wrapper configurations is
     * created.
     */
    public List<ImmutableHierarchicalConfiguration> immutableChildConfigurationsAt(
            String key)
    {
        return toImmutable(childConfigurationsAt(key));
    }

    /**
     * Creates a new {@code SubnodeConfiguration} for the specified node and
     * sets its construction key. If the key is not <b>null</b>, a
     * {@code SubnodeConfiguration} created this way will be aware of structural
     * changes of its parent.
     *
     * @param node the node, for which a {@code SubnodeConfiguration} is to be
     *        created
     * @param subnodeKey the key used to construct the configuration
     * @return the configuration for the given node
     * @since 1.5
     */
    protected SubnodeConfiguration createSubnodeConfiguration(
            ConfigurationNode node, String subnodeKey)
    {
        //TODO implementation
        return null; //new SubnodeConfiguration(this, node, subnodeKey);
    }

    /**
     * This method is always called when a subnode configuration created from
     * this configuration has been modified. This implementation transforms the
     * received event into an event of type {@code EVENT_SUBNODE_CHANGED}
     * and notifies the registered listeners.
     *
     * @param event the event describing the change
     * @since 1.5
     */
    protected void subnodeConfigurationChanged(ConfigurationEvent event)
    {
        fireEvent(EVENT_SUBNODE_CHANGED, null, event, event.isBeforeUpdate());
    }

    /**
     * Creates a new {@code SubnodeConfiguration} instance from this
     * configuration and initializes it. This method also takes care that data
     * structures are created to manage all {@code SubnodeConfiguration}
     * instances with support for updates. They are stored, so that they can be
     * triggered when this configuration is changed. <strong>Important
     * note:</strong> This method expects that a write lock is held on this
     * configuration!
     *
     * @param node the root node of the new {@code SubnodeConfiguration}
     * @param key the key to this node
     * @param supportUpdates a flag whether updates are supported
     * @return the newly created and initialized {@code SubnodeConfiguration}
     * @since 2.0
     */
    protected final SubnodeConfiguration createAndInitializeSubnodeConfiguration(
            ConfigurationNode node, String key, boolean supportUpdates)
    {
        //TODO adapt clients
        return null;
    }

    /**
     * Initializes properties of a sub configuration. A sub configuration
     * inherits some settings from its parent, e.g. the expression engine or the
     * synchronizer. The corresponding values are copied by this method.
     *
     * @param sub the sub configuration to be initialized
     */
    private void initSubConfiguration(BaseHierarchicalConfiguration sub)
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
    private ConfigurationListener createChangeListener()
    {
        return new ConfigurationListener()
        {
            public void configurationChanged(ConfigurationEvent event)
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
//        BaseHierarchicalConfiguration c = (BaseHierarchicalConfiguration) clone();
//        c.getRootNode().visit(new ConfigurationNodeVisitorAdapter()
//        {
//            @Override
//            public void visitAfterChildren(ConfigurationNode node)
//            {
//                node.setValue(interpolate(node.getValue()));
//            }
//        });
//        return c;
        //TODO implementation
        return null;
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
        return new InMemoryNodeModel(getRootNode());
    }

    /**
     * Creates a list with immutable configurations from the given input list.
     *
     * @param subs a list with mutable configurations
     * @return a list with corresponding immutable configurations
     */
    private static List<ImmutableHierarchicalConfiguration> toImmutable(
            List<? extends HierarchicalConfiguration> subs)
    {
        List<ImmutableHierarchicalConfiguration> res =
                new ArrayList<ImmutableHierarchicalConfiguration>(subs.size());
        for (HierarchicalConfiguration sub : subs)
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
            HierarchicalConfiguration<ImmutableNode> c)
    {
        ImmutableNode root = (c != null) ? c.getRootNode() : null;
        return new InMemoryNodeModel(root);
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
     *
     */
    protected abstract static class BuilderVisitor extends ConfigurationNodeVisitorAdapter
    {
        /**
         * Visits the specified node before its children have been traversed.
         *
         * @param node the node to visit
         */
        public void visitBeforeChildren(ConfigurationNode node)
        {
            Collection<ConfigurationNode> subNodes = new LinkedList<ConfigurationNode>(node.getChildren());
            subNodes.addAll(node.getAttributes());
            Iterator<ConfigurationNode> children = subNodes.iterator();
            ConfigurationNode sibling1 = null;
            ConfigurationNode nd = null;

            while (children.hasNext())
            {
                // find the next new node
                do
                {
                    sibling1 = nd;
                    nd = children.next();
                } while (nd.getReference() != null && children.hasNext());

                if (nd.getReference() == null)
                {
                    // find all following new nodes
                    List<ConfigurationNode> newNodes = new LinkedList<ConfigurationNode>();
                    newNodes.add(nd);
                    while (children.hasNext())
                    {
                        nd = children.next();
                        if (nd.getReference() == null)
                        {
                            newNodes.add(nd);
                        }
                        else
                        {
                            break;
                        }
                    }

                    // Insert all new nodes
                    ConfigurationNode sibling2 = (nd.getReference() == null) ? null : nd;
                    for (ConfigurationNode insertNode : newNodes)
                    {
                        if (insertNode.getReference() == null)
                        {
                            Object ref = insert(insertNode, node, sibling1, sibling2);
                            if (ref != null)
                            {
                                insertNode.setReference(ref);
                            }
                            sibling1 = insertNode;
                        }
                    }
                }
            }
        }

        /**
         * Inserts a new node into the structure constructed by this builder.
         * This method is called for each node that has been added to the
         * configuration tree after the configuration has been loaded from its
         * source. These new nodes have to be inserted into the original
         * structure. The passed in nodes define the position of the node to be
         * inserted: its parent and the siblings between to insert. The return
         * value is interpreted as the new reference of the affected
         * {@code Node} object; if it is not <b>null </b>, it is passed
         * to the node's {@code setReference()} method.
         *
         * @param newNode the node to be inserted
         * @param parent the parent node
         * @param sibling1 the sibling after which the node is to be inserted;
         * can be <b>null </b> if the new node is going to be the first child
         * node
         * @param sibling2 the sibling before which the node is to be inserted;
         * can be <b>null </b> if the new node is going to be the last child
         * node
         * @return the reference object for the node to be inserted
         */
        protected abstract Object insert(ConfigurationNode newNode,
                ConfigurationNode parent, ConfigurationNode sibling1,
                ConfigurationNode sibling2);
    }
}
