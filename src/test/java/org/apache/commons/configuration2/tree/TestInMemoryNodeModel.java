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

import static org.apache.commons.configuration2.tree.NodeStructureHelper.ROOT_AUTHORS_TREE;
import static org.apache.commons.configuration2.tree.NodeStructureHelper.ROOT_PERSONAE_TREE;
import static org.apache.commons.configuration2.tree.NodeStructureHelper.nodeForKey;
import static org.apache.commons.configuration2.tree.NodeStructureHelper.nodePathWithEndNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code InMemoryNodeModel}.
 *
 */
public class TestInMemoryNodeModel {
    /** Constant for a test key. */
    private static final String KEY = "aTestKey";

    /**
     * Helper method for checking whether the expected nodes are encountered on a path from a start node to the root node.
     *
     * @param model the node model
     * @param node the start node in the path
     * @param path an array with the expected node names on the path
     */
    private static void checkPathToRoot(final InMemoryNodeModel model, ImmutableNode node, final String... path) {
        final NodeHandler<ImmutableNode> handler = model.getNodeHandler();
        for (int i = path.length - 1; i >= 0; i--) {
            node = handler.getParent(node);
            assertEquals(path[i], node.getNodeName(), "Wrong node name");
        }
        assertSame(model.getRootNode(), handler.getParent(node), "Wrong root node");
    }

    /**
     * Creates a mock for a {@code NodeKeyResolver}.
     *
     * @return the mock for the resolver
     */
    private static NodeKeyResolver<ImmutableNode> createResolver() {
        return EasyMock.createMock(NodeKeyResolver.class);
    }

    /**
     * Helper method for testing the behavior of addNodes() if no nodes to be added are provided.
     *
     * @param newNodes the collection with new nodes
     */
    private void checkAddNodesNoNodes(final Collection<ImmutableNode> newNodes) {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(NodeStructureHelper.ROOT_AUTHORS_TREE);
        EasyMock.replay(resolver);

        model.addNodes(KEY, newNodes, resolver);
        assertSame(NodeStructureHelper.ROOT_AUTHORS_TREE, model.getRootNode(), "Model was changed");
    }

    /**
     * Helper method for testing whether nodes removed from the model can no longer be looked up in the parent mapping.
     *
     * @param pathToRemove the path to the node to be removed
     * @param nodeToCheck the node to check in the parent mapping
     */
    private void checkClearTreeUpdatedParentMapping(final String pathToRemove, final ImmutableNode nodeToCheck) {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        final QueryResult<ImmutableNode> result = QueryResult.createNodeResult(nodeForKey(model, pathToRemove));
        EasyMock.expect(resolver.resolveKey(ROOT_AUTHORS_TREE, KEY, model.getNodeHandler())).andReturn(Collections.singletonList(result));
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        final NodeHandler<ImmutableNode> nodeHandler = model.getNodeHandler();
        assertThrows(IllegalArgumentException.class, () -> nodeHandler.getParent(nodeToCheck), "Removed node still in parent mapping!");
    }

    /**
     * Tests an add nodes operation if an empty collection is passed in.
     */
    @Test
    public void testAddNodesEmptyCollection() {
        checkAddNodesNoNodes(Collections.<ImmutableNode>emptySet());
    }

    /**
     * Tests an add nodes operation if a null collection is passed in.
     */
    @Test
    public void testAddNodesNullCollection() {
        checkAddNodesNoNodes(null);
    }

    /**
     * Tries to add new nodes if the key references an attribute.
     */
    @Test
    public void testAddNodesToAttribute() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(NodeStructureHelper.ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveKey(NodeStructureHelper.ROOT_AUTHORS_TREE, KEY, model.getNodeHandler()))
            .andReturn(Collections.singletonList(QueryResult.createAttributeResult(nodeForKey(model, NodeStructureHelper.author(1)), "test")));
        EasyMock.replay(resolver);

        final ImmutableNode newNode = new ImmutableNode.Builder().name("newNode").create();
        final Set<ImmutableNode> nodes = Collections.singleton(newNode);
        assertThrows(IllegalArgumentException.class, () -> model.addNodes(KEY, nodes, resolver));
    }

    /**
     * Tests whether new nodes can be added to an existing node in the model.
     */
    @Test
    public void testAddNodesToExistingNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(NodeStructureHelper.ROOT_AUTHORS_TREE);
        final String key = NodeStructureHelper.author(0);
        final ImmutableNode newWork1 = new ImmutableNode.Builder().name("King Lear").create();
        final ImmutableNode newWork2 = new ImmutableNode.Builder().name("The Taming of the Shrew").create();
        EasyMock.expect(resolver.resolveKey(NodeStructureHelper.ROOT_AUTHORS_TREE, KEY, model.getNodeHandler()))
            .andReturn(Collections.singletonList(QueryResult.createNodeResult(nodeForKey(model, key))));
        EasyMock.replay(resolver);

        model.addNodes(KEY, Arrays.asList(newWork1, newWork2), resolver);
        final ImmutableNode node = nodeForKey(model, key);
        final int size = node.getChildren().size();
        assertSame(newWork1, node.getChildren().get(size - 2), "New child 1 not added");
        assertSame(newWork2, node.getChildren().get(size - 1), "New child 2 not added");
    }

    /**
     * Tries to add new nodes to an non-existing key pointing to an attribute.
     */
    @Test
    public void testAddNodesToNewAttributeKey() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(NodeStructureHelper.ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveKey(NodeStructureHelper.ROOT_AUTHORS_TREE, KEY, model.getNodeHandler()))
            .andReturn(Collections.<QueryResult<ImmutableNode>>emptyList());
        EasyMock.expect(resolver.resolveAddKey(NodeStructureHelper.ROOT_AUTHORS_TREE, KEY, model.getNodeHandler()))
            .andReturn(new NodeAddData<>(NodeStructureHelper.ROOT_AUTHORS_TREE, "test", true, null));
        EasyMock.replay(resolver);

        final ImmutableNode newNode = new ImmutableNode.Builder().name("newNode").create();
        final Set<ImmutableNode> nodes = Collections.singleton(newNode);
        assertThrows(IllegalArgumentException.class, () -> model.addNodes(KEY, nodes, resolver));
    }

    /**
     * Tests whether nodes can be added to a node which has to be created.
     */
    @Test
    public void testAddNodesToNewNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(NodeStructureHelper.ROOT_AUTHORS_TREE);
        final String newAuthor = "Goethe";
        final String newWork = "Faust";
        final String newPersona = "Mephisto";
        EasyMock.expect(resolver.resolveKey(NodeStructureHelper.ROOT_AUTHORS_TREE, KEY, model.getNodeHandler())).andReturn(new ArrayList<>(0));
        EasyMock.expect(resolver.resolveAddKey(NodeStructureHelper.ROOT_AUTHORS_TREE, KEY, model.getNodeHandler()))
            .andReturn(new NodeAddData<>(NodeStructureHelper.ROOT_AUTHORS_TREE, newWork, false, Arrays.asList(newAuthor)));
        EasyMock.replay(resolver);

        final ImmutableNode personaNode = new ImmutableNode.Builder().name(newPersona).create();
        model.addNodes(KEY, Collections.singleton(personaNode), resolver);
        assertSame(personaNode, nodeForKey(model, newAuthor + "/" + newWork + "/" + newPersona), "Wrong added node");
    }

    /**
     * Tests whether an attribute property can be added if there are no path nodes.
     */
    @Test
    public void testAddPropertyAttributeNoPathNodes() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final NodeAddData<ImmutableNode> addData = new NodeAddData<>(nodeForKey(ROOT_AUTHORS_TREE, "Shakespeare/The Tempest"), "year", true, null);
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY, model.getNodeHandler())).andReturn(addData);
        EasyMock.replay(resolver);

        model.addProperty(KEY, Collections.singleton(1611), resolver);
        final ImmutableNode node = nodeForKey(model, "Shakespeare/The Tempest");
        assertEquals(1611, node.getAttributes().get("year"), "Attribute not set");
    }

    /**
     * Tests whether an attribute can be added if there are some path nodes.
     */
    @Test
    public void testAddPropertyAttributeWithPathNodes() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final NodeAddData<ImmutableNode> addData = new NodeAddData<>(nodeForKey(ROOT_AUTHORS_TREE, "Homer/Ilias"), "number", true,
            Arrays.asList("scenes", "scene"));
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY, model.getNodeHandler())).andReturn(addData);
        EasyMock.replay(resolver);

        model.addProperty(KEY, Collections.singleton(1), resolver);
        final ImmutableNode node = nodeForKey(model, "Homer/Ilias/scenes/scene");
        assertEquals(1, node.getAttributes().get("number"), "Attribute not set");
    }

    /**
     * Tests the special case that an attribute is added with a single path node.
     */
    @Test
    public void testAddPropertyAttributeWithSinglePathNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final NodeAddData<ImmutableNode> addData = new NodeAddData<>(nodeForKey(ROOT_AUTHORS_TREE, NodeStructureHelper.author(0)), "year", true,
            Arrays.asList("dateOfBirth"));
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY, model.getNodeHandler())).andReturn(addData);
        EasyMock.replay(resolver);

        final Integer year = 1564;
        model.addProperty(KEY, Collections.singleton(year), resolver);
        final ImmutableNode node = nodeForKey(model, "Shakespeare/dateOfBirth");
        assertEquals(year, node.getAttributes().get("year"), "Attribute not set");
    }

    /**
     * Tests whether a property can be added if there are no intermediate path nodes.
     */
    @Test
    public void testAddPropertyNoPathNodes() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final NodeAddData<ImmutableNode> addData = new NodeAddData<>(nodeForKey(ROOT_AUTHORS_TREE, "Homer"), "work", false, null);
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY, model.getNodeHandler())).andReturn(addData);
        EasyMock.replay(resolver);

        model.addProperty(KEY, Collections.singleton("Odyssee"), resolver);
        final ImmutableNode node = nodeForKey(model, "Homer/work");
        assertEquals("Odyssee", node.getValue(), "Wrong node value");
        assertNotNull(nodeForKey(model, "Homer/Ilias/Hektor"), "Could not find other nodes");
    }

    /**
     * Tests an addProperty() operation if no values are provided.
     */
    @Test
    public void testAddPropertyNoValues() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        EasyMock.replay(resolver);
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);

        model.addProperty(KEY, Collections.emptySet(), resolver);
        assertSame(ROOT_AUTHORS_TREE, model.getRootNode(), "Root node was changed");
    }

    /**
     * Tests whether the parent node references are updated when nodes are added.
     */
    @Test
    public void testAddPropertyUpdateParentReferences() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final NodeAddData<ImmutableNode> addData = new NodeAddData<>(nodeForKey(ROOT_AUTHORS_TREE, "Homer/Ilias"), "location", false,
            Collections.singleton("locations"));
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY, model.getNodeHandler())).andReturn(addData);
        EasyMock.replay(resolver);
        final String[] locations = {"Troja", "Beach", "Olympos"};

        model.addProperty(KEY, Arrays.asList(locations), resolver);
        final String[] path = {"Homer", "Ilias", "locations"};
        final ImmutableNode node = nodeForKey(model, nodePathWithEndNode("location(1)", path));
        checkPathToRoot(model, node, path);
    }

    /**
     * Tests whether a property can be added to the node model if there are some additional path nodes to be created.
     */
    @Test
    public void testAddPropertyWithPathNodes() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final NodeAddData<ImmutableNode> addData = new NodeAddData<>(nodeForKey(ROOT_AUTHORS_TREE, "Homer/Ilias"), "location", false,
            Collections.singleton("locations"));
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY, model.getNodeHandler())).andReturn(addData);
        EasyMock.replay(resolver);
        final String[] locations = {"Troja", "Beach", "Olympos"};

        model.addProperty(KEY, Arrays.asList(locations), resolver);
        final ImmutableNode nodeLocs = nodeForKey(model, "Homer/Ilias/locations");
        assertEquals(locations.length, nodeLocs.getChildren().size(), "Wrong number of children");
        int idx = 0;
        for (final ImmutableNode c : nodeLocs) {
            assertEquals("location", c.getNodeName(), "Wrong node name");
            assertEquals(locations[idx], c.getValue(), "Wrong value");
            assertTrue(c.getChildren().isEmpty(), "Got children");
            assertTrue(c.getAttributes().isEmpty(), "Got attributes");
            idx++;
        }
        assertNotNull(nodeForKey(model, "Homer/Ilias/Hektor"), "Could not find other nodes");
    }

    /**
     * Tests whether the whole node structure can be cleared.
     */
    @Test
    public void testClear() {
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        model.clear(createResolver());
        assertFalse(model.getNodeHandler().isDefined(model.getRootNode()), "Got still data");
        assertEquals(ROOT_AUTHORS_TREE.getNodeName(), model.getRootNode().getNodeName(), "Root name was changed");
    }

    /**
     * Tests whether a property value stored as an attribute can be cleared.
     */
    @Test
    public void testClearPropertyAttribute() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        final String nodeKey = "Prospero/The Tempest/" + NodeStructureHelper.ELEM_ORG_VALUE;
        EasyMock.expect(resolver.resolveKey(model.getRootNode(), KEY, model.getNodeHandler()))
            .andReturn(Collections.singletonList(QueryResult.createAttributeResult(nodeForKey(model, nodeKey), NodeStructureHelper.ATTR_TESTED)));
        EasyMock.replay(resolver);

        model.clearProperty(KEY, resolver);
        final ImmutableNode node = nodeForKey(model, nodeKey);
        assertTrue(node.getAttributes().isEmpty(), "Attribute not removed");
    }

    /**
     * Tests whether a property value can be cleared on a node.
     */
    @Test
    public void testClearPropertyNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        final String nodeKey = "Ariel/The Tempest/" + NodeStructureHelper.ELEM_ORG_VALUE;
        EasyMock.expect(resolver.resolveKey(model.getRootNode(), KEY, model.getNodeHandler()))
            .andReturn(Collections.singletonList(QueryResult.createNodeResult(nodeForKey(model, nodeKey))));
        EasyMock.replay(resolver);

        model.clearProperty(KEY, resolver);
        final ImmutableNode node = nodeForKey(model, nodeKey);
        assertNull(node.getValue(), "Value not cleared");
    }

    /**
     * Tests clearProperty() for a non existing property.
     */
    @Test
    public void testClearPropertyNonExisting() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        EasyMock.expect(resolver.resolveKey(model.getRootNode(), KEY, model.getNodeHandler())).andReturn(Collections.<QueryResult<ImmutableNode>>emptyList());
        EasyMock.replay(resolver);

        final TreeData treeDataOld = model.getTreeData();
        model.clearProperty(KEY, resolver);
        assertNotNull(model.getNodeHandler().getRootNode(), "No root node");
        assertSame(treeDataOld, model.getTreeData(), "Data was changed");
    }

    /**
     * Tests whether attributes can be cleared with clearTree().
     */
    @Test
    public void testClearTreeAttribute() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        final String nodeName = "Puck";
        final QueryResult<ImmutableNode> result = QueryResult.createAttributeResult(nodeForKey(model, nodeName), NodeStructureHelper.ATTR_AUTHOR);
        EasyMock.expect(resolver.resolveKey(ROOT_PERSONAE_TREE, KEY, model.getNodeHandler())).andReturn(Collections.singletonList(result));
        EasyMock.replay(resolver);

        final List<QueryResult<ImmutableNode>> removed = model.clearTree(KEY, resolver);
        final ImmutableNode node = nodeForKey(model, nodeName);
        assertTrue(node.getAttributes().isEmpty(), "Got still attributes");
        assertEquals(1, removed.size(), "Wrong number of removed elements");
        assertTrue(removed.contains(result), "Wrong removed element");
    }

    /**
     * Tests whether the children of removed nodes are also removed from the parent mapping.
     */
    @Test
    public void testClearTreeChildrenRemovedFromParentMapping() {
        final String path = "Homer/Ilias";
        checkClearTreeUpdatedParentMapping(path, nodeForKey(ROOT_AUTHORS_TREE, path + "/Achilles"));
    }

    /**
     * Tests whether a removed node can no longer be passed to getParent().
     */
    @Test
    public void testClearTreeNodeRemovedFromParentMapping() {
        final String path = "Homer/Ilias/Achilles";
        checkClearTreeUpdatedParentMapping(path, nodeForKey(ROOT_AUTHORS_TREE, path));
    }

    /**
     * Tests whether a clearTree() operation can be performed if only nodes are involved.
     */
    @Test
    public void testClearTreeNodes() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        final QueryResult<ImmutableNode> result = QueryResult.createNodeResult(nodeForKey(model, "Homer/Ilias/Achilles"));
        EasyMock.expect(resolver.resolveKey(ROOT_AUTHORS_TREE, KEY, model.getNodeHandler())).andReturn(Collections.singletonList(result));
        EasyMock.replay(resolver);

        final List<QueryResult<ImmutableNode>> removed = model.clearTree(KEY, resolver);
        final ImmutableNode node = nodeForKey(model, "Homer/Ilias");
        assertEquals(2, node.getChildren().size(), "Wrong number of children");
        for (final ImmutableNode c : node) {
            assertNotEquals(result.getNode().getNodeName(), c.getNodeName(), "Node still found");
        }
        assertEquals(1, removed.size(), "Wrong number of removed elements");
        assertTrue(removed.contains(result), "Wrong removed element");
    }

    /**
     * Tests whether both nodes and attributes can be removed by a clearTree() operation. We remove all attributes and
     * children from a node. The node becomes undefined and should be removed.
     */
    @Test
    public void testClearTreeNodesAndAttributes() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        final String nodeName = "Puck";
        final ImmutableNode orgNode = nodeForKey(model, nodeName);
        final List<QueryResult<ImmutableNode>> results = new ArrayList<>(2);
        results.add(QueryResult.createAttributeResult(orgNode, NodeStructureHelper.ATTR_AUTHOR));
        results.add(QueryResult.createNodeResult(orgNode.getChildren().get(0)));
        EasyMock.expect(resolver.resolveKey(ROOT_PERSONAE_TREE, KEY, model.getNodeHandler())).andReturn(results);
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        assertThrows(NoSuchElementException.class, () -> nodeForKey(model, nodeName), "Node still present!");
    }

    /**
     * Tests clearTree() if the passed in key does not exist.
     */
    @Test
    public void testClearTreeNonExistingKey() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        EasyMock.expect(resolver.resolveKey(ROOT_PERSONAE_TREE, KEY, model.getNodeHandler())).andReturn(Collections.<QueryResult<ImmutableNode>>emptyList());
        EasyMock.replay(resolver);

        final TreeData treeDataOld = model.getTreeData();
        assertTrue(model.clearTree(KEY, resolver).isEmpty(), "Elements removed");
        assertNotNull(model.getNodeHandler().getRootNode(), "No root node");
        assertSame(treeDataOld, model.getTreeData(), "Data was changed");
    }

    /**
     * Tests whether undefined nodes are removed from the hierarchy when clearing properties.
     */
    @Test
    public void testClearTreeRemoveUndefinedNodes() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        final ImmutableNode node = nodeForKey(model, "Homer/Ilias");
        final List<QueryResult<ImmutableNode>> results = new ArrayList<>(node.getChildren().size());
        for (final ImmutableNode child : node) {
            results.add(QueryResult.createNodeResult(child));
        }
        EasyMock.expect(resolver.resolveKey(ROOT_AUTHORS_TREE, KEY, model.getNodeHandler())).andReturn(results);
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        assertEquals(NodeStructureHelper.authorsLength() - 1, model.getRootNode().getChildren().size(), "Child of root not removed");
        for (final ImmutableNode child : model.getRootNode()) {
            assertNotEquals("Homer", child.getNodeName(), "Child still found");
        }
    }

    /**
     * Tests a clearTree() operation which should yield an empty tree structure.
     */
    @Test
    public void testClearTreeResultIsEmpty() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final ImmutableNode child = new ImmutableNode.Builder().name("child").value("test").create();
        final ImmutableNode root = new ImmutableNode.Builder(1).addChild(child).create();
        final InMemoryNodeModel model = new InMemoryNodeModel(root);
        EasyMock.expect(resolver.resolveKey(root, KEY, model.getNodeHandler())).andReturn(Collections.singletonList(QueryResult.createNodeResult(child)));
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        assertFalse(model.getNodeHandler().isDefined(model.getRootNode()), "Root node still defined");
    }

    /**
     * Tests whether clearTree() handles the root node in a special way.
     */
    @Test
    public void testClearTreeRootNode() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        final List<QueryResult<ImmutableNode>> results = new ArrayList<>(2);
        results.add(QueryResult.createNodeResult(nodeForKey(model, NodeStructureHelper.author(0))));
        results.add(QueryResult.createNodeResult(ROOT_AUTHORS_TREE));
        EasyMock.expect(resolver.resolveKey(ROOT_AUTHORS_TREE, KEY, model.getNodeHandler())).andReturn(results);
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        assertFalse(model.getNodeHandler().isDefined(model.getRootNode()), "Got still data");
    }

    /**
     * Tests whether references to parent nodes are updated correctly when clearing properties.
     */
    @Test
    public void testClearTreeUpdateParentReferences() {
        final String[] path = {"Homer", "Ilias"};
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        final QueryResult<ImmutableNode> result = QueryResult.createNodeResult(nodeForKey(model, nodePathWithEndNode("Achilles", path)));
        EasyMock.expect(resolver.resolveKey(ROOT_AUTHORS_TREE, KEY, model.getNodeHandler())).andReturn(Collections.singletonList(result));
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        checkPathToRoot(model, nodeForKey(model, nodePathWithEndNode("Hektor", path)), path);
    }

    /**
     * Tests whether the replacement mapping is automatically compacted if it gets too large.
     */
    @Test
    public void testCompactReplacementMapping() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        final int numberOfOperations = 200;
        final String key = "Homer/Ilias";
        for (int i = 0; i < numberOfOperations; i++) {
            final int index = i;
            EasyMock.expect(resolver.resolveAddKey(EasyMock.anyObject(ImmutableNode.class), EasyMock.eq(KEY), EasyMock.anyObject(TreeData.class)))
                .andAnswer(() -> {
                    assertSame(model.getRootNode(), EasyMock.getCurrentArguments()[0], "Wrong root node");
                    final ImmutableNode addParent = nodeForKey(model, key);
                    return new NodeAddData<>(addParent, "Warrior" + index, false, null);
                });
        }
        EasyMock.replay(resolver);

        for (int i = 0; i < numberOfOperations; i++) {
            model.addProperty(KEY, Collections.singleton(i), resolver);
        }
        final ImmutableNode orgNode = nodeForKey(ROOT_AUTHORS_TREE, key);
        final ImmutableNode changedNode = nodeForKey(model, key);
        assertEquals(orgNode.getChildren().size() + numberOfOperations, changedNode.getChildren().size(), "Wrong number of children");
        final Map<ImmutableNode, ImmutableNode> replacementMapping = model.getTreeData().copyReplacementMapping();
        assertTrue(replacementMapping.size() < numberOfOperations, "Replacement mapping too big: " + replacementMapping.size());
    }

    /**
     * Tests whether concurrent updates of the model are handled correctly. This test adds a number of authors in parallel.
     * Then it is checked whether all authors have been added correctly.
     */
    @Test
    public void testConcurrentUpdate() throws InterruptedException {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(NodeStructureHelper.ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveAddKey(EasyMock.anyObject(ImmutableNode.class), EasyMock.eq(KEY), EasyMock.anyObject(TreeData.class))).andAnswer(() -> {
            final ImmutableNode addParent = (ImmutableNode) EasyMock.getCurrentArguments()[0];
            return new NodeAddData<>(addParent, "name", false, Collections.singleton("author"));
        }).anyTimes();
        EasyMock.replay(resolver);

        final CountDownLatch latch = new CountDownLatch(1);
        final String authorPrefix = "newAuthor";
        final int threadCount = 32;
        final Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final String authorName = authorPrefix + i;
            threads[i] = new Thread(() -> {
                try {
                    latch.await();
                    model.addProperty(KEY, Collections.singleton(authorName), resolver);
                } catch (final InterruptedException iex) {
                    // ignore
                }
            });
            threads[i].start();
        }
        latch.countDown();
        for (final Thread t : threads) {
            t.join();
        }

        final Pattern patternAuthorName = Pattern.compile(Pattern.quote(authorPrefix) + "(\\d+)");
        final Set<Integer> indices = new HashSet<>();
        for (int i = 0; i < threadCount; i++) {
            final ImmutableNode node = nodeForKey(model, "author(" + i + ")/name");
            final Matcher m = patternAuthorName.matcher(String.valueOf(node.getValue()));
            assertTrue(m.matches(), "Wrong value: " + node.getValue());
            final int idx = Integer.parseInt(m.group(1));
            assertTrue(idx >= 0 && idx < threadCount, "Invalid index: " + idx);
            indices.add(idx);
        }
        assertEquals(threadCount, indices.size(), "Not all authors were created");
    }

    /**
     * Tests whether the model's data can be represented as immutable node objects (which is trivial in this case).
     */
    @Test
    public void testGetInMemoryRepresentation() {
        final InMemoryNodeModel model = new InMemoryNodeModel(NodeStructureHelper.ROOT_AUTHORS_TREE);
        assertSame(NodeStructureHelper.ROOT_AUTHORS_TREE, model.getInMemoryRepresentation(), "Wrong in-memory representation");
    }

    /**
     * Tests whether the correct node handler is returned.
     */
    @Test
    public void testGetNodeHandler() {
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        assertSame(model.getTreeData(), model.getNodeHandler(), "Wrong node handler");
    }

    /**
     * Tests whether the correct root node is returned if a tree was passed at construction time.
     */
    @Test
    public void testGetRootNodeFromConstructor() {
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        assertSame(ROOT_AUTHORS_TREE, model.getRootNode(), "Wrong root node");
    }

    /**
     * Tests whether an undefined default root node is created if none is specified.
     */
    @Test
    public void testInitDefaultRoot() {
        final InMemoryNodeModel model = new InMemoryNodeModel();
        final ImmutableNode root = model.getRootNode();
        assertNull(root.getNodeName(), "Got a name");
        assertNull(root.getValue(), "Got a value");
        assertTrue(root.getChildren().isEmpty(), "Got children");
        assertTrue(root.getAttributes().isEmpty(), "Got attributes");
    }

    /**
     * Tests whether setProperty() can handle changes in node values.
     */
    @Test
    public void testSetPropertyChangedValues() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        final String nodeKey = "Ariel/The Tempest/" + NodeStructureHelper.ELEM_ORG_VALUE;
        final Map<QueryResult<ImmutableNode>, Object> changedValues = new HashMap<>();
        final String newValue = "of course";
        final ImmutableNode changedNode = nodeForKey(model, nodeKey);
        changedValues.put(QueryResult.createAttributeResult(changedNode, NodeStructureHelper.ATTR_TESTED), newValue);
        changedValues.put(QueryResult.createNodeResult(changedNode), newValue);
        final NodeUpdateData<ImmutableNode> updateData = new NodeUpdateData<>(changedValues, null, null, null);
        EasyMock.expect(resolver.resolveUpdateKey(NodeStructureHelper.ROOT_PERSONAE_TREE, KEY, this, model.getNodeHandler())).andReturn(updateData);
        EasyMock.replay(resolver);

        model.setProperty(KEY, this, resolver);
        final ImmutableNode node = nodeForKey(model, nodeKey);
        assertEquals(newValue, node.getAttributes().get(NodeStructureHelper.ATTR_TESTED), "Attribute value not changed");
        assertEquals(newValue, node.getValue(), "Node value not changed");
    }

    /**
     * Tests whether setProperty() can handle nodes to be cleared.
     */
    @Test
    public void testSetPropertyClearValues() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        final String nodeKey = "Ariel/The Tempest/" + NodeStructureHelper.ELEM_ORG_VALUE;
        final NodeUpdateData<ImmutableNode> updateData = new NodeUpdateData<>(null, null,
            Collections.singletonList(QueryResult.createNodeResult(nodeForKey(model, nodeKey))), null);
        EasyMock.expect(resolver.resolveUpdateKey(NodeStructureHelper.ROOT_PERSONAE_TREE, KEY, this, model.getNodeHandler())).andReturn(updateData);
        EasyMock.replay(resolver);

        model.setProperty(KEY, this, resolver);
        final ImmutableNode node = nodeForKey(model, nodeKey);
        assertNull(node.getValue(), "Value not cleared");
    }

    /**
     * Tests whether setProperty() can handle newly added values.
     */
    @Test
    public void testSetPropertyNewValues() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final NodeAddData<ImmutableNode> addData = new NodeAddData<>(nodeForKey(ROOT_AUTHORS_TREE, "Homer"), "work", false, null);
        final NodeUpdateData<ImmutableNode> updateData = new NodeUpdateData<>(null, Collections.<Object>singleton("Odyssee"), null, KEY);
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveUpdateKey(ROOT_AUTHORS_TREE, KEY, this, model.getNodeHandler())).andReturn(updateData);
        EasyMock.expect(resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY, model.getNodeHandler())).andReturn(addData);
        EasyMock.replay(resolver);

        model.setProperty(KEY, this, resolver);
        final ImmutableNode node = nodeForKey(model, "Homer/work");
        assertEquals("Odyssee", node.getValue(), "Wrong node value");
        assertNotNull(nodeForKey(model, "Homer/Ilias/Hektor"), "Could not find other nodes");
    }

    /**
     * Tests a set property operation which is a no-op.
     */
    @Test
    public void testSetPropertyNoChanges() {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        EasyMock.expect(resolver.resolveUpdateKey(NodeStructureHelper.ROOT_PERSONAE_TREE, KEY, this, model.getNodeHandler()))
            .andReturn(new NodeUpdateData<>(null, null, null, null));
        EasyMock.replay(resolver);

        model.setProperty(KEY, this, resolver);
        assertSame(NodeStructureHelper.ROOT_PERSONAE_TREE, model.getRootNode(), "Model was changed");
    }

    /**
     * Tests whether a new root node can be set.
     */
    @Test
    public void testSetRoot() {
        final InMemoryNodeModel model = new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        model.setRootNode(NodeStructureHelper.ROOT_AUTHORS_TREE);
        assertSame(NodeStructureHelper.ROOT_AUTHORS_TREE, model.getRootNode(), "Root node not changed");
        final ImmutableNode node = nodeForKey(model, "Homer/Ilias");
        assertEquals(nodeForKey(model, "Homer"), model.getNodeHandler().getParent(node), "Wrong parent mapping");
    }

    /**
     * Tests whether the root node can be set to null.
     */
    @Test
    public void testSetRootNull() {
        final InMemoryNodeModel model = new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        model.setRootNode(null);
        final ImmutableNode rootNode = model.getRootNode();
        assertTrue(rootNode.getChildren().isEmpty(), "Got children");
    }
}
