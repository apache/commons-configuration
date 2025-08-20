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

package org.apache.commons.configuration2.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.lang.annotation.ElementType;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.ex.ConversionException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test class for PropertyConverter.
 */
public class TestPropertyConverter {
    /** Constant for an enumeration class used by some tests. */
    private static final Class<ElementType> ENUM_CLASS = ElementType.class;

    /**
     * See CONFIGURATION-766.
     */
    @Test
    void testToBigDecimalDoubleConstructor() {
        // If the conversion uses new BigDecimal(0.1) the result is not exact due to round off.
        // The result is 0.1000000000000000055511151231257827021181583404541015625.
        // See Sonar rule: https://rules.sonarsource.com/java/type/Bug/RSPEC-2111
        final double d = 0.1;
        // Use BigDecimal#valueOf() Fix PMD AvoidDecimalLiteralsInBigDecimalConstructor
        assertEquals(BigDecimal.valueOf(d), PropertyConverter.toBigDecimal(d));
    }

    /**
     * See CONFIGURATION-766.
     */
    @Test
    @Disabled
    void testToBigDecimalStringConstructor() {
        // If the conversion uses new BigDecimal(0.1) the result is not exact due to round off.
        // The result is 0.1000000000000000055511151231257827021181583404541015625.
        // See Sonar rule: https://rules.sonarsource.com/java/type/Bug/RSPEC-2111
        final double d = 0.1;
        assertEquals(new BigDecimal(Double.toString(d)), PropertyConverter.toBigDecimal(d));
    }

    /**
     * Tests a failed conversion to character.
     */
    @Test
    void testToCharFailed() {
        final DefaultConversionHandler conversionHandler = new DefaultConversionHandler();
        assertThrows(ConversionException.class, () -> PropertyConverter.to(Character.TYPE, "FF", conversionHandler));
    }

    /**
     * Tests whether a conversion to character is possible.
     */
    @Test
    void testToCharSuccess() {
        assertEquals(Character.valueOf('t'), PropertyConverter.to(Character.class, "t", new DefaultConversionHandler()));
    }

    /**
     * Tests whether other objects implementing a toString() method can be converted to character.
     */
    @Test
    void testToCharViaToString() {
        final Object value = new Object() {
            @Override
            public String toString() {
                return "X";
            }
        };
        assertEquals(Character.valueOf('X'), PropertyConverter.to(Character.TYPE, value, new DefaultConversionHandler()));
    }

    @Test
    void testToCustomNumber() {
        assertEquals(new MyNumber(1), PropertyConverter.to(MyNumber.class, "1", null));
        assertEquals(new MyNumber(2), PropertyConverter.to(MyNumber.class, new MyNumber(2), null));
        assertEquals(new MyNumber(0), PropertyConverter.to(MyNumber.class, null, null));
    }

    @Test
    void testToEnumFromEnum() {
        assertEquals(ElementType.METHOD, PropertyConverter.toEnum(ElementType.METHOD, ENUM_CLASS));
    }

    @Test
    void testToEnumFromInvalidNumber() {
        assertThrows(ConversionException.class, () -> PropertyConverter.toEnum(-1, ENUM_CLASS));
    }

    @Test
    void testToEnumFromInvalidString() {
        assertThrows(ConversionException.class, () -> PropertyConverter.toEnum("FOO", ENUM_CLASS));
    }

    @Test
    void testToEnumFromNumber() {
        assertEquals(PropertyConverter.toEnum(Integer.valueOf(ElementType.METHOD.ordinal()), ENUM_CLASS), ElementType.METHOD);
    }

    @Test
    void testToEnumFromString() {
        assertEquals(ElementType.METHOD, PropertyConverter.toEnum("METHOD", ENUM_CLASS));
    }

    /**
     * Tests conversion to files when the passed in objects are already files.
     */
    @Test
    void testToFileDirect() {
        final File f = new File("dir", "file");
        assertSame(f, PropertyConverter.toFile(f));
    }

    /**
     * Tests conversion to file when the passed in objects are paths.
     */
    @Test
    void testToFileFromPath() {
        final Path p = Paths.get("dir", "file");
        assertEquals(new File("dir", "file"), PropertyConverter.toFile(p));
    }

    /**
     * Tests conversion to file when the passed in objects have a compatible string representation.
     */
    @Test
    void testToFileFromString() {
        assertEquals(new File("dir", "file"), PropertyConverter.toFile("dir/file"));
    }

    /**
     * Tests a trivial conversion: the value has already the desired type.
     */
    @Test
    void testToNoConversionNeeded() {
        final String value = "testValue";
        assertEquals(value, PropertyConverter.to(String.class, value, new DefaultConversionHandler()));
    }

    /**
     * Tests conversion to numbers when the passed in objects are already numbers.
     */
    @Test
    void testToNumberDirect() {
        final Integer i = Integer.valueOf(42);
        assertSame(i, PropertyConverter.toNumber(i, Integer.class));
        final BigDecimal d = new BigDecimal("3.1415");
        assertSame(d, PropertyConverter.toNumber(d, Integer.class));
    }

    /**
     * Tests conversion to numbers when the passed in objects are strings with prefixes for special radices.
     */
    @Test
    void testToNumberFromBinaryString() {
        final Number n = PropertyConverter.toNumber("0b1111", Integer.class);
        assertEquals(15, n.intValue());
    }

    /**
     * Tests conversion to numbers when the passed in objects are strings with prefixes for special radices.
     */
    @Test
    void testToNumberFromHexString() {
        final Number n = PropertyConverter.toNumber("0x10", Integer.class);
        assertEquals(16, n.intValue());
    }

    /**
     * Tests conversion to numbers when an invalid binary value is passed in. This should cause an exception.
     */
    @Test
    void testToNumberFromInvalidBinaryString() {
        assertThrows(ConversionException.class, () -> PropertyConverter.toNumber("0bNotABinValue", Integer.class));
    }

    /**
     * Tests conversion to numbers when an invalid Hex value is passed in. This should cause an exception.
     */
    @Test
    void testToNumberFromInvalidHexString() {
        assertThrows(ConversionException.class, () -> PropertyConverter.toNumber("0xNotAHexValue", Integer.class));
    }

    /**
     * Tests conversion to numbers when the passed in objects have no numeric String representation. This should cause an
     * exception.
     */
    @Test
    void testToNumberFromInvalidString() {
        assertThrows(ConversionException.class, () -> PropertyConverter.toNumber("Not a number", Byte.class));
    }

    /**
     * Tests conversion to numbers when the passed in objects have a compatible string representation.
     */
    @Test
    void testToNumberFromString() {
        assertEquals(Integer.valueOf(42), PropertyConverter.toNumber("42", Integer.class));
        assertEquals(Short.valueOf((short) 10), PropertyConverter.toNumber(new StringBuffer("10"), Short.class));
    }

    /**
     * Tests conversion to numbers when the passed in target class is invalid. This should cause an exception.
     */
    @Test
    void testToNumberWithInvalidClass() {
        assertThrows(ConversionException.class, () -> PropertyConverter.toNumber("42", Object.class));
    }

    /**
     * Tests conversion to paths when the passed in objects are already paths.
     */
    @Test
    void testToPathDirect() {
        final Path p = Paths.get("dir", "file");
        assertSame(p, PropertyConverter.toPath(p));
    }

    /**
     * Tests conversion to path when the passed in objects are files.
     */
    @Test
    void testToPathFromFile() {
        final File f = new File("dir", "file");
        assertEquals(Paths.get("dir", "file"), PropertyConverter.toPath(f));
    }

    /**
     * Tests conversion to file when the passed in objects have a compatible string representation.
     */
    @Test
    void testToPathFromString() {
        assertEquals(Paths.get("dir", "file"), PropertyConverter.toPath("dir/file"));
    }

    /**
     * Tests conversion to patterns when the passed in objects are already patterns.
     */
    @Test
    void testToPatternDirect() {
        final Pattern p = Pattern.compile(".+");
        assertSame(p, PropertyConverter.toPattern(p));
    }

    /**
     * Tests conversion to patterns when the passed in objects have a compatible string representation.
     */
    @Test
    void testToPatternFromString() {
        final Pattern p = Pattern.compile(".+");
        assertEquals(p.pattern(), PropertyConverter.toPattern(".+").pattern());
    }

    /**
     * Tests a conversion to a string.
     */
    @Test
    void testToStringConversion() {
        final Integer src = 42;
        final Object result = PropertyConverter.to(String.class, src, new DefaultConversionHandler());
        assertEquals("42", result);
    }
}
