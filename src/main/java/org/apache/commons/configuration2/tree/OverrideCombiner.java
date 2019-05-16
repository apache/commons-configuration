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
 * {@code override} section of a configuration definition file (hence the
 * name).
 * </p>
 * <p>
 * This combiner will iterate over the second node hierarchy and find all nodes
 * that are not contained in the first hierarchy; these are added to the result.
 * If a node can be found in both structures, it is checked whether a
 * combination (in a recursive way) can be constructed for the two, which will
 * then be added. Per default, nodes are combined, which occur only once in both
 * structures. This test is implemented in the {@code canCombine()}
 * method.
 * </p>
 * <p>
 * As is true for the {@link UnionCombiner}, for this combiner
 * list nodes are important. The {@code addListNode()} can be called to
 * declare certain nodes as list nodes. This has the effect that these nodes
 * will never be combined.
 * </p>
 *
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
    @Override
    public ImmutableNode combine(final ImmutableNode node1,
            final ImmutableNode node2)
    {
        final ImmutableNode.Builder result = new ImmutableNode.Builder();
        result.name(node1.getNodeName());

        // Process nodes from the first structure, which override the second
        for (final ImmutableNode child : node1.getChildren())
        {
            final ImmutableNode child2 = canCombine(node1, node2, child);
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
        for (final ImmutableNode child : node2.getChildren())
        {
            if (HANDLER.getChildrenCount(node1, child.getNodeName()) < 1)
            {
                result.addChild(child);
            }
        }

        // Handle attributes and value
        addAttributes(result, node1, node2);
        result.value((node1.getValue() != null) ? node1.getValue() : node2
                .getValue());

        return result.create();
    }

    /**
     * Handles the attributes during a combination process. First all attributes
     * of the first node are added to the result. Then all attributes of the
     * second node, which are not contained in the first node, are also added.
     *
     * @param result the resulting node
     * @param node1 the first node
     * @param node2 the second node
     */
    protected void addAttributes(final ImmutableNode.Builder result,
            final ImmutableNode node1, final ImmutableNode node2)
    {
        result.addAttributes(node1.getAttributes());
        for (final String attr : node2.getAttributes().keySet())
        {
            if (!node1.getAttributes().containsKey(attr))
            {
                result.addAttribute(attr,
                        HANDLER.getAttributeValue(node2, attr));
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
    protected ImmutableNode canCombine(final ImmutableNode node1,
            final ImmutableNode node2, final ImmutableNode child)
    {
        if (HANDLER.getChildrenCount(node2, child.getNodeName()) == 1
                && HANDLER.getChildrenCount(node1, child.getNodeName()) == 1
                && !isListNode(child))
        {
            return HANDLER.getChildren(node2, child.getNodeName()).get(0);
        }
        return null;
    }
}
