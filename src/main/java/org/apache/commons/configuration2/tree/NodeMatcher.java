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
 * An interface for matching nodes based on specific criteria.
 * </p>
 * <p>
 * This interface is used by {@link NodeHandler} to support advanced filtering
 * on the child nodes of a given parent node. This is useful for instance for
 * special {@link ExpressionEngine} implementations which do no direct or strict
 * matches based on node names. An example could be an expression engine that
 * treats the passed in node keys in a case-insensitive manner. Such an engine
 * would use a special case-insensitive matcher when resolving configuration
 * keys.
 * </p>
 * <p>
 * The idea behind this interface is that a matcher has to match a property of a
 * node against a given criterion. This criterion is passed to the matching
 * function so that matchers can be implemented in a state-less fashion and
 * shared between multiple components.
 * </p>
 *
 * @since 2.0
 * @param <C> the type of the criterion evaluated by this matcher
 */
public interface NodeMatcher<C>
{
    /**
     * Tests whether the passed in node matches the given criterion.
     *
     * @param node the node to be tested
     * @param handler the corresponding {@code NodeHandler}
     * @param criterion the criterion to match against
     * @param <T> the type of the node
     * @return <b>true</b> if this node matches the criterion, <b>false</b>
     *         otherwise
     */
    <T> boolean matches(T node, NodeHandler<T> handler, C criterion);
}
