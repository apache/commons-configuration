/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import junit.framework.TestCase;

/**
 * Test class for ViewNode.
 *
 * @version $Id$
 */
public class TestViewNode extends TestCase
{
    /** Stores the view node to be tested. */
    ViewNode viewNode;

    /** Stores a regular node. */
    ConfigurationNode node;

    /** A child node of the regular node. */
    ConfigurationNode child;

    /** An attribute node of the regular node. */
    ConfigurationNode attr;

    protected void setUp() throws Exception
    {
        super.setUp();
        node = new DefaultConfigurationNode();
        child = new DefaultConfigurationNode("child");
        attr = new DefaultConfigurationNode("attr");
        node.addChild(child);
        node.addAttribute(attr);
        viewNode = new ViewNode();
    }

    /**
     * Tests adding a child to the view node.
     */
    public void testAddChild()
    {
        viewNode.addChild(child);
        assertEquals("Parent was changed", node, child.getParentNode());
        assertEquals("Child was not added", 1, viewNode.getChildrenCount());
    }

    /**
     * Tests adding a null child to the view node. This should throw an
     * exception.
     */
    public void testAddNullChild()
    {
        try
        {
            viewNode.addChild(null);
            fail("Could add null child!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests adding an attribute to the view node.
     */
    public void testAddAttribute()
    {
        viewNode.addAttribute(attr);
        assertEquals("Parent was changed", node, attr.getParentNode());
        assertEquals("Attribute was not added", 1, viewNode.getAttributeCount());
    }

    /**
     * Tests adding a null attribute to the view node. This should cause an
     * exception.
     */
    public void testAddNullAttribute()
    {
        try
        {
            viewNode.addAttribute(null);
            fail("Could add null attribute");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests appending all children to a view node.
     */
    public void testAppendChildren()
    {
        viewNode.addChild(new DefaultConfigurationNode("testNode"));
        viewNode.appendChildren(node);
        assertEquals("Wrong number of children", 2, viewNode.getChildrenCount());
        assertEquals("Cannot find child", child, viewNode.getChild(1));
        assertEquals("Parent was changed", node, ((ConfigurationNode) viewNode
                .getChild(1)).getParentNode());
    }

    /**
     * Tests appending children from a null source. This should be a noop.
     */
    public void testAppendNullChildren()
    {
        viewNode.appendChildren(null);
        assertEquals("Wrong number of children", 0, viewNode.getChildrenCount());
    }

    /**
     * tests appending all attributes to a view node.
     */
    public void testAppendAttributes()
    {
        viewNode.appendAttributes(node);
        assertEquals("Wrong number of attributes", 1, viewNode
                .getAttributeCount());
        assertEquals("Cannot find attribute", attr, viewNode.getAttribute(0));
        assertEquals("Parent was changed", node, ((ConfigurationNode) viewNode
                .getAttribute(0)).getParentNode());
    }

    /**
     * Tests appending attributes from a null source. This should be a noop.
     */
    public void testAppendNullAttributes()
    {
        viewNode.appendAttributes(null);
        assertEquals("Wrong number of attributes", 0, viewNode
                .getAttributeCount());
    }
}
