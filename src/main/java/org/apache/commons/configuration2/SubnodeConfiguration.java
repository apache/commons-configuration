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
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.tree.ConfigurationNode;

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
 * <code>SubnodeConfiguration</code> is when multiple properties from a
 * specific sub tree of the whole configuration need to be accessed. Then a
 * <code>SubnodeConfiguration</code> can be created with the parent node of
 * the affected sub tree as root node. This allows for simpler property keys and
 * is also more efficient.
 * </p>
 * <p>
 * A subnode configuration and its parent configuration operate on the same
 * hierarchy of configuration nodes. So if modifications are performed at the
 * subnode configuration, these changes are immideately visible in the parent
 * configuration. Analogously will updates of the parent configuration affect
 * the subnode configuration if the sub tree spanned by the subnode
 * configuration's root node is involved.
 * </p>
 * <p>
 * There are however changes at the parent configuration, which cause the
 * subnode configuration to become detached. An example for such a change is a
 * reload operation of a file-based configuration, which replaces all nodes of
 * the parent configuration. The subnode configuration per default still
 * references the old nodes. Another example are list structures: a subnode
 * configuration can be created to point on the <em>i</em>th element of the
 * list. Now list elements can be added or removed, so that the list elements'
 * indices change. In such a scenario the subnode configuration would always
 * point to the same list element, regardless of its current index.
 * </p>
 * <p>
 * To solve these problems and make a subnode configuration aware of
 * such structural changes of its parent, it is possible to associate a
 * subnode configuration with a configuration key. This can be done by calling
 * the <code>setSubnodeKey()</code> method. If here a key is set, the subnode
 * configuration will evaluate it on each access, thus ensuring that it is
 * always in sync with its parent. In this mode the subnode configuration really
 * behaves like a live-view on its parent. The price for this is a decreased
 * performance because now an additional evaluation has to be performed on each
 * property access. So this mode should only be used if necessary; if for
 * instance a subnode configuration is only used for a temporary convenient
 * access to a complex configuration, there is no need to make it aware for
 * structural changes of its parent. If a subnode configuration is created
 * using the <code>{@link HierarchicalConfiguration#configurationAt(String, boolean)
 * configurationAt()}</code> method of <code>HierarchicalConfiguration</code>
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
 * <code>throwExceptionOnMissing</code> flag or the settings for handling list
 * delimiters) or the expression engine. If these settings are changed later in
 * either the subnode or the parent configuration, the changes are not visible
 * for each other. So you could create a subnode configuration, change its
 * expression engine without affecting the parent configuration.
 * </p>
 * <p>
 * From its purpose this class is quite similar to
 * <code>{@link SubsetConfiguration}</code>. The difference is that a subset
 * configuration of a hierarchical configuration may combine multiple
 * configuration nodes from different sub trees of the configuration, while all
 * nodes in a subnode configuration belong to the same sub tree. If an
 * application can live with this limitation, it is recommended to use this
 * class instead of <code>SubsetConfiguration</code> because creating a subset
 * configuration is more expensive than creating a subnode configuration.
 * </p>
 *
 * @since 1.3
 * @author Oliver Heger
 * @version $Id$
 */
public class SubnodeConfiguration extends HierarchicalConfiguration
{
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 3105734147019386480L;

    /** Stores the parent configuration. */
    private HierarchicalConfiguration parent;

    /** Stores the key that was used to construct this configuration.*/
    private String subnodeKey;

    /**
     * Creates a new instance of <code>SubnodeConfiguration</code> and
     * initializes it with the parent configuration and the new root node.
     *
     * @param parent the parent configuration
     * @param root the root node of this subnode configuration
     */
    public SubnodeConfiguration(HierarchicalConfiguration parent, ConfigurationNode root)
    {
        if (parent == null)
        {
            throw new IllegalArgumentException(
                    "Parent configuration must not be null!");
        }
        if (root == null)
        {
            throw new IllegalArgumentException("Root node must not be null!");
        }

        setRootNode(root);
        this.parent = parent;
        initFromParent(parent);
    }

    /**
     * Returns the parent configuration of this subnode configuration.
     *
     * @return the parent configuration
     */
    public HierarchicalConfiguration getParent()
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
        return subnodeKey;
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
        this.subnodeKey = subnodeKey;
    }

    /**
     * Returns the root node for this configuration. If a subnode key is set,
     * this implementation re-evaluates this key to find out if this subnode
     * configuration needs to be reconstructed. This ensures that the subnode
     * configuration is always synchronized with its parent configuration.
     *
     * @return the root node of this configuration
     * @since 1.5
     * @see #setSubnodeKey(String)
     */
    public ConfigurationNode getRootNode()
    {
        if (getSubnodeKey() != null)
        {
            try
            {
                List<ConfigurationNode> nodes = getParent().fetchNodeList(getSubnodeKey());
                if (nodes.size() != 1)
                {
                    // key is invalid, so detach this subnode configuration
                    setSubnodeKey(null);
                }
                else
                {
                    ConfigurationNode currentRoot = nodes.get(0);
                    if (currentRoot != super.getRootNode())
                    {
                        // the root node was changed due to a change of the parent
                        setRootNode(currentRoot);
                    }
                    return currentRoot;
                }
            }
            catch (Exception ex)
            {
                // Evaluation of the key caused an exception. Probably the
                // expression engine has changed on the parent. Detach this
                // configuration, there is not much we can do about this.
                setSubnodeKey(null);
            }
        }

        return super.getRootNode(); // use stored root node
    }

    /**
     * Returns a hierarchical configuration object for the given sub node.
     * This implementation will ensure that the returned
     * <code>SubnodeConfiguration</code> object will have the same parent than
     * this object.
     *
     * @param node the sub node, for which the configuration is to be created
     * @return a hierarchical configuration for this sub node
     */
    protected SubnodeConfiguration createSubnodeConfiguration(ConfigurationNode node)
    {
        SubnodeConfiguration result = new SubnodeConfiguration(getParent(), node);
        getParent().registerSubnodeConfiguration(result);
        return result;
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
     * @param subnodeKey the construction key
     * @return a hierarchical configuration for this sub node
     * @since 1.5
     */
    protected SubnodeConfiguration createSubnodeConfiguration(ConfigurationNode node, String subnodeKey)
    {
        SubnodeConfiguration result = createSubnodeConfiguration(node);

        if (getSubnodeKey() != null)
        {
            // construct the correct subnode key
            // determine path to root node
            List<ConfigurationNode> lstPathToRoot = new ArrayList<ConfigurationNode>();
            ConfigurationNode root = super.getRootNode();
            ConfigurationNode nd = node;
            while (nd != root)
            {
                lstPathToRoot.add(nd);
                nd = nd.getParentNode();
            }

            // construct the keys for the nodes on this path
            Collections.reverse(lstPathToRoot);
            String key = getSubnodeKey();
            for (ConfigurationNode pathElement : lstPathToRoot)
            {
                key = getParent().getExpressionEngine().nodeKey(pathElement, key);
            }
            result.setSubnodeKey(key);
        }

        return result;
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
    protected void initFromParent(HierarchicalConfiguration parentConfig)
    {
        setExpressionEngine(parentConfig.getExpressionEngine());
        setListDelimiter(parentConfig.getListDelimiter());
        setDelimiterParsingDisabled(parentConfig.isDelimiterParsingDisabled());
        setThrowExceptionOnMissing(parentConfig.isThrowExceptionOnMissing());
    }

    /**
     * Creates a ConfigurationInterpolator with a chain to the parent's
     * interpolator. 
     *
     * @return the new interpolator
     */
    @Override
    protected ConfigurationInterpolator createInterpolator() {
        ConfigurationInterpolator interpolator = super.createInterpolator();
        interpolator.setParentInterpolator(getParent().getInterpolator());
        return interpolator;
    }
    
}
