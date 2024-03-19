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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code TrackedNodeHandler}.
 */
public class TestTrackedNodeHandler {
    /** A test root node. */
    private static ImmutableNode root;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        root = new ImmutableNode.Builder().name("ROOT").create();
    }

    /** A mock node handler. */
    private NodeHandler<ImmutableNode> parentHandler;

    /** The handler to be tested. */
    private TrackedNodeHandler handler;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        parentHandler = mock(NodeHandler.class);
        handler = new TrackedNodeHandler(root, parentHandler);
    }

    /**
     * Tests whether a parent node can be queried.
     */
    @Test
    public void testGetParent() {
        final ImmutableNode node = new ImmutableNode.Builder().name("node").create();
        final ImmutableNode parent = new ImmutableNode.Builder().name("parent").create();

        when(parentHandler.getParent(node)).thenReturn(parent);

        assertSame(parent, handler.getParent(node));

        verify(parentHandler).getParent(node);
        verifyNoMoreInteractions(parentHandler);
    }

    /**
     * Tests whether the correct root node is returned.
     */
    @Test
    public void testGetRootNode() {
        assertSame(root, handler.getRootNode());
    }
}
