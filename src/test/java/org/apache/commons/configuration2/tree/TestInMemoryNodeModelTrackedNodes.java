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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A special test class for {@code InMemoryNodeModel} which tests the facilities for tracking nodes.
 *
 */
public class TestInMemoryNodeModelTrackedNodes {
    /** Constant for the name of a new table field. */
    private static final String NEW_FIELD = "newTableField";

    /** Constant for a test key. */
    private static final String TEST_KEY = "someTestKey";

    /** Constant for the key used by the test selector. */
    private static final String SELECTOR_KEY = "tables.table(1)";

    /** The root node for the test hierarchy. */
    private static ImmutableNode root;

    /** A default node selector initialized with a test key. */
    private static NodeSelector selector;

    /**
     * Checks whether a fields node was correctly changed by an update operation.
     *
     * @param nodeFields the fields node
     * @param idx the index of the changed node
     */
    private static void checkedForChangedField(final ImmutableNode nodeFields, final int idx) {
        assertEquals(NodeStructureHelper.fieldsLength(1), nodeFields.getChildren().size(), "Wrong number of field nodes");
        int childIndex = 0;
        for (final ImmutableNode field : nodeFields) {
            final String expName = childIndex == idx ? NEW_FIELD : NodeStructureHelper.field(1, childIndex);
            checkFieldNode(field, expName);
            childIndex++;
        }
    }

    /**
     * Checks whether a field node has the expected content.
     *
     * @param nodeField the field node to be checked
     * @param name the expected name of this field
     */
    private static void checkFieldNode(final ImmutableNode nodeField, final String name) {
        assertEquals("field", nodeField.getNodeName(), "Wrong node name");
        assertEquals(1, nodeField.getChildren().size(), "Wrong number of children of field node");
        final ImmutableNode nodeName = nodeField.getChildren().get(0);
        assertEquals("name", nodeName.getNodeName(), "Wrong name of name node");
        assertEquals(name, nodeName.getValue(), "Wrong node value");
    }

    /**
     * Tests whether a field node was added.
     *
     * @param nodeFields the fields node
     */
    private static void checkForAddedField(final ImmutableNode nodeFields) {
        assertEquals(NodeStructureHelper.fieldsLength(1) + 1, nodeFields.getChildren().size(), "Wrong number of children");
        final ImmutableNode nodeField = nodeFields.getChildren().get(NodeStructureHelper.fieldsLength(1));
        checkFieldNode(nodeField, NEW_FIELD);
    }

    /**
     * Helper method for checking whether the expected field node was removed.
     *
     * @param nodeFields the fields node
     * @param idx the index of the removed field
     */
    private static void checkForRemovedField(final ImmutableNode nodeFields, final int idx) {
        assertEquals(NodeStructureHelper.fieldsLength(1) - 1, nodeFields.getChildren().size(), "Field not removed");
        final Set<String> expectedNames = new HashSet<>();
        final Set<String> actualNames = new HashSet<>();
        for (int i = 0; i < NodeStructureHelper.fieldsLength(1); i++) {
            if (idx != i) {
                expectedNames.add(NodeStructureHelper.field(1, i));
            }
        }
        for (final ImmutableNode field : nodeFields) {
            final ImmutableNode nodeName = field.getChildren().get(0);
            actualNames.add(String.valueOf(nodeName.getValue()));
        }
        assertEquals(expectedNames, actualNames, "Wrong field names");
    }

    /**
     * Creates a default resolver which supports arbitrary queries on a target node.
     *
     * @return the resolver
     */
    private static NodeKeyResolver<ImmutableNode> createResolver() {
        return createResolver(true);
    }

    /**
     * Creates a default resolver which supports arbitrary queries on a target node and allows specifying the replay flag.
     * If the boolean parameter is false, the mock is not replayed; so additional behaviors can be defined.
     *
     * @param replay the replay flag
     * @return the resolver mock
     */
    private static NodeKeyResolver<ImmutableNode> createResolver(final boolean replay) {
        final NodeKeyResolver<ImmutableNode> resolver = NodeStructureHelper.createResolverMock();
        NodeStructureHelper.expectResolveKeyForQueries(resolver);
        if (replay) {
            EasyMock.replay(resolver);
        }
        return resolver;
    }

    /**
     * Prepares a mock for a resolver to handle keys for update operations. Support is limited. It is expected that only a
     * single value is changed.
     *
     * @param resolver the {@code NodeKeyResolver} mock
     */
    private static void prepareResolverForUpdateKeys(final NodeKeyResolver<ImmutableNode> resolver) {
        EasyMock.expect(
            resolver.resolveUpdateKey(EasyMock.anyObject(ImmutableNode.class), EasyMock.anyString(), EasyMock.anyObject(), EasyMock.anyObject(TreeData.class)))
            .andAnswer(() -> {
                final ImmutableNode root = (ImmutableNode) EasyMock.getCurrentArguments()[0];
                final String key = (String) EasyMock.getCurrentArguments()[1];
                final TreeData handler = (TreeData) EasyMock.getCurrentArguments()[3];
                final List<QueryResult<ImmutableNode>> results = DefaultExpressionEngine.INSTANCE.query(root, key, handler);
                assertEquals(1, results.size(), "Wrong number of query results");
                return new NodeUpdateData<>(Collections.singletonMap(results.get(0), EasyMock.getCurrentArguments()[2]), null, null, null);
            }).anyTimes();
    }

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        root = new ImmutableNode.Builder(1).addChild(NodeStructureHelper.ROOT_TABLES_TREE).create();
        selector = new NodeSelector(SELECTOR_KEY);
    }

    /** The model to be tested. */
    private InMemoryNodeModel model;

    /**
     * Helper method for testing whether a tracked node can be replaced.
     */
    private void checkReplaceTrackedNode() {
        final ImmutableNode newNode = new ImmutableNode.Builder().name("newNode").create();
        model.replaceTrackedNode(selector, newNode);
        assertSame(newNode, model.getTrackedNode(selector), "Node not changed");
        assertTrue(model.isTrackedNodeDetached(selector), "Node not detached");
    }

    /**
     * Checks trackChildNodes() if the passed in key has a result set which causes the operation to be aborted.
     *
     * @param queryResult the result set of the key
     */
    private void checkTrackChildNodesNoResult(final List<ImmutableNode> queryResult) {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver(false);
        EasyMock.expect(resolver.resolveNodeKey(root, TEST_KEY, model.getNodeHandler())).andReturn(queryResult);
        EasyMock.replay(resolver);
        final TreeData oldData = model.getTreeData();

        assertTrue(model.trackChildNodes(TEST_KEY, resolver).isEmpty(), "Got selectors");
        assertSame(oldData, model.getTreeData(), "Model was changed");
    }

    /**
     * Helper method for testing trackChildNodeWithCreation() if invalid query results are generated.
     *
     * @param queryResult the result set of the key
     */
    private void checkTrackChildNodeWithCreationInvalidKey(final List<ImmutableNode> queryResult) {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver(false);
        EasyMock.expect(resolver.resolveNodeKey(model.getRootNode(), TEST_KEY, model.getNodeHandler())).andReturn(queryResult);
        EasyMock.replay(resolver);
        assertThrows(ConfigurationRuntimeException.class, () -> model.trackChildNodeWithCreation(TEST_KEY, "someChild", resolver));
    }

    /**
     * Prepares the resolver mock to expect a nodeKey() request.
     *
     * @param resolver the {@code NodeKeyResolver}
     * @param node the node whose name is to be resolved
     * @param key the key to be returned for this node
     */
    private void expectNodeKey(final NodeKeyResolver<ImmutableNode> resolver, final ImmutableNode node, final String key) {
        final Map<ImmutableNode, String> cache = new HashMap<>();
        EasyMock.expect(resolver.nodeKey(node, cache, model.getNodeHandler())).andReturn(key);
    }

    /**
     * Returns the fields node from the model.
     *
     * @return the fields node
     */
    private ImmutableNode fieldsNodeFromModel() {
        return NodeStructureHelper.nodeForKey(model, "tables/table(1)/fields");
    }

    /**
     * Returns the fields node from a tracked node.
     *
     * @return the fields node
     */
    private ImmutableNode fieldsNodeFromTrackedNode() {
        return NodeStructureHelper.nodeForKey(model.getTrackedNode(selector), "fields");
    }

    /**
     * Produces a tracked node with the default selector and executes an operation which detaches this node.
     *
     * @param resolver the {@code NodeKeyResolver}
     */
    private void initDetachedNode(final NodeKeyResolver<ImmutableNode> resolver) {
        model.trackNode(selector, resolver);
        model.clearTree("tables.table(0)", resolver);
    }

    @BeforeEach
    public void setUp() throws Exception {
        model = new InMemoryNodeModel(root);
    }

    /**
     * Tests an addNodes() operation on a tracked node that is detached.
     */
    @Test
    public void testAddNodesOnDetachedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver(false);
        NodeStructureHelper.expectResolveAddKeys(resolver);
        EasyMock.replay(resolver);
        model.trackNode(selector, resolver);
        initDetachedNode(resolver);
        final ImmutableNode rootNode = model.getRootNode();
        model.addNodes("fields", selector, Collections.singleton(NodeStructureHelper.createFieldNode(NEW_FIELD)), resolver);
        assertSame(rootNode, model.getRootNode(), "Root node was changed");
        checkForAddedField(fieldsNodeFromTrackedNode());
    }

    /**
     * Tests whether an addNodes() operation works on a tracked node.
     */
    @Test
    public void testAddNodesOnTrackedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver(false);
        NodeStructureHelper.expectResolveAddKeys(resolver);
        EasyMock.replay(resolver);
        model.trackNode(selector, resolver);
        model.addNodes("fields", selector, Collections.singleton(NodeStructureHelper.createFieldNode(NEW_FIELD)), resolver);
        checkForAddedField(fieldsNodeFromModel());
        checkForAddedField(fieldsNodeFromTrackedNode());
    }

    /**
     * Tests an addProperty() operation on a tracked node that is detached.
     */
    @Test
    public void testAddPropertyOnDetachedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver(false);
        NodeStructureHelper.expectResolveAddKeys(resolver);
        EasyMock.replay(resolver);
        model.trackNode(selector, resolver);
        initDetachedNode(resolver);
        final ImmutableNode rootNode = model.getRootNode();
        model.addProperty("fields.field(-1).name", selector, Collections.singleton(NEW_FIELD), resolver);
        assertSame(rootNode, model.getRootNode(), "Root node was changed");
        checkForAddedField(fieldsNodeFromTrackedNode());
    }

    /**
     * Tests whether an addProperty() operation works on a tracked node.
     */
    @Test
    public void testAddPropertyOnTrackedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver(false);
        NodeStructureHelper.expectResolveAddKeys(resolver);
        EasyMock.replay(resolver);
        model.trackNode(selector, resolver);
        model.addProperty("fields.field(-1).name", selector, Collections.singleton(NEW_FIELD), resolver);
        checkForAddedField(fieldsNodeFromModel());
        checkForAddedField(fieldsNodeFromTrackedNode());
    }

    /**
     * Tests a clearProperty() operation on a tracked node which is detached.
     */
    @Test
    public void testClearPropertyOnDetachedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        initDetachedNode(resolver);
        final ImmutableNode rootNode = model.getRootNode();
        model.clearProperty("fields.field(0).name", selector, resolver);
        assertSame(rootNode, model.getRootNode(), "Model root was changed");
        final ImmutableNode nodeFields = fieldsNodeFromTrackedNode();
        checkForRemovedField(nodeFields, 0);
    }

    /**
     * Tests whether clearProperty() can operate on a tracked node.
     */
    @Test
    public void testClearPropertyOnTrackedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clearProperty("fields.field(0).name", selector, resolver);
        final ImmutableNode nodeFields = fieldsNodeFromModel();
        checkForRemovedField(nodeFields, 0);
    }

    /**
     * Tests a clearTree() operation on a tracked node which is detached.
     */
    @Test
    public void testClearTreeOnDetachedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        initDetachedNode(resolver);
        final ImmutableNode rootNode = model.getRootNode();
        model.clearTree("fields.field(1)", selector, resolver);
        assertSame(rootNode, model.getRootNode(), "Model root was changed");
        final ImmutableNode nodeFields = fieldsNodeFromTrackedNode();
        checkForRemovedField(nodeFields, 1);
    }

    /**
     * Tests whether clearTree() can operate on a tracked node.
     */
    @Test
    public void testClearTreeOnTrackedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clearTree("fields.field(1)", selector, resolver);
        final ImmutableNode nodeFields = fieldsNodeFromModel();
        checkForRemovedField(nodeFields, 1);
    }

    /**
     * Tests whether a tracked node can be queried even after the model was cleared.
     */
    @Test
    public void testGetTrackedNodeAfterClear() {
        final ImmutableNode node = NodeStructureHelper.nodeForKey(model, "tables/table(1)");
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clear(resolver);
        assertSame(node, model.getTrackedNode(selector), "Wrong node");
    }

    /**
     * Tests whether a tracked node can be queried after the root node was changed.
     */
    @Test
    public void testGetTrackedNodeAfterSetRootNode() {
        final ImmutableNode node = NodeStructureHelper.nodeForKey(model, "tables/table(1)");
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.setRootNode(root);
        assertSame(node, model.getTrackedNode(selector), "Wrong node");
    }

    /**
     * Tests whether a tracked node survives updates of the node model.
     */
    @Test
    public void testGetTrackedNodeAfterUpdate() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clearProperty("tables.table(1).fields.field(1).name", resolver);
        final ImmutableNode node = model.getTrackedNode(selector);
        assertEquals(NodeStructureHelper.table(1), node.getChildren().get(0).getValue(), "Wrong node");
    }

    /**
     * Tests whether a tracked node can be queried even if it was removed from the structure.
     */
    @Test
    public void testGetTrackedNodeAfterUpdateNoLongerExisting() {
        final ImmutableNode node = NodeStructureHelper.nodeForKey(model, "tables/table(1)");
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        initDetachedNode(resolver);
        assertSame(node, model.getTrackedNode(selector), "Wrong node");
    }

    /**
     * Tests whether a tracked node can be queried.
     */
    @Test
    public void testGetTrackedNodeExisting() {
        final ImmutableNode node = NodeStructureHelper.nodeForKey(model, "tables/table(1)");
        model.trackNode(selector, createResolver());
        assertSame(node, model.getTrackedNode(selector), "Wrong node");
    }

    /**
     * Tests whether a node handler for a tracked node can be queried which is still active.
     */
    @Test
    public void testGetTrackedNodeHandlerActive() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        final NodeHandler<ImmutableNode> handler = model.getTrackedNodeHandler(selector);
        assertInstanceOf(TrackedNodeHandler.class, handler, "Wrong node handler: " + handler);
        assertSame(model.getTrackedNode(selector), handler.getRootNode(), "Wrong root node");
        final TrackedNodeHandler tnh = (TrackedNodeHandler) handler;
        assertSame(model.getTreeData(), tnh.getParentHandler(), "Wrong parent handler");
    }

    /**
     * Tests whether a node handler for a detached tracked node can be queried.
     */
    @Test
    public void testGetTrackedNodeHandlerDetached() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        initDetachedNode(resolver);
        final NodeHandler<ImmutableNode> handler = model.getTrackedNodeHandler(selector);
        assertSame(model.getTrackedNode(selector), handler.getRootNode(), "Wrong root node");
        assertInstanceOf(TreeData.class, handler, "Wrong handler: " + handler);
        assertNotSame(model.getNodeHandler(), handler, "Shared handler");
    }

    /**
     * Tries to obtain a tracked node which is unknown.
     */
    @Test
    public void testGetTrackedNodeNonExisting() {
        assertThrows(ConfigurationRuntimeException.class, () -> model.getTrackedNode(selector));
    }

    /**
     * Tests whether a clear() operation causes nodes to be detached.
     */
    @Test
    public void testIsDetachedAfterClear() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clear(resolver);
        assertTrue(model.isTrackedNodeDetached(selector), "Node is not detached");
    }

    /**
     * Tests whether tracked nodes become detached when a new root node is set.
     */
    @Test
    public void testIsDetachedAfterSetRoot() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clearProperty("tables.table(1).fields.field(1).name", resolver);
        model.setRootNode(root);
        assertTrue(model.isTrackedNodeDetached(selector), "Node is not detached");
    }

    /**
     * Tests isDetached() for a life node.
     */
    @Test
    public void testIsDetachedFalseAfterUpdate() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clearProperty("tables.table(1).fields.field(1).name", resolver);
        assertFalse(model.isTrackedNodeDetached(selector), "Node is detached");
    }

    /**
     * Tests isDetached() for a node which has just been tracked.
     */
    @Test
    public void testIsDetachedFalseNoUpdates() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        assertFalse(model.isTrackedNodeDetached(selector), "Node is detached");
    }

    /**
     * Tests isDetached() for an actually detached node.
     */
    @Test
    public void testIsDetachedTrue() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        initDetachedNode(resolver);
        assertTrue(model.isTrackedNodeDetached(selector), "Node is not detached");
    }

    /**
     * Tests whether an active tracked node can be replaced.
     */
    @Test
    public void testReplaceTrackedNodeForActiveTrackedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        checkReplaceTrackedNode();
    }

    /**
     * Tests whether a detached tracked node can be replaced.
     */
    @Test
    public void testReplaceTrackedNodeForDetachedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        initDetachedNode(resolver);
        checkReplaceTrackedNode();
    }

    /**
     * Tries to replace a tracked node with a null node.
     */
    @Test
    public void testReplaceTrackedNodeNull() {
        model.trackNode(selector, createResolver());
        assertThrows(IllegalArgumentException.class, () -> model.replaceTrackedNode(selector, null));
    }

    /**
     * Tests whether tracked nodes can be created from a key.
     */
    @Test
    public void testSelectAndTrackNodes() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver(false);
        final String nodeKey1 = "tables/table(0)";
        final String nodeKey2 = "tables/table(1)";
        final ImmutableNode node1 = NodeStructureHelper.nodeForKey(root, nodeKey1);
        final ImmutableNode node2 = NodeStructureHelper.nodeForKey(root, nodeKey2);
        EasyMock.expect(resolver.resolveNodeKey(root, TEST_KEY, model.getNodeHandler())).andReturn(Arrays.asList(node1, node2));
        expectNodeKey(resolver, node1, nodeKey1);
        expectNodeKey(resolver, node2, nodeKey2);
        EasyMock.replay(resolver);

        final Collection<NodeSelector> selectors = model.selectAndTrackNodes(TEST_KEY, resolver);
        final Iterator<NodeSelector> it = selectors.iterator();
        NodeSelector sel = it.next();
        assertEquals(new NodeSelector(nodeKey1), sel, "Wrong selector 1");
        assertSame(node1, model.getTrackedNode(sel), "Wrong tracked node 1");
        sel = it.next();
        assertEquals(new NodeSelector(nodeKey2), sel, "Wrong selector 2");
        assertSame(node2, model.getTrackedNode(sel), "Wrong tracked node 2");
        assertFalse(it.hasNext(), "Too many selectors");
    }

    /**
     * Tests whether selectAndTrackNodes() works for nodes that are already tracked.
     */
    @Test
    public void testSelectAndTrackNodesNodeAlreadyTracked() {
        NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        resolver = createResolver(false);
        final ImmutableNode node = model.getTrackedNode(selector);
        EasyMock.expect(resolver.resolveNodeKey(root, TEST_KEY, model.getNodeHandler())).andReturn(Collections.singletonList(node));
        expectNodeKey(resolver, node, SELECTOR_KEY);
        EasyMock.replay(resolver);

        final Collection<NodeSelector> selectors = model.selectAndTrackNodes(TEST_KEY, resolver);
        assertEquals(1, selectors.size(), "Wrong number of selectors");
        assertEquals(selector, selectors.iterator().next(), "Wrong selector");
        model.untrackNode(selector);
        assertSame(node, model.getTrackedNode(selector), "Node not tracked");
    }

    /**
     * Tests selectAndTrackNodes() if the key does not select any nodes.
     */
    @Test
    public void testSelectAndTrackNodesNoSelection() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver(false);
        EasyMock.expect(resolver.resolveNodeKey(root, TEST_KEY, model.getNodeHandler())).andReturn(Collections.<ImmutableNode>emptyList());
        EasyMock.replay(resolver);

        assertTrue(model.selectAndTrackNodes(TEST_KEY, resolver).isEmpty(), "Got selectors");
    }

    /**
     * Tests a setProperty() operation on a tracked node that is detached.
     */
    @Test
    public void testSetPropertyOnDetachedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver(false);
        prepareResolverForUpdateKeys(resolver);
        EasyMock.replay(resolver);
        model.trackNode(selector, resolver);
        initDetachedNode(resolver);
        final ImmutableNode rootNode = model.getRootNode();
        model.setProperty("fields.field(0).name", selector, NEW_FIELD, resolver);
        assertSame(rootNode, model.getRootNode(), "Root node of model was changed");
        checkedForChangedField(fieldsNodeFromTrackedNode(), 0);
    }

    /**
     * Tests whether a setProperty() operation works on a tracked node.
     */
    @Test
    public void testSetPropertyOnTrackedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver(false);
        prepareResolverForUpdateKeys(resolver);
        EasyMock.replay(resolver);
        model.trackNode(selector, resolver);
        model.setProperty("fields.field(0).name", selector, NEW_FIELD, resolver);
        checkedForChangedField(fieldsNodeFromModel(), 0);
        checkedForChangedField(fieldsNodeFromTrackedNode(), 0);
    }

    /**
     * Tests whether all children of a node can be tracked at once.
     */
    @Test
    public void testTrackChildNodes() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver(false);
        final ImmutableNode node = NodeStructureHelper.nodeForKey(root, "tables");
        final String[] keys = new String[node.getChildren().size()];
        for (int i = 0; i < keys.length; i++) {
            final ImmutableNode child = node.getChildren().get(i);
            keys[i] = String.format("%s.%s(%d)", node.getNodeName(), child.getNodeName(), i);
            expectNodeKey(resolver, child, keys[i]);
        }
        EasyMock.expect(resolver.resolveNodeKey(root, TEST_KEY, model.getNodeHandler())).andReturn(Collections.singletonList(node));
        EasyMock.replay(resolver);

        final Collection<NodeSelector> selectors = model.trackChildNodes(TEST_KEY, resolver);
        assertEquals(node.getChildren().size(), selectors.size(), "Wrong number of selectors");
        int idx = 0;
        for (final NodeSelector sel : selectors) {
            assertEquals(new NodeSelector(keys[idx]), sel, "Wrong selector");
            assertEquals(node.getChildren().get(idx), model.getTrackedNode(sel), "Wrong tracked node for " + sel);
            idx++;
        }
    }

    /**
     * Tests trackChildNodes() for a key that returns more than a single result.
     */
    @Test
    public void testTrackChildNodesMultipleResults() {
        checkTrackChildNodesNoResult(
            Arrays.asList(NodeStructureHelper.nodeForKey(root, "tables/table(0)"), NodeStructureHelper.nodeForKey(root, "tables/table(1)")));
    }

    /**
     * Tests trackChildNodes() for a key pointing to a node with no children.
     */
    @Test
    public void testTrackChildNodesNodeWithNoChildren() {
        checkTrackChildNodesNoResult(Collections.singletonList(NodeStructureHelper.nodeForKey(root, "tables/table(0)/name")));
    }

    /**
     * Tests trackChildNodes() for a key that does not return any results.
     */
    @Test
    public void testTrackChildNodesNoResults() {
        checkTrackChildNodesNoResult(Collections.<ImmutableNode>emptyList());
    }

    /**
     * Tests whether an existing child of a selected node can be tracked.
     */
    @Test
    public void testTrackChildNodeWithCreationExisting() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver(false);
        final String childName = "name";
        final String parentKey = "tables/table(0)";
        final String childKey = parentKey + "/" + childName;
        final ImmutableNode node = NodeStructureHelper.nodeForKey(model, parentKey);
        final ImmutableNode child = NodeStructureHelper.nodeForKey(node, childName);
        EasyMock.expect(resolver.resolveNodeKey(root, TEST_KEY, model.getNodeHandler())).andReturn(Collections.singletonList(node));
        expectNodeKey(resolver, child, childKey);
        EasyMock.replay(resolver);

        final NodeSelector childSelector = model.trackChildNodeWithCreation(TEST_KEY, childName, resolver);
        assertEquals(new NodeSelector(childKey), childSelector, "Wrong selector");
        assertSame(child, model.getTrackedNode(childSelector), "Wrong tracked node");
    }

    /**
     * Tests trackChildNodeWithCreation() if the passed in key selects multiple nodes.
     */
    @Test
    public void testTrackChildNodeWithCreationMultipleResults() {
        final List<ImmutableNode> nodes = Arrays.asList(NodeStructureHelper.nodeForKey(root, "tables/table(0)"),
            NodeStructureHelper.nodeForKey(root, "tables/table(1)"));
        checkTrackChildNodeWithCreationInvalidKey(nodes);
    }

    /**
     * Tests whether a child node to be tracked is created if necessary.
     */
    @Test
    public void testTrackChildNodeWithCreationNonExisting() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver(false);
        final String childName = "space";
        final String parentKey = "tables/table(0)";
        final String childKey = parentKey + "/" + childName;
        final ImmutableNode node = NodeStructureHelper.nodeForKey(model, parentKey);
        EasyMock.expect(resolver.resolveNodeKey(root, TEST_KEY, model.getNodeHandler())).andReturn(Collections.singletonList(node));
        EasyMock.expect(resolver.nodeKey(EasyMock.anyObject(ImmutableNode.class), EasyMock.eq(new HashMap<>()), EasyMock.anyObject(TreeData.class)))
            .andReturn(childKey);
        EasyMock.replay(resolver);

        final NodeSelector childSelector = model.trackChildNodeWithCreation(TEST_KEY, childName, resolver);
        assertEquals(new NodeSelector(childKey), childSelector, "Wrong selector");
        final ImmutableNode child = model.getTrackedNode(childSelector);
        assertEquals(childName, child.getNodeName(), "Wrong child name");
        assertNull(child.getValue(), "Got a value");
        final ImmutableNode parent = model.getNodeHandler().getParent(child);
        assertEquals("table", parent.getNodeName(), "Wrong parent node");
        assertEquals(child, NodeStructureHelper.nodeForKey(model, childKey), "Wrong node path");
    }

    /**
     * Tests trackChildNodeWithCreation() if the passed in key does not select a node.
     */
    @Test
    public void testTrackChildNodeWithCreationNoResults() {
        checkTrackChildNodeWithCreationInvalidKey(new ArrayList<>());
    }

    /**
     * Tests whether a tracked node is handled correctly if an operation is executed on this node which causes the node to
     * be detached. In this case, the node should be cleared (it makes no sense to use the last defined node instance).
     */
    @Test
    public void testTrackedNodeClearedInOperation() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clearTree(null, selector, resolver);
        assertTrue(model.isTrackedNodeDetached(selector), "Node not detached");
        final ImmutableNode node = model.getTrackedNode(selector);
        assertEquals("table", node.getNodeName(), "Name was changed");
        assertFalse(model.getNodeHandler().isDefined(node), "Node is defined");
    }

    /**
     * Tries to call trackNode() with a key that selects multiple results.
     */
    @Test
    public void testTrackNodeKeyMultipleResults() {
        final NodeSelector nodeSelector = new NodeSelector("tables.table.fields.field.name");
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        assertThrows(ConfigurationRuntimeException.class, () -> model.trackNode(nodeSelector, resolver));
    }

    /**
     * Tries to call trackNode() with a key that does not yield any results.
     */
    @Test
    public void testTrackNodeKeyNoResults() {
        final NodeSelector nodeSelector = new NodeSelector("tables.unknown");
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        assertThrows(ConfigurationRuntimeException.class, () -> model.trackNode(nodeSelector, resolver));
    }

    /**
     * Tests whether a single node can be tracked multiple times.
     */
    @Test
    public void testTrackNodeMultipleTimes() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.trackNode(selector, resolver);
        model.untrackNode(selector);
        assertNotNull(model.getTrackedNode(selector), "No tracked node");
    }

    /**
     * Tests whether tracking of a node can be stopped.
     */
    @Test
    public void testUntrackNode() {
        model.trackNode(selector, createResolver());
        model.untrackNode(selector);
        assertThrows(ConfigurationRuntimeException.class, () -> model.getTrackedNode(selector), "Could get untracked node!");
    }

    /**
     * Tries to stop tracking of a node which is not tracked.
     */
    @Test
    public void testUntrackNodeNonExisting() {
        assertThrows(ConfigurationRuntimeException.class, () -> model.untrackNode(selector));
    }
}
