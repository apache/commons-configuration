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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An internally used helper class for storing information about the managed
 * node structure. An instance of this class represents the current tree. It
 * stores the current root node and additional information which is not part
 * of the {@code ImmutableNode} class.
 *
 * @since 2.0
 */
class TreeData extends AbstractImmutableNodeHandler implements ReferenceNodeHandler
{
    /** The root node of the tree. */
    private final ImmutableNode root;

    /** A map that associates the parent node to each node. */
    private final Map<ImmutableNode, ImmutableNode> parentMapping;

    /**
     * Stores information about nodes which have been replaced by
     * manipulations of the structure. This map is used to avoid that the
     * parent mapping has to be updated after each change.
     */
    private final Map<ImmutableNode, ImmutableNode> replacementMapping;

    /** An inverse replacement mapping. */
    private final Map<ImmutableNode, ImmutableNode> inverseReplacementMapping;

    /** The node tracker. */
    private final NodeTracker nodeTracker;

    /** The reference tracker. */
    private final ReferenceTracker referenceTracker;

    /**
     * Creates a new instance of {@code TreeData} and initializes it with all
     * data to be stored.
     *
     * @param root the root node of the current tree
     * @param parentMapping the mapping to parent nodes
     * @param replacements the map with the nodes that have been replaced
     * @param tracker the {@code NodeTracker}
     * @param refTracker the {@code ReferenceTracker}
     */
    public TreeData(final ImmutableNode root,
            final Map<ImmutableNode, ImmutableNode> parentMapping,
            final Map<ImmutableNode, ImmutableNode> replacements,
            final NodeTracker tracker, final ReferenceTracker refTracker)
    {
        this.root = root;
        this.parentMapping = parentMapping;
        replacementMapping = replacements;
        inverseReplacementMapping = createInverseMapping(replacements);
        nodeTracker = tracker;
        referenceTracker = refTracker;
    }

    @Override
    public ImmutableNode getRootNode()
    {
        return root;
    }

    /**
     * Returns the {@code NodeTracker}
     *
     * @return the {@code NodeTracker}
     */
    public NodeTracker getNodeTracker()
    {
        return nodeTracker;
    }

    /**
     * Returns the {@code ReferenceTracker}.
     *
     * @return the {@code ReferenceTracker}
     */
    public ReferenceTracker getReferenceTracker()
    {
        return referenceTracker;
    }

    /**
     * Returns the parent node of the specified node. Result is <b>null</b>
     * for the root node. If the passed in node cannot be resolved, an
     * exception is thrown.
     *
     * @param node the node in question
     * @return the parent node for this node
     * @throws IllegalArgumentException if the node cannot be resolved
     */
    @Override
    public ImmutableNode getParent(final ImmutableNode node)
    {
        if (node == getRootNode())
        {
            return null;
        }
        final ImmutableNode org = handleReplacements(node, inverseReplacementMapping);

        final ImmutableNode parent = parentMapping.get(org);
        if (parent == null)
        {
            throw new IllegalArgumentException("Cannot determine parent! "
                    + node + " is not part of this model.");
        }
        return handleReplacements(parent, replacementMapping);
    }

    /**
     * Returns a copy of the mapping from nodes to their parents.
     *
     * @return the copy of the parent mapping
     */
    public Map<ImmutableNode, ImmutableNode> copyParentMapping()
    {
        return new HashMap<>(parentMapping);
    }

    /**
     * Returns a copy of the map storing the replaced nodes.
     *
     * @return the copy of the replacement mapping
     */
    public Map<ImmutableNode, ImmutableNode> copyReplacementMapping()
    {
        return new HashMap<>(replacementMapping);
    }

    /**
     * Creates a new instance which uses the specified {@code NodeTracker}.
     * This method is called when there are updates of the state of tracked
     * nodes.
     *
     * @param newTracker the new {@code NodeTracker}
     * @return the updated instance
     */
    public TreeData updateNodeTracker(final NodeTracker newTracker)
    {
        return new TreeData(root, parentMapping, replacementMapping,
                newTracker, referenceTracker);
    }

    /**
     * Creates a new instance which uses the specified {@code ReferenceTracker}.
     * All other information are unchanged. This method is called when there
     * updates for references.
     *
     * @param newTracker the new {@code ReferenceTracker}
     * @return the updated instance
     */
    public TreeData updateReferenceTracker(final ReferenceTracker newTracker)
    {
        return new TreeData(root, parentMapping, replacementMapping,
                nodeTracker, newTracker);
    }

    /**
     * {@inheritDoc} This implementation delegates to the reference tracker.
     */
    @Override
    public Object getReference(final ImmutableNode node)
    {
        return getReferenceTracker().getReference(node);
    }

    /**
     * {@inheritDoc} This implementation delegates to the reference tracker.
     */
    @Override
    public List<Object> removedReferences()
    {
        return getReferenceTracker().getRemovedReferences();
    }

    /**
     * Checks whether the passed in node is subject of a replacement by
     * another one. If so, the other node is returned. This is done until a
     * node is found which had not been replaced. Updating the parent
     * mapping may be expensive for large node structures. Therefore, it
     * initially remains constant, and a map with replacements is used. When
     * querying a parent node, the replacement map has to be consulted
     * whether the parent node is still valid.
     *
     * @param replace the replacement node
     * @param mapping the replacement mapping
     * @return the corresponding node according to the mapping
     */
    private static ImmutableNode handleReplacements(final ImmutableNode replace,
            final Map<ImmutableNode, ImmutableNode> mapping)
    {
        ImmutableNode node = replace;
        ImmutableNode org;
        do
        {
            org = mapping.get(node);
            if (org != null)
            {
                node = org;
            }
        } while (org != null);
        return node;
    }

    /**
     * Creates the inverse replacement mapping.
     *
     * @param replacements the original replacement mapping
     * @return the inverse replacement mapping
     */
    private Map<ImmutableNode, ImmutableNode> createInverseMapping(
            final Map<ImmutableNode, ImmutableNode> replacements)
    {
        final Map<ImmutableNode, ImmutableNode> inverseMapping =
                new HashMap<>();
        for (final Map.Entry<ImmutableNode, ImmutableNode> e : replacements
                .entrySet())
        {
            inverseMapping.put(e.getValue(), e.getKey());
        }
        return inverseMapping;
    }
}
