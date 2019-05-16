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
 * A special {@code NodeHandler} implementation for tracked nodes.
 * </p>
 * <p>
 * While basic access to a tracked node works in the same way as for usual
 * immutable nodes, there are differences for other operations. For instance,
 * the root node of the hierarchy is always the tracked node itself. Also the
 * parent mapping requires some special attention: as long as the node is not
 * detached, the parent mapping of the model to which the node belongs can be
 * used.
 * </p>
 * <p>
 * This class inherits the major part of the {@code NodeHandler} implementation
 * from its base class. In order to implement the parent mapping, an underlying
 * {@code NodeHandler} object has to be passed at construction time which
 * contains this information; requests for a node's parent are delegated to this
 * handler. Further, the root node has to be provided explicitly.
 * </p>
 *
 * @since 2.0
 */
class TrackedNodeHandler extends AbstractImmutableNodeHandler
{
    /** The root node. */
    private final ImmutableNode rootNode;

    /** The handler for querying the parent mapping. */
    private final NodeHandler<ImmutableNode> parentHandler;

    /**
     * Creates a new instance of {@code TrackedNodeHandler} and initializes it
     * with all required information.
     *
     * @param root the root node of the represented hierarchy
     * @param handler an underlying handler for delegation
     */
    public TrackedNodeHandler(final ImmutableNode root,
            final NodeHandler<ImmutableNode> handler)
    {
        rootNode = root;
        parentHandler = handler;
    }

    /**
     * Returns the parent handler. This is the {@code NodeHandler} which is
     * consulted for determining a node's parent node.
     *
     * @return the parent {@code NodeHandler}
     */
    public NodeHandler<ImmutableNode> getParentHandler()
    {
        return parentHandler;
    }

    /**
     * {@inheritDoc} This implementation delegates to the handler with the
     * parent mapping.
     */
    @Override
    public ImmutableNode getParent(final ImmutableNode node)
    {
        return getParentHandler().getParent(node);
    }

    /**
     * {@inheritDoc} This implementation returns the root node passed at
     * construction time.
     */
    @Override
    public ImmutableNode getRootNode()
    {
        return rootNode;
    }
}
