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

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for ViewNode.
 *
 * @version $Id$
 */
public class TestViewNode
{
    /** Stores the view node to be tested. */
    ViewNode viewNode;

    /** Stores a regular node. */
    ConfigurationNode node;

    /** A child node of the regular node. */
    ConfigurationNode child;

    /** An attribute node of the regular node. */
    ConfigurationNode attr;

    @Before
    public void setUp() throws Exception
    {
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
    @Test
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
    @Test(expected = IllegalArgumentException.class)
    public void testAddNullChild()
    {
        viewNode.addChild(null);
    }

    /**
     * Tests adding an attribute to the view node.
     */
    @Test
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
    @Test(expected = IllegalArgumentException.class)
    public void testAddNullAttribute()
    {
        viewNode.addAttribute(null);
    }

    /**
     * Tests appending all children to a view node.
     */
    @Test
    public void testAppendChildren()
    {
        viewNode.addChild(new DefaultConfigurationNode("testNode"));
        viewNode.appendChildren(node);
        assertEquals("Wrong number of children", 2, viewNode.getChildrenCount());
        assertEquals("Cannot find child", child, viewNode.getChild(1));
        assertEquals("Parent was changed", node, viewNode
                .getChild(1).getParentNode());
    }

    /**
     * Tests appending children from a null source. This should be a noop.
     */
    @Test
    public void testAppendNullChildren()
    {
        viewNode.appendChildren(null);
        assertEquals("Wrong number of children", 0, viewNode.getChildrenCount());
    }

    /**
     * tests appending all attributes to a view node.
     */
    @Test
    public void testAppendAttributes()
    {
        viewNode.appendAttributes(node);
        assertEquals("Wrong number of attributes", 1, viewNode
                .getAttributeCount());
        assertEquals("Cannot find attribute", attr, viewNode.getAttribute(0));
        assertEquals("Parent was changed", node, viewNode
                .getAttribute(0).getParentNode());
    }

    /**
     * Tests appending attributes from a null source. This should be a noop.
     */
    @Test
    public void testAppendNullAttributes()
    {
        viewNode.appendAttributes(null);
        assertEquals("Wrong number of attributes", 0, viewNode
                .getAttributeCount());
    }
}
