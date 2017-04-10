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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;

/**
 * <p>
 * A specialized implementation of the {@code NodeCombiner} interface
 * that performs a merge from two passed in node hierarchies.
 * </p>
 * <p>
 * This combiner performs the merge using a few rules:
 * </p>
 * <ol>
 * <li>Nodes can be merged when attributes that appear in both have the same value.</li>
 * <li>Only a single node in the second file is considered a match to the node in the first file.</li>
 * <li>Attributes in nodes that match are merged.
 * <li>Nodes in both files that do not match are added to the result.</li>
 * </ol>
 *
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

    @Override
    public ImmutableNode combine(ImmutableNode node1, ImmutableNode node2)
    {
        ImmutableNode.Builder result = new ImmutableNode.Builder();
        result.name(node1.getNodeName());
        result.value(node1.getValue());
        addAttributes(result, node1, node2);

        // Check if nodes can be combined
        List<ImmutableNode> children2 = new LinkedList<>(node2.getChildren());
        for (ImmutableNode child1 : node1.getChildren())
        {
            ImmutableNode child2 = canCombine(node2, child1, children2);
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
        for (ImmutableNode c : children2)
        {
            result.addChild(c);
        }
        return result.create();
    }

    /**
     * Handles the attributes during a combination process. First all attributes
     * of the first node will be added to the result. Then all attributes of the
     * second node, which are not contained in the first node, will also be
     * added.
     *
     * @param result the builder for the resulting node
     * @param node1 the first node
     * @param node2 the second node
     */
    protected void addAttributes(ImmutableNode.Builder result, ImmutableNode node1,
            ImmutableNode node2)
    {
        Map<String, Object> attributes = new HashMap<>();
        attributes.putAll(node1.getAttributes());
        for (Map.Entry<String, Object> e : node2.getAttributes().entrySet())
        {
            if (!attributes.containsKey(e.getKey()))
            {
                attributes.put(e.getKey(), e.getValue());
            }
        }
        result.addAttributes(attributes);
    }

    /**
     * Tests if the first node can be combined with the second node. A node can
     * only be combined if its attributes are all present in the second node and
     * they all have the same value.
     *
     * @param node2 the second node
     * @param child the child node (of the first node)
     * @param children2 the children of the 2nd node
     * @return a child of the second node, with which a combination is possible
     */
    protected ImmutableNode canCombine(ImmutableNode node2,
            ImmutableNode child, List<ImmutableNode> children2)
    {
        Map<String, Object> attrs1 = child.getAttributes();
        List<ImmutableNode> nodes = new ArrayList<>();

        List<ImmutableNode> children =
                HANDLER.getChildren(node2, child.getNodeName());
        for (ImmutableNode node : children)
        {
            if (matchAttributes(attrs1, node))
            {
                nodes.add(node);
            }
        }

        if (nodes.size() == 1)
        {
            return nodes.get(0);
        }
        if (nodes.size() > 1 && !isListNode(child))
        {
            for (ImmutableNode node : nodes)
            {
                children2.remove(node);
            }
        }

        return null;
    }

    /**
     * Checks whether the attributes of the passed in node are compatible.
     *
     * @param attrs1 the attributes of the first node
     * @param node the 2nd node
     * @return a flag whether these nodes can be combined regarding their
     *         attributes
     */
    private static boolean matchAttributes(Map<String, Object> attrs1,
            ImmutableNode node)
    {
        Map<String, Object> attrs2 = node.getAttributes();
        for (Map.Entry<String, Object> e : attrs1.entrySet())
        {
            if (attrs2.containsKey(e.getKey())
                    && !ObjectUtils
                            .equals(e.getValue(), attrs2.get(e.getKey())))
            {
                return false;
            }
        }
        return true;
    }
}
