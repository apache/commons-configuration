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

import java.util.Collection;

/**
 * <p>
 * A specialized {@code NodeModel} implementation that uses a tracked node
 * managed by an {@link InMemoryNodeModel} object as root node.
 * </p>
 * <p>
 * Models of this type are useful when working on specific sub trees of a nodes
 * structure. This is the case for instance for a {@code SubnodeConfiguration}.
 * </p>
 * <p>
 * An instance of this class is constructed with a reference to the underlying
 * {@code InMemoryNodeModel} and the {@link NodeSelector} pointing to the
 * tracked node acting as this model's root node. The {@code NodeModel}
 * operations are implemented by delegating to the wrapped
 * {@code InMemoryNodeModel} object specifying the selector to the tracked node
 * as target root node for the update transaction. Note that the tracked node
 * can become detached at any time. This situation is handled transparently by
 * the implementation of {@code InMemoryNodeModel}.
 * </p>
 * <p>
 * As {@code InMemoryNodeModel}, this class is thread-safe.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public class TrackedNodeModel implements NodeModel<ImmutableNode>
{
    /** Stores the underlying parent model. */
    private final InMemoryNodeModel parentModel;

    /** The selector for the managed tracked node. */
    private final NodeSelector selector;

    /**
     * A flag whether the tracked not should be released when this object is
     * finalized.
     */
    private final boolean releaseTrackedNodeOnFinalize;

    /**
     * Creates a new instance of {@code TrackedNodeModel} and initializes it
     * with the given underlying model and the selector to the root node. The
     * boolean argument controls whether the associated tracked node should be
     * released when this object gets finalized. This allows the underlying
     * model to free some resources. If used as model within a
     * {@code SubnodeConfiguration}, there is typically no way to discard the
     * model explicitly. Therefore, it makes sense to do this automatically on
     * finalization.
     *
     * @param model the underlying {@code InMemoryNodeModel} (must not be
     *        <b>null</b>)
     * @param sel the selector to the root node of this model (must not be
     *        <b>null</b>)
     * @param untrackOnFinalize a flag whether the tracked node should be
     *        released on finalization
     * @throws IllegalArgumentException if a required parameter is missing
     */
    public TrackedNodeModel(InMemoryNodeModel model, NodeSelector sel,
            boolean untrackOnFinalize)
    {
        if (model == null)
        {
            throw new IllegalArgumentException(
                    "Underlying model must not be null!");
        }
        if (sel == null)
        {
            throw new IllegalArgumentException("Selector must not be null!");
        }

        parentModel = model;
        selector = sel;
        releaseTrackedNodeOnFinalize = untrackOnFinalize;
    }

    /**
     * Returns the parent model. Operations on this model are delegated to this
     * parent model specifying the selector to the tracked node.
     *
     * @return the parent model
     */
    public InMemoryNodeModel getParentModel()
    {
        return parentModel;
    }

    /**
     * Returns the {@code NodeSelector} pointing to the tracked node managed by
     * this model.
     *
     * @return the tracked node selector
     */
    public NodeSelector getSelector()
    {
        return selector;
    }

    /**
     * Returns the flag whether the managed tracked node is to be released when
     * this object gets finalized. This method returns the value of the
     * corresponding flag passed to the constructor. If result is true, the
     * underlying model is asked to untrack the managed node when this object is
     * claimed by the GC.
     *
     * @return a flag whether the managed tracked node should be released when
     *         this object dies
     * @see InMemoryNodeModel#untrackNode(NodeSelector)
     */
    public boolean isReleaseTrackedNodeOnFinalize()
    {
        return releaseTrackedNodeOnFinalize;
    }

    public void setRootNode(ImmutableNode newRoot)
    {
        getParentModel().replaceTrackedNode(getSelector(), newRoot);
    }

    public NodeHandler<ImmutableNode> getNodeHandler()
    {
        return getParentModel().getTrackedNodeHandler(getSelector());
    }

    public void addProperty(String key, Iterable<?> values,
            NodeKeyResolver<ImmutableNode> resolver)
    {
        getParentModel().addProperty(key, getSelector(), values, resolver);
    }

    public void addNodes(String key, Collection<? extends ImmutableNode> nodes,
            NodeKeyResolver<ImmutableNode> resolver)
    {
        getParentModel().addNodes(key, getSelector(), nodes, resolver);
    }

    public void setProperty(String key, Object value,
            NodeKeyResolver<ImmutableNode> resolver)
    {
        getParentModel().setProperty(key, getSelector(), value, resolver);
    }

    public void clearTree(String key, NodeKeyResolver<ImmutableNode> resolver)
    {
        getParentModel().clearTree(key, getSelector(), resolver);
    }

    public void clearProperty(String key,
            NodeKeyResolver<ImmutableNode> resolver)
    {
        getParentModel().clearProperty(key, getSelector(), resolver);
    }

    /**
     * {@inheritDoc} This implementation clears the sub tree spanned by the
     * associate tracked node. This has the side effect that this in any case
     * becomes detached.
     *
     * @param resolver
     */
    public void clear(NodeKeyResolver<ImmutableNode> resolver)
    {
        getParentModel().clearTree(null, getSelector(), resolver);
    }

    /**
     * {@inheritDoc} This implementation calls
     * {@link InMemoryNodeModel#untrackNode(NodeSelector)} on the underlying
     * model if the corresponding flag has been set at construction time. While
     * this is not 100 percent reliable, it is better than keeping the tracked
     * node hanging around.
     */
    @Override
    protected void finalize() throws Throwable
    {
        if (isReleaseTrackedNodeOnFinalize())
        {
            getParentModel().untrackNode(getSelector());
        }
        super.finalize();
    }
}
