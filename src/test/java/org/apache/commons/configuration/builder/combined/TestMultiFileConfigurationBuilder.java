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
package org.apache.commons.configuration.builder.combined;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.builder.BasicBuilderParameters;
import org.apache.commons.configuration.builder.BuilderListener;
import org.apache.commons.configuration.builder.BuilderParameters;
import org.apache.commons.configuration.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration.builder.XMLBuilderParametersImpl;
import org.apache.commons.configuration.event.ConfigurationErrorListener;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration.tree.ExpressionEngine;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.easymock.EasyMock;
import org.junit.Test;
import org.xml.sax.SAXParseException;

/**
 * Test class for {@code MultiFileConfigurationBuilder}.
 *
 * @version $Id$
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
            BuilderParameters managedParams)
    {
        MultiFileConfigurationBuilder<XMLConfiguration> builder =
                new MultiFileConfigurationBuilder<XMLConfiguration>(
                        XMLConfiguration.class);
        builder.configure(createTestBuilderParameters(managedParams));
        return builder;
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
        MultiFileConfigurationBuilder<XMLConfiguration> builder =
                new MultiFileConfigurationBuilder<XMLConfiguration>(
                        XMLConfiguration.class)
                {
                    @Override
                    protected FileBasedConfigurationBuilder<XMLConfiguration> createInitializedManagedBuilder(
                            String fileName,
                            java.util.Map<String, Object> params)
                            throws ConfigurationException
                    {
                        FileBasedConfigurationBuilder<XMLConfiguration> result =
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
        MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createTestBuilder(null);
        String key = "rowsPerPage";
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
     * Tests whether a managed configuration is properly initialized.
     */
    @Test
    public void testManagedConfigurationSettings()
            throws ConfigurationException
    {
        MultiFileConfigurationBuilder<XMLConfiguration> builder =
                new MultiFileConfigurationBuilder<XMLConfiguration>(
                        XMLConfiguration.class);
        ExpressionEngine engine = new XPathExpressionEngine();
        BuilderParameters xmlParams =
                new XMLBuilderParametersImpl().setExpressionEngine(engine)
                        .setListDelimiter(';');
        MultiFileBuilderParametersImpl params =
                new MultiFileBuilderParametersImpl().setFilePattern(PATTERN)
                        .setManagedBuilderParameters(xmlParams);
        ConfigurationInterpolator ci = createInterpolator();
        params.setInterpolator(ci).setListDelimiter('#');
        builder.configure(params);
        switchToConfig(1);
        XMLConfiguration config = builder.getConfiguration();
        assertSame("Wrong expression engine", engine,
                config.getExpressionEngine());
        assertEquals("Wrong list delimiter", ';', config.getListDelimiter());
        assertNotSame("Interpolator was copied", ci, config.getInterpolator());
    }

    /**
     * Tests whether XML schema validation can be enabled.
     */
    @Test
    public void testSchemaValidationError() throws ConfigurationException
    {
        MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createTestBuilder(new XMLBuilderParametersImpl().setValidating(
                        true).setSchemaValidation(true));
        switchToConfig("2001");
        try
        {
            builder.getConfiguration();
            fail("No exception thrown");
        }
        catch (ConfigurationException ex)
        {
            Throwable cause = ex.getCause();
            while (cause != null && !(cause instanceof SAXParseException))
            {
                cause = cause.getCause();
            }
            assertTrue("SAXParseException was not thrown",
                    cause instanceof SAXParseException);
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
        BasicBuilderParameters params = createTestBuilderParameters(null);
        MultiFileConfigurationBuilder<XMLConfiguration> builder =
                new MultiFileConfigurationBuilder<XMLConfiguration>(
                        XMLConfiguration.class, params.getParameters(), true);
        switchToConfig("unknown configuration ID");
        XMLConfiguration config = builder.getConfiguration();
        assertTrue("Got content", config.isEmpty());
    }

    /**
     * Tests whether a missing file name pattern causes an exception.
     */
    @Test(expected = ConfigurationException.class)
    public void testNoPattern() throws ConfigurationException
    {
        BasicBuilderParameters params =
                new MultiFileBuilderParametersImpl()
                        .setInterpolator(createInterpolator());
        MultiFileConfigurationBuilder<XMLConfiguration> builder =
                new MultiFileConfigurationBuilder<XMLConfiguration>(
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
        ConfigurationListener l1 =
                EasyMock.createMock(ConfigurationListener.class);
        ConfigurationListener l2 =
                EasyMock.createMock(ConfigurationListener.class);
        EasyMock.replay(l1, l2);
        MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createTestBuilder(null);
        assertSame("Wrong result", builder,
                builder.addConfigurationListener(l1));
        switchToConfig(1);
        XMLConfiguration config = builder.getConfiguration();
        assertTrue("Listener not added", config.getConfigurationListeners()
                .contains(l1));
        builder.addConfigurationListener(l2);
        assertTrue("Listener 2 not added", config.getConfigurationListeners()
                .contains(l2));
        builder.removeConfigurationListener(l2);
        assertFalse("Listener not removed", config.getConfigurationListeners()
                .contains(l2));
        switchToConfig(2);
        XMLConfiguration config2 = builder.getConfiguration();
        assertFalse("Listener not globally removed", config2
                .getConfigurationListeners().contains(l2));
    }

    /**
     * Tests whether error listeners are handled correctly.
     */
    @Test
    public void testAddErrorListener() throws ConfigurationException
    {
        ConfigurationErrorListener l1 =
                EasyMock.createMock(ConfigurationErrorListener.class);
        ConfigurationErrorListener l2 =
                EasyMock.createMock(ConfigurationErrorListener.class);
        EasyMock.replay(l1, l2);
        MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createTestBuilder(null);
        assertSame("Wrong result", builder, builder.addErrorListener(l1));
        switchToConfig(1);
        XMLConfiguration config = builder.getConfiguration();
        assertTrue("Listener not added", config.getErrorListeners()
                .contains(l1));
        builder.addErrorListener(l2);
        assertTrue("Listener 2 not added",
                config.getErrorListeners().contains(l2));
        builder.removeErrorListener(l2);
        assertFalse("Listener not removed", config.getErrorListeners()
                .contains(l2));
        switchToConfig(2);
        XMLConfiguration config2 = builder.getConfiguration();
        assertFalse("Listener not globally removed", config2
                .getErrorListeners().contains(l2));
    }

    /**
     * Tests whether managed builders are cached.
     */
    @Test
    public void testCaching() throws ConfigurationException
    {
        Collection<FileBasedConfigurationBuilder<XMLConfiguration>> managedBuilders =
                new ArrayList<FileBasedConfigurationBuilder<XMLConfiguration>>();
        MultiFileConfigurationBuilder<XMLConfiguration> builder =
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
        Collection<FileBasedConfigurationBuilder<XMLConfiguration>> managedBuilders =
                new ArrayList<FileBasedConfigurationBuilder<XMLConfiguration>>();
        MultiFileConfigurationBuilder<XMLConfiguration> builder =
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
     * Tests whether builder listeners are handled correctly.
     */
    @Test
    public void testBuilderListener() throws ConfigurationException
    {
        BuilderListener listener = EasyMock.createMock(BuilderListener.class);
        Collection<FileBasedConfigurationBuilder<XMLConfiguration>> managedBuilders =
                new ArrayList<FileBasedConfigurationBuilder<XMLConfiguration>>();
        MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createBuilderWithAccessToManagedBuilders(managedBuilders);
        listener.builderReset(builder);
        EasyMock.replay(listener);
        switchToConfig(1);
        builder.addBuilderListener(listener);
        builder.getConfiguration();
        managedBuilders.iterator().next().resetResult();
        EasyMock.verify(listener);
    }

    /**
     * Tests whether listeners at managed builders are removed when the cache is
     * cleared.
     */
    @Test
    public void testRemoveBuilderListenerOnReset()
            throws ConfigurationException
    {
        BuilderListener listener = EasyMock.createMock(BuilderListener.class);
        Collection<FileBasedConfigurationBuilder<XMLConfiguration>> managedBuilders =
                new ArrayList<FileBasedConfigurationBuilder<XMLConfiguration>>();
        MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createBuilderWithAccessToManagedBuilders(managedBuilders);
        EasyMock.replay(listener);
        switchToConfig(1);
        builder.addBuilderListener(listener);
        builder.getConfiguration();
        builder.resetParameters();
        managedBuilders.iterator().next().resetResult();
        EasyMock.verify(listener);
    }

    /**
     * Tests whether initialization parameters of managed builders are cloned
     * before they are applied.
     */
    @Test
    public void testGetManagedBuilderClonedParameters()
            throws ConfigurationException
    {
        MultiFileConfigurationBuilder<XMLConfiguration> builder =
                createTestBuilder(new XMLBuilderParametersImpl());
        switchToConfig(1);
        FileBasedConfigurationBuilder<XMLConfiguration> managedBuilder1 =
                builder.getManagedBuilder();
        switchToConfig(2);
        FileBasedConfigurationBuilder<XMLConfiguration> managedBuilder2 =
                builder.getManagedBuilder();
        assertNotSame("Managed parameters not cloned",
                managedBuilder1.getFileHandler(),
                managedBuilder2.getFileHandler());
    }
}
