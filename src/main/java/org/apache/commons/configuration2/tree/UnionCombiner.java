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

import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * A specialized implementation of the {@code NodeCombiner} interface
 * that constructs a union from two passed in node hierarchies.
 * </p>
 * <p>
 * The given source hierarchies are traversed, and their nodes are added to the
 * resulting structure. Under some circumstances two nodes can be combined
 * rather than adding both. This is the case if both nodes are single children
 * (no lists) of their parents and do not have values. The corresponding check
 * is implemented in the {@code findCombineNode()} method.
 * </p>
 * <p>
 * Sometimes it is not possible for this combiner to detect whether two nodes
 * can be combined or not. Consider the following two node hierarchies:
 * </p>
 *
 * <pre>
 * Hierarchy 1:
 *
 * Database
 *   +--Tables
 *        +--Table
 *             +--name [users]
 *             +--fields
 *                   +--field
 *                   |    +--name [uid]
 *                   +--field
 *                   |    +--name [usrname]
 *                     ...
 * </pre>
 *
 * <pre>
 * Hierarchy 2:
 *
 * Database
 *   +--Tables
 *        +--Table
 *             +--name [documents]
 *             +--fields
 *                   +--field
 *                   |    +--name [docid]
 *                   +--field
 *                   |    +--name [docname]
 *                     ...
 * </pre>
 *
 * <p>
 * Both hierarchies contain data about database tables. Each describes a single
 * table. If these hierarchies are to be combined, the result should probably
 * look like the following:
 * </p>
 *
 * <pre>
 * Database
 *   +--Tables
 *        +--Table
 *        |    +--name [users]
 *        |    +--fields
 *        |          +--field
 *        |          |    +--name [uid]
 *        |            ...
 *        +--Table
 *             +--name [documents]
 *             +--fields
 *                   +--field
 *                   |    +--name [docid]
 *                     ...
 * </pre>
 *
 * <p>
 * i.e. the {@code Tables} nodes should be combined, while the
 * {@code Table} nodes should both be added to the resulting tree. From
 * the combiner's point of view there is no difference between the
 * {@code Tables} and the {@code Table} nodes in the source trees,
 * so the developer has to help out and give a hint that the {@code Table}
 * nodes belong to a list structure. This can be done using the
 * {@code addListNode()} method; this method expects the name of a node,
 * which should be treated as a list node. So if
 * {@code addListNode("Table");} was called, the combiner knows that it
 * must not combine the {@code Table} nodes, but add it both to the
 * resulting tree.
 * </p>
 * <p>
 * Another limitation is the handling of attributes: Attributes can only
 * have a single value. So if two nodes are to be combined which both have
 * an attribute with the same name, it is not possible to construct a
 * proper union attribute. In this case, the attribute value from the
 * first node is used.
 * </p>
 *
 * @since 1.3
 */
public class UnionCombiner extends NodeCombiner
{
    /**
     * Combines the given nodes to a new union node.
     *
     * @param node1 the first source node
     * @param node2 the second source node
     * @return the union node
     */
    @Override
    public ImmutableNode combine(final ImmutableNode node1,
            final ImmutableNode node2)
    {
        final ImmutableNode.Builder result = new ImmutableNode.Builder();
        result.name(node1.getNodeName());

        // attributes of the first node take precedence
        result.addAttributes(node2.getAttributes());
        result.addAttributes(node1.getAttributes());

        // Check if nodes can be combined
        final List<ImmutableNode> children2 = new LinkedList<>(node2.getChildren());
        for (final ImmutableNode child1 : node1.getChildren())
        {
            final ImmutableNode child2 = findCombineNode(node1, node2, child1
            );
            if (child2 != null)
            {
                result.addChild(combine(child1, child2));
                children2.remove(child2);
            }
            else
            {
                result.addChild(child1);
            }
        }

        // Add remaining children of node 2
        for (final ImmutableNode c : children2)
        {
            result.addChild(c);
        }

        return result.create();
    }

    /**
     * <p>
     * Tries to find a child node of the second source node, with which a child
     * of the first source node can be combined. During combining of the source
     * nodes an iteration over the first source node's children is performed.
     * For each child node it is checked whether a corresponding child node in
     * the second source node exists. If this is the case, these corresponding
     * child nodes are recursively combined and the result is added to the
     * combined node. This method implements the checks whether such a recursive
     * combination is possible. The actual implementation tests the following
     * conditions:
     * </p>
     * <ul>
     * <li>In both the first and the second source node there is only one child
     * node with the given name (no list structures).</li>
     * <li>The given name is not in the list of known list nodes, i.e. it was
     * not passed to the {@code addListNode()} method.</li>
     * <li>None of these matching child nodes has a value.</li>
     * </ul>
     * <p>
     * If all of these tests are successful, the matching child node of the
     * second source node is returned. Otherwise the result is <b>null</b>.
     * </p>
     *
     * @param node1 the first source node
     * @param node2 the second source node
     * @param child the child node of the first source node to be checked
     * @return the matching child node of the second source node or <b>null</b>
     * if there is none
     */
    protected ImmutableNode findCombineNode(final ImmutableNode node1,
            final ImmutableNode node2, final ImmutableNode child)
    {
        if (child.getValue() == null && !isListNode(child)
                && HANDLER.getChildrenCount(node1, child.getNodeName()) == 1
                && HANDLER.getChildrenCount(node2, child.getNodeName()) == 1)
        {
            final ImmutableNode child2 =
                    HANDLER.getChildren(node2, child.getNodeName()).get(0);
            if (child2.getValue() == null)
            {
                return child2;
            }
        }
        return null;
    }
}
