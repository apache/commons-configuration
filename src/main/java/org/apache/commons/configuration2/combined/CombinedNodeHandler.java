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

import java.util.List;

import org.apache.commons.configuration2.expr.NodeHandler;

/**
 * <p>
 * A <code>NodeHandler</code> implementation for dealing with
 * <code>CombinedNode</code> objects.
 * </p>
 * <p>
 * This node handler is used by a <code>CombinedConfiguration</code> for
 * manipulating <code>{@link CombinedNode}</code> instances that are part of
 * their node structure.
 * </p>
 * <p>
 * Note that the type parameter in the <code>NodeHandler</code> interface is
 * set to object. This is because a <code>CombinedNodeHandler</code> typically has
 * to deal with different types of nodes at the same time. The main purpose of a
 * <code>CombinedNode</code> is to combine the node structures of arbitrary source
 * configurations. So for instance the children of a <code>CombinedNode</code> can
 * be of an arbitrary type. Nevertheless do most methods expect a
 * <code>CombinedNode</code> object in their <code>node</code> parameter and
 * will throw an <code>IllegalArgumentException</code> exception if an
 * incompatible object is passed in.
 * </p>
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 * @since 2.0
 */
public class CombinedNodeHandler implements NodeHandler<Object>
{
    /**
     * Adds another value to an attribute of the specified combined node.
     *
     * @param node the combined node
     * @param name the name of the attribute
     * @param value the value to add
     */
    public void addAttributeValue(Object node, String name, Object value)
    {
        node(node).addAttributeValue(name, value);
    }

    /**
     * Adds a new child to the specified combined node. This implementation creates
     * another <code>CombinedNode</code> instance with the given name and adds it
     * as new child to the specified parent node.
     *
     * @param node the combined node
     * @param name the name of the new child node
     * @return the newly created child node
     */
    public Object addChild(Object node, String name)
    {
        CombinedNode vn = new CombinedNode(name);
        vn.setParent(node(node));
        node(node).addChild(name, vn);
        return vn;
    }

    /**
     * Returns the value of the specified attribute of the given combined node.
     *
     * @param node the combined node
     * @param name the name of the attribute in question
     * @return the value of this attribute
     */
    public Object getAttributeValue(Object node, String name)
    {
        return node(node).getAttribute(name);
    }

    /**
     * Returns a list with the names of the attributes of the given combined node.
     *
     * @param node the combined node
     * @return a list with the names of the defined attributes
     */
    public List<String> getAttributes(Object node)
    {
        return node(node).getAttributes();
    }

    /**
     * Returns the child with the given index.
     *
     * @param node the combined node
     * @param index the index
     * @return the child with this index
     */
    public Object getChild(Object node, int index)
    {
        return node(node).getChild(index);
    }

    /**
     * Returns a list with all children of the specified node.
     *
     * @param node the combined node
     * @return a list with the child nodes of this combined node
     */
    public List<Object> getChildren(Object node)
    {
        return node(node).getChildren();
    }

    /**
     * Returns a list with all children of the specified node with the given
     * name.
     *
     * @param node the combined node
     * @param name the name of the desired children
     * @return a list with all children with this name
     */
    public List<Object> getChildren(Object node, String name)
    {
        return node(node).getChildren(name);
    }

    /**
     * Returns the number of the selected children of the specified combined node.
     *
     * @param node the combined node
     * @param name the name of the children in question
     * @return the number of the selected children
     */
    public int getChildrenCount(Object node, String name)
    {
        return node(node).getChildrenCount(name);
    }

    /**
     * Returns the parent of the specified node.
     *
     * @param node the combined node
     * @return the parent node
     */
    public Object getParent(Object node)
    {
        return node(node).getParent();
    }

    /**
     * Returns the value of the given node.
     *
     * @return the value
     */
    public Object getValue(Object node)
    {
        return node(node).getValue();
    }

    /**
     * Tests whether the passed in node is defined. This implementation checks
     * whether the node has a value or any attributes.
     *
     * @param node the node to test
     * @return a flag whether this node is defined
     */
    public boolean isDefined(Object node)
    {
        CombinedNode vn = node(node);
        return vn.getValue() != null || vn.hasAttributes();
    }

    /**
     * Returns the name of the given node.
     *
     * @param node the combined node
     * @return the name of the node
     */
    public String nodeName(Object node)
    {
        return node(node).getName();
    }

    /**
     * Removes an attribute of the specified combined node.
     *
     * @param node the combined node
     * @param name the name of the attribute to remove
     */
    public void removeAttribute(Object node, String name)
    {
        node(node).removeAttribute(name);
    }

    /**
     * Removes the specified child from the given combined node.
     *
     * @param node the combined node
     * @param child the child to remove
     */
    public void removeChild(Object node, Object child)
    {
        node(node).removeChild(child);
    }

    /**
     * Sets the value of an attribute of the specified combined node.
     *
     * @param node the combined node
     * @param name the name of the attribute
     * @param value the new value
     */
    public void setAttributeValue(Object node, String name, Object value)
    {
        node(node).setAttribute(name, value);
    }

    /**
     * Sets the value of the given node.
     *
     * @param node the node
     * @param value the new value
     */
    public void setValue(Object node, Object value)
    {
        node(node).setValue(value);
    }

    /**
     * Converts the specified node object to a <code>CombinedNode</code>. If this
     * is not possible, an exception is thrown.
     *
     * @param node the object to convert
     * @return the converted <code>CombinedNode</code>
     * @throws IllegalArgumentException if the object is not a
     *         <code>CombinedNode</code>
     */
    private static CombinedNode node(Object node)
    {
        if (!(node instanceof CombinedNode))
        {
            throw new IllegalArgumentException("Node must be a ViewNode!");
        }

        return (CombinedNode) node;
    }
}
