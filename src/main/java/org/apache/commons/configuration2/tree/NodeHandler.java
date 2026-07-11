/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration2.tree;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * Definition of an interface for accessing the data of a configuration node.
 * </p>
 * <p>
 * Hierarchical configurations can deal with arbitrary node structures. In order to obtain information about a specific
 * node object, a so-called {@code NodeHandler} is used. The handler provides a number of methods for querying the
 * internal state of a node in a read-only way.
 * </p>
 *
 * @param <T> The type of the nodes this handler deals with
 */
public interface NodeHandler<T> {

    /**
     * Gets an unmodifiable set with the names of all attributes of the specified node.
     *
     * @param node The node
     * @return A set with the names of all attributes of this node
     */
    Set<String> getAttributes(T node);

    /**
     * Gets the value of the specified attribute from the given node. If a concrete {@code NodeHandler} supports
     * attributes with multiple values, result might be a collection.
     *
     * @param node The node
     * @param name The name of the attribute
     * @return The value of this attribute
     */
    Object getAttributeValue(T node, String name);

    /**
     * Gets the child with the given index of the specified node.
     *
     * @param node The node
     * @param index The index (0-based)
     * @return The child with the given index
     */
    T getChild(T node, int index);

    /**
     * Gets an unmodifiable list with all children of the specified node.
     *
     * @param node The node
     * @return A list with the child nodes of this node
     */
    List<T> getChildren(T node);

    /**
     * Gets an unmodifiable list of all children of the specified node with the given name.
     *
     * @param node The node
     * @param name The name of the desired child nodes
     * @return A list with all children with the given name
     */
    List<T> getChildren(T node, String name);

    /**
     * Gets the number of children of the specified node with the given name. This method exists for performance reasons:
     * for some node implementations it may be by far more efficient to count the children than to query a list of all
     * children and determine its size. A concrete implementation can choose the most efficient way to determine the number
     * of children. If a child name is passed in, only the children with this name are taken into account. If the name
     * <strong>null</strong> is passed, the total number of children must be returned.
     *
     * @param node The node
     * @param name The name of the children in question (can be <strong>null</strong> for all children)
     * @return The number of the selected children
     */
    int getChildrenCount(T node, String name);

    /**
     * Gets an unmodifiable list of all children of the specified node which are matched by the passed in
     * {@code NodeMatcher} against the provided criterion. This method allows for advanced queries on a node's children.
     *
     * @param node The node
     * @param matcher The {@code NodeMatcher} defining filter criteria
     * @param criterion The criterion to be matched against; this object is passed to the {@code NodeMatcher}
     * @param <C> The type of the criterion
     * @return A list with all children matched by the matcher
     */
    <C> List<T> getMatchingChildren(T node, NodeMatcher<C> matcher, C criterion);

    /**
     * Gets the number of children of the specified node which are matched by the given {@code NodeMatcher}. This is a
     * more generic version of {@link #getChildrenCount(Object, String)}. It allows checking for arbitrary filter
     * conditions.
     *
     * @param node The node
     * @param matcher The {@code NodeMatcher}
     * @param criterion The criterion to be passed to the {@code NodeMatcher}
     * @param <C> The type of the criterion
     * @return The number of matched children
     */
    <C> int getMatchingChildrenCount(T node, NodeMatcher<C> matcher, C criterion);

    /**
     * Gets the parent of the specified node.
     *
     * @param node The node
     * @return The parent node
     */
    T getParent(T node);

    /**
     * Gets the root node of the underlying hierarchy.
     *
     * @return The current root node
     */
    T getRootNode();

    /**
     * Gets the value of the specified node.
     *
     * @param node The node
     * @return The value of this node
     */
    Object getValue(T node);

    /**
     * Returns a flag whether the passed in node has any attributes.
     *
     * @param node The node
     * @return A flag whether this node has any attributes
     */
    boolean hasAttributes(T node);

    /**
     * Returns the index of the given child node in the list of children of its parent. This method is the opposite
     * operation of {@link #getChild(Object, int)}. This method returns 0 if the given node is the first child node with
     * this name, 1 for the second child node and so on. If the node has no parent node or if it is an attribute, -1 is
     * returned.
     *
     * @param parent The parent node
     * @param child A child node whose index is to be retrieved
     * @return The index of this child node
     */
    int indexOfChild(T parent, T child);

    /**
     * Checks whether the specified node is defined. Nodes are &quot;defined&quot; if they contain any data, for example a value,
     * or attributes, or defined children.
     *
     * @param node The node to test
     * @return A flag whether the passed in node is defined
     */
    boolean isDefined(T node);

    /**
     * Returns the name of the specified node
     *
     * @param node The node
     * @return The name of this node
     */
    String nodeName(T node);
}
