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
package org.apache.commons.configuration2.tree;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * A default implementation of the {@code ExpressionEngine} interface
 * providing the &quot;native&quot; expression language for hierarchical
 * configurations.
 * </p>
 * <p>
 * This class implements a rather simple expression language for navigating
 * through a hierarchy of configuration nodes. It supports the following
 * operations:
 * </p>
 * <ul>
 * <li>Navigating from a node to one of its children using the child node
 * delimiter, which is by the default a dot (&quot;.&quot;).</li>
 * <li>Navigating from a node to one of its attributes using the attribute node
 * delimiter, which by default follows the XPATH like syntax
 * <code>[@&lt;attributeName&gt;]</code>.</li>
 * <li>If there are multiple child or attribute nodes with the same name, a
 * specific node can be selected using a numerical index. By default indices are
 * written in parenthesis.</li>
 * </ul>
 * <p>
 * As an example consider the following XML document:
 * </p>
 *
 * <pre>
 *  &lt;database&gt;
 *    &lt;tables&gt;
 *      &lt;table type=&quot;system&quot;&gt;
 *        &lt;name&gt;users&lt;/name&gt;
 *        &lt;fields&gt;
 *          &lt;field&gt;
 *            &lt;name&gt;lid&lt;/name&gt;
 *            &lt;type&gt;long&lt;/name&gt;
 *          &lt;/field&gt;
 *          &lt;field&gt;
 *            &lt;name&gt;usrName&lt;/name&gt;
 *            &lt;type&gt;java.lang.String&lt;/type&gt;
 *          &lt;/field&gt;
 *         ...
 *        &lt;/fields&gt;
 *      &lt;/table&gt;
 *      &lt;table&gt;
 *        &lt;name&gt;documents&lt;/name&gt;
 *        &lt;fields&gt;
 *          &lt;field&gt;
 *            &lt;name&gt;docid&lt;/name&gt;
 *            &lt;type&gt;long&lt;/type&gt;
 *          &lt;/field&gt;
 *          ...
 *        &lt;/fields&gt;
 *      &lt;/table&gt;
 *      ...
 *    &lt;/tables&gt;
 *  &lt;/database&gt;
 * </pre>
 *
 * <p>
 * If this document is parsed and stored in a hierarchical configuration object,
 * for instance the key {@code tables.table(0).name} can be used to find
 * out the name of the first table. In opposite {@code tables.table.name}
 * would return a collection with the names of all available tables. Similarly
 * the key {@code tables.table(1).fields.field.name} returns a collection
 * with the names of all fields of the second table. If another index is added
 * after the {@code field} element, a single field can be accessed:
 * {@code tables.table(1).fields.field(0).name}. The key
 * {@code tables.table(0)[@type]} would select the type attribute of the
 * first table.
 * </p>
 * <p>
 * This example works with the default values for delimiters and index markers.
 * It is also possible to set custom values for these properties so that you can
 * adapt a {@code DefaultExpressionEngine} to your personal needs.
 * </p>
 * <p>
 * The concrete symbols used by an instance are determined by a
 * {@link DefaultExpressionEngineSymbols} object passed to the constructor.
 * By providing a custom symbols object the syntax for querying properties in
 * a hierarchical configuration can be altered.
 * </p>
 * <p>
 * Instances of this class are thread-safe and can be shared between multiple
 * hierarchical configuration objects.
 * </p>
 *
 * @since 1.3
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 */
public class DefaultExpressionEngine implements ExpressionEngine
{
    /**
     * A default instance of this class that is used as expression engine for
     * hierarchical configurations per default.
     */
    public static final DefaultExpressionEngine INSTANCE =
            new DefaultExpressionEngine(
                    DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS);

    /** The symbols used by this instance. */
    private final DefaultExpressionEngineSymbols symbols;

    /** The matcher for node names. */
    private final NodeMatcher<String> nameMatcher;

    /**
     * Creates a new instance of {@code DefaultExpressionEngine} and initializes
     * its symbols.
     *
     * @param syms the object with the symbols (must not be <b>null</b>)
     * @throws IllegalArgumentException if the symbols are <b>null</b>
     */
    public DefaultExpressionEngine(final DefaultExpressionEngineSymbols syms)
    {
        this(syms, null);
    }

    /**
     * Creates a new instance of {@code DefaultExpressionEngine} and initializes
     * its symbols and the matcher for comparing node names. The passed in
     * matcher is always used when the names of nodes have to be matched against
     * parts of configuration keys.
     *
     * @param syms the object with the symbols (must not be <b>null</b>)
     * @param nodeNameMatcher the matcher for node names; can be <b>null</b>,
     *        then a default matcher is used
     * @throws IllegalArgumentException if the symbols are <b>null</b>
     */
    public DefaultExpressionEngine(final DefaultExpressionEngineSymbols syms,
            final NodeMatcher<String> nodeNameMatcher)
    {
        if (syms == null)
        {
            throw new IllegalArgumentException("Symbols must not be null!");
        }

        symbols = syms;
        nameMatcher =
                (nodeNameMatcher != null) ? nodeNameMatcher
                        : NodeNameMatchers.EQUALS;
    }

    /**
     * Returns the {@code DefaultExpressionEngineSymbols} object associated with
     * this instance.
     *
     * @return the {@code DefaultExpressionEngineSymbols} used by this engine
     * @since 2.0
     */
    public DefaultExpressionEngineSymbols getSymbols()
    {
        return symbols;
    }

    /**
     * {@inheritDoc} This method supports the syntax as described in the class
     * comment.
     */
    @Override
    public <T> List<QueryResult<T>> query(final T root, final String key,
            final NodeHandler<T> handler)
    {
        final List<QueryResult<T>> results = new LinkedList<>();
        findNodesForKey(new DefaultConfigurationKey(this, key).iterator(),
                root, results, handler);
        return results;
    }

    /**
     * {@inheritDoc} This implementation takes the
     * given parent key, adds a property delimiter, and then adds the node's
     * name.
     * The name of the root node is a blank string. Note that no indices are
     * returned.
     */
    @Override
    public <T> String nodeKey(final T node, final String parentKey, final NodeHandler<T> handler)
    {
        if (parentKey == null)
        {
            // this is the root node
            return StringUtils.EMPTY;
        }
        final DefaultConfigurationKey key = new DefaultConfigurationKey(this,
                parentKey);
            key.append(handler.nodeName(node), true);
        return key.toString();
    }

    @Override
    public String attributeKey(final String parentKey, final String attributeName)
    {
        final DefaultConfigurationKey key =
                new DefaultConfigurationKey(this, parentKey);
        key.appendAttribute(attributeName);
        return key.toString();
    }

    /**
     * {@inheritDoc} This implementation works similar to {@code nodeKey()};
     * however, each key returned by this method has an index (except for the
     * root node). The parent key is prepended to the name of the current node
     * in any case and without further checks. If it is <b>null</b>, only the
     * name of the current node with its index is returned.
     */
    @Override
    public <T> String canonicalKey(final T node, final String parentKey,
            final NodeHandler<T> handler)
    {
        final String nodeName = handler.nodeName(node);
        final T parent = handler.getParent(node);
        final DefaultConfigurationKey key =
                new DefaultConfigurationKey(this, parentKey);
        key.append(StringUtils.defaultString(nodeName));

        if (parent != null)
        {
            // this is not the root key
            key.appendIndex(determineIndex(node, parent, nodeName, handler));
        }
        return key.toString();
    }

    /**
     * <p>
     * Prepares Adding the property with the specified key.
     * </p>
     * <p>
     * To be able to deal with the structure supported by hierarchical
     * configuration implementations the passed in key is of importance,
     * especially the indices it might contain. The following example should
     * clarify this: Suppose the current node structure looks like the
     * following:
     * </p>
     * <pre>
     *  tables
     *     +-- table
     *             +-- name = user
     *             +-- fields
     *                     +-- field
     *                             +-- name = uid
     *                     +-- field
     *                             +-- name = firstName
     *                     ...
     *     +-- table
     *             +-- name = documents
     *             +-- fields
     *                    ...
     * </pre>
     * <p>
     * In this example a database structure is defined, e.g. all fields of the
     * first table could be accessed using the key
     * {@code tables.table(0).fields.field.name}. If now properties are
     * to be added, it must be exactly specified at which position in the
     * hierarchy the new property is to be inserted. So to add a new field name
     * to a table it is not enough to say just
     * </p>
     * <pre>
     * config.addProperty(&quot;tables.table.fields.field.name&quot;, &quot;newField&quot;);
     * </pre>
     * <p>
     * The statement given above contains some ambiguity. For instance it is not
     * clear, to which table the new field should be added. If this method finds
     * such an ambiguity, it is resolved by following the last valid path. Here
     * this would be the last table. The same is true for the {@code field};
     * because there are multiple fields and no explicit index is provided, a
     * new {@code name} property would be added to the last field - which
     * is probably not what was desired.
     * </p>
     * <p>
     * To make things clear explicit indices should be provided whenever
     * possible. In the example above the exact table could be specified by
     * providing an index for the {@code table} element as in
     * {@code tables.table(1).fields}. By specifying an index it can
     * also be expressed that at a given position in the configuration tree a
     * new branch should be added. In the example above we did not want to add
     * an additional {@code name} element to the last field of the table,
     * but we want a complete new {@code field} element. This can be
     * achieved by specifying an invalid index (like -1) after the element where
     * a new branch should be created. Given this our example would run:
     * </p>
     * <pre>
     * config.addProperty(&quot;tables.table(1).fields.field(-1).name&quot;, &quot;newField&quot;);
     * </pre>
     * <p>
     * With this notation it is possible to add new branches everywhere. We
     * could for instance create a new {@code table} element by
     * specifying
     * </p>
     * <pre>
     * config.addProperty(&quot;tables.table(-1).fields.field.name&quot;, &quot;newField2&quot;);
     * </pre>
     * <p>
     * (Note that because after the {@code table} element a new branch is
     * created indices in following elements are not relevant; the branch is new
     * so there cannot be any ambiguities.)
     * </p>
     *
     * @param <T> the type of the nodes to be dealt with
     * @param root the root node of the nodes hierarchy
     * @param key the key of the new property
     * @param handler the node handler
     * @return a data object with information needed for the add operation
     */
    @Override
    public <T> NodeAddData<T> prepareAdd(final T root, final String key, final NodeHandler<T> handler)
    {
        final DefaultConfigurationKey.KeyIterator it = new DefaultConfigurationKey(
                this, key).iterator();
        if (!it.hasNext())
        {
            throw new IllegalArgumentException(
                    "Key for add operation must be defined!");
        }

        final T parent = findLastPathNode(it, root, handler);
        final List<String> pathNodes = new LinkedList<>();

        while (it.hasNext())
        {
            if (!it.isPropertyKey())
            {
                throw new IllegalArgumentException(
                        "Invalid key for add operation: " + key
                                + " (Attribute key in the middle.)");
            }
            pathNodes.add(it.currentKey());
            it.next();
        }

        return new NodeAddData<>(parent, it.currentKey(), !it.isPropertyKey(),
                pathNodes);
    }

    /**
     * Recursive helper method for evaluating a key. This method processes all
     * facets of a configuration key, traverses the tree of properties and
     * fetches the results of all matching properties.
     *
     * @param <T> the type of nodes to be dealt with
     * @param keyPart the configuration key iterator
     * @param node the current node
     * @param results here the found results are stored
     * @param handler the node handler
     */
    protected <T> void findNodesForKey(
            final DefaultConfigurationKey.KeyIterator keyPart, final T node,
            final Collection<QueryResult<T>> results, final NodeHandler<T> handler)
    {
        if (!keyPart.hasNext())
        {
            results.add(QueryResult.createNodeResult(node));
        }

        else
        {
            final String key = keyPart.nextKey(false);
            if (keyPart.isPropertyKey())
            {
                processSubNodes(keyPart, findChildNodesByName(handler, node, key),
                        results, handler);
            }
            if (keyPart.isAttribute() && !keyPart.hasNext())
            {
                if (handler.getAttributeValue(node, key) != null)
                {
                    results.add(QueryResult.createAttributeResult(node, key));
                }
            }
        }
    }

    /**
     * Finds the last existing node for an add operation. This method traverses
     * the node tree along the specified key. The last existing node on this
     * path is returned.
     *
     * @param <T> the type of the nodes to be dealt with
     * @param keyIt the key iterator
     * @param node the current node
     * @param handler the node handler
     * @return the last existing node on the given path
     */
    protected <T> T findLastPathNode(final DefaultConfigurationKey.KeyIterator keyIt,
            final T node, final NodeHandler<T> handler)
    {
        final String keyPart = keyIt.nextKey(false);

        if (keyIt.hasNext())
        {
            if (!keyIt.isPropertyKey())
            {
                // Attribute keys can only appear as last elements of the path
                throw new IllegalArgumentException(
                        "Invalid path for add operation: "
                                + "Attribute key in the middle!");
            }
            final int idx =
                    keyIt.hasIndex() ? keyIt.getIndex() : handler
                            .getMatchingChildrenCount(node, nameMatcher,
                                    keyPart) - 1;
            if (idx < 0
                    || idx >= handler.getMatchingChildrenCount(node,
                            nameMatcher, keyPart))
            {
                return node;
            }
            return findLastPathNode(keyIt,
                    findChildNodesByName(handler, node, keyPart).get(idx),
                    handler);
        }
        return node;
    }

    /**
     * Called by {@code findNodesForKey()} to process the sub nodes of
     * the current node depending on the type of the current key part (children,
     * attributes, or both).
     *
     * @param <T> the type of the nodes to be dealt with
     * @param keyPart the key part
     * @param subNodes a list with the sub nodes to process
     * @param nodes the target collection
     * @param handler the node handler
     */
    private <T> void processSubNodes(final DefaultConfigurationKey.KeyIterator keyPart,
            final List<T> subNodes, final Collection<QueryResult<T>> nodes, final NodeHandler<T> handler)
    {
        if (keyPart.hasIndex())
        {
            if (keyPart.getIndex() >= 0 && keyPart.getIndex() < subNodes.size())
            {
                findNodesForKey((DefaultConfigurationKey.KeyIterator) keyPart
                        .clone(), subNodes.get(keyPart.getIndex()), nodes, handler);
            }
        }
        else
        {
            for (final T node : subNodes)
            {
                findNodesForKey((DefaultConfigurationKey.KeyIterator) keyPart
                        .clone(), node, nodes, handler);
            }
        }
    }

    /**
     * Determines the index of the given node based on its parent node.
     *
     * @param node the current node
     * @param parent the parent node
     * @param nodeName the name of the current node
     * @param handler the node handler
     * @param <T> the type of the nodes to be dealt with
     * @return the index of this node
     */
    private <T> int determineIndex(final T node, final T parent, final String nodeName,
                                          final NodeHandler<T> handler)
    {
        return findChildNodesByName(handler, parent, nodeName).indexOf(node);
    }

    /**
     * Returns a list with all child nodes of the given parent node which match
     * the specified node name. The match is done using the current node name
     * matcher.
     *
     * @param handler the {@code NodeHandler}
     * @param parent the parent node
     * @param nodeName the name of the current node
     * @param <T> the type of the nodes to be dealt with
     * @return a list with all matching child nodes
     */
    private <T> List<T> findChildNodesByName(final NodeHandler<T> handler, final T parent,
            final String nodeName)
    {
        return handler.getMatchingChildren(parent, nameMatcher, nodeName);
    }
}
