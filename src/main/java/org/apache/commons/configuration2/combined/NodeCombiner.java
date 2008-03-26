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
package org.apache.commons.configuration2.combined;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration2.expr.NodeHandler;

/**
 * <p>
 * A base class for node combiner implementations.
 * </p>
 * <p>
 * A <em>node combiner</em> is an object that knows how two hierarchical node
 * structures can be combined into a single one. Of course, there are many
 * possible ways of implementing such a combination, e.g. constructing a union,
 * an intersection, or an "override" structure (were nodes in the first
 * hierarchy take precedence over nodes in the second hierarchy). This abstract
 * base class only provides some helper methods and defines the common interface
 * for node combiners. Concrete sub classes will implement the diverse
 * combination algorithms.
 * </p>
 * <p>
 * For some concrete combiner implementations it is important to distinguish
 * whether a node is a single node or whether it belongs to a list structure.
 * Alone from the input structures, the combiner will not always be able to make
 * this decision. So sometimes it may be necessary for the developer to
 * configure the combiner and tell it, which nodes should be treated as list
 * nodes. For this purpose the <code>addListNode()</code> method exists. It
 * can be passed the name of a node, which should be considered a list node.
 * </p>
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 * @version $Id$
 * @since 2.0
 */
public abstract class NodeCombiner
{
    /** Stores a list with node names that are known to be list nodes. */
    protected Set<String> listNodes;

    /**
     * Creates a new instance of <code>NodeCombiner</code>.
     */
    public NodeCombiner()
    {
        listNodes = new HashSet<String>();
    }

    /**
     * Adds the name of a node to the list of known list nodes. This means that
     * nodes with this name will never be combined.
     *
     * @param nodeName the name to be added
     */
    public void addListNode(String nodeName)
    {
        listNodes.add(nodeName);
    }

    /**
     * Returns a set with the names of nodes that are known to be list nodes.
     *
     * @return a set with the names of list nodes
     */
    public Set<String> getListNodes()
    {
        return Collections.unmodifiableSet(listNodes);
    }

    /**
     * Checks if a node is a list node. This implementation tests if the name of
     * the given node is contained in the set of known list nodes. Derived
     * classes which use different criteria may override this method.
     *
     * @param node the node to be tested
     * @param handler the corresponding node handler
     * @return a flag whether this is a list node
     */
    public <T> boolean isListNode(T node, NodeHandler<T> handler)
    {
        return listNodes.contains(handler.nodeName(node));
    }

    /**
     * Combines the hierarchies represented by the given root nodes. This method
     * must be defined in concrete sub classes with the implementation of a
     * specific combination algorithm.
     *
     * @param node1 the first root node
     * @param node2 the second root node
     * @return the resulting combined node structure
     */
    public abstract <T, U> CombinedNode combine(T node1, NodeHandler<T> handler1,
            U node2, NodeHandler<U> handler2);

    /**
     * Creates a new view node. This method will be called whenever a new view
     * node is to be created. It can be overridden to create special view nodes.
     * This base implementation returns a new instance of
     * <code>{@link CombinedNode}</code>.
     *
     * @return the new view node
     */
    protected CombinedNode createViewNode()
    {
        return new CombinedNode();
    }

    /**
     * Appends all attributes of the given source node to the specified view
     * node.
     *
     * @param <T> the type of nodes to deal with
     * @param vn the view node
     * @param node the source node
     * @param handler the handler for the source node
     */
    protected <T> void appendAttributes(CombinedNode vn, T node,
            NodeHandler<T> handler)
    {
        for (String attrName : handler.getAttributes(node))
        {
            vn.addAttributeValue(attrName, handler.getAttributeValue(node,
                    attrName));
        }
    }
}
