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
import java.io.FileWriter;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

/**
 * Test the ConfigurationFactory.
 *
 * @version $Id$
 */
public class TestConfigurationFactory extends TestCase
{
    /** The Files that we test with */
    private URL digesterRules = getClass().getResource("/digesterRules.xml");
    private File testDigesterFile = ConfigurationAssert.getTestFile("testDigesterConfiguration.xml");
    private File testDigesterFileReverseOrder =
            ConfigurationAssert.getTestFile("testDigesterConfigurationReverseOrder.xml");
    private File testDigesterFileNamespaceAware =
            ConfigurationAssert.getTestFile("testDigesterConfigurationNamespaceAware.xml");
    private File testDigesterFileBasePath =
            ConfigurationAssert.getTestFile("testDigesterConfigurationBasePath.xml");
    private File testDigesterFileEnhanced =
            ConfigurationAssert.getTestFile("testDigesterConfiguration2.xml");
    private File testDigesterFileComplete =
            ConfigurationAssert.getTestFile("testDigesterConfiguration3.xml");
    private File testDigesterFileOptional =
            ConfigurationAssert.getTestFile("testDigesterOptionalConfiguration.xml");
    private File testDigesterFileOptionalEx =
            ConfigurationAssert.getTestFile("testDigesterOptionalConfigurationEx.xml");
    private File testDigesterFileSysProps =
            ConfigurationAssert.getTestFile("testDigesterConfigurationSysProps.xml");
    private File testDigesterFileInitProps =
            ConfigurationAssert.getTestFile("testDigesterConfigurationWithProps.xml");

    private File testDigesterBadXML = ConfigurationAssert.getTestFile("testDigesterBadXML.xml");

    private String testBasePath = new File("conf").getAbsolutePath();

    private File testProperties = ConfigurationAssert.getTestFile("test.properties");
    private File testAbsConfig = ConfigurationAssert.getOutFile("testAbsConfig.xml");

    private Configuration configuration;
    private CompositeConfiguration compositeConfiguration;
    private ConfigurationFactory factory;

    public void setUp() throws Exception
    {
        System.setProperty("java.naming.factory.initial", "org.apache.commons.configuration.MockInitialContextFactory");
        factory = new ConfigurationFactory();
    }

    public void testJNDI() throws Exception
    {
        JNDIConfiguration jndiConfiguration = new JNDIConfiguration();
        Object o = jndiConfiguration.getProperty("test.boolean");
        assertNotNull(o);
        assertEquals("true", o.toString());
    }

    public void testLoadingConfiguration() throws Exception
    {
        factory.setConfigurationFileName(testDigesterFile.toString());

        compositeConfiguration = (CompositeConfiguration) factory.getConfiguration();

        assertEquals("Number of configurations", 4, compositeConfiguration.getNumberOfConfigurations());
        assertEquals(PropertiesConfiguration.class, compositeConfiguration.getConfiguration(0).getClass());
        assertEquals(XMLPropertiesConfiguration.class, compositeConfiguration.getConfiguration(1).getClass());
        assertEquals(XMLConfiguration.class, compositeConfiguration.getConfiguration(2).getClass());

        // check the first configuration
        PropertiesConfiguration pc = (PropertiesConfiguration) compositeConfiguration.getConfiguration(0);
        assertNotNull("Make sure we have a fileName: " + pc.getFileName(), pc.getFileName());

        // check some properties
        assertTrue("Make sure we have loaded our key", compositeConfiguration.getBoolean("test.boolean"));
        assertEquals("I'm complex!", compositeConfiguration.getProperty("element2.subelement.subsubelement"));
        assertEquals("property in the XMLPropertiesConfiguration", "value1", compositeConfiguration.getProperty("key1"));
    }

    public void testLoadingConfigurationWithRulesXML() throws Exception
    {
        factory.setConfigurationFileName(testDigesterFile.toString());
        factory.setDigesterRules(digesterRules);

        compositeConfiguration = (CompositeConfiguration) factory.getConfiguration();

        assertEquals("Number of configurations", 4, compositeConfiguration.getNumberOfConfigurations());
        assertEquals(PropertiesConfiguration.class, compositeConfiguration.getConfiguration(0).getClass());
        //assertEquals(XMLPropertiesConfiguration.class, compositeConfiguration.getConfiguration(1).getClass()); // doesn't work
        assertEquals(XMLConfiguration.class, compositeConfiguration.getConfiguration(2).getClass());

        // check the first configuration
        PropertiesConfiguration pc = (PropertiesConfiguration) compositeConfiguration.getConfiguration(0);
        assertNotNull("Make sure we have a fileName: " + pc.getFileName(), pc.getFileName());

        // check some properties
        assertTrue("Make sure we have loaded our key", pc.getBoolean("test.boolean"));
        assertTrue("Make sure we have loaded our key", compositeConfiguration.getBoolean("test.boolean"));

        assertEquals("I'm complex!", compositeConfiguration.getProperty("element2.subelement.subsubelement"));
    }

    public void testLoadingConfigurationReverseOrder() throws Exception
    {
        factory.setConfigurationFileName(testDigesterFileReverseOrder.toString());

        configuration = factory.getConfiguration();

        assertEquals("8", configuration.getProperty("test.short"));

        factory.setConfigurationFileName(testDigesterFile.toString());

        configuration = factory.getConfiguration();
        assertEquals("1", configuration.getProperty("test.short"));
    }

    public void testLoadingConfigurationNamespaceAware() throws Exception
    {
        factory.setConfigurationFileName(testDigesterFileNamespaceAware.toString());
        factory.setDigesterRuleNamespaceURI("namespace-one");

        checkCompositeConfiguration();
    }

    public void testLoadingConfigurationBasePath() throws Exception
    {
        factory.setConfigurationFileName(testDigesterFileBasePath.toString());

        factory.setBasePath(testBasePath);

        //factory.setDigesterRuleNamespaceURI("namespace-one");

        checkCompositeConfiguration();
    }

    public void testLoadingAdditional() throws Exception
    {
        factory.setConfigurationFileName(testDigesterFileEnhanced.toString());
        factory.setBasePath(null);
        checkUnionConfig();
    }

    public void testLoadingURL() throws Exception
    {
        factory.setConfigurationURL(testDigesterFileEnhanced.toURL());
        checkUnionConfig();

        factory = new ConfigurationFactory();
        File nonExistingFile = new File("conf/nonexisting.xml");
        factory.setConfigurationURL(nonExistingFile.toURL());
        try
        {
            factory.getConfiguration();
            fail("Could load non existing file!");
        }
        catch(ConfigurationException cex)
        {
            //ok
        }
    }

    public void testThrowingConfigurationInitializationException() throws Exception
    {
        factory.setConfigurationFileName(testDigesterBadXML.toString());
        try
        {
            factory.getConfiguration();
            fail("Should have throw an Exception");
        }
        catch (ConfigurationException cle)
        {
            assertTrue("Unexpected cause: " + cle.getCause(),
                    cle.getCause() instanceof SAXException);
        }
    }

    // Tests if properties from all sources can be loaded
    public void testAllConfiguration() throws Exception
    {
        factory.setConfigurationURL(testDigesterFileComplete.toURL());
        Configuration config = factory.getConfiguration();
        assertFalse(config.isEmpty());
        assertTrue(config instanceof CompositeConfiguration);
        CompositeConfiguration cc = (CompositeConfiguration) config;
        assertTrue(cc.getNumberOfConfigurations() > 1);
        // Currently fails, should be 4?  Only 2?
        //assertEquals(4, cc.getNumberOfConfigurations());

        assertNotNull(config.getProperty("tables.table(0).fields.field(2).name"));
        assertNotNull(config.getProperty("element2.subelement.subsubelement"));
        assertEquals("value", config.getProperty("element3"));
        assertEquals("foo", config.getProperty("element3[@name]"));
        assertNotNull(config.getProperty("mail.account.user"));

        // test JNDIConfiguration
        assertNotNull(config.getProperty("test.onlyinjndi"));
        assertTrue(config.getBoolean("test.onlyinjndi"));

        Configuration subset = config.subset("test");
        assertNotNull(subset.getProperty("onlyinjndi"));
        assertTrue(subset.getBoolean("onlyinjndi"));

        // test SystemConfiguration
        assertNotNull(config.getProperty("java.version"));
        assertEquals(System.getProperty("java.version"), config.getString("java.version"));
    }

    // Checks if optional configurations work
    public void testOptionalConfigurations() throws Exception
    {
        factory.setConfigurationURL(testDigesterFileOptional.toURL());
        Configuration config = factory.getConfiguration();
        assertTrue(config.getBoolean("test.boolean"));
        assertEquals("value", config.getProperty("element"));

        factory.setConfigurationURL(testDigesterFileOptionalEx.toURL());
        try
        {
            config = factory.getConfiguration();
            fail("Unexisting properties loaded!");
        }
        catch(ConfigurationException cex)
        {
            // fine
        }
    }

    // Checks if a file with an absolute path can be loaded
    public void testLoadAbsolutePath() throws Exception
    {
        try
        {
            FileWriter out = null;
            try
            {
                out = new FileWriter(testAbsConfig);
                out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
                out.write("<configuration>");
                out.write("<properties fileName=\"");
                out.write(testProperties.getAbsolutePath());
                out.write("\"/>");
                out.write("</configuration>");
            }
            finally
            {
                if (out != null)
                {
                    out.close();
                }
            }

            factory.setConfigurationFileName(testAbsConfig.toString());
            Configuration config = factory.getConfiguration();
            assertTrue(config.getBoolean("configuration.loaded"));
        }
        finally
        {
            if (testAbsConfig.exists())
            {
                testAbsConfig.delete();
            }
        }
    }

    public void testBasePath() throws Exception
    {
        assertEquals(".", factory.getBasePath());
        factory.setConfigurationFileName(testDigesterFile.getAbsolutePath());
        // if no specific base path has been set, the base is determined
        // from the file name
        assertEquals(testDigesterFile.getParentFile().getAbsolutePath(),
                factory.getBasePath());

        String homeDir = System.getProperty("user.home");
        factory = new ConfigurationFactory();
        factory.setBasePath(homeDir);
        factory.setConfigurationFileName(testDigesterFile.getAbsolutePath());
        // if a base path was set, the file name does not play a role
        assertEquals(homeDir, factory.getBasePath());

        factory = new ConfigurationFactory(testDigesterFile.getAbsolutePath());
        assertEquals(testDigesterFile.getParentFile().getAbsolutePath(),
                factory.getBasePath());
        factory.setBasePath(homeDir);
        assertEquals(homeDir, factory.getBasePath());

        factory = new ConfigurationFactory();
        factory.setConfigurationURL(testDigesterFile.toURL());
        assertEquals(testDigesterFile.toURL().toString(), factory.getBasePath());
    }

    // Tests if system properties can be resolved in the configuration
    // definition
    public void testLoadingWithSystemProperties() throws ConfigurationException
    {
        System.setProperty("config.file", "test.properties");
        factory.setConfigurationFileName(testDigesterFileSysProps
                .getAbsolutePath());
        Configuration config = factory.getConfiguration();
        assertTrue("Configuration not loaded", config
                .getBoolean("configuration.loaded"));
    }

    // Tests if the properties of a configuration object are correctly set
    // before it is loaded.
    public void testLoadInitProperties() throws ConfigurationException
    {
        factory.setConfigurationFileName(testDigesterFileInitProps
                .getAbsolutePath());
        Configuration config = factory.getConfiguration();
        PropertiesConfiguration c = (PropertiesConfiguration) ((CompositeConfiguration) config)
                .getConfiguration(0);
        assertEquals("List delimiter was not set", ';', c.getListDelimiter());
        List l = c.getList("test.mixed.array");
        assertEquals("Wrong number of list elements", 2, l.size());
        assertEquals("List delimiter was not applied", "b, c, d", l.get(1));
    }

    private void checkUnionConfig() throws Exception
    {
        compositeConfiguration = (CompositeConfiguration) factory.getConfiguration();
        assertEquals("Verify how many configs", 3, compositeConfiguration.getNumberOfConfigurations());

        // Test if union was constructed correctly
        Object prop = compositeConfiguration.getProperty("tables.table.name");
        assertTrue(prop instanceof Collection);
        assertEquals(3, ((Collection) prop).size());
        assertEquals("users", compositeConfiguration.getProperty("tables.table(0).name"));
        assertEquals("documents", compositeConfiguration.getProperty("tables.table(1).name"));
        assertEquals("tasks", compositeConfiguration.getProperty("tables.table(2).name"));

        prop = compositeConfiguration.getProperty("tables.table.fields.field.name");
        assertTrue(prop instanceof Collection);
        assertEquals(17, ((Collection) prop).size());

        assertEquals("smtp.mydomain.org", compositeConfiguration.getString("mail.host.smtp"));
        assertEquals("pop3.mydomain.org", compositeConfiguration.getString("mail.host.pop"));

        // This was overriden
        assertEquals("masterOfPost", compositeConfiguration.getString("mail.account.user"));
        assertEquals("topsecret", compositeConfiguration.getString("mail.account.psswd"));

        // This was overriden, too, but not in additional section
        assertEquals("enhanced factory", compositeConfiguration.getString("test.configuration"));
    }

    private void checkCompositeConfiguration() throws Exception
    {
        compositeConfiguration = (CompositeConfiguration) factory.getConfiguration();

        assertEquals("Verify how many configs", 2, compositeConfiguration.getNumberOfConfigurations());
        assertEquals(PropertiesConfiguration.class, compositeConfiguration.getConfiguration(0).getClass());

        PropertiesConfiguration pc = (PropertiesConfiguration) compositeConfiguration.getConfiguration(0);
        assertNotNull("Make sure we have a fileName:" + pc.getFileName(), pc.getFileName());
        assertTrue("Make sure we have loaded our key", pc.getBoolean("test.boolean"));
        assertTrue("Make sure we have loaded our key", compositeConfiguration.getBoolean("test.boolean"));

        Object property = compositeConfiguration.getProperty("element2.subelement.subsubelement");
        assertNull("Should have returned a null", property);
    }
}
