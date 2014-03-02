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
package org.apache.commons.configuration;

import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration.tree.ExpressionEngine;

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
 * @version $Id$
 * @since 2.0
 * @param <T> the type of the nodes used by this hierarchical configuration
 */
public interface HierarchicalConfiguration<T>
    extends Configuration, ImmutableHierarchicalConfiguration
{
    /**
     * Returns the root node of this hierarchical configuration.
     *
     * @return the root node
     */
    T getRootNode();

    /**
     * Sets the root node of this hierarchical configuration.
     *
     * @param rootNode the root node
     */
    void setRootNode(T rootNode);

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
     * object to this object. (However be aware that a
     * {@code ConfigurationNode} object can only belong to a single
     * configuration. So if nodes from one configuration are directly added to
     * another one using this method, the structure of the source configuration
     * will be broken. In this case you should clone the nodes to be added
     * before calling {@code addNodes()}.) If the passed in key refers to
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
     * Returns a hierarchical subnode configuration object that wraps the
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
     * {@code SubnodeConfiguration} class to obtain further information
     * about subnode configurations and when they should be used.
     * </p>
     * <p>
     * With the {@code supportUpdate} flag the behavior of the returned
     * {@code SubnodeConfiguration} regarding updates of its parent
     * configuration can be determined. A subnode configuration operates on the
     * same nodes as its parent, so changes at one configuration are normally
     * directly visible for the other configuration. There are however changes
     * of the parent configuration, which are not recognized by the subnode
     * configuration per default. An example for this is a reload operation (for
     * file-based configurations): Here the complete node set of the parent
     * configuration is replaced, but the subnode configuration still references
     * the old nodes. If such changes should be detected by the subnode
     * configuration, the {@code supportUpdates} flag must be set to
     * <b>true</b>. This causes the subnode configuration to reevaluate the key
     * used for its creation each time it is accessed. This guarantees that the
     * subnode configuration always stays in sync with its key, even if the
     * parent configuration's data significantly changes. If such a change
     * makes the key invalid - because it now no longer points to exactly one
     * node -, the subnode configuration is not reconstructed, but keeps its
     * old data. It is then quasi detached from its parent.
     * </p>
     *
     * @param key the key that selects the sub tree
     * @param supportUpdates a flag whether the returned subnode configuration
     * should be able to handle updates of its parent
     * @return a hierarchical configuration that contains this sub tree
     * @see SubnodeConfiguration
     */
    SubnodeConfiguration configurationAt(String key, boolean supportUpdates);

    /**
     * Returns a hierarchical subnode configuration for the node specified by
     * the given key. This is a short form for {@code configurationAt(key,
     * <b>false</b>)}.
     *
     * @param key the key that selects the sub tree
     * @return a hierarchical configuration that contains this sub tree
     * @see SubnodeConfiguration
     */
    SubnodeConfiguration configurationAt(String key);

    /**
     * Returns a list of sub configurations for all configuration nodes selected
     * by the given key. This method will evaluate the passed in key (using the
     * current {@code ExpressionEngine}) and then create a subnode
     * configuration for each returned node (like
     * {@link #configurationAt(String)}}). This is especially
     * useful when dealing with list-like structures. As an example consider the
     * configuration that contains data about database tables and their fields.
     * If you need access to all fields of a certain table, you can simply do
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
     * @param key the key for selecting the desired nodes
     * @return a list with hierarchical configuration objects; each
     * configuration represents one of the nodes selected by the passed in key
     */
    List<SubnodeConfiguration> configurationsAt(String key);

    /**
     * Returns a list with sub configurations for all child nodes of the node
     * selected by the given key. This method works like
     * {@link #immutableChildConfigurationsAt(String)}, but returns a list with
     * {@code SubnodeConfiguration} objects.
     *
     * @param key the key for selecting the desired parent node
     * @return a collection with {@code SubnodeConfiguration} objects for all
     *         child nodes of the selected parent node
     */
    List<SubnodeConfiguration> childConfigurationsAt(String key);

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
