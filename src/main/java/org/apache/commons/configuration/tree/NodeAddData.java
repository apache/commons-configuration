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
package org.apache.commons.configuration.tree;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * A simple data class used by <code>{@link ExpressionEngine}</code> to store
 * the results of the <code>prepareAdd()</code> operation.
 * </p>
 * <p>
 * If a new property is to be added to a configuration, the affected
 * <code>Configuration</code> object must know, where in its hierarchy of
 * configuration nodes new elements have to be added. This information is
 * obtained by an <code>ExpressionEngine</code> object that interprets the key
 * of the new property. This expression engine will pack all information
 * necessary for the configuration to perform the add operation in an instance
 * of this class.
 * </p>
 * <p>
 * Information managed by this class contains:
 * <ul>
 * <li>the configuration node, to which new elements must be added</li>
 * <li>the name of the new node</li>
 * <li>whether the new node is a child node or an attribute node</li>
 * <li>if a whole branch is to be added at once, the names of all nodes between
 * the parent node (the target of the add operation) and the new node</li>
 * </ul>
 * </p>
 *
 * @since 1.3
 * @author Oliver Heger
 */
public class NodeAddData
{
    /** Stores the parent node of the add operation. */
    private ConfigurationNode parent;

    /**
     * Stores a list with nodes that are on the path between the parent node and
     * the new node.
     */
    private List pathNodes;

    /** Stores the name of the new node. */
    private String newNodeName;

    /** Stores the attribute flag. */
    private boolean attribute;

    /**
     * Creates a new, uninitialized instance of <code>NodeAddData</code>.
     */
    public NodeAddData()
    {
        this(null, null);
    }

    /**
     * Creates a new instance of <code>NodeAddData</code> and sets the most
     * important data fields.
     *
     * @param parent the parent node
     * @param nodeName the name of the new node
     */
    public NodeAddData(ConfigurationNode parent, String nodeName)
    {
        setParent(parent);
        setNewNodeName(nodeName);
    }

    /**
     * Returns a flag if the new node to be added is an attribute.
     *
     * @return <b>true</b> for an attribute node, <b>false</b> for a child
     * node
     */
    public boolean isAttribute()
    {
        return attribute;
    }

    /**
     * Sets the attribute flag. This flag determines whether an attribute or a
     * child node will be added.
     *
     * @param attribute the attribute flag
     */
    public void setAttribute(boolean attribute)
    {
        this.attribute = attribute;
    }

    /**
     * Returns the name of the new node.
     *
     * @return the new node's name
     */
    public String getNewNodeName()
    {
        return newNodeName;
    }

    /**
     * Sets the name of the new node. A node with this name will be added to the
     * configuration's node hierarchy.
     *
     * @param newNodeName the name of the new node
     */
    public void setNewNodeName(String newNodeName)
    {
        this.newNodeName = newNodeName;
    }

    /**
     * Returns the parent node.
     *
     * @return the parent node
     */
    public ConfigurationNode getParent()
    {
        return parent;
    }

    /**
     * Sets the parent node. New nodes will be added to this node.
     *
     * @param parent the parent node
     */
    public void setParent(ConfigurationNode parent)
    {
        this.parent = parent;
    }

    /**
     * Returns a list with further nodes that must be added. This is needed if a
     * complete branch is to be added at once. For instance imagine that there
     * exists only a node <code>database</code>. Now the key
     * <code>database.connection.settings.username</code> (assuming the syntax
     * of the default expression engine) is to be added. Then
     * <code>username</code> is the name of the new node, but the nodes
     * <code>connection</code> and <code>settings</code> must be added to
     * the parent node first. In this example these names would be returned by
     * this method.
     *
     * @return a list with the names of nodes that must be added as parents of
     * the new node (never <b>null</b>)
     */
    public List getPathNodes()
    {
        return (pathNodes != null) ? Collections.unmodifiableList(pathNodes)
                : Collections.EMPTY_LIST;
    }

    /**
     * Adds the name of a path node. With this method an additional node to be
     * added can be defined.
     *
     * @param nodeName the name of the node
     * @see #getPathNodes()
     */
    public void addPathNode(String nodeName)
    {
        if (pathNodes == null)
        {
            pathNodes = new LinkedList();
        }
        pathNodes.add(nodeName);
    }
}
