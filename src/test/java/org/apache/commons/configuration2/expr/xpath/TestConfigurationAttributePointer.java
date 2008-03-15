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
import java.util.Locale;

import org.apache.commons.configuration2.expr.ConfigurationNodeHandler;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;

import junit.framework.TestCase;

/**
 * Test class for ConfigurationAttributePointer.
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class TestConfigurationAttributePointer extends TestCase
{
    /** Constant for the name of the test attribute. */
    private static final String ATTR_NAME = "myAttr";

    /** Constant for the value of the test attribute. */
    private static final String ATTR_VALUE = "myValue";

    /** Stores the parent node pointer. */
    private ConfigurationNodePointer<ConfigurationNode> parent;

    /** The attribute pointer to be tested. */
    private ConfigurationAttributePointer<ConfigurationNode> pointer;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ConfigurationNode nd = new DefaultConfigurationNode("parent");
        ConfigurationNode attr = new DefaultConfigurationNode(ATTR_NAME,
                ATTR_VALUE);
        nd.addAttribute(attr);
        parent = new ConfigurationNodePointer<ConfigurationNode>(nd,
                new ConfigurationNodeHandler(), Locale.ENGLISH);
        pointer = new ConfigurationAttributePointer<ConfigurationNode>(parent,
                ATTR_NAME);
    }

    /**
     * Tests whether the correct pointer is returned.
     */
    public void testGetParentPointer()
    {
        assertSame("Wrong parent pointer", parent, pointer.getParentPointer());
    }

    /**
     * Tests querying the base value.
     */
    public void testGetBaseValue()
    {
        assertEquals("Wrong base value", ATTR_VALUE, pointer.getBaseValue());
    }

    /**
     * Tests querying the immediate node. Here a proxy for an attribute node
     * should be returned.
     */
    @SuppressWarnings("unchecked")
    public void testGetImmediateNode()
    {
        Object node = pointer.getImmediateNode();
        assertTrue(
                "Wrong node class",
                node instanceof ConfigurationAttributePointer.AttributeNodeProxy);
        ConfigurationAttributePointer<ConfigurationNode>.AttributeNodeProxy proxy =
            (ConfigurationAttributePointer.AttributeNodeProxy) node;
        assertEquals("Wrong parent node", parent.getConfigurationNode(), proxy
                .getParentNode());
        assertEquals("Wrong attribute name", ATTR_NAME, proxy
                .getAttributeName());
    }

    /**
     * Tests the length.
     */
    public void testGetLength()
    {
        assertEquals("Wrong length", 1, pointer.getLength());
    }

    /**
     * Tests querying the node name.
     */
    public void testGetName()
    {
        QName name = pointer.getName();
        assertEquals("Wrong name", ATTR_NAME, name.getName());
        assertNull("Prefix not null", name.getPrefix());
    }

    /**
     * Tests the collection flag.
     */
    public void testIsCollection()
    {
        assertFalse("Wrong collection flag", pointer.isCollection());
    }

    /**
     * Tests the leaf flag.
     */
    public void testIsLeaf()
    {
        assertTrue("Wrong leaf flag", pointer.isLeaf());
    }

    /**
     * Tests the attribute flag.
     */
    public void testIsAttribute()
    {
        assertTrue("Not an attribute", pointer.isAttribute());
    }

    /**
     * Tests querying the attribute's value.
     */
    public void testGetValue()
    {
        assertEquals("Wrong value", ATTR_VALUE, pointer.getValue());
    }

    /**
     * Tests setting a new value.
     */
    public void testSetValue()
    {
        pointer.setValue("newValue");
        List<ConfigurationNode> attrs = parent.getConfigurationNode()
                .getAttributes();
        assertEquals("Wrong number of attributes", 1, attrs.size());
        assertEquals("Wrong attribute value", "newValue", attrs.get(0)
                .getValue());
    }

    /**
     * Tests querying an iterator for attributes. Result should be null.
     */
    public void testAttributeIterator()
    {
        assertNull("Returned an attribute iterator", pointer
                .attributeIterator(new QName(null, "test")));
    }

    /**
     * Tests querying an iterator for children. Result should be null.
     */
    public void testChildIterator()
    {
        assertNull("Returned an iterator for children", pointer.childIterator(
                null, false, null));
    }

    /**
     * Tests the testNode() method.
     */
    public void testTestNode()
    {
        NodeTest test = new NodeTypeTest(Compiler.NODE_TYPE_TEXT);
        assertTrue("No a text node", pointer.testNode(test));
        test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        assertFalse("A comment node", pointer.testNode(test));
    }
}
