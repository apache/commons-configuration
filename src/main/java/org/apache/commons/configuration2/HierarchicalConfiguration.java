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
package org.apache.commons.configuration2;

import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.apache.commons.configuration2.tree.NodeModelSupport;

/**
 * <p>
 * An interface for mutable hierarchical configurations.
 * </p>
 * <p>
 * This interface introduces methods for manipulating tree-like structured
 * configuration sources. Also, all methods defined by the {@code Configuration}
 * interface are available.
 * </p>
 * <p>
 * This interface does not make any assumptions about the concrete type of nodes
 * used by an implementation; this is reflected by a generic type parameter.
 * Concrete implementations may therefore define their own hierarchical
 * structures.
 * </p>
 *
 * @since 2.0
 * @param <T> the type of the nodes used by this hierarchical configuration
 */
public interface HierarchicalConfiguration<T>
    extends Configuration, ImmutableHierarchicalConfiguration, NodeModelSupport<T>
{
    /**
     * Sets the expression engine to be used by this configuration. All property
     * keys this configuration has to deal with will be interpreted by this
     * engine.
     *
     * @param expressionEngine the new expression engine; can be <b>null</b>,
     * then the default expression engine will be used
     */
    void setExpressionEngine(ExpressionEngine expressionEngine);

    /**
     * Adds a collection of nodes at the specified position of the configuration
     * tree. This method works similar to {@code addProperty()}, but
     * instead of a single property a whole collection of nodes can be added -
     * and thus complete configuration sub trees. E.g. with this method it is
     * possible to add parts of another {@code BaseHierarchicalConfiguration}
     * object to this object. If the passed in key refers to
     * an existing and unique node, the new nodes are added to this node.
     * Otherwise a new node will be created at the specified position in the
     * hierarchy.
     *
     * @param key the key where the nodes are to be added; can be <b>null </b>,
     * then they are added to the root node
     * @param nodes a collection with the {@code Node} objects to be
     * added
     */
    void addNodes(String key, Collection<? extends T> nodes);

    /**
     * <p>
     * Returns a hierarchical sub configuration object that wraps the
     * configuration node specified by the given key. This method provides an
     * easy means of accessing sub trees of a hierarchical configuration. In the
     * returned configuration the sub tree can directly be accessed, it becomes
     * the root node of this configuration. Because of this the passed in key
     * must select exactly one configuration node; otherwise an
     * {@code IllegalArgumentException} will be thrown.
     * </p>
     * <p>
     * The difference between this method and the
     * {@link #subset(String)} method is that
     * {@code subset()} supports arbitrary subsets of configuration nodes
     * while {@code configurationAt()} only returns a single sub tree.
     * Please refer to the documentation of the
     * {@link SubnodeConfiguration} class to obtain further information
     * about sub configurations and when they should be used.
     * </p>
     * <p>
     * With the {@code supportUpdate} flag the behavior of the returned
     * sub configuration regarding updates of its parent
     * configuration can be determined. If set to <b>false</b>, the configurations
     * return on independent nodes structures. So changes made on one configuration
     * cannot be seen by the other one. A value of <b>true</b> in contrast creates
     * a direct connection between both configurations - they are then using the
     * same underlying data structures as much as possible. There are however changes
     * which break this connection; for instance, if the sub tree the sub configuration
     * belongs to is completely removed from the parent configuration. If such a
     * change happens, the sub configuration becomes detached from its parent.
     * It can still be used in a normal way, but changes on it are not reflected
     * by the parent and vice verse. Also, it is not possible to reattach a once
     * detached sub configuration.
     * </p>
     *
     * @param key the key that selects the sub tree
     * @param supportUpdates a flag whether the returned sub configuration
     * should be directly connected to its parent
     * @return a hierarchical configuration that contains this sub tree
     * @see SubnodeConfiguration
     */
    HierarchicalConfiguration<T> configurationAt(String key, boolean supportUpdates);

    /**
     * Returns a hierarchical subnode configuration for the node specified by
     * the given key. This is a short form for {@code configurationAt(key,
     * <b>false</b>)}.
     *
     * @param key the key that selects the sub tree
     * @return a hierarchical configuration that contains this sub tree
     * @see SubnodeConfiguration
     */
    HierarchicalConfiguration<T> configurationAt(String key);

    /**
     * Returns a list of sub configurations for all configuration nodes selected
     * by the given key. This method will evaluate the passed in key (using the
     * current {@code ExpressionEngine}) and then create a sub configuration for
     * each returned node (like {@link #configurationAt(String)} ). This is
     * especially useful when dealing with list-like structures. As an example
     * consider the configuration that contains data about database tables and
     * their fields. If you need access to all fields of a certain table, you
     * can simply do
     *
     * <pre>
     * List fields = config.configurationsAt("tables.table(0).fields.field");
     * for(Iterator it = fields.iterator(); it.hasNext();)
     * {
     *     BaseHierarchicalConfiguration sub = (BaseHierarchicalConfiguration) it.next();
     *     // now the children and attributes of the field node can be
     *     // directly accessed
     *     String fieldName = sub.getString("name");
     *     String fieldType = sub.getString("type");
     *     ...
     * </pre>
     *
     * The configuration objects returned are <strong>not</strong> connected to
     * the parent configuration.
     *
     * @param key the key for selecting the desired nodes
     * @return a list with hierarchical configuration objects; each
     *         configuration represents one of the nodes selected by the passed
     *         in key
     */
    List<HierarchicalConfiguration<T>> configurationsAt(String key);

    /**
     * Returns a list of sub configurations for all configuration nodes selected
     * by the given key allowing the caller to specify the
     * {@code supportUpdates} flag. This method works like
     * {@link #configurationsAt(String)}, but with the additional boolean
     * parameter it can be specified whether the returned configurations react
     * on updates of the parent configuration.
     *
     * @param key the key for selecting the desired nodes
     * @param supportUpdates a flag whether the returned sub configuration
     *        should be directly connected to its parent
     * @return a list with hierarchical configuration objects; each
     *         configuration represents one of the nodes selected by the passed
     *         in key
     * @see #configurationsAt(String, boolean)
     */
    List<HierarchicalConfiguration<T>> configurationsAt(String key,
            boolean supportUpdates);

    /**
     * Returns a list with sub configurations for all child nodes of the node
     * selected by the given key. This method works like
     * {@link #immutableChildConfigurationsAt(String)}, but returns a list with
     * mutable configuration objects. The configuration objects returned are
     * <strong>not</strong> connected to the parent configuration.
     *
     * @param key the key for selecting the desired parent node
     * @return a collection with {@code HierarchicalConfiguration} objects for all
     *         child nodes of the selected parent node
     */
    List<HierarchicalConfiguration<T>> childConfigurationsAt(String key);

    /**
     * Returns a list with sub configurations for all child nodes of the node
     * selected by the given key allowing the caller to specify the
     * {@code supportUpdates} flag.
     *
     * @param key the key for selecting the desired parent node
     * @param supportUpdates a flag whether the returned sub configuration
     *        should be directly connected to its parent
     * @return a collection with {@code HierarchicalConfiguration} objects for
     *         all child nodes of the selected parent node
     */
    List<HierarchicalConfiguration<T>> childConfigurationsAt(String key,
            boolean supportUpdates);

    /**
     * Removes all values of the property with the given name and of keys that
     * start with this name. So if there is a property with the key
     * &quot;foo&quot; and a property with the key &quot;foo.bar&quot;, a call
     * of {@code clearTree("foo")} would remove both properties.
     *
     * @param key the key of the property to be removed
     */
    void clearTree(String key);
}
