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

package org.apache.commons.configuration2.expr.def;

import java.util.List;

import org.apache.commons.configuration2.expr.ExpressionEngine;
import org.apache.commons.configuration2.expr.NodeAddData;
import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.expr.NodeList;
import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * A default implementation of the <code>ExpressionEngine</code> interface
 * providing the &quot;native&quote; expression language for hierarchical
 * configurations.
 * </p>
 * <p>
 * This class implements a rather simple expression language for navigating
 * through a hierarchy of configuration nodes. It supports the following
 * operations:
 * </p>
 * <p>
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
 * </p>
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
 * </p>
 * <p>
 * If this document is parsed and stored in a hierarchical configuration object,
 * for instance the key <code>tables.table(0).name</code> can be used to find
 * out the name of the first table. In opposite <code>tables.table.name</code>
 * would return a collection with the names of all available tables. Similarily
 * the key <code>tables.table(1).fields.field.name</code> returns a collection
 * with the names of all fields of the second table. If another index is added
 * after the <code>field</code> element, a single field can be accessed:
 * <code>tables.table(1).fields.field(0).name</code>. The key
 * <code>tables.table(0)[@type]</code> would select the type attribute of the
 * first table.
 * </p>
 * <p>
 * This example works with the default values for delimiters and index markers.
 * It is also possible to set custom values for these properties so that you can
 * adapt a <code>DefaultExpressionEngine</code> to your personal needs.
 * </p>
 *
 * @since 1.3
 * @author Oliver Heger
 * @version $Id$
 */
public class DefaultExpressionEngine implements ExpressionEngine
{
    /** Constant for the default property delimiter. */
    public static final String DEFAULT_PROPERTY_DELIMITER = ".";

    /** Constant for the default escaped property delimiter. */
    public static final String DEFAULT_ESCAPED_DELIMITER = DEFAULT_PROPERTY_DELIMITER
            + DEFAULT_PROPERTY_DELIMITER;

    /** Constant for the default attribute start marker. */
    public static final String DEFAULT_ATTRIBUTE_START = "[@";

    /** Constant for the default attribute end marker. */
    public static final String DEFAULT_ATTRIBUTE_END = "]";

    /** Constant for the default index start marker. */
    public static final String DEFAULT_INDEX_START = "(";

    /** Constant for the default index end marker. */
    public static final String DEFAULT_INDEX_END = ")";

    /** Stores the property delimiter. */
    private String propertyDelimiter = DEFAULT_PROPERTY_DELIMITER;

    /** Stores the escaped property delimiter. */
    private String escapedDelimiter = DEFAULT_ESCAPED_DELIMITER;

    /** Stores the attribute start marker. */
    private String attributeStart = DEFAULT_ATTRIBUTE_START;

    /** Stores the attribute end marker. */
    private String attributeEnd = DEFAULT_ATTRIBUTE_END;

    /** Stores the index start marker. */
    private String indexStart = DEFAULT_INDEX_START;

    /** stores the index end marker. */
    private String indexEnd = DEFAULT_INDEX_END;

    /**
     * Sets the attribute end marker.
     *
     * @return the attribute end marker
     */
    public String getAttributeEnd()
    {
        return attributeEnd;
    }

    /**
     * Sets the attribute end marker.
     *
     * @param attributeEnd the attribute end marker; can be <b>null</b> if no
     *        end marker is needed
     */
    public void setAttributeEnd(String attributeEnd)
    {
        this.attributeEnd = attributeEnd;
    }

    /**
     * Returns the attribute start marker.
     *
     * @return the attribute start marker
     */
    public String getAttributeStart()
    {
        return attributeStart;
    }

    /**
     * Sets the attribute start marker. Attribute start and end marker are used
     * together to detect attributes in a property key.
     *
     * @param attributeStart the attribute start marker
     */
    public void setAttributeStart(String attributeStart)
    {
        this.attributeStart = attributeStart;
    }

    /**
     * Returns the escaped property delimiter string.
     *
     * @return the escaped property delimiter
     */
    public String getEscapedDelimiter()
    {
        return escapedDelimiter;
    }

    /**
     * Sets the escaped property delimiter string. With this string a delimiter
     * that belongs to the key of a property can be escaped. If for instance
     * &quot;.&quot; is used as property delimiter, you can set the escaped
     * delimiter to &quot;\.&quot; and can then escape the delimiter with a back
     * slash.
     *
     * @param escapedDelimiter the escaped delimiter string
     */
    public void setEscapedDelimiter(String escapedDelimiter)
    {
        this.escapedDelimiter = escapedDelimiter;
    }

    /**
     * Returns the index end marker.
     *
     * @return the index end marker
     */
    public String getIndexEnd()
    {
        return indexEnd;
    }

    /**
     * Sets the index end marker.
     *
     * @param indexEnd the index end marker
     */
    public void setIndexEnd(String indexEnd)
    {
        this.indexEnd = indexEnd;
    }

    /**
     * Returns the index start marker.
     *
     * @return the index start marker
     */
    public String getIndexStart()
    {
        return indexStart;
    }

    /**
     * Sets the index start marker. Index start and end marker are used together
     * to detect indices in a property key.
     *
     * @param indexStart the index start marker
     */
    public void setIndexStart(String indexStart)
    {
        this.indexStart = indexStart;
    }

    /**
     * Returns the property delimiter.
     *
     * @return the property delimiter
     */
    public String getPropertyDelimiter()
    {
        return propertyDelimiter;
    }

    /**
     * Sets the property delmiter. This string is used to split the parts of a
     * property key.
     *
     * @param propertyDelimiter the property delimiter
     */
    public void setPropertyDelimiter(String propertyDelimiter)
    {
        this.propertyDelimiter = propertyDelimiter;
    }

    /**
     * Evaluates the given key and returns all matching nodes. This method
     * supports the syntax as described in the class comment.
     *
     * @param root the root node
     * @param key the key
     * @param handler the node handler
     * @return a list with the matching nodes
     */
    public <T> NodeList<T> query(T root, String key, NodeHandler<T> handler)
    {
        NodeList<T> nodes = new NodeList<T>();
        findNodesForKey(new DefaultConfigurationKey(this, key).iterator(),
                root, nodes, handler);
        return nodes;
    }

    /**
     * Determines the key of the passed in node. This implementation takes the
     * given parent key, adds a property delimiter, and then adds the node's
     * name. The name of the root node is an empty string. Note that no indices
     * will be returned.
     *
     * @param node the node whose key is to be determined
     * @param parentKey the key of this node's parent
     * @param handler the node handler
     * @return the key for the given node
     */
    public <T> String nodeKey(T node, String parentKey, NodeHandler<T> handler)
    {
        if (parentKey == null)
        {
            // this is the root node
            return StringUtils.EMPTY;
        }

        else
        {
            DefaultConfigurationKey key = new DefaultConfigurationKey(this,
                    parentKey);
            key.append(handler.nodeName(node), true);
            return key.toString();
        }
    }

    /**
     * Determines a unique key of the passed in node. This implementation first
     * fetches the default key using <code>nodeKey()</code>. Then it obtains the
     * index of the node relative to its parent. If an index is defined, it is
     * added.
     *
     * @param node the node whose key is to be determined
     * @param parentKey the key of this node's parent
     * @param handler the node handler
     * @return the key for the given node
     * @since 2.0
     */
    public <T> String uniqueNodeKey(T node, String parentKey,
            NodeHandler<T> handler)
    {
        String key = nodeKey(node, parentKey, handler);

        int index = handler.indexOfChild(node);
        if (index >= 0)
        {
            DefaultConfigurationKey ckey = new DefaultConfigurationKey(this,
                    key);
            ckey.appendIndex(index);
            key = ckey.toString();
        }

        return key;
    }

    /**
     * Determines the key of the specified attribute. This implementation
     * appends the name of the attribute to the parent key using the attribute
     * marker as separator.
     *
     * @param parentNode the parent node
     * @param parentKey the key of the parent node
     * @param attrName the name of the attribute
     * @param handler the node handler
     */
    public <T> String attributeKey(T parentNode, String parentKey,
            String attrName, NodeHandler<T> handler)
    {
        DefaultConfigurationKey key = new DefaultConfigurationKey(this);
        if (parentKey != null)
        {
            key.append(parentKey, false);
        }
        key.appendAttribute(attrName);
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
     * clarify this: Suppose the actual node structure looks like the following:
     * </p>
     * <p>
     *
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
     *
     * </p>
     * <p>
     * In this example a database structure is defined, e.g. all fields of the
     * first table could be accessed using the key
     * <code>tables.table(0).fields.field.name</code>. If now properties are
     * to be added, it must be exactly specified at which position in the
     * hierarchy the new property is to be inserted. So to add a new field name
     * to a table it is not enough to say just
     * </p>
     * <p>
     *
     * <pre>
     * config.addProperty(&quot;tables.table.fields.field.name&quot;, &quot;newField&quot;);
     * </pre>
     *
     * </p>
     * <p>
     * The statement given above contains some ambiguity. For instance it is not
     * clear, to which table the new field should be added. If this method finds
     * such an ambiguity, it is resolved by following the last valid path. Here
     * this would be the last table. The same is true for the <code>field</code>;
     * because there are multiple fields and no explicit index is provided, a
     * new <code>name</code> property would be added to the last field - which
     * is probably not what was desired.
     * </p>
     * <p>
     * To make things clear explicit indices should be provided whenever
     * possible. In the example above the exact table could be specified by
     * providing an index for the <code>table</code> element as in
     * <code>tables.table(1).fields</code>. By specifying an index it can
     * also be expressed that at a given position in the configuration tree a
     * new branch should be added. In the example above we did not want to add
     * an additional <code>name</code> element to the last field of the table,
     * but we want a complete new <code>field</code> element. This can be
     * achieved by specifying an invalid index (like -1) after the element where
     * a new branch should be created. Given this our example would run:
     * </p>
     * <p>
     *
     * <pre>
     * config.addProperty(&quot;tables.table(1).fields.field(-1).name&quot;, &quot;newField&quot;);
     * </pre>
     *
     * </p>
     * <p>
     * With this notation it is possible to add new branches everywhere. We
     * could for instance create a new <code>table</code> element by
     * specifying
     * </p>
     * <p>
     *
     * <pre>
     * config.addProperty(&quot;tables.table(-1).fields.field.name&quot;, &quot;newField2&quot;);
     * </pre>
     *
     * </p>
     * <p>
     * (Note that because after the <code>table</code> element a new branch is
     * created indices in following elements are not relevant; the branch is new
     * so there cannot be any ambiguities.)
     * </p>
     *
     * @param root the root node of the nodes hierarchy
     * @param key the key of the new property
     * @return a data object with information needed for the add operation
     */
    public <T> NodeAddData<T> prepareAdd(T root, String key,
            NodeHandler<T> handler)
    {
        DefaultConfigurationKey.KeyIterator it = new DefaultConfigurationKey(
                this, key).iterator();
        if (!it.hasNext())
        {
            throw new IllegalArgumentException(
                    "Key for add operation must be defined!");
        }

        NodeAddData<T> result = new NodeAddData<T>();
        result.setParent(findLastPathNode(it, root, handler));

        while (it.hasNext())
        {
            if (!it.isPropertyKey())
            {
                throw new IllegalArgumentException(
                        "Invalid key for add operation: " + key
                                + " (Attribute key in the middle.)");
            }
            result.addPathNode(it.currentKey());
            it.next();
        }

        result.setNewNodeName(it.currentKey());
        result.setAttribute(!it.isPropertyKey());
        return result;
    }

    /**
     * Recursive helper method for evaluating a key. This method processes all
     * facets of a configuration key, traverses the tree of properties and
     * fetches the nodes of all matching properties.
     *
     * @param keyPart the configuration key iterator
     * @param node the actual node
     * @param nodes here the found nodes are stored
     * @param handler the node handler
     */
    protected <T> void findNodesForKey(
            DefaultConfigurationKey.KeyIterator keyPart, T node,
            NodeList<T> nodes, NodeHandler<T> handler)
    {
        if (!keyPart.hasNext())
        {
            nodes.addNode(node);
        }

        else
        {
            String key = keyPart.nextKey(false);
            if (keyPart.isPropertyKey())
            {
                processSubNodes(keyPart, handler.getChildren(node, key), nodes,
                        handler);
            }

            if (keyPart.isAttribute())
            {
                if (handler.getAttributeValue(node, key) != null)
                {
                    if (keyPart.hasIndex())
                    {
                        nodes.addAttribute(node, key, keyPart.getIndex());
                    }
                    else
                    {
                        nodes.addAttribute(node, key);
                    }
                }
            }
        }
    }

    /**
     * Finds the last existing node for an add operation. This method traverses
     * the configuration node tree along the specified key. The last existing
     * node on this path is returned.
     *
     * @param keyIt the key iterator
     * @param node the actual node
     * @param handler the node handler
     * @return the last existing node on the given path
     */
    protected <T> T findLastPathNode(DefaultConfigurationKey.KeyIterator keyIt,
            T node, NodeHandler<T> handler)
    {
        String keyPart = keyIt.nextKey(false);

        if (keyIt.hasNext())
        {
            if (!keyIt.isPropertyKey())
            {
                // Attribute keys can only appear as last elements of the path
                throw new IllegalArgumentException(
                        "Invalid path for add operation: Attribute key in the middle!");
            }

            List<T> children = handler.getChildren(node, keyPart);
            int idx = keyIt.hasIndex() ? keyIt.getIndex() : children.size() - 1;
            if (idx < 0 || idx >= children.size())
            {
                return node;
            }
            else
            {
                return findLastPathNode(keyIt, children.get(idx), handler);
            }
        }

        else
        {
            return node;
        }
    }

    /**
     * Called by <code>findNodesForKey()</code> to process the sub nodes of
     * the current node depending on the type of the current key part (children,
     * attributes, or both).
     *
     * @param keyPart the key part
     * @param subNodes a list with the sub nodes to process
     * @param nodes the target collection
     * @param handler the node handler
     */
    private <T> void processSubNodes(
            DefaultConfigurationKey.KeyIterator keyPart, List<T> subNodes,
            NodeList<T> nodes, NodeHandler<T> handler)
    {
        if (keyPart.hasIndex())
        {
            if (keyPart.getIndex() >= 0 && keyPart.getIndex() < subNodes.size())
            {
                findNodesForKey(keyPart.clone(), subNodes.get(keyPart
                        .getIndex()), nodes, handler);
            }
        }
        else
        {
            for (T subNode : subNodes)
            {
                findNodesForKey(keyPart.clone(), subNode, nodes, handler);
            }
        }
    }
}
