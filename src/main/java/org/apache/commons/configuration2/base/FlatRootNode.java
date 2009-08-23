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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ConfigurationRuntimeException;

/**
 * <p>
 * The root node in the hierarchy of flat nodes.
 * </p>
 * <p>
 * The node structure of a flat {@code ConfigurationSource} has two kinds of
 * nodes: a single root node and an arbitrary number of child nodes. This class
 * represents the root node. The root node is somewhat special. It does not have
 * a name nor a value. It is the only node in the whole structure that has
 * children.
 * </p>
 * <p>
 * The children are stored internally in two different structures: a list for
 * accessing all children (optionally with indices), and a map for accessing
 * children by name. Because standard lists and maps from the Java collection
 * framework are used, this data cannot be updated concurrently. Read-only
 * access from multiple threads is possible though.
 * </p>
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
class FlatRootNode extends FlatNode
{
    /** Constant for an empty default child node manager. */
    private static final ChildNodeManager DEF_MANAGER = new ChildNodeManager();

    /** Stores the child nodes of this root node. */
    private final List<FlatNode> children;

    /** A map for direct access to child nodes by name. */
    private final Map<String, ChildNodeManager> childrenByName;

    /**
     * Creates a new instance of {@code FlatRootNode}.
     */
    public FlatRootNode()
    {
        children = new ArrayList<FlatNode>();
        childrenByName = new HashMap<String, ChildNodeManager>();
    }

    /**
     * Creates a new child node and adds it to the list of children. This is a
     * short cut of {@code addChild(name, false)}.
     *
     * @param name the name of the child node
     * @return the newly created child node
     */
    @Override
    public FlatNode addChild(String name)
    {
        return addChild(name, false);
    }

    /**
     * Creates a new child node, adds it to the list of children, and sets its
     * {@code hasValue} flag. This implementation will create a new {@code
     * FlatLeafNode} instance.
     *
     * @param name the name of the child node
     * @param hasValue a flag whether the node already has a value; this flag
     *        impacts the behavior of the {@code setValue()} method: if it is
     *        <b>false</b>, the next {@code setValue()} call will add a new
     *        property to the configuration; otherwise an existing property
     *        value is overridden
     * @return the newly created child node
     */
    public FlatNode addChild(String name, boolean hasValue)
    {
        FlatLeafNode child = new FlatLeafNode(this, name, hasValue);
        children.add(child);
        fetchChildNodeManager(name, true).addChild(child);
        return child;
    }

    /**
     * Returns the child node with the given index.
     *
     * @param index the index (0-based)
     * @return the child node with this index
     */
    @Override
    public FlatNode getChild(int index)
    {
        return children.get(index);
    }

    /**
     * Returns a list with all child nodes of this node.
     *
     * @return a list with all child nodes
     */
    @Override
    public List<FlatNode> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    /**
     * Returns a list with all child nodes with the given name.
     *
     * @param name the name of the desired children
     * @return a list with all children with this name
     */
    @Override
    public List<FlatNode> getChildren(String name)
    {
        return fetchChildNodeManager(name, false).getChildren();
    }

    /**
     * Returns the number of children with the given name. If the name is
     * <b>null</b>, the total number of children is returned.
     *
     * @param name the desired name
     * @return the number of children with this name
     */
    @Override
    public int getChildrenCount(String name)
    {
        if (name == null)
        {
            return children.size();
        }

        else
        {
            return fetchChildNodeManager(name, false).count();
        }
    }

    /**
     * Returns the name of this node. A root node does not have a name.
     *
     * @return the name of this node
     */
    @Override
    public String getName()
    {
        return null;
    }

    /**
     * Returns the parent of this node. A root node does not have a parent.
     *
     * @return the parent node
     */
    @Override
    public FlatNode getParent()
    {
        return null;
    }

    /**
     * Returns the value of this node. The root node does not have a value, so
     * result is always <b>null</b>.
     *
     * @param config the associated {@code ConfigurationSource}
     * @return the value of this node
     */
    @Override
    public Object getValue(ConfigurationSource config)
    {
        return null;
    }

    /**
     * Returns the index of the associated values. For root nodes, this is an
     * undefined index.
     *
     * @return the value index
     */
    @Override
    public int getValueIndex()
    {
        return INDEX_UNDEFINED;
    }

    /**
     * Removes the specified child node. The corresponding value in the
     * associated {@code ConfigurationSource} will also be removed.
     *
     * @param config the associated {@code ConfigurationSource}
     * @param child the node to be removed
     * @throws ConfigurationRuntimeException if this node is not a child of this
     *         node
     */
    @Override
    public void removeChild(ConfigurationSource config, FlatNode child)
    {
        if (child != null)
        {
            ChildNodeManager cnm = fetchChildNodeManager(child.getName(), false);
            int index = cnm.getValueIndex(child);
            cnm.removeChild(child);
            children.remove(child);

            if (index != INDEX_UNDEFINED)
            {
                changeMultiProperty(config, child, index, null, true);
            }
            else
            {
                config.clearProperty(child.getName());
            }
        }
        else
        {
            // child was not found
            throw new ConfigurationRuntimeException(
                    "Cannot remove null child node!");
        }
    }

    /**
     * Sets the value of this node. A root node cannot have a value. This
     * implementation will throw an exception.
     *
     * @param config the associated {@code ConfigurationSource}
     * @param value the new value
     * @throws ConfigurationRuntimeException if the value cannot be set
     */
    @Override
    public void setValue(ConfigurationSource config, Object value)
    {
        throw new ConfigurationRuntimeException(
                "Cannot set the value of the root node of a flat configuration!");
    }

    /**
     * Returns the value index for the specified child node. This method is used
     * to determine the index of the value of a property with multiple values
     * that corresponds to the given child node. It counts the occurrences of
     * child nodes with the same name as the given child node.
     *
     * @param child the child node
     * @return the value index for this child node
     */
    int getChildValueIndex(FlatNode child)
    {
        return fetchChildNodeManager(child.getName(), false).getValueIndex(
                child);
    }

    /**
     * Changes the value of a property with multiple values. This method is
     * called when the value of a child node was changed that occurs multiple
     * times. It obtains the list with all values for this property, changes the
     * value with the given index, and sets the new value.
     *
     * @param config the current {@code ConfigurationSource}
     * @param child the child node that was changed
     * @param index the value index of this child node
     * @param value the new value
     */
    void setMultiProperty(ConfigurationSource config, FlatNode child,
            int index, Object value)
    {
        changeMultiProperty(config, child, index, value, false);
    }

    /**
     * Obtains the {@code ChildNodeManager} for the child nodes with the given
     * name. This implementation looks up the {@code ChildNodeManager} in an
     * internal map. If the manager cannot be found, the behavior depends on the
     * {@code create} parameter: If set to <b>true</b>, a new {@code
     * ChildNodeManager} instance is created and added to the map. Otherwise an
     * empty default manager is returned.
     *
     * @param name the name of the child node
     * @param create the create flag
     * @return the {@code ChildNodeManager} for these child nodes
     */
    private ChildNodeManager fetchChildNodeManager(String name, boolean create)
    {
        ChildNodeManager cnm = childrenByName.get(name);

        if (cnm == null)
        {
            if (create)
            {
                cnm = new ChildNodeManager();
                childrenByName.put(name, cnm);
            }
            else
            {
                return DEF_MANAGER;
            }
        }

        return cnm;
    }

    /**
     * Helper method for manipulating a property with multiple values. A value
     * at a given index can either be changed or removed. If the index is
     * invalid, no change is performed.
     *
     * @param config the current {@code ConfigurationSource}
     * @param child the child node that was changed
     * @param index the value index of this child node
     * @param value the new value
     * @param remove a flag whether the value at the index is to be removed
     */
    private static void changeMultiProperty(ConfigurationSource config,
            FlatNode child, int index, Object value, boolean remove)
    {
        Object val = config.getProperty(child.getName());
        if (val instanceof Collection<?>)
        {
            Collection<?> col = (Collection<?>) val;
            if (col.size() > index)
            {
                List<Object> newValues = new ArrayList<Object>(col);
                if (remove)
                {
                    newValues.remove(index);
                }
                else
                {
                    newValues.set(index, value);
                }

                Object newValue = (newValues.size() > 1) ? newValues
                        : newValues.get(0);
                config.setProperty(child.getName(), newValue);
            }
        }
    }

    /**
     * A helper class for managing all child nodes of a specific name. This
     * class provides basic CRUD operations for child nodes. A {@code
     * FlatRootNode} holds a map that associates the names of child nodes with
     * instances of this class. An instance can then be used for manipulating
     * the child nodes with this name.
     */
    private static class ChildNodeManager
    {
        /** Holds the single child node with this name. */
        private FlatNode child;

        /** Holds all child nodes with this name. */
        private List<FlatNode> childNodes;

        /**
         * Adds the given child node to this manager.
         *
         * @param node the node to add
         */
        public void addChild(FlatNode node)
        {
            if (childNodes != null)
            {
                childNodes.add(node);
            }
            else if (child != null)
            {
                childNodes = new LinkedList<FlatNode>();
                childNodes.add(child);
                childNodes.add(node);
                child = null;
            }
            else
            {
                child = node;
            }
        }

        /**
         * Removes the given child node. If the return value is <b>false</b>,
         * there are no more children with this name, and this instance can be
         * removed.
         *
         * @param node the node to remove
         * @return a flag whether there are remaining nodes with this name
         * @throws ConfigurationRuntimeException if the child is unknown
         */
        public boolean removeChild(FlatNode node)
        {
            boolean found = false;

            if (node == child)
            {
                child = null;
                return false;
            }

            if (childNodes != null)
            {
                for (Iterator<FlatNode> it = childNodes.iterator(); it
                        .hasNext();)
                {
                    FlatNode n = it.next();
                    if (n == node)
                    {
                        it.remove();
                        found = true;
                        break;
                    }
                }

                if (childNodes.size() == 1)
                {
                    child = childNodes.get(0);
                    childNodes = null;
                }
            }

            if (!found)
            {
                throw new ConfigurationRuntimeException(
                        "Node to remove is no child of this node!");
            }
            return true;
        }

        /**
         * Returns the number of child nodes with this name.
         *
         * @return the number of child nodes with this name
         */
        public int count()
        {
            if (childNodes != null)
            {
                return childNodes.size();
            }
            else
            {
                return (child != null) ? 1 : 0;
            }
        }

        /**
         * Returns a list with all child nodes managed by this object.
         *
         * @return a list with all child nodes
         */
        public List<FlatNode> getChildren()
        {
            if (childNodes != null)
            {
                return Collections.unmodifiableList(childNodes);
            }
            if (child != null)
            {
                return Collections.singletonList(child);
            }
            return Collections.emptyList();
        }

        /**
         * Returns the value index for the specified child node.
         *
         * @param node the child node
         * @return the value index for this node
         */
        public int getValueIndex(FlatNode node)
        {
            if (childNodes != null)
            {
                int index = 0;
                for (FlatNode n : childNodes)
                {
                    if (n == node)
                    {
                        return index;
                    }
                    index++;
                }
            }

            return INDEX_UNDEFINED;
        }
    }
}
