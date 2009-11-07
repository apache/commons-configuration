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
package org.apache.commons.configuration2.base;

import java.util.Stack;

import org.apache.commons.configuration2.expr.ConfigurationNodeHandler;
import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.expr.NodeVisitorAdapter;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;

/**
 * <p>
 * A specialized implementation of {@code HierarchicalConfigurationSource} that
 * operates on a structure of {@link ConfigurationNode} objects that are hold in
 * memory.
 * </p>
 * <p>
 * Implementation note: an {@code InMemoryConfigurationSource} can be queried
 * concurrently by multiple threads. However, if updates are performed, client
 * code must ensure proper synchronization.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class InMemoryConfigurationSource extends AbstractConfigurationSource
        implements HierarchicalConfigurationSource<ConfigurationNode>
{
    /**
     * The node handler used by this configuration source class. Because {@code
     * ConfigurationNodeHandler} is stateless an instance can be shared between
     * all {@code InMemoryConfigurationSource} instances.
     */
    private static final NodeHandler<ConfigurationNode> NODE_HANDLER = new ConfigurationNodeHandler();

    /** Stores the root configuration node. */
    private volatile ConfigurationNode rootNode;

    /**
     * Creates a new instance of {@code InMemoryConfigurationSource}.
     */
    public InMemoryConfigurationSource()
    {
        rootNode = new DefaultConfigurationNode();
    }

    /**
     * Creates a new instance of {@code InMemoryConfigurationSource} and
     * initializes it with the data stored in the specified {@code
     * HierarchicalConfigurationSource}. From the nodes in the specified source
     * a deep copy is created. So the node structure of the newly created source
     * exactly corresponds to the one of the original source, but they are
     * independent of each other. The passed in {@code
     * HierarchicalConfigurationSource} can be <b>null</b>, then this
     * constructor behaves like the default constructor.
     *
     * @param c the {@code HierarchicalConfigurationSource} to be copied
     */
    public InMemoryConfigurationSource(
            HierarchicalConfigurationSource<? extends ConfigurationNode> c)
    {
        this();
        if (c != null)
        {
            setRootNode(copyNodes(c.getRootNode(), NODE_HANDLER));
        }
    }

    /**
     * Returns a reference to the root node.
     *
     * @return the root configuration node
     */
    public ConfigurationNode getRootNode()
    {
        return rootNode;
    }

    /**
     * Sets the root node for this configuration source. An {@code
     * InMemoryConfigurationSource} allows changing its root node. This will
     * change the whole content of the source.
     *
     * @param root the new root node (must not be <b>null</b>)
     * @throws IllegalArgumentException if the root node is <b>null</b>
     */
    public void setRootNode(ConfigurationNode root)
    {
        if (root == null)
        {
            throw new IllegalArgumentException("Root node must not be null!");
        }

        rootNode = root;
    }

    /**
     * Removes all data from this configuration source. This implementation
     * simply creates a new, empty root node.
     */
    public void clear()
    {
        rootNode = new DefaultConfigurationNode();
    }

    /**
     * Returns the {@code NodeHandler} used by this configuration source.
     *
     * @return the {@code NodeHandler}
     */
    public NodeHandler<ConfigurationNode> getNodeHandler()
    {
        return NODE_HANDLER;
    }

    /**
     * Copies the node structure below the specified root node. This method
     * traverses the node structure with a specialized visitor that can create a
     * deep clone of all nodes.
     *
     * @param <N> the type of the nodes
     * @param node the root node of the hierarchy which is to be copied
     * @param handler the {@code NodeHandler} to be used
     * @return the root node of the copied structure
     */
    protected static <N extends ConfigurationNode> N copyNodes(N node,
            NodeHandler<N> handler)
    {
        CloneVisitor<N> visitor = new CloneVisitor<N>();
        NodeVisitorAdapter.visit(visitor, node, handler);
        return visitor.getClone();
    }

    /**
     * Clears all reference fields in a node structure. A configuration node can
     * store a so-called &quot;reference&quot;. The meaning of this data is
     * determined by a concrete sub class. Typically such references are
     * specific for a configuration instance. If this instance is cloned or
     * copied, they must be cleared. This can be done using this method.
     *
     * @param <N> the type of the nodes
     * @param node the root node of the node hierarchy, in which the references
     *        are to be cleared
     * @param handler the {@code NodeHandler} to be used
     */
    protected static <N extends ConfigurationNode> void clearReferences(N node,
            NodeHandler<N> handler)
    {
        NodeVisitorAdapter.visit(new NodeVisitorAdapter<N>()
        {
            @Override
            public void visitBeforeChildren(N node, NodeHandler<N> handler)
            {
                node.setReference(null);
                for (ConfigurationNode attr : node.getAttributes())
                {
                    attr.setReference(null);
                }
            }
        }, node, handler);
    }

    /**
     * A specialized visitor that is able to create a deep copy of a node
     * hierarchy.
     */
    private static class CloneVisitor<N extends ConfigurationNode> extends
            NodeVisitorAdapter<N>
    {
        /** A stack with the actual object to be copied. */
        private Stack<N> copyStack;

        /** Stores the result of the clone process. */
        private N result;

        /**
         * Creates a new instance of {@code CloneVisitor}.
         */
        public CloneVisitor()
        {
            copyStack = new Stack<N>();
        }

        /**
         * Visits the specified node after its children have been processed.
         *
         * @param node the node
         */
        @Override
        public void visitAfterChildren(N node,
                NodeHandler<N> handler)
        {
            N copy = copyStack.pop();
            if (copyStack.isEmpty())
            {
                result = copy;
            }
        }

        /**
         * Visits and copies the specified node.
         *
         * @param node the node
         */
        @Override
        public void visitBeforeChildren(N node,
                NodeHandler<N> handler)
        {
            @SuppressWarnings("unchecked")
            N copy = (N) node.clone();
            copy.setParentNode(null);

            for (ConfigurationNode attr : node.getAttributes())
            {
                copy.addAttribute((ConfigurationNode) attr.clone());
            }
            if (!copyStack.isEmpty())
            {
                copyStack.peek().addChild(copy);
            }

            copyStack.push(copy);
        }

        /**
         * Returns the result of the clone process. This is the root node of the
         * cloned node hierarchy.
         *
         * @return the cloned root node
         */
        public N getClone()
        {
            return result;
        }
    }
}
