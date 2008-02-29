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
package org.apache.commons.configuration2.expr;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;
import org.easymock.EasyMock;

import junit.framework.TestCase;

/**
 * Test class for ConfigurationNodeHandler.
 * 
 * @author hacker
 * @version $Id$
 */
public class TestConfigurationNodeHandler extends TestCase
{
    /** Constant for a node value. */
    private static final Object VALUE = "TEST";

    /** Constant for the name of a node. */
    private static final String NAME = "nodeName";

    /** The handler to be tested. */
    private ConfigurationNodeHandler handler;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        handler = new ConfigurationNodeHandler();
    }

    /**
     * Creates a mock for a configuration node.
     * 
     * @return the mock node
     */
    private static ConfigurationNode mockNode()
    {
        return EasyMock.createMock(ConfigurationNode.class);
    }

    /**
     * Tests querying the parent node.
     */
    public void testGetParent()
    {
        ConfigurationNode node = mockNode();
        ConfigurationNode ndParent = mockNode();
        EasyMock.expect(node.getParentNode()).andReturn(ndParent);
        EasyMock.replay(node, ndParent);
        assertEquals("Wrong parent node", ndParent, handler.getParent(node));
        EasyMock.verify(node, ndParent);
    }

    /**
     * Tests querying the value of a node.
     */
    public void testGetValue()
    {
        ConfigurationNode node = mockNode();
        EasyMock.expect(node.getValue()).andReturn(VALUE);
        EasyMock.replay(node);
        assertEquals("Wrong value", VALUE, handler.getValue(node));
        EasyMock.verify(node);
    }

    /**
     * Tests setting a node's value.
     */
    public void testSetValue()
    {
        ConfigurationNode node = mockNode();
        node.setValue(VALUE);
        EasyMock.replay(node);
        handler.setValue(node, VALUE);
        EasyMock.verify(node);
    }

    /**
     * Tests querying the name of a node.
     */
    public void testNodeName()
    {
        ConfigurationNode node = mockNode();
        EasyMock.expect(node.getName()).andReturn(NAME);
        EasyMock.replay(node);
        assertEquals("Wrong name", NAME, handler.nodeName(node));
        EasyMock.verify(node);
    }

    /**
     * Tests adding a child node.
     */
    public void testAddChild()
    {
        ConfigurationNode node = mockNode();
        node.addChild((ConfigurationNode) EasyMock.anyObject());
        EasyMock.replay(node);
        handler.addChild(node, NAME);
        EasyMock.verify(node);
    }

    /**
     * Tests creating a new node.
     */
    public void testCreateNode()
    {
        ConfigurationNode parent = mockNode();
        EasyMock.replay(parent);
        ConfigurationNode node = handler.createNode(parent, NAME);
        assertEquals("Wrong parent", parent, node.getParentNode());
        assertEquals("Wrong name", NAME, node.getName());
        assertNull("Node has a value", node.getValue());
    }

    /**
     * Tests querying a child by its index.
     */
    public void testGetChild()
    {
        ConfigurationNode node = mockNode();
        ConfigurationNode child = mockNode();
        final int index = 2;
        EasyMock.expect(node.getChild(index)).andReturn(child);
        EasyMock.replay(node, child);
        assertEquals("Wrong child node", child, handler.getChild(node, index));
        EasyMock.verify(node, child);
    }

    /**
     * Tests querying all children.
     */
    public void testGetChildren()
    {
        ConfigurationNode node = mockNode();
        List<ConfigurationNode> children = new ArrayList<ConfigurationNode>();
        EasyMock.expect(node.getChildren()).andReturn(children);
        EasyMock.replay(node);
        assertSame("Wrong children", children, handler.getChildren(node));
        EasyMock.verify(node);
    }

    /**
     * Tests querying all children with a given name.
     */
    public void testGetChildrenName()
    {
        ConfigurationNode node = mockNode();
        List<ConfigurationNode> children = new ArrayList<ConfigurationNode>();
        EasyMock.expect(node.getChildren(NAME)).andReturn(children);
        EasyMock.replay(node);
        assertSame("Wrong children", children, handler.getChildren(node, NAME));
        EasyMock.verify(node);
    }

    /**
     * Tests querying the attribute names.
     */
    public void testGetAttributes()
    {
        ConfigurationNode node = mockNode();
        final String[] attrNames = {
                "attr1", "testAttr", "anotherAttr"
        };
        List<ConfigurationNode> attrNodes = new ArrayList<ConfigurationNode>();
        for (String an : attrNames)
        {
            ConfigurationNode attr = mockNode();
            EasyMock.expect(attr.getName()).andStubReturn(an);
            EasyMock.replay(attr);
            attrNodes.add(attr);
        }
        EasyMock.expect(node.getAttributes()).andReturn(attrNodes);
        EasyMock.replay(node);

        List<String> attrs = handler.getAttributes(node);
        assertEquals("Wrong number of attribute names", attrNames.length, attrs
                .size());
        for (int i = 0; i < attrNames.length; i++)
        {
            assertEquals("Wrong attribute name at " + i, attrNames[i], attrs
                    .get(i));
        }
        EasyMock.verify(node);
    }

    /**
     * Tests querying the value of an attribute.
     */
    public void testGetAttributeValue()
    {
        ConfigurationNode node = mockNode();
        ConfigurationNode attr = mockNode();
        EasyMock.expect(attr.getValue()).andReturn(VALUE);
        List<ConfigurationNode> attrs = new ArrayList<ConfigurationNode>(1);
        attrs.add(attr);
        EasyMock.expect(node.getAttributes(NAME)).andReturn(attrs);
        EasyMock.replay(node, attr);
        assertEquals("Wrong value for attribute", VALUE, handler
                .getAttributeValue(node, NAME));
        EasyMock.verify(node, attr);
    }

    /**
     * Tests querying the value of a non-existing attribute. Result should be
     * null.
     */
    public void testGetAttributeValueUnknown()
    {
        ConfigurationNode node = mockNode();
        EasyMock.expect(node.getAttributes(NAME)).andReturn(
                new ArrayList<ConfigurationNode>(0));
        EasyMock.replay(node);
        assertNull("Wrong value for non-existing attribute", handler
                .getAttributeValue(node, NAME));
        EasyMock.verify(node);
    }

    /**
     * Tests setting the value of an attribute.
     */
    public void testSetAttributeValue()
    {
        ConfigurationNode node = new DefaultConfigurationNode();
        ConfigurationNode attr = new DefaultConfigurationNode(NAME, "oldValue");
        node.addAttribute(attr);
        handler.setAttributeValue(node, NAME, VALUE);
        List<ConfigurationNode> attrs = node.getAttributes(NAME);
        assertEquals("Wrong size of attribute list", 1, attrs.size());
        assertEquals("Wrong attribute value", VALUE, attrs.get(0).getValue());
    }

    /**
     * Tests removing a child from a node.
     */
    public void testRemoveChild()
    {
        ConfigurationNode node = mockNode();
        ConfigurationNode child = mockNode();
        EasyMock.expect(node.removeChild(child)).andReturn(Boolean.TRUE);
        EasyMock.replay(node, child);
        handler.removeChild(node, child);
        EasyMock.verify(node, child);
    }

    /**
     * Tests removing an attribute from a node.
     */
    public void testRemoveAttribute()
    {
        ConfigurationNode node = mockNode();
        EasyMock.expect(node.removeAttribute(NAME)).andReturn(Boolean.TRUE);
        EasyMock.replay(node);
        handler.removeAttribute(node, NAME);
        EasyMock.verify(node);
    }
}
