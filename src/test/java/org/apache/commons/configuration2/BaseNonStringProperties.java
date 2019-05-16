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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test if non-string properties are handled correctly.
 *
 */
public abstract class BaseNonStringProperties
{

    protected NonStringTestHolder nonStringTestHolder = new NonStringTestHolder();

    protected Configuration conf;

    @Test
    public void testBoolean() throws Exception
    {
        nonStringTestHolder.testBoolean();
    }

    @Test
    public void testBooleanDefaultValue() throws Exception
    {
        nonStringTestHolder.testBooleanDefaultValue();
    }

    @Test
    public void testBooleanArrayValue() throws Exception
    {
        final boolean booleanValue = conf.getBoolean("test.boolean");
        assertTrue(booleanValue);
        assertEquals(2, conf.getList("test.boolean.array").size());
    }

    @Test
    public void testByte() throws Exception
    {
        nonStringTestHolder.testByte();
    }

    @Test
    public void testByteArrayValue() throws Exception
    {
        final byte testValue = 10;
        final byte byteValue = conf.getByte("test.byte");
        assertEquals(testValue, byteValue);
        assertEquals(2, conf.getList("test.byte.array").size());
    }

    @Test
    public void testDouble() throws Exception
    {
        nonStringTestHolder.testDouble();
    }

    @Test
    public void testDoubleDefaultValue() throws Exception
    {
        nonStringTestHolder.testDoubleDefaultValue();
    }

    @Test
    public void testDoubleArrayValue() throws Exception
    {
        final double testValue = 10.25;
        final double doubleValue = conf.getDouble("test.double");
        assertEquals(testValue, doubleValue, 0.01);
        assertEquals(2, conf.getList("test.double.array").size());
    }

    @Test
    public void testFloat() throws Exception
    {
        nonStringTestHolder.testFloat();
    }

    @Test
    public void testFloatDefaultValue() throws Exception
    {
        nonStringTestHolder.testFloatDefaultValue();

    }

    @Test
    public void testFloatArrayValue() throws Exception
    {
        final float testValue = (float) 20.25;
        final float floatValue = conf.getFloat("test.float");
        assertEquals(testValue, floatValue, 0.01);
        assertEquals(2, conf.getList("test.float.array").size());
    }

    @Test
    public void testInteger() throws Exception
    {
        nonStringTestHolder.testInteger();
    }

    @Test
    public void testIntegerDefaultValue() throws Exception
    {
        nonStringTestHolder.testIntegerDefaultValue();
    }

    @Test
    public void testIntegerArrayValue() throws Exception
    {
        final int intValue = conf.getInt("test.integer");
        assertEquals(10, intValue);
        assertEquals(2, conf.getList("test.integer.array").size());
    }

    @Test
    public void testLong() throws Exception
    {
        nonStringTestHolder.testLong();
    }

    @Test
    public void testLongDefaultValue() throws Exception
    {
        nonStringTestHolder.testLongDefaultValue();
    }

    @Test
    public void testLongArrayValue() throws Exception
    {
        final long longValue = conf.getLong("test.long");
        assertEquals(1000000, longValue);
        assertEquals(2, conf.getList("test.long.array").size());
    }

    @Test
    public void testShort() throws Exception
    {
        nonStringTestHolder.testShort();
    }

    @Test
    public void testShortDefaultValue() throws Exception
    {
        nonStringTestHolder.testShortDefaultValue();
    }

    @Test
    public void testShortArrayValue() throws Exception
    {
        final short shortValue = conf.getShort("test.short");
        assertEquals(1, shortValue);
        assertEquals(2, conf.getList("test.short.array").size());
    }

    @Test
    public void testListMissing() throws Exception
    {
        nonStringTestHolder.testListMissing();
    }

    @Test
    public void testSubset() throws Exception
    {
        nonStringTestHolder.testSubset();
    }

    @Test
    public void testIsEmpty() throws Exception
    {
        nonStringTestHolder.testIsEmpty();
    }
}
