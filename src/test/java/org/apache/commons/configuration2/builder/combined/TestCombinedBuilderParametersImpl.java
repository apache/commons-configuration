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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.configuration2.builder.BasicBuilderParameters;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.ConfigurationBuilder;
import org.apache.commons.configuration2.builder.DefaultParametersHandler;
import org.apache.commons.configuration2.builder.DefaultParametersManager;
import org.apache.commons.configuration2.builder.XMLBuilderParametersImpl;
import org.apache.commons.configuration2.builder.fluent.FileBasedBuilderParameters;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test class for {@code CombinedBuilderParametersImpl}.
 *
 */
public class TestCombinedBuilderParametersImpl
{
    /**
     * Tests fromParameters() if the map does not contain an instance.
     */
    @Test
    public void testFromParametersNotFound()
    {
        assertNull("Got an instance",
                CombinedBuilderParametersImpl
                        .fromParameters(new HashMap<String, Object>()));
    }

    /**
     * Tests whether a new instance can be created if none is found in the
     * parameters map.
     */
    @Test
    public void testFromParametersCreate()
    {
        final CombinedBuilderParametersImpl params =
                CombinedBuilderParametersImpl.fromParameters(
                        new HashMap<String, Object>(), true);
        assertNotNull("No instance", params);
        assertNull("Got data", params.getDefinitionBuilder());
    }

    /**
     * Tests whether an instance can be obtained from a parameters map.
     */
    @Test
    public void testFromParametersExisting()
    {
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        final Map<String, Object> map = params.getParameters();
        assertSame("Wrong result", params,
                CombinedBuilderParametersImpl.fromParameters(map));
    }

    /**
     * Tests that inherited properties are also stored in the parameters map.
     */
    @Test
    public void testGetParametersInherited()
    {
        final CombinedBuilderParametersImpl params =
                new CombinedBuilderParametersImpl();
        params.setThrowExceptionOnMissing(true);
        final Map<String, Object> map = params.getParameters();
        assertEquals("Exception flag not found", Boolean.TRUE,
                map.get("throwExceptionOnMissing"));
    }

    /**
     * Tests whether the flag that controls settings inheritance can be set.
     */
    @Test
    public void testSetInheritSettings()
    {
        final CombinedBuilderParametersImpl params =
                new CombinedBuilderParametersImpl();
        assertTrue("Wrong initial value", params.isInheritSettings());
        assertSame("Wrong result", params, params.setInheritSettings(false));
        assertFalse("Property not set", params.isInheritSettings());
    }

    /**
     * Tests whether the definition builder can be set.
     */
    @Test
    public void testSetDefinitionBuilder()
    {
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        assertNull("Got a definition builder", params.getDefinitionBuilder());
        final ConfigurationBuilder<XMLConfiguration> builder =
                new BasicConfigurationBuilder<>(
                        XMLConfiguration.class);
        assertSame("Wrong result", params, params.setDefinitionBuilder(builder));
        assertSame("Builder was not set", builder,
                params.getDefinitionBuilder());
    }

    /**
     * Tests whether the map with providers is initially empty.
     */
    @Test
    public void testGetProvidersInitial()
    {
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        assertTrue("Got providers", params.getProviders().isEmpty());
    }

    /**
     * Tests whether a new builder provider can be registered.
     */
    @Test
    public void testRegisterProvider()
    {
        final ConfigurationBuilderProvider provider =
                EasyMock.createMock(ConfigurationBuilderProvider.class);
        EasyMock.replay(provider);
        final String tagName = "testTag";
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        assertSame("Wrong result", params,
                params.registerProvider(tagName, provider));
        final Map<String, ConfigurationBuilderProvider> providers =
                params.getProviders();
        assertEquals("Wrong number of providers", 1, providers.size());
        assertSame("Wrong provider (1)", provider, providers.get(tagName));
        assertSame("Wrong provider (2)", provider,
                params.providerForTag(tagName));
    }

    /**
     * Tries to register a provider without a tag name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterProviderNoTag()
    {
        new CombinedBuilderParametersImpl().registerProvider(null,
                EasyMock.createMock(ConfigurationBuilderProvider.class));
    }

    /**
     * Tries to register a null provider.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterProviderNoProvider()
    {
        new CombinedBuilderParametersImpl().registerProvider("aTag", null);
    }

    /**
     * Tests that the map with providers cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetProvidersModify()
    {
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        params.getProviders().put("tag",
                EasyMock.createMock(ConfigurationBuilderProvider.class));
    }

    /**
     * Tests whether missing providers can be registered.
     */
    @Test
    public void testRegisterMissingProviders()
    {
        final ConfigurationBuilderProvider provider1 =
                EasyMock.createMock(ConfigurationBuilderProvider.class);
        final ConfigurationBuilderProvider provider2 =
                EasyMock.createMock(ConfigurationBuilderProvider.class);
        final ConfigurationBuilderProvider provider3 =
                EasyMock.createMock(ConfigurationBuilderProvider.class);
        final String tagPrefix = "testTag";
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        params.registerProvider(tagPrefix, provider1);
        final Map<String, ConfigurationBuilderProvider> map =
                new HashMap<>();
        map.put(tagPrefix, provider2);
        map.put(tagPrefix + 1, provider3);
        assertSame("Wrong result", params, params.registerMissingProviders(map));
        assertEquals("Wrong number of providers", 2, params.getProviders()
                .size());
        assertSame("Wrong provider (1)", provider1,
                params.providerForTag(tagPrefix));
        assertSame("Wrong provider (2)", provider3,
                params.providerForTag(tagPrefix + 1));
    }

    /**
     * Tries to register a null map with missing providers.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterMissingProvidersNullMap()
    {
        final Map<String, ConfigurationBuilderProvider> map = null;
        new CombinedBuilderParametersImpl().registerMissingProviders(map);
    }

    /**
     * Tries to register a map with missing providers containing a null entry.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterMissingProvidersNullEntry()
    {
        final Map<String, ConfigurationBuilderProvider> map =
                new HashMap<>();
        map.put("tag", null);
        new CombinedBuilderParametersImpl().registerMissingProviders(map);
    }

    /**
     * Tests whether missing providers can be copied from a parameters object.
     */
    @Test
    public void testRegisterMissingProvidersParams()
    {
        final ConfigurationBuilderProvider provider1 =
                EasyMock.createMock(ConfigurationBuilderProvider.class);
        final ConfigurationBuilderProvider provider2 =
                EasyMock.createMock(ConfigurationBuilderProvider.class);
        final ConfigurationBuilderProvider provider3 =
                EasyMock.createMock(ConfigurationBuilderProvider.class);
        final String tagPrefix = "testTag";
        final CombinedBuilderParametersImpl params =
                new CombinedBuilderParametersImpl();
        final CombinedBuilderParametersImpl params2 =
                new CombinedBuilderParametersImpl();
        params.registerProvider(tagPrefix, provider1);
        params2.registerProvider(tagPrefix, provider2);
        params2.registerProvider(tagPrefix + 1, provider3);
        assertSame("Wrong result", params,
                params.registerMissingProviders(params2));
        assertEquals("Wrong number of providers", 2, params.getProviders()
                .size());
        assertSame("Wrong provider (1)", provider1,
                params.providerForTag(tagPrefix));
        assertSame("Wrong provider (2)", provider3,
                params.providerForTag(tagPrefix + 1));
    }

    /**
     * Tries to copy providers from a null parameters object.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterMissingProvidersParamsNull()
    {
        new CombinedBuilderParametersImpl()
                .registerMissingProviders((CombinedBuilderParametersImpl) null);
    }

    /**
     * Tests the result for an unknown provider.
     */
    @Test
    public void testProviderForUnknown()
    {
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        assertNull("Got a provider", params.providerForTag("someTag"));
    }

    /**
     * Tests whether the base path can be set.
     */
    @Test
    public void testSetBasePath()
    {
        final CombinedBuilderParametersImpl params =
                new CombinedBuilderParametersImpl();
        final String basePath = ConfigurationAssert.OUT_DIR.getAbsolutePath();
        assertSame("Wrong result", params, params.setBasePath(basePath));
        assertEquals("Wrong base path", basePath, params.getBasePath());
    }

    /**
     * Tests whether a parameters object for the definition builder can be set.
     */
    @Test
    public void testSetDefinitionBuilderParameters()
    {
        final BuilderParameters defparams =
                EasyMock.createMock(BuilderParameters.class);
        EasyMock.replay(defparams);
        final CombinedBuilderParametersImpl params =
                new CombinedBuilderParametersImpl();
        assertSame("Wrong result", params,
                params.setDefinitionBuilderParameters(defparams));
        assertSame("Wrong parameters object", defparams,
                params.getDefinitionBuilderParameters());
    }

    /**
     * Tests whether properties can be set using BeanUtils.
     */
    @Test
    public void testSetBeanProperties() throws Exception
    {
        final BuilderParameters defparams =
                EasyMock.createMock(BuilderParameters.class);
        EasyMock.replay(defparams);
        final CombinedBuilderParametersImpl params =
                new CombinedBuilderParametersImpl();
        BeanHelper.setProperty(params, "basePath", "testPath");
        BeanHelper.setProperty(params, "definitionBuilderParameters",
                defparams);
        BeanHelper.setProperty(params, "inheritSettings", false);
        assertEquals("Wrong path", "testPath", params.getBasePath());
        assertSame("Wrong def parameters", defparams,
                params.getDefinitionBuilderParameters());
        assertFalse("Wrong inherit flag", params.isInheritSettings());
    }

    /**
     * Tests whether cloning works as expected.
     */
    @Test
    public void testClone()
    {
        final CombinedBuilderParametersImpl params =
                new CombinedBuilderParametersImpl();
        params.setBasePath("some base path");
        final XMLBuilderParametersImpl defParams = new XMLBuilderParametersImpl();
        defParams.setSystemID("someSysID");
        params.setDefinitionBuilderParameters(defParams);
        final CombinedBuilderParametersImpl clone = params.clone();
        assertEquals("Wrong field value", params.getBasePath(),
                clone.getBasePath());
        assertNotSame("Parameters object not cloned",
                params.getDefinitionBuilderParameters(),
                clone.getDefinitionBuilderParameters());
        assertEquals(
                "Wrong field value in parameters object",
                params.getDefinitionBuilderParameters().getParameters()
                        .get("systemID"),
                clone.getDefinitionBuilderParameters().getParameters()
                        .get("systemID"));
    }

    /**
     * Tests whether a default parameters manager is dynamically created if it
     * has not been set.
     */
    @Test
    public void testGetChildDefaultParametersManagerUndefined()
    {
        final CombinedBuilderParametersImpl params =
                new CombinedBuilderParametersImpl();
        assertNotNull("No default manager",
                params.getChildDefaultParametersManager());
    }

    /**
     * Tests whether a default parameters manager can be set and queried.
     */
    @Test
    public void testGetChildDefaultParametersManagerSpecific()
    {
        final DefaultParametersManager manager =
                EasyMock.createMock(DefaultParametersManager.class);
        EasyMock.replay(manager);
        final CombinedBuilderParametersImpl params =
                new CombinedBuilderParametersImpl();
        assertSame("Wrong result", params,
                params.setChildDefaultParametersManager(manager));
        assertSame("Wrong manager", manager,
                params.getChildDefaultParametersManager());
    }

    /**
     * Creates a mock for a defaults handler.
     *
     * @return the handler mock
     */
    private static DefaultParametersHandler<BuilderParameters> createDefaultsHandlerMock()
    {
        @SuppressWarnings("unchecked")
        final
        DefaultParametersHandler<BuilderParameters> mock =
                EasyMock.createMock(DefaultParametersHandler.class);
        return mock;
    }

    /**
     * Tests whether a defaults handler for a child source can be registered.
     */
    @Test
    public void testRegisterChildDefaultsHandler()
    {
        final DefaultParametersManager manager =
                EasyMock.createMock(DefaultParametersManager.class);
        final DefaultParametersHandler<BuilderParameters> handler =
                createDefaultsHandlerMock();
        manager.registerDefaultsHandler(BuilderParameters.class, handler);
        EasyMock.replay(manager, handler);
        final CombinedBuilderParametersImpl params =
                new CombinedBuilderParametersImpl();
        params.setChildDefaultParametersManager(manager);
        assertSame("Wrong result", params, params.registerChildDefaultsHandler(
                BuilderParameters.class, handler));
        EasyMock.verify(manager);
    }

    /**
     * Tests whether a defaults handler for a child source with a class
     * restriction can be registered.
     */
    @Test
    public void testRegisterChildDefaultsHandlerWithStartClass()
    {
        final DefaultParametersManager manager =
                EasyMock.createMock(DefaultParametersManager.class);
        final DefaultParametersHandler<BuilderParameters> handler =
                createDefaultsHandlerMock();
        manager.registerDefaultsHandler(BuilderParameters.class, handler,
                FileBasedBuilderParameters.class);
        EasyMock.replay(manager, handler);
        final CombinedBuilderParametersImpl params =
                new CombinedBuilderParametersImpl();
        params.setChildDefaultParametersManager(manager);
        assertSame("Wrong result", params,
                params.registerChildDefaultsHandler(BuilderParameters.class,
                        handler, FileBasedBuilderParameters.class));
        EasyMock.verify(manager);
    }

    /**
     * Tests whether properties can be inherited.
     */
    @Test
    public void testInheritFrom()
    {
        final DefaultParametersManager manager =
                EasyMock.createMock(DefaultParametersManager.class);
        final CombinedBuilderParametersImpl params =
                new CombinedBuilderParametersImpl().setInheritSettings(false)
                        .setChildDefaultParametersManager(manager);
        params.setThrowExceptionOnMissing(true);
        final CombinedBuilderParametersImpl params2 =
                new CombinedBuilderParametersImpl();

        params2.inheritFrom(params.getParameters());
        final Map<String, Object> parameters = params2.getParameters();
        assertEquals("Exception flag not set", Boolean.TRUE,
                parameters.get("throwExceptionOnMissing"));
        assertEquals("Default manager not set", manager,
                params2.getChildDefaultParametersManager());
        assertFalse("Inherit flag not set", params2.isInheritSettings());
    }

    /**
     * Tests that inheritFrom() can handle a map which does not contain a
     * parameters object.
     */
    @Test
    public void testInheritFromNoParametersInMap()
    {
        final BasicBuilderParameters params =
                new BasicBuilderParameters().setThrowExceptionOnMissing(true);
        final CombinedBuilderParametersImpl params2 =
                new CombinedBuilderParametersImpl();

        params2.inheritFrom(params.getParameters());
        final Map<String, Object> parameters = params2.getParameters();
        assertEquals("Exception flag not set", Boolean.TRUE,
                parameters.get("throwExceptionOnMissing"));
    }
}
