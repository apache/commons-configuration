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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.List;

/**
 * Pulling out the calls to do the tests so both JUnit and Cactus tests can share.
 */
public class NonStringTestHolder {
    private Configuration configuration;

    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    public void testBoolean() throws Exception {
        final boolean booleanValue = configuration.getBoolean("test.boolean");
        assertTrue(booleanValue);
        assertEquals(1, configuration.getList("test.boolean").size());
    }

    public void testBooleanDefaultValue() throws Exception {
        final boolean booleanValue = configuration.getBoolean("test.boolean.missing", true);
        assertTrue(booleanValue);

        final Boolean booleanObject = configuration.getBoolean("test.boolean.missing", Boolean.valueOf(true));
        assertEquals(Boolean.valueOf(true), booleanObject);
    }

    public void testByte() throws Exception {
        final byte testValue = 10;
        final byte byteValue = configuration.getByte("test.byte");
        assertEquals(testValue, byteValue);
        assertEquals(1, configuration.getList("test.byte").size());
    }

    public void testDouble() throws Exception {
        final double testValue = 10.25;
        final double doubleValue = configuration.getDouble("test.double");
        assertEquals(testValue, doubleValue, 0.01);
        assertEquals(1, configuration.getList("test.double").size());
    }

    public void testDoubleDefaultValue() throws Exception {
        final double testValue = 10.25;
        final double doubleValue = configuration.getDouble("test.double.missing", 10.25);
        assertEquals(testValue, doubleValue, 0.01);
    }

    public void testFloat() throws Exception {
        final float testValue = (float) 20.25;
        final float floatValue = configuration.getFloat("test.float");
        assertEquals(testValue, floatValue, 0.01);
        assertEquals(1, configuration.getList("test.float").size());
    }

    public void testFloatDefaultValue() throws Exception {
        final float testValue = (float) 20.25;
        final float floatValue = configuration.getFloat("test.float.missing", testValue);
        assertEquals(testValue, floatValue, 0.01);
    }

    public void testInteger() throws Exception {
        final int intValue = configuration.getInt("test.integer");
        assertEquals(10, intValue);
        assertEquals(1, configuration.getList("test.integer").size());
    }

    public void testIntegerDefaultValue() throws Exception {
        final int intValue = configuration.getInt("test.integer.missing", 10);
        assertEquals(10, intValue);
    }

    public void testIsEmpty() throws Exception {
        assertFalse(configuration.isEmpty());
    }

    public void testListMissing() throws Exception {
        final List<?> list = configuration.getList("missing.list");
        assertTrue(list.isEmpty());
    }

    public void testLong() throws Exception {
        final long longValue = configuration.getLong("test.long");
        assertEquals(1000000, longValue);
        assertEquals(1, configuration.getList("test.long").size());
    }

    public void testLongDefaultValue() throws Exception {
        final long longValue = configuration.getLong("test.long.missing", 1000000);
        assertEquals(1000000, longValue);
    }

    public void testShort() throws Exception {
        final short shortValue = configuration.getShort("test.short");
        assertEquals(1, shortValue);
        assertEquals(1, configuration.getList("test.short").size());
    }

    public void testShortDefaultValue() throws Exception {
        final short shortValue = configuration.getShort("test.short.missing", (short) 1);
        assertEquals(1, shortValue);
    }

    public void testSubset() throws Exception {
        final Configuration subset = configuration.subset("test");

        // search the "short" key in the subset using the key iterator
        boolean foundKeyValue = false;
        final Iterator<String> it = subset.getKeys();
        while (it.hasNext() && !foundKeyValue) {
            final String key = it.next();
            foundKeyValue = "short".equals(key);
        }

        assertTrue(foundKeyValue);
    }
}
