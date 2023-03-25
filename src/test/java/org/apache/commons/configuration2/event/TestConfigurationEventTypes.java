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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * A test class which tests whether the types of basic configuration events are correctly defined.
 */
public class TestConfigurationEventTypes {
    /**
     * Helper method for checking the relevant properties of an error event type.
     *
     * @param type the type to be checked
     */
    private void checkErrorEvent(final EventType<ConfigurationErrorEvent> type) {
        assertSame(ConfigurationErrorEvent.ANY, type.getSuperType(), "Wrong super type for " + type);
    }

    /**
     * Helper method for checking the relevant properties of a given event type representing a hierarchical update event.
     *
     * @param eventType the event type to check
     */
    private void checkHierarchicalEvent(final EventType<ConfigurationEvent> eventType) {
        assertSame(ConfigurationEvent.ANY_HIERARCHICAL, eventType.getSuperType(), "Wrong super type for " + eventType);
    }

    /**
     * Helper method for checking the relevant properties of a given event type representing a configuration update event.
     *
     * @param eventType the event type to check
     */
    private void checkUpdateEvent(final EventType<ConfigurationEvent> eventType) {
        assertSame(ConfigurationEvent.ANY, eventType.getSuperType(), "Wrong super type for " + eventType);
    }

    /**
     * Tests the event type for an add nodes operation.
     */
    @Test
    public void testAddNodesEventType() {
        checkHierarchicalEvent(ConfigurationEvent.ADD_NODES);
    }

    /**
     * Tests the event type for adding a property.
     */
    @Test
    public void testAddPropertyEventType() {
        checkUpdateEvent(ConfigurationEvent.ADD_PROPERTY);
    }

    /**
     * Tests the common base event type for error events.
     */
    @Test
    public void testBaseErrorEventType() {
        assertEquals(Event.ANY, ConfigurationErrorEvent.ANY.getSuperType());
    }

    /**
     * Tests the event type for clearing a whole configuration.
     */
    @Test
    public void testClearEventType() {
        checkUpdateEvent(ConfigurationEvent.CLEAR);
    }

    /**
     * Tests the event type for clearing a property.
     */
    @Test
    public void testClearPropertyEventType() {
        checkUpdateEvent(ConfigurationEvent.CLEAR_PROPERTY);
    }

    /**
     * Tests the event type for a clear tree operation.
     */
    @Test
    public void testClearTreeEventType() {
        checkHierarchicalEvent(ConfigurationEvent.CLEAR_TREE);
    }

    /**
     * Tests the base event type for configuration events.
     */
    @Test
    public void testConfigurationEventType() {
        assertSame(Event.ANY, ConfigurationEvent.ANY.getSuperType());
    }

    /**
     * Tests whether the set of super event types for the base type can be obtained.
     */
    @Test
    public void testFetchSuperEventTypesForBaseType() {
        final Set<EventType<?>> superTypes = EventType.fetchSuperEventTypes(Event.ANY);
        assertEquals(Collections.singleton(Event.ANY), superTypes);
    }

    /**
     * Tests whether the set of super event types for null input can be obtained.
     */
    @Test
    public void testFetchSuperEventTypesNull() {
        final Set<EventType<?>> superTypes = EventType.fetchSuperEventTypes(null);
        assertTrue(superTypes.isEmpty());
    }

    /**
     * Tests whether the super event types of a specific type can be retrieved.
     */
    @Test
    public void testFetchSuperEventTypesOfType() {
        final Set<EventType<?>> superTypes = EventType.fetchSuperEventTypes(ConfigurationEvent.ADD_NODES);
        final Set<EventType<? extends Event>> expected = new HashSet<>();
        expected.add(ConfigurationEvent.ADD_NODES);
        expected.add(ConfigurationEvent.ANY_HIERARCHICAL);
        expected.add(ConfigurationEvent.ANY);
        expected.add(Event.ANY);
        assertEquals(expected, superTypes);
    }

    /**
     * Tests the common base event type for hierarchical update events.
     */
    @Test
    public void testHierarchicalEventType() {
        checkUpdateEvent(ConfigurationEvent.ANY_HIERARCHICAL);
    }

    /**
     * Tests isInstanceOf() if the base type is null.
     */
    @Test
    public void testIsInstanceOfBaseNull() {
        assertFalse(EventType.isInstanceOf(ConfigurationEvent.ANY, null));
    }

    /**
     * Tests isInstanceOf() if the derived type is null.
     */
    @Test
    public void testIsInstanceOfDerivedNull() {
        assertFalse(EventType.isInstanceOf(null, Event.ANY));
    }

    /**
     * Tests isInstanceOf() if there is no instanceof relationship.
     */
    @Test
    public void testIsInstanceOfFalse() {
        assertFalse(EventType.isInstanceOf(ConfigurationErrorEvent.READ, ConfigurationEvent.ANY));
    }

    /**
     * Tests isInstanceOf() if the expected result is true.
     */
    @Test
    public void testIsInstanceOfTrue() {
        assertTrue(EventType.isInstanceOf(ConfigurationEvent.ADD_NODES, ConfigurationEvent.ANY_HIERARCHICAL));
        assertTrue(EventType.isInstanceOf(ConfigurationEvent.ADD_NODES, ConfigurationEvent.ANY));
        assertTrue(EventType.isInstanceOf(ConfigurationEvent.ADD_NODES, Event.ANY));
        assertTrue(EventType.isInstanceOf(ConfigurationEvent.ADD_NODES, ConfigurationEvent.ADD_NODES));
    }

    /**
     * Tests the event type indicating a read error.
     */
    @Test
    public void testReadErrorEventType() {
        checkErrorEvent(ConfigurationErrorEvent.READ);
    }

    /**
     * Tests the event type for setting a property.
     */
    @Test
    public void testSetPropertyEventType() {
        checkUpdateEvent(ConfigurationEvent.SET_PROPERTY);
    }

    /**
     * Tests the event type indicating a change on a sub configuration.
     */
    @Test
    public void testSubnodeChangedEventType() {
        checkHierarchicalEvent(ConfigurationEvent.SUBNODE_CHANGED);
    }

    /**
     * Tests the event type indicating a write error.
     */
    @Test
    public void testWriteErrorEventType() {
        checkErrorEvent(ConfigurationErrorEvent.WRITE);
    }
}
