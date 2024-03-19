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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code DefaultConversionHandler}.
 */
public class TestDefaultConversionHandler {
    /** Constant for a variable. */
    private static final String VAR = "${test}";

    /** Constant for the value to replace the variable. */
    private static final String REPLACEMENT = "1";

    /**
     * Creates a special test ConfigurationInterpolator. This object only replaces the test variable by its replacement.
     * Other substitutions are not performed.
     *
     * @return the test {@code ConfigurationInterpolator}
     */
    private static ConfigurationInterpolator createInterpolator() {
        return new ConfigurationInterpolator() {
            @Override
            public Object interpolate(final Object value) {
                if (VAR.equals(value)) {
                    return REPLACEMENT;
                }
                return value;
            }
        };
    }

    /** The conversion handler to be tested. */
    private DefaultConversionHandler handler;

    /**
     * Helper method for testing the result of the conversion of a single value.
     *
     * @param expResult the expected result
     */
    private void checkSingleValue(final Integer expResult) {
        assertEquals(Integer.parseInt(REPLACEMENT), expResult.intValue());
    }

    @BeforeEach
    public void setUp() {
        handler = new DefaultConversionHandler();
    }

    /**
     * Tests whether the default date format is used if no format has been set.
     */
    @Test
    public void testGetDateFormatNotSet() {
        assertEquals(DefaultConversionHandler.DEFAULT_DATE_FORMAT, handler.getDateFormat());
    }

    @Test
    public synchronized void testListDelimiterHandler() {
        assertEquals(DefaultConversionHandler.LIST_DELIMITER_HANDLER, handler.getListDelimiterHandler());
        handler.setListDelimiterHandler(null);
        assertEquals(DefaultConversionHandler.LIST_DELIMITER_HANDLER, handler.getListDelimiterHandler());
        final LegacyListDelimiterHandler legacyListDelimiterHandler = new LegacyListDelimiterHandler(',');
        handler.setListDelimiterHandler(legacyListDelimiterHandler);
        assertEquals(legacyListDelimiterHandler, handler.getListDelimiterHandler());
        handler.setListDelimiterHandler(null);
        assertEquals(DefaultConversionHandler.LIST_DELIMITER_HANDLER, handler.getListDelimiterHandler());
    }

    /**
     * Tests whether the date format can be changed.
     */
    @Test
    public void testSetDateFormat() {
        final String dateFormat = "dd.MM.yyyy";
        handler.setDateFormat(dateFormat);
        assertEquals(dateFormat, handler.getDateFormat());
    }

    /**
     * Tests a conversion to an array from an empty string. An empty string should be interpreted as an empty array.
     */
    @Test
    public void testToArrayEmptyString() {
        final int[] array = (int[]) handler.toArray("", Integer.TYPE, null);
        assertEquals(0, array.length);
    }

    /**
     * Tests toArray() if the source object is null.
     */
    @Test
    public void testToArrayNullInput() {
        assertNull(handler.toArray(null, Integer.class, null));
    }

    /**
     * Tests a conversion to an array of Objects.
     */
    @Test
    public void testToArrayObject() {
        final List<String> src = Arrays.asList(VAR, "100");
        final Integer[] array = (Integer[]) handler.toArray(src, Integer.class, createInterpolator());
        assertArrayEquals(new Integer[] {Integer.valueOf(REPLACEMENT), Integer.valueOf(src.get(1))}, array);
    }

    /**
     * Tests a conversion to an array of primitive type if the source object is something else.
     */
    @Test
    public void testToArrayPrimitiveOtherType() {
        final List<String> src = Arrays.asList(VAR, "100");
        final int[] array = (int[]) handler.toArray(src, Integer.TYPE, createInterpolator());
        assertArrayEquals(new int[] {Integer.parseInt(REPLACEMENT), Integer.parseInt(src.get(1))}, array);
    }

    /**
     * Tests a conversion to an array of primitive type if the source array already has the correct type.
     */
    @Test
    public void testToArrayPrimitiveSameType() {
        final int[] src = {1, 2, 3, 4, 5, 6};
        final int[] array = (int[]) handler.toArray(src, Integer.TYPE, createInterpolator());
        assertArrayEquals(src, array);
    }

    /**
     * Tests a conversion to an array of primitive type if the source array is of the corresponding wrapper type.
     */
    @Test
    public void testToArrayPrimitiveWrapperType() {
        final Integer[] src = {0, 1, 2, 4, 8, 16, 32, 64, 128};
        final int[] array = (int[]) handler.toArray(src, Integer.TYPE, createInterpolator());
        assertArrayEquals(new int[] {0, 1, 2, 4, 8, 16, 32, 64, 128}, array);
    }

    /**
     * Tests a conversion to a Calendar object using the default format.
     */
    @Test
    public void testToCalendarWithDefaultFormat() {
        final Calendar cal = handler.to("2013-08-19 21:17:22", Calendar.class, null);
        assertEquals(19, cal.get(Calendar.DATE));
        assertEquals(Calendar.AUGUST, cal.get(Calendar.MONTH));
        assertEquals(2013, cal.get(Calendar.YEAR));
        assertEquals(21, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(17, cal.get(Calendar.MINUTE));
        assertEquals(22, cal.get(Calendar.SECOND));
    }

    /**
     * Tests a conversion to a collection if an empty string is passed in. An empty string should be interpreted as a list
     * with no values.
     */
    @Test
    public void testToCollectionEmptyString() {
        final List<Integer> col = new ArrayList<>(1);
        handler.toCollection("", Integer.class, null, col);
        assertTrue(col.isEmpty());
    }

    /**
     * Tries to pass a null collection to toCollection().
     */
    @Test
    public void testToCollectionNullCollection() {
        final List<Integer> src = Arrays.asList(1, 2, 3);
        assertThrows(IllegalArgumentException.class, () -> handler.toCollection(src, Integer.class, null, null));
    }

    /**
     * Tests a conversion to a collection if the source object is null.
     */
    @Test
    public void testToCollectionNullInput() {
        final ArrayList<Integer> col = new ArrayList<>();
        handler.toCollection(null, Integer.class, null, col);
        assertTrue(col.isEmpty());
    }

    /**
     * Tests a successful conversion to a collection.
     */
    @Test
    public void testToCollectionSuccess() {
        final Object[] src = {VAR, "100"};
        final List<Integer> col = new ArrayList<>(src.length);
        handler.toCollection(src, Integer.class, createInterpolator(), col);
        assertEquals(Arrays.asList(Integer.valueOf(REPLACEMENT), Integer.valueOf(src[1].toString())), col);
    }

    /**
     * Tests whether a conversion to a date object is possible if a specific date format is used.
     */
    @Test
    public void testToDateWithFormat() {
        handler.setDateFormat("dd.MM.yyyy");
        final Date dt = handler.to("19.08.2013", Date.class, null);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        assertEquals(19, cal.get(Calendar.DATE));
        assertEquals(Calendar.AUGUST, cal.get(Calendar.MONTH));
        assertEquals(2013, cal.get(Calendar.YEAR));
    }

    /**
     * Tests a failed conversion.
     */
    @Test
    public void testToFailedConversion() {
        assertThrows(ConversionException.class, () -> handler.to(VAR, Integer.class, null));
    }

    /**
     * Tests whether a conversion from an array is possible.
     */
    @Test
    public void testToFromArray() {
        final Object[] src = {VAR, true, 20130808221759L};
        checkSingleValue(handler.to(src, Integer.class, createInterpolator()));
    }

    /**
     * Tests whether a conversion from a collection is possible.
     */
    @Test
    public void testToFromCollection() {
        final Collection<String> src = Arrays.asList(VAR, "true", "1000");
        checkSingleValue(handler.to(src, Integer.class, createInterpolator()));
    }

    /**
     * Tests whether empty complex objects are handled when converting to a single value.
     */
    @Test
    public void testToFromEmptyCollection() {
        assertNull(handler.to(new ArrayList<>(), Integer.class, createInterpolator()));
    }

    /**
     * Tests whether a conversion from an iterator is possible.
     */
    @Test
    public void testToFromIterator() {
        final Iterator<String> src = Arrays.asList(VAR, "true", "1000").iterator();
        checkSingleValue(handler.to(src, Integer.class, createInterpolator()));
    }

    /**
     * Tests whether a ConfigurationInterpolator is optional.
     */
    @Test
    public void testToNoInterpolator() {
        final Integer result = handler.to(REPLACEMENT, Integer.class, null);
        checkSingleValue(result);
    }

    /**
     * Tests whether null input is handled by to().
     */
    @Test
    public void testToNull() {
        assertNull(handler.to(null, Integer.class, null));
    }

    /**
     * Tests a conversion to a primitive type.
     */
    @Test
    public void testToPrimitive() {
        final Long value = 20130819214935L;
        final Object result = handler.to(value.toString(), Long.TYPE, null);
        assertEquals(value, result);
    }

    /**
     * Tests a conversion with a ConfigurationInterpolator.
     */
    @Test
    public void testToWithInterpolator() {
        final Integer result = handler.to(VAR, Integer.class, createInterpolator());
        checkSingleValue(result);
    }
}
