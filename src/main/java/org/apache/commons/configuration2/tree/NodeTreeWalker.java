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

import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * A class providing different algorithms for traversing a hierarchy of
 * configuration nodes.
 * </p>
 * <p>
 * The methods provided by this class accept a {@link ConfigurationNodeVisitor}
 * and visit all nodes in a hierarchy starting from a given root node. Because a
 * {@link NodeHandler} has to be passed in, too, arbitrary types of nodes can be
 * processed. The {@code walk()} methods differ in the order in which nodes are
 * visited. Details can be found in the method documentation.
 * </p>
 * <p>
 * An instance of this class does not define any state; therefore, it can be
 * shared and used concurrently. The {@code INSTANCE} member field can be used
 * for accessing a default instance. If desired (e.g. for testing purposes), new
 * instances can be created.
 * </p>
 *
 * @since 2.0
 */
public class NodeTreeWalker
{
    /** The default instance of this class. */
    public static final NodeTreeWalker INSTANCE = new NodeTreeWalker();

    /**
     * Visits all nodes in the hierarchy represented by the given root node in
     * <em>depth first search</em> manner. This means that first
     * {@link ConfigurationNodeVisitor#visitBeforeChildren(Object, NodeHandler)}
     * is called on a node, then recursively all of its children are processed,
     * and eventually
     * {@link ConfigurationNodeVisitor#visitAfterChildren(Object, NodeHandler)}
     * gets invoked.
     *
     * @param root the root node of the hierarchy to be processed (may be
     *        <b>null</b>, then this call has no effect)
     * @param visitor the {@code ConfigurationNodeVisitor} (must not be
     *        <b>null</b>)
     * @param handler the {@code NodeHandler} (must not be <b>null</b>)
     * @param <T> the type of the nodes involved
     * @throws IllegalArgumentException if a required parameter is <b>null</b>
     */
    public <T> void walkDFS(final T root, final ConfigurationNodeVisitor<T> visitor,
            final NodeHandler<T> handler)
    {
        if (checkParameters(root, visitor, handler))
        {
            dfs(root, visitor, handler);
        }
    }

    /**
     * Visits all nodes in the hierarchy represented by the given root node in
     * <em>breadth first search</em> manner. This means that the nodes are
     * visited in an order corresponding to the distance from the root node:
     * first the root node is visited, then all direct children of the root
     * node, then all direct children of the first child of the root node, etc.
     * In this mode of traversal, there is no direct connection between the
     * encounter of a node and its children. <strong>Therefore, on the visitor
     * object only the {@code visitBeforeChildren()} method gets
     * called!</strong>.
     *
     * @param root the root node of the hierarchy to be processed (may be
     *        <b>null</b>, then this call has no effect)
     * @param visitor the {@code ConfigurationNodeVisitor} (must not be
     *        <b>null</b>)
     * @param handler the {@code NodeHandler} (must not be <b>null</b>)
     * @param <T> the type of the nodes involved
     * @throws IllegalArgumentException if a required parameter is <b>null</b>
     */
    public <T> void walkBFS(final T root, final ConfigurationNodeVisitor<T> visitor,
            final NodeHandler<T> handler)
    {
        if (checkParameters(root, visitor, handler))
        {
            bfs(root, visitor, handler);
        }
    }

    /**
     * Recursive helper method for performing a DFS traversal.
     *
     * @param node the current node
     * @param visitor the visitor
     * @param handler the handler
     * @param <T> the type of the nodes involved
     */
    private static <T> void dfs(final T node, final ConfigurationNodeVisitor<T> visitor,
            final NodeHandler<T> handler)
    {
        if (!visitor.terminate())
        {
            visitor.visitBeforeChildren(node, handler);
            for (final T c : handler.getChildren(node))
            {
                dfs(c, visitor, handler);
            }
            if (!visitor.terminate())
            {
                visitor.visitAfterChildren(node, handler);
            }
        }
    }

    /**
     * Helper method for performing a BFS traversal. Implementation node: This
     * method organizes the nodes to be visited in structures on the heap.
     * Therefore, it can deal with larger structures than would be the case in a
     * recursive approach (where the stack size limits the size of the
     * structures which can be traversed).
     *
     * @param root the root node to be navigated
     * @param visitor the visitor
     * @param handler the handler
     * @param <T> the type of the nodes involved
     */
    private static <T> void bfs(final T root, final ConfigurationNodeVisitor<T> visitor,
            final NodeHandler<T> handler)
    {
        final List<T> pendingNodes = new LinkedList<>();
        pendingNodes.add(root);
        boolean cancel = false;

        while (!pendingNodes.isEmpty() && !cancel)
        {
            final T node = pendingNodes.remove(0);
            visitor.visitBeforeChildren(node, handler);
            cancel = visitor.terminate();
            for (final T c : handler.getChildren(node))
            {
                pendingNodes.add(c);
            }
        }
    }

    /**
     * Helper method for checking the parameters for the walk() methods. If
     * mandatory parameters are missing, an exception is thrown. The return
     * value indicates whether an operation can be performed.
     *
     * @param root the root node
     * @param visitor the visitor
     * @param handler the handler
     * @param <T> the type of the nodes involved
     * @return <b>true</b> if a walk operation can be performed, <b>false</b>
     *         otherwise
     * @throws IllegalArgumentException if a required parameter is missing
     */
    private static <T> boolean checkParameters(final T root,
            final ConfigurationNodeVisitor<T> visitor, final NodeHandler<T> handler)
    {
        if (visitor == null)
        {
            throw new IllegalArgumentException("Visitor must not be null!");
        }
        if (handler == null)
        {
            throw new IllegalArgumentException("NodeHandler must not be null!");
        }
        return root != null;
    }
}
