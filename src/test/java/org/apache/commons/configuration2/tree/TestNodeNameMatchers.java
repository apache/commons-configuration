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
package org.apache.commons.configuration2.tree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code NodeNameMatchers}.
 *
 * @version $Id$
 */
public class TestNodeNameMatchers
{
    /** Constant for a test node name. */
    private static final String NODE_NAME = "TestNodeName";

    /** A node handler. */
    private NodeHandler<ImmutableNode> handler;

    @Before
    public void setUp() throws Exception
    {
        InMemoryNodeModel model = new InMemoryNodeModel();
        handler = model.getNodeHandler();
    }

    /**
     * Creates a node with the given name.
     *
     * @param name the name
     * @return the newly created node
     */
    private static ImmutableNode createNode(String name)
    {
        return new ImmutableNode.Builder().name(name).create();
    }

    /**
     * Tests the equals matcher if the expected result is true.
     */
    @Test
    public void testEqualsMatch()
    {
        ImmutableNode node = createNode(NODE_NAME);
        assertTrue("No match",
                NodeNameMatchers.EQUALS.matches(node, handler, NODE_NAME));
    }

    /**
     * Tests the equals matcher for a non matching name.
     */
    @Test
    public void testEqualsNoMatch()
    {
        ImmutableNode node = createNode(NODE_NAME);
        assertFalse(
                "Match (1)",
                NodeNameMatchers.EQUALS.matches(node, handler, NODE_NAME
                        + "_other"));
        assertFalse(
                "Match (2)",
                NodeNameMatchers.EQUALS.matches(node, handler,
                        NODE_NAME.toLowerCase(Locale.ENGLISH)));
    }

    /**
     * Tests whether the equals matcher can handle a null criterion.
     */
    @Test
    public void testEqualsNullCriterion()
    {
        ImmutableNode node = createNode(NODE_NAME);
        assertFalse("Match (1)",
                NodeNameMatchers.EQUALS.matches(node, handler, null));
        assertFalse("Match (2)", NodeNameMatchers.EQUALS.matches(
                createNode(null), handler, NODE_NAME));
    }
}
