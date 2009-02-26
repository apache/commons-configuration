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
import org.apache.commons.configuration2.expr.NodeList;

/**
 * <p>
 * A specialized {@code ConfigurationSource} that organizes its data in a
 * hierarchical node structure.
 * </p>
 * <p>
 * This interface extends the {@code ConfigurationSource} interface by some
 * methods providing a hierarchical view on the data stored in this
 * configuration source. There is one method returning the root node of the
 * hierarchy. A {@link NodeHandler} can also be queried for performing
 * operations on the node structure. Concrete implementations can use different
 * node classes; therefore the concrete type of the nodes is determined using a
 * type parameter.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 * @param <T> the type of the nodes used by this hierarchical configuration
 *        source
 */
public interface HierarchicalConfigurationSource<T> extends ConfigurationSource
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
     * Evaluates the specified expression and returns a {@code NodeList} with
     * the results. An implementation must use the current {@code
     * ExpressionEngine} to execute the query on the hierarchical node structure
     * maintained by this source.
     *
     * @param expr the expression to be evaluated
     * @return a {@code NodeList} with the results
     */
    NodeList<T> find(String expr);
}
