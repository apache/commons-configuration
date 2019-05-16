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

import static org.junit.Assert.assertSame;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@code TrackedNodeHandler}.
 *
 */
public class TestTrackedNodeHandler
{
    /** A test root node. */
    private static ImmutableNode root;

    /** A mock node handler. */
    private NodeHandler<ImmutableNode> parentHandler;

    /** The handler to be tested. */
    private TrackedNodeHandler handler;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        root = new ImmutableNode.Builder().name("ROOT").create();
    }

    @Before
    public void setUp() throws Exception
    {
        @SuppressWarnings("unchecked")
        final
        NodeHandler<ImmutableNode> h = EasyMock.createMock(NodeHandler.class);
        parentHandler = h;
        handler = new TrackedNodeHandler(root, parentHandler);
    }

    /**
     * Tests whether the correct root node is returned.
     */
    @Test
    public void testGetRootNode()
    {
        assertSame("Wrong root node", root, handler.getRootNode());
    }

    /**
     * Tests whether a parent node can be queried.
     */
    @Test
    public void testGetParent()
    {
        final ImmutableNode node = new ImmutableNode.Builder().name("node").create();
        final ImmutableNode parent = new ImmutableNode.Builder().name("parent").create();
        EasyMock.expect(parentHandler.getParent(node)).andReturn(parent);
        EasyMock.replay(parentHandler);

        assertSame("Wrong parent node", parent, handler.getParent(node));
        EasyMock.verify(parentHandler);
    }
}
