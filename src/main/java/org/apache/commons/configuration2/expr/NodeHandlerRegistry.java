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

import org.apache.commons.configuration2.ConfigurationRuntimeException;

/**
 * <p>
 * Definition of an interface for obtaining a <code>{@link NodeHandler}</code>
 * for a specific configuration node.
 * </p>
 * <p>
 * Through the methods defined by this interface it is possible to query a
 * <code>NodeHandler</code> for a given configuration node. This is especially
 * important in complex scenarios where multiple configurations with different
 * types of nodes are involved, e.g. for combined configurations. In such cases,
 * it may be necessary to use different handlers when navigating a hierarchy of
 * configuration nodes.
 * </p>
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 * @since 2.0
 */
public interface NodeHandlerRegistry
{
    /**
     * Returns a suitable <code>NodeHandler</code> for the specified node
     * object. This is the main method for querying node handlers for specific
     * nodes. A concrete implementation has to determine a
     * <code>NodeHandler</code> that can cope with the specified node object.
     * If this fails, an exception should be thrown (result should never be
     * <b>null</b>).
     *
     * @param node the node in question
     * @return a <code>NodeHandler</code> that can deal with the passed in
     *         node
     * @throws ConfigurationRuntimeException if no suitable
     *         <code>NodeHandler</code> can be found for the given node
     */
    NodeHandler<?> resolveHandler(Object node);

    /**
     * Checks whether this <code>NodeHandlerRegistry</code> has a suitable
     * <code>NodeHandler</code> that can handle the passed in node object and
     * returns it if this is the case. This method only checks the internal data
     * structures for a suitable <code>NodeHandler</code> implementation; it
     * can return <b>null</b> if none is found. <code>resolveHandler()</code>
     * in contrast will try very hard to find a compatible handler: if
     * necessary, connected sub registries will also be queried. As a rule of
     * thumb, clients should always invoke <code>resolveHandler()</code> to
     * obtain a <code>NodeHandler</code>. The <code>lookupHandler()</code>
     * method is intended for internal processing.
     *
     * @param node the node in question
     * @return a <code>NodeHandler</code> that can deal with the passed in
     *         node (can be <b>null</b>)
     */
    NodeHandler<?> lookupHandler(Object node);

    /**
     * Adds a sub registry to this object. When resolving a
     * <code>NodeHandler</code>, this sub registry can also be queried.
     * Connecting <code>NodeHandlerRegistry</code> implementations to complex
     * hierarchical structures makes it possible to deal with complicated node
     * structures, in which different sub trees consist of different
     * configuration nodes. One example of such a structure is a combined
     * configuration that is again contained in another combined configuration.
     * While each combined configuration should be able to handle the nodes in
     * its sub tree properly, there must be a global way of finding a compatible
     * <code>NodeHandler</code> for each node in the overall structure.
     *
     * @param subreg the sub registry to be added
     */
    void addSubRegistry(NodeHandlerRegistry subreg);
}
