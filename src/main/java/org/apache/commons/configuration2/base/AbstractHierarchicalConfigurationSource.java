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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.configuration2.expr.ExpressionEngine;
import org.apache.commons.configuration2.expr.NodeAddData;
import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.expr.NodeList;
import org.apache.commons.configuration2.expr.NodeVisitor;
import org.apache.commons.configuration2.expr.NodeVisitorAdapter;
import org.apache.commons.configuration2.expr.def.DefaultExpressionEngine;

/**
 * <p>
 * An abstract base implementation of the {@code
 * HierarchicalConfigurationSource} interface.
 * </p>
 * <p>
 * This class provides fully functional implementations for most of the methods
 * required by the {@code HierarchicalConfigurationSource} interface. These
 * methods operate on the hierarchical node structure maintained by this
 * configuration source. Concrete sub classes must implement the
 * {@link HierarchicalConfigurationSource#getRootNode()} method to return the
 * starting point of this structure.
 * </p>
 * <p>
 * Implementation note: Provided that the configuration nodes used are properly
 * implemented, a {@code AbstractHierarchicalConfigurationSource} can be queried
 * concurrently by multiple threads. However, if updates are performed, client
 * code must ensure proper synchronization.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 * @param <T> the type of the nodes this source operates on
 */
public abstract class AbstractHierarchicalConfigurationSource<T> implements
        HierarchicalConfigurationSource<T>
{
    /** Stores the node handler used by this source. */
    private final NodeHandler<T> nodeHandler;

    /** The currently used expression engine. */
    private volatile ExpressionEngine expressionEngine = new DefaultExpressionEngine();

    /**
     * Creates a new instance of {@code AbstractHierarchicalConfigurationSource}
     * and initializes it with the {@code NodeHandler}.
     *
     * @param handler the {@code NodeHandler}
     */
    protected AbstractHierarchicalConfigurationSource(NodeHandler<T> handler)
    {
        nodeHandler = handler;
    }

    /**
     * Evaluates the specified expression and returns a {@code NodeList} with
     * the matching nodes.
     *
     * @param expr the expression to evaluate
     * @return the found nodes
     */
    public NodeList<T> find(String expr)
    {
        return getExpressionEngine().query(getRootNode(), expr,
                getNodeHandler());
    }

    /**
     * Returns the {@code NodeHandler} used by this source. This implementation
     * returns the {@code NodeHandler} that was passed in when this object was
     * created.
     *
     * @return the {@code NodeHandler}
     */
    public NodeHandler<T> getNodeHandler()
    {
        return nodeHandler;
    }

    /**
     * Sets the specified node as root node. This base implementation does not
     * support changing the root node. It always throws an exception.
     *
     * @param root the new root node
     * @throws UnsupportedOperationException if the operation is not supported
     */
    public void setRootNode(T root)
    {
        throw new UnsupportedOperationException(
                "Changing the root node is not supported!");
    }

    /**
     * Adds the property with the specified key. This task will be delegated to
     * the associated {@code ExpressionEngine}, so the passed in key must match
     * the requirements of this implementation.
     *
     * @param key the key of the new property
     * @param obj the value of the new property
     */
    public void addProperty(String key, Object value)
    {
        NodeAddData<T> data = getExpressionEngine().prepareAdd(getRootNode(),
                key, getNodeHandler());
        processNodeAddData(data, value);
    }

    /**
     * Removes all data from this configuration source. This implementation
     * removes all child nodes and all attributes from the root node.
     */
    public void clear()
    {
        // remove value
        getNodeHandler().setValue(getRootNode(), null);

        // remove all child nodes
        List<T> children = new ArrayList<T>(getNodeHandler().getChildren(
                getRootNode()));
        for (T node : children)
        {
            getNodeHandler().removeChild(getRootNode(), node);
        }

        // remove all attributes
        for (String attr : getNodeHandler().getAttributes(getRootNode()))
        {
            getNodeHandler().removeAttribute(getRootNode(), attr);
        }
    }

    /**
     * Removes the property with the given key. Properties with names that start
     * with the given key (i.e. properties below the specified key in the
     * hierarchy) won't be affected.
     *
     * @param key the key of the property to be removed
     */
    public void clearProperty(String key)
    {
        removeNodeList(key, true);
    }

    /**
     * Removes a whole sub tree from this configuration node.
     *
     * @param key the key pointing to the start node
     */
    public void clearTree(String key)
    {
        removeNodeList(key, false);
    }

    /**
     * Checks if the specified key is contained in this configuration source.
     * Note that for this source the term &quot;contained&quot; means that the
     * key has an associated value. If there is a node for this key that has no
     * value but children (either defined or undefined), this method will still
     * return <b>false</b>.
     *
     * @param key the key to be checked
     * @return a flag if this key is contained in this configuration
     */
    public boolean containsKey(String key)
    {
        return getProperty(key) != null;
    }

    /**
     * Returns the {@code ExpressionEngine} used by this source. When a new
     * instance of this class is created a default expression engine is set. It
     * is later possible to change this engine.
     *
     * @return the currently used {@code ExpressionEngine}
     */
    public ExpressionEngine getExpressionEngine()
    {
        return expressionEngine;
    }

    /**
     * Returns an iterator with all keys defined in this configuration. Note
     * that the keys returned by this method will not contain any indices. This
     * means that some structure will be lost.</p>
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
    public Iterator<String> getKeys(String prefix)
    {
        DefinedKeysVisitor visitor = new DefinedKeysVisitor(prefix);
        if (containsKey(prefix))
        {
            // explicitly add the prefix
            visitor.getKeyList().add(prefix);
        }

        NodeList<T> nodes = find(prefix);

        for (int i = 0; i < nodes.size(); i++)
        {
            if (nodes.isNode(i))
            {
                for (T child : getNodeHandler().getChildren(nodes.getNode(i)))
                {
                    visit(child, visitor);
                }
                visitor.appendAttributes(nodes.getNode(i), prefix,
                        getNodeHandler());
            }
        }

        return visitor.getKeyList().iterator();
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
        NodeList<T> nodes = find(key);

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
     * Returns a flag whether this configuration source is empty. This is the
     * case if it does not contain at least one value. This implementation is
     * slightly more efficient than the {@code size()} method. So it is
     * preferable to check {@code isEmpty()} rather than {@code size() == 0}.
     *
     * @return <b>true</b> if this configuration is empty, <b>false</b>
     *         otherwise
     */
    public boolean isEmpty()
    {
        return !nodeDefined(getRootNode());
    }

    /**
     * Sets the {@code ExpressionEngine} to be used by this source. The {@code
     * ExpressionEngine} is used for resolving property keys.
     *
     * @param engine the new {@code ExpressionEngine} (must not be <b>null</b>)
     * @throws IllegalArgumentException if the {@code ExpressionEngine} is
     *         <b>null</b>
     */
    public void setExpressionEngine(ExpressionEngine engine)
    {
        if (engine == null)
        {
            throw new IllegalArgumentException(
                    "Expression engine must not be null!");
        }

        expressionEngine = engine;
    }

    /**
     * Sets the specified property to the given single value.
     *
     * @param key the key of the property
     * @param value the new value
     */
    public void setProperty(String key, Object value)
    {
        setProperty(key, Collections.singletonList(value));
    }

    /**
     * Sets multiple values for the specified property.
     *
     * @param key the key of the property
     * @param values a collection with the new values (must not be <b>null</b>)
     * @throws IllegalArgumentException if the collection with the new values is
     *         <b>null</b>
     */
    public void setProperty(String key, Collection<?> values)
    {
        if (values == null)
        {
            throw new IllegalArgumentException(
                    "Value collection must not be null!");
        }

        // Update the existing nodes for this property
        NodeList<T> nodes = find(key);
        Iterator<?> itValues = values.iterator();
        int index = 0;
        while (index < nodes.size() && itValues.hasNext())
        {
            nodes.setValue(index, itValues.next(), getNodeHandler());
            index++;
        }

        // Add additional nodes if necessary
        while (itValues.hasNext())
        {
            addProperty(key, itValues.next());
        }

        // Remove remaining nodes
        while (index < nodes.size())
        {
            removeListElement(nodes, index++, true);
        }
    }

    /**
     * Determines the size of this configuration source. This implementation
     * iterates over all nodes and count their values. So complexity is O(n)
     * where n is the size of this source.
     *
     * @return the number of values stored in this configuration source
     */
    public int size()
    {
        SizeVisitor<T> visitor = new SizeVisitor<T>();
        visit(null, visitor);
        return visitor.size();
    }

    /**
     * Returns the number of values stored for the passed in key. Note that
     * using this method is not possible to distinguish between a key that does
     * not exist in this source and a key referencing a node without a value. In
     * both cases result is 0.
     *
     * @param key the key in question
     * @return the number of values stored for this key
     */
    public int valueCount(String key)
    {
        NodeList<T> nodes = find(key);
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
                if (value != null)
                {
                    cnt++;
                }
            }
        }

        return cnt;
    }

    /**
     * Navigates the specified visitor over the node structure starting with the
     * given node. If the start node is <b>null</b>, the root node will be used
     * instead.
     *
     * @param node the start node
     * @param visitor the visitor (must not be <b>null</b>)
     * @throws IllegalArgumentException if the visitor is <b>null</b>
     */
    public void visit(T node, NodeVisitor<T> visitor)
    {
        if (visitor == null)
        {
            throw new IllegalArgumentException("Visitor must not be null!");
        }

        doVisit((node == null) ? getRootNode() : node, visitor);
    }

    /**
     * The actual implementation of the traversal of all nodes. This method is
     * called by {@link #visit(Object, NodeVisitor)}.
     *
     * @param node the current node to visit
     * @param visitor the visitor
     */
    protected void doVisit(T node, NodeVisitor<T> visitor)
    {
        if (!visitor.terminate())
        {
            visitor.visitBeforeChildren(node, getNodeHandler());

            for (Iterator<T> it = getNodeHandler().getChildren(node).iterator(); it
                    .hasNext()
                    && !visitor.terminate();)
            {
                doVisit(it.next(), visitor);
            }

            visitor.visitAfterChildren(node, getNodeHandler());
        }
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
     * Creates a new node object with the specified name. This base
     * implementation delegates to the {@code NodeHandler} for creating a new
     * node.
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
     * implementation delegates to the {@code NodeHandler} for creating a new
     * node.
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
     * Helper method for processing a {@code NodeAddData} object obtained from
     * the expression engine. This method will create all new nodes and set the
     * value of the last node, which represents the newly added property.
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
            T child = createNode(node, nodeName);
            node = child;
        }

        // Add the new property
        return addNodeValue(node, data.getNewNodeName(), value, data
                .isAttribute());
    }

    /**
     * Adds a new value to a node, which can either be a child node or an
     * attribute. This method is called by {@code processNodeAddData()} for the
     * final node to be added. It can be overridden by concrete sub classes with
     * specific requirements for adding values. This base implementation uses
     * the {@code NodeHandler} of this configuration source for either adding a
     * new child node or an attribute value.
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
            T child = createNode(parent, name, value);
            return child;
        }
    }

    /**
     * Removes the list element with the specified index from this
     * configuration. This method calls the appropriate remove method depending
     * on the type of the list element.
     *
     * @param nodes the node list
     * @param index the index
     * @param clear a flag whether the element should only be cleared or
     *        completely removed
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
     * Removes or clears all nodes or attributes matched by the given key.
     *
     * @param key the key
     * @param clear determines whether the elements are cleared or removed
     */
    private void removeNodeList(String key, boolean clear)
    {
        NodeList<T> nodes = find(key);

        for (int index = 0; index < nodes.size(); index++)
        {
            removeListElement(nodes, index, clear);
        }
    }

    /**
     * A specialized visitor implementation for determining the size of this
     * configuration source.
     *
     * @param <T> the type of the nodes this visitor iterates over
     */
    private static class SizeVisitor<T> extends NodeVisitorAdapter<T>
    {
        /** The counter for the values. */
        private int count;

        /**
         * Visits a node.
         *
         * @param node the node
         * @param handler the node handler
         */
        @Override
        public void visitBeforeChildren(T node, NodeHandler<T> handler)
        {
            if (handler.getValue(node) != null)
            {
                count++;
            }

            for (String attr : handler.getAttributes(node))
            {
                Object value = handler.getAttributeValue(node, attr);
                if (value instanceof Collection)
                {
                    count += ((Collection<?>) value).size();
                }
                else
                {
                    count++;
                }
            }
        }

        /**
         * Returns the size.
         *
         * @return the size
         */
        public int size()
        {
            return count;
        }
    }

    /**
     * A specialized visitor that checks if a node is defined.
     * &quot;Defined&quot; in this terms means that the node or at least one of
     * its sub nodes is associated with a value.
     *
     * @param <T> the type of the nodes this visitor iterates over
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
        private final Set<String> keyList;

        /** A stack with the keys of the already processed nodes. */
        private final Stack<String> parentKeys;

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
            String parentKey = parentKeys.isEmpty() ? null
                    : (String) parentKeys.peek();
            String key = getExpressionEngine()
                    .nodeKey(node, parentKey, handler);
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
        public void appendAttributes(T node, String parentKey,
                NodeHandler<T> handler)
        {
            List<String> attributes = handler.getAttributes(node);
            for (String attr : attributes)
            {
                keyList.add(getExpressionEngine().attributeKey(node, parentKey,
                        attr, handler));
            }
        }
    }
}
