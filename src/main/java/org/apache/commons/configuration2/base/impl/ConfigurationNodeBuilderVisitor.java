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
package org.apache.commons.configuration2.base.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.expr.NodeVisitorAdapter;
import org.apache.commons.configuration2.tree.ConfigurationNode;

/**
 * A specialized visitor base class that can be used by in-memory hierarchical
 * configuration sources for persisting the tree of configuration nodes.</p>
 * <p>
 * The basic idea behind this class is that each node can be associated with a
 * reference object. This reference object has a concrete meaning in a derived
 * class, e.g. an entry in a JNDI context or an XML element. When the
 * configuration node tree is set up, the concrete configuration source
 * implementation is responsible for setting the reference objects. When the
 * configuration node tree is later modified, new nodes do not have a defined
 * reference object. This visitor class processes all nodes and finds the ones
 * without a defined reference object. For those nodes the {@code insert()}
 * method is called, which must be defined in concrete sub classes. This method
 * can perform all steps to integrate the new node into the original structure.
 * </p>
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public abstract class ConfigurationNodeBuilderVisitor extends
        NodeVisitorAdapter<ConfigurationNode>
{
    /**
     * Visits the specified node before its children have been traversed.
     *
     * @param node the node to visit
     * @param handler the node handler
     */
    @Override
    public void visitBeforeChildren(ConfigurationNode node,
            NodeHandler<ConfigurationNode> handler)
    {
        Collection<ConfigurationNode> subNodes = new LinkedList<ConfigurationNode>(
                node.getChildren());
        subNodes.addAll(node.getAttributes());
        Iterator<ConfigurationNode> children = subNodes.iterator();
        ConfigurationNode sibling1 = null;
        ConfigurationNode nd = null;

        while (children.hasNext())
        {
            // find the next new node
            do
            {
                sibling1 = nd;
                nd = children.next();
            } while (nd.getReference() != null && children.hasNext());

            if (nd.getReference() == null)
            {
                // find all following new nodes
                List<ConfigurationNode> newNodes = new LinkedList<ConfigurationNode>();
                newNodes.add(nd);
                while (children.hasNext())
                {
                    nd = children.next();
                    if (nd.getReference() == null)
                    {
                        newNodes.add(nd);
                    }
                    else
                    {
                        break;
                    }
                }

                // Insert all new nodes
                ConfigurationNode sibling2 = (nd.getReference() == null) ? null
                        : nd;
                for (ConfigurationNode insertNode : newNodes)
                {
                    if (insertNode.getReference() == null)
                    {
                        Object ref = insert(insertNode, node, sibling1,
                                sibling2);
                        if (ref != null)
                        {
                            insertNode.setReference(ref);
                        }
                        sibling1 = insertNode;
                    }
                }
            }
        }
    }

    /**
     * Inserts a new node into the structure constructed by this builder. This
     * method is called for each node that has been added to the configuration
     * tree after the configuration has been loaded from its source. These new
     * nodes have to be inserted into the original structure. The passed in
     * nodes define the position of the node to be inserted: its parent and the
     * siblings between to insert. The return value is interpreted as the new
     * reference of the affected <code>Node</code> object; if it is not <b>null
     * </b>, it is passed to the node's <code>setReference()</code> method.
     *
     * @param newNode the node to be inserted
     * @param parent the parent node
     * @param sibling1 the sibling after which the node is to be inserted; can
     *        be <b>null </b> if the new node is going to be the first child
     *        node
     * @param sibling2 the sibling before which the node is to be inserted; can
     *        be <b>null </b> if the new node is going to be the last child node
     * @return the reference object for the node to be inserted
     */
    protected abstract Object insert(ConfigurationNode newNode,
            ConfigurationNode parent, ConfigurationNode sibling1,
            ConfigurationNode sibling2);
}
