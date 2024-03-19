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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@code NodeTreeWalker}.
 */
public class TestNodeTreeWalker {
    /**
     * A visitor implementation used for testing purposes. The visitor produces a list with the names of the nodes visited
     * in the order it was called. With this it can be tested whether the nodes were visited in the correct order.
     */
    private static final class TestVisitor implements ConfigurationNodeVisitor<ImmutableNode> {
        /** A list with the names of the visited nodes. */
        private final List<String> visitedNodes = new LinkedList<>();

        /** The maximum number of nodes to be visited. */
        private int maxNodeCount = Integer.MAX_VALUE;

        /**
         * Returns the maximum number of nodes visited by this visitor.
         *
         * @return the maximum number of nodes
         */
        public int getMaxNodeCount() {
            return maxNodeCount;
        }

        /**
         * Returns the list with the names of the visited nodes.
         *
         * @return the visit list
         */
        public List<String> getVisitedNodes() {
            return visitedNodes;
        }

        /**
         * Sets the maximum number of nodes to be visited. After this the terminate flag is set.
         *
         * @param maxNodeCount the maximum number of nodes
         */
        public void setMaxNodeCount(final int maxNodeCount) {
            this.maxNodeCount = maxNodeCount;
        }

        @Override
        public boolean terminate() {
            return visitedNodes.size() >= getMaxNodeCount();
        }

        @Override
        public void visitAfterChildren(final ImmutableNode node, final NodeHandler<ImmutableNode> handler) {
            visitedNodes.add(visitAfterName(handler.nodeName(node)));
        }

        @Override
        public void visitBeforeChildren(final ImmutableNode node, final NodeHandler<ImmutableNode> handler) {
            visitedNodes.add(handler.nodeName(node));
        }
    }

    /**
     * Creates a dummy node handler.
     *
     * @return the node handler
     */
    private static NodeHandler<ImmutableNode> createHandler() {
        return new InMemoryNodeModel().getNodeHandler();
    }

    /**
     * Creates a mock for a node handler.
     *
     * @return the handler mock
     */
    @SuppressWarnings("unchecked")
    private static NodeHandler<ImmutableNode> handlerMock() {
        return mock(NodeHandler.class);
    }

    /**
     * Generates a name which indicates that the corresponding node was visited after its children.
     *
     * @param name the node name to be decorated
     * @return the name with the after indicator
     */
    private static String visitAfterName(final String name) {
        return "->" + name;
    }

    /**
     * Creates a mock for a visitor.
     *
     * @return the visitor mock
     */
    @SuppressWarnings("unchecked")
    private static ConfigurationNodeVisitor<ImmutableNode> visitorMock() {
        return mock(ConfigurationNodeVisitor.class);
    }

    /**
     * Prepares a list with the names of nodes encountered during a BFS walk.
     *
     * @return the expected node names in BFS mode
     */
    private List<String> expectBFS() {
        final List<String> expected = new LinkedList<>();
        final List<String> works = new LinkedList<>();
        final List<String> personae = new LinkedList<>();
        expected.add(NodeStructureHelper.ROOT_AUTHORS_TREE.getNodeName());
        for (int authorIdx = 0; authorIdx < NodeStructureHelper.authorsLength(); authorIdx++) {
            expected.add(NodeStructureHelper.author(authorIdx));
            for (int workIdx = 0; workIdx < NodeStructureHelper.worksLength(authorIdx); workIdx++) {
                works.add(NodeStructureHelper.work(authorIdx, workIdx));
                for (int personIdx = 0; personIdx < NodeStructureHelper.personaeLength(authorIdx, workIdx); personIdx++) {
                    personae.add(NodeStructureHelper.persona(authorIdx, workIdx, personIdx));
                }
            }
        }
        expected.addAll(works);
        expected.addAll(personae);
        return expected;
    }

    /**
     * Prepares a list with the names of nodes encountered during a DFS walk.
     *
     * @return the expected node names in DFS mode
     */
    private List<String> expectDFS() {
        final List<String> expected = new LinkedList<>();
        expected.add(NodeStructureHelper.ROOT_AUTHORS_TREE.getNodeName());
        for (int authorIdx = 0; authorIdx < NodeStructureHelper.authorsLength(); authorIdx++) {
            expected.add(NodeStructureHelper.author(authorIdx));
            for (int workIdx = 0; workIdx < NodeStructureHelper.worksLength(authorIdx); workIdx++) {
                expected.add(NodeStructureHelper.work(authorIdx, workIdx));
                for (int personaIdx = 0; personaIdx < NodeStructureHelper.personaeLength(authorIdx, workIdx); personaIdx++) {
                    final String persona = NodeStructureHelper.persona(authorIdx, workIdx, personaIdx);
                    expected.add(persona);
                    expected.add(visitAfterName(persona));
                }
                expected.add(visitAfterName(NodeStructureHelper.work(authorIdx, workIdx)));
            }
            expected.add(visitAfterName(NodeStructureHelper.author(authorIdx)));
        }
        expected.add(visitAfterName(NodeStructureHelper.ROOT_AUTHORS_TREE.getNodeName()));
        return expected;
    }

    /**
     * Tests a traversal in BFS mode.
     */
    @Test
    public void testWalkBFS() {
        final List<String> expected = expectBFS();
        final TestVisitor visitor = new TestVisitor();
        NodeTreeWalker.INSTANCE.walkBFS(NodeStructureHelper.ROOT_AUTHORS_TREE, visitor, createHandler());
        assertEquals(expected, visitor.getVisitedNodes());
    }

    /**
     * Tests a BFS walk if node is passed in.
     */
    @Test
    public void testWalkBFSNoNode() {
        final ConfigurationNodeVisitor<ImmutableNode> visitor = visitorMock();
        final NodeHandler<ImmutableNode> handler = handlerMock();
        NodeTreeWalker.INSTANCE.walkBFS(null, visitor, handler);
    }

    /**
     * Tests whether the terminate flag is evaluated in BFS mode.
     */
    @Test
    public void testWalkBFSTerminate() {
        final TestVisitor visitor = new TestVisitor();
        final int nodeCount = 9;
        visitor.setMaxNodeCount(nodeCount);
        NodeTreeWalker.INSTANCE.walkBFS(NodeStructureHelper.ROOT_AUTHORS_TREE, visitor, createHandler());
        assertEquals(nodeCount, visitor.getVisitedNodes().size());
    }

    /**
     * Tests a DFS traversal.
     */
    @Test
    public void testWalkDFS() {
        final List<String> expected = expectDFS();
        final TestVisitor visitor = new TestVisitor();
        NodeTreeWalker.INSTANCE.walkDFS(NodeStructureHelper.ROOT_AUTHORS_TREE, visitor, createHandler());
        assertEquals(expected, visitor.getVisitedNodes());
    }

    /**
     * Tests whether walkDFS() can handle a null node.
     */
    @Test
    public void testWalkDFSNoNode() {
        final ConfigurationNodeVisitor<ImmutableNode> visitor = visitorMock();
        final NodeHandler<ImmutableNode> handler = handlerMock();
        NodeTreeWalker.INSTANCE.walkDFS(null, visitor, handler);
    }

    /**
     * Tests whether the terminate flag is taken into account during a DFS walk.
     */
    @Test
    public void testWalkDFSTerminate() {
        final TestVisitor visitor = new TestVisitor();
        final int nodeCount = 5;
        visitor.setMaxNodeCount(nodeCount);
        NodeTreeWalker.INSTANCE.walkDFS(NodeStructureHelper.ROOT_AUTHORS_TREE, visitor, createHandler());
        assertEquals(nodeCount, visitor.getVisitedNodes().size());
    }

    /**
     * Tries a walk() operation without a node handler.
     */
    @Test
    public void testWalkNoNodeHandler() {
        final TestVisitor visitor = new TestVisitor();
        assertThrows(IllegalArgumentException.class, () -> NodeTreeWalker.INSTANCE.walkDFS(NodeStructureHelper.ROOT_AUTHORS_TREE, visitor, null));
    }

    /**
     * Tries a walk operation without a visitor.
     */
    @Test
    public void testWalkNoVisitor() {
        final NodeHandler<ImmutableNode> handler = createHandler();
        assertThrows(IllegalArgumentException.class, () -> NodeTreeWalker.INSTANCE.walkDFS(NodeStructureHelper.ROOT_AUTHORS_TREE, null, handler));
    }
}
