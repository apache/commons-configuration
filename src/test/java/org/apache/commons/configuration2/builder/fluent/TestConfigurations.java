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
package org.apache.commons.configuration2.builder.fluent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.combined.CombinedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.plist.PropertyListConfiguration;
import org.junit.Test;

/**
 * Test class for {@code Configurations}.
 *
 */
public class TestConfigurations
{
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
     * Generates a full path for the test file with the given name.
     *
     * @param name the name of the test file
     * @return the full path to this file
     */
    private static String filePath(final String name)
    {
        return ConfigurationAssert.getTestFile(name).getAbsolutePath();
    }

    /**
     * Tests whether a default {@code Parameters} instance is created if
     * necessary.
     */
    @Test
    public void testDefaultParameters()
    {
        final Configurations configs = new Configurations();
        assertNotNull("No parameters", configs.getParameters());
    }

    /**
     * Tests whether parameters can be passed in at construction time.
     */
    @Test
    public void testInitWithParameters()
    {
        final Parameters params = new Parameters();
        final Configurations configs = new Configurations(params);
        assertSame("Wrong parameters", params, configs.getParameters());
    }

    /**
     * Tests whether a builder for a file-based configuration can be created if
     * an input File is specified.
     */
    @Test
    public void testFileBasedBuilderWithFile()
    {
        final Configurations configs = new Configurations();
        final File file = ConfigurationAssert.getTestFile(TEST_PROPERTIES);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                configs.fileBasedBuilder(PropertiesConfiguration.class, file);
        assertEquals("Wrong file", file.toURI(), builder.getFileHandler()
                .getFile().toURI());
    }

    /**
     * Tests whether a builder for a file-based configuration can be created if
     * a URL is specified.
     */
    @Test
    public void testFileBasedBuilderWithURL()
    {
        final Configurations configs = new Configurations();
        final URL url = ConfigurationAssert.getTestURL("test.properties");
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                configs.fileBasedBuilder(PropertiesConfiguration.class, url);
        assertEquals("Wrong URL", url, builder.getFileHandler().getURL());
    }

    /**
     * Tests whether a builder for a file-based configuration can be created if
     * a file name is specified.
     */
    @Test
    public void testFileBasedBuilderWithPath()
    {
        final Configurations configs = new Configurations();
        final String filePath = filePath(TEST_PROPERTIES);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                configs.fileBasedBuilder(PropertiesConfiguration.class,
                        filePath);
        assertEquals("Wrong path", filePath, builder.getFileHandler()
                .getFileName());
    }

    /**
     * Checks whether a property list configuration was correctly loaded.
     *
     * @param config the configuration instance to be checked.
     */
    private static void checkPList(final Configuration config)
    {
        assertEquals("string1", config.getProperty("simple-string"));
    }

    /**
     * Tests whether a file-based configuration can be loaded from a file.
     */
    @Test
    public void testFileBasedFile() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final PropertyListConfiguration config =
                configs.fileBased(PropertyListConfiguration.class,
                        ConfigurationAssert.getTestFile(TEST_PLIST));
        checkPList(config);
    }

    /**
     * Tests whether a file-based configuration can be loaded from a URL.
     */
    @Test
    public void testFileBasedURL() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final PropertyListConfiguration config =
                configs.fileBased(PropertyListConfiguration.class,
                        ConfigurationAssert.getTestURL(TEST_PLIST));
        checkPList(config);
    }

    /**
     * Tests whether a file-based configuration can be loaded from a file path.
     */
    @Test
    public void testFileBasedPath() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final PropertyListConfiguration config =
                configs.fileBased(PropertyListConfiguration.class,
                        filePath(TEST_PLIST));
        checkPList(config);
    }

    /**
     * Checks whether a test properties configuration was correctly loaded.
     *
     * @param config the configuration instance to be checked.
     */
    private static void checkProperties(final Configuration config)
    {
        assertEquals("true", config.getString("configuration.loaded"));
    }

    /**
     * Tests whether a builder for a properties configuration can be created for
     * a given file.
     */
    @Test
    public void testPropertiesBuilderFromFile() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                configs.propertiesBuilder(ConfigurationAssert
                        .getTestFile(TEST_PROPERTIES));
        checkProperties(builder.getConfiguration());
    }

    /**
     * Tests whether a properties configuration can be loaded from a file.
     */
    @Test
    public void testPropertiesFromFile() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final PropertiesConfiguration config =
                configs.properties(ConfigurationAssert
                        .getTestFile(TEST_PROPERTIES));
        checkProperties(config);
    }

    /**
     * Tests whether a builder for a properties configuration can be created for
     * a given URL.
     */
    @Test
    public void testPropertiesBuilderFromURL() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                configs.propertiesBuilder(ConfigurationAssert
                        .getTestURL(TEST_PROPERTIES));
        checkProperties(builder.getConfiguration());
    }

    /**
     * Tests whether a properties configuration can be loaded from a URL.
     */
    @Test
    public void testPropertiesFromURL() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final PropertiesConfiguration config =
                configs.properties(ConfigurationAssert
                        .getTestURL(TEST_PROPERTIES));
        checkProperties(config);
    }

    /**
     * Tests whether a builder for a properties configuration can be created for
     * a given file path.
     */
    @Test
    public void testPropertiesBuilderFromPath() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                configs.propertiesBuilder(filePath(TEST_PROPERTIES));
        checkProperties(builder.getConfiguration());
    }

    /**
     * Tests whether a properties configuration can be loaded from a file path.
     */
    @Test
    public void testPropertiesFromPath() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final PropertiesConfiguration config =
                configs.properties(filePath(TEST_PROPERTIES));
        checkProperties(config);
    }

    /**
     * Checks whether a test XML configuration was correctly loaded.
     *
     * @param config the configuration instance to be checked.
     */
    private static void checkXML(final Configuration config)
    {
        assertEquals("value", config.getProperty("element"));
    }

    /**
     * Tests whether a builder for a XML configuration can be created for a
     * given file.
     */
    @Test
    public void testXMLBuilderFromFile() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<XMLConfiguration> builder =
                configs.xmlBuilder(ConfigurationAssert.getTestFile(TEST_XML));
        checkXML(builder.getConfiguration());
    }

    /**
     * Tests whether a XML configuration can be loaded from a file.
     */
    @Test
    public void testXMLFromFile() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final XMLConfiguration config =
                configs.xml(ConfigurationAssert.getTestFile(TEST_XML));
        checkXML(config);
    }

    /**
     * Tests whether a builder for a XML configuration can be created for a
     * given URL.
     */
    @Test
    public void testXMLBuilderFromURL() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<XMLConfiguration> builder =
                configs.xmlBuilder(ConfigurationAssert.getTestURL(TEST_XML));
        checkXML(builder.getConfiguration());
    }

    /**
     * Tests whether a XML configuration can be loaded from a URL.
     */
    @Test
    public void testXMLFromURL() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final XMLConfiguration config =
                configs.xml(ConfigurationAssert.getTestURL(TEST_XML));
        checkXML(config);
    }

    /**
     * Tests whether a builder for a XML configuration can be created for a
     * given file path.
     */
    @Test
    public void testXMLBuilderFromPath() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<XMLConfiguration> builder =
                configs.xmlBuilder(filePath(TEST_XML));
        checkXML(builder.getConfiguration());
    }

    /**
     * Tests whether a XML configuration can be loaded from a URL.
     */
    @Test
    public void testXMLFromPath() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final XMLConfiguration config = configs.xml(filePath(TEST_XML));
        checkXML(config);
    }

    /**
     * Checks whether a test INI configuration was correctly loaded.
     *
     * @param config the configuration instance to be checked.
     */
    private static void checkINI(final INIConfiguration config)
    {
        assertEquals("yes", config.getProperty("testini.loaded"));
    }

    /**
     * Tests whether a builder for a INI configuration can be created for a
     * given file.
     */
    @Test
    public void testINIBuilderFromFile() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<INIConfiguration> builder =
                configs.iniBuilder(ConfigurationAssert.getTestFile(TEST_INI));
        checkINI(builder.getConfiguration());
    }

    /**
     * Tests whether a INI configuration can be loaded from a file.
     */
    @Test
    public void testINIFromFile() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final INIConfiguration config =
                configs.ini(ConfigurationAssert.getTestFile(TEST_INI));
        checkINI(config);
    }

    /**
     * Tests whether a builder for a INI configuration can be created for a
     * given URL.
     */
    @Test
    public void testINIBuilderFromURL() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<INIConfiguration> builder =
                configs.iniBuilder(ConfigurationAssert.getTestURL(TEST_INI));
        checkINI(builder.getConfiguration());
    }

    /**
     * Tests whether a INI configuration can be loaded from a URL.
     */
    @Test
    public void testINIFromURL() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final INIConfiguration config =
                configs.ini(ConfigurationAssert.getTestURL(TEST_INI));
        checkINI(config);
    }

    /**
     * Tests whether a builder for a INI configuration can be created for a
     * given file path.
     */
    @Test
    public void testINIBuilderFromPath() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final FileBasedConfigurationBuilder<INIConfiguration> builder =
                configs.iniBuilder(filePath(TEST_INI));
        checkINI(builder.getConfiguration());
    }

    /**
     * Tests whether a INI configuration can be loaded from a file path.
     */
    @Test
    public void testINIFromPath() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final INIConfiguration config = configs.ini(filePath(TEST_INI));
        checkINI(config);
    }

    /**
     * Checks whether a combined configuration was successfully loaded.
     *
     * @param config the configuration instance to be checked.
     */
    private static void checkCombined(final Configuration config)
    {
        checkProperties(config);
        checkXML(config);
    }

    /**
     * Tests whether a combined configuration builder can be constructed for a
     * file.
     */
    @Test
    public void testCombinedBuilderFromFile() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final CombinedConfigurationBuilder builder =
                configs.combinedBuilder(ConfigurationAssert
                        .getTestFile(TEST_COMBINED));
        checkCombined(builder.getConfiguration());
    }

    /**
     * Tests whether a combined configuration can be loaded from a file.
     */
    @Test
    public void testCombinedFromFile() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final CombinedConfiguration config =
                configs.combined(ConfigurationAssert.getTestFile(TEST_COMBINED));
        checkCombined(config);
    }

    /**
     * Tests whether a combined configuration builder can be constructed for a
     * URL.
     */
    @Test
    public void testCombinedBuilderFromURL() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final CombinedConfigurationBuilder builder =
                configs.combinedBuilder(ConfigurationAssert
                        .getTestURL(TEST_COMBINED));
        checkCombined(builder.getConfiguration());
    }

    /**
     * Tests whether a combined configuration can be loaded from a URL.
     */
    @Test
    public void testCombinedFromURL() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final CombinedConfiguration config =
                configs.combined(ConfigurationAssert.getTestURL(TEST_COMBINED));
        checkCombined(config);
    }

    /**
     * Tests whether a combined configuration builder can be constructed for a
     * file path.
     */
    @Test
    public void testCombinedBuilderFromPath() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final CombinedConfigurationBuilder builder =
                configs.combinedBuilder(filePath(TEST_COMBINED));
        checkCombined(builder.getConfiguration());
    }

    /**
     * Tests whether a combined configuration can be loaded from a file path.
     */
    @Test
    public void testCombinedFromPath() throws ConfigurationException
    {
        final Configurations configs = new Configurations();
        final CombinedConfiguration config =
                configs.combined(filePath(TEST_COMBINED));
        checkCombined(config);
    }
}
