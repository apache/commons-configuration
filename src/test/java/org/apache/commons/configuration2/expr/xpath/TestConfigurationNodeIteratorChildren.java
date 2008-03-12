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

import java.util.List;
import java.util.Locale;

import org.apache.commons.configuration2.expr.ConfigurationNodeHandler;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * Test class for ConfigurationNodeIteratorChildren.
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class TestConfigurationNodeIteratorChildren extends AbstractXPathTest
{
    /** Stores the node pointer to the root node. */
    private ConfigurationNodePointer<ConfigurationNode> rootPointer;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        rootPointer = new ConfigurationNodePointer<ConfigurationNode>(root,
                new ConfigurationNodeHandler(), Locale.getDefault());
    }

    /**
     * Tests to iterate over all children of the root node.
     */
    public void testIterateAllChildren()
    {
        ConfigurationNodeIteratorChildren<ConfigurationNode> it =
            new ConfigurationNodeIteratorChildren<ConfigurationNode>(rootPointer, null, false, null);
        assertEquals("Wrong number of elements", CHILD_COUNT, iteratorSize(it));
        checkValues(it, new int[] { 1, 2, 3, 4, 5 });
    }

    /**
     * Tests a reverse iteration.
     */
    public void testIterateReverse()
    {
        ConfigurationNodeIteratorChildren<ConfigurationNode> it =
            new ConfigurationNodeIteratorChildren<ConfigurationNode>(rootPointer, null, true, null);
        assertEquals("Wrong number of elements", CHILD_COUNT, iteratorSize(it));
        checkValues(it, new int[] { 5, 4, 3, 2, 1 });
    }

    /**
     * Tests using a node test with a wildcard name.
     */
    public void testIterateWithWildcardTest()
    {
        NodeNameTest test = new NodeNameTest(new QName(null, "*"));
        ConfigurationNodeIteratorChildren<ConfigurationNode> it =
            new ConfigurationNodeIteratorChildren<ConfigurationNode>(rootPointer, test, false, null);
        assertEquals("Wrong number of elements", CHILD_COUNT, iteratorSize(it));
    }

    /**
     * Tests using a node test that defines a namespace prefix. Because
     * namespaces are not supported, no elements should be in the iteration.
     */
    public void testIterateWithPrefixTest()
    {
        NodeNameTest test = new NodeNameTest(new QName("prefix", "*"));
        ConfigurationNodeIteratorChildren<ConfigurationNode> it =
            new ConfigurationNodeIteratorChildren<ConfigurationNode>(rootPointer, test, false, null);
        assertNull("Undefined node pointer not returned", it.getNodePointer());
        assertEquals("Prefix was not evaluated", 0, iteratorSize(it));
    }

    /**
     * Tests using a node test that selects a certain sub node name.
     */
    public void testIterateWithNameTest()
    {
        NodeNameTest test = new NodeNameTest(new QName(null, CHILD_NAME2));
        ConfigurationNodeIteratorChildren<ConfigurationNode> it =
            new ConfigurationNodeIteratorChildren<ConfigurationNode>(rootPointer, test, false, null);
        assertTrue("No children found", iteratorSize(it) > 0);
        for (NodePointer np : iterationElements(it))
        {
            assertEquals("Wrong child element", CHILD_NAME2, np.getName().getName());
        }
    }

    /**
     * Tests using a not supported test class. This should yield an empty
     * iteration.
     */
    public void testIterateWithUnknownTest()
    {
        NodeTest test = new ProcessingInstructionTest("test");
        ConfigurationNodeIteratorChildren<ConfigurationNode> it =
            new ConfigurationNodeIteratorChildren<ConfigurationNode>(rootPointer, test, false, null);
        assertEquals("Unknown test was not evaluated", 0, iteratorSize(it));
    }

    /**
     * Tests using a type test for nodes. This should return all nodes.
     */
    public void testIterateWithNodeType()
    {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_NODE);
        ConfigurationNodeIteratorChildren<ConfigurationNode> it =
            new ConfigurationNodeIteratorChildren<ConfigurationNode>(rootPointer, test, false, null);
        assertEquals("Node type not evaluated", CHILD_COUNT, iteratorSize(it));
    }

    /**
     * Tests using a type test for a non supported type. This should return an
     * empty iteration.
     */
    public void testIterateWithUnknownType()
    {
        NodeTypeTest test = new NodeTypeTest(Compiler.NODE_TYPE_COMMENT);
        ConfigurationNodeIteratorChildren<ConfigurationNode> it =
            new ConfigurationNodeIteratorChildren<ConfigurationNode>(rootPointer, test, false, null);
        assertEquals("Unknown node type not evaluated", 0, iteratorSize(it));
    }

    /**
     * Tests defining a start node for the iteration.
     */
    public void testIterateStartsWith()
    {
        ConfigurationNodePointer<ConfigurationNode> childPointer =
            new ConfigurationNodePointer<ConfigurationNode>(rootPointer,
                root.getChild(2), new ConfigurationNodeHandler());
        ConfigurationNodeIteratorChildren<ConfigurationNode> it =
            new ConfigurationNodeIteratorChildren<ConfigurationNode>(rootPointer, null, false, childPointer);
        assertEquals("Wrong start position", 0, it.getPosition());
        List<NodePointer> nodes = iterationElements(it);
        assertEquals("Wrong size of iteration", CHILD_COUNT - 3, nodes.size());
        int index = 4;
        for (NodePointer np : nodes)
        {
            ConfigurationNode node = (ConfigurationNode) np.getImmediateNode();
            assertEquals("Wrong node value", String.valueOf(index++), node
                    .getValue());
        }
    }

    /**
     * Tests defining a start node for a reverse iteration.
     */
    public void testIterateStartsWithReverse()
    {
        ConfigurationNodePointer<ConfigurationNode> childPointer =
            new ConfigurationNodePointer<ConfigurationNode>(rootPointer,
                root.getChild(3), new ConfigurationNodeHandler());
        ConfigurationNodeIteratorChildren<ConfigurationNode> it =
            new ConfigurationNodeIteratorChildren<ConfigurationNode>(rootPointer, null, true, childPointer);
        int value = 3;
        for (int index = 1; it.setPosition(index); index++, value--)
        {
            ConfigurationNode node = (ConfigurationNode) it.getNodePointer()
                    .getNode();
            assertEquals("Incorrect value at index " + index, String
                    .valueOf(value), node.getValue());
        }
        assertEquals("Iteration ended not at end node", 0, value);
    }

    /**
     * Tests iteration with an invalid start node. This should cause the
     * iteration to start at the first position.
     */
    public void testIterateStartsWithInvalid()
    {
        ConfigurationNodePointer<ConfigurationNode> childPointer =
            new ConfigurationNodePointer<ConfigurationNode>(rootPointer,
                new DefaultConfigurationNode("newNode"), new ConfigurationNodeHandler());
        ConfigurationNodeIteratorChildren<ConfigurationNode> it =
            new ConfigurationNodeIteratorChildren<ConfigurationNode>(rootPointer, null, false, childPointer);
        assertEquals("Wrong size of iteration", CHILD_COUNT, iteratorSize(it));
        it.setPosition(1);
        ConfigurationNode node = (ConfigurationNode) it.getNodePointer()
                .getNode();
        assertEquals("Wrong start node", "1", node.getValue());
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
    private void checkValues(NodeIterator iterator, int[] expectedIndices)
    {
        List<NodePointer> nodes = iterationElements(iterator);
        for (int i = 0; i < expectedIndices.length; i++)
        {
            ConfigurationNode child = (ConfigurationNode) nodes.get(i).getImmediateNode();
            assertTrue("Wrong index value for child " + i, child.getValue()
                    .toString().endsWith(String.valueOf(expectedIndices[i])));
        }
    }
}
