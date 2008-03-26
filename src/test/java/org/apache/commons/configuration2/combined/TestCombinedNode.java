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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 * Test class or CombinedNode.
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public class TestCombinedNode extends TestCase
{
    /** Constant for the name of the node. */
    private static final String NODE_NAME = "MyViewNode";

    /** Constant for the name prefix of child elements. */
    private static final String CHILD_NAME = "child";

    /** Constant for the name of an attribute. */
    private static final String ATTR_NAME = "attribute";

    /** Constant for the number of different child names. */
    private static final int CHILD_COUNT = 3;

    /** The node to be tested. */
    private CombinedNode node;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        node = new CombinedNode(NODE_NAME);
    }

    /**
     * Adds a number of child nodes to the test node. This method adds one child
     * with name child1, two children with name child2, etc. The object used as
     * representation of the children is an integer with the running index of
     * the children with this name.
     */
    private void initChildren()
    {
        for (int i = 1; i <= CHILD_COUNT; i++)
        {
            for (int j = 1; j <= i; j++)
            {
                node.addChild(CHILD_NAME + i, Integer.valueOf(j));
            }
        }
    }

    /**
     * Returns the total number of children of the test node.
     *
     * @return the total number of test children
     */
    private static int totalChildCount()
    {
        return CHILD_COUNT * (CHILD_COUNT + 1) / 2; // Gauss formula
    }

    /**
     * Tests a newly created combined node.
     */
    public void testInit()
    {
        assertEquals("Wrong node name", NODE_NAME, node.getName());
        assertNull("A parent was set", node.getParent());
        assertNull("A value was set", node.getValue());
        assertTrue("Children not empty", node.getChildren().isEmpty());
        assertTrue("Attributes not empty", node.getAttributes().isEmpty());
        assertEquals("Wrong number of children", 0, node.getChildrenCount(null));
        assertEquals("Wrong number of named children", 0, node
                .getChildrenCount(CHILD_NAME));
    }

    /**
     * Tests creating a combined node using the default constructor.
     */
    public void testInitDefault()
    {
        node = new CombinedNode();
        assertNull("Node has a name", node.getName());
    }

    /**
     * Tests querying the children of the combined node.
     */
    public void testGetChildren()
    {
        initChildren();
        List<Object> children = node.getChildren();
        assertEquals("Wrong number of child nodes", totalChildCount(), children
                .size());
        for (int i = 1, idx = 0; i <= CHILD_COUNT; i++)
        {
            for (int j = 1; j <= i; j++, idx++)
            {
                assertEquals("Wrong child at " + idx, Integer.valueOf(j),
                        children.get(idx));
            }
        }
    }

    /**
     * Tests accessing children based on their index.
     */
    public void testGetChild()
    {
        initChildren();
        List<Object> children = node.getChildren();
        int idx = 0;
        for (Object o : children)
        {
            assertEquals("Wrong child at " + idx, o, node.getChild(idx++));
        }
    }

    /**
     * Tests querying children by their name.
     */
    public void testGetChildrenName()
    {
        initChildren();
        for (int i = 1; i <= CHILD_COUNT; i++)
        {
            List<Object> children = node.getChildren(CHILD_NAME + i);
            assertEquals("Wrong number of children", i, children.size());
            for (int j = 0; j < i; j++)
            {
                assertEquals("Wrong child at " + j, Integer.valueOf(j + 1),
                        children.get(j));
            }
        }
    }

    /**
     * Tests removing a child node.
     */
    public void testRemoveChild()
    {
        initChildren();
        Object child = Integer.valueOf(CHILD_COUNT);
        node.removeChild(child);
        for (Object o : node.getChildren())
        {
            if (child.equals(o))
            {
                fail("Child was not removed!");
            }
        }
    }

    /**
     * Tests querying the total number of children.
     */
    public void testGetChildrenCount()
    {
        initChildren();
        assertEquals("Wrong number of children", totalChildCount(), node
                .getChildrenCount(null));
    }

    /**
     * Tests querying the number of named children.
     */
    public void testGetChildrenCountName()
    {
        initChildren();
        for (int i = 1; i <= CHILD_COUNT; i++)
        {
            assertEquals("Wrong number of children", i, node
                    .getChildrenCount(CHILD_NAME + i));
        }
    }

    /**
     * Tests adding values for an attribute.
     */
    public void testAddAttributeValue()
    {
        node.addAttributeValue(ATTR_NAME, "test");
        assertEquals("Wrong attribute value", "test", node
                .getAttribute(ATTR_NAME));
        List<String> attrs = node.getAttributes();
        assertEquals("Wrong number of attributes", 1, attrs.size());
        assertEquals("Wrong attribute name", ATTR_NAME, attrs.get(0));
    }

    /**
     * Tests adding multiple values for the same attribute.
     */
    public void testAddAttributeValueMultiple()
    {
        for (int i = 0; i < CHILD_COUNT; i++)
        {
            node.addAttributeValue(ATTR_NAME, i);
        }
        List<?> values = (List<?>) node.getAttribute(ATTR_NAME);
        assertEquals("Wrong number of values", CHILD_COUNT, values.size());
        for (int i = 0; i < CHILD_COUNT; i++)
        {
            assertEquals("Wrong value at " + i, Integer.valueOf(i), values
                    .get(i));
        }
    }

    /**
     * Tests setting the value of an attribute.
     */
    public void testSetAttributeValue()
    {
        node.setAttribute(ATTR_NAME, "test");
        assertEquals("Wrong attribute value", "test", node
                .getAttribute(ATTR_NAME));
    }

    /**
     * Tests setting the value of an existing attribute. The old value should be
     * removed first.
     */
    public void testSetAttributeValueExisting()
    {
        node.addAttributeValue(ATTR_NAME, "value1");
        node.addAttributeValue(ATTR_NAME, "value2");
        node.setAttribute(ATTR_NAME, "newValue");
        assertEquals("Wrong attribute value", "newValue", node
                .getAttribute(ATTR_NAME));
    }

    /**
     * Tests adding an attribute with multiple values.
     */
    public void testAddAttributeValueCollection()
    {
        node.addAttributeValue(ATTR_NAME, Arrays.asList(new String[] {
                "val1", "val2"
        }));
        node.addAttributeValue(ATTR_NAME, "val3");
        List<?> values = (List<?>) node.getAttribute(ATTR_NAME);
        assertEquals("Wrong number of values", 3, values.size());
        assertEquals("Wrong value 1", "val1", values.get(0));
        assertEquals("Wrong value 2", "val2", values.get(1));
        assertEquals("Wrong value 3", "val3", values.get(2));
    }

    /**
     * Tests adding a collection with values to an attribute that already has
     * multiple values.
     */
    public void testAddAttributeValueCollections()
    {
        List<Object> val1 = new ArrayList<Object>();
        val1.add("test1");
        val1.add("test2");
        List<Object> val2 = new ArrayList<Object>();
        val2.add("testVal3");
        val2.add("anotherTest");
        val2.add("even more tests");
        node.setAttribute(ATTR_NAME, val1);
        node.addAttributeValue(ATTR_NAME, val2);
        List<Object> expected = new ArrayList<Object>(val1);
        expected.addAll(val2);
        List<?> values = (List<?>) node.getAttribute(ATTR_NAME);
        assertEquals("Wrong number of values", expected.size(), values.size());
        for (int i = 0; i < expected.size(); i++)
        {
            assertEquals("Wrong value at " + i, expected.get(i), values.get(i));
        }
    }

    /**
     * Tests removing an attribute.
     */
    public void testRemoveAttribute()
    {
        node.setAttribute(ATTR_NAME, "test");
        node.addAttributeValue(ATTR_NAME, "anotherTest");
        node.removeAttribute(ATTR_NAME);
        assertNull("Attribute still found", node.getAttribute(ATTR_NAME));
        assertTrue("Attribute name still found", node.getAttributes().isEmpty());
    }
}
