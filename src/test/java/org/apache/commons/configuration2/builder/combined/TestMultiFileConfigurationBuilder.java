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
package org.apache.commons.configuration2.builder.combined;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.configuration2.ConfigurationLookup;
import org.apache.commons.configuration2.DynamicCombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicBuilderParameters;
import org.apache.commons.configuration2.builder.BuilderConfigurationWrapperFactory;
import org.apache.commons.configuration2.builder.BuilderEventListenerImpl;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.ConfigurationBuilderEvent;
import org.apache.commons.configuration2.builder.ConfigurationBuilderResultCreatedEvent;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.XMLBuilderParametersImpl;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventListenerTestImpl;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.DefaultLookups;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.easymock.EasyMock;
import org.junit.Test;
import org.xml.sax.SAXParseException;

/**
 * Test class for {@code MultiFileConfigurationBuilder}.
 *
 */
public class TestMultiFileConfigurationBuilder extends AbstractMultiFileConfigurationBuilderTest
{
    /**
     * Creates a test builder object with default settings.
     *
     * @param managedParams the parameters for managed configurations
     * @return the test instance
     */
    private static MultiFileConfigurationBuilder<XMLConfiguration> createTestBuilder(
            final BuilderParameters managedParams)
    {
        return new MultiFileConfigurationBuilder<>(
                XMLConfiguration.class)
                .configure(createTestBuilderParameters(managedParams));
    }

    /**
     * Creates a test builder instance which allows access to the managed
     * builders created by it. The returned builder instance overrides the
     * method for creating managed builders. It stores newly created builders in
     * the passed in collection.
     *
     * @param managedBuilders a collection in which to store managed builders
     * @return the test builder instance
     */
    private static MultiFileConfigurationBuilder<XMLConfiguration> createBuilderWithAccessToManagedBuilders(
            final Collection<FileBasedConfigurationBuilder<XMLConfiguration>> managedBuilders)
    {
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                new MultiFileConfigurationBuilder<XMLConfiguration>(
                        XMLConfiguration.class)
                {
                    @Override
                    protected FileBasedConfigurationBuilder<XMLConfiguration> createInitializedManagedBuilder(
                            final String fileName,
                            final java.util.Map<String, Object> params)
                            throws ConfigurationException
                    {
                        final FileBasedConfigurationBuilder<XMLConfiguration> result =
                                super.createInitializedManagedBuilder(fileName,
                                        params);
                        managedBuilders.add(result);
                        return result;
                    }
                };
        builder.configure(createTestBuilderParameters(null));
        return builder;
    }

    /**
     * Tests whether access to multiple configurations works.
     */
    @Test
    public void testGetConfiguration() throws ConfigurationException
    {
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createTestBuilder(null);
        final String key = "rowsPerPage";
        switchToConfig(1);
        assertEquals("Wrong property (1)", 15, builder.getConfiguration()
                .getInt(key));
        switchToConfig(2);
        assertEquals("Wrong property (2)", 25, builder.getConfiguration()
                .getInt(key));
        switchToConfig(3);
        assertEquals("Wrong property (3)", 35, builder.getConfiguration()
                .getInt(key));
    }

    /**
     * Tests whether a {@code ConfigurationInterpolator} is created from
     * properties defined in the parameters object if necessary.
     */
    @Test
    public void testInterpolatorFromParameters() throws ConfigurationException
    {
        final BasicBuilderParameters params =
                new MultiFileBuilderParametersImpl().setFilePattern(PATTERN)
                        .setPrefixLookups(
                                Collections.singletonMap(
                                        DefaultLookups.SYSTEM_PROPERTIES
                                                .getPrefix(),
                                        DefaultLookups.SYSTEM_PROPERTIES
                                                .getLookup()));
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                new MultiFileConfigurationBuilder<>(
                        XMLConfiguration.class);
        builder.configure(params);
        switchToConfig(1);
        assertEquals("Wrong property", 15,
                builder.getConfiguration().getInt("rowsPerPage"));
    }

    /**
     * Tests whether a managed configuration is properly initialized.
     */
    @Test
    public void testManagedConfigurationSettings()
            throws ConfigurationException
    {
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                new MultiFileConfigurationBuilder<>(
                        XMLConfiguration.class);
        final ExpressionEngine engine = new XPathExpressionEngine();
        final BuilderParameters xmlParams =
                new XMLBuilderParametersImpl().setExpressionEngine(engine)
                        .setListDelimiterHandler(
                                new DefaultListDelimiterHandler(';'));
        final MultiFileBuilderParametersImpl params =
                new MultiFileBuilderParametersImpl().setFilePattern(PATTERN)
                        .setManagedBuilderParameters(xmlParams);
        final ConfigurationInterpolator ci = createInterpolator();
        params.setInterpolator(ci).setListDelimiterHandler(
                new DefaultListDelimiterHandler('#'));
        builder.configure(params);
        switchToConfig(1);
        final XMLConfiguration config = builder.getConfiguration();
        assertSame("Wrong expression engine", engine,
                config.getExpressionEngine());
        final DefaultListDelimiterHandler listHandler =
                (DefaultListDelimiterHandler) config.getListDelimiterHandler();
        assertEquals("Wrong list delimiter", ';', listHandler.getDelimiter());
        assertNotSame("Interpolator was copied", ci, config.getInterpolator());
    }

    /**
     * Tests whether XML schema validation can be enabled.
     */
    @Test
    public void testSchemaValidationError()
    {
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createTestBuilder(new XMLBuilderParametersImpl().setValidating(
                        true).setSchemaValidation(true));
        switchToConfig("2001");
        try
        {
            builder.getConfiguration();
            fail("No exception thrown");
        }
        catch (final ConfigurationException ex)
        {
            Throwable cause = ex.getCause();
            while (cause != null && !(cause instanceof SAXParseException))
            {
                cause = cause.getCause();
            }
            assertTrue("SAXParseException was not thrown", cause != null);
        }
    }

    /**
     * Tests the behavior if a configuration is accessed which cannot be
     * located.
     */
    @Test(expected = ConfigurationException.class)
    public void testFileNotFound() throws ConfigurationException
    {
        switchToConfig("unknown configuration ID");
        createTestBuilder(null).getConfiguration();
    }

    /**
     * Tests whether exceptions when creating configurations can be suppressed.
     */
    @Test
    public void testFileNotFoundAllowFailOnInit() throws ConfigurationException
    {
        final BasicBuilderParameters params = createTestBuilderParameters(null);
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                new MultiFileConfigurationBuilder<>(
                        XMLConfiguration.class, params.getParameters(), true);
        switchToConfig("unknown configuration ID");
        final XMLConfiguration config = builder.getConfiguration();
        assertTrue("Got content", config.isEmpty());
    }

    /**
     * Tests whether a missing file name pattern causes an exception.
     */
    @Test(expected = ConfigurationException.class)
    public void testNoPattern() throws ConfigurationException
    {
        final BasicBuilderParameters params =
                new MultiFileBuilderParametersImpl()
                        .setInterpolator(createInterpolator());
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                new MultiFileConfigurationBuilder<>(
                        XMLConfiguration.class, params.getParameters(), true);
        switchToConfig(1);
        builder.getConfiguration();
    }

    /**
     * Tests whether configuration listeners are handled correctly.
     */
    @Test
    public void testAddConfigurationListener() throws ConfigurationException
    {
        final EventListener<ConfigurationEvent> l1 = new EventListenerTestImpl(null);
        @SuppressWarnings("unchecked")
        final
        EventListener<Event> l2 =
                EasyMock.createMock(EventListener.class);
        EasyMock.replay(l2);
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createTestBuilder(null);
        builder.addEventListener(ConfigurationEvent.ANY, l1);
        switchToConfig(1);
        final XMLConfiguration config = builder.getConfiguration();
        assertTrue("Listener not added", config.getEventListeners(ConfigurationEvent.ANY)
                .contains(l1));
        builder.addEventListener(Event.ANY, l2);
        assertTrue("Listener 2 not added", config.getEventListeners(Event.ANY)
                .contains(l2));
        assertTrue("Wrong result", builder.removeEventListener(Event.ANY, l2));
        assertFalse("Wrong result after removal",
                builder.removeEventListener(Event.ANY, l2));
        assertFalse("Listener not removed", config.getEventListeners(Event.ANY)
                .contains(l2));
        switchToConfig(2);
        final XMLConfiguration config2 = builder.getConfiguration();
        assertFalse("Listener not globally removed", config2
                .getEventListeners(Event.ANY).contains(l2));
    }

    /**
     * Tests whether managed builders are cached.
     */
    @Test
    public void testCaching() throws ConfigurationException
    {
        final Collection<FileBasedConfigurationBuilder<XMLConfiguration>> managedBuilders =
                new ArrayList<>();
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createBuilderWithAccessToManagedBuilders(managedBuilders);
        switchToConfig(1);
        builder.getConfiguration();
        assertEquals("Wrong number of managed builders (1)", 1,
                managedBuilders.size());
        builder.getConfiguration();
        assertEquals("Wrong number of managed builders (2)", 1,
                managedBuilders.size());
        switchToConfig(2);
        builder.getConfiguration();
        assertEquals("Wrong number of managed builders (3)", 2,
                managedBuilders.size());
    }

    /**
     * Tests whether a reset of the builder configuration also flushes the
     * cache.
     */
    @Test
    public void testCachingWithReset() throws ConfigurationException
    {
        final Collection<FileBasedConfigurationBuilder<XMLConfiguration>> managedBuilders =
                new ArrayList<>();
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createBuilderWithAccessToManagedBuilders(managedBuilders);
        switchToConfig(1);
        builder.getConfiguration();
        builder.resetParameters();
        builder.configure(createTestBuilderParameters(null));
        builder.getConfiguration();
        assertEquals("Wrong number of managed builders", 2,
                managedBuilders.size());
    }

    /**
     * Tests whether the ConfigurationInterpolator is reset, too.
     */
    @Test
    public void testInterpolatorReset()
    {
        final BasicBuilderParameters params =
                new MultiFileBuilderParametersImpl().setFilePattern(PATTERN);
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                new MultiFileConfigurationBuilder<>(
                        XMLConfiguration.class);
        builder.configure(params);
        final ConfigurationInterpolator interpolator = builder.getInterpolator();
        assertNotNull("No interpolator", interpolator);
        builder.resetParameters();
        assertNotSame("No new interpolator", interpolator,
                builder.getInterpolator());
    }

    /**
     * Tests whether builder reset events are handled correctly.
     */
    @Test
    public void testBuilderListenerReset() throws ConfigurationException
    {
        final BuilderEventListenerImpl listener = new BuilderEventListenerImpl();
        final Collection<FileBasedConfigurationBuilder<XMLConfiguration>> managedBuilders =
                new ArrayList<>();
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createBuilderWithAccessToManagedBuilders(managedBuilders);
        switchToConfig(1);
        builder.addEventListener(ConfigurationBuilderEvent.RESET, listener);
        final XMLConfiguration configuration = builder.getConfiguration();
        managedBuilders.iterator().next().resetResult();
        final ConfigurationBuilderEvent event =
                listener.nextEvent(ConfigurationBuilderEvent.RESET);
        assertSame("Wrong event source", builder, event.getSource());
        assertNotSame("Configuration not reset", configuration,
                builder.getConfiguration());
    }

    /**
     * Tests whether listeners at managed builders are removed when the cache is
     * cleared.
     */
    @Test
    public void testRemoveBuilderListenerOnReset()
            throws ConfigurationException
    {
        final BuilderEventListenerImpl listener = new BuilderEventListenerImpl();
        final Collection<FileBasedConfigurationBuilder<XMLConfiguration>> managedBuilders =
                new ArrayList<>();
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createBuilderWithAccessToManagedBuilders(managedBuilders);
        switchToConfig(1);
        builder.addEventListener(ConfigurationBuilderEvent.RESET, listener);
        builder.getConfiguration();
        builder.resetParameters();
        managedBuilders.iterator().next().resetResult();
        listener.assertNoMoreEvents();
    }

    /**
     * Tests whether builder events of other types can be received.
     */
    @Test
    public void testBuilderListenerOtherTypes() throws ConfigurationException
    {
        final BuilderEventListenerImpl listener = new BuilderEventListenerImpl();
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createTestBuilder(null);
        builder.addEventListener(ConfigurationBuilderEvent.ANY, listener);
        switchToConfig(1);
        builder.getConfiguration();
        final ConfigurationBuilderEvent event =
                listener.nextEvent(ConfigurationBuilderEvent.CONFIGURATION_REQUEST);
        assertEquals("Wrong event source of request event", builder,
                event.getSource());
        final ConfigurationBuilderResultCreatedEvent createdEvent =
                listener.nextEvent(ConfigurationBuilderResultCreatedEvent.RESULT_CREATED);
        assertEquals("Wrong source of creation event", builder,
                createdEvent.getSource());
        listener.assertNoMoreEvents();
    }

    /**
     * Tests whether initialization parameters of managed builders are cloned
     * before they are applied.
     */
    @Test
    public void testGetManagedBuilderClonedParameters()
            throws ConfigurationException
    {
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createTestBuilder(new XMLBuilderParametersImpl());
        switchToConfig(1);
        final FileBasedConfigurationBuilder<XMLConfiguration> managedBuilder1 =
                builder.getManagedBuilder();
        switchToConfig(2);
        final FileBasedConfigurationBuilder<XMLConfiguration> managedBuilder2 =
                builder.getManagedBuilder();
        assertNotSame("Managed parameters not cloned",
                managedBuilder1.getFileHandler(),
                managedBuilder2.getFileHandler());
    }

    /**
     * Tests whether infinite loops on constructing the file name using
     * interpolation can be handled. This can happen if a pattern cannot be
     * resolved and the {@code ConfigurationInterpolator} causes again a lookup
     * of the builder's configuration.
     */
    @Test
    public void testRecursiveInterpolation()
    {
        final DynamicCombinedConfiguration config =
                new DynamicCombinedConfiguration();
        config.setKeyPattern(PATTERN_VAR);
        final BasicBuilderParameters params = createTestBuilderParameters(null);
        final ConfigurationInterpolator ci = new ConfigurationInterpolator();
        ci.addDefaultLookup(new ConfigurationLookup(config));
        params.setInterpolator(ci);
        final MultiFileConfigurationBuilder<XMLConfiguration> builder =
                new MultiFileConfigurationBuilder<>(
                        XMLConfiguration.class, null, true);
        builder.configure(params);
        final BuilderConfigurationWrapperFactory wrapFactory =
                new BuilderConfigurationWrapperFactory();
        config.addConfiguration(wrapFactory.createBuilderConfigurationWrapper(
                HierarchicalConfiguration.class, builder), "Multi");
        assertTrue("Got configuration data", config.isEmpty());
    }
}
