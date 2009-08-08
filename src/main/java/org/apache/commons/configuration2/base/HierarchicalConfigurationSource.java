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
package org.apache.commons.configuration2.base;

import org.apache.commons.configuration2.expr.NodeHandler;

/**
 * <p>
 * An interface defining a source for configuration settings that organizes its
 * data in a hierarchical node structure.
 * </p>
 * <p>
 * This interface provides a completely different way of accessing properties
 * than a plain configuration source using a map-like data structure. It exposes
 * the root node of a hierarchical node structure and a {@code NodeHandler} for
 * traversing this structure. Each node can have a value and a set of
 * attributes. Concrete implementations can use different node classes;
 * therefore the concrete type of the nodes is determined using a type
 * parameter.
 * </p>
 * <p>
 * This interface is pretty lean. It focuses on access to the nodes contained in
 * this source. However, it is possible to implement sophisticated operations
 * related to querying and manipulating configuration settings on top of it.
 * </p>
 * <p>
 * The motivation and purpose for this interface are analogous to
 * {@link ConfigurationSource}, but the organization of the data is different.
 * Because many typical sources used for storing configuration settings are
 * hierarchical in nature (e.g. XML documents, the Java Preferences API, or
 * JNDI) it makes sense to have this interface for providing a uniform access to
 * these sources.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 * @param <T> the type of the nodes used by this hierarchical configuration
 *        source
 */
public interface HierarchicalConfigurationSource<T>
{
    /**
     * Returns the {@code NodeHandler} for dealing with the nodes used by this
     * {@code HierarchicalConfigurationSource}.
     *
     * @return the {@code NodeHandler} for the nodes used by this implementation
     */
    NodeHandler<T> getNodeHandler();

    /**
     * Returns the root node of the hierarchy managed by this source.
     *
     * @return the root node
     */
    T getRootNode();

    /**
     * Sets the root node of the hierarchy managed by this source. Using this
     * method the whole content of this source can be changed. This operation
     * may not be supported by all implementations. An implementation that does
     * not allow changing the root node may throw an {@code
     * UnsupportedOperationException} exception.
     *
     * @param root the new root node (must not be <b>null</b>)
     * @throws UnsupportedOperationException if this operation is not allowed
     *         for this source
     * @throws IllegalArgumentException if the root node is <b>null</b>
     */
    void setRootNode(T root);

    /**
     * Removes all properties contained in this {@code
     * HierarchicalConfigurationSource}. This is an optional operation. It can
     * be implemented by removing the content of the root node.
     *
     * @throws UnsupportedOperationException if this operation is not
     *         implemented
     */
    void clear();

    /**
     * Adds a {@code ConfigurationSourceListener} for this {@code
     * HierarchicalConfigurationSource}. This listener will be notified about
     * manipulations on this source. Support for event listeners is optional. An
     * implementation can throw an {@code UnsupportedOperationException}
     * exception.
     *
     * @param l the listener to be added (must not be <b>null</b>)
     * @throws IllegalArgumentException if the listener is <b>null</b>
     * @throws UnsupportedOperationException if this operation is not
     *         implemented
     */
    void addConfigurationSourceListener(ConfigurationSourceListener l);

    /**
     * Removes the specified {@code ConfigurationSourceListener} from this
     * {@code HierarchicalConfigurationSource}. It will not receive
     * notifications about changes on this source any more. The return value
     * indicates whether the listener existed and could be removed. As was
     * pointed out for
     * {@link #addConfigurationSourceListener(ConfigurationSourceListener)},
     * this is an optional operation.
     *
     * @param l the listener to be removed
     * @return a flag whether the listener could be removed
     * @throws UnsupportedOperationException if this operation is not
     *         implemented
     */
    boolean removeConfigurationSourceListener(ConfigurationSourceListener l);
}
