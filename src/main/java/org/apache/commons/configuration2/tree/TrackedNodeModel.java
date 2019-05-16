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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
 * An instance of this class is constructed with an
 * {@link InMemoryNodeModelSupport} object providing a reference to the
 * underlying {@code InMemoryNodeModel} and the {@link NodeSelector} pointing to
 * the tracked node acting as this model's root node. The {@code NodeModel}
 * operations are implemented by delegating to the wrapped
 * {@code InMemoryNodeModel} object specifying the selector to the tracked node
 * as target root node for the update transaction. Note that the tracked node
 * can become detached at any time. This situation is handled transparently by
 * the implementation of {@code InMemoryNodeModel}. The reason for using an
 * {@code InMemoryNodeModelSupport} object rather than an
 * {@code InMemoryNodeModel} directly is that this additional layer of
 * indirection can be used for performing special initializations on the model
 * before it is returned to the {@code TrackedNodeModel} object. This is needed
 * by some dynamic configuration implementations, e.g. by
 * {@code CombinedConfiguration}.
 * </p>
 * <p>
 * If the tracked node acting as root node is exclusively used by this model, it
 * should be released when this model is no longer needed. This can be done
 * manually by calling the {@link #close()} method. It is also possible to pass
 * a value of <strong>true</strong> to the {@code untrackOnFinalize} argument of
 * the constructor. This causes {@code close()} to be called automatically if
 * this object gets claimed by the garbage collector.
 * </p>
 * <p>
 * As {@code InMemoryNodeModel}, this class is thread-safe.
 * </p>
 *
 * @since 2.0
 */
public class TrackedNodeModel implements NodeModel<ImmutableNode>
{
    /** Stores the underlying parent model. */
    private final InMemoryNodeModelSupport parentModelSupport;

    /** The selector for the managed tracked node. */
    private final NodeSelector selector;

    /**
     * A flag whether the tracked not should be released when this object is
     * finalized.
     */
    private final boolean releaseTrackedNodeOnFinalize;

    /** A flag whether this model has already been closed. */
    private final AtomicBoolean closed;

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
     * @param modelSupport the underlying {@code InMemoryNodeModelSupport} (must not be
     *        <b>null</b>)
     * @param sel the selector to the root node of this model (must not be
     *        <b>null</b>)
     * @param untrackOnFinalize a flag whether the tracked node should be
     *        released on finalization
     * @throws IllegalArgumentException if a required parameter is missing
     */
    public TrackedNodeModel(final InMemoryNodeModelSupport modelSupport, final NodeSelector sel,
            final boolean untrackOnFinalize)
    {
        if (modelSupport == null)
        {
            throw new IllegalArgumentException(
                    "Underlying model support must not be null!");
        }
        if (sel == null)
        {
            throw new IllegalArgumentException("Selector must not be null!");
        }

        parentModelSupport = modelSupport;
        selector = sel;
        releaseTrackedNodeOnFinalize = untrackOnFinalize;
        closed = new AtomicBoolean();
    }

    /**
     * Returns the {@code InMemoryNodeModelSupport} object which is used to gain
     * access to the underlying node model.
     *
     * @return the associated {@code InMemoryNodeModelSupport} object
     */
    public InMemoryNodeModelSupport getParentModelSupport()
    {
        return parentModelSupport;
    }

    /**
     * Returns the parent model. Operations on this model are delegated to this
     * parent model specifying the selector to the tracked node.
     *
     * @return the parent model
     */
    public InMemoryNodeModel getParentModel()
    {
        return getParentModelSupport().getNodeModel();
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

    @Override
    public void setRootNode(final ImmutableNode newRoot)
    {
        getParentModel().replaceTrackedNode(getSelector(), newRoot);
    }

    @Override
    public NodeHandler<ImmutableNode> getNodeHandler()
    {
        return getParentModel().getTrackedNodeHandler(getSelector());
    }

    @Override
    public void addProperty(final String key, final Iterable<?> values,
            final NodeKeyResolver<ImmutableNode> resolver)
    {
        getParentModel().addProperty(key, getSelector(), values, resolver);
    }

    @Override
    public void addNodes(final String key, final Collection<? extends ImmutableNode> nodes,
            final NodeKeyResolver<ImmutableNode> resolver)
    {
        getParentModel().addNodes(key, getSelector(), nodes, resolver);
    }

    @Override
    public void setProperty(final String key, final Object value,
            final NodeKeyResolver<ImmutableNode> resolver)
    {
        getParentModel().setProperty(key, getSelector(), value, resolver);
    }

    @Override
    public List<QueryResult<ImmutableNode>> clearTree(final String key,
            final NodeKeyResolver<ImmutableNode> resolver)
    {
        return getParentModel().clearTree(key, getSelector(), resolver);
    }

    @Override
    public void clearProperty(final String key,
            final NodeKeyResolver<ImmutableNode> resolver)
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
    @Override
    public void clear(final NodeKeyResolver<ImmutableNode> resolver)
    {
        getParentModel().clearTree(null, getSelector(), resolver);
    }

    /**
     * {@inheritDoc} This implementation returns the tracked node instance
     * acting as root node of this model.
     */
    @Override
    public ImmutableNode getInMemoryRepresentation()
    {
        return getNodeHandler().getRootNode();
    }

    /**
     * Closes this model. This causes the tracked node this model is based upon
     * to be released (i.e. {@link InMemoryNodeModel#untrackNode(NodeSelector)}
     * is called). This method should be called when this model is no longer
     * needed. This implementation is idempotent; it is safe to call
     * {@code close()} multiple times - only the first invocation has an effect.
     * After this method has been called this model can no longer be used
     * because there is no guarantee that the node can still be accessed from
     * the parent model.
     */
    public void close()
    {
        if (closed.compareAndSet(false, true))
        {
            getParentModel().untrackNode(getSelector());
        }
    }

    /**
     * {@inheritDoc} This implementation calls {@code close()} if the
     * {@code untrackOnFinalize} flag was set when this instance was
     * constructed. While this is not 100 percent reliable, it is better than
     * keeping the tracked node hanging around. Note that it is not a problem if
     * {@code close()} already had been invoked manually because this method is
     * idempotent.
     *
     * @see #close()
     */
    @Override
    protected void finalize() throws Throwable
    {
        if (isReleaseTrackedNodeOnFinalize())
        {
            close();
        }
        super.finalize();
    }
}
