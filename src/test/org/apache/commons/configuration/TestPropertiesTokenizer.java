/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import junit.framework.TestCase;

/**
 * Test case for the tokenizer used to slice properties into lists.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 1.1 $, $Date: 2004/06/10 09:38:00 $
 */
public class TestPropertiesTokenizer extends TestCase {

    public void testNextToken() {
        String s1 = "abc,xyz";
        AbstractConfiguration.PropertiesTokenizer tokenizer = new AbstractConfiguration.PropertiesTokenizer(s1);
        assertEquals("1st token for '" + s1 + "'", "abc", tokenizer.nextToken());
        assertEquals("2nd token for '" + s1 + "'", "xyz", tokenizer.nextToken());
        assertFalse("more than 2 tokens found for '" + s1 + "'", tokenizer.hasMoreTokens());

        String s2 = "abc\\,xyz";
        tokenizer = new AbstractConfiguration.PropertiesTokenizer(s2);
        assertEquals("1st token for '" + s2 + "'", "abc,xyz", tokenizer.nextToken());
        assertFalse("more than 1 token found for '" + s2 + "'", tokenizer.hasMoreTokens());
    }
}
