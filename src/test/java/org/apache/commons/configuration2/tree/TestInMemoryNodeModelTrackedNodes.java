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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A special test class for {@code InMemoryNodeModel} which tests the facilities for tracking nodes.
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
        assertEquals(NodeStructureHelper.fieldsLength(1), nodeFields.getChildren().size());
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
        assertEquals("field", nodeField.getNodeName());
        assertEquals(1, nodeField.getChildren().size());
        final ImmutableNode nodeName = nodeField.getChildren().get(0);
        assertEquals("name", nodeName.getNodeName());
        assertEquals(name, nodeName.getValue());
    }

    /**
     * Tests whether a field node was added.
     *
     * @param nodeFields the fields node
     */
    private static void checkForAddedField(final ImmutableNode nodeFields) {
        assertEquals(NodeStructureHelper.fieldsLength(1) + 1, nodeFields.getChildren().size());
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
        assertEquals(NodeStructureHelper.fieldsLength(1) - 1, nodeFields.getChildren().size());
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
        assertEquals(expectedNames, actualNames);
    }

    /**
     * Creates a default resolver which supports arbitrary queries on a target node.
     *
     * @return the resolver
     */
    private static NodeKeyResolver<ImmutableNode> createResolver() {
        final NodeKeyResolver<ImmutableNode> resolver = NodeStructureHelper.createResolverMock();
        NodeStructureHelper.prepareResolveKeyForQueries(resolver);
        return resolver;
    }

    /**
     * Prepares a mock for a resolver to handle keys for update operations. Support is limited. It is expected that only a
     * single value is changed.
     *
     * @param resolver the {@code NodeKeyResolver} mock
     */
    private static void prepareResolverForUpdateKeys(final NodeKeyResolver<ImmutableNode> resolver) {
        when(resolver.resolveUpdateKey(any(), any(), any(), any())).thenAnswer(invocation -> {
            final ImmutableNode root = invocation.getArgument(0, ImmutableNode.class);
            final String key = invocation.getArgument(1, String.class);
            final TreeData handler = invocation.getArgument(3, TreeData.class);
            final List<QueryResult<ImmutableNode>> results = DefaultExpressionEngine.INSTANCE.query(root, key, handler);
            assertEquals(1, results.size());
            return new NodeUpdateData<>(Collections.singletonMap(results.get(0), invocation.getArgument(2)), null, null, null);
        });
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
        assertSame(newNode, model.getTrackedNode(selector));
        assertTrue(model.isTrackedNodeDetached(selector));
    }

    /**
     * Checks trackChildNodes() if the passed in key has a result set which causes the operation to be aborted.
     *
     * @param queryResult the result set of the key
     */
    private void checkTrackChildNodesNoResult(final List<ImmutableNode> queryResult) {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        when(resolver.resolveNodeKey(root, TEST_KEY, model.getNodeHandler())).thenReturn(queryResult);

        final TreeData oldData = model.getTreeData();

        assertTrue(model.trackChildNodes(TEST_KEY, resolver).isEmpty());
        assertSame(oldData, model.getTreeData());
    }

    /**
     * Helper method for testing trackChildNodeWithCreation() if invalid query results are generated.
     *
     * @param queryResult the result set of the key
     */
    private void checkTrackChildNodeWithCreationInvalidKey(final List<ImmutableNode> queryResult) {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        when(resolver.resolveNodeKey(model.getRootNode(), TEST_KEY, model.getNodeHandler())).thenReturn(queryResult);

        assertThrows(ConfigurationRuntimeException.class, () -> model.trackChildNodeWithCreation(TEST_KEY, "someChild", resolver));
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

    /**
     * Prepares the resolver mock to expect a nodeKey() request.
     *
     * @param resolver the {@code NodeKeyResolver}
     * @param node the node whose name is to be resolved
     * @param key the key to be returned for this node
     */
    private void prepareNodeKey(final NodeKeyResolver<ImmutableNode> resolver, final ImmutableNode node, final String key) {
        final Map<ImmutableNode, String> cache = new HashMap<>();
        when(resolver.nodeKey(node, cache, model.getNodeHandler())).thenReturn(key);
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
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        NodeStructureHelper.prepareResolveAddKeys(resolver);
        model.trackNode(selector, resolver);
        initDetachedNode(resolver);
        final ImmutableNode rootNode = model.getRootNode();
        model.addNodes("fields", selector, Collections.singleton(NodeStructureHelper.createFieldNode(NEW_FIELD)), resolver);
        assertSame(rootNode, model.getRootNode());
        checkForAddedField(fieldsNodeFromTrackedNode());
    }

    /**
     * Tests whether an addNodes() operation works on a tracked node.
     */
    @Test
    public void testAddNodesOnTrackedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        NodeStructureHelper.prepareResolveAddKeys(resolver);
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
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        NodeStructureHelper.prepareResolveAddKeys(resolver);
        model.trackNode(selector, resolver);
        initDetachedNode(resolver);
        final ImmutableNode rootNode = model.getRootNode();
        model.addProperty("fields.field(-1).name", selector, Collections.singleton(NEW_FIELD), resolver);
        assertSame(rootNode, model.getRootNode());
        checkForAddedField(fieldsNodeFromTrackedNode());
    }

    /**
     * Tests whether an addProperty() operation works on a tracked node.
     */
    @Test
    public void testAddPropertyOnTrackedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        NodeStructureHelper.prepareResolveAddKeys(resolver);
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
        assertSame(rootNode, model.getRootNode());
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
        assertSame(rootNode, model.getRootNode());
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
        assertSame(node, model.getTrackedNode(selector));
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
        assertSame(node, model.getTrackedNode(selector));
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
        assertEquals(NodeStructureHelper.table(1), node.getChildren().get(0).getValue());
    }

    /**
     * Tests whether a tracked node can be queried even if it was removed from the structure.
     */
    @Test
    public void testGetTrackedNodeAfterUpdateNoLongerExisting() {
        final ImmutableNode node = NodeStructureHelper.nodeForKey(model, "tables/table(1)");
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        initDetachedNode(resolver);
        assertSame(node, model.getTrackedNode(selector));
    }

    /**
     * Tests whether a tracked node can be queried.
     */
    @Test
    public void testGetTrackedNodeExisting() {
        final ImmutableNode node = NodeStructureHelper.nodeForKey(model, "tables/table(1)");
        model.trackNode(selector, createResolver());
        assertSame(node, model.getTrackedNode(selector));
    }

    /**
     * Tests whether a node handler for a tracked node can be queried which is still active.
     */
    @Test
    public void testGetTrackedNodeHandlerActive() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        final NodeHandler<ImmutableNode> handler = model.getTrackedNodeHandler(selector);
        final TrackedNodeHandler tnh = assertInstanceOf(TrackedNodeHandler.class, handler);
        assertSame(model.getTrackedNode(selector), handler.getRootNode());
        assertSame(model.getTreeData(), tnh.getParentHandler());
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
        assertSame(model.getTrackedNode(selector), handler.getRootNode());
        assertInstanceOf(TreeData.class, handler);
        assertNotSame(model.getNodeHandler(), handler);
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
        assertTrue(model.isTrackedNodeDetached(selector));
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
        assertTrue(model.isTrackedNodeDetached(selector));
    }

    /**
     * Tests isDetached() for a life node.
     */
    @Test
    public void testIsDetachedFalseAfterUpdate() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        model.clearProperty("tables.table(1).fields.field(1).name", resolver);
        assertFalse(model.isTrackedNodeDetached(selector));
    }

    /**
     * Tests isDetached() for a node which has just been tracked.
     */
    @Test
    public void testIsDetachedFalseNoUpdates() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        assertFalse(model.isTrackedNodeDetached(selector));
    }

    /**
     * Tests isDetached() for an actually detached node.
     */
    @Test
    public void testIsDetachedTrue() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        initDetachedNode(resolver);
        assertTrue(model.isTrackedNodeDetached(selector));
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
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final String nodeKey1 = "tables/table(0)";
        final String nodeKey2 = "tables/table(1)";
        final ImmutableNode node1 = NodeStructureHelper.nodeForKey(root, nodeKey1);
        final ImmutableNode node2 = NodeStructureHelper.nodeForKey(root, nodeKey2);

        when(resolver.resolveNodeKey(root, TEST_KEY, model.getNodeHandler())).thenReturn(Arrays.asList(node1, node2));
        prepareNodeKey(resolver, node1, nodeKey1);
        prepareNodeKey(resolver, node2, nodeKey2);

        final Collection<NodeSelector> selectors = model.selectAndTrackNodes(TEST_KEY, resolver);
        final Iterator<NodeSelector> it = selectors.iterator();
        NodeSelector sel = it.next();
        assertEquals(new NodeSelector(nodeKey1), sel);
        assertSame(node1, model.getTrackedNode(sel));
        sel = it.next();
        assertEquals(new NodeSelector(nodeKey2), sel);
        assertSame(node2, model.getTrackedNode(sel));
        assertFalse(it.hasNext());
    }

    /**
     * Tests whether selectAndTrackNodes() works for nodes that are already tracked.
     */
    @Test
    public void testSelectAndTrackNodesNodeAlreadyTracked() {
        NodeKeyResolver<ImmutableNode> resolver = createResolver();
        model.trackNode(selector, resolver);
        resolver = createResolver();
        final ImmutableNode node = model.getTrackedNode(selector);

        when(resolver.resolveNodeKey(root, TEST_KEY, model.getNodeHandler())).thenReturn(Collections.singletonList(node));
        prepareNodeKey(resolver, node, SELECTOR_KEY);

        final Collection<NodeSelector> selectors = model.selectAndTrackNodes(TEST_KEY, resolver);
        assertEquals(1, selectors.size());
        assertEquals(selector, selectors.iterator().next());
        model.untrackNode(selector);
        assertSame(node, model.getTrackedNode(selector));
    }

    /**
     * Tests selectAndTrackNodes() if the key does not select any nodes.
     */
    @Test
    public void testSelectAndTrackNodesNoSelection() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();

        when(resolver.resolveNodeKey(root, TEST_KEY, model.getNodeHandler())).thenReturn(Collections.<ImmutableNode>emptyList());

        assertTrue(model.selectAndTrackNodes(TEST_KEY, resolver).isEmpty());
    }

    /**
     * Tests a setProperty() operation on a tracked node that is detached.
     */
    @Test
    public void testSetPropertyOnDetachedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        prepareResolverForUpdateKeys(resolver);
        model.trackNode(selector, resolver);
        initDetachedNode(resolver);
        final ImmutableNode rootNode = model.getRootNode();
        model.setProperty("fields.field(0).name", selector, NEW_FIELD, resolver);
        assertSame(rootNode, model.getRootNode());
        checkedForChangedField(fieldsNodeFromTrackedNode(), 0);
    }

    /**
     * Tests whether a setProperty() operation works on a tracked node.
     */
    @Test
    public void testSetPropertyOnTrackedNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        prepareResolverForUpdateKeys(resolver);
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
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final ImmutableNode node = NodeStructureHelper.nodeForKey(root, "tables");
        final String[] keys = new String[node.getChildren().size()];

        for (int i = 0; i < keys.length; i++) {
            final ImmutableNode child = node.getChildren().get(i);
            keys[i] = String.format("%s.%s(%d)", node.getNodeName(), child.getNodeName(), i);
            prepareNodeKey(resolver, child, keys[i]);
        }
        when(resolver.resolveNodeKey(root, TEST_KEY, model.getNodeHandler())).thenReturn(Collections.singletonList(node));

        final Collection<NodeSelector> selectors = model.trackChildNodes(TEST_KEY, resolver);
        assertEquals(node.getChildren().size(), selectors.size());
        int idx = 0;
        for (final NodeSelector sel : selectors) {
            assertEquals(new NodeSelector(keys[idx]), sel);
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
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final String childName = "name";
        final String parentKey = "tables/table(0)";
        final String childKey = parentKey + "/" + childName;
        final ImmutableNode node = NodeStructureHelper.nodeForKey(model, parentKey);
        final ImmutableNode child = NodeStructureHelper.nodeForKey(node, childName);

        when(resolver.resolveNodeKey(root, TEST_KEY, model.getNodeHandler())).thenReturn(Collections.singletonList(node));
        prepareNodeKey(resolver, child, childKey);

        final NodeSelector childSelector = model.trackChildNodeWithCreation(TEST_KEY, childName, resolver);
        assertEquals(new NodeSelector(childKey), childSelector);
        assertSame(child, model.getTrackedNode(childSelector));
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
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final String childName = "space";
        final String parentKey = "tables/table(0)";
        final String childKey = parentKey + "/" + childName;
        final ImmutableNode node = NodeStructureHelper.nodeForKey(model, parentKey);

        when(resolver.resolveNodeKey(root, TEST_KEY, model.getNodeHandler())).thenReturn(Collections.singletonList(node));
        when(resolver.nodeKey(any(), eq(new HashMap<>()), any())).thenReturn(childKey);

        final NodeSelector childSelector = model.trackChildNodeWithCreation(TEST_KEY, childName, resolver);
        assertEquals(new NodeSelector(childKey), childSelector);
        final ImmutableNode child = model.getTrackedNode(childSelector);
        assertEquals(childName, child.getNodeName());
        assertNull(child.getValue());
        final ImmutableNode parent = model.getNodeHandler().getParent(child);
        assertEquals("table", parent.getNodeName());
        assertEquals(child, NodeStructureHelper.nodeForKey(model, childKey));
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
        assertTrue(model.isTrackedNodeDetached(selector));
        final ImmutableNode node = model.getTrackedNode(selector);
        assertEquals("table", node.getNodeName());
        assertFalse(model.getNodeHandler().isDefined(node));
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
        assertNotNull(model.getTrackedNode(selector));
    }

    /**
     * Tests whether tracking of a node can be stopped.
     */
    @Test
    public void testUntrackNode() {
        model.trackNode(selector, createResolver());
        model.untrackNode(selector);
        assertThrows(ConfigurationRuntimeException.class, () -> model.getTrackedNode(selector));
    }

    /**
     * Tries to stop tracking of a node which is not tracked.
     */
    @Test
    public void testUntrackNodeNonExisting() {
        assertThrows(ConfigurationRuntimeException.class, () -> model.untrackNode(selector));
    }
}
