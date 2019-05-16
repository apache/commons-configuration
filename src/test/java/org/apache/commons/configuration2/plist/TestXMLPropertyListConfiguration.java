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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import junitx.framework.ArrayAssert;
import junitx.framework.ListAssert;
import junitx.framework.ObjectAssert;

/**
 * @author Emmanuel Bourg
 */
public class TestXMLPropertyListConfiguration
{
    /** A helper object for dealing with temporary files. */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** The test configuration. */
    private XMLPropertyListConfiguration config;

    @Before
    public void setUp() throws Exception
    {
        config = new XMLPropertyListConfiguration();
        load(config, ConfigurationAssert.getTestFile("test.plist.xml"));
    }

    /**
     * Loads a test configuration.
     *
     * @param c the configuration object to be loaded
     * @param file the test file to be loaded
     * @throws ConfigurationException if an error occurs
     */
    private static void load(final XMLPropertyListConfiguration c, final File file)
            throws ConfigurationException
    {
        new FileHandler(c).load(file);
    }

    /**
     * Saves the test configuration to the specified file.
     *
     * @param file the target file
     * @throws ConfigurationException if an error occurs
     */
    private void save(final File file) throws ConfigurationException
    {
        new FileHandler(config).save(file);
    }

    @Test
    public void testString() throws Exception
    {
        assertEquals("'string' property", "value1", config.getString("string"));
    }

    @Test
    public void testInteger() throws Exception
    {
        assertEquals("'integer' property", 12345678900L, config.getLong("integer"));
    }

    @Test
    public void testReal() throws Exception
    {
        assertEquals("'real' property", -12.345, config.getDouble("real"), 0);
    }

    @Test
    public void testBoolean() throws Exception
    {
        assertEquals("'boolean1' property", true, config.getBoolean("boolean1"));
        assertEquals("'boolean2' property", false, config.getBoolean("boolean2"));
    }

    @Test
    public void testDictionary()
    {
        assertEquals("1st element", "value1", config.getProperty("dictionary.key1"));
        assertEquals("2nd element", "value2", config.getProperty("dictionary.key2"));
        assertEquals("3rd element", "value3", config.getProperty("dictionary.key3"));
    }

    @Test
    public void testDate() throws Exception
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2005, Calendar.JANUARY, 1, 12, 0, 0);

        assertEquals("'date' property", calendar.getTime(), config.getProperty("date"));

        calendar.setTimeZone(TimeZone.getTimeZone("CET"));
        calendar.set(2002, Calendar.MARCH, 22, 11, 30, 0);

        assertEquals("'date-gnustep' property", calendar.getTime(), config.getProperty("date-gnustep"));
    }

    @Test
    public void testSubset()
    {
        final Configuration subset = config.subset("dictionary");
        final Iterator<String> keys = subset.getKeys();

        String key = keys.next();
        assertEquals("1st key", "key1", key);
        assertEquals("1st value", "value1", subset.getString(key));

        key = keys.next();
        assertEquals("2nd key", "key2", key);
        assertEquals("2nd value", "value2", subset.getString(key));

        key = keys.next();
        assertEquals("3rd key", "key3", key);
        assertEquals("3rd value", "value3", subset.getString(key));

        assertFalse("more than 3 properties founds", keys.hasNext());
    }

    @Test
    public void testArray()
    {
        final Object array = config.getProperty("array");

        assertNotNull("array not found", array);
        ObjectAssert.assertInstanceOf("the array element is not parsed as a List", List.class, array);
        final List<?> list = config.getList("array");

        assertFalse("empty array", list.isEmpty());
        assertEquals("size", 3, list.size());
        assertEquals("1st element", "value1", list.get(0));
        assertEquals("2nd element", "value2", list.get(1));
        assertEquals("3rd element", "value3", list.get(2));
    }

    @Test
    public void testNestedArray()
    {
        final String key = "nested-array";

        final Object array = config.getProperty(key);

        // root array
        assertNotNull("array not found", array);
        ObjectAssert.assertInstanceOf("the array element is not parsed as a List", List.class, array);
        final List<?> list = config.getList(key);

        assertFalse("empty array", list.isEmpty());
        assertEquals("size", 2, list.size());

        // 1st array
        ObjectAssert.assertInstanceOf("the array element is not parsed as a List", List.class, list.get(0));
        final List<?> list1 = (List<?>) list.get(0);
        assertFalse("nested array 1 is empty", list1.isEmpty());
        assertEquals("size", 2, list1.size());
        assertEquals("1st element", "a", list1.get(0));
        assertEquals("2nd element", "b", list1.get(1));

        // 2nd array
        ObjectAssert.assertInstanceOf("the array element is not parsed as a List", List.class, list.get(1));
        final List<?> list2 = (List<?>) list.get(1);
        assertFalse("nested array 2 is empty", list2.isEmpty());
        assertEquals("size", 2, list2.size());
        assertEquals("1st element", "c", list2.get(0));
        assertEquals("2nd element", "d", list2.get(1));
    }

    @Test
    public void testDictionaryArray()
    {
        final String key = "dictionary-array";

        final Object array = config.getProperty(key);

        // root array
        assertNotNull("array not found", array);
        ObjectAssert.assertInstanceOf("the array element is not parsed as a List", List.class, array);
        final List<?> list = config.getList(key);

        assertFalse("empty array", list.isEmpty());
        assertEquals("size", 2, list.size());

        // 1st dictionary
        ObjectAssert.assertInstanceOf("the dict element is not parsed as a Configuration", Configuration.class, list.get(0));
        final Configuration conf1 = (Configuration) list.get(0);
        assertFalse("configuration 1 is empty", conf1.isEmpty());
        assertEquals("configuration element", "bar", conf1.getProperty("foo"));

        // 2nd dictionary
        ObjectAssert.assertInstanceOf("the dict element is not parsed as a Configuration", Configuration.class, list.get(1));
        final Configuration conf2 = (Configuration) list.get(1);
        assertFalse("configuration 2 is empty", conf2.isEmpty());
        assertEquals("configuration element", "value", conf2.getProperty("key"));
    }

    @Test
    public void testNested()
    {
        assertEquals("nested property", "value", config.getString("nested.node1.node2.node3"));
    }

    @Test
    public void testSave() throws Exception
    {
        final File savedFile = folder.newFile();

        // add an array of strings to the configuration
        /*
        config.addProperty("string", "value1");
        List list = new ArrayList();
        for (int i = 1; i < 5; i++)
        {
            list.add("value" + i);
        }
        config.addProperty("newarray", list);*/
        // todo : investigate why the array structure of 'newarray' is lost in the saved file

        // add a map of strings
        /*
        Map map = new HashMap();
        map.put("foo", "bar");
        map.put("int", new Integer(123));
        config.addProperty("newmap", map);
        */
        // todo : a Map added to a HierarchicalConfiguration should be decomposed as list of nodes

        // save the configuration
        save(savedFile);
        assertTrue("The saved file doesn't exist", savedFile.exists());

        // read the configuration and compare the properties
        final XMLPropertyListConfiguration checkConfig = new XMLPropertyListConfiguration();
        load(checkConfig, savedFile);

        final Iterator<String> it = config.getKeys();
        while (it.hasNext())
        {
            final String key = it.next();
            assertTrue("The saved configuration doesn't contain the key '" + key + "'", checkConfig.containsKey(key));

            final Object value = checkConfig.getProperty(key);
            if (value instanceof byte[])
            {
                final byte[] array = (byte[]) value;
                ArrayAssert.assertEquals("Value of the '" + key + "' property", (byte[]) config.getProperty(key), array);
            }
            else if (value instanceof List)
            {
                final List<?> list1 = (List<?>) config.getProperty(key);
                final List<?> list2 = (List<?>) value;

                assertEquals("The size of the list for the key '" + key + "' doesn't match", list1.size(), list2.size());

                for (int i = 0; i < list2.size(); i++)
                {
                    final Object value1 = list1.get(i);
                    final Object value2 = list2.get(i);

                    if (value1 instanceof Configuration)
                    {
                        final ConfigurationComparator comparator = new StrictConfigurationComparator();
                        assertTrue("The dictionnary at index " + i + " for the key '" + key + "' doesn't match", comparator.compare((Configuration) value1, (Configuration) value2));
                    }
                    else
                    {
                        assertEquals("Element at index " + i + " for the key '" + key + "'", value1, value2);
                    }
                }

                ListAssert.assertEquals("Value of the '" + key + "' property", (List<?>) config.getProperty(key), list1);
            }
            else
            {
                assertEquals("Value of the '" + key + "' property", config.getProperty(key), checkConfig.getProperty(key));
            }

        }
    }

    @Test
    public void testSaveEmptyDictionary() throws Exception
    {
        final File savedFile = folder.newFile();

        // save the configuration
        save(savedFile);
        assertTrue("The saved file doesn't exist", savedFile.exists());

        // read the configuration and compare the properties
        final XMLPropertyListConfiguration checkConfig = new XMLPropertyListConfiguration();
        load(checkConfig, savedFile);

        assertEquals(null, config.getProperty("empty-dictionary"));
        assertEquals(null, checkConfig.getProperty("empty-dictionary"));
    }

    /**
     * Ensure that setProperty doesn't alter an array of byte
     * since it's a first class type in plist file
     */
    @Test
    public void testSetDataProperty() throws Exception
    {
        final File savedFile = folder.newFile();
        final byte[] expected = new byte[]{1, 2, 3, 4};
        config = new XMLPropertyListConfiguration();
        config.setProperty("foo", expected);
        save(savedFile);

        final XMLPropertyListConfiguration config2 = new XMLPropertyListConfiguration();
        load(config2, savedFile);
        final Object array = config2.getProperty("foo");

        assertNotNull("data not found", array);
        assertEquals("property type", byte[].class, array.getClass());
        ArrayAssert.assertEquals(expected, (byte[]) array);
    }

    /**
     * Ensure that addProperty doesn't alter an array of byte
     */
    @Test
    public void testAddDataProperty() throws Exception
    {
        final File savedFile = folder.newFile();
        final byte[] expected = new byte[]{1, 2, 3, 4};
        config = new XMLPropertyListConfiguration();
        config.addProperty("foo", expected);
        save(savedFile);

        final XMLPropertyListConfiguration config2 = new XMLPropertyListConfiguration();
        load(config2, savedFile);
        final Object array = config2.getProperty("foo");

        assertNotNull("data not found", array);
        assertEquals("property type", byte[].class, array.getClass());
        ArrayAssert.assertEquals(expected, (byte[]) array);
    }

    @Test
    public void testInitCopy()
    {
        final XMLPropertyListConfiguration copy = new XMLPropertyListConfiguration(config);
        final StrictConfigurationComparator comp = new StrictConfigurationComparator();
        assertTrue("Configurations are not equal", comp.compare(config, copy));
    }

    /**
     * Tests whether a configuration can be loaded that does not start with a
     * {@code dict} element. This test case is related to
     * CONFIGURATION-405.
     */
    @Test
    public void testLoadNoDict() throws ConfigurationException
    {
        final XMLPropertyListConfiguration plist = new XMLPropertyListConfiguration();
        load(plist, ConfigurationAssert.getTestFile("test2.plist.xml"));
        assertFalse("Configuration is empty", plist.isEmpty());
    }

    /**
     * Tests whether a configuration that does not start with a
     * {@code dict} element can be loaded from a constructor. This test
     * case is related to CONFIGURATION-405.
     */
    @Test
    public void testLoadNoDictConstr() throws ConfigurationException
    {
        final XMLPropertyListConfiguration plist = new XMLPropertyListConfiguration();
        load(plist, ConfigurationAssert.getTestFile("test2.plist.xml"));
        assertFalse("Configuration is empty", plist.isEmpty());
    }

    /**
     * Tests a configuration file which contains an invalid date property value.
     * This test is related to CONFIGURATION-501.
     */
    @Test
    public void testSetDatePropertyInvalid() throws ConfigurationException
    {
        config.clear();
        load(config, ConfigurationAssert.getTestFile("test_invalid_date.plist.xml"));
        assertEquals("'string' property", "value1", config.getString("string"));
        assertFalse("Date property was loaded", config.containsKey("date"));
    }

    /**
     * Tests the header of a saved file if no encoding is specified.
     */
    @Test
    public void testSaveNoEncoding() throws ConfigurationException
    {
        final StringWriter writer = new StringWriter();
        new FileHandler(config).save(writer);
        assertTrue("Wrong document header",
                writer.toString().indexOf("<?xml version=\"1.0\"?>") >= 0);
    }

    /**
     * Tests whether the encoding is written when saving a configuration.
     */
    @Test
    public void testSaveWithEncoding() throws ConfigurationException
    {
        final String encoding = "UTF-8";
        final FileHandler handler = new FileHandler(config);
        handler.setEncoding(encoding);
        final StringWriter writer = new StringWriter();
        handler.save(writer);
        assertTrue(
                "Encoding not found",
                writer.toString()
                        .indexOf(
                                "<?xml version=\"1.0\" encoding=\"" + encoding
                                        + "\"?>") >= 0);
    }

    /**
     * Checks whether the test configuration contains a key with an array value.
     *
     * @param expectedValues the expected values
     */
    private void checkArrayProperty(final List<?> expectedValues)
            throws ConfigurationException
    {
        final StringWriter out = new StringWriter();
        new FileHandler(config).save(out);
        final StringBuilder values = new StringBuilder();
        for (final Object v : expectedValues)
        {
            values.append("<string>").append(v).append("</string>");
        }
        final String content = out.toString().replaceAll("[ \n\r]", "");
        assertThat(content, containsString(String.format(
                "<key>array</key><array>%s</array>", values)));
    }

    /**
     * Tests whether a list can be saved correctly. This test is related to
     * CONFIGURATION-427.
     */
    @Test
    public void testSaveList() throws ConfigurationException
    {
        final List<String> elems =
                Arrays.asList("element1", "element2", "anotherElement");
        config = new XMLPropertyListConfiguration();
        config.addProperty("array", elems);

        checkArrayProperty(elems);
    }

    /**
     * Tests whether an array can be saved correctly. This test is related to
     * CONFIGURATION-427.
     */
    @Test
    public void testSaveArray() throws ConfigurationException
    {
        final Object[] elems = {
                "arrayElem1", "arrayElem2", "arrayElem3"
        };
        config = new XMLPropertyListConfiguration();
        config.addProperty("array", elems);

        checkArrayProperty(Arrays.asList(elems));
    }

    /**
     * Tests a direct invocation of the write() method. This test is
     * related to CONFIGURATION-641.
     */
    @Test
    public void testWriteCalledDirectly() throws IOException
    {
        config = new XMLPropertyListConfiguration();
        config.addProperty("foo", "bar");

        final Writer out = new FileWriter(folder.newFile());
        try
        {
            config.write(out);
            fail("No exception thrown!");
        }
        catch (final ConfigurationException e)
        {
            assertThat(e.getMessage(), containsString("FileHandler"));
        }
        finally
        {
            out.close();
        }
    }
}
