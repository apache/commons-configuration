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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@code InMemoryNodeModel}.
 *
 * @version $Id$
 */
public class TestInMemoryNodeModel
{
    /** A pattern for parsing node keys with optional indices. */
    private static final Pattern PAT_KEY_WITH_INDEX = Pattern
            .compile("(\\w+)\\((\\d+)\\)");

    /** The character for splitting node path elements. */
    private static final String PATH_SEPARATOR = "/";

    /** An array with authors. */
    private static final String[] AUTHORS = {
            "Shakespeare", "Homer", "Simmons"
    };

    /** An array with the works of the test authors. */
    private static final String[][] WORKS = {
            {
                    "Troilus and Cressida", "The Tempest",
                    "A Midsummer Night?s Dream"
            }, {
                "Ilias"
            }, {
                    "Ilium", "Hyperion"
            }
    };

    /** An array with the personae in the works. */
    private static final String[][][] PERSONAE = {
            {
                    // Works of Shakespeare
                    {
                            "Troilus", "Cressidia", "Ajax", "Achilles"
                    }, {
                            "Prospero", "Ariel"
                    }, {
                            "Oberon", "Titania", "Puck"
                    }
            }, {
                // Works of Homer
                {
                        "Achilles", "Agamemnon", "Hektor"
                }
            }, {
                    // Works of Dan Simmons
                    {
                            "Hockenberry", "Achilles"
                    }, {
                            "Shrike", "Moneta", "Consul", "Weintraub"
                    }
            }
    };

    /** Constant for the author attribute. */
    private static final String ATTR_AUTHOR = "author";

    /** Constant for a test key. */
    private static final String KEY = "aTestKey";

    /** The root node of the authors tree. */
    private static ImmutableNode rootAuthorsTree;

    /** The root node of the personae tree. */
    private static ImmutableNode rootPersonaeTree;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        rootAuthorsTree = createAuthorsTree();
        rootPersonaeTree = createPersonaeTree();
    }

    /**
     * Creates a tree with a root node whose children are the test authors. Each
     * other has his works as child nodes. Each work has its personae as
     * children.
     *
     * @return the root node of the authors tree
     */
    private static ImmutableNode createAuthorsTree()
    {
        ImmutableNode.Builder rootBuilder =
                new ImmutableNode.Builder(AUTHORS.length);
        for (int author = 0; author < AUTHORS.length; author++)
        {
            ImmutableNode.Builder authorBuilder = new ImmutableNode.Builder();
            authorBuilder.name(AUTHORS[author]);
            for (int work = 0; work < WORKS[author].length; work++)
            {
                ImmutableNode.Builder workBuilder = new ImmutableNode.Builder();
                workBuilder.name(WORKS[author][work]);
                for (String person : PERSONAE[author][work])
                {
                    workBuilder.addChild(new ImmutableNode.Builder().name(
                            person).create());
                }
                authorBuilder.addChild(workBuilder.create());
            }
            rootBuilder.addChild(authorBuilder.create());
        }
        return rootBuilder.create();
    }

    /**
     * Creates a tree with a root node whose children are the test personae.
     * Each node represents a person and has an attribute pointing to the author
     * who invented this person. There is a single child node for the associated
     * work.
     *
     * @return the root node of the personae tree
     */
    private static ImmutableNode createPersonaeTree()
    {
        ImmutableNode.Builder rootBuilder = new ImmutableNode.Builder();
        for (int author = 0; author < AUTHORS.length; author++)
        {
            for (int work = 0; work < WORKS[author].length; work++)
            {
                for (String person : PERSONAE[author][work])
                {
                    ImmutableNode workNode =
                            new ImmutableNode.Builder().name(
                                    WORKS[author][work]).create();
                    ImmutableNode personNode =
                            new ImmutableNode.Builder(1).name(person)
                                    .addAttribute(ATTR_AUTHOR, AUTHORS[author])
                                    .addChild(workNode).create();
                    rootBuilder.addChild(personNode);
                }
            }
        }
        return rootBuilder.create();
    }

    /**
     * Evaluates the given key and finds the corresponding child node of the
     * specified root. Keys have the form {@code path/to/node}. If there are
     * multiple sibling nodes with the same name, a numerical index can be
     * specified in parenthesis.
     *
     * @param root the root node
     * @param key the key to the desired node
     * @return the node with this key
     * @throws NoSuchElementException if the key cannot be resolved
     */
    private static ImmutableNode nodeForKey(ImmutableNode root, String key)
    {
        String[] components = key.split(PATH_SEPARATOR);
        return findNode(root, components, 0);
    }

    /**
     * Helper method for evaluating a single component of a node key.
     *
     * @param parent the current parent node
     * @param components the array with the components of the node key
     * @param currentIdx the index of the current path component
     * @return the found target node
     * @throws NoSuchElementException if the desired node cannot be found
     */
    private static ImmutableNode findNode(ImmutableNode parent,
            String[] components, int currentIdx)
    {
        if (currentIdx >= components.length)
        {
            return parent;
        }

        Matcher m = PAT_KEY_WITH_INDEX.matcher(components[currentIdx]);
        String childName;
        int childIndex;
        if (m.matches())
        {
            childName = m.group(1);
            childIndex = Integer.parseInt(m.group(2));
        }
        else
        {
            childName = components[currentIdx];
            childIndex = 0;
        }

        int foundIdx = 0;
        for (ImmutableNode node : parent.getChildren())
        {
            if (childName.equals(node.getNodeName()))
            {
                if (foundIdx++ == childIndex)
                {
                    return findNode(node, components, currentIdx + 1);
                }
            }
        }
        throw new NoSuchElementException("Cannot resolve child "
                + components[currentIdx]);
    }

    /**
     * Evaluates the given key and finds the corresponding child node of the
     * root node of the specified model. This is a convenience method that works
     * like the method with the same name, but obtains the root node from the
     * given model.
     *
     * @param model the node model
     * @param key the key to the desired node
     * @return the found target node
     * @throws NoSuchElementException if the desired node cannot be found
     */
    private static ImmutableNode nodeForKey(InMemoryNodeModel model, String key)
    {
        return nodeForKey(model.getRootNode(), key);
    }

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
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        assertSame("Wrong root node", rootAuthorsTree, model.getRootNode());
    }

    /**
     * Tests whether the correct parent nodes are returned. All nodes in the
     * tree are checked.
     */
    @Test
    public void testGetParentNode()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        for (int authorIdx = 0; authorIdx < AUTHORS.length; authorIdx++)
        {
            ImmutableNode authorNode =
                    nodeForKey(model.getRootNode(), AUTHORS[authorIdx]);
            assertSame("Wrong parent for " + AUTHORS[authorIdx],
                    model.getRootNode(), model.getParent(authorNode));
            for (int workIdx = 0; workIdx < WORKS[authorIdx].length; workIdx++)
            {
                String workKey =
                        AUTHORS[authorIdx] + PATH_SEPARATOR
                                + WORKS[authorIdx][workIdx];
                ImmutableNode workNode =
                        nodeForKey(model.getRootNode(), workKey);
                assertSame("Wrong parent for " + workKey, authorNode,
                        model.getParent(workNode));
                for (String person : PERSONAE[authorIdx][workIdx])
                {
                    String personKey = workKey + PATH_SEPARATOR + person;
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
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        assertNull("Got a parent", model.getParent(rootAuthorsTree));
    }

    /**
     * Tries to query the parent node for a node which does not belong to the
     * managed tree.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetParentInvalidNode()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        model.getParent(new ImmutableNode.Builder().name("unknown").create());
    }

    /**
     * Tests whether the name of a node can be queried.
     */
    @Test
    public void testNodeHandlerName()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        ImmutableNode author = nodeForKey(model, AUTHORS[0]);
        assertEquals("Wrong node name", AUTHORS[0], model.nodeName(author));
    }

    /**
     * Tests whether the value of a node can be queried.
     */
    @Test
    public void testNodeHandlerValue()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
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
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        ImmutableNode node = nodeForKey(model, AUTHORS[0]);
        assertSame("Wrong children", node.getChildren(),
                model.getChildren(node));
    }

    /**
     * Tests whether all children of a specific name can be queried.
     */
    @Test
    public void testNodeHandlerGetChildrenByName()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootPersonaeTree);
        String name = "Achilles";
        Set<ImmutableNode> children =
                new HashSet<ImmutableNode>(model.getChildren(rootPersonaeTree,
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
        InMemoryNodeModel model = new InMemoryNodeModel(rootPersonaeTree);
        List<ImmutableNode> children =
                model.getChildren(rootPersonaeTree, "Ajax");
        children.add(null);
    }

    /**
     * Tests whether a child at a given index can be accessed.
     */
    @Test
    public void testNodeHandlerGetChildAtIndex()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        ImmutableNode node = nodeForKey(model, AUTHORS[0]);
        assertSame("Wrong child", node.getChildren().get(1),
                model.getChild(node, 1));
    }

    /**
     * Tests whether the index of a given child can be queried.
     */
    @Test
    public void testNodeHandlerIndexOfChild()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
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
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
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
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        ImmutableNode node = nodeForKey(model, AUTHORS[0]);
        assertEquals("Wrong number of children", WORKS[0].length,
                model.getChildrenCount(node, null));
    }

    /**
     * Tests whether the number of all children with a given name can be
     * queried.
     */
    @Test
    public void testNodeHandlerGetChildrenCountSpecific()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootPersonaeTree);
        assertEquals("Wrong number of children", 3,
                model.getChildrenCount(rootPersonaeTree, "Achilles"));
    }

    /**
     * Tests whether a node's attributes can be queried.
     */
    @Test
    public void testNodeHandlerGetAttributes()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootPersonaeTree);
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
        InMemoryNodeModel model = new InMemoryNodeModel(rootPersonaeTree);
        ImmutableNode node = nodeForKey(model, "Puck");
        model.getAttributes(node).add("test");
    }

    /**
     * Tests a positive check whether a node has attributes.
     */
    @Test
    public void testNodeHandlerHasAttributesTrue()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootPersonaeTree);
        ImmutableNode node = nodeForKey(model, "Puck");
        assertTrue("No attributes", model.hasAttributes(node));
    }

    /**
     * Tests a negative check whether a node has attributes.
     */
    @Test
    public void testNodeHandlerHasAttributesFalse()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootPersonaeTree);
        assertFalse("Got attributes", model.hasAttributes(rootPersonaeTree));
    }

    /**
     * Tests whether the value of an attribute can be queried.
     */
    @Test
    public void testNodeHandlerGetAttributeValue()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootPersonaeTree);
        ImmutableNode node = nodeForKey(model, "Prospero");
        assertEquals("Wrong value", "Shakespeare",
                model.getAttributeValue(node, ATTR_AUTHOR));
    }

    /**
     * Tests whether a node with children is defined.
     */
    @Test
    public void testNodeHandlerIsDefinedChildren()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        ImmutableNode node = nodeForKey(model, AUTHORS[2]);
        assertTrue("Not defined", model.isDefined(node));
    }

    /**
     * Tests whether a node with attributes is defined.
     */
    @Test
    public void testNodeHandlerIsDefinedAttributes()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootPersonaeTree);
        ImmutableNode node =
                new ImmutableNode.Builder().addAttribute(ATTR_AUTHOR,
                        AUTHORS[0]).create();
        assertTrue("Not defined", model.isDefined(node));
    }

    /**
     * Tests whether a node with a value is defined.
     */
    @Test
    public void testNodeHandlerIsDefinedValue()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootPersonaeTree);
        ImmutableNode node = new ImmutableNode.Builder().value(42).create();
        assertTrue("Not defined", model.isDefined(node));
    }

    /**
     * Tests whether an undefined node is correctly detected.
     */
    @Test
    public void testNodeHandlerIsDefinedFalse()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootPersonaeTree);
        ImmutableNode node =
                new ImmutableNode.Builder().name(AUTHORS[1]).create();
        assertFalse("Defined", model.isDefined(node));
    }

    /**
     * Tests whether the correct node handler is returned.
     */
    @Test
    public void testGetNodeHandler()
    {
        InMemoryNodeModel model = new InMemoryNodeModel(rootPersonaeTree);
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
                new NodeAddData<ImmutableNode>(nodeForKey(rootAuthorsTree,
                        "Homer/Ilias"), "location", false,
                        Collections.singleton("locations"));
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        EasyMock.expect(resolver.resolveAddKey(rootAuthorsTree, KEY, model))
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
                new NodeAddData<ImmutableNode>(nodeForKey(rootAuthorsTree,
                        "Homer"), "work", false, null);
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        EasyMock.expect(resolver.resolveAddKey(rootAuthorsTree, KEY, model))
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
                new NodeAddData<ImmutableNode>(nodeForKey(rootAuthorsTree,
                        "Homer/Ilias"), "location", false,
                        Collections.singleton("locations"));
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        EasyMock.expect(resolver.resolveAddKey(rootAuthorsTree, KEY, model))
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
     * Convenience method for creating a path for accessing a node based on the
     * node names.
     *
     * @param path an array with the expected node names on the path
     * @return the resulting path as string
     */
    private static String nodePath(String... path)
    {
        return StringUtils.join(path, PATH_SEPARATOR);
    }

    /**
     * Convenience method for creating a node path with a special end node.
     *
     * @param endNode the name of the last path component
     * @param path an array with the expected node names on the path
     * @return the resulting path as string
     */
    private static String nodePathWithEndNode(String endNode, String... path)
    {
        return nodePath(path) + PATH_SEPARATOR + endNode;
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
                new NodeAddData<ImmutableNode>(nodeForKey(rootAuthorsTree,
                        "Homer/Ilias"), "number", true, Arrays.asList("scenes",
                        "scene"));
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        EasyMock.expect(resolver.resolveAddKey(rootAuthorsTree, KEY, model))
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
                new NodeAddData<ImmutableNode>(nodeForKey(rootAuthorsTree,
                        AUTHORS[0]), "year", true, Arrays.asList("dateOfBirth"));
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        EasyMock.expect(resolver.resolveAddKey(rootAuthorsTree, KEY, model))
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
                new NodeAddData<ImmutableNode>(nodeForKey(rootAuthorsTree,
                        "Shakespeare/The Tempest"), "year", true, null);
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        EasyMock.expect(resolver.resolveAddKey(rootAuthorsTree, KEY, model))
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
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);

        model.addProperty(KEY, Collections.emptySet(), resolver);
        assertSame("Root node was changed", rootAuthorsTree,
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
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        QueryResult<ImmutableNode> result =
                QueryResult.createNodeResult(nodeForKey(model,
                        "Homer/Ilias/Achilles"));
        EasyMock.expect(resolver.resolveKey(rootAuthorsTree, KEY, model))
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
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        QueryResult<ImmutableNode> result =
                QueryResult.createNodeResult(nodeForKey(model, pathToRemove));
        EasyMock.expect(resolver.resolveKey(rootAuthorsTree, KEY, model))
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
                nodeForKey(rootAuthorsTree, path));
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
                nodeForKey(rootAuthorsTree, path + "/Achilles"));
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
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        QueryResult<ImmutableNode> result =
                QueryResult.createNodeResult(nodeForKey(model,
                        nodePathWithEndNode("Achilles", path)));
        EasyMock.expect(resolver.resolveKey(rootAuthorsTree, KEY, model))
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
        InMemoryNodeModel model = new InMemoryNodeModel(rootAuthorsTree);
        ImmutableNode node = nodeForKey(model, "Homer/Ilias");
        List<QueryResult<ImmutableNode>> results =
                new ArrayList<QueryResult<ImmutableNode>>(node.getChildren()
                        .size());
        for (ImmutableNode child : node.getChildren())
        {
            results.add(QueryResult.createNodeResult(child));
        }
        EasyMock.expect(resolver.resolveKey(rootAuthorsTree, KEY, model))
                .andReturn(results);
        EasyMock.replay(resolver);

        model.clearTree(KEY, resolver);
        assertEquals("Child of root not removed", AUTHORS.length - 1, model
                .getRootNode().getChildren().size());
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
        InMemoryNodeModel model = new InMemoryNodeModel(rootPersonaeTree);
        final String nodeName = "Puck";
        EasyMock.expect(resolver.resolveKey(rootPersonaeTree, KEY, model))
                .andReturn(
                        Collections.singletonList(QueryResult
                                .createAttributeResult(
                                        nodeForKey(model, nodeName),
                                        ATTR_AUTHOR)));
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
        InMemoryNodeModel model = new InMemoryNodeModel(rootPersonaeTree);
        final String nodeName = "Puck";
        ImmutableNode orgNode = nodeForKey(model, nodeName);
        List<QueryResult<ImmutableNode>> results =
                new ArrayList<QueryResult<ImmutableNode>>(2);
        results.add(QueryResult.createAttributeResult(orgNode, ATTR_AUTHOR));
        results.add(QueryResult.createNodeResult(orgNode.getChildren().get(0)));
        EasyMock.expect(resolver.resolveKey(rootPersonaeTree, KEY, model))
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
}
