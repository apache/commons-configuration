/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.net.URL;

import junit.framework.TestCase;

import org.xml.sax.SAXParseException;

/**
 * Test the ConfigurationFactory.
 *
 * @version $Id$
 */
public class TestConfigurationFactory extends TestCase
{
    /** The Files that we test with */
    private File digesterRules = new File("conf/digesterRules.xml");
    private File testDigesterFile =
            new File("conf/testDigesterConfiguration.xml");
    private File testDigesterFileReverseOrder =
            new File("conf/testDigesterConfigurationReverseOrder.xml");
    private File testDigesterFileNamespaceAware =
            new File("conf/testDigesterConfigurationNamespaceAware.xml");
    private File testDigesterFileBasePath =
            new File("conf/testDigesterConfigurationBasePath.xml");
    private File testDigesterFileEnhanced =
            new File("conf/testDigesterConfiguration2.xml");
    private File testDigesterFileComplete =
            new File("conf/testDigesterConfiguration3.xml");
    private File testDigesterFileOptional =
            new File("conf/testDigesterOptionalConfiguration.xml");
    private File testDigesterFileOptionalEx =
            new File("conf/testDigesterOptionalConfigurationEx.xml");

    private File testDigesterBadXML = new File("conf/testDigesterBadXML.xml");

    private String testBasePath = new File("conf").getAbsolutePath();
    
    private File testProperties = new File("conf/test.properties");
    private File testAbsConfig = new File("target/testAbsConfig.xml");

    private Configuration configuration;
    private CompositeConfiguration compositeConfiguration;
    private ConfigurationFactory factory;

    public void setUp() throws Exception
    {
        System.setProperty("java.naming.factory.initial", "org.apache.commons.configuration.MockStaticMemoryInitialContextFactory");
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
        factory.setConfigurationFileName(
                testDigesterFile.toString());

        compositeConfiguration =
                (CompositeConfiguration) factory.getConfiguration();

        assertEquals(
                "Verify how many configs",
                3,
                compositeConfiguration.getNumberOfConfigurations());
        assertEquals(
                PropertiesConfiguration.class,
                compositeConfiguration.getConfiguration(0).getClass());
        PropertiesConfiguration pc =
                (PropertiesConfiguration) compositeConfiguration.getConfiguration(
                        0);

        assertNotNull(
                "Make sure we have a fileName:" + pc.getFileName(),
                pc.getFileName());

        assertTrue(
                "Make sure we have loades our key",
                compositeConfiguration.getBoolean("test.boolean"));
        assertEquals(
                "I'm complex!",
                compositeConfiguration.getProperty(
                        "element2.subelement.subsubelement"));

        configuration = compositeConfiguration;
        assertEquals(
                "I'm complex!",
                configuration.getProperty("element2.subelement.subsubelement"));
    }

    public void testLoadingConfigurationReverseOrder() throws Exception
    {
        factory.setConfigurationFileName(
                testDigesterFileReverseOrder.toString());

        configuration = factory.getConfiguration();

        assertEquals("8", configuration.getProperty("test.short"));

        factory.setConfigurationFileName(testDigesterFile.toString());

        configuration = factory.getConfiguration();
        assertEquals("1", configuration.getProperty("test.short"));
    }

    public void testLoadingConfigurationWithRulesXML() throws Exception
    {
        factory.setConfigurationFileName(testDigesterFile.toString());
        factory.setDigesterRules(digesterRules.toURL());

        compositeConfiguration = (CompositeConfiguration) factory.getConfiguration();

        assertEquals(
                "Verify how many configs",
                3,
                compositeConfiguration.getNumberOfConfigurations());

        assertEquals(
                PropertiesConfiguration.class,
                compositeConfiguration.getConfiguration(0).getClass());

        PropertiesConfiguration pc =
                (PropertiesConfiguration) compositeConfiguration.getConfiguration(
                        0);
        assertNotNull(
                "Make sure we have a fileName:" + pc.getFileName(),
                pc.getFileName());
        assertTrue(
                "Make sure we have loaded our key",
                pc.getBoolean("test.boolean"));

        assertTrue(
                "Make sure we have loaded our key",
                compositeConfiguration.getBoolean("test.boolean"));

        assertEquals(
                "I'm complex!",
                compositeConfiguration.getProperty(
                        "element2.subelement.subsubelement"));

        configuration = compositeConfiguration;
        assertEquals(
                "I'm complex!",
                configuration.getProperty("element2.subelement.subsubelement"));
    }

    public void testLoadingConfigurationNamespaceAware() throws Exception
    {
        factory.setConfigurationFileName(testDigesterFileNamespaceAware.toString());
        //factory.setDigesterRules(digesterRules.toURL());
        factory.setDigesterRuleNamespaceURI("namespace-one");

        checkCompositeConfiguration();
    }

    public void testLoadingConfigurationBasePath() throws Exception
    {
        factory.setConfigurationFileName(testDigesterFileBasePath.toString());

        factory.setBasePath(testBasePath);

        //factory.setDigesterRules(digesterRules.toURL());
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
    }

    public void testLoadingFromJAR() throws Exception
    {
        URL url = Thread.currentThread().getContextClassLoader().getResource("config-jar.xml");
        assertNotNull("config-jar.xml not found on the classpath", url);
        factory.setConfigurationURL(url);

        Configuration conf = factory.getConfiguration();
        assertFalse("The configuration is empty", conf.isEmpty());
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
            assertTrue(cle.getCause() instanceof SAXParseException);
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

    private void checkUnionConfig() throws Exception
    {
        compositeConfiguration = (CompositeConfiguration) factory.getConfiguration();
        assertEquals(
                "Verify how many configs",
                3,
                compositeConfiguration.getNumberOfConfigurations());

        // Test if union was constructed correctly
        Object prop = compositeConfiguration.getProperty("tables.table.name");
        assertTrue(prop instanceof Collection);
        assertEquals(3, ((Collection) prop).size());
        assertEquals(
                "users",
                compositeConfiguration.getProperty("tables.table(0).name"));
        assertEquals(
                "documents",
                compositeConfiguration.getProperty("tables.table(1).name"));
        assertEquals(
                "tasks",
                compositeConfiguration.getProperty("tables.table(2).name"));

        prop =
                compositeConfiguration.getProperty(
                        "tables.table.fields.field.name");
        assertTrue(prop instanceof Collection);
        assertEquals(17, ((Collection) prop).size());

        assertEquals(
                "smtp.mydomain.org",
                compositeConfiguration.getString("mail.host.smtp"));
        assertEquals(
                "pop3.mydomain.org",
                compositeConfiguration.getString("mail.host.pop"));

        // This was overriden
        assertEquals(
                "masterOfPost",
                compositeConfiguration.getString("mail.account.user"));
        assertEquals(
                "topsecret",
                compositeConfiguration.getString("mail.account.psswd"));

        // This was overriden, too, but not in additional section
        assertEquals(
                "enhanced factory",
                compositeConfiguration.getString("test.configuration"));
    }

    private void checkCompositeConfiguration() throws Exception
    {
        compositeConfiguration = (CompositeConfiguration) factory.getConfiguration();

        assertEquals(
                "Verify how many configs",
                2,
                compositeConfiguration.getNumberOfConfigurations());

        assertEquals(
                PropertiesConfiguration.class,
                compositeConfiguration.getConfiguration(0).getClass());

        PropertiesConfiguration pc =
                (PropertiesConfiguration) compositeConfiguration.getConfiguration(
                        0);
        assertNotNull(
                "Make sure we have a fileName:" + pc.getFileName(),
                pc.getFileName());
        assertTrue(
                "Make sure we have loaded our key",
                pc.getBoolean("test.boolean"));

        assertTrue(
                "Make sure we have loaded our key",
                compositeConfiguration.getBoolean("test.boolean"));


        Object property = compositeConfiguration.getProperty(
                "element2.subelement.subsubelement");
        assertNull("Should have returned a null", property);
    }
}
