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

package org.apache.commons.configuration2.plist;

import static org.apache.commons.configuration2.TempDirUtils.newFile;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.ConfigurationComparator;
import org.apache.commons.configuration2.StrictConfigurationComparator;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 */
public class TestXMLPropertyListConfiguration {
    /**
     * Loads a test configuration.
     *
     * @param c the configuration object to be loaded
     * @param file the test file to be loaded
     * @throws ConfigurationException if an error occurs
     */
    private static void load(final XMLPropertyListConfiguration c, final File file) throws ConfigurationException {
        new FileHandler(c).load(file);
    }

    /** A folder for temporary files. */
    @TempDir
    public File tempFolder;

    /** The test configuration. */
    private XMLPropertyListConfiguration config;

    /**
     * Checks whether the test configuration contains a key with an array value.
     *
     * @param expectedValues the expected values
     */
    private void checkArrayProperty(final List<?> expectedValues) throws ConfigurationException {
        final StringWriter out = new StringWriter();
        new FileHandler(config).save(out);
        final StringBuilder values = new StringBuilder();
        for (final Object v : expectedValues) {
            values.append("<string>").append(v).append("</string>");
        }
        final String content = out.toString().replaceAll("[ \n\r]", "");
        assertThat(content, containsString(String.format("<key>array</key><array>%s</array>", values)));
    }

    /**
     * Saves the test configuration to the specified file.
     *
     * @param file the target file
     * @throws ConfigurationException if an error occurs
     */
    private void save(final File file) throws ConfigurationException {
        new FileHandler(config).save(file);
    }

    @BeforeEach
    public void setUp() throws Exception {
        config = new XMLPropertyListConfiguration();
        load(config, ConfigurationAssert.getTestFile("test.plist.xml"));
    }

    /**
     * Tests whether an array can be added correctly. This test is related to CONFIGURATION-427.
     */
    @Test
    public void testAddArray() throws ConfigurationException {
        final Object[] elems = {"arrayElem1", "arrayElem2", "arrayElem3"};
        config = new XMLPropertyListConfiguration();
        config.addProperty("array", elems);

        checkArrayProperty(Arrays.asList(elems));
    }

    /**
     * Ensure that addProperty doesn't alter an array of byte
     */
    @Test
    public void testAddDataProperty() throws Exception {
        final File savedFile = newFile(tempFolder);
        final byte[] expected = {1, 2, 3, 4};
        config = new XMLPropertyListConfiguration();
        config.addProperty("foo", expected);
        save(savedFile);

        final XMLPropertyListConfiguration config2 = new XMLPropertyListConfiguration();
        load(config2, savedFile);
        final Object array = config2.getProperty("foo");

        assertNotNull(array);
        assertEquals(byte[].class, array.getClass());
        assertArrayEquals(expected, (byte[]) array);
    }

    /**
     * Tests whether a list can be added correctly. This test is related to CONFIGURATION-427.
     */
    @Test
    public void testAddList() throws ConfigurationException {
        final List<String> elems = Arrays.asList("element1", "element2", "anotherElement");
        config = new XMLPropertyListConfiguration();
        config.addProperty("array", elems);

        checkArrayProperty(elems);
    }

    @Test
    public void testArray() {
        final Object array = config.getProperty("array");

        assertInstanceOf(List.class, array);
        final List<?> list = config.getList("array");
        assertEquals(Arrays.asList("value1", "value2", "value3"), list);
    }

    @Test
    public void testBoolean() throws Exception {
        assertTrue(config.getBoolean("boolean1"));
        assertFalse(config.getBoolean("boolean2"));
    }

    @Test
    public void testDate() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2005, Calendar.JANUARY, 1, 12, 0, 0);

        assertEquals(calendar.getTime(), config.getProperty("date"));

        calendar.setTimeZone(TimeZone.getTimeZone("CET"));
        calendar.set(2002, Calendar.MARCH, 22, 11, 30, 0);

        assertEquals(calendar.getTime(), config.getProperty("date-gnustep"));
    }

    @Test
    public void testDictionary() {
        assertEquals("value1", config.getProperty("dictionary.key1"));
        assertEquals("value2", config.getProperty("dictionary.key2"));
        assertEquals("value3", config.getProperty("dictionary.key3"));
    }

    @Test
    public void testDictionaryArray() {
        final String key = "dictionary-array";

        final Object array = config.getProperty(key);

        // root array
        assertNotNull(array);
        assertInstanceOf(List.class, array);
        final List<?> list = config.getList(key);

        assertEquals(2, list.size());

        // 1st dictionary
        final Configuration conf1 = assertInstanceOf(Configuration.class, list.get(0));
        assertFalse(conf1.isEmpty());
        assertEquals("bar", conf1.getProperty("foo"));

        // 2nd dictionary
        final Configuration conf2 = assertInstanceOf(Configuration.class, list.get(1));
        assertFalse(conf2.isEmpty());
        assertEquals("value", conf2.getProperty("key"));
    }

    @Test
    public void testInitCopy() {
        final XMLPropertyListConfiguration copy = new XMLPropertyListConfiguration(config);
        final StrictConfigurationComparator comp = new StrictConfigurationComparator();
        assertTrue(comp.compare(config, copy));
    }

    @Test
    public void testInteger() throws Exception {
        assertEquals(12345678900L, config.getLong("integer"));
    }

    /**
     * Tests whether a configuration can be loaded that does not start with a {@code dict} element. This test case is
     * related to CONFIGURATION-405.
     */
    @Test
    public void testLoadNoDict() throws ConfigurationException {
        final XMLPropertyListConfiguration plist = new XMLPropertyListConfiguration();
        load(plist, ConfigurationAssert.getTestFile("test2.plist.xml"));
        assertFalse(plist.isEmpty());
    }

    /**
     * Tests whether a configuration that does not start with a {@code dict} element can be loaded from a constructor. This
     * test case is related to CONFIGURATION-405.
     */
    @Test
    public void testLoadNoDictConstr() throws ConfigurationException {
        final XMLPropertyListConfiguration plist = new XMLPropertyListConfiguration();
        load(plist, ConfigurationAssert.getTestFile("test2.plist.xml"));
        assertFalse(plist.isEmpty());
    }

    @Test
    public void testNested() {
        assertEquals("value", config.getString("nested.node1.node2.node3"));
    }

    @Test
    public void testNestedArray() {
        final String key = "nested-array";

        final Object array = config.getProperty(key);

        // root array
        assertNotNull(array);
        assertInstanceOf(List.class, array);
        final List<?> list = config.getList(key);

        assertEquals(2, list.size());

        // 1st array
        final List<?> list1 = assertInstanceOf(List.class, list.get(0));
        assertEquals(Arrays.asList("a", "b"), list1);

        // 2nd array
        final List<?> list2 = assertInstanceOf(List.class, list.get(1));
        assertEquals(Arrays.asList("c", "d"), list2);
    }

    @Test
    public void testReal() throws Exception {
        assertEquals(-12.345, config.getDouble("real"), 0);
    }

    @Test
    public void testSave() throws Exception {
        final File savedFile = newFile(tempFolder);

        // add an array of strings to the configuration
        /*
         * config.addProperty("string", "value1"); List list = new ArrayList(); for (int i = 1; i < 5; i++) { list.add("value" +
         * i); } config.addProperty("newarray", list);
         */
        // todo : investigate why the array structure of 'newarray' is lost in the saved file

        // add a map of strings
        /*
         * Map map = new HashMap(); map.put("foo", "bar"); map.put("int", new Integer(123)); config.addProperty("newmap", map);
         */
        // todo : a Map added to a HierarchicalConfiguration should be decomposed as list of nodes

        // save the configuration
        save(savedFile);
        assertTrue(savedFile.exists());

        // read the configuration and compare the properties
        final XMLPropertyListConfiguration checkConfig = new XMLPropertyListConfiguration();
        load(checkConfig, savedFile);

        final Iterator<String> it = config.getKeys();
        while (it.hasNext()) {
            final String key = it.next();
            assertTrue(checkConfig.containsKey(key), "The saved configuration doesn't contain the key '" + key + "'");

            final Object value = checkConfig.getProperty(key);
            if (value instanceof byte[]) {
                final byte[] array = (byte[]) value;
                assertArrayEquals((byte[]) config.getProperty(key), array, "Value of the '" + key + "' property");
            } else if (value instanceof List) {
                final List<?> list1 = (List<?>) config.getProperty(key);
                final List<?> list2 = (List<?>) value;

                assertEquals(list1.size(), list2.size(), "The size of the list for the key '" + key + "' doesn't match");

                for (int i = 0; i < list2.size(); i++) {
                    final Object value1 = list1.get(i);
                    final Object value2 = list2.get(i);

                    if (value1 instanceof Configuration) {
                        final ConfigurationComparator comparator = new StrictConfigurationComparator();
                        assertTrue(comparator.compare((Configuration) value1, (Configuration) value2),
                                "The dictionnary at index " + i + " for the key '" + key + "' doesn't match");
                    } else {
                        assertEquals(value1, value2, "Element at index " + i + " for the key '" + key + "'");
                    }
                }

                assertEquals(config.getProperty(key), list1, "Value of the '" + key + "' property");
            } else {
                assertEquals(config.getProperty(key), checkConfig.getProperty(key), "Value of the '" + key + "' property");
            }

        }
    }

    @Test
    public void testSaveEmptyDictionary() throws Exception {
        final File savedFile = newFile(tempFolder);

        // save the configuration
        save(savedFile);
        assertTrue(savedFile.exists());

        // read the configuration and compare the properties
        final XMLPropertyListConfiguration checkConfig = new XMLPropertyListConfiguration();
        load(checkConfig, savedFile);

        assertNull(config.getProperty("empty-dictionary"));
        assertNull(checkConfig.getProperty("empty-dictionary"));
    }

    /**
     * Tests the header of a saved file if no encoding is specified.
     */
    @Test
    public void testSaveNoEncoding() throws ConfigurationException {
        final StringWriter writer = new StringWriter();
        new FileHandler(config).save(writer);
        assertTrue(writer.toString().contains("<?xml version=\"1.0\"?>"));
    }

    /**
     * Tests whether the encoding is written when saving a configuration.
     */
    @Test
    public void testSaveWithEncoding() throws ConfigurationException {
        final String encoding = StandardCharsets.UTF_8.name();
        final FileHandler handler = new FileHandler(config);
        handler.setEncoding(encoding);
        final StringWriter writer = new StringWriter();
        handler.save(writer);
        assertTrue(writer.toString().contains("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>"));
    }

    /**
     * Tests whether an array can be set correctly. This test is related to CONFIGURATION-750.
     */
    @Test
    public void testSetArray() throws ConfigurationException {
        final Object[] elems = {"arrayElem1", "arrayElem2", "arrayElem3"};
        config = new XMLPropertyListConfiguration();
        config.setProperty("array", elems);

        checkArrayProperty(Arrays.asList(elems));
    }

    /**
     * Ensure that setProperty doesn't alter an array of byte since it's a first class type in plist file
     */
    @Test
    public void testSetDataProperty() throws Exception {
        final File savedFile = newFile(tempFolder);
        final byte[] expected = {1, 2, 3, 4};
        config = new XMLPropertyListConfiguration();
        config.setProperty("foo", expected);
        save(savedFile);

        final XMLPropertyListConfiguration config2 = new XMLPropertyListConfiguration();
        load(config2, savedFile);
        final Object array = config2.getProperty("foo");

        assertNotNull(array);
        assertEquals(byte[].class, array.getClass());
        assertArrayEquals(expected, (byte[]) array);
    }

    /**
     * Tests a configuration file which contains an invalid date property value. This test is related to CONFIGURATION-501.
     */
    @Test
    public void testSetDatePropertyInvalid() throws ConfigurationException {
        config.clear();
        load(config, ConfigurationAssert.getTestFile("test_invalid_date.plist.xml"));
        assertEquals("value1", config.getString("string"));
        assertFalse(config.containsKey("date"));
    }

    /**
     * Tests whether a list can be set correctly. This test is related to CONFIGURATION-750.
     */
    @Test
    public void testSetList() throws ConfigurationException {
        final List<String> elems = Arrays.asList("element1", "element2", "anotherElement");
        config = new XMLPropertyListConfiguration();
        config.setProperty("array", elems);

        checkArrayProperty(elems);
    }

    @Test
    public void testString() throws Exception {
        assertEquals("value1", config.getString("string"));
    }

    @Test
    public void testSubset() {
        final Configuration subset = config.subset("dictionary");
        final Iterator<String> keys = subset.getKeys();

        String key = keys.next();
        assertEquals("key1", key);
        assertEquals("value1", subset.getString(key));

        key = keys.next();
        assertEquals("key2", key);
        assertEquals("value2", subset.getString(key));

        key = keys.next();
        assertEquals("key3", key);
        assertEquals("value3", subset.getString(key));

        assertFalse(keys.hasNext());
    }

    /**
     * Tests a direct invocation of the write() method. This test is related to CONFIGURATION-641.
     */
    @Test
    public void testWriteCalledDirectly() throws IOException {
        config = new XMLPropertyListConfiguration();
        config.addProperty("foo", "bar");

        try (Writer out = new FileWriter(newFile(tempFolder))) {
            final ConfigurationException e = assertThrows(ConfigurationException.class, () -> config.write(out));
            assertThat(e.getMessage(), containsString("FileHandler"));
        }
    }
}
