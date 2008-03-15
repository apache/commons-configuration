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

package org.apache.commons.configuration2.expr.xpath;

import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.configuration2.expr.ExpressionEngine;
import org.apache.commons.configuration2.expr.NodeAddData;
import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.expr.NodeList;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * A specialized implementation of the <code>ExpressionEngine</code> interface
 * that is able to evaluate XPATH expressions.
 * </p>
 * <p>
 * This class makes use of <a href="http://commons.apache.org/jxpath/">
 * Commons JXPath</a> for handling XPath expressions and mapping them to the
 * nodes of a hierarchical configuration. This makes the rich and powerful
 * XPATH syntax available for accessing properties from a configuration object.
 * </p>
 * <p>
 * For selecting properties arbitrary XPATH expressions can be used, which
 * select single or multiple configuration nodes. The associated
 * <code>Configuration</code> instance will directly pass the specified
 * property keys into this engine. If a key is not syntactically correct, an
 * exception will be thrown.
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
 * &quot;/&quot; character) can be specified.</li>
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
 *
 * @since 2.0
 * @author Oliver Heger
 * @version $Id$
 */
public class XPathExpressionEngine implements ExpressionEngine
{
    /** Constant for the path delimiter. */
    static final String PATH_DELIMITER = "/";

    /** Constant for the attribute delimiter. */
    static final String ATTR_DELIMITER = "@";

    /** Constant for the delimiters for splitting node paths. */
    private static final String NODE_PATH_DELIMITERS = PATH_DELIMITER + ATTR_DELIMITER;

    /**
     * Executes a query. The passed in property key is directly passed to a
     * JXPath context.
     *
     * @param root the configuration root node
     * @param key the query to be executed
     * @param handler the node handler
     * @return a list with the nodes that are selected by the query
     */
    @SuppressWarnings("unchecked")
    public <T> NodeList<T> query(T root, String key, NodeHandler<T> handler)
    {
        NodeList<T> result = new NodeList<T>();

        if (StringUtils.isEmpty(key))
        {
            result.addNode(root);
        }

        else
        {
            JXPathContext context = createContext(root, key, handler);
            List<?> nodes = context.selectNodes(key);
            if (nodes != null)
            {
                for (Object o : nodes)
                {
                    if (o instanceof ConfigurationAttributePointer.AttributeNodeProxy)
                    {
                        ConfigurationAttributePointer<T>.AttributeNodeProxy anp =
                            (ConfigurationAttributePointer.AttributeNodeProxy) o;
                        result.addAttribute(anp.getParentNode(), anp
                                .getAttributeName());
                    }
                    else
                    {
                        result.addNode((T) o);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns a (canonical) key for the given node based on the parent's key.
     * This implementation will create an XPATH expression that selects the
     * given node (under the assumption that the passed in parent key is valid).
     * As the <code>nodeKey()</code> implementation of
     * <code>DefaultExpressionEngine</code>
     * this method will not return indices for nodes. So all child nodes of a
     * given parent with the same name will have the same key.
     *
     * @param node the node for which a key is to be constructed
     * @param parentKey the key of the parent node
     * @param handler the node handler
     * @return the key for the given node
     */
    public <T> String nodeKey(T node, String parentKey, NodeHandler<T> handler)
    {
        if (parentKey == null)
        {
            // name of the root node
            return StringUtils.EMPTY;
        }

        String nodeName = handler.nodeName(node);
        if (nodeName == null)
        {
            // paranoia check for undefined node names
            return parentKey;
        }

        else
        {
            StringBuilder buf = new StringBuilder(parentKey.length() + nodeName.length() + PATH_DELIMITER.length());
            if (parentKey.length() > 0)
            {
                buf.append(parentKey);
                buf.append(PATH_DELIMITER);
            }
            buf.append(nodeName);
            return buf.toString();
        }
    }

    /**
     * Returns a key for the specified attribute. This method works similar to
     * <code>nodeKey()</code>, but deals with attributes.
     *
     * @param parentNode the parent node
     * @param parentKey the key of the parent
     * @param attrName the name of the attribute
     * @param handler the node handler
     * @return the key for this attribute
     */
    public <T> String attributeKey(T parentNode, String parentKey,
            String attrName, NodeHandler<T> handler)
    {
        StringBuilder buf = new StringBuilder();
        if (parentKey != null && parentKey.length() > 0)
        {
            buf.append(parentKey).append(PATH_DELIMITER);
        }
        buf.append(ATTR_DELIMITER).append(attrName);
        return buf.toString();
    }

    /**
     * Prepares an add operation for a configuration property. The expected
     * format of the passed in key is explained in the class comment.
     *
     * @param root the configuration's root node
     * @param key the key describing the target of the add operation and the
     * path of the new node
     * @param handler the node handler
     * @return a data object to be evaluated by the calling configuration object
     */
    public <T> NodeAddData<T> prepareAdd(T root, String key, NodeHandler<T> handler)
    {
        if (key == null)
        {
            throw new IllegalArgumentException(
                    "prepareAdd: key must not be null!");
        }

        int index = key.length() - 1;
        while (index >= 0 && !Character.isWhitespace(key.charAt(index)))
        {
            index--;
        }
        if (index < 0)
        {
            throw new IllegalArgumentException("prepareAdd: Passed in key must contain a whitespace!");
        }

        NodeList<T> nodes = query(root, key.substring(0, index).trim(), handler);
        if (nodes.size() != 1)
        {
            throw new IllegalArgumentException("prepareAdd: key must select exactly one target node!");
        }

        NodeAddData<T> data = new NodeAddData<T>();
        data.setParent(nodes.getNode(0));
        initNodeAddData(data, key.substring(index).trim());
        return data;
    }

    /**
     * Creates the <code>JXPathContext</code> used for executing a query. This
     * method will create a new context and ensure that it is correctly
     * initialized.
     *
     * @param root the configuration root node
     * @param key the key to be queried
     * @param handler the node handler
     * @return the new context
     */
    protected <T> JXPathContext createContext(T root, String key, NodeHandler<T> handler)
    {
        JXPathContext context = JXPathContext
                .newContext(ConfigurationNodePointerFactory.wrapNode(root,
                        handler));
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
    protected <T> void initNodeAddData(NodeAddData<T> data, String path)
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

    // static initializer: registers the configuration node pointer factory
    static
    {
        JXPathContextReferenceImpl
                .addNodePointerFactory(new ConfigurationNodePointerFactory());
    }
}
