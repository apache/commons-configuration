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

import java.util.List;

import org.apache.commons.configuration2.tree.ExpressionEngine;

/**
 * <p>
 * An interface for immutable hierarchical configurations.
 * </p>
 * <p>
 * There are some sources of configuration data that cannot be stored very well
 * in a flat configuration object (like {@link BaseConfiguration}) because then
 * their structure is lost. A prominent example are XML documents.
 * </p>
 * <p>
 * This interface extends the basic {@link ImmutableConfiguration} interface by
 * structured access to configuration properties. An {@link ExpressionEngine} is
 * used to evaluate complex property keys and to map them to nodes of a
 * tree-like structure.
 * </p>
 *
 * @since 2.0
 */
public interface ImmutableHierarchicalConfiguration extends ImmutableConfiguration
{
    /**
     * Returns the expression engine used by this configuration. This method
     * will never return <b>null</b>; if no specific expression engine was set,
     * the default expression engine will be returned.
     *
     * @return the current expression engine
     */
    ExpressionEngine getExpressionEngine();

    /**
     * Returns the maximum defined index for the given key. This is useful if
     * there are multiple values for this key. They can then be addressed
     * separately by specifying indices from 0 to the return value of this
     * method.
     *
     * @param key the key to be checked
     * @return the maximum defined index for this key
     */
    int getMaxIndex(String key);

    /**
     * Returns the name of the root element of this configuration. This
     * information may be of use in some cases, e.g. for sub configurations
     * created using the {@code immutableConfigurationsAt()} method. The exact
     * meaning of the string returned by this method is specific to a concrete
     * implementation. For instance, an XML configuration might return the name
     * of the document element.
     *
     * @return the name of the root element of this configuration
     */
    String getRootElementName();

    /**
     * <p>
     * Returns an immutable hierarchical configuration object that wraps the
     * configuration node specified by the given key. This method provides an
     * easy means of accessing sub trees of a hierarchical configuration. In the
     * returned configuration the sub tree can directly be accessed, it becomes
     * the root node of this configuration. Because of this the passed in key
     * must select exactly one configuration node; otherwise an
     * {@code IllegalArgumentException} will be thrown.
     * </p>
     * <p>
     * The difference between this method and the
     * {@link #immutableSubset(String)} method is that
     * {@code immutableSubset()} supports arbitrary subsets of configuration nodes
     * while {@code immutableConfigurationAt()} only returns a single sub tree.
     * Please refer to the documentation of the
     * {@code SubnodeConfiguration} class to obtain further information
     * about subnode configurations and when they should be used.
     * </p>
     *
     * @param key the key that selects the sub tree
     * @param supportUpdates a flag whether the returned subnode configuration
     * should be able to handle updates of its parent
     * @return a hierarchical configuration that contains this sub tree
     */
    ImmutableHierarchicalConfiguration immutableConfigurationAt(String key,
            boolean supportUpdates);

    /**
     * Returns an immutable hierarchical configuration for the node specified by
     * the given key. This is a short form for {@code immutableConfigurationAt(key,
     * <b>false</b>)}.
     *
     * @param key the key that selects the sub tree
     * @return a hierarchical configuration that contains this sub tree
     */
    ImmutableHierarchicalConfiguration immutableConfigurationAt(String key);

    /**
     * Returns a list of immutable configurations for all configuration nodes selected
     * by the given key. This method will evaluate the passed in key (using the
     * current {@code ExpressionEngine}) and then create an immutable subnode
     * configuration for each returned node (like
     * {@link #immutableConfigurationAt(String)}}). This is especially
     * useful when dealing with list-like structures. As an example consider the
     * configuration that contains data about database tables and their fields.
     * If you need access to all fields of a certain table, you can simply do
     *
     * <pre>
     * List&lt;ImmutableHierarchicalConfiguration&gt; fields =
     *   config.immutableConfigurationsAt("tables.table(0).fields.field");
     * for(Iterator&lt;ImmutableHierarchicalConfiguration&gt; it = fields.iterator();
     *   it.hasNext();)
     * {
     *     ImmutableHierarchicalConfiguration sub = it.next();
     *     // now the children and attributes of the field node can be
     *     // directly accessed
     *     String fieldName = sub.getString("name");
     *     String fieldType = sub.getString("type");
     *     ...
     * </pre>
     *
     * @param key the key for selecting the desired nodes
     * @return a list with immutable hierarchical configuration objects; each
     * configuration represents one of the nodes selected by the passed in key
     */
    List<ImmutableHierarchicalConfiguration> immutableConfigurationsAt(String key);

    /**
     * Returns a list of immutable configurations for all direct child elements
     * of the node selected by the given key. With this method it is possible to
     * inspect the content of a hierarchical structure; all children of a given
     * node can be queried without having to know their exact names. If the
     * passed in key does not point to a single node, an empty list is returned.
     * This is also the result if the node referred to by the key does not have
     * child elements.
     *
     * @param key the key for selecting the desired parent node
     * @return a collection with immutable configurations for all child nodes of
     *         the selected parent node
     */
    List<ImmutableHierarchicalConfiguration> immutableChildConfigurationsAt(
            String key);
}
