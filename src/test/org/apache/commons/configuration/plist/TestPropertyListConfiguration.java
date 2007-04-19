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

package org.apache.commons.configuration.plist;

import java.io.File;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Date;

import junit.framework.TestCase;
import junitx.framework.ArrayAssert;
import junitx.framework.ListAssert;
import junitx.framework.ObjectAssert;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationComparator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.StrictConfigurationComparator;

/**
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class TestPropertyListConfiguration extends TestCase
{
    private PropertyListConfiguration config;

    private String testProperties = new File("conf/test.plist").getAbsolutePath();

    protected void setUp() throws Exception
    {
        config = new PropertyListConfiguration();
        config.setFileName(testProperties);
        config.load();
    }

    public void testLoad()
    {
        assertFalse("the configuration is empty", config.isEmpty());
    }

    public void testLoadWithError()
    {
        config = new PropertyListConfiguration();
        try {
            config.load(new StringReader(""));
            fail("No exception thrown on loading an empty file");
        } catch (ConfigurationException e) {
            // expected
            assertNotNull(e.getMessage());
        }
    }

    public void testString()
    {
        assertEquals("simple-string", "string1", config.getProperty("simple-string"));
    }

    public void testQuotedString()
    {
        assertEquals("quoted-string", "string2", config.getProperty("quoted-string"));
        assertEquals("quoted-string2", "this is a string", config.getProperty("quoted-string2"));
        assertEquals("complex-string", "this is a \"complex\" string {(=,;)}", config.getProperty("complex-string"));
    }

    public void testEmptyArray()
    {
        String key = "empty-array";
        assertNotNull("array null", config.getProperty(key));

        List list = (List) config.getProperty(key);
        assertTrue("array is not empty", list.isEmpty());
    }

    public void testArray()
    {
        String key = "array";
        assertNotNull("array null", config.getProperty(key));

        List list = (List) config.getProperty(key);
        assertFalse("array is empty", list.isEmpty());

        assertEquals("1st value", "value1", list.get(0));
        assertEquals("2nd value", "value2", list.get(1));
        assertEquals("3rd value", "value3", list.get(2));
    }

    public void testNestedArrays()
    {
        String key = "nested-arrays";

        Object array = config.getProperty(key);

        // root array
        assertNotNull("array not found", array);
        ObjectAssert.assertInstanceOf("the array element is not parsed as a List", List.class, array);
        List list = config.getList(key);

        assertFalse("empty array", list.isEmpty());
        assertEquals("size", 2, list.size());

        // 1st array
        ObjectAssert.assertInstanceOf("the array element is not parsed as a List", List.class, list.get(0));
        List list1 = (List) list.get(0);
        assertFalse("nested array 1 is empty", list1.isEmpty());
        assertEquals("size", 2, list1.size());
        assertEquals("1st element", "a", list1.get(0));
        assertEquals("2nd element", "b", list1.get(1));

        // 2nd array
        ObjectAssert.assertInstanceOf("the array element is not parsed as a List", List.class, list.get(1));
        List list2 = (List) list.get(1);
        assertFalse("nested array 2 is empty", list2.isEmpty());
        assertEquals("size", 2, list2.size());
        assertEquals("1st element", "c", list2.get(0));
        assertEquals("2nd element", "d", list2.get(1));
    }

    public void testDictionary()
    {
        assertEquals("1st element in dictionary", "bar1", config.getProperty("dictionary.foo1"));
        assertEquals("2nd element in dictionary", "bar2", config.getProperty("dictionary.foo2"));
    }

    public void testDictionaryArray()
    {
        String key = "dictionary-array";

        Object array = config.getProperty(key);

        // root array
        assertNotNull("array not found", array);
        ObjectAssert.assertInstanceOf("the array element is not parsed as a List", List.class, array);
        List list = config.getList(key);

        assertFalse("empty array", list.isEmpty());
        assertEquals("size", 2, list.size());

        // 1st dictionary
        ObjectAssert.assertInstanceOf("the dict element is not parsed as a Configuration", Configuration.class, list.get(0));
        Configuration conf1 = (Configuration) list.get(0);
        assertFalse("configuration 1 is empty", conf1.isEmpty());
        assertEquals("configuration element", "bar", conf1.getProperty("foo"));

        // 2nd dictionary
        ObjectAssert.assertInstanceOf("the dict element is not parsed as a Configuration", Configuration.class, list.get(1));
        Configuration conf2 = (Configuration) list.get(1);
        assertFalse("configuration 2 is empty", conf2.isEmpty());
        assertEquals("configuration element", "value", conf2.getProperty("key"));
    }

    public void testNestedDictionaries()
    {
        assertEquals("nested property", "value", config.getString("nested-dictionaries.foo.bar.key"));
    }

    public void testData()
    {
        ObjectAssert.assertInstanceOf("data", (new byte[0]).getClass(), config.getProperty("data"));
        ArrayAssert.assertEquals("data", "foo bar".getBytes(), (byte[]) config.getProperty("data"));
    }

    public void testDate() throws Exception
    {
        Date date = PropertyListConfiguration.DATE_FORMAT.parse("2002-03-22 11:30:00 +0100");

        assertEquals("date", date, config.getProperty("date"));        
    }

    public void testSave() throws Exception
    {
        File savedFile = new File("target/testsave.plist");

        // remove the file previously saved if necessary
        if (savedFile.exists())
        {
            assertTrue(savedFile.delete());
        }

        // save the configuration
        String filename = savedFile.getAbsolutePath();
        config.save(filename);

        assertTrue("The saved file doesn't exist", savedFile.exists());

        // read the configuration and compare the properties
        Configuration checkConfig = new PropertyListConfiguration(new File(filename));

        Iterator it = config.getKeys();
        while (it.hasNext())
        {
            String key = (String) it.next();
            assertTrue("The saved configuration doesn't contain the key '" + key + "'", checkConfig.containsKey(key));

            Object value = checkConfig.getProperty(key);
            if (value instanceof byte[])
            {
                byte[] array = (byte[]) value;
                ArrayAssert.assertEquals("Value of the '" + key + "' property", (byte[]) config.getProperty(key), array);
            }
            else if (value instanceof List)
            {
                List list1 = (List) config.getProperty(key);
                List list2 = (List) value;

                assertEquals("The size of the list for the key '" + key + "' doesn't match", list1.size(), list2.size());

                for (int i = 0; i < list2.size(); i++)
                {
                    Object value1 = list1.get(i);
                    Object value2 = list2.get(i);

                    if (value1 instanceof Configuration)
                    {
                        ConfigurationComparator comparator = new StrictConfigurationComparator();
                        assertTrue("The dictionnary at index " + i + " for the key '" + key + "' doesn't match", comparator.compare((Configuration) value1, (Configuration) value2));
                    }
                    else
                    {
                        assertEquals("Element at index " + i + " for the key '" + key + "'", value1, value2);
                    }
                }

                ListAssert.assertEquals("Value of the '" + key + "' property", (List) config.getProperty(key), list1);
            }
            else
            {
                assertEquals("Value of the '" + key + "' property", config.getProperty(key), checkConfig.getProperty(key));
            }

        }
    }

    public void testQuoteString()
    {
        assertEquals("null string", null, config.quoteString(null));
        assertEquals("simple string", "abcd", config.quoteString("abcd"));
        assertEquals("string with a space", "\"ab cd\"", config.quoteString("ab cd"));
        assertEquals("string with a quote", "\"foo\\\"bar\"", config.quoteString("foo\"bar"));
        assertEquals("string with a special char", "\"foo;bar\"", config.quoteString("foo;bar"));
    }

    /**
     * Ensure that setProperty doesn't alter an array of byte
     * since it's a first class type in plist file
     */
    public void testSetDataProperty() throws Exception
    {
        byte[] expected = new byte[]{1, 2, 3, 4};
        PropertyListConfiguration config = new PropertyListConfiguration();
        config.setProperty("foo", expected);
        config.save("target/testdata.plist");

        PropertyListConfiguration config2 = new PropertyListConfiguration("target/testdata.plist");
        Object array = config2.getProperty("foo");

        assertNotNull("data not found", array);
        assertEquals("property type", byte[].class, array.getClass());
        ArrayAssert.assertEquals(expected, (byte[]) array);
    }

    /**
     * Ensure that addProperty doesn't alter an array of byte
     */
    public void testAddDataProperty() throws Exception
    {
        byte[] expected = new byte[]{1, 2, 3, 4};
        PropertyListConfiguration config = new PropertyListConfiguration();
        config.addProperty("foo", expected);
        config.save("target/testdata.plist");

        PropertyListConfiguration config2 = new PropertyListConfiguration("target/testdata.plist");
        Object array = config2.getProperty("foo");

        assertNotNull("data not found", array);
        assertEquals("property type", byte[].class, array.getClass());
        ArrayAssert.assertEquals(expected, (byte[]) array);
    }

    public void testInitCopy()
    {
    	PropertyListConfiguration copy = new PropertyListConfiguration(config);
    	assertFalse("Nothing was copied", copy.isEmpty());
    }
}
