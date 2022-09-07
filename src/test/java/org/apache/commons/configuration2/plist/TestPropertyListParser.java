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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.Reader;
import java.util.Calendar;
import java.util.SimpleTimeZone;

import org.junit.jupiter.api.Test;

/**
 */
public class TestPropertyListParser {
    private final PropertyListParser parser = new PropertyListParser((Reader) null);

    @Test
    public void testFilterData() throws Exception {
        final byte[] expected = {0x20, 0x20};
        assertArrayEquals(null, parser.filterData(null));
        assertArrayEquals(expected, parser.filterData("<2020>"));
        assertArrayEquals(expected, parser.filterData("2020"));
        assertArrayEquals(expected, parser.filterData("20 20"));
        assertArrayEquals(new byte[] {9, 0x20}, parser.filterData("920"));
    }

    @Test
    public void testParseDate() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2002);
        calendar.set(Calendar.MONTH, Calendar.MARCH);
        calendar.set(Calendar.DAY_OF_MONTH, 22);
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeZone(new SimpleTimeZone(60 * 60 * 1000, "Apache/Jakarta"));

        assertEquals(calendar.getTime(), parser.parseDate("<*D2002-03-22 11:30:00 +0100>"));
    }

    @Test
    public void testRemoveQuotes() {
        assertEquals("abc", parser.removeQuotes("abc"));
        assertEquals("abc", parser.removeQuotes("\"abc\""));
        assertEquals("", parser.removeQuotes("\"\""));
        assertEquals("", parser.removeQuotes(""));
        assertNull(parser.removeQuotes(null));
    }

    @Test
    public void testUnescapeQuotes() {
        assertEquals("aaa\"bbb\"ccc", parser.unescapeQuotes("aaa\"bbb\"ccc"));
        assertEquals("aaa\"bbb\"ccc", parser.unescapeQuotes("aaa\\\"bbb\\\"ccc"));
    }
}
