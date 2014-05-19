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
package org.apache.commons.configuration.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.event.Event;
import org.apache.commons.configuration.event.EventType;
import org.junit.Test;

/**
 * A test class for {@code BasicConfigurationBuilder} with tests related to
 * events fired by the builder.
 *
 * @version $Id$
 */
public class TestBasicConfigurationBuilderEvents
{
    /**
     * Tests whether the base type for builder events is correctly configured.
     */
    @Test
    public void testBuilderEventType()
    {
        EventType<ConfigurationBuilderEvent> builderEventType =
                ConfigurationBuilderEvent.ANY;
        assertEquals("Wrong super type", Event.ANY,
                builderEventType.getSuperType());
    }

    /**
     * Tests whether the reset builder event type is correctly configured.
     */
    @Test
    public void testBuilderResetEventType()
    {
        EventType<ConfigurationBuilderEvent> builderResetType =
                ConfigurationBuilderEvent.RESET;
        assertEquals("Wrong super type", ConfigurationBuilderEvent.ANY,
                builderResetType.getSuperType());
    }

    /**
     * Tests whether builder reset events are correctly distributed.
     */
    @Test
    public void testBuilderResetEvent()
    {
        BuilderEventListenerImpl listener = new BuilderEventListenerImpl();
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class);
        builder.addEventListener(ConfigurationBuilderEvent.RESET, listener);

        builder.reset();
        builder.resetResult();
        ConfigurationBuilderEvent event =
                listener.nextEvent(ConfigurationBuilderEvent.RESET);
        assertSame("Wrong builder (1)", builder, event.getSource());
        event = listener.nextEvent(ConfigurationBuilderEvent.RESET);
        assertSame("Wrong builder (2)", builder, event.getSource());
        listener.assertNoMoreEvents();
    }

    /**
     * Tests whether an event listener can be removed again.
     */
    @Test
    public void testRemoveEventListener()
    {
        BuilderEventListenerImpl listener = new BuilderEventListenerImpl();
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class);
        builder.addEventListener(ConfigurationBuilderEvent.RESET, listener);

        builder.reset();
        builder.removeEventListener(ConfigurationBuilderEvent.RESET, listener);
        builder.resetResult();
        listener.nextEvent(ConfigurationBuilderEvent.RESET);
        listener.assertNoMoreEvents();
    }
}
