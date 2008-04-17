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
package org.apache.commons.configuration2.combined;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ConfigurationRuntimeException;
import org.apache.commons.configuration2.expr.ConfigurationNodeHandler;
import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.expr.NodeHandlerRegistry;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;
import org.easymock.EasyMock;

import junit.framework.TestCase;

/**
 * Test class for CombinedConfigurationNodeHandler.
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public class TestCombinedConfigurationNodeHandler extends TestCase
{
    /** Constant for the name of an attribute or child node. */
    private static final String SUB_NAME = "element";

    /** Constant for a value passed to the handler. */
    private static final Object VALUE = "A value";

    /** Constant for a test node. */
    private static final Object NODE = EasyMock
            .createNiceMock(ConfigurationNode.class);

    /** A mock object for a (sub) node handler. */
    private NodeHandler<Object> subHandler;

    /** The handler to be tested. */
    private CombinedConfigurationNodeHandler handler;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        handler = new CombinedConfigurationNodeHandler();
        setUpHandlerMap();
    }

    /**
     * Initializes a map with test node handlers.
     */
    @SuppressWarnings("unchecked")
    private void setUpHandlerMap()
    {
        Map<Class<?>, NodeHandler<?>> handlers = new HashMap<Class<?>, NodeHandler<?>>();
        subHandler = EasyMock.createMock(NodeHandler.class);
        handlers.put(ConfigurationNode.class, new ConfigurationNodeHandler());
        handlers.put(getClass(), subHandler);
        handler.setHandlers(handlers);
    }

    /**
     * Tests the resolveHandler() method when there is no parent registry. In
     * this case only the local map with handlers should be searched.
     */
    public void testResolveHandlerNoParentDirect()
    {
        NodeHandler<?> h = handler.resolveHandler(this);
        assertEquals("Wrong handler returned", subHandler, h);
    }

    /**
     * Tests the resolveHandler() method when there is no parent registry and
     * only a handler for a super interface is registered.
     */
    public void testResolveHandlerNoParentSubClass()
    {
        NodeHandler<?> h = handler
                .resolveHandler(new DefaultConfigurationNode());
        assertTrue("Wrong handler returned",
                h instanceof ConfigurationNodeHandler);
    }

    /**
     * Tests the resolveHandler() method when a parent registry is set. In this
     * case only this parent should be invoked.
     */
    public void testResolveHandlerParent()
    {
        NodeHandlerRegistry reg = EasyMock
                .createMock(NodeHandlerRegistry.class);
        reg.addSubRegistry(handler);
        EasyMock.expect(reg.resolveHandler(this));
        EasyMock.expectLastCall().andReturn(subHandler);
        EasyMock.replay(reg);
        handler.initNodeHandlerRegistry(reg);
        assertEquals("Wrong handler returned", subHandler, handler
                .resolveHandler(this));
        EasyMock.verify(reg);
    }

    /**
     * Tests the resolveHandler() method when no parent registry is set, but
     * there are sub registries that can resolve the node class.
     */
    public void testResolveHandlerNoParentSubRegistry()
    {
        NodeHandlerRegistry subReg1 = EasyMock
                .createMock(NodeHandlerRegistry.class);
        NodeHandlerRegistry subReg2 = EasyMock
                .createMock(NodeHandlerRegistry.class);
        NodeHandlerRegistry subReg3 = EasyMock
                .createMock(NodeHandlerRegistry.class);
        final Object testNode = 42; // a test "node" object of class Integer
        EasyMock.expect(subReg1.lookupHandler(testNode, false)).andReturn(null);
        EasyMock.expect(subReg2.lookupHandler(testNode, false));
        EasyMock.expectLastCall().andReturn(subHandler);
        EasyMock.replay(subReg1, subReg2, subReg3);
        handler.addSubRegistry(subReg1);
        handler.addSubRegistry(subReg2);
        handler.addSubRegistry(subReg3);
        assertEquals("Wrong handler returned", subHandler, handler
                .resolveHandler(testNode));
        EasyMock.verify(subReg1, subReg2, subReg3);
    }

    /**
     * Tests the resolveHandler() method when no parent registry is set, but
     * there are sub registries that know a super class of the passed in node.
     */
    public void testResolveHandlerNoParentSubRegistrySubClass()
    {
        NodeHandlerRegistry subReg1 = EasyMock
                .createMock(NodeHandlerRegistry.class);
        NodeHandlerRegistry subReg2 = EasyMock
                .createMock(NodeHandlerRegistry.class);
        NodeHandlerRegistry subReg3 = EasyMock
                .createMock(NodeHandlerRegistry.class);
        final Object testNode = 42; // a test "node" object of class Integer
        EasyMock.expect(subReg1.lookupHandler(testNode, false)).andReturn(null);
        EasyMock.expect(subReg2.lookupHandler(testNode, false)).andReturn(null);
        EasyMock.expect(subReg3.lookupHandler(testNode, false)).andReturn(null);
        EasyMock.expect(subReg1.lookupHandler(testNode, true)).andReturn(null);
        EasyMock.expect(subReg2.lookupHandler(testNode, true));
        EasyMock.expectLastCall().andReturn(subHandler);
        EasyMock.replay(subReg1, subReg2, subReg3);
        handler.addSubRegistry(subReg1);
        handler.addSubRegistry(subReg2);
        handler.addSubRegistry(subReg3);
        assertEquals("Wrong handler returned", subHandler, handler
                .resolveHandler(testNode));
        EasyMock.verify(subReg1, subReg2, subReg3);
    }

    /**
     * Tests the resolveHandler() method when there is a parent registry and sub
     * registries. In this case only the parent is invoked.
     */
    public void testResolveHandlerParentAndSubRegistry()
    {
        NodeHandlerRegistry reg = EasyMock
                .createMock(NodeHandlerRegistry.class);
        NodeHandlerRegistry subReg = EasyMock
                .createMock(NodeHandlerRegistry.class);
        reg.addSubRegistry(handler);
        EasyMock.expect(reg.resolveHandler(this));
        EasyMock.expectLastCall().andReturn(subHandler);
        EasyMock.replay(reg, subReg);
        handler.initNodeHandlerRegistry(reg);
        handler.addSubRegistry(subReg);
        assertEquals("Wrong handler returned", subHandler, handler
                .resolveHandler(this));
        EasyMock.verify(reg, subReg);
    }

    /**
     * Tests resolving a node, for which no handler can be found. This should
     * cause an exception.
     */
    public void testResolveHandlerNoParentUnknown()
    {
        NodeHandlerRegistry subReg = EasyMock
                .createMock(NodeHandlerRegistry.class);
        final Object testNode = 42;
        EasyMock.expect(subReg.lookupHandler(testNode, false)).andReturn(null);
        EasyMock.expect(subReg.lookupHandler(testNode, true)).andReturn(null);
        EasyMock.replay(subReg);
        handler.addSubRegistry(subReg);
        try
        {
            handler.resolveHandler(testNode);
            fail("No exception for unknown node type!");
        }
        catch (ConfigurationRuntimeException crex)
        {
            // ok
        }
    }

    /**
     * Tests setting the node handler registry to null.
     */
    public void testInitNodeHandlerRegistryNull()
    {
        handler.initNodeHandlerRegistry(null);
        assertNull("A parent registry is set", handler.getParentRegistry());
    }

    /**
     * Tests initializing the node handler factory. The handler should register
     * itself as a sub registry.
     */
    public void testInitNodeHandlerRegistry()
    {
        NodeHandlerRegistry registry = EasyMock
                .createMock(NodeHandlerRegistry.class);
        registry.addSubRegistry(handler);
        EasyMock.replay(registry);
        handler.initNodeHandlerRegistry(registry);
        assertEquals("Parent registry not set", registry, handler
                .getParentRegistry());
        EasyMock.verify(registry);
    }

    /**
     * Tests the addAttributeValue() implementation.
     */
    public void testAddAttributeValue()
    {
        subHandler.addAttributeValue(this, SUB_NAME, VALUE);
        EasyMock.replay(subHandler);
        handler.addAttributeValue(this, SUB_NAME, VALUE);
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the addChild() implementation.
     */
    public void testAddChild()
    {
        subHandler.addChild(this, SUB_NAME);
        EasyMock.expectLastCall().andReturn(NODE);
        EasyMock.replay(subHandler);
        assertEquals("Wrong child node", NODE, handler.addChild(this, SUB_NAME));
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the addChild() implementation that sets a value.
     */
    public void testAddChildValue()
    {
        subHandler.addChild(this, SUB_NAME, VALUE);
        EasyMock.expectLastCall().andReturn(NODE);
        EasyMock.replay(subHandler);
        assertEquals("Wrong child node", NODE, handler.addChild(this, SUB_NAME,
                VALUE));
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the getAttributeValue() implementation.
     */
    public void testGetAttributeValue()
    {
        EasyMock.expect(subHandler.getAttributeValue(this, SUB_NAME))
                .andReturn(VALUE);
        EasyMock.replay(subHandler);
        assertEquals("Wrong attribute value", VALUE, handler.getAttributeValue(
                this, SUB_NAME));
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the getAttributes() implementation.
     */
    public void testGetAttributes()
    {
        List<String> attrs = new ArrayList<String>();
        attrs.add(SUB_NAME);
        EasyMock.expect(subHandler.getAttributes(this)).andReturn(attrs);
        EasyMock.replay(subHandler);
        assertSame("Wrong attribute list", attrs, handler.getAttributes(this));
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the getChild() implementation.
     */
    public void testGetChild()
    {
        EasyMock.expect(subHandler.getChild(this, 1)).andReturn(NODE);
        EasyMock.replay(subHandler);
        assertEquals("Wrong child node", NODE, handler.getChild(this, 1));
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the getChildren() implementation.
     */
    public void testGetChildren()
    {
        List<Object> children = new ArrayList<Object>();
        children.add(NODE);
        EasyMock.expect(subHandler.getChildren(this)).andReturn(children);
        EasyMock.replay(subHandler);
        assertSame("Wrong children list", children, handler.getChildren(this));
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the getChildren(String) implementation.
     */
    public void testGetChildrenName()
    {
        List<Object> children = new ArrayList<Object>();
        children.add(NODE);
        EasyMock.expect(subHandler.getChildren(this, SUB_NAME)).andReturn(
                children);
        EasyMock.replay(subHandler);
        assertSame("Wrong children list", children, handler.getChildren(this,
                SUB_NAME));
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the getChildrenCount() implementation.
     */
    public void testGetChildrenCount()
    {
        final int count = 10;
        EasyMock.expect(subHandler.getChildrenCount(this, SUB_NAME)).andReturn(
                count);
        EasyMock.replay(subHandler);
        assertEquals("Wrong number of child nodes", count, handler
                .getChildrenCount(this, SUB_NAME));
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the getParent() implementation.
     */
    public void testGetParent()
    {
        EasyMock.expect(subHandler.getParent(this)).andReturn(NODE);
        EasyMock.replay(subHandler);
        assertEquals("Wrong parent node", NODE, handler.getParent(this));
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the getValue() implementation.
     */
    public void testGetValue()
    {
        EasyMock.expect(subHandler.getValue(this)).andReturn(VALUE);
        EasyMock.replay(subHandler);
        assertEquals("Wrong value", VALUE, handler.getValue(this));
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the hasAttributes() implementation.
     */
    public void testHasAttributes()
    {
        EasyMock.expect(subHandler.hasAttributes(this)).andReturn(Boolean.TRUE);
        EasyMock.replay(subHandler);
        assertTrue("No attributes", handler.hasAttributes(this));
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the isDefined() implementation.
     */
    public void testIsDefined()
    {
        EasyMock.expect(subHandler.isDefined(this)).andReturn(Boolean.TRUE);
        EasyMock.replay(subHandler);
        assertTrue("Not defined", handler.isDefined(this));
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the nodeName() implementation.
     */
    public void testNodeName()
    {
        EasyMock.expect(subHandler.nodeName(this)).andReturn(SUB_NAME);
        EasyMock.replay(subHandler);
        assertEquals("Wrong node name", SUB_NAME, handler.nodeName(this));
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the removeAttribute() implementation.
     */
    public void testRemoveAttribute()
    {
        subHandler.removeAttribute(this, SUB_NAME);
        EasyMock.replay(subHandler);
        handler.removeAttribute(this, SUB_NAME);
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the removeChild() implementation.
     */
    public void testRemoveChild()
    {
        subHandler.removeChild(this, NODE);
        EasyMock.replay(subHandler);
        handler.removeChild(this, NODE);
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the setAttributeValue() implementation.
     */
    public void testSetAttributeValue()
    {
        subHandler.setAttributeValue(this, SUB_NAME, VALUE);
        EasyMock.replay(subHandler);
        handler.setAttributeValue(this, SUB_NAME, VALUE);
        EasyMock.verify(subHandler);
    }

    /**
     * Tests the setValue() implementation.
     */
    public void testSetValue()
    {
        subHandler.setValue(this, VALUE);
        EasyMock.replay(subHandler);
        handler.setValue(this, VALUE);
        EasyMock.verify(subHandler);
    }
}
