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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * A specialized implementation of the <code>NodeCombiner</code> interface
 * that constructs a union from two passed in node hierarchies.
 * </p>
 * <p>
 * The given source hierarchies are traversed and their nodes are added to the
 * resulting structure. Under some circumstances two nodes can be combined
 * rather than adding both. This is the case if both nodes are single children
 * (no lists) of their parents and do not have values. The corresponding check
 * is implemented in the <code>findCombineNode()</code> method.
 * </p>
 * <p>
 * Sometimes it is not possible for this combiner to detect whether two nodes
 * can be combined or not. Consider the following two node hierarchies:
 * </p>
 * <p>
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
 * </p>
 * <p>
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
 * </p>
 * <p>
 * Both hierarchies contain data about database tables. Each describes a single
 * table. If these hierarchies are to be combined, the result should probably
 * look like the following:
 * <p>
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
 * </p>
 * <p>
 * i.e. the <code>Tables</code> nodes should be combined, while the
 * <code>Table</code> nodes should both be added to the resulting tree. From
 * the combiner's point of view there is no difference between the
 * <code>Tables</code> and the <code>Table</code> nodes in the source trees,
 * so the developer has to help out and give a hint that the <code>Table</code>
 * nodes belong to a list structure. This can be done using the
 * <code>addListNode()</code> method; this method expects the name of a node,
 * which should be treated as a list node. So if
 * <code>addListNode("Table");</code> was called, the combiner knows that it
 * must not combine the <code>Table</code> nodes, but add it both to the
 * resulting tree.
 * </p>
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
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
    public ConfigurationNode combine(ConfigurationNode node1,
            ConfigurationNode node2)
    {
        ViewNode result = createViewNode();
        result.setName(node1.getName());
        result.appendAttributes(node1);
        result.appendAttributes(node2);

        // Check if nodes can be combined
        List children2 = new LinkedList(node2.getChildren());
        for (Iterator it = node1.getChildren().iterator(); it.hasNext();)
        {
            ConfigurationNode child1 = (ConfigurationNode) it.next();
            ConfigurationNode child2 = findCombineNode(node1, node2, child1,
                    children2);
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
        for (Iterator it = children2.iterator(); it.hasNext();)
        {
            result.addChild((ConfigurationNode) it.next());
        }

        return result;
    }

    /**
     * <p>
     * Tries to find a child node of the second source node, with whitch a child
     * of the first source node can be combined. During combining of the source
     * nodes an iteration over the first source node's children is performed.
     * For each child node it is checked whether a corresponding child node in
     * the second source node exists. If this is the case, these corresponsing
     * child nodes are recursively combined and the result is added to the
     * combined node. This method implements the checks whether such a recursive
     * combination is possible. The actual implementation tests the following
     * conditions:
     * </p>
     * <p>
     * <ul>
     * <li>In both the first and the second source node there is only one child
     * node with the given name (no list structures).</li>
     * <li>The given name is not in the list of known list nodes, i.e. it was
     * not passed to the <code>addListNode()</code> method.</li>
     * <li>None of these matching child nodes has a value.</li>
     * </ul>
     * </p>
     * <p>
     * If all of these tests are successfull, the matching child node of the
     * second source node is returned. Otherwise the result is <b>null</b>.
     * </p>
     *
     * @param node1 the first source node
     * @param node2 the second source node
     * @param child the child node of the first source node to be checked
     * @param children a list with all children of the second source node
     * @return the matching child node of the second source node or <b>null</b>
     * if there is none
     */
    protected ConfigurationNode findCombineNode(ConfigurationNode node1,
            ConfigurationNode node2, ConfigurationNode child, List children)
    {
        if (child.getValue() == null && !isListNode(child)
                && node1.getChildrenCount(child.getName()) == 1
                && node2.getChildrenCount(child.getName()) == 1)
        {
            ConfigurationNode child2 = (ConfigurationNode) node2.getChildren(
                    child.getName()).iterator().next();
            if (child2.getValue() == null)
            {
                return child2;
            }
        }
        return null;
    }
}
