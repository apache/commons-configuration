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
package org.apache.commons.configuration2.expr;

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
 * configuration that have to handle property key (e.g.
 * <code>getProperty()</code> or <code>addProperty()</code> do not interpret
 * the passed in keys on their own, but delegate this task to an associated
 * expression engine. This expression engine will then find out, which
 * configuration nodes are addressed by the key.
 * </p>
 * <p>
 * Separating the task of evaluating property keys from the configuration object
 * has the advantage that many different expression languages (i.e. ways for
 * querying or setting properties) can be supported. Just set a suitable
 * implementation of this interface as the configuration's expression engine,
 * and you can use the syntax provided by this implementation.
 * </p>
 *
 * @since 1.3
 * @author Oliver Heger
 */
public interface ExpressionEngine
{
    /**
     * Finds the node(s) that is (are) matched by the specified key. This is the
     * main method for interpreting property keys. An implementation must
     * traverse the given root node and its children to find all nodes that are
     * matched by the given key. If the key is not correct in the syntax
     * provided by this implementation, it is free to throw a (runtime)
     * exception indicating this error condition. The passed in
     * <code>{@link NodeHandler}</code> can be used for accessing the properties
     * of the node. The resulting <code>NodeList</code> object can be used to
     * browse the results. It can contain nodes and attributes as well.
     *
     * @param root the root node of a hierarchy of configuration nodes
     * @param key the key to be evaluated
     * @param handler the node handler to be used
     * @return a list with the nodes that are matched by the key (should never
     * be <b>null</b>)
     */
    <T> NodeList<T> query(T root, String key, NodeHandler<T> handler);

    /**
     * Returns the key for the specified node in the expression language
     * supported by an implementation. This method is called whenever a property
     * key for a node has to be constructed, e.g. by the
     * <code>{@link org.apache.commons.configuration2.Configuration#getKeys() getKeys()}</code>
     * method.
     *
     * @param node the node, for which the key must be constructed
     * @param parentKey the key of this node's parent (can be <b>null</b> for
     * the root node)
     * @param handler the node handler
     * @return this node's key
     */
    <T> String nodeKey(T node, String parentKey, NodeHandler<T> handler);

    /**
     * Returns information needed for an add operation. This method gets called
     * when new properties are to be added to a configuration. An implementation
     * has to interpret the specified key, find the parent node for the new
     * elements, and provide all information about new nodes to be added.
     *
     * @param root the root node
     * @param key the key for the new property
     * @param handler the node handler
     * @return an object with all information needed for the add operation
     */
    <T> NodeAddData<T> prepareAdd(T root, String key, NodeHandler<T> handler);
}
