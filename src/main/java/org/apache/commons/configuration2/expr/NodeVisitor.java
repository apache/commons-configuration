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

/**
 * <p>
 * Definition of a <em>visitor</em> interface for a node structure used by a
 * configuration.
 * </p>
 * <p>
 * Hierarchical configurations often need to traverse their internal nodes in a
 * structured manner. While the concrete type of the nodes used differs for
 * specific configuration implementations, the overall structure is always
 * similar: The nodes form a tree-like structure, each node can have a value and
 * an arbitrary number of attributes.
 * </p>
 * <p>
 * This interface defines a generic visitor (according to the GoF
 * <em>Visitor</em> pattern) that can deal with such structures. It defines
 * methods for visiting each node of a structure before or after its children
 * have been processed. These methods are also passed a
 * <code>{@link NodeHandler}</code> reference, which can be used for accessing
 * further properties of the node (e.g. its attributes) or manipulating it. With
 * an additional method the traversal process can be aborted.
 * </p>
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 * @param <T> the type of the involved nodes
 */
public interface NodeVisitor<T>
{
    /**
     * Visits the specified node before its children are processed. This method
     * is called for all nodes in the currently processed hierarchy.
     *
     * @param node the node to be visited
     * @param handler the node handler
     */
    void visitBeforeChildren(T node, NodeHandler<T> handler);

    /**
     * Visits the specified node after its children have been processed. This
     * method is called for all nodes in the currently processed hierarchy.
     *
     * @param node the node to be visited
     * @param handler the node handler
     */
    void visitAfterChildren(T node, NodeHandler<T> handler);

    /**
     * Returns a flag whether the actual visit process should be aborted. This
     * method allows a visitor implementation to state that it does not need any
     * further data. It may be used e.g. by visitors that search for a certain
     * node in the hierarchy. After that node was found, there is no need to
     * process the remaining nodes, too.
     *
     * @return a flag if the visit process should be stopped
     */
    boolean terminate();
}
