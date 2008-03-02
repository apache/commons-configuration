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

import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.easymock.EasyMock;

import junit.framework.TestCase;

/**
 * Test class for NodeVisitorAdapter.
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class TestNodeVisitorAdapter extends TestCase
{
    /** The adapter to be tested. */
    private NodeVisitorAdapter<ConfigurationNode> adapter;

    /** Stores the mock node handler. */
    private NodeHandler<ConfigurationNode> handler;

    /** Stores the mock for the configuration node. */
    private ConfigurationNode node;

    @Override
    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception
    {
        super.setUp();
        adapter = new NodeVisitorAdapter<ConfigurationNode>();
        handler = EasyMock.createMock(NodeHandler.class);
    }

    /**
     * Returns a mock configuration node.
     *
     * @return the mock node
     */
    private ConfigurationNode mockNode()
    {
        node = EasyMock.createMock(ConfigurationNode.class);
        return node;
    }

    /**
     * Replays the involved mock objects.
     */
    private void replayMocks()
    {
        EasyMock.replay(handler);
        if (node != null)
        {
            EasyMock.replay(node);
        }
    }

    /**
     * Verifies the involved mock objects.
     */
    private void verifyMocks()
    {
        EasyMock.verify(handler);
        if (node != null)
        {
            EasyMock.verify(node);
        }
    }

    /**
     * Tests the terminate() implementation.
     */
    public void testTerminate()
    {
        assertFalse("Should terminate", adapter.terminate());
    }

    /**
     * Tests the visitBeforeChildren() implementation. We only test whether the
     * passed in objects are not touched.
     */
    public void testVisitBeforeChildren()
    {
        ConfigurationNode nd = mockNode();
        replayMocks();
        adapter.visitBeforeChildren(nd, handler);
        verifyMocks();
    }

    /**
     * Tests the visitAfterChildren() implementation. We only test whether the
     * passed in objects are not touched.
     */
    public void testVisitAfterChildren()
    {
        ConfigurationNode nd = mockNode();
        replayMocks();
        adapter.visitAfterChildren(nd, handler);
        verifyMocks();
    }
}
