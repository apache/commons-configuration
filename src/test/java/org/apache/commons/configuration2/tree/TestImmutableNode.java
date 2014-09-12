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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Test class for {@code ImmutableNode}
 *
 * @version $Id$
 */
public class TestImmutableNode
{
    /** Constant for a test node name. */
    private static final String NAME = "testNode";

    /** Constant for an attribute key. */
    private static final String ATTR = "attr";

    /** Constant for an attribute value. */
    private static final String ATTR_VALUE = "attrValue";

    /** Constant for a test node value. */
    private static final Integer VALUE = 42;

    /** A counter for generating unique child node names. */
    private int childCounter;

    /**
     * Sets up a builder with default settings.
     *
     * @return the default builder
     */
    private static ImmutableNode.Builder setUpBuilder()
    {
        ImmutableNode.Builder builder = new ImmutableNode.Builder();
        builder.name(NAME).value(VALUE);
        return builder;
    }

    /**
     * Tests whether a node with basic properties can be created.
     */
    @Test
    public void testSimpleProperties()
    {
        ImmutableNode node = setUpBuilder().create();
        assertEquals("Wrong node name", NAME, node.getNodeName());
        assertTrue("Got children", node.getChildren().isEmpty());
        assertTrue("Got attributes", node.getAttributes().isEmpty());
    }

    /**
     * Tests that a node's children cannot be manipulated.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testChildrenImmutable()
    {
        ImmutableNode node = setUpBuilder().create();
        node.getChildren().add(null);
    }

    /**
     * Tests that a node's attributes cannot be directly manipulated.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testAttributesImmutable()
    {
        ImmutableNode node = setUpBuilder().create();
        node.getAttributes().put("test", VALUE);
    }

    /**
     * Checks whether a node has the expected children.
     *
     * @param node the node to be checked
     * @param expChildren the collection with the expected children
     */
    private static void checkChildNodes(ImmutableNode node,
            Collection<ImmutableNode> expChildren)
    {
        assertEquals("Wrong number of child nodes", expChildren.size(), node
                .getChildren().size());
        Iterator<ImmutableNode> itExp = expChildren.iterator();
        int idx = 0;
        for(ImmutableNode c : node.getChildren())
        {
            assertEquals("Wrong child at " + idx, itExp.next(), c);
            idx++;
        }
    }

    /**
     * Checks whether a node has exactly the specified children.
     *
     * @param parent the parent node to be checked
     * @param children the expected children
     */
    private static void checkChildNodes(ImmutableNode parent,
            ImmutableNode... children)
    {
        checkChildNodes(parent, Arrays.asList(children));
    }

    /**
     * Tests whether child nodes can be added.
     */
    @Test
    public void testNodeWithChildren()
    {
        final int childCount = 8;
        List<ImmutableNode> childNodes = new ArrayList<ImmutableNode>(childCount);
        ImmutableNode.Builder builder = new ImmutableNode.Builder(childCount);
        for (int i = 0; i < childCount; i++)
        {
            ImmutableNode.Builder childBuilder = new ImmutableNode.Builder();
            ImmutableNode child = childBuilder.name(NAME + i).value(i).create();
            builder.addChild(child);
            childNodes.add(child);
        }
        ImmutableNode node = builder.name(NAME).create();
        checkChildNodes(node, childNodes);
    }

    /**
     * Tests whether multiple child nodes can be added to a builder.
     */
    @Test
    public void testNodeWithAddMultipleChildren()
    {
        final int childCount = 4;
        List<ImmutableNode> childNodes =
                new ArrayList<ImmutableNode>(childCount);
        for (int i = 0; i < childCount; i++)
        {
            ImmutableNode.Builder childBuilder = new ImmutableNode.Builder();
            ImmutableNode child = childBuilder.name(NAME + i).value(i).create();
            childNodes.add(child);
        }
        ImmutableNode.Builder builder = setUpBuilder();
        ImmutableNode node = builder.addChildren(childNodes).create();
        checkChildNodes(node, childNodes);
    }

    /**
     * Tests whether the builder ignores a null child node.
     */
    @Test
    public void testNodeWithNullChild()
    {
        ImmutableNode.Builder builder = setUpBuilder();
        builder.addChild(null);
        ImmutableNode node = builder.create();
        checkChildNodes(node);
    }

    /**
     * Tests that the list of children cannot be changed by a later manipulation
     * of the builder.
     */
    @Test
    public void testNodeWithChildrenManipulateLater()
    {
        ImmutableNode.Builder builder = setUpBuilder();
        ImmutableNode child =
                new ImmutableNode.Builder().name("Child").create();
        ImmutableNode node = builder.addChild(child).create();
        builder.addChild(new ImmutableNode.Builder().name("AnotherChild")
                .create());
        checkChildNodes(node, Collections.singleton(child));
    }

    /**
     * Tests whether addChildren() can deal with null input.
     */
    @Test
    public void testAddChildrenNull()
    {
        ImmutableNode.Builder builder = setUpBuilder();
        builder.addChildren(null);
        ImmutableNode node = builder.create();
        assertTrue("Got children", node.getChildren().isEmpty());
    }

    /**
     * Tests whether null entries in a collection with new child nodes are ignored.
     */
    @Test
    public void testAddChildrenNullElement()
    {
        ImmutableNode.Builder builder = setUpBuilder();
        List<ImmutableNode> children = Arrays.asList(createChild(), null, createChild());
        builder.addChildren(children);
        ImmutableNode node = builder.create();
        checkChildNodes(node, children.get(0), children.get(2));
    }

    /**
     * Tests whether a node with attributes can be created.
     */
    @Test
    public void testNodeWithAttributes()
    {
        ImmutableNode.Builder builder = setUpBuilder();
        final int attrCount = 4;
        Map<String, Object> attrs = new HashMap<String, Object>();
        for (int i = 0; i < attrCount; i++)
        {
            String attrName = NAME + i;
            attrs.put(attrName, i);
            builder.addAttribute(attrName, i);
        }
        ImmutableNode node = builder.create();
        checkAttributes(node, attrs);
    }

    /**
     * Checks whether a node has the expected attributes.
     *
     * @param node the node to be checked
     * @param expAttrs the expected attributes
     */
    private static void checkAttributes(ImmutableNode node,
            Map<String, ?> expAttrs)
    {
        assertEquals("Wrong number of attributes", expAttrs.size(), node
                .getAttributes().size());
        for (Map.Entry<String, ?> e : expAttrs.entrySet())
        {
            assertEquals("Wrong value for " + e.getKey(), e.getValue(), node
                    .getAttributes().get(e.getKey()));
        }
    }

    /**
     * Tests that the map of attributes cannot be changed by a later
     * manipulation of the builder.
     */
    @Test
    public void testNodeWithAttributesManipulateLater()
    {
        ImmutableNode.Builder builder = setUpBuilder();
        builder.addAttribute(ATTR, ATTR_VALUE);
        ImmutableNode node = builder.create();
        builder.addAttribute("attr2", "a2");
        assertEquals("Wrong number of attributes", 1, node.getAttributes()
                .size());
        assertEquals("Wrong attribute", ATTR_VALUE, node.getAttributes().get(ATTR));
    }

    /**
     * Tests whether multiple attributes can be added in a single operation.
     */
    @Test
    public void testNodeWithMultipleAttributes()
    {
        ImmutableNode.Builder builder = setUpBuilder();
        final int attrCount = 4;
        Map<String, Object> attrs = new HashMap<String, Object>();
        for (int i = 0; i < attrCount; i++)
        {
            String attrName = NAME + i;
            attrs.put(attrName, i);
        }
        ImmutableNode node = builder.addAttributes(attrs).create();
        checkAttributes(node, attrs);
    }

    /**
     * Tests whether addAttributes() handles null input.
     */
    @Test
    public void testAddAttributesNull()
    {
        ImmutableNode.Builder builder = setUpBuilder();
        builder.addAttributes(null);
        ImmutableNode node = builder.create();
        assertTrue("Got attributes", node.getAttributes().isEmpty());
    }

    /**
     * Creates a default node instance which can be used by tests for updating
     * properties.
     *
     * @param value the value of the node
     * @return the default node instance
     */
    private ImmutableNode createDefaultNode(Object value)
    {
        return createDefaultNode(NAME, value);
    }

    /**
     * Creates a default node instance with a variable name and value that can
     * be used by tests for updating properties.
     *
     * @param name the name of the node
     * @param value the value of the node
     * @return the default node instance
     */
    private ImmutableNode createDefaultNode(String name, Object value)
    {
        ImmutableNode.Builder builder = new ImmutableNode.Builder(1);
        return builder.name(name).addChild(createChild())
                .addAttribute("testAttr", "anotherTest").value(value).create();
    }

    /**
     * Creates a default child node with a unique name.
     *
     * @return the new child node
     */
    private ImmutableNode createChild()
    {
        int idx = childCounter++;
        return new ImmutableNode.Builder().name("Child" + idx)
                .value("childValue" + idx).create();
    }

    /**
     * Checks whether an updated node has the expected basic properties.
     *
     * @param org the original node
     * @param updated the updated node
     */
    private static void checkUpdatedNode(ImmutableNode org,
            ImmutableNode updated)
    {
        assertNotSame("Same instance", org, updated);
        assertEquals("Wrong node name", NAME, updated.getNodeName());
        assertEquals("Wrong value", VALUE, updated.getValue());
    }

    /**
     * Tests whether a new node with a changed value can be created.
     */
    @Test
    public void testSetValue()
    {
        ImmutableNode node = createDefaultNode("test");
        ImmutableNode node2 = node.setValue(VALUE);
        checkUpdatedNode(node, node2);
        assertSame("Different children", node.getChildren(),
                node2.getChildren());
        assertSame("Different attributes", node.getAttributes(),
                node2.getAttributes());
    }

    /**
     * Tests whether the name of a node can be changed for a new instance.
     */
    @Test
    public void testSetName()
    {
        ImmutableNode node = createDefaultNode("anotherName", VALUE);
        ImmutableNode node2 = node.setName(NAME);
        checkUpdatedNode(node, node2);
        assertSame("Different children", node.getChildren(),
                node2.getChildren());
        assertSame("Different attributes", node.getAttributes(),
                node2.getAttributes());
    }

    /**
     * Tests whether a child node can be added.
     */
    @Test
    public void testAddChild()
    {
        ImmutableNode node = createDefaultNode(VALUE);
        ImmutableNode child2 =
                new ImmutableNode.Builder().name("child2").create();
        ImmutableNode node2 = node.addChild(child2);
        checkUpdatedNode(node, node2);
        checkChildNodes(node2, node.getChildren().get(0), child2);
    }

    /**
     * Tests whether a new null child node is rejected.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddChildNull()
    {
        createDefaultNode(VALUE).addChild(null);
    }

    /**
     * Tests whether a child node can be removed.
     */
    @Test
    public void testRemoveChildExisting()
    {
        ImmutableNode node = createDefaultNode(VALUE);
        ImmutableNode child = node.getChildren().get(0);
        ImmutableNode node2 = node.removeChild(child);
        checkUpdatedNode(node, node2);
        checkChildNodes(node2);
    }

    /**
     * Tests whether the correct child node is removed if there are multiple.
     */
    @Test
    public void testRemoveChildMultiple()
    {
        ImmutableNode childRemove = createChild();
        ImmutableNode node =
                createDefaultNode(VALUE).addChild(createChild())
                        .addChild(childRemove).addChild(createChild());
        ImmutableNode node2 = node.removeChild(childRemove);
        checkChildNodes(node2, node.getChildren().get(0), node.getChildren()
                .get(1), node.getChildren().get(3));
    }

    /**
     * Tests whether the behavior of removeChildNode() if the node in question
     * is not found.
     */
    @Test
    public void testRemoveChildNodeNotExisting()
    {
        ImmutableNode node = createDefaultNode(VALUE);
        assertSame("Got different instance", node, node.removeChild(null));
    }

    /**
     * Tests whether a child node can be replaced by another one.
     */
    @Test
    public void testReplaceChildExisting()
    {
        ImmutableNode childRemove = createChild();
        ImmutableNode childReplace = createChild();
        ImmutableNode node = createDefaultNode(VALUE).addChild(childRemove);
        ImmutableNode node2 = node.replaceChild(childRemove, childReplace);
        checkUpdatedNode(node, node2);
        checkChildNodes(node2, node.getChildren().get(0), childReplace);
    }

    /**
     * Tests replaceChild() if the child node cannot be found.
     */
    @Test
    public void testReplaceChildNotExisting()
    {
        ImmutableNode node = createDefaultNode(VALUE);
        assertSame("Got different instance", node,
                node.replaceChild(createChild(), createChild()));
    }

    /**
     * Tries to replace a child node by null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testReplaceChildNull()
    {
        createDefaultNode(VALUE).replaceChild(createChild(), null);
    }

    /**
     * Tests whether attribute values can be set.
     */
    @Test
    public void testSetAttribute()
    {
        ImmutableNode node = createDefaultNode(VALUE);
        ImmutableNode node2 = node.setAttribute("attr", ATTR_VALUE);
        checkUpdatedNode(node, node2);
        assertSame("Wrong children", node.getChildren(), node2.getChildren());
        Map<String, Object> newAttrs =
                new HashMap<String, Object>(node.getAttributes());
        newAttrs.put(ATTR, ATTR_VALUE);
        checkAttributes(node2, newAttrs);
    }

    /**
     * Tests whether an attribute can be overridden.
     */
    @Test
    public void testSetAttributeOverride()
    {
        ImmutableNode.Builder builder = setUpBuilder();
        final String attr2 = ATTR + "_other";
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put(ATTR, ATTR_VALUE);
        attrs.put(attr2, "someValue");
        ImmutableNode node = builder.addAttributes(attrs).create();
        ImmutableNode node2 = node.setAttribute(attr2, VALUE);
        attrs.put(attr2, VALUE);
        checkAttributes(node2, attrs);
    }

    /**
     * Tests whether multiple attributes can be set.
     */
    @Test
    public void testSetAttributes()
    {
        ImmutableNode node = createDefaultNode(VALUE);
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("newAttribute1", "value1");
        attributes.put("newAttribute2", "value2");
        ImmutableNode node2 = node.setAttributes(attributes);
        assertEquals("Wrong number of attributes", attributes.size()
                + node.getAttributes().size(), node2.getAttributes().size());
        checkAttributesContained(node2, attributes);
        checkAttributesContained(node2, node.getAttributes());
    }

    /**
     * Helper method for testing whether a node contains all the attributes in
     * the specified map.
     *
     * @param node the node to be checked
     * @param attributes the map with expected attributes
     */
    private static void checkAttributesContained(ImmutableNode node,
            Map<String, Object> attributes)
    {
        for (Map.Entry<String, Object> e : attributes.entrySet())
        {
            assertEquals("Wrong attribute value", e.getValue(), node
                    .getAttributes().get(e.getKey()));
        }
    }

    /**
     * Helper method for testing a setAttributes() operation which has no
     * effect.
     *
     * @param attributes the map with attributes
     */
    private void checkSetAttributesNoOp(Map<String, Object> attributes)
    {
        ImmutableNode node = createDefaultNode(VALUE);
        assertSame("Node was changed", node, node.setAttributes(attributes));
    }

    /**
     * Tests setAttributes() if an empty map is passed in.
     */
    @Test
    public void testSetAttributesEmpty()
    {
        checkSetAttributesNoOp(new HashMap<String, Object>());
    }

    /**
     * Tests setAttributes() for null input.
     */
    @Test
    public void testSetAttributesNull()
    {
        checkSetAttributesNoOp(null);
    }

    /**
     * Tests whether an existing attribute can be removed.
     */
    @Test
    public void testRemoveAttributeExisting()
    {
        ImmutableNode node = createDefaultNode(VALUE);
        String attrName = node.getAttributes().keySet().iterator().next();
        ImmutableNode node2 = node.removeAttribute(attrName);
        checkUpdatedNode(node, node2);
        assertSame("Wrong children", node.getChildren(), node2.getChildren());
        assertTrue("Attribute not deleted", node2.getAttributes().isEmpty());
    }

    /**
     * Tests removeAttribute() if the attribute does not exist.
     */
    @Test
    public void testRemoveAttributeNotExisting()
    {
        ImmutableNode node = createDefaultNode(VALUE);
        assertSame("Got different instance", node, node.removeAttribute(ATTR));
    }

    /**
     * Tests whether all children can be replaced at once.
     */
    @Test
    public void testReplaceChildren()
    {
        final int childCount = 8;
        Collection<ImmutableNode> newChildren =
                new ArrayList<ImmutableNode>(childCount);
        for (int i = 0; i < childCount; i++)
        {
            newChildren.add(createChild());
        }
        ImmutableNode node = createDefaultNode(VALUE);
        ImmutableNode node2 = node.replaceChildren(newChildren);
        checkUpdatedNode(node, node2);
        checkChildNodes(node2, newChildren);
    }

    /**
     * Tests whether a node's children can be replaced by a null collection.
     */
    @Test
    public void testReplaceChildrenNullCollection()
    {
        ImmutableNode node = createDefaultNode(VALUE);
        ImmutableNode node2 = node.replaceChildren(null);
        checkUpdatedNode(node, node2);
        checkChildNodes(node2);
    }
}
