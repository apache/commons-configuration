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
package org.apache.commons.configuration2.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * A simple data class used by {@link ExpressionEngine} to store
 * the results of the {@code prepareAdd()} operation.
 * </p>
 * <p>
 * If a new property is to be added to a configuration, the affected
 * {@code Configuration} object must know, where in its hierarchy of
 * configuration nodes new elements have to be added. This information is
 * obtained by an {@code ExpressionEngine} object that interprets the key
 * of the new property. This expression engine will pack all information
 * necessary for the configuration to perform the add operation in an instance
 * of this class.
 * </p>
 * <p>
 * Information managed by this class contains:
 * </p>
 * <ul>
 * <li>the configuration node, to which new elements must be added</li>
 * <li>the name of the new node</li>
 * <li>whether the new node is a child node or an attribute node</li>
 * <li>if a whole branch is to be added at once, the names of all nodes between
 * the parent node (the target of the add operation) and the new node</li>
 * </ul>
 *
 * @since 1.3
 * @param <T> the type of nodes this class can handle
 */
public class NodeAddData<T>
{
    /** Stores the parent node of the add operation. */
    private final T parent;

    /**
     * Stores a list with the names of nodes that are on the path between the
     * parent node and the new node.
     */
    private final List<String> pathNodes;

    /** Stores the name of the new node. */
    private final String newNodeName;

    /** Stores the attribute flag. */
    private final boolean attribute;

    /**
     * Creates a new instance of {@code NodeAddData} and initializes it.
     *
     * @param parentNode the parent node of the add operation
     * @param newName the name of the new node
     * @param isAttr flag whether the new node is an attribute
     * @param intermediateNodes an optional collection with path nodes
     */
    public NodeAddData(final T parentNode, final String newName, final boolean isAttr,
            final Collection<String> intermediateNodes)
    {
        parent = parentNode;
        newNodeName = newName;
        attribute = isAttr;
        pathNodes = createPathNodes(intermediateNodes);
    }

    /**
     * Returns a flag if the new node to be added is an attribute.
     *
     * @return <b>true</b> for an attribute node, <b>false</b> for a child
     * node
     */
    public boolean isAttribute()
    {
        return attribute;
    }

    /**
     * Returns the name of the new node.
     *
     * @return the new node's name
     */
    public String getNewNodeName()
    {
        return newNodeName;
    }

    /**
     * Returns the parent node.
     *
     * @return the parent node
     */
    public T getParent()
    {
        return parent;
    }

    /**
     * Returns a list with further nodes that must be added. This is needed if a
     * complete branch is to be added at once. For instance, imagine that there
     * exists only a node {@code database}. Now the key
     * {@code database.connection.settings.username} (assuming the syntax
     * of the default expression engine) is to be added. Then
     * {@code username} is the name of the new node, but the nodes
     * {@code connection} and {@code settings} must be added to
     * the parent node first. In this example these names would be returned by
     * this method.
     *
     * @return a list with the names of nodes that must be added as parents of
     * the new node (never <b>null</b>)
     */
    public List<String> getPathNodes()
    {
        return pathNodes;
    }

    /**
     * Creates the list with path nodes. Handles null input.
     *
     * @param intermediateNodes the nodes passed to the constructor
     * @return an unmodifiable list of path nodes
     */
    private static List<String> createPathNodes(
            final Collection<String> intermediateNodes)
    {
        if (intermediateNodes == null)
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(
                intermediateNodes));
    }
}
