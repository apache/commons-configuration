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

package org.apache.commons.configuration2.combined;

import junit.framework.TestCase;
import org.apache.commons.configuration2.MultiFileHierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;

public class TestDynamicCombinedConfiguration extends TestCase
{
    private static String PATTERN ="${sys:Id}";
    private static String PATTERN1 = "target/test-classes/testMultiConfiguration_${sys:Id}.xml";
    private static String DEFAULT_FILE = "target/test-classes/testMultiConfiguration_default.xml";

    public void testConfiguration() throws Exception
    {
        DynamicCombinedConfiguration config = new DynamicCombinedConfiguration();
        config.setKeyPattern(PATTERN);
        MultiFileHierarchicalConfiguration multi = new MultiFileHierarchicalConfiguration(PATTERN1);
        config.addConfiguration(multi, "Multi");
        XMLConfiguration xml = new XMLConfiguration(DEFAULT_FILE);
        config.addConfiguration(xml, "Default");

        verify("1001", config, 15);
        verify("1002", config, 25);
        verify("1003", config, 35);
        verify("1004", config, 50);
    }

    private void verify(String key, DynamicCombinedConfiguration config, int rows)
    {
        System.setProperty("Id", key);
        assertTrue(config.getInt("rowsPerPage") == rows);
    }
}
