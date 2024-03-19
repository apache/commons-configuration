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
package org.apache.commons.configuration2;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.configuration2.convert.ConversionHandler;
import org.apache.commons.configuration2.convert.DefaultConversionHandler;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.DisabledListDelimiterHandler;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventType;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

/**
 * A test class for some of the basic functionality implemented by AbstractConfiguration.
 */
public class TestAbstractConfigurationBasicFeatures {
    /**
     * An event listener implementation that simply collects all received configuration events.
     */
    private static final class CollectingConfigurationListener implements EventListener<ConfigurationEvent> {
        final List<ConfigurationEvent> events = new ArrayList<>();

        @Override
        public void onEvent(final ConfigurationEvent event) {
            events.add(event);
        }
    }

    /**
     * A test configuration implementation. This implementation inherits directly from AbstractConfiguration. For
     * implementing the required functionality another implementation of AbstractConfiguration is used; all methods that
     * need to be implemented delegate to this wrapped configuration.
     */
    static class TestConfigurationImpl extends AbstractConfiguration {
        /** Stores the underlying configuration. */
        private final AbstractConfiguration config;

        public TestConfigurationImpl(final AbstractConfiguration wrappedConfig) {
            config = wrappedConfig;
        }

        @Override
        protected void addPropertyDirect(final String key, final Object value) {
            config.addPropertyDirect(key, value);
        }

        @Override
        protected void clearPropertyDirect(final String key) {
            config.clearPropertyDirect(key);
        }

        @Override
        protected boolean containsKeyInternal(final String key) {
            return config.containsKey(key);
        }

        @Override
        protected Iterator<String> getKeysInternal() {
            return config.getKeys();
        }

        @Override
        protected Object getPropertyInternal(final String key) {
            return config.getProperty(key);
        }

        public AbstractConfiguration getUnderlyingConfiguration() {
            return config;
        }

        @Override
        protected boolean isEmptyInternal() {
            return config.isEmpty();
        }
    }

    /** Constant for text to be used in tests for variable substitution. */
    private static final String SUBST_TXT = "The ${animal} jumps over the ${target}.";

    /** Constant for the prefix of test keys. */
    private static final String KEY_PREFIX = "key";

    /** Constant for the number of properties in tests for copy operations. */
    private static final int PROP_COUNT = 12;

    /**
     * Prepares a test configuration for a test for a list conversion. The configuration is populated with a list property.
     * The returned list contains the expected list values converted to integers.
     *
     * @param config the test configuration
     * @return the list with expected values
     */
    private static List<Integer> prepareListTest(final PropertiesConfiguration config) {
        final List<Integer> expected = new ArrayList<>(PROP_COUNT);
        for (int i = 0; i < PROP_COUNT; i++) {
            config.addProperty(KEY_PREFIX, String.valueOf(i));
            expected.add(i);
        }
        return expected;
    }

    /**
     * Helper method for adding properties with multiple values.
     *
     * @param config the configuration to be used for testing
     */
    private void checkAddListProperty(final AbstractConfiguration config) {
        config.addProperty("test", "value1");
        final Object[] lstValues1 = {"value2", "value3"};
        final Object[] lstValues2 = {"value4", "value5", "value6"};
        config.addProperty("test", lstValues1);
        config.addProperty("test", Arrays.asList(lstValues2));
        final List<Object> lst = config.getList("test");

        final List<Object> expected = new ArrayList<>();
        for (int i = 0; i < lst.size(); i++) {
            expected.add("value" + (i + 1));
        }
        assertEquals(expected, lst);
    }

    /**
     * Tests whether the correct events are received for a copy operation.
     *
     * @param l the event listener
     * @param src the configuration that was copied
     * @param eventType the expected event type
     */
    private void checkCopyEvents(final CollectingConfigurationListener l, final Configuration src, final EventType<?> eventType) {
        final Map<String, ConfigurationEvent> events = new HashMap<>();
        for (final ConfigurationEvent e : l.events) {
            assertEquals(eventType, e.getEventType());
            assertTrue(src.containsKey(e.getPropertyName()), "Unknown property: " + e.getPropertyName());
            if (!e.isBeforeUpdate()) {
                assertTrue(events.containsKey(e.getPropertyName()));
            } else {
                events.put(e.getPropertyName(), e);
            }
        }

        for (final Iterator<String> it = src.getKeys(); it.hasNext();) {
            final String key = it.next();
            assertTrue(events.containsKey(key), "No event received for key " + key);
        }
    }

    /**
     * Helper method for checking getList() if the property value is a scalar.
     *
     * @param value the value of the property
     */
    private void checkGetListScalar(final Object value) {
        final BaseConfiguration config = new BaseConfiguration();
        config.addProperty(KEY_PREFIX, value);
        final List<Object> lst = config.getList(KEY_PREFIX);
        assertEquals(Arrays.asList(value.toString()), lst);
    }

    /**
     * Helper method for checking getStringArray() if the property value is a scalar.
     *
     * @param value the value of the property
     */
    private void checkGetStringArrayScalar(final Object value) {
        final BaseConfiguration config = new BaseConfiguration();
        config.addProperty(KEY_PREFIX, value);
        final String[] array = config.getStringArray(KEY_PREFIX);
        assertArrayEquals(new String[] {value.toString()}, array);
    }

    /**
     * Tests the values of list properties after a copy operation.
     *
     * @param config the configuration to test
     */
    private void checkListProperties(final Configuration config) {
        List<Object> values = config.getList("list1");
        assertEquals(3, values.size());
        values = config.getList("list2");
        assertEquals(Arrays.asList("3,1415", "9,81"), values);
    }

    /**
     * Creates the destination configuration for testing the copy() and append() methods. This configuration contains keys
     * with a running index and corresponding values starting with the prefix "value".
     *
     * @return the destination configuration for copy operations
     */
    private AbstractConfiguration setUpDestConfig() {
        final AbstractConfiguration config = new TestConfigurationImpl(new PropertiesConfiguration());
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        for (int i = 0; i < PROP_COUNT; i++) {
            config.addProperty(KEY_PREFIX + i, "value" + i);
        }
        return config;
    }

    /**
     * Creates the source configuration for testing the copy() and append() methods. This configuration contains keys with
     * an odd index and values starting with the prefix "src". There are also some list properties.
     *
     * @return the source configuration for copy operations
     */
    private Configuration setUpSourceConfig() {
        final BaseConfiguration config = new BaseConfiguration();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        for (int i = 1; i < PROP_COUNT; i += 2) {
            config.addProperty(KEY_PREFIX + i, "src" + i);
        }
        config.addProperty("list1", "1,2,3");
        config.addProperty("list2", "3\\,1415,9\\,81");
        return config;
    }

    /**
     * Tests adding list properties. The single elements of the list should be added.
     */
    @Test
    public void testAddPropertyList() {
        checkAddListProperty(new TestConfigurationImpl(new PropertiesConfiguration()));
    }

    /**
     * Tests adding list properties if delimiter parsing is disabled.
     */
    @Test
    public void testAddPropertyListNoDelimiterParsing() {
        final AbstractConfiguration config = new TestConfigurationImpl(new PropertiesConfiguration());
        checkAddListProperty(config);
    }

    /**
     * Tests the append() method.
     */
    @Test
    public void testAppend() {
        final AbstractConfiguration config = setUpDestConfig();
        final Configuration srcConfig = setUpSourceConfig();
        config.append(srcConfig);
        for (int i = 0; i < PROP_COUNT; i++) {
            final String key = KEY_PREFIX + i;
            if (srcConfig.containsKey(key)) {
                final List<Object> values = config.getList(key);
                assertEquals(Arrays.asList("value" + i, "src" + i), values, "Wrong values for " + key);
            } else {
                assertEquals("value" + i, config.getProperty(key), "Value modified: " + key);
            }
        }
    }

    /**
     * Tests whether the list delimiter is correctly handled if a configuration is appended.
     */
    @Test
    public void testAppendDelimiterHandling() {
        final BaseConfiguration srcConfig = new BaseConfiguration();
        final BaseConfiguration dstConfig = new BaseConfiguration();
        dstConfig.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        srcConfig.setProperty(KEY_PREFIX, "C:\\Temp\\,D:\\Data");
        dstConfig.append(srcConfig);
        assertEquals(srcConfig.getString(KEY_PREFIX), dstConfig.getString(KEY_PREFIX));
    }

    /**
     * Tests the events generated by an append() operation.
     */
    @Test
    public void testAppendEvents() {
        final AbstractConfiguration config = setUpDestConfig();
        final Configuration srcConfig = setUpSourceConfig();
        final CollectingConfigurationListener l = new CollectingConfigurationListener();
        config.addEventListener(ConfigurationEvent.ANY, l);
        config.append(srcConfig);
        checkCopyEvents(l, srcConfig, ConfigurationEvent.ADD_PROPERTY);
    }

    /**
     * Tests appending a null configuration. This should be a noop.
     */
    @Test
    public void testAppendNull() {
        final AbstractConfiguration config = setUpDestConfig();
        config.append(null);
        ConfigurationAssert.assertConfigurationEquals(setUpDestConfig(), config);
    }

    /**
     * Tests the append() method when properties with multiple values and escaped list delimiters are involved.
     */
    @Test
    public void testAppendWithLists() {
        final AbstractConfiguration config = setUpDestConfig();
        config.append(setUpSourceConfig());
        checkListProperties(config);
    }

    /**
     * Tests the clear() implementation of AbstractConfiguration if the iterator returned by getKeys() does not support the
     * remove() operation.
     */
    @Test
    public void testClearIteratorNoRemove() {
        final AbstractConfiguration config = new TestConfigurationImpl(new BaseConfiguration()) {
            // return an iterator that does not support remove operations
            @Override
            protected Iterator<String> getKeysInternal() {
                final Collection<String> keyCol = new ArrayList<>();
                ConfigurationAssert.appendKeys(getUnderlyingConfiguration(), keyCol);
                final String[] keys = keyCol.toArray(new String[keyCol.size()]);
                return Arrays.asList(keys).iterator();
            }
        };
        for (int i = 0; i < 20; i++) {
            config.addProperty("key" + i, "value" + i);
        }
        config.clear();
        assertTrue(config.isEmpty());
    }

    /**
     * Tests the copy() method.
     */
    @Test
    public void testCopy() {
        final AbstractConfiguration config = setUpDestConfig();
        final Configuration srcConfig = setUpSourceConfig();
        config.copy(srcConfig);
        for (int i = 0; i < PROP_COUNT; i++) {
            final String key = KEY_PREFIX + i;
            if (srcConfig.containsKey(key)) {
                assertEquals(srcConfig.getProperty(key), config.getProperty(key), "Value not replaced: " + key);
            } else {
                assertEquals("value" + i, config.getProperty(key), "Value modified: " + key);
            }
        }
    }

    /**
     * Tests whether list delimiters are correctly handled when copying a configuration.
     */
    @Test
    public void testCopyDelimiterHandling() {
        final BaseConfiguration srcConfig = new BaseConfiguration();
        final BaseConfiguration dstConfig = new BaseConfiguration();
        dstConfig.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        srcConfig.setProperty(KEY_PREFIX, "C:\\Temp\\,D:\\Data");
        dstConfig.copy(srcConfig);
        assertEquals(srcConfig.getString(KEY_PREFIX), dstConfig.getString(KEY_PREFIX));
    }

    /**
     * Tests the events generated by a copy() operation.
     */
    @Test
    public void testCopyEvents() {
        final AbstractConfiguration config = setUpDestConfig();
        final Configuration srcConfig = setUpSourceConfig();
        final CollectingConfigurationListener l = new CollectingConfigurationListener();
        config.addEventListener(ConfigurationEvent.ANY, l);
        config.copy(srcConfig);
        checkCopyEvents(l, srcConfig, ConfigurationEvent.SET_PROPERTY);
    }

    /**
     * Tests copying a null configuration. This should be a noop.
     */
    @Test
    public void testCopyNull() {
        final AbstractConfiguration config = setUpDestConfig();
        config.copy(null);
        ConfigurationAssert.assertConfigurationEquals(setUpDestConfig(), config);
    }

    /**
     * Tests the copy() method if properties with multiple values and escaped list delimiters are involved.
     */
    @Test
    public void testCopyWithLists() {
        final Configuration srcConfig = setUpSourceConfig();
        final AbstractConfiguration config = setUpDestConfig();
        config.copy(srcConfig);
        checkListProperties(config);
    }

    /**
     * Tests an interpolation that leads to a cycle. This should throw an exception.
     */
    @Test
    public void testCyclicInterpolation() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "${animal_attr} ${species}");
        config.addProperty("animal_attr", "quick brown");
        config.addProperty("species", "${animal}");
        config.addProperty(KEY_PREFIX, "This is a ${animal}");
        assertThrows(IllegalStateException.class, () -> config.getString(KEY_PREFIX));
    }

    /**
     * Tests whether a configuration instance has a default conversion hander.
     */
    @Test
    public void testDefaultConversionHandler() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        assertEquals(DefaultConversionHandler.class, config.getConversionHandler().getClass());
    }

    /**
     * Tests that the default conversion handler is shared between all configuration instances.
     */
    @Test
    public void testDefaultConversionHandlerSharedInstance() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final PropertiesConfiguration config2 = new PropertiesConfiguration();
        assertSame(config.getConversionHandler(), config2.getConversionHandler());
    }

    /**
     * Tests the default list delimiter hander.
     */
    @Test
    public void testDefaultListDelimiterHandler() {
        final BaseConfiguration config = new BaseConfiguration();
        assertInstanceOf(DisabledListDelimiterHandler.class, config.getListDelimiterHandler());
    }

    /**
     * Tests the generic get() method.
     */
    @Test
    public void testGet() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final Integer value = 20130816;
        config.addProperty(KEY_PREFIX, value.toString());
        assertEquals(value, config.get(Integer.class, KEY_PREFIX));
    }

    /**
     * Tests whether conversion to an array is possible.
     */
    @Test
    public void testGetArray() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final Integer[] expected = new Integer[PROP_COUNT];
        for (int i = 0; i < PROP_COUNT; i++) {
            config.addProperty(KEY_PREFIX, String.valueOf(i));
            expected[i] = Integer.valueOf(i);
        }
        final Integer[] result = config.get(Integer[].class, KEY_PREFIX);
        assertArrayEquals(expected, result);
    }

    /**
     * Tests getArray() if the default value is not an array.
     */
    @Test
    public void testGetArrayDefaultValueNotAnArray() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        assertThrows(IllegalArgumentException.class, () -> config.getArray(Integer.class, KEY_PREFIX, this));
    }

    /**
     * Tests getArray() if the default value is an array with a different component type.
     */
    @Test
    public void testGetArrayDefaultValueWrongComponentClass() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        assertThrows(IllegalArgumentException.class, () -> config.getArray(Integer.class, KEY_PREFIX, new short[1]));
    }

    /**
     * Tests a conversion to an array of primitive types.
     */
    @Test
    public void testGetArrayPrimitive() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final short[] expected = new short[PROP_COUNT];
        for (int i = 0; i < PROP_COUNT; i++) {
            config.addProperty(KEY_PREFIX, String.valueOf(i));
            expected[i] = (short) i;
        }
        final short[] result = config.get(short[].class, KEY_PREFIX, ArrayUtils.EMPTY_SHORT_ARRAY);
        assertArrayEquals(expected, result);
    }

    /**
     * Tests get() for an unknown array property if no default value is provided.
     */
    @Test
    public void testGetArrayUnknownNoDefault() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        assertNull(config.get(Integer[].class, KEY_PREFIX));
    }

    /**
     * Tests get() for an unknown array property if a default value is provided.
     */
    @Test
    public void testGetArrayUnknownWithDefault() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final int[] defValue = {1, 2, 3};
        assertArrayEquals(defValue, config.get(int[].class, KEY_PREFIX, defValue));
    }

    /**
     * Tests a conversion to a collection.
     */
    @Test
    public void testGetCollection() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final List<Integer> expected = prepareListTest(config);
        final List<Integer> result = new ArrayList<>(PROP_COUNT);
        assertSame(result, config.getCollection(Integer.class, KEY_PREFIX, result));
        assertEquals(expected, result);
    }

    /**
     * Tests getCollection() if no target collection is provided.
     */
    @Test
    public void testGetCollectionNullTarget() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final List<Integer> expected = prepareListTest(config);
        final Collection<Integer> result = config.getCollection(Integer.class, KEY_PREFIX, null, new ArrayList<>());
        assertEquals(expected, result);
    }

    /**
     * Tests whether a single value property can be converted to a collection.
     */
    @Test
    public void testGetCollectionSingleValue() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty(KEY_PREFIX, "1");
        final List<Integer> result = new ArrayList<>(1);
        config.getCollection(Integer.class, KEY_PREFIX, result);
        assertEquals(Arrays.asList(1), result);
    }

    /**
     * Tests getCollection() for an unknown property if no default value is provided.
     */
    @Test
    public void testGetCollectionUnknownNoDefault() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final List<Integer> result = new ArrayList<>();
        assertNull(config.getCollection(Integer.class, KEY_PREFIX, result));
        assertTrue(result.isEmpty());
    }

    /**
     * Tests getCollection() for an unknown property if a default collection is provided.
     */
    @Test
    public void testGetCollectionUnknownWithDefault() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final List<Integer> defValue = Arrays.asList(1, 2, 4, 8, 16, 32);
        final Collection<Integer> result = config.getCollection(Integer.class, KEY_PREFIX, null, defValue);
        assertEquals(defValue, result);
    }

    /**
     * Tries to query an encoded string without a decoder.
     */
    @Test
    public void testGetEncodedStringNoDecoder() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        assertThrows(IllegalArgumentException.class, () -> config.getEncodedString(KEY_PREFIX, null));
    }

    /**
     * Tries to query an encoded string with the default decoder if this property is not defined.
     */
    @Test
    public void testGetEncodedStringNoDefaultDecoderDefined() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        assertThrows(IllegalStateException.class, () -> config.getEncodedString(KEY_PREFIX));
    }

    /**
     * Tests whether undefined keys are handled when querying encoded strings.
     */
    @Test
    public void testGetEncodedStringNoValue() {
        final ConfigurationDecoder decoder = mock(ConfigurationDecoder.class);
        final PropertiesConfiguration config = new PropertiesConfiguration();
        assertNull(config.getEncodedString(KEY_PREFIX, decoder));
    }

    /**
     * Tests whether an encoded value can be retrieved.
     */
    @Test
    public void testGetEncodedStringValue() {
        final ConfigurationDecoder decoder = mock(ConfigurationDecoder.class);
        final String value = "original value";
        final String decodedValue = "decoded value";

        when(decoder.decode(value)).thenReturn(decodedValue);

        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty(KEY_PREFIX, value);
        assertEquals(decodedValue, config.getEncodedString(KEY_PREFIX, decoder));
    }

    /**
     * Tests whether a default decoder can be set which is queried for encoded strings.
     */
    @Test
    public void testGetEncodedStringWithDefaultDecoder() {
        final ConfigurationDecoder decoder = mock(ConfigurationDecoder.class);
        final String value = "original value";
        final String decodedValue = "decoded value";

        when(decoder.decode(value)).thenReturn(decodedValue);

        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.setConfigurationDecoder(decoder);
        config.addProperty(KEY_PREFIX, value);
        assertEquals(decodedValue, config.getEncodedString(KEY_PREFIX));
    }

    /**
     * Tests a conversion to a list.
     */
    @Test
    public void testGetList() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final List<Integer> expected = prepareListTest(config);
        final List<Integer> result = config.getList(Integer.class, KEY_PREFIX);
        assertEquals(expected, result);
    }

    /**
     * Tests getList() for single non-string values.
     */
    @Test
    public void testGetListNonString() {
        checkGetListScalar(Integer.valueOf(42));
        checkGetListScalar(Long.valueOf(42));
        checkGetListScalar(Short.valueOf((short) 42));
        checkGetListScalar(Byte.valueOf((byte) 42));
        checkGetListScalar(Float.valueOf(42));
        checkGetListScalar(Double.valueOf(42));
        checkGetListScalar(Boolean.TRUE);
    }

    /**
     * Tests a conversion to a list if the property is unknown and no default value is provided.
     */
    @Test
    public void testGetListUnknownNoDefault() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        assertNull(config.getList(Integer.class, KEY_PREFIX));
    }

    /**
     * Tests a conversion to a list if the property is unknown and a default list is provided.
     */
    @Test
    public void testGetListUnknownWithDefault() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final List<Integer> defValue = Arrays.asList(1, 2, 3);
        assertEquals(defValue, config.getList(Integer.class, KEY_PREFIX, defValue));
    }

    /**
     * Tests getStringArray() for single son-string values.
     */
    @Test
    public void testGetStringArrayNonString() {
        checkGetStringArrayScalar(Integer.valueOf(42));
        checkGetStringArrayScalar(Long.valueOf(42));
        checkGetStringArrayScalar(Short.valueOf((short) 42));
        checkGetStringArrayScalar(Byte.valueOf((byte) 42));
        checkGetStringArrayScalar(Float.valueOf(42));
        checkGetStringArrayScalar(Double.valueOf(42));
        checkGetStringArrayScalar(Boolean.TRUE);
    }

    /**
     * Tests getStringArray() if the key cannot be found.
     */
    @Test
    public void testGetStringArrayUnknown() {
        final BaseConfiguration config = new BaseConfiguration();
        final String[] array = config.getStringArray(KEY_PREFIX);
        assertEquals(0, array.length);
    }

    /**
     * Tests get() for an unknown property if no default value is provided.
     */
    @Test
    public void testGetUnknownNoDefaultValue() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        assertNull(config.get(Integer.class, KEY_PREFIX));
    }

    /**
     * Tests get() for an unknown property if a default value is provided.
     */
    @Test
    public void testGetUnknownWithDefaultValue() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final Integer defaultValue = 2121;
        assertEquals(defaultValue, config.get(Integer.class, KEY_PREFIX, defaultValue));
    }

    /**
     * Tests get() for an unknown property if the throwExceptionOnMissing flag is set.
     */
    @Test
    public void testGetUnknownWithThrowExceptionOnMissing() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.setThrowExceptionOnMissing(true);
        assertThrows(NoSuchElementException.class, () -> config.get(Integer.class, KEY_PREFIX));
    }

    /**
     * Tests get() for an unknown property with a default value and the throwExceptionOnMissing flag. Because of the default
     * value no exception should be thrown.
     */
    @Test
    public void testGetUnownWithDefaultValueThrowExceptionOnMissing() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.setThrowExceptionOnMissing(true);
        final Integer defaultValue = 2121;
        assertEquals(defaultValue, config.get(Integer.class, KEY_PREFIX, defaultValue));
    }

    /**
     * Tests whether a new {@code ConfigurationInterpolator} can be installed without providing custom lookups.
     */
    @Test
    public void testInstallInterpolatorNull() {
        final AbstractConfiguration config = new TestConfigurationImpl(new PropertiesConfiguration());
        config.installInterpolator(null, null);
        assertTrue(config.getInterpolator().getLookups().isEmpty());
        final List<Lookup> defLookups = config.getInterpolator().getDefaultLookups();
        assertEquals(1, defLookups.size());
        assertInstanceOf(ConfigurationLookup.class, defLookups.get(0));
    }

    /**
     * Tests whether a property can reference an array using interpolation. This is related to CONFIGURATION-633.
     */
    @Test
    public void testInterpolateArray() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final String[] values = {"some", "test", "values"};
        final String keyArray = "testArray";
        config.addProperty(keyArray, values);
        config.addProperty(KEY_PREFIX, "${" + keyArray + "}");

        assertArrayEquals(values, config.getStringArray(KEY_PREFIX));
    }

    /**
     * Tests whether environment variables can be interpolated.
     */
    @Test
    public void testInterpolateEnvironmentVariables() {
        final AbstractConfiguration config = new TestConfigurationImpl(new PropertiesConfiguration());
        InterpolationTestHelper.testInterpolationEnvironment(config);
    }

    /**
     * Tests escaping the variable marker, so that no interpolation will be performed.
     */
    @Test
    public void testInterpolateEscape() {
        final AbstractConfiguration config = new TestConfigurationImpl(new PropertiesConfiguration());
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        config.addProperty("mypath", "$${DB2UNIVERSAL_JDBC_DRIVER_PATH}/db2jcc.jar\\,$${DB2UNIVERSAL_JDBC_DRIVER_PATH}/db2jcc_license_cu.jar");
        assertEquals("${DB2UNIVERSAL_JDBC_DRIVER_PATH}/db2jcc.jar,${DB2UNIVERSAL_JDBC_DRIVER_PATH}/db2jcc_license_cu.jar", config.getString("mypath"));
    }

    /**
     * Tests whether a property can reference a list using interpolation. This is related to CONFIGURATION-633.
     */
    @Test
    public void testInterpolateList() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final List<String> values = Arrays.asList("some", "test", "values");
        final String keyList = "testList";
        config.addProperty(keyList, values);
        config.addProperty(KEY_PREFIX, "${" + keyList + "}");

        assertEquals(values, config.getList(String.class, KEY_PREFIX));
    }

    /**
     * Tests complex interpolation where the variables' values contain in turn other variables.
     */
    @Test
    public void testInterpolateRecursive() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "${animal_attr} fox");
        config.addProperty("target", "${target_attr} dog");
        config.addProperty("animal_attr", "quick brown");
        config.addProperty("target_attr", "lazy");
        config.addProperty(KEY_PREFIX, SUBST_TXT);
        assertEquals("The quick brown fox jumps over the lazy dog.", config.getString(KEY_PREFIX));
    }

    /**
     * Tests the interpolation features.
     */
    @Test
    public void testInterpolateString() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "quick brown fox");
        config.addProperty("target", "lazy dog");
        config.addProperty(KEY_PREFIX, SUBST_TXT);
        assertEquals("The quick brown fox jumps over the lazy dog.", config.getString(KEY_PREFIX));
    }

    /**
     * Tests that variables with list values in interpolated string are resolved with the first element
     * in the list.
     */
    @Test
    public void testInterpolateStringWithListVariable() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final List<String> values = Arrays.asList("some", "test", "values");
        final String keyList = "testList";
        config.addProperty(keyList, values);
        config.addProperty(KEY_PREFIX, "result = ${" + keyList + "}");

        assertEquals("result = some", config.getString(KEY_PREFIX));
    }

    /**
     * Tests interpolate() if the configuration does not have a {@code ConfigurationInterpolator}.
     */
    @Test
    public void testInterpolationNoInterpolator() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "quick brown fox");
        config.addProperty("target", "lazy dog");
        config.addProperty(KEY_PREFIX, SUBST_TXT);
        config.setInterpolator(null);
        assertEquals(SUBST_TXT, config.getString(KEY_PREFIX));
    }

    /**
     * Tests interpolation if a variable is unknown. Then the variable won't be substituted.
     */
    @Test
    public void testInterpolationUnknownVariable() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "quick brown fox");
        config.addProperty(KEY_PREFIX, SUBST_TXT);
        assertEquals("The quick brown fox jumps over the ${target}.", config.getString(KEY_PREFIX));
    }

    /**
     * Tests whether interpolation works in variable names.
     */
    @Test
    public void testNestedVariableInterpolation() {
        final BaseConfiguration config = new BaseConfiguration();
        config.getInterpolator().setEnableSubstitutionInVariables(true);
        config.addProperty("java.version", "1.4");
        config.addProperty("jre-1.4", "C:\\java\\1.4");
        config.addProperty("jre.path", "${jre-${java.version}}");
        assertEquals("C:\\java\\1.4", config.getString("jre.path"));
    }

    /**
     * Tests whether the conversion handler can be changed.
     */
    @Test
    public void testSetDefaultConversionHandler() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final ConversionHandler handler = new DefaultConversionHandler();
        config.setConversionHandler(handler);
        assertSame(handler, config.getConversionHandler());
    }

    /**
     * Tries to set a null value for the conversion handler.
     */
    @Test
    public void testSetDefaultConversionHandlerNull() {
        final PropertiesConfiguration configuration = new PropertiesConfiguration();
        assertThrows(IllegalArgumentException.class, () -> configuration.setConversionHandler(null));
    }

    /**
     * Tests whether default lookups can be added to an already existing {@code ConfigurationInterpolator}.
     */
    @Test
    public void testSetDefaultLookupsExistingInterpolator() {
        final Lookup look = mock(Lookup.class);
        final AbstractConfiguration config = new TestConfigurationImpl(new PropertiesConfiguration());
        config.getInterpolator().addDefaultLookup(new ConfigurationLookup(new PropertiesConfiguration()));
        config.setDefaultLookups(Collections.singleton(look));
        final List<Lookup> lookups = config.getInterpolator().getDefaultLookups();
        assertEquals(3, lookups.size());
        assertSame(look, lookups.get(1));
        assertInstanceOf(ConfigurationLookup.class, lookups.get(2));
    }

    /**
     * Tests whether default lookups can be added if not {@code ConfigurationInterpolator} exists yet.
     */
    @Test
    public void testSetDefaultLookupsNoInterpolator() {
        final Lookup look = mock(Lookup.class);
        final AbstractConfiguration config = new TestConfigurationImpl(new PropertiesConfiguration());
        config.setInterpolator(null);
        config.setDefaultLookups(Collections.singleton(look));
        final List<Lookup> lookups = config.getInterpolator().getDefaultLookups();
        assertEquals(2, lookups.size());
        assertSame(look, lookups.get(0));
        assertInstanceOf(ConfigurationLookup.class, lookups.get(1));
    }

    /**
     * Tries to set a null list delimiter handler.
     */
    @Test
    public void testSetListDelimiterHandlerNull() {
        final BaseConfiguration config = new BaseConfiguration();
        assertThrows(IllegalArgumentException.class, () -> config.setListDelimiterHandler(null));
    }

    /**
     * Tests whether a parent {@code ConfigurationInterpolator} can be set if already a {@code ConfigurationInterpolator} is
     * available.
     */
    @Test
    public void testSetParentInterpolatorExistingInterpolator() {
        final ConfigurationInterpolator parent = mock(ConfigurationInterpolator.class);
        final AbstractConfiguration config = new TestConfigurationImpl(new PropertiesConfiguration());
        final ConfigurationInterpolator ci = config.getInterpolator();
        config.setParentInterpolator(parent);
        assertSame(parent, config.getInterpolator().getParentInterpolator());
        assertSame(ci, config.getInterpolator());
    }

    /**
     * Tests whether a parent {@code ConfigurationInterpolator} can be set if currently no {@code ConfigurationInterpolator}
     * is available.
     */
    @Test
    public void testSetParentInterpolatorNoInterpolator() {
        final ConfigurationInterpolator parent = mock(ConfigurationInterpolator.class);
        final AbstractConfiguration config = new TestConfigurationImpl(new PropertiesConfiguration());
        config.setInterpolator(null);
        config.setParentInterpolator(parent);
        assertSame(parent, config.getInterpolator().getParentInterpolator());
    }

    /**
     * Tests whether prefix lookups can be added to an existing {@code ConfigurationInterpolator}.
     */
    @Test
    public void testSetPrefixLookupsExistingInterpolator() {
        final Lookup look = mock(Lookup.class);
        final AbstractConfiguration config = new TestConfigurationImpl(new PropertiesConfiguration());
        final int count = config.getInterpolator().getLookups().size();
        final Map<String, Lookup> lookups = new HashMap<>();
        lookups.put("test", look);
        config.setPrefixLookups(lookups);
        final Map<String, Lookup> lookups2 = config.getInterpolator().getLookups();
        assertEquals(count + 1, lookups2.size());
        assertSame(look, lookups2.get("test"));
    }

    /**
     * Tests whether prefix lookups can be added if no {@code ConfigurationInterpolator} exists yet.
     */
    @Test
    public void testSetPrefixLookupsNoInterpolator() {
        final Lookup look = mock(Lookup.class);
        final AbstractConfiguration config = new TestConfigurationImpl(new PropertiesConfiguration());
        config.setInterpolator(null);
        config.setPrefixLookups(Collections.singletonMap("test", look));
        final Map<String, Lookup> lookups = config.getInterpolator().getLookups();
        assertEquals(1, lookups.size());
        assertSame(look, lookups.get("test"));
    }

    /**
     * Tests the default implementation of sizeInternal().
     */
    @Test
    public void testSizeInternal() {
        final AbstractConfiguration config = new TestConfigurationImpl(new PropertiesConfiguration());
        for (int i = 0; i < PROP_COUNT; i++) {
            config.addProperty(KEY_PREFIX + i, "value" + i);
        }
        assertEquals(PROP_COUNT, config.size());
    }
}
