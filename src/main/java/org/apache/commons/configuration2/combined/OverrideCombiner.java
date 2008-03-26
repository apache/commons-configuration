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

import org.apache.commons.configuration2.expr.NodeHandler;

/**
 * <p>
 * A concrete combiner implementation that is able to construct an override
 * combination.
 * </p>
 * <p>
 * An <em>override combination</em> means that nodes in the first node
 * structure take precedence over nodes in the second, or - in other words -
 * nodes of the second structure are only added to the resulting structure if
 * they do not occur in the first one. This is especially suitable for dealing
 * with the properties of configurations that are defined in an
 * <code>override</code> section of a configuration definition file (hence the
 * name).
 * </p>
 * <p>
 * This combiner will iterate over the second node hierarchy and find all nodes
 * that are not contained in the first hierarchy; these are added to the result.
 * If a node can be found in both structures, it is checked whether a
 * combination (in a recursive way) can be constructed for the two, which will
 * then be added. Per default, nodes are combined, which occur only once in both
 * structures. This test is implemented in the <code>canCombine()</code>
 * method.
 * </p>
 * <p>
 * As is true for the <code>{@link UnionCombiner}</code>, for this combiner
 * list nodes are important. The <code>addListNode()</code> method can be
 * called to declare certain nodes as list nodes. This has the effect that these
 * nodes will never be combined.
 * </p>
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 * @since 2.0
 */
public class OverrideCombiner extends NodeCombiner
{
    /**
     * Constructs an override combination for the passed in node structures.
     *
     * @param node1 the first node
     * @param handler1 the node handler for the first source node
     * @param node2 the second node
     * @param handler2 the node handler for the second source node
     * @return the resulting combined node structure
     */
    public <T, U> CombinedNode combine(T node1, NodeHandler<T> handler1,
            U node2, NodeHandler<U> handler2)
    {
        CombinedNode result = createViewNode();
        result.setName(handler1.nodeName(node1));

        // Process nodes from the first structure, which override the second
        for (T child : handler1.getChildren(node1))
        {
            String childName = handler1.nodeName(child);
            U child2 = canCombine(node1, handler1, node2, handler2, child);
            if (child2 != null)
            {
                result.addChild(childName, combine(child, handler1, child2,
                        handler2));
            }
            else
            {
                result.addChild(childName, child);
            }
        }

        // Process nodes from the second structure, which are not contained
        // in the first structure
        for (U child : handler2.getChildren(node2))
        {
            String childName = handler2.nodeName(child);
            if (handler1.getChildrenCount(node1, childName) < 1)
            {
                result.addChild(childName, child);
            }
        }

        // Handle attributes and value
        addAttributes(result, node1, handler1, node2, handler2);
        result.setValue((handler1.getValue(node1) != null) ? handler1
                .getValue(node1) : handler2.getValue(node2));

        return result;
    }

    /**
     * Handles the attributes during a combination process. First all attributes
     * of the first node will be added to the result. Then all attributes of the
     * second node, which are not contained in the first node, will also be
     * added.
     *
     * @param result the resulting node
     * @param node1 the first node
     * @param handler1 the node handler for the first node
     * @param node2 the second node
     * @param handler2 the node handler for the second node
     */
    protected <T, U> void addAttributes(CombinedNode result, T node1,
            NodeHandler<T> handler1, U node2, NodeHandler<U> handler2)
    {
        appendAttributes(result, node1, handler1);
        for (String attr : handler2.getAttributes(node2))
        {
            if (handler1.getAttributeValue(node1, attr) == null)
            {
                result.addAttributeValue(attr, handler2.getAttributeValue(
                        node2, attr));
            }
        }
    }

    /**
     * Tests if a child node of the second node can be combined with the given
     * child node of the first node. If this is the case, the corresponding node
     * will be returned, otherwise <b>null</b>. This implementation checks
     * whether the child node occurs only once in both hierarchies and is no
     * known list node.
     *
     * @param node1 the first node
     * @param handler1 the node handler for the first node
     * @param node2 the second node
     * @param handler2 the node handler for the second node
     * @param child the child node (of the first node)
     * @return a child of the second node, with which a combination is possible
     */
    protected <T, U> U canCombine(T node1, NodeHandler<T> handler1, U node2,
            NodeHandler<U> handler2, T child)
    {
        String childName = handler1.nodeName(child);
        if (handler2.getChildrenCount(node2, childName) == 1
                && handler1.getChildrenCount(node1, childName) == 1
                && !isListNode(child, handler1))
        {
            return handler2.getChildren(node2, childName).get(0);
        }
        else
        {
            return null;
        }
    }
}
