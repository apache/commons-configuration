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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Test class for {@code ImmutableNode}
 *
 * @version $Id$
 */
public class TestImmutableNode
{
    /** Constant for a test node name. */
    public static final String NAME = "testNode";

    /** Constant for a test node value. */
    public static final Integer VALUE = 42;

    /**
     * Sets up a builder with default settings.
     *
     * @return the default builder
     */
    private static ImmutableNode.Builder setUpBuilder()
    {
        ImmutableNode.Builder builder = new ImmutableNode.Builder();
        builder.name(NAME).value(VALUE);
        return builder;
    }

    /**
     * Tests whether a node with basic properties can be created.
     */
    @Test
    public void testSimpleProperties()
    {
        ImmutableNode node = setUpBuilder().create();
        assertEquals("Wrong node name", NAME, node.getNodeName());
        assertTrue("Got children", node.getChildren().isEmpty());
        assertTrue("Got attributes", node.getAttributes().isEmpty());
    }

    /**
     * Tests that a node's children cannot be manipulated.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testChildrenImmutable()
    {
        ImmutableNode node = setUpBuilder().create();
        node.getChildren().add(null);
    }

    /**
     * Tests that a node's attributes cannot be directly manipulated.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testAttributesImmutable()
    {
        ImmutableNode node = setUpBuilder().create();
        node.getAttributes().put("test", VALUE);
    }

    /**
     * Checks whether a node has the expected children.
     *
     * @param node the node to be checked
     * @param expChildren the collection with the expected children
     */
    private static void checkChildNodes(ImmutableNode node,
            Collection<ImmutableNode> expChildren)
    {
        assertEquals("Wrong number of child nodes", expChildren.size(), node
                .getChildren().size());
        assertTrue("Wrong children", node.getChildren()
                .containsAll(expChildren));
    }

    /**
     * Tests whether child nodes can be added.
     */
    @Test
    public void testNodeWithChildren()
    {
        Set<ImmutableNode> childNodes = new HashSet<ImmutableNode>();
        final int childCount = 8;
        ImmutableNode.Builder builder = new ImmutableNode.Builder(childCount);
        for (int i = 0; i < childCount; i++)
        {
            ImmutableNode.Builder childBuilder = new ImmutableNode.Builder();
            ImmutableNode child = childBuilder.name(NAME + i).value(i).create();
            builder.addChild(child);
            childNodes.add(child);
        }
        ImmutableNode node = builder.name(NAME).create();
        checkChildNodes(node, childNodes);
    }

    /**
     * Tests whether multiple child nodes can be added to a builder.
     */
    @Test
    public void testNodeWithAddMultipleChildren()
    {
        final int childCount = 4;
        List<ImmutableNode> childNodes =
                new ArrayList<ImmutableNode>(childCount);
        for (int i = 0; i < childCount; i++)
        {
            ImmutableNode.Builder childBuilder = new ImmutableNode.Builder();
            ImmutableNode child = childBuilder.name(NAME + i).value(i).create();
            childNodes.add(child);
        }
        ImmutableNode.Builder builder = setUpBuilder();
        ImmutableNode node = builder.addChildren(childNodes).create();
        checkChildNodes(node, childNodes);
    }

    /**
     * Tests that the list of children cannot be changed by a later manipulation
     * of the builder.
     */
    @Test
    public void testNodeWithChildrenManipulateLater()
    {
        ImmutableNode.Builder builder = setUpBuilder();
        ImmutableNode child =
                new ImmutableNode.Builder().name("Child").create();
        ImmutableNode node = builder.addChild(child).create();
        builder.addChild(new ImmutableNode.Builder().name("AnotherChild")
                .create());
        checkChildNodes(node, Collections.singleton(child));
    }

    /**
     * Tests whether addChildren() can deal with null input.
     */
    @Test
    public void testAddChildrenNull()
    {
        ImmutableNode.Builder builder = setUpBuilder();
        builder.addChildren(null);
        ImmutableNode node = builder.create();
        assertTrue("Got children", node.getChildren().isEmpty());
    }

    /**
     * Tests whether a node with attributes can be created.
     */
    @Test
    public void testNodeWithAttributes()
    {
        ImmutableNode.Builder builder = setUpBuilder();
        final int attrCount = 4;
        Map<String, Object> attrs = new HashMap<String, Object>();
        for (int i = 0; i < attrCount; i++)
        {
            String attrName = NAME + i;
            attrs.put(attrName, i);
            builder.addAttribute(attrName, i);
        }
        ImmutableNode node = builder.create();
        checkAttributes(node, attrs);
    }

    /**
     * Checks whether a node has the expected attributes.
     *
     * @param node the node to be checked
     * @param expAttrs the expected attributes
     */
    private static void checkAttributes(ImmutableNode node,
            Map<String, Object> expAttrs)
    {
        assertEquals("Wrong number of attributes", expAttrs.size(), node
                .getAttributes().size());
        for (Map.Entry<String, Object> e : expAttrs.entrySet())
        {
            assertEquals("Wrong value for " + e.getKey(), e.getValue(), node
                    .getAttributes().get(e.getKey()));
        }
    }

    /**
     * Tests that the map of attributes cannot be changed by a later
     * manipulation of the builder.
     */
    @Test
    public void testNodeWithAttributesManipulateLater()
    {
        ImmutableNode.Builder builder = setUpBuilder();
        builder.addAttribute("attr", "a1");
        ImmutableNode node = builder.create();
        builder.addAttribute("attr2", "a2");
        assertEquals("Wrong number of attributes", 1, node.getAttributes()
                .size());
        assertEquals("Wrong attribute", "a1", node.getAttributes().get("attr"));
    }

    /**
     * Tests whether multiple attributes can be added in a single operation.
     */
    @Test
    public void testNodeWithMultipleAttributes()
    {
        ImmutableNode.Builder builder = setUpBuilder();
        final int attrCount = 4;
        Map<String, Object> attrs = new HashMap<String, Object>();
        for (int i = 0; i < attrCount; i++)
        {
            String attrName = NAME + i;
            attrs.put(attrName, i);
        }
        ImmutableNode node = builder.addAttributes(attrs).create();
        checkAttributes(node, attrs);
    }

    /**
     * Tests whether addAttributes() handles null input.
     */
    @Test
    public void testAddAttributesNull()
    {
        ImmutableNode.Builder builder = setUpBuilder();
        builder.addAttributes(null);
        ImmutableNode node = builder.create();
        assertTrue("Got attributes", node.getAttributes().isEmpty());
    }
}
