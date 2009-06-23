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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration2.expr.NodeHandler;

/**
 * <p>
 * A specialized implementation of the <code>NodeCombiner</code> interface
 * that performs a merge from two passed in node hierarchies.
 * </p>
 * <p>
 * This combiner performs the merge using a few rules:
 * <ol>
 * <li>Nodes can be merged when attributes that appear in both have the same value.</li>
 * <li>Only a single node in the second file is considered a match to the node in the first file.</li>
 * <li>Attributes in nodes that match are merged.
 * <li>Nodes in both files that do not match are added to the result.</li>
 * </ol>
 * </p>
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 * @version $Id$
 * @since 1.7
 */
public class MergeCombiner extends NodeCombiner
{
    /**
     * Combines the given nodes to a new union node.
     *
     * @param node1 the first source node
     * @param node2 the second source node
     * @return the union node
     */    
    public <T, U> CombinedNode combine(T node1, NodeHandler<T> handler1, U node2, NodeHandler<U> handler2)
    {
        CombinedNode result = createCombinedNode();
        result.setName(handler1.nodeName(node1));
        result.setValue(handler1.getValue(node1));
        addAttributes(result, node1, handler1, node2, handler2);

        // Check if nodes can be combined
        List<U> children2 = new LinkedList<U>(handler2.getChildren(node2));
        for (T child1 : handler1.getChildren(node1))
        {
            U child2 = canCombine(node1, handler1, node2, handler2, child1, children2);
            if (child2 != null)
            {
                CombinedNode n = combine(child1, handler1, child2, handler2);
                result.addChild(n.getName(), n);
                children2.remove(child2);
            }
            else
            {
                result.addChild(handler1.nodeName(child1), child1);
            }
        }

        // Add remaining children of node 2
        for (U node : children2)
        {
            result.addChild(handler2.nodeName(node), node);
        }
        
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
                result.addAttributeValue(attr, handler2.getAttributeValue(node2, attr));
            }
        }
    }

    /**
     * Tests if the first node can be combined with the second node. A node can
     * only be combined if its attributes are all present in the second node and
     * they all have the same value.
     *
     * @param node1 the first node
     * @param node2 the second node
     * @param child the child node (of the first node)
     * @return a child of the second node, with which a combination is possible
     */
    protected <T, U> U canCombine(T node1, NodeHandler<T> handler1, U node2, NodeHandler<U> handler2, T child, List<U> children2)
    {
        List<String> attrs1 = handler1.getAttributes(child);
        List<U> nodes = new ArrayList<U>();

        List<U> children = handler2.getChildren(node2, handler1.nodeName(child));
        for (U node : children)
        {
            for (String attr1 : attrs1)
            {
                Object attr2val = handler2.getAttributeValue(node, attr1);
                if (attr2val != null && !handler1.getAttributeValue(child, attr1).equals(attr2val))
                {
                    node = null;
                    break;
                }
            }
            if (node != null)
            {
                nodes.add(node);
            }
        }

        if (nodes.size() == 1)
        {
            return nodes.get(0);
        }
        if (nodes.size() > 1 && !isListNode(child, handler1))
        {
            for (U node : nodes)
            {
                children2.remove(node);
            }
        }

        return null;
    }
}
