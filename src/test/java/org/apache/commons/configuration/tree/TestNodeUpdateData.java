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
package org.apache.commons.configuration.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Test;

/**
 * Test class for {@code NodeUpdateData}.
 *
 * @version $Id$
 */
public class TestNodeUpdateData
{
    /**
     * Tests whether null parameters for collections are converted to empty
     * collections.
     */
    @Test
    public void testInitNoData()
    {
        NodeUpdateData<Object> data =
                new NodeUpdateData<Object>(null, null, null, null);
        assertTrue("Got changed values", data.getChangedValues().isEmpty());
        assertTrue("Got new values", data.getNewValues().isEmpty());
        assertTrue("Got removed nodes", data.getRemovedNodes().isEmpty());
    }

    /**
     * Tests whether a defensive copy is created from the changed values.
     */
    @Test
    public void testInitChangedValuesDefensiveCopy()
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("test", "value");
        NodeUpdateData<Object> data =
                new NodeUpdateData<Object>(map, null, null, null);
        map.put("anotherTest", "anotherValue");
        Map<Object, Object> changedValues = data.getChangedValues();
        assertEquals("Wrong number of changed values", 1, changedValues.size());
        assertEquals("Wrong changed value", "value", changedValues.get("test"));
    }

    /**
     * Tests whether a defensive copy is created from the new values.
     */
    @Test
    public void testInitNewValuesDefensiveCopy()
    {
        Collection<Object> col = new LinkedList<Object>();
        col.add(42);
        NodeUpdateData<Object> data =
                new NodeUpdateData<Object>(null, col, null, null);
        col.add("anotherValue");
        Collection<Object> newValues = data.getNewValues();
        assertEquals("Wrong number of new values", 1, newValues.size());
        assertEquals("Wrong value", 42, newValues.iterator().next());
    }

    /**
     * Tests whether a defensive copy is created from the removed nodes.
     */
    @Test
    public void testInitRemovedNodesDefensiveCopy()
    {
        Collection<Object> col = new LinkedList<Object>();
        col.add("n1");
        NodeUpdateData<Object> data =
                new NodeUpdateData<Object>(null, null, col, null);
        col.add("n2");
        Collection<Object> removedNodes = data.getRemovedNodes();
        assertEquals("Wrong number of new values", 1, removedNodes.size());
        assertEquals("Wrong value", "n1", removedNodes.iterator().next());
    }

    /**
     * Tests that the map with changed values cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetChangedValuesModify()
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("n1", 42);
        NodeUpdateData<Object> data =
                new NodeUpdateData<Object>(map, null, null, null);
        data.getChangedValues().put("n2", 43);
    }

    /**
     * Tests that the collection with new values cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetNewValuesModify()
    {
        Collection<Object> col = new LinkedList<Object>();
        col.add(42);
        NodeUpdateData<Object> data =
                new NodeUpdateData<Object>(null, col, null, null);
        data.getNewValues().add(43);
    }

    /**
     * Tests that the collection with removed nodes cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetRemovedNodesModify()
    {
        Collection<String> col = new LinkedList<String>();
        col.add("n1");
        NodeUpdateData<String> data =
                new NodeUpdateData<String>(null, null, col, null);
        data.getRemovedNodes().add("newNode");
    }
}
