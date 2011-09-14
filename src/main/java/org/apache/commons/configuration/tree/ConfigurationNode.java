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

import java.util.List;

/**
 * <p>
 * Definition of an interface for the nodes of a hierarchical configuration.
 * </p>
 * <p>
 * This interface defines a tree like structure for configuration data. A node
 * has a value and can have an arbitrary number of children and attributes.
 * </p>
 *
 * @since 1.3
 * @author Oliver Heger
 * @version $Id$
 */
public interface ConfigurationNode
{
    /**
     * Returns the name of this node.
     *
     * @return the node name
     */
    String getName();

    /**
     * Sets the name of this node.
     *
     * @param name the node name
     */
    void setName(String name);

    /**
     * Returns the value of this node.
     *
     * @return the node's value
     */
    Object getValue();

    /**
     * Sets the value of this node.
     *
     * @param val the node's value
     */
    void setValue(Object val);

    /**
     * Returns this node's reference.
     *
     * @return the reference
     */
    Object getReference();

    /**
     * Sets this node's reference. This reference can be used by concrete
     * Configuration implementations to store data associated with each node. A
     * XML based configuration for instance could here store a reference to the
     * corresponding DOM element.
     *
     * @param ref the reference
     */
    void setReference(Object ref);

    /**
     * Returns this node's parent. Can be <b>null</b>, then this node is the
     * top level node.
     *
     * @return the parent of this node
     */
    ConfigurationNode getParentNode();

    /**
     * Sets the parent of this node.
     *
     * @param parent the parent of this node
     */
    void setParentNode(ConfigurationNode parent);

    /**
     * Adds a child to this node.
     *
     * @param node the new child
     */
    void addChild(ConfigurationNode node);

    /**
     * Returns a list with the child nodes of this node. The nodes in this list
     * should be in the order they were inserted into this node.
     *
     * @return a list with the children of this node (never <b>null</b>)
     */
    List getChildren();

    /**
     * Returns the number of this node's children.
     *
     * @return the number of the children of this node
     */
    int getChildrenCount();

    /**
     * Returns a list with all children of this node with the given name.
     *
     * @param name the name of the searched children
     * @return a list with all child nodes with this name (never <b>null</b>)
     */
    List getChildren(String name);

    /**
     * Returns the number of children with the given name.
     *
     * @param name the name
     * @return the number of children with this name
     */
    int getChildrenCount(String name);

    /**
     * Returns the child node with the given index. If the index does not
     * exist, an exception will be thrown.
     * @param index the index of the child node (0-based)
     * @return the child node with this index
     */
    ConfigurationNode getChild(int index);

    /**
     * Removes the given node from this node's children.
     *
     * @param child the child node to be removed
     * @return a flag if the node could be removed
     */
    boolean removeChild(ConfigurationNode child);

    /**
     * Removes all child nodes of this node with the given name.
     *
     * @param childName the name of the children to be removed
     * @return a flag if at least one child was removed
     */
    boolean removeChild(String childName);

    /**
     * Removes all children from this node.
     */
    void removeChildren();

    /**
     * Returns a flag whether this node is an attribute.
     *
     * @return a flag whether this node is an attribute
     */
    boolean isAttribute();

    /**
     * Sets a flag whether this node is an attribute.
     *
     * @param f the attribute flag
     */
    void setAttribute(boolean f);

    /**
     * Returns a list with this node's attributes. Attributes are also modeled
     * as <code>ConfigurationNode</code> objects.
     *
     * @return a list with the attributes
     */
    List getAttributes();

    /**
     * Returns the number of attributes of this node.
     * @return the number of attributes
     */
    int getAttributeCount();

    /**
     * Returns a list with the attribute nodes with the given name. Attributes
     * with same names can be added multiple times, so the return value of this
     * method is a list.
     *
     * @param name the name of the attribute
     * @return the attribute nodes with this name (never <b>null</b>)
     */
    List getAttributes(String name);

    /**
     * Returns the number of attributes with the given name.
     *
     * @param name the name of the attribute
     * @return the number of attributes with this name
     */
    int getAttributeCount(String name);

    /**
     * Returns the attribute node with the given index. If no such index exists,
     * an exception will be thrown.
     * @param index the index
     * @return the attribute node with this index
     */
    ConfigurationNode getAttribute(int index);

    /**
     * Removes the specified attribute from this node.
     *
     * @param node the attribute to remove
     * @return a flag if the node could be removed
     */
    boolean removeAttribute(ConfigurationNode node);

    /**
     * Removes all attributes with the given name.
     *
     * @param name the name of the attributes to be removed
     * @return a flag if at least one attribute was removed
     */
    boolean removeAttribute(String name);

    /**
     * Removes all attributes of this node.
     */
    void removeAttributes();

    /**
     * Adds the specified attribute to this node
     *
     * @param attr the attribute node
     */
    void addAttribute(ConfigurationNode attr);

    /**
     * Returns a flag if this node is defined. This means that the node contains
     * some data.
     *
     * @return a flag whether this node is defined
     */
    boolean isDefined();

    /**
     * Visits this node and all its sub nodes. This method provides a simple
     * means for going through a hierarchical structure of configuration nodes.
     *
     * @see ConfigurationNodeVisitor
     * @param visitor the visitor
     */
    void visit(ConfigurationNodeVisitor visitor);

    /**
     * Returns a copy of this node.
     * @return the copy
     */
    Object clone();
}
