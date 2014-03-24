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
package org.apache.commons.configuration.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for DefaultConfigurationNode.
 *
 * @version $Id$
 */
public class TestDefaultConfigurationNode
{
    /** Constant array for the field names. */
    private static final String[] FIELD_NAMES =
    { "UID", "NAME", "FIRSTNAME", "LASTLOGIN"};

    /** Constant array for the field data types. */
    private static final String[] FIELD_TYPES =
    { "long", "string", "string", "date"};

    /** Constant array for additional field attributes. */
    private static final String[] FIELD_ATTRS =
    { "primarykey,unique", "notnull", "notnull", null};

    /** The node to be tested. */
    DefaultConfigurationNode node;

    @Before
    public void setUp() throws Exception
    {
        node = new DefaultConfigurationNode();
        node.setName("table");
        node.setReference("TestReference");
        node.addAttribute(new DefaultConfigurationNode("type", "system"));
        node.addChild(new DefaultConfigurationNode("name", "users"));

        // Add nodes for the table's fields
        for (int i = 0; i < FIELD_NAMES.length; i++)
        {
            DefaultConfigurationNode field = new DefaultConfigurationNode(
                    "field");
            field
                    .addChild(new DefaultConfigurationNode("name",
                            FIELD_NAMES[i]));
            field.addAttribute(new DefaultConfigurationNode("type",
                    FIELD_TYPES[i]));
            if (FIELD_ATTRS[i] != null)
            {
                StringTokenizer tok = new StringTokenizer(FIELD_ATTRS[i], ", ");
                while (tok.hasMoreTokens())
                {
                    field.addAttribute(new DefaultConfigurationNode(
                            "attribute", tok.nextToken()));
                }
            }
            node.addChild(field);
        }
    }

    /**
     * Tests a newly created, uninitialized node.
     */
    @Test
    public void testNewNode()
    {
        node = new DefaultConfigurationNode();
        assertNull("name is not null", node.getName());
        assertNull("value is not null", node.getValue());
        assertNull("reference is not null", node.getReference());
        assertTrue("Children are not empty", node.getChildren().isEmpty());
        assertTrue("Named children are not empty", node.getChildren("test")
                .isEmpty());
        assertEquals("Children cound is not 0", 0, node.getChildrenCount());
        assertEquals("Named children count is not 0", 0, node
                .getChildrenCount("test"));
        assertTrue("Attributes are not empty", node.getAttributes().isEmpty());
        assertTrue("Named attributes are not empty", node.getAttributes("test")
                .isEmpty());
        assertNull("Node has a parent", node.getParentNode());
        assertFalse("Node is defined", node.isDefined());
    }

    /**
     * Tries to access an attribute using an invalid index.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetAttributeNonExisting()
    {
        node = new DefaultConfigurationNode();
        node.getAttribute(0);
    }

    /**
     * Tests accessing a node's reference.
     */
    @Test
    public void testGetReference()
    {
        assertEquals("Reference was not stored", "TestReference", node
                .getReference());
    }

    /**
     * Tests accessing the node's children.
     */
    @Test
    public void testGetChildren()
    {
        assertEquals("Number of children incorrect", FIELD_NAMES.length + 1,
                node.getChildrenCount());
        List<ConfigurationNode> children = node.getChildren();
        Iterator<ConfigurationNode> it = children.iterator();
        DefaultConfigurationNode child = (DefaultConfigurationNode) it.next();
        assertEquals("Wrong node", "name", child.getName());
        checkFieldNodes(it);
    }

    /**
     * Tests accessing the node's children by name.
     */
    @Test
    public void testGetChildrenByName()
    {
        List<ConfigurationNode> children = node.getChildren("field");
        assertEquals("Incorrect number of child nodes", FIELD_NAMES.length,
                children.size());
        assertEquals("Incorrect result of getChildrenCount()",
                FIELD_NAMES.length, node.getChildrenCount("field"));
        checkFieldNodes(children.iterator());
        assertTrue("Found non existing nodes", node.getChildren("test")
                .isEmpty());
        assertEquals("Wrong children list for null", node.getChildren(), node
                .getChildren(null));
    }

    /**
     * Tests adding a new child node.
     */
    @Test
    public void testAddChild()
    {
        int cnt = node.getChildrenCount();
        DefaultConfigurationNode ndNew = new DefaultConfigurationNode("test",
                "xyz");
        node.addChild(ndNew);
        assertEquals("New node was not added", cnt + 1, node.getChildrenCount());
        List<ConfigurationNode> children = node.getChildren();
        assertEquals("Incorrect number of children", node.getChildrenCount(),
                children.size());
        assertSame("Node was not added to end", ndNew, children.get(cnt));
        assertEquals("Incorrect number of named children", 1, node
                .getChildrenCount(ndNew.getName()));
        assertFalse("Child is an attribute", ndNew.isAttribute());
        assertSame("Parent was not set", node, ndNew.getParentNode());
    }

    /**
     * Tries to add a null child node.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddChildNull()
    {
        node.addChild(null);
    }

    /**
     * Tries to add a node without a name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddUndefinedChild()
    {
        node.addChild(new DefaultConfigurationNode());
    }

    /**
     * Tests removing a child node.
     */
    @Test
    public void testRemoveChild()
    {
        DefaultConfigurationNode child = (DefaultConfigurationNode) node
                .getChildren().get(3);
        int cnt = node.getChildrenCount();
        node.removeChild(child);
        assertEquals("Child was not removed", cnt - 1, node.getChildrenCount());
        for (ConfigurationNode nd : node.getChildren())
        {
            assertNotSame("Found removed node", child, nd);
        }
        assertNull("Parent reference was not removed", child.getParentNode());
    }

    /**
     * Tests removing a child node that does not belong to this node.
     */
    @Test
    public void testRemoveNonExistingChild()
    {
        int cnt = node.getChildrenCount();
        node.removeChild(new DefaultConfigurationNode("test"));
        node.removeChild(new DefaultConfigurationNode());
        node.removeChild((ConfigurationNode) null);
        node.removeChild("non existing child node");
        node.removeChild((String) null);
        assertEquals("Children were changed", cnt, node.getChildrenCount());
    }

    /**
     * Tests removing children by their name.
     */
    @Test
    public void testRemoveChildByName()
    {
        int cnt = node.getChildrenCount();
        node.removeChild("name");
        assertEquals("Child was not removed", cnt - 1, node.getChildrenCount());
        assertEquals("Still found name child", 0, node.getChildrenCount("name"));
        node.removeChild("field");
        assertEquals("Still remaining nodes", 0, node.getChildrenCount());
    }

    /**
     * Tests removing all children at once.
     */
    @Test
    public void testRemoveChildren()
    {
        node.removeChildren();
        assertEquals("Children count is not 0", 0, node.getChildrenCount());
        assertTrue("Children are not empty", node.getChildren().isEmpty());
    }

    /**
     * Tests accessing a child by its index.
     */
    @Test
    public void testGetChild()
    {
        ConfigurationNode child = node.getChild(2);
        assertEquals("Wrong child returned", child, node.getChildren().get(2));
    }

    /**
     * Tests accessing child nodes with invalid indices.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetChildInvalidIndex()
    {
        node.getChild(4724);
    }

    /**
     * Tests accessing the node's attributes.
     */
    @Test
    public void testGetAttributes()
    {
        assertEquals("Number of attributes incorrect", 1, node
                .getAttributeCount());
        List<ConfigurationNode> attributes = node.getAttributes();
        Iterator<ConfigurationNode> it = attributes.iterator();
        DefaultConfigurationNode attr = (DefaultConfigurationNode) it.next();
        assertEquals("Wrong node", "type", attr.getName());
        assertFalse("More attributes", it.hasNext());
    }

    /**
     * Tests accessing the node's attributes by name.
     */
    @Test
    public void testGetAttributesByName()
    {
        assertEquals("Incorrect number of attributes", 1, node
                .getAttributeCount("type"));
        DefaultConfigurationNode field = (DefaultConfigurationNode) node
                .getChildren().get(1);
        assertEquals("Incorrect number of attributes", 2, field
                .getAttributeCount("attribute"));
        List<ConfigurationNode> attrs = field.getAttributes("attribute");
        assertEquals("Wrong value", "primarykey",
                ((DefaultConfigurationNode) attrs.get(0)).getValue());
        assertEquals("Wrong value", "unique", ((DefaultConfigurationNode) attrs
                .get(1)).getValue());
    }

    /**
     * Tests adding a new attribute node.
     */
    @Test
    public void testAddAttribute()
    {
        int cnt = node.getAttributeCount();
        DefaultConfigurationNode ndNew = new DefaultConfigurationNode("test",
                "xyz");
        node.addAttribute(ndNew);
        assertEquals("New node was not added", cnt + 1, node
                .getAttributeCount());
        List<ConfigurationNode> attrs = node.getAttributes();
        assertEquals("Incorrect number of attributes",
                node.getAttributeCount(), attrs.size());
        assertSame("Node was not added to end", ndNew, attrs.get(cnt));
        assertEquals("Incorrect number of named attributes", 1, node
                .getAttributeCount(ndNew.getName()));
        assertTrue("Child is no attribute", ndNew.isAttribute());
        assertSame("Parent was not set", node, ndNew.getParentNode());
    }

    /**
     * Tests removing an attribute node.
     */
    @Test
    public void testRemoveAttribute()
    {
        DefaultConfigurationNode attr = (DefaultConfigurationNode) node
                .getAttributes().get(0);
        int cnt = node.getAttributeCount();
        node.removeAttribute(attr);
        assertEquals("Attribute was not removed", cnt - 1, node
                .getAttributeCount());
        for (ConfigurationNode nd : node.getAttributes())
        {
            assertNotSame("Found removed node", attr, nd);
        }
        assertNull("Parent reference was not removed", attr.getParentNode());
    }

    /**
     * Tests removing attributes by their names.
     */
    @Test
    public void testRemoveAttributeByName()
    {
        ConfigurationNode field = node.getChild(1);
        assertEquals("Incorrect number of attributes", 3, field
                .getAttributeCount());
        field.removeAttribute("attribute");
        assertEquals("Not all nodes removed", 1, field.getAttributeCount());
        assertTrue("Remaining attributes", field.getAttributes("attribute")
                .isEmpty());
        field.removeAttribute("type");
        assertEquals("Remaining attributes", 0, field.getAttributeCount());
    }

    /**
     * Tests removing all attributes.
     */
    @Test
    public void testRemoveAttributes()
    {
        node.removeAttributes();
        assertEquals("Not all attributes removed", 0, node.getAttributeCount());
        assertTrue("Attributes not empty", node.getAttributes().isEmpty());
    }

    /**
     * Tests changing a node's attribute state.
     */
    @Test(expected = IllegalStateException.class)
    public void testChangeAttributeState()
    {
        ConfigurationNode attr = node.getAttribute(0);
        attr.setAttribute(false);
    }

    /**
     * Tests the visit() method using a simple visitor.
     */
    @Test
    public void testVisit()
    {
        CountNodeVisitor visitor = new CountNodeVisitor();
        node.visit(visitor);
        assertEquals("Not all nodes visited", 19, visitor.beforeCalls);
        assertEquals("Different number of before and after calls",
                visitor.beforeCalls, visitor.afterCalls);
    }

    /**
     * Tests the visit() method with a visitor that terminates the visit
     * process.
     */
    @Test
    public void testVisitWithTerminate()
    {
        CountNodeVisitor visitor = new CountNodeVisitor(10);
        node.visit(visitor);
        assertEquals("Incorrect number of nodes visited", visitor.maxCalls,
                visitor.beforeCalls);
        assertEquals("Different number of before and after calls",
                visitor.beforeCalls, visitor.afterCalls);
    }

    /**
     * Tests the visit() method when null is passed in. This should throw an
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testVisitWithNullVisitor()
    {
        node.visit(null);
    }

    /**
     * Tests cloning a node.
     */
    @Test
    public void testClone()
    {
        node.setValue("TestValue");
        DefaultConfigurationNode clone = (DefaultConfigurationNode) node.clone();
        assertEquals("Value not cloned", "TestValue", clone.getValue());
        assertEquals("Name not cloned", "table", clone.getName());
        assertEquals("Reference not cloned", "TestReference", clone.getReference());
        assertEquals("Children were cloned", 0, clone.getChildrenCount());
        assertEquals("Attributes were cloned", 0, clone.getAttributeCount());
    }

    /**
     * Helper method for checking the child nodes of type &quot;field&quot;.
     *
     * @param itFields the iterator with the child nodes
     */
    private void checkFieldNodes(Iterator<ConfigurationNode> itFields)
    {
        for (String element : FIELD_NAMES) {
            DefaultConfigurationNode child = (DefaultConfigurationNode) itFields
                    .next();
            assertEquals("Wrong node", "field", child.getName());
            List<ConfigurationNode> nameNodes = child.getChildren("name");
            assertEquals("Wrong number of name nodes", 1, nameNodes.size());
            DefaultConfigurationNode nameNode = (DefaultConfigurationNode) nameNodes
                    .get(0);
            assertEquals("Wrong field name", element, nameNode
                    .getValue());
        }
    }

    /**
     * A test visitor implementation that is able to count the number of visits.
     * It also supports a maximum number of visits to be set; if this number is
     * reached, the <code>terminate()</code> method returns <b>true</b>.
     */
    public static class CountNodeVisitor implements ConfigurationNodeVisitor<ConfigurationNode>
    {
        public int beforeCalls;

        public int afterCalls;

        public int maxCalls;

        public CountNodeVisitor()
        {
            this(Integer.MAX_VALUE);
        }

        public CountNodeVisitor(int maxNumberOfVisits)
        {
            maxCalls = maxNumberOfVisits;
        }

        @Override
        public void visitBeforeChildren(ConfigurationNode node,
                NodeHandler<ConfigurationNode> handler)
        {
            beforeCalls++;
        }

        @Override
        public void visitAfterChildren(ConfigurationNode node,
                NodeHandler<ConfigurationNode> handler)
        {
            afterCalls++;
        }

        @Override
        public boolean terminate()
        {
            return beforeCalls >= maxCalls;
        }
    }
}
