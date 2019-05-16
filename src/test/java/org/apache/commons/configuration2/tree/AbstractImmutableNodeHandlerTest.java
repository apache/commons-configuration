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
 * A base test class for {@code NodeHandler} implementations for immutable
 * nodes. Concrete sub classes have to implement a method which creates a new
 * handler object for a given nodes structure.
 *
 */
public abstract class AbstractImmutableNodeHandlerTest
{
    /**
     * Creates a new {@code NodeHandler} object for the specified nodes
     * structure.
     *
     * @param root the root of the nodes structure
     * @return the handler object
     */
    protected abstract NodeHandler<ImmutableNode> createHandler(
            ImmutableNode root);

    /**
     * Tests whether the correct parent nodes are returned. All nodes in the
     * tree are checked.
     */
    @Test
    public void testGetParentNode()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_AUTHORS_TREE);
        for (int authorIdx = 0; authorIdx < NodeStructureHelper.authorsLength(); authorIdx++)
        {
            final ImmutableNode authorNode =
                    nodeForKey(handler.getRootNode(),
                            NodeStructureHelper.author(authorIdx));
            assertSame(
                    "Wrong parent for " + NodeStructureHelper.author(authorIdx),
                    handler.getRootNode(), handler.getParent(authorNode));
            for (int workIdx = 0; workIdx < NodeStructureHelper
                    .worksLength(authorIdx); workIdx++)
            {
                final String workKey =
                        NodeStructureHelper.appendPath(
                                NodeStructureHelper.author(authorIdx),
                                NodeStructureHelper.work(authorIdx, workIdx));
                final ImmutableNode workNode =
                        nodeForKey(handler.getRootNode(), workKey);
                assertSame("Wrong parent for " + workKey, authorNode,
                        handler.getParent(workNode));
                for (int personaIdx = 0; personaIdx < NodeStructureHelper
                        .personaeLength(authorIdx, workIdx); personaIdx++)
                {
                    final String personKey =
                            NodeStructureHelper.appendPath(workKey,
                                    NodeStructureHelper.persona(authorIdx,
                                            workIdx, personaIdx));
                    final ImmutableNode personNode =
                            nodeForKey(handler.getRootNode(), personKey);
                    assertSame("Wrong parent for " + personKey, workNode,
                            handler.getParent(personNode));
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
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_AUTHORS_TREE);
        assertNull("Got a parent", handler.getParent(ROOT_AUTHORS_TREE));
    }

    /**
     * Tries to query the parent node for a node which does not belong to the
     * managed tree.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetParentInvalidNode()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_AUTHORS_TREE);
        handler.getParent(new ImmutableNode.Builder().name("unknown").create());
    }

    /**
     * Tests whether the name of a node can be queried.
     */
    @Test
    public void testNodeHandlerName()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_AUTHORS_TREE);
        final ImmutableNode author =
                nodeForKey(handler, NodeStructureHelper.author(0));
        assertEquals("Wrong node name", NodeStructureHelper.author(0),
                handler.nodeName(author));
    }

    /**
     * Tests whether the value of a node can be queried.
     */
    @Test
    public void testNodeHandlerValue()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_AUTHORS_TREE);
        ImmutableNode work = nodeForKey(handler, "Shakespeare/The Tempest");
        final int year = 1611;
        work = work.setValue(year);
        assertEquals("Wrong value", year, handler.getValue(work));
    }

    /**
     * Tests whether the children of a node can be queried.
     */
    @Test
    public void testNodeHandlerGetChildren()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_AUTHORS_TREE);
        final ImmutableNode node =
                nodeForKey(handler, NodeStructureHelper.author(0));
        assertSame("Wrong children", node.getChildren(),
                handler.getChildren(node));
    }

    /**
     * Tests whether all children of a specific name can be queried.
     */
    @Test
    public void testNodeHandlerGetChildrenByName()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_PERSONAE_TREE);
        final String name = "Achilles";
        final Set<ImmutableNode> children =
                new HashSet<>(handler.getChildren(
                        ROOT_PERSONAE_TREE, name));
        assertEquals("Wrong number of children", 3, children.size());
        for (final ImmutableNode c : children)
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
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_PERSONAE_TREE);
        final List<ImmutableNode> children =
                handler.getChildren(ROOT_PERSONAE_TREE, "Ajax");
        children.add(null);
    }

    /**
     * Tests whether a child at a given index can be accessed.
     */
    @Test
    public void testNodeHandlerGetChildAtIndex()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_AUTHORS_TREE);
        final ImmutableNode node =
                nodeForKey(handler, NodeStructureHelper.author(0));
        assertSame("Wrong child", node.getChildren().get(1),
                handler.getChild(node, 1));
    }

    /**
     * Tests whether the index of a given child can be queried.
     */
    @Test
    public void testNodeHandlerIndexOfChild()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_AUTHORS_TREE);
        final String key = "Simmons/Hyperion";
        final ImmutableNode parent = nodeForKey(handler, key);
        final ImmutableNode child = nodeForKey(handler, key + "/Weintraub");
        assertEquals("Wrong child index", 3,
                handler.indexOfChild(parent, child));
    }

    /**
     * Tests the indexOfChild() method for an unknown child node.
     */
    @Test
    public void testNodeHandlerIndexOfUnknownChild()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_AUTHORS_TREE);
        final ImmutableNode parent = nodeForKey(handler, "Homer/Ilias");
        final ImmutableNode child =
                nodeForKey(handler,
                        "Shakespeare/Troilus and Cressida/Achilles");
        assertEquals("Wrong child index", -1,
                handler.indexOfChild(parent, child));
    }

    /**
     * Tests whether the number of all children can be queried.
     */
    @Test
    public void testNodeHandlerGetChildrenCountAll()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_AUTHORS_TREE);
        final ImmutableNode node =
                nodeForKey(handler, NodeStructureHelper.author(0));
        assertEquals("Wrong number of children",
                NodeStructureHelper.worksLength(0),
                handler.getChildrenCount(node, null));
    }

    /**
     * Tests whether the number of all children with a given name can be
     * queried.
     */
    @Test
    public void testNodeHandlerGetChildrenCountSpecific()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_PERSONAE_TREE);
        assertEquals("Wrong number of children", 3,
                handler.getChildrenCount(ROOT_PERSONAE_TREE, "Achilles"));
    }

    /**
     * Tests whether a node's attributes can be queried.
     */
    @Test
    public void testNodeHandlerGetAttributes()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_PERSONAE_TREE);
        final ImmutableNode node = nodeForKey(handler, "Puck");
        assertEquals("Wrong attributes", node.getAttributes().keySet(),
                handler.getAttributes(node));
    }

    /**
     * Tests that the keys of attributes cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testNodeHandlerGetAttributesImmutable()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_PERSONAE_TREE);
        final ImmutableNode node = nodeForKey(handler, "Puck");
        handler.getAttributes(node).add("test");
    }

    /**
     * Tests a positive check whether a node has attributes.
     */
    @Test
    public void testNodeHandlerHasAttributesTrue()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_PERSONAE_TREE);
        final ImmutableNode node = nodeForKey(handler, "Puck");
        assertTrue("No attributes", handler.hasAttributes(node));
    }

    /**
     * Tests a negative check whether a node has attributes.
     */
    @Test
    public void testNodeHandlerHasAttributesFalse()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_PERSONAE_TREE);
        assertFalse("Got attributes",
                handler.hasAttributes(ROOT_PERSONAE_TREE));
    }

    /**
     * Tests whether the value of an attribute can be queried.
     */
    @Test
    public void testNodeHandlerGetAttributeValue()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_PERSONAE_TREE);
        final ImmutableNode node = nodeForKey(handler, "Prospero");
        assertEquals("Wrong value", "Shakespeare", handler.getAttributeValue(
                node, NodeStructureHelper.ATTR_AUTHOR));
    }

    /**
     * Tests whether a node with children is defined.
     */
    @Test
    public void testNodeHandlerIsDefinedChildren()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_AUTHORS_TREE);
        final ImmutableNode node =
                nodeForKey(handler, NodeStructureHelper.author(2));
        assertTrue("Not defined", handler.isDefined(node));
    }

    /**
     * Tests whether a node with attributes is defined.
     */
    @Test
    public void testNodeHandlerIsDefinedAttributes()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_PERSONAE_TREE);
        final ImmutableNode node =
                new ImmutableNode.Builder().addAttribute(
                        NodeStructureHelper.ATTR_AUTHOR,
                        NodeStructureHelper.author(0)).create();
        assertTrue("Not defined", handler.isDefined(node));
    }

    /**
     * Tests whether a node with a value is defined.
     */
    @Test
    public void testNodeHandlerIsDefinedValue()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_PERSONAE_TREE);
        final ImmutableNode node = new ImmutableNode.Builder().value(42).create();
        assertTrue("Not defined", handler.isDefined(node));
    }

    /**
     * Tests whether an undefined node is correctly detected.
     */
    @Test
    public void testNodeHandlerIsDefinedFalse()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_PERSONAE_TREE);
        final ImmutableNode node =
                new ImmutableNode.Builder().name(NodeStructureHelper.author(1))
                        .create();
        assertFalse("Defined", handler.isDefined(node));
    }

    /**
     * Tests a filter operation on child nodes.
     */
    @Test
    public void testNodeHandlerGetMatchingChildren()
    {
        final NodeHandler<ImmutableNode> handler =
                createHandler(ROOT_AUTHORS_TREE);
        final ImmutableNode target =
                NodeStructureHelper.nodeForKey(ROOT_AUTHORS_TREE,
                        NodeStructureHelper.author(1));
        final Set<String> encounteredAuthors = new HashSet<>();

        final NodeMatcher<ImmutableNode> matcher = new NodeMatcher<ImmutableNode>()
        {
            @Override
            public <T> boolean matches(final T node, final NodeHandler<T> paramHandler,
                    final ImmutableNode criterion)
            {
                encounteredAuthors.add(paramHandler.nodeName(node));
                return node == target;
            }
        };

        final List<ImmutableNode> result =
                handler.getMatchingChildren(handler.getRootNode(), matcher,
                        target);
        assertEquals("Wrong number of matched nodes", 1, result.size());
        assertSame("Wrong result", target, result.get(0));
        assertEquals("Wrong number of encountered nodes",
                NodeStructureHelper.authorsLength(), encounteredAuthors.size());
        for (int i = 0; i < NodeStructureHelper.authorsLength(); i++)
        {
            assertTrue("Author not found: " + NodeStructureHelper.author(i),
                    encounteredAuthors.contains(NodeStructureHelper.author(i)));
        }
    }

    /**
     * Tests that the list returned by getMatchingChildren() cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testNodeHandlerGetMatchingChildrenImmutable()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_AUTHORS_TREE);
        final List<ImmutableNode> result =
                handler.getMatchingChildren(handler.getRootNode(),
                        new DummyNodeMatcher(), this);
        result.clear();
    }

    /**
     * Tests whether filtered nodes can be counted.
     */
    @Test
    public void testNodeHandlerGetMatchingChildrenCount()
    {
        final NodeHandler<ImmutableNode> handler = createHandler(ROOT_AUTHORS_TREE);
        assertEquals("Wrong result", NodeStructureHelper.authorsLength(),
                handler.getMatchingChildrenCount(handler.getRootNode(),
                        new DummyNodeMatcher(), this));
    }

    /**
     * A dummy NodeMatcher implementation that will simply accept all passed in nodes.
     */
    private static class DummyNodeMatcher implements NodeMatcher<Object>
    {
        @Override
        public <T> boolean matches(final T node, final NodeHandler<T> handler, final Object criterion) {
            return true;
        }
    }
}
