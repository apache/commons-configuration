package org.apache.commons.configuration;

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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.apache.commons.collections.ExtendedProperties;


/**
 * Tests the ConfigurationConverter class
 *
 * @version $Id: TestConfigurationConverter.java,v 1.4 2004/02/27 17:41:34 epugh Exp $
 */
public class TestConfigurationConverter extends TestCase
{
    protected Configuration config = new BaseConfiguration();

    public void testConverter()
    {
        config.setProperty("string", "teststring");
        config.setProperty("int", "123");
        List list = new ArrayList();
        list.add("item 1");
        list.add("item 2");
        config.setProperty("list", list);

        ExtendedProperties ep = ConfigurationConverter.getExtendedProperties(config);


        assertEquals("This returns 'teststring'", ep.getString("string"),
                "teststring");
        List v = ep.getVector("list");
        assertEquals("This returns 'item 1'", (String) v.get(0), "item 1");
        assertEquals("This returns 123", ep.getInt("int"), 123);

        Configuration c = ConfigurationConverter.getConfiguration(ep);


        assertEquals("This returns 'teststring'", c.getString("string"),
                "teststring");
        List v1 = c.getList("list");
        assertEquals("This returns 'item 1'", (String) v1.get(0), "item 1");
        assertEquals("This returns 123", c.getInt("int"), 123);
    }
}
