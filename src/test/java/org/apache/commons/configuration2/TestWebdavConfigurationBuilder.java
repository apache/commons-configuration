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
import java.io.IOException;
import java.io.Reader;
import java.io.FileReader;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.configuration2.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration2.reloading.VFSFileMonitorReloadingStrategy;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.ConfigurationListener;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemOptions;

/**
 * Test class for DefaultConfigurationBuilder.
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class TestWebdavConfigurationBuilder extends TestCase implements FileOptionsProvider,
        ConfigurationListener
{
    /** Test configuration definition file. */
    private static final String TEST_FILE =
            "testDigesterConfiguration.xml";

    private static final String ADDITIONAL_FILE =
            "testDigesterConfiguration2.xml";

    private static final String OPTIONAL_FILE =
            "testDigesterOptionalConfiguration.xml";

    private static final String OPTIONALEX_FILE =
            "testDigesterOptionalConfigurationEx.xml";

    private static final String MULTI_FILE =
            "testDigesterConfiguration3.xml";

    private static final String INIT_FILE =
            "testComplexInitialization.xml";

    private static final String CLASS_FILE =
            "testExtendedClass.xml";

    private static final String PROVIDER_FILE =
            "testConfigurationProvider.xml";

    private static final String GLOBAL_LOOKUP_FILE =
            "testGlobalLookup.xml";

    private static final String SYSTEM_PROPS_FILE =
            "testSystemProperties.xml";

    private static final String VALIDATION_FILE =
            "testValidation.xml";

    private static final String MULTI_TENENT_FILE =
            "testMultiTenentConfigurationBuilder.xml";

    private static final String FILEMONITOR2_FILE =
            "testFileMonitorConfigurationBuilder2.xml";

    private static final String FILEMONITOR_1001_FILE =
            "testwrite/testMultiConfiguration_1001.xml";

    private static final String FILEMONITOR_1002_FILE =
            "testwrite/testMultiConfiguration_1002.xml";

    private static final String TEST_PROPERTIES = "test.properties.xml";

    private static final String TEST_SAVE = "testsave.xml";
    /** Constant for the name of an optional configuration.*/
    private static final String OPTIONAL_NAME = "optionalConfig";

    /** Stores the object to be tested. */
    private DefaultConfigurationBuilder factory;

    private Map options;

    /** true when a file is changed */
    private boolean configChanged = false;

    private String getBasePath()
    {
        String path = System.getProperty("test.webdav.base");
        assertNotNull("No base url provided", path);
        return path;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        System
                .setProperty("java.naming.factory.initial",
                        "org.apache.commons.configuration2.MockInitialContextFactory");
        System.setProperty("test_file_xml", "test.xml");
        System.setProperty("test_file_combine", "testcombine1.xml");
        FileSystem fs = new VFSFileSystem();
        fs.setFileOptionsProvider(this);
        FileSystem.setDefaultFileSystem(fs);
        factory = new DefaultConfigurationBuilder();
        factory.setBasePath(getBasePath());
        factory.clearErrorListeners();  // avoid exception messages
    }

    protected void tearDown() throws Exception
    {
        FileSystem.resetDefaultFileSystem();
        super.tearDown();
    }

    /**
     * Tests the isReservedNode() method of ConfigurationDeclaration.
     */
    public void testConfigurationDeclarationIsReserved()
    {
        DefaultConfigurationBuilder.ConfigurationDeclaration decl = new DefaultConfigurationBuilder.ConfigurationDeclaration(
                factory, factory);
        DefaultConfigurationNode parent = new DefaultConfigurationNode();
        DefaultConfigurationNode nd = new DefaultConfigurationNode("at");
        parent.addAttribute(nd);
        assertTrue("Attribute at not recognized", decl.isReservedAttribute(parent, nd.getName()));
        nd = new DefaultConfigurationNode("optional");
        parent.addAttribute(nd);
        assertTrue("Attribute optional not recognized", decl.isReservedAttribute(parent, nd.getName()));
        nd = new DefaultConfigurationNode("config-class");
        parent.addAttribute(nd);
        assertTrue("Inherited attribute not recognized", decl
                .isReservedAttribute(parent, nd.getName()));
        nd = new DefaultConfigurationNode("different");
        parent.addAttribute(nd);
        assertFalse("Wrong reserved attribute", decl.isReservedAttribute(parent, nd.getName()));
        nd = new DefaultConfigurationNode("at");
        parent.addChild(nd);
    }

    /**
     * Tests if the at attribute is correctly detected as reserved attribute.
     */
    public void testConfigurationDeclarationIsReservedAt()
    {
        checkOldReservedAttribute("at");
    }

    /**
     * Tests if the optional attribute is correctly detected as reserved
     * attribute.
     */
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
                + name, "test");
        parent.addAttribute(nd);
        assertTrue("config-" + name + " attribute not recognized", decl
                .isReservedAttribute(parent, nd.getName()));
        DefaultConfigurationNode nd2 = new DefaultConfigurationNode(name, "test");
        parent.addAttribute(nd2);
        assertFalse(name + " is reserved though config- exists", decl
                .isReservedAttribute(parent, nd2.getName()));
        assertTrue("config- attribute not recognized when " + name + " exists",
                decl.isReservedAttribute(parent, nd.getName()));
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
        factory.setProperty("xml[@optional]", "invalid value");
        try
        {
            decl.isOptional();
            fail("Invalid optional attribute was not detected!");
        }
        catch (ConfigurationRuntimeException crex)
        {
            // ok
        }
    }

    /**
     * Tests adding a new configuration provider.
     */
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
    public void testAddConfigurationProviderNull()
    {
        try
        {
            factory.addConfigurationProvider("test", null);
            fail("Could register null provider");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tries to register a configuration provider for a null tag. This should
     * cause an exception to be thrown.
     */
    public void testAddConfigurationProviderNullTag()
    {
        try
        {
            factory.addConfigurationProvider(null,
                    new DefaultConfigurationBuilder.ConfigurationProvider());
            fail("Could register provider for null tag!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests removing configuration providers.
     */
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
    public void testConfigurationBeanFactoryCreateUnknownTag()
    {
        factory.addProperty("test[@throwExceptionOnMissing]", "true");
        DefaultConfigurationBuilder.ConfigurationDeclaration decl = new DefaultConfigurationBuilder.ConfigurationDeclaration(
                factory, factory.configurationAt("test"));
        try
        {
            BeanHelper.createBean(decl);
            fail("Could create configuration from unknown tag!");
        }
        catch (ConfigurationRuntimeException crex)
        {
            // ok
        }
    }

    /**
     * Tests loading a simple configuration definition file.
     */
    public void testLoadConfiguration() throws ConfigurationException
    {
        factory.setFileName(TEST_FILE);
        checkConfiguration();
    }

    /**
     * Tests the file constructor.
     */
    public void testLoadConfigurationFromFile() throws ConfigurationException
    {
        factory = new DefaultConfigurationBuilder(getBasePath() + TEST_FILE);
        checkConfiguration();
    }

    /**
     * This test doesn't test DefaultConfigurationBuilder. It tests saving a file
     * using Webdav file options.
     */
    public void testSaveConfiguration() throws ConfigurationException
    {
        options = new HashMap();
        options.put(FileOptionsProvider.VERSIONING, Boolean.TRUE);
        options.put(FileOptionsProvider.CURRENT_USER, "TestUser");
        XMLConfiguration conf = new XMLConfiguration();
        conf.setFileName(getBasePath() + TEST_PROPERTIES);
        conf.load();
        conf.save(getBasePath() + TEST_SAVE);
        XMLConfiguration checkConfig = new XMLConfiguration();
        checkConfig.setFileName(getBasePath() + TEST_SAVE);
        checkSavedConfig(conf, checkConfig);
        options = null;
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
    public void testLoadAdditional() throws ConfigurationException
    {
        factory.setFileName(ADDITIONAL_FILE);
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

        // This was overridden
        assertEquals("masterOfPost", compositeConfiguration
                .getString("mail.account.user"));
        assertEquals("topsecret", compositeConfiguration
                .getString("mail.account.psswd"));

        // This was overridden, too, but not in additional section
        assertEquals("enhanced factory", compositeConfiguration
                .getString("test.configuration"));
    }

    /**
     * Tests whether a default log error listener is registered at the builder
     * instance.
     */
    public void testLogErrorListener()
    {
        assertEquals("No default error listener registered", 1,
                new DefaultConfigurationBuilder().getErrorListeners().size());
    }

    /**
     * Tests loading a definition file that contains optional configurations.
     */
    public void testLoadOptional() throws Exception
    {
        factory.setFileName(OPTIONAL_FILE);
        Configuration config = factory.getConfiguration();
        assertTrue(config.getBoolean("test.boolean"));
        assertEquals("value", config.getProperty("element"));
    }

    /**
     * Tests whether loading a failing optional configuration causes an error
     * event.
     */
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
    public void testLoadOptionalWithException()
    {
        factory.setFileName(OPTIONALEX_FILE);
        try
        {
            factory.getConfiguration();
            fail("Non existing source did not cause an exception!");
        }
        catch (ConfigurationException cex)
        {
            // ok
        }
    }

    /**
     * Tries to load a configuration file with an optional, non file-based
     * configuration. The optional attribute should work for other configuration
     * classes, too.
     */
    public void testLoadOptionalNonFileBased() throws ConfigurationException
    {
        CombinedConfiguration config = prepareOptionalTest("configuration", false);
        assertTrue("Configuration not empty", config.isEmpty());
        assertEquals("Wrong number of configurations", 0, config
                .getNumberOfConfigurations());
    }

    /**
     * Tests loading an embedded optional configuration builder with the force
     * create attribute.
     */
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
     * Tests loading a definition file with multiple different sources.
     */
    public void testLoadDifferentSources() throws ConfigurationException
    {
        factory.setFileName(MULTI_FILE);
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
        assertEquals(System.getProperty("java.version"), config.getString("java.version"));

        // test EnvironmentConfiguration
        assertNotNull("JAVA_HOME property not found", config.getProperty("JAVA_HOME"));
    }

    /**
     * Tests if the base path is correctly evaluated.
     */
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
    public void testComplexInitialization() throws ConfigurationException
    {
        factory.setFileName(INIT_FILE);
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
    public void testCombinedConfiguration() throws ConfigurationException
    {
        factory.setFileName(INIT_FILE);
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
        Set<?> names = cc2.getConfigurationNames();
        assertEquals("Wrong number of contained additional configs", 2, names
                .size());
        assertTrue("Config 1 not contained", names.contains("combiner1"));
        assertTrue("Config 2 not contained", names.contains("combiner2"));
    }

    /**
     * Tests the structure of the returned combined configuration if there is no
     * additional section.
     */
    public void testCombinedConfigurationNoAdditional()
            throws ConfigurationException
    {
        factory.setFileName(TEST_FILE);
        CombinedConfiguration cc = factory.getConfiguration(true);
        assertNull("Additional configuration was found", cc
                .getConfiguration(DefaultConfigurationBuilder.ADDITIONAL_NAME));
    }

    /**
     * Tests whether the list node definition was correctly processed.
     */
    public void testCombinedConfigurationListNodes()
            throws ConfigurationException
    {
        factory.setFileName(INIT_FILE);
        CombinedConfiguration cc = factory.getConfiguration(true);
        Set<?> listNodes = cc.getNodeCombiner().getListNodes();
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
     * Tests whether XML settings can be inherited.
     */
    public void testLoadXMLWithSettings() throws ConfigurationException,
            IOException
    {
        File confDir = ConfigurationAssert.TEST_DIR;
        File targetDir = ConfigurationAssert.OUT_DIR;
        File testXMLSource = new File(confDir, "testDtd.xml");
        File testXMLValidationSource = new File(confDir,
                "testValidateInvalid.xml");
        File testSavedXML = new File(targetDir, "testSave.xml");
        File testSavedFactory = new File(targetDir, "testSaveFactory.xml");
        File dtdFile = new File(confDir, "properties.dtd");
        final String publicId = "http://commons.apache.org/test.dtd";

        XMLConfiguration config = new XMLConfiguration(testXMLSource);
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
        factory.registerEntityId(publicId, dtdFile.toURI().toURL());
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
    public void testExtendedClass() throws ConfigurationException
    {
        factory.setFileName(CLASS_FILE);
        CombinedConfiguration cc = factory.getConfiguration(true);
        String prop = (String)cc.getProperty("test");
        assertEquals("Expected 'Extended', actual '" + prop + "'", "Extended", prop);
        assertTrue("Wrong result class: " + cc.getClass(),
                cc instanceof TestDefaultConfigurationBuilder.ExtendedCombinedConfiguration);
    }

    /**
     * Tests loading a configuration definition file that defines new providers.
     */
    public void testConfigurationProvider() throws ConfigurationException
    {
        factory.setFileName(PROVIDER_FILE);
        factory.getConfiguration(true);
        DefaultConfigurationBuilder.ConfigurationProvider provider = factory
                .providerForTag("test");
        assertNotNull("Provider 'test' not registered", provider);
    }

    public void testGlobalLookup() throws Exception
    {
        factory.setFileName(GLOBAL_LOOKUP_FILE);
        CombinedConfiguration cc = factory.getConfiguration(true);
        String value = cc.getInterpolator().lookup("test:test_key");
        assertNotNull("The test key was not located", value);
        assertEquals("Incorrect value retrieved","test.value",value);
    }

    public void testSystemProperties() throws Exception
    {
        factory.setFileName(SYSTEM_PROPS_FILE);
        factory.getConfiguration(true);
        String value = System.getProperty("key1");
        assertNotNull("The test key was not located", value);
        assertEquals("Incorrect value retrieved","value1",value);
    }


    public void testValidation() throws Exception
    {
        factory.setFileName(VALIDATION_FILE);
        factory.getConfiguration(true);
        String value = System.getProperty("key1");
        assertNotNull("The test key was not located", value);
        assertEquals("Incorrect value retrieved","value1",value);
    }

    public void testMultiTenentConfiguration() throws Exception
    {
        factory.setFileName(MULTI_TENENT_FILE);
        System.clearProperty("Id");

        CombinedConfiguration config = factory.getConfiguration(true);
        assertTrue("Incorrect configuration", config instanceof DynamicCombinedConfiguration);

        verify("1001", config, 15);
        verify("1002", config, 25);
        verify("1003", config, 35);
        verify("1004", config, 50);
        verify("1005", config, 50);
    }

    public void testMultiTenentConfiguration2() throws Exception
    {
        factory.setFileName(MULTI_TENENT_FILE);
        System.setProperty("Id", "1004");

        CombinedConfiguration config = factory.getConfiguration(true);
        assertTrue("Incorrect configuration", config instanceof DynamicCombinedConfiguration);

        verify("1001", config, 15);
        verify("1002", config, 25);
        verify("1003", config, 35);
        verify("1004", config, 50);
        verify("1005", config, 50);
    }

    public void testMultiTenentConfiguration3() throws Exception
    {
        factory.setFileName(MULTI_TENENT_FILE);
        System.setProperty("Id", "1005");

        CombinedConfiguration config = factory.getConfiguration(true);
        assertTrue("Incorrect configuration", config instanceof DynamicCombinedConfiguration);

        verify("1001", config, 15);
        verify("1002", config, 25);
        verify("1003", config, 35);
        verify("1004", config, 50);
        verify("1005", config, 50);
    }

    public void testFileMonitor1() throws Exception
    {
        // create a new configuration
        File input = new File("target/test-classes/testMultiConfiguration_1001.xml");
        FileObject output = getFile(getBasePath() + FILEMONITOR_1001_FILE);
        output.delete();
        output.getParent().createFolder();
        copyFile(input, output);

        factory.setFileName(getBasePath() + FILEMONITOR2_FILE);
        System.getProperties().remove("Id");

        CombinedConfiguration config = factory.getConfiguration(true);
        assertNotNull(config);
        config.addConfigurationListener(this);
        verify("1001", config, 15);

        // Allow time for FileMonitor to set up.
        Thread.sleep(1000);
        XMLConfiguration x = new XMLConfiguration(getBasePath() + FILEMONITOR_1001_FILE);
        x.setProperty("rowsPerPage", "50");
        x.save();
        // Let FileMonitor detect the change.
        //Thread.sleep(2000);
        waitForChange();
        verify("1001", config, 50);
        output.delete();
        VFSFileMonitorReloadingStrategy.stopMonitor();
    }

    public void testFileMonitor2() throws Exception
    {
        // create a new configuration
        File input = new File("target/test-classes/testMultiConfiguration_1002.xml");
        FileObject output = getFile(getBasePath() + FILEMONITOR_1002_FILE);
        output.delete();

        factory.setFileName(getBasePath() + FILEMONITOR2_FILE);
        System.getProperties().remove("Id");

        CombinedConfiguration config = factory.getConfiguration(true);
        assertNotNull(config);
        config.addConfigurationListener(this);

        verify("1002", config, 50);
        Thread.sleep(1000);

        output.getParent().createFolder();
        copyFile(input, output);

        // Allow time for the monitor to notice the change.
        //Thread.sleep(2000);
        waitForChange();
        verify("1002", config, 25);
        output.delete();
        VFSFileMonitorReloadingStrategy.stopMonitor();
    }


    private void verify(String key, CombinedConfiguration config, int rows)
    {
        System.setProperty("Id", key);
        int actual = config.getInt("rowsPerPage");
        assertTrue("expected: " + rows + " actual: " + actual, actual == rows);
    }

    public Map getOptions()
    {
        return this.options;
    }

    /**
     * Helper method for checking if a save operation was successful. Loads a
     * saved configuration and then tests against a reference configuration.
     * @param conf the original configuration
     * @param newConfig the configuration to check
     * @throws ConfigurationException if an error occurs
     */
    private void checkSavedConfig(XMLConfiguration conf, FileConfiguration newConfig)
        throws ConfigurationException
    {
        newConfig.load();
        ConfigurationAssert.assertEquals(conf, newConfig);
    }

    private FileObject getFile(String fileName) throws Exception
    {
        FileSystemManager manager = VFS.getManager();
        FileName file = manager.resolveURI(fileName);
        FileName base = file.getParent();
        FileName path = manager.resolveName(base, file.getBaseName());
        FileSystemOptions opts = new FileSystemOptions();
        return manager.resolveFile(path.getURI(), opts);
    }

    private void copyFile(File input, FileObject output) throws IOException
    {
        Reader reader = new FileReader(input);
        Writer writer = new OutputStreamWriter(output.getContent().getOutputStream());
        char[] buffer = new char[4096];
        int n = 0;
        while (-1 != (n = reader.read(buffer)))
        {
            writer.write(buffer, 0, n);
        }
        reader.close();
        writer.close();
    }

    private void waitForChange()
    {
        synchronized(this)
        {
            try
            {
                int count = 0;
                while (!configChanged && count++ <= 3)
                {
                    this.wait(5000);
                }
            }
            catch (InterruptedException ie)
            {
                throw new IllegalStateException("wait timed out");
            }
            finally
            {
                configChanged = false;
            }
        }
    }

    public void configurationChanged(ConfigurationEvent event)
    {
        if (event.getType() == AbstractFileConfiguration.EVENT_CONFIG_CHANGED)
        {
            synchronized(this)
            {
                configChanged = true;
                this.notify();
            }
        }
    }
}
