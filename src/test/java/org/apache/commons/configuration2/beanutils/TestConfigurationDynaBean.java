/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration2.beanutils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.beanutils2.DynaProperty;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Test Case for the {@code ConfigurationDynaBean} implementation class. These tests were based on the ones in
 * {@code BasicDynaBeanTestCase} because the two classes provide similar levels of functionality.
 * </p>
 */
public class TestConfigurationDynaBean {

    /**
     * The basic test bean for each test.
     */
    private ConfigurationDynaBean bean;

    /**
     * The set of property names we expect to have returned when calling {@code getDynaProperties()}. You should update this
     * list when new properties are added to TestBean.
     */
    private final String[] properties = {"booleanProperty", "booleanSecond", "doubleProperty", "floatProperty", "intProperty", "longProperty",
            "mappedProperty.key1", "mappedProperty.key2", "mappedProperty.key3", "mappedIntProperty.key1", "shortProperty", "stringProperty", "byteProperty",
            "charProperty"};

    private final Object[] values = {Boolean.TRUE, Boolean.TRUE, Double.MAX_VALUE, Float.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, "First Value",
            "Second Value", "Third Value", Integer.MAX_VALUE, Short.MAX_VALUE, "This is a string", Byte.MAX_VALUE, Character.MAX_VALUE};

    private final int[] intArray = {0, 10, 20, 30, 40};
    private final boolean[] booleanArray = {true, false, true, false, true};
    private final char[] charArray = {'a', 'b', 'c', 'd', 'e'};
    private final byte[] byteArray = {0, 10, 20, 30, 40};
    private final long[] longArray = {0, 10, 20, 30, 40};
    private final short[] shortArray = {0, 10, 20, 30, 40};
    private final float[] floatArray = {0, 10, 20, 30, 40};
    private final double[] doubleArray = {0.0, 10.0, 20.0, 30.0, 40.0};
    private final String[] stringArray = {"String 0", "String 1", "String 2", "String 3", "String 4"};

    /**
     * Creates the underlying configuration object for the dyna bean.
     *
     * @return the underlying configuration object
     */
    protected Configuration createConfiguration() {
        return new BaseConfiguration();
    }

    /**
     * Sets up instance variables required by this test case.
     */
    @BeforeEach
    public void setUp() throws Exception {
        final Configuration configuration = createConfiguration();

        for (int i = 0; i < properties.length; i++) {
            configuration.setProperty(properties[i], values[i]);
        }

        for (final int element : intArray) {
            configuration.addProperty("intIndexed", element);
        }

        for (final String element : stringArray) {
            configuration.addProperty("stringIndexed", element);
        }

        final List<String> list = Arrays.asList(stringArray);
        configuration.addProperty("listIndexed", list);

        bean = new ConfigurationDynaBean(configuration);

        bean.set("listIndexed", list);
        bean.set("intArray", intArray);
        bean.set("booleanArray", booleanArray);
        bean.set("charArray", charArray);
        bean.set("longArray", longArray);
        bean.set("shortArray", shortArray);
        bean.set("floatArray", floatArray);
        bean.set("doubleArray", doubleArray);
        bean.set("byteArray", byteArray);
        bean.set("stringArray", stringArray);
    }

    /**
     * Tests set on a null value: should throw NPE.
     */
    @Test
    void testAddNullPropertyValue() {
        assertThrows(NullPointerException.class, () -> bean.set("nullProperty", null));
    }

    /**
     * Corner cases on getDynaProperty invalid arguments.
     */
    @Test
    void testGetDescriptorArguments() {
        final DynaProperty descriptor = bean.getDynaClass().getDynaProperty("unknown");
        assertNull(descriptor);
        assertThrows(IllegalArgumentException.class, () -> bean.getDynaClass().getDynaProperty(null));
    }

    /**
     * Base for testGetDescriptorXxxxx() series of tests.
     *
     * @param name Name of the property to be retrieved
     * @param type Expected class type of this property
     */
    protected void testGetDescriptorBase(final String name, final Class<?> type) {
        final DynaProperty descriptor = bean.getDynaClass().getDynaProperty(name);

        assertNotNull(descriptor);
        assertEquals(type, descriptor.getType());
    }

    /**
     * Positive getDynaProperty on property {@code booleanProperty}.
     */
    @Test
    void testGetDescriptorBoolean() {
        testGetDescriptorBase("booleanProperty", Boolean.TYPE);
    }

    /**
     * Positive getDynaProperty on property {@code doubleProperty}.
     */
    @Test
    void testGetDescriptorDouble() {
        testGetDescriptorBase("doubleProperty", Double.TYPE);
    }

    /**
     * Positive getDynaProperty on property {@code floatProperty}.
     */
    @Test
    void testGetDescriptorFloat() {
        testGetDescriptorBase("floatProperty", Float.TYPE);
    }

    /**
     * Positive getDynaProperty on property {@code intProperty}.
     */
    @Test
    void testGetDescriptorInt() {
        testGetDescriptorBase("intProperty", Integer.TYPE);
    }

    /**
     * Positive getDynaProperty on property {@code longProperty}.
     */
    @Test
    void testGetDescriptorLong() {
        testGetDescriptorBase("longProperty", Long.TYPE);
    }

    /**
     * Positive test for getDynaPropertys(). Each property name listed in {@code properties} should be returned exactly
     * once.
     */
    @Test
    void testGetDescriptors() {
        final DynaProperty[] pd = bean.getDynaClass().getDynaProperties();
        assertNotNull(pd);
        final int[] count = new int[properties.length];
        for (final DynaProperty element : pd) {
            final String name = element.getName();
            for (int j = 0; j < properties.length; j++) {
                if (name.equals(properties[j])) {
                    count[j]++;
                }
            }
        }

        for (int j = 0; j < properties.length; j++) {
            assertFalse(count[j] < 0, "Missing property " + properties[j]);
            assertFalse(count[j] > 1, "Duplicate property " + properties[j]);
        }
    }

    /**
     * Positive getDynaProperty on property {@code booleanSecond} that uses an "is" method as the getter.
     */
    @Test
    void testGetDescriptorSecond() {
        testGetDescriptorBase("booleanSecond", Boolean.TYPE);
    }

    /**
     * Positive getDynaProperty on property {@code shortProperty}.
     */
    @Test
    void testGetDescriptorShort() {
        testGetDescriptorBase("shortProperty", Short.TYPE);
    }

    /**
     * Positive getDynaProperty on property {@code stringProperty}.
     */
    @Test
    void testGetDescriptorString() {
        testGetDescriptorBase("stringProperty", String.class);
    }

    /**
     * Corner cases on getIndexedProperty invalid arguments.
     */
    @Test
    void testGetIndexedArguments() {
        assertThrows(IndexOutOfBoundsException.class, () -> bean.get("intArray", -1));
    }

    /**
     * Tests whether an indexed access to a non-existing property causes an exception.
     */
    @Test
    void testGetIndexedNonExisting() {
        assertThrows(IllegalArgumentException.class, () -> bean.get("Non existing property", 0));
    }

    /**
     * Tests whether accessing a non-indexed string property using the index get method causes an exception.
     */
    @Test
    void testGetIndexedString() {
        bean.set("stringProp", "value");
        assertThrows(IllegalArgumentException.class, () -> bean.get("stringProp", 0));
    }

    /**
     * Positive and negative tests on getIndexedProperty valid arguments.
     */
    @Test
    void testGetIndexedValues() {
        for (int i = 0; i < 5; i++) {
            Object value = bean.get("intArray", i);

            int intValue = assertInstanceOf(Integer.class, value, "intArray index " + i);
            assertEquals(i * 10, intValue, "intArray " + i);

            value = bean.get("intIndexed", i);

            intValue = assertInstanceOf(Integer.class, value, "intIndexed index " + i);
            assertEquals(i * 10, intValue, "intIndexed index " + i);

            value = bean.get("listIndexed", i);

            assertInstanceOf(String.class, value, "list index " + i);
            assertEquals("String " + i, value, "listIndexed index " + i);

            value = bean.get("stringArray", i);

            assertInstanceOf(String.class, value, "stringArray index " + i);
            assertEquals("String " + i, value, "stringArray index " + i);

            value = bean.get("stringIndexed", i);

            assertInstanceOf(String.class, value, "stringIndexed index " + i);
            assertEquals("String " + i, value, "stringIndexed index " + i);
        }
    }

    /**
     * Corner cases on getMappedProperty invalid arguments.
     */
    @Test
    void testGetMappedArguments() {
        final Object value = bean.get("mappedProperty", "unknown");
        assertNull(value);
    }

    /**
     * Positive and negative tests on getMappedProperty valid arguments.
     */
    @Test
    void testGetMappedValues() {
        Object value = bean.get("mappedProperty", "key1");
        assertEquals("First Value", value);

        value = bean.get("mappedProperty", "key2");
        assertEquals("Second Value", value);

        value = bean.get("mappedProperty", "key3");
        assertNotNull(value);
    }

    /**
     * Test the retrieval of a non-existent property.
     */
    @Test
    void testGetNonExistentProperty() {
        assertThrows(IllegalArgumentException.class, () -> bean.get("nonexistProperty"));
    }

    /**
     * Tests if reading a non-indexed property using the index get method throws an IllegalArgumentException as it should.
     */
    @Test
    void testGetNonIndexedProperties() {
        assertThrows(IllegalArgumentException.class, () -> bean.get("booleanProperty", 0));
    }

    /**
     * Corner cases on getSimpleProperty invalid arguments.
     */
    @Test
    void testGetSimpleArguments() {
        assertThrows(IllegalArgumentException.class, () -> bean.get("a non existing property"));
    }

    /**
     * Test getSimpleProperty on a boolean property.
     */
    @Test
    void testGetSimpleBoolean() {
        final Object value = bean.get("booleanProperty");
        assertInstanceOf(Boolean.class, value);
        assertEquals(Boolean.TRUE, value);
    }

    /**
     * Test getSimpleProperty on a double property.
     */
    @Test
    void testGetSimpleDouble() {
        final Object value = bean.get("doubleProperty");
        final double doubleValue = assertInstanceOf(Double.class, value);
        assertEquals(Double.MAX_VALUE, doubleValue, 0.005);
    }

    /**
     * Test getSimpleProperty on a float property.
     */
    @Test
    void testGetSimpleFloat() {
        final Object value = bean.get("floatProperty");
        final float floatValue = assertInstanceOf(Float.class, value);
        assertEquals(Float.MAX_VALUE, floatValue, 0.005f);
    }

    /**
     * Test getSimpleProperty on a int property.
     */
    @Test
    void testGetSimpleInt() {
        final Object value = bean.get("intProperty");
        final int intValue = assertInstanceOf(Integer.class, value);
        assertEquals(Integer.MAX_VALUE, intValue);
    }

    /**
     * Test getSimpleProperty on a long property.
     */
    @Test
    void testGetSimpleLong() {
        final Object value = bean.get("longProperty");
        final long longValue = assertInstanceOf(Long.class, value);
        assertEquals(Long.MAX_VALUE, longValue);
    }

    /**
     * Test getSimpleProperty on a short property.
     */
    @Test
    void testGetSimpleShort() {
        final Object value = bean.get("shortProperty");
        final short shortValue = assertInstanceOf(Short.class, value);
        assertEquals(Short.MAX_VALUE, shortValue);
    }

    /**
     * Test getSimpleProperty on a String property.
     */
    @Test
    void testGetSimpleString() {
        final Object value = bean.get("stringProperty");
        assertInstanceOf(String.class, value);
        assertEquals("This is a string", value);
    }

    /**
     * Test {@code contains()} method for mapped properties.
     */
    @Test
    void testMappedContains() {
        assertTrue(bean.contains("mappedProperty", "key1"));
        assertFalse(bean.contains("mappedProperty", "Unknown Key"));
    }

    /**
     * Test {@code remove()} method for mapped properties.
     */
    @Test
    void testMappedRemove() {
        assertTrue(bean.contains("mappedProperty", "key1"));
        bean.remove("mappedProperty", "key1");
        assertFalse(bean.contains("mappedProperty", "key1"));

        assertFalse(bean.contains("mappedProperty", "key4"));
        bean.remove("mappedProperty", "key4");
        assertFalse(bean.contains("mappedProperty", "key4"));
    }

    /**
     * Tests whether nested properties can be accessed.
     */
    @Test
    void testNestedPropeties() {
        final ConfigurationDynaBean nested = (ConfigurationDynaBean) bean.get("mappedProperty");

        final String value = (String) nested.get("key1");
        assertEquals("First Value", value);

        nested.set("key1", "undefined");
        assertEquals("undefined", bean.get("mappedProperty.key1"));
    }

    /**
     * Test the modification of a configuration property stored internally as an array.
     */
    @Test
    void testSetArrayValue() {
        final MapConfiguration configuration = new MapConfiguration(new HashMap<>());
        configuration.getMap().put("objectArray", new Object[] {"value1", "value2", "value3"});

        final ConfigurationDynaBean bean = new ConfigurationDynaBean(configuration);

        bean.set("objectArray", 1, "New Value 1");
        final Object value = bean.get("objectArray", 1);

        assertInstanceOf(String.class, value);
        assertEquals("New Value 1", value);
    }

    /**
     * Corner cases on setIndexedProperty invalid arguments.
     */
    @Test
    void testSetIndexedArguments() {
        assertThrows(IndexOutOfBoundsException.class, () -> bean.set("intArray", -1, 0));
    }

    /**
     * Positive and negative tests on setIndexedProperty valid arguments.
     */
    @Test
    void testSetIndexedValues() {
        bean.set("intArray", 0, 1);
        Object value = bean.get("intArray", 0);

        int intValue = assertInstanceOf(Integer.class, value);
        assertEquals(1, intValue);

        bean.set("intIndexed", 1, 11);
        value = bean.get("intIndexed", 1);

        intValue = assertInstanceOf(Integer.class, value);
        assertEquals(11, intValue);

        bean.set("listIndexed", 2, "New Value 2");
        value = bean.get("listIndexed", 2);

        assertInstanceOf(String.class, value);
        assertEquals("New Value 2", value);

        bean.set("stringArray", 3, "New Value 3");
        value = bean.get("stringArray", 3);

        assertInstanceOf(String.class, value);
        assertEquals("New Value 3", value);

        bean.set("stringIndexed", 4, "New Value 4");
        value = bean.get("stringIndexed", 4);

        assertInstanceOf(String.class, value);
        assertEquals("New Value 4", value);
    }

    /**
     * Positive and negative tests on setMappedProperty valid arguments.
     */
    @Test
    void testSetMappedValues() {
        bean.set("mappedProperty", "First Key", "New First Value");
        assertEquals("New First Value", bean.get("mappedProperty", "First Key"));

        bean.set("mappedProperty", "Fourth Key", "Fourth Value");
        assertEquals("Fourth Value", bean.get("mappedProperty", "Fourth Key"));
    }

    /**
     * Tests if writing a non-indexed property using the index set method with an index &gt; 0 throws an
     * IllegalArgumentException as it should.
     */
    @Test
    void testSetNonIndexedProperties() {
        assertThrows(IllegalArgumentException.class, () -> bean.set("booleanProperty", 1, Boolean.TRUE));
    }

    /**
     * Test setSimpleProperty on a boolean property.
     */
    @Test
    void testSetSimpleBoolean() {
        final boolean oldValue = ((Boolean) bean.get("booleanProperty")).booleanValue();
        final boolean newValue = !oldValue;
        bean.set("booleanProperty", newValue);
        assertEquals(newValue, ((Boolean) bean.get("booleanProperty")).booleanValue());
    }

    /**
     * Test setSimpleProperty on a double property.
     */
    @Test
    void testSetSimpleDouble() {
        final double oldValue = ((Double) bean.get("doubleProperty")).doubleValue();
        final double newValue = oldValue + 1.0;
        bean.set("doubleProperty", newValue);
        assertEquals(newValue, ((Double) bean.get("doubleProperty")).doubleValue(), 0.005);
    }

    /**
     * Test setSimpleProperty on a float property.
     */
    @Test
    void testSetSimpleFloat() {
        final float oldValue = ((Float) bean.get("floatProperty")).floatValue();
        final float newValue = oldValue + (float) 1.0;
        bean.set("floatProperty", newValue);
        assertEquals(newValue, ((Float) bean.get("floatProperty")).floatValue(), 0.005f);
    }

    /**
     * Test setSimpleProperty on a int property.
     */
    @Test
    void testSetSimpleInt() {
        final int oldValue = ((Integer) bean.get("intProperty")).intValue();
        final int newValue = oldValue + 1;
        bean.set("intProperty", newValue);
        assertEquals(newValue, ((Integer) bean.get("intProperty")).intValue());
    }

    /**
     * Test setSimpleProperty on a long property.
     */
    @Test
    void testSetSimpleLong() {
        final long oldValue = ((Long) bean.get("longProperty")).longValue();
        final long newValue = oldValue + 1;
        bean.set("longProperty", newValue);
        assertEquals(newValue, ((Long) bean.get("longProperty")).longValue());
    }

    /**
     * Test setSimpleProperty on a short property.
     */
    @Test
    void testSetSimpleShort() {
        final short oldValue = ((Short) bean.get("shortProperty")).shortValue();
        final short newValue = (short) (oldValue + 1);
        bean.set("shortProperty", newValue);
        assertEquals(newValue, ((Short) bean.get("shortProperty")).shortValue());
    }

    /**
     * Test setSimpleProperty on a String property.
     */
    @Test
    void testSetSimpleString() {
        final String oldValue = (String) bean.get("stringProperty");
        final String newValue = oldValue + " Extra Value";
        bean.set("stringProperty", newValue);
        assertEquals(newValue, bean.get("stringProperty"));
    }
}
