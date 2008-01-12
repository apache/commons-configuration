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

import java.util.Iterator;

/**
 * <p>
 * A concrete combiner implementation that is able to construct an override
 * combination.
 * </p>
 * <p>
 * An <em>override combination</em> means that nodes in the first node
 * structure take precedence over nodes in the second, or - in other words -
 * nodes of the second structure are only added to the resulting structure if
 * they do not occure in the first one. This is especially suitable for dealing
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
 * list nodes are important. The <code>addListNode()</code> can be called to
 * declare certain nodes as list nodes. This has the effect that these nodes
 * will never be combined.
 * </p>
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 * @since 1.3
 */
public class OverrideCombiner extends NodeCombiner
{
    /**
     * Constructs an override combination for the passed in node structures.
     *
     * @param node1 the first node
     * @param node2 the second node
     * @return the resulting combined node structure
     */
    public ConfigurationNode combine(ConfigurationNode node1,
            ConfigurationNode node2)
    {
        ViewNode result = createViewNode();
        result.setName(node1.getName());

        // Process nodes from the first structure, which override the second
        for (Iterator it = node1.getChildren().iterator(); it.hasNext();)
        {
            ConfigurationNode child = (ConfigurationNode) it.next();
            ConfigurationNode child2 = canCombine(node1, node2, child);
            if (child2 != null)
            {
                result.addChild(combine(child, child2));
            }
            else
            {
                result.addChild(child);
            }
        }

        // Process nodes from the second structure, which are not contained
        // in the first structure
        for (Iterator it = node2.getChildren().iterator(); it.hasNext();)
        {
            ConfigurationNode child = (ConfigurationNode) it.next();
            if (node1.getChildrenCount(child.getName()) < 1)
            {
                result.addChild(child);
            }
        }

        // Handle attributes and value
        addAttributes(result, node1, node2);
        result.setValue((node1.getValue() != null) ? node1.getValue() : node2
                .getValue());

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
        for (Iterator it = node2.getAttributes().iterator(); it.hasNext();)
        {
            ConfigurationNode attr = (ConfigurationNode) it.next();
            if (node1.getAttributeCount(attr.getName()) == 0)
            {
                result.addAttribute(attr);
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
     * @param node2 the second node
     * @param child the child node (of the first node)
     * @return a child of the second node, with which a combination is possible
     */
    protected ConfigurationNode canCombine(ConfigurationNode node1,
            ConfigurationNode node2, ConfigurationNode child)
    {
        if (node2.getChildrenCount(child.getName()) == 1
                && node1.getChildrenCount(child.getName()) == 1
                && !isListNode(child))
        {
            return (ConfigurationNode) node2.getChildren(child.getName())
                    .get(0);
        }
        else
        {
            return null;
        }
    }
}
