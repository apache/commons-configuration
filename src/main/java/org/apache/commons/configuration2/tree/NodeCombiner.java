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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * A base class for node combiner implementations.
 * </p>
 * <p>
 * A <em>node combiner</em> is an object that knows how two hierarchical node
 * structures can be combined into a single one. Of course, there are many
 * possible ways of implementing such a combination, e.g. constructing a union,
 * an intersection, or an "override" structure (were nodes in the first
 * hierarchy take precedence over nodes in the second hierarchy). This abstract
 * base class only provides some helper methods and defines the common interface
 * for node combiners. Concrete sub classes will implement the diverse
 * combination algorithms.
 * </p>
 * <p>
 * For some concrete combiner implementations it is important to distinguish
 * whether a node is a single node or whether it belongs to a list structure.
 * Alone from the input structures, the combiner will not always be able to make
 * this decision. So sometimes it may be necessary for the developer to
 * configure the combiner and tell it, which nodes should be treated as list
 * nodes. For this purpose the {@code addListNode()} method exists. It
 * can be passed the name of a node, which should be considered a list node.
 * </p>
 *
 * @since 1.3
 */
public abstract class NodeCombiner
{
    /**
     * A default handler object for immutable nodes. This object can be used by
     * derived classes for dealing with nodes. However, it provides only limited
     * functionality; it supports only operations on child nodes, but no
     * references to parent nodes.
     */
    protected static final NodeHandler<ImmutableNode> HANDLER =
            createNodeHandler();

    /** Stores a list with node names that are known to be list nodes. */
    private final Set<String> listNodes;

    /**
     * Creates a new instance of {@code NodeCombiner}.
     */
    public NodeCombiner()
    {
        listNodes = new HashSet<>();
    }

    /**
     * Adds the name of a node to the list of known list nodes. This means that
     * nodes with this name will never be combined.
     *
     * @param nodeName the name to be added
     */
    public void addListNode(final String nodeName)
    {
        listNodes.add(nodeName);
    }

    /**
     * Returns a set with the names of nodes that are known to be list nodes.
     *
     * @return a set with the names of list nodes
     */
    public Set<String> getListNodes()
    {
        return Collections.unmodifiableSet(listNodes);
    }

    /**
     * Checks if a node is a list node. This implementation tests if the given
     * node name is contained in the set of known list nodes. Derived classes
     * which use different criteria may overload this method.
     *
     * @param node the node to be tested
     * @return a flag whether this is a list node
     */
    public boolean isListNode(final ImmutableNode node)
    {
        return listNodes.contains(node.getNodeName());
    }

    /**
     * Combines the hierarchies represented by the given root nodes. This method
     * must be defined in concrete sub classes with the implementation of a
     * specific combination algorithm.
     *
     * @param node1 the first root node
     * @param node2 the second root node
     * @return the root node of the resulting combined node structure
     */
    public abstract ImmutableNode combine(ImmutableNode node1,
            ImmutableNode node2);

    /**
     * Creates a node handler object for immutable nodes which can be used by
     * sub classes to perform advanced operations on nodes.
     *
     * @return the node handler implementation
     */
    private static NodeHandler<ImmutableNode> createNodeHandler()
    {
        return new AbstractImmutableNodeHandler()
        {
            @Override
            public ImmutableNode getParent(final ImmutableNode node)
            {
                return null;
            }

            @Override
            public ImmutableNode getRootNode()
            {
                return null;
            }
        };
    }
}
