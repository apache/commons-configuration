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

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

/**
 * Tests the ConfigurationUtils class
 *
 */
public class TestConfigurationUtils extends TestCase
{
    protected Configuration configuration = new BaseConfiguration();

    public void testCompare()
    {
        assertEquals(
            "Get string representation of an empty configuration",
            "",
            ConfigurationUtils.toString(configuration));

        configuration.setProperty("one", "1");
        assertEquals(
            "Get string representation of a configuration",
            "one=1",
            ConfigurationUtils.toString(configuration));

        configuration.setProperty("two", "2");
        /*        assertEquals("Get string representation of a configuration", "one=1\ntwo=2" , ConfigurationUtils.toString(configuration));
        
                configuration.clearProperty("one");
                assertEquals("Get string representation of a configuration", "two=2" , ConfigurationUtils.toString(configuration));
                
                configuration.setProperty("one","1");
                assertEquals("Get string representation of a configuration", "two=2\none=1" , ConfigurationUtils.toString(configuration));
        */
    }

    public void testGetURL() throws Exception
    {
        assertEquals(
            "http://localhost:8080/webapp/config/config.xml",
            ConfigurationUtils
                .getURL(
                    "http://localhost:8080/webapp/config/baseConfig.xml",
                    "config.xml")
                .toString());
        assertEquals(
            "http://localhost:8080/webapp/config/config.xml",
            ConfigurationUtils
                .getURL(
                    "http://localhost:8080/webapp/baseConfig.xml",
                    "config/config.xml")
                .toString());
        URL url = ConfigurationUtils.getURL(null, "config.xml");
        assertEquals("file", url.getProtocol());
        assertEquals("", url.getHost());
        
        assertEquals(
            "http://localhost:8080/webapp/config/config.xml",
            ConfigurationUtils
                .getURL(
                    "ftp://ftp.server.com/downloads/baseConfig.xml",
                    "http://localhost:8080/webapp/config/config.xml")
                .toString());
        assertEquals(
            "http://localhost:8080/webapp/config/config.xml",
            ConfigurationUtils
                .getURL(null, "http://localhost:8080/webapp/config/config.xml")
                .toString());
        File absFile = new File("config.xml").getAbsoluteFile();
        assertEquals(
            absFile.toURL(),
            ConfigurationUtils.getURL(
                "http://localhost:8080/webapp/config/baseConfig.xml",
                absFile.getAbsolutePath()));
        assertEquals(
            absFile.toURL(),
            ConfigurationUtils.getURL(null, absFile.getAbsolutePath()));
        
		assertEquals(absFile.toURL(),
		ConfigurationUtils.getURL(absFile.getParent(), "config.xml"));
    }
}
