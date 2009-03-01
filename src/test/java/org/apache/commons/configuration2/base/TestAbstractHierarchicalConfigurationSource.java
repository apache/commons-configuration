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
package org.apache.commons.configuration2.base;

import java.util.Collection;

import junit.framework.TestCase;

import org.apache.commons.configuration2.expr.ExpressionEngine;
import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.expr.NodeList;
import org.apache.commons.configuration2.expr.def.DefaultExpressionEngine;
import org.easymock.EasyMock;

/**
 * Test class for {@link AbstractHierarchicalConfigurationSource}. This class
 * tests basic functionality provided by
 * {@link AbstractHierarchicalConfigurationSource}. More complex operations are
 * tested by test classes for concrete sub classes.
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class TestAbstractHierarchicalConfigurationSource extends TestCase
{
    /** Constant for the root node. */
    private static final Object ROOT = "rootNode";

    /** Constant for a property key. */
    private static final String KEY = "test.property.key";

    /** A mock object for the node handler. */
    private NodeHandler<Object> handler;

    /** The source to be tested. */
    private AbstractHierarchicalConfigurationSourceTestImpl source;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        @SuppressWarnings("unchecked")
        NodeHandler<Object> nodeHandler = EasyMock
                .createMock(NodeHandler.class);
        handler = nodeHandler;
        source = new AbstractHierarchicalConfigurationSourceTestImpl(handler);
    }

    /**
     * Tests querying the node handler.
     */
    public void testGetNodeHandler()
    {
        assertEquals("Wrong node handler", handler, source.getNodeHandler());
    }

    /**
     * Tests setting another root node. This should not be supported.
     */
    public void testSetRootNode()
    {
        try
        {
            source.setRootNode(this);
            fail("Could set another root node!");
        }
        catch (UnsupportedOperationException uoex)
        {
            // ok
        }
    }

    /**
     * Tests whether a default expression engine can be queried.
     */
    public void testGetExpressionEngineDefault()
    {
        assertTrue("Wrong default expression engine", source
                .getExpressionEngine() instanceof DefaultExpressionEngine);
    }

    /**
     * Tests setting the expression engine to null. This should cause an
     * exception.
     */
    public void testSetExpressionEngineNull()
    {
        try
        {
            source.setExpressionEngine(null);
            fail("Could set expression engine to null!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests the find() method.
     */
    public void testFind()
    {
        ExpressionEngine expr = EasyMock.createMock(ExpressionEngine.class);
        NodeList<Object> list = new NodeList<Object>();
        EasyMock.expect(expr.query(ROOT, KEY, handler)).andReturn(list);
        EasyMock.replay(expr, handler);
        source.setExpressionEngine(expr);
        assertSame("Wrong result of find()", list, source.find(KEY));
        EasyMock.verify(expr, handler);
    }

    /**
     * Tests the visit() method when a null visitor is passed in. This should
     * cause an exception.
     */
    public void testVisitNullVisitor()
    {
        try
        {
            source.visit(ROOT, null);
            fail("Could pass a null visitor!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests setting the value of a property to a null collection. This should
     * cause an exception.
     */
    public void testSetPropertyNullCollection()
    {
        try
        {
            source.setProperty(KEY, (Collection<?>) null);
            fail("Could set property to null collection!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * A concrete implementation of AbstractHierarchicalConfigurationSource.
     */
    private static class AbstractHierarchicalConfigurationSourceTestImpl extends
            AbstractHierarchicalConfigurationSource<Object>
    {
        public AbstractHierarchicalConfigurationSourceTestImpl(
                NodeHandler<Object> handler)
        {
            super(handler);
        }

        public Object getRootNode()
        {
            return ROOT;
        }
    }
}
