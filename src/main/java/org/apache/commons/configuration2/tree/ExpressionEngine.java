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

/**
 * <p>
 * Definition of an interface for evaluating keys for hierarchical
 * configurations.
 * </p>
 * <p>
 * An <em>expression engine</em> knows how to map a key for a configuration's
 * property to a single or a set of configuration nodes. Thus it defines the way
 * how properties are addressed in this configuration. Methods of a
 * configuration that have to handle property keys (e.g. {@code getProperty()}
 * or {@code addProperty()} do not interpret the passed in keys on their own,
 * but delegate this task to an associated expression engine. This expression
 * engine will then find out, which configuration nodes are addressed by the
 * key.
 * </p>
 * <p>
 * Separating the task of evaluating property keys from the configuration object
 * has the advantage that multiple different expression languages (i.e. ways for
 * querying or setting properties) can be supported. Just set a suitable
 * implementation of this interface as the configuration's expression engine,
 * and you can use the syntax provided by this implementation.
 * </p>
 * <p>
 * An {@code ExpressionEngine} can deal with nodes of different types. To
 * achieve this, a {@link NodeHandler} for the desired type must be passed to
 * the methods.
 * </p>
 *
 * @since 1.3
 */
public interface ExpressionEngine
{
    /**
     * Finds the nodes and/or attributes that are matched by the specified key.
     * This is the main method for interpreting property keys. An implementation
     * must traverse the given root node and its children to find all results
     * that are matched by the given key. If the key is not correct in the
     * syntax provided by that implementation, it is free to throw a (runtime)
     * exception indicating this error condition. The passed in
     * {@code NodeHandler} can be used to gather the required information from
     * the node object.
     *
     * @param <T> the type of the node to be processed
     * @param root the root node of a hierarchy of nodes
     * @param key the key to be evaluated
     * @param handler the {@code NodeHandler} for accessing the node
     * @return a list with the results that are matched by the key (should never
     *         be <b>null</b>)
     */
    <T> List<QueryResult<T>> query(T root, String key, NodeHandler<T> handler);

    /**
     * Returns the key for the specified node in the expression language
     * supported by an implementation. This method is called whenever a property
     * key for a node has to be constructed, e.g. by the
     * {@link org.apache.commons.configuration2.Configuration#getKeys()
     * getKeys()} method.
     *
     * @param <T> the type of the node to be processed
     * @param node the node, for which the key must be constructed
     * @param parentKey the key of this node's parent (can be <b>null</b> for
     *        the root node)
     * @param handler the {@code NodeHandler} for accessing the node
     * @return this node's key
     */
    <T> String nodeKey(T node, String parentKey, NodeHandler<T> handler);

    /**
     * Returns the key of an attribute. The passed in {@code parentKey} must
     * reference the parent node of the attribute. A concrete implementation
     * must concatenate this parent key with the attribute name to a valid key
     * for this attribute.
     *
     * @param parentKey the key to the node owning this attribute
     * @param attributeName the name of the attribute in question
     * @return the resulting key referencing this attribute
     */
    String attributeKey(String parentKey, String attributeName);

    /**
     * Determines a &quot;canonical&quot; key for the specified node in the
     * expression language supported by this implementation. This means that
     * always a unique key if generated pointing to this specific node. For most
     * concrete implementations, this means that an index is added to the node
     * name to ensure that there are no ambiguities with child nodes having the
     * same names.
     *
     * @param <T> the type of the node to be processed
     * @param node the node, for which the key must be constructed
     * @param parentKey the key of this node's parent (can be <b>null</b> for
     *        the root node)
     * @param handler the {@code NodeHandler} for accessing the node
     * @return the canonical key of this node
     */
    <T> String canonicalKey(T node, String parentKey, NodeHandler<T> handler);

    /**
     * Returns information needed for an add operation. This method gets called
     * when new properties are to be added to a configuration. An implementation
     * has to interpret the specified key, find the parent node for the new
     * elements, and provide all information about new nodes to be added.
     *
     * @param <T> the type of the node to be processed
     * @param root the root node
     * @param key the key for the new property
     * @param handler the {@code NodeHandler} for accessing the node
     * @return an object with all information needed for the add operation
     */
    <T> NodeAddData<T> prepareAdd(T root, String key, NodeHandler<T> handler);
}
