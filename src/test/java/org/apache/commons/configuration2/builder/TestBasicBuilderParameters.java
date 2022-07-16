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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.ConfigurationDecoder;
import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.configuration2.convert.ConversionHandler;
import org.apache.commons.configuration2.convert.DefaultConversionHandler;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.InterpolatorSpecification;
import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.apache.commons.configuration2.sync.ReadWriteSynchronizer;
import org.apache.commons.configuration2.sync.Synchronizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code BasicBuilderParameters}.
 *
 */
public class TestBasicBuilderParameters {
    /** The instance to be tested. */
    private BasicBuilderParameters params;

    @BeforeEach
    public void setUp() throws Exception {
        params = new BasicBuilderParameters();
    }

    /**
     * Tests whether the collection with default lookups can be cloned, too.
     */
    @Test
    public void testCloneDefaultLookups() {
        final Lookup look = mock(Lookup.class);
        final Collection<Lookup> looks = Collections.singleton(look);
        params.setDefaultLookups(looks);
        final BasicBuilderParameters clone = params.clone();
        Collection<?> defLooks = (Collection<?>) params.getParameters().get("defaultLookups");
        defLooks.clear();
        defLooks = (Collection<?>) clone.getParameters().get("defaultLookups");
        assertEquals(1, defLooks.size());
        assertTrue(defLooks.contains(look));
    }

    /**
     * Tests whether the map with prefix lookups is cloned, too.
     */
    @Test
    public void testClonePrefixLookups() {
        final Lookup look = mock(Lookup.class);
        final Map<String, Lookup> lookups = Collections.singletonMap("test", look);
        params.setPrefixLookups(lookups);
        final BasicBuilderParameters clone = params.clone();
        Map<?, ?> map = (Map<?, ?>) params.getParameters().get("prefixLookups");
        map.clear();
        map = (Map<?, ?>) clone.getParameters().get("prefixLookups");
        assertEquals(1, map.size());
        assertSame(look, map.get("test"));
    }

    /**
     * Tests whether a cloned instance contains the same data as the original object.
     */
    @Test
    public void testCloneValues() {
        final ConfigurationLogger log = mock(ConfigurationLogger.class);
        final ConfigurationInterpolator ci = mock(ConfigurationInterpolator.class);
        final ListDelimiterHandler handler1 = mock(ListDelimiterHandler.class);
        final ListDelimiterHandler handler2 = mock(ListDelimiterHandler.class);
        params.setListDelimiterHandler(handler1);
        params.setLogger(log);
        params.setInterpolator(ci);
        params.setThrowExceptionOnMissing(true);
        final BasicBuilderParameters clone = params.clone();
        params.setListDelimiterHandler(handler2);
        params.setThrowExceptionOnMissing(false);
        final Map<String, Object> map = clone.getParameters();
        assertSame(log, map.get("logger"));
        assertSame(ci, map.get("interpolator"));
        assertEquals(handler1, map.get("listDelimiterHandler"));
        assertEquals(Boolean.TRUE, map.get("throwExceptionOnMissing"));
    }

    /**
     * Tests the default parameter values.
     */
    @Test
    public void testDefaults() {
        final Map<String, Object> paramMap = params.getParameters();
        assertTrue(paramMap.isEmpty());
    }

    /**
     * Tests fetchBeanHelper() if no helper was set.
     */
    @Test
    public void testFetchBeanHelperNoSet() {
        assertNull(BasicBuilderParameters.fetchBeanHelper(params.getParameters()));
    }

    /**
     * Tries to invoke fetchBeanHelper() on a null map.
     */
    @Test
    public void testFetchBeanHelperNullMap() {
        assertThrows(IllegalArgumentException.class, () -> BasicBuilderParameters.fetchBeanHelper(null));
    }

    /**
     * Tests whether a specification object for interpolation can be obtained.
     */
    @Test
    public void testFetchInterpolatorSpecification() {
        final ConfigurationInterpolator parent = mock(ConfigurationInterpolator.class);
        final Lookup l1 = mock(Lookup.class);
        final Lookup l2 = mock(Lookup.class);
        final Lookup l3 = mock(Lookup.class);
        final Map<String, Lookup> prefixLookups = new HashMap<>();
        prefixLookups.put("p1", l1);
        prefixLookups.put("p2", l2);
        final Collection<Lookup> defLookups = Collections.singleton(l3);
        params.setParentInterpolator(parent);
        params.setPrefixLookups(prefixLookups);
        params.setDefaultLookups(defLookups);
        final Map<String, Object> map = params.getParameters();
        final InterpolatorSpecification spec = BasicBuilderParameters.fetchInterpolatorSpecification(map);
        assertSame(parent, spec.getParentInterpolator());
        assertEquals(prefixLookups, spec.getPrefixLookups());
        assertEquals(1, spec.getDefaultLookups().size());
        assertTrue(spec.getDefaultLookups().contains(l3));
    }

    /**
     * Tests that an empty map does not cause any problems.
     */
    @Test
    public void testFetchInterpolatorSpecificationEmpty() {
        final InterpolatorSpecification spec = BasicBuilderParameters.fetchInterpolatorSpecification(params.getParameters());
        assertNull(spec.getInterpolator());
        assertTrue(spec.getDefaultLookups().isEmpty());
    }

    /**
     * Tests fetchInterpolatorSpecification() if the collection with default lookups contains an invalid value.
     */
    @Test
    public void testFetchInterpolatorSpecificationInvalidCollectionValue() {
        final Map<String, Object> map = new HashMap<>();
        map.put("defaultLookups", Collections.singleton("not a lookup"));
        assertThrows(IllegalArgumentException.class, () -> BasicBuilderParameters.fetchInterpolatorSpecification(map));
    }

    /**
     * Tests fetchInterpolatorSpecification() if the map contains a property of an invalid data type.
     */
    @Test
    public void testFetchInterpolatorSpecificationInvalidDataType() {
        final Map<String, Object> map = new HashMap<>();
        map.put("interpolator", this);
        assertThrows(IllegalArgumentException.class, () -> BasicBuilderParameters.fetchInterpolatorSpecification(map));
    }

    /**
     * Tests fetchInterpolatorSpecification() if the map with prefix lookups contains an invalid key.
     */
    @Test
    public void testFetchInterpolatorSpecificationInvalidMapKey() {
        final Map<String, Object> map = new HashMap<>();
        final Map<Object, Object> prefix = new HashMap<>();
        prefix.put(42, mock(Lookup.class));
        map.put("prefixLookups", prefix);
        assertThrows(IllegalArgumentException.class, () -> BasicBuilderParameters.fetchInterpolatorSpecification(map));
    }

    /**
     * Tests fetchInterpolatorSpecification() if the map with prefix lookups contains an invalid value.
     */
    @Test
    public void testFetchInterpolatorSpecificationInvalidMapValue() {
        final Map<String, Object> map = new HashMap<>();
        final Map<Object, Object> prefix = new HashMap<>();
        prefix.put("test", this);
        map.put("prefixLookups", prefix);
        assertThrows(IllegalArgumentException.class, () -> BasicBuilderParameters.fetchInterpolatorSpecification(map));
    }

    /**
     * Tries to obtain an {@code InterpolatorSpecification} from a null map.
     */
    @Test
    public void testFetchInterpolatorSpecificationNull() {
        assertThrows(IllegalArgumentException.class, () -> BasicBuilderParameters.fetchInterpolatorSpecification(null));
    }

    /**
     * Tests whether an InterpolatorSpecification can be fetched if a ConfigurationInterpolator is present.
     */
    @Test
    public void testFetchInterpolatorSpecificationWithInterpolator() {
        final ConfigurationInterpolator ci = mock(ConfigurationInterpolator.class);
        params.setInterpolator(ci);
        final InterpolatorSpecification spec = BasicBuilderParameters.fetchInterpolatorSpecification(params.getParameters());
        assertSame(ci, spec.getInterpolator());
        assertNull(spec.getParentInterpolator());
    }

    /**
     * Tests whether a defensive copy is created when the parameter map is returned.
     */
    @Test
    public void testGetParametersDefensiveCopy() {
        final Map<String, Object> map1 = params.getParameters();
        final Map<String, Object> mapCopy = new HashMap<>(map1);
        map1.put("otherProperty", "value");
        final Map<String, Object> map2 = params.getParameters();
        assertNotSame(map1, map2);
        assertEquals(mapCopy, map2);
    }

    /**
     * Tests whether properties can be inherited from another parameters map.
     */
    @Test
    public void testInheritFrom() {
        final BeanHelper beanHelper = new BeanHelper();
        final ConfigurationDecoder decoder = mock(ConfigurationDecoder.class);
        final ConversionHandler conversionHandler = new DefaultConversionHandler();
        final ListDelimiterHandler listDelimiterHandler = new DefaultListDelimiterHandler('#');
        final ConfigurationLogger logger = new ConfigurationLogger("test");
        final Synchronizer synchronizer = new ReadWriteSynchronizer();
        params.setBeanHelper(beanHelper).setConfigurationDecoder(decoder).setConversionHandler(conversionHandler).setListDelimiterHandler(listDelimiterHandler)
            .setLogger(logger).setSynchronizer(synchronizer).setThrowExceptionOnMissing(true);
        final BasicBuilderParameters p2 = new BasicBuilderParameters();

        p2.inheritFrom(params.getParameters());
        final Map<String, Object> parameters = p2.getParameters();
        assertEquals(beanHelper, parameters.get("config-BeanHelper"));
        assertEquals(decoder, parameters.get("configurationDecoder"));
        assertEquals(conversionHandler, parameters.get("conversionHandler"));
        assertEquals(listDelimiterHandler, parameters.get("listDelimiterHandler"));
        assertEquals(logger, parameters.get("logger"));
        assertEquals(synchronizer, parameters.get("synchronizer"));
        assertEquals(Boolean.TRUE, parameters.get("throwExceptionOnMissing"));
    }

    /**
     * Tests whether null input is handled by inheritFrom().
     */
    @Test
    public void testInheritFromNull() {
        assertThrows(IllegalArgumentException.class, () -> params.inheritFrom(null));
    }

    /**
     * Tests that undefined properties are not copied over by inheritFrom().
     */
    @Test
    public void testInheritFromUndefinedProperties() {
        final BasicBuilderParameters p2 = new BasicBuilderParameters().setThrowExceptionOnMissing(true);

        p2.inheritFrom(Collections.<String, Object>emptyMap());
        final Map<String, Object> parameters = p2.getParameters();
        assertEquals(Collections.singletonMap("throwExceptionOnMissing", Boolean.TRUE), parameters);
    }

    /**
     * Tests whether properties of other parameter objects can be merged.
     */
    @Test
    public void testMerge() {
        final ListDelimiterHandler handler1 = mock(ListDelimiterHandler.class);
        final ListDelimiterHandler handler2 = mock(ListDelimiterHandler.class);
        final Map<String, Object> props = new HashMap<>();
        props.put("throwExceptionOnMissing", Boolean.TRUE);
        props.put("listDelimiterHandler", handler1);
        props.put("other", "test");
        props.put(BuilderParameters.RESERVED_PARAMETER_PREFIX + "test", "reserved");
        final BuilderParameters p = mock(BuilderParameters.class);

        when(p.getParameters()).thenReturn(props);

        params.setListDelimiterHandler(handler2);
        params.merge(p);
        final Map<String, Object> map = params.getParameters();
        assertEquals(handler2, map.get("listDelimiterHandler"));
        assertEquals(Boolean.TRUE, map.get("throwExceptionOnMissing"));
        assertEquals("test", map.get("other"));
        assertFalse(map.containsKey(BuilderParameters.RESERVED_PARAMETER_PREFIX + "test"));

        verify(p).getParameters();
        verifyNoMoreInteractions(p);
    }

    /**
     * Tries a merge with a null object.
     */
    @Test
    public void testMergeNull() {
        assertThrows(IllegalArgumentException.class, () -> params.merge(null));
    }

    /**
     * Tests whether a BeanHelper can be set.
     */
    @Test
    public void testSetBeanHelper() {
        final BeanHelper helper = new BeanHelper();
        assertSame(params, params.setBeanHelper(helper));
        assertSame(helper, BasicBuilderParameters.fetchBeanHelper(params.getParameters()));
    }

    /**
     * Tests whether a decoder can be set.
     */
    @Test
    public void testSetConfigurationDecoder() {
        final ConfigurationDecoder decoder = mock(ConfigurationDecoder.class);
        assertSame(params, params.setConfigurationDecoder(decoder));
        assertSame(decoder, params.getParameters().get("configurationDecoder"));
    }

    /**
     * Tests whether a ConversionHandler can be set.
     */
    @Test
    public void testSetConversionHandler() {
        final ConversionHandler handler = mock(ConversionHandler.class);
        assertSame(params, params.setConversionHandler(handler));
        assertSame(handler, params.getParameters().get("conversionHandler"));
    }

    /**
     * Tests whether default lookups can be set.
     */
    @Test
    public void testSetDefaultLookups() {
        final Lookup look = mock(Lookup.class);
        final Collection<Lookup> looks = Collections.singleton(look);
        assertSame(params, params.setDefaultLookups(looks));
        final Collection<?> col = (Collection<?>) params.getParameters().get("defaultLookups");
        assertNotSame(col, looks);
        assertEquals(1, col.size());
        assertSame(look, col.iterator().next());
        final Collection<?> col2 = (Collection<?>) params.getParameters().get("defaultLookups");
        assertNotSame(col, col2);
    }

    /**
     * Tests whether null values are handled by setDefaultLookups().
     */
    @Test
    public void testSetDefaultLookupsNull() {
        params.setDefaultLookups(new ArrayList<>());
        params.setDefaultLookups(null);
        assertFalse(params.getParameters().containsKey("defaultLookups"));
    }

    /**
     * Tests whether a {@code ConfigurationInterpolator} can be set.
     */
    @Test
    public void testSetInterpolator() {
        final ConfigurationInterpolator ci = mock(ConfigurationInterpolator.class);
        assertSame(params, params.setInterpolator(ci));
        assertSame(ci, params.getParameters().get("interpolator"));
    }

    /**
     * Tests whether the list delimiter handler property can be set.
     */
    @Test
    public void testSetListDelimiter() {
        final ListDelimiterHandler handler = mock(ListDelimiterHandler.class);
        assertSame(params, params.setListDelimiterHandler(handler));
        assertSame(handler, params.getParameters().get("listDelimiterHandler"));
    }

    /**
     * Tests whether the logger parameter can be set.
     */
    @Test
    public void testSetLogger() {
        final ConfigurationLogger log = mock(ConfigurationLogger.class);
        assertSame(params, params.setLogger(log));
        assertSame(log, params.getParameters().get("logger"));
    }

    /**
     * Tests whether a custom {@code ConfigurationInterpolator} overrides settings for custom lookups.
     */
    @Test
    public void testSetLookupsAndInterpolator() {
        final Lookup look1 = mock(Lookup.class);
        final Lookup look2 = mock(Lookup.class);
        final ConfigurationInterpolator parent = mock(ConfigurationInterpolator.class);
        final ConfigurationInterpolator ci = mock(ConfigurationInterpolator.class);
        params.setDefaultLookups(Collections.singleton(look1));
        params.setPrefixLookups(Collections.singletonMap("test", look2));
        params.setInterpolator(ci);
        params.setParentInterpolator(parent);
        final Map<String, Object> map = params.getParameters();
        assertFalse(map.containsKey("prefixLookups"));
        assertFalse(map.containsKey("defaultLookups"));
        assertFalse(map.containsKey("parentInterpolator"));
    }

    /**
     * Tests whether a parent {@code ConfigurationInterpolator} can be set.
     */
    @Test
    public void testSetParentInterpolator() {
        final ConfigurationInterpolator parent = mock(ConfigurationInterpolator.class);
        assertSame(params, params.setParentInterpolator(parent));
        assertSame(parent, params.getParameters().get("parentInterpolator"));
    }

    /**
     * Tests whether prefix lookups can be set.
     */
    @Test
    public void testSetPrefixLookups() {
        final Lookup look = mock(Lookup.class);
        final Map<String, Lookup> lookups = Collections.singletonMap("test", look);
        assertSame(params, params.setPrefixLookups(lookups));
        final Map<?, ?> map = (Map<?, ?>) params.getParameters().get("prefixLookups");
        assertNotSame(lookups, map);
        assertEquals(Collections.singletonMap("test", look), map);
        final Map<?, ?> map2 = (Map<?, ?>) params.getParameters().get("prefixLookups");
        assertNotSame(map, map2);
    }

    /**
     * Tests whether null values are handled by setPrefixLookups().
     */
    @Test
    public void testSetPrefixLookupsNull() {
        params.setPrefixLookups(new HashMap<>());
        params.setPrefixLookups(null);
        assertFalse(params.getParameters().containsKey("prefixLookups"));
    }

    /**
     * Tests whether a Synchronizer can be set.
     */
    @Test
    public void testSetSynchronizer() {
        final Synchronizer sync = mock(Synchronizer.class);
        assertSame(params, params.setSynchronizer(sync));
        assertSame(sync, params.getParameters().get("synchronizer"));
    }

    /**
     * Tests whether the throw exception on missing property can be set.
     */
    @Test
    public void testSetThrowExceptionOnMissing() {
        assertSame(params, params.setThrowExceptionOnMissing(true));
        assertEquals(Boolean.TRUE, params.getParameters().get("throwExceptionOnMissing"));
    }
}
