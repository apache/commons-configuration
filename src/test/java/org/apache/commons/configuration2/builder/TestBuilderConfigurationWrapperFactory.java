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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.BuilderConfigurationWrapperFactory.EventSourceSupport;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventListenerTestImpl;
import org.apache.commons.configuration2.event.EventSource;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code BuilderConfigurationWrapperFactory}.
 *
 */
public class TestBuilderConfigurationWrapperFactory {
    /**
     * Returns a mock builder which always returns the specified configuration.
     *
     * @param conf the builder's result configuration
     * @return the mock builder
     */
    private ConfigurationBuilder<BaseHierarchicalConfiguration> createBuilderMock(final BaseHierarchicalConfiguration conf) {
        final ConfigurationBuilder<BaseHierarchicalConfiguration> builder = EasyMock.createMock(ConfigurationBuilder.class);
        assertDoesNotThrow(() -> EasyMock.expect(builder.getConfiguration()).andReturn(conf).anyTimes());
        return builder;
    }

    /**
     * Tests whether the returned configuration correctly wraps the builder.
     */
    @Test
    public void testConfigurationBuilderWrapper() {
        final BaseHierarchicalConfiguration conf = new BaseHierarchicalConfiguration();
        final ConfigurationBuilder<BaseHierarchicalConfiguration> builder = createBuilderMock(conf);
        EasyMock.replay(builder);
        conf.addProperty("test1", "value1");
        conf.addProperty("test2", "42");
        final BuilderConfigurationWrapperFactory factory = new BuilderConfigurationWrapperFactory();
        final HierarchicalConfiguration<?> wrapper = factory.createBuilderConfigurationWrapper(HierarchicalConfiguration.class, builder);
        assertEquals("value1", wrapper.getString("test1"), "Wrong value (1)");
        assertEquals(42, wrapper.getInt("test2"), "Wrong value (2)");
        assertSame(conf.getNodeModel().getNodeHandler().getRootNode(), wrapper.getNodeModel().getNodeHandler().getRootNode(), "Wrong root node");
    }

    /**
     * Tries to create a wrapper without passing a builder.
     */
    @Test
    public void testCreateBuilderConfigurationWrapperNoBuilder() {
        final BuilderConfigurationWrapperFactory factory = new BuilderConfigurationWrapperFactory();
        assertThrows(IllegalArgumentException.class, () -> factory.createBuilderConfigurationWrapper(Configuration.class, null));
    }

    /**
     * Tries to create a wrapper without passing an interface class.
     */
    @Test
    public void testCreateBuilderConfigurationWrapperNoClass() {
        final BuilderConfigurationWrapperFactory factory = new BuilderConfigurationWrapperFactory(EventSourceSupport.BUILDER);
        final ConfigurationBuilder<BaseHierarchicalConfiguration> builder = createBuilderMock(new BaseHierarchicalConfiguration());
        assertThrows(IllegalArgumentException.class, () -> factory.createBuilderConfigurationWrapper(null, builder));
    }

    /**
     * Tests the default event source support level.
     */
    @Test
    public void testDefaultEventSourceSupport() {
        final BuilderConfigurationWrapperFactory factory = new BuilderConfigurationWrapperFactory();
        assertEquals(EventSourceSupport.NONE, factory.getEventSourceSupport(), "Wrong result");
    }

    /**
     * Tests whether EventSource methods can be delegated to the builder.
     */
    @Test
    public void testEventSourceSupportBuilder() throws ConfigurationException {
        final BasicConfigurationBuilder<PropertiesConfiguration> builder = new BasicConfigurationBuilder<>(PropertiesConfiguration.class);
        final EventListener<ConfigurationEvent> l1 = new EventListenerTestImpl(null);
        final EventListener<ConfigurationEvent> l2 = new EventListenerTestImpl(null);
        final BuilderConfigurationWrapperFactory factory = new BuilderConfigurationWrapperFactory(EventSourceSupport.BUILDER);
        final EventSource src = (EventSource) factory.createBuilderConfigurationWrapper(Configuration.class, builder);

        src.addEventListener(ConfigurationEvent.ANY, l1);
        src.addEventListener(ConfigurationEvent.ANY_HIERARCHICAL, l2);
        assertTrue(src.removeEventListener(ConfigurationEvent.ANY_HIERARCHICAL, l2), "Wrong result for existing listener");
        assertFalse(src.removeEventListener(ConfigurationEvent.ANY_HIERARCHICAL, l2), "Wrong result for non-existing listener");
        final PropertiesConfiguration config = builder.getConfiguration();
        final Collection<EventListener<? super ConfigurationEvent>> listeners = config.getEventListeners(ConfigurationEvent.ANY_HIERARCHICAL);
        assertTrue(listeners.contains(l1), "Registered listener not found");
        assertFalse(listeners.contains(l2), "Removed listener still found");
    }

    /**
     * Tests the EventSource support level 'dummy'.
     */
    @Test
    public void testEventSourceSupportDummy() {
        final BaseHierarchicalConfiguration conf = new BaseHierarchicalConfiguration();
        final ConfigurationBuilder<BaseHierarchicalConfiguration> builder = createBuilderMock(conf);
        EasyMock.replay(builder);
        final BuilderConfigurationWrapperFactory factory = new BuilderConfigurationWrapperFactory(EventSourceSupport.DUMMY);
        final EventSource src = (EventSource) factory.createBuilderConfigurationWrapper(HierarchicalConfiguration.class, builder);
        src.addEventListener(ConfigurationEvent.ANY, null);
    }

    /**
     * Tests whether event source support of level builder is possible even for a mock builder.
     */
    @Test
    public void testEventSourceSupportMockBuilder() {
        final BaseHierarchicalConfiguration conf = new BaseHierarchicalConfiguration();
        final ConfigurationBuilder<BaseHierarchicalConfiguration> builder = createBuilderMock(conf);
        final EventListenerTestImpl listener = new EventListenerTestImpl(null);
        builder.addEventListener(ConfigurationEvent.ANY, listener);
        EasyMock.replay(builder);

        final BuilderConfigurationWrapperFactory factory = new BuilderConfigurationWrapperFactory(EventSourceSupport.BUILDER);
        final EventSource src = (EventSource) factory.createBuilderConfigurationWrapper(HierarchicalConfiguration.class, builder);
        src.addEventListener(ConfigurationEvent.ANY, listener);
        EasyMock.verify(builder);
    }

    /**
     * Tests the factory if support for EventSource is disabled.
     */
    @Test
    public void testEventSourceSupportNone() {
        final BaseHierarchicalConfiguration conf = new BaseHierarchicalConfiguration();
        final ConfigurationBuilder<BaseHierarchicalConfiguration> builder = createBuilderMock(conf);
        EasyMock.replay(builder);
        final BuilderConfigurationWrapperFactory factory = new BuilderConfigurationWrapperFactory();
        final HierarchicalConfiguration<?> wrapper = factory.createBuilderConfigurationWrapper(HierarchicalConfiguration.class, builder);
        assertFalse(wrapper instanceof EventSource, "EventSource support");
    }
}
