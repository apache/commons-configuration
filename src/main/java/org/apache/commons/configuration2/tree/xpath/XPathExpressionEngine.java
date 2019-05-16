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
package org.apache.commons.configuration2.tree.xpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.apache.commons.configuration2.tree.NodeAddData;
import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.configuration2.tree.QueryResult;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * A specialized implementation of the {@code ExpressionEngine} interface that
 * is able to evaluate XPATH expressions.
 * </p>
 * <p>
 * This class makes use of <a href="http://commons.apache.org/jxpath/"> Commons
 * JXPath</a> for handling XPath expressions and mapping them to the nodes of a
 * hierarchical configuration. This makes the rich and powerful XPATH syntax
 * available for accessing properties from a configuration object.
 * </p>
 * <p>
 * For selecting properties arbitrary XPATH expressions can be used, which
 * select single or multiple configuration nodes. The associated
 * {@code Configuration} instance will directly pass the specified property keys
 * into this engine. If a key is not syntactically correct, an exception will be
 * thrown.
 * </p>
 * <p>
 * For adding new properties, this expression engine uses a specific syntax: the
 * &quot;key&quot; of a new property must consist of two parts that are
 * separated by whitespace:
 * </p>
 * <ol>
 * <li>An XPATH expression selecting a single node, to which the new element(s)
 * are to be added. This can be an arbitrary complex expression, but it must
 * select exactly one node, otherwise an exception will be thrown.</li>
 * <li>The name of the new element(s) to be added below this parent node. Here
 * either a single node name or a complete path of nodes (separated by the
 * &quot;/&quot; character or &quot;@&quot; for an attribute) can be specified.</li>
 * </ol>
 * <p>
 * Some examples for valid keys that can be passed into the configuration's
 * {@code addProperty()} method follow:
 * </p>
 *
 * <pre>
 * &quot;/tables/table[1] type&quot;
 * </pre>
 *
 * <p>
 * This will add a new {@code type} node as a child of the first {@code table}
 * element.
 * </p>
 *
 * <pre>
 * &quot;/tables/table[1] @type&quot;
 * </pre>
 *
 * <p>
 * Similar to the example above, but this time a new attribute named
 * {@code type} will be added to the first {@code table} element.
 * </p>
 *
 * <pre>
 * &quot;/tables table/fields/field/name&quot;
 * </pre>
 *
 * <p>
 * This example shows how a complex path can be added. Parent node is the
 * {@code tables} element. Here a new branch consisting of the nodes
 * {@code table}, {@code fields}, {@code field}, and {@code name} will be added.
 * </p>
 *
 * <pre>
 * &quot;/tables table/fields/field@type&quot;
 * </pre>
 *
 * <p>
 * This is similar to the last example, but in this case a complex path ending
 * with an attribute is defined.
 * </p>
 * <p>
 * <strong>Note:</strong> This extended syntax for adding properties only works
 * with the {@code addProperty()} method. {@code setProperty()} does not support
 * creating new nodes this way.
 * </p>
 * <p>
 * From version 1.7 on, it is possible to use regular keys in calls to
 * {@code addProperty()} (i.e. keys that do not have to contain a whitespace as
 * delimiter). In this case the key is evaluated, and the biggest part pointing
 * to an existing node is determined. The remaining part is then added as new
 * path. As an example consider the key
 * </p>
 *
 * <pre>
 * &quot;tables/table[last()]/fields/field/name&quot;
 * </pre>
 *
 * <p>
 * If the key does not point to an existing node, the engine will check the
 * paths {@code "tables/table[last()]/fields/field"},
 * {@code "tables/table[last()]/fields"}, {@code "tables/table[last()]"}, and so
 * on, until a key is found which points to a node. Let's assume that the last
 * key listed above can be resolved in this way. Then from this key the
 * following key is derived: {@code "tables/table[last()] fields/field/name"} by
 * appending the remaining part after a whitespace. This key can now be
 * processed using the original algorithm. Keys of this form can also be used
 * with the {@code setProperty()} method. However, it is still recommended to
 * use the old format because it makes explicit at which position new nodes
 * should be added. For keys without a whitespace delimiter there may be
 * ambiguities.
 * </p>
 *
 * @since 1.3
 */
public class XPathExpressionEngine implements ExpressionEngine
{
    /** Constant for the path delimiter. */
    static final String PATH_DELIMITER = "/";

    /** Constant for the attribute delimiter. */
    static final String ATTR_DELIMITER = "@";

    /** Constant for the delimiters for splitting node paths. */
    private static final String NODE_PATH_DELIMITERS = PATH_DELIMITER
            + ATTR_DELIMITER;

    /**
     * Constant for a space which is used as delimiter in keys for adding
     * properties.
     */
    private static final String SPACE = " ";

    /** Constant for a default size of a key buffer. */
    private static final int BUF_SIZE = 128;

    /** Constant for the start of an index expression. */
    private static final char START_INDEX = '[';

    /** Constant for the end of an index expression. */
    private static final char END_INDEX = ']';

    /** The internally used context factory. */
    private final XPathContextFactory contextFactory;

    /**
     * Creates a new instance of {@code XPathExpressionEngine} with default
     * settings.
     */
    public XPathExpressionEngine()
    {
        this(new XPathContextFactory());
    }

    /**
     * Creates a new instance of {@code XPathExpressionEngine} and sets the
     * context factory. This constructor is mainly used for testing purposes.
     *
     * @param factory the {@code XPathContextFactory}
     */
    XPathExpressionEngine(final XPathContextFactory factory)
    {
        contextFactory = factory;
    }

    /**
     * {@inheritDoc} This implementation interprets the passed in key as an XPATH
     * expression.
     */
    @Override
    public <T> List<QueryResult<T>> query(final T root, final String key,
            final NodeHandler<T> handler)
    {
        if (StringUtils.isEmpty(key))
        {
            final QueryResult<T> result = createResult(root);
            return Collections.singletonList(result);
        }
        final JXPathContext context = createContext(root, handler);
        List<?> results = context.selectNodes(key);
        if (results == null)
        {
            results = Collections.emptyList();
        }
        return convertResults(results);
    }

    /**
     * {@inheritDoc} This implementation creates an XPATH expression that
     * selects the given node (under the assumption that the passed in parent
     * key is valid). As the {@code nodeKey()} implementation of
     * {@link org.apache.commons.configuration2.tree.DefaultExpressionEngine
     * DefaultExpressionEngine} this method does not return indices for nodes.
     * So all child nodes of a given parent with the same name have the same
     * key.
     */
    @Override
    public <T> String nodeKey(final T node, final String parentKey, final NodeHandler<T> handler)
    {
        if (parentKey == null)
        {
            // name of the root node
            return StringUtils.EMPTY;
        }
        else if (handler.nodeName(node) == null)
        {
            // paranoia check for undefined node names
            return parentKey;
        }

        else
        {
            final StringBuilder buf =
                    new StringBuilder(parentKey.length()
                            + handler.nodeName(node).length()
                            + PATH_DELIMITER.length());
            if (parentKey.length() > 0)
            {
                buf.append(parentKey);
                buf.append(PATH_DELIMITER);
            }
            buf.append(handler.nodeName(node));
            return buf.toString();
        }
    }

    @Override
    public String attributeKey(final String parentKey, final String attributeName)
    {
        final StringBuilder buf =
                new StringBuilder(StringUtils.length(parentKey)
                        + StringUtils.length(attributeName)
                        + PATH_DELIMITER.length() + ATTR_DELIMITER.length());
        if (StringUtils.isNotEmpty(parentKey))
        {
            buf.append(parentKey).append(PATH_DELIMITER);
        }
        buf.append(ATTR_DELIMITER).append(attributeName);
        return buf.toString();
    }

    /**
     * {@inheritDoc} This implementation works similar to {@code nodeKey()}, but
     * always adds an index expression to the resulting key.
     */
    @Override
    public <T> String canonicalKey(final T node, final String parentKey,
            final NodeHandler<T> handler)
    {
        final T parent = handler.getParent(node);
        if (parent == null)
        {
            // this is the root node
            return StringUtils.defaultString(parentKey);
        }

        final StringBuilder buf = new StringBuilder(BUF_SIZE);
        if (StringUtils.isNotEmpty(parentKey))
        {
            buf.append(parentKey).append(PATH_DELIMITER);
        }
        buf.append(handler.nodeName(node));
        buf.append(START_INDEX);
        buf.append(determineIndex(parent, node, handler));
        buf.append(END_INDEX);
        return buf.toString();
    }

    /**
     * {@inheritDoc} The expected format of the passed in key is explained in
     * the class comment.
     */
    @Override
    public <T> NodeAddData<T> prepareAdd(final T root, final String key,
            final NodeHandler<T> handler)
    {
        if (key == null)
        {
            throw new IllegalArgumentException(
                    "prepareAdd: key must not be null!");
        }

        String addKey = key;
        int index = findKeySeparator(addKey);
        if (index < 0)
        {
            addKey = generateKeyForAdd(root, addKey, handler);
            index = findKeySeparator(addKey);
        }
        else if (index >= addKey.length() - 1)
        {
            invalidPath(addKey, " new node path must not be empty.");
        }

        final List<QueryResult<T>> nodes =
                query(root, addKey.substring(0, index).trim(), handler);
        if (nodes.size() != 1)
        {
            throw new IllegalArgumentException("prepareAdd: key '" + key
                    + "' must select exactly one target node!");
        }

        return createNodeAddData(addKey.substring(index).trim(), nodes.get(0));
    }

    /**
     * Creates the {@code JXPathContext} to be used for executing a query. This
     * method delegates to the context factory.
     *
     * @param root the configuration root node
     * @param handler the node handler
     * @return the new context
     */
    private <T> JXPathContext createContext(final T root, final NodeHandler<T> handler)
    {
        return getContextFactory().createContext(root, handler);
    }

    /**
     * Creates a {@code NodeAddData} object as a result of a
     * {@code prepareAdd()} operation. This method interprets the passed in path
     * of the new node.
     *
     * @param path the path of the new node
     * @param parentNodeResult the parent node
     * @param <T> the type of the nodes involved
     */
    <T> NodeAddData<T> createNodeAddData(final String path,
            final QueryResult<T> parentNodeResult)
    {
        if (parentNodeResult.isAttributeResult())
        {
            invalidPath(path, " cannot add properties to an attribute.");
        }
        final List<String> pathNodes = new LinkedList<>();
        String lastComponent = null;
        boolean attr = false;
        boolean first = true;

        final StringTokenizer tok =
                new StringTokenizer(path, NODE_PATH_DELIMITERS, true);
        while (tok.hasMoreTokens())
        {
            final String token = tok.nextToken();
            if (PATH_DELIMITER.equals(token))
            {
                if (attr)
                {
                    invalidPath(path, " contains an attribute"
                            + " delimiter at a disallowed position.");
                }
                if (lastComponent == null)
                {
                    invalidPath(path,
                            " contains a '/' at a disallowed position.");
                }
                pathNodes.add(lastComponent);
                lastComponent = null;
            }

            else if (ATTR_DELIMITER.equals(token))
            {
                if (attr)
                {
                    invalidPath(path,
                            " contains multiple attribute delimiters.");
                }
                if (lastComponent == null && !first)
                {
                    invalidPath(path,
                            " contains an attribute delimiter at a disallowed position.");
                }
                if (lastComponent != null)
                {
                    pathNodes.add(lastComponent);
                }
                attr = true;
                lastComponent = null;
            }

            else
            {
                lastComponent = token;
            }
            first = false;
        }

        if (lastComponent == null)
        {
            invalidPath(path, "contains no components.");
        }

        return new NodeAddData<>(parentNodeResult.getNode(), lastComponent,
                attr, pathNodes);
    }

    /**
     * Returns the {@code XPathContextFactory} used by this instance.
     *
     * @return the {@code XPathContextFactory}
     */
    XPathContextFactory getContextFactory()
    {
        return contextFactory;
    }

    /**
     * Tries to generate a key for adding a property. This method is called if a
     * key was used for adding properties which does not contain a space
     * character. It splits the key at its single components and searches for
     * the last existing component. Then a key compatible key for adding
     * properties is generated.
     *
     * @param root the root node of the configuration
     * @param key the key in question
     * @param handler the node handler
     * @return the key to be used for adding the property
     */
    private <T> String generateKeyForAdd(final T root, final String key,
            final NodeHandler<T> handler)
    {
        int pos = key.lastIndexOf(PATH_DELIMITER, key.length());

        while (pos >= 0)
        {
            final String keyExisting = key.substring(0, pos);
            if (!query(root, keyExisting, handler).isEmpty())
            {
                final StringBuilder buf = new StringBuilder(key.length() + 1);
                buf.append(keyExisting).append(SPACE);
                buf.append(key.substring(pos + 1));
                return buf.toString();
            }
            pos = key.lastIndexOf(PATH_DELIMITER, pos - 1);
        }

        return SPACE + key;
    }

    /**
     * Determines the index of the given child node in the node list of its
     * parent.
     *
     * @param parent the parent node
     * @param child the child node
     * @param handler the node handler
     * @param <T> the type of the nodes involved
     * @return the index of this child node
     */
    private static <T> int determineIndex(final T parent, final T child,
            final NodeHandler<T> handler)
    {
        return handler.getChildren(parent, handler.nodeName(child)).indexOf(
                child) + 1;
    }

    /**
     * Helper method for throwing an exception about an invalid path.
     *
     * @param path the invalid path
     * @param msg the exception message
     */
    private static void invalidPath(final String path, final String msg)
    {
        throw new IllegalArgumentException("Invalid node path: \"" + path
                + "\" " + msg);
    }

    /**
     * Determines the position of the separator in a key for adding new
     * properties. If no delimiter is found, result is -1.
     *
     * @param key the key
     * @return the position of the delimiter
     */
    private static int findKeySeparator(final String key)
    {
        int index = key.length() - 1;
        while (index >= 0 && !Character.isWhitespace(key.charAt(index)))
        {
            index--;
        }
        return index;
    }

    /**
     * Converts the objects returned as query result from the JXPathContext to
     * query result objects.
     *
     * @param results the list with results from the context
     * @param <T> the type of results to be produced
     * @return the result list
     */
    private static <T> List<QueryResult<T>> convertResults(final List<?> results)
    {
        final List<QueryResult<T>> queryResults =
                new ArrayList<>(results.size());
        for (final Object res : results)
        {
            final QueryResult<T> queryResult = createResult(res);
            queryResults.add(queryResult);
        }
        return queryResults;
    }

    /**
     * Creates a {@code QueryResult} object from the given result object of a
     * query. Because of the node pointers involved result objects can only be
     * of two types:
     * <ul>
     * <li>nodes of type T</li>
     * <li>attribute results already wrapped in {@code QueryResult} objects</li>
     * </ul>
     * This method performs a corresponding cast. Warnings can be suppressed
     * because of the implementation of the query functionality.
     *
     * @param resObj the query result object
     * @param <T> the type of the result to be produced
     * @return the {@code QueryResult}
     */
    @SuppressWarnings("unchecked")
    private static <T> QueryResult<T> createResult(final Object resObj)
    {
        if (resObj instanceof QueryResult)
        {
            return (QueryResult<T>) resObj;
        }
        return QueryResult.createNodeResult((T) resObj);
    }

    // static initializer: registers the configuration node pointer factory
    static
    {
        JXPathContextReferenceImpl
                .addNodePointerFactory(new ConfigurationNodePointerFactory());
    }
}
