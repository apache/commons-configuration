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
package org.apache.commons.configuration2.expr;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * A class for maintaining a list of nodes that are returned by a query.
 * </p>
 * <p>
 * This class is used by an <code>{@link ExpressionEngine}</code> to deliver
 * the results of a query. It basically implements a list that can contain nodes
 * and attributes (in the result of a query both node types can occur). There
 * are methods for querying the content of the list. It is also possible to set
 * or retrieve the values of list elements.
 * </p>
 * <p>
 * Implementation note: This class is intended for internal use by an expression
 * engine only. It is not thread-safe and does not perform any parameter
 * checking.
 * </p>
 *
 * @author Oliver Heger
 * @version $Id$
 * @param <T> the type of the nodes contained in this node list
 */
public class NodeList<T>
{
    /** Constant for an undefined index.*/
    private static final int IDX_UNDEF = -1;

    /** Stores the entries of this list. */
    private List<ListElement<T>> elements;

    /**
     * Creates a new instance of <code>NodeList</code>.
     */
    public NodeList()
    {
        elements = new ArrayList<ListElement<T>>();
    }

    /**
     * Returns the number of elements stored in this list.
     *
     * @return
     */
    public int size()
    {
        return elements.size();
    }

    /**
     * Returns a flag whether the element at the specified index is a node.
     *
     * @param index the index of the desired element
     * @return <b>true</b> if the element at this index is a node, <b>false</b>
     *         otherwise
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public boolean isNode(int index)
    {
        return element(index).isNode();
    }

    /**
     * Returns a flag whether the element at the specified index is an
     * attribute.
     *
     * @param index the index of the desired element
     * @return <b>true</b> if the element at this index is an attribute,
     *         <b>false</b> otherwise
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public boolean isAttribute(int index)
    {
        return !isNode(index);
    }

    /**
     * Returns the node at the specified index.
     *
     * @param index the index
     * @return the node at this index
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws IllegalArgumentException if the element at this index is not a
     *         node
     */
    public T getNode(int index)
    {
        if (!isNode(index))
        {
            throw new IllegalArgumentException("Element at " + index
                    + " is not a node!");
        }

        return element(index).getAssociatedNode();
    }

    /**
     * Returns the parent of the attribute at the specified index. If the
     * element at this index is not an attribute, an exception will be thrown.
     *
     * @param index the index
     * @return the parent node, to which the attribute at this index belongs
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws IllegalArgumentException if the element at this index is not an
     *         attribute
     */
    public T getAttributeParent(int index)
    {
        if (!isAttribute(index))
        {
            throw new IllegalArgumentException("Element at " + index
                    + " is not an attribute!");
        }

        return element(index).getAssociatedNode();
    }

    /**
     * Returns the name of the element at the specified index. If the element is
     * a node, the node name is returned. For an attribute the attribute name is
     * returned.
     *
     * @param index the index
     * @param handler the node handler
     * @return the name of the element at this index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public String getName(int index, NodeHandler<T> handler)
    {
        return element(index).getName(handler);
    }

    /**
     * Returns the value of the element at the specified index. This method
     * works for both nodes and attributes.
     *
     * @param index the index
     * @param handler the node handler for accessing the node data
     * @return the value of the element at this index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public Object getValue(int index, NodeHandler<T> handler)
    {
        return element(index).getValue(handler);
    }

    /**
     * Sets the value of the element at the specified index. This method works
     * for both nodes and attributes.
     *
     * @param index the index
     * @param value the new value for this element
     * @param handler the node handler for accessing the node data
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public void setValue(int index, Object value, NodeHandler<T> handler)
    {
        element(index).setValue(value, handler);
    }

    /**
     * Adds a new node to this list.
     *
     * @param node the node to be added
     */
    public void addNode(T node)
    {
        elements.add(new NodeListElement<T>(node));
    }

    /**
     * Adds a new attribute to this list.
     *
     * @param parent the parent node of this attribute
     * @param name the name of this attribute
     */
    public void addAttribute(T parent, String name)
    {
        addAttribute(parent, name, IDX_UNDEF);
    }

    /**
     * Adds a new attribute to this list and selects a specific value. This
     * method is used for attributes with multiple values if a specific value is
     * selected.
     *
     * @param parent the parent node of the attribute
     * @param name the name of the attribute
     * @param index the index of the attribute's value
     */
    public void addAttribute(T parent, String name, int index)
    {
        elements.add(new AttributeListElement<T>(parent, name, index));
    }

    /**
     * Returns the list element with the given index.
     *
     * @param index the index
     * @return the list element with this index
     */
    private ListElement<T> element(int index)
    {
        return elements.get(index);
    }

    /**
     * A simple data class for managing list elements. This is an abstract base
     * class. There will be concrete subclasses for nodes and attributes.
     */
    private static abstract class ListElement<T>
    {
        /** Stores the involved node. */
        private T node;

        /**
         * Creates a new instance of <code>ListElement</code> and sets the
         * node associated with this list element.
         *
         * @param nd the associated node
         */
        protected ListElement(T nd)
        {
            node = nd;
        }

        /**
         * Returns a flag whether this element represents a node.
         *
         * @return a flag whether this is a node
         */
        public abstract boolean isNode();

        /**
         * Returns the name of this list element.
         *
         * @param handler the node handler
         * @return the name of this list element
         */
        public abstract String getName(NodeHandler<T> handler);

        /**
         * Obtains the value from this list element.
         *
         * @param handler the node handler
         * @return the value of this element
         */
        public abstract Object getValue(NodeHandler<T> handler);

        /**
         * Sets the value of this list element.
         *
         * @param value the new value
         * @param handler the node handler
         */
        public abstract void setValue(Object value, NodeHandler<T> handler);

        /**
         * Returns the node associated with this list element.
         *
         * @return the associated node
         */
        protected T getAssociatedNode()
        {
            return node;
        }
    }

    /**
     * A data class for representing node list elements.
     */
    private static class NodeListElement<T> extends ListElement<T>
    {
        /**
         * Creates a new instance of <code>NodeListElement</code> and sets the
         * represented node.
         *
         * @param nd the node
         */
        public NodeListElement(T nd)
        {
            super(nd);
        }

        /**
         * Returns the value of the represented node.
         *
         * @param handler the node handler
         * @return the node value
         */
        @Override
        public Object getValue(NodeHandler<T> handler)
        {
            return handler.getValue(getAssociatedNode());
        }

        /**
         * Returns a flag whether this element is a node. This is the case.
         *
         * @return a flag if this is a node
         */
        @Override
        public boolean isNode()
        {
            return true;
        }

        /**
         * Sets the value of the represented node.
         *
         * @param value the new value
         * @param handler the node handler
         */
        @Override
        public void setValue(Object value, NodeHandler<T> handler)
        {
            handler.setValue(getAssociatedNode(), value);
        }

        /**
         * Returns the name of the represented node.
         *
         * @param handler the node handler
         * @return the name of this node
         */
        @Override
        public String getName(NodeHandler<T> handler)
        {
            return handler.nodeName(getAssociatedNode());
        }
    }

    /**
     * A data class for representing attribute list elements.
     */
    private static class AttributeListElement<T> extends ListElement<T>
    {
        /** Stores the name of the attribute. */
        private String name;

        /** Stores the index of the value.*/
        private int index;

        /**
         * Creates a new instance of <code>AttributeListElement</code> and
         * initializes it.
         *
         * @param nd the parent node
         * @param attrName the name of the attribute
         * @param idx the index of the value
         */
        public AttributeListElement(T nd, String attrName, int idx)
        {
            super(nd);
            name = attrName;
            index = idx;
        }

        /**
         * Returns the value of the represented attribute. If an index is
         * defined, this method checks whether there are multiple values. In
         * this case the value with the given index is returned.
         *
         * @param handler the node handler
         * @return the value of this attribute
         */
        @Override
        public Object getValue(NodeHandler<T> handler)
        {
            Object value = handler.getAttributeValue(getAssociatedNode(), name);

            if(index != IDX_UNDEF && value instanceof List)
            {
                List<?> valList = (List<?>) value;
                if(index < valList.size())
                {
                    value = valList.get(index);
                }
            }

            return value;
        }

        /**
         * Returns a flag whether this element is a node. This is not the case.
         *
         * @return a flag if this is a node
         */
        @Override
        public boolean isNode()
        {
            return false;
        }

        /**
         * Sets the value of the represented attribute.
         *
         * @param value the new value
         * @param handler the node handler
         */
        @Override
        public void setValue(Object value, NodeHandler<T> handler)
        {
            handler.setAttributeValue(getAssociatedNode(), name, value);
        }

        /**
         * Returns the name of the represented attribute.
         *
         * @param handler the node handler
         * @return the name of this attribute
         */
        @Override
        public String getName(NodeHandler<T> handler)
        {
            return name;
        }
    }
}
