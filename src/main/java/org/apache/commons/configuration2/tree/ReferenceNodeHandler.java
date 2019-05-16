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
 * An extension of the {@link NodeHandler} interface which allows access to
 * so-called <em>references</em> stored for a node.
 * </p>
 * <p>
 * Some specialized configuration implementations needs to store additional data
 * for the nodes representing configuration properties. This interface provides
 * methods for querying this data. For instance, it is possible to query a
 * reference object stored for a specific node.
 * </p>
 * <p>
 * {@link InMemoryNodeModel} supports references. It can be queried for a
 * {@code ReferenceNodeHandler} which can then be used for dealing with
 * references.
 * </p>
 *
 * @since 2.0
 */
public interface ReferenceNodeHandler extends NodeHandler<ImmutableNode>
{
    /**
     * Returns the reference object associated with the specified node. If no
     * reference data is associated with this node, result is <b>null</b>.
     *
     * @param node the node in question
     * @return the reference object for this node or <b>null</b>
     */
    Object getReference(ImmutableNode node);

    /**
     * Returns a list with the reference objects for nodes which have been
     * removed. Whenever a node associated with a reference object is removed
     * from the nodes structure managed by the owning model, the reference
     * object is recorded. This is necessary for instance to free some
     * resources. With this method all recorded reference objects can be
     * queried. They are typically returned in the order in which they have been
     * removed.
     *
     * @return a list with reference objects for nodes removed from the model
     */
    List<Object> removedReferences();
}
