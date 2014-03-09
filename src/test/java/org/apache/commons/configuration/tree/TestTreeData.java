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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * Test class for {@code TreeData}
 *
 * @version $Id$
 */
public class TestTreeData
{
    /**
     * Creates a TreeData object initialized with the given root node.
     *
     * @param root the root node
     * @return the TreeData instance
     */
    private static TreeData createTreeData(ImmutableNode root)
    {
        InMemoryNodeModel model = new InMemoryNodeModel(root);
        return model.getTreeData();
    }

    /**
     * Tests whether the correct parent nodes are returned. All nodes in the
     * tree are checked.
     */
    @Test
    public void testGetParentNode()
    {
        TreeData treeData = createTreeData(ROOT_AUTHORS_TREE);
        for (int authorIdx = 0; authorIdx < NodeStructureHelper.authorsLength(); authorIdx++)
        {
            ImmutableNode authorNode =
                    nodeForKey(treeData.getRoot(),
                            NodeStructureHelper.author(authorIdx));
            assertSame(
                    "Wrong parent for " + NodeStructureHelper.author(authorIdx),
                    treeData.getRoot(), treeData.getParent(authorNode));
            for (int workIdx = 0; workIdx < NodeStructureHelper
                    .worksLength(authorIdx); workIdx++)
            {
                String workKey =
                        NodeStructureHelper.appendPath(
                                NodeStructureHelper.author(authorIdx),
                                NodeStructureHelper.work(authorIdx, workIdx));
                ImmutableNode workNode =
                        nodeForKey(treeData.getRoot(), workKey);
                assertSame("Wrong parent for " + workKey, authorNode,
                        treeData.getParent(workNode));
                for (int personaIdx = 0; personaIdx < NodeStructureHelper
                        .personaeLength(authorIdx, workIdx); personaIdx++)
                {
                    String personKey =
                            NodeStructureHelper.appendPath(workKey,
                                    NodeStructureHelper.persona(authorIdx,
                                            workIdx, personaIdx));
                    ImmutableNode personNode =
                            nodeForKey(treeData.getRoot(), personKey);
                    assertSame("Wrong parent for " + personKey, workNode,
                            treeData.getParent(personNode));
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
        TreeData treeData = createTreeData(ROOT_AUTHORS_TREE);
        assertNull("Got a parent", treeData.getParent(ROOT_AUTHORS_TREE));
    }

    /**
     * Tries to query the parent node for a node which does not belong to the
     * managed tree.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetParentInvalidNode()
    {
        TreeData treeData = createTreeData(ROOT_AUTHORS_TREE);
        treeData.getParent(new ImmutableNode.Builder().name("unknown").create());
    }

    /**
     * Tests whether the name of a node can be queried.
     */
    @Test
    public void testNodeHandlerName()
    {
        TreeData treeData = createTreeData(ROOT_AUTHORS_TREE);
        ImmutableNode author =
                nodeForKey(treeData, NodeStructureHelper.author(0));
        assertEquals("Wrong node name", NodeStructureHelper.author(0),
                treeData.nodeName(author));
    }

    /**
     * Tests whether the value of a node can be queried.
     */
    @Test
    public void testNodeHandlerValue()
    {
        TreeData treeData = createTreeData(ROOT_AUTHORS_TREE);
        ImmutableNode work = nodeForKey(treeData, "Shakespeare/The Tempest");
        int year = 1611;
        work = work.setValue(year);
        assertEquals("Wrong value", year, treeData.getValue(work));
    }

    /**
     * Tests whether the children of a node can be queried.
     */
    @Test
    public void testNodeHandlerGetChildren()
    {
        TreeData treeData = createTreeData(ROOT_AUTHORS_TREE);
        ImmutableNode node =
                nodeForKey(treeData, NodeStructureHelper.author(0));
        assertSame("Wrong children", node.getChildren(),
                treeData.getChildren(node));
    }

    /**
     * Tests whether all children of a specific name can be queried.
     */
    @Test
    public void testNodeHandlerGetChildrenByName()
    {
        TreeData treeData = createTreeData(ROOT_PERSONAE_TREE);
        String name = "Achilles";
        Set<ImmutableNode> children =
                new HashSet<ImmutableNode>(treeData.getChildren(
                        ROOT_PERSONAE_TREE, name));
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
        TreeData treeData = createTreeData(ROOT_PERSONAE_TREE);
        List<ImmutableNode> children =
                treeData.getChildren(ROOT_PERSONAE_TREE, "Ajax");
        children.add(null);
    }

    /**
     * Tests whether a child at a given index can be accessed.
     */
    @Test
    public void testNodeHandlerGetChildAtIndex()
    {
        TreeData treeData = createTreeData(ROOT_AUTHORS_TREE);
        ImmutableNode node =
                nodeForKey(treeData, NodeStructureHelper.author(0));
        assertSame("Wrong child", node.getChildren().get(1),
                treeData.getChild(node, 1));
    }

    /**
     * Tests whether the index of a given child can be queried.
     */
    @Test
    public void testNodeHandlerIndexOfChild()
    {
        TreeData treeData = createTreeData(ROOT_AUTHORS_TREE);
        String key = "Simmons/Hyperion";
        ImmutableNode parent = nodeForKey(treeData, key);
        ImmutableNode child = nodeForKey(treeData, key + "/Weintraub");
        assertEquals("Wrong child index", 3,
                treeData.indexOfChild(parent, child));
    }

    /**
     * Tests the indexOfChild() method for an unknown child node.
     */
    @Test
    public void testNodeHandlerIndexOfUnknownChild()
    {
        TreeData treeData = createTreeData(ROOT_AUTHORS_TREE);
        ImmutableNode parent = nodeForKey(treeData, "Homer/Ilias");
        ImmutableNode child =
                nodeForKey(treeData,
                        "Shakespeare/Troilus and Cressida/Achilles");
        assertEquals("Wrong child index", -1,
                treeData.indexOfChild(parent, child));
    }

    /**
     * Tests whether the number of all children can be queried.
     */
    @Test
    public void testNodeHandlerGetChildrenCountAll()
    {
        TreeData treeData = createTreeData(ROOT_AUTHORS_TREE);
        ImmutableNode node =
                nodeForKey(treeData, NodeStructureHelper.author(0));
        assertEquals("Wrong number of children",
                NodeStructureHelper.worksLength(0),
                treeData.getChildrenCount(node, null));
    }

    /**
     * Tests whether the number of all children with a given name can be
     * queried.
     */
    @Test
    public void testNodeHandlerGetChildrenCountSpecific()
    {
        TreeData treeData = createTreeData(ROOT_PERSONAE_TREE);
        assertEquals("Wrong number of children", 3,
                treeData.getChildrenCount(ROOT_PERSONAE_TREE, "Achilles"));
    }

    /**
     * Tests whether a node's attributes can be queried.
     */
    @Test
    public void testNodeHandlerGetAttributes()
    {
        TreeData treeData = createTreeData(ROOT_PERSONAE_TREE);
        ImmutableNode node = nodeForKey(treeData, "Puck");
        assertEquals("Wrong attributes", node.getAttributes().keySet(),
                treeData.getAttributes(node));
    }

    /**
     * Tests that the keys of attributes cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testNodeHandlerGetAttributesImmutable()
    {
        TreeData treeData = createTreeData(ROOT_PERSONAE_TREE);
        ImmutableNode node = nodeForKey(treeData, "Puck");
        treeData.getAttributes(node).add("test");
    }

    /**
     * Tests a positive check whether a node has attributes.
     */
    @Test
    public void testNodeHandlerHasAttributesTrue()
    {
        TreeData treeData = createTreeData(ROOT_PERSONAE_TREE);
        ImmutableNode node = nodeForKey(treeData, "Puck");
        assertTrue("No attributes", treeData.hasAttributes(node));
    }

    /**
     * Tests a negative check whether a node has attributes.
     */
    @Test
    public void testNodeHandlerHasAttributesFalse()
    {
        TreeData treeData = createTreeData(ROOT_PERSONAE_TREE);
        assertFalse("Got attributes",
                treeData.hasAttributes(ROOT_PERSONAE_TREE));
    }

    /**
     * Tests whether the value of an attribute can be queried.
     */
    @Test
    public void testNodeHandlerGetAttributeValue()
    {
        TreeData treeData = createTreeData(ROOT_PERSONAE_TREE);
        ImmutableNode node = nodeForKey(treeData, "Prospero");
        assertEquals("Wrong value", "Shakespeare", treeData.getAttributeValue(
                node, NodeStructureHelper.ATTR_AUTHOR));
    }

    /**
     * Tests whether a node with children is defined.
     */
    @Test
    public void testNodeHandlerIsDefinedChildren()
    {
        TreeData treeData = createTreeData(ROOT_AUTHORS_TREE);
        ImmutableNode node =
                nodeForKey(treeData, NodeStructureHelper.author(2));
        assertTrue("Not defined", treeData.isDefined(node));
    }

    /**
     * Tests whether a node with attributes is defined.
     */
    @Test
    public void testNodeHandlerIsDefinedAttributes()
    {
        TreeData treeData = createTreeData(ROOT_PERSONAE_TREE);
        ImmutableNode node =
                new ImmutableNode.Builder().addAttribute(
                        NodeStructureHelper.ATTR_AUTHOR,
                        NodeStructureHelper.author(0)).create();
        assertTrue("Not defined", treeData.isDefined(node));
    }

    /**
     * Tests whether a node with a value is defined.
     */
    @Test
    public void testNodeHandlerIsDefinedValue()
    {
        TreeData treeData = createTreeData(ROOT_PERSONAE_TREE);
        ImmutableNode node = new ImmutableNode.Builder().value(42).create();
        assertTrue("Not defined", treeData.isDefined(node));
    }

    /**
     * Tests whether an undefined node is correctly detected.
     */
    @Test
    public void testNodeHandlerIsDefinedFalse()
    {
        TreeData treeData = createTreeData(ROOT_PERSONAE_TREE);
        ImmutableNode node =
                new ImmutableNode.Builder().name(NodeStructureHelper.author(1))
                        .create();
        assertFalse("Defined", treeData.isDefined(node));
    }
}
