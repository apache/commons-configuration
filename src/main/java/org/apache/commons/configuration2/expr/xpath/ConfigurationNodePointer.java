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

import java.util.Locale;

import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * <p>
 * A specific <code>NodePointer</code> implementation for configuration nodes.
 * </p>
 * <p>
 * This is needed for queries using JXPath.
 * </p>
 *
 * @since 2.0
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 * @param <T> the type of the nodes this pointer deals with
 */
class ConfigurationNodePointer<T> extends NodePointer
{
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = -1087475639680007713L;

    /** Stores the associated configuration node. */
    private T node;

    /** The node handler for the represented node.*/
    private NodeHandler<T> nodeHandler;

    /**
     * Creates a new instance of <code>ConfigurationNodePointer</code>.
     *
     * @param node the node
     * @param handler the node handler
     * @param locale the locale
     */
    public ConfigurationNodePointer(T node, NodeHandler<T> handler, Locale locale)
    {
        super(null, locale);
        this.node = node;
        nodeHandler = handler;
    }

    /**
     * Creates a new instance of <code>ConfigurationNodePointer</code> and
     * initializes it with its parent pointer.
     *
     * @param parent the parent pointer
     * @param node the associated node
     */
    public ConfigurationNodePointer(NodePointer parent, T node, NodeHandler<T> handler)
    {
        super(parent);
        this.node = node;
        nodeHandler = handler;
    }

    /**
     * Returns the <code>NodeHandler</code> used by this poiinter.
     * @return the <code>NodeHandler</code>
     */
    public NodeHandler<T> getNodeHandler()
    {
        return nodeHandler;
    }

    /**
     * Returns the wrapped configuration node.
     * @return the wrapped configuration node
     */
    public T getConfigurationNode()
    {
        return node;
    }

    /**
     * Returns a flag whether this node is a leaf. This is the case if there are
     * no child nodes or attributes.
     *
     * @return a flag if this node is a leaf
     */
    @Override
    public boolean isLeaf()
    {
        return getNodeHandler().getChildren(getConfigurationNode()).isEmpty()
                && getNodeHandler().getAttributes(getConfigurationNode())
                        .isEmpty();
    }

    /**
     * Returns a flag if this node is a collection. This is not the case.
     *
     * @return the collection flag
     */
    @Override
    public boolean isCollection()
    {
        return false;
    }

    /**
     * Returns this node's length. This is always 1.
     *
     * @return the node's length
     */
    @Override
    public int getLength()
    {
        return 1;
    }

    /**
     * Checks whether this node pointer refers to an attribute node. This is not
     * the case for the nodes maintained by this pointer.
     *
     * @return the attribute flag
     */
    @Override
    public boolean isAttribute()
    {
        return false;
    }

    /**
     * Returns this node's name.
     *
     * @return the name
     */
    @Override
    public QName getName()
    {
        return new QName(null, getNodeHandler().nodeName(getConfigurationNode()));
    }

    /**
     * Returns this node's base value. This is the associated configuration
     * node.
     *
     * @return the base value
     */
    @Override
    public Object getBaseValue()
    {
        return getConfigurationNode();
    }

    /**
     * Returns the immediate node. This is the associated configuration node.
     *
     * @return the immediate node
     */
    @Override
    public Object getImmediateNode()
    {
        return getConfigurationNode();
    }

    /**
     * Returns the value of this node.
     *
     * @return the represented node's value
     */
    @Override
    public Object getValue()
    {
        return getNodeHandler().getValue(getConfigurationNode());
    }

    /**
     * Sets the value of this node.
     *
     * @param value the new value
     */
    @Override
    public void setValue(Object value)
    {
        getNodeHandler().setValue(getConfigurationNode(), value);
    }

    /**
     * Compares two child node pointers.
     *
     * @param pointer1 one pointer
     * @param pointer2 another pointer
     * @return a flag, which pointer should be sorted first
     */
    @SuppressWarnings("unchecked")
    @Override
    public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2)
    {
        T node1 = ((ConfigurationNodePointer<T>) pointer1).getConfigurationNode();
        T node2 = ((ConfigurationNodePointer<T>) pointer2).getConfigurationNode();

        // sort based on the occurrence in the sub node list
        for (T child : getNodeHandler().getChildren(getConfigurationNode()))
        {
            if (child == node1)
            {
                return -1;
            }
            else if (child == node2)
            {
                return 1;
            }
        }
        return 0; // should not happen
    }

    /**
     * Returns an iterator for the attributes that match the given name.
     *
     * @param name the attribute name
     * @return the iterator for the attributes
     */
    @Override
    public NodeIterator attributeIterator(QName name)
    {
        return new ConfigurationNodeIteratorAttribute<T>(this, name);
    }

    /**
     * Returns an iterator for the children of this pointer that match the given
     * test object.
     *
     * @param test the test object
     * @param reverse the reverse flag
     * @param startWith the start value of the iteration
     */
    @SuppressWarnings("unchecked")
    @Override
    public NodeIterator childIterator(NodeTest test, boolean reverse,
            NodePointer startWith)
    {
        return new ConfigurationNodeIteratorChildren<T>(this, test, reverse,
                (ConfigurationNodePointer<T>) startWith);
    }

    /**
     * Tests if this node matches the given test. Configuration nodes are text
     * nodes, too because they can contain a value.
     *
     * @param test the test object
     * @return a flag if this node corresponds to the test
     */
    @Override
    public boolean testNode(NodeTest test)
    {
        if (test instanceof NodeTypeTest
                && ((NodeTypeTest) test).getNodeType() == Compiler.NODE_TYPE_TEXT)
        {
            return true;
        }
        return super.testNode(test);
    }
}
