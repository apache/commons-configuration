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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code DefaultListDelimiterHandler}.
 *
 */
public class TestDefaultListDelimiterHandler
{
    /** The handler to be tested. */
    private DefaultListDelimiterHandler handler;

    @Before
    public void setUp() throws Exception
    {
        handler = new DefaultListDelimiterHandler(',');
    }

    /**
     * Tests whether a string is correctly escaped which does not contain any
     * special character.
     */
    @Test
    public void testEscapeStringNoSpecialCharacter()
    {
        assertEquals("Wrong result", "test", handler.escapeString("test"));
    }

    /**
     * Tests whether the list delimiter character is correctly escaped in a
     * string.
     */
    @Test
    public void testEscapeStringListDelimiter()
    {
        assertEquals("Wrong result", "3\\,1415", handler.escapeString("3,1415"));
    }

    /**
     * Tests whether a backslash is correctly escaped.
     */
    @Test
    public void testEscapeStringBackslash()
    {
        assertEquals("Wrong result", "C:\\\\Temp\\\\",
                handler.escapeString("C:\\Temp\\"));
    }

    /**
     * Tests whether combinations of list delimiters and backslashes are
     * correctly escaped.
     */
    @Test
    public void testEscapeStringListDelimiterAndBackslash()
    {
        assertEquals("Wrong result", "C:\\\\Temp\\\\\\,\\\\\\\\Share\\,/root",
                handler.escapeString("C:\\Temp\\,\\\\Share,/root"));
    }

    /**
     * Tests whether a value transformer is correctly called when escaping a
     * single value.
     */
    @Test
    public void testEscapeWithTransformer()
    {
        final ValueTransformer trans = EasyMock.createMock(ValueTransformer.class);
        EasyMock.expect(trans.transformValue("a\\,b")).andReturn("ok");
        EasyMock.replay(trans);
        assertEquals("Wrong result", "ok", handler.escape("a,b", trans));
        EasyMock.verify(trans);
    }

    /**
     * Tests whether a list is correctly escaped.
     */
    @Test
    public void testEscapeList()
    {
        final ValueTransformer trans = new ValueTransformer()
        {
            @Override
            public Object transformValue(final Object value)
            {
                return String.valueOf(value) + "_trans";
            }
        };
        final List<String> data =
                Arrays.asList("simple", "Hello,world!", "\\,\\", "end");
        assertEquals("Wrong result", "simple_trans,Hello\\,world!_trans,"
                + "\\\\\\,\\\\_trans,end_trans",
                handler.escapeList(data, trans));
    }

    /**
     * Helper methods for testing a split operation. A split is executed with
     * the passed in parameters. Then the results are compared to the expected
     * elements.
     *
     * @param value the value to be split
     * @param trim the trim flag
     * @param expectedElements the expected results
     */
    private void checkSplit(final String value, final boolean trim,
            final String... expectedElements)
    {
        final Collection<String> elems = handler.split(value, trim);
        assertEquals("Wrong number of elements", expectedElements.length,
                elems.size());
        int idx = 0;
        for (final String elem : elems)
        {
            assertEquals("Wrong value at " + idx, expectedElements[idx++], elem);
        }
    }

    /**
     * Tests split() if there is only a single element.
     */
    @Test
    public void testSplitSingleElement()
    {
        checkSplit("test", true, "test");
    }

    /**
     * Tests whether a string list is split correctly.
     */
    @Test
    public void testSplitList()
    {
        checkSplit("a, b,c   ,   d", true, "a", "b", "c", "d");
    }

    /**
     * Tests whether trimming can be disabled when splitting a list.
     */
    @Test
    public void testSplitNoTrim()
    {
        checkSplit("a , b,  c  ,d", false, "a ", " b", "  c  ", "d");
    }

    /**
     * Tests whether a line delimiter can be escaped when splitting a list.
     */
    @Test
    public void testSplitEscapeLineDelimiter()
    {
        checkSplit("3\\,1415", true, "3,1415");
    }

    /**
     * Tests whether split() deals correctly with escaped backslashes.
     */
    @Test
    public void testSplitEscapeBackslash()
    {
        checkSplit("C:\\\\Temp\\\\", true, "C:\\Temp\\");
    }

    /**
     * Tests a split operation with a complex combination of list delimiters and
     * backslashes.
     */
    @Test
    public void testSplitEscapeListDelimiterAndBackslashes()
    {
        checkSplit("C:\\\\Temp\\\\\\,\\\\\\\\Share\\\\,/root", false,
                "C:\\Temp\\,\\\\Share\\", "/root");
    }

    /**
     * Tests whether an unexpected escape character is handled properly.
     */
    @Test
    public void testSplitUnexpectedEscape()
    {
        checkSplit("\\x, \\,y, \\", true, "\\x", ",y", "\\");
    }
}
