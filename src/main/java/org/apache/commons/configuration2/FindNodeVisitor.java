/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration2;

import org.apache.commons.configuration2.tree.ConfigurationNodeVisitorAdapter;
import org.apache.commons.configuration2.tree.NodeHandler;

/**
 * <p>
 * A specialized {@code NodeVisitor} implementation which searches for a specific node in a hierarchy.
 * </p>
 *
 * @param <T> the type of the nodes to be visited
 */
final class FindNodeVisitor<T> extends ConfigurationNodeVisitorAdapter<T> {
    /** The node to be searched for. */
    private final T searchNode;

    /** A flag whether the node was found. */
    private boolean found;

    /**
     * Creates a new instance of {@code FindNodeVisitor} and sets the node to be searched for.
     *
     * @param node the search node
     */
    public FindNodeVisitor(final T node) {
        searchNode = node;
    }

    /**
     * Returns a flag whether the search node was found in the last search operation.
     *
     * @return <strong>true</strong> if the search node was found; <strong>false</strong> otherwise
     */
    public boolean isFound() {
        return found;
    }

    /**
     * Resets this visitor. This method sets the {@code found} property to <strong>false</strong> again, so that this instance can be
     * used to inspect another nodes hierarchy.
     */
    public void reset() {
        found = false;
    }

    /**
     * {@inheritDoc} This implementation returns <strong>true</strong> as soon as the node was found.
     */
    @Override
    public boolean terminate() {
        return found;
    }

    @Override
    public void visitBeforeChildren(final T node, final NodeHandler<T> handler) {
        if (node.equals(searchNode)) {
            found = true;
        }
    }
}
