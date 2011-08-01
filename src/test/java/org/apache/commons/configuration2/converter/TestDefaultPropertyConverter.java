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

package org.apache.commons.configuration2.converter;

import java.lang.annotation.ElementType;
import java.math.BigDecimal;

import org.apache.commons.configuration2.ConversionException;

import junit.framework.TestCase;

/**
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class TestDefaultPropertyConverter extends TestCase
{
    /** The enum class we use in tests for enum conversions.*/
    private static Class<ElementType> ENUM_CLASS = ElementType.class;

    /** An enumeration object used for testing conversions with enums.*/
    private static ElementType ENUM_OBJECT = ElementType.METHOD;

    private static Converter converter = new DefaultPropertyConverter();
    
    /**
     * Tests conversion to numbers when the passed in objects are already
     * numbers.
     */
    public void testToNumberDirect()
    {
        Integer i = 42;
        assertSame("Wrong integer", i, converter.convert(Integer.class, i));
        BigDecimal d = new BigDecimal("3.1415");
        assertSame("Wrong BigDecimal", d, converter.convert(Number.class, d));
    }

    /**
     * Tests conversion to numbers when the passed in objects have a compatible
     * string representation.
     */
    public void testToNumberFromString()
    {
        assertEquals("Incorrect Integer value", new Integer(42), converter.convert(Integer.class, "42"));
        assertEquals("Incorrect Short value", new Short((short) 10), converter.convert(Short.class, new StringBuilder("10")));
    }

    /**
     * Tests conversion to numbers when the passed in objects are strings with
     * prefixes for special radices.
     */
    public void testToNumberFromHexString()
    {
        Number n = converter.convert(Integer.class, "0x10");
        assertEquals("Incorrect Integer value", 16, n.intValue());
    }

    /**
     * Tests conversion to numbers when an invalid Hex value is passed in. This
     * should cause an exception.
     */
    public void testToNumberFromInvalidHexString()
    {
        try
        {
            converter.convert(Integer.class, "0xNotAHexValue");
            fail("Could convert invalid hex value!");
        }
        catch (ConversionException cex)
        {
            // ok
        }
    }

    /**
     * Tests conversion to numbers when the passed in objects have no numeric
     * String representation. This should cause an exception.
     */
    public void testToNumberFromInvalidString()
    {
        try
        {
            converter.convert(Byte.class, "Not a number");
            fail("Could convert invalid String!");
        }
        catch (ConversionException cex)
        {
            // ok
        }
    }

    public void testToEnumFromEnum()
    {
        assertEquals(ENUM_OBJECT, converter.convert(ENUM_CLASS, ENUM_OBJECT));
    }

    public void testToEnumFromString()
    {
        assertEquals(ENUM_OBJECT, converter.convert(ENUM_CLASS, "METHOD"));
    }

    public void testToEnumFromInvalidString()
    {
        try
        {
            converter.convert(ENUM_CLASS, "FOO");
            fail("Could convert invalid String!");
        }
        catch (ConversionException e)
        {
            // expected
        }
    }

    public void testToEnumFromNumber()
    {
        assertEquals(ENUM_OBJECT, converter.convert(ENUM_CLASS, 2));
    }

    public void testToEnumFromInvalidNumber()
    {
        try
        {
            converter.convert(ENUM_CLASS, -1);
            fail("Could convert invalid number!");
        }
        catch (ConversionException e)
        {
            // expected
        }
    }
}
