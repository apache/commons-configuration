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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A test class for {@code InMemoryNodeModel} which tests functionality related to node references. This test class
 * creates a model for the test structure with authors data. Each node is associated a string reference object with the
 * node name. It can then be checked whether updates of the hierarchy do not affect the references.
 */
public class TestInMemoryNodeModelReferences {
    /** A mock resolver. */
    private NodeKeyResolver<ImmutableNode> resolver;

    /** The test model. */
    private InMemoryNodeModel model;

    /**
     * Returns a flat collection of all nodes contained in the specified nodes hierarchy.
     *
     * @param root the root node of the hierarchy
     * @return a collection with all nodes in this hierarchy
     */
    private Collection<ImmutableNode> collectNodes(final ImmutableNode root) {
        final Set<ImmutableNode> nodes = new HashSet<>();
        NodeTreeWalker.INSTANCE.walkBFS(root, new ConfigurationNodeVisitorAdapter<ImmutableNode>() {
            @Override
            public void visitBeforeChildren(final ImmutableNode node, final NodeHandler<ImmutableNode> handler) {
                nodes.add(node);
            }
        }, model.getNodeHandler());
        return nodes;
    }

    /**
     * Creates the initial reference data for the test hierarchy.
     *
     * @return the map with reference data
     */
    private Map<ImmutableNode, String> createReferences() {
        final Collection<ImmutableNode> nodes = collectNodes(NodeStructureHelper.ROOT_AUTHORS_TREE);
        nodes.remove(NodeStructureHelper.ROOT_AUTHORS_TREE);
        final Map<ImmutableNode, String> refs = new HashMap<>();
        for (final ImmutableNode node : nodes) {
            refs.put(node, node.getNodeName());
        }
        return refs;
    }

    @BeforeEach
    public void setUp() throws Exception {
        resolver = NodeStructureHelper.createResolverMock();
        NodeStructureHelper.prepareResolveKeyForQueries(resolver);
        NodeStructureHelper.prepareResolveAddKeys(resolver);
        model = new InMemoryNodeModel();
        final Map<ImmutableNode, String> references = createReferences();
        model.mergeRoot(NodeStructureHelper.ROOT_AUTHORS_TREE, null, references, NodeStructureHelper.ROOT_AUTHORS_TREE.getNodeName(), resolver);
    }

    /**
     * Tests whether the name of the root node can be changed during a merge operation.
     */
    @Test
    public void testMergeRootOverrideName() {
        final ImmutableNode node = NodeStructureHelper.createNode("newNode", null);
        final String newName = "newRootNode";

        model.mergeRoot(node, newName, null, null, resolver);
        final ImmutableNode root = model.getNodeHandler().getRootNode();
        assertEquals(newName, root.getNodeName());
    }

    /**
     * Tests whether mergeRoot() handles an explicit reference object for the root node correctly.
     */
    @Test
    public void testMergeRootReference() {
        final Object rootRef = 20140404210508L;
        final ImmutableNode node = NodeStructureHelper.createNode("newNode", null);

        model.mergeRoot(node, null, null, rootRef, resolver);
        final ReferenceNodeHandler refHandler = model.getReferenceNodeHandler();
        final ImmutableNode checkNode = NodeStructureHelper.nodeForKey(model, "Simmons/Ilium");
        assertEquals(checkNode.getNodeName(), refHandler.getReference(checkNode));
        assertEquals(rootRef, refHandler.getReference(refHandler.getRootNode()));
    }

    /**
     * Tests whether attributes are taken into account by a merge operation.
     */
    @Test
    public void testMergeRootWithAttributes() {
        final ImmutableNode node = new ImmutableNode.Builder().addAttribute("key", "value").create();
        model.mergeRoot(node, null, null, null, resolver);
        final ImmutableNode root = model.getNodeHandler().getRootNode();
        assertEquals(Collections.singletonMap("key", "value"), root.getAttributes());
    }

    /**
     * Tests whether a value is taken into account when the root node is merged.
     */
    @Test
    public void testMergeRootWithValue() {
        final ImmutableNode node = NodeStructureHelper.createNode("newNode", "test");
        model.mergeRoot(node, null, null, null, resolver);
        final ImmutableNode root = model.getNodeHandler().getRootNode();
        assertEquals(NodeStructureHelper.ROOT_AUTHORS_TREE.getNodeName(), root.getNodeName());
        assertEquals("test", root.getValue());
    }

    /**
     * Tests whether the stored references can be queried.
     */
    @Test
    public void testQueryReferences() {
        final ReferenceNodeHandler handler = model.getReferenceNodeHandler();
        final Collection<ImmutableNode> nodes = collectNodes(handler.getRootNode());
        for (final ImmutableNode node : nodes) {
            assertEquals(node.getNodeName(), handler.getReference(node));
        }
    }

    /**
     * Tests whether references can be queried after an update operation.
     */
    @Test
    public void testQueryReferencesAfterUpdate() {
        model.addProperty("Simmons.Hyperion", Collections.singleton("Lamia"), resolver);
        final ReferenceNodeHandler handler = model.getReferenceNodeHandler();
        assertEquals("Hyperion", handler.getReference(NodeStructureHelper.nodeForKey(model, "Simmons/Hyperion")));
        assertEquals("Simmons", handler.getReference(NodeStructureHelper.nodeForKey(model, "Simmons")));
    }

    /**
     * Tests the reference returned for an unknown node.
     */
    @Test
    public void testQueryReferenceUnknown() {
        final ReferenceNodeHandler handler = model.getReferenceNodeHandler();
        assertNull(handler.getReference(new ImmutableNode.Builder().create()));
    }

    /**
     * Tests whether removed references can be queried.
     */
    @Test
    public void testQueryRemovedReferencesAfterRemove() {
        model.clearTree("Simmons", resolver);
        final ReferenceNodeHandler handler = model.getReferenceNodeHandler();
        final List<Object> removedRefs = handler.removedReferences();
        final int authorIdx = 2;
        for (int i = 0; i < NodeStructureHelper.worksLength(authorIdx); i++) {
            assertTrue(removedRefs.contains(NodeStructureHelper.work(authorIdx, i)), "Work not found: " + i);
            for (int j = 0; j < NodeStructureHelper.personaeLength(authorIdx, i); j++) {
                assertTrue(removedRefs.contains(NodeStructureHelper.persona(authorIdx, i, j)), "Persona not found: " + j);
            }
        }
    }

    /**
     * Tests whether the removed references can be queried if there are none.
     */
    @Test
    public void testQueryRemovedReferencesEmpty() {
        final ReferenceNodeHandler handler = model.getReferenceNodeHandler();
        assertTrue(handler.removedReferences().isEmpty());
    }

    /**
     * Tests that the list with removed references cannot be modified.
     */
    @Test
    public void testRemovedReferencesModify() {
        model.clearTree("Simmons", resolver);
        final ReferenceNodeHandler handler = model.getReferenceNodeHandler();
        final List<Object> removedRefs = handler.removedReferences();
        assertThrows(UnsupportedOperationException.class, () -> removedRefs.add("another one"));
    }

    /**
     * Tests whether the root node of the model can be replaced.
     */
    @Test
    public void testReplaceRoot() {
        final NodeSelector selector = new NodeSelector("Simmons.Hyperion");
        model.trackNode(selector, resolver);
        final ImmutableNode trackedNode = model.getTrackedNode(selector);
        model.addProperty("Simmons.Hyperion.Lamia", Collections.singleton("new person"), resolver);

        model.replaceRoot(NodeStructureHelper.ROOT_AUTHORS_TREE, resolver);
        final ImmutableNode node = model.getTrackedNode(selector);
        assertEquals(trackedNode, node);
        assertFalse(model.isTrackedNodeDetached(selector));
        assertNull(model.getReferenceNodeHandler().getReference(trackedNode));
    }

    /**
     * Tries to call replaceRoot() with a null node.
     */
    @Test
    public void testReplaceRootNull() {
        assertThrows(IllegalArgumentException.class, () -> model.replaceRoot(null, resolver));
    }
}
