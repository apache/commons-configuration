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

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * A base class for configuration implementations based on YAML structures.
 * </p>
 * <p>
 * This base class offers functionality related to YAML-like data structures
 * based on maps. Such a map has strings as keys and arbitrary objects as
 * values. The class offers methods to transform such a map into a hierarchy
 * of {@link ImmutableNode} objects and vice versa.
 * </p>
 *
 * @since 2.2
 */
public class AbstractYAMLBasedConfiguration extends BaseHierarchicalConfiguration
{
    /**
     * Creates a new instance of {@code AbstractYAMLBasedConfiguration}.
     */
    protected AbstractYAMLBasedConfiguration()
    {
        initLogger(new ConfigurationLogger(getClass()));
    }

    /**
     * Creates a new instance of {@code AbstractYAMLBasedConfiguration} as a
     * copy of the specified configuration.
     *
     * @param c the configuration to be copied
     */
    protected AbstractYAMLBasedConfiguration(
            final HierarchicalConfiguration<ImmutableNode> c)
    {
        super(c);
        initLogger(new ConfigurationLogger(getClass()));
    }

    /**
     * Loads this configuration from the content of the specified map. The data
     * in the map is transformed into a hierarchy of {@link ImmutableNode}
     * objects.
     *
     * @param map the map to be processed
     */
    protected void load(final Map<String, Object> map)
    {
        final List<ImmutableNode> roots = constructHierarchy("", map);
        getNodeModel().setRootNode(roots.get(0));
    }

    /**
     * Constructs a YAML map, i.e. String -&gt; Object from a given configuration
     * node.
     *
     * @param node The configuration node to create a map from.
     * @return A Map that contains the configuration node information.
     */
    protected Map<String, Object> constructMap(final ImmutableNode node)
    {
        final Map<String, Object> map = new HashMap<>(node.getChildren().size());
        for (final ImmutableNode cNode : node.getChildren())
        {
            final Object value = cNode.getChildren().isEmpty() ? cNode.getValue()
                    : constructMap(cNode);
            addEntry(map, cNode.getNodeName(), value);
        }
        return map;
    }

    /**
     * Adds a key value pair to a map, taking list structures into account. If a
     * key is added which is already present in the map, this method ensures
     * that a list is created.
     *
     * @param map the map
     * @param key the key
     * @param value the value
     */
    private static void addEntry(final Map<String, Object> map, final String key,
            final Object value)
    {
        final Object oldValue = map.get(key);
        if (oldValue == null)
        {
            map.put(key, value);
        }
        else if (oldValue instanceof Collection)
        {
            // safe case because the collection was created by ourselves
            @SuppressWarnings("unchecked")
            final
            Collection<Object> values = (Collection<Object>) oldValue;
            values.add(value);
        }
        else
        {
            final Collection<Object> values = new ArrayList<>();
            values.add(oldValue);
            values.add(value);
            map.put(key, values);
        }
    }

    /**
     * Creates a part of the hierarchical nodes structure of the resulting
     * configuration. The passed in element is converted into one or multiple
     * configuration nodes. (If list structures are involved, multiple nodes are
     * returned.)
     *
     * @param key the key of the new node(s)
     * @param elem the element to be processed
     * @return a list with configuration nodes representing the element
     */
    private static List<ImmutableNode> constructHierarchy(final String key,
            final Object elem)
    {
        if (elem instanceof Map)
        {
            return parseMap((Map<String, Object>) elem, key);
        }
        else if (elem instanceof Collection)
        {
            return parseCollection((Collection<Object>) elem, key);
        }
        else
        {
            return Collections.singletonList(
                    new ImmutableNode.Builder().name(key).value(elem).create());
        }
    }

    /**
     * Parses a map structure. The single keys of the map are processed
     * recursively.
     *
     * @param map the map to be processed
     * @param key the key under which this map is to be stored
     * @return a node representing this map
     */
    private static List<ImmutableNode> parseMap(final Map<String, Object> map, final String key)
    {
        final ImmutableNode.Builder subtree = new ImmutableNode.Builder().name(key);
        for (final Map.Entry<String, Object> entry : map.entrySet())
        {
            final List<ImmutableNode> children =
                    constructHierarchy(entry.getKey(), entry.getValue());
            for (final ImmutableNode child : children)
            {
                subtree.addChild(child);
            }
        }
        return Collections.singletonList(subtree.create());
    }

    /**
     * Parses a collection structure. The elements of the collection are
     * processed recursively.
     *
     * @param col the collection to be processed
     * @param key the key under which this collection is to be stored
     * @return a node representing this collection
     */
    private static List<ImmutableNode> parseCollection(final Collection<Object> col, final String key)
    {
        final List<ImmutableNode> nodes = new ArrayList<>(col.size());
        for (final Object elem : col)
        {
            nodes.addAll(constructHierarchy(key, elem));
        }
        return nodes;
    }

    /**
     * Internal helper method to wrap an exception in a
     * {@code ConfigurationException}.
     * @param e the exception to be wrapped
     * @throws ConfigurationException the resulting exception
     */
    static void rethrowException(final Exception e) throws ConfigurationException
    {
        if (e instanceof ClassCastException)
        {
            throw new ConfigurationException("Error parsing", e);
        }
        throw new ConfigurationException("Unable to load the configuration",
                e);
    }
}
