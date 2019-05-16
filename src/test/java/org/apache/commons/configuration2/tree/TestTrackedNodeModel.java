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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@code TrackedNodeModel}.
 *
 */
public class TestTrackedNodeModel
{
    /** Constant for a test key. */
    private static final String KEY = "aTestKey";

    /** A test node selector. */
    private static NodeSelector selector;

    /** A mock resolver. */
    private static NodeKeyResolver<ImmutableNode> resolver;

    /** A mock for the underlying node model. */
    private InMemoryNodeModel parentModel;

    /** A mock for the support object that provides the model. */
    private InMemoryNodeModelSupport modelSupport;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        selector = new NodeSelector("someKey");
        @SuppressWarnings("unchecked")
        final
        NodeKeyResolver<ImmutableNode> resolverMock =
                EasyMock.createMock(NodeKeyResolver.class);
        EasyMock.replay(resolverMock);
        resolver = resolverMock;
    }

    @Before
    public void setUp() throws Exception
    {
        parentModel = EasyMock.createMock(InMemoryNodeModel.class);
        modelSupport = EasyMock.createMock(InMemoryNodeModelSupport.class);
        EasyMock.expect(modelSupport.getNodeModel()).andReturn(parentModel)
                .anyTimes();
        EasyMock.replay(modelSupport);
    }

    /**
     * Creates a test model with default settings.
     *
     * @return the test model
     */
    private TrackedNodeModel setUpModel()
    {
        return new TrackedNodeModel(modelSupport, selector, true);
    }

    /**
     * Tries to create an instance without a selector.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoSelector()
    {
        new TrackedNodeModel(modelSupport, null, true);
    }

    /**
     * Tries to create an instance without a parent model.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoParentModel()
    {
        new TrackedNodeModel(null, selector, true);
    }

    /**
     * Tests whether the root node can be changed.
     */
    @Test
    public void testSetRootNode()
    {
        final ImmutableNode root = NodeStructureHelper.createNode("root", null);
        parentModel.replaceTrackedNode(selector, root);
        EasyMock.replay(parentModel);

        final TrackedNodeModel model = setUpModel();
        model.setRootNode(root);
        EasyMock.verify(parentModel);
    }

    /**
     * Creates a mock for a node handler and prepares the parent model to expect
     * a request for the tracked node handler.
     *
     * @return the mock for the node handler
     */
    private NodeHandler<ImmutableNode> expectGetNodeHandler()
    {
        @SuppressWarnings("unchecked")
        final
        NodeHandler<ImmutableNode> handler =
                EasyMock.createMock(NodeHandler.class);
        EasyMock.expect(parentModel.getTrackedNodeHandler(selector)).andReturn(
                handler);
        return handler;
    }

    /**
     * Tests whether a node handler can be queried.
     */
    @Test
    public void testGetNodeHandler()
    {
        final NodeHandler<ImmutableNode> handler = expectGetNodeHandler();
        EasyMock.replay(handler, parentModel);

        assertSame("Wrong node handler", handler, setUpModel().getNodeHandler());
        EasyMock.verify(parentModel);
    }

    /**
     * Tests whether properties can be added.
     */
    @Test
    public void testAddProperty()
    {
        final Iterable<?> values = EasyMock.createMock(Iterable.class);
        parentModel.addProperty(KEY, selector, values, resolver);
        EasyMock.replay(values, parentModel);

        setUpModel().addProperty(KEY, values, resolver);
        EasyMock.verify(parentModel);
    }

    /**
     * Tests whether nodes can be added.
     */
    @Test
    public void testAddNodes()
    {
        final List<ImmutableNode> nodes =
                Arrays.asList(NodeStructureHelper.createNode("n1", 1),
                        NodeStructureHelper.createNode("n2", 2));
        parentModel.addNodes(KEY, selector, nodes, resolver);
        EasyMock.replay(parentModel);

        setUpModel().addNodes(KEY, nodes, resolver);
        EasyMock.verify(parentModel);
    }

    /**
     * Tests whether a property can be set.
     */
    @Test
    public void testSetProperty()
    {
        final Object value = 42;
        parentModel.setProperty(KEY, selector, value, resolver);
        EasyMock.replay(parentModel);

        setUpModel().setProperty(KEY, value, resolver);
        EasyMock.verify(parentModel);
    }

    /**
     * Tests whether a sub tree can be cleared.
     */
    @Test
    public void testClearTree()
    {
        final QueryResult<ImmutableNode> result =
                QueryResult.createNodeResult(NodeStructureHelper.createNode(
                        "test", null));
        final List<QueryResult<ImmutableNode>> removed =
                Collections.singletonList(result);
        EasyMock.expect(parentModel.clearTree(KEY, selector, resolver))
                .andReturn(removed);
        EasyMock.replay(parentModel);

        assertSame("Wrong removed elements", removed,
                setUpModel().clearTree(KEY, resolver));
        EasyMock.verify(parentModel);
    }

    /**
     * Tests whether a property can be cleared.
     */
    @Test
    public void testClearProperty()
    {
        parentModel.clearProperty(KEY, selector, resolver);
        EasyMock.replay(parentModel);

        setUpModel().clearProperty(KEY, resolver);
        EasyMock.verify(parentModel);
    }

    /**
     * Tests whether the whole model can be cleared.
     */
    @Test
    public void testClear()
    {
        EasyMock.expect(parentModel.clearTree(null, selector, resolver))
                .andReturn(null);
        EasyMock.replay(parentModel);

        setUpModel().clear(resolver);
        EasyMock.verify(parentModel);
    }

    /**
     * Tests whether the model can be closed.
     */
    @Test
    public void testClose()
    {
        parentModel.untrackNode(selector);
        EasyMock.replay(parentModel);

        setUpModel().close();
        EasyMock.verify(parentModel);
    }

    /**
     * Tests whether close can be called multiple times.
     */
    @Test
    public void testCloseMultipleTimes()
    {
        parentModel.untrackNode(selector);
        EasyMock.replay(parentModel);

        final TrackedNodeModel model = setUpModel();
        model.close();
        model.close();
        EasyMock.verify(parentModel);
    }

    /**
     * Tests whether the correct in-memory representation can be queried.
     */
    @Test
    public void testGetInMemoryRepresentation()
    {
        final NodeHandler<ImmutableNode> handler = expectGetNodeHandler();
        final ImmutableNode root = NodeStructureHelper.createNode("Root", null);
        EasyMock.expect(handler.getRootNode()).andReturn(root);
        EasyMock.replay(handler, parentModel);

        final TrackedNodeModel model = setUpModel();
        assertSame("Wrong root node", root, model.getInMemoryRepresentation());
    }
}
