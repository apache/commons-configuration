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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;

import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.NodeStructureHelper;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for ConfigurationNodeIteratorChildren.
 *
 */
public class TestConfigurationNodeIteratorChildren extends AbstractXPathTest
{
    /** Constant for a namespace prefix. */
    private static final String PREFIX = "commons";

    /** Constant for the name of a node with a namespace. */
    private static final String PREFIX_NODE = "configuration";

    /** Stores the node pointer to the root node. */
    private ConfigurationNodePointer<ImmutableNode> rootPointer;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        rootPointer = createPointer(root);
    }

    /**
     * Helper method for creating a node pointer for a given node.
     *
     * @param node the node the pointer points to
     * @return the node pointer
     */
    private ConfigurationNodePointer<ImmutableNode> createPointer(
            final ImmutableNode node)
    {
        return new ConfigurationNodePointer<>(node,
                Locale.getDefault(), handler);
    }

    /**
     * Tests to iterate over all children of the root node.
     */
    @Test
    public void testIterateAllChildren()
    {
        final ConfigurationNodeIteratorChildren<ImmutableNode> it =
                new ConfigurationNodeIteratorChildren<>(
                        rootPointer, null, false, null);
        assertEquals("Wrong number of elements", CHILD_COUNT, iteratorSize(it));
        checkValues(it, 1, 2, 3, 4, 5);
    }

    /**
     * Tests a reverse iteration.
     */
    @Test
    public void testIterateReverse()
    {
        final ConfigurationNodeIteratorChildren<ImmutableNode> it =
                new ConfigurationNodeIteratorChildren<>(
                        rootPointer, null, true, null);
        assertEquals("Wrong number of elements", CHILD_COUNT, iteratorSize(it));
        checkValues(it, 5, 4, 3, 2, 1);
    }

    /**
     * Tests using a node test with a wildcard name.
     */
    @Test
    public void testIterateWithWildcardTest()
    {
        final NodeNameTest test = new NodeNameTest(new QName(null, "*"));
        final ConfigurationNodeIteratorChildren<ImmutableNode> it =
                new ConfigurationNodeIteratorChildren<>(
                        rootPointer, test, false, null);
        assertEquals("Wrong number of elements", CHILD_COUNT, iteratorSize(it));
    }

    /**
     * Tests using a node test that defines a namespace prefix. Because
     * namespaces are not supported, no elements should be in the iteration.
     */
    @Test
    public void testIterateWithPrefixTest()
    {
        final NodeNameTest test = new NodeNameTest(new QName("prefix", "*"));
        final ConfigurationNodeIteratorChildren<ImmutableNode> it =
                new ConfigurationNodeIteratorChildren<>(
                        rootPointer, test, false, null);
        assertNull("Undefined node pointer not returned", it.getNodePointer());
        assertEquals("Prefix was not evaluated", 0, iteratorSize(it));
    }

    /**
     * Tests using a node test that selects a certain sub node name.
     */
    @Test
    public void testIterateWithNameTest()
    {
        final NodeNameTest test = new NodeNameTest(new QName(null, CHILD_NAME2));
        final ConfigurationNodeIteratorChildren<ImmutableNode> it =
                new ConfigurationNodeIteratorChildren<>(
                        rootPointer, test, false, null);
        assertTrue("No children found", iteratorSize(it) > 0);
        for (final NodePointer nd : iterationElements(it))
        {
            assertEquals("Wrong child element", CHILD_NAME2, nd.getName().getName());
        }
    }

    /**
     * Tests using a not supported test class. This should yield an empty
     * iteration.
     */
    @Test
    public void testIterateWithUnknownTest()
    {
        final NodeTest test = new ProcessingInstructionTest("test");
        final ConfigurationNodeIteratorChildren<ImmutableNode> it =
                new ConfigurationNodeIteratorChildren<>(
                        rootPointer, test, false, null);
        assertEquals("Unknown test was not evaluated", 0, iteratorSize(it));
    }

    /**
     * Tests using a type test for nodes. This should return all nodes.
     */
    @Test
    public void testIterateWithNodeType()
    {
        final NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        final ConfigurationNodeIteratorChildren<ImmutableNode> it =
                new ConfigurationNodeIteratorChildren<>(
                        rootPointer, test, false, null);
        assertEquals("Node type not evaluated", CHILD_COUNT, iteratorSize(it));
    }

    /**
     * Tests using a type test for a non supported type. This should return an
     * empty iteration.
     */
    @Test
    public void testIterateWithUnknownType()
    {
        final NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        final ConfigurationNodeIteratorChildren<ImmutableNode> it =
                new ConfigurationNodeIteratorChildren<>(
                        rootPointer, test, false, null);
        assertEquals("Unknown node type not evaluated", 0, iteratorSize(it));
    }

    /**
     * Tests defining a start node for the iteration.
     */
    @Test
    public void testIterateStartsWith()
    {
        final ConfigurationNodePointer<ImmutableNode> childPointer =
                new ConfigurationNodePointer<>(rootPointer, root
                        .getChildren().get(2), handler);
        final ConfigurationNodeIteratorChildren<ImmutableNode> it =
                new ConfigurationNodeIteratorChildren<>(
                        rootPointer, null, false, childPointer);
        assertEquals("Wrong start position", 0, it.getPosition());
        final List<NodePointer> nodes = iterationElements(it);
        assertEquals("Wrong size of iteration", CHILD_COUNT - 3, nodes.size());
        int index = 4;
        for (final NodePointer np : nodes)
        {
            final ImmutableNode node = (ImmutableNode) np.getImmediateNode();
            assertEquals("Wrong node value", String.valueOf(index),
                    node.getValue());
            index++;
        }
    }

    /**
     * Tests defining a start node for a reverse iteration.
     */
    @Test
    public void testIterateStartsWithReverse()
    {
        final ConfigurationNodePointer<ImmutableNode> childPointer =
                new ConfigurationNodePointer<>(rootPointer, root
                        .getChildren().get(3), handler);
        final ConfigurationNodeIteratorChildren<ImmutableNode> it =
                new ConfigurationNodeIteratorChildren<>(
                        rootPointer, null, true, childPointer);
        int value = 3;
        for (int index = 1; it.setPosition(index); index++, value--)
        {
            final ImmutableNode node = (ImmutableNode) it.getNodePointer().getNode();
            assertEquals("Incorrect value at index " + index,
                    String.valueOf(value), node.getValue());
        }
        assertEquals("Iteration ended not at end node", 0, value);
    }

    /**
     * Tests iteration with an invalid start node. This should cause the
     * iteration to start at the first position.
     */
    @Test
    public void testIterateStartsWithInvalid()
    {
        final ConfigurationNodePointer<ImmutableNode> childPointer =
                new ConfigurationNodePointer<>(rootPointer,
                        new ImmutableNode.Builder().name("newNode").create(),
                        handler);
        final ConfigurationNodeIteratorChildren<ImmutableNode> it =
                new ConfigurationNodeIteratorChildren<>(
                        rootPointer, null, false, childPointer);
        assertEquals("Wrong size of iteration", CHILD_COUNT, iteratorSize(it));
        it.setPosition(1);
        final ImmutableNode node = (ImmutableNode) it.getNodePointer().getNode();
        assertEquals("Wrong start node", "1", node.getValue());
    }

    /**
     * Creates a node pointer to a node which also contains a child node with a
     * namespace prefix.
     *
     * @return the node pointer
     */
    private ConfigurationNodePointer<ImmutableNode> createPointerWithNamespace()
    {
        final ImmutableNode node =
                new ImmutableNode.Builder(2)
                        .addChild(root)
                        .addChild(
                                NodeStructureHelper.createNode(PREFIX + ':'
                                        + PREFIX_NODE, "test")
                        ).create();
        return createPointer(node);
    }

    /**
     * Tests whether all nodes with a specific prefix can be obtained.
     */
    @Test
    public void testIterateWithWildcardTestPrefix()
    {
        final NodeNameTest test = new NodeNameTest(new QName(PREFIX, "*"));
        final ConfigurationNodeIteratorChildren<ImmutableNode> it =
                new ConfigurationNodeIteratorChildren<>(
                        createPointerWithNamespace(), test, false, null);
        assertEquals("Wrong number of elements", 1, iteratorSize(it));
        for (final NodePointer p : iterationElements(it))
        {
            assertEquals("Wrong element", PREFIX + ':' + PREFIX_NODE, p
                    .getName().getName());
        }
    }

    /**
     * Tests whether nodes with a matching namespace prefix can be obtained.
     */
    @Test
    public void testIterateWithMatchingPrefixTest()
    {
        final NodeNameTest test = new NodeNameTest(new QName(PREFIX, PREFIX_NODE));
        final ConfigurationNodeIteratorChildren<ImmutableNode> it =
                new ConfigurationNodeIteratorChildren<>(
                        createPointerWithNamespace(), test, false, null);
        assertEquals("Wrong number of elements", 1, iteratorSize(it));
        for (final NodePointer p : iterationElements(it))
        {
            assertEquals("Wrong element", PREFIX + ':' + PREFIX_NODE, p
                    .getName().getName());
        }
    }

    /**
     * Helper method for checking the values of the nodes returned by an
     * iterator. Because the values indicate the order of the child nodes with
     * this test it can be checked whether the nodes were returned in the
     * correct order.
     *
     * @param iterator the iterator
     * @param expectedIndices an array with the expected indices
     */
    private void checkValues(final NodeIterator iterator, final int... expectedIndices)
    {
        final List<NodePointer> nodes = iterationElements(iterator);
        for (int i = 0; i < expectedIndices.length; i++)
        {
            final ImmutableNode child = (ImmutableNode) nodes.get(i).getImmediateNode();
            assertTrue("Wrong index value for child " + i, child.getValue()
                    .toString().endsWith(String.valueOf(expectedIndices[i])));
        }
    }
}
