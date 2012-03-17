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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * A specialized implementation of the {@code NodeCombiner} interface
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
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
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
    public ConfigurationNode combine(ConfigurationNode node1, ConfigurationNode node2)
    {
        ViewNode result = createViewNode();
        result.setName(node1.getName());
        result.setValue(node1.getValue());
        addAttributes(result, node1, node2);

        // Check if nodes can be combined
        List<ConfigurationNode> children2 = new LinkedList<ConfigurationNode>(node2.getChildren());
        for (ConfigurationNode child1 : node1.getChildren())
        {
            ConfigurationNode child2 = canCombine(node1, node2, child1, children2);
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
        for (ConfigurationNode c : children2)
        {
            result.addChild(c);
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
     * @param node2 the second node
     */
    protected void addAttributes(ViewNode result, ConfigurationNode node1,
            ConfigurationNode node2)
    {
        result.appendAttributes(node1);
        for (ConfigurationNode attr : node2.getAttributes())
        {
            if (node1.getAttributeCount(attr.getName()) == 0)
            {
                result.addAttribute(attr);
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
    protected ConfigurationNode canCombine(ConfigurationNode node1,
            ConfigurationNode node2, ConfigurationNode child, List<ConfigurationNode> children2)
    {
        List<ConfigurationNode> attrs1 = child.getAttributes();
        List<ConfigurationNode> nodes = new ArrayList<ConfigurationNode>();

        List<ConfigurationNode> children = node2.getChildren(child.getName());
        Iterator<ConfigurationNode> it = children.iterator();
        while (it.hasNext())
        {
            ConfigurationNode node = it.next();
            Iterator<ConfigurationNode> iter = attrs1.iterator();
            while (iter.hasNext())
            {
                ConfigurationNode attr1 = iter.next();
                List<ConfigurationNode> list2 = node.getAttributes(attr1.getName());
                if (list2.size() == 1
                    && !attr1.getValue().equals(list2.get(0).getValue()))
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
        if (nodes.size() > 1 && !isListNode(child))
        {
            Iterator<ConfigurationNode> iter = nodes.iterator();
            while (iter.hasNext())
            {
                children2.remove(iter.next());
            }
        }

        return null;
    }
}
