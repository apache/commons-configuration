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

import java.util.List;

/**
 * <p>
 * Definition of an interface for dealing with the nodes of a hierarchical
 * configuration.
 * </p>
 * <p>
 * Different configuration implementations internally use different node
 * structures. This interface provides a generic way of dealing with these
 * structures by providing common query and manipulation methods. That way large
 * parts of the functionality required by a hierarchical configuration can be
 * implemented in a central base class, while tasks specific for a concrete node
 * structure are delegated to an implementation of this interface.
 * </p>
 *
 * @author Oliver Heger
 * @version $Id$
 * @param <T> the type of the nodes this handler deals with
 */
public interface NodeHandler<T>
{
    /**
     * Returns the name of the specified node
     *
     * @param node the node
     * @return the name of this node
     */
    String nodeName(T node);

    /**
     * Returns the value of the specified node.
     *
     * @param node the node
     * @return the value of this node
     */
    Object getValue(T node);

    /**
     * Sets the value of the specified node.
     *
     * @param node the node
     * @param value the new value
     */
    void setValue(T node, Object value);

    /**
     * Returns the parent of the specified node.
     *
     * @param node the node
     * @return the parent node
     */
    T getParent(T node);

    /**
     * Adds a child with the given node name to the specified node.
     *
     * @param node the node
     * @param name the name of the new child
     * @return the newly added child
     */
    T addChild(T node, String name);

    /**
     * Adds a child with the given node name and value to the specified parent
     * node. This method is used for creating nodes with a value while the other
     * <code>addChild()</code> method creates structure nodes (i.e. nodes that
     * may contain child nodes and attributes, but do not have a value). Some
     * concrete implementations of node handler have to distinguish between
     * these node types.
     *
     * @param node the parent node
     * @param name the name of the new child
     * @param value the value of the new child
     * @return the newly added child (this can be <b>null</b> if an
     *         implementation decides to create an attribute for the value node)
     */
    T addChild(T node, String name, Object value);

    /**
     * Returns a list with all children of the specified node.
     *
     * @param node the node
     * @return a list with the child nodes of this node
     */
    List<T> getChildren(T node);

    /**
     * Returns a list of all children of the specified node with the given name.
     *
     * @param node the node
     * @param name the name of the desired child nodes
     * @return a list with all children with the given name
     */
    List<T> getChildren(T node, String name);

    /**
     * Returns the child with the given index of the specified node.
     *
     * @param node the node
     * @param index the index (0-based)
     * @return the child with the given index
     */
    T getChild(T node, int index);

    /**
     * Returns the index of the given child node relative to its name. This
     * method will be called when a unique identifier for a specific node is
     * needed. The node name alone might not be sufficient because there may be
     * multiple child nodes with the same name. This method returns 0 if the
     * given node is the first child node with this name, 1 for the second child
     * node and so on. If the node has no parent node or if it is an attribute,
     * -1 will be returned.
     *
     * @param node a child node whose index is to be retrieved
     * @return the index of this child node
     */
    int indexOfChild(T node);

    /**
     * Returns the number of children of the specified node with the given name.
     * This method exists for performance reasons: for some node implementations
     * it may be by far more efficient to count the children than to query a
     * list of all children and determine its size. A concrete implementation
     * can choose the most efficient way to determine the number of children. If
     * a child name is passed in, only the children with this name are taken
     * into account. If the name <b>null</b> is passed, the total number of
     * children must be returned.
     *
     * @param node the node
     * @param name the name of the children in question (can be <b>null</b> for
     *        all children)
     * @return the number of the selected children
     */
    int getChildrenCount(T node, String name);

    /**
     * Removes the specified child from the given node.
     *
     * @param node the node
     * @param child the child to be removed
     */
    void removeChild(T node, T child);

    /**
     * Returns a list with the names of all attributes of the specified node.
     *
     * @param node the node
     * @return a list with all attributes of this node
     */
    List<String> getAttributes(T node);

    /**
     * Returns a flag whether the passed in node has any attributes.
     *
     * @param node the node
     * @return a flag whether this node has any attributes
     */
    boolean hasAttributes(T node);

    /**
     * Returns the value of the specified attribute from the given node. If a
     * concrete <code>NodeHandler</code> supports attributes with multiple
     * values, result might be a collection.
     *
     * @param node the node
     * @param name the name of the attribute
     * @return the value of this attribute
     */
    Object getAttributeValue(T node, String name);

    /**
     * Sets the value of an attribute for the specified node.
     *
     * @param node the parent node
     * @param name the name of the attribute
     * @param value the value of the attribute
     */
    void setAttributeValue(T node, String name, Object value);

    /**
     * Adds a value to an attribute. This method can be used to create
     * attributes with multiple values (as far as the specific
     * <code>NodeHandler</code> implementation supports this). In contrast to
     * <code>setAttributeValue()</code>, an existing attribute value is not
     * removed, but the new value is added to the existing values.
     *
     * @param node the parent node
     * @param name the name of the attribute
     * @param value the value to be added
     */
    void addAttributeValue(T node, String name, Object value);

    /**
     * Removes the attribute with the specified name from the given node.
     *
     * @param node the node
     * @param name the name of the attribute to be removed
     */
    void removeAttribute(T node, String name);

    /**
     * Checks whether the specified node is defined. Nodes are
     * &quot;defined&quot; if they contain any data, e.g. a value, or
     * attributes, or defined children.
     *
     * @param node the node to test
     * @return a flag whether the passed in node is defined
     */
    boolean isDefined(T node);

    /**
     * Initializes this <code>NodeHandler</code> with a reference to a
     * <code>{@link NodeHandlerRegistry}</code>. This method is called when
     * multiple types of configuration nodes are involved. It is especially
     * useful for complex implementations that have to deal with arbitrary
     * configuration nodes. <code>NodeHandler</code> implementations that only
     * handle a specific node type can simply ignore this method.
     *
     * @param registry a reference to a <code>NodeHandlerRegistry</code>
     */
    void initNodeHandlerRegistry(NodeHandlerRegistry registry);
}
