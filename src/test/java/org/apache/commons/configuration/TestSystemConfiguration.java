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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.configuration.ex.ConfigurationException;
import org.apache.commons.configuration.io.FileHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for {@code SystemConfiguration}.
 *
 * @author Emmanuel Bourg
 * @version $Id$
 */
public class TestSystemConfiguration
{
    /** An object for creating temporary files. */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testSystemConfiguration()
    {
        Properties props = System.getProperties();
        props.put("test.number", "123");

        Configuration conf = new SystemConfiguration();
        assertEquals("number", 123, conf.getInt("test.number"));
    }

    @Test
    public void testSetSystemProperties()
    {
        PropertiesConfiguration props = new PropertiesConfiguration();
        props.addProperty("test.name", "Apache");
        SystemConfiguration.setSystemProperties(props);
        assertEquals("System Properties", "Apache", System.getProperty("test.name"));
    }

    /**
     * Tests whether system properties can be set from a configuration file.
     */
    @Test
    public void testSetSystemPropertiesFromPropertiesFile()
            throws ConfigurationException, IOException
    {
        File file = folder.newFile("sys.properties");
        PropertiesConfiguration pconfig = new PropertiesConfiguration();
        FileHandler handler = new FileHandler(pconfig);
        pconfig.addProperty("fromFile", Boolean.TRUE);
        handler.setFile(file);
        handler.save();
        SystemConfiguration.setSystemProperties(handler.getBasePath(),
                handler.getFileName());
        SystemConfiguration sconf = new SystemConfiguration();
        assertTrue("Property from file not found", sconf.getBoolean("fromFile"));
    }

    /**
     * Tests whether the configuration can be used to change system properties.
     */
    @Test
    public void testChangeSystemProperties()
    {
        String testProperty = "someTest";
        SystemConfiguration config = new SystemConfiguration();
        config.setProperty(testProperty, "true");
        assertEquals("System property not changed", "true",
                System.getProperty(testProperty));
    }
}
