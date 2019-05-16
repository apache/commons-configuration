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

import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.configuration2.tree.QueryResult;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * <p>
 * A specialized {@code NodePointer} implementation for the attributes of
 * a configuration node.
 * </p>
 *
 * @since 2.0
 * @param <T> the type of the nodes this pointer deals with
 */
class ConfigurationAttributePointer<T> extends NodePointer
{
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 5504551041716043748L;

    /** Stores information about the represented attribute. */
    private final QueryResult<T> attributeResult;

    /**
     * Creates a new instance of {@code ConfigurationAttributePointer}.
     *
     * @param parent the parent node pointer
     * @param attrName the name of the managed attribute
     */
    public ConfigurationAttributePointer(final ConfigurationNodePointer<T> parent,
            final String attrName)
    {
        super(parent);
        attributeResult =
                QueryResult.createAttributeResult(
                        parent.getConfigurationNode(), attrName);
    }

    /**
     * Returns a reference to the parent node pointer.
     *
     * @return the parent pointer
     */
    public ConfigurationNodePointer<T> getParentPointer()
    {
        // safe to cast because the constructor only expects pointers of this
        // type
        @SuppressWarnings("unchecked")
        final
        ConfigurationNodePointer<T> configurationNodePointer =
                (ConfigurationNodePointer<T>) getParent();
        return configurationNodePointer;
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
    public int compareChildNodePointers(final NodePointer p1, final NodePointer p2)
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
     * Returns the immediate node. This is actually a {@link QueryResult}
     * object describing the represented attribute.
     *
     * @return the immediate node
     */
    @Override
    public Object getImmediateNode()
    {
        return attributeResult;
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
        return new QName(null, attributeResult.getAttributeName());
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
        return attributeResult.getAttributeValue(getNodeHandler());
    }

    /**
     * Sets the value of this node. This is not supported because the classes of
     * the {@code XPathExpressionEngine} are only used for queries. This
     * implementation always throws an exception.
     *
     * @param value the new value
     */
    @Override
    public void setValue(final Object value)
    {
        throw new UnsupportedOperationException(
                "Updating the value is not supported!");
    }

    /**
     * Tests if this node matches the given test. Attribute nodes are text
     * nodes, too, because they can contain a value.
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
     * Returns a reference to the current node handler. The handler is obtained
     * from the parent pointer.
     *
     * @return the node handler
     */
    private NodeHandler<T> getNodeHandler()
    {
        return getParentPointer().getNodeHandler();
    }
}
