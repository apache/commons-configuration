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

import java.util.Locale;

import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * <p>
 * A specific {@code NodePointer} implementation for configuration nodes.
 * </p>
 * <p>
 * This is needed for queries using JXPath.
 * </p>
 *
 * @since 1.3
 * @param <T> the type of the nodes this pointer deals with
 */
class ConfigurationNodePointer<T> extends NodePointer
{
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = -1087475639680007713L;

    /** The node handler. */
    private final NodeHandler<T> handler;

    /** Stores the associated node. */
    private final T node;

    /**
     * Creates a new instance of {@code ConfigurationNodePointer} pointing to
     * the specified node.
     *
     * @param node the wrapped node
     * @param locale the locale
     * @param handler the {@code NodeHandler}
     */
    public ConfigurationNodePointer(final T node, final Locale locale,
            final NodeHandler<T> handler)
    {
        super(null, locale);
        this.node = node;
        this.handler = handler;
    }

    /**
     * Creates a new instance of {@code ConfigurationNodePointer} and
     * initializes it with its parent pointer.
     *
     * @param parent the parent pointer
     * @param node the associated node
     * @param handler the {@code NodeHandler}
     */
    public ConfigurationNodePointer(final ConfigurationNodePointer<T> parent, final T node,
            final NodeHandler<T> handler)
    {
        super(parent);
        this.node = node;
        this.handler = handler;
    }

    /**
     * Returns a flag whether this node is a leaf. This is the case if there are
     * no child nodes.
     *
     * @return a flag if this node is a leaf
     */
    @Override
    public boolean isLeaf()
    {
        return getNodeHandler().getChildrenCount(node, null) < 1;
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
     * Checks whether this node pointer refers to an attribute node. This is
     * not the case.
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
        return new QName(null, getNodeHandler().nodeName(node));
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
        return node;
    }

    /**
     * Returns the immediate node. This is the associated configuration node.
     *
     * @return the immediate node
     */
    @Override
    public Object getImmediateNode()
    {
        return node;
    }

    /**
     * Returns the value of this node.
     *
     * @return the represented node's value
     */
    @Override
    public Object getValue()
    {
        return getNodeHandler().getValue(node);
    }

    /**
     * Sets the value of this node. This is not supported, so always an
     * exception is thrown.
     *
     * @param value the new value
     */
    @Override
    public void setValue(final Object value)
    {
        throw new UnsupportedOperationException("Node value cannot be set!");
    }

    /**
     * Compares two child node pointers.
     *
     * @param pointer1 one pointer
     * @param pointer2 another pointer
     * @return a flag, which pointer should be sorted first
     */
    @Override
    public int compareChildNodePointers(final NodePointer pointer1,
            final NodePointer pointer2)
    {
        final Object node1 = pointer1.getBaseValue();
        final Object node2 = pointer2.getBaseValue();

        // sort based on the occurrence in the sub node list
        for (final T child : getNodeHandler().getChildren(node))
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
    public NodeIterator attributeIterator(final QName name)
    {
        return new ConfigurationNodeIteratorAttribute<>(this, name);
    }

    /**
     * Returns an iterator for the children of this pointer that match the given
     * test object.
     *
     * @param test the test object
     * @param reverse the reverse flag
     * @param startWith the start value of the iteration
     */
    @Override
    public NodeIterator childIterator(final NodeTest test, final boolean reverse,
            final NodePointer startWith)
    {
        return new ConfigurationNodeIteratorChildren<>(this, test, reverse,
                castPointer(startWith));
    }

    /**
     * Tests if this node matches the given test. Configuration nodes are text
     * nodes, too because they can contain a value.
     *
     * @param test the test object
     * @return a flag if this node corresponds to the test
     */
    @Override
    public boolean testNode(final NodeTest test)
    {
        if (test instanceof NodeTypeTest
                && ((NodeTypeTest) test).getNodeType() == Compiler.NODE_TYPE_TEXT)
        {
            return true;
        }
        return super.testNode(test);
    }

    /**
     * Returns the {@code NodeHandler} used by this instance.
     *
     * @return the {@code NodeHandler}
     */
    public NodeHandler<T> getNodeHandler()
    {
        return handler;
    }

    /**
     * Returns the wrapped configuration node.
     *
     * @return the wrapped node
     */
    public T getConfigurationNode()
    {
        return node;
    }

    /**
     * Casts the given child pointer to a node pointer of this type. This is a
     * bit dangerous. However, in a typical setup, child node pointers can only
     * be created by this instance which ensures that they are of the correct
     * type. Therefore, this cast is safe.
     *
     * @param p the {@code NodePointer} to cast
     * @return the resulting {@code ConfigurationNodePointer}
     */
    private ConfigurationNodePointer<T> castPointer(final NodePointer p)
    {
        @SuppressWarnings("unchecked") // see method comment
        final
        ConfigurationNodePointer<T> result = (ConfigurationNodePointer<T>) p;
        return result;
    }
}
