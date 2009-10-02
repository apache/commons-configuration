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
package org.apache.commons.configuration2.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.expr.NodeList;

/**
 * <p>
 * A specialized hierarchical configuration class that wraps a single node of
 * its parent configuration.
 * </p>
 * <p>
 * Configurations of this type are initialized with a parent configuration and a
 * configuration node of this configuration. This node becomes the root node of
 * the sub configuration. All property accessor methods are evaluated
 * relative to this root node. A good use case for a
 * <code>SubConfiguration</code> is when multiple properties from a
 * specific sub tree of the whole configuration need to be accessed. Then a
 * <code>SubConfiguration</code> can be created with the parent node of
 * the affected sub tree as root node. This allows for simpler property keys and
 * is also more efficient.
 * </p>
 * <p>
 * A sub configuration and its parent configuration operate on the same
 * hierarchy of configuration nodes. So if modifications are performed at the
 * sub configuration, these changes are immediately visible in the parent
 * configuration. Analogously will updates of the parent configuration affect
 * the sub configuration if the sub tree spanned by the sub
 * configuration's root node is involved.
 * </p>
 * <p>
 * There are however changes at the parent configuration, which cause the
 * sub configuration to become detached. An example for such a change is a
 * reload operation of a file-based configuration, which replaces all nodes of
 * the parent configuration. The sub configuration per default still
 * references the old nodes. Another example are list structures: a sub
 * configuration can be created to point on the <em>i</em>th element of the
 * list. Now list elements can be added or removed, so that the list elements'
 * indices change. In such a scenario the sub configuration would always
 * point to the same list element, regardless of its current index.
 * </p>
 * <p>
 * To solve these problems and make a sub configuration aware of
 * such structural changes of its parent, it is possible to associate a
 * sub configuration with a configuration key. This can be done by calling
 * the <code>setSubnodeKey()</code> method. If here a key is set, the sub
 * configuration will evaluate it on each access, thus ensuring that it is
 * always in sync with its parent. In this mode the sub configuration really
 * behaves like a live-view on its parent. The price for this is a decreased
 * performance because now an additional evaluation has to be performed on each
 * property access. So this mode should only be used if necessary; if for
 * instance a sub configuration is only used for a temporary convenient
 * access to a complex configuration, there is no need to make it aware for
 * structural changes of its parent. If a sub configuration is created
 * using the <code>{@link ConfigurationImpl#configurationAt(String, boolean)
 * configurationAt()}</code> method of <code>AbstractHierarchicalConfiguration</code>
 * (which should be the preferred way), with an additional boolean parameter it
 * can be specified whether the resulting sub configuration should be
 * aware of structural changes or not. Then the configuration key will be
 * automatically set.
 * </p>
 * <p>
 * <em>Note:</em> At the moment support for creating a sub configuration
 * that is aware of structural changes of its parent from another sub
 * configuration (a "sub sub configuration") is limited. This only works if
 * <ol><li>the sub configuration that serves as the parent for the new
 * sub configuration is itself associated with a configuration key and</li>
 * <li>the key passed in to create the new sub configuration is not too
 * complex (if configuration keys are used that contain indices, a corresponding
 * key that is valid from the parent configuration's point of view cannot be
 * constructed).</li></ol>
 * </p>
 * <p>
 * When a sub configuration is created, it inherits the settings of its
 * parent configuration, e.g. some flags like the
 * <code>throwExceptionOnMissing</code> flag or the settings for handling list
 * delimiters) or the expression engine. If these settings are changed later in
 * either the sub or the parent configuration, the changes are not visible
 * for each other. So you could create a sub configuration and then change its
 * expression engine without affecting the parent configuration.
 * </p>
 *
 * @param <T> the type of configuration nodes used by this configuration
 * @since 2.0
 * @author Commons Configuration team
 * @version $Id$
 */
public class SubConfiguration<T> extends ConfigurationImpl<T> implements
        HierarchicalConfigurationSource<T>
{
    /** Stores the parent configuration. */
    private ConfigurationImpl<T> parent;

    /** Stores the root node of this sub configuration.*/
    private T rootNode;

    /** Stores the key that was used to construct this configuration.*/
    private String subnodeKey;

    /**
     * Creates a new instance of <code>SubnodeConfiguration</code> and
     * initializes it with the parent configuration and the new root node.
     *
     * @param parent the parent configuration
     * @param root the root node of this sub configuration
     * @throws IllegalArgumentException if the parent or the root node are <b>null</b>
     */
    public SubConfiguration(ConfigurationImpl<T> parent, T root)
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

        rootNode = root;
        this.parent = parent;
        initFromParent(parent);
    }

    /**
     * Returns the parent configuration of this sub configuration.
     *
     * @return the parent configuration
     */
    public ConfigurationImpl<T> getParent()
    {
        return parent;
    }

    /**
     * Returns the key that was used to construct this configuration. If here a
     * non-<b>null</b> value is returned, the sub configuration will
     * always check its parent for structural changes and reconstruct itself if
     * necessary.
     *
     * @return the key for selecting this configuration's root node
     */
    public String getSubnodeKey()
    {
        return subnodeKey;
    }

    /**
     * Sets the key to the root node of this sub configuration. If here a
     * key is set, the sub configuration will behave like a live-view on its
     * parent for this key. See the class comment for more details.
     *
     * @param subnodeKey the key used to construct this configuration
     */
    public void setSubnodeKey(String subnodeKey)
    {
        this.subnodeKey = subnodeKey;
    }

    /**
     * Returns the root node for this configuration. If a sub key is set,
     * this implementation re-evaluates this key to find out if this sub
     * configuration needs to be reconstructed. This ensures that the sub
     * configuration is always synchronized with its parent configuration.
     *
     * @return the root node of this configuration
     * @see #setSubnodeKey(String)
     */
    public T getRootNode()
    {
        if (getSubnodeKey() != null)
        {
            try
            {
                NodeList<T> nodes = getParent().fetchNodeList(getSubnodeKey());
                if (nodes.size() != 1 || !nodes.isNode(0))
                {
                    // key is invalid, so detach this sub configuration
                    setSubnodeKey(null);
                }
                else
                {
                    T currentRoot = nodes.getNode(0);
                    if (currentRoot != rootNode)
                    {
                        // the root node was changed due to a change of the parent
                        fireEvent(EVENT_SUBNODE_CHANGED, null, null, true);
                        rootNode = currentRoot;
                        fireEvent(EVENT_SUBNODE_CHANGED, null, null, false);
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

        return rootNode; // use stored root node
    }

    /**
     * Dummy implementation of this {@code HierarchicalConfigurationSource}
     * method. Event listeners are not supported by this implementation.
     *
     * @param l the listener to be added
     */
    public void addConfigurationSourceListener(ConfigurationSourceListener l)
    {
        throw new UnsupportedOperationException("Not implemented!");
    }

    /**
     * Dummy implementation of this {@code HierarchicalConfigurationSource}
     * method. Event listeners are not supported by this implementation.
     *
     * @param l the listener to be removed
     */
    public boolean removeConfigurationSourceListener(
            ConfigurationSourceListener l)
    {
        throw new UnsupportedOperationException("Not implemented!");
    }

    /**
     * Dummy implementation of this {@code HierarchicalConfigurationSource}
     * method. It is not supported to set a different root node.
     *
     * @param root the new root node
     */
    public void setRootNode(T root)
    {
        throw new UnsupportedOperationException("Not implemented!");
    }

    /**
     * Returns the underlying {@code HierarchicalConfigurationSource}. This
     * configuration acts as its own configuration source. This trick makes it
     * possible to let the {@code SubConfiguration} operate on its own root
     * node.
     *
     * @return the associated {@code HierarchicalConfigurationSource}
     */
    @Override
    public HierarchicalConfigurationSource<T> getConfigurationSource()
    {
        return this;
    }

    /**
     * Returns the {@code NodeHandler} for this configuration. This handler is
     * obtained from the parent.
     *
     * @return the {@code NodeHandler} for this configuration
     */
    @Override
    public NodeHandler<T> getNodeHandler()
    {
        return getParent().getNodeHandler();
    }

    /**
     * Returns the capability with the specified class. A {@code
     * SubConfiguration} does not inherit the capabilities of the {@code
     * ConfigurationSource} used by its parent. So this implementation always
     * returns <b>null</b>.
     *
     * @param <T> the type of the capability requested
     * @param cls the class of the capability interface
     * @return the object implementing the desired capability or <b>null</b> if
     *         this capability is not supported
     */
    public <C> C getCapability(Class<C> cls)
    {
        return null;
    }

    /**
     * Returns a hierarchical configuration object for the given sub node.
     * This implementation will ensure that the returned
     * <code>SubConfiguration</code> object will have the same parent as
     * this object.
     *
     * @param node the sub node, for which the configuration is to be created
     * @return a hierarchical configuration for this sub node
     */
    @Override
    protected SubConfiguration<T> createSubnodeConfiguration(T node)
    {
        SubConfiguration<T> result = new SubConfiguration<T>(getParent(), node);
        getParent().registerSubnodeConfiguration(result);
        return result;
    }

    /**
     * Returns a hierarchical configuration object for the given sub node that
     * is aware of structural changes of its parent. Works like the method with
     * the same name, but also sets the subnode key for the new sub
     * configuration, so it can check whether the parent has been changed. This
     * only works if this sub configuration has itself a valid sub key.
     * So if a sub configuration that should be aware of structural changes
     * is created from an already existing sub configuration, this sub
     * configuration must also be aware of such changes.
     *
     * @param node the sub node, for which the configuration is to be created
     * @param subnodeKey the construction key
     * @return a hierarchical configuration for this sub node
     */
    @Override
    protected SubConfiguration<T> createSubnodeConfiguration(
            T node, String subnodeKey)
    {
        SubConfiguration<T> result = createSubnodeConfiguration(node);

        if (getSubnodeKey() != null)
        {
            // construct the correct subnode key
            // determine path to root node
            List<T> lstPathToRoot = new ArrayList<T>();
            T top = rootNode;
            T nd = node;
            while (nd != top)
            {
                lstPathToRoot.add(nd);
                nd = getNodeHandler().getParent(nd);
            }

            // construct the keys for the nodes on this path
            Collections.reverse(lstPathToRoot);
            String key = getSubnodeKey();
            for (T currentNode : lstPathToRoot)
            {
                key = getParent().getExpressionEngine().nodeKey(
                        currentNode, key, getNodeHandler());
            }
            result.setSubnodeKey(key);
        }

        return result;
    }

    /**
     * Creates a new node. This task is delegated to the parent.
     *
     * @param parent the parent node
     * @param name the node's name
     * @return the new node
     */
    @Override
    protected T createNode(T parent, String name)
    {
        return getParent().createNode(parent, name);
    }

    /**
     * Initializes this sub configuration from the given parent
     * configuration. This method is called by the constructor. It will copy
     * many settings from the parent.
     *
     * @param parentConfig the parent configuration
     */
    protected void initFromParent(ConfigurationImpl<T> parentConfig)
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
    @Override
    protected Object interpolate(Object value)
    {
        return getParent().interpolate(value);
    }
}
