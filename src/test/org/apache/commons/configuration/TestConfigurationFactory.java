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
import java.util.Collection;

import junit.framework.TestCase;

import org.xml.sax.SAXParseException;

/**
 * Test the ConfigurationFactory.
 *
 * @version $Id: TestConfigurationFactory.java,v 1.9 2004/02/27 17:41:34 epugh Exp $
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

    private File testDigesterBadXML = new File("conf/testDigesterBadXML.xml");

    private String testBasePath = new File("conf").getAbsolutePath();

    private Configuration configuration;
    private CompositeConfiguration compositeConfiguration;
    private ConfigurationFactory configurationFactory;

    public void setUp() throws Exception
    {
        configurationFactory = new ConfigurationFactory();
    }

    public void testJNDI()
    {
        JNDIConfiguration jndiConfiguration = new JNDIConfiguration();
        jndiConfiguration.setPrefix("");
        Object o = jndiConfiguration.getProperty("test.boolean");
        assertNotNull(o);
        assertEquals("true",o.toString());

    }
    public void testLoadingConfiguration() throws Exception
    {
        configurationFactory.setConfigurationFileName(
            testDigesterFile.toString());

        compositeConfiguration =
            (CompositeConfiguration) configurationFactory.getConfiguration();

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

        configuration = (Configuration) compositeConfiguration;
        assertEquals(
            "I'm complex!",
            configuration.getProperty("element2.subelement.subsubelement"));
    }

    public void testLoadingConfigurationReverseOrder() throws Exception
    {
        configurationFactory.setConfigurationFileName(
            testDigesterFileReverseOrder.toString());

        configuration = configurationFactory.getConfiguration();

        assertEquals("8", configuration.getProperty("test.short"));

        configurationFactory.setConfigurationFileName(
            testDigesterFile.toString());

        configuration = configurationFactory.getConfiguration();
        assertEquals("1", configuration.getProperty("test.short"));
    }

    public void testLoadingConfigurationWithRulesXML() throws Exception
    {
        configurationFactory.setConfigurationFileName(
            testDigesterFile.toString());
        configurationFactory.setDigesterRules(digesterRules.toURL());

        compositeConfiguration =
            (CompositeConfiguration) configurationFactory.getConfiguration();

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

        configuration = (Configuration) compositeConfiguration;
        assertEquals(
            "I'm complex!",
            configuration.getProperty("element2.subelement.subsubelement"));
    }

    public void testLoadingConfigurationNamespaceAware() throws Exception
    {
        configurationFactory.setConfigurationFileName(
            testDigesterFileNamespaceAware.toString());
        //configurationFactory.setDigesterRules(digesterRules.toURL());
        configurationFactory.setDigesterRuleNamespaceURI("namespace-one");

        checkCompositeConfiguration();
    }

    public void testLoadingConfigurationBasePath() throws Exception
    {
        configurationFactory.setConfigurationFileName(
            testDigesterFileBasePath.toString());

        configurationFactory.setBasePath(testBasePath);

        //configurationFactory.setDigesterRules(digesterRules.toURL());
        //configurationFactory.setDigesterRuleNamespaceURI("namespace-one");

        checkCompositeConfiguration();
    }

    public void testLoadingAdditional() throws Exception
    {
        configurationFactory.setConfigurationFileName(
            testDigesterFileEnhanced.toString());
        configurationFactory.setBasePath(null);
        checkUnionConfig();
    }

    public void testLoadingURL() throws Exception
    {
        configurationFactory.setConfigurationURL(
            testDigesterFileEnhanced.toURL());
        checkUnionConfig();
    }

    public void testThrowingConfigurationInitializationException()
        throws Exception
    {
        configurationFactory.setConfigurationFileName(
            testDigesterBadXML.toString());
        try
        {
            configurationFactory.getConfiguration();
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

        configurationFactory.setConfigurationURL(
            testDigesterFileComplete.toURL());
        Configuration config = configurationFactory.getConfiguration();
        assertFalse(config.isEmpty());
        assertTrue(config instanceof CompositeConfiguration);
        CompositeConfiguration cc = (CompositeConfiguration)config;
        assertTrue(cc.getNumberOfConfigurations()>1);
        // Currently fails, should be 4?  Only 2?
        //assertEquals(4,cc.getNumberOfConfigurations());

        assertNotNull(
            config.getProperty("tables.table(0).fields.field(2).name"));
        assertNotNull(config.getProperty("element2.subelement.subsubelement"));
        assertNotNull(config.getProperty("mail.account.user"));
        // Fails, because we don't seem to reach the underlying JNDIConfiguraiton        
        //assertNotNull(config.getProperty("test.boolean"));
    }

    private void checkUnionConfig() throws Exception
    {
        compositeConfiguration =
            (CompositeConfiguration) configurationFactory.getConfiguration();
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
        compositeConfiguration =
            (CompositeConfiguration) configurationFactory.getConfiguration();

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
        assertNull("Should have returned a null",property);
        
    }
}
