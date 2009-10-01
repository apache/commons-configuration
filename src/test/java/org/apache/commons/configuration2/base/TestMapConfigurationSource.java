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
package org.apache.commons.configuration2.base;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.easymock.EasyMock;

/**
 * Test class for {@code MapConfigurationSource}.
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class TestMapConfigurationSource extends TestCase
{
    /** Constant for a test key. */
    private static final String KEY = "testKey";

    /** Constant for the number of test keys. */
    private static final int COUNT = 8;

    /**
     * Helper method for checking a list property. Tests whether the property
     * value is a list that contains exactly the given elements.
     *
     * @param value the value
     * @param expected the expected values
     */
    private static void checkList(Object value, Object... expected)
    {
        assertTrue("Not a list: " + value, value instanceof List<?>);
        List<?> lst = (List<?>) value;
        assertEquals("Wrong size", expected.length, lst.size());
        for (int i = 0; i < expected.length; i++)
        {
            assertEquals("Wrong value at " + i, expected[i], lst.get(i));
        }
    }

    /**
     * Creates a map with test data. The map contains {@link #COUNT} keys
     * starting with the prefix {@link #KEY} and a running index. Their values
     * are their indices.
     *
     * @return the map with test data
     */
    private static Map<String, Object> setUpMap()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < COUNT; i++)
        {
            map.put(KEY + i, i);
        }
        return map;
    }

    /**
     * Tries to create an instance without a map. This should cause an
     * exception.
     */
    public void testInitNoMap()
    {
        try
        {
            new MapConfigurationSource((Map<String, Object>) null);
            fail("Could create instance without a map!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests whether an instance can be created from an existing map.
     */
    public void testInitMap()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < COUNT; i++)
        {
            map.put(KEY + i, i);
        }
        MapConfigurationSource src = new MapConfigurationSource(map);
        assertSame("Wrong map as store", map, src.getStore());
        for (int i = 0; i < COUNT; i++)
        {
            assertEquals("Wrong property value", Integer.valueOf(i), src
                    .getProperty(KEY + i));
        }
    }

    /**
     * Tests the default constructor.
     */
    public void testInitDefault()
    {
        MapConfigurationSource src = new MapConfigurationSource();
        Map<String, Object> store = src.getStore();
        assertTrue("Store not empty", src.isEmpty());
        assertTrue("Wrong map class: " + store,
                store instanceof LinkedHashMap<?, ?>);
    }

    /**
     * Tests adding a single property value.
     */
    public void testAddPropertySingleValue()
    {
        MapConfigurationSource src = new MapConfigurationSource();
        final Integer value = 1;
        src.addProperty(KEY, value);
        assertEquals("Value not in store", value, src.getStore().get(KEY));
        assertEquals("Wrong property value", value, src.getProperty(KEY));
    }

    /**
     * Tests whether multiple values for a property can be added.
     */
    public void testAddPropertyMultipleValues()
    {
        MapConfigurationSource src = new MapConfigurationSource();
        src.addProperty(KEY, 1);
        src.addProperty(KEY, 2);
        checkList(src.getStore().get(KEY), 1, 2);
        src.addProperty(KEY, 3);
        checkList(src.getStore().get(KEY), 1, 2, 3);
    }

    /**
     * Tests whether the whole source can be cleared.
     */
    public void testClear()
    {
        Map<String, Object> map = setUpMap();
        MapConfigurationSource src = new MapConfigurationSource(map);
        src.clear();
        assertTrue("Store not cleared", map.isEmpty());
    }

    /**
     * Tests whether a property can be removed.
     */
    public void testClearProperty()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(KEY, 42);
        MapConfigurationSource src = new MapConfigurationSource(map);
        src.clearProperty(KEY);
        assertFalse("Property not removed", map.containsKey(KEY));
    }

    /**
     * Tests the containsKey() implementation.
     */
    public void testContainsKey()
    {
        Map<String, Object> map = setUpMap();
        MapConfigurationSource src = new MapConfigurationSource(map);
        for (String k : map.keySet())
        {
            assertTrue("Key not found: " + k, src.containsKey(k));
        }
    }

    /**
     * Tests containsKey() for a non-existing key.
     */
    public void testContainsKeyNonExisting()
    {
        MapConfigurationSource src = new MapConfigurationSource();
        assertFalse("Found non-existing key", src.containsKey(KEY));
    }

    /**
     * Tests whether all keys of the source can be retrieved.
     */
    public void testGetKeys()
    {
        Map<String, Object> map = setUpMap();
        MapConfigurationSource src = new MapConfigurationSource(map);
        Set<String> keys = new HashSet<String>(map.keySet());
        for (Iterator<String> it = src.getKeys(); it.hasNext();)
        {
            String k = it.next();
            assertTrue("Unexpected key: " + k, keys.remove(k));
        }
        assertTrue("Remaining keys: " + keys, keys.isEmpty());
    }

    /**
     * Tests whether the order of keys is retained.
     */
    public void testGetKeysOrdered()
    {
        MapConfigurationSource src = new MapConfigurationSource();
        for (int i = 0; i < COUNT; i++)
        {
            src.addProperty(KEY + i, i);
        }
        Iterator<String> it = src.getKeys();
        for (int i = 0; i < COUNT; i++)
        {
            assertEquals("Wrong key at " + i, KEY + i, it.next());
        }
        assertFalse("Too many keys", it.hasNext());
    }

    /**
     * Tests the getKeys() method that expects a prefix. This method is not
     * implemented.
     */
    public void testGetKeysPrefix()
    {
        MapConfigurationSource src = new MapConfigurationSource(setUpMap());
        try
        {
            src.getKeys(KEY);
            fail("Could obtain keys with prefix!");
        }
        catch (UnsupportedOperationException uoex)
        {
            // ok
        }
    }

    /**
     * Tests getProperty() for a non-existing property.
     */
    public void testGetPropertyNonExisting()
    {
        MapConfigurationSource src = new MapConfigurationSource();
        assertNull("Got value for non-existing property", src.getProperty(KEY));
    }

    /**
     * Tests the isEmpty() implementation.
     */
    public void testIsEmpty()
    {
        Map<String, Object> map = setUpMap();
        MapConfigurationSource src = new MapConfigurationSource(map);
        assertFalse("Source is empty", src.isEmpty());
        map.clear();
        assertTrue("Source not empty", src.isEmpty());
    }

    /**
     * Tests setProperty() if the property does not exist yet.
     */
    public void testSetPropertyNew()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        MapConfigurationSource src = new MapConfigurationSource(map);
        final Object value = "a test value";
        src.setProperty(KEY, value);
        assertEquals("Property not set", value, map.get(KEY));
        assertEquals("Too many values set", 1, map.size());
    }

    /**
     * Tests whether an existing property can be overridden.
     */
    public void testSetPropertyOverride()
    {
        Map<String, Object> map = setUpMap();
        MapConfigurationSource src = new MapConfigurationSource(map);
        final String key = KEY + "1";
        final Object value = "the new value";
        src.setProperty(key, value);
        assertEquals("Value not changed", value, map.get(key));
        assertEquals("Number of properties changed", COUNT, map.size());
    }

    /**
     * Tests whether the size of the source can be queried.
     */
    public void testSize()
    {
        MapConfigurationSource src = new MapConfigurationSource(setUpMap());
        assertEquals("Wrong size", COUNT, src.size());
    }

    /**
     * Tests the valueCount() implementation. This method is not supported, so
     * an exception should be thrown.
     */
    public void testValueCount()
    {
        MapConfigurationSource src = new MapConfigurationSource(setUpMap());
        try
        {
            src.valueCount(KEY);
            fail("Could invoke valueCount()!");
        }
        catch (UnsupportedOperationException uex)
        {
            // ok
        }
    }

    /**
     * Tries to add an event listener. This is not supported, so an exception
     * should be thrown.
     */
    public void testAddConfigurationSourceListener()
    {
        ConfigurationSourceListener l = EasyMock
                .createNiceMock(ConfigurationSourceListener.class);
        MapConfigurationSource src = new MapConfigurationSource();
        try
        {
            src.addConfigurationSourceListener(l);
            fail("Could add an event listener!");
        }
        catch (UnsupportedOperationException uex)
        {
            // ok
        }
    }

    /**
     * Tries to remove an event listener. Event listeners are not supported, so
     * this should cause an exception.
     */
    public void testRemoveConfigurationSourceListener()
    {
        ConfigurationSourceListener l = EasyMock
                .createNiceMock(ConfigurationSourceListener.class);
        MapConfigurationSource src = new MapConfigurationSource();
        try
        {
            src.removeConfigurationSourceListener(l);
            fail("Could remove an event listener!");
        }
        catch (UnsupportedOperationException uex)
        {
            // ok
        }
    }

    /**
     * Tests whether a copy of a source can be created and the copy contains the
     * same properties.
     */
    public void testInitCopy()
    {
        MapConfigurationSource src = new MapConfigurationSource(setUpMap());
        MapConfigurationSource copy = new MapConfigurationSource(src);
        assertEquals("Wrong number of properties", COUNT, copy.size());
        for (int i = 0; i < COUNT; i++)
        {
            String key = KEY + i;
            assertEquals("Wrong property for " + key, i, copy.getProperty(key));
        }
    }

    /**
     * Tests whether changing the original or the copy does not affect the other
     * object.
     */
    public void testInitCopyModify()
    {
        MapConfigurationSource src = new MapConfigurationSource(setUpMap());
        MapConfigurationSource copy = new MapConfigurationSource(src);
        src.addProperty("original", Boolean.TRUE);
        src.addProperty("clone", Boolean.FALSE);
        copy.addProperty("original", Boolean.FALSE);
        copy.addProperty("clone", Boolean.TRUE);
        assertEquals("Wrong original property in original", Boolean.TRUE, src
                .getProperty("original"));
        assertEquals("Wrong clone property in original", Boolean.FALSE, src
                .getProperty("clone"));
        assertEquals("Wrong original property in clone", Boolean.FALSE, copy
                .getProperty("original"));
        assertEquals("Wrong clone property in clone", Boolean.TRUE, copy
                .getProperty("clone"));
    }

    /**
     * Tests the copy constructor if list properties are involved.
     */
    public void testInitCopyListProperty()
    {
        MapConfigurationSource src = new MapConfigurationSource(setUpMap());
        src.addProperty(KEY, 1);
        src.addProperty(KEY, 2);
        MapConfigurationSource copy = new MapConfigurationSource(src);
        copy.addProperty(KEY, 3);
        checkList(src.getProperty(KEY), 1, 2);
        checkList(copy.getProperty(KEY), 1, 2, 3);
    }

    /**
     * Tries to invoke the copy constructor with a null source. This should
     * cause an exception.
     */
    public void testInitCopyNullSource()
    {
        try
        {
            new MapConfigurationSource((FlatConfigurationSource) null);
            fail("Could create copy of null source!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests whether the source can be serialized.
     */
    public void testSerialization() throws IOException, ClassNotFoundException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        MapConfigurationSource src = new MapConfigurationSource(setUpMap());
        src.addProperty(KEY, "value1");
        src.addProperty(KEY, "value2");
        oos.writeObject(src);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                bos.toByteArray()));
        MapConfigurationSource src2 = (MapConfigurationSource) ois.readObject();
        ois.close();
        assertEquals("Wrong number of properties", src.size(), src2.size());
        for (Iterator<String> it = src.getKeys(); it.hasNext();)
        {
            String key = it.next();
            assertEquals("Wrong value for property " + key, src
                    .getProperty(key), src2.getProperty(key));
        }
    }
}
