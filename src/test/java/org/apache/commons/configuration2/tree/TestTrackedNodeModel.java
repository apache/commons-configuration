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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code TrackedNodeModel}.
 */
public class TestTrackedNodeModel {
    /** Constant for a test key. */
    private static final String KEY = "aTestKey";

    /** A test node selector. */
    private static NodeSelector selector;

    /** A mock resolver. */
    private static NodeKeyResolver<ImmutableNode> resolver;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        selector = new NodeSelector("someKey");
        @SuppressWarnings("unchecked")
        final NodeKeyResolver<ImmutableNode> resolverMock = mock(NodeKeyResolver.class);
        resolver = resolverMock;
    }

    /** A mock for the underlying node model. */
    private InMemoryNodeModel parentModel;

    /** A mock for the support object that provides the model. */
    private InMemoryNodeModelSupport modelSupport;

    /**
     * Creates a mock for a node handler and prepares the parent model to expect a request for the tracked node handler.
     *
     * @return the mock for the node handler
     */
    private NodeHandler<ImmutableNode> prepareGetNodeHandler() {
        @SuppressWarnings("unchecked")
        final NodeHandler<ImmutableNode> handler = mock(NodeHandler.class);
        when(parentModel.getTrackedNodeHandler(selector)).thenReturn(handler);
        return handler;
    }

    @BeforeEach
    public void setUp() throws Exception {
        parentModel = mock(InMemoryNodeModel.class);
        modelSupport = mock(InMemoryNodeModelSupport.class);

        when(modelSupport.getNodeModel()).thenReturn(parentModel);
    }

    /**
     * Creates a test model with default settings.
     *
     * @return the test model
     */
    private TrackedNodeModel setUpModel() {
        return new TrackedNodeModel(modelSupport, selector, true);
    }

    /**
     * Tests whether nodes can be added.
     */
    @Test
    public void testAddNodes() {
        final List<ImmutableNode> nodes = Arrays.asList(NodeStructureHelper.createNode("n1", 1), NodeStructureHelper.createNode("n2", 2));

        setUpModel().addNodes(KEY, nodes, resolver);

        verify(parentModel).addNodes(KEY, selector, nodes, resolver);
        verifyNoMoreInteractions(parentModel);
    }

    /**
     * Tests whether properties can be added.
     */
    @Test
    public void testAddProperty() {
        final Iterable<?> values = mock(Iterable.class);

        setUpModel().addProperty(KEY, values, resolver);

        verify(parentModel).addProperty(KEY, selector, values, resolver);
        verifyNoMoreInteractions(parentModel);
    }

    /**
     * Tests whether the whole model can be cleared.
     */
    @Test
    public void testClear() {
        when(parentModel.clearTree(null, selector, resolver)).thenReturn(null);

        setUpModel().clear(resolver);

        verify(parentModel).clearTree(null, selector, resolver);
        verifyNoMoreInteractions(parentModel);
    }

    /**
     * Tests whether a property can be cleared.
     */
    @Test
    public void testClearProperty() {
        setUpModel().clearProperty(KEY, resolver);

        verify(parentModel).clearProperty(KEY, selector, resolver);
        verifyNoMoreInteractions(parentModel);
    }

    /**
     * Tests whether a sub tree can be cleared.
     */
    @Test
    public void testClearTree() {
        final QueryResult<ImmutableNode> result = QueryResult.createNodeResult(NodeStructureHelper.createNode("test", null));
        final List<QueryResult<ImmutableNode>> removed = Collections.singletonList(result);

        when(parentModel.clearTree(KEY, selector, resolver)).thenReturn(removed);

        assertSame(removed, setUpModel().clearTree(KEY, resolver));

        verify(parentModel).clearTree(KEY, selector, resolver);
        verifyNoMoreInteractions(parentModel);
    }

    /**
     * Tests whether the model can be closed.
     */
    @Test
    public void testClose() {
        setUpModel().close();

        verify(parentModel).untrackNode(selector);
        verifyNoMoreInteractions(parentModel);
    }

    /**
     * Tests whether close can be called multiple times.
     */
    @Test
    public void testCloseMultipleTimes() {
        final TrackedNodeModel model = setUpModel();
        model.close();
        model.close();

        verify(parentModel).untrackNode(selector);
        verifyNoMoreInteractions(parentModel);
    }

    /**
     * Tests whether the correct in-memory representation can be queried.
     */
    @Test
    public void testGetInMemoryRepresentation() {
        final NodeHandler<ImmutableNode> handler = prepareGetNodeHandler();
        final ImmutableNode root = NodeStructureHelper.createNode("Root", null);

        when(handler.getRootNode()).thenReturn(root);

        final TrackedNodeModel model = setUpModel();
        assertSame(root, model.getInMemoryRepresentation());
    }

    /**
     * Tests whether a node handler can be queried.
     */
    @Test
    public void testGetNodeHandler() {
        final NodeHandler<ImmutableNode> handler = prepareGetNodeHandler();

        assertSame(handler, setUpModel().getNodeHandler());

        verify(parentModel).getTrackedNodeHandler(selector);
        verifyNoMoreInteractions(parentModel);
    }

    /**
     * Tries to create an instance without a parent model.
     */
    @Test
    public void testInitNoParentModel() {
        assertThrows(IllegalArgumentException.class, () -> new TrackedNodeModel(null, selector, true));
    }

    /**
     * Tries to create an instance without a selector.
     */
    @Test
    public void testInitNoSelector() {
        assertThrows(IllegalArgumentException.class, () -> new TrackedNodeModel(modelSupport, null, true));
    }

    /**
     * Tests whether a property can be set.
     */
    @Test
    public void testSetProperty() {
        final Object value = 42;

        setUpModel().setProperty(KEY, value, resolver);

        verify(parentModel).setProperty(KEY, selector, value, resolver);
        verifyNoMoreInteractions(parentModel);
    }

    /**
     * Tests whether the root node can be changed.
     */
    @Test
    public void testSetRootNode() {
        final ImmutableNode root = NodeStructureHelper.createNode("root", null);

        final TrackedNodeModel model = setUpModel();
        model.setRootNode(root);

        verify(parentModel).replaceTrackedNode(selector, root);
        verifyNoMoreInteractions(parentModel);
    }
}
