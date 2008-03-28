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
package org.apache.commons.configuration2.combined;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.expr.NodeHandler;

/**
 * <p>
 * A specialized node implementation to be used in combined configurations.
 * </p>
 * <p>
 * Some configurations provide a logical view on the nodes of other
 * configurations. These configurations construct their own hierarchy of nodes
 * based on the node trees of their source configurations. This special node
 * class can be used for this purpose. It allows child nodes and attributes to
 * be added without changing their parent node. So a node can belong to a
 * hierarchy of nodes of a source configuration, but be also contained in a
 * combined configuration.
 * </p>
 * <p>
 * Implementation note: This class is intended to be used internally only. So
 * checks for the validity of passed in parameters are limited.
 * </p>
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 * @since 2.0
 */
public class CombinedNode
{
    /** Stores the parent node. */
    private CombinedNode parent;

    /** Stores the name of this node. */
    private String name;

    /** Stores the value of this node. */
    private Object value;

    /** A list with the child nodes. */
    private List<ChildData> children;

    /** Stores the attributes of this node. */
    private Map<String, AttributeData> attributes;

    /**
     * Creates a new, empty instance of <code>CombinedNode</code>.
     */
    public CombinedNode()
    {
        this(null, null, null);
    }

    /**
     * Creates a new instance of <code>CombinedNode</code> and initializes it.
     *
     * @param parent the parent node
     * @param name the name
     * @param value the value
     */
    public CombinedNode(CombinedNode parent, String name, Object value)
    {
        setParent(parent);
        setName(name);
        setValue(value);
        children = new ArrayList<ChildData>();
        attributes = new LinkedHashMap<String, AttributeData>();
    }

    /**
     * Creates a new instance of <code>CombinedNode</code> and sets the name.
     *
     * @param name the name
     */
    public CombinedNode(String name)
    {
        this(null, name, null);
    }

    /**
     * Returns the parent node of this combined node.
     *
     * @return the parent node
     */
    public CombinedNode getParent()
    {
        return parent;
    }

    /**
     * Sets the parent node.
     *
     * @param parent the new parent node
     */
    public void setParent(CombinedNode parent)
    {
        this.parent = parent;
    }

    /**
     * Returns the name of this node.
     *
     * @return the name
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
        this.name = name;
    }

    /**
     * Returns the value of this node.
     *
     * @return the value
     */
    public Object getValue()
    {
        return value;
    }

    /**
     * Sets the value of this node.
     *
     * @param value the new value
     */
    public void setValue(Object value)
    {
        this.value = value;
    }

    /**
     * Adds a new child node to this view node. The child can be an arbitrary
     * node that may be part of the node structure of a different configuration.
     *
     * @param name the name of the child node
     * @param child the new child node
     */
    public void addChild(String name, Object child)
    {
        children.add(new ChildData(name, child));
    }

    /**
     * Removes the specified child node from this view node.
     *
     * @param child the child to be removed
     */
    public void removeChild(Object child)
    {
        for (int idx = 0; idx < children.size(); idx++)
        {
            if (children.get(idx).node.equals(child))
            {
                children.remove(idx);
                return;
            }
        }
    }

    /**
     * Returns the child with the given index.
     *
     * @param idx the index
     * @return the child with this index
     */
    public Object getChild(int idx)
    {
        return children.get(idx).node;
    }

    /**
     * Returns a list with all child nodes of this combined node.
     *
     * @return a list with all child nodes
     */
    public List<Object> getChildren()
    {
        List<Object> result = new ArrayList<Object>(children.size());
        for (ChildData cd : children)
        {
            result.add(cd.node);
        }
        return result;
    }

    /**
     * Returns a list with all child nodes of this combined node that have the given
     * name.
     *
     * @param name the desired name
     * @return a list with the children with this name
     */
    public List<Object> getChildren(String name)
    {
        List<Object> result = new ArrayList<Object>();
        for (ChildData cd : children)
        {
            if (cd.name.equals(name))
            {
                result.add(cd.node);
            }
        }
        return result;
    }

    /**
     * Returns the number of children with the given name. If <b>null</b> is
     * passed for the name, the number of all children is returned.
     *
     * @param name the name of the children in question
     * @return the number of the child nodes with this name
     */
    public int getChildrenCount(String name)
    {
        if (name == null)
        {
            return children.size();
        }

        int count = 0;
        for (ChildData cd : children)
        {
            if (cd.name.equals(name))
            {
                count++;
            }
        }

        return count;
    }

    /**
     * Sets the value of the specified attribute.
     *
     * @param name the name of the attribute
     * @param value the new value
     */
    public void setAttribute(String name, Object value)
    {
        attributes.put(name, new AttributeData(value));
    }

    /**
     * Adds another value to the specified attribute. With this method an
     * attribute can be assigned multiple values. If the attribute does not
     * exist, it is created.
     *
     * @param name the name of the attribute
     * @param value the value to be added
     */
    public void addAttributeValue(String name, Object value)
    {
        AttributeData ad = attributes.get(name);
        if (ad == null)
        {
            setAttribute(name, value);
        }
        else
        {
            ad.addValue(value);
        }
    }

    /**
     * Returns a list with the names of all existing attributes.
     *
     * @return a list with the names of the attributes
     */
    public List<String> getAttributes()
    {
        return new ArrayList<String>(attributes.keySet());
    }

    /**
     * Returns a flag whether this node has any attributes.
     *
     * @return a flag whether this node has attributes
     */
    public boolean hasAttributes()
    {
        return !attributes.isEmpty();
    }

    /**
     * Returns the value of the specified attribute. If the attribute does not
     * exist, <b>null</b> is returned. If the attribute has multiple values,
     * result is a collection.
     *
     * @param name the name of the attribute
     * @return the value of this attribute
     */
    public Object getAttribute(String name)
    {
        AttributeData ad = attributes.get(name);
        return (ad != null) ? ad.getValue() : null;
    }

    /**
     * Removes the attribute with the specified name.
     *
     * @param name the name of the affected attribute
     */
    public void removeAttribute(String name)
    {
        attributes.remove(name);
    }

    /**
     * Appends all attributes of the specified node to this combined node.
     *
     * @param <T> the type of the affected node
     * @param node the source node
     * @param handler the handler for the source node
     */
    public <T> void appendAttributes(T node, NodeHandler<T> handler)
    {
        for (String attrName : handler.getAttributes(node))
        {
            addAttributeValue(attrName, handler.getAttributeValue(node,
                    attrName));
        }
    }

    /**
     * Appends all children of the specified node to this combined node.
     *
     * @param <T> the type of the affected node
     * @param node the source node
     * @param handler the handler for the source node
     */
    public <T> void appendChildren(T node, NodeHandler<T> handler)
    {
        for (T child : handler.getChildren(node))
        {
            addChild(handler.nodeName(child), child);
        }
    }

    /**
     * A data class for storing information about a child node.
     */
    private static class ChildData
    {
        /** The node name. */
        String name;

        /** The child node. */
        Object node;

        /**
         * Creates a new instance of <code>ChildData</code>.
         *
         * @param n the name
         * @param nd the node
         */
        public ChildData(String n, Object nd)
        {
            name = n;
            node = nd;
        }
    }

    /**
     * A data class for storing the value(s) of an attribute.
     */
    private static class AttributeData
    {
        /** Stores the single value of the attribute. */
        private Object value;

        /** Stores a collection of values if there are multiple. */
        private List<Object> values;

        /**
         * Creates a new instance of <code>AttributeData</code> and sets the
         * initial value.
         *
         * @param v the value (either a single value or a collection)
         */
        public AttributeData(Object v)
        {
            if (v instanceof Collection)
            {
                values = new ArrayList<Object>((Collection<?>) v);
            }
            else
            {
                value = v;
            }
        }

        /**
         * Returns the value. Depending on the number of values either a list or
         * a single object is returned.
         *
         * @return the value of this attribute
         */
        public Object getValue()
        {
            return (values != null) ? values : value;
        }

        /**
         * Adds another value to this attribute.
         *
         * @param v the new value
         */
        public void addValue(Object v)
        {
            if (values == null)
            {
                values = new ArrayList<Object>();
                values.add(value);
            }

            if (v instanceof Collection)
            {
                values.addAll((Collection<?>) v);
            }
            else
            {
                values.add(v);
            }
        }
    }
}
