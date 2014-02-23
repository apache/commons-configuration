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

import static org.apache.commons.configuration.tree.NodeStructureHelper.ROOT_AUTHORS_TREE;
import static org.apache.commons.configuration.tree.NodeStructureHelper.ROOT_PERSONAE_TREE;
import static org.apache.commons.configuration.tree.NodeStructureHelper.nodeForKey;
import static org.apache.commons.configuration.tree.NodeStructureHelper.nodePathWithEndNode;
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
import java.util.Collections;
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
 * @version $Id$
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
        InMemoryNodeModel model = new InMemoryNodeModel();
        ImmutableNode root = model.getRootNode();
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
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        assertSame("Wrong root node", ROOT_AUTHORS_TREE, model.getRootNode());
    }

    /**
     * Tests whether the correct parent nodes are returned. All nodes in the
     * tree are checked.
     */
    @Test
    public void testGetParentNode()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        for (int authorIdx = 0; authorIdx < NodeStructureHelper.authorsLength(); authorIdx++)
        {
            ImmutableNode authorNode =
                    nodeForKey(model.getRootNode(),
                            NodeStructureHelper.author(authorIdx));
            assertSame(
                    "Wrong parent for " + NodeStructureHelper.author(authorIdx),
                    model.getRootNode(), model.getParent(authorNode));
            for (int workIdx = 0; workIdx < NodeStructureHelper
                    .worksLength(authorIdx); workIdx++)
            {
                String workKey =
                        NodeStructureHelper.appendPath(
                                NodeStructureHelper.author(authorIdx),
                                NodeStructureHelper.work(authorIdx, workIdx));
                ImmutableNode workNode =
                        nodeForKey(model.getRootNode(), workKey);
                assertSame("Wrong parent for " + workKey, authorNode,
                        model.getParent(workNode));
                for (int personaIdx = 0; personaIdx < NodeStructureHelper
                        .personaeLength(authorIdx, workIdx); personaIdx++)
                {
                    String personKey =
                            NodeStructureHelper.appendPath(workKey,
                                    NodeStructureHelper.persona(authorIdx,
                                            workIdx, personaIdx));
                    ImmutableNode personNode =
                            nodeForKey(model.getRootNode(), personKey);
                    assertSame("Wrong parent for " + personKey, workNode,
                            model.getParent(personNode));
                }
            }
        }
    }

    /**
     * Tests whether the correct parent for the root node is returned.
     */
    @Test
    public void testGetParentForRoot()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        assertNull("Got a parent", model.getParent(ROOT_AUTHORS_TREE));
    }

    /**
     * Tries to query the parent node for a node which does not belong to the
     * managed tree.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetParentInvalidNode()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        model.getParent(new ImmutableNode.Builder().name("unknown").create());
    }

    /**
     * Tests whether the name of a node can be queried.
     */
    @Test
    public void testNodeHandlerName()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        ImmutableNode author = nodeForKey(model, NodeStructureHelper.author(0));
        assertEquals("Wrong node name", NodeStructureHelper.author(0), model.nodeName(author));
    }

    /**
     * Tests whether the value of a node can be queried.
     */
    @Test
    public void testNodeHandlerValue()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        ImmutableNode work = nodeForKey(model, "Shakespeare/The Tempest");
        int year = 1611;
        work = work.setValue(year);
        assertEquals("Wrong value", year, model.getValue(work));
    }

    /**
     * Tests whether the children of a node can be queried.
     */
    @Test
    public void testNodeHandlerGetChildren()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        ImmutableNode node = nodeForKey(model, NodeStructureHelper.author(0));
        assertSame("Wrong children", node.getChildren(),
                model.getChildren(node));
    }

    /**
     * Tests whether all children of a specific name can be queried.
     */
    @Test
    public void testNodeHandlerGetChildrenByName()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        String name = "Achilles";
        Set<ImmutableNode> children =
                new HashSet<ImmutableNode>(model.getChildren(ROOT_PERSONAE_TREE,
                        name));
        assertEquals("Wrong number of children", 3, children.size());
        for (ImmutableNode c : children)
        {
            assertEquals("Wrong node name", name, c.getNodeName());
        }
    }

    /**
     * Tests whether the collection of children cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testNodeHandlerGetChildrenByNameImmutable()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        List<ImmutableNode> children =
                model.getChildren(ROOT_PERSONAE_TREE, "Ajax");
        children.add(null);
    }

    /**
     * Tests whether a child at a given index can be accessed.
     */
    @Test
    public void testNodeHandlerGetChildAtIndex()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        ImmutableNode node = nodeForKey(model, NodeStructureHelper.author(0));
        assertSame("Wrong child", node.getChildren().get(1),
                model.getChild(node, 1));
    }

    /**
     * Tests whether the index of a given child can be queried.
     */
    @Test
    public void testNodeHandlerIndexOfChild()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        String key = "Simmons/Hyperion";
        ImmutableNode parent = nodeForKey(model, key);
        ImmutableNode child = nodeForKey(model, key + "/Weintraub");
        assertEquals("Wrong child index", 3, model.indexOfChild(parent, child));
    }

    /**
     * Tests the indexOfChild() method for an unknown child node.
     */
    @Test
    public void testNodeHandlerIndexOfUnknownChild()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        ImmutableNode parent = nodeForKey(model, "Homer/Ilias");
        ImmutableNode child =
                nodeForKey(model, "Shakespeare/Troilus and Cressida/Achilles");
        assertEquals("Wrong child index", -1, model.indexOfChild(parent, child));
    }

    /**
     * Tests whether the number of all children can be queried.
     */
    @Test
    public void testNodeHandlerGetChildrenCountAll()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        ImmutableNode node = nodeForKey(model, NodeStructureHelper.author(0));
        assertEquals("Wrong number of children", NodeStructureHelper.worksLength(0),
                model.getChildrenCount(node, null));
    }

    /**
     * Tests whether the number of all children with a given name can be
     * queried.
     */
    @Test
    public void testNodeHandlerGetChildrenCountSpecific()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        assertEquals("Wrong number of children", 3,
                model.getChildrenCount(ROOT_PERSONAE_TREE, "Achilles"));
    }

    /**
     * Tests whether a node's attributes can be queried.
     */
    @Test
    public void testNodeHandlerGetAttributes()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        ImmutableNode node = nodeForKey(model, "Puck");
        assertEquals("Wrong attributes", node.getAttributes().keySet(),
                model.getAttributes(node));
    }

    /**
     * Tests that the keys of attributes cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testNodeHandlerGetAttributesImmutable()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        ImmutableNode node = nodeForKey(model, "Puck");
        model.getAttributes(node).add("test");
    }

    /**
     * Tests a positive check whether a node has attributes.
     */
    @Test
    public void testNodeHandlerHasAttributesTrue()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        ImmutableNode node = nodeForKey(model, "Puck");
        assertTrue("No attributes", model.hasAttributes(node));
    }

    /**
     * Tests a negative check whether a node has attributes.
     */
    @Test
    public void testNodeHandlerHasAttributesFalse()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        assertFalse("Got attributes", model.hasAttributes(ROOT_PERSONAE_TREE));
    }

    /**
     * Tests whether the value of an attribute can be queried.
     */
    @Test
    public void testNodeHandlerGetAttributeValue()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        ImmutableNode node = nodeForKey(model, "Prospero");
        assertEquals("Wrong value", "Shakespeare",
                model.getAttributeValue(node, NodeStructureHelper.ATTR_AUTHOR));
    }

    /**
     * Tests whether a node with children is defined.
     */
    @Test
    public void testNodeHandlerIsDefinedChildren()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        ImmutableNode node = nodeForKey(model, NodeStructureHelper.author(2));
        assertTrue("Not defined", model.isDefined(node));
    }

    /**
     * Tests whether a node with attributes is defined.
     */
    @Test
    public void testNodeHandlerIsDefinedAttributes()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        ImmutableNode node =
                new ImmutableNode.Builder().addAttribute(
                        NodeStructureHelper.ATTR_AUTHOR,
                        NodeStructureHelper.author(0)).create();
        assertTrue("Not defined", model.isDefined(node));
    }

    /**
     * Tests whether a node with a value is defined.
     */
    @Test
    public void testNodeHandlerIsDefinedValue()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        ImmutableNode node = new ImmutableNode.Builder().value(42).create();
        assertTrue("Not defined", model.isDefined(node));
    }

    /**
     * Tests whether an undefined node is correctly detected.
     */
    @Test
    public void testNodeHandlerIsDefinedFalse()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        ImmutableNode node =
                new ImmutableNode.Builder().name(NodeStructureHelper.author(1))
                        .create();
        assertFalse("Defined", model.isDefined(node));
    }

    /**
     * Tests whether the correct node handler is returned.
     */
    @Test
    public void testGetNodeHandler()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        assertSame("Wrong node handler", model, model.getNodeHandler());
    }

    /**
     * Tests whether a property can be added to the node model if there are some
     * additional path nodes to be created.
     */
    @Test
    public void testAddPropertyWithPathNodes()
    {
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        NodeAddData<ImmutableNode> addData =
                new NodeAddData<ImmutableNode>(nodeForKey(ROOT_AUTHORS_TREE,
                        "Homer/Ilias"), "location", false,
                        Collections.singleton("locations"));
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY, model))
                .andReturn(addData);
        EasyMock.replay(resolver);
        String[] locations = {
                "Troja", "Beach", "Olympos"
        };

        model.addProperty(KEY, Arrays.asList(locations), resolver);
        ImmutableNode nodeLocs = nodeForKey(model, "Homer/Ilias/locations");
        assertEquals("Wrong number of children", locations.length, nodeLocs
                .getChildren().size());
        int idx = 0;
        for (ImmutableNode c : nodeLocs.getChildren())
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
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        NodeAddData<ImmutableNode> addData =
                new NodeAddData<ImmutableNode>(nodeForKey(ROOT_AUTHORS_TREE,
                        "Homer"), "work", false, null);
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY, model))
                .andReturn(addData);
        EasyMock.replay(resolver);

        model.addProperty(KEY, Collections.singleton("Odyssee"), resolver);
        ImmutableNode node = nodeForKey(model, "Homer/work");
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
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        NodeAddData<ImmutableNode> addData =
                new NodeAddData<ImmutableNode>(nodeForKey(ROOT_AUTHORS_TREE,
                        "Homer/Ilias"), "location", false,
                        Collections.singleton("locations"));
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY, model))
                .andReturn(addData);
        EasyMock.replay(resolver);
        String[] locations = {
                "Troja", "Beach", "Olympos"
        };

        model.addProperty(KEY, Arrays.asList(locations), resolver);
        String[] path = {
                "Homer", "Ilias", "locations"
        };
        ImmutableNode node =
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
    private static void checkPathToRoot(InMemoryNodeModel model,
            ImmutableNode node, String... path)
    {
        for (int i = path.length - 1; i >= 0; i--)
        {
            node = model.getParent(node);
            assertEquals("Wrong node name", path[i], node.getNodeName());
        }
        assertSame("Wrong root node", model.getRootNode(),
                model.getParent(node));
    }

    /**
     * Tests whether an attribute can be added if there are some path nodes.
     */
    @Test
    public void testAddPropertyAttributeWithPathNodes()
    {
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        NodeAddData<ImmutableNode> addData =
                new NodeAddData<ImmutableNode>(nodeForKey(ROOT_AUTHORS_TREE,
                        "Homer/Ilias"), "number", true, Arrays.asList("scenes",
                        "scene"));
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY, model))
                .andReturn(addData);
        EasyMock.replay(resolver);

        model.addProperty(KEY, Collections.singleton(1), resolver);
        ImmutableNode node = nodeForKey(model, "Homer/Ilias/scenes/scene");
        assertEquals("Attribute not set", 1, node.getAttributes().get("number"));
    }

    /**
     * Tests the special case that an attribute is added with a single path
     * node.
     */
    @Test
    public void testAddPropertyAttributeWithSinglePathNode()
    {
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        NodeAddData<ImmutableNode> addData =
                new NodeAddData<ImmutableNode>(nodeForKey(ROOT_AUTHORS_TREE,
                        NodeStructureHelper.author(0)), "year", true,
                        Arrays.asList("dateOfBirth"));
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY, model))
                .andReturn(addData);
        EasyMock.replay(resolver);

        final Integer year = 1564;
        model.addProperty(KEY, Collections.singleton(year), resolver);
        ImmutableNode node = nodeForKey(model, "Shakespeare/dateOfBirth");
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
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        NodeAddData<ImmutableNode> addData =
                new NodeAddData<ImmutableNode>(nodeForKey(ROOT_AUTHORS_TREE,
                        "Shakespeare/The Tempest"), "year", true, null);
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        EasyMock.expect(resolver.resolveAddKey(ROOT_AUTHORS_TREE, KEY, model))
                .andReturn(addData);
        EasyMock.replay(resolver);

        model.addProperty(KEY, Collections.singleton(1611), resolver);
        ImmutableNode node = nodeForKey(model, "Shakespeare/The Tempest");
        assertEquals("Attribute not set", 1611, node.getAttributes()
                .get("year"));
    }

    /**
     * Tests an addProperty() operation if no values are provided.
     */
    @Test
    public void testAddPropertyNoValues()
    {
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        EasyMock.replay(resolver);
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);

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
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        QueryResult<ImmutableNode> result =
                QueryResult.createNodeResult(nodeForKey(model,
                        "Homer/Ilias/Achilles"));
        EasyMock.expect(resolver.resolveKey(ROOT_AUTHORS_TREE, KEY, model))
                .andReturn(Collections.singletonList(result));
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        ImmutableNode node = nodeForKey(model, "Homer/Ilias");
        assertEquals("Wrong number of children", 2, node.getChildren().size());
        for (ImmutableNode c : node.getChildren())
        {
            assertNotEquals("Node still found", result.getNode().getNodeName(),
                    c.getNodeName());
        }
    }

    /**
     * Helper method for testing whether nodes removed from the model can no
     * longer be looked up in the parent mapping.
     *
     * @param pathToRemove the path to the node to be removed
     * @param nodeToCheck the node to check in the parent mapping
     */
    private void checkClearTreeUpdatedParentMapping(String pathToRemove,
            ImmutableNode nodeToCheck)
    {
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        QueryResult<ImmutableNode> result =
                QueryResult.createNodeResult(nodeForKey(model, pathToRemove));
        EasyMock.expect(resolver.resolveKey(ROOT_AUTHORS_TREE, KEY, model))
                .andReturn(Collections.singletonList(result));
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        try
        {
            model.getParent(nodeToCheck);
            fail("Removed node still in parent mapping!");
        }
        catch (IllegalArgumentException iaex)
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
        String path = "Homer/Ilias/Achilles";
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
        String path = "Homer/Ilias";
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
        String[] path = {
                "Homer", "Ilias"
        };
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        QueryResult<ImmutableNode> result =
                QueryResult.createNodeResult(nodeForKey(model,
                        nodePathWithEndNode("Achilles", path)));
        EasyMock.expect(resolver.resolveKey(ROOT_AUTHORS_TREE, KEY, model))
                .andReturn(Collections.singletonList(result));
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
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        ImmutableNode node = nodeForKey(model, "Homer/Ilias");
        List<QueryResult<ImmutableNode>> results =
                new ArrayList<QueryResult<ImmutableNode>>(node.getChildren()
                        .size());
        for (ImmutableNode child : node.getChildren())
        {
            results.add(QueryResult.createNodeResult(child));
        }
        EasyMock.expect(resolver.resolveKey(ROOT_AUTHORS_TREE, KEY, model))
                .andReturn(results);
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        assertEquals("Child of root not removed",
                NodeStructureHelper.authorsLength() - 1, model.getRootNode()
                        .getChildren().size());
        for (ImmutableNode child : model.getRootNode().getChildren())
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
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        ImmutableNode child =
                new ImmutableNode.Builder().name("child").value("test")
                        .create();
        ImmutableNode root =
                new ImmutableNode.Builder(1).addChild(child).create();
        InMemoryNodeModel model = new InMemoryNodeModel(root);
        EasyMock.expect(resolver.resolveKey(root, KEY, model)).andReturn(
                Collections.singletonList(QueryResult.createNodeResult(child)));
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        assertFalse("Root node still defined",
                model.isDefined(model.getRootNode()));
    }

    /**
     * Tests whether attributes can be cleared with clearTree().
     */
    @Test
    public void testClearTreeAttribute()
    {
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        final String nodeName = "Puck";
        EasyMock.expect(resolver.resolveKey(ROOT_PERSONAE_TREE, KEY, model))
                .andReturn(
                        Collections.singletonList(QueryResult
                                .createAttributeResult(
                                        nodeForKey(model, nodeName),
                                        NodeStructureHelper.ATTR_AUTHOR)));
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        ImmutableNode node = nodeForKey(model, nodeName);
        assertTrue("Got still attributes", node.getAttributes().isEmpty());
    }

    /**
     * Tests whether both nodes and attributes can be removed by a clearTree()
     * operation. We remove all attributes and children from a node. The node
     * becomes undefined and should be removed.
     */
    @Test
    public void testClearTreeNodesAndAttributes()
    {
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_PERSONAE_TREE);
        final String nodeName = "Puck";
        ImmutableNode orgNode = nodeForKey(model, nodeName);
        List<QueryResult<ImmutableNode>> results =
                new ArrayList<QueryResult<ImmutableNode>>(2);
        results.add(QueryResult.createAttributeResult(orgNode,
                NodeStructureHelper.ATTR_AUTHOR));
        results.add(QueryResult.createNodeResult(orgNode.getChildren().get(0)));
        EasyMock.expect(resolver.resolveKey(ROOT_PERSONAE_TREE, KEY, model))
                .andReturn(results);
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        try
        {
            nodeForKey(model, nodeName);
            fail("Node still present!");
        }
        catch (NoSuchElementException nex)
        {
            // expected
        }
    }

    /**
     * Tests whether the whole node structure can be cleared.
     */
    @Test
    public void testClear()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        model.clear();
        assertFalse("Got still data", model.isDefined(model.getRootNode()));
        assertEquals("Root name was changed", ROOT_AUTHORS_TREE.getNodeName(),
                model.getRootNode().getNodeName());
    }

    /**
     * Tests whether clearTree() handles the root node in a special way.
     */
    @Test
    public void testClearTreeRootNode()
    {
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        List<QueryResult<ImmutableNode>> results =
                new ArrayList<QueryResult<ImmutableNode>>(2);
        results.add(QueryResult.createNodeResult(nodeForKey(model,
                NodeStructureHelper.author(0))));
        results.add(QueryResult.createNodeResult(ROOT_AUTHORS_TREE));
        EasyMock.expect(resolver.resolveKey(ROOT_AUTHORS_TREE, KEY, model))
                .andReturn(results);
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        assertFalse("Got still data", model.isDefined(model.getRootNode()));
    }

    /**
     * Tests whether the replacement mapping is automatically compacted if it
     * gets too large.
     */
    @Test
    public void testCompactReplacementMapping()
    {
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        final InMemoryNodeModel model = new InMemoryNodeModel(ROOT_AUTHORS_TREE);
        final int numberOfOperations = 200;
        final String key = "Homer/Ilias";
        for (int i = 0; i < numberOfOperations; i++)
        {
            final int index = i;
            EasyMock.expect(
                    resolver.resolveAddKey(
                            EasyMock.anyObject(ImmutableNode.class),
                            EasyMock.eq(KEY), EasyMock.eq(model))).andAnswer(
                    new IAnswer<NodeAddData<ImmutableNode>>()
                    {
                        public NodeAddData<ImmutableNode> answer()
                                throws Throwable
                        {
                            assertSame("Wrong root node", model.getRootNode(),
                                    EasyMock.getCurrentArguments()[0]);
                            ImmutableNode addParent = nodeForKey(model, key);
                            return new NodeAddData<ImmutableNode>(addParent,
                                    "Warrior" + index, false, null);
                        }
                    });
        }
        EasyMock.replay(resolver);

        for (int i = 0; i < numberOfOperations; i++)
        {
            model.addProperty(KEY, Collections.singleton(i), resolver);
        }
        ImmutableNode orgNode = nodeForKey(ROOT_AUTHORS_TREE, key);
        ImmutableNode changedNode = nodeForKey(model, key);
        assertEquals("Wrong number of children", orgNode.getChildren().size()
                + numberOfOperations, changedNode.getChildren().size());
        Map<ImmutableNode, ImmutableNode> replacementMapping =
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
        final NodeKeyResolver resolver =
                EasyMock.createMock(NodeKeyResolver.class);
        final InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_AUTHORS_TREE);
        EasyMock.expect(
                resolver.resolveAddKey(EasyMock.anyObject(ImmutableNode.class),
                        EasyMock.eq(KEY), EasyMock.eq(model)))
                .andAnswer(new IAnswer<NodeAddData<ImmutableNode>>()
                {
                    public NodeAddData<ImmutableNode> answer() throws Throwable
                    {
                        ImmutableNode addParent =
                                (ImmutableNode) EasyMock.getCurrentArguments()[0];
                        return new NodeAddData<ImmutableNode>(addParent,
                                "name", false, Collections.singleton("author"));
                    }
                }).anyTimes();
        EasyMock.replay(resolver);

        final CountDownLatch latch = new CountDownLatch(1);
        final String authorPrefix = "newAuthor";
        final int threadCount = 32;
        Thread[] threads = new Thread[threadCount];
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
                    catch (InterruptedException iex)
                    {
                        // ignore
                    }
                }
            };
            threads[i].start();
        }
        latch.countDown();
        for (Thread t : threads)
        {
            t.join();
        }

        Pattern patternAuthorName =
                Pattern.compile(Pattern.quote(authorPrefix) + "(\\d+)");
        Set<Integer> indices = new HashSet<Integer>();
        for (int i = 0; i < threadCount; i++)
        {
            ImmutableNode node = nodeForKey(model, "author(" + i + ")/name");
            Matcher m =
                    patternAuthorName.matcher(String.valueOf(node.getValue()));
            assertTrue("Wrong value: " + node.getValue(), m.matches());
            int idx = Integer.parseInt(m.group(1));
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
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        final String nodeKey =
                "Ariel/The Tempest/" + NodeStructureHelper.ELEM_ORG_VALUE;
        EasyMock.expect(resolver.resolveKey(model.getRootNode(), KEY, model))
                .andReturn(
                        Collections.singletonList(QueryResult
                                .createNodeResult(nodeForKey(model, nodeKey))));
        EasyMock.replay(resolver);

        model.clearProperty(KEY, resolver);
        ImmutableNode node = nodeForKey(model, nodeKey);
        assertNull("Value not cleared", node.getValue());
    }

    /**
     * Tests whether a property value stored as an attribute can be cleared.
     */
    @Test
    public void testClearPropertyAttribute()
    {
        NodeKeyResolver resolver = EasyMock.createMock(NodeKeyResolver.class);
        InMemoryNodeModel model =
                new InMemoryNodeModel(NodeStructureHelper.ROOT_PERSONAE_TREE);
        final String nodeKey =
                "Prospero/The Tempest/" + NodeStructureHelper.ELEM_ORG_VALUE;
        EasyMock.expect(resolver.resolveKey(model.getRootNode(), KEY, model))
                .andReturn(
                        Collections.singletonList(QueryResult
                                .createAttributeResult(
                                        nodeForKey(model, nodeKey),
                                        NodeStructureHelper.ATTR_TESTED)));
        EasyMock.replay(resolver);

        model.clearProperty(KEY, resolver);
        ImmutableNode node = nodeForKey(model, nodeKey);
        assertTrue("Attribute not removed", node.getAttributes().isEmpty());
    }
}
