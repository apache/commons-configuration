/*
 * Copyright 2004 The Apache Software Foundation.
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

import java.util.Properties;

import junit.framework.TestCase;

/**
 * Tests for MapConfiguration.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 1.1 $, $Date: 2004/10/18 12:50:41 $
 */
public class TestSystemConfiguration extends TestCase
{
    public void testSystemConfiguration()
    {
        Properties props = System.getProperties();
        props.put("test.number", "123");

        Configuration conf = new SystemConfiguration();
        assertEquals("number", 123, conf.getInt("test.number"));
    }
}
