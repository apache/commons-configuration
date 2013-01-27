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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.configuration.BaseHierarchicalConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationRuntimeException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.builder.BuilderConfigurationWrapperFactory.EventSourceSupport;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.event.EventSource;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test class for {@code BuilderConfigurationWrapperFactory}.
 *
 * @version $Id$
 */
public class TestBuilderConfigurationWrapperFactory
{
    /**
     * Tests the default event source support level.
     */
    @Test
    public void testDefaultEventSourceSupport()
    {
        BuilderConfigurationWrapperFactory factory =
                new BuilderConfigurationWrapperFactory();
        assertEquals("Wrong result", EventSourceSupport.NONE,
                factory.getEventSourceSupport());
    }

    /**
     * Returns a mock builder which always returns the specified configuration.
     *
     * @param conf the builder's result configuration
     * @return the mock builder
     */
    private ConfigurationBuilder<BaseHierarchicalConfiguration> createBuilderMock(
            BaseHierarchicalConfiguration conf)
    {
        @SuppressWarnings("unchecked")
        ConfigurationBuilder<BaseHierarchicalConfiguration> builder =
                EasyMock.createMock(ConfigurationBuilder.class);
        try
        {
            EasyMock.expect(builder.getConfiguration()).andReturn(conf)
                    .anyTimes();
        }
        catch (ConfigurationException e)
        {
            // Cannot happen
            fail("Unexpected exception: " + e);
        }
        return builder;
    }

    /**
     * Tests whether the returned configuration correctly wraps the builder.
     */
    @Test
    public void testConfigurationBuilderWrapper()
    {
        BaseHierarchicalConfiguration conf =
                new BaseHierarchicalConfiguration();
        ConfigurationBuilder<BaseHierarchicalConfiguration> builder =
                createBuilderMock(conf);
        EasyMock.replay(builder);
        conf.addProperty("test1", "value1");
        conf.addProperty("test2", "42");
        BuilderConfigurationWrapperFactory factory =
                new BuilderConfigurationWrapperFactory();
        HierarchicalConfiguration wrapper =
                factory.createBuilderConfigurationWrapper(
                        HierarchicalConfiguration.class, builder);
        assertEquals("Wrong value (1)", "value1", wrapper.getString("test1"));
        assertEquals("Wrong value (2)", 42, wrapper.getInt("test2"));
        assertSame("Wrong root node", conf.getRootNode(), wrapper.getRootNode());
    }

    /**
     * Tests the factory if support for EventSource is disabled.
     */
    @Test
    public void testEventSourceSupportNone()
    {
        BaseHierarchicalConfiguration conf =
                new BaseHierarchicalConfiguration();
        ConfigurationBuilder<BaseHierarchicalConfiguration> builder =
                createBuilderMock(conf);
        EasyMock.replay(builder);
        BuilderConfigurationWrapperFactory factory =
                new BuilderConfigurationWrapperFactory();
        HierarchicalConfiguration wrapper =
                factory.createBuilderConfigurationWrapper(
                        HierarchicalConfiguration.class, builder);
        assertFalse("EventSource support", wrapper instanceof EventSource);
    }

    /**
     * Tests the EventSource support level 'dummy'.
     */
    @Test
    public void testEventSourceSupportDummy()
    {
        BaseHierarchicalConfiguration conf =
                new BaseHierarchicalConfiguration();
        ConfigurationBuilder<BaseHierarchicalConfiguration> builder =
                createBuilderMock(conf);
        EasyMock.replay(builder);
        BuilderConfigurationWrapperFactory factory =
                new BuilderConfigurationWrapperFactory(EventSourceSupport.DUMMY);
        EventSource src =
                (EventSource) factory.createBuilderConfigurationWrapper(
                        HierarchicalConfiguration.class, builder);
        src.addConfigurationListener(null);
    }

    /**
     * Tests EventSource support level 'builder'.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testEventSourceSupportBuilder()
    {
        BaseHierarchicalConfiguration conf =
                new BaseHierarchicalConfiguration();
        ConfigurationBuilder<BaseHierarchicalConfiguration> builder =
                createBuilderMock(conf);
        EasyMock.replay(builder);
        BuilderConfigurationWrapperFactory factory =
                new BuilderConfigurationWrapperFactory(
                        EventSourceSupport.BUILDER);
        EventSource src =
                (EventSource) factory.createBuilderConfigurationWrapper(
                        HierarchicalConfiguration.class, builder);
        src.addConfigurationListener(null);
    }

    /**
     * Tests whether EventSource methods can be delegated to the builder.
     */
    @Test
    public void testEventSourceSupportBuilderOptionalSupported()
    {
        BuilderWithEventSource builder =
                EasyMock.createMock(BuilderWithEventSource.class);
        ConfigurationListener l =
                EasyMock.createMock(ConfigurationListener.class);
        builder.addConfigurationListener(l);
        EasyMock.expect(builder.removeConfigurationListener(l)).andReturn(
                Boolean.TRUE);
        EasyMock.replay(builder, l);
        BuilderConfigurationWrapperFactory factory =
                new BuilderConfigurationWrapperFactory(
                        EventSourceSupport.BUILDER_OPTIONAL);
        EventSource src =
                (EventSource) factory.createBuilderConfigurationWrapper(
                        Configuration.class, builder);
        src.addConfigurationListener(l);
        assertTrue("Wrong result", src.removeConfigurationListener(l));
        EasyMock.verify(builder);
    }

    /**
     * Tests EventSource support level 'builder optional' if the builder does
     * not provide support.
     */
    @Test
    public void testEventSourceSupportBuilderOptionalNotSupported()
    {
        BaseHierarchicalConfiguration conf =
                new BaseHierarchicalConfiguration();
        ConfigurationBuilder<BaseHierarchicalConfiguration> builder =
                createBuilderMock(conf);
        EasyMock.replay(builder);
        BuilderConfigurationWrapperFactory factory =
                new BuilderConfigurationWrapperFactory(
                        EventSourceSupport.BUILDER_OPTIONAL);
        EventSource src =
                (EventSource) factory.createBuilderConfigurationWrapper(
                        HierarchicalConfiguration.class, builder);
        src.addErrorListener(null);
    }

    /**
     * Tries to create a wrapper without passing an interface class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateBuilderConfigurationWrapperNoClass()
    {
        BuilderConfigurationWrapperFactory factory =
                new BuilderConfigurationWrapperFactory(
                        EventSourceSupport.BUILDER_OPTIONAL);
        factory.createBuilderConfigurationWrapper(null,
                EasyMock.createMock(BuilderWithEventSource.class));
    }

    /**
     * Tries to create a wrapper without passing a builder.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateBuilderConfigurationWrapperNoBuilder()
    {
        BuilderConfigurationWrapperFactory factory =
                new BuilderConfigurationWrapperFactory();
        factory.createBuilderConfigurationWrapper(Configuration.class, null);
    }

    /**
     * A combined interface needed for mock generation.
     */
    private static interface BuilderWithEventSource extends
            ConfigurationBuilder<Configuration>, EventSource
    {
    }
}
