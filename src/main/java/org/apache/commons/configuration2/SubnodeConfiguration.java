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
package org.apache.commons.configuration2;

import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.InMemoryNodeModel;
import org.apache.commons.configuration2.tree.InMemoryNodeModelSupport;
import org.apache.commons.configuration2.tree.NodeModel;
import org.apache.commons.configuration2.tree.NodeSelector;
import org.apache.commons.configuration2.tree.TrackedNodeModel;

/**
 * <p>
 * A specialized hierarchical configuration class with a node model that uses a
 * tracked node of another node model as its root node.
 * </p>
 * <p>
 * Configurations of this type are initialized with a special {@link NodeModel}
 * operating on a specific tracked node of the parent configuration and the
 * corresponding {@link NodeSelector}. All property accessor methods are
 * evaluated relative to this root node. A good use case for a
 * {@code SubnodeConfiguration} is when multiple properties from a specific sub
 * tree of the whole configuration need to be accessed. Then a
 * {@code SubnodeConfiguration} can be created with the parent node of the
 * affected sub tree as root node. This allows for simpler property keys and is
 * also more efficient.
 * </p>
 * <p>
 * By making use of a tracked node as root node, a {@code SubnodeConfiguration}
 * and its parent configuration initially operate on the same hierarchy of
 * configuration nodes. So if modifications are performed at the subnode
 * configuration, these changes are immediately visible in the parent
 * configuration. Analogously will updates of the parent configuration affect
 * the {@code SubnodeConfiguration} if the sub tree spanned by the
 * {@code SubnodeConfiguration}'s root node is involved.
 * </p>
 * <p>
 * Note that by making use of a {@code NodeSelector} the
 * {@code SubnodeConfiguration} is not associated with a physical node instance,
 * but the selection criteria stored in the selector are evaluated after each
 * change of the nodes structure. As an example consider that the selector uses
 * a key with an index into a list element, say index 2. Now if an update occurs
 * on the underlying nodes structure which removes the first element in this
 * list structure, the {@code SubnodeConfiguration} still references the element
 * with index 2 which is now another one.
 * </p>
 * <p>
 * There are also possible changes of the underlying nodes structure which
 * completely detach the {@code SubnodeConfiguration} from its parent
 * configuration. For instance, the key referenced by the
 * {@code SubnodeConfiguration} could be removed in the parent configuration. If
 * this happens, the {@code SubnodeConfiguration} stays functional; however, it
 * now operates on a separate node model than its parent configuration. Changes
 * made by one configuration are no longer visible for the other one (as the
 * node models have no longer overlapping nodes, there is no way to have a
 * synchronization here).
 * </p>
 * <p>
 * When a subnode configuration is created, it inherits the settings of its
 * parent configuration, e.g. some flags like the
 * {@code throwExceptionOnMissing} flag or the settings for handling list
 * delimiters) or the expression engine. If these settings are changed later in
 * either the subnode or the parent configuration, the changes are not visible
 * for each other. So you could create a subnode configuration, and change its
 * expression engine without affecting the parent configuration.
 * </p>
 * <p>
 * Because the {@code SubnodeConfiguration} operates on the same nodes structure
 * as its parent it uses the same {@code Synchronizer} instance per default.
 * This means that locks held on one {@code SubnodeConfiguration} also impact
 * the parent configuration and all of its other {@code SubnodeConfiguration}
 * objects. You should not change this without a good reason! Otherwise, there
 * is the risk of data corruption when multiple threads access these
 * configuration concurrently.
 * </p>
 * <p>
 * From its purpose this class is quite similar to {@link SubsetConfiguration}.
 * The difference is that a subset configuration of a hierarchical configuration
 * may combine multiple configuration nodes from different sub trees of the
 * configuration, while all nodes in a subnode configuration belong to the same
 * sub tree. If an application can live with this limitation, it is recommended
 * to use this class instead of {@code SubsetConfiguration} because creating a
 * subset configuration is more expensive than creating a subnode configuration.
 * </p>
 * <p>
 * It is strongly recommended to create {@code SubnodeConfiguration} instances
 * only through the {@code configurationAt()} methods of a hierarchical
 * configuration. These methods ensure that all necessary initializations are
 * done. Creating instances manually without doing proper initialization may
 * break some of the functionality provided by this class.
 * </p>
 *
 * @since 1.3
 */
public class SubnodeConfiguration extends BaseHierarchicalConfiguration
{
    /** Stores the parent configuration. */
    private final BaseHierarchicalConfiguration parent;

    /** The node selector selecting the root node of this configuration. */
    private final NodeSelector rootSelector;

    /**
     * Creates a new instance of {@code SubnodeConfiguration} and initializes it
     * with all relevant properties.
     *
     * @param parent the parent configuration
     * @param model the {@code TrackedNodeModel} to be used for this configuration
     * @throws IllegalArgumentException if a required argument is missing
     */
    public SubnodeConfiguration(final BaseHierarchicalConfiguration parent,
                                final TrackedNodeModel model)
    {
        super(model);
        if (parent == null)
        {
            throw new IllegalArgumentException(
                    "Parent configuration must not be null!");
        }
        if (model == null)
        {
            throw new IllegalArgumentException("Node model must not be null!");
        }

        this.parent = parent;
        rootSelector = model.getSelector();
    }

    /**
     * Returns the parent configuration of this subnode configuration.
     *
     * @return the parent configuration
     */
    public BaseHierarchicalConfiguration getParent()
    {
        return parent;
    }

    /**
     * Returns the selector to the root node of this configuration.
     *
     * @return the {@code NodeSelector} to the root node
     */
    public NodeSelector getRootSelector()
    {
        return rootSelector;
    }

    /**
     * Closes this sub configuration. This method closes the underlying
     * {@link TrackedNodeModel}, thus causing the tracked node acting as root
     * node to be released. Per default, this happens automatically when the
     * model is claimed by the garbage collector. By calling this method
     * explicitly, it can be indicated that this configuration is no longer used
     * and that resources used by it can be freed immediately.
     */
    public void close()
    {
        (getTrackedModel()).close();
    }

    /**
     * {@inheritDoc} This implementation returns a newly created node model
     * with the correct root node set. Note that this model is not used for
     * property access, but only made available to clients that need to
     * operate on the node structure of this {@code SubnodeConfiguration}.
     * Be aware that the implementation of this method is not very efficient.
     */
    @Override
    public InMemoryNodeModel getNodeModel()
    {
        final ImmutableNode root =
                getParent().getNodeModel().getTrackedNode(getRootSelector());
        return new InMemoryNodeModel(root);
    }

    /**
     * Returns the node model of the root configuration.
     * {@code SubnodeConfiguration} instances created from a hierarchical
     * configuration operate on the same node model, using different nodes as
     * their local root nodes. With this method the top-level node model can be
     * obtained. It works even in constellations where a
     * {@code SubnodeConfiguration} has been created from another
     * {@code SubnodeConfiguration}.
     *
     * @return the root node model
     * @since 2.2
     */
    public InMemoryNodeModel getRootNodeModel()
    {
        if (getParent() instanceof SubnodeConfiguration)
        {
            return ((SubnodeConfiguration) getParent()).getRootNodeModel();
        }
        return getParent().getNodeModel();
    }

    /**
     * {@inheritDoc} This implementation returns a copy of the current node
     * model with the same settings. However, it has to be ensured that the
     * track count for the node selector is increased.
     *
     * @return the node model for the clone
     */
    @Override
    protected NodeModel<ImmutableNode> cloneNodeModel()
    {
        final InMemoryNodeModel parentModel =
                (InMemoryNodeModel) getParent().getModel();
        parentModel.trackNode(getRootSelector(), getParent());
        return new TrackedNodeModel(getParent(), getRootSelector(), true);
    }

    /**
     * {@inheritDoc} This implementation returns a sub selector of the selector
     * of this configuration.
     */
    @Override
    protected NodeSelector getSubConfigurationNodeSelector(final String key)
    {
        return getRootSelector().subSelector(key);
    }

    /**
     * {@inheritDoc} This implementation returns the parent model of the
     * {@link TrackedNodeModel} used by this configuration.
     */
    @Override
    protected InMemoryNodeModel getSubConfigurationParentModel()
    {
        return getTrackedModel().getParentModel();
    }

    /**
     * {@inheritDoc} This implementation makes sure that the correct node model
     * (the one of the parent) is used for the new sub configuration.
     */
    @Override
    protected SubnodeConfiguration createSubConfigurationForTrackedNode(
            final NodeSelector selector, final InMemoryNodeModelSupport parentModelSupport)
    {
        return super.createSubConfigurationForTrackedNode(selector, getParent());
    }

    /**
     * Convenience method that returns the tracked model used by this sub
     * configuration.
     *
     * @return the {@code TrackedNodeModel}
     */
    private TrackedNodeModel getTrackedModel()
    {
        return (TrackedNodeModel) getModel();
    }
}
