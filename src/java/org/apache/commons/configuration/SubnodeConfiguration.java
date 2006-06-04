/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
    /** Stores the parent configuration. */
    private HierarchicalConfiguration parent;

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
     * Returns a hierarchical configuration object for the given sub node.
     * This implementation will ensure that the returned
     * <code>SubnodeConfiguration</code> object will have the same parent than
     * this object.
     *
     * @param node the sub node, for which the configuration is to be created
     * @return a hierarchical configuration for this sub node
     */
    protected HierarchicalConfiguration createSubnodeConfiguration(ConfigurationNode node)
    {
        return new SubnodeConfiguration(getParent(), node);
    }

    /**
     * Creates a new node. This task is delegated to the parent.
     *
     * @param name the node's name
     * @return the new node
     */
    protected Node createNode(String name)
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
     * Performs interpolation. This implementation will ask the parent
     * configuration to perform the interpolation so that variables can be
     * evaluated in the global context.
     *
     * @param value the value to be interpolated
     */
    protected Object interpolate(Object value)
    {
        return getParent().interpolate(value);
    }
}
