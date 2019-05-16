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

/**
 * <p>
 * Definition of an interface describing a model based on a nodes structure.
 * </p>
 * <p>
 * This interface can be used for dealing with hierarchical, tree-like data. It
 * defines basic operations for manipulating the tree structure which use keys
 * to select the nodes affected.
 * </p>
 * <p>
 * The idea behind this interface is that concrete implementations can be used
 * by hierarchical configurations. This makes it possible to integrate various
 * hierarchical structures with the API of a hierarchical configuration, e.g.
 * configuration nodes stored in memory, JNDI contexts, or other structures. The
 * configuration object interacts with the underlying data structure via this
 * interface. For more complex operations access to an {@link ExpressionEngine}
 * may be required in order to interpret the passed in keys. For these purposes
 * a {@link NodeKeyResolver} has to be provided which knows how to deal with
 * keys.
 * </p>
 *
 * @since 2.0
 * @param <T> the type of the nodes managed by this model
 */
public interface NodeModel<T>
{
    /**
     * Sets a new root node for this model. The whole structure is replaced by
     * the new node and its children.
     *
     * @param newRoot the new root node to be set (can be <b>null</b>, then an
     *        empty root node is set)
     */
    void setRootNode(T newRoot);

    /**
     * Returns a {@code NodeHandler} for dealing with the nodes managed by this
     * model.
     *
     * @return the {@code NodeHandler}
     */
    NodeHandler<T> getNodeHandler();

    /**
     * Adds a new property to this node model consisting of an arbitrary number
     * of values. The key for the add operation is provided. For each value a
     * new node has to be added. The passed in resolver is queried for a
     * {@link NodeAddData} object defining the add operation to be performed.
     *
     * @param key the key
     * @param values the values to be added at the position defined by the key
     * @param resolver the {@code NodeKeyResolver}
     */
    void addProperty(String key, Iterable<?> values, NodeKeyResolver<T> resolver);

    /**
     * Adds a collection of new nodes to this model. This operation corresponds
     * to the {@code addNodes()} method of the {@code HierarchicalConfiguration}
     * interface. The new nodes are either added to an existing node (if the
     * passed in key selects exactly one node) or to a newly created node. The
     * passed in {@code NodeKeyResolver} is used to interpret the given key.
     *
     * @param key the key
     * @param nodes the collection of nodes to be added (may be <b>null</b>)
     * @param resolver the {@code NodeKeyResolver}
     * @throws IllegalArgumentException if the key references an attribute (of
     *         course, it is not possible to add something to an attribute)
     */
    void addNodes(String key, Collection<? extends T> nodes,
            NodeKeyResolver<T> resolver);

    /**
     * Changes the value of a property. This is a more complex operation as it
     * might involve adding, updating, or deleting nodes and attributes from the
     * model. The object representing the new value is passed to the
     * {@code NodeKeyResolver} which will produce a corresponding
     * {@link NodeUpdateData} object. Based on the content of this object,
     * update operations are performed.
     *
     * @param key the key
     * @param value the new value for this property (to be evaluated by the
     *        {@code NodeKeyResolver})
     * @param resolver the {@code NodeKeyResolver}
     */
    void setProperty(String key, Object value, NodeKeyResolver<T> resolver);

    /**
     * Removes the sub trees defined by the given key from this model. All nodes
     * selected by this key are retrieved from the specified
     * {@code NodeKeyResolver} and removed from the model.
     *
     * @param key the key selecting the properties to be removed
     * @param resolver the {@code NodeKeyResolver}
     * @return an object with information about the data removed
     */
    Object clearTree(String key, NodeKeyResolver<T> resolver);

    /**
     * Clears the value of a property. This method is similar to
     * {@link #clearTree(String, NodeKeyResolver)}: However, the nodes
     * referenced by the passed in key are not removed completely, but only
     * their value is set to <b>null</b>.
     *
     * @param key the key selecting the properties to be cleared
     * @param resolver the {@code NodeKeyResolver}
     */
    void clearProperty(String key, NodeKeyResolver<T> resolver);

    /**
     * Removes all data from this model.
     *
     * @param resolver the {@code NodeKeyResolver}
     */
    void clear(NodeKeyResolver<T> resolver);

    /**
     * Returns a representation of the data stored in this model in form of a
     * nodes hierarchy of {@code ImmutableNode} objects. A concrete model
     * implementation can use an arbitrary means to store its data. When a
     * model's data is to be used together with other functionality of the
     * <em>Configuration</em> library (e.g. when combining multiple
     * configuration sources) it has to be transformed into a common format.
     * This is done by this method. {@code ImmutableNode} is a generic
     * representation of a hierarchical structure. Thus, it should be possible
     * to generate a corresponding structure from arbitrary model data.
     *
     * @return the root node of an in-memory hierarchy representing the data
     *         stored in this model
     */
    ImmutableNode getInMemoryRepresentation();
}
