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
package org.apache.commons.configuration.tree.xpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.ExpressionEngine;
import org.apache.commons.configuration.tree.NodeAddData;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * A specialized implementation of the <code>ExpressionEngine</code> interface
 * that is able to evaluate XPATH expressions.
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
 * <code>Configuration</code> instance will directly pass the specified property
 * keys into this engine. If a key is not syntactically correct, an exception
 * will be thrown.
 * </p>
 * <p>
 * For adding new properties, this expression engine uses a specific syntax: the
 * &quot;key&quot; of a new property must consist of two parts that are
 * separated by whitespace:
 * <ol>
 * <li>An XPATH expression selecting a single node, to which the new element(s)
 * are to be added. This can be an arbitrary complex expression, but it must
 * select exactly one node, otherwise an exception will be thrown.</li>
 * <li>The name of the new element(s) to be added below this parent node. Here
 * either a single node name or a complete path of nodes (separated by the
 * &quot;/&quot; character or &quot;@&quot; for an attribute) can be specified.</li>
 * </ol>
 * Some examples for valid keys that can be passed into the configuration's
 * <code>addProperty()</code> method follow:
 * </p>
 * <p>
 *
 * <pre>
 * &quot;/tables/table[1] type&quot;
 * </pre>
 *
 * </p>
 * <p>
 * This will add a new <code>type</code> node as a child of the first
 * <code>table</code> element.
 * </p>
 * <p>
 *
 * <pre>
 * &quot;/tables/table[1] @type&quot;
 * </pre>
 *
 * </p>
 * <p>
 * Similar to the example above, but this time a new attribute named
 * <code>type</code> will be added to the first <code>table</code> element.
 * </p>
 * <p>
 *
 * <pre>
 * &quot;/tables table/fields/field/name&quot;
 * </pre>
 *
 * </p>
 * <p>
 * This example shows how a complex path can be added. Parent node is the
 * <code>tables</code> element. Here a new branch consisting of the nodes
 * <code>table</code>, <code>fields</code>, <code>field</code>, and
 * <code>name</code> will be added.
 * </p>
 * <p>
 *
 * <pre>
 * &quot;/tables table/fields/field@type&quot;
 * </pre>
 *
 * </p>
 * <p>
 * This is similar to the last example, but in this case a complex path ending
 * with an attribute is defined.
 * </p>
 * <p>
 * <strong>Note:</strong> This extended syntax for adding properties only works
 * with the <code>addProperty()</code> method. <code>setProperty()</code> does
 * not support creating new nodes this way.
 * </p>
 * <p>
 * From version 1.7 on, it is possible to use regular keys in calls to
 * <code>addProperty()</code> (i.e. keys that do not have to contain a
 * whitespace as delimiter). In this case the key is evaluated, and the biggest
 * part pointing to an existing node is determined. The remaining part is then
 * added as new path. As an example consider the key
 *
 * <pre>
 * &quot;tables/table[last()]/fields/field/name&quot;
 * </pre>
 *
 * If the key does not point to an existing node, the engine will check the
 * paths <code>&quot;tables/table[last()]/fields/field&quot;</code>,
 * <code>&quot;tables/table[last()]/fields&quot;</code>,
 * <code>&quot;tables/table[last()]&quot;</code>, and so on, until a key is
 * found which points to a node. Let's assume that the last key listed above can
 * be resolved in this way. Then from this key the following key is derived:
 * <code>&quot;tables/table[last()] fields/field/name&quot;</code> by appending
 * the remaining part after a whitespace. This key can now be processed using
 * the original algorithm. Keys of this form can also be used with the
 * <code>setProperty()</code> method. However, it is still recommended to use
 * the old format because it makes explicit at which position new nodes should
 * be added. For keys without a whitespace delimiter there may be ambiguities.
 * </p>
 *
 * @since 1.3
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
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

    /**
     * Executes a query. The passed in property key is directly passed to a
     * JXPath context.
     *
     * @param root the configuration root node
     * @param key the query to be executed
     * @return a list with the nodes that are selected by the query
     */
    public List query(ConfigurationNode root, String key)
    {
        if (StringUtils.isEmpty(key))
        {
            List result = new ArrayList(1);
            result.add(root);
            return result;
        }
        else
        {
            JXPathContext context = createContext(root, key);
            List result = context.selectNodes(key);
            return (result != null) ? result : Collections.EMPTY_LIST;
        }
    }

    /**
     * Returns a (canonical) key for the given node based on the parent's key.
     * This implementation will create an XPATH expression that selects the
     * given node (under the assumption that the passed in parent key is valid).
     * As the <code>nodeKey()</code> implementation of
     * <code>{@link org.apache.commons.configuration.tree.DefaultExpressionEngine DefaultExpressionEngine}</code>
     * this method will not return indices for nodes. So all child nodes of a
     * given parent with the same name will have the same key.
     *
     * @param node the node for which a key is to be constructed
     * @param parentKey the key of the parent node
     * @return the key for the given node
     */
    public String nodeKey(ConfigurationNode node, String parentKey)
    {
        if (parentKey == null)
        {
            // name of the root node
            return StringUtils.EMPTY;
        }
        else if (node.getName() == null)
        {
            // paranoia check for undefined node names
            return parentKey;
        }

        else
        {
            StringBuffer buf = new StringBuffer(parentKey.length()
                    + node.getName().length() + PATH_DELIMITER.length());
            if (parentKey.length() > 0)
            {
                buf.append(parentKey);
                buf.append(PATH_DELIMITER);
            }
            if (node.isAttribute())
            {
                buf.append(ATTR_DELIMITER);
            }
            buf.append(node.getName());
            return buf.toString();
        }
    }

    /**
     * Prepares an add operation for a configuration property. The expected
     * format of the passed in key is explained in the class comment.
     *
     * @param root the configuration's root node
     * @param key the key describing the target of the add operation and the
     * path of the new node
     * @return a data object to be evaluated by the calling configuration object
     */
    public NodeAddData prepareAdd(ConfigurationNode root, String key)
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
            addKey = generateKeyForAdd(root, addKey);
            index = findKeySeparator(addKey);
        }

        List nodes = query(root, addKey.substring(0, index).trim());
        if (nodes.size() != 1)
        {
            throw new IllegalArgumentException(
                    "prepareAdd: key must select exactly one target node!");
        }

        NodeAddData data = new NodeAddData();
        data.setParent((ConfigurationNode) nodes.get(0));
        initNodeAddData(data, addKey.substring(index).trim());
        return data;
    }

    /**
     * Creates the <code>JXPathContext</code> used for executing a query. This
     * method will create a new context and ensure that it is correctly
     * initialized.
     *
     * @param root the configuration root node
     * @param key the key to be queried
     * @return the new context
     */
    protected JXPathContext createContext(ConfigurationNode root, String key)
    {
        JXPathContext context = JXPathContext.newContext(root);
        context.setLenient(true);
        return context;
    }

    /**
     * Initializes most properties of a <code>NodeAddData</code> object. This
     * method is called by <code>prepareAdd()</code> after the parent node has
     * been found. Its task is to interpret the passed in path of the new node.
     *
     * @param data the data object to initialize
     * @param path the path of the new node
     */
    protected void initNodeAddData(NodeAddData data, String path)
    {
        String lastComponent = null;
        boolean attr = false;
        boolean first = true;

        StringTokenizer tok = new StringTokenizer(path, NODE_PATH_DELIMITERS,
                true);
        while (tok.hasMoreTokens())
        {
            String token = tok.nextToken();
            if (PATH_DELIMITER.equals(token))
            {
                if (attr)
                {
                    invalidPath(path, " contains an attribute"
                            + " delimiter at an unallowed position.");
                }
                if (lastComponent == null)
                {
                    invalidPath(path,
                            " contains a '/' at an unallowed position.");
                }
                data.addPathNode(lastComponent);
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
                            " contains an attribute delimiter at an unallowed position.");
                }
                if (lastComponent != null)
                {
                    data.addPathNode(lastComponent);
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
        data.setNewNodeName(lastComponent);
        data.setAttribute(attr);
    }

    /**
     * Tries to generate a key for adding a property. This method is called if a
     * key was used for adding properties which does not contain a space
     * character. It splits the key at its single components and searches for
     * the last existing component. Then a key compatible for adding properties
     * is generated.
     *
     * @param root the root node of the configuration
     * @param key the key in question
     * @return the key to be used for adding the property
     */
    private String generateKeyForAdd(ConfigurationNode root, String key)
    {
        int pos = key.lastIndexOf(PATH_DELIMITER, key.length());

        while (pos >= 0)
        {
            String keyExisting = key.substring(0, pos);
            if (!query(root, keyExisting).isEmpty())
            {
                StringBuffer buf = new StringBuffer(key.length() + 1);
                buf.append(keyExisting).append(SPACE);
                buf.append(key.substring(pos + 1));
                return buf.toString();
            }
            pos = key.lastIndexOf(PATH_DELIMITER, pos - 1);
        }

        return SPACE + key;
    }

    /**
     * Helper method for throwing an exception about an invalid path.
     *
     * @param path the invalid path
     * @param msg the exception message
     */
    private void invalidPath(String path, String msg)
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
    private static int findKeySeparator(String key)
    {
        int index = key.length() - 1;
        while (index >= 0 && !Character.isWhitespace(key.charAt(index)))
        {
            index--;
        }
        return index;
    }

    // static initializer: registers the configuration node pointer factory
    static
    {
        JXPathContextReferenceImpl
                .addNodePointerFactory(new ConfigurationNodePointerFactory());
    }
}
