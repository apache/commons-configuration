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

import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * <p>
 * A specialized <code>NodePointer</code> implementation for the attributes of
 * a configuration.
 * </p>
 * <p>
 * This is needed for queries using JXPath.
 * </p>
 *
 * @author Oliver Heger
 * @version $Id$
 * @param <T> the type of the nodes this pointer deals with
 */
class ConfigurationAttributePointer<T> extends NodePointer
{
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 5504551041716043748L;

    /** Stores the parent node. */
    private T parentNode;

    /** Stores the name of the managed attribute. */
    private String attributeName;

    /**
     * Creates a new instance of <code>ConfigurationAttributePointer</code>.
     *
     * @param parent the parent node pointer
     * @param attrName the name of the managed attribute
     */
    public ConfigurationAttributePointer(ConfigurationNodePointer<T> parent,
            String attrName)
    {
        super(parent);
        parentNode = parent.getConfigurationNode();
        attributeName = attrName;
    }

    /**
     * Returns a reference to the parent node pointer.
     *
     * @return the parent pointer
     */
    @SuppressWarnings("unchecked")
    public ConfigurationNodePointer<T> getParentPointer()
    {
        return (ConfigurationNodePointer<T>) getParent();
    }

    /**
     * Compares two child node pointers. Attributes do not have any children, so
     * this is just a dummy implementation.
     *
     * @param p1 the first pointer
     * @param p2 the second pointer
     * @return the order of these pointers
     */
    @Override
    public int compareChildNodePointers(NodePointer p1, NodePointer p2)
    {
        return 0;
    }

    /**
     * Returns the base value. We return the value.
     *
     * @return the base value
     */
    @Override
    public Object getBaseValue()
    {
        return getValue();
    }

    /**
     * Returns the immediate node. We return the parent node here.
     *
     * @return the immediate node
     */
    @Override
    public Object getImmediateNode()
    {
        return parentNode;
    }

    /**
     * Returns the length of the represented node. This is always 1.
     *
     * @return the length
     */
    @Override
    public int getLength()
    {
        return 1;
    }

    /**
     * Returns the name of this node. This is the attribute name.
     *
     * @return the name of this node
     */
    @Override
    public QName getName()
    {
        return new QName(null, attributeName);
    }

    /**
     * Returns a flag whether the represented node is a collection. This is not
     * the case.
     *
     * @return the collection flag
     */
    @Override
    public boolean isCollection()
    {
        return false;
    }

    /**
     * Returns a flag whether the represented node is a leaf. This is the case
     * for attributes.
     *
     * @return the leaf flag
     */
    @Override
    public boolean isLeaf()
    {
        return true;
    }

    /**
     * Returns a flag whether this node is an attribute. Of course, this is the
     * case.
     *
     * @return the attribute flag
     */
    @Override
    public boolean isAttribute()
    {
        return true;
    }

    /**
     * Returns the value of this node.
     *
     * @return this node's value
     */
    @Override
    public Object getValue()
    {
        return getNodeHandler().getAttributeValue(parentNode, attributeName);
    }

    /**
     * Sets the value of this node.
     *
     * @param value the new value
     */
    @Override
    public void setValue(Object value)
    {
        getNodeHandler().setAttributeValue(parentNode, attributeName, value);
    }

    /**
     * Tests if this node matches the given test. Attributes nodes are text
     * nodes, too, because they can contain a value.
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

    /**
     * Returns a reference to the current node handler. The handler is obtained
     * from the parent pointer.
     *
     * @return the node handler
     */
    protected NodeHandler<T> getNodeHandler()
    {
        return getParentPointer().getNodeHandler();
    }
}
