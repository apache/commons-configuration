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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
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
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code CombinedBuilderParametersImpl}.
 */
public class TestCombinedBuilderParametersImpl {
    /**
     * Creates a mock for a defaults handler.
     *
     * @return the handler mock
     */
    @SuppressWarnings("unchecked")
    private static DefaultParametersHandler<BuilderParameters> createDefaultsHandlerMock() {
        return mock(DefaultParametersHandler.class);
    }

    /**
     * Tests whether cloning works as expected.
     */
    @Test
    public void testClone() {
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        params.setBasePath("some base path");
        final XMLBuilderParametersImpl defParams = new XMLBuilderParametersImpl();
        defParams.setSystemID("someSysID");
        params.setDefinitionBuilderParameters(defParams);
        final CombinedBuilderParametersImpl clone = params.clone();
        assertEquals(params.getBasePath(), clone.getBasePath());
        assertNotSame(params.getDefinitionBuilderParameters(), clone.getDefinitionBuilderParameters());
        assertEquals(params.getDefinitionBuilderParameters().getParameters().get("systemID"),
                clone.getDefinitionBuilderParameters().getParameters().get("systemID"));
    }

    /**
     * Tests whether a new instance can be created if none is found in the parameters map.
     */
    @Test
    public void testFromParametersCreate() {
        final CombinedBuilderParametersImpl params = CombinedBuilderParametersImpl.fromParameters(new HashMap<>(), true);
        assertNotNull(params);
        assertNull(params.getDefinitionBuilder());
    }

    /**
     * Tests whether an instance can be obtained from a parameters map.
     */
    @Test
    public void testFromParametersExisting() {
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        final Map<String, Object> map = params.getParameters();
        assertSame(params, CombinedBuilderParametersImpl.fromParameters(map));
    }

    /**
     * Tests fromParameters() if the map does not contain an instance.
     */
    @Test
    public void testFromParametersNotFound() {
        assertNull(CombinedBuilderParametersImpl.fromParameters(new HashMap<>()));
    }

    /**
     * Tests whether a default parameters manager can be set and queried.
     */
    @Test
    public void testGetChildDefaultParametersManagerSpecific() {
        final DefaultParametersManager manager = mock(DefaultParametersManager.class);
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        assertSame(params, params.setChildDefaultParametersManager(manager));
        assertSame(manager, params.getChildDefaultParametersManager());
    }

    /**
     * Tests whether a default parameters manager is dynamically created if it has not been set.
     */
    @Test
    public void testGetChildDefaultParametersManagerUndefined() {
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        assertNotNull(params.getChildDefaultParametersManager());
    }

    /**
     * Tests that inherited properties are also stored in the parameters map.
     */
    @Test
    public void testGetParametersInherited() {
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        params.setThrowExceptionOnMissing(true);
        final Map<String, Object> map = params.getParameters();
        assertEquals(Boolean.TRUE, map.get("throwExceptionOnMissing"));
    }

    /**
     * Tests whether the map with providers is initially empty.
     */
    @Test
    public void testGetProvidersInitial() {
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        assertEquals(Collections.emptyMap(), params.getProviders());
    }

    /**
     * Tests that the map with providers cannot be modified.
     */
    @Test
    public void testGetProvidersModify() {
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        final Map<String, ConfigurationBuilderProvider> providers = params.getProviders();
        final ConfigurationBuilderProvider provider = mock(ConfigurationBuilderProvider.class);
        assertThrows(UnsupportedOperationException.class, () -> providers.put("tag", provider));
    }

    /**
     * Tests whether properties can be inherited.
     */
    @Test
    public void testInheritFrom() {
        final DefaultParametersManager manager = mock(DefaultParametersManager.class);
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl().setInheritSettings(false).setChildDefaultParametersManager(manager);
        params.setThrowExceptionOnMissing(true);
        final CombinedBuilderParametersImpl params2 = new CombinedBuilderParametersImpl();

        params2.inheritFrom(params.getParameters());
        final Map<String, Object> parameters = params2.getParameters();
        assertEquals(Boolean.TRUE, parameters.get("throwExceptionOnMissing"));
        assertEquals(manager, params2.getChildDefaultParametersManager());
        assertFalse(params2.isInheritSettings());
    }

    /**
     * Tests that inheritFrom() can handle a map which does not contain a parameters object.
     */
    @Test
    public void testInheritFromNoParametersInMap() {
        final BasicBuilderParameters params = new BasicBuilderParameters().setThrowExceptionOnMissing(true);
        final CombinedBuilderParametersImpl params2 = new CombinedBuilderParametersImpl();

        params2.inheritFrom(params.getParameters());
        final Map<String, Object> parameters = params2.getParameters();
        assertEquals(Boolean.TRUE, parameters.get("throwExceptionOnMissing"));
    }

    /**
     * Tests the result for an unknown provider.
     */
    @Test
    public void testProviderForUnknown() {
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        assertNull(params.providerForTag("someTag"));
    }

    /**
     * Tests whether a defaults handler for a child source can be registered.
     */
    @Test
    public void testRegisterChildDefaultsHandler() {
        final DefaultParametersManager manager = mock(DefaultParametersManager.class);
        final DefaultParametersHandler<BuilderParameters> handler = createDefaultsHandlerMock();
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        params.setChildDefaultParametersManager(manager);
        assertSame(params, params.registerChildDefaultsHandler(BuilderParameters.class, handler));

        verify(manager).registerDefaultsHandler(BuilderParameters.class, handler);
    }

    /**
     * Tests whether a defaults handler for a child source with a class restriction can be registered.
     */
    @Test
    public void testRegisterChildDefaultsHandlerWithStartClass() {
        final DefaultParametersManager manager = mock(DefaultParametersManager.class);
        final DefaultParametersHandler<BuilderParameters> handler = createDefaultsHandlerMock();
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        params.setChildDefaultParametersManager(manager);
        assertSame(params, params.registerChildDefaultsHandler(BuilderParameters.class, handler, FileBasedBuilderParameters.class));

        verify(manager).registerDefaultsHandler(BuilderParameters.class, handler, FileBasedBuilderParameters.class);
    }

    /**
     * Tests whether missing providers can be registered.
     */
    @Test
    public void testRegisterMissingProviders() {
        final ConfigurationBuilderProvider provider1 = mock(ConfigurationBuilderProvider.class);
        final ConfigurationBuilderProvider provider2 = mock(ConfigurationBuilderProvider.class);
        final ConfigurationBuilderProvider provider3 = mock(ConfigurationBuilderProvider.class);
        final String tagPrefix = "testTag";
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        params.registerProvider(tagPrefix, provider1);
        final Map<String, ConfigurationBuilderProvider> map = new HashMap<>();
        map.put(tagPrefix, provider2);
        map.put(tagPrefix + 1, provider3);
        assertSame(params, params.registerMissingProviders(map));
        assertEquals(2, params.getProviders().size());
        assertSame(provider1, params.providerForTag(tagPrefix));
        assertSame(provider3, params.providerForTag(tagPrefix + 1));
    }

    /**
     * Tries to register a map with missing providers containing a null entry.
     */
    @Test
    public void testRegisterMissingProvidersNullEntry() {
        final Map<String, ConfigurationBuilderProvider> map = new HashMap<>();
        map.put("tag", null);
        final CombinedBuilderParametersImpl builderParameters = new CombinedBuilderParametersImpl();
        assertThrows(IllegalArgumentException.class, () -> builderParameters.registerMissingProviders(map));
    }

    /**
     * Tries to register a null map with missing providers.
     */
    @Test
    public void testRegisterMissingProvidersNullMap() {
        final Map<String, ConfigurationBuilderProvider> map = null;
        final CombinedBuilderParametersImpl builderParameters = new CombinedBuilderParametersImpl();
        assertThrows(IllegalArgumentException.class, () -> builderParameters.registerMissingProviders(map));
    }

    /**
     * Tests whether missing providers can be copied from a parameters object.
     */
    @Test
    public void testRegisterMissingProvidersParams() {
        final ConfigurationBuilderProvider provider1 = mock(ConfigurationBuilderProvider.class);
        final ConfigurationBuilderProvider provider2 = mock(ConfigurationBuilderProvider.class);
        final ConfigurationBuilderProvider provider3 = mock(ConfigurationBuilderProvider.class);
        final String tagPrefix = "testTag";
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        final CombinedBuilderParametersImpl params2 = new CombinedBuilderParametersImpl();
        params.registerProvider(tagPrefix, provider1);
        params2.registerProvider(tagPrefix, provider2);
        params2.registerProvider(tagPrefix + 1, provider3);
        assertSame(params, params.registerMissingProviders(params2));
        assertEquals(2, params.getProviders().size());
        assertSame(provider1, params.providerForTag(tagPrefix));
        assertSame(provider3, params.providerForTag(tagPrefix + 1));
    }

    /**
     * Tries to copy providers from a null parameters object.
     */
    @Test
    public void testRegisterMissingProvidersParamsNull() {
        final CombinedBuilderParametersImpl builderParameters = new CombinedBuilderParametersImpl();
        assertThrows(IllegalArgumentException.class, () -> builderParameters.registerMissingProviders((CombinedBuilderParametersImpl) null));
    }

    /**
     * Tests whether a new builder provider can be registered.
     */
    @Test
    public void testRegisterProvider() {
        final ConfigurationBuilderProvider provider = mock(ConfigurationBuilderProvider.class);
        final String tagName = "testTag";
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        assertSame(params, params.registerProvider(tagName, provider));
        final Map<String, ConfigurationBuilderProvider> providers = params.getProviders();
        assertEquals(1, providers.size());
        assertSame(provider, providers.get(tagName));
        assertSame(provider, params.providerForTag(tagName));
    }

    /**
     * Tries to register a null provider.
     */
    @Test
    public void testRegisterProviderNoProvider() {
        final CombinedBuilderParametersImpl builderParameters = new CombinedBuilderParametersImpl();
        assertThrows(IllegalArgumentException.class, () -> builderParameters.registerProvider("aTag", null));
    }

    /**
     * Tries to register a provider without a tag name.
     */
    @Test
    public void testRegisterProviderNoTag() {
        final CombinedBuilderParametersImpl builderParameters = new CombinedBuilderParametersImpl();
        final ConfigurationBuilderProvider provider = mock(ConfigurationBuilderProvider.class);
        assertThrows(IllegalArgumentException.class, () -> builderParameters.registerProvider(null, provider));
    }

    /**
     * Tests whether the base path can be set.
     */
    @Test
    public void testSetBasePath() {
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        final String basePath = ConfigurationAssert.OUT_DIR.getAbsolutePath();
        assertSame(params, params.setBasePath(basePath));
        assertEquals(basePath, params.getBasePath());
    }

    /**
     * Tests whether properties can be set using BeanUtils.
     */
    @Test
    public void testSetBeanProperties() throws Exception {
        final BuilderParameters defparams = mock(BuilderParameters.class);
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        BeanHelper.setProperty(params, "basePath", "testPath");
        BeanHelper.setProperty(params, "definitionBuilderParameters", defparams);
        BeanHelper.setProperty(params, "inheritSettings", false);
        assertEquals("testPath", params.getBasePath());
        assertSame(defparams, params.getDefinitionBuilderParameters());
        assertFalse(params.isInheritSettings());
    }

    /**
     * Tests whether the definition builder can be set.
     */
    @Test
    public void testSetDefinitionBuilder() {
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        assertNull(params.getDefinitionBuilder());
        final ConfigurationBuilder<XMLConfiguration> builder = new BasicConfigurationBuilder<>(XMLConfiguration.class);
        assertSame(params, params.setDefinitionBuilder(builder));
        assertSame(builder, params.getDefinitionBuilder());
    }

    /**
     * Tests whether a parameters object for the definition builder can be set.
     */
    @Test
    public void testSetDefinitionBuilderParameters() {
        final BuilderParameters defparams = mock(BuilderParameters.class);
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        assertSame(params, params.setDefinitionBuilderParameters(defparams));
        assertSame(defparams, params.getDefinitionBuilderParameters());
    }

    /**
     * Tests whether the flag that controls settings inheritance can be set.
     */
    @Test
    public void testSetInheritSettings() {
        final CombinedBuilderParametersImpl params = new CombinedBuilderParametersImpl();
        assertTrue(params.isInheritSettings());
        assertSame(params, params.setInheritSettings(false));
        assertFalse(params.isInheritSettings());
    }
}
