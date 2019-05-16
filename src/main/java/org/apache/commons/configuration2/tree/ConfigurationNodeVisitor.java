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

/**
 * <p>
 * Definition of a <em>Visitor</em> interface for a configuration node
 * structure.
 * </p>
 * <p>
 * This is a typical application of the GoF <em>Visitor</em> pattern. An object
 * implementing this interface can be used to traverse a hierarchical structure
 * of nodes in various ways. The type of the nodes in the structure is generic;
 * a corresponding {@link NodeHandler} implementation must be available for
 * navigating through the structure.
 * </p>
 * <p>
 * Note that the exact way the methods of a {@code ConfigurationNodeVisitor} are
 * invoked is dependent on a specific traversal process.
 * </p>
 *
 * @since 1.3
 * @param <T> the type of the nodes processed by this visitor
 */
public interface ConfigurationNodeVisitor<T>
{
    /**
     * Visits the specified node before the children of this node - if existing
     * - are processed.
     *
     * @param node the node to be visited
     * @param handler the {@code NodeHandler}
     */
    void visitBeforeChildren(T node, NodeHandler<T> handler);

    /**
     * Visits the specified node after after its children - if existing - have
     * been processed.
     *
     * @param node the node to be visited
     * @param handler the {@code NodeHandler}
     */
    void visitAfterChildren(T node, NodeHandler<T> handler);

    /**
     * Returns a flag whether the current visit process should be aborted. This
     * method allows a visitor implementation to state that it does not need any
     * further data. It may be used e.g. by visitors that search for a certain
     * node in the hierarchy. After that node was found, there is no need to
     * process the remaining nodes, too. This method is called after each
     * visited node. A result of <strong>true</strong> indicates that the
     * current iteration is to be aborted.
     *
     * @return a flag if the visit process should be stopped
     */
    boolean terminate();
}
