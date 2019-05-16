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
package org.apache.commons.configuration2.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * A test class which tests whether the types of basic configuration events are
 * correctly defined.
 *
 */
public class TestConfigurationEventTypes
{
    /**
     * Tests whether the set of super event types for null input can be
     * obtained.
     */
    @Test
    public void testFetchSuperEventTypesNull()
    {
        final Set<EventType<?>> superTypes = EventType.fetchSuperEventTypes(null);
        assertTrue("Got super types", superTypes.isEmpty());
    }

    /**
     * Tests whether the set of super event types for the base type can be
     * obtained.
     */
    @Test
    public void testFetchSuperEventTypesForBaseType()
    {
        final Set<EventType<?>> superTypes =
                EventType.fetchSuperEventTypes(Event.ANY);
        assertEquals("Wrong number of super types", 1, superTypes.size());
        assertTrue("Wrong super types", superTypes.contains(Event.ANY));
    }

    /**
     * Tests whether the super event types of a specific type can be retrieved.
     */
    @Test
    public void testFetchSuperEventTypesOfType()
    {
        final Set<EventType<?>> superTypes =
                EventType.fetchSuperEventTypes(ConfigurationEvent.ADD_NODES);
        final List<EventType<? extends Event>> expected =
                new LinkedList<>();
        expected.add(ConfigurationEvent.ADD_NODES);
        expected.add(ConfigurationEvent.ANY_HIERARCHICAL);
        expected.add(ConfigurationEvent.ANY);
        expected.add(Event.ANY);
        assertEquals("Wrong number of super types", expected.size(),
                superTypes.size());
        assertTrue("Wrong super types: " + superTypes,
                superTypes.containsAll(expected));
    }

    /**
     * Tests isInstanceOf() if the derived type is null.
     */
    @Test
    public void testIsInstanceOfDerivedNull()
    {
        assertFalse("Wrong result", EventType.isInstanceOf(null, Event.ANY));
    }

    /**
     * Tests isInstanceOf() if the base type is null.
     */
    @Test
    public void testIsInstanceOfBaseNull()
    {
        assertFalse("Wrong result",
                EventType.isInstanceOf(ConfigurationEvent.ANY, null));
    }

    /**
     * Tests isInstanceOf() if there is no instanceof relationship.
     */
    @Test
    public void testIsInstanceOfFalse()
    {
        assertFalse("Wrong result", EventType.isInstanceOf(
                ConfigurationErrorEvent.READ, ConfigurationEvent.ANY));
    }

    /**
     * Tests isInstanceOf() if the expected result is true.
     */
    @Test
    public void testIsInstanceOfTrue()
    {
        assertTrue("Wrong result (1)", EventType.isInstanceOf(
                ConfigurationEvent.ADD_NODES,
                ConfigurationEvent.ANY_HIERARCHICAL));
        assertTrue("Wrong result (2)", EventType.isInstanceOf(
                ConfigurationEvent.ADD_NODES, ConfigurationEvent.ANY));
        assertTrue("Wrong result (3)",
                EventType.isInstanceOf(ConfigurationEvent.ADD_NODES, Event.ANY));
        assertTrue("Wrong result (4)", EventType.isInstanceOf(
                ConfigurationEvent.ADD_NODES, ConfigurationEvent.ADD_NODES));
    }

    /**
     * Tests the base event type for configuration events.
     */
    @Test
    public void testConfigurationEventType()
    {
        assertSame("Wrong super type", Event.ANY,
                ConfigurationEvent.ANY.getSuperType());
    }

    /**
     * Helper method for checking the relevant properties of a given event type
     * representing a configuration update event.
     *
     * @param eventType the event type to check
     */
    private void checkUpdateEvent(final EventType<ConfigurationEvent> eventType)
    {
        assertSame("Wrong super type for " + eventType, ConfigurationEvent.ANY,
                eventType.getSuperType());
    }

    /**
     * Tests the event type for adding a property.
     */
    @Test
    public void testAddPropertyEventType()
    {
        checkUpdateEvent(ConfigurationEvent.ADD_PROPERTY);
    }

    /**
     * Tests the event type for setting a property.
     */
    @Test
    public void testSetPropertyEventType()
    {
        checkUpdateEvent(ConfigurationEvent.SET_PROPERTY);
    }

    /**
     * Tests the event type for clearing a property.
     */
    @Test
    public void testClearPropertyEventType()
    {
        checkUpdateEvent(ConfigurationEvent.CLEAR_PROPERTY);
    }

    /**
     * Tests the event type for clearing a whole configuration.
     */
    @Test
    public void testClearEventType()
    {
        checkUpdateEvent(ConfigurationEvent.CLEAR);
    }

    /**
     * Tests the common base event type for hierarchical update events.
     */
    @Test
    public void testHierarchicalEventType()
    {
        checkUpdateEvent(ConfigurationEvent.ANY_HIERARCHICAL);
    }

    /**
     * Helper method for checking the relevant properties of a given event type
     * representing a hierarchical update event.
     *
     * @param eventType the event type to check
     */
    private void checkHierarchicalEvent(final EventType<ConfigurationEvent> eventType)
    {
        assertSame("Wrong super type for " + eventType,
                ConfigurationEvent.ANY_HIERARCHICAL, eventType.getSuperType());
    }

    /**
     * Tests the event type for an add nodes operation.
     */
    @Test
    public void testAddNodesEventType()
    {
        checkHierarchicalEvent(ConfigurationEvent.ADD_NODES);
    }

    /**
     * Tests the event type for a clear tree operation.
     */
    @Test
    public void testClearTreeEventType()
    {
        checkHierarchicalEvent(ConfigurationEvent.CLEAR_TREE);
    }

    /**
     * Tests the event type indicating a change on a sub configuration.
     */
    @Test
    public void testSubnodeChangedEventType()
    {
        checkHierarchicalEvent(ConfigurationEvent.SUBNODE_CHANGED);
    }

    /**
     * Tests the common base event type for error events.
     */
    @Test
    public void testBaseErrorEventType()
    {
        assertEquals("Wrong super type", Event.ANY,
                ConfigurationErrorEvent.ANY.getSuperType());
    }

    /**
     * Helper method for checking the relevant properties of an error event
     * type.
     *
     * @param type the type to be checked
     */
    private void checkErrorEvent(final EventType<ConfigurationErrorEvent> type)
    {
        assertSame("Wrong super type for " + type, ConfigurationErrorEvent.ANY,
                type.getSuperType());
    }

    /**
     * Tests the event type indicating a read error.
     */
    @Test
    public void testReadErrorEventType()
    {
        checkErrorEvent(ConfigurationErrorEvent.READ);
    }

    /**
     * Tests the event type indicating a write error.
     */
    @Test
    public void testWriteErrorEventType()
    {
        checkErrorEvent(ConfigurationErrorEvent.WRITE);
    }
}
