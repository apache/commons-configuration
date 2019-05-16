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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.BasicBuilderParameters;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.ConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.DisabledListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

/**
 * Test class for {@code BaseConfigurationBuilderProvider}.
 *
 */
public class TestBaseConfigurationBuilderProvider
{
    /**
     * Creates a configuration object describing a configuration source.
     *
     * @param reload a flag whether reload operations are supported
     * @return the configuration object
     */
    private HierarchicalConfiguration<?> setUpConfig(final boolean reload)
    {
        final HierarchicalConfiguration<?> config = new BaseHierarchicalConfiguration();
        config.addProperty(CombinedConfigurationBuilder.ATTR_RELOAD,
                Boolean.valueOf(reload));
        config.addProperty("[@throwExceptionOnMissing]", Boolean.TRUE);
        config.addProperty("[@path]",
                ConfigurationAssert.getTestFile("test.properties")
                        .getAbsolutePath());
        config.addProperty("listDelimiterHandler[@config-class]",
                DefaultListDelimiterHandler.class.getName());
        config.addProperty(
                "listDelimiterHandler.config-constrarg[@config-value]", ";");
        return config;
    }

    /**
     * Creates a configuration declaration based on the given configuration.
     *
     * @param declConfig the configuration for the declaration
     * @return the declaration
     */
    private ConfigurationDeclaration createDeclaration(
            final HierarchicalConfiguration<?> declConfig)
    {
        final CombinedConfigurationBuilder parentBuilder =
                new CombinedConfigurationBuilder()
                {
                    @Override
                    protected void initChildBuilderParameters(
                            final BuilderParameters params)
                    {
                        // set a property value; this should be overridden by
                        // child builders
                        if (params instanceof BasicBuilderParameters)
                        {
                            ((BasicBuilderParameters) params)
                                    .setListDelimiterHandler(DisabledListDelimiterHandler.INSTANCE);
                        }
                    }
                };
        final ConfigurationDeclaration decl =
                new ConfigurationDeclaration(parentBuilder, declConfig)
                {
                    @Override
                    protected Object interpolate(final Object value)
                    {
                        return value;
                    }
                };
        return decl;
    }

    /**
     * Creates a default test instance.
     *
     * @return the test instance
     */
    private BaseConfigurationBuilderProvider createProvider()
    {
        return new BaseConfigurationBuilderProvider(
                FileBasedConfigurationBuilder.class.getName(),
                ReloadingFileBasedConfigurationBuilder.class.getName(),
                PropertiesConfiguration.class.getName(),
                Arrays.asList(FileBasedBuilderParametersImpl.class.getName()));
    }

    /**
     * Helper method for setting up a builder and checking properties of the
     * created configuration object.
     *
     * @param reload a flag whether reloading is supported
     * @return the builder created by the provider
     * @throws ConfigurationException if an error occurs
     */
    private ConfigurationBuilder<? extends Configuration> checkBuilder(
            final boolean reload) throws ConfigurationException
    {
        final HierarchicalConfiguration<?> declConfig = setUpConfig(reload);
        final ConfigurationDeclaration decl = createDeclaration(declConfig);
        final ConfigurationBuilder<? extends Configuration> builder =
                createProvider().getConfigurationBuilder(decl);
        final Configuration config = builder.getConfiguration();
        assertEquals("Wrong configuration class",
                PropertiesConfiguration.class, config.getClass());
        final PropertiesConfiguration pconfig = (PropertiesConfiguration) config;
        assertTrue("Wrong exception flag",
                pconfig.isThrowExceptionOnMissing());
        final DefaultListDelimiterHandler listHandler =
                (DefaultListDelimiterHandler) pconfig.getListDelimiterHandler();
        assertEquals("Wrong list delimiter", ';', listHandler.getDelimiter());
        assertTrue("Configuration not loaded",
                pconfig.getBoolean("configuration.loaded"));
        return builder;
    }

    /**
     * Tries to create an instance without a builder class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoBuilderClass()
    {
        new BaseConfigurationBuilderProvider(null, null,
                PropertiesConfiguration.class.getName(), null);
    }

    /**
     * Tries to create an instance without a configuration class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoConfigurationClass()
    {
        new BaseConfigurationBuilderProvider(BasicConfigurationBuilder.class.getName(),
                null, null, null);
    }

    /**
     * Tests whether a builder without reloading support can be created.
     */
    @Test
    public void testGetBuilderNotReloading() throws ConfigurationException
    {
        final ConfigurationBuilder<? extends Configuration> builder =
                checkBuilder(false);
        assertEquals("Wrong builder class",
                FileBasedConfigurationBuilder.class, builder.getClass());
    }

    /**
     * Tests whether a builder with reloading support can be created.
     */
    @Test
    public void testGetBuilderReloading() throws ConfigurationException
    {
        final ConfigurationBuilder<? extends Configuration> builder =
                checkBuilder(true);
        assertEquals("Wrong builder class",
                ReloadingFileBasedConfigurationBuilder.class,
                builder.getClass());
    }

    /**
     * Tries to create a reloading builder if this is not supported by the
     * provider.
     */
    @Test(expected = ConfigurationException.class)
    public void testGetReloadingBuilderNotSupported()
            throws ConfigurationException
    {
        final BaseConfigurationBuilderProvider provider =
                new BaseConfigurationBuilderProvider(
                        FileBasedConfigurationBuilder.class.getName(), null,
                        PropertiesConfiguration.class.getName(),
                        Arrays.asList(FileBasedBuilderParametersImpl.class
                                .getName()));
        final HierarchicalConfiguration<?> declConfig = setUpConfig(true);
        final ConfigurationDeclaration decl = createDeclaration(declConfig);
        provider.getConfigurationBuilder(decl);
    }

    /**
     * Helper method for testing whether the builder's allowFailOnInit flag is
     * set correctly.
     *
     * @param expFlag the expected flag value
     * @param props the properties to set in the configuration for the
     *        declaration
     * @throws ConfigurationException if an error occurs
     */
    private void checkAllowFailOnInit(final boolean expFlag, final String... props)
            throws ConfigurationException
    {
        final HierarchicalConfiguration<?> declConfig = setUpConfig(false);
        for (final String key : props)
        {
            declConfig.addProperty(key, Boolean.TRUE);
        }
        final ConfigurationDeclaration decl = createDeclaration(declConfig);
        final BasicConfigurationBuilder<? extends Configuration> builder =
                (BasicConfigurationBuilder<? extends Configuration>) createProvider()
                        .getConfigurationBuilder(decl);
        assertEquals("Wrong flag value", expFlag, builder.isAllowFailOnInit());
    }

    /**
     * Tests that the allowFailOnInit flag is not set per default on the
     * builder.
     */
    @Test
    public void testGetBuilderNoFailOnInit() throws ConfigurationException
    {
        checkAllowFailOnInit(false);
    }

    /**
     * Tests that the allowFailOnInit flag is not set for builders which are not
     * optional.
     */
    public void testGetBuilderAllowFailOnInitNotOptional()
            throws ConfigurationException
    {
        checkAllowFailOnInit(false,
                CombinedConfigurationBuilder.ATTR_FORCECREATE);
    }

    /**
     * Tests whether the allowFailOnInit flag can be enabled on the builder.
     */
    @Test
    public void testGetBuilderAllowFailOnInit() throws ConfigurationException
    {
        checkAllowFailOnInit(true,
                CombinedConfigurationBuilder.ATTR_OPTIONAL_RES,
                CombinedConfigurationBuilder.ATTR_FORCECREATE);
    }

    /**
     * Tests whether a null collection of parameter classes is handled
     * correctly.
     */
    @Test
    public void testInitNoParameterClasses()
    {
        final BaseConfigurationBuilderProvider provider =
                new BaseConfigurationBuilderProvider(
                        BasicConfigurationBuilder.class.getName(), null,
                        PropertiesConfiguration.class.getName(), null);
        assertTrue("Got parameter classes", provider.getParameterClasses()
                .isEmpty());
    }

    /**
     * Tests that the collection with parameter classes cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetParameterClassesModify()
    {
        final BaseConfigurationBuilderProvider provider =
                new BaseConfigurationBuilderProvider(
                        BasicConfigurationBuilder.class.getName(), null,
                        PropertiesConfiguration.class.getName(),
                        Arrays.asList(BasicBuilderParameters.class.getName()));
        provider.getParameterClasses().clear();
    }
}
