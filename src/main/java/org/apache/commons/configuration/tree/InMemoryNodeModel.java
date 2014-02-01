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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * A specialized node model implementation which operates on
 * {@link ImmutableNode} structures.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public class InMemoryNodeModel implements NodeHandler<ImmutableNode>
{
    /** Stores information about the current nodes structure. */
    private final AtomicReference<TreeData> structure;

    /**
     * Creates a new instance of {@code InMemoryNodeModel} which is initialized
     * with an empty root node.
     */
    public InMemoryNodeModel()
    {
        this(null);
    }

    /**
     * Creates a new instance of {@code InMemoryNodeModel} and initializes it
     * from the given root node. If the passed in node is <b>null</b>, a new,
     * empty root node is created.
     *
     * @param root the new root node for this model
     */
    public InMemoryNodeModel(ImmutableNode root)
    {
        structure =
                new AtomicReference<TreeData>(
                        createTreeData(initialRootNode(root)));
    }

    /**
     * Returns the root node of this model.
     *
     * @return the root node
     */
    public ImmutableNode getRootNode()
    {
        return structure.get().getRoot();
    }

    /**
     * Returns a {@code NodeHandler} for dealing with the nodes managed by this
     * model.
     *
     * @return the {@code NodeHandler}
     */
    public NodeHandler<ImmutableNode> getNodeHandler()
    {
        return this;
    }

    public String nodeName(ImmutableNode node)
    {
        return node.getNodeName();
    }

    public Object getValue(ImmutableNode node)
    {
        return node.getValue();
    }

    /**
     * {@inheritDoc} This implementation uses internal mapping information to
     * determine the parent node of the given node. If the passed in node is the
     * root node of this model, result is <b>null</b>. If the node is not part
     * of this model, an exception is thrown. Otherwise, the parent node is
     * returned.
     *
     * @throws IllegalArgumentException if the passed in node does not belong to
     *         this model
     */
    public ImmutableNode getParent(ImmutableNode node)
    {
        return structure.get().getParent(node);
    }

    public List<ImmutableNode> getChildren(ImmutableNode node)
    {
        return node.getChildren();
    }

    /**
     * {@inheritDoc} This implementation returns an immutable list with all
     * child nodes that have the specified name.
     */
    public List<ImmutableNode> getChildren(ImmutableNode node, String name)
    {
        List<ImmutableNode> result =
                new ArrayList<ImmutableNode>(node.getChildren().size());
        for (ImmutableNode c : node.getChildren())
        {
            if (StringUtils.equals(name, c.getNodeName()))
            {
                result.add(c);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public ImmutableNode getChild(ImmutableNode node, int index)
    {
        return node.getChildren().get(index);
    }

    public int indexOfChild(ImmutableNode parent, ImmutableNode child)
    {
        return parent.getChildren().indexOf(child);
    }

    public int getChildrenCount(ImmutableNode node, String name)
    {
        if (name == null)
        {
            return node.getChildren().size();
        }
        else
        {
            return getChildren(node, name).size();
        }
    }

    public Set<String> getAttributes(ImmutableNode node)
    {
        return node.getAttributes().keySet();
    }

    public boolean hasAttributes(ImmutableNode node)
    {
        return !node.getAttributes().isEmpty();
    }

    public Object getAttributeValue(ImmutableNode node, String name)
    {
        return node.getAttributes().get(name);
    }

    /**
     * {@inheritDoc} This implementation assumes that a node is defined if it
     * has a value or has children or has attributes.
     */
    public boolean isDefined(ImmutableNode node)
    {
        return node.getValue() != null || !node.getChildren().isEmpty()
                || !node.getAttributes().isEmpty();
    }

    /**
     * Determines the initial root node of this model. If a root node has been
     * provided, it is used. Otherwise, an empty dummy root node is created.
     *
     * @param providedRoot the passed in root node
     * @return the root node to be used
     */
    private static ImmutableNode initialRootNode(ImmutableNode providedRoot)
    {
        return (providedRoot != null) ? providedRoot
                : new ImmutableNode.Builder().create();
    }

    /**
     * Creates a {@code TreeData} object for the specified root node.
     *
     * @param root the root node of the current tree
     * @return the {@code TreeData} describing the current tree
     */
    private static TreeData createTreeData(ImmutableNode root)
    {
        return new TreeData(root, createParentMapping(root));
    }

    /**
     * Creates the mapping to parent nodes for the nodes structured represented
     * by the passed in root node. Each node is assigned its parent node. Here
     * an iterative algorithm is used rather than a recursive one to avoid stack
     * overflow for huge structures.
     *
     * @param root the root node of the structure
     * @return the parent node mapping
     */
    private static Map<ImmutableNode, ImmutableNode> createParentMapping(
            ImmutableNode root)
    {
        Map<ImmutableNode, ImmutableNode> parents =
                new HashMap<ImmutableNode, ImmutableNode>();
        List<ImmutableNode> pendingNodes = new LinkedList<ImmutableNode>();
        pendingNodes.add(root);

        while (!pendingNodes.isEmpty())
        {
            ImmutableNode node = pendingNodes.remove(0);
            for (ImmutableNode c : node.getChildren())
            {
                pendingNodes.add(c);
                parents.put(c, node);
            }
        }
        return parents;
    }

    /**
     * An internally used helper class for storing information about the managed
     * node structure. An instance of this class represents the current tree. It
     * stores the current root node and additional information which is not part
     * of the {@code ImmutableNode} class.
     */
    private static class TreeData
    {
        /** The root node of the tree. */
        private final ImmutableNode root;

        /** A map that associates the parent node to each node. */
        private final Map<ImmutableNode, ImmutableNode> parentMapping;

        /**
         * Creates a new instance of {@code TreeData} and initializes it with
         * all data to be stored.
         *
         * @param root the root node of the current tree
         * @param parentMapping the mapping to parent nodes
         */
        public TreeData(ImmutableNode root,
                Map<ImmutableNode, ImmutableNode> parentMapping)
        {
            this.root = root;
            this.parentMapping = parentMapping;
        }

        /**
         * Returns the root node.
         *
         * @return the current root node
         */
        public ImmutableNode getRoot()
        {
            return root;
        }

        /**
         * Returns the parent node of the specified node. Result is <b>null</b>
         * for the root node. If the passed in node cannot be resolved, an
         * exception is thrown.
         *
         * @param node the node in question
         * @return the parent node for this node
         * @throws IllegalArgumentException if the node cannot be reslved
         */
        public ImmutableNode getParent(ImmutableNode node)
        {
            if (node == getRoot())
            {
                return null;
            }

            ImmutableNode parent = parentMapping.get(node);
            if (parent == null)
            {
                throw new IllegalArgumentException("Cannot determine parent! "
                        + node + " is not part of this model.");
            }
            return parent;
        }
    }
}
