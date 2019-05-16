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

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Definition of an interface which allows resolving a (property) key for
 * different manipulating operations.
 * </p>
 * <p>
 * This interface is used when interacting with a node model. It is an
 * abstraction over a concrete {@link ExpressionEngine} instance. It also
 * implements some functionality for creating special helper objects for the
 * processing of complex update operations.
 * </p>
 *
 * @since 2.0
 * @param <T> the type of the nodes supported by this resolver
 */
public interface NodeKeyResolver<T>
{
    /**
     * Performs a query for the specified key on the given root node. This is a
     * thin wrapper over the {@code query()} method of an
     * {@link ExpressionEngine}.
     *
     * @param root the root node
     * @param key the key to be resolved
     * @param handler the {@code NodeHandler}
     * @return a list with query results
     */
    List<QueryResult<T>> resolveKey(T root, String key, NodeHandler<T> handler);

    /**
     * Performs a query for the specified key on the given root node returning
     * only node results. Some operations require results of type node and do
     * not support attributes (e.g. for tracking nodes). This operation can be
     * used in such cases. It works like {@code resolveKey()}, but filters only
     * for results of type node.
     *
     * @param root the root node
     * @param key the key to be resolved
     * @param handler the {@code NodeHandler}
     * @return a list with the resolved nodes
     */
    List<T> resolveNodeKey(T root, String key, NodeHandler<T> handler);

    /**
     * Resolves a key of an add operation. Result is a {@code NodeAddData}
     * object containing all information for actually performing the add
     * operation at the specified key.
     *
     * @param root the root node
     * @param key the key to be resolved
     * @param handler the {@code NodeHandler}
     * @return a {@code NodeAddData} object to be used for the add operation
     */
    NodeAddData<T> resolveAddKey(T root, String key, NodeHandler<T> handler);

    /**
     * Resolves a key for an update operation. Result is a
     * {@code NodeUpdateData} object containing all information for actually
     * performing the update operation at the specified key using the provided
     * new value object.
     *
     * @param root the root node
     * @param key the key to be resolved
     * @param newValue the new value for the key to be updated; this can be a
     *        single value or a container for multiple values
     * @param handler the {@code NodeHandler}
     * @return a {@code NodeUpdateData} object to be used for this update
     *         operation
     */
    NodeUpdateData<T> resolveUpdateKey(T root, String key, Object newValue,
            NodeHandler<T> handler);

    /**
     * Generates a unique key for the specified node. This method is used if
     * keys have to be generated for nodes received as query results. An
     * implementation must generate a canonical key which is compatible with the
     * current expression engine. The passed in map can be used by an
     * implementation as cache. It is created initially by the caller and then
     * passed in subsequent calls. An implementation may use this to avoid that
     * keys for nodes already encountered have to be generated again.
     *
     * @param node the node in question
     * @param cache a map serving as cache
     * @param handler the {@code NodeHandler}
     * @return a key for the specified node
     */
    String nodeKey(T node, Map<T, String> cache, NodeHandler<T> handler);
}
