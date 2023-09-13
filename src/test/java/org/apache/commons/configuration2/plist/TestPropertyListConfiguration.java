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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.ConfigurationComparator;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.StrictConfigurationComparator;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.NodeHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 */
public class TestPropertyListConfiguration {
    /**
     * Returns a list with the children of the given configuration's root note with the specified name.
     *
     * @param config the configuration
     * @param name the name of the desired children
     * @return the list with the corresponding child nodes
     */
    private static List<ImmutableNode> getNamedChildren(final HierarchicalConfiguration<ImmutableNode> config, final String name) {
        final NodeHandler<ImmutableNode> handler = config.getNodeModel().getNodeHandler();
        return handler.getChildren(handler.getRootNode(), name);
    }

    /**
     * Loads a configuration from the specified test file.
     *
     * @param c the configuration to be loaded
     * @param f the file to be loaded
     * @throws ConfigurationException if an error occurs
     */
    private static void load(final PropertyListConfiguration c, final File f) throws ConfigurationException {
        new FileHandler(c).load(f);
    }

    /** A folder for temporary files. */
    @TempDir
    public File tempFolder;

    private PropertyListConfiguration config;

    private final File testProperties = ConfigurationAssert.getTestFile("test.plist");

    /**
     * Saves the test configuration to the specified file.
     *
     * @param file the target file
     * @throws ConfigurationException if an error occurs
     */
    private void saveConfig(final File file) throws ConfigurationException {
        new FileHandler(config).save(file);
    }

    @BeforeEach
    public void setUp() throws Exception {
        config = new PropertyListConfiguration();
        load(config, testProperties);
    }

    /**
     * Ensure that addProperty doesn't alter an array of byte
     */
    @Test
    public void testAddDataProperty() throws Exception {
        final File saveFile = newFile(tempFolder);
        final byte[] expected = {1, 2, 3, 4};
        config = new PropertyListConfiguration();
        config.addProperty("foo", expected);
        saveConfig(saveFile);

        final PropertyListConfiguration config2 = new PropertyListConfiguration();
        load(config2, saveFile);
        final Object array = config2.getProperty("foo");

        assertNotNull(array);
        assertEquals(byte[].class, array.getClass());
        assertArrayEquals(expected, (byte[]) array);
    }

    @Test
    public void testArray() {
        final List<?> list = assertInstanceOf(List.class, config.getProperty("array"));
        assertEquals(Arrays.asList("value1", "value2", "value3"), list);
    }

    @Test
    public void testData() {
        final byte[] bytes = assertInstanceOf(byte[].class, config.getProperty("data"));
        assertArrayEquals("foo bar".getBytes(), bytes);
    }

    @Test
    public void testDate() throws Exception {
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2002, Calendar.MARCH, 22, 11, 30, 0);
        cal.setTimeZone(TimeZone.getTimeZone("GMT+0100"));
        final Date date = cal.getTime();

        assertEquals(date, config.getProperty("date"));
    }

    @Test
    public void testDictionary() {
        assertEquals("bar1", config.getProperty("dictionary.foo1"));
        assertEquals("bar2", config.getProperty("dictionary.foo2"));
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
    public void testEmptyArray() {
        final String key = "empty-array";
        assertNotNull(config.getProperty(key));

        final List<?> list = (List<?>) config.getProperty(key);
        assertTrue(list.isEmpty());
    }

    /**
     * Tests formatting a date.
     */
    @Test
    public void testFormatDate() {
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2007, Calendar.OCTOBER, 29, 23, 4, 30);
        cal.setTimeZone(TimeZone.getTimeZone("GMT-0230"));
        assertEquals("<*D2007-10-29 23:04:30 -0230>", PropertyListConfiguration.formatDate(cal));
        cal.clear();
        cal.set(2007, Calendar.OCTOBER, 30, 22, 2, 15);
        cal.setTimeZone(TimeZone.getTimeZone("GMT+1111"));
        assertEquals("<*D2007-10-30 22:02:15 +1111>", PropertyListConfiguration.formatDate(cal));
    }

    @Test
    public void testInitCopy() {
        final PropertyListConfiguration copy = new PropertyListConfiguration(config);
        assertFalse(copy.isEmpty());
    }

    @Test
    public void testLoad() {
        assertFalse(config.isEmpty());
    }

    @Test
    public void testLoadWithError() {
        config = new PropertyListConfiguration();
        final FileHandler fileHandler = new FileHandler(config);
        final StringReader reader = new StringReader("");
        final ConfigurationException e = assertThrows(ConfigurationException.class, () -> fileHandler.load(reader));
        assertNotNull(e.getMessage());
    }

    @Test
    public void testNestedArrays() {
        final String key = "nested-arrays";

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
    public void testNestedDictionaries() {
        assertEquals("value", config.getString("nested-dictionaries.foo.bar.key"));
    }

    /**
     * Tests parsing a date that contains an invalid separator character.
     */
    @Test
    public void testParseDateInvalidChar() {
        assertThrows(ParseException.class, () -> PropertyListConfiguration.parseDate("<*D2002+03-22 11:30:00 +0100>"));
    }

    /**
     * Tests parsing a date with an invalid numeric value.
     */
    @Test
    public void testParseDateNoNumber() {
        assertThrows(ParseException.class, () -> PropertyListConfiguration.parseDate("<*D2002-03-22 1c:30:00 +0100>"));
    }

    /**
     * Tries parsing a null date. This should cause an exception.n
     */
    @Test
    public void testParseDateNull() {
        assertThrows(ParseException.class, () -> PropertyListConfiguration.parseDate(null));
    }

    /**
     * Tests parsing a date that is not long enough.
     */
    @Test
    public void testParseDateTooShort() {
        assertThrows(ParseException.class, () -> PropertyListConfiguration.parseDate("<*D2002-03-22 11:3>"));
    }

    @Test
    public void testQuotedString() {
        assertEquals("string2", config.getProperty("quoted-string"));
        assertEquals("this is a string", config.getProperty("quoted-string2"));
        assertEquals("this is a \"complex\" string {(=,;)}", config.getProperty("complex-string"));
    }

    @Test
    public void testQuoteString() {
        assertNull(config.quoteString(null));
        assertEquals("abcd", config.quoteString("abcd"));
        assertEquals("\"ab cd\"", config.quoteString("ab cd"));
        assertEquals("\"foo\\\"bar\"", config.quoteString("foo\"bar"));
        assertEquals("\"foo;bar\"", config.quoteString("foo;bar"));
    }

    @Test
    public void testSave() throws Exception {
        final File savedFile = newFile("testsave.plist", tempFolder);

        // save the configuration
        saveConfig(savedFile);
        assertTrue(savedFile.exists());

        // read the configuration and compare the properties
        final PropertyListConfiguration checkConfig = new PropertyListConfiguration();
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
        final File savedFile = newFile("testsave.plist", tempFolder);

        // save the configuration
        saveConfig(savedFile);
        assertTrue(savedFile.exists());

        // read the configuration and compare the properties
        final PropertyListConfiguration checkConfig = new PropertyListConfiguration();
        load(checkConfig, savedFile);

        assertFalse(getNamedChildren(config, "empty-dictionary").isEmpty());
        assertFalse(getNamedChildren(checkConfig, "empty-dictionary").isEmpty());
    }

    /**
     * Ensure that setProperty doesn't alter an array of byte since it's a first class type in plist file
     */
    @Test
    public void testSetDataProperty() throws Exception {
        final File saveFile = newFile(tempFolder);
        final byte[] expected = {1, 2, 3, 4};
        config = new PropertyListConfiguration();
        config.setProperty("foo", expected);
        saveConfig(saveFile);

        final PropertyListConfiguration config2 = new PropertyListConfiguration();
        load(config2, saveFile);
        final Object array = config2.getProperty("foo");

        assertNotNull(array);
        assertEquals(byte[].class, array.getClass());
        assertArrayEquals(expected, (byte[]) array);
    }

    @Test
    public void testString() {
        assertEquals("string1", config.getProperty("simple-string"));
    }
}
