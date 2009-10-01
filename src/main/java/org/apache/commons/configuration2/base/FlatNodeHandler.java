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
package org.apache.commons.configuration2.base;

import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration2.ConfigurationRuntimeException;
import org.apache.commons.configuration2.expr.AbstractNodeHandler;
import org.apache.commons.configuration2.expr.NodeHandler;

/**
 * <p>
 * A {@code NodeHandler} implementation for dealing with a hierarchy of
 * {@link FlatNode} objects.
 * </p>
 * <p>
 * The implementation of the methods required by the {@link NodeHandler}
 * interface is straight forward. In most cases, it is possible to simply
 * delegate to the corresponding method of the {@link FlatNode} class.
 * Attributes are not supported by flat nodes, so in this area there are only
 * dummy implementations.
 * </p>
 * <p>
 * Actions caused by this node handler may modify the associated
 * {@link FlatConfigurationSource} and thus trigger change events. Per default a
 * change of the {@link FlatConfigurationSource} causes the invalidation of the node
 * structure. Because of that the node handler has to keep track of the updates
 * caused by itself to avoid unnecessary invalidation of nodes. (The adapter
 * implementation using this {@code NodeHandler} to simulate a hierarchical
 * configuration source asks the node handler for each change event whether the
 * node structure should be invalidated.) Note that modifications of a {@code
 * ConfigurationSource} are not thread-safe. So no additional synchronization is
 * done in this class.
 * </p>
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 * @since 2.0
 * @see FlatNode
 */
class FlatNodeHandler extends AbstractNodeHandler<FlatNode>
{
    /** Stores the associated ConfigurationSource. */
    private final FlatConfigurationSource configurationSource;

    /**
     * A flag whether an update of the configuration was caused by an operation
     * on its node structure.
     */
    private boolean internalUpdate;

    /**
     * Creates a new instance of {@code FlatNodeHandler} and initializes it with
     * the associated {@code ConfigurationSource}.
     *
     * @param config the {@code ConfigurationSource}
     */
    public FlatNodeHandler(FlatConfigurationSource config)
    {
        configurationSource = config;
    }

    /**
     * Returns the {@code ConfigurationSource} associated with this node
     * handler.
     *
     * @return the associated {@code ConfigurationSource}
     */
    public FlatConfigurationSource getConfigurationSource()
    {
        return configurationSource;
    }

    /**
     * Returns a flag whether an update of the associated {@code
     * ConfigurationSource} was caused by this node handler. Whenever the
     * {@code ConfigurationSource} adapter receives a change event, it asks the
     * node hander whether it is responsible for this event. The result of this
     * method determines whether the adapter's node structure has to be
     * invalidated: if the event was caused by the node handler, the structure
     * has already been updated and there is no need to invalidate it. Otherwise
     * the {@code ConfigurationSource} was directly manipulated, and the node
     * structure is now out of sync.
     *
     * @return a flag whether an internal update was caused by this node handler
     */
    public boolean isInternalUpdate()
    {
        return internalUpdate;
    }

    /**
     * Adds an attribute to the specified node. Flat nodes do not support
     * attributes, so this implementation just throws an exception.
     *
     * @param node the node
     * @param name the name of the attribute
     * @param value the new value
     * @throws ConfigurationRuntimeException if the attribute value cannot be
     *         added
     */
    public void addAttributeValue(FlatNode node, String name, Object value)
    {
        throw new ConfigurationRuntimeException(
                "Cannot add an attribute to a flat node!");
    }

    /**
     * Adds a new child to the given node.
     *
     * @param node the node
     * @param name the name of the new child
     * @return the newly created child node
     */
    public FlatNode addChild(FlatNode node, String name)
    {
        return node.addChild(name);
    }

    /**
     * Returns the value of an attribute of the specified node. Flat nodes do
     * not support attributes, so this implementation always returns
     * <b>null</b>.
     *
     * @param node the node
     * @param name the name of the attribute
     * @return the value of this attribute
     */
    public Object getAttributeValue(FlatNode node, String name)
    {
        return null;
    }

    /**
     * Returns a list with the names of the attributes of the specified node.
     * Flat nodes do not support attributes, so this implementation always
     * returns an empty list.
     *
     * @param node the node
     * @return a list with the names of the existing attributes
     */
    public List<String> getAttributes(FlatNode node)
    {
        return Collections.emptyList();
    }

    /**
     * Returns the child of the specified node with the given index.
     *
     * @param node the node
     * @param index the index
     * @return the child node with this index
     */
    public FlatNode getChild(FlatNode node, int index)
    {
        return node.getChild(index);
    }

    /**
     * Returns a list with all children of the specified node.
     *
     * @param node the node
     * @return a list with all child nodes of this node
     */
    public List<FlatNode> getChildren(FlatNode node)
    {
        return node.getChildren();
    }

    /**
     * Returns a list with all children of the specified node with the given
     * name.
     *
     * @param node the node
     * @param name the desired name
     * @return a list with the child nodes with this name
     */
    public List<FlatNode> getChildren(FlatNode node, String name)
    {
        return node.getChildren(name);
    }

    /**
     * Returns the number of children with the given name of the specified node.
     *
     * @param node the node
     * @param name the name of the desired child nodes
     * @return the number of the child nodes with this name
     */
    public int getChildrenCount(FlatNode node, String name)
    {
        return node.getChildrenCount(name);
    }

    /**
     * Returns the parent of the specified node.
     *
     * @param node the node
     * @return the parent node
     */
    public FlatNode getParent(FlatNode node)
    {
        return node.getParent();
    }

    /**
     * Returns the value of the specified node.
     *
     * @param node the node
     * @return the value of this node
     */
    public Object getValue(FlatNode node)
    {
        return node.getValue(getConfigurationSource());
    }

    /**
     * Returns the name of the specified node.
     *
     * @param node the node
     * @return the name of this node
     */
    public String nodeName(FlatNode node)
    {
        return node.getName();
    }

    /**
     * Removes an attribute of the specified node. Flat nodes do not have
     * attributes, so this implementation is just an empty dummy.
     *
     * @param node the node
     * @param name the name of the attribute
     */
    public void removeAttribute(FlatNode node, String name)
    {
    }

    /**
     * Removes a child of the given parent node.
     *
     * @param node the parent node
     * @param child the child node to be removed
     */
    public void removeChild(FlatNode node, FlatNode child)
    {
        internalUpdate = true;
        try
        {
            node.removeChild(getConfigurationSource(), child);
        }
        finally
        {
            internalUpdate = false;
        }
    }

    /**
     * Sets an attribute of the specified node. Flat nodes do not support
     * attributes, so this implementation just throws an exception.
     *
     * @param node the node
     * @param name the name of the attribute
     * @param value the new value
     * @throws ConfigurationRuntimeException if the attribute value cannot be
     *         set
     */
    public void setAttributeValue(FlatNode node, String name, Object value)
    {
        throw new ConfigurationRuntimeException(
                "Cannot set an attribute of a flat node!");
    }

    /**
     * Sets the value of the specified node.
     *
     * @param node the node
     * @param value the new value
     */
    public void setValue(FlatNode node, Object value)
    {
        internalUpdate = true;
        try
        {
            node.setValue(getConfigurationSource(), value);
        }
        finally
        {
            internalUpdate = false;
        }
    }
}
