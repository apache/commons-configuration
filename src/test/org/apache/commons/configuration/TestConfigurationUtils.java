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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import junitx.framework.ListAssert;

/**
 * Tests the ConfigurationUtils class
 *
 * @version $Revision$, $Date$
 */
public class TestConfigurationUtils extends TestCase
{
    protected Configuration config = new BaseConfiguration();

    public void testToString()
    {
        String lineSeparator = System.getProperty("line.separator");

        assertEquals("String representation of an empty configuration", "", ConfigurationUtils.toString(config));

        config.setProperty("one", "1");
        assertEquals("String representation of a configuration", "one=1", ConfigurationUtils.toString(config));

        config.setProperty("two", "2");
        assertEquals("String representation of a configuration", "one=1" + lineSeparator + "two=2" , ConfigurationUtils.toString(config));
        
        config.clearProperty("one");
        assertEquals("String representation of a configuration", "two=2" , ConfigurationUtils.toString(config));
                
        config.setProperty("one","1");
        assertEquals("String representation of a configuration", "two=2" + lineSeparator + "one=1" , ConfigurationUtils.toString(config));
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

    public void testGetBasePath() throws Exception
    {
        URL url = new URL("http://xyz.net/foo/bar.xml");
        assertEquals("base path of " + url, "http://xyz.net/foo/", ConfigurationUtils.getBasePath(url));

        url = new URL("http://xyz.net/foo/");
        assertEquals("base path of " + url, "http://xyz.net/foo/", ConfigurationUtils.getBasePath(url));

        url = new URL("http://xyz.net/foo");
        assertEquals("base path of " + url, "http://xyz.net/", ConfigurationUtils.getBasePath(url));

        url = new URL("http://xyz.net/");
        assertEquals("base path of " + url, "http://xyz.net/", ConfigurationUtils.getBasePath(url));

        url = new URL("http://xyz.net");
        assertEquals("base path of " + url, "http://xyz.net", ConfigurationUtils.getBasePath(url));
    }

    public void testGetFileName() throws Exception
    {
        assertEquals("file name for a null URL", null, ConfigurationUtils.getFileName(null));

        URL url = new URL("http://xyz.net/foo/");
        assertEquals("file for a directory URL " + url, null, ConfigurationUtils.getFileName(url));

        url = new URL("http://xyz.net/foo/bar.xml");
        assertEquals("file name for a valid URL " + url, "bar.xml", ConfigurationUtils.getFileName(url));
    }

    public void testCopy()
    {
        // create the source configuration
        Configuration conf1 = new BaseConfiguration();
        conf1.addProperty("key1", "value1");
        conf1.addProperty("key2", "value2");

        // create the target configuration
        Configuration conf2 = new BaseConfiguration();
        conf2.addProperty("key1", "value3");
        conf2.addProperty("key2", "value4");

        // copy the source configuration into the target configuration
        ConfigurationUtils.copy(conf1, conf2);

        assertEquals("'key1' property", "value1", conf2.getProperty("key1"));
        assertEquals("'key2' property", "value2", conf2.getProperty("key2"));
    }

    public void testAppend()
    {
        // create the source configuration
        Configuration conf1 = new BaseConfiguration();
        conf1.addProperty("key1", "value1");
        conf1.addProperty("key2", "value2");

        // create the target configuration
        Configuration conf2 = new BaseConfiguration();
        conf2.addProperty("key1", "value3");
        conf2.addProperty("key2", "value4");

        // append the source configuration to the target configuration
        ConfigurationUtils.append(conf1, conf2);

        List expected = new ArrayList();
        expected.add("value3");
        expected.add("value1");
        ListAssert.assertEquals("'key1' property", expected, conf2.getList("key1"));

        expected = new ArrayList();
        expected.add("value4");
        expected.add("value2");
        ListAssert.assertEquals("'key2' property", expected, conf2.getList("key2"));
    }
    
    public void testGetFile() throws Exception
    {
        File directory = new File("target");
        File reference = new File(directory, "test.txt").getAbsoluteFile();
        
        assertEquals(reference, ConfigurationUtils.getFile(null, reference.getAbsolutePath()));
        assertEquals(reference, ConfigurationUtils.getFile(directory.getAbsolutePath(), reference.getAbsolutePath()));
        assertEquals(reference, ConfigurationUtils.getFile(directory.getAbsolutePath(), reference.getName()));        
        assertEquals(reference, ConfigurationUtils.getFile(directory.toURL().toString(), reference.getName()));
        assertEquals(reference, ConfigurationUtils.getFile("invalid", reference.toURL().toString()));
    }

    public void testLocateWithNullTCCL() throws Exception
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(null);
            assertNull(ConfigurationUtils.locate("abase", "aname"));
            assertNotNull(ConfigurationUtils.locate("test.xml"));
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    /**
     * Tests converting a configuration into a hierarchical one.
     */
    public void testConvertToHierarchical()
    {
        Configuration conf = new BaseConfiguration();
        for (int i = 0; i < 10; i++)
        {
            conf.addProperty("test" + i, "value" + i);
            conf.addProperty("test.list", "item" + i);
        }

        HierarchicalConfiguration hc = ConfigurationUtils
                .convertToHierarchical(conf);
        for (Iterator it = conf.getKeys(); it.hasNext();)
        {
            String key = (String) it.next();
            assertEquals("Wrong value for key " + key, conf.getProperty(key),
                    hc.getProperty(key));
        }
    }

    /**
     * Tests converting a configuration into a hierarchical one that is already
     * hierarchical.
     */
    public void testConvertHierarchicalToHierarchical()
    {
        Configuration conf = new HierarchicalConfiguration();
        conf.addProperty("test", "yes");
        assertSame("Wrong configuration returned", conf, ConfigurationUtils
                .convertToHierarchical(conf));
    }

    /**
     * Tests converting a null configuration to a hierarchical one. The result
     * should be null, too.
     */
    public void testConvertNullToHierarchical()
    {
        assertNull("Wrong conversion result for null config",
                ConfigurationUtils.convertToHierarchical(null));
    }

    /**
     * Tests cloning a configuration that supports this operation.
     */
    public void testCloneConfiguration()
    {
        HierarchicalConfiguration conf = new HierarchicalConfiguration();
        conf.addProperty("test", "yes");
        HierarchicalConfiguration copy = (HierarchicalConfiguration) ConfigurationUtils
                .cloneConfiguration(conf);
        assertNotSame("Same object was returned", conf, copy);
        assertEquals("Property was not cloned", "yes", copy.getString("test"));
    }

    /**
     * Tests cloning a configuration that does not support this operation. This
     * should cause an exception.
     */
    public void testCloneConfigurationNotSupported()
    {
        Configuration myNonCloneableConfig = new NonCloneableConfiguration();
        try
        {
            ConfigurationUtils.cloneConfiguration(myNonCloneableConfig);
            fail("Could clone non cloneable config!");
        }
        catch (ConfigurationRuntimeException crex)
        {
            // ok
        }
    }

    /**
     * Tests cloning a <b>null</b> configuration.
     */
    public void testCloneConfigurationNull()
    {
        assertNull("Wrong return value", ConfigurationUtils
                .cloneConfiguration(null));
    }
}
