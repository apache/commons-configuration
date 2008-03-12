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

import java.util.Locale;

import org.apache.commons.configuration2.expr.ConfigurationNodeHandler;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * Test class for ConfigurationNodePointer.
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class TestConfigurationNodePointer extends AbstractXPathTest
{
    /** Stores the node pointer to be tested. */
    private ConfigurationNodePointer<ConfigurationNode> pointer;

    /** Stores the node handler to be used for the node pointers.*/
    private ConfigurationNodeHandler handler;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        handler = new ConfigurationNodeHandler();
        pointer = new ConfigurationNodePointer<ConfigurationNode>(root,
                handler, Locale.getDefault());
    }

    /**
     * Tests comparing child node pointers for child nodes.
     */
    public void testCompareChildNodePointersChildren()
    {
        NodePointer p1 = new ConfigurationNodePointer<ConfigurationNode>(
                pointer, root.getChild(1), handler);
        NodePointer p2 = new ConfigurationNodePointer<ConfigurationNode>(
                pointer, root.getChild(3), handler);
        assertEquals("Incorrect order", -1, pointer.compareChildNodePointers(
                p1, p2));
        assertEquals("Incorrect symmetric order", 1, pointer
                .compareChildNodePointers(p2, p1));
    }

    /**
     * Tests comparing child node pointers for attribute nodes. Attributes are
     * not taken into account, so result should be 0.
     */
    public void testCompareChildNodePointersAttributes()
    {
        root.addAttribute(new DefaultConfigurationNode("attr1", "test1"));
        root.addAttribute(new DefaultConfigurationNode("attr2", "test2"));
        NodePointer p1 = new ConfigurationNodePointer<ConfigurationNode>(pointer,
                root.getAttribute(0), handler);
        NodePointer p2 = new ConfigurationNodePointer<ConfigurationNode>(pointer,
                root.getAttribute(1), handler);
        assertEquals("Incorrect order", 0, pointer.compareChildNodePointers(
                p1, p2));
        assertEquals("Incorrect symmetric order", 0, pointer
                .compareChildNodePointers(p2, p1));
    }

    /**
     * Tests comparing child node pointers for both child and attribute nodes.
     * Attributes are not taken into account, so the child node should be
     * sorted first.
     */
    public void testCompareChildNodePointersChildAndAttribute()
    {
        root.addAttribute(new DefaultConfigurationNode("attr1", "test1"));
        NodePointer p1 = new ConfigurationNodePointer<ConfigurationNode>(pointer,
                root.getChild(2), handler);
        NodePointer p2 = new ConfigurationNodePointer<ConfigurationNode>(pointer,
                root.getAttribute(0), handler);
        assertEquals("Incorrect order for attributes", -1, pointer
                .compareChildNodePointers(p1, p2));
        assertEquals("Incorrect symmetric order for attributes", 1, pointer
                .compareChildNodePointers(p2, p1));
    }

    /**
     * Tests comparing child node pointers for child nodes that do not belong to
     * the parent node.
     */
    public void testCompareChildNodePointersInvalidChildren()
    {
        ConfigurationNode node = root.getChild(1);
        NodePointer p1 = new ConfigurationNodePointer<ConfigurationNode>(pointer,
                node.getChild(1), handler);
        NodePointer p2 = new ConfigurationNodePointer<ConfigurationNode>(pointer,
                node.getChild(3), handler);
        assertEquals("Non child nodes could be sorted", 0, pointer
                .compareChildNodePointers(p1, p2));
        assertEquals("Non child nodes could be sorted symmetrically", 0,
                pointer.compareChildNodePointers(p2, p1));
    }

    /**
     * Tests the attribute flag. Node pointers of this type never represent
     * attributes. So the result should always be false.
     */
    public void testIsAttribute()
    {
        ConfigurationNode node = new DefaultConfigurationNode("test", "testval");
        NodePointer p = new ConfigurationNodePointer<ConfigurationNode>(pointer, node, handler);
        assertFalse("Node is an attribute", p.isAttribute());
        node.setAttribute(true);
        assertFalse("Node is now an attribute", p.isAttribute());
    }

    /**
     * Tests isLeaf() for a node with attributes.
     */
    public void testIsLeafAttributes()
    {
        ConfigurationNode node = new DefaultConfigurationNode("test");
        node.addAttribute(new DefaultConfigurationNode("attr", "test"));
        NodePointer p = new ConfigurationNodePointer<ConfigurationNode>(pointer, node, handler);
        assertFalse("Node is a leaf", p.isLeaf());
    }

    /**
     * Tests isLeaf() for a node with children.
     */
    public void testIsLeafChildren()
    {
        ConfigurationNode node = new DefaultConfigurationNode("test");
        node.addChild(new DefaultConfigurationNode("child", "test"));
        NodePointer p = new ConfigurationNodePointer<ConfigurationNode>(pointer, node, handler);
        assertFalse("Node is a leaf", p.isLeaf());
    }

    /**
     * Tests isLeaf() for a real leaf.
     */
    public void testIsLeafTrue()
    {
        ConfigurationNode node = new DefaultConfigurationNode("test");
        NodePointer p = new ConfigurationNodePointer<ConfigurationNode>(pointer, node, handler);
        assertTrue("Node is no leaf", p.isLeaf());
    }

    /**
     * Tests the iterators returned by the node pointer.
     */
    public void testIterators()
    {
        checkIterators(pointer);
    }

    /**
     * Recursive helper method for testing the returned iterators.
     *
     * @param p the node pointer to test
     */
    private void checkIterators(NodePointer p)
    {
        ConfigurationNode node = (ConfigurationNode) p.getNode();
        NodeIterator it = p.childIterator(null, false, null);
        assertEquals("Iterator count differs from children count", node
                .getChildrenCount(), iteratorSize(it));

        for (int index = 1; it.setPosition(index); index++)
        {
            NodePointer pchild = it.getNodePointer();
            assertEquals("Wrong child", node.getChild(index - 1), pchild
                    .getNode());
            checkIterators(pchild);
        }

        it = p.attributeIterator(new QName(null, "*"));
        assertEquals("Iterator count differs from attribute count", node
                .getAttributeCount(), iteratorSize(it));
        for (int index = 1; it.setPosition(index); index++)
        {
            NodePointer pattr = it.getNodePointer();
            assertTrue("Node pointer is no attribute", pattr.isAttribute());
            assertEquals("Wrong attribute", node.getAttribute(index - 1).getValue(),
                    pattr.getValue());
        }
    }
}
