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

import java.util.List;
import java.util.Locale;

import org.apache.commons.configuration2.tree.ConfigurationNode;
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
 * @since 1.3
 * @author Oliver Heger
 * @version $Id$
 */
class ConfigurationNodePointer extends NodePointer
{
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = -1087475639680007713L;

    /** Stores the associated configuration node. */
    private ConfigurationNode node;

    /**
     * Creates a new instance of <code>ConfigurationNodePointer</code>.
     *
     * @param node the node
     * @param locale the locale
     */
    public ConfigurationNodePointer(ConfigurationNode node, Locale locale)
    {
        super(null, locale);
        this.node = node;
    }

    /**
     * Creates a new instance of <code>ConfigurationNodePointer</code> and
     * initializes it with its parent pointer.
     *
     * @param parent the parent pointer
     * @param node the associated node
     */
    public ConfigurationNodePointer(NodePointer parent, ConfigurationNode node)
    {
        super(parent);
        this.node = node;
    }

    /**
     * Returns a flag whether this node is a leaf. This is the case if there are
     * no child nodes.
     *
     * @return a flag if this node is a leaf
     */
    public boolean isLeaf()
    {
        return node.getChildrenCount() < 1;
    }

    /**
     * Returns a flag if this node is a collection. This is not the case.
     *
     * @return the collection flag
     */
    public boolean isCollection()
    {
        return false;
    }

    /**
     * Returns this node's length. This is always 1.
     *
     * @return the node's length
     */
    public int getLength()
    {
        return 1;
    }

    /**
     * Checks whether this node pointer refers to an attribute node. This method
     * checks the attribute flag of the associated configuration node.
     *
     * @return the attribute flag
     */
    public boolean isAttribute()
    {
        return node.isAttribute();
    }

    /**
     * Returns this node's name.
     *
     * @return the name
     */
    public QName getName()
    {
        return new QName(null, node.getName());
    }

    /**
     * Returns this node's base value. This is the associated configuration
     * node.
     *
     * @return the base value
     */
    public Object getBaseValue()
    {
        return node;
    }

    /**
     * Returns the immediate node. This is the associated configuration node.
     *
     * @return the immediate node
     */
    public Object getImmediateNode()
    {
        return node;
    }

    /**
     * Returns the value of this node.
     *
     * @return the represented node's value
     */
    public Object getValue()
    {
        return node.getValue();
    }

    /**
     * Sets the value of this node.
     *
     * @param value the new value
     */
    public void setValue(Object value)
    {
        node.setValue(value);
    }

    /**
     * Compares two child node pointers.
     *
     * @param pointer1 one pointer
     * @param pointer2 another pointer
     * @return a flag, which pointer should be sorted first
     */
    public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2)
    {
        ConfigurationNode node1 = (ConfigurationNode) pointer1.getBaseValue();
        ConfigurationNode node2 = (ConfigurationNode) pointer2.getBaseValue();

        // attributes will be sorted before child nodes
        if (node1.isAttribute() && !node2.isAttribute())
        {
            return -1;
        }
        else if (node2.isAttribute() && !node1.isAttribute())
        {
            return 1;
        }

        else
        {
            // sort based on the occurrence in the sub node list
            List<ConfigurationNode> subNodes = node1.isAttribute() ? node.getAttributes() : node.getChildren();
            for (ConfigurationNode child : subNodes)
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
    }

    /**
     * Returns an iterator for the attributes that match the given name.
     *
     * @param name the attribute name
     * @return the iterator for the attributes
     */
    public NodeIterator attributeIterator(QName name)
    {
        return new ConfigurationNodeIteratorAttribute(this, name);
    }

    /**
     * Returns an iterator for the children of this pointer that match the given
     * test object.
     *
     * @param test the test object
     * @param reverse the reverse flag
     * @param startWith the start value of the iteration
     */
    public NodeIterator childIterator(NodeTest test, boolean reverse, NodePointer startWith)
    {
        return new ConfigurationNodeIteratorChildren(this, test, reverse, startWith);
    }

    /**
     * Tests if this node matches the given test. Configuration nodes are text
     * nodes, too because they can contain a value.
     *
     * @param test the test object
     * @return a flag if this node corresponds to the test
     */
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
