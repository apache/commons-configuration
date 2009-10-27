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

import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationRuntimeException;

/**
 * <p>
 * A base class for representing a configuration node for a &quot;flat&quot;
 * configuration source.
 * </p>
 * <p>
 * The {@link FlatConfigurationSource} interface provides a map-oriented access to
 * configuration settings rather than organizing data in a hierarchical
 * structure. Nevertheless, the {@link Configuration} interface defines some
 * operations that are hierarchical in nature. This is required for instance to
 * support enhanced query facilities (expression engines) for all kind of
 * configurations or to allow them to be added to combined configurations.
 * </p>
 * <p>
 * Because of that a way is needed to make the hierarchical operations required
 * available for non-hierarchical configuration sources. The idea is to create a
 * pseudo-hierarchical node structure (consisting of one root node and its
 * children) when necessary. This class is the base class of the nodes that are
 * part of this structure. It defines the general properties of such nodes.
 * There will be concrete sub classes for the root node and the leaf nodes.
 * </p>
 * <p>
 * Note that in contrast to truly hierarchical configuration nodes the nodes
 * used here are pretty simple. For instance, they do not support attributes.
 * Leaf nodes can have a name and a value. The root node has an arbitrary number
 * of children.
 * </p>
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 * @since 2.0
 */
public abstract class FlatNode
{
    /**
     * Constant for an undefined index. This constant is returned by
     * {@link #getValueIndex()} to indicate that this node does not correspond
     * to a single value of a property with multiple values, but represents the
     * whole property.
     */
    public static final int INDEX_UNDEFINED = -1;

    /**
     * Returns the name of this node.
     *
     * @return the name of this node
     */
    public abstract String getName();

    /**
     * Returns the value of this node. An implementation can access the passed
     * in {@code ConfigurationSource} to obtain the value.
     *
     * @return the value of this node
     */
    public abstract Object getValue();

    /**
     * Sets the value of this node. An implementation can access the passed in
     * {@code ConfigurationSource} to set the value.
     *
     * @param value the new value
     * @throws ConfigurationRuntimeException if the value cannot be set
     */
    public abstract void setValue(Object value);

    /**
     * Returns the index of the value represented by this node. This is needed
     * for properties with multiple values: In this case, for each value a
     * separate node instance is created. Manipulating a node instance will only
     * affect the selected value. A return value of {@link #INDEX_UNDEFINED}
     * means that there is only a single value of the represented property (in
     * this case the property is affected as a whole).
     *
     * @return the index of the property value
     */
    public abstract int getValueIndex();

    /**
     * Returns the parent node.
     *
     * @return the parent node
     */
    public abstract FlatNode getParent();

    /**
     * Returns a list with the child nodes of this node. Note that only the root
     * node has children. All children of the root are leaf nodes.
     *
     * @return a list with the child nodes of this node
     */
    public abstract List<FlatNode> getChildren();

    /**
     * Returns a list with the child nodes of this node with the given name.
     *
     * @param name the name of the desired child nodes
     * @return a list with the found children
     */
    public abstract List<FlatNode> getChildren(String name);

    /**
     * Returns the number of children with the given name. If the passed in name
     * is <b>null</b>, the total number of children is returned.
     *
     * @param name the name (can be <b>null</b>)
     * @return the number of children with this name
     */
    public abstract int getChildrenCount(String name);

    /**
     * Returns the child node of this node with the given index.
     *
     * @param index the index (0-based)
     * @return the child with this index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public abstract FlatNode getChild(int index);

    /**
     * Adds a child node to this node with the given name.
     *
     * @param name the name of the new child
     * @return the newly created child node
     * @throws ConfigurationRuntimeException if the child node cannot be added
     */
    public abstract FlatNode addChild(String name);

    /**
     * Removes the specified child node from this node. This may also affect the
     * owning {@code ConfigurationSource}, so this object is also passed to this
     * method.
     *
     * @param child the child to be removed
     * @throws ConfigurationRuntimeException if the child cannot be removed
     */
    public abstract void removeChild(FlatNode child);

    /**
     * Returns the {@code FlatConfigurationSource} object this node belongs to.
     *
     * @return the owning {@code FlatConfigurationSource}
     */
    public abstract FlatConfigurationSource getConfigurationSource();
}
