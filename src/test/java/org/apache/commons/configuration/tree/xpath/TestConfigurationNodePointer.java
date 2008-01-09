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
package org.apache.commons.configuration.tree.xpath;

import java.util.Locale;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
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
    NodePointer pointer;

    protected void setUp() throws Exception
    {
        super.setUp();
        pointer = new ConfigurationNodePointer(root, Locale.getDefault());
    }

    /**
     * Tests comparing child node pointers for child nodes.
     */
    public void testCompareChildNodePointersChildren()
    {
        NodePointer p1 = new ConfigurationNodePointer(pointer, root.getChild(1));
        NodePointer p2 = new ConfigurationNodePointer(pointer, root.getChild(3));
        assertEquals("Incorrect order", -1, pointer.compareChildNodePointers(
                p1, p2));
        assertEquals("Incorrect symmetric order", 1, pointer
                .compareChildNodePointers(p2, p1));
    }

    /**
     * Tests comparing child node pointers for attribute nodes.
     */
    public void testCompareChildNodePointersAttributes()
    {
        root.addAttribute(new DefaultConfigurationNode("attr1", "test1"));
        root.addAttribute(new DefaultConfigurationNode("attr2", "test2"));
        NodePointer p1 = new ConfigurationNodePointer(pointer, root
                .getAttribute(0));
        NodePointer p2 = new ConfigurationNodePointer(pointer, root
                .getAttribute(1));
        assertEquals("Incorrect order", -1, pointer.compareChildNodePointers(
                p1, p2));
        assertEquals("Incorrect symmetric order", 1, pointer
                .compareChildNodePointers(p2, p1));
    }

    /**
     * tests comparing child node pointers for both child and attribute nodes.
     */
    public void testCompareChildNodePointersChildAndAttribute()
    {
        root.addAttribute(new DefaultConfigurationNode("attr1", "test1"));
        NodePointer p1 = new ConfigurationNodePointer(pointer, root.getChild(2));
        NodePointer p2 = new ConfigurationNodePointer(pointer, root
                .getAttribute(0));
        assertEquals("Incorrect order for attributes", 1, pointer
                .compareChildNodePointers(p1, p2));
        assertEquals("Incorrect symmetric order for attributes", -1, pointer
                .compareChildNodePointers(p2, p1));
    }

    /**
     * Tests comparing child node pointers for child nodes that do not belong to
     * the parent node.
     */
    public void testCompareChildNodePointersInvalidChildren()
    {
        ConfigurationNode node = root.getChild(1);
        NodePointer p1 = new ConfigurationNodePointer(pointer, node.getChild(1));
        NodePointer p2 = new ConfigurationNodePointer(pointer, node.getChild(3));
        assertEquals("Non child nodes could be sorted", 0, pointer
                .compareChildNodePointers(p1, p2));
        assertEquals("Non child nodes could be sorted symmetrically", 0,
                pointer.compareChildNodePointers(p2, p1));
    }

    /**
     * Tests the attribute flag.
     */
    public void testIsAttribute()
    {
        ConfigurationNode node = new DefaultConfigurationNode("test", "testval");
        NodePointer p = new ConfigurationNodePointer(pointer, node);
        assertFalse("Node is an attribute", p.isAttribute());
        node.setAttribute(true);
        assertTrue("Node is no attribute", p.isAttribute());
    }

    /**
     * Tests if leaves in the tree are correctly detected.
     */
    public void testIsLeave()
    {
        assertFalse("Root node is leaf", pointer.isLeaf());

        NodePointer p = pointer;
        while (!p.isLeaf())
        {
            ConfigurationNode node = (ConfigurationNode) p.getNode();
            assertTrue("Node has no children", node.getChildrenCount() > 0);
            p = new ConfigurationNodePointer(p, node.getChild(0));
        }
        assertTrue("Node has children", ((ConfigurationNode) p.getNode())
                .getChildrenCount() == 0);
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
            assertEquals("Wrong attribute", node.getAttribute(index - 1), pattr
                    .getNode());
            checkIterators(pattr);
        }
    }
}
