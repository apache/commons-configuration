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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.beanutils.BeanHelper;
import org.apache.commons.configuration.event.ConfigurationListenerTestImpl;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for DefaultConfigurationBuilder.
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 */
public class TestDefaultConfigurationBuilder
{
    /** Test configuration definition file. */
    private static final File TEST_FILE = ConfigurationAssert
            .getTestFile("testDigesterConfiguration.xml");

    private static final File ADDITIONAL_FILE = ConfigurationAssert
            .getTestFile("testDigesterConfiguration2.xml");

    private static final File OPTIONAL_FILE = ConfigurationAssert
            .getTestFile("testDigesterOptionalConfiguration.xml");

    private static final File OPTIONALEX_FILE = ConfigurationAssert
            .getTestFile("testDigesterOptionalConfigurationEx.xml");

    private static final File MULTI_FILE = ConfigurationAssert
            .getTestFile("testDigesterConfiguration3.xml");

    private static final File INIT_FILE = ConfigurationAssert
            .getTestFile("testComplexInitialization.xml");

    private static final File CLASS_FILE = ConfigurationAssert
            .getTestFile("testExtendedClass.xml");

    private static final File PROVIDER_FILE = ConfigurationAssert
            .getTestFile("testConfigurationProvider.xml");

    private static final File EXTENDED_PROVIDER_FILE = ConfigurationAssert
            .getTestFile("testExtendedXMLConfigurationProvider.xml");

    private static final File GLOBAL_LOOKUP_FILE = ConfigurationAssert
            .getTestFile("testGlobalLookup.xml");

    private static final File SYSTEM_PROPS_FILE = ConfigurationAssert
            .getTestFile("testSystemProperties.xml");

    private static final File VALIDATION_FILE = ConfigurationAssert
            .getTestFile("testValidation.xml");

    private static final File VALIDATION3_FILE = ConfigurationAssert
            .getTestFile("testValidation3.xml");

    private static final File MULTI_TENENT_FILE = ConfigurationAssert
            .getTestFile("testMultiTenentConfigurationBuilder.xml");

    private static final File EXPRESSION_FILE = ConfigurationAssert
            .getTestFile("testExpression.xml");

    /** Constant for the name of an optional configuration.*/
    private static final String OPTIONAL_NAME = "optionalConfig";

    /** Stores the object to be tested. */
    DefaultConfigurationBuilder factory;

    @Before
    public void setUp() throws Exception
    {
        System
                .setProperty("java.naming.factory.initial",
                        "org.apache.commons.configuration.MockInitialContextFactory");
        System.setProperty("test_file_xml", "test.xml");
        System.setProperty("test_file_combine", "testcombine1.xml");
        factory = new DefaultConfigurationBuilder();
        factory.clearErrorListeners();  // avoid exception messages
    }

    /**
     * Tests the isReservedNode() method of ConfigurationDeclaration.
     */
    @Test
    public void testConfigurationDeclarationIsReserved()
    {
        DefaultConfigurationBuilder.ConfigurationDeclaration decl = new DefaultConfigurationBuilder.ConfigurationDeclaration(
                factory, factory);
        DefaultConfigurationNode parent = new DefaultConfigurationNode();
        DefaultConfigurationNode nd = new DefaultConfigurationNode("at");
        parent.addAttribute(nd);
        assertTrue("Attribute at not recognized", decl.isReservedNode(nd));
        nd = new DefaultConfigurationNode("optional");
        parent.addAttribute(nd);
        assertTrue("Attribute optional not recognized", decl.isReservedNode(nd));
        nd = new DefaultConfigurationNode("config-class");
        parent.addAttribute(nd);
        assertTrue("Inherited attribute not recognized", decl
                .isReservedNode(nd));
        nd = new DefaultConfigurationNode("different");
        parent.addAttribute(nd);
        assertFalse("Wrong reserved attribute", decl.isReservedNode(nd));
        nd = new DefaultConfigurationNode("at");
        parent.addChild(nd);
        assertFalse("Node type not evaluated", decl.isReservedNode(nd));
    }

    /**
     * Tests if the at attribute is correctly detected as reserved attribute.
     */
    @Test
    public void testConfigurationDeclarationIsReservedAt()
    {
        checkOldReservedAttribute("at");
    }

    /**
     * Tests if the optional attribute is correctly detected as reserved
     * attribute.
     */
    @Test
    public void testConfigurationDeclarationIsReservedOptional()
    {
        checkOldReservedAttribute("optional");
    }

    /**
     * Tests if special reserved attributes are recognized by the
     * isReservedNode() method. For compatibility reasons the attributes "at"
     * and "optional" are also treated as reserved attributes, but only if there
     * are no corresponding attributes with the "config-" prefix.
     *
     * @param name the attribute name
     */
    private void checkOldReservedAttribute(String name)
    {
        DefaultConfigurationBuilder.ConfigurationDeclaration decl = new DefaultConfigurationBuilder.ConfigurationDeclaration(
                factory, factory);
        DefaultConfigurationNode parent = new DefaultConfigurationNode();
        DefaultConfigurationNode nd = new DefaultConfigurationNode("config-"
                + name);
        parent.addAttribute(nd);
        assertTrue("config-" + name + " attribute not recognized", decl
                .isReservedNode(nd));
        DefaultConfigurationNode nd2 = new DefaultConfigurationNode(name);
        parent.addAttribute(nd2);
        assertFalse(name + " is reserved though config- exists", decl
                .isReservedNode(nd2));
        assertTrue("config- attribute not recognized when " + name + " exists",
                decl.isReservedNode(nd));
    }

    /**
     * Tests access to certain reserved attributes of a
     * ConfigurationDeclaration.
     */
    public void testConfigurationDeclarationGetAttributes()
    {
        factory.addProperty("xml.fileName", "test.xml");
        DefaultConfigurationBuilder.ConfigurationDeclaration decl = new DefaultConfigurationBuilder.ConfigurationDeclaration(
                factory, factory.configurationAt("xml"));
        assertNull("Found an at attribute", decl.getAt());
        assertFalse("Found an optional attribute", decl.isOptional());
        factory.addProperty("xml[@config-at]", "test1");
        assertEquals("Wrong value of at attribute", "test1", decl.getAt());
        factory.addProperty("xml[@at]", "test2");
        assertEquals("Wrong value of config-at attribute", "test1", decl.getAt());
        factory.clearProperty("xml[@config-at]");
        assertEquals("Old at attribute not detected", "test2", decl.getAt());
        factory.addProperty("xml[@config-optional]", "true");
        assertTrue("Wrong value of optional attribute", decl.isOptional());
        factory.addProperty("xml[@optional]", "false");
        assertTrue("Wrong value of config-optional attribute", decl.isOptional());
        factory.clearProperty("xml[@config-optional]");
        factory.setProperty("xml[@optional]", Boolean.TRUE);
        assertTrue("Old optional attribute not detected", decl.isOptional());
    }

    /**
     * Tests whether an invalid value of an optional attribute is detected.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testConfigurationDeclarationOptionalAttributeInvalid()
    {
        factory.addProperty("xml.fileName", "test.xml");
        DefaultConfigurationBuilder.ConfigurationDeclaration decl = new DefaultConfigurationBuilder.ConfigurationDeclaration(
                factory, factory.configurationAt("xml"));
        factory.setProperty("xml[@optional]", "invalid value");
        decl.isOptional();
    }

    /**
     * Tests adding a new configuration provider.
     */
    @Test
    public void testAddConfigurationProvider()
    {
        DefaultConfigurationBuilder.ConfigurationProvider provider = new DefaultConfigurationBuilder.ConfigurationProvider();
        assertNull("Provider already registered", factory
                .providerForTag("test"));
        factory.addConfigurationProvider("test", provider);
        assertSame("Provider not registered", provider, factory
                .providerForTag("test"));
    }

    /**
     * Tries to register a null configuration provider. This should cause an
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddConfigurationProviderNull()
    {
        factory.addConfigurationProvider("test", null);
    }

    /**
     * Tries to register a configuration provider for a null tag. This should
     * cause an exception to be thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddConfigurationProviderNullTag()
    {
        factory.addConfigurationProvider(null,
                new DefaultConfigurationBuilder.ConfigurationProvider());
    }

    /**
     * Tests removing configuration providers.
     */
    @Test
    public void testRemoveConfigurationProvider()
    {
        assertNull("Removing unknown provider", factory
                .removeConfigurationProvider("test"));
        assertNull("Removing provider for null tag", factory
                .removeConfigurationProvider(null));
        DefaultConfigurationBuilder.ConfigurationProvider provider = new DefaultConfigurationBuilder.ConfigurationProvider();
        factory.addConfigurationProvider("test", provider);
        assertSame("Failed to remove provider", provider, factory
                .removeConfigurationProvider("test"));
        assertNull("Provider still registered", factory.providerForTag("test"));
    }

    /**
     * Tests creating a configuration object from a configuration declaration.
     */
    @Test
    public void testConfigurationBeanFactoryCreateBean()
    {
        factory.addConfigurationProvider("test",
                new DefaultConfigurationBuilder.ConfigurationProvider(
                        PropertiesConfiguration.class));
        factory.addProperty("test[@throwExceptionOnMissing]", "true");
        DefaultConfigurationBuilder.ConfigurationDeclaration decl = new DefaultConfigurationBuilder.ConfigurationDeclaration(
                factory, factory.configurationAt("test"));
        PropertiesConfiguration conf = (PropertiesConfiguration) BeanHelper
                .createBean(decl);
        assertTrue("Property was not initialized", conf
                .isThrowExceptionOnMissing());
    }

    /**
     * Tests creating a configuration object from an unknown tag. This should
     * cause an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testConfigurationBeanFactoryCreateUnknownTag()
    {
        factory.addProperty("test[@throwExceptionOnMissing]", "true");
        DefaultConfigurationBuilder.ConfigurationDeclaration decl = new DefaultConfigurationBuilder.ConfigurationDeclaration(
                factory, factory.configurationAt("test"));
        BeanHelper.createBean(decl);
    }

    /**
     * Tests loading a simple configuration definition file.
     */
    @Test
    public void testLoadConfiguration() throws ConfigurationException
    {
        factory.setFile(TEST_FILE);
        checkConfiguration();
    }

    /**
     * Tests the file constructor.
     */
    @Test
    public void testLoadConfigurationFromFile() throws ConfigurationException
    {
        factory = new DefaultConfigurationBuilder(TEST_FILE);
        checkConfiguration();
    }

    /**
     * Tests the file name constructor.
     */
    @Test
    public void testLoadConfigurationFromFileName()
            throws ConfigurationException
    {
        factory = new DefaultConfigurationBuilder(TEST_FILE.getAbsolutePath());
        checkConfiguration();
    }

    /**
     * Tests the URL constructor.
     */
    @Test
    public void testLoadConfigurationFromURL() throws Exception
    {
        factory = new DefaultConfigurationBuilder(TEST_FILE.toURI().toURL());
        checkConfiguration();
    }

    /**
     * Tests if the configuration was correctly created by the factory.
     */
    private void checkConfiguration() throws ConfigurationException
    {
        CombinedConfiguration compositeConfiguration = (CombinedConfiguration) factory
                .getConfiguration();

        assertEquals("Number of configurations", 3, compositeConfiguration
                .getNumberOfConfigurations());
        assertEquals(PropertiesConfiguration.class, compositeConfiguration
                .getConfiguration(0).getClass());
        assertEquals(XMLPropertiesConfiguration.class, compositeConfiguration
                .getConfiguration(1).getClass());
        assertEquals(XMLConfiguration.class, compositeConfiguration
                .getConfiguration(2).getClass());

        // check the first configuration
        PropertiesConfiguration pc = (PropertiesConfiguration) compositeConfiguration
                .getConfiguration(0);
        assertNotNull("Make sure we have a fileName: " + pc.getFileName(), pc
                .getFileName());

        // check some properties
        checkProperties(compositeConfiguration);
    }

    /**
     * Checks if the passed in configuration contains the expected properties.
     *
     * @param compositeConfiguration the configuration to check
     */
    private void checkProperties(Configuration compositeConfiguration)
    {
        assertTrue("Make sure we have loaded our key", compositeConfiguration
                .getBoolean("test.boolean"));
        assertEquals("I'm complex!", compositeConfiguration
                .getProperty("element2.subelement.subsubelement"));
        assertEquals("property in the XMLPropertiesConfiguration", "value1",
                compositeConfiguration.getProperty("key1"));
    }

    /**
     * Tests loading a configuration definition file with an additional section.
     */
    @Test
    public void testLoadAdditional() throws ConfigurationException
    {
        factory.setFile(ADDITIONAL_FILE);
        CombinedConfiguration compositeConfiguration = (CombinedConfiguration) factory
                .getConfiguration();
        assertEquals("Verify how many configs", 2, compositeConfiguration
                .getNumberOfConfigurations());

        // Test if union was constructed correctly
        Object prop = compositeConfiguration.getProperty("tables.table.name");
        assertTrue(prop instanceof Collection);
        assertEquals(3, ((Collection<?>) prop).size());
        assertEquals("users", compositeConfiguration
                .getProperty("tables.table(0).name"));
        assertEquals("documents", compositeConfiguration
                .getProperty("tables.table(1).name"));
        assertEquals("tasks", compositeConfiguration
                .getProperty("tables.table(2).name"));

        prop = compositeConfiguration
                .getProperty("tables.table.fields.field.name");
        assertTrue(prop instanceof Collection);
        assertEquals(17, ((Collection<?>) prop).size());

        assertEquals("smtp.mydomain.org", compositeConfiguration
                .getString("mail.host.smtp"));
        assertEquals("pop3.mydomain.org", compositeConfiguration
                .getString("mail.host.pop"));

        // This was overriden
        assertEquals("masterOfPost", compositeConfiguration
                .getString("mail.account.user"));
        assertEquals("topsecret", compositeConfiguration
                .getString("mail.account.psswd"));

        // This was overriden, too, but not in additional section
        assertEquals("enhanced factory", compositeConfiguration
                .getString("test.configuration"));
    }

    /**
     * Tests whether a default log error listener is registered at the builder
     * instance.
     */
    @Test
    public void testLogErrorListener()
    {
        assertEquals("No default error listener registered", 1,
                new DefaultConfigurationBuilder().getErrorListeners().size());
    }

    /**
     * Tests loading a definition file that contains optional configurations.
     */
    @Test
    public void testLoadOptional() throws Exception
    {
        factory.setURL(OPTIONAL_FILE.toURI().toURL());
        Configuration config = factory.getConfiguration();
        assertTrue(config.getBoolean("test.boolean"));
        assertEquals("value", config.getProperty("element"));
    }

    /**
     * Tests whether loading a failing optional configuration causes an error
     * event.
     */
    @Test
    public void testLoadOptionalErrorEvent() throws Exception
    {
        factory.clearErrorListeners();
        ConfigurationErrorListenerImpl listener = new ConfigurationErrorListenerImpl();
        factory.addErrorListener(listener);
        prepareOptionalTest("configuration", false);
        listener.verify(DefaultConfigurationBuilder.EVENT_ERR_LOAD_OPTIONAL,
                OPTIONAL_NAME, null);
    }

    /**
     * Tests loading a definition file with optional and non optional
     * configuration sources. One non optional does not exist, so this should
     * cause an exception.
     */
    @Test(expected = ConfigurationException.class)
    public void testLoadOptionalWithException() throws ConfigurationException
    {
        factory.setFile(OPTIONALEX_FILE);
        factory.getConfiguration();
    }

    /**
     * Tries to load a configuration file with an optional, non file-based
     * configuration. The optional attribute should work for other configuration
     * classes, too.
     */
    @Test
    public void testLoadOptionalNonFileBased() throws ConfigurationException
    {
        CombinedConfiguration config = prepareOptionalTest("configuration", false);
        assertTrue("Configuration not empty", config.isEmpty());
        assertEquals("Wrong number of configurations", 0, config
                .getNumberOfConfigurations());
    }

    /**
     * Tests an optional, non existing configuration with the forceCreate
     * attribute. This configuration should be added to the resulting
     * configuration.
     */
    @Test
    public void testLoadOptionalForceCreate() throws ConfigurationException
    {
        factory.setBasePath(TEST_FILE.getParent());
        CombinedConfiguration config = prepareOptionalTest("xml", true);
        assertEquals("Wrong number of configurations", 1, config
                .getNumberOfConfigurations());
        FileConfiguration fc = (FileConfiguration) config
                .getConfiguration(OPTIONAL_NAME);
        assertNotNull("Optional config not found", fc);
        assertEquals("File name was not set", "nonExisting.xml", fc
                .getFileName());
        assertNotNull("Base path was not set", fc.getBasePath());
    }

    /**
     * Tests loading an embedded optional configuration builder with the force
     * create attribute.
     */
    @Test
    public void testLoadOptionalBuilderForceCreate()
            throws ConfigurationException
    {
        CombinedConfiguration config = prepareOptionalTest("configuration",
                true);
        assertEquals("Wrong number of configurations", 1, config
                .getNumberOfConfigurations());
        assertTrue(
                "Wrong optional configuration type",
                config.getConfiguration(OPTIONAL_NAME) instanceof CombinedConfiguration);
    }

    /**
     * Tests loading an optional configuration with the force create attribute
     * set. The provider will always throw an exception. In this case the
     * configuration will not be added to the resulting combined configuration.
     */
    @Test
    public void testLoadOptionalForceCreateWithException()
            throws ConfigurationException
    {
        factory.addConfigurationProvider("test",
                new DefaultConfigurationBuilder.ConfigurationBuilderProvider()
                {
                    // Throw an exception here, too
                    @Override
                    public AbstractConfiguration getEmptyConfiguration(
                            DefaultConfigurationBuilder.ConfigurationDeclaration decl) throws Exception
                    {
                        throw new Exception("Unable to create configuration!");
                    }
                });
        CombinedConfiguration config = prepareOptionalTest("test", true);
        assertEquals("Optional configuration could be created", 0, config
                .getNumberOfConfigurations());
    }

    /**
     * Prepares a test for loading a configuration definition file with an
     * optional configuration declaration.
     *
     * @param tag the tag name with the optional configuration
     * @param force the forceCreate attribute
     * @return the combined configuration obtained from the builder
     * @throws ConfigurationException if an error occurs
     */
    private CombinedConfiguration prepareOptionalTest(String tag, boolean force)
            throws ConfigurationException
    {
        String prefix = "override." + tag;
        factory.addProperty(prefix + "[@fileName]", "nonExisting.xml");
        factory.addProperty(prefix + "[@config-optional]", Boolean.TRUE);
        factory.addProperty(prefix + "[@config-name]", OPTIONAL_NAME);
        if (force)
        {
            factory.addProperty(prefix + "[@config-forceCreate]", Boolean.TRUE);
        }
        return factory.getConfiguration(false);
    }

    /**
     * Tests whether the error log message caused by an optional configuration
     * can be suppressed if a child builder is involved.
     */
    @Test
    public void testLoadOptionalChildBuilderSuppressErrorLog()
            throws ConfigurationException
    {
        factory.addProperty("override.configuration[@fileName]",
                OPTIONAL_FILE.getAbsolutePath());
        // a special invocation handler which checks that the warn() method of
        // a logger is not called
        InvocationHandler handler = new InvocationHandler()
        {
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable
            {
                String methodName = method.getName();
                if (methodName.startsWith("is"))
                {
                    return Boolean.TRUE;
                }
                if ("warn".equals(methodName))
                {
                    fail("Unexpected log output!");
                }
                return null;
            }
        };
        factory.setLogger((Log) Proxy.newProxyInstance(getClass()
                .getClassLoader(), new Class[] {
            Log.class
        }, handler));
        factory.getConfiguration(false);
    }

    /**
     * Tests loading a definition file with multiple different sources.
     */
    @Test
    public void testLoadDifferentSources() throws ConfigurationException
    {
        factory.setFile(MULTI_FILE);
        Configuration config = factory.getConfiguration();
        assertFalse(config.isEmpty());
        assertTrue(config instanceof CombinedConfiguration);
        CombinedConfiguration cc = (CombinedConfiguration) config;
        assertEquals("Wrong number of configurations", 1, cc
                .getNumberOfConfigurations());

        assertNotNull(config
                .getProperty("tables.table(0).fields.field(2).name"));
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
        assertEquals(System.getProperty("java.version"), config
                .getString("java.version"));

        // test INIConfiguration
        assertEquals("Property from ini file not found", "yes",
                config.getString("testini.loaded"));

        // test environment configuration
        EnvironmentConfiguration envConf = new EnvironmentConfiguration();
        for (Iterator<String> it = envConf.getKeys(); it.hasNext();)
        {
            String key = it.next();
            String combinedKey = "env." + key;
            assertEquals("Wrong value for env property " + key,
                    envConf.getString(key), config.getString(combinedKey));
        }
    }

    /**
     * Tests if the base path is correctly evaluated.
     */
    @Test
    public void testSetConfigurationBasePath() throws ConfigurationException
    {
        factory.addProperty("properties[@fileName]", "test.properties");
        File deepDir = new File(ConfigurationAssert.TEST_DIR, "config/deep");
        factory.setConfigurationBasePath(deepDir.getAbsolutePath());

        Configuration config = factory.getConfiguration(false);
        assertEquals("Wrong property value", "somevalue", config
                .getString("somekey"));
    }

    /**
     * Tests reading a configuration definition file that contains complex
     * initialization of properties of the declared configuration sources.
     */
    @Test
    public void testComplexInitialization() throws ConfigurationException
    {
        factory.setFile(INIT_FILE);
        CombinedConfiguration cc = (CombinedConfiguration) factory
                .getConfiguration();

        assertEquals("System property not found", "test.xml",
                cc.getString("test_file_xml"));
        PropertiesConfiguration c1 = (PropertiesConfiguration) cc
                .getConfiguration(1);
        assertTrue(
                "Reloading strategy was not set",
                c1.getReloadingStrategy() instanceof FileChangedReloadingStrategy);
        assertEquals("Refresh delay was not set", 10000,
                ((FileChangedReloadingStrategy) c1.getReloadingStrategy())
                        .getRefreshDelay());

        Configuration xmlConf = cc.getConfiguration("xml");
        assertEquals("Property not found", "I'm complex!", xmlConf
                .getString("element2/subelement/subsubelement"));
        assertEquals("List index not found", "two", xmlConf
                .getString("list[0]/item[1]"));
        assertEquals("Property in combiner file not found", "yellow", cc
                .getString("/gui/selcolor"));

        assertTrue("Delimiter flag was not set", cc
                .isDelimiterParsingDisabled());
        assertTrue("Expression engine was not set",
                cc.getExpressionEngine() instanceof XPathExpressionEngine);
    }

    /**
     * Tests if the returned combined configuration has the expected structure.
     */
    @Test
    public void testCombinedConfigurationStructure() throws ConfigurationException
    {
        factory.setFile(INIT_FILE);
        CombinedConfiguration cc = (CombinedConfiguration) factory
                .getConfiguration();
        assertNotNull("Properties configuration not found", cc
                .getConfiguration("properties"));
        assertNotNull("XML configuration not found", cc.getConfiguration("xml"));
        assertEquals("Wrong number of contained configs", 4, cc
                .getNumberOfConfigurations());

        CombinedConfiguration cc2 = (CombinedConfiguration) cc
                .getConfiguration(DefaultConfigurationBuilder.ADDITIONAL_NAME);
        assertNotNull("No additional configuration found", cc2);
        Set<String> names = cc2.getConfigurationNames();
        assertEquals("Wrong number of contained additional configs", 2, names
                .size());
        assertTrue("Config 1 not contained", names.contains("combiner1"));
        assertTrue("Config 2 not contained", names.contains("combiner2"));
    }

    /**
     * Helper method for testing the attributes of a combined configuration
     * created by the builder.
     *
     * @param cc the configuration to be checked
     */
    private void checkCombinedConfigAttrs(CombinedConfiguration cc)
    {
        assertTrue("Wrong delimiter parsing flag",
                cc.isDelimiterParsingDisabled());
        assertTrue("Wrong reload check", cc.isForceReloadCheck());
        assertTrue("Wrong ignore reload ex flag", cc.isIgnoreReloadExceptions());
    }

    /**
     * Tests whether attributes are correctly set on the combined configurations
     * for the override and additional sections.
     */
    @Test
    public void testCombinedConfigurationAttributes() throws ConfigurationException
    {
        factory.setFile(INIT_FILE);
        CombinedConfiguration cc = (CombinedConfiguration) factory
                .getConfiguration();
        checkCombinedConfigAttrs(cc);
        CombinedConfiguration cc2 = (CombinedConfiguration) cc
                .getConfiguration(DefaultConfigurationBuilder.ADDITIONAL_NAME);
        checkCombinedConfigAttrs(cc2);
    }

    /**
     * Tests the structure of the returned combined configuration if there is no
     * additional section.
     */
    @Test
    public void testCombinedConfigurationNoAdditional()
            throws ConfigurationException
    {
        factory.setFile(TEST_FILE);
        CombinedConfiguration cc = factory.getConfiguration(true);
        assertNull("Additional configuration was found", cc
                .getConfiguration(DefaultConfigurationBuilder.ADDITIONAL_NAME));
    }

    /**
     * Tests whether the list node definition was correctly processed.
     */
    @Test
    public void testCombinedConfigurationListNodes()
            throws ConfigurationException
    {
        factory.setFile(INIT_FILE);
        CombinedConfiguration cc = factory.getConfiguration(true);
        Set<String> listNodes = cc.getNodeCombiner().getListNodes();
        assertEquals("Wrong number of list nodes", 2, listNodes.size());
        assertTrue("table node not a list node", listNodes.contains("table"));
        assertTrue("list node not a list node", listNodes.contains("list"));

        CombinedConfiguration cca = (CombinedConfiguration) cc
                .getConfiguration(DefaultConfigurationBuilder.ADDITIONAL_NAME);
        listNodes = cca.getNodeCombiner().getListNodes();
        assertTrue("Found list nodes for additional combiner", listNodes
                .isEmpty());
    }

    /**
     * Tests whether a configuration builder can itself be declared in a
     * configuration definition file.
     */
    @Test
    public void testConfigurationBuilderProvider()
            throws ConfigurationException
    {
        factory.addProperty("override.configuration[@fileName]", TEST_FILE
                .getAbsolutePath());
        CombinedConfiguration cc = factory.getConfiguration(false);
        assertEquals("Wrong number of configurations", 1, cc
                .getNumberOfConfigurations());
        checkProperties(cc);
    }

    /**
     * Tests whether settings of the builder are propagated to child builders.
     */
    @Test
    public void testConfigurationBuilderProviderInheritProperties()
            throws Exception
    {
        factory.addProperty("override.configuration[@fileName]",
                TEST_FILE.getAbsolutePath());
        factory.setBasePath("conf");
        factory.setAttributeSplittingDisabled(true);
        factory.setDelimiterParsingDisabled(true);
        factory.setListDelimiter('/');
        factory.setThrowExceptionOnMissing(true);
        Log log = LogFactory.getLog(getClass());
        factory.setLogger(log);
        factory.clearErrorListeners();
        factory.clearConfigurationListeners();
        ConfigurationListenerTestImpl l =
                new ConfigurationListenerTestImpl(factory);
        factory.addConfigurationListener(l);
        DefaultConfigurationBuilder.ConfigurationDeclaration decl =
                new DefaultConfigurationBuilder.ConfigurationDeclaration(
                        factory,
                        factory.configurationAt("override.configuration"));
        DefaultConfigurationBuilder.ConfigurationBuilderProvider provider =
                new DefaultConfigurationBuilder.ConfigurationBuilderProvider();
        DefaultConfigurationBuilder child =
                (DefaultConfigurationBuilder) provider.createBean(
                        provider.fetchConfigurationClass(), decl, null);
        assertEquals("Wrong base path", factory.getBasePath(),
                child.getBasePath());
        assertEquals("Wrong attribute splitting flag",
                factory.isAttributeSplittingDisabled(),
                child.isAttributeSplittingDisabled());
        assertEquals("Wrong delimiter parsing flag",
                factory.isDelimiterParsingDisabled(),
                child.isDelimiterParsingDisabled());
        assertEquals("Wrong list delimiter", factory.getListDelimiter(),
                child.getListDelimiter());
        assertEquals("Wrong exception flag",
                factory.isThrowExceptionOnMissing(),
                child.isThrowExceptionOnMissing());
        assertSame("Wrong logger", log, child.getLogger());
        assertTrue("Got error listeners", child.getErrorListeners().isEmpty());
        assertEquals("Wrong number of listeners", 1, child
                .getConfigurationListeners().size());
        assertEquals("Wrong listener", l, child.getConfigurationListeners()
                .iterator().next());
    }

    /**
     * Tests whether properties of the parent configuration can be overridden.
     */
    @Test
    public void testConfigurationBuilderProviderOverrideProperties()
            throws Exception
    {
        factory.addProperty("override.configuration[@fileName]",
                TEST_FILE.getAbsolutePath());
        factory.addProperty("override.configuration[@basePath]", "base");
        factory.addProperty("override.configuration[@throwExceptionOnMissing]",
                "false");
        factory.setBasePath("conf");
        factory.setThrowExceptionOnMissing(true);
        DefaultConfigurationBuilder.ConfigurationDeclaration decl =
                new DefaultConfigurationBuilder.ConfigurationDeclaration(
                        factory,
                        factory.configurationAt("override.configuration"));
        DefaultConfigurationBuilder.ConfigurationBuilderProvider provider =
                new DefaultConfigurationBuilder.ConfigurationBuilderProvider();
        DefaultConfigurationBuilder child =
                (DefaultConfigurationBuilder) provider.createBean(
                        provider.fetchConfigurationClass(), decl, null);
        assertEquals("Wrong base path", "base", child.getBasePath());
        assertFalse("Wrong exception flag", child.isThrowExceptionOnMissing());
    }

    /**
     * Tests whether XML settings can be inherited.
     */
    @Test
    public void testLoadXMLWithSettings() throws Exception
    {
        File confDir = new File("conf");
        File targetDir = new File("target");
        File testXMLValidationSource = new File(confDir,
                "testValidateInvalid.xml");
        File testSavedXML = new File(targetDir, "testSave.xml");
        File testSavedFactory = new File(targetDir, "testSaveFactory.xml");
        URL dtdFile = getClass().getResource("/properties.dtd");
        final String publicId = "http://commons.apache.org/test.dtd";

        XMLConfiguration config = new XMLConfiguration("testDtd.xml");
        config.setPublicID(publicId);
        config.save(testSavedXML);
        factory.addProperty("xml[@fileName]", testSavedXML.getAbsolutePath());
        factory.addProperty("xml(0)[@validating]", "true");
        factory.addProperty("xml(-1)[@fileName]", testXMLValidationSource
                .getAbsolutePath());
        factory.addProperty("xml(1)[@config-optional]", "true");
        factory.addProperty("xml(1)[@validating]", "true");
        factory.save(testSavedFactory);

        factory = new DefaultConfigurationBuilder();
        factory.setFile(testSavedFactory);
        factory.registerEntityId(publicId, dtdFile);
        factory.clearErrorListeners();
        Configuration c = factory.getConfiguration();
        assertEquals("Wrong property value", "value1", c.getString("entry(0)"));
        assertFalse("Invalid XML source was loaded", c
                .containsKey("table.name"));

        testSavedXML.delete();
        testSavedFactory.delete();
    }

    /**
     * Tests loading a configuration definition file that defines a custom
     * result class.
     */
    @Test
    public void testExtendedClass() throws ConfigurationException
    {
        factory.setFile(CLASS_FILE);
        CombinedConfiguration cc = factory.getConfiguration(true);
        assertEquals("Extended", cc.getProperty("test"));
        assertTrue("Wrong result class: " + cc.getClass(),
                cc instanceof ExtendedCombinedConfiguration);
    }

    /**
     * Tests loading a configuration definition file that defines new providers.
     */
    @Test
    public void testConfigurationProvider() throws ConfigurationException
    {
        factory.setFile(PROVIDER_FILE);
        factory.getConfiguration(true);
        DefaultConfigurationBuilder.ConfigurationProvider provider = factory
                .providerForTag("test");
        assertNotNull("Provider 'test' not registered", provider);
    }

    /**
     * Tests loading a configuration definition file that defines new providers.
     */
    @Test
    public void testExtendedXMLConfigurationProvider() throws ConfigurationException
    {
        factory.setFile(EXTENDED_PROVIDER_FILE);
        CombinedConfiguration cc = factory.getConfiguration(true);
        DefaultConfigurationBuilder.ConfigurationProvider provider = factory
                .providerForTag("test");
        assertNotNull("Provider 'test' not registered", provider);
        Configuration config = cc.getConfiguration("xml");
        assertNotNull("Test configuration not present", config);
        assertTrue("Configuration is not ExtendedXMLConfiguration, is " +
                config.getClass().getName(), config instanceof ExtendedXMLConfiguration);
    }

    @Test
    public void testGlobalLookup() throws Exception
    {
        factory.setFile(GLOBAL_LOOKUP_FILE);
        CombinedConfiguration cc = factory.getConfiguration(true);
        String value = cc.getInterpolator().lookup("test:test_key");
        assertNotNull("The test key was not located", value);
        assertEquals("Incorrect value retrieved","test.value",value);
    }

    @Test
    public void testSystemProperties() throws Exception
    {
        factory.setFile(SYSTEM_PROPS_FILE);
        factory.getConfiguration(true);
        String value = System.getProperty("key1");
        assertNotNull("The test key was not located", value);
        assertEquals("Incorrect value retrieved","value1",value);
    }

    @Test
    public void testValidation() throws Exception
    {
        factory.setFile(VALIDATION_FILE);
        factory.getConfiguration(true);
        String value = System.getProperty("key1");
        assertNotNull("The test key was not located", value);
        assertEquals("Incorrect value retrieved","value1",value);
    }

    @Test
    public void testValidation3() throws Exception
    {
        System.getProperties().remove("Id");
        factory.setFile(VALIDATION3_FILE);
        CombinedConfiguration config = factory.getConfiguration(true);
        String value = config.getString("Employee/Name");
        assertNotNull("The test key was not located", value);
        assertEquals("Incorrect value retrieved","John Doe",value);
        System.setProperty("Id", "1001");
        value = config.getString("Employee/Name");
        assertNotNull("The test key was not located", value);
        assertEquals("Incorrect value retrieved","Jane Doe",value);
    }

    @Test
    public void testMultiTenentConfiguration() throws Exception
    {
        factory.setFile(MULTI_TENENT_FILE);
        System.getProperties().remove("Id");

        CombinedConfiguration config = factory.getConfiguration(true);
        assertTrue("Incorrect configuration", config instanceof DynamicCombinedConfiguration);

        verify("1001", config, 15);
        verify("1002", config, 25);
        verify("1003", config, 35);
        verify("1004", config, 50);
        verify("1005", config, 50);
    }

    @Test
    public void testMultiTenentConfiguration2() throws Exception
    {
        factory.setFile(MULTI_TENENT_FILE);
        System.setProperty("Id", "1004");

        CombinedConfiguration config = factory.getConfiguration(true);
        assertTrue("Incorrect configuration", config instanceof DynamicCombinedConfiguration);

        verify("1001", config, 15);
        verify("1002", config, 25);
        verify("1003", config, 35);
        verify("1004", config, 50);
        verify("1005", config, 50);
    }

    @Test
    public void testMultiTenentConfiguration3() throws Exception
    {
        factory.setFile(MULTI_TENENT_FILE);
        StringWriter writer = new StringWriter();
        WriterAppender app = new WriterAppender(new SimpleLayout(), writer);
        Log log = LogFactory.getLog("TestLogger");
        Logger logger = ((Log4JLogger)log).getLogger();
        logger.addAppender(app);
        logger.setLevel(Level.DEBUG);
        logger.setAdditivity(false);

        System.setProperty("Id", "1005");

        CombinedConfiguration config = factory.getConfiguration(true);
        assertTrue("Incorrect configuration", config instanceof DynamicCombinedConfiguration);

        verify("1001", config, 15);
        String xml = writer.getBuffer().toString();
        assertNotNull("No XML returned", xml);
        assertTrue("Incorect configuration data", xml.indexOf("<rowsPerPage>15</rowsPerPage>") >= 0);
        logger.removeAppender(app);
        logger.setLevel(Level.OFF);
        verify("1002", config, 25);
        verify("1003", config, 35);
        verify("1004", config, 50);
        verify("1005", config, 50);
    }

    @Test
    public void testMultiTenantConfigurationAt() throws Exception
    {
        factory.setFile(MULTI_TENENT_FILE);
        System.setProperty("Id", "1001");
        CombinedConfiguration config = factory.getConfiguration(true);
        HierarchicalConfiguration sub1 = config.configurationAt("Channels/Channel[@id='1']");
        assertEquals("My Channel", sub1.getString("Name"));
        assertEquals("test 1 data", sub1.getString("ChannelData"));
        HierarchicalConfiguration sub2 = config.configurationAt("Channels/Channel[@id='2']");
        assertEquals("Channel 2", sub2.getString("Name"));
        assertEquals("more test 2 data", sub2.getString("MoreChannelData"));
    }

    @Test
    public void testMerge() throws Exception
    {
        factory.setFile(MULTI_TENENT_FILE);
        System.setProperty("Id", "1004");
        Map<String, String> map = new HashMap<String, String>();
        map.put("default", "${colors.header4}");
        map.put("background", "#40404040");
        map.put("text", "#000000");
        map.put("header", "#444444");

        CombinedConfiguration config = factory.getConfiguration(true);
        assertTrue("Incorrect configuration", config instanceof DynamicCombinedConfiguration);

        List<HierarchicalConfiguration> list = config.configurationsAt("colors/*");
        Iterator<HierarchicalConfiguration> iter = list.iterator();
        while (iter.hasNext())
        {
            SubnodeConfiguration sub = (SubnodeConfiguration)iter.next();
            ConfigurationNode node = sub.getRootNode();
            String value = (node.getValue() == null) ? "null" : node.getValue().toString();
            if (map.containsKey(node.getName()))
            {
                assertEquals(map.get(node.getName()), value);
            }
        }

    }

    @Test
    public void testDelimiterParsingDisabled() throws Exception
    {
        factory.setFile(MULTI_TENENT_FILE);
        System.setProperty("Id", "1004");

        CombinedConfiguration config = factory.getConfiguration(true);
        assertTrue("Incorrect configuration", config instanceof DynamicCombinedConfiguration);

        assertEquals("a,b,c", config.getString("split/list3/@values"));
        assertEquals(0, config.getMaxIndex("split/list3/@values"));
        assertEquals("a\\,b\\,c", config.getString("split/list4/@values"));
        assertEquals("a,b,c", config.getString("split/list1"));
        assertEquals(0, config.getMaxIndex("split/list1"));
        assertEquals("a\\,b\\,c", config.getString("split/list2"));
    }

    @Test
    public void testExpression() throws Exception
    {
        if (SystemUtils.isJavaVersionAtLeast(150))
        {
            factory.setFile(EXPRESSION_FILE);
            factory.setAttributeSplittingDisabled(true);
            System.getProperties().remove("Id");
            org.slf4j.MDC.clear();

            CombinedConfiguration config = factory.getConfiguration(true);
            assertTrue("Incorrect configuration",
                    config instanceof DynamicCombinedConfiguration);

            verify("1001", config, 15);
        }
    }

    /**
     * Tests whether variable substitution works across multiple child
     * configurations. This test is related to CONFIGURATION-481.
     */
    @Test
    public void testInterpolationOverMultipleSources()
            throws ConfigurationException
    {
        File testFile =
                ConfigurationAssert.getTestFile("testInterpolationBuilder.xml");
        factory.setFile(testFile);
        CombinedConfiguration combConfig = factory.getConfiguration(true);
        assertEquals("Wrong value", "abc-product",
                combConfig.getString("products.product.desc"));
        XMLConfiguration xmlConfig =
                (XMLConfiguration) combConfig.getConfiguration("test");
        assertEquals("Wrong value from XML config", "abc-product",
                xmlConfig.getString("products/product/desc"));
        SubnodeConfiguration subConfig =
                xmlConfig
                        .configurationAt("products/product[@name='abc']", true);
        assertEquals("Wrong value from sub config", "abc-product",
                subConfig.getString("desc"));
    }

    private void verify(String key, CombinedConfiguration config, int rows)
    {
        System.setProperty("Id", key);
        org.slf4j.MDC.put("Id", key);
        int actual = config.getInt("rowsPerPage");
        assertTrue("expected: " + rows + " actual: " + actual, actual == rows);
    }


    /**
     * A specialized combined configuration implementation used for testing
     * custom result classes.
     */
    public static class ExtendedCombinedConfiguration extends
            CombinedConfiguration
    {
        /**
         * The serial version UID.
         */
        private static final long serialVersionUID = 4678031745085083392L;

        @Override
        public Object getProperty(String key)
        {
            if (key.equals("test"))
            {
                return "Extended";
            }
            return super.getProperty(key);
        }
    }

    public static class ExtendedXMLConfiguration extends XMLConfiguration
    {
        private static final long serialVersionUID = 1L;

        public ExtendedXMLConfiguration()
        {
        }

    }

    public static class TestLookup extends StrLookup
    {
        Map<String, String> map = new HashMap<String, String>();

        public TestLookup()
        {
            map.put("test_file_xml", "test.xml");
            map.put("test_file_combine", "testcombine1.xml");
            map.put("test_key", "test.value");
        }

        @Override
        public String lookup(String key)
        {
            if (key == null)
            {
                return null;
            }
            return map.get(key);

        }
    }
}

