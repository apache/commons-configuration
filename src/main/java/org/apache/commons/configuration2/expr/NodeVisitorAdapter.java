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
 * A simple adapter class that simplifies writing custom node visitor
 * implementations.
 * </p>
 * <p>
 * This class provides dummy implementations for the methods defined in the
 * <code>ConfigurationNodeVisitor</code> interface. Derived classes only need
 * to override the methods they really need.
 * </p>
 *
 * @author Oliver Heger
 * @version $Id$
 * @param <T> the type of the involved nodes
 */
public class NodeVisitorAdapter<T> implements NodeVisitor<T>
{
    /**
     * Checks whether the visiting process should be aborted. This base
     * implementation always returns <b>false</b>
     *
     * @return a flag whether the visiting process should be aborted
     */
    public boolean terminate()
    {
        return false;
    }

    /**
     * Visits the specified node after its children have been processed. This is
     * an empty dummy implementation.
     *
     * @param node the node
     * @param handler the node handler
     */
    public void visitAfterChildren(T node, NodeHandler<T> handler)
    {
    }

    /**
     * Visits the specified node before its children are processed. This is an
     * empty dummy implementation.
     *
     * @param node the node
     * @param handler the node handler
     */
    public void visitBeforeChildren(T node, NodeHandler<T> handler)
    {
    }
}
