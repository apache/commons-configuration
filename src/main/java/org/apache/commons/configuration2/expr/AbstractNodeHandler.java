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
 * An abstract base class that simplifies concrete <code>NodeHandler</code>
 * implementations.
 * </p>
 * <p>
 * This class already provides default implementations for some of the methods
 * defined by the <code>NodeHandler</code> interface. It is especially useful
 * for simple handlers that operate on a concrete node type.
 * </p>
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 * @param <T> the type of the nodes this handler can handle
 */
public abstract class AbstractNodeHandler<T> implements NodeHandler<T>
{
    /**
     * Returns a flag whether the passed in node has any attributes. This base
     * implementation delegates to <code>getAttributes()</code> and checks
     * whether the returned list is empty. Derived classes may override this
     * method to implement a more efficient algorithm.
     *
     * @param node the node
     * @return a flag whether this node has attributes
     */
    public boolean hasAttributes(T node)
    {
        return !getAttributes(node).isEmpty();
    }

    /**
     * Initializes this <code>NodeHandler</code> with a reference to a
     * <code>NodeHandlerRegistry</code>. This is just a dummy implementation;
     * the passed in reference is ignored.
     *
     * @param registry the registry
     */
    public void initNodeHandlerRegistry(NodeHandlerRegistry registry)
    {
    }

    /**
     * Tests whether the passed in node is defined. This base implementation
     * checks whether the node has a value or any attributes.
     *
     * @param node the node to test
     * @return a flag whether this node is defined
     */
    public boolean isDefined(T node)
    {
        return getValue(node) != null || hasAttributes(node);
    }
}
