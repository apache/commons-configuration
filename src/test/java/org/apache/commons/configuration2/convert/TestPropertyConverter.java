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

package org.apache.commons.configuration2.convert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.lang.annotation.ElementType;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.ex.ConversionException;
import org.junit.Test;

/**
 * Test class for PropertyConverter.
 *
 * @author Emmanuel Bourg
 */
public class TestPropertyConverter
{
    /** Constant for an enumeration class used by some tests. */
    private static final Class<ElementType> ENUM_CLASS = ElementType.class;

    /**
     * Tests conversion to files when the passed in objects are already
     * files.
     */
    @Test
    public void testToFileDirect()
    {
        final File f = new File("dir", "file");
        assertSame("Wrong file", f, PropertyConverter.toFile(f));
    }

    /**
     * Tests conversion to file when the passed in objects have a compatible
     * string representation.
     */
    @Test
    public void testToFileFromString()
    {
        assertEquals("Wrong conversion result", new File("dir", "file"), PropertyConverter.toFile("dir/file"));
    }

    /**
     * Tests conversion to file when the passed in objects are paths.
     */
    @Test
    public void testToFileFromPath()
    {
        final Path p = Paths.get("dir", "file");
        assertEquals("Wrong conversion result", new File("dir", "file"), PropertyConverter.toFile(p));
    }

    /**
     * Tests conversion to paths when the passed in objects are already
     * paths.
     */
    @Test
    public void testToPathDirect()
    {
        final Path p = Paths.get("dir", "file");
        assertSame("Wrong path", p, PropertyConverter.toPath(p));
    }

    /**
     * Tests conversion to file when the passed in objects have a compatible
     * string representation.
     */
    @Test
    public void testToPathFromString()
    {
        assertEquals("Wrong conversion result", Paths.get("dir", "file"), PropertyConverter.toPath("dir/file"));
    }

    /**
     * Tests conversion to path when the passed in objects are files.
     */
    @Test
    public void testToPathFromFile()
    {
        final File f =  new File("dir", "file");
        assertEquals("Wrong conversion result", Paths.get("dir", "file"), PropertyConverter.toPath(f));
    }

    /**
     * Tests conversion to numbers when the passed in objects are already
     * numbers.
     */
    @Test
    public void testToNumberDirect()
    {
        final Integer i = new Integer(42);
        assertSame("Wrong integer", i, PropertyConverter.toNumber(i, Integer.class));
        final BigDecimal d = new BigDecimal("3.1415");
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
        final Number n = PropertyConverter.toNumber("0x10", Integer.class);
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
        final Number n = PropertyConverter.toNumber("0b1111", Integer.class);
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

    /**
     * Tests conversion to patterns when the passed in objects are already
     * patterns.
     */
    @Test
    public void testToPatternDirect()
    {
        final Pattern p = Pattern.compile(".+");
        assertSame("Wrong pattern", p, PropertyConverter.toPattern(p));
    }

    /**
     * Tests conversion to patterns when the passed in objects have a compatible
     * string representation.
     */
    @Test
    public void testToPatternFromString()
    {
        final Pattern p = Pattern.compile(".+");
        assertEquals("Wrong conversion result", p.pattern(), PropertyConverter.toPattern(".+").pattern());
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
        final String value = "testValue";
        assertEquals("Wrong conversion result", value, PropertyConverter.to(
                String.class, value, new DefaultConversionHandler()));
    }

    /**
     * Tests whether a conversion to character is possible.
     */
    @Test
    public void testToCharSuccess()
    {
        assertEquals("Wrong conversion result", Character.valueOf('t'),
                PropertyConverter.to(Character.class, "t",
                        new DefaultConversionHandler()));
    }

    /**
     * Tests whether other objects implementing a toString() method can be
     * converted to character.
     */
    @Test
    public void testToCharViaToString()
    {
        final Object value = new Object()
        {
            @Override
            public String toString()
            {
                return "X";
            }
        };
        assertEquals("Wrong conversion result", Character.valueOf('X'),
                PropertyConverter.to(Character.TYPE, value,
                        new DefaultConversionHandler()));
    }

    /**
     * Tests a failed conversion to character.
     */
    @Test(expected = ConversionException.class)
    public void testToCharFailed()
    {
        PropertyConverter.to(Character.TYPE, "FF",
                new DefaultConversionHandler());
    }

    /**
     * Tests a conversion to a string.
     */
    @Test
    public void testToStringConversion()
    {
        final Integer src = 42;
        final Object result =
                PropertyConverter.to(String.class, src,
                        new DefaultConversionHandler());
        assertEquals("Wrong resulting string", "42", result);
    }
}
