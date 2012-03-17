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
package org.apache.commons.configuration.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationRuntimeException;

/**
 * <p>
 * A default implementation of the {@code ConfigurationNode} interface.
 * </p>
 *
 * @since 1.3
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 */
public class DefaultConfigurationNode implements ConfigurationNode, Cloneable
{
    /** Stores the children of this node. */
    private SubNodes children;

    /** Stores the attributes of this node. */
    private SubNodes attributes;

    /** Stores a reference to this node's parent. */
    private ConfigurationNode parent;

    /** Stores the value of this node. */
    private Object value;

    /** Stores the reference. */
    private Object reference;

    /** Stores the name of this node. */
    private String name;

    /** Stores a flag if this is an attribute. */
    private boolean attribute;

    /**
     * Creates a new uninitialized instance of {@code DefaultConfigurationNode}.
     */
    public DefaultConfigurationNode()
    {
        this(null);
    }

    /**
     * Creates a new instance of {@code DefaultConfigurationNode} and
     * initializes it with the node name.
     *
     * @param name the name of this node
     */
    public DefaultConfigurationNode(String name)
    {
        this(name, null);
    }

    /**
     * Creates a new instance of {@code DefaultConfigurationNode} and
     * initializes it with the name and a value.
     *
     * @param name the node's name
     * @param value the node's value
     */
    public DefaultConfigurationNode(String name, Object value)
    {
        setName(name);
        setValue(value);
        initSubNodes();
    }

    /**
     * Returns the name of this node.
     *
     * @return the name of this node
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of this node.
     *
     * @param name the new name
     */
    public void setName(String name)
    {
        checkState();
        this.name = name;
    }

    /**
     * Returns the value of this node.
     *
     * @return the value of this node
     */
    public Object getValue()
    {
        return value;
    }

    /**
     * Sets the value of this node.
     *
     * @param val the value of this node
     */
    public void setValue(Object val)
    {
        value = val;
    }

    /**
     * Returns the reference.
     *
     * @return the reference
     */
    public Object getReference()
    {
        return reference;
    }

    /**
     * Sets the reference.
     *
     * @param reference the reference object
     */
    public void setReference(Object reference)
    {
        this.reference = reference;
    }

    /**
     * Returns a reference to this node's parent.
     *
     * @return the parent node or <b>null </b> if this is the root
     */
    public ConfigurationNode getParentNode()
    {
        return parent;
    }

    /**
     * Sets the parent of this node.
     *
     * @param parent the parent of this node
     */
    public void setParentNode(ConfigurationNode parent)
    {
        this.parent = parent;
    }

    /**
     * Adds a new child to this node.
     *
     * @param child the new child
     */
    public void addChild(ConfigurationNode child)
    {
        children.addNode(child);
        child.setAttribute(false);
        child.setParentNode(this);
    }

    /**
     * Returns a list with all children of this node.
     *
     * @return a list with all child nodes
     */
    public List<ConfigurationNode> getChildren()
    {
        return children.getSubNodes();
    }

    /**
     * Returns the number of all children of this node.
     *
     * @return the number of all children
     */
    public int getChildrenCount()
    {
        return children.getSubNodes().size();
    }

    /**
     * Returns a list of all children with the given name.
     *
     * @param name the name; can be <b>null </b>, then all children are returned
     * @return a list of all children with the given name
     */
    public List<ConfigurationNode> getChildren(String name)
    {
        return children.getSubNodes(name);
    }

    /**
     * Returns the number of children with the given name.
     *
     * @param name the name; can be <b>null </b>, then the number of all
     * children is returned
     * @return the number of child nodes with this name
     */
    public int getChildrenCount(String name)
    {
        return children.getSubNodes(name).size();
    }

    /**
     * Returns the child node with the given index.
     *
     * @param index the index (0-based)
     * @return the child with this index
     */
    public ConfigurationNode getChild(int index)
    {
        return children.getNode(index);
    }

    /**
     * Removes the specified child node from this node.
     *
     * @param child the node to be removed
     * @return a flag if a node was removed
     */
    public boolean removeChild(ConfigurationNode child)
    {
        return children.removeNode(child);
    }

    /**
     * Removes all children with the given name.
     *
     * @param childName the name of the children to be removed
     * @return a flag if at least one child node was removed
     */
    public boolean removeChild(String childName)
    {
        return children.removeNodes(childName);
    }

    /**
     * Removes all child nodes of this node.
     */
    public void removeChildren()
    {
        children.clear();
    }

    /**
     * Checks if this node is an attribute node.
     *
     * @return a flag if this is an attribute node
     */
    public boolean isAttribute()
    {
        return attribute;
    }

    /**
     * Sets the attribute flag. Note: this method can only be called if the node
     * is not already part of a node hierarchy.
     *
     * @param f the attribute flag
     */
    public void setAttribute(boolean f)
    {
        checkState();
        attribute = f;
    }

    /**
     * Adds the specified attribute to this node.
     *
     * @param attr the attribute to be added
     */
    public void addAttribute(ConfigurationNode attr)
    {
        attributes.addNode(attr);
        attr.setAttribute(true);
        attr.setParentNode(this);
    }

    /**
     * Returns a list with the attributes of this node. This list contains
     * {@code DefaultConfigurationNode} objects, too.
     *
     * @return the attribute list, never <b>null </b>
     */
    public List<ConfigurationNode> getAttributes()
    {
        return attributes.getSubNodes();
    }

    /**
     * Returns the number of attributes contained in this node.
     *
     * @return the number of attributes
     */
    public int getAttributeCount()
    {
        return attributes.getSubNodes().size();
    }

    /**
     * Returns a list with all attributes of this node with the given name.
     *
     * @param name the attribute's name
     * @return all attributes with this name
     */
    public List<ConfigurationNode> getAttributes(String name)
    {
        return attributes.getSubNodes(name);
    }

    /**
     * Returns the number of attributes of this node with the given name.
     *
     * @param name the name
     * @return the number of attributes with this name
     */
    public int getAttributeCount(String name)
    {
        return getAttributes(name).size();
    }

    /**
     * Removes the specified attribute.
     *
     * @param node the attribute node to be removed
     * @return a flag if the attribute could be removed
     */
    public boolean removeAttribute(ConfigurationNode node)
    {
        return attributes.removeNode(node);
    }

    /**
     * Removes all attributes with the specified name.
     *
     * @param name the name
     * @return a flag if at least one attribute was removed
     */
    public boolean removeAttribute(String name)
    {
        return attributes.removeNodes(name);
    }

    /**
     * Returns the attribute with the given index.
     *
     * @param index the index (0-based)
     * @return the attribute with this index
     */
    public ConfigurationNode getAttribute(int index)
    {
        return attributes.getNode(index);
    }

    /**
     * Removes all attributes of this node.
     */
    public void removeAttributes()
    {
        attributes.clear();
    }

    /**
     * Returns a flag if this node is defined. This means that the node contains
     * some data.
     *
     * @return a flag whether this node is defined
     */
    public boolean isDefined()
    {
        return getValue() != null || getChildrenCount() > 0
                || getAttributeCount() > 0;
    }

    /**
     * Visits this node and all its sub nodes.
     *
     * @param visitor the visitor
     */
    public void visit(ConfigurationNodeVisitor visitor)
    {
        if (visitor == null)
        {
            throw new IllegalArgumentException("Visitor must not be null!");
        }

        if (!visitor.terminate())
        {
            visitor.visitBeforeChildren(this);
            children.visit(visitor);
            attributes.visit(visitor);
            visitor.visitAfterChildren(this);
        }
    }

    /**
     * Creates a copy of this object. This is not a deep copy, the children are
     * not cloned.
     *
     * @return a copy of this object
     */
    @Override
    public Object clone()
    {
        try
        {
            DefaultConfigurationNode copy = (DefaultConfigurationNode) super
                    .clone();
            copy.initSubNodes();
            return copy;
        }
        catch (CloneNotSupportedException cex)
        {
            // should not happen
            throw new ConfigurationRuntimeException("Cannot clone " + getClass());
        }
    }

    /**
     * Checks if a modification of this node is allowed. Some properties of a
     * node must not be changed when the node has a parent. This method checks
     * this and throws a runtime exception if necessary.
     */
    protected void checkState()
    {
        if (getParentNode() != null)
        {
            throw new IllegalStateException(
                    "Node cannot be modified when added to a parent!");
        }
    }

    /**
     * Creates a {@code SubNodes} instance that is used for storing
     * either this node's children or attributes.
     *
     * @param attributes <b>true</b> if the returned instance is used for
     * storing attributes, <b>false</b> for storing child nodes
     * @return the {@code SubNodes} object to use
     */
    protected SubNodes createSubNodes(boolean attributes)
    {
        return new SubNodes();
    }

    /**
     * Deals with the reference when a node is removed. This method is called
     * for each removed child node or attribute. It can be overloaded in sub
     * classes, for which the reference has a concrete meaning and remove
     * operations need some update actions. This default implementation is
     * empty.
     */
    protected void removeReference()
    {
    }

    /**
     * Helper method for initializing the sub nodes objects.
     */
    private void initSubNodes()
    {
        children = createSubNodes(false);
        attributes = createSubNodes(true);
    }

    /**
     * An internally used helper class for managing a collection of sub nodes.
     */
    protected static class SubNodes
    {
        /** Stores a list for the sub nodes. */
        private List<ConfigurationNode> nodes;

        /** Stores a map for accessing subnodes by name. */
        private Map<String, List<ConfigurationNode>> namedNodes;

        /**
         * Adds a new sub node.
         *
         * @param node the node to add
         */
        public void addNode(ConfigurationNode node)
        {
            if (node == null || node.getName() == null)
            {
                throw new IllegalArgumentException(
                        "Node to add must have a defined name!");
            }
            node.setParentNode(null);  // reset, will later be set

            if (nodes == null)
            {
                nodes = new ArrayList<ConfigurationNode>();
                namedNodes = new HashMap<String, List<ConfigurationNode>>();
            }

            nodes.add(node);
            List<ConfigurationNode> lst = namedNodes.get(node.getName());
            if (lst == null)
            {
                lst = new LinkedList<ConfigurationNode>();
                namedNodes.put(node.getName(), lst);
            }
            lst.add(node);
        }

        /**
         * Removes a sub node.
         *
         * @param node the node to remove
         * @return a flag if the node could be removed
         */
        public boolean removeNode(ConfigurationNode node)
        {
            if (nodes != null && node != null && nodes.contains(node))
            {
                detachNode(node);
                nodes.remove(node);

                List<ConfigurationNode> lst = namedNodes.get(node.getName());
                if (lst != null)
                {
                    lst.remove(node);
                    if (lst.isEmpty())
                    {
                        namedNodes.remove(node.getName());
                    }
                }
                return true;
            }

            else
            {
                return false;
            }
        }

        /**
         * Removes all sub nodes with the given name.
         *
         * @param name the name
         * @return a flag if at least on sub node was removed
         */
        public boolean removeNodes(String name)
        {
            if (nodes != null && name != null)
            {
                List<ConfigurationNode> lst = namedNodes.remove(name);
                if (lst != null)
                {
                    detachNodes(lst);
                    nodes.removeAll(lst);
                    return true;
                }
            }
            return false;
        }

        /**
         * Removes all sub nodes.
         */
        public void clear()
        {
            if (nodes != null)
            {
                detachNodes(nodes);
                nodes = null;
                namedNodes = null;
            }
        }

        /**
         * Returns the node with the given index. If this index cannot be found,
         * an {@code IndexOutOfBoundException} exception will be thrown.
         *
         * @param index the index (0-based)
         * @return the sub node at the specified index
         */
        public ConfigurationNode getNode(int index)
        {
            if (nodes == null)
            {
                throw new IndexOutOfBoundsException("No sub nodes available!");
            }
            return nodes.get(index);
        }

        /**
         * Returns a list with all stored sub nodes. The return value is never
         * <b>null</b>.
         *
         * @return a list with the sub nodes
         */
        public List<ConfigurationNode> getSubNodes()
        {
            if (nodes == null)
            {
                return Collections.emptyList();
            }
            else
            {
                return Collections.unmodifiableList(nodes);
            }
        }

        /**
         * Returns a list of the sub nodes with the given name. The return value
         * is never <b>null</b>.
         *
         * @param name the name; if <b>null</b> is passed, all sub nodes will
         * be returned
         * @return all sub nodes with this name
         */
        public List<ConfigurationNode> getSubNodes(String name)
        {
            if (name == null)
            {
                return getSubNodes();
            }

            List<ConfigurationNode> result;
            if (nodes == null)
            {
                result = null;
            }
            else
            {
                result = namedNodes.get(name);
            }

            if (result == null)
            {
                return Collections.emptyList();
            }
            else
            {
                return Collections.unmodifiableList(result);
            }
        }

        /**
         * Let the passed in visitor visit all sub nodes.
         *
         * @param visitor the visitor
         */
        public void visit(ConfigurationNodeVisitor visitor)
        {
            if (nodes != null)
            {
                for (Iterator<ConfigurationNode> it = nodes.iterator(); it.hasNext()
                        && !visitor.terminate();)
                {
                    it.next().visit(visitor);
                }
            }
        }

        /**
         * This method is called whenever a sub node is removed from this
         * object. It ensures that the removed node's parent is reset and its
         * {@code removeReference()} method gets called.
         *
         * @param subNode the node to be removed
         */
        protected void detachNode(ConfigurationNode subNode)
        {
            subNode.setParentNode(null);
            if (subNode instanceof DefaultConfigurationNode)
            {
                ((DefaultConfigurationNode) subNode).removeReference();
            }
        }

        /**
         * Detaches a list of sub nodes. This method calls
         * {@code detachNode()} for each node contained in the list.
         *
         * @param subNodes the list with nodes to be detached
         */
        protected void detachNodes(Collection<? extends ConfigurationNode> subNodes)
        {
            for (ConfigurationNode nd : subNodes)
            {
                detachNode(nd);
            }
        }
    }
}
