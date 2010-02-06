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

package org.apache.commons.configuration2;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junitx.framework.ListAssert;

import org.apache.commons.configuration2.expr.ExpressionEngine;
import org.apache.commons.configuration2.expr.def.DefaultExpressionEngine;
import org.apache.commons.configuration2.flat.BaseConfiguration;

import com.mockobjects.dynamic.Mock;

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
            absFile.toURI().toURL(),
            ConfigurationUtils.getURL(
                "http://localhost:8080/webapp/config/baseConfig.xml",
                absFile.getAbsolutePath()));
        assertEquals(
            absFile.toURI().toURL(),
            ConfigurationUtils.getURL(null, absFile.getAbsolutePath()));

        assertEquals(absFile.toURI().toURL(),
        ConfigurationUtils.getURL(absFile.getParent(), "config.xml"));
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

        List<Object> expected = new ArrayList<Object>();
        expected.add("value3");
        expected.add("value1");
        ListAssert.assertEquals("'key1' property", expected, conf2.getList("key1"));

        expected = new ArrayList<Object>();
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
        assertEquals(reference, ConfigurationUtils.getFile(directory.toURI().toURL().toString(), reference.getName()));
        assertEquals(reference, ConfigurationUtils.getFile("invalid", reference.toURI().toURL().toString()));
        assertEquals(reference, ConfigurationUtils.getFile(
                "jar:file:/C:/myjar.jar!/my-config.xml/someprops.properties",
                reference.getAbsolutePath()));
    }

    public void testLocateWithNullTCCL() throws Exception
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(null);
            assertNull(ConfigurationUtils.locate("abase", "aname"));
            // This assert fails when maven 2 is used, so commented out
            //assertNotNull(ConfigurationUtils.locate("test.xml"));
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    /**
     * Tests converting a configuration to a hierarchical one using a specific
     * expression engine.
     */
    public void testConvertToHierarchicalEngine()
    {
        Configuration conf = new BaseConfiguration();
        conf.addProperty("test(a)", Boolean.TRUE);
        conf.addProperty("test(b)", Boolean.FALSE);
        DefaultExpressionEngine engine = new DefaultExpressionEngine();
        engine.setIndexStart("[");
        engine.setIndexEnd("]");
        AbstractHierarchicalConfiguration<?> hc = ConfigurationUtils.convertToHierarchical(conf, engine);
        assertTrue("Wrong value for test(a)", hc.getBoolean("test(a)"));
        assertFalse("Wrong value for test(b)", hc.getBoolean("test(b)"));
    }

    /**
     * Tests converting an already hierarchical configuration using an
     * expression engine. The new engine should be set.
     */
    public void testConvertHierarchicalToHierarchicalEngine()
    {
        InMemoryConfiguration hc = new InMemoryConfiguration();
        ExpressionEngine engine = new DefaultExpressionEngine();
        assertSame("Created new configuration", hc, ConfigurationUtils.convertToHierarchical(hc, engine));
        assertSame("Engine was not set", engine, hc.getExpressionEngine());
    }

    /**
     * Tests converting an already hierarchical configuration using a null
     * expression engine. In this case the expression engine of the
     * configuration should not be touched.
     */
    public void testConvertHierarchicalToHierarchicalNullEngine()
    {
        InMemoryConfiguration hc = new InMemoryConfiguration();
        ExpressionEngine engine = new DefaultExpressionEngine();
        hc.setExpressionEngine(engine);
        assertSame("Created new configuration", hc, ConfigurationUtils.convertToHierarchical(hc, null));
        assertSame("Expression engine was changed", engine, hc.getExpressionEngine());
    }

    /**
     * Tests converting a configuration to a hierarchical one that contains a
     * property with multiple values. This test is related to CONFIGURATION-346.
     */
    public void testConvertToHierarchicalMultiValues()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty("test", "1,2,3");
        AbstractHierarchicalConfiguration<?> hc = ConfigurationUtils.convertToHierarchical(config, null);
        assertEquals("Wrong value 1", 1, hc.getInt("test(0)"));
        assertEquals("Wrong value 2", 2, hc.getInt("test(1)"));
        assertEquals("Wrong value 3", 3, hc.getInt("test(2)"));
    }

    /**
     * Tests cloning a configuration that supports this operation.
     */
    public void testCloneConfiguration()
    {
        InMemoryConfiguration conf = new InMemoryConfiguration();
        conf.addProperty("test", "yes");
        InMemoryConfiguration copy = (InMemoryConfiguration) ConfigurationUtils
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

    /**
     * Tests whether runtime exceptions can be enabled.
     */
    public void testEnableRuntimeExceptions()
    {
        PropertiesConfiguration config = new PropertiesConfiguration()
        {
            @Override
            protected void addPropertyDirect(String key, Object value)
            {
                // always simulate an exception
                fireError(EVENT_ADD_PROPERTY, key, value, new RuntimeException(
                        "A faked exception!"));
            }
        };
        config.clearErrorListeners();
        ConfigurationUtils.enableRuntimeExceptions(config);
        try
        {
            config.addProperty("test", "testValue");
            fail("No runtime exception was thrown!");
        }
        catch (ConfigurationRuntimeException crex)
        {
            // ok
        }
    }

    /**
     * Tries to enable runtime exceptions for a configurtion that does not
     * inherit from EventSource. This should cause an exception.
     */
    public void testEnableRuntimeExceptionsInvalid()
    {
        try
        {
            ConfigurationUtils
                    .enableRuntimeExceptions((Configuration) new Mock(
                            Configuration.class).proxy());
            fail("Could enable exceptions for non EventSource configuration!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tries to enable runtime exceptions for a null configuration. This should
     * cause an exception.
     */
    public void testEnableRuntimeExceptionsNull()
    {
        try
        {
            ConfigurationUtils.enableRuntimeExceptions(null);
            fail("Could enable exceptions for a null configuration!");
        }
        catch (IllegalArgumentException iex)
        {
            //ok
        }
    }
}
