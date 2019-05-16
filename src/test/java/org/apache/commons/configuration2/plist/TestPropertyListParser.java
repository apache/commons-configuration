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

package org.apache.commons.configuration2.plist;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.util.Calendar;
import java.util.SimpleTimeZone;

import junitx.framework.ArrayAssert;

import org.junit.Test;

/**
 * @author Emmanuel Bourg
 */
public class TestPropertyListParser
{
    private final PropertyListParser parser = new PropertyListParser((Reader) null);

    @Test
    public void testRemoveQuotes()
    {
        assertEquals("unquoted string", "abc", parser.removeQuotes("abc"));
        assertEquals("quoted string", "abc", parser.removeQuotes("\"abc\""));
        assertEquals("empty quotes", "", parser.removeQuotes("\"\""));
        assertEquals("empty string", "", parser.removeQuotes(""));
        assertEquals("null string", null, parser.removeQuotes(null));
    }

    @Test
    public void testUnescapeQuotes()
    {
        assertEquals("non escaped quotes", "aaa\"bbb\"ccc", parser.unescapeQuotes("aaa\"bbb\"ccc"));
        assertEquals("escaped quotes", "aaa\"bbb\"ccc", parser.unescapeQuotes("aaa\\\"bbb\\\"ccc"));
    }

    @Test
    public void testParseDate() throws Exception
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2002);
        calendar.set(Calendar.MONTH, Calendar.MARCH);
        calendar.set(Calendar.DAY_OF_MONTH, 22);
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeZone(new SimpleTimeZone(60 * 60 * 1000, "Apache/Jakarta"));

        assertEquals("parsed date", calendar.getTime(), parser.parseDate("<*D2002-03-22 11:30:00 +0100>"));
    }

    @Test
    public void testFilterData() throws Exception
    {
        final byte[] expected = new byte[] {0x20, 0x20};
        ArrayAssert.assertEquals("null string", null, parser.filterData(null));
        ArrayAssert.assertEquals("data with < >", expected, parser.filterData("<2020>"));
        ArrayAssert.assertEquals("data without < >", expected, parser.filterData("2020"));
        ArrayAssert.assertEquals("data with space", expected, parser.filterData("20 20"));
        ArrayAssert.assertEquals("odd length", new byte[]{9, 0x20}, parser.filterData("920"));
    }
}
