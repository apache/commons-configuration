package org.apache.commons.configuration;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import junit.framework.TestCase;

/**
 * Test if non-string properties are handled correctly.
 *
 * @version $Id$
 */
public abstract class BaseNonStringProperties extends TestCase
{

    protected NonStringTestHolder nonStringTestHolder = new NonStringTestHolder();
    public abstract void setUp() throws Exception;

    public Configuration conf = null;

    public void testBoolean() throws Exception
    {
        nonStringTestHolder.testBoolean();
    }

    public void testBooleanDefaultValue() throws Exception
    {
        nonStringTestHolder.testBooleanDefaultValue();
    }

    public void testBooleanArrayValue() throws Exception
    {
        boolean booleanValue = conf.getBoolean("test.boolean");
        assertEquals(true, booleanValue);
        assertEquals(2, conf.getList("test.boolean.array").size());
    }

    public void testByte() throws Exception
    {
        nonStringTestHolder.testByte();
    }

    public void testByteArrayValue() throws Exception
    {
        byte testValue = 10;
        byte byteValue = conf.getByte("test.byte");
        assertEquals(testValue, byteValue);
        assertEquals(2, conf.getList("test.byte.array").size());
    }

    public void testDouble() throws Exception
    {
        nonStringTestHolder.testDouble();
    }

    public void testDoubleDefaultValue() throws Exception
    {
        nonStringTestHolder.testDoubleDefaultValue();
    }

    public void testDoubleArrayValue() throws Exception
    {
        double testValue = 10.25;
        double doubleValue = conf.getDouble("test.double");
        assertEquals(testValue, doubleValue, 0.01);
        assertEquals(2, conf.getList("test.double.array").size());
    }

    public void testFloat() throws Exception
    {
        nonStringTestHolder.testFloat();
    }

    public void testFloatDefaultValue() throws Exception
    {
        nonStringTestHolder.testFloatDefaultValue();

    }

    public void testFloatArrayValue() throws Exception
    {
        float testValue = (float) 20.25;
        float floatValue = conf.getFloat("test.float");
        assertEquals(testValue, floatValue, 0.01);
        assertEquals(2, conf.getList("test.float.array").size());
    }

    public void testInteger() throws Exception
    {
        nonStringTestHolder.testInteger();
    }

    public void testIntegerDefaultValue() throws Exception
    {
        nonStringTestHolder.testIntegerDefaultValue();
    }

    public void testIntegerArrayValue() throws Exception
    {
        int intValue = conf.getInt("test.integer");
        assertEquals(10, intValue);
        assertEquals(2, conf.getList("test.integer.array").size());
    }

    public void testLong() throws Exception
    {
        nonStringTestHolder.testLong();
    }
    public void testLongDefaultValue() throws Exception
    {
        nonStringTestHolder.testLongDefaultValue();
    }
    public void testLongArrayValue() throws Exception
    {
        long longValue = conf.getLong("test.long");
        assertEquals(1000000, longValue);
        assertEquals(2, conf.getList("test.long.array").size());
    }

    public void testShort() throws Exception
    {
        nonStringTestHolder.testShort();
    }

    public void testShortDefaultValue() throws Exception
    {
        nonStringTestHolder.testShortDefaultValue();
    }
    public void testShortArrayValue() throws Exception
    {
        short shortValue = conf.getShort("test.short");
        assertEquals(1, shortValue);
        assertEquals(2, conf.getList("test.short.array").size());
    }

    public void testListMissing() throws Exception
    {
        nonStringTestHolder.testListMissing();
    }

    public void testSubset() throws Exception
    {
        nonStringTestHolder.testSubset();
    }
    public void testIsEmpty() throws Exception
    {
        nonStringTestHolder.testIsEmpty();
    }
}
