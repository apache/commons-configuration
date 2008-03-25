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

import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;

/**
 * <p>
 * An implementation of the <code>{@link NodeHandler}</code> interface that
 * operates on <code>ConfigurationNode</code> objects.
 * </p>
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class ConfigurationNodeHandler implements NodeHandler<ConfigurationNode>
{
    /**
     * Creates a new child node with the given name and adds it to the specified
     * node.
     *
     * @param node the node
     * @param name the name of the new child
     * @return the new child node
     */
    public ConfigurationNode addChild(ConfigurationNode node, String name)
    {
        ConfigurationNode child = createNode(node, name);
        node.addChild(child);
        return child;
    }

    /**
     * Returns the value of the attribute with the given name. If the attribute
     * has exactly one value, this value is returned. If the attribute
     * is present multiple times, a collection with all values is
     * returned. If the attribute cannot be found, result is <b>null</b>.
     *
     * @param node the node
     * @param name the name of the desired attribute
     * @return the value of this attribute
     */
    public Object getAttributeValue(ConfigurationNode node, String name)
    {
        List<ConfigurationNode> attrs = node.getAttributes(name);

        if (attrs.isEmpty())
        {
            return null;
        }
        else if (attrs.size() == 1)
        {
            return attrs.get(0).getValue();
        }

        else
        {
            List<Object> result = new ArrayList<Object>(attrs.size());
            for (ConfigurationNode attr : attrs)
            {
                result.add(attr.getValue());
            }
            return result;
        }
    }

    /**
     * Returns a list with the names of the attributes of the given node.
     *
     * @param node the node
     * @return a list with the names of the existing attributes
     */
    public List<String> getAttributes(ConfigurationNode node)
    {
        List<ConfigurationNode> attrs = node.getAttributes();
        assert attrs != null : "Attribute list is null";
        List<String> names = new ArrayList<String>(attrs.size());
        for (ConfigurationNode n : attrs)
        {
            names.add(n.getName());
        }
        return names;
    }

    /**
     * Returns the child with the given index from the specified node.
     *
     * @param node the node
     * @param index the index
     * @return the child with this index
     */
    public ConfigurationNode getChild(ConfigurationNode node, int index)
    {
        return node.getChild(index);
    }

    /**
     * Returns a list with all children of the specified node.
     *
     * @param node the node
     * @return a list with the children of this node
     */
    public List<ConfigurationNode> getChildren(ConfigurationNode node)
    {
        return node.getChildren();
    }

    /**
     * Returns a list with all children of the specified node with the given
     * name.
     *
     * @param node the node
     * @param name the name of the children
     * @return a list with all children with this name
     */
    public List<ConfigurationNode> getChildren(ConfigurationNode node,
            String name)
    {
        return node.getChildren(name);
    }

    /**
     * Returns the parent of the specified node.
     *
     * @param node the node
     * @return the parent node
     */
    public ConfigurationNode getParent(ConfigurationNode node)
    {
        return node.getParentNode();
    }

    /**
     * Returns the value of the given node.
     *
     * @param node the node
     * @return the value of the node
     */
    public Object getValue(ConfigurationNode node)
    {
        return node.getValue();
    }

    /**
     * Returns the name of the specified node.
     *
     * @param node the node
     * @return the name of this node
     */
    public String nodeName(ConfigurationNode node)
    {
        return node.getName();
    }

    /**
     * Sets the value of the specified attribute. This implementation removes
     * any existing attribute values before calling <code>addAttributeValue()</code>.
     *
     * @param node the node
     * @param name the name of the attribute to set
     * @param value the new value
     */
    public void setAttributeValue(ConfigurationNode node, String name,
            Object value)
    {
        node.removeAttribute(name);
        addAttributeValue(node, name, value);
    }

    /**
     * Adds another value to an attribute. Using this method it is possible to
     * create attributes with multiple values.
     * @param node the parent node
     * @param name the name of the affected attribute
     * @param value the value to add
     */
    public void addAttributeValue(ConfigurationNode node, String name,
            Object value)
    {
        ConfigurationNode attr = createNode(node, name);
        attr.setValue(value);
        node.addAttribute(attr);
    }

    /**
     * Sets the value of the specified node.
     *
     * @param node the node
     * @param value the new value
     */
    public void setValue(ConfigurationNode node, Object value)
    {
        node.setValue(value);
    }

    /**
     * Creates a new configuration node. This method is called by
     * <code>addChild()</code> for creating the new child node. This
     * implementation returns an instance of
     * <code>DefaultConfigurationNode</code>. Derived classes may override
     * this method to create a different node implementation.
     *
     * @param parent the parent node
     * @param name the name of the new node
     * @return the newly created node
     */
    protected ConfigurationNode createNode(ConfigurationNode parent, String name)
    {
        ConfigurationNode node = new DefaultConfigurationNode(name);
        node.setParentNode(parent);
        return node;
    }

    /**
     * Removes an attribute from the given node.
     *
     * @param node the node
     * @param name the name of the attribute to be removed
     */
    public void removeAttribute(ConfigurationNode node, String name)
    {
        node.removeAttribute(name);
    }

    /**
     * Removes a child from the given node.
     *
     * @param node the node
     * @param child the child to be removed
     */
    public void removeChild(ConfigurationNode node, ConfigurationNode child)
    {
        node.removeChild(child);
    }

    /**
     * Tests whether the passed in node is defined. This implementation checks
     * whether the node has a value or any attributes.
     *
     * @param node the node to test
     * @return a flag whether this node is defined
     */
    public boolean isDefined(ConfigurationNode node)
    {
        return node.getValue() != null || !node.getAttributes().isEmpty();
    }

    /**
     * Returns the number of children of the specified node.
     *
     * @param node the node
     * @param name the name of the children to take into account or <b>null</b>
     * @return the number of children
     */
    public int getChildrenCount(ConfigurationNode node, String name)
    {
        return node.getChildrenCount(name);
    }
}
