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
package org.apache.commons.configuration2.expr;

import java.util.Iterator;

/**
 * <p>
 * A simple adapter class that simplifies writing custom node visitor
 * implementations.
 * </p>
 * <p>
 * This class provides dummy implementations for the methods defined in the
 * {@link NodeVisitor} interface. Derived classes only need to override the
 * methods they really need.
 * </p>
 * <p>
 * In addition to the dummy implementations of the {@link NodeVisitor} methods,
 * this class also provides a static method that implements the default visiting
 * mechanism: The {@code visit()} method is passed a {@code NodeVisitor}, a
 * node, and a corresponding {@code NodeHandler}. It then traverses the whole
 * nodes structure and invokes the visitor for each encountered node.
 * </p>
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 * @param <T> the type of the involved nodes
 */
public class NodeVisitorAdapter<T> implements NodeVisitor<T>
{
    /**
     * Checks whether the visiting process should be aborted. This base
     * implementation always returns <b>false</b>
     *
     * @return a flag whether the visiting process should be aborted
     */
    public boolean terminate()
    {
        return false;
    }

    /**
     * Visits the specified node after its children have been processed. This is
     * an empty dummy implementation.
     *
     * @param node the node
     * @param handler the node handler
     */
    public void visitAfterChildren(T node, NodeHandler<T> handler)
    {
    }

    /**
     * Visits the specified node before its children are processed. This is an
     * empty dummy implementation.
     *
     * @param node the node
     * @param handler the node handler
     */
    public void visitBeforeChildren(T node, NodeHandler<T> handler)
    {
    }

    /**
     * Traverses the nodes structure below the specified root node and calls the
     * visitor for each encountered node. If the {@code terminate()} method of
     * the visitor returns <b>true</b>, the visit operation is aborted. This
     * method provides a default implementation of the visitor mechanism.
     *
     * @param <N> the type of the nodes involved
     * @param visitor the visitor
     * @param node the root node of the hierarchy
     * @param handler the node handler
     * @throws NullPointerException if a required parameter is missing
     */
    public static <N> void visit(NodeVisitor<N> visitor, N node,
            NodeHandler<N> handler)
    {
        if (!visitor.terminate())
        {
            visitor.visitBeforeChildren(node, handler);

            for (Iterator<N> it = handler.getChildren(node).iterator(); it
                    .hasNext()
                    && !visitor.terminate();)
            {
                visit(visitor, it.next(), handler);
            }

            visitor.visitAfterChildren(node, handler);
        }
    }
}
