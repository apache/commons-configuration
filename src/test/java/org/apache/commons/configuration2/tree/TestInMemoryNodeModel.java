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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.easymock.IAnswer;
import org.junit.Test;

/**
 * Test class for {@code InMemoryNodeModel}.
 *
 */
public class TestInMemoryNodeModel
{
    /** Constant for a test key. */
    private static final String KEY = "aTestKey";

    /**
     * Tests whether an undefined default root node is created if none is
     * specified.
     */
    @Test
    public void testInitDefaultRoot()
    {
        final InMemoryNodeModel model = new InMemoryNodeModel();
        final ImmutableNode root = model.getRootNode();
        assertNull("Got a name", root.getNodeName());
        assertNull("Got a value", root.getValue());
        assertTrue("Got children", root.getChildren().isEmpty());
        assertTrue("Got attributes", root.getAttributes().isEmpty());
    }

    /**
     * Tests whether the correct root node is returned if a tree was passed at
     * construction time.
     */
    @Test
    public void testGetRootNodeFromConstructor()
    {
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        assertSame("Wrong root node", ROOT_AUTHORS_TREE, model.getRootNode());
    }

    /**
     * Tests whether the correct node handler is returned.
     */
    @Test
    public void testGetNodeHandler()
    {
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        assertSame("Wrong node handler", model.getTreeData(), model.getNodeHandler());
    }

    /**
     * Creates a mock for a {@code NodeKeyResolver}.
     *
     * @return the mock for the resolver
     */
    private static NodeKeyResolver<ImmutableNode> createResolver()
    {
        @SuppressWarnings("unchecked")
        final
        NodeKeyResolver<ImmutableNode> resolver =
                EasyMock.createMock(NodeKeyResolver.class);
        return resolver;
    }

    /**
     * Tests whether a property can be added to the node model if there are some
     * additional path nodes to be created.
     */
    @Test
    public void testAddPropertyWithPathNodes()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final NodeAddData<ImmutableNode> addData =
                new NodeAddData<>(nodeForKey(ROOT_AUTHORS_TREE,
                        "Homer/Ilias"), "location", false,
                        Collections.singleton("locations"));
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY, model.getNodeHandler()))
                .andReturn(addData);
        EasyMock.replay(resolver);
        final String[] locations = {
                "Troja", "Beach", "Olympos"
        };

        model.addProperty(KEY, Arrays.asList(locations), resolver);
        final ImmutableNode nodeLocs = nodeForKey(model, "Homer/Ilias/locations");
        assertEquals("Wrong number of children", locations.length, nodeLocs
                .getChildren().size());
        int idx = 0;
        for (final ImmutableNode c : nodeLocs.getChildren())
        {
            assertEquals("Wrong node name", "location", c.getNodeName());
            assertEquals("Wrong value", locations[idx], c.getValue());
            assertTrue("Got children", c.getChildren().isEmpty());
            assertTrue("Got attributes", c.getAttributes().isEmpty());
            idx++;
        }
        assertNotNull("Could not find other nodes",
                nodeForKey(model, "Homer/Ilias/Hektor"));
    }

    /**
     * Tests whether a property can be added if there are no intermediate path
     * nodes.
     */
    @Test
    public void testAddPropertyNoPathNodes()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final NodeAddData<ImmutableNode> addData =
                new NodeAddData<>(nodeForKey(ROOT_AUTHORS_TREE,
                        "Homer"), "work", false, null);
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(
                resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY,
                        model.getNodeHandler())).andReturn(addData);
        EasyMock.replay(resolver);

        model.addProperty(KEY, Collections.singleton("Odyssee"), resolver);
        final ImmutableNode node = nodeForKey(model, "Homer/work");
        assertEquals("Wrong node value", "Odyssee", node.getValue());
        assertNotNull("Could not find other nodes",
                nodeForKey(model, "Homer/Ilias/Hektor"));
    }

    /**
     * Tests whether the parent node references are updated when nodes are
     * added.
     */
    @Test
    public void testAddPropertyUpdateParentReferences()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final NodeAddData<ImmutableNode> addData =
                new NodeAddData<>(nodeForKey(ROOT_AUTHORS_TREE,
                        "Homer/Ilias"), "location", false,
                        Collections.singleton("locations"));
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(
                resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY,
                        model.getNodeHandler())).andReturn(addData);
        EasyMock.replay(resolver);
        final String[] locations = {
                "Troja", "Beach", "Olympos"
        };

        model.addProperty(KEY, Arrays.asList(locations), resolver);
        final String[] path = {
                "Homer", "Ilias", "locations"
        };
        final ImmutableNode node =
                nodeForKey(model, nodePathWithEndNode("location(1)", path));
        checkPathToRoot(model, node, path);
    }

    /**
     * Helper method for checking whether the expected nodes are encountered on
     * a path from a start node to the root node.
     *
     * @param model the node model
     * @param node the start node in the path
     * @param path an array with the expected node names on the path
     */
    private static void checkPathToRoot(final InMemoryNodeModel model,
            ImmutableNode node, final String... path)
    {
        final NodeHandler<ImmutableNode> handler = model.getNodeHandler();
        for (int i = path.length - 1; i >= 0; i--)
        {
            node = handler.getParent(node);
            assertEquals("Wrong node name", path[i], node.getNodeName());
        }
        assertSame("Wrong root node", model.getRootNode(),
                handler.getParent(node));
    }

    /**
     * Tests whether an attribute can be added if there are some path nodes.
     */
    @Test
    public void testAddPropertyAttributeWithPathNodes()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final NodeAddData<ImmutableNode> addData =
                new NodeAddData<>(nodeForKey(ROOT_AUTHORS_TREE,
                        "Homer/Ilias"), "number", true, Arrays.asList("scenes",
                        "scene"));
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(
                resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY,
                        model.getNodeHandler())).andReturn(addData);
        EasyMock.replay(resolver);

        model.addProperty(KEY, Collections.singleton(1), resolver);
        final ImmutableNode node = nodeForKey(model, "Homer/Ilias/scenes/scene");
        assertEquals("Attribute not set", 1, node.getAttributes().get("number"));
    }

    /**
     * Tests the special case that an attribute is added with a single path
     * node.
     */
    @Test
    public void testAddPropertyAttributeWithSinglePathNode()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final NodeAddData<ImmutableNode> addData =
                new NodeAddData<>(nodeForKey(ROOT_AUTHORS_TREE,
                        NodeStructureHelper.author(0)), "year", true,
                        Arrays.asList("dateOfBirth"));
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(
                resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY,
                        model.getNodeHandler())).andReturn(addData);
        EasyMock.replay(resolver);

        final Integer year = 1564;
        model.addProperty(KEY, Collections.singleton(year), resolver);
        final ImmutableNode node = nodeForKey(model, "Shakespeare/dateOfBirth");
        assertEquals("Attribute not set", year, node.getAttributes()
                .get("year"));
    }

    /**
     * Tests whether an attribute property can be added if there are no path
     * nodes.
     */
    @Test
    public void testAddPropertyAttributeNoPathNodes()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final NodeAddData<ImmutableNode> addData =
                new NodeAddData<>(nodeForKey(ROOT_AUTHORS_TREE,
                        "Shakespeare/The Tempest"), "year", true, null);
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(
                resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY,
                        model.getNodeHandler())).andReturn(addData);
        EasyMock.replay(resolver);

        model.addProperty(KEY, Collections.singleton(1611), resolver);
        final ImmutableNode node = nodeForKey(model, "Shakespeare/The Tempest");
        assertEquals("Attribute not set", 1611, node.getAttributes()
                .get("year"));
    }

    /**
     * Tests an addProperty() operation if no values are provided.
     */
    @Test
    public void testAddPropertyNoValues()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        EasyMock.replay(resolver);
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);

        model.addProperty(KEY, Collections.emptySet(), resolver);
        assertSame("Root node was changed", ROOT_AUTHORS_TREE,
                model.getRootNode());
    }

    /**
     * Tests whether a clearTree() operation can be performed if only nodes are
     * involved.
     */
    @Test
    public void testClearTreeNodes()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        final QueryResult<ImmutableNode> result =
                QueryResult.createNodeResult(nodeForKey(model,
                        "Homer/Ilias/Achilles"));
        EasyMock.expect(
                resolver.resolveKey(ROOT_AUTHORS_TREE, KEY,
                        model.getNodeHandler())).andReturn(
                Collections.singletonList(result));
        EasyMock.replay(resolver);

        final List<QueryResult<ImmutableNode>> removed = model.clearTree(KEY, resolver);
        final ImmutableNode node = nodeForKey(model, "Homer/Ilias");
        assertEquals("Wrong number of children", 2, node.getChildren().size());
        for (final ImmutableNode c : node.getChildren())
        {
            assertNotEquals("Node still found", result.getNode().getNodeName(),
                    c.getNodeName());
        }
        assertEquals("Wrong number of removed elements", 1, removed.size());
        assertTrue("Wrong removed element", removed.contains(result));
    }

    /**
     * Helper method for testing whether nodes removed from the model can no
     * longer be looked up in the parent mapping.
     *
     * @param pathToRemove the path to the node to be removed
     * @param nodeToCheck the node to check in the parent mapping
     */
    private void checkClearTreeUpdatedParentMapping(final String pathToRemove,
            final ImmutableNode nodeToCheck)
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        final QueryResult<ImmutableNode> result =
                QueryResult.createNodeResult(nodeForKey(model, pathToRemove));
        EasyMock.expect(
                resolver.resolveKey(ROOT_AUTHORS_TREE, KEY,
                        model.getNodeHandler())).andReturn(
                Collections.singletonList(result));
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        try
        {
            model.getNodeHandler().getParent(nodeToCheck);
            fail("Removed node still in parent mapping!");
        }
        catch (final IllegalArgumentException iaex)
        {
            // expected result
        }
    }

    /**
     * Tests whether a removed node can no longer be passed to getParent().
     */
    @Test
    public void testClearTreeNodeRemovedFromParentMapping()
    {
        final String path = "Homer/Ilias/Achilles";
        checkClearTreeUpdatedParentMapping(path,
                nodeForKey(ROOT_AUTHORS_TREE, path));
    }

    /**
     * Tests whether the children of removed nodes are also removed from the
     * parent mapping.
     */
    @Test
    public void testClearTreeChildrenRemovedFromParentMapping()
    {
        final String path = "Homer/Ilias";
        checkClearTreeUpdatedParentMapping(path,
                nodeForKey(ROOT_AUTHORS_TREE, path + "/Achilles"));
    }

    /**
     * Tests whether references to parent nodes are updated correctly when
     * clearing properties.
     */
    @Test
    public void testClearTreeUpdateParentReferences()
    {
        final String[] path = {
                "Homer", "Ilias"
        };
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        final QueryResult<ImmutableNode> result =
                QueryResult.createNodeResult(nodeForKey(model,
                        nodePathWithEndNode("Achilles", path)));
        EasyMock.expect(
                resolver.resolveKey(ROOT_AUTHORS_TREE, KEY,
                        model.getNodeHandler())).andReturn(
                Collections.singletonList(result));
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        checkPathToRoot(model,
                nodeForKey(model, nodePathWithEndNode("Hektor", path)), path);
    }

    /**
     * Tests whether undefined nodes are removed from the hierarchy when
     * clearing properties.
     */
    @Test
    public void testClearTreeRemoveUndefinedNodes()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        final ImmutableNode node = nodeForKey(model, "Homer/Ilias");
        final List<QueryResult<ImmutableNode>> results =
                new ArrayList<>(node.getChildren()
                        .size());
        for (final ImmutableNode child : node.getChildren())
        {
            results.add(QueryResult.createNodeResult(child));
        }
        EasyMock.expect(
                resolver.resolveKey(ROOT_AUTHORS_TREE, KEY,
                        model.getNodeHandler())).andReturn(results);
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        assertEquals("Child of root not removed",
                NodeStructureHelper.authorsLength() - 1, model.getRootNode()
                        .getChildren().size());
        for (final ImmutableNode child : model.getRootNode().getChildren())
        {
            assertNotEquals("Child still found", "Homer", child.getNodeName());
        }
    }

    /**
     * Tests a clearTree() operation which should yield an empty tree structure.
     */
    @Test
    public void testClearTreeResultIsEmpty()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final ImmutableNode child =
                new ImmutableNode.Builder().name("child").value("test")
                        .create();
        final ImmutableNode root =
                new ImmutableNode.Builder(1).addChild(child).create();
        final InMemoryNodeModel model = new InMemoryNodeModel(root);
        EasyMock.expect(resolver.resolveKey(root, KEY, model.getNodeHandler()))
                .andReturn(
                        Collections.singletonList(QueryResult
                                .createNodeResult(child)));
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        assertFalse("Root node still defined",
                model.getNodeHandler().isDefined(model.getRootNode()));
    }

    /**
     * Tests whether attributes can be cleared with clearTree().
     */
    @Test
    public void testClearTreeAttribute()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        final String nodeName = "Puck";
        final QueryResult<ImmutableNode> result = QueryResult.createAttributeResult(
                nodeForKey(model, nodeName),
                NodeStructureHelper.ATTR_AUTHOR);
        EasyMock.expect(
                resolver.resolveKey(ROOT_PERSONAE_TREE, KEY,
                        model.getNodeHandler())).andReturn(
                Collections.singletonList(result));
        EasyMock.replay(resolver);

        final List<QueryResult<ImmutableNode>> removed = model.clearTree(KEY, resolver);
        final ImmutableNode node = nodeForKey(model, nodeName);
        assertTrue("Got still attributes", node.getAttributes().isEmpty());
        assertEquals("Wrong number of removed elements", 1, removed.size());
        assertTrue("Wrong removed element", removed.contains(result));
    }

    /**
     * Tests whether both nodes and attributes can be removed by a clearTree()
     * operation. We remove all attributes and children from a node. The node
     * becomes undefined and should be removed.
     */
    @Test
    public void testClearTreeNodesAndAttributes()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        final String nodeName = "Puck";
        final ImmutableNode orgNode = nodeForKey(model, nodeName);
        final List<QueryResult<ImmutableNode>> results =
                new ArrayList<>(2);
        results.add(QueryResult.createAttributeResult(orgNode,
                NodeStructureHelper.ATTR_AUTHOR));
        results.add(QueryResult.createNodeResult(orgNode.getChildren().get(0)));
        EasyMock.expect(
                resolver.resolveKey(ROOT_PERSONAE_TREE, KEY,
                        model.getNodeHandler())).andReturn(results);
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        try
        {
            nodeForKey(model, nodeName);
            fail("Node still present!");
        }
        catch (final NoSuchElementException nex)
        {
            // expected
        }
    }

    /**
     * Tests clearTree() if the passed in key does not exist.
     */
    @Test
    public void testClearTreeNonExistingKey()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        EasyMock.expect(
                resolver.resolveKey(ROOT_PERSONAE_TREE, KEY,
                        model.getNodeHandler())).andReturn(
                Collections.<QueryResult<ImmutableNode>> emptyList());
        EasyMock.replay(resolver);

        final TreeData treeDataOld = model.getTreeData();
        assertTrue("Elements removed", model.clearTree(KEY, resolver).isEmpty());
        assertNotNull("No root node", model.getNodeHandler().getRootNode());
        assertSame("Data was changed", treeDataOld, model.getTreeData());
    }

    /**
     * Tests whether the whole node structure can be cleared.
     */
    @Test
    public void testClear()
    {
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        model.clear(createResolver());
        assertFalse("Got still data",
                model.getNodeHandler().isDefined(model.getRootNode()));
        assertEquals("Root name was changed", ROOT_AUTHORS_TREE.getNodeName(),
                model.getRootNode().getNodeName());
    }

    /**
     * Tests whether clearTree() handles the root node in a special way.
     */
    @Test
    public void testClearTreeRootNode()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        final List<QueryResult<ImmutableNode>> results =
                new ArrayList<>(2);
        results.add(QueryResult.createNodeResult(nodeForKey(model,
                NodeStructureHelper.author(0))));
        results.add(QueryResult.createNodeResult(ROOT_AUTHORS_TREE));
        EasyMock.expect(
                resolver.resolveKey(ROOT_AUTHORS_TREE, KEY,
                        model.getNodeHandler())).andReturn(results);
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        assertFalse("Got still data",
                model.getNodeHandler().isDefined(model.getRootNode()));
    }

    /**
     * Tests whether the replacement mapping is automatically compacted if it
     * gets too large.
     */
    @Test
    public void testCompactReplacementMapping()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        final int numberOfOperations = 200;
        final String key = "Homer/Ilias";
        for (int i = 0; i < numberOfOperations; i++)
        {
            final int index = i;
            EasyMock.expect(
                    resolver.resolveAddKey(
                            EasyMock.anyObject(ImmutableNode.class),
                            EasyMock.eq(KEY),
                            EasyMock.anyObject(TreeData.class))).andAnswer(
                    new IAnswer<NodeAddData<ImmutableNode>>() {
                        @Override
                        public NodeAddData<ImmutableNode> answer()
                                throws Throwable {
                            assertSame("Wrong root node", model.getRootNode(),
                                    EasyMock.getCurrentArguments()[0]);
                            final ImmutableNode addParent = nodeForKey(model, key);
                            return new NodeAddData<>(addParent,
                                    "Warrior" + index, false, null);
                        }
                    });
        }
        EasyMock.replay(resolver);

        for (int i = 0; i < numberOfOperations; i++)
        {
            model.addProperty(KEY, Collections.singleton(i), resolver);
        }
        final ImmutableNode orgNode = nodeForKey(ROOT_AUTHORS_TREE, key);
        final ImmutableNode changedNode = nodeForKey(model, key);
        assertEquals("Wrong number of children", orgNode.getChildren().size()
                + numberOfOperations, changedNode.getChildren().size());
        final Map<ImmutableNode, ImmutableNode> replacementMapping =
                model.getTreeData().copyReplacementMapping();
        assertTrue("Replacement mapping too big: " + replacementMapping.size(),
                replacementMapping.size() < numberOfOperations);
    }

    /**
     * Tests whether concurrent updates of the model are handled correctly. This
     * test adds a number of authors in parallel. Then it is checked whether all
     * authors have been added correctly.
     */
    @Test
    public void testConcurrentUpdate() throws InterruptedException
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_AUTHORS_TREE);
        EasyMock.expect(
                resolver.resolveAddKey(EasyMock.anyObject(ImmutableNode.class),
                        EasyMock.eq(KEY), EasyMock.anyObject(TreeData.class)))
                .andAnswer(new IAnswer<NodeAddData<ImmutableNode>>()
                {
                    @Override
                    public NodeAddData<ImmutableNode> answer() throws Throwable
                    {
                        final ImmutableNode addParent =
                                (ImmutableNode) EasyMock.getCurrentArguments()[0];
                        return new NodeAddData<>(addParent,
                                "name", false, Collections.singleton("author"));
                    }
                }).anyTimes();
        EasyMock.replay(resolver);

        final CountDownLatch latch = new CountDownLatch(1);
        final String authorPrefix = "newAuthor";
        final int threadCount = 32;
        final Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++)
        {
            final String authorName = authorPrefix + i;
            threads[i] = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        latch.await();
                        model.addProperty(KEY,
                                Collections.singleton(authorName), resolver);
                    }
                    catch (final InterruptedException iex)
                    {
                        // ignore
                    }
                }
            };
            threads[i].start();
        }
        latch.countDown();
        for (final Thread t : threads)
        {
            t.join();
        }

        final Pattern patternAuthorName =
                Pattern.compile(Pattern.quote(authorPrefix) + "(\\d+)");
        final Set<Integer> indices = new HashSet<>();
        for (int i = 0; i < threadCount; i++)
        {
            final ImmutableNode node = nodeForKey(model, "author(" + i + ")/name");
            final Matcher m =
                    patternAuthorName.matcher(String.valueOf(node.getValue()));
            assertTrue("Wrong value: " + node.getValue(), m.matches());
            final int idx = Integer.parseInt(m.group(1));
            assertTrue("Invalid index: " + idx, idx >= 0 && idx < threadCount);
            indices.add(idx);
        }
        assertEquals("Not all authors were created", threadCount,
                indices.size());
    }

    /**
     * Tests whether a property value can be cleared on a node.
     */
    @Test
    public void testClearPropertyNode()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        final String nodeKey =
                "Ariel/The Tempest/" + NodeStructureHelper.ELEM_ORG_VALUE;
        EasyMock.expect(
                resolver.resolveKey(model.getRootNode(), KEY,
                        model.getNodeHandler())).andReturn(
                Collections.singletonList(QueryResult
                        .createNodeResult(nodeForKey(model, nodeKey))));
        EasyMock.replay(resolver);

        model.clearProperty(KEY, resolver);
        final ImmutableNode node = nodeForKey(model, nodeKey);
        assertNull("Value not cleared", node.getValue());
    }

    /**
     * Tests whether a property value stored as an attribute can be cleared.
     */
    @Test
    public void testClearPropertyAttribute()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        final String nodeKey =
                "Prospero/The Tempest/" + NodeStructureHelper.ELEM_ORG_VALUE;
        EasyMock.expect(
                resolver.resolveKey(model.getRootNode(), KEY,
                        model.getNodeHandler())).andReturn(
                Collections.singletonList(QueryResult.createAttributeResult(
                        nodeForKey(model, nodeKey),
                        NodeStructureHelper.ATTR_TESTED)));
        EasyMock.replay(resolver);

        model.clearProperty(KEY, resolver);
        final ImmutableNode node = nodeForKey(model, nodeKey);
        assertTrue("Attribute not removed", node.getAttributes().isEmpty());
    }

    /**
     * Tests clearProperty() for a non existing property.
     */
    @Test
    public void testClearPropertyNonExisting()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        EasyMock.expect(
                resolver.resolveKey(model.getRootNode(), KEY,
                        model.getNodeHandler())).andReturn(
                Collections.<QueryResult<ImmutableNode>> emptyList());
        EasyMock.replay(resolver);

        final TreeData treeDataOld = model.getTreeData();
        model.clearProperty(KEY, resolver);
        assertNotNull("No root node", model.getNodeHandler().getRootNode());
        assertSame("Data was changed", treeDataOld, model.getTreeData());
    }

    /**
     * Tests whether setProperty() can handle newly added values.
     */
    @Test
    public void testSetPropertyNewValues()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final NodeAddData<ImmutableNode> addData =
                new NodeAddData<>(nodeForKey(ROOT_AUTHORS_TREE,
                        "Homer"), "work", false, null);
        final NodeUpdateData<ImmutableNode> updateData =
                new NodeUpdateData<>(null,
                        Collections.<Object> singleton("Odyssee"), null, KEY);
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(
                resolver.resolveUpdateKey(ROOT_AUTHORS_TREE, KEY, this,
                        model.getNodeHandler())).andReturn(updateData);
        EasyMock.expect(
                resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY,
                        model.getNodeHandler())).andReturn(addData);
        EasyMock.replay(resolver);

        model.setProperty(KEY, this, resolver);
        final ImmutableNode node = nodeForKey(model, "Homer/work");
        assertEquals("Wrong node value", "Odyssee", node.getValue());
        assertNotNull("Could not find other nodes",
                nodeForKey(model, "Homer/Ilias/Hektor"));
    }

    /**
     * Tests whether setProperty() can handle nodes to be cleared.
     */
    @Test
    public void testSetPropertyClearValues()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        final String nodeKey =
                "Ariel/The Tempest/" + NodeStructureHelper.ELEM_ORG_VALUE;
        final NodeUpdateData<ImmutableNode> updateData =
                new NodeUpdateData<>(null, null,
                        Collections.singletonList(QueryResult
                                .createNodeResult(nodeForKey(model, nodeKey))),
                        null);
        EasyMock.expect(
                resolver.resolveUpdateKey(
                        NodeStructureHelper.ROOT_PERSONAE_TREE, KEY, this,
                        model.getNodeHandler())).andReturn(updateData);
        EasyMock.replay(resolver);

        model.setProperty(KEY, this, resolver);
        final ImmutableNode node = nodeForKey(model, nodeKey);
        assertNull("Value not cleared", node.getValue());
    }

    /**
     * Tests whether setProperty() can handle changes in node values.
     */
    @Test
    public void testSetPropertyChangedValues()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        final String nodeKey =
                "Ariel/The Tempest/" + NodeStructureHelper.ELEM_ORG_VALUE;
        final Map<QueryResult<ImmutableNode>, Object> changedValues =
                new HashMap<>();
        final String newValue = "of course";
        final ImmutableNode changedNode = nodeForKey(model, nodeKey);
        changedValues.put(QueryResult.createAttributeResult(changedNode,
                NodeStructureHelper.ATTR_TESTED), newValue);
        changedValues.put(QueryResult.createNodeResult(changedNode), newValue);
        final NodeUpdateData<ImmutableNode> updateData =
                new NodeUpdateData<>(changedValues, null, null,
                        null);
        EasyMock.expect(
                resolver.resolveUpdateKey(
                        NodeStructureHelper.ROOT_PERSONAE_TREE, KEY, this,
                        model.getNodeHandler())).andReturn(updateData);
        EasyMock.replay(resolver);

        model.setProperty(KEY, this, resolver);
        final ImmutableNode node = nodeForKey(model, nodeKey);
        assertEquals("Attribute value not changed", newValue, node
                .getAttributes().get(NodeStructureHelper.ATTR_TESTED));
        assertEquals("Node value not changed", newValue, node.getValue());
    }

    /**
     * Tests a set property operation which is a no-op.
     */
    @Test
    public void testSetPropertyNoChanges()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        EasyMock.expect(
                resolver.resolveUpdateKey(
                        NodeStructureHelper.ROOT_PERSONAE_TREE, KEY, this,
                        model.getNodeHandler())).andReturn(
                new NodeUpdateData<ImmutableNode>(null, null, null, null));
        EasyMock.replay(resolver);

        model.setProperty(KEY, this, resolver);
        assertSame("Model was changed", NodeStructureHelper.ROOT_PERSONAE_TREE,
                model.getRootNode());
    }

    /**
     * Tests whether new nodes can be added to an existing node in the model.
     */
    @Test
    public void testAddNodesToExistingNode()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_AUTHORS_TREE);
        final String key = NodeStructureHelper.author(0);
        final ImmutableNode newWork1 =
                new ImmutableNode.Builder().name("King Lear").create();
        final ImmutableNode newWork2 =
                new ImmutableNode.Builder().name("The Taming of the Shrew")
                        .create();
        EasyMock.expect(
                resolver.resolveKey(NodeStructureHelper.ROOT_AUTHORS_TREE, KEY,
                        model.getNodeHandler())).andReturn(
                Collections.singletonList(QueryResult
                        .createNodeResult(nodeForKey(model, key))));
        EasyMock.replay(resolver);

        model.addNodes(KEY, Arrays.asList(newWork1, newWork2), resolver);
        final ImmutableNode node = nodeForKey(model, key);
        final int size = node.getChildren().size();
        assertSame("New child 1 not added", newWork1,
                node.getChildren().get(size - 2));
        assertSame("New child 2 not added", newWork2,
                node.getChildren().get(size - 1));
    }

    /**
     * Tests whether nodes can be added to a node which has to be created.
     */
    @Test
    public void testAddNodesToNewNode()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_AUTHORS_TREE);
        final String newAuthor = "Goethe";
        final String newWork = "Faust";
        final String newPersona = "Mephisto";
        EasyMock.expect(
                resolver.resolveKey(NodeStructureHelper.ROOT_AUTHORS_TREE, KEY,
                        model.getNodeHandler())).andReturn(
                new ArrayList<QueryResult<ImmutableNode>>(0));
        EasyMock.expect(
                resolver.resolveAddKey(NodeStructureHelper.ROOT_AUTHORS_TREE,
                        KEY, model.getNodeHandler())).andReturn(
                new NodeAddData<>(
                        NodeStructureHelper.ROOT_AUTHORS_TREE, newWork, false,
                        Arrays.asList(newAuthor)));
        EasyMock.replay(resolver);

        final ImmutableNode personaNode =
                new ImmutableNode.Builder().name(newPersona).create();
        model.addNodes(KEY, Collections.singleton(personaNode), resolver);
        assertSame("Wrong added node", personaNode,
                nodeForKey(model, newAuthor + "/" + newWork + "/" + newPersona));
    }

    /**
     * Tries to add new nodes if the key references an attribute.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddNodesToAttribute()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_AUTHORS_TREE);
        EasyMock.expect(
                resolver.resolveKey(NodeStructureHelper.ROOT_AUTHORS_TREE, KEY,
                        model.getNodeHandler())).andReturn(
                Collections.singletonList(QueryResult.createAttributeResult(
                        nodeForKey(model, NodeStructureHelper.author(1)),
                        "test")));
        EasyMock.replay(resolver);

        final ImmutableNode newNode =
                new ImmutableNode.Builder().name("newNode").create();
        model.addNodes(KEY, Collections.singleton(newNode), resolver);
    }

    /**
     * Tries to add new nodes to an non-existing key pointing to an attribute.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddNodesToNewAttributeKey()
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_AUTHORS_TREE);
        EasyMock.expect(
                resolver.resolveKey(NodeStructureHelper.ROOT_AUTHORS_TREE, KEY,
                        model.getNodeHandler())).andReturn(
                Collections.<QueryResult<ImmutableNode>> emptyList());
        EasyMock.expect(
                resolver.resolveAddKey(NodeStructureHelper.ROOT_AUTHORS_TREE,
                        KEY, model.getNodeHandler())).andReturn(
                new NodeAddData<>(
                        NodeStructureHelper.ROOT_AUTHORS_TREE, "test", true,
                        null));
        EasyMock.replay(resolver);

        final ImmutableNode newNode =
                new ImmutableNode.Builder().name("newNode").create();
        model.addNodes(KEY, Collections.singleton(newNode), resolver);
    }

    /**
     * Helper method for testing the behavior of addNodes() if no nodes to be
     * added are provided.
     *
     * @param newNodes the collection with new nodes
     */
    private void checkAddNodesNoNodes(final Collection<ImmutableNode> newNodes)
    {
        final NodeKeyResolver<ImmutableNode> resolver = createResolver();
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_AUTHORS_TREE);
        EasyMock.replay(resolver);

        model.addNodes(KEY, newNodes, resolver);
        assertSame("Model was changed", NodeStructureHelper.ROOT_AUTHORS_TREE,
                model.getRootNode());
    }

    /**
     * Tests an add nodes operation if a null collection is passed in.
     */
    @Test
    public void testAddNodesNullCollection()
    {
        checkAddNodesNoNodes(null);
    }

    /**
     * Tests an add nodes operation if an empty collection is passed in.
     */
    @Test
    public void testAddNodesEmptyCollection()
    {
        checkAddNodesNoNodes(Collections.<ImmutableNode> emptySet());
    }

    /**
     * Tests whether a new root node can be set.
     */
    @Test
    public void testSetRoot()
    {
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        model.setRootNode(NodeStructureHelper.ROOT_AUTHORS_TREE);
        assertSame("Root node not changed",
                NodeStructureHelper.ROOT_AUTHORS_TREE, model.getRootNode());
        final ImmutableNode node = nodeForKey(model, "Homer/Ilias");
        assertEquals("Wrong parent mapping", nodeForKey(model, "Homer"),
                model.getNodeHandler().getParent(node));
    }

    /**
     * Tests whether the root node can be set to null.
     */
    @Test
    public void testSetRootNull()
    {
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        model.setRootNode(null);
        final ImmutableNode rootNode = model.getRootNode();
        assertTrue("Got children", rootNode.getChildren().isEmpty());
    }

    /**
     * Tests whether the model's data can be represented as immutable node
     * objects (which is trivial in this case).
     */
    @Test
    public void testGetInMemoryRepresentation()
    {
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_AUTHORS_TREE);
        assertSame("Wrong in-memory representation",
                NodeStructureHelper.ROOT_AUTHORS_TREE,
                model.getInMemoryRepresentation());
    }
}
