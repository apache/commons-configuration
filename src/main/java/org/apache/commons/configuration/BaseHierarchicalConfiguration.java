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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.ConfigurationNodeVisitorAdapter;
import org.apache.commons.configuration.tree.ImmutableNode;
import org.apache.commons.configuration.tree.InMemoryNodeModel;
import org.apache.commons.configuration.tree.NodeModel;
import org.apache.commons.configuration.tree.QueryResult;

/**
 * <p>
 * A specialized hierarchical configuration implementation that is based on a
 * structure of {@link ImmutableNode} objects.
 * </p>
 *
 * @version $Id$
 */
public class BaseHierarchicalConfiguration extends AbstractHierarchicalConfiguration<ImmutableNode>
    implements Serializable, Cloneable, Initializable
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

    /**
     * A map for managing the {@code SubnodeConfiguration} instances created
     * from this configuration.
     */
    private Map<SubnodeConfiguration, Object> subConfigs;

    /** A listener for reacting on changes to update sub configurations. */
    private ConfigurationListener changeListener;

    /**
     * Creates a new instance of {@code BaseHierarchicalConfiguration}.
     */
    public BaseHierarchicalConfiguration()
    {
        super(new InMemoryNodeModel());
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
        this();
        //TODO implementation
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    /**
     * Performs special initialization of this configuration. This
     * implementation ensures that internal data structures for managing
     * {@code SubnodeConfiguration} objects are initialized. If this is done
     * directly after the creation of an instance, this instance can be accessed
     * in a read-only manner without requiring a specific {@code Synchronizer}.
     */
    public void initialize()
    {
        ensureSubConfigManagementDataSetUp();
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
     * <p>
     * Returns a hierarchical subnode configuration object that wraps the
     * configuration node specified by the given key. This method provides an
     * easy means of accessing sub trees of a hierarchical configuration. In the
     * returned configuration the sub tree can directly be accessed, it becomes
     * the root node of this configuration. Because of this the passed in key
     * must select exactly one configuration node; otherwise an
     * {@code IllegalArgumentException} will be thrown.
     * </p>
     * <p>
     * The difference between this method and the
     * {@link #subset(String)} method is that
     * {@code subset()} supports arbitrary subsets of configuration nodes
     * while {@code configurationAt()} only returns a single sub tree.
     * Please refer to the documentation of the
     * {@code SubnodeConfiguration} class to obtain further information
     * about subnode configurations and when they should be used.
     * </p>
     * <p>
     * With the {@code supportUpdate} flag the behavior of the returned
     * {@code SubnodeConfiguration} regarding updates of its parent
     * configuration can be determined. A subnode configuration operates on the
     * same nodes as its parent, so changes at one configuration are normally
     * directly visible for the other configuration. There are however changes
     * of the parent configuration, which are not recognized by the subnode
     * configuration per default. An example for this is a reload operation (for
     * file-based configurations): Here the complete node set of the parent
     * configuration is replaced, but the subnode configuration still references
     * the old nodes. If such changes should be detected by the subnode
     * configuration, the {@code supportUpdates} flag must be set to
     * <b>true</b>. This causes the subnode configuration to reevaluate the key
     * used for its creation each time it is accessed. This guarantees that the
     * subnode configuration always stays in sync with its key, even if the
     * parent configuration's data significantly changes. If such a change
     * makes the key invalid - because it now no longer points to exactly one
     * node -, the subnode configuration is not reconstructed, but keeps its
     * old data. It is then quasi detached from its parent.
     * </p>
     *
     * @param key the key that selects the sub tree
     * @param supportUpdates a flag whether the returned subnode configuration
     * should be able to handle updates of its parent
     * @return a hierarchical configuration that contains this sub tree
     * @see SubnodeConfiguration
     * @since 1.5
     */
    public SubnodeConfiguration configurationAt(String key,
            boolean supportUpdates)
    {
        beginWrite(false);
        try
        {
//            List<ConfigurationNode> nodes = fetchNodeList(key);
//            if (nodes.size() != 1)
//            {
//                throw new IllegalArgumentException(
//                        "Passed in key must select exactly one node: " + key);
//            }
//            return createAndInitializeSubnodeConfiguration(nodes.get(0), key,
//                    supportUpdates);
            //TODO implementation
            return null;
        }
        finally
        {
            endWrite();
        }
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
     * Returns a hierarchical subnode configuration for the node specified by
     * the given key. This is a short form for {@code configurationAt(key,
     * <b>false</b>)}.
     *
     * @param key the key that selects the sub tree
     * @return a hierarchical configuration that contains this sub tree
     * @see SubnodeConfiguration
     * @since 1.3
     */
    public SubnodeConfiguration configurationAt(String key)
    {
        return configurationAt(key, false);
    }

    /**
     * {@inheritDoc} This implementation creates a {@code SubnodeConfiguration}
     * by delegating to {@code configurationAt()}. Then an immutable wrapper
     * is created and returned.
     */
    public ImmutableHierarchicalConfiguration immutableConfigurationAt(
            String key)
    {
        return ConfigurationUtils.unmodifiableConfiguration(configurationAt(
                key));
    }

    /**
     * Returns a list of sub configurations for all configuration nodes selected
     * by the given key. This method will evaluate the passed in key (using the
     * current {@code ExpressionEngine}) and then create a subnode
     * configuration for each returned node (like
     * {@link #configurationAt(String)}}). This is especially
     * useful when dealing with list-like structures. As an example consider the
     * configuration that contains data about database tables and their fields.
     * If you need access to all fields of a certain table, you can simply do
     *
     * <pre>
     * List fields = config.configurationsAt("tables.table(0).fields.field");
     * for(Iterator it = fields.iterator(); it.hasNext();)
     * {
     *     BaseHierarchicalConfiguration sub = (BaseHierarchicalConfiguration) it.next();
     *     // now the children and attributes of the field node can be
     *     // directly accessed
     *     String fieldName = sub.getString("name");
     *     String fieldType = sub.getString("type");
     *     ...
     * </pre>
     *
     * @param key the key for selecting the desired nodes
     * @return a list with hierarchical configuration objects; each
     * configuration represents one of the nodes selected by the passed in key
     * @since 1.3
     */
    public List<SubnodeConfiguration> configurationsAt(String key)
    {
        beginWrite(false);
        try
        {
//            List<ConfigurationNode> nodes = fetchNodeList(key);
//            List<SubnodeConfiguration> configs =
//                    new ArrayList<SubnodeConfiguration>(nodes.size());
//            for (ConfigurationNode node : nodes)
//            {
//                configs.add(createAndInitializeSubnodeConfiguration(node, null,
//                        false));
//            }
//            return configs;
            //TODO implementation
            return null;
        }
        finally
        {
            endWrite();
        }
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
    public List<SubnodeConfiguration> childConfigurationsAt(String key)
    {
        beginWrite(false);
        try
        {
//            List<ConfigurationNode> nodes = fetchNodeList(key);
//            if (nodes.size() != 1)
//            {
//                return Collections.emptyList();
//            }
//
//            ConfigurationNode parent = nodes.get(0);
//            List<SubnodeConfiguration> subs =
//                    new ArrayList<SubnodeConfiguration>(
//                            parent.getChildrenCount());
//            for (ConfigurationNode c : parent.getChildren())
//            {
//                subs.add(createAndInitializeSubnodeConfiguration(c, null, false));
//            }
//            return subs;
            //TODO implementation
            return null;
        }
        finally
        {
            endWrite();
        }
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
        return new SubnodeConfiguration(this, node, subnodeKey);
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
        String subnodeKey = supportUpdates ? key : null;
        SubnodeConfiguration sub = createSubnodeConfiguration(node, subnodeKey);

        ensureSubConfigManagementDataSetUp();
        sub.addConfigurationListener(changeListener);
        sub.initSubConfigManagementData(subConfigs, changeListener);
        sub.setSynchronizer(getSynchronizer());

        if (supportUpdates)
        {
            // store this configuration so it can later be validated
            subConfigs.put(sub, Boolean.TRUE);
        }
        return sub;
    }

    /**
     * Initializes the data related to the management of
     * {@code SubnodeConfiguration} instances. This method is called each time a
     * new {@code SubnodeConfiguration} was created. A configuration and its
     * {@code SubnodeConfiguration} instances operate on the same set of data.
     *
     * @param subMap the map with all {@code SubnodeConfiguration} instances
     * @param listener the listener for reacting on changes
     */
    void initSubConfigManagementData(Map<SubnodeConfiguration, Object> subMap,
            ConfigurationListener listener)
    {
        subConfigs = subMap;
        changeListener = listener;
    }

    /**
     * Ensures that internal data structures for managing associated
     * {@code SubnodeConfiguration} objects are initialized.
     */
    private void ensureSubConfigManagementDataSetUp()
    {
        if (changeListener == null)
        {
            setUpSubConfigManagementData();
        }
    }

    /**
     * Initializes internal data structures for managing associated
     * {@code SubnodeConfiguration} objects.
     */
    private void setUpSubConfigManagementData()
    {
        changeListener = createChangeListener();
        subConfigs = new WeakHashMap<SubnodeConfiguration, Object>();
        addConfigurationListener(changeListener);
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
                nodeStructureChanged(event);
            }
        };
    }

    /**
     * A change on the node structure of this configuration has been detected.
     * This can be caused either by an update of this configuration or by one if
     * its {@code SubnodeConfiguration} instances. This method calls
     * {@link #subnodeConfigurationChanged(ConfigurationEvent)} if necessary and
     * ensures that all {@code SubnodeConfiguration} instances are validated.
     * Note: when this method is called, a write lock is held on this
     * configuration.
     *
     * @param event the change event
     */
    private void nodeStructureChanged(ConfigurationEvent event)
    {
        if (this != event.getSource())
        {
            subnodeConfigurationChanged(event);
        }

        if (!event.isBeforeUpdate() && EVENT_SUBNODE_CHANGED != event.getType())
        {
            validSubnodeConfigurations(event);
        }
    }

    /**
     * Triggers validation on all {@code SubnodeConfiguration} instances created
     * by this configuration.
     *
     * @param event the change event
     */
    private void validSubnodeConfigurations(ConfigurationEvent event)
    {
        Set<SubnodeConfiguration> subs =
                new HashSet<SubnodeConfiguration>(subConfigs.keySet());
        for (SubnodeConfiguration sub : subs)
        {
            if (sub != event.getSource())
            {
                sub.validateRootNode();
            }
        }
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
