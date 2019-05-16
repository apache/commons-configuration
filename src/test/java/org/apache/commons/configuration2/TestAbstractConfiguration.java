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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junitx.framework.ListAssert;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.junit.Test;

/**
 * Abstract TestCase for implementations of {@link AbstractConfiguration}.
 *
 * @author Emmanuel Bourg
 */
public abstract class TestAbstractConfiguration
{
    /**
     * Return an abstract configuration with the following data:<br>
     * <pre>
     * key1 = value1
     * key2 = value2
     * list = value1, value2
     * listesc = value1\\,value2
     * </pre>
     */
    protected abstract AbstractConfiguration getConfiguration();

    /**
     * Return an empty configuration.
     */
    protected abstract AbstractConfiguration getEmptyConfiguration();

    @Test
    public void testGetProperty()
    {
        final Configuration config = getConfiguration();
        assertEquals("key1", "value1", config.getProperty("key1"));
        assertEquals("key2", "value2", config.getProperty("key2"));
        assertNull("key3", config.getProperty("key3"));
    }

    @Test
    public void testList()
    {
        final Configuration config = getConfiguration();

        final List<?> list = config.getList("list");
        assertNotNull("list not found", config.getProperty("list"));
        assertEquals("list size", 2, list.size());
        assertTrue("'value1' is not in the list", list.contains("value1"));
        assertTrue("'value2' is not in the list", list.contains("value2"));
    }

    /**
     * Tests whether the escape character for list delimiters is recocknized and
     * removed.
     */
    @Test
    public void testListEscaped()
    {
        assertEquals("Wrong value for escaped list", "value1,value2",
                getConfiguration().getString("listesc"));
    }

    @Test
    public void testAddPropertyDirect()
    {
        final AbstractConfiguration config = getConfiguration();
        config.addPropertyDirect("key3", "value3");
        assertEquals("key3", "value3", config.getProperty("key3"));

        config.addPropertyDirect("key3", "value4");
        config.addPropertyDirect("key3", "value5");
        final List<Object> list = config.getList("key3");
        assertNotNull("no list found for the 'key3' property", list);

        final List<Object> expected = new ArrayList<>();
        expected.add("value3");
        expected.add("value4");
        expected.add("value5");

        ListAssert.assertEquals("values for the 'key3' property", expected, list);
    }

    @Test
    public void testIsEmpty()
    {
        final Configuration config = getConfiguration();
        assertFalse("the configuration is empty", config.isEmpty());
        assertTrue("the configuration is not empty", getEmptyConfiguration().isEmpty());
    }

    @Test
    public void testSize()
    {
        assertEquals("Wrong size", 4, getConfiguration().size());
    }

    @Test
    public void testSizeEmpty()
    {
        assertEquals("Wrong size of empty configuration", 0, getEmptyConfiguration().size());
    }

    @Test
    public void testContainsKey()
    {
        final Configuration config = getConfiguration();
        assertTrue("key1 not found", config.containsKey("key1"));
        assertFalse("key3 found", config.containsKey("key3"));
    }

    @Test
    public void testClearProperty()
    {
        final Configuration config = getConfiguration();
        config.clearProperty("key2");
        assertFalse("key2 not cleared", config.containsKey("key2"));
    }

    @Test
    public void testGetKeys()
    {
        final Configuration config = getConfiguration();
        final Iterator<String> keys = config.getKeys();

        final List<String> expectedKeys = new ArrayList<>();
        expectedKeys.add("key1");
        expectedKeys.add("key2");
        expectedKeys.add("list");
        expectedKeys.add("listesc");

        assertNotNull("null iterator", keys);
        assertTrue("empty iterator", keys.hasNext());

        final List<String> actualKeys = new ArrayList<>();
        while (keys.hasNext())
        {
            actualKeys.add(keys.next());
        }

        ListAssert.assertEquals("keys", expectedKeys, actualKeys);
    }

    /**
     * Tests accessing the configuration's logger.
     */
    @Test
    public void testSetLogger()
    {
        final AbstractConfiguration config = getEmptyConfiguration();
        assertNotNull("Default logger is null", config.getLogger());
        final ConfigurationLogger log = new ConfigurationLogger(config.getClass());
        config.setLogger(log);
        assertSame("Logger was not set", log, config.getLogger());
    }

    /**
     * Tests the exception message triggered by the conversion to BigInteger.
     * This test is related to CONFIGURATION-357.
     */
    @Test
    public void testGetBigIntegerConversion()
    {
        final Configuration config = getConfiguration();
        try
        {
            config.getBigInteger("key1");
            fail("No conversion exception thrown!");
        }
        catch (final ConversionException cex)
        {
            assertTrue("Key not found in exception message: " + cex, cex
                    .getMessage().contains("'key1'"));
            assertTrue("Target class not found in exception message: " + cex,
                    cex.getMessage().contains(BigInteger.class.getName()));
            assertTrue("Value not found in exception message: " + cex, cex
                    .getMessage().contains(config.getString("key1")));
        }
    }
}
