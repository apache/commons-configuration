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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@code ImmutableNode}
 */
public class TestImmutableNode {
    /** Constant for a test node name. */
    private static final String NAME = "testNode";

    /** Constant for an attribute key. */
    private static final String ATTR = "attr";

    /** Constant for an attribute value. */
    private static final String ATTR_VALUE = "attrValue";

    /** Constant for a test node value. */
    private static final Integer VALUE = 42;

    /**
     * Checks whether a node has the expected attributes.
     *
     * @param node the node to be checked
     * @param expAttrs the expected attributes
     */
    private static void checkAttributes(final ImmutableNode node, final Map<String, ?> expAttrs) {
        assertEquals(expAttrs, node.getAttributes());
    }

    /**
     * Helper method for testing whether a node contains all the attributes in the specified map.
     *
     * @param node the node to be checked
     * @param attributes the map with expected attributes
     */
    private static void checkAttributesContained(final ImmutableNode node, final Map<String, Object> attributes) {
        for (final Map.Entry<String, Object> e : attributes.entrySet()) {
            assertEquals(e.getValue(), node.getAttributes().get(e.getKey()));
        }
    }

    /**
     * Checks whether a node has exactly the specified children.
     *
     * @param parent the parent node to be checked
     * @param children the expected children
     */
    private static void checkChildNodes(final ImmutableNode parent, final ImmutableNode... children) {
        checkChildNodes(parent, Arrays.asList(children));
    }

    /**
     * Checks whether a node has the expected children.
     *
     * @param node the node to be checked
     * @param expChildren the collection with the expected children
     */
    private static void checkChildNodes(final ImmutableNode node, final List<ImmutableNode> expChildren) {
        assertEquals(expChildren, node.getChildren());
    }

    /**
     * Checks whether an updated node has the expected basic properties.
     *
     * @param org the original node
     * @param updated the updated node
     */
    private static void checkUpdatedNode(final ImmutableNode org, final ImmutableNode updated) {
        assertNotSame(org, updated);
        assertEquals(NAME, updated.getNodeName());
        assertEquals(VALUE, updated.getValue());
    }

    /**
     * Sets up a builder with default settings.
     *
     * @return the default builder
     */
    private static ImmutableNode.Builder setUpBuilder() {
        final ImmutableNode.Builder builder = new ImmutableNode.Builder();
        builder.name(NAME).value(VALUE);
        return builder;
    }

    /** A counter for generating unique child node names. */
    private int childCounter;

    /**
     * Helper method for testing a setAttributes() operation which has no effect.
     *
     * @param attributes the map with attributes
     */
    private void checkSetAttributesNoOp(final Map<String, Object> attributes) {
        final ImmutableNode node = createDefaultNode(VALUE);
        assertSame(node, node.setAttributes(attributes));
    }

    /**
     * Creates a default child node with a unique name.
     *
     * @return the new child node
     */
    private ImmutableNode createChild() {
        final int idx = childCounter++;
        return new ImmutableNode.Builder().name("Child" + idx).value("childValue" + idx).create();
    }

    /**
     * Creates a default node instance which can be used by tests for updating properties.
     *
     * @param value the value of the node
     * @return the default node instance
     */
    private ImmutableNode createDefaultNode(final Object value) {
        return createDefaultNode(NAME, value);
    }

    /**
     * Creates a default node instance with a variable name and value that can be used by tests for updating properties.
     *
     * @param name the name of the node
     * @param value the value of the node
     * @return the default node instance
     */
    private ImmutableNode createDefaultNode(final String name, final Object value) {
        final ImmutableNode.Builder builder = new ImmutableNode.Builder(1);
        return builder.name(name).addChild(createChild()).addAttribute("testAttr", "anotherTest").value(value).create();
    }

    /**
     * Tests whether addAttributes() handles null input.
     */
    @Test
    public void testAddAttributesNull() {
        final ImmutableNode.Builder builder = setUpBuilder();
        builder.addAttributes(null);
        final ImmutableNode node = builder.create();
        assertTrue(node.getAttributes().isEmpty());
    }

    /**
     * Tests whether a child node can be added.
     */
    @Test
    public void testAddChild() {
        final ImmutableNode node = createDefaultNode(VALUE);
        final ImmutableNode child2 = new ImmutableNode.Builder().name("child2").create();
        final ImmutableNode node2 = node.addChild(child2);
        checkUpdatedNode(node, node2);
        checkChildNodes(node2, node.getChildren().get(0), child2);
    }

    /**
     * Tests whether a new null child node is rejected.
     */
    @Test
    public void testAddChildNull() {
        final ImmutableNode node = createDefaultNode(VALUE);
        assertThrows(IllegalArgumentException.class, () -> node.addChild(null));
    }

    /**
     * Tests whether addChildren() can deal with null input.
     */
    @Test
    public void testAddChildrenNull() {
        final ImmutableNode.Builder builder = setUpBuilder();
        builder.addChildren(null);
        final ImmutableNode node = builder.create();
        assertTrue(node.getChildren().isEmpty());
    }

    /**
     * Tests whether null entries in a collection with new child nodes are ignored.
     */
    @Test
    public void testAddChildrenNullElement() {
        final ImmutableNode.Builder builder = setUpBuilder();
        final List<ImmutableNode> children = Arrays.asList(createChild(), null, createChild());
        builder.addChildren(children);
        final ImmutableNode node = builder.create();
        checkChildNodes(node, children.get(0), children.get(2));
    }

    /**
     * Tests that a node's attributes cannot be directly manipulated.
     */
    @Test
    public void testAttributesImmutable() {
        final ImmutableNode node = setUpBuilder().create();
        final Map<String, Object> attributes = node.getAttributes();
        assertThrows(UnsupportedOperationException.class, () -> attributes.put("test", VALUE));
    }

    /**
     * Tests that a node's children cannot be manipulated.
     */
    @Test
    public void testChildrenImmutable() {
        final ImmutableNode node = setUpBuilder().create();
        final List<ImmutableNode> children = node.getChildren();
        assertThrows(UnsupportedOperationException.class, () -> children.add(null));
    }

    /**
     * Tests getting named children.
     */
    @Test
    public void testGetChildrenByMissingName() {
        final ImmutableNode node = createDefaultNode(VALUE);
        final ImmutableNode child2 = new ImmutableNode.Builder().name("child2").create();
        final ImmutableNode node2 = node.addChild(child2);
        checkUpdatedNode(node, node2);
        assertTrue(node2.getChildren("NotFound").isEmpty());
    }

    /**
     * Tests getting named children.
     */
    @Test
    public void testGetChildrenByName() {
        final ImmutableNode node = createDefaultNode(VALUE);
        final ImmutableNode child2 = new ImmutableNode.Builder().name("child2").create();
        final ImmutableNode node2 = node.addChild(child2);
        checkUpdatedNode(node, node2);
        assertEquals("child2", node2.getChildren("child2").get(0).getNodeName());
        assertEquals(child2, node2.getChildren("child2").get(0));
    }

    /**
     * Tests getting named children.
     */
    @Test
    public void testGetChildrenByNullName() {
        final ImmutableNode node = createDefaultNode(VALUE);
        final ImmutableNode child2 = new ImmutableNode.Builder().name("child2").create();
        final ImmutableNode node2 = node.addChild(child2);
        checkUpdatedNode(node, node2);
        assertTrue(node2.getChildren(null).isEmpty());
    }

    /**
     * Tests whether multiple child nodes can be added to a builder.
     */
    @Test
    public void testNodeWithAddMultipleChildren() {
        final int childCount = 4;
        final List<ImmutableNode> childNodes = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            final ImmutableNode.Builder childBuilder = new ImmutableNode.Builder();
            final ImmutableNode child = childBuilder.name(NAME + i).value(i).create();
            childNodes.add(child);
        }
        final ImmutableNode.Builder builder = setUpBuilder();
        final ImmutableNode node = builder.addChildren(childNodes).create();
        checkChildNodes(node, childNodes);
    }

    /**
     * Tests whether a node with attributes can be created.
     */
    @Test
    public void testNodeWithAttributes() {
        final ImmutableNode.Builder builder = setUpBuilder();
        final int attrCount = 4;
        final Map<String, Object> attrs = new HashMap<>();
        for (int i = 0; i < attrCount; i++) {
            final String attrName = NAME + i;
            attrs.put(attrName, i);
            builder.addAttribute(attrName, i);
        }
        final ImmutableNode node = builder.create();
        checkAttributes(node, attrs);
    }

    /**
     * Tests that the map of attributes cannot be changed by a later manipulation of the builder.
     */
    @Test
    public void testNodeWithAttributesManipulateLater() {
        final ImmutableNode.Builder builder = setUpBuilder();
        builder.addAttribute(ATTR, ATTR_VALUE);
        final ImmutableNode node = builder.create();
        builder.addAttribute("attr2", "a2");
        assertEquals(1, node.getAttributes().size());
        assertEquals(ATTR_VALUE, node.getAttributes().get(ATTR));
    }

    /**
     * Tests whether child nodes can be added.
     */
    @Test
    public void testNodeWithChildren() {
        final int childCount = 8;
        final List<ImmutableNode> childNodes = new ArrayList<>(childCount);
        final ImmutableNode.Builder builder = new ImmutableNode.Builder(childCount);
        for (int i = 0; i < childCount; i++) {
            final ImmutableNode.Builder childBuilder = new ImmutableNode.Builder();
            final ImmutableNode child = childBuilder.name(NAME + i).value(i).create();
            builder.addChild(child);
            childNodes.add(child);
        }
        final ImmutableNode node = builder.name(NAME).create();
        checkChildNodes(node, childNodes);
    }

    /**
     * Tests that the list of children cannot be changed by a later manipulation of the builder.
     */
    @Test
    public void testNodeWithChildrenManipulateLater() {
        final ImmutableNode.Builder builder = setUpBuilder();
        final ImmutableNode child = new ImmutableNode.Builder().name("Child").create();
        final ImmutableNode node = builder.addChild(child).create();
        builder.addChild(new ImmutableNode.Builder().name("AnotherChild").create());
        checkChildNodes(node, Collections.singletonList(child));
    }

    /**
     * Tests whether multiple attributes can be added in a single operation.
     */
    @Test
    public void testNodeWithMultipleAttributes() {
        final ImmutableNode.Builder builder = setUpBuilder();
        final int attrCount = 4;
        final Map<String, Object> attrs = new HashMap<>();
        for (int i = 0; i < attrCount; i++) {
            final String attrName = NAME + i;
            attrs.put(attrName, i);
        }
        final ImmutableNode node = builder.addAttributes(attrs).create();
        checkAttributes(node, attrs);
    }

    /**
     * Tests whether the builder ignores a null child node.
     */
    @Test
    public void testNodeWithNullChild() {
        final ImmutableNode.Builder builder = setUpBuilder();
        builder.addChild(null);
        final ImmutableNode node = builder.create();
        checkChildNodes(node);
    }

    /**
     * Tests whether an existing attribute can be removed.
     */
    @Test
    public void testRemoveAttributeExisting() {
        final ImmutableNode node = createDefaultNode(VALUE);
        final String attrName = node.getAttributes().keySet().iterator().next();
        final ImmutableNode node2 = node.removeAttribute(attrName);
        checkUpdatedNode(node, node2);
        assertSame(node.getChildren(), node2.getChildren());
        assertTrue(node2.getAttributes().isEmpty());
    }

    /**
     * Tests removeAttribute() if the attribute does not exist.
     */
    @Test
    public void testRemoveAttributeNotExisting() {
        final ImmutableNode node = createDefaultNode(VALUE);
        assertSame(node, node.removeAttribute(ATTR));
    }

    /**
     * Tests whether a child node can be removed.
     */
    @Test
    public void testRemoveChildExisting() {
        final ImmutableNode node = createDefaultNode(VALUE);
        final ImmutableNode child = node.getChildren().get(0);
        final ImmutableNode node2 = node.removeChild(child);
        checkUpdatedNode(node, node2);
        checkChildNodes(node2);
    }

    /**
     * Tests whether the correct child node is removed if there are multiple.
     */
    @Test
    public void testRemoveChildMultiple() {
        final ImmutableNode childRemove = createChild();
        final ImmutableNode node = createDefaultNode(VALUE).addChild(createChild()).addChild(childRemove).addChild(createChild());
        final ImmutableNode node2 = node.removeChild(childRemove);
        checkChildNodes(node2, node.getChildren().get(0), node.getChildren().get(1), node.getChildren().get(3));
    }

    /**
     * Tests whether the behavior of removeChildNode() if the node in question is not found.
     */
    @Test
    public void testRemoveChildNodeNotExisting() {
        final ImmutableNode node = createDefaultNode(VALUE);
        assertSame(node, node.removeChild(null));
    }

    /**
     * Tests whether a child node can be replaced by another one.
     */
    @Test
    public void testReplaceChildExisting() {
        final ImmutableNode childRemove = createChild();
        final ImmutableNode childReplace = createChild();
        final ImmutableNode node = createDefaultNode(VALUE).addChild(childRemove);
        final ImmutableNode node2 = node.replaceChild(childRemove, childReplace);
        checkUpdatedNode(node, node2);
        checkChildNodes(node2, node.getChildren().get(0), childReplace);
    }

    /**
     * Tests replaceChild() if the child node cannot be found.
     */
    @Test
    public void testReplaceChildNotExisting() {
        final ImmutableNode node = createDefaultNode(VALUE);
        assertSame(node, node.replaceChild(createChild(), createChild()));
    }

    /**
     * Tries to replace a child node by null.
     */
    @Test
    public void testReplaceChildNull() {
        final ImmutableNode node = createDefaultNode(VALUE);
        final ImmutableNode child = createChild();
        assertThrows(IllegalArgumentException.class, () -> node.replaceChild(child, null));
    }

    /**
     * Tests whether all children can be replaced at once.
     */
    @Test
    public void testReplaceChildren() {
        final int childCount = 8;
        final List<ImmutableNode> newChildren = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            newChildren.add(createChild());
        }
        final ImmutableNode node = createDefaultNode(VALUE);
        final ImmutableNode node2 = node.replaceChildren(newChildren);
        checkUpdatedNode(node, node2);
        checkChildNodes(node2, newChildren);
    }

    /**
     * Tests whether a node's children can be replaced by a null collection.
     */
    @Test
    public void testReplaceChildrenNullCollection() {
        final ImmutableNode node = createDefaultNode(VALUE);
        final ImmutableNode node2 = node.replaceChildren(null);
        checkUpdatedNode(node, node2);
        checkChildNodes(node2);
    }

    /**
     * Tests whether attribute values can be set.
     */
    @Test
    public void testSetAttribute() {
        final ImmutableNode node = createDefaultNode(VALUE);
        final ImmutableNode node2 = node.setAttribute("attr", ATTR_VALUE);
        checkUpdatedNode(node, node2);
        assertSame(node.getChildren(), node2.getChildren());
        final Map<String, Object> newAttrs = new HashMap<>(node.getAttributes());
        newAttrs.put(ATTR, ATTR_VALUE);
        checkAttributes(node2, newAttrs);
    }

    /**
     * Tests whether an attribute can be overridden.
     */
    @Test
    public void testSetAttributeOverride() {
        final ImmutableNode.Builder builder = setUpBuilder();
        final String attr2 = ATTR + "_other";
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put(ATTR, ATTR_VALUE);
        attrs.put(attr2, "someValue");
        final ImmutableNode node = builder.addAttributes(attrs).create();
        final ImmutableNode node2 = node.setAttribute(attr2, VALUE);
        attrs.put(attr2, VALUE);
        checkAttributes(node2, attrs);
    }

    /**
     * Tests whether multiple attributes can be set.
     */
    @Test
    public void testSetAttributes() {
        final ImmutableNode node = createDefaultNode(VALUE);
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("newAttribute1", "value1");
        attributes.put("newAttribute2", "value2");
        final ImmutableNode node2 = node.setAttributes(attributes);
        assertEquals(attributes.size() + node.getAttributes().size(), node2.getAttributes().size());
        checkAttributesContained(node2, attributes);
        checkAttributesContained(node2, node.getAttributes());
    }

    /**
     * Tests setAttributes() if an empty map is passed in.
     */
    @Test
    public void testSetAttributesEmpty() {
        checkSetAttributesNoOp(new HashMap<>());
    }

    /**
     * Tests setAttributes() for null input.
     */
    @Test
    public void testSetAttributesNull() {
        checkSetAttributesNoOp(null);
    }

    /**
     * Tests whether the name of a node can be changed for a new instance.
     */
    @Test
    public void testSetName() {
        final ImmutableNode node = createDefaultNode("anotherName", VALUE);
        final ImmutableNode node2 = node.setName(NAME);
        checkUpdatedNode(node, node2);
        assertSame(node.getChildren(), node2.getChildren());
        assertSame(node.getAttributes(), node2.getAttributes());
    }

    /**
     * Tests whether a new node with a changed value can be created.
     */
    @Test
    public void testSetValue() {
        final ImmutableNode node = createDefaultNode("test");
        final ImmutableNode node2 = node.setValue(VALUE);
        checkUpdatedNode(node, node2);
        assertSame(node.getChildren(), node2.getChildren());
        assertSame(node.getAttributes(), node2.getAttributes());
    }

    /**
     * Tests whether a node with basic properties can be created.
     */
    @Test
    public void testSimpleProperties() {
        final ImmutableNode node = setUpBuilder().create();
        assertEquals(NAME, node.getNodeName());
        assertTrue(node.getChildren().isEmpty());
        assertTrue(node.getAttributes().isEmpty());
    }

    /**
     * Tests streaming children.
     */
    @Test
    public void testStream() {
        final ImmutableNode node = createDefaultNode(VALUE);
        final ImmutableNode child2 = new ImmutableNode.Builder().name("child2").create();
        final ImmutableNode node2 = node.addChild(child2);
        checkUpdatedNode(node, node2);
        assertEquals(1, node.stream().count());
        assertEquals(0, child2.stream().count());
        assertEquals(2, node2.stream().count());
        assertEquals(1, node.stream().count());
    }

}
