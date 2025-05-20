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

package org.apache.commons.configuration2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.junit.jupiter.api.Test;

/**
 * Abstract TestCase for implementations of {@link AbstractConfiguration}.
 */
public abstract class TestAbstractConfiguration {
    /**
     * Gets an abstract configuration with the following data:<br>
     *
     * <pre>
     * key1 = value1
     * key2 = value2
     * list = value1, value2
     * listesc = value1\\,value2
     * </pre>
     */
    protected abstract AbstractConfiguration getConfiguration();

    /**
     * Gets an empty configuration.
     */
    protected abstract AbstractConfiguration getEmptyConfiguration();

    @Test
    public void givenNullIteratorTestContains() {
        final AbstractConfiguration config = getConfiguration();

        assertThrows(NullPointerException.class, () -> config.contains(null, "value1"));
    }

    @Test
    public void givenNullValueTestContains() {
        final AbstractConfiguration config = getConfiguration();
        final Iterator<String> keys = config.getKeys();
        assertFalse(config.contains(keys, null));
        assertFalse(config.contains(keys, ""));
    }

    @Test
    public void testAddPropertyDirect() {
        final AbstractConfiguration config = getConfiguration();
        config.addPropertyDirect("key3", "value3");
        assertEquals("value3", config.getProperty("key3"));

        config.addPropertyDirect("key3", "value4");
        config.addPropertyDirect("key3", "value5");
        final List<Object> list = config.getList("key3");
        assertNotNull(list);

        final List<Object> expected = new ArrayList<>();
        expected.add("value3");
        expected.add("value4");
        expected.add("value5");

        assertEquals(expected, list);
    }

    @Test
    public void testClearProperty() {
        final Configuration config = getConfiguration();
        config.clearProperty("key2");
        assertFalse(config.containsKey("key2"));
    }

    @Test
    public void testContains() {
        final AbstractConfiguration config = getConfiguration();
        assertTrue(config.contains(config.getKeys(), "value1"));
        assertFalse(config.contains(config.getKeys(), "value99999"));
    }

    @Test
    public void testContainsKey() {
        final Configuration config = getConfiguration();
        assertTrue(config.containsKey("key1"));
        assertFalse(config.containsKey("key3"));
    }

    @Test
    public void testContainsValue() {
        final Configuration config = getConfiguration();
        assertFalse(config.containsValue(null));
        assertFalse(config.containsValue(""));
        assertTrue(config.containsValue("value1"));
        assertFalse(config.containsValue("value99999"));
    }

    /**
     * Tests the exception message triggered by the conversion to BigInteger. This test is related to CONFIGURATION-357.
     */
    @Test
    public void testGetBigIntegerConversion() {
        final Configuration config = getConfiguration();
        final ConversionException cex = assertThrows(ConversionException.class, () -> config.getBigInteger("key1"));
        assertTrue(cex.getMessage().contains("'key1'"));
        assertTrue(cex.getMessage().contains(BigInteger.class.getName()));
        assertTrue(cex.getMessage().contains(config.getString("key1")));
    }

    @Test
    public void testGetKeys() {
        final Configuration config = getConfiguration();
        final Iterator<String> keys = config.getKeys();
        final Set<String> expectedKeys = new HashSet<>(Arrays.asList("key1", "key2", "list", "listesc"));
        assertNotNull(keys);
        assertTrue(keys.hasNext());
        final Set<String> actualKeys = new HashSet<>();
        while (keys.hasNext()) {
            actualKeys.add(keys.next());
        }
        assertEquals(actualKeys, expectedKeys, "keys");
    }

    @Test
    public void testGetProperty() {
        final Configuration config = getConfiguration();
        assertEquals("value1", config.getProperty("key1"));
        assertEquals("value2", config.getProperty("key2"));
        assertNull(config.getProperty("key3"));
    }

    @Test
    public void testIsEmpty() {
        final Configuration config = getConfiguration();
        assertFalse(config.isEmpty());
        assertTrue(getEmptyConfiguration().isEmpty());
    }

    @Test
    public void testList() {
        final Configuration config = getConfiguration();

        final List<?> list = config.getList("list");
        assertNotNull(config.getProperty("list"));
        assertEquals(Arrays.asList("value1", "value2"), list);
    }

    /**
     * Tests whether the escape character for list delimiters is recocknized and removed.
     */
    @Test
    public void testListEscaped() {
        assertEquals("value1,value2", getConfiguration().getString("listesc"));
    }

    /**
     * Tests accessing the configuration's logger.
     */
    @Test
    public void testSetLogger() {
        final AbstractConfiguration config = getEmptyConfiguration();
        assertNotNull(config.getLogger());
        final ConfigurationLogger log = new ConfigurationLogger(config.getClass());
        config.setLogger(log);
        assertSame(log, config.getLogger());
    }

    @Test
    public void testSize() {
        assertEquals(4, getConfiguration().size());
    }

    @Test
    public void testSizeEmpty() {
        assertEquals(0, getEmptyConfiguration().size());
    }
}
