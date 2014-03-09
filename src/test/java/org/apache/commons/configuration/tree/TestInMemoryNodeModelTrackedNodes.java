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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.configuration.ex.ConfigurationRuntimeException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A special test class for {@code InMemoryNodeModel} which tests the facilities
 * for tracking nodes.
 *
 * @version $Id$
 */
public class TestInMemoryNodeModelTrackedNodes
{
    /** The root node for the test hierarchy. */
    private static ImmutableNode root;

    /** A default node selector initialized with a test key. */
    private static NodeSelector selector;

    /** The model to be tested. */
    private InMemoryNodeModel model;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        root =
                new ImmutableNode.Builder(1).addChild(
                        NodeStructureHelper.ROOT_TABLES_TREE).create();
        selector = new NodeSelector("tables.table(1)");
    }

    @Before
    public void setUp() throws Exception
    {
        model = new InMemoryNodeModel(root);
    }

    /**
     * Creates a default resolver which supports arbitrary queries on a target
     * node.
     *
     * @return the resolver
     */
    private static NodeKeyResolver<ImmutableNode> createResolver()
    {
        NodeKeyResolver<ImmutableNode> resolver =
                NodeStructureHelper.createResolverMock();
        NodeStructureHelper.expectResolveKeyForQueries(resolver);
        EasyMock.replay(resolver);
        return resolver;
    }

    /**
     * Tries to call trackNode() with a key that does not yield any results.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testTrackNodeKeyNoResults()
    {
        model.trackNode(new NodeSelector("tables.unknown"), createResolver());
    }

    /**
     * Tries to call trackNode() with a key that selects multiple results.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testTrackNodeKeyMultipleResults()
    {
        model.trackNode(new NodeSelector("tables.table.fields.field.name"),
                createResolver());
    }

    /**
     * Tests whether a tracked node can be queried.
     */
    @Test
    public void testGetTrackedNodeExisting()
    {
        ImmutableNode node =
                NodeStructureHelper.nodeForKey(model, "tables/table(1)");
        model.trackNode(selector, createResolver());
        assertSame("Wrong node", node, model.getTrackedNode(selector));
    }

    /**
     * Tries to obtain a tracked node which is unknown.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testGetTrackedNodeNonExisting()
    {
        model.getTrackedNode(selector);
    }

    /**
     * Tests whether a tracked node survives updates of the node model.
     */
    @Test
    public void testGetTrackedNodeAfterUpdate()
    {
        NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clearProperty("tables.table(1).fields.field(1).name", resolver);
        ImmutableNode node = model.getTrackedNode(selector);
        assertEquals("Wrong node", NodeStructureHelper.table(1), node
                .getChildren().get(0).getValue());
    }

    /**
     * Tests whether a tracked node can be queried even if it was removed from
     * the structure.
     */
    @Test
    public void testGetTrackedNodeAfterUpdateNoLongerExisting()
    {
        ImmutableNode node =
                NodeStructureHelper.nodeForKey(model, "tables/table(1)");
        NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clearTree("tables.table(0)", resolver);
        assertSame("Wrong node", node, model.getTrackedNode(selector));
    }

    /**
     * Tests whether a tracked node can be queried even after the model was
     * cleared.
     */
    @Test
    public void testGetTrackedNodeAfterClear()
    {
        ImmutableNode node =
                NodeStructureHelper.nodeForKey(model, "tables/table(1)");
        NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clear();
        assertSame("Wrong node", node, model.getTrackedNode(selector));
    }

    /**
     * Tests whether a tracked node can be queried after the root node was
     * changed.
     */
    @Test
    public void testGetTrackedNodeAfterSetRootNode()
    {
        ImmutableNode node =
                NodeStructureHelper.nodeForKey(model, "tables/table(1)");
        NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.setRootNode(root);
        assertSame("Wrong node", node, model.getTrackedNode(selector));
    }

    /**
     * Tries to stop tracking of a node which is not tracked.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testUntrackNodeNonExisting()
    {
        model.untrackNode(selector);
    }

    /**
     * Tests whether tracking of a node can be stopped.
     */
    @Test
    public void testUntrackNode()
    {
        model.trackNode(selector, createResolver());
        model.untrackNode(selector);
        try
        {
            model.getTrackedNode(selector);
            fail("Could get untracked node!");
        }
        catch (ConfigurationRuntimeException crex)
        {
            // expected
        }
    }

    /**
     * Tests whether a single node can be tracked multiple times.
     */
    @Test
    public void testTrackNodeMultipleTimes()
    {
        NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.trackNode(selector, resolver);
        model.untrackNode(selector);
        assertNotNull("No tracked node", model.getTrackedNode(selector));
    }

    /**
     * Tests isDetached() for a node which has just been tracked.
     */
    @Test
    public void testIsDetachedFalseNoUpdates()
    {
        NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        assertFalse("Node is detached", model.isTrackedNodeDetached(selector));
    }

    /**
     * Tests isDetached() for a life node.
     */
    @Test
    public void testIsDetachedFalseAfterUpdate()
    {
        NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clearProperty("tables.table(1).fields.field(1).name", resolver);
        assertFalse("Node is detached", model.isTrackedNodeDetached(selector));
    }

    /**
     * Tests isDetached() for an actually detached node.
     */
    @Test
    public void testIsDetachedTrue()
    {
        NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clearTree("tables.table(0)", resolver);
        assertTrue("Node is not detached",
                model.isTrackedNodeDetached(selector));
    }

    /**
     * Tests whether a clear() operation causes nodes to be detached.
     */
    @Test
    public void testIsDetachedAfterClear()
    {
        NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clear();
        assertTrue("Node is not detached",
                model.isTrackedNodeDetached(selector));
    }

    /**
     * Tests whether tracked nodes become detached when a new root node is set.
     */
    @Test
    public void testIsDetachedAfterSetRoot()
    {
        NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clearProperty("tables.table(1).fields.field(1).name", resolver);
        model.setRootNode(root);
        assertTrue("Node is not detached",
                model.isTrackedNodeDetached(selector));
    }
}
