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

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test class for {@code NodeTreeWalker}.
 *
 */
public class TestNodeTreeWalker
{
    /**
     * Generates a name which indicates that the corresponding node was visited
     * after its children.
     *
     * @param name the node name to be decorated
     * @return the name with the after indicator
     */
    private static String visitAfterName(final String name)
    {
        return "->" + name;
    }

    /**
     * Creates a mock for a visitor.
     *
     * @return the visitor mock
     */
    private static ConfigurationNodeVisitor<ImmutableNode> visitorMock()
    {
        @SuppressWarnings("unchecked")
        final
        ConfigurationNodeVisitor<ImmutableNode> visitor =
                EasyMock.createMock(ConfigurationNodeVisitor.class);
        return visitor;
    }

    /**
     * Creates a mock for a node handler.
     *
     * @return the handler mock
     */
    private static NodeHandler<ImmutableNode> handlerMock()
    {
        @SuppressWarnings("unchecked")
        final
        NodeHandler<ImmutableNode> handler =
                EasyMock.createMock(NodeHandler.class);
        return handler;
    }

    /**
     * Creates a dummy node handler.
     *
     * @return the node handler
     */
    private static NodeHandler<ImmutableNode> createHandler()
    {
        return new InMemoryNodeModel().getNodeHandler();
    }

    /**
     * Tries a walk() operation without a node handler.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWalkNoNodeHandler()
    {
        NodeTreeWalker.INSTANCE.walkDFS(NodeStructureHelper.ROOT_AUTHORS_TREE,
                new TestVisitor(), null);
    }

    /**
     * Tries a walk operation without a visitor.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWalkNoVisitor()
    {
        NodeTreeWalker.INSTANCE.walkDFS(NodeStructureHelper.ROOT_AUTHORS_TREE,
                null, createHandler());
    }

    /**
     * Tests whether walkDFS() can handle a null node.
     */
    @Test
    public void testWalkDFSNoNode()
    {
        final ConfigurationNodeVisitor<ImmutableNode> visitor = visitorMock();
        final NodeHandler<ImmutableNode> handler = handlerMock();
        EasyMock.replay(visitor, handler);
        NodeTreeWalker.INSTANCE.walkDFS(null, visitor, handler);
    }

    /**
     * Tests a DFS traversal.
     */
    @Test
    public void testWalkDFS()
    {
        final List<String> expected = expectDFS();
        final TestVisitor visitor = new TestVisitor();
        NodeTreeWalker.INSTANCE.walkDFS(NodeStructureHelper.ROOT_AUTHORS_TREE,
                visitor, createHandler());
        assertEquals("Wrong visited nodes", expected, visitor.getVisitedNodes());
    }

    /**
     * Prepares a list with the names of nodes encountered during a DFS walk.
     *
     * @return the expected node names in DFS mode
     */
    private List<String> expectDFS()
    {
        final List<String> expected = new LinkedList<>();
        expected.add(NodeStructureHelper.ROOT_AUTHORS_TREE.getNodeName());
        for (int authorIdx = 0; authorIdx < NodeStructureHelper.authorsLength(); authorIdx++)
        {
            expected.add(NodeStructureHelper.author(authorIdx));
            for (int workIdx = 0; workIdx < NodeStructureHelper
                    .worksLength(authorIdx); workIdx++)
            {
                expected.add(NodeStructureHelper.work(authorIdx, workIdx));
                for (int personaIdx = 0; personaIdx < NodeStructureHelper
                        .personaeLength(authorIdx, workIdx); personaIdx++)
                {
                    final String persona =
                            NodeStructureHelper.persona(authorIdx, workIdx,
                                    personaIdx);
                    expected.add(persona);
                    expected.add(visitAfterName(persona));
                }
                expected.add(visitAfterName(NodeStructureHelper.work(authorIdx,
                        workIdx)));
            }
            expected.add(visitAfterName(NodeStructureHelper.author(authorIdx)));
        }
        expected.add(visitAfterName(NodeStructureHelper.ROOT_AUTHORS_TREE
                .getNodeName()));
        return expected;
    }

    /**
     * Tests whether the terminate flag is taken into account during a DFS walk.
     */
    @Test
    public void testWalkDFSTerminate()
    {
        final TestVisitor visitor = new TestVisitor();
        final int nodeCount = 5;
        visitor.setMaxNodeCount(nodeCount);
        NodeTreeWalker.INSTANCE.walkDFS(NodeStructureHelper.ROOT_AUTHORS_TREE,
                visitor, createHandler());
        assertEquals("Wrong number of visited nodes", nodeCount, visitor
                .getVisitedNodes().size());
    }

    /**
     * Tests a BFS walk if node is passed in.
     */
    @Test
    public void testWalkBFSNoNode()
    {
        final ConfigurationNodeVisitor<ImmutableNode> visitor = visitorMock();
        final NodeHandler<ImmutableNode> handler = handlerMock();
        EasyMock.replay(visitor, handler);
        NodeTreeWalker.INSTANCE.walkBFS(null, visitor, handler);
    }

    /**
     * Tests a traversal in BFS mode.
     */
    @Test
    public void testWalkBFS()
    {
        final List<String> expected = expectBFS();
        final TestVisitor visitor = new TestVisitor();
        NodeTreeWalker.INSTANCE.walkBFS(NodeStructureHelper.ROOT_AUTHORS_TREE,
                visitor, createHandler());
        assertEquals("Wrong visited nodes", expected, visitor.getVisitedNodes());
    }

    /**
     * Prepares a list with the names of nodes encountered during a BFS walk.
     *
     * @return the expected node names in BFS mode
     */
    private List<String> expectBFS()
    {
        final List<String> expected = new LinkedList<>();
        final List<String> works = new LinkedList<>();
        final List<String> personae = new LinkedList<>();
        expected.add(NodeStructureHelper.ROOT_AUTHORS_TREE.getNodeName());
        for (int authorIdx = 0; authorIdx < NodeStructureHelper.authorsLength(); authorIdx++)
        {
            expected.add(NodeStructureHelper.author(authorIdx));
            for (int workIdx = 0; workIdx < NodeStructureHelper
                    .worksLength(authorIdx); workIdx++)
            {
                works.add(NodeStructureHelper.work(authorIdx, workIdx));
                for (int personIdx = 0; personIdx < NodeStructureHelper
                        .personaeLength(authorIdx, workIdx); personIdx++)
                {
                    personae.add(NodeStructureHelper.persona(authorIdx,
                            workIdx, personIdx));
                }
            }
        }
        expected.addAll(works);
        expected.addAll(personae);
        return expected;
    }

    /**
     * Tests whether the terminate flag is evaluated in BFS mode.
     */
    @Test
    public void testWalkBFSTerminate()
    {
        final TestVisitor visitor = new TestVisitor();
        final int nodeCount = 9;
        visitor.setMaxNodeCount(nodeCount);
        NodeTreeWalker.INSTANCE.walkBFS(NodeStructureHelper.ROOT_AUTHORS_TREE,
                visitor, createHandler());
        assertEquals("Wrong number of visited nodes", nodeCount, visitor
                .getVisitedNodes().size());
    }

    /**
     * A visitor implementation used for testing purposes. The visitor produces
     * a list with the names of the nodes visited in the order it was called.
     * With this it can be tested whether the nodes were visited in the correct
     * order.
     */
    private static class TestVisitor implements
            ConfigurationNodeVisitor<ImmutableNode>
    {
        /** A list with the names of the visited nodes. */
        private final List<String> visitedNodes = new LinkedList<>();

        /** The maximum number of nodes to be visited. */
        private int maxNodeCount = Integer.MAX_VALUE;

        /**
         * Returns the list with the names of the visited nodes.
         *
         * @return the visit list
         */
        public List<String> getVisitedNodes()
        {
            return visitedNodes;
        }

        /**
         * Returns the maximum number of nodes visited by this visitor.
         *
         * @return the maximum number of nodes
         */
        public int getMaxNodeCount()
        {
            return maxNodeCount;
        }

        /**
         * Sets the maximum number of nodes to be visited. After this the
         * terminate flag is set.
         *
         * @param maxNodeCount the maximum number of nodes
         */
        public void setMaxNodeCount(final int maxNodeCount)
        {
            this.maxNodeCount = maxNodeCount;
        }

        @Override
        public void visitBeforeChildren(final ImmutableNode node,
                final NodeHandler<ImmutableNode> handler)
        {
            visitedNodes.add(handler.nodeName(node));
        }

        @Override
        public void visitAfterChildren(final ImmutableNode node,
                final NodeHandler<ImmutableNode> handler)
        {
            visitedNodes.add(visitAfterName(handler.nodeName(node)));
        }

        @Override
        public boolean terminate()
        {
            return visitedNodes.size() >= getMaxNodeCount();
        }
    }
}
