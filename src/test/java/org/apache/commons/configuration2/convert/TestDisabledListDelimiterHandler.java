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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code DisabledListDelimiterHandler}. Note that some functionality of the base class is tested, too.
 *
 */
public class TestDisabledListDelimiterHandler {
    /** An array with some test values. */
    private static final Object[] VALUES = {20130630213801L, "A test value", 5};

    /** Constant for a test string value. */
    private static final String STR_VALUE = "  A test, string; value! ";

    /**
     * Checks whether the passed in container contains the expected values.
     *
     * @param container the iterator to test
     */
    private static void checkIterator(final Iterable<?> container) {
        final Iterator<?> it = container.iterator();
        for (final Object o : VALUES) {
            assertEquals(o, it.next());
        }
        assertFalse(it.hasNext());
    }

    /** The instance to be tested. */
    private DisabledListDelimiterHandler handler;

    @BeforeEach
    public void setUp() throws Exception {
        handler = new DisabledListDelimiterHandler();
    }

    /**
     * Tests escapeList(). This operation is not supported.
     */
    @Test
    public void testEscapeList() {
        final List<Object> values = Arrays.asList(VALUES);
        assertThrows(UnsupportedOperationException.class, () -> handler.escapeList(values, ListDelimiterHandler.NOOP_TRANSFORMER));
    }

    /**
     * Tests whether a non-string value is correctly escaped. The object should not be modified.
     */
    @Test
    public void testEscapeNonStringValue() {
        final Object value = 42;
        assertEquals(value, handler.escape(value, ListDelimiterHandler.NOOP_TRANSFORMER));
    }

    /**
     * Tests whether the transformer is correctly called when escaping a non string value.
     */
    @Test
    public void testEscapeNonStringValueTransformer() {
        final ValueTransformer trans = mock(ValueTransformer.class);
        final Object value = 42;

        when(trans.transformValue(value)).thenReturn(STR_VALUE);

        assertEquals(STR_VALUE, handler.escape(value, trans));

        verify(trans).transformValue(value);
        verifyNoMoreInteractions(trans);
    }

    /**
     * Tests whether a string value is correctly escaped. The string should not be modified.
     */
    @Test
    public void testEscapeStringValue() {
        assertEquals(STR_VALUE, handler.escape(STR_VALUE, ListDelimiterHandler.NOOP_TRANSFORMER));
    }

    /**
     * Tests whether the transformer is correctly invoked when escaping a string.
     */
    @Test
    public void testEscapeStringValueTransformer() {
        final ValueTransformer trans = mock(ValueTransformer.class);
        final String testStr = "Some other string";

        when(trans.transformValue(testStr)).thenReturn(STR_VALUE);

        assertEquals(STR_VALUE, handler.escape(testStr, trans));

        verify(trans).transformValue(testStr);
        verifyNoMoreInteractions(trans);
    }

    /**
     * Tests whether a limit is applied when extracting values from an array.
     */
    @Test
    public void testFlattenArrayWithLimit() {
        final Collection<?> res = handler.flatten(VALUES, 1);
        assertEquals(1, res.size());
        assertEquals(VALUES[0], res.iterator().next());
    }

    /**
     * Tests whether elements can be extracted from a collection that contains an array if a limit is specified.
     */
    @Test
    public void testFlattenCollectionWithArrayWithLimit() {
        final Collection<Object> src = new ArrayList<>(2);
        src.add(STR_VALUE);
        src.add(VALUES);
        final Collection<?> res = handler.flatten(src, 2);
        assertEquals(2, res.size());
        final Iterator<?> it = res.iterator();
        assertEquals(STR_VALUE, it.next());
        assertEquals(VALUES[0], it.next());
    }

    /**
     * Tests whether a limit is applied when extracting elements from a collection.
     */
    @Test
    public void testFlattenCollectionWithLimit() {
        final Collection<Object> src = Arrays.asList(VALUES);
        final Collection<?> res = handler.flatten(src, 1);
        assertEquals(1, res.size());
        assertEquals(VALUES[0], res.iterator().next());
    }

    /**
     * Tests whether the values of an array can be extracted.
     */
    @Test
    public void testParseArray() {
        checkIterator(handler.parse(VALUES));
    }

    /**
     * Tests whether the values of an Iterable object can be extracted.
     */
    @Test
    public void testParseIterable() {
        checkIterator(handler.parse(Arrays.asList(VALUES)));
    }

    /**
     * Tests whether the values of an Iterator object can be extracted.
     */
    @Test
    public void testParseIterator() {
        checkIterator(handler.parse(Arrays.asList(VALUES).iterator()));
    }

    /**
     * Tests whether a null value can be parsed.
     */
    @Test
    public void testParseNull() {
        assertFalse(handler.parse(null).iterator().hasNext());
    }

    /**
     * Tests whether a simple string value can be parsed.
     */
    @Test
    public void testParseSimpleValue() {
        final Iterator<?> it = handler.parse(STR_VALUE).iterator();
        assertEquals(STR_VALUE, it.next());
        assertFalse(it.hasNext());
    }
}
