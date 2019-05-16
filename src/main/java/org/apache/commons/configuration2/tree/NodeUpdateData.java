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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * A simple data class used by node models to store parameters of an update
 * operation.
 * </p>
 * <p>
 * The {@code Configuration} interface provides a method for setting the value
 * of a given key. The passed in value can be a single object or a collection of
 * values. This makes an update operation rather complicated because a
 * collection of query results selected by the passed in key has to be matched
 * to another collection of values - and both collections can have different
 * sizes. Therefore, an update operation may involve changing of existing nodes,
 * adding new nodes (if there are more values than currently existing nodes),
 * and removing nodes (if there are more existing nodes than provided values).
 * This class collects all this information making it possible to actually
 * perform the update based on a passed in instance.
 * </p>
 *
 * @since 2.0
 * @param <T> the type of nodes involved in this update operation
 */
public class NodeUpdateData<T>
{
    /** The map with the query results whose value has to be changed. */
    private final Map<QueryResult<T>, Object> changedValues;

    /** The collection with the new values to be added. */
    private final Collection<Object> newValues;

    /** The collection with query results about the nodes to be removed. */
    private final Collection<QueryResult<T>> removedNodes;

    /** The key of the current update operation. */
    private final String key;

    /**
     * Creates a new instance of {@code NodeUpdateData} and initializes all its
     * properties. All passed in collections are optional and can be
     * <b>null</b>.
     *
     * @param changedValues the map defining the changed values
     * @param newValues the collection with the new values
     * @param removedNodes the collection with the nodes to be removed
     * @param key the key of the update operation
     */
    public NodeUpdateData(final Map<QueryResult<T>, Object> changedValues,
            final Collection<Object> newValues,
            final Collection<QueryResult<T>> removedNodes, final String key)
    {
        this.changedValues = copyMap(changedValues);
        this.newValues = copyCollection(newValues);
        this.removedNodes = copyCollection(removedNodes);
        this.key = key;
    }

    /**
     * Returns an unmodifiable map with the values to be changed. The keys of
     * the map are the query results for the nodes affected, the values are the
     * new values to be assigned to these nodes.
     *
     * @return the map with values to be changed
     */
    public Map<QueryResult<T>, Object> getChangedValues()
    {
        return changedValues;
    }

    /**
     * Returns a collection with the values to be newly added. For these values
     * new nodes have to be created and added under the key stored in this
     * object.
     *
     * @return the collection with new values
     */
    public Collection<Object> getNewValues()
    {
        return newValues;
    }

    /**
     * Adds a collection with the nodes to be removed. These nodes are no longer
     * needed and have to be removed from the node model processing this
     * request.
     *
     * @return the collection with nodes to be removed
     */
    public Collection<QueryResult<T>> getRemovedNodes()
    {
        return removedNodes;
    }

    /**
     * Returns the key for this update operation.
     *
     * @return the key for this operation
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Creates an unmodifiable defensive copy of the passed in map which may be
     * null.
     *
     * @param map the map to be copied
     * @param <K> the type of the keys involved
     * @param <V> the type of the values involved
     * @return the unmodifiable copy
     */
    private static <K, V> Map<K, V> copyMap(final Map<? extends K, ? extends V> map)
    {
        if (map == null)
        {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new HashMap<>(map));
    }

    /**
     * Creates an unmodifiable defensive copy of the passed in collection with
     * may be null.
     *
     * @param col the collection to be copied
     * @param <T> the element type of the collection
     * @return the unmodifiable copy
     */
    private static <T> Collection<T> copyCollection(final Collection<? extends T> col)
    {
        if (col == null)
        {
            return Collections.emptySet();
        }
        return Collections.unmodifiableCollection(new ArrayList<>(col));
    }
}
