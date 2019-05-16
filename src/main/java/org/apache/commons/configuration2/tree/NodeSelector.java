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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * <p>
 * A class for selecting a specific node based on a key or a set of keys.
 * </p>
 * <p>
 * An instance of this class is initialized with the key of a node. It is also
 * possible to concatenate multiple keys - e.g. if a sub key is to be
 * constructed from another sub key. {@code NodeSelector} provides the
 * {@code select()} method which evaluates the wrapped keys on a specified root
 * node and returns the resulting unique target node. The class expects that the
 * key(s) stored in an instance select exactly one target node. If this is not
 * the case, result is <b>null</b> indicating that the selection criteria are
 * not sufficient.
 * </p>
 * <p>
 * Implementation node: Instances of this class are immutable. They can be
 * shared between arbitrary components.
 * </p>
 *
 * @since 2.0
 */
public class NodeSelector
{
    /** Stores the wrapped keys. */
    private final List<String> nodeKeys;

    /**
     * Creates a new instance of {@code NodeSelector} and initializes it with
     * the key to the target node.
     *
     * @param key the key
     */
    public NodeSelector(final String key)
    {
        this(Collections.singletonList(key));
    }

    /**
     * Creates a new instance of {@code NodeSelector} and initializes it with
     * the list of keys to be used as selection criteria.
     *
     * @param keys the keys for selecting nodes
     */
    private NodeSelector(final List<String> keys)
    {
        nodeKeys = keys;
    }

    /**
     * Applies this {@code NodeSelector} on the specified root node. This method
     * applies the selection criteria stored in this object and tries to
     * determine a single target node. If this is successful, the target node is
     * returned. Otherwise, result is <b>null</b>.
     *
     * @param root the root node on which to apply this selector
     * @param resolver the {@code NodeKeyResolver}
     * @param handler the {@code NodeHandler}
     * @return the selected target node or <b>null</b>
     */
    public ImmutableNode select(final ImmutableNode root,
            final NodeKeyResolver<ImmutableNode> resolver,
            final NodeHandler<ImmutableNode> handler)
    {
        List<ImmutableNode> nodes = new LinkedList<>();
        final Iterator<String> itKeys = nodeKeys.iterator();
        getFilteredResults(root, resolver, handler, itKeys.next(), nodes);

        while (itKeys.hasNext())
        {
            final String currentKey = itKeys.next();
            final List<ImmutableNode> currentResults =
                    new LinkedList<>();
            for (final ImmutableNode currentRoot : nodes)
            {
                getFilteredResults(currentRoot, resolver, handler, currentKey,
                        currentResults);
            }
            nodes = currentResults;
        }

        return (nodes.size() == 1) ? nodes.get(0) : null;
    }

    /**
     * Creates a sub {@code NodeSelector} object which uses the key(s) of this
     * selector plus the specified key as selection criteria. This is useful
     * when another selection is to be performed on the results of a first
     * selector.
     *
     * @param subKey the additional key for the sub selector
     * @return the sub {@code NodeSelector} instance
     */
    public NodeSelector subSelector(final String subKey)
    {
        final List<String> keys = new ArrayList<>(nodeKeys.size() + 1);
        keys.addAll(nodeKeys);
        keys.add(subKey);
        return new NodeSelector(keys);
    }

    /**
     * Compares this object with another one. Two instances of
     * {@code NodeSelector} are considered equal if they have the same keys as
     * selection criteria.
     *
     * @param obj the object to be compared
     * @return a flag whether these objects are equal
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof NodeSelector))
        {
            return false;
        }

        final NodeSelector c = (NodeSelector) obj;
        return nodeKeys.equals(c.nodeKeys);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return a hash code
     */
    @Override
    public int hashCode()
    {
        return nodeKeys.hashCode();
    }

    /**
     * Returns a string representation for this object. This string contains the
     * keys to be used as selection criteria.
     *
     * @return a string for this object
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("keys", nodeKeys).toString();
    }

    /**
     * Executes a query for a given key and filters the results for nodes only.
     *
     * @param root the root node for the query
     * @param resolver the {@code NodeKeyResolver}
     * @param handler the {@code NodeHandler}
     * @param key the key
     * @param nodes here the results are stored
     */
    private void getFilteredResults(final ImmutableNode root,
            final NodeKeyResolver<ImmutableNode> resolver,
            final NodeHandler<ImmutableNode> handler, final String key,
            final List<ImmutableNode> nodes)
    {
        final List<QueryResult<ImmutableNode>> results =
                resolver.resolveKey(root, key, handler);
        for (final QueryResult<ImmutableNode> result : results)
        {
            if (!result.isAttributeResult())
            {
                nodes.add(result.getNode());
            }
        }
    }
}
