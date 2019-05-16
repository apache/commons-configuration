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
package org.apache.commons.configuration2.tree;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test class for {@code DefaultExpressionEngineSymbols}.
 *
 */
public class TestDefaultExpressionEngineSymbols
{
    /**
     * Tests the instance with default symbols.
     */
    @Test
    public void testDefaultSymbols()
    {
        assertEquals("Wrong delimiter", ".",
                DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS
                        .getPropertyDelimiter());
        assertEquals("Wrong escaped delimiter", "..",
                DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS
                        .getEscapedDelimiter());
        assertEquals("Wrong index start", "(",
                DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS.getIndexStart());
        assertEquals("Wrong index end", ")",
                DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS.getIndexEnd());
        assertEquals("Wrong attribute start", "[@",
                DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS
                        .getAttributeStart());
        assertEquals("Wrong attribute end", "]",
                DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS
                        .getAttributeEnd());
    }

    /**
     * Helper method for checking whether two objects are equal.
     *
     * @param o1 object 1
     * @param o2 object 2
     */
    private static void expEqual(final Object o1, final Object o2)
    {
        assertTrue("Not equal", o1.equals(o2));
        assertTrue("Not symmetric", o2.equals(o1));
        assertEquals("Different hash codes", o1.hashCode(), o2.hashCode());
    }

    /**
     * Helper method for testing that two objects are not equal.
     *
     * @param o1 object 1
     * @param o2 object 2
     */
    private static void expNE(final Object o1, final Object o2)
    {
        assertFalse("Equal", o1.equals(o2));
        if (o2 != null)
        {
            assertFalse("Not symmetric", o2.equals(o1));
        }
    }

    /**
     * Tests equals() if the expected result is true.
     */
    @Test
    public void testEqualsTrue()
    {
        expEqual(DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS,
                DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS);
        final DefaultExpressionEngineSymbols s2 =
                new DefaultExpressionEngineSymbols.Builder(
                        DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS)
                        .create();
        expEqual(DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS, s2);
    }

    /**
     * Helper method for creating a builder object which is initialized with the
     * default symbols.
     *
     * @return the initialized builder
     */
    private static DefaultExpressionEngineSymbols.Builder builder()
    {
        return new DefaultExpressionEngineSymbols.Builder(
                DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS);
    }

    /**
     * Tests equals() if the expected result is false.
     */
    @Test
    public void testEqualsFalse()
    {
        final DefaultExpressionEngineSymbols s1 =
                DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS;
        DefaultExpressionEngineSymbols s2 =
                builder().setPropertyDelimiter("/").create();
        expNE(s1, s2);
        s2 = builder().setEscapedDelimiter("\\.").create();
        expNE(s1, s2);
        s2 = builder().setIndexStart("[").create();
        expNE(s1, s2);
        s2 = builder().setIndexEnd("]").create();
        expNE(s1, s2);
        s2 = builder().setAttributeStart("#").create();
        expNE(s1, s2);
        s2 = builder().setAttributeEnd("~").create();
        expNE(s1, s2);
    }

    /**
     * Tests equals for null input.
     */
    @Test
    public void testEqualsNull()
    {
        expNE(builder().create(), null);
    }

    /**
     * Tests equals with an object of another class.
     */
    @Test
    public void testEqualsOtherClass()
    {
        expNE(builder().create(), this);
    }

    /**
     * Tests the string representation.
     */
    @Test
    public void testToString()
    {
        final DefaultExpressionEngineSymbols symbols = builder().create();
        final String s = symbols.toString();
        assertThat(
                s,
                containsString("propertyDelimiter="
                        + symbols.getPropertyDelimiter()));
        assertThat(
                s,
                containsString("escapedDelimiter="
                        + symbols.getEscapedDelimiter()));
        assertThat(s, containsString("indexStart=" + symbols.getIndexStart()));
        assertThat(s, containsString("indexEnd=" + symbols.getIndexEnd()));
        assertThat(s,
                containsString("attributeStart=" + symbols.getAttributeStart()));
        assertThat(s,
                containsString("attributeEnd=" + symbols.getAttributeEnd()));
    }
}
