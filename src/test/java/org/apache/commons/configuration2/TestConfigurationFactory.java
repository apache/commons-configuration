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
import java.io.FileWriter;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration2.combined.CombinedConfiguration;
import org.xml.sax.SAXException;

/**
 * Test the DefaultConfigurationBuilder (former test cases for ConfigurationFactory).
 *
 * @version $Id$
 */
public class TestConfigurationFactory extends TestCase
{
    /** The Files that we test with */
    private File testDigesterFile =
        ConfigurationAssert.getTestFile("testDigesterConfiguration.xml");
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

    private String testBasePath = ConfigurationAssert.TEST_DIR.getAbsolutePath();

    private File testProperties = ConfigurationAssert.getTestFile("test.properties");
    private File testAbsConfig = ConfigurationAssert.getOutFile("testAbsConfig.xml");

    private DefaultConfigurationBuilder factory;

    @Override
    public void setUp() throws Exception
    {
        System.setProperty("java.naming.factory.initial", "org.apache.commons.configuration2.MockInitialContextFactory");
        factory = new DefaultConfigurationBuilder();
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
        factory.setFileName(testDigesterFile.toString());

        CombinedConfiguration configuration = (CombinedConfiguration) factory.getConfiguration();

        assertEquals("Number of configurations", 3, configuration.getNumberOfConfigurations());
        assertEquals(PropertiesConfiguration.class, configuration.getConfiguration(0).getClass());
        assertEquals(XMLPropertiesConfiguration.class, configuration.getConfiguration(1).getClass());
        assertEquals(XMLConfiguration.class, configuration.getConfiguration(2).getClass());

        // check the first configuration
        PropertiesConfiguration pc = (PropertiesConfiguration) configuration.getConfiguration(0);
        assertNotNull("Make sure we have a fileName: " + pc.getFileName(), pc.getFileName());

        // check some properties
        assertTrue("Make sure we have loaded our key", configuration.getBoolean("test.boolean"));
        assertEquals("I'm complex!", configuration.getProperty("element2.subelement.subsubelement"));
        assertEquals("property in the XMLPropertiesConfiguration", "value1", configuration.getProperty("key1"));
    }

    public void testLoadingConfigurationWithRulesXML() throws Exception
    {
        factory.setFileName(testDigesterFile.toString());
        //factory.setDigesterRules(digesterRules.toURL());

        CombinedConfiguration configuration = (CombinedConfiguration) factory.getConfiguration();

        assertEquals("Number of configurations", 3, configuration.getNumberOfConfigurations());
        assertEquals(PropertiesConfiguration.class, configuration.getConfiguration(0).getClass());
        //assertEquals(XMLPropertiesConfiguration.class, configuration.getConfiguration(1).getClass()); // doesn't work
        assertEquals(XMLConfiguration.class, configuration.getConfiguration(2).getClass());

        // check the first configuration
        PropertiesConfiguration pc = (PropertiesConfiguration) configuration.getConfiguration(0);
        assertNotNull("Make sure we have a fileName: " + pc.getFileName(), pc.getFileName());

        // check some properties
        assertTrue("Make sure we have loaded our key", pc.getBoolean("test.boolean"));
        assertTrue("Make sure we have loaded our key", configuration.getBoolean("test.boolean"));

        assertEquals("I'm complex!", configuration.getProperty("element2.subelement.subsubelement"));
    }

    public void testLoadingConfigurationReverseOrder() throws Exception
    {
        factory.setFile(testDigesterFileReverseOrder);

        Configuration configuration = factory.getConfiguration();

        assertEquals("8", configuration.getProperty("test.short"));

        factory.clear();
        factory.setFile(testDigesterFile);

        configuration = factory.getConfiguration();
        assertEquals("1", configuration.getProperty("test.short"));
    }

    public void testLoadingConfigurationNamespaceAware() throws Exception
    {
        factory.setFile(testDigesterFileNamespaceAware);
        //factory.setDigesterRules(digesterRules.toURL());
        //factory.setDigesterRuleNamespaceURI("namespace-one");

        // todo DefaultConfigurationBuilder doesn't support namespaces

        //checkCombinedConfiguration();
    }

    public void testLoadingConfigurationBasePath() throws Exception
    {
        factory.setFile(testDigesterFileBasePath);

        factory.setBasePath(testBasePath);

        //factory.setDigesterRules(digesterRules.toURL());
        //factory.setDigesterRuleNamespaceURI("namespace-one");

        checkCombinedConfiguration();
    }

    public void testLoadingAdditional() throws Exception
    {
        factory.setFileName(testDigesterFileEnhanced.toString());
        factory.setBasePath(null);
        checkUnionConfig();
    }

    public void testLoadingURL() throws Exception
    {
        factory.setURL(testDigesterFileEnhanced.toURL());
        checkUnionConfig();

        factory = new DefaultConfigurationBuilder();
        File nonExistingFile = new File("conf/nonexisting.xml");
        factory.setURL(nonExistingFile.toURL());
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
        factory.setFileName(testDigesterBadXML.toString());
        try
        {
            factory.getConfiguration();
            fail("Should have throw an Exception");
        }
        catch (ConfigurationException cle)
        {
            assertTrue("Unexpected cause: " + cle.getCause(), cle.getCause() instanceof SAXException);
        }
    }

    // Tests if properties from all sources can be loaded
    public void testAllConfiguration() throws Exception
    {
        factory.setURL(testDigesterFileComplete.toURL());
        Configuration config = factory.getConfiguration();
        assertFalse(config.isEmpty());
        assertTrue(config instanceof CombinedConfiguration);
        // Currently fails
        //assertTrue(((CombinedConfiguration) config).getNumberOfConfigurations() > 1);

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
        assertNotNull(config.getProperty("java..version"));
        assertEquals(System.getProperty("java.version"), config.getString("java..version"));
    }

    // Checks if optional configurations work
    public void testOptionalConfigurations() throws Exception
    {
        factory.setURL(testDigesterFileOptional.toURL());
        Configuration config = factory.getConfiguration();
        assertTrue(config.getBoolean("test.boolean"));
        assertEquals("value", config.getProperty("element"));

        factory.setURL(testDigesterFileOptionalEx.toURL());
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

            factory.setFileName(testAbsConfig.toString());
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
        assertEquals(null, factory.getBasePath());
        factory.setBasePath(testDigesterFile.getParentFile().getAbsolutePath());
        factory.setFileName(testDigesterFile.getAbsolutePath());
        // if no specific base path has been set, the base is determined
        // from the file name
        assertEquals(testDigesterFile.getParentFile().getAbsolutePath(), factory.getBasePath());

        String homeDir = System.getProperty("user.home");
        factory = new DefaultConfigurationBuilder();
        factory.setBasePath(homeDir);
        factory.setFileName(testDigesterFile.getAbsolutePath());
        // if a base path was set, the file name does not play a role
        assertEquals(homeDir, factory.getBasePath());

        factory = new DefaultConfigurationBuilder(testDigesterFile.getAbsolutePath());
        factory.setBasePath(testDigesterFile.getParentFile().getAbsolutePath());
        assertEquals(testDigesterFile.getParentFile().getAbsolutePath(), factory.getBasePath());
        factory.setBasePath(homeDir);
        assertEquals(homeDir, factory.getBasePath());

        factory = new DefaultConfigurationBuilder();
        factory.setURL(testDigesterFile.toURL());
        assertEquals(testDigesterFile.getParentFile().toURI(), new URI(factory.getBasePath()));
    }

    // Tests if system properties can be resolved in the configuration
    // definition
    public void testLoadingWithSystemProperties() throws ConfigurationException
    {
        System.setProperty("config.file", "test.properties");
        factory.setFile(testDigesterFileSysProps);
        Configuration config = factory.getConfiguration();
        assertTrue("Configuration not loaded", config.getBoolean("configuration.loaded"));
    }

    // Tests if the properties of a configuration object are correctly set
    // before it is loaded.
    public void testLoadInitProperties() throws ConfigurationException
    {
        factory.setFile(testDigesterFileInitProps);
        Configuration config = factory.getConfiguration();
        PropertiesConfiguration c = (PropertiesConfiguration) ((CombinedConfiguration) config).getConfiguration(0);
        assertEquals("List delimiter was not set", ';', c.getListDelimiter());
        List l = c.getList("test.mixed.array");
        assertEquals("Wrong number of list elements", 2, l.size());
        assertEquals("List delimiter was not applied", "b, c, d", l.get(1));
    }

    private void checkUnionConfig() throws Exception
    {
        CombinedConfiguration configuration = (CombinedConfiguration) factory.getConfiguration();
        assertEquals("Verify how many configs", 2, configuration.getNumberOfConfigurations());

        // Test if union was constructed correctly
        Object prop = configuration.getProperty("tables.table.name");
        assertTrue(prop instanceof Collection);
        assertEquals(3, ((Collection) prop).size());
        assertEquals("users", configuration.getProperty("tables.table(0).name"));
        assertEquals("documents", configuration.getProperty("tables.table(1).name"));
        assertEquals("tasks", configuration.getProperty("tables.table(2).name"));

        prop = configuration.getProperty("tables.table.fields.field.name");
        assertTrue(prop instanceof Collection);
        assertEquals(17, ((Collection) prop).size());

        assertEquals("smtp.mydomain.org", configuration.getString("mail.host.smtp"));
        assertEquals("pop3.mydomain.org", configuration.getString("mail.host.pop"));

        // This was overriden
        assertEquals("masterOfPost", configuration.getString("mail.account.user"));
        assertEquals("topsecret", configuration.getString("mail.account.psswd"));

        // This was overriden, too, but not in additional section
        assertEquals("enhanced factory", configuration.getString("test.configuration"));
    }

    private void checkCombinedConfiguration() throws Exception
    {
        CombinedConfiguration configuration = (CombinedConfiguration) factory.getConfiguration();

        assertEquals("Verify how many configs", 1, configuration.getNumberOfConfigurations());
        assertEquals(PropertiesConfiguration.class, configuration.getConfiguration(0).getClass());

        PropertiesConfiguration pc = (PropertiesConfiguration) configuration.getConfiguration(0);
        assertNotNull("Make sure we have a fileName:" + pc.getFileName(), pc.getFileName());
        assertTrue("Make sure we have loaded our key", pc.getBoolean("test.boolean"));
        assertTrue("Make sure we have loaded our key", configuration.getBoolean("test.boolean"));

        Object property = configuration.getProperty("element2.subelement.subsubelement");
        assertNull("Should have returned a null", property);
    }
}
