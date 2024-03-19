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
package org.apache.commons.configuration2.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventType;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.Test;

/**
 * A test class for {@code BasicConfigurationBuilder} with tests related to events fired by the builder.
 */
public class TestBasicConfigurationBuilderEvents {
    /**
     * Tests whether the base type for builder events is correctly configured.
     */
    @Test
    public void testBuilderEventType() {
        final EventType<ConfigurationBuilderEvent> builderEventType = ConfigurationBuilderEvent.ANY;
        assertEquals(Event.ANY, builderEventType.getSuperType());
    }

    /**
     * Tests whether builder reset events are correctly distributed.
     */
    @Test
    public void testBuilderResetEvent() {
        final BuilderEventListenerImpl listener = new BuilderEventListenerImpl();
        final BasicConfigurationBuilder<PropertiesConfiguration> builder = new BasicConfigurationBuilder<>(PropertiesConfiguration.class);
        builder.addEventListener(ConfigurationBuilderEvent.RESET, listener);

        builder.reset();
        builder.resetResult();
        ConfigurationBuilderEvent event = listener.nextEvent(ConfigurationBuilderEvent.RESET);
        assertSame(builder, event.getSource());
        event = listener.nextEvent(ConfigurationBuilderEvent.RESET);
        assertSame(builder, event.getSource());
        listener.assertNoMoreEvents();
    }

    /**
     * Tests whether the reset builder event type is correctly configured.
     */
    @Test
    public void testBuilderResetEventType() {
        final EventType<ConfigurationBuilderEvent> builderResetType = ConfigurationBuilderEvent.RESET;
        assertEquals(ConfigurationBuilderEvent.ANY, builderResetType.getSuperType());
    }

    /**
     * Tests whether a configuration request event is generated.
     */
    @Test
    public void testConfigurationRequestEvent() throws ConfigurationException {
        final BasicConfigurationBuilder<PropertiesConfiguration> builder = new BasicConfigurationBuilder<>(PropertiesConfiguration.class);
        builder.getConfiguration();
        final BuilderEventListenerImpl listener = new BuilderEventListenerImpl();
        builder.addEventListener(ConfigurationBuilderEvent.ANY, listener);

        builder.getConfiguration();
        final ConfigurationBuilderEvent event = listener.nextEvent(ConfigurationBuilderEvent.CONFIGURATION_REQUEST);
        assertSame(builder, event.getSource());
        listener.assertNoMoreEvents();
    }

    /**
     * Tests whether the configuration request event type is correctly configured.
     */
    @Test
    public void testConfigurationRequestEventType() {
        final EventType<ConfigurationBuilderEvent> eventType = ConfigurationBuilderEvent.CONFIGURATION_REQUEST;
        assertEquals(ConfigurationBuilderEvent.ANY, eventType.getSuperType());
    }

    /**
     * Tests whether an event listener can be removed again.
     */
    @Test
    public void testRemoveEventListener() {
        final BuilderEventListenerImpl listener = new BuilderEventListenerImpl();
        final BasicConfigurationBuilder<PropertiesConfiguration> builder = new BasicConfigurationBuilder<>(PropertiesConfiguration.class);
        builder.addEventListener(ConfigurationBuilderEvent.RESET, listener);

        builder.reset();
        assertTrue(builder.removeEventListener(ConfigurationBuilderEvent.RESET, listener));
        builder.resetResult();
        listener.nextEvent(ConfigurationBuilderEvent.RESET);
        listener.assertNoMoreEvents();
    }

    /**
     * Tests removeEventListener() for a non-existing listener.
     */
    @Test
    public void testRemoveEventListenerNotExisting() {
        final BasicConfigurationBuilder<PropertiesConfiguration> builder = new BasicConfigurationBuilder<>(PropertiesConfiguration.class);
        final BuilderEventListenerImpl listener = new BuilderEventListenerImpl();
        builder.addEventListener(ConfigurationBuilderEvent.RESET, listener);
        assertFalse(builder.removeEventListener(ConfigurationBuilderEvent.CONFIGURATION_REQUEST, listener));
    }

    /**
     * Tests the use case that a listener on the request event triggers a reset of the builder.
     */
    @Test
    public void testResetOnConfigurationRequestEvent() throws ConfigurationException {
        final BasicConfigurationBuilder<PropertiesConfiguration> builder = new BasicConfigurationBuilder<>(PropertiesConfiguration.class);
        final PropertiesConfiguration configuration = builder.getConfiguration();
        final BuilderEventListenerImpl listener = new BuilderEventListenerImpl();
        builder.addEventListener(ConfigurationBuilderEvent.RESET, listener);
        builder.addEventListener(ConfigurationBuilderEvent.CONFIGURATION_REQUEST, event -> builder.resetResult());

        final PropertiesConfiguration configuration2 = builder.getConfiguration();
        assertNotSame(configuration, configuration2);
        listener.nextEvent(ConfigurationBuilderEvent.RESET);
        listener.assertNoMoreEvents();
    }

    /**
     * Tests whether a result created event is correctly generated.
     */
    @Test
    public void testResultCreatedEvent() throws ConfigurationException {
        final BasicConfigurationBuilder<PropertiesConfiguration> builder = new BasicConfigurationBuilder<>(PropertiesConfiguration.class);
        final BuilderEventListenerImpl listener = new BuilderEventListenerImpl();
        builder.addEventListener(ConfigurationBuilderEvent.ANY, listener);

        final PropertiesConfiguration configuration = builder.getConfiguration();
        listener.nextEvent(ConfigurationBuilderEvent.CONFIGURATION_REQUEST);
        final ConfigurationBuilderResultCreatedEvent event = listener.nextEvent(ConfigurationBuilderResultCreatedEvent.RESULT_CREATED);
        assertSame(builder, event.getSource());
        assertSame(configuration, event.getConfiguration());
    }

    /**
     * Tries to create an event about a newly created configuration without a configuration instance.
     */
    @Test
    public void testResultCreatedEventNoConfiguration() {
        final BasicConfigurationBuilder<Configuration> builder = new BasicConfigurationBuilder<>(Configuration.class);
        assertThrows(IllegalArgumentException.class,
                () -> new ConfigurationBuilderResultCreatedEvent(builder, ConfigurationBuilderResultCreatedEvent.RESULT_CREATED, null));
    }

    /**
     * Tests whether the type of a result created event is correctly configured.
     */
    @Test
    public void testResultCreatedEventType() {
        assertEquals(ConfigurationBuilderEvent.ANY, ConfigurationBuilderResultCreatedEvent.RESULT_CREATED.getSuperType());
    }
}
