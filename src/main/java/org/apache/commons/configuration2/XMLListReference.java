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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.ReferenceNodeHandler;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

/**
 * <p>
 * An internal class implementing list handling functionality for
 * {@link XMLConfiguration}.
 * </p>
 * <p>
 * When an XML document is loaded list properties defined as a string with
 * multiple values separated by the list delimiter are split into multiple
 * configuration nodes. When the configuration is saved the original format
 * should be kept if possible. This class implements functionality to achieve
 * this. Instances are used as references associated with configuration nodes so
 * that the original format can be restored when the configuration is saved.
 * </p>
 */
final class XMLListReference
{
    /** The wrapped XML element. */
    private final Element element;

    /**
     * Private constructor. No instances can be created from other classes.
     *
     * @param e the associated element
     */
    private XMLListReference(final Element e)
    {
        element = e;
    }

    /**
     * Returns the associated element.
     *
     * @return the associated XML element
     */
    public Element getElement()
    {
        return element;
    }

    /**
     * Assigns an instance of this class as reference to the specified
     * configuration node. This reference acts as a marker indicating that this
     * node is subject to extended list handling.
     *
     * @param refs the mapping for node references
     * @param node the affected configuration node
     * @param elem the current XML element
     */
    public static void assignListReference(final Map<ImmutableNode, Object> refs,
            final ImmutableNode node, final Element elem)
    {
        if (refs != null)
        {
            refs.put(node, new XMLListReference(elem));
        }
    }

    /**
     * Checks whether the specified configuration node has to be taken into
     * account for list handling. This is the case if the node's parent has at
     * least one child node with the same name which has a special list
     * reference assigned. (Note that the passed in node does not necessarily
     * have such a reference; if it has been added at a later point in time, it
     * also has to become an item of the list.)
     *
     * @param node the configuration node
     * @param handler the reference node handler
     * @return a flag whether this node is relevant for list handling
     */
    public static boolean isListNode(final ImmutableNode node,
            final ReferenceNodeHandler handler)
    {
        if (hasListReference(node, handler))
        {
            return true;
        }

        final ImmutableNode parent = handler.getParent(node);
        if (parent != null)
        {
            for (int i = 0; i < handler.getChildrenCount(parent, null); i++)
            {
                final ImmutableNode child = handler.getChild(parent, i);
                if (hasListReference(child, handler) && nameEquals(node, child))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the specified node is the first node of a list. This is
     * needed because all items of the list are collected and stored as value of
     * the first list node. Note: This method requires that the passed in node
     * is a list node, so
     * {@link #isListNode(ImmutableNode, ReferenceNodeHandler)} must have
     * returned <strong>true</strong> for it.
     *
     * @param node the configuration node
     * @param handler the reference node handler
     * @return a flag whether this is the first node of a list
     */
    public static boolean isFirstListItem(final ImmutableNode node,
            final ReferenceNodeHandler handler)
    {
        final ImmutableNode parent = handler.getParent(node);
        ImmutableNode firstItem = null;
        int idx = 0;
        while (firstItem == null)
        {
            final ImmutableNode child = handler.getChild(parent, idx);
            if (nameEquals(node, child))
            {
                firstItem = child;
            }
            idx++;
        }
        return firstItem == node;
    }

    /**
     * Constructs the concatenated string value of all items comprising the list
     * the specified node belongs to. This method is called when saving an
     * {@link XMLConfiguration}. Then configuration nodes created for list items
     * have to be collected again and transformed into a string defining all
     * list elements.
     *
     * @param node the configuration node
     * @param nodeHandler the reference node handler
     * @param delimiterHandler the list delimiter handler of the configuration
     * @return a string with all values of the current list
     * @throws ConfigurationRuntimeException if the list delimiter handler does
     *         not support the transformation of list items to a string
     */
    public static String listValue(final ImmutableNode node,
            final ReferenceNodeHandler nodeHandler,
            final ListDelimiterHandler delimiterHandler)
    {
        // cannot be null if the current node is a list node
        final ImmutableNode parent = nodeHandler.getParent(node);
        final List<ImmutableNode> items =
                nodeHandler.getChildren(parent, node.getNodeName());
        final List<Object> values = new ArrayList<>(items.size());
        for (final ImmutableNode n : items)
        {
            values.add(n.getValue());
        }
        try
        {
            return String.valueOf(delimiterHandler.escapeList(values,
                    ListDelimiterHandler.NOOP_TRANSFORMER));
        }
        catch (final UnsupportedOperationException e)
        {
            throw new ConfigurationRuntimeException(
                    "List handling not supported by "
                            + "the current ListDelimiterHandler! Make sure that the same delimiter "
                            + "handler is used for loading and saving the configuration.",
                    e);
        }
    }

    /**
     * Checks whether the specified node has an associated list reference. This
     * marks the node as part of a list.
     *
     * @param node the node to be checked
     * @param handler the reference handler
     * @return a flag whether this node has a list reference
     */
    private static boolean hasListReference(final ImmutableNode node,
            final ReferenceNodeHandler handler)
    {
        return handler.getReference(node) instanceof XMLListReference;
    }

    /**
     * Helper method for comparing the names of two nodes.
     *
     * @param n1 node 1
     * @param n2 node 2
     * @return a flag whether these nodes have equal names
     */
    private static boolean nameEquals(final ImmutableNode n1, final ImmutableNode n2)
    {
        return StringUtils.equals(n2.getNodeName(), n1.getNodeName());
    }
}
