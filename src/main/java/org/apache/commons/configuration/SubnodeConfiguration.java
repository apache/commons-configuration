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
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.tree.ConfigurationNode;

/**
 * <p>
 * A specialized hierarchical configuration class that wraps a single node of
 * its parent configuration.
 * </p>
 * <p>
 * Configurations of this type are initialized with a parent configuration and a
 * configuration node of this configuration. This node becomes the root node of
 * the subnode configuration. All property accessor methods are evaluated
 * relative to this root node. A good use case for a
 * {@code SubnodeConfiguration} is when multiple properties from a
 * specific sub tree of the whole configuration need to be accessed. Then a
 * {@code SubnodeConfiguration} can be created with the parent node of
 * the affected sub tree as root node. This allows for simpler property keys and
 * is also more efficient.
 * </p>
 * <p>
 * A subnode configuration and its parent configuration operate on the same
 * hierarchy of configuration nodes. So if modifications are performed at the
 * subnode configuration, these changes are immediately visible in the parent
 * configuration. Analogously will updates of the parent configuration affect
 * the subnode configuration if the sub tree spanned by the subnode
 * configuration's root node is involved.
 * </p>
 * <p>
 * There are however changes at the parent configuration, which cause the
 * subnode configuration to become detached. An example for such a change is a
 * {@code clearTree()} operation, which replaces the sub tree to which this
 * configuration's root node belongs. Another example are list structures: a subnode
 * configuration can be created to point on the <em>i</em>th element of the
 * list. Now list elements can be added or removed, so that the list elements'
 * indices change. In such a scenario the subnode configuration would always
 * point to the same list element, regardless of its current index.
 * </p>
 * <p>
 * To solve these problems and make a subnode configuration aware of
 * such structural changes of its parent, it is possible to associate a
 * subnode configuration with a configuration key. This can be done by calling
 * the {@code setSubnodeKey()} method. If here a key is set, the subnode
 * configuration will evaluate it on each access, thus ensuring that it is
 * always in sync with its parent. In this mode the subnode configuration really
 * behaves like a live-view on its parent. The price for this is a decreased
 * performance because now additional evaluation has to be performed to keep
 * the root node up-to-date. So this mode should only be used if necessary; if for
 * instance a subnode configuration is only used for a temporary convenient
 * access to a complex configuration, there is no need to make it aware for
 * structural changes of its parent. If a subnode configuration is created
 * using the {@link BaseHierarchicalConfiguration#configurationAt(String, boolean)
 * configurationAt()} method of {@code BaseHierarchicalConfiguration}
 * (which should be the preferred way), with an additional boolean parameter it
 * can be specified whether the resulting subnode configuration should be
 * aware of structural changes or not. Then the configuration key will be
 * automatically set.
 * </p>
 * <p>
 * <em>Note:</em> At the moment support for creating a subnode configuration
 * that is aware of structural changes of its parent from another subnode
 * configuration (a "sub subnode configuration") is limited. This only works if
 * <ol><li>the subnode configuration that serves as the parent for the new
 * subnode configuration is itself associated with a configuration key and</li>
 * <li>the key passed in to create the new subnode configuration is not too
 * complex (if configuration keys are used that contain indices, a corresponding
 * key that is valid from the parent configuration's point of view cannot be
 * constructed).</li></ol>
 * </p>
 * <p>
 * When a subnode configuration is created, it inherits the settings of its
 * parent configuration, e.g. some flags like the
 * {@code throwExceptionOnMissing} flag or the settings for handling list
 * delimiters) or the expression engine. If these settings are changed later in
 * either the subnode or the parent configuration, the changes are not visible
 * for each other. So you could create a subnode configuration, and change its
 * expression engine without affecting the parent configuration.
 * </p>
 * <p>
 * Because the {@code SubnodeConfiguration} operates on the same nodes
 * structure as its parent it uses the same {@code Synchronizer} instance per
 * default. This means that locks held on one {@code SubnodeConfiguration}
 * also impact the parent configuration and all of its other {@code SubnodeConfiguration}
 * objects. You should not change this without a good reason! Otherwise, there
 * is the risk of data corruption when multiple threads access these
 * configuration concurrently.
 * </p>
 * <p>
 * From its purpose this class is quite similar to
 * {@link SubsetConfiguration}. The difference is that a subset
 * configuration of a hierarchical configuration may combine multiple
 * configuration nodes from different sub trees of the configuration, while all
 * nodes in a subnode configuration belong to the same sub tree. If an
 * application can live with this limitation, it is recommended to use this
 * class instead of {@code SubsetConfiguration} because creating a subset
 * configuration is more expensive than creating a subnode configuration.
 * </p>
 * <p>
 * It is strongly recommended to create {@code SubnodeConfiguration} instances
 * only through the {@code configurationAt()} methods of a hierarchical
 * configuration. These methods ensure that all necessary initializations are
 * done. Creating instances manually without doing proper initialization may
 * break some of the functionality provided by this class.
 * </p>
 *
 * @since 1.3
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 */
public class SubnodeConfiguration extends BaseHierarchicalConfiguration
{
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 3105734147019386480L;

    /** Stores the parent configuration. */
    private final BaseHierarchicalConfiguration parent;

    /** Stores the key that was used to construct this configuration.*/
    private String subnodeKey;

    /**
     * Creates a new instance of {@code SubnodeConfiguration} and initializes it
     * with the parent configuration and the new root node.
     *
     * @param parent the parent configuration
     * @param root the root node of this {@code SubnodeConfiguration}
     * @param subKey the key associated with this {@code SubnodeConfiguration}
     *        (can be <b>null</b>)
     */
    public SubnodeConfiguration(BaseHierarchicalConfiguration parent,
            ConfigurationNode root, String subKey)
    {
        super(root);
        if (parent == null)
        {
            throw new IllegalArgumentException(
                    "Parent configuration must not be null!");
        }
        if (root == null)
        {
            throw new IllegalArgumentException("Root node must not be null!");
        }

        this.parent = parent;
        subnodeKey = subKey;
        initFromParent(parent);
        initInterpolator();
    }

    /**
     * Returns the parent configuration of this subnode configuration.
     *
     * @return the parent configuration
     */
    public BaseHierarchicalConfiguration getParent()
    {
        return parent;
    }

    /**
     * Returns the key that was used to construct this configuration. If here a
     * non-<b>null</b> value is returned, the subnode configuration will
     * always check its parent for structural changes and reconstruct itself if
     * necessary.
     *
     * @return the key for selecting this configuration's root node
     * @since 1.5
     */
    public String getSubnodeKey()
    {
        beginRead(false);
        try
        {
            return subnodeKey;
        }
        finally
        {
            endRead();
        }
    }

    /**
     * Sets the key to the root node of this subnode configuration. If here a
     * key is set, the subnode configuration will behave like a live-view on its
     * parent for this key. See the class comment for more details.
     *
     * @param subnodeKey the key used to construct this configuration
     * @since 1.5
     */
    public void setSubnodeKey(String subnodeKey)
    {
        beginWrite(false);
        try
        {
            this.subnodeKey = subnodeKey;
        }
        finally
        {
            endWrite();
        }
    }

    /**
     * Clears this configuration and removes its root node from the parent
     * configuration. While a default {@link #clear()} operation just removes
     * all content from the root node, this method is more radical. It also
     * removes this configuration's root node from the node structure of its
     * parent. This means that even if later properties are added to this
     * {@code SubnodeConfiguration}, they will not be visible in the parent
     * configuration.
     *
     * @since 2.0
     */
    public void clearAndDetachFromParent()
    {
        beginWrite(false);
        try
        {
            clearInternal();
            subnodeKey = null; // always detach
            getParent().removeNode(getRootNode());
        }
        finally
        {
            endWrite();
        }
    }

    /**
     * Returns a hierarchical configuration object for the given sub node that
     * is aware of structural changes of its parent. Works like the method with
     * the same name, but also sets the subnode key for the new subnode
     * configuration, so it can check whether the parent has been changed. This
     * only works if this subnode configuration has itself a valid subnode key.
     * So if a subnode configuration that should be aware of structural changes
     * is created from an already existing subnode configuration, this subnode
     * configuration must also be aware of such changes.
     *
     * @param node the sub node, for which the configuration is to be created
     * @param subKey the construction key
     * @return a hierarchical configuration for this sub node
     * @since 1.5
     */
    @Override
    protected SubnodeConfiguration createSubnodeConfiguration(
            ConfigurationNode node, String subKey)
    {
        String key =
                (subKey != null && subnodeKey != null) ? constructSubKeyForSubnodeConfig(node)
                        : null;
        return new SubnodeConfiguration(getParent(), node, key);
    }

    /**
     * Creates a new node. This task is delegated to the parent.
     *
     * @param name the node's name
     * @return the new node
     */
    @Override
    protected ConfigurationNode createNode(String name)
    {
        return getParent().createNode(name);
    }

    /**
     * Initializes this subnode configuration from the given parent
     * configuration. This method is called by the constructor. It will copy
     * many settings from the parent.
     *
     * @param parentConfig the parent configuration
     */
    protected void initFromParent(BaseHierarchicalConfiguration parentConfig)
    {
        setExpressionEngine(parentConfig.getExpressionEngine());
        setListDelimiterHandler(parentConfig.getListDelimiterHandler());
        setThrowExceptionOnMissing(parentConfig.isThrowExceptionOnMissing());
    }

    /**
     * Validates this configuration's root node. This method checks whether the
     * key associated with this {@code SubnodeConfiguration} (if any) still
     * points to a valid node in the parent configuration. If not, the key is
     * cleared, and this configuration is now detached from its parent.
     */
    void validateRootNode()
    {
        if (subnodeKey != null)
        {
            try
            {
                List<ConfigurationNode> nodes = getParent().fetchNodeList(subnodeKey);
                if (nodes.size() != 1)
                {
                    // key is invalid, so detach this subnode configuration
                    subnodeKey = null;
                }
                else
                {
                    ConfigurationNode currentRoot = nodes.get(0);
                    if (currentRoot != super.getRootNode())
                    {
                        // the root node was changed due to a change of the
                        // parent
                        fireEvent(EVENT_SUBNODE_CHANGED, null, null, true);
                        setRootNode(currentRoot);
                        fireEvent(EVENT_SUBNODE_CHANGED, null, null, false);
                    }
                }
            }
            catch (Exception ex)
            {
                // Evaluation of the key caused an exception. Probably the
                // expression engine has changed on the parent. Detach this
                // configuration, there is not much we can do about this.
                subnodeKey = null;
            }
        }
    }

    /**
     * Initializes the {@code ConfigurationInterpolator} for this sub configuration.
     * This is a standard {@code ConfigurationInterpolator} which also references
     * the {@code ConfigurationInterpolator} of the parent configuration.
     */
    private void initInterpolator()
    {
        getInterpolator().setParentInterpolator(getParent().getInterpolator());
    }

    /**
     * Constructs the key for a {@code SubnodeConfiguration} for associating it
     * with a node in the parent configuration. This method creates a canonical
     * key based on the path from the given node to the root node.
     *
     * @param node the root node for the new {@code SubnodeConfiguration}
     * @return the key for this {@code SubnodeConfiguration}
     */
    private String constructSubKeyForSubnodeConfig(ConfigurationNode node)
    {
        List<ConfigurationNode> lstPathToRoot =
                new ArrayList<ConfigurationNode>();
        ConfigurationNode top = super.getRootNode();
        ConfigurationNode nd = node;
        while (nd != top)
        {
            lstPathToRoot.add(nd);
            nd = nd.getParentNode();
        }

        // construct the keys for the nodes on this path
        Collections.reverse(lstPathToRoot);
        String key = subnodeKey;
        for (ConfigurationNode pathNode : lstPathToRoot)
        {
            key = getParent().getExpressionEngine().nodeKey(pathNode, key);
        }
        return key;
    }
}
