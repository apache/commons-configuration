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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration2.ConfigurationRuntimeException;

/**
 * <p>
 * A leaf node in the hierarchy of flat nodes.
 * </p>
 * <p>
 * The node structure of a flat {@code ConfigurationSource} has two kinds of
 * nodes: a single root node and an arbitrary number of child nodes. This class
 * represents the child nodes. A child node has a name, a value and a parent.
 * Child nodes, however, are not supported.
 * </p>
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
class FlatLeafNode extends FlatNode
{
    /** Stores the parent node. */
    private final FlatRootNode parent;

    /** Stores the name of this node. */
    private final String name;

    /** Stores a flag whether this node is associated with an existing value. */
    private boolean hasValue;

    /**
     * Creates a new instance of {@code FlatLeafNode} and initializes it. (This
     * constructor is intended to be called by {@code FlatRootNode} only.)
     *
     * @param parent the parent node
     * @param name the name of this node
     * @param value a flag whether this node already has a value
     */
    FlatLeafNode(FlatRootNode parent, String name, boolean value)
    {
        this.parent = parent;
        this.name = name;
        hasValue = value;
    }

    /**
     * Adds a new child node to this node. Because leaf nodes do not support
     * children, this implementation always throws a runtime exception.
     *
     * @param name the name of the new child
     * @return the newly created child node
     * @throws ConfigurationRuntimeException if the child cannot be added
     */
    @Override
    public FlatNode addChild(String name)
    {
        throw new ConfigurationRuntimeException(
                "Cannot add child to a leaf node!");
    }

    /**
     * Returns the child node with the given index. Because leaf nodes do not
     * support children, this implementation always throws an {@code
     * IndexOutOfBoundsException} exception.
     *
     * @param index the index
     * @return the child node at this index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public FlatNode getChild(int index)
    {
        throw new IndexOutOfBoundsException(
                "Invalid child index for leaf node!");
    }

    /**
     * Returns a list with the children of this node. A leaf node does not have
     * any children, so this implementation always returns an empty list.
     *
     * @return the children of this node
     */
    @Override
    public List<FlatNode> getChildren()
    {
        return Collections.emptyList();
    }

    /**
     * Returns a list with the child nodes of this node with the given name. A
     * leaf node does not have any children, so this implementation always
     * returns an empty list.
     *
     * @param name the name of the desired children
     * @return a list with the found children
     */
    @Override
    public List<FlatNode> getChildren(String name)
    {
        return getChildren();
    }

    /**
     * Returns the number of the child nodes with the given name. Because a leaf
     * node does not have any children result is always 0.
     *
     * @param name the name of the desired children
     * @return the number of child nodes with this name
     */
    @Override
    public int getChildrenCount(String name)
    {
        return 0;
    }

    /**
     * Returns the name of this node.
     *
     * @return the name
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Returns the parent node. The parent was set when this node was created.
     *
     * @return the parent node
     */
    @Override
    public FlatNode getParent()
    {
        return parent;
    }

    /**
     * Returns the value of this node. This implementation obtains the value
     * from the passed in {@code ConfigurationSource}. It also takes its value
     * index into account if the represented property has multiple values.
     *
     * @return the value of the represented property
     */
    @Override
    public Object getValue()
    {
        Object value = getConfigurationSource().getProperty(getName());
        if (value instanceof Collection<?>)
        {
            int valueIndex = getValueIndex();
            if (valueIndex != INDEX_UNDEFINED)
            {
                int idx = 0;
                for (Object o : ((Collection<?>) value))
                {
                    if (idx++ == valueIndex)
                    {
                        return o;
                    }
                }

                // the index is invalid
                return null;
            }
        }

        return value;
    }

    /**
     * Determines the value index of this node. The index is relevant for
     * properties with multiple values. In this case the node is associated with
     * a specific value. This implementation asks the parent node to determine
     * the current index.
     *
     * @return the value index of this node
     */
    @Override
    public int getValueIndex()
    {
        return parent.getChildValueIndex(this);
    }

    /**
     * Removes a child from this node. Leaf nodes do not support children, so
     * this implementation always throws a runtime exception.
     *
     * @param child the node to be removed
     * @throws ConfigurationRuntimeException if the child cannot be removed
     */
    @Override
    public void removeChild(FlatNode child)
    {
        throw new ConfigurationRuntimeException(
                "Cannot remove a child from a leaf node!");
    }

    /**
     * Sets the value of this node. The value is set at the passed in {@code
     * ConfigurationSource}. If this node is not associated with a value (i.e.
     * the node was newly created), this method adds a new value to the
     * represented property. Otherwise the corresponding property value is
     * overridden.
     *
     * @param value the new value
     */
    @Override
    public void setValue(Object value)
    {
        if (hasValue)
        {
            int index = getValueIndex();
            if (index != INDEX_UNDEFINED)
            {
                parent.setMultiProperty(getConfigurationSource(), this, index, value);
            }
            else
            {
                getConfigurationSource().setProperty(getName(), value);
            }
        }

        else
        {
            getConfigurationSource().addProperty(getName(), value);
            hasValue = true;
        }
    }

    /**
     * Returns the {@code FlatConfigurationSource} this node belongs to. This
     * implementation fetches the source from the parent node.
     *
     * @return the owning {@code FlatConfigurationSource}
     */
    @Override
    public FlatConfigurationSource getConfigurationSource()
    {
        return getParent().getConfigurationSource();
    }
}
