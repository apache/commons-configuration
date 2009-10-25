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
package org.apache.commons.configuration2.flat;

import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration2.ConfigurationRuntimeException;
import org.apache.commons.configuration2.expr.AbstractNodeHandler;
import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.expr.NodeHandlerRegistry;

/**
 * <p>
 * A <code>NodeHandler</code> implementation for dealing with the nodes of a
 * <code>{@link AbstractFlatConfiguration}</code>.
 * </p>
 * <p>
 * This class is used internally by a <code>AbstractFlatConfiguration</code>
 * to deal with its nodes. Note that a node structure is only constructed if the
 * configuration is treated as a hierarchical configuration, for instance if it
 * is added to a <code>CombinedConfiguration</code>.
 * </p>
 * <p>
 * The implementation of the methods required by the
 * {@link NodeHandler} interface is straightforward. In most
 * cases, it is possible to simply delegate to the corresponding
 * <code>FlatNode</code> method. Attributes are not supported by flat nodes,
 * so in this area there are only dummy implementations.
 * </p>
 * <p>
 * Actions caused by this node handler may modify the associated configuration
 * and thus trigger change events. Per default the configuration will invalidate
 * its node structure if a change event is received. Because of that the node
 * handler has to keep track of the updates caused by itself to avoid
 * unnecessary invalidation of nodes. (The configuration asks the node handler
 * for each change event whether the node structure should be invalidated.) Note
 * that modifications of a configuration are not thread-safe. So no additional
 * synchronization is done in this class.
 * </p>
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 * @since 2.0
 * @see FlatNode
 */
class FlatNodeHandler extends AbstractNodeHandler<FlatNode>
{
    /** Stores the NodeHandlerRegistry. */
    private NodeHandlerRegistry nodeHandlerRegistry;

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
     * not support attributes, so this implementation always returns <b>null</b>.
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
        return node.getValue();
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
        setInternalUpdate(node, true);
        try
        {
            node.removeChild(child);
        }
        finally
        {
            setInternalUpdate(node, false);
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
        setInternalUpdate(node, true);
        try
        {
            node.setValue(value);
        }
        finally
        {
            setInternalUpdate(node, false);
        }
    }

    /**
     * Initializes the {@code NodeHandlerRegistry}. A {@code FlatNodeHandler}
     * deals with multiple types of nodes (flat root node and leaf nodes). To be
     * compatible with a combined configuration it has to tell the parent
     * {@code NodeHandlerRegistry} that it is responsible for these types of
     * nodes. It does so by adding a specialized sub {@code NodeHandlerRegistry}
     * that can lookup all flat node types.
     *
     * @param registry the {@code NodeHandlerRegistry}
     */
    @Override
    public void initNodeHandlerRegistry(NodeHandlerRegistry registry)
    {
        nodeHandlerRegistry = registry;

        registry.addSubRegistry(new NodeHandlerRegistry()
        {
            /**
             * Resolves the node handler. This is delegated to the parent
             * registry.
             *
             * @param node the node in question
             * @return a {@code NodeHandler} for this node
             */
            public NodeHandler<?> resolveHandler(Object node)
            {
                assert nodeHandlerRegistry != null : "No parent registry!";
                return nodeHandlerRegistry.resolveHandler(node);
            }

            /**
             * Checks whether a {@code NodeHandler} for the specified node is
             * known. This implementation returns this {@code NodeHandler} if
             * the passed in node is a flat node.
             *
             * @param node the node in question
             * @param subClasses a flag whether subclasses should be taken into
             *        account
             * @return a {@code NodeHandler} for this node
             */
            public NodeHandler<?> lookupHandler(Object node, boolean subClasses)
            {
                return (node instanceof FlatNode) ? FlatNodeHandler.this : null;
            }

            /**
             * Adds a sub registry. This is not supported by this
             * implementation.
             *
             * @param subreg the registry to be added
             */
            public void addSubRegistry(NodeHandlerRegistry subreg)
            {
                throw new UnsupportedOperationException("Not implemented!");
            }
        });
    }

    /**
     * Sets the internal update flag of the specified node. This method is if a
     * change on the node structure is performed that affects the associated
     * configuration.
     *
     * @param node the flat node affected by the change
     * @param f the value of the internal update flag
     */
    void setInternalUpdate(FlatNode node, boolean f)
    {
        node.setInternalUpdate(f);
    }
}
