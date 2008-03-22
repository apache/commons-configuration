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
package org.apache.commons.configuration2.expr;

import java.util.List;

import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;

import junit.framework.TestCase;

/**
 * Test class for NodeList.
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class TestNodeList extends TestCase
{
    /** Constant for a test value. */
    private static final String VALUE = "test";

    /** Constant for a test name.*/
    private static final String NAME = "testName";

    /** Constant for the number of test nodes. */
    private static final int COUNT = 10;

    /** The list to be tested. */
    private NodeList<ConfigurationNode> list;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        list = new NodeList<ConfigurationNode>();
    }

    /**
     * Tests a newly created node list.
     */
    public void testInit()
    {
        assertEquals("List is not empty", 0, list.size());
    }

    /**
     * Tests adding nodes to a node list.
     */
    public void testAddNode()
    {
        for (int i = 0; i < COUNT; i++)
        {
            list.addNode(new DefaultConfigurationNode("node", VALUE + i));
        }

        assertEquals("Wrong number of nodes", COUNT, list.size());
        ConfigurationNodeHandler handler = new ConfigurationNodeHandler();
        for (int i = 0; i < COUNT; i++)
        {
            assertTrue("Not a node", list.isNode(i));
            assertFalse("An attribute", list.isAttribute(i));
            assertEquals("Wrong node value", VALUE + i, list.getValue(i,
                    handler));
            ConfigurationNode node = list.getNode(i);
            assertEquals("Wrong node name", "node", node.getName());
            assertEquals("Wrong node value", VALUE + i, node.getValue());
        }
    }

    /**
     * Tests adding attributes to a node list.
     */
    public void testAddAttribute()
    {
        final String attr = "attr";
        ConfigurationNode parent = new DefaultConfigurationNode("parent");

        for (int i = 0; i < COUNT; i++)
        {
            ConfigurationNode nd = new DefaultConfigurationNode(attr + i, VALUE
                    + i);
            parent.addAttribute(nd);
            list.addAttribute(parent, nd.getName());
        }

        assertEquals("Wrong number of nodes", COUNT, list.size());
        ConfigurationNodeHandler handler = new ConfigurationNodeHandler();
        for (int i = 0; i < COUNT; i++)
        {
            assertFalse("A node", list.isNode(i));
            assertTrue("Not an attribute", list.isAttribute(i));
            assertEquals("Wrong node value", VALUE + i, list.getValue(i,
                    handler));
        }
    }

    /**
     * Tests the getNode() method when the specified index is not a node. This
     * should cause an exception.
     */
    public void testGetNodeInvalidType()
    {
        list.addAttribute(new DefaultConfigurationNode(), "test");
        try
        {
            list.getNode(0);
            fail("Could obtain node that does not exist!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests the setValue() method for a node.
     */
    public void testSetValueNode()
    {
        ConfigurationNode node = new DefaultConfigurationNode("node");
        list.addNode(node);
        list.setValue(0, VALUE, new ConfigurationNodeHandler());
        assertEquals("Value was not set", VALUE, node.getValue());
    }

    /**
     * Tests setting the value of an attribute.
     */
    public void testSetValueAttribute()
    {
        ConfigurationNode node = new DefaultConfigurationNode("node");
        list.addAttribute(node, "testAttr");
        list.setValue(0, VALUE, new ConfigurationNodeHandler());
        List<ConfigurationNode> attrs = node.getAttributes();
        assertEquals("Wrong number of attributes", 1, attrs.size());
        ConfigurationNode attr = attrs.get(0);
        assertEquals("Wrong attribute name", "testAttr", attr.getName());
        assertEquals("Wrong attribute value", VALUE, attr.getValue());
    }

    /**
     * Tests specifying an invalid index. This should cause an exception.
     */
    public void testGetValueInvalidIndex()
    {
        for (int i = 0; i < COUNT; i++)
        {
            list.addNode(new DefaultConfigurationNode());
        }

        try
        {
            list.getValue(COUNT, new ConfigurationNodeHandler());
            fail("Invalid index was not detected!");
        }
        catch (IndexOutOfBoundsException iex)
        {
            // ok
        }
    }

    /**
     * Tests querying the name of a node.
     */
    public void testGetNameNode()
    {
        ConfigurationNode node = new DefaultConfigurationNode(NAME);
        list.addNode(node);
        assertEquals("Wrong node name", NAME, list.getName(0,
                new ConfigurationNodeHandler()));
    }

    /**
     * Tests querying the name of an attribute.
     */
    public void testGetNameAttribute()
    {
        ConfigurationNode parent = new DefaultConfigurationNode();
        list.addAttribute(parent, NAME);
        assertEquals("Wrong attribute name", NAME, list.getName(0,
                new ConfigurationNodeHandler()));
    }

    /**
     * Tests querying the parent of an attribute.
     */
    public void testGetAttributeParent()
    {
        ConfigurationNode parent = new DefaultConfigurationNode();
        list.addAttribute(parent, NAME);
        assertEquals("Wrong parent node", parent, list.getAttributeParent(0));
    }

    /**
     * Tests querying the parent of an attribute when the specified element is
     * not an attribute. This should cause an exception.
     */
    public void testGetAttributeParentNoAttribute()
    {
        list.addNode(new DefaultConfigurationNode(NAME));
        try
        {
            list.getAttributeParent(0);
            fail("Invalid element type not detected!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests selecting specific values of a multi-valued attribute.
     */
    public void testAddAttributeIndex()
    {
        ConfigurationNode parent = new DefaultConfigurationNode("parent");
        for (int i = 0; i < COUNT; i++)
        {
            ConfigurationNode attr = new DefaultConfigurationNode(NAME, VALUE
                    + i);
            parent.addAttribute(attr);
            list.addAttribute(parent, attr.getName(), i);
        }
        NodeList<ConfigurationNode> list2 = new NodeList<ConfigurationNode>();
        list2.addAttribute(parent, NAME);

        assertEquals("Wrong number of nodes", COUNT, list.size());
        ConfigurationNodeHandler handler = new ConfigurationNodeHandler();
        List<?> values = (List<?>) list2.getValue(0, handler);
        assertEquals("Wrong number of list values", COUNT, values.size());
        for (int i = 0; i < COUNT; i++)
        {
            assertFalse("A node", list.isNode(i));
            assertTrue("Not an attribute", list.isAttribute(i));
            assertEquals("Wrong node value", VALUE + i, list.getValue(i,
                    handler));
            assertEquals("Wrong list value", VALUE + i, values.get(i));
        }
    }

    /**
     * Tests accessing an attribute with an index that does not have multiple
     * values. In this case the normal value of the attribute should be
     * returned.
     */
    public void testGetAttributeIndexNoCollection()
    {
        ConfigurationNode parent = new DefaultConfigurationNode("parent");
        parent.addAttribute(new DefaultConfigurationNode(NAME, VALUE));
        list.addAttribute(parent, NAME, 1);
        assertEquals("Wrong attribute value", VALUE, list.getValue(0,
                new ConfigurationNodeHandler()));
    }

    /**
     * Tests accessing an attribute with an invalid index. In this case the
     * whole value collection should be returned.
     */
    public void testGetAttributeIndexInvalid()
    {
        ConfigurationNode parent = new DefaultConfigurationNode("parent");
        for (int i = 0; i < COUNT; i++)
        {
            ConfigurationNode attr = new DefaultConfigurationNode(NAME, VALUE
                    + i);
            parent.addAttribute(attr);
        }
        list.addAttribute(parent, NAME, COUNT + 10);
        List<?> val = (List<?>) list
                .getValue(0, new ConfigurationNodeHandler());
        assertEquals("Wrong number of list elements", COUNT, val.size());
        for (int i = 0; i < COUNT; i++)
        {
            assertEquals("Wrong list value " + i, VALUE + i, val.get(i));
        }
    }
}
