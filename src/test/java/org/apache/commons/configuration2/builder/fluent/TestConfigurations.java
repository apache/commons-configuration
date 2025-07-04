/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration2.builder.fluent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.combined.CombinedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.plist.PropertyListConfiguration;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code Configurations}.
 */
public class TestConfigurations {
    /** Constant for the name of the test properties file. */
    private static final String TEST_PROPERTIES = "test.properties";

    /** Constant for the name of the test XML file. */
    private static final String TEST_XML = "test.xml";

    /** Constant for the name of the test ini file. */
    private static final String TEST_INI = "test.ini";

    /** Constant for the name of the configuration definition file. */
    private static final String TEST_COMBINED = "testDigesterConfiguration.xml";

    /** Constant for the name of the test PList file. */
    private static final String TEST_PLIST = "test.plist";

    /**
     * Generates an absolute path for the test file with the given name.
     *
     * @param name the name of the test file
     * @return the full path to this file
     */
    private static String absolutePath(final String name) {
        return ConfigurationAssert.getTestFile(name).getAbsolutePath();
    }

    /**
     * Checks whether a combined configuration was successfully loaded.
     *
     * @param config the configuration instance to be checked.
     */
    private static void checkCombined(final Configuration config) {
        checkProperties(config);
        checkXML(config);
    }

    /**
     * Checks whether a test INI configuration was correctly loaded.
     *
     * @param config the configuration instance to be checked.
     */
    private static void checkINI(final INIConfiguration config) {
        assertEquals("yes", config.getProperty("testini.loaded"));
    }

    /**
     * Checks whether a property list configuration was correctly loaded.
     *
     * @param config the configuration instance to be checked.
     */
    private static void checkPList(final Configuration config) {
        assertEquals("string1", config.getProperty("simple-string"));
    }

    /**
     * Checks whether a test properties configuration was correctly loaded.
     *
     * @param config the configuration instance to be checked.
     */
    private static void checkProperties(final Configuration config) {
        assertEquals("true", config.getString("configuration.loaded"));
    }

    /**
     * Checks whether a test XML configuration was correctly loaded.
     *
     * @param config the configuration instance to be checked.
     */
    private static void checkXML(final Configuration config) {
        assertEquals("value", config.getProperty("element"));
    }

    /**
     * Tests whether a combined configuration builder can be constructed for a file.
     */
    @Test
    void testCombinedBuilderFromFile() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final CombinedConfigurationBuilder builder = configs.combinedBuilder(ConfigurationAssert.getTestFile(TEST_COMBINED));
        checkCombined(builder.getConfiguration());
    }

    /**
     * Tests whether a combined configuration builder can be constructed for a file path.
     */
    @Test
    void testCombinedBuilderFromPath() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final CombinedConfigurationBuilder builder = configs.combinedBuilder(absolutePath(TEST_COMBINED));
        checkCombined(builder.getConfiguration());
    }

    /**
     * Tests whether a combined configuration builder can be constructed for a URL.
     */
    @Test
    void testCombinedBuilderFromURL() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final CombinedConfigurationBuilder builder = configs.combinedBuilder(ConfigurationAssert.getTestURL(TEST_COMBINED));
        checkCombined(builder.getConfiguration());
    }

    /**
     * Tests whether a combined configuration can be loaded from a file.
     */
    @Test
    void testCombinedFromFile() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final CombinedConfiguration config = configs.combined(ConfigurationAssert.getTestFile(TEST_COMBINED));
        checkCombined(config);
    }

    /**
     * Tests whether a combined configuration can be loaded from a file path.
     */
    @Test
    void testCombinedFromPath() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final CombinedConfiguration config = configs.combined(absolutePath(TEST_COMBINED));
        checkCombined(config);
    }

    /**
     * Tests whether a combined configuration can be loaded from a URL.
     */
    @Test
    void testCombinedFromURL() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final CombinedConfiguration config = configs.combined(ConfigurationAssert.getTestURL(TEST_COMBINED));
        checkCombined(config);
    }

    /**
     * Tests whether a default {@code Parameters} instance is created if necessary.
     */
    @Test
    void testDefaultParameters() {
        final Configurations configs = new Configurations();
        assertNotNull(configs.getParameters());
    }

    /**
     * Tests whether a builder for a file-based configuration can be created if an input File is specified.
     */
    @Test
    void testFileBasedBuilderWithFile() {
        final Configurations configs = new Configurations();
        final File file = ConfigurationAssert.getTestFile(TEST_PROPERTIES);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder = configs.fileBasedBuilder(PropertiesConfiguration.class, file);
        assertEquals(file.toURI(), builder.getFileHandler().getFile().toURI());
    }

    /**
     * Tests whether a builder for a file-based configuration can be created if a file name is specified.
     */
    @Test
    void testFileBasedBuilderWithPath() {
        final Configurations configs = new Configurations();
        final String filePath = absolutePath(TEST_PROPERTIES);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder = configs.fileBasedBuilder(PropertiesConfiguration.class, filePath);
        assertEquals(filePath, builder.getFileHandler().getFileName());
    }

    /**
     * Tests whether a builder for a file-based configuration can be created if a URL is specified.
     */
    @Test
    void testFileBasedBuilderWithURL() {
        final Configurations configs = new Configurations();
        final URL url = ConfigurationAssert.getTestURL("test.properties");
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder = configs.fileBasedBuilder(PropertiesConfiguration.class, url);
        assertEquals(url, builder.getFileHandler().getURL());
    }

    /**
     * Tests whether a file-based configuration can be loaded from a file.
     */
    @Test
    void testFileBasedFile() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final PropertyListConfiguration config = configs.fileBased(PropertyListConfiguration.class, ConfigurationAssert.getTestFile(TEST_PLIST));
        checkPList(config);
    }

    /**
     * Tests whether a file-based configuration can be loaded from a file path.
     */
    @Test
    void testFileBasedPath() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final PropertyListConfiguration config = configs.fileBased(PropertyListConfiguration.class, absolutePath(TEST_PLIST));
        checkPList(config);
    }

    /**
     * Tests whether a file-based configuration can be loaded from a URL.
     */
    @Test
    void testFileBasedURL() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final PropertyListConfiguration config = configs.fileBased(PropertyListConfiguration.class, ConfigurationAssert.getTestURL(TEST_PLIST));
        checkPList(config);
    }

    /**
     * Tests whether a builder for a INI configuration can be created for a given file.
     */
    @Test
    void testINIBuilderFromFile() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<INIConfiguration> builder = configs.iniBuilder(ConfigurationAssert.getTestFile(TEST_INI));
        checkINI(builder.getConfiguration());
    }

    /**
     * Tests whether a builder for a INI configuration can be created for a given file path.
     */
    @Test
    void testINIBuilderFromPath() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<INIConfiguration> builder = configs.iniBuilder(absolutePath(TEST_INI));
        checkINI(builder.getConfiguration());
    }

    /**
     * Tests whether a builder for a INI configuration can be created for a given URL.
     */
    @Test
    void testINIBuilderFromURL() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<INIConfiguration> builder = configs.iniBuilder(ConfigurationAssert.getTestURL(TEST_INI));
        checkINI(builder.getConfiguration());
    }

    /**
     * Tests whether a INI configuration can be loaded from a file.
     */
    @Test
    void testINIFromFile() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final INIConfiguration config = configs.ini(ConfigurationAssert.getTestFile(TEST_INI));
        checkINI(config);
    }

    /**
     * Tests whether a INI configuration can be loaded from a file path.
     */
    @Test
    void testINIFromPath() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final INIConfiguration config = configs.ini(absolutePath(TEST_INI));
        checkINI(config);
    }

    /**
     * Tests whether a INI configuration can be loaded from a URL.
     */
    @Test
    void testINIFromURL() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final INIConfiguration config = configs.ini(ConfigurationAssert.getTestURL(TEST_INI));
        checkINI(config);
    }

    /**
     * Tests whether parameters can be passed in at construction time.
     */
    @Test
    void testInitWithParameters() {
        final Parameters params = new Parameters();
        final Configurations configs = new Configurations(params);
        assertSame(params, configs.getParameters());
    }

    /**
     * Tests whether a builder for a properties configuration can be created for a given file.
     */
    @Test
    void testPropertiesBuilderFromFile() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder = configs.propertiesBuilder(ConfigurationAssert.getTestFile(TEST_PROPERTIES));
        checkProperties(builder.getConfiguration());
    }

    /**
     * Tests whether a builder for a properties configuration can be created for a given file path.
     */
    @Test
    void testPropertiesBuilderFromPath() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder = configs.propertiesBuilder(absolutePath(TEST_PROPERTIES));
        checkProperties(builder.getConfiguration());
    }

    /**
     * Tests whether a builder for a properties configuration can be created for a given file path when an include is not
     * found.
     */
    @Test
    void testPropertiesBuilderFromPathIncludeNotFoundFail() {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder = configs.propertiesBuilder(absolutePath("include-not-found.properties"));
        assertThrows(ConfigurationException.class, builder::getConfiguration);
    }

    /**
     * Tests whether a builder for a properties configuration can be created for a given file path when an include is not
     * found.
     */
    @Test
    void testPropertiesBuilderFromPathIncludeNotFoundPass() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final String absPath = absolutePath("include-not-found.properties");
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builderFail = configs.propertiesBuilder(absPath);
        assertThrows(ConfigurationException.class, builderFail::getConfiguration);
        assertThrows(ConfigurationException.class, () -> configs.properties(absPath));
        // Expect success:
        // @formatter:off
            final Map<String, Object> map =
                    new Parameters().properties()
                            .setPath(absPath)
                            .setIncludeListener(PropertiesConfiguration.NOOP_INCLUDE_LISTENER)
                            .getParameters();
        // @formatter:on
        final BasicConfigurationBuilder<PropertiesConfiguration> builderOk = configs.propertiesBuilder(absPath).addParameters(map);
        assertEquals("valueA", builderOk.getConfiguration().getString("keyA"));
        // Expect success:
        // @formatter:off
            final BasicConfigurationBuilder<PropertiesConfiguration> builderOk2 = configs.propertiesBuilder(
                    new Parameters().properties()
                        .setPath(absPath)
                        .setIncludeListener(PropertiesConfiguration.NOOP_INCLUDE_LISTENER));
        // @formatter:on
        assertEquals("valueA", builderOk2.getConfiguration().getString("keyA"));
    }

    /**
     * Tests whether a builder for a properties configuration can be created for a given URL.
     */
    @Test
    void testPropertiesBuilderFromURL() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder = configs.propertiesBuilder(ConfigurationAssert.getTestURL(TEST_PROPERTIES));
        checkProperties(builder.getConfiguration());
    }

    /**
     * Tests whether a properties configuration can be loaded from a file.
     */
    @Test
    void testPropertiesFromFile() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final PropertiesConfiguration config = configs.properties(ConfigurationAssert.getTestFile(TEST_PROPERTIES));
        checkProperties(config);
    }

    /**
     * Tests whether a properties configuration can be loaded from a file path.
     */
    @Test
    void testPropertiesFromPath() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final PropertiesConfiguration config = configs.properties(absolutePath(TEST_PROPERTIES));
        checkProperties(config);
    }

    /**
     * Tests whether a properties configuration can be loaded from a URL.
     */
    @Test
    void testPropertiesFromURL() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final PropertiesConfiguration config = configs.properties(ConfigurationAssert.getTestURL(TEST_PROPERTIES));
        checkProperties(config);
    }

    /**
     * Tests whether a builder for a XML configuration can be created for a given file.
     */
    @Test
    void testXMLBuilderFromFile() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<XMLConfiguration> builder = configs.xmlBuilder(ConfigurationAssert.getTestFile(TEST_XML));
        checkXML(builder.getConfiguration());
    }

    /**
     * Tests whether a builder for a XML configuration can be created for a given file path.
     */
    @Test
    void testXMLBuilderFromPath() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<XMLConfiguration> builder = configs.xmlBuilder(absolutePath(TEST_XML));
        checkXML(builder.getConfiguration());
    }

    /**
     * Tests whether a builder for a XML configuration can be created for a given URL.
     */
    @Test
    void testXMLBuilderFromURL() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<XMLConfiguration> builder = configs.xmlBuilder(ConfigurationAssert.getTestURL(TEST_XML));
        checkXML(builder.getConfiguration());
    }

    /**
     * Tests whether a XML configuration can be loaded from a file.
     */
    @Test
    void testXMLFromFile() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final XMLConfiguration config = configs.xml(ConfigurationAssert.getTestFile(TEST_XML));
        checkXML(config);
    }

    /**
     * Tests whether a XML configuration can be loaded from a URL.
     */
    @Test
    void testXMLFromPath() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final XMLConfiguration config = configs.xml(absolutePath(TEST_XML));
        checkXML(config);
    }

    /**
     * Tests whether a XML configuration can be loaded from a URL.
     */
    @Test
    void testXMLFromURL() throws ConfigurationException {
        final Configurations configs = new Configurations();
        final XMLConfiguration config = configs.xml(ConfigurationAssert.getTestURL(TEST_XML));
        checkXML(config);
    }
}
