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
package org.apache.commons.configuration.builder.combined;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.BaseHierarchicalConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationAssert;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultFileSystem;
import org.apache.commons.configuration.FileSystem;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.apache.commons.configuration.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration.builder.ConfigurationBuilder;
import org.apache.commons.configuration.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration.builder.XMLBuilderParametersImpl;
import org.apache.commons.configuration.event.ConfigurationErrorListener;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.resolver.CatalogResolver;
import org.apache.commons.configuration.resolver.DefaultEntityResolver;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code CombinedConfigurationBuilder}.
 *
 * @version $Id$
 */
public class TestCombinedConfigurationBuilder
{
    /** Test configuration definition file. */
    private static final File TEST_FILE = ConfigurationAssert
            .getTestFile("testDigesterConfiguration.xml");

    /** Test file name for a sub configuration. */
    private static final String TEST_SUB_XML = "test.xml";

    /** Constant for a named builder. */
    private static final String BUILDER_NAME = "subBuilderName";

    /** Stores the object to be tested. */
    private CombinedConfigurationBuilder factory;

    @Before
    public void setUp() throws Exception
    {
        System.setProperty("java.naming.factory.initial",
                "org.apache.commons.configuration.MockInitialContextFactory");
        System.setProperty("test_file_xml", TEST_SUB_XML);
        System.setProperty("test_file_combine", "testcombine1.xml");
        factory = new CombinedConfigurationBuilder();
    }

    /**
     * Creates a configuration builder for the definition configuration which
     * always returns the passed in definition configuration.
     *
     * @param defConfig the definition configuration
     * @return the definition builder
     */
    private static BasicConfigurationBuilder<? extends HierarchicalConfiguration> createDefinitionBuilder(
            HierarchicalConfiguration defConfig)
    {
        return new ConstantConfigurationBuilder(defConfig);
    }

    /**
     * Convenience method for creating a definition configuration. This method
     * creates a configuration containing a tag referring to a configuration
     * source. The tag has attributes defined by the given map.
     *
     * @param tag the name of the tag to create
     * @param attrs the attributes of this tag
     * @return the definition configuration
     */
    private static HierarchicalConfiguration createDefinitionConfig(String tag,
            Map<String, Object> attrs)
    {
        HierarchicalConfiguration defConfig =
                new BaseHierarchicalConfiguration();
        String prefix = "override." + tag;
        for (Map.Entry<String, Object> e : attrs.entrySet())
        {
            defConfig.addProperty(prefix + "[@" + e.getKey() + "]",
                    e.getValue());
        }
        return defConfig;
    }

    /**
     * Tries to build a configuration if no definition builder is provided.
     */
    @Test(expected = ConfigurationException.class)
    public void testNoDefinitionBuilder() throws ConfigurationException
    {
        factory.getConfiguration();
    }

    /**
     * Tests if the configuration was correctly created by the factory.
     *
     * @return the combined configuration obtained from the builder
     */
    private CombinedConfiguration checkConfiguration()
            throws ConfigurationException
    {
        CombinedConfiguration compositeConfiguration =
                factory.getConfiguration();

        assertEquals("Number of configurations", 3,
                compositeConfiguration.getNumberOfConfigurations());
        assertEquals(PropertiesConfiguration.class, compositeConfiguration
                .getConfiguration(0).getClass());
        assertEquals(XMLPropertiesConfiguration.class, compositeConfiguration
                .getConfiguration(1).getClass());
        assertEquals(XMLConfiguration.class, compositeConfiguration
                .getConfiguration(2).getClass());

        // check the first configuration
        PropertiesConfiguration pc =
                (PropertiesConfiguration) compositeConfiguration
                        .getConfiguration(0);
        assertNotNull("No properties configuration", pc);

        // check some properties
        checkProperties(compositeConfiguration);
        return compositeConfiguration;
    }

    /**
     * Checks if the passed in configuration contains the expected properties.
     *
     * @param compositeConfiguration the configuration to check
     */
    private void checkProperties(Configuration compositeConfiguration)
    {
        assertTrue("Make sure we have loaded our key",
                compositeConfiguration.getBoolean("test.boolean"));
        assertEquals("I'm complex!",
                compositeConfiguration
                        .getProperty("element2.subelement.subsubelement"));
        assertEquals("property in the XMLPropertiesConfiguration", "value1",
                compositeConfiguration.getProperty("key1"));
    }

    /**
     * Tests loading a simple configuration definition file.
     */
    @Test
    public void testLoadConfiguration() throws ConfigurationException
    {
        factory.configure(new FileBasedBuilderParametersImpl()
                .setFile(TEST_FILE));
        checkConfiguration();
    }

    /**
     * Tests loading a configuration definition file with an additional section.
     */
    @Test
    public void testLoadAdditional() throws ConfigurationException
    {
        File additonalFile =
                ConfigurationAssert
                        .getTestFile("testDigesterConfiguration2.xml");
        factory.configure(new FileBasedBuilderParametersImpl()
                .setFile(additonalFile));
        CombinedConfiguration compositeConfiguration =
                factory.getConfiguration();
        assertEquals("Verify how many configs", 2,
                compositeConfiguration.getNumberOfConfigurations());

        // Test if union was constructed correctly
        Object prop = compositeConfiguration.getProperty("tables.table.name");
        assertTrue(prop instanceof Collection);
        assertEquals(3, ((Collection<?>) prop).size());
        assertEquals("users",
                compositeConfiguration.getProperty("tables.table(0).name"));
        assertEquals("documents",
                compositeConfiguration.getProperty("tables.table(1).name"));
        assertEquals("tasks",
                compositeConfiguration.getProperty("tables.table(2).name"));

        prop =
                compositeConfiguration
                        .getProperty("tables.table.fields.field.name");
        assertTrue(prop instanceof Collection);
        assertEquals(17, ((Collection<?>) prop).size());

        assertEquals("smtp.mydomain.org",
                compositeConfiguration.getString("mail.host.smtp"));
        assertEquals("pop3.mydomain.org",
                compositeConfiguration.getString("mail.host.pop"));

        // This was overriden
        assertEquals("masterOfPost",
                compositeConfiguration.getString("mail.account.user"));
        assertEquals("topsecret",
                compositeConfiguration.getString("mail.account.psswd"));

        // This was overridden, too, but not in additional section
        assertEquals("enhanced factory",
                compositeConfiguration.getString("test.configuration"));
    }

    /**
     * Tests loading a definition file that contains optional configurations.
     */
    @Test
    public void testLoadOptional() throws Exception
    {
        File optionalFile =
                ConfigurationAssert
                        .getTestFile("testDigesterOptionalConfiguration.xml");
        factory.configure(new FileBasedBuilderParametersImpl()
                .setFile(optionalFile));
        Configuration config = factory.getConfiguration();
        assertTrue(config.getBoolean("test.boolean"));
        assertEquals("value", config.getProperty("element"));
    }

    /**
     * Tests loading a definition file with optional and non optional
     * configuration sources. One non optional does not exist, so this should
     * cause an exception.
     */
    @Test(expected = ConfigurationException.class)
    public void testLoadOptionalWithException() throws ConfigurationException
    {
        File optionalExFile =
                ConfigurationAssert
                        .getTestFile("testDigesterOptionalConfigurationEx.xml");
        factory.configure(new FileBasedBuilderParametersImpl()
                .setFile(optionalExFile));
        factory.getConfiguration();
    }

    /**
     * Tests whether the force-create attribute is taken into account.
     */
    @Test
    public void testLoadOptionalForceCreate() throws ConfigurationException
    {
        String name = "optionalConfig";
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("fileName", "nonExisting.xml");
        attrs.put("config-name", name);
        attrs.put("config-optional", Boolean.TRUE);
        attrs.put("config-forceCreate", Boolean.TRUE);
        HierarchicalConfiguration defConfig =
                createDefinitionConfig("xml", attrs);
        BasicConfigurationBuilder<? extends HierarchicalConfiguration> defBuilder =
                createDefinitionBuilder(defConfig);
        factory.configure(new CombinedBuilderParametersImpl()
                .setDefinitionBuilder(defBuilder));
        CombinedConfiguration cc = factory.getConfiguration();
        assertEquals("Wrong number of configurations", 1,
                cc.getNumberOfConfigurations());
        assertTrue("Wrong configuration type",
                cc.getConfiguration(name) instanceof XMLConfiguration);
    }

    /**
     * Tests whether the names of sub builders can be queried.
     */
    @Test
    public void testBuilderNames() throws ConfigurationException
    {
        factory.configure(new FileBasedBuilderParametersImpl()
                .setFile(TEST_FILE));
        Set<String> names = factory.builderNames();
        List<String> expected = Arrays.asList("props", "xml");
        assertEquals("Wrong number of named builders", expected.size(),
                names.size());
        assertTrue("Wrong builder names: " + names, names.containsAll(expected));
    }

    /**
     * Tests that the collection with builder names cannot be manipulated.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testBuilderNamesManipulate() throws ConfigurationException
    {
        factory.configure(new FileBasedBuilderParametersImpl()
                .setFile(TEST_FILE));
        Set<String> names = factory.builderNames();
        names.clear();
    }

    /**
     * Tests whether named builders can be accessed.
     */
    @Test
    public void testGetNamedBuilder() throws ConfigurationException
    {
        factory.configure(new FileBasedBuilderParametersImpl()
                .setFile(TEST_FILE));
        ConfigurationBuilder<? extends Configuration> propBuilder =
                factory.getNamedBuilder("props");
        assertTrue("Wrong builder class",
                propBuilder instanceof FileBasedConfigurationBuilder);
        assertTrue(
                "Wrong sub configuration",
                propBuilder.getConfiguration() instanceof PropertiesConfiguration);
    }

    /**
     * Tries to query a non-existing builder by name.
     */
    @Test(expected = ConfigurationException.class)
    public void testGetNamedBuilderUnknown() throws ConfigurationException
    {
        factory.configure(new FileBasedBuilderParametersImpl()
                .setFile(TEST_FILE));
        factory.getNamedBuilder("nonExistingBuilder");
    }

    /**
     * Prepares a test with a combined configuration that uses a single sub
     * builder. This method adds some default attributes to the given map,
     * creates the corresponding definition builder and configures the combined
     * builder.
     *
     * @param attrs the map with attributes
     * @return the definition builder
     */
    private BasicConfigurationBuilder<? extends HierarchicalConfiguration> prepareSubBuilderTest(
            Map<String, Object> attrs)
    {
        attrs.put("fileName", TEST_SUB_XML);
        attrs.put("config-name", BUILDER_NAME);
        HierarchicalConfiguration defConfig =
                createDefinitionConfig("xml", attrs);
        BasicConfigurationBuilder<? extends HierarchicalConfiguration> defBuilder =
                createDefinitionBuilder(defConfig);
        factory.configure(new CombinedBuilderParametersImpl()
                .setDefinitionBuilder(defBuilder));
        return defBuilder;
    }

    /**
     * Tests a reset of the builder. The configuration instance should be
     * created anew.
     */
    @Test
    public void testResetBuilder() throws ConfigurationException
    {
        Map<String, Object> attrs = new HashMap<String, Object>();
        BasicConfigurationBuilder<? extends HierarchicalConfiguration> defBuilder =
                prepareSubBuilderTest(attrs);
        CombinedConfiguration cc = factory.getConfiguration();
        ConfigurationBuilder<? extends Configuration> subBuilder =
                factory.getNamedBuilder(BUILDER_NAME);
        defBuilder.reset();
        CombinedConfiguration cc2 = factory.getConfiguration();
        assertNotSame("No new configuration instance", cc, cc2);
        ConfigurationBuilder<? extends Configuration> subBuilder2 =
                factory.getNamedBuilder(BUILDER_NAME);
        assertNotSame("No new sub builder instance", subBuilder, subBuilder2);
    }

    /**
     * Tests whether a reloading sub builder can be created.
     */
    @Test
    public void testReloadingBuilder() throws ConfigurationException
    {
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("config-reload", Boolean.TRUE);
        prepareSubBuilderTest(attrs);
        assertTrue(
                "Not a reloading builder",
                factory.getNamedBuilder(BUILDER_NAME) instanceof ReloadingFileBasedConfigurationBuilder);
    }

    /**
     * Tests whether a reset of one of the sub builders causes the combined
     * configuration to be re-created.
     */
    @Test
    public void testReactOnSubBuilderChange() throws ConfigurationException
    {
        Map<String, Object> attrs = new HashMap<String, Object>();
        prepareSubBuilderTest(attrs);
        CombinedConfiguration cc = factory.getConfiguration();
        BasicConfigurationBuilder<?> subBuilder =
                (BasicConfigurationBuilder<?>) factory
                        .getNamedBuilder(BUILDER_NAME);
        subBuilder.reset();
        assertNotSame("Configuration not newly created", cc,
                factory.getConfiguration());
    }

    /**
     * Tests that change listeners registered at sub builders are removed on a
     * reset.
     */
    @Test
    public void testRemoveSubBuilderListener() throws ConfigurationException
    {
        Map<String, Object> attrs = new HashMap<String, Object>();
        prepareSubBuilderTest(attrs);
        BasicConfigurationBuilder<?> subBuilder =
                (BasicConfigurationBuilder<?>) factory
                        .getNamedBuilder(BUILDER_NAME);
        factory.reset();
        prepareSubBuilderTest(attrs);
        CombinedConfiguration cc = factory.getConfiguration();
        BasicConfigurationBuilder<?> subBuilder2 =
                (BasicConfigurationBuilder<?>) factory
                        .getNamedBuilder(BUILDER_NAME);
        assertNotSame("Got the same sub builder", subBuilder, subBuilder2);
        subBuilder.reset();
        assertSame("Configuration was reset", cc, factory.getConfiguration());
    }

    /**
     * Helper method for testing the attributes of a combined configuration
     * created by the builder.
     *
     * @param cc the configuration to be checked
     */
    private static void checkCombinedConfigAttrs(CombinedConfiguration cc)
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
    public void testCombinedConfigurationAttributes()
            throws ConfigurationException
    {
        File initFile =
                ConfigurationAssert
                        .getTestFile("testCCResultInitialization.xml");
        factory.configure(new FileBasedBuilderParametersImpl()
                .setFile(initFile));
        CombinedConfiguration cc = factory.getConfiguration();
        checkCombinedConfigAttrs(cc);
        CombinedConfiguration cc2 =
                (CombinedConfiguration) cc
                        .getConfiguration(CombinedConfigurationBuilder.ADDITIONAL_NAME);
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
        factory.configure(new FileBasedBuilderParametersImpl()
                .setFile(TEST_FILE));
        CombinedConfiguration cc = factory.getConfiguration();
        assertNull(
                "Additional configuration was found",
                cc.getConfiguration(CombinedConfigurationBuilder.ADDITIONAL_NAME));
    }

    /**
     * Tests whether the list node definition was correctly processed.
     */
    @Test
    public void testCombinedConfigurationListNodes()
            throws ConfigurationException
    {
        File initFile =
                ConfigurationAssert
                        .getTestFile("testCCResultInitialization.xml");
        factory.configure(new FileBasedBuilderParametersImpl()
                .setFile(initFile));
        CombinedConfiguration cc = factory.getConfiguration();
        Set<String> listNodes = cc.getNodeCombiner().getListNodes();
        assertEquals("Wrong number of list nodes", 2, listNodes.size());
        assertTrue("table node not a list node", listNodes.contains("table"));
        assertTrue("list node not a list node", listNodes.contains("list"));

        CombinedConfiguration cca =
                (CombinedConfiguration) cc
                        .getConfiguration(CombinedConfigurationBuilder.ADDITIONAL_NAME);
        listNodes = cca.getNodeCombiner().getListNodes();
        assertTrue("Found list nodes for additional combiner",
                listNodes.isEmpty());
    }

    /**
     * Tests whether a custom provider can be registered.
     */
    @Test
    public void testCustomBuilderProvider() throws ConfigurationException
    {
        String tagName = "myTestTag";
        final HierarchicalConfiguration dataConf =
                new BaseHierarchicalConfiguration();
        dataConf.addProperty(tagName, Boolean.TRUE);
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("config-name", BUILDER_NAME);
        attrs.put("config-at", "tests");
        factory.configure(new CombinedBuilderParametersImpl()
                .setDefinitionBuilder(
                        createDefinitionBuilder(createDefinitionConfig(tagName,
                                attrs))).registerProvider(tagName,
                        new ConfigurationBuilderProvider()
                        {
                            public ConfigurationBuilder<? extends Configuration> getConfiguration(
                                    ConfigurationDeclaration decl)
                                    throws ConfigurationException
                            {
                                return new ConstantConfigurationBuilder(
                                        dataConf);
                            }
                        }));
        CombinedConfiguration cc = factory.getConfiguration();
        assertEquals("Configuration not added", dataConf,
                cc.getConfiguration(BUILDER_NAME));
        assertEquals("Property not set", Boolean.TRUE,
                cc.getProperty("tests." + tagName));
    }

    /**
     * Tests whether a custom provider can be defined in the definition file.
     */
    @Test
    public void testProviderInDefinitionConfig() throws ConfigurationException
    {
        factory.configure(new FileBasedBuilderParametersImpl()
                .setFile(ConfigurationAssert
                        .getTestFile("testCCCustomProvider.xml")));
        CombinedConfiguration cc = factory.getConfiguration();
        assertTrue("Property not found", cc.getBoolean("testKey"));
    }

    /**
     * Tests whether a file with system properties can be specified in the
     * configuration definition file and that system properties can be added to
     * the resulting configuration.
     */
    @Test
    public void testSystemProperties() throws ConfigurationException
    {
        File systemFile =
                ConfigurationAssert.getTestFile("testCCSystemProperties.xml");
        factory.configure(new FileBasedBuilderParametersImpl()
                .setFile(systemFile));
        CombinedConfiguration cc = factory.getConfiguration();
        assertTrue("System property not found", cc.containsKey("user.name"));
        assertEquals("Properties not added", "value1",
                System.getProperty("key1"));
    }

    /**
     * Tests whether environment properties can be added as a configuration
     * source.
     */
    @Test
    public void testEnvironmentProperties() throws ConfigurationException
    {
        File envFile =
                ConfigurationAssert.getTestFile("testCCEnvProperties.xml");
        factory.configure(new FileBasedBuilderParametersImpl().setFile(envFile));
        CombinedConfiguration cc = factory.getConfiguration();
        assertFalse("Configuration is empty", cc.isEmpty());
        for (Map.Entry<String, String> e : System.getenv().entrySet())
        {
            assertEquals("Wrong value for property: " + e.getKey(),
                    e.getValue(), cc.getString(e.getKey()));
        }
    }

    /**
     * Tests whether a JNDI configuration can be integrated into the combined
     * configuration.
     */
    @Test
    public void testJndiConfiguration() throws ConfigurationException
    {
        File multiFile =
                ConfigurationAssert
                        .getTestFile("testDigesterConfiguration3.xml");
        factory.configure(new CombinedBuilderParametersImpl()
                .setDefinitionBuilderParameters(new FileBasedBuilderParametersImpl()
                        .setFile(multiFile)));
        CombinedConfiguration cc = factory.getConfiguration();
        assertTrue("JNDI property not found", cc.getBoolean("test.onlyinjndi"));
    }

    /**
     * Tests whether an INI configuration source can be added to the combined
     * configuration.
     */
    @Test
    public void testINIConfiguration() throws ConfigurationException
    {
        File multiFile =
                ConfigurationAssert
                        .getTestFile("testDigesterConfiguration3.xml");
        factory.configure(new CombinedBuilderParametersImpl()
                .setDefinitionBuilderParameters(new FileBasedBuilderParametersImpl()
                        .setFile(multiFile)));
        CombinedConfiguration cc = factory.getConfiguration();
        assertEquals("Property from ini file not found", "yes",
                cc.getString("testini.loaded"));
    }

    /**
     * Tests whether an entity resolver can be defined in the definition file.
     */
    @Test
    public void testCustomEntityResolver() throws ConfigurationException
    {
        File resolverFile =
                ConfigurationAssert.getTestFile("testCCEntityResolver.xml");
        factory.configure(new FileBasedBuilderParametersImpl()
                .setFile(resolverFile));
        CombinedConfiguration cc = factory.getConfiguration();
        XMLConfiguration xmlConf =
                (XMLConfiguration) cc.getConfiguration("xml");
        assertTrue("Wrong entity resolver: " + xmlConf.getEntityResolver(),
                xmlConf.getEntityResolver() instanceof EntityResolverTestImpl);
    }

    /**
     * Tests whether the entity resolver is initialized with other XML-related
     * properties.
     */
    @Test
    public void testConfigureEntityResolverWithProperties()
            throws ConfigurationException
    {
        HierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        config.addProperty("header.entity-resolver[@config-class]",
                EntityResolverWithPropertiesTestImpl.class.getName());
        XMLBuilderParametersImpl xmlParams = new XMLBuilderParametersImpl();
        FileSystem fs = EasyMock.createMock(FileSystem.class);
        String baseDir = ConfigurationAssert.OUT_DIR_NAME;
        xmlParams.setBasePath(baseDir);
        xmlParams.setFileSystem(fs);
        factory.configureEntityResolver(config, xmlParams);
        EntityResolverWithPropertiesTestImpl resolver =
                (EntityResolverWithPropertiesTestImpl) xmlParams
                        .getEntityResolver();
        assertSame("File system not set", fs, resolver.getFileSystem());
        assertSame("Base directory not set", baseDir, resolver.getBaseDir());
    }

    /**
     * Tests whether a default file system can be configured in the definition
     * file.
     */
    @Test
    public void testCustomFileSystem() throws ConfigurationException
    {
        File fsFile = ConfigurationAssert.getTestFile("testCCFileSystem.xml");
        factory.configure(new FileBasedBuilderParametersImpl().setFile(fsFile));
        factory.getConfiguration();
        FileBasedConfigurationBuilder<? extends Configuration> xmlBuilder =
                (FileBasedConfigurationBuilder<? extends Configuration>) factory
                        .getNamedBuilder("xml");
        assertTrue("Wrong file system: "
                + xmlBuilder.getFileHandler().getFileSystem(), xmlBuilder
                .getFileHandler().getFileSystem() instanceof FileSystemTestImpl);
    }

    /**
     * Tests whether a default base path for all file-based child configurations
     * can be set in the builder parameters.
     */
    @Test
    public void testDefaultBasePathInParameters() throws ConfigurationException
    {
        File testFile =
                ConfigurationAssert.getTestFile("testCCSystemProperties.xml");
        String basePath = ConfigurationAssert.OUT_DIR.getAbsolutePath();
        factory.configure(new CombinedBuilderParametersImpl().setBasePath(
                basePath).setDefinitionBuilderParameters(
                new FileBasedBuilderParametersImpl().setFile(testFile)));
        factory.getConfiguration();
        XMLBuilderParametersImpl xmlParams = new XMLBuilderParametersImpl();
        factory.initChildBuilderParameters(xmlParams);
        assertEquals("Base path not set", basePath, xmlParams.getFileHandler()
                .getBasePath());
    }

    /**
     * Tests whether the default base path for file-based configurations is
     * derived from the configuration definition builder.
     */
    @Test
    public void testDefaultBasePathFromDefinitionBuilder()
            throws ConfigurationException
    {
        String testFile = "testCCSystemProperties.xml";
        String basePath = ConfigurationAssert.TEST_DIR.getAbsolutePath();
        factory.configure(new CombinedBuilderParametersImpl()
                .setDefinitionBuilderParameters(new FileBasedBuilderParametersImpl()
                        .setBasePath(basePath).setFileName(testFile)));
        factory.getConfiguration();
        XMLBuilderParametersImpl xmlParams = new XMLBuilderParametersImpl();
        factory.initChildBuilderParameters(xmlParams);
        assertEquals("Base path not set", basePath, xmlParams.getFileHandler()
                .getBasePath());
    }

    /**
     * Tests if the base path is correctly evaluated.
     */
    @Test
    public void testBasePathForChildConfigurations()
            throws ConfigurationException
    {
        HierarchicalConfiguration defConfig =
                new BaseHierarchicalConfiguration();
        defConfig.addProperty("properties[@fileName]", "test.properties");
        File deepDir = new File(ConfigurationAssert.TEST_DIR, "config/deep");
        factory.configure(new CombinedBuilderParametersImpl().setBasePath(
                deepDir.getAbsolutePath()).setDefinitionBuilder(
                new ConstantConfigurationBuilder(defConfig)));
        CombinedConfiguration config = factory.getConfiguration();
        assertEquals("Wrong property value", "somevalue",
                config.getString("somekey"));
    }

    /**
     * Tests whether the resulting combined configuration can be customized.
     */
    @Test
    public void testCustomResultConfiguration() throws ConfigurationException
    {
        File testFile =
                ConfigurationAssert.getTestFile("testCCResultClass.xml");
        factory.configure(new CombinedBuilderParametersImpl()
                .setDefinitionBuilderParameters(
                        new XMLBuilderParametersImpl().setFile(testFile))
                .setListDelimiter('.').setThrowExceptionOnMissing(false));
        CombinedConfiguration cc = factory.getConfiguration();
        assertTrue("Wrong configuration class: " + cc.getClass(),
                cc instanceof CombinedConfigurationTestImpl);
        assertTrue("Wrong exception flag", cc.isThrowExceptionOnMissing());
        assertEquals("Wrong list delimiter", '.', cc.getListDelimiter());
    }

    /**
     * Tests whether a configuration builder can itself be declared in a
     * configuration definition file.
     */
    @Test
    public void testConfigurationBuilderProvider()
            throws ConfigurationException
    {
        HierarchicalConfiguration defConfig =
                new BaseHierarchicalConfiguration();
        defConfig.addProperty("override.configuration[@fileName]",
                TEST_FILE.getAbsolutePath());
        factory.configure(new CombinedBuilderParametersImpl()
                .setDefinitionBuilder(new ConstantConfigurationBuilder(
                        defConfig)));
        CombinedConfiguration cc = factory.getConfiguration();
        assertEquals("Wrong number of configurations", 1,
                cc.getNumberOfConfigurations());
        checkProperties(cc);
    }

    /**
     * Tests whether basic properties defined for the combined configuration are
     * inherited by a child combined configuration builder.
     */
    @Test
    public void testConfigurationBuilderProviderInheritBasicProperties()
            throws ConfigurationException
    {
        File testFile =
                ConfigurationAssert
                        .getTestFile("testCCCombinedChildBuilder.xml");
        factory.configure(new CombinedBuilderParametersImpl()
                .setDefinitionBuilderParameters(
                        new XMLBuilderParametersImpl().setFile(testFile))
                .setListDelimiter('*'));
        CombinedConfiguration cc = factory.getConfiguration();
        CombinedConfiguration cc2 =
                (CombinedConfiguration) cc.getConfiguration("subcc");
        assertFalse("Wrong exception flag", cc2.isThrowExceptionOnMissing());
        assertEquals("Wrong list delimiter", '*', cc2.getListDelimiter());
    }

    /**
     * Tests whether a child configuration builder inherits the event listeners
     * from its parent.
     */
    @Test
    public void testConfigurationBuilderProviderInheritEventListeners()
            throws ConfigurationException
    {
        ConfigurationListener cl =
                EasyMock.createNiceMock(ConfigurationListener.class);
        ConfigurationErrorListener el =
                EasyMock.createNiceMock(ConfigurationErrorListener.class);
        EasyMock.replay(cl, el);
        File testFile =
                ConfigurationAssert
                        .getTestFile("testCCCombinedChildBuilder.xml");
        factory.configure(new XMLBuilderParametersImpl().setFile(testFile))
                .addConfigurationListener(cl).addErrorListener(el);
        CombinedConfiguration cc = factory.getConfiguration();
        CombinedConfiguration cc2 =
                (CombinedConfiguration) cc.getConfiguration("subcc");
        assertTrue("Configuration listener not found", cc2
                .getConfigurationListeners().contains(cl));
        assertTrue("Error listener not found", cc2.getErrorListeners()
                .contains(el));
    }

    /**
     * Tests whether custom builder providers are inherited to child combined
     * configuration builder providers.
     */
    @Test
    public void testConfigurationBuilderProviderInheritCustomProviders()
            throws ConfigurationException
    {
        factory.configure(new FileBasedBuilderParametersImpl()
                .setFile(ConfigurationAssert
                        .getTestFile("testCCCustomProvider.xml")));
        factory.getConfiguration();
        CombinedBuilderParametersImpl ccparams =
                new CombinedBuilderParametersImpl();
        factory.initChildBuilderParameters(ccparams);
        assertNotNull("Custom provider not found",
                ccparams.providerForTag("test"));
    }

    /**
     * Tests whether the base path can be inherited to child combined
     * configuration builders.
     */
    @Test
    public void testConfigurationBuilderProviderInheritBasePath()
            throws ConfigurationException
    {
        File envFile =
                ConfigurationAssert.getTestFile("testCCEnvProperties.xml");
        String basePath = ConfigurationAssert.OUT_DIR.getAbsolutePath();
        factory.configure(new CombinedBuilderParametersImpl().setBasePath(
                basePath).setDefinitionBuilderParameters(
                new FileBasedBuilderParametersImpl().setFile(envFile)));
        factory.getConfiguration();
        CombinedBuilderParametersImpl params =
                new CombinedBuilderParametersImpl();
        factory.initChildBuilderParameters(params);
        assertEquals("Base path not set", basePath, params.getBasePath());
    }

    /**
     * A test builder provider implementation for testing whether providers can
     * be defined in the definition file.
     */
    public static class BuilderProviderTestImpl implements
            ConfigurationBuilderProvider
    {
        /** The test property key of the configuration to be created. */
        private String propertyKey;

        public String getPropertyKey()
        {
            return propertyKey;
        }

        public void setPropertyKey(String propertyKey)
        {
            this.propertyKey = propertyKey;
        }

        public ConfigurationBuilder<? extends Configuration> getConfiguration(
                ConfigurationDeclaration decl) throws ConfigurationException
        {
            HierarchicalConfiguration config =
                    new BaseHierarchicalConfiguration();
            config.addProperty(getPropertyKey(), Boolean.TRUE);
            return new ConstantConfigurationBuilder(config);
        }
    }

    /**
     * A test builder class which always returns the same configuration.
     */
    private static class ConstantConfigurationBuilder extends
            BasicConfigurationBuilder<HierarchicalConfiguration>
    {
        private final HierarchicalConfiguration configuration;

        public ConstantConfigurationBuilder(HierarchicalConfiguration conf)
        {
            super(HierarchicalConfiguration.class);
            configuration = conf;
        }

        @Override
        public HierarchicalConfiguration getConfiguration()
                throws ConfigurationException
        {
            return configuration;
        }
    }

    /**
     * A special entity resolver implementation for testing whether a resolver
     * can be defined in the definition file.
     */
    public static class EntityResolverTestImpl extends DefaultEntityResolver
    {
    }

    /**
     * A specialized entity resolver implementation for testing whether
     * properties of a catalog resolver are correctly set.
     */
    public static class EntityResolverWithPropertiesTestImpl extends
            CatalogResolver
    {
        /** The base directory. */
        private String baseDirectory;

        /** The file system. */
        private FileSystem fileSystem;

        public FileSystem getFileSystem()
        {
            return fileSystem;
        }

        @Override
        public void setFileSystem(FileSystem fileSystem)
        {
            super.setFileSystem(fileSystem);
            this.fileSystem = fileSystem;
        }

        public String getBaseDir()
        {
            return baseDirectory;
        }

        @Override
        public void setBaseDir(String baseDir)
        {
            super.setBaseDir(baseDir);
            baseDirectory = baseDir;
        }
    }

    /**
     * A test file system implementation for testing whether a custom file
     * system class can be specified in the configuration definition file.
     */
    public static class FileSystemTestImpl extends DefaultFileSystem
    {
    }

    /**
     * A test combined configuration class for testing whether a specific result
     * configuration class can be declared in the definition configuration.
     */
    public static class CombinedConfigurationTestImpl extends
            CombinedConfiguration
    {
        /**
         * The serial version UID.
         */
        private static final long serialVersionUID = 20121216L;
    }
}
