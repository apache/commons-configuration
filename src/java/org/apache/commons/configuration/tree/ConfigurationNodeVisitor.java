/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration.tree;

/**
 * <p>
 * Definition of a <em>Visitor</em> interface for a configuration node
 * structure.
 * </p>
 * <p>
 * The <code>ConfigurationNode</code> interface defines a <code>visit()</code>,
 * which simplifies traversal of a complex node hierarchy. A configuration node
 * implementation must provide a way of visiting all nodes in the current
 * hierarchy. This is a typical application of the GoF <em>Visitor</em>
 * pattern.
 * </p>
 *
 * @since 1.3
 * @see ConfigurationNode
 * @author Oliver Heger
 */
public interface ConfigurationNodeVisitor
{
    /**
     * Visits the specified node. This method is called before eventually
     * existing children of this node are processed.
     *
     * @param node the node to be visited
     */
    void visitBeforeChildren(ConfigurationNode node);

    /**
     * Visits the specified node. This method is called after eventually
     * existing children of this node have been processed.
     *
     * @param node the node to be visited
     */
    void visitAfterChildren(ConfigurationNode node);

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
