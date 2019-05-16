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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code ConfigurationNodePointer}.
 *
 */
public class TestConfigurationNodePointer extends AbstractXPathTest
{
    /** Stores the node pointer to be tested. */
    private ConfigurationNodePointer<ImmutableNode> pointer;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        pointer =
                new ConfigurationNodePointer<>(root,
                        Locale.getDefault(), handler);
    }

    /**
     * Tests comparing child node pointers for child nodes.
     */
    @Test
    public void testCompareChildNodePointersChildren()
    {
        final NodePointer p1 = new ConfigurationNodePointer<>(
                pointer, root.getChildren().get(1), handler);
        final NodePointer p2 = new ConfigurationNodePointer<>(
                pointer, root.getChildren().get(3), handler);
        assertEquals("Incorrect order", -1, pointer.compareChildNodePointers(
                p1, p2));
        assertEquals("Incorrect symmetric order", 1, pointer
                .compareChildNodePointers(p2, p1));
    }

    /**
     * Tests whether a comparison of child node pointers handle the case that
     * the child nodes are unknown. (This should not happen in practice.)
     */
    @Test
    public void testCompareChildNodePointersAttributes()
    {
        final ImmutableNode n1 = new ImmutableNode.Builder().name("n1").create();
        final ImmutableNode n2 = new ImmutableNode.Builder().name("n2").create();
        final NodePointer p1 =
                new ConfigurationNodePointer<>(pointer, n1,
                        handler);
        final NodePointer p2 =
                new ConfigurationNodePointer<>(pointer, n2,
                        handler);
        assertEquals("Incorrect order", 0,
                pointer.compareChildNodePointers(p1, p2));
        assertEquals("Incorrect symmetric order", 0,
                pointer.compareChildNodePointers(p2, p1));
    }

    /**
     * Tests the attribute flag.
     */
    @Test
    public void testIsAttribute()
    {
        assertFalse("Node is an attribute", pointer.isAttribute());
    }

    /**
     * Tests if leaves in the tree are correctly detected.
     */
    @Test
    public void testIsLeave()
    {
        assertFalse("Root node is leaf", pointer.isLeaf());
    }

    /**
     * Tests the leaf flag for a real leaf node.
     */
    @Test
    public void testIsLeafTrue()
    {
        final ImmutableNode leafNode =
                new ImmutableNode.Builder().name("leafNode").create();
        pointer =
                new ConfigurationNodePointer<>(pointer, leafNode,
                        handler);
        assertTrue("Not a leaf node", pointer.isLeaf());
    }

    /**
     * Tests the iterators returned by the node pointer.
     */
    @Test
    public void testIterators()
    {
        checkIterators(pointer);
    }

    /**
     * Recursive helper method for testing the returned iterators.
     *
     * @param p the node pointer to test
     */
    private void checkIterators(final NodePointer p)
    {
        final ImmutableNode node = (ImmutableNode) p.getNode();
        NodeIterator it = p.childIterator(null, false, null);
        assertEquals("Iterator count differs from children count", node
                .getChildren().size(), iteratorSize(it));

        for (int index = 1; it.setPosition(index); index++)
        {
            final NodePointer pchild = it.getNodePointer();
            assertEquals("Wrong child", node.getChildren().get(index - 1),
                    pchild.getNode());
            checkIterators(pchild);
        }

        it = p.attributeIterator(new QName(null, "*"));
        assertEquals("Iterator count differs from attribute count", node
                .getAttributes().size(), iteratorSize(it));
        for (int index = 1; it.setPosition(index); index++)
        {
            final NodePointer pattr = it.getNodePointer();
            assertTrue("Node pointer is no attribute", pattr.isAttribute());
            assertTrue("Wrong attribute name", node.getAttributes()
                    .containsKey(pattr.getName().getName()));
        }
    }

    /**
     * Tests that no new value can be set.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetValue()
    {
        pointer.setValue("newValue");
    }
}
