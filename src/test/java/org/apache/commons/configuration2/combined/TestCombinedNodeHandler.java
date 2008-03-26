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
package org.apache.commons.configuration2.combined;

import java.util.List;

import junit.framework.TestCase;

/**
 * Test class for CombinedNodeHandler.
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public class TestCombinedNodeHandler extends TestCase
{
    /** Constant for the prefix of child nodes. */
    private static final String CHILD_NAME = "child";

    /** Constant for the prefix of attributes. */
    private static final String ATTR_NAME = "attr";

    /** Constant for the number of children. */
    private static final int CHILD_COUNT = 10;

    /** Constant for the number of attributes. */
    private static final int ATTR_COUNT = 8;

    /** Constant for the name of the test view node. */
    private static final String NAME = "MyTestViewNode";

    /** A combined node that can be used for testing. */
    private CombinedNode node;

    /** The handler to be tested. */
    private CombinedNodeHandler handler;

    /**
     * Initializes the fixture. Creates a combined node with a number children and
     * attributes. The test handler is also created.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        node = new CombinedNode(NAME);
        for (int i = 0; i < CHILD_COUNT; i++)
        {
            String name = CHILD_NAME + i;
            node.addChild(name, name);
        }
        for (int i = 0; i < ATTR_COUNT; i++)
        {
            String attr = ATTR_NAME + i;
            node.setAttribute(attr, attr);
        }
        handler = new CombinedNodeHandler();
    }

    /**
     * Tests querying the name of the node.
     */
    public void testNodeName()
    {
        assertEquals("Wrong node name", NAME, handler.nodeName(node));
    }

    /**
     * Tests querying the value of the node.
     */
    public void testGetValue()
    {
        Object value = 42;
        node.setValue(value);
        assertEquals("Wrong value", value, handler.getValue(node));
    }

    /**
     * Tests setting the value of a node.
     */
    public void testSetValue()
    {
        Object value = 42;
        handler.setValue(node, value);
        assertEquals("Value was not set", value, node.getValue());
    }

    /**
     * Tests querying the parent node.
     */
    public void testGetParent()
    {
        CombinedNode parent = new CombinedNode();
        node.setParent(parent);
        assertEquals("Wrong parent", parent, handler.getParent(node));
    }

    /**
     * Tests querying the children of a combined node.
     */
    public void testGetChildren()
    {
        List<?> children = handler.getChildren(node);
        assertEquals("Wrong number of children", CHILD_COUNT, children.size());
        for (int i = 0; i < CHILD_COUNT; i++)
        {
            assertEquals("Wrong child at " + i, CHILD_NAME + i, children.get(i));
        }
    }

    /**
     * Tests querying the children with the given name.
     */
    public void testGetChildrenName()
    {
        final String name = CHILD_NAME + "1";
        List<Object> children = handler.getChildren(node, name);
        assertEquals("Wrong number of children with name", 1, children.size());
        assertEquals("Wrong child", name, children.get(0));
    }

    /**
     * Tests querying the number of all children.
     */
    public void testGetChildrenCount()
    {
        assertEquals("Wrong number of children", CHILD_COUNT, handler
                .getChildrenCount(node, null));
    }

    /**
     * Tests querying the number of children with a given name.
     */
    public void testGetChildrenCountName()
    {
        assertEquals("Wrong number of named children", 1, handler
                .getChildrenCount(node, CHILD_NAME + "2"));
        assertEquals("Wrong number of non-existing children", 0, handler
                .getChildrenCount(node, "unknownChild"));
    }

    /**
     * Tests querying a child by its index.
     */
    public void testGetChild()
    {
        for (int i = 0; i < CHILD_COUNT; i++)
        {
            assertEquals("Wrong child at " + i, CHILD_NAME + i, handler
                    .getChild(node, i));
        }
    }

    /**
     * Tests removing a child.
     */
    public void testRemoveChild()
    {
        Object child = node.getChild(2);
        handler.removeChild(node, child);
        List<Object> children = node.getChildren();
        assertEquals("Wrong number of children", CHILD_COUNT - 1, children
                .size());
        assertFalse("Child still present", children.contains(child));
    }

    /**
     * Tests adding another child.
     */
    public void testAddChild()
    {
        final String childName = "newChild";
        Object newChild = handler.addChild(node, childName);
        assertEquals("No child added", CHILD_COUNT + 1, node
                .getChildrenCount(null));
        CombinedNode child = (CombinedNode) node.getChild(CHILD_COUNT);
        assertSame("Wrong child", newChild, child);
        assertEquals("Name was not set", childName, child.getName());
    }

    /**
     * Tests querying the names of the existing attributes.
     */
    public void testGetAttributes()
    {
        List<String> attrs = handler.getAttributes(node);
        assertEquals("Wrong number of attributes", ATTR_COUNT, attrs.size());
        for (int i = 0; i < ATTR_COUNT; i++)
        {
            assertEquals("Wrong attribute at " + i, ATTR_NAME + i, attrs.get(i));
        }
    }

    /**
     * Tests adding a value to an attribute.
     */
    public void testAddAttributeValue()
    {
        final String attrName = ATTR_NAME + "3";
        final Object newVal = "newAttributeValue";
        handler.addAttributeValue(node, attrName, newVal);
        List<?> vals = (List<?>) node.getAttribute(attrName);
        assertEquals("Wrong number of attribute values", 2, vals.size());
        assertEquals("Wrong value 1", attrName, vals.get(0));
        assertEquals("Wrong value 2", newVal, vals.get(1));
    }

    /**
     * Tests setting the value of an attribute.
     */
    public void testSetAttributeValue()
    {
        final String attrName = ATTR_NAME + "4";
        final Object newVal = "anotherValue";
        handler.setAttributeValue(node, attrName, newVal);
        assertEquals("Wrong attribute value", newVal, node
                .getAttribute(attrName));
    }

    /**
     * Tests querying the value of an attribute.
     */
    public void testGetAttribute()
    {
        for (int i = 0; i < ATTR_COUNT; i++)
        {
            String attr = ATTR_NAME + i;
            assertEquals("Wrong value for " + attr, attr, handler
                    .getAttributeValue(node, attr));
        }
    }

    /**
     * Tests removing an attribute.
     */
    public void testRemoveAttribute()
    {
        final String attr = ATTR_NAME + "1";
        handler.removeAttribute(node, attr);
        List<String> attrs = node.getAttributes();
        assertEquals("No attribute removed", ATTR_COUNT - 1, attrs.size());
        assertFalse("Attribute still found", attrs.contains(attr));
    }

    /**
     * Tests isDefined() for a defined node.
     */
    public void testIsDefinedTrue()
    {
        assertTrue("Node not defined", handler.isDefined(node));
    }

    /**
     * Tests the isDefined() method in multiple variants.
     */
    public void testIsDefined()
    {
        CombinedNode n = new CombinedNode();
        assertFalse("Empty node defined", handler.isDefined(n));
        n.addChild(CHILD_NAME, "test");
        assertFalse("Node with child defined", handler.isDefined(n));
        n.setValue("value");
        assertTrue("Node with value not defined", handler.isDefined(n));
        n.setValue(null);
        n.addAttributeValue(ATTR_NAME, "test");
        assertTrue("Node with attribute not defined", handler.isDefined(n));
    }

    /**
     * Tests passing an illegal node to the handler. This should cause an
     * exception.
     */
    public void testIllegalArgument()
    {
        try
        {
            handler.setValue(this, "new value");
            fail("Illegal node argument was not detected!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }
}
