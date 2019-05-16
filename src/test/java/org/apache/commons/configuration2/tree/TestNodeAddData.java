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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for NodeAddData.
 *
 */
public class TestNodeAddData
{
    /** Constant for the name of the new node. */
    private static final String TEST_NODENAME = "testNewNode";

    /** Constant for the name of a path node. */
    private static final String PATH_NODE_NAME = "PATHNODE";

    /** A default parent node. */
    private static ImmutableNode parentNode;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        parentNode = new ImmutableNode.Builder().name("testParent").create();
    }

    /**
     * Tests whether the constructor can handle a null collection of path nodes.
     */
    @Test
    public void testPathNodesNull()
    {
        final NodeAddData<ImmutableNode> data =
                new NodeAddData<>(parentNode, TEST_NODENAME,
                        false, null);
        assertTrue("Got path nodes", data.getPathNodes().isEmpty());
    }

    /**
     * Tests whether the collection with path nodes cannot be modified if no
     * data is available.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testPathNodesNullModify()
    {
        final NodeAddData<ImmutableNode> data =
                new NodeAddData<>(parentNode, TEST_NODENAME,
                        false, null);
        data.getPathNodes().add("test");
    }

    /**
     * Tests whether a defensive copy of the collection with path nodes is
     * created.
     */
    @Test
    public void testInitPathNodesDefensiveCopy()
    {
        final List<String> pathNodes = new ArrayList<>();
        pathNodes.add(PATH_NODE_NAME);
        final NodeAddData<ImmutableNode> data =
                new NodeAddData<>(parentNode, TEST_NODENAME,
                        false, pathNodes);
        pathNodes.add("anotherNode");
        assertEquals("Wrong number of path nodes", 1, data.getPathNodes()
                .size());
        assertEquals("Wrong path node", PATH_NODE_NAME, data.getPathNodes()
                .get(0));
    }

    /**
     * Tests that the collection with path nodes cannot be modified if data is
     * available.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testPathNodesDefinedModify()
    {
        final NodeAddData<ImmutableNode> data =
                new NodeAddData<>(parentNode, TEST_NODENAME,
                        false, Collections.singleton(PATH_NODE_NAME));
        data.getPathNodes().add("anotherNode");
    }
}
