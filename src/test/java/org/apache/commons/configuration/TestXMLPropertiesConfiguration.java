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

package org.apache.commons.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

/**
 * @author Emmanuel Bourg
 * @version $Id$
 */
public class TestXMLPropertiesConfiguration
{
    @Test
    public void testLoad() throws Exception
    {
        XMLPropertiesConfiguration conf = new XMLPropertiesConfiguration("test.properties.xml");

        assertEquals("header", "Description of the property list", conf.getHeader());

        assertFalse("The configuration is empty", conf.isEmpty());
        assertEquals("'key1' property", "value1", conf.getProperty("key1"));
        assertEquals("'key2' property", "value2", conf.getProperty("key2"));
        assertEquals("'key3' property", "value3", conf.getProperty("key3"));
    }

    @Test
    public void testSave() throws Exception
    {
        // load the configuration
        XMLPropertiesConfiguration conf = new XMLPropertiesConfiguration("test.properties.xml");

        // update the configuration
        conf.addProperty("key4", "value4");
        conf.clearProperty("key2");
        conf.setHeader("Description of the new property list");

        // save the configuration
        File saveFile = new File("target/test2.properties.xml");
        if (saveFile.exists())
        {
            assertTrue(saveFile.delete());
        }
        conf.save(saveFile);

        // reload the configuration
        XMLPropertiesConfiguration conf2 = new XMLPropertiesConfiguration(saveFile);

        // test the configuration
        assertEquals("header", "Description of the new property list", conf2.getHeader());

        assertFalse("The configuration is empty", conf2.isEmpty());
        assertEquals("'key1' property", "value1", conf2.getProperty("key1"));
        assertEquals("'key3' property", "value3", conf2.getProperty("key3"));
        assertEquals("'key4' property", "value4", conf2.getProperty("key4"));
    }
}
