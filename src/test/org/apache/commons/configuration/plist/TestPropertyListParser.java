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

package org.apache.commons.configuration.plist;

import java.io.Reader;

import junit.framework.TestCase;
import junitx.framework.ArrayAssert;

/**
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class TestPropertyListParser extends TestCase
{
    private PropertyListParser parser = new PropertyListParser((Reader) null);

    public void testRemoveQuotes()
    {
        assertEquals("unquoted string", "abc", parser.removeQuotes("abc"));
        assertEquals("quoted string", "abc", parser.removeQuotes("\"abc\""));
        assertEquals("empty quotes", "", parser.removeQuotes("\"\""));
        assertEquals("empty string", "", parser.removeQuotes(""));
        assertEquals("null string", null, parser.removeQuotes(null));
    }

    public void testUnescapeQuotes()
    {
        assertEquals("non escaped quotes", "aaa\"bbb\"ccc", parser.unescapeQuotes("aaa\"bbb\"ccc"));
        assertEquals("escaped quotes", "aaa\"bbb\"ccc", parser.unescapeQuotes("aaa\\\"bbb\\\"ccc"));
    }

    public void testFilterData() throws Exception
    {
        byte[] expected = new byte[] {0x20, 0x20};
        ArrayAssert.assertEquals("null string", null, parser.filterData(null));
        ArrayAssert.assertEquals("data with < >", expected, parser.filterData("<2020>"));
        ArrayAssert.assertEquals("data without < >", expected, parser.filterData("2020"));
        ArrayAssert.assertEquals("data with space", expected, parser.filterData("20 20"));
        ArrayAssert.assertEquals("odd length", new byte[]{9, 0x20}, parser.filterData("920"));
    }
}
