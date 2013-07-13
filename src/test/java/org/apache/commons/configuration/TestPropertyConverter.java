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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.annotation.ElementType;
import java.math.BigDecimal;

import org.junit.Test;

/**
 * Test class for PropertyConverter.
 *
 * @author Emmanuel Bourg
 * @version $Id$
 */
public class TestPropertyConverter
{
    /** Constant for an enumeration class used by some tests. */
    private static final Class<ElementType> ENUM_CLASS = ElementType.class;

    /**
     * Tests the interpolation features.
     */
    @Test
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
    @Test
    public void testInterpolateObject()
    {
        assertEquals("Object was not correctly interpolated", new Integer(42),
                PropertyConverter.interpolate(new Integer(42), new PropertiesConfiguration()));
    }

    /**
     * Tests complex interpolation where the variables' values contain in turn
     * other variables.
     */
    @Test
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
    @Test(expected = IllegalStateException.class)
    public void testCyclicInterpolation()
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "${animal_attr} ${species}");
        config.addProperty("animal_attr", "quick brown");
        config.addProperty("species", "${animal}");
        PropertyConverter.interpolate("This is a ${animal}", config);
    }

    /**
     * Tests interpolation if a variable is unknown. Then the variable won't be
     * substituted.
     */
    @Test
    public void testInterpolationUnknownVariable()
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "quick brown fox");
        assertEquals("Wrong interpolation",
                "The quick brown fox jumps over ${target}.",
                PropertyConverter.interpolate("The ${animal} jumps over ${target}.", config));
    }

    /**
     * Tests interpolate() if the configuration does not have a
     * {@code ConfigurationInterpolator}.
     */
    @Test
    public void testInterpolationNoInterpolator()
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("animal", "quick brown fox");
        config.addProperty("target", "lazy dog");
        config.setInterpolator(null);
        String txt = "The ${animal} jumps over the ${target}.";
        assertEquals("Interpolation was performed", txt,
                PropertyConverter.interpolate(txt, config));
    }

    /**
     * Tests interpolate() if the passed in configuration is null.
     */
    @Test
    public void testInterpolationNoConfiguration()
    {
        String txt = "The ${animal} jumps over the ${target}.";
        assertEquals("Interpolation was performed", txt,
                PropertyConverter.interpolate(txt, null));
    }

    /**
     * Tests conversion to numbers when the passed in objects are already
     * numbers.
     */
    @Test
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
    @Test
    public void testToNumberFromString()
    {
        assertEquals("Incorrect Integer value", new Integer(42), PropertyConverter.toNumber("42", Integer.class));
        assertEquals("Incorrect Short value", new Short((short) 10), PropertyConverter.toNumber(new StringBuffer("10"), Short.class));
    }

    /**
     * Tests conversion to numbers when the passed in objects are strings with
     * prefixes for special radices.
     */
    @Test
    public void testToNumberFromHexString()
    {
        Number n = PropertyConverter.toNumber("0x10", Integer.class);
        assertEquals("Incorrect Integer value", 16, n.intValue());
    }

    /**
     * Tests conversion to numbers when an invalid Hex value is passed in.
     * This should cause an exception.
     */
    @Test(expected = ConversionException.class)
    public void testToNumberFromInvalidHexString()
    {
        PropertyConverter.toNumber("0xNotAHexValue", Integer.class);
    }

    /**
     * Tests conversion to numbers when the passed in objects are strings with
     * prefixes for special radices.
     */
    @Test
    public void testToNumberFromBinaryString()
    {
        Number n = PropertyConverter.toNumber("0b1111", Integer.class);
        assertEquals("Incorrect Integer value", 15, n.intValue());
    }

    /**
     * Tests conversion to numbers when an invalid binary value is passed in.
     * This should cause an exception.
     */
    @Test(expected = ConversionException.class)
    public void testToNumberFromInvalidBinaryString()
    {
        PropertyConverter.toNumber("0bNotABinValue", Integer.class);
    }

    /**
     * Tests conversion to numbers when the passed in objects have no numeric
     * String representation. This should cause an exception.
     */
    @Test(expected = ConversionException.class)
    public void testToNumberFromInvalidString()
    {
        PropertyConverter.toNumber("Not a number", Byte.class);
    }

    /**
     * Tests conversion to numbers when the passed in target class is invalid.
     * This should cause an exception.
     */
    @Test(expected = ConversionException.class)
    public void testToNumberWithInvalidClass()
    {
        PropertyConverter.toNumber("42", Object.class);
    }

    @Test
    public void testToEnumFromEnum()
    {
        assertEquals(ElementType.METHOD, PropertyConverter.toEnum(ElementType.METHOD, ENUM_CLASS));
    }

    @Test
    public void testToEnumFromString()
    {
        assertEquals(ElementType.METHOD, PropertyConverter.toEnum("METHOD", ENUM_CLASS));
    }

    @Test(expected = ConversionException.class)
    public void testToEnumFromInvalidString()
    {
        PropertyConverter.toEnum("FOO", ENUM_CLASS);
    }

    @Test
    public void testToEnumFromNumber()
    {
        assertEquals(ElementType.METHOD, PropertyConverter.toEnum(
                Integer.valueOf(ElementType.METHOD.ordinal()),
                ENUM_CLASS));
    }

    @Test(expected = ConversionException.class)
    public void testToEnumFromInvalidNumber()
    {
        PropertyConverter.toEnum(Integer.valueOf(-1), ENUM_CLASS);
    }

    /**
     * Tests a trivial conversion: the value has already the desired type.
     */
    @Test
    public void testToNoConversionNeeded()
    {
        String value = "testValue";
        assertEquals("Wrong conversion result", value,
                PropertyConverter.to(String.class, value, null));
    }
}
