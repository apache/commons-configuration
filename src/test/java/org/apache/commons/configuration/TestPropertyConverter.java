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

package org.apache.commons.configuration;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.SystemUtils;

/**
 * Test class for PropertyConverter.
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class TestPropertyConverter extends TestCase
{
    public void testSplit()
    {
        String s = "abc, xyz , 123";
        List list = PropertyConverter.split(s, ',');

        assertEquals("size", 3, list.size());
        assertEquals("1st token for '" + s + "'", "abc", list.get(0));
        assertEquals("2nd token for '" + s + "'", "xyz", list.get(1));
        assertEquals("3rd token for '" + s + "'", "123", list.get(2));
    }

    public void testSplitNoTrim()
    {
        String s = "abc, xyz , 123";
        List list = PropertyConverter.split(s, ',', false);

        assertEquals("size", 3, list.size());
        assertEquals("1st token for '" + s + "'", "abc", list.get(0));
        assertEquals("2nd token for '" + s + "'", " xyz ", list.get(1));
        assertEquals("3rd token for '" + s + "'", " 123", list.get(2));
    }

    public void testSplitWithEscapedSeparator()
    {
        String s = "abc\\,xyz, 123";
        List list = PropertyConverter.split(s, ',');

        assertEquals("size", 2, list.size());
        assertEquals("1st token for '" + s + "'", "abc,xyz", list.get(0));
        assertEquals("2nd token for '" + s + "'", "123", list.get(1));
    }

    public void testSplitEmptyValues()
    {
        String s = ",,";
        List list = PropertyConverter.split(s, ',');

        assertEquals("size", 3, list.size());
        assertEquals("1st token for '" + s + "'", "", list.get(0));
        assertEquals("2nd token for '" + s + "'", "", list.get(1));
        assertEquals("3rd token for '" + s + "'", "", list.get(2));
    }

    public void testSplitWithEndingSlash()
    {
        String s = "abc, xyz\\";
        List list = PropertyConverter.split(s, ',');

        assertEquals("size", 2, list.size());
        assertEquals("1st token for '" + s + "'", "abc", list.get(0));
        assertEquals("2nd token for '" + s + "'", "xyz\\", list.get(1));
    }

    public void testSplitNull()
    {
        List list = PropertyConverter.split(null, ',');
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    /**
     * Tests whether an escape character can be itself escaped.
     */
    public void testSplitEscapeEscapeChar()
    {
        List list = PropertyConverter.split("C:\\Temp\\\\,xyz", ',');
        assertEquals("Wrong list size", 2, list.size());
        assertEquals("Wrong element 1", "C:\\Temp\\", list.get(0));
        assertEquals("Wrong element 2", "xyz", list.get(1));
    }

    /**
     * Tests whether delimiters are correctly escaped.
     */
    public void testEscapeDelimiters()
    {
        assertEquals("Wrong escaped delimiters",
                "C:\\\\Temp\\\\\\,D:\\\\Data\\\\", PropertyConverter
                        .escapeDelimiters("C:\\Temp\\,D:\\Data\\", ','));
    }

    /**
     * Tests whether only the list delimiter can be escaped.
     */
    public void testEscapeListDelimiter()
    {
        assertEquals("Wrong escaped list delimiter", "C:\\Temp\\\\,D:\\Data\\",
                PropertyConverter.escapeListDelimiter("C:\\Temp\\,D:\\Data\\",
                        ','));
    }

    public void testToIterator()
    {
        int[] array = new int[]{1, 2, 3};

        Iterator it = PropertyConverter.toIterator(array, ',');

        assertEquals("1st element", new Integer(1), it.next());
        assertEquals("2nd element", new Integer(2), it.next());
        assertEquals("3rd element", new Integer(3), it.next());
    }

    /**
     * Tests the interpolation features.
     */
    public void testInterpolateString()
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "quick brown fox");
        config.addProperty("target", "lazy dog");
        assertEquals("Wrong interpolation",
                "The quick brown fox jumps over the lazy dog.",
                PropertyConverter.interpolate("The ${animal} jumps over the ${target}.", config));
    }

    /**
     * Tests interpolation of an object. Here nothing should be substituted.
     */
    public void testInterpolateObject()
    {
        assertEquals("Object was not correctly interpolated", new Integer(42),
                PropertyConverter.interpolate(new Integer(42), new PropertiesConfiguration()));
    }

    /**
     * Tests complex interpolation where the variables' values contain in turn
     * other variables.
     */
    public void testInterpolateRecursive()
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "${animal_attr} fox");
        config.addProperty("target", "${target_attr} dog");
        config.addProperty("animal_attr", "quick brown");
        config.addProperty("target_attr", "lazy");
        assertEquals("Wrong complex interpolation",
                "The quick brown fox jumps over the lazy dog.",
                PropertyConverter.interpolate("The ${animal} jumps over the ${target}.", config));
    }

    /**
     * Tests an interpolation that leads to a cycle. This should throw an
     * exception.
     */
    public void testCyclicInterpolation()
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "${animal_attr} ${species}");
        config.addProperty("animal_attr", "quick brown");
        config.addProperty("species", "${animal}");
        try
        {
            PropertyConverter.interpolate("This is a ${animal}", config);
            fail("Cyclic interpolation was not detected!");
        }
        catch (IllegalStateException iex)
        {
            // ok
        }
    }

    /**
     * Tests interpolation if a variable is unknown. Then the variable won't be
     * substituted.
     */
    public void testInterpolationUnknownVariable()
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "quick brown fox");
        assertEquals("Wrong interpolation",
                "The quick brown fox jumps over ${target}.",
                PropertyConverter.interpolate("The ${animal} jumps over ${target}.", config));
    }

    /**
     * Tests conversion to numbers when the passed in objects are already
     * numbers.
     */
    public void testToNumberDirect()
    {
        Integer i = new Integer(42);
        assertSame("Wrong integer", i, PropertyConverter.toNumber(i, Integer.class));
        BigDecimal d = new BigDecimal("3.1415");
        assertSame("Wrong BigDecimal", d, PropertyConverter.toNumber(d, Integer.class));
    }

    /**
     * Tests conversion to numbers when the passed in objects have a compatible
     * string representation.
     */
    public void testToNumberFromString()
    {
        assertEquals("Incorrect Integer value", new Integer(42), PropertyConverter.toNumber("42", Integer.class));
        assertEquals("Incorrect Short value", new Short((short) 10), PropertyConverter.toNumber(new StringBuffer("10"), Short.class));
    }

    /**
     * Tests conversion to numbers when the passed in objects are strings with
     * prefixes for special radices.
     */
    public void testToNumberFromHexString()
    {
        Number n = PropertyConverter.toNumber("0x10", Integer.class);
        assertEquals("Incorrect Integer value", 16, n.intValue());
    }

    /**
     * Tests conversion to numbers when an invalid Hex value is passed in.
     * This should cause an exception.
     */
    public void testToNumberFromInvalidHexString()
    {
        try
        {
            PropertyConverter.toNumber("0xNotAHexValue", Integer.class);
            fail("Could convert invalid hex value!");
        }
        catch (ConversionException cex)
        {
            // ok
        }
    }

    /**
     * Tests conversion to numbers when the passed in objects are strings with
     * prefixes for special radices.
     */
    public void testToNumberFromBinaryString()
    {
        Number n = PropertyConverter.toNumber("0b1111", Integer.class);
        assertEquals("Incorrect Integer value", 15, n.intValue());
    }

    /**
     * Tests conversion to numbers when an invalid binary value is passed in.
     * This should cause an exception.
     */
    public void testToNumberFromInvalidBinaryString()
    {
        try
        {
            PropertyConverter.toNumber("0bNotABinValue", Integer.class);
            fail("Could convert invalid binary value!");
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
            PropertyConverter.toNumber("Not a number", Byte.class);
            fail("Could convert invalid String!");
        }
        catch (ConversionException cex)
        {
            // ok
        }
    }

    /**
     * Tests conversion to numbers when the passed in target class is invalid.
     * This should cause an exception.
     */
    public void testToNumberWithInvalidClass()
    {
        try
        {
            PropertyConverter.toNumber("42", Object.class);
            fail("Could convert to invalid target class!");
        }
        catch (ConversionException cex)
        {
            //ok
        }
    }

    // enumeration type used for the tests, Java 5 only
    private Class enumClass;
    private Object enumObject;
    {
        if (SystemUtils.isJavaVersionAtLeast(1.5f))
        {
            try
            {
                enumClass = Class.forName("java.lang.annotation.ElementType");

                Method valueOfMethod = enumClass.getMethod("valueOf", new Class[] { String.class });
                enumObject = valueOfMethod.invoke(null, new Object[] { "METHOD" });
            }
            catch (Exception e)
            {
            }
        }
    }

    public void testToEnumFromEnum()
    {
        if (!SystemUtils.isJavaVersionAtLeast(1.5f))
        {
            return;
        }

        assertEquals(enumObject, PropertyConverter.toEnum(enumObject, enumClass));
    }

    public void testToEnumFromString()
    {
        if (!SystemUtils.isJavaVersionAtLeast(1.5f))
        {
            return;
        }

        assertEquals(enumObject, PropertyConverter.toEnum("METHOD", enumClass));
    }

    public void testToEnumFromInvalidString()
    {
        if (!SystemUtils.isJavaVersionAtLeast(1.5f))
        {
            return;
        }

        try
        {
            assertEquals(enumObject, PropertyConverter.toEnum("FOO", enumClass));
            fail("Could convert invalid String!");
        }
        catch (ConversionException e)
        {
            // expected
        }
    }

    public void testToEnumFromNumber()
    {
        if (!SystemUtils.isJavaVersionAtLeast(1.5f))
        {
            return;
        }

        assertEquals(enumObject, PropertyConverter.toEnum(new Integer(2), enumClass));
    }

    public void testToEnumFromInvalidNumber()
    {
        if (!SystemUtils.isJavaVersionAtLeast(1.5f))
        {
            return;
        }

        try
        {
            assertEquals(enumObject, PropertyConverter.toEnum(new Integer(-1), enumClass));
            fail("Could convert invalid number!");
        }
        catch (ConversionException e)
        {
            // expected
        }
    }
}
