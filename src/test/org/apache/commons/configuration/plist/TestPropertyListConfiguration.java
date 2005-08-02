/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.TestCase;
import junitx.framework.ObjectAssert;
import junitx.framework.ArrayAssert;
import junitx.framework.ListAssert;
import org.apache.commons.configuration.Configuration;

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
        Configuration checkConfig = new PropertyListConfiguration(filename);
        for (Iterator i = config.getKeys(); i.hasNext();)
        {
            String key = (String) i.next();
            assertTrue("The saved configuration doesn't contain the key '" + key + "'", checkConfig.containsKey(key));

            Object value = checkConfig.getProperty(key);
            if (value instanceof byte[])
            {
                byte[] array = (byte[]) value;
                ArrayAssert.assertEquals("Value of the '" + key + "' property", (byte[]) config.getProperty(key), array);
            }
            else if (value instanceof List)
            {
                List list1 = (List) value;
                ListAssert.assertEquals("Value of the '" + key + "' property", (List) config.getProperty(key), list1);
            }
            else
            {
                assertEquals("Value of the '" + key + "' property", config.getProperty(key), checkConfig.getProperty(key));
            }

        }
    }
}
