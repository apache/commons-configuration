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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.ConfigurationListener;
import org.apache.commons.configuration2.expr.ExpressionEngine;
import org.apache.commons.configuration2.expr.NodeAddData;
import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.expr.NodeList;
import org.apache.commons.configuration2.expr.NodeVisitor;
import org.apache.commons.configuration2.expr.NodeVisitorAdapter;
import org.apache.commons.configuration2.expr.def.DefaultExpressionEngine;
import org.apache.commons.lang.StringUtils;

/**
 * <p>A base class for hierarchical configurations.</p>
 * <p>This class implements the major part of the functionality required for
 * dealing with hierarchical node structures. It provides fundamental algorithms
 * for traversing and manipulating such structures. Access to the node objects
 * is controlled by a <code>{@link NodeHandler}</code> object; therefore this
 * base class can operate on arbitrary node types. (By making use of Java generics,
 * this can even be achieved in a type-safe manner.)</p>
 * <p>Concrete subclasses must initialize this base class with an appropriate
 * <code>{@link NodeHandler}</code> instance. They also have to define a method
 * that returns the root node of the maintained node hierarchy.</p>
 *
 * @author Oliver Heger
 * @version $Id$
 * @since 2.0
 * @param <T> the type of the nodes this configuration deals with
 */
public abstract class AbstractHierarchicalConfiguration<T> extends AbstractConfiguration implements Cloneable
{
    /**
     * Constant for the clear tree event.
     */
    public static final int EVENT_CLEAR_TREE = 10;

    /**
     * Constant for the subnode configuration modified event.
     */
    public static final int EVENT_SUBNODE_CHANGED = 12;

    /** Stores the default expression engine to be used for new objects.*/
    private static ExpressionEngine defaultExpressionEngine;

    /** Stores the expression engine for this instance.*/
    private ExpressionEngine expressionEngine;

    /** Stores the node handler for accessing the internally used nodes.*/
    private NodeHandler<T> nodeHandler;

    /**
     * Creates a new instance of <code>HierarchicalConfiguration</code>.
     */
    protected AbstractHierarchicalConfiguration(NodeHandler<T> handler)
    {
        nodeHandler = handler;
    }

    /**
     * Returns the <code>NodeHandler</code> used by this configuration.
     *
     * @return the node handler
     */
    public NodeHandler<T> getNodeHandler()
    {
        return nodeHandler;
    }

    /**
     * Allows setting the {@code NodeHandler}. This method is intended to be
     * used by sub classes with specific requirements for node handlers.
     *
     * @param handler the new {@code NodeHandler}
     */
    protected void setNodeHandler(NodeHandler<T> handler)
    {
        nodeHandler = handler;
    }

    /**
     * Returns the root node of this hierarchical configuration.
     *
     * @return the root node
     */
    public abstract T getRootNode();

    /**
     * Returns the default expression engine.
     *
     * @return the default expression engine
     */
    public static synchronized ExpressionEngine getDefaultExpressionEngine()
    {
        if (defaultExpressionEngine == null)
        {
            defaultExpressionEngine = new DefaultExpressionEngine();
        }
        return defaultExpressionEngine;
    }

    /**
     * Sets the default expression engine. This expression engine will be used
     * if no specific engine was set for an instance. It is shared between all
     * hierarchical configuration instances. So modifying its properties will
     * impact all instances, for which no specific engine is set.
     *
     * @param engine the new default expression engine
     */
    public static synchronized void setDefaultExpressionEngine(ExpressionEngine engine)
    {
        if (engine == null)
        {
            throw new IllegalArgumentException("Default expression engine must not be null!");
        }
        defaultExpressionEngine = engine;
    }

    /**
     * Returns the expression engine used by this configuration. This method
     * will never return <b>null</b>; if no specific expression engine was set,
     * the default expression engine will be returned.
     *
     * @return the current expression engine
     */
    public ExpressionEngine getExpressionEngine()
    {
        return (expressionEngine != null) ? expressionEngine : getDefaultExpressionEngine();
    }

    /**
     * Sets the expression engine to be used by this configuration. All property
     * keys this configuration has to deal with will be interpreted by this
     * engine.
     *
     * @param expressionEngine the new expression engine; can be <b>null</b>,
     * then the default expression engine will be used
     */
    public void setExpressionEngine(ExpressionEngine expressionEngine)
    {
        this.expressionEngine = expressionEngine;
    }

    /**
     * Fetches the specified property. This task is delegated to the associated
     * expression engine.
     *
     * @param key the key to be looked up
     * @return the found value
     */
    public Object getProperty(String key)
    {
        NodeList<T> nodes = fetchNodeList(key);

        if (nodes.size() == 0)
        {
            return null;
        }
        else
        {
            List<Object> list = new ArrayList<Object>();
            for (int i = 0; i < nodes.size(); i++)
            {
                Object value = nodes.getValue(i, getNodeHandler());
                if (value != null)
                {
                    if (nodes.isAttribute(i) && value instanceof Collection)
                    {
                        // there may be multiple values
                        list.addAll((Collection<?>) value);
                    }
                    else
                    {
                        list.add(value);
                    }
                }
            }

            if (list.size() < 1)
            {
                return null;
            }
            else
            {
                return (list.size() == 1) ? list.get(0) : list;
            }
        }
    }

    /**
     * Adds the property with the specified key. This task will be delegated to
     * the associated <code>ExpressionEngine</code>, so the passed in key
     * must match the requirements of this implementation.
     *
     * @param key the key of the new property
     * @param obj the value of the new property
     */
    @Override
    protected void addPropertyDirect(String key, Object obj)
    {
        NodeAddData<T> data = getExpressionEngine().prepareAdd(getRootNode(),
                key, getNodeHandler());
        processNodeAddData(data, obj);
    }

    /**
     * Checks if this configuration is empty. Empty means that there are no keys
     * with any values, though there can be some (empty) nodes.
     *
     * @return a flag if this configuration is empty
     */
    public boolean isEmpty()
    {
        return !nodeDefined(getRootNode());
    }

    /**
     * <p>
     * Returns a hierarchical sub configuration object that wraps the
     * configuration node specified by the given key. This method provides an
     * easy means of accessing sub trees of a hierarchical configuration. In the
     * returned configuration the sub tree can directly be accessed, it becomes
     * the root node of this configuration. Because of this the passed in key
     * must select exactly one configuration node; otherwise an
     * <code>IllegalArgumentException</code> will be thrown.
     * </p>
     * <p>
     * The difference between this method and the
     * <code>{@link #subset(String)}</code> method is that
     * <code>subset()</code> supports arbitrary subsets of configuration nodes
     * while <code>configurationAt()</code> only returns a single sub tree.
     * Please refer to the documentation of the
     * <code>{@link SubConfiguration}</code> class to obtain further information
     * about sub configurations and when they should be used.
     * </p>
     * <p>
     * With the <code>supportUpdate</code> flag the behavior of the returned
     * <code>SubConfiguration</code> regarding updates of its parent
     * configuration can be determined. A sub configuration operates on the
     * same nodes as its parent, so changes at one configuration are normally
     * directly visible for the other configuration. There are however changes
     * of the parent configuration, which are not recognized by the sub
     * configuration per default. An example for this is a reload operation (for
     * file-based configurations): Here the complete node set of the parent
     * configuration is replaced, but the sub configuration still references
     * the old nodes. If such changes should be detected by the sub
     * configuration, the <code>supportUpdates</code> flag must be set to
     * <b>true</b>. This causes the sub configuration to reevaluate the key
     * used for its creation each time it is accessed. This guarantees that the
     * sub configuration always stays in sync with its key, even if the
     * parent configuration's data significantly changes. If such a change
     * makes the key invalid - because it now no longer points to exactly one
     * node -, the sub configuration is not reconstructed, but keeps its
     * old data. It is then quasi detached from its parent.
     * </p>
     *
     * @param key the key that selects the sub tree
     * @param supportUpdates a flag whether the returned sub configuration
     * should be able to handle updates of its parent
     * @return a hierarchical configuration that contains this sub tree
     * @see SubConfiguration
     */
    public SubConfiguration<T> configurationAt(String key, boolean supportUpdates)
    {
        NodeList<T> nodes = fetchNodeList(key);
        if (nodes.size() != 1 || !nodes.isNode(0))
        {
            throw new IllegalArgumentException("Passed in key must select exactly one node: " + key);
        }
        return supportUpdates ? createSubnodeConfiguration(nodes.getNode(0), key) : createSubnodeConfiguration(nodes.getNode(0));
    }

    /**
     * Returns a hierarchical sub configuration for the node specified by
     * the given key. This is a short form for <code>configurationAt(key,
     * <b>false</b>)</code>.
     *
     * @param key the key that selects the sub tree
     * @return a hierarchical configuration that contains this sub tree
     * @see SubConfiguration
     */
    public SubConfiguration<T> configurationAt(String key)
    {
        return configurationAt(key, false);
    }

    /**
     * Returns a list of sub configurations for all configuration nodes selected
     * by the given key. This method will evaluate the passed in key (using the
     * current <code>ExpressionEngine</code>) and then create a
     * <code>{@link SubConfiguration}</code> for each returned node (like
     * <code>{@link #configurationAt(String)}</code>}). This is especially
     * useful when dealing with list-like structures. As an example consider the
     * configuration that contains data about database tables and their fields.
     * If you need access to all fields of a certain table, you can simply do
     *
     * <pre>
     * List<SubConfiguration<T>> fields = config.configurationsAt("tables.table(0).fields.field");
     * for(SubConfiguration sub : fields)
     * {
     *     // now the children and attributes of the field node can be
     *     // directly accessed
     *     String fieldName = sub.getString("name");
     *     String fieldType = sub.getString("type");
     *     ...
     * </pre>
     * This method also supports a <code>supportUpdates</code> parameter for
     * making the sub configurations returned aware of structural changes in
     * the parent configuration. Refer to the documentation of
     * <code>{@link #configurationAt(String, boolean)}</code> for more details
     * about the effect of this flag.
     *
     * @param key the key for selecting the desired nodes
     * @param supportUpdates a flag whether the returned sub configurations
     * should be able to handle updates of its parent
     * @return a list with hierarchical configuration objects; each
     * configuration represents one of the nodes selected by the passed in key
     */
    public List<SubConfiguration<T>> configurationsAt(String key, boolean supportUpdates)
    {
        NodeList<T> nodes = fetchNodeList(key);
        List<SubConfiguration<T>> configs = new ArrayList<SubConfiguration<T>>(
                nodes.size());

        for (int index = 0; index < nodes.size(); index++)
        {
            SubConfiguration<T> subConfig;
            if(supportUpdates)
            {
                String subnodeKey = constructPath(nodes.getNode(index));
                subConfig = createSubnodeConfiguration(nodes.getNode(index), subnodeKey);
            }
            else
            {
                subConfig = createSubnodeConfiguration(nodes.getNode(index));
            }
            configs.add(subConfig);
        }

        return configs;
    }

    /**
     * Returns a list of sub configurations for all configuration nodes selected
     * by the given key that are not aware of structural updates of their
     * parent. This is a short form for
     * <code>configurationsAt(key, <b>false</b>)</code>.
     *
     * @param key the key for selecting the desired nodes
     * @return a list with hierarchical configuration objects; each
     *         configuration represents one of the nodes selected by the passed
     *         in key
     */
    public List<SubConfiguration<T>> configurationsAt(String key)
    {
        return configurationsAt(key, false);
    }

    /**
     * Creates a sub configuration for the specified node. This method is
     * called by <code>configurationAt()</code> and
     * <code>configurationsAt()</code>.
     *
     * @param node the node, for which a sub configuration is to be created
     * @return the configuration for the given node
     */
    protected SubConfiguration<T> createSubnodeConfiguration(T node)
    {
        SubConfiguration<T> result = new SubConfiguration<T>(this, node);
        registerSubnodeConfiguration(result);
        return result;
    }

    /**
     * Creates a new sub configuration for the specified node and sets its
     * construction key. A sub configuration created this way will be aware
     * of structural changes of its parent.
     *
     * @param node the node, for which a sub configuration is to be created
     * @param subnodeKey the key used to construct the configuration
     * @return the configuration for the given node
     */
    protected SubConfiguration<T> createSubnodeConfiguration(T node, String subnodeKey)
    {
        SubConfiguration<T> result = createSubnodeConfiguration(node);
        result.setSubnodeKey(subnodeKey);
        return result;
    }

    /**
     * This method is always called when a subnode configuration created from
     * this configuration has been modified. This implementation transforms the
     * received event into an event of type <code>EVENT_SUBNODE_CHANGED</code>
     * and notifies the registered listeners.
     *
     * @param event the event describing the change
     */
    protected void subnodeConfigurationChanged(ConfigurationEvent event)
    {
        fireEvent(EVENT_SUBNODE_CHANGED, null, event, event.isBeforeUpdate());
    }

    /**
     * Registers this instance at the given sub configuration. This
     * implementation will register a change listener, so that modifications of
     * the sub configuration can be tracked.
     *
     * @param config the sub configuration
     */
    void registerSubnodeConfiguration(SubConfiguration<T> config)
    {
        config.addConfigurationListener(new ConfigurationListener()
        {
            public void configurationChanged(ConfigurationEvent event)
            {
                subnodeConfigurationChanged(event);
            }
        });
    }

    /**
     * Determines the path from the given node to the root node. The return
     * value is the key that uniquely identifies the given node. The associated
     * expression engine is used for constructing and combining the parts the
     * key is composed of.
     *
     * @param node the node in question (must not be <b>null</b>)
     * @return a unique key for this node
     */
    protected String constructPath(T node)
    {
        if (node == null)
        {
            return StringUtils.EMPTY;
        }
        else
        {
            // recursively navigate to the root and construct all paths
            return getExpressionEngine().uniqueNodeKey(node,
                    constructPath(getNodeHandler().getParent(node)),
                    getNodeHandler());
        }
    }

    /**
     * Checks if the specified key is contained in this configuration. Note that
     * for this configuration the term &quot;contained&quot; means that the key
     * has an associated value. If there is a node for this key that has no
     * value but children (either defined or undefined), this method will still
     * return <b>false </b>.
     *
     * @param key the key to be checked
     * @return a flag if this key is contained in this configuration
     */
    public boolean containsKey(String key)
    {
        return getProperty(key) != null;
    }

    /**
     * Sets the value of the specified property.
     *
     * @param key the key of the property to set
     * @param value the new value of this property
     */
    @Override
    public void setProperty(String key, Object value)
    {
        fireEvent(EVENT_SET_PROPERTY, key, value, true);

        // Update the existing nodes for this property
        NodeList<T> nodes = fetchNodeList(key);
        Iterator<?> itValues;
        if (!isDelimiterParsingDisabled())
        {
            itValues = PropertyConverter.toIterator(value, getListDelimiter());
        }
        else
        {
            itValues = Collections.singleton(value).iterator();
        }

        int index = 0;
        while (index < nodes.size() && itValues.hasNext())
        {
            nodes.setValue(index, itValues.next(), getNodeHandler());
            index++;
        }

        // Add additional nodes if necessary
        while (itValues.hasNext())
        {
            addPropertyDirect(key, itValues.next());
        }

        // Remove remaining nodes
        while (index < nodes.size())
        {
            removeListElement(nodes, index++, true);
        }

        fireEvent(EVENT_SET_PROPERTY, key, value, false);
    }

    /**
     * Removes all values of the property with the given name and of keys that
     * start with this name. So if there is a property with the key
     * &quot;foo&quot; and a property with the key &quot;foo.bar&quot;, a call
     * of <code>clearTree("foo")</code> would remove both properties.
     *
     * @param key the key of the property to be removed
     */
    public void clearTree(String key)
    {
        removeNodeList(key, EVENT_CLEAR_TREE, false);
    }

    /**
     * Removes the property with the given key. Properties with names that start
     * with the given key (i.e. properties below the specified key in the
     * hierarchy) won't be affected.
     *
     * @param key the key of the property to be removed
     */
    @Override
    protected void clearPropertyDirect(String key)
    {
        removeNodeList(key, -1, true);
    }

    /**
     * Removes the list element with the specified index from this
     * configuration. This method calls the appropriate remove method depending
     * on the type of the list element.
     *
     * @param nodes the node list
     * @param index the index
     * @param clear a flag whether the element should only be cleared or completely removed
     */
    private void removeListElement(NodeList<T> nodes, int index, boolean clear)
    {
        if (nodes.isNode(index))
        {
            if (clear)
            {
                clearNode(nodes.getNode(index));
            }
            else
            {
                removeNode(nodes.getNode(index));
            }
        }
        else
        {
            T parent = nodes.getAttributeParent(index);
            getNodeHandler().removeAttribute(parent,
                    nodes.getName(index, getNodeHandler()));
            removeNodeIfUndefined(parent);
        }
    }

    /**
     * Removes or clears all nodes or attributes matched by the given key.
     * @param key the key
     * @param event the event to fire
     * @param clear determines whether the elements are cleared or removed
     */
    private void removeNodeList(String key, int event, boolean clear)
    {
        if (event >= 0)
        {
            fireEvent(event, key, null, true);
        }

        NodeList<T> nodes = fetchNodeList(key);

        for (int index = 0; index < nodes.size(); index++)
        {
            removeListElement(nodes, index, clear);
        }

        if (event >= 0)
        {
            fireEvent(event, key, null, false);
        }
    }

    /**
     * Returns an iterator with all keys defined in this configuration.
     * Note that the keys returned by this method will not contain any
     * indices. This means that some structure will be lost.</p>
     *
     * @return an iterator with the defined keys in this configuration
     */
    public Iterator<String> getKeys()
    {
        DefinedKeysVisitor visitor = new DefinedKeysVisitor();
        visit(getRootNode(), visitor);

        return visitor.getKeyList().iterator();
    }

    /**
     * Returns an iterator with all keys defined in this configuration that
     * start with the given prefix. The returned keys will not contain any
     * indices.
     *
     * @param prefix the prefix of the keys to start with
     * @return an iterator with the found keys
     */
    @Override
    public Iterator<String> getKeys(String prefix)
    {
        DefinedKeysVisitor visitor = new DefinedKeysVisitor(prefix);
        if (containsKey(prefix))
        {
            // explicitly add the prefix
            visitor.getKeyList().add(prefix);
        }

        NodeList<T> nodes = fetchNodeList(prefix);

        for (int i = 0; i < nodes.size(); i++)
        {
            if(nodes.isNode(i))
            {
                for(T child : getNodeHandler().getChildren(nodes.getNode(i)))
                {
                    visit(child, visitor);
                }
                visitor.appendAttributes(nodes.getNode(i), prefix, getNodeHandler());
            }
        }

        return visitor.getKeyList().iterator();
    }

    /**
     * Returns the maximum defined index for the given key. This is useful if
     * there are multiple values for this key. They can then be addressed
     * separately by specifying indices from 0 to the return value of this
     * method.
     *
     * @param key the key to be checked
     * @return the maximum defined index for this key
     */
    public int getMaxIndex(String key)
    {
        NodeList<T> nodes = fetchNodeList(key);
        int cnt = 0;

        for (int index = 0; index < nodes.size(); index++)
        {
            Object value = nodes.getValue(index, getNodeHandler());
            if (value instanceof Collection)
            {
                // if there are multiple values, count them all
                cnt += ((Collection<?>) value).size();
            }
            else
            {
                cnt++;
            }
        }

        return cnt - 1;
    }

    /**
     * Helper method for fetching a list of all nodes that are addressed by the
     * specified key.
     *
     * @param key the key
     * @return a list with all affected nodes (never <b>null </b>)
     */
    protected NodeList<T> fetchNodeList(String key)
    {
        return getExpressionEngine().query(getRootNode(), key, getNodeHandler());
    }

    /**
     * Visits the specified configuration node. This method implements the
     * traversal of the node hierarchy starting with the specified node.
     *
     * @param node the node to be visited
     * @param visitor the visitor
     */
    protected void visit(T node, NodeVisitor<T> visitor)
    {
        NodeVisitorAdapter.visit(visitor, node, getNodeHandler());
    }

    /**
     * Checks if the specified node is defined.
     *
     * @param node the node to be checked
     * @return a flag if this node is defined
     */
    protected boolean nodeDefined(T node)
    {
        DefinedVisitor<T> visitor = new DefinedVisitor<T>();
        visit(node, visitor);
        return visitor.isDefined();
    }

    /**
     * Removes the specified node from this configuration. This method ensures
     * that parent nodes that become undefined by this operation are also
     * removed.
     *
     * @param node the node to be removed
     */
    protected void removeNode(T node)
    {
        T parent = getNodeHandler().getParent(node);
        if (parent != null)
        {
            getNodeHandler().removeChild(parent, node);
            removeNodeIfUndefined(parent);
        }
    }

    /**
     * Clears the value of the specified node. If the node becomes undefined by
     * this operation, it is removed from the hierarchy.
     *
     * @param node the node to be cleared
     */
    protected void clearNode(T node)
    {
        getNodeHandler().setValue(node, null);
        removeNodeIfUndefined(node);
    }

    /**
     * Removes the specified node if it is undefined.
     *
     * @param node the node
     */
    private void removeNodeIfUndefined(T node)
    {
        if (!nodeDefined(node))
        {
            removeNode(node);
        }
    }

    /**
     * Creates a new node object with the specified name. This base implementation
     * delegates to the <code>NodeHandler</code> for creating a new node.
     *
     * @param parent the parent of the new node
     * @param name the name of the new node
     * @return the new node
     */
    protected T createNode(T parent, String name)
    {
        return getNodeHandler().addChild(parent, name);
    }

    /**
     * Creates a new node object with the specified name and value. This base
     * implementation delegates to the <code>NodeHandler</code> for creating a
     * new node.
     *
     * @param parent the parent of the new node
     * @param name the name of the new node
     * @param value the value of the new node
     * @return the new node
     */
    protected T createNode(T parent, String name, Object value)
    {
        return getNodeHandler().addChild(parent, name, value);
    }

    /**
     * Helper method for processing a <code>NodeAddData</code> object obtained from the
     * expression engine. This method will create all new nodes and set the value
     * of the last node, which represents the newly added property.
     *
     * @param data the data object
     * @param value the value of the new property
     * @return the new node (<b>null</b> if an attribute was added)
     */
    protected T processNodeAddData(NodeAddData<T> data, Object value)
    {
        T node = data.getParent();

        // Create missing nodes on the path
        for (String nodeName : data.getPathNodes())
        {
            node = createNode(node, nodeName);
        }

        // Add the new property
        return addNodeValue(node, data.getNewNodeName(), value, data.isAttribute());
    }

    /**
     * Adds a new value to a node, which can either be a child node or an
     * attribute. This method is called by <code>processNodeAddData()</code>
     * for the final node to be added. It can be overridden by concrete sub
     * classes with specific requirements for adding values. This base
     * implementation uses the <code>NodeHandler</code> of this configuration
     * for either adding a new child node or an attribute value.
     *
     * @param parent the parent node (to which a value should be added)
     * @param name the name of the property to be added
     * @param value the value itself
     * @param attr a flag whether a child node or an attribute should be added
     * @return the newly created child node or <b>null</b> for an attribute
     */
    protected T addNodeValue(T parent, String name, Object value, boolean attr)
    {
        if (attr)
        {
            getNodeHandler().addAttributeValue(parent, name, value);
            return null;
        }
        else
        {
            return createNode(parent, name, value);
        }
    }

    /**
     * A specialized visitor that checks if a node is defined.
     * &quot;Defined&quot; in this terms means that the node or at least one of
     * its sub nodes is associated with a value.
     */
    private static class DefinedVisitor<T> extends NodeVisitorAdapter<T>
    {
        /** Stores the defined flag. */
        private boolean defined;

        /**
         * Checks if iteration should be stopped. This can be done if the first
         * defined node is found.
         *
         * @return a flag if iteration should be stopped
         */
        @Override
        public boolean terminate()
        {
            return isDefined();
        }

        /**
         * Visits the node. Checks if a value is defined.
         *
         * @param node the actual node
         */
        @Override
        public void visitBeforeChildren(T node, NodeHandler<T> handler)
        {
            defined = handler.isDefined(node);
        }

        /**
         * Returns the defined flag.
         *
         * @return the defined flag
         */
        public boolean isDefined()
        {
            return defined;
        }
    }

    /**
     * A specialized visitor that fills a list with keys that are defined in a
     * node hierarchy.
     */
    private class DefinedKeysVisitor extends NodeVisitorAdapter<T>
    {
        /** Stores the list to be filled. */
        private Set<String> keyList;

        /** A stack with the keys of the already processed nodes. */
        private Stack<String> parentKeys;

        /**
         * Default constructor.
         */
        public DefinedKeysVisitor()
        {
            keyList = new LinkedHashSet<String>();
            parentKeys = new Stack<String>();
        }

        /**
         * Creates a new <code>DefinedKeysVisitor</code> instance and sets the
         * prefix for the keys to fetch.
         *
         * @param prefix the prefix
         */
        public DefinedKeysVisitor(String prefix)
        {
            this();
            parentKeys.push(prefix);
        }

        /**
         * Returns the list with all defined keys.
         *
         * @return the list with the defined keys
         */
        public Set<String> getKeyList()
        {
            return keyList;
        }

        /**
         * Visits the node after its children has been processed. Removes this
         * node's key from the stack.
         *
         * @param node the node
         * @param handler the node handler
         */
        @Override
        public void visitAfterChildren(T node, NodeHandler<T> handler)
        {
            parentKeys.pop();
        }

        /**
         * Visits the specified node. If this node has a value, its key is added
         * to the internal list.
         *
         * @param node the node to be visited
         * @param handler the node handler
         */
        @Override
        public void visitBeforeChildren(T node, NodeHandler<T> handler)
        {
            String parentKey = parentKeys.isEmpty() ? null : parentKeys.peek();
            String key = getExpressionEngine().nodeKey(node, parentKey, handler);
            parentKeys.push(key);
            if (handler.getValue(node) != null)
            {
                keyList.add(key);
            }

            appendAttributes(node, key, handler);
        }

        /**
         * Adds the keys of the attributes of the given node to the internal key
         * list.
         *
         * @param node the parent node
         * @param parentKey the key of the parent node
         * @param handler the node handler
         */
        public void appendAttributes(T node, String parentKey, NodeHandler<T> handler)
        {
            List<String> attributes = handler.getAttributes(node);
            for (String attr : attributes)
            {
                keyList.add(getExpressionEngine().attributeKey(node, parentKey, attr, handler));
            }
        }
    }
}
