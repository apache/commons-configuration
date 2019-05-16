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
package org.apache.commons.configuration2.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.XMLPropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileLocatorUtils;
import org.apache.commons.configuration2.io.HomeDirectoryLocationStrategy;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test class for {@code FileBasedConfigurationBuilder}.
 *
 */
public class TestFileBasedConfigurationBuilder
{
    /** Constant for a test property name. */
    private static final String PROP = "testProperty";

    /** Helper object for managing temporary files. */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Creates a test properties file with the given property value
     *
     * @param value the value for the test property
     * @return the File object pointing to the test file
     */
    private File createTestFile(final int value)
    {
        Writer out = null;
        File file;
        try
        {
            file = folder.newFile();
            out = new FileWriter(file);
            out.write(String.format("%s=%d", PROP, value));
        }
        catch (final IOException ioex)
        {
            fail("Could not create test file: " + ioex);
            return null; // cannot happen
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (final IOException ioex)
                {
                    // ignore
                }
            }
        }
        return file;
    }

    /**
     * Tests whether a configuration can be created if no location is set.
     */
    @Test
    public void testGetConfigurationNoLocation() throws ConfigurationException
    {
        final Map<String, Object> params = new HashMap<>();
        params.put("throwExceptionOnMissing", Boolean.TRUE);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class, params);
        final PropertiesConfiguration conf = builder.getConfiguration();
        assertTrue("Property not set", conf.isThrowExceptionOnMissing());
        assertTrue("Not empty", conf.isEmpty());
    }

    /**
     * Tests whether a configuration is loaded from file if a location is
     * provided.
     */
    @Test
    public void testGetConfigurationLoadFromFile()
            throws ConfigurationException
    {
        final File file = createTestFile(1);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class)
                        .configure(new FileBasedBuilderParametersImpl()
                                .setFile(file));
        final PropertiesConfiguration config = builder.getConfiguration();
        assertEquals("Not read from file", 1, config.getInt(PROP));
        assertSame("FileHandler not initialized", config, builder
                .getFileHandler().getContent());
    }

    /**
     * Tests that the location in the FileHandler remains the same if the
     * builder's result is reset.
     */
    @Test
    public void testLocationSurvivesResetResult() throws ConfigurationException
    {
        final File file = createTestFile(1);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class)
                        .configure(new FileBasedBuilderParametersImpl()
                                .setFile(file));
        final PropertiesConfiguration config = builder.getConfiguration();
        builder.resetResult();
        final PropertiesConfiguration config2 = builder.getConfiguration();
        assertNotSame("Same configuration", config, config2);
        assertEquals("Not read from file", 1, config2.getInt(PROP));
    }

    /**
     * Tests whether the location in the FileHandler is fully defined. This
     * ensures that saving writes to the expected file.
     */
    @Test
    public void testLocationIsFullyDefined() throws ConfigurationException
    {
        final File file = createTestFile(1);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class)
                        .configure(new FileBasedBuilderParametersImpl()
                                .setFile(file));
        builder.getConfiguration();
        final FileLocator locator = builder.getFileHandler().getFileLocator();
        assertTrue("Not fully defined: " + locator,
                FileLocatorUtils.isFullyInitialized(locator));
    }

    /**
     * Tests whether the location can be changed after a configuration has been
     * created.
     */
    @Test
    public void testChangeLocationAfterCreation() throws ConfigurationException
    {
        final File file1 = createTestFile(1);
        final File file2 = createTestFile(2);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class)
                        .configure(new FileBasedBuilderParametersImpl()
                                .setFile(file1));
        builder.getConfiguration();
        builder.getFileHandler().setFile(file2);
        builder.resetResult();
        final PropertiesConfiguration config = builder.getConfiguration();
        assertEquals("Not read from file 2", 2, config.getInt(PROP));
    }

    /**
     * Tests whether a reset of the builder's initialization parameters also
     * resets the file location.
     */
    @Test
    public void testResetLocation() throws ConfigurationException
    {
        final File file = createTestFile(1);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class)
                        .configure(new FileBasedBuilderParametersImpl()
                                .setFile(file));
        builder.getConfiguration();
        builder.reset();
        final PropertiesConfiguration config = builder.getConfiguration();
        assertTrue("Configuration was read from file", config.isEmpty());
        assertFalse("FileHandler has location", builder.getFileHandler()
                .isLocationDefined());
    }

    /**
     * Tests whether it is possible to permanently change the location after a
     * reset of parameters.
     */
    @Test
    public void testChangeLocationAfterReset() throws ConfigurationException
    {
        final File file1 = createTestFile(1);
        final File file2 = createTestFile(2);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class)
                        .configure(new FileBasedBuilderParametersImpl()
                                .setFile(file1));
        builder.getConfiguration();
        builder.getFileHandler().setFile(file2);
        builder.reset();
        builder.configure(new FileBasedBuilderParametersImpl().setFile(file1));
        PropertiesConfiguration config = builder.getConfiguration();
        assertEquals("Not read from file 1", 1, config.getInt(PROP));
        builder.getFileHandler().setFile(file2);
        builder.resetResult();
        config = builder.getConfiguration();
        assertEquals("Not read from file 2", 2, config.getInt(PROP));
    }

    /**
     * Tests whether the allowFailOnInit flag is correctly initialized.
     */
    @Test
    public void testInitAllowFailOnInitFlag()
    {
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class, null, true);
        assertTrue("Flag not set", builder.isAllowFailOnInit());
    }

    /**
     * Tests whether a file handler can be accessed and manipulated even if no
     * file-based parameters are part of the initialization parameters.
     */
    @Test
    public void testSetLocationNoFileHandler() throws ConfigurationException
    {
        final File file = createTestFile(1);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class);
        builder.getFileHandler().setFile(file);
        final PropertiesConfiguration config = builder.getConfiguration();
        assertFalse("No data was loaded", config.isEmpty());
    }

    /**
     * Checks whether a test configuration was saved successfully.
     *
     * @param file the file to which the configuration was saved
     * @param expValue the expected value of the test property
     * @throws ConfigurationException if an error occurs
     */
    private static void checkSavedConfig(final File file, final int expValue)
            throws ConfigurationException
    {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(config);
        handler.load(file);
        assertEquals("Configuration was not saved", expValue,
                config.getInt(PROP));
    }

    /**
     * Tests whether the managed configuration can be saved.
     */
    @Test
    public void testSave() throws ConfigurationException
    {
        final File file = createTestFile(1);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class)
                        .configure(new FileBasedBuilderParametersImpl()
                                .setFile(file));
        final PropertiesConfiguration config = builder.getConfiguration();
        config.setProperty(PROP, 5);
        builder.save();
        checkSavedConfig(file, 5);
    }

    /**
     * Tests whether a new configuration can be saved to a file.
     */
    @Test
    public void testSaveNewFile() throws ConfigurationException, IOException
    {
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class);
        final PropertiesConfiguration config = builder.getConfiguration();
        config.setProperty(PROP, 2);
        final File file = folder.newFile();
        builder.getFileHandler().setFile(file);
        builder.save();
        checkSavedConfig(file, 2);
    }

    /**
     * Tests whether a configuration can be created and associated with a file that does
     * not yet exist. Later the configuration is saved to this file.
     */
    @Test
    public void testCreateConfigurationNonExistingFileAndThenSave()
            throws ConfigurationException {
        final File outFile = ConfigurationAssert.getOutFile("save.properties");
        final Parameters parameters = new Parameters();
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(
                PropertiesConfiguration.class, null, true).configure(parameters
                .properties().setFile(outFile));
        final Configuration config = builder.getConfiguration();
        config.setProperty(PROP, 1);
        builder.save();
        checkSavedConfig(outFile, 1);
        assertTrue("Could not remove test file", outFile.delete());
    }

    /**
     * Tests whether auto save mode works.
     */
    @Test
    public void testAutoSave() throws ConfigurationException
    {
        final File file = createTestFile(0);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class)
                        .configure(new FileBasedBuilderParametersImpl()
                                .setFile(file));
        assertFalse("Wrong auto save flag", builder.isAutoSave());
        builder.setAutoSave(true);
        assertTrue("Auto save not enabled", builder.isAutoSave());
        builder.setAutoSave(true); // should have no effect
        final PropertiesConfiguration config = builder.getConfiguration();
        config.setProperty(PROP, 1);
        checkSavedConfig(file, 1);
    }

    /**
     * Tests that the auto save mechanism survives a reset of the builder's
     * configuration.
     */
    @Test
    public void testAutoSaveWithReset() throws ConfigurationException
    {
        final File file = createTestFile(0);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class)
                        .configure(new FileBasedBuilderParametersImpl()
                                .setFile(file));
        final PropertiesConfiguration config1 = builder.getConfiguration();
        builder.setAutoSave(true);
        builder.resetResult();
        final PropertiesConfiguration config2 = builder.getConfiguration();
        assertNotSame("No new configuration created", config1, config2);
        config2.setProperty(PROP, 1);
        config1.setProperty(PROP, 2);
        checkSavedConfig(file, 1);
    }

    /**
     * Tests whether auto save mode can be disabled again.
     */
    @Test
    public void testDisableAutoSave() throws ConfigurationException
    {
        final File file = createTestFile(0);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class)
                        .configure(new FileBasedBuilderParametersImpl()
                                .setFile(file));
        final PropertiesConfiguration config = builder.getConfiguration();
        builder.setAutoSave(true);
        config.setProperty(PROP, 1);
        builder.setAutoSave(false);
        config.setProperty(PROP, 2);
        builder.setAutoSave(false); // should have no effect
        checkSavedConfig(file, 1);
    }

    /**
     * Tests whether auto save mode works with a properties configuration.
     * This is related to CONFIGURATION-646.
     */
    @Test
    public void testAutoSaveWithPropertiesConfiguration() throws ConfigurationException,
            IOException
    {
        final File file = folder.newFile();
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class)
                        .configure(new FileBasedBuilderParametersImpl()
                                .setFile(file));
        builder.setAutoSave(true);
        final PropertiesConfiguration config = builder.getConfiguration();
        config.setProperty(PROP, 1);
        checkSavedConfig(file, 1);
    }

    /**
     * Tries to set a default encoding for a null class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefaultEncodingNull()
    {
        FileBasedConfigurationBuilder.setDefaultEncoding(null, "UTF-8");
    }

    /**
     * Tests whether a default encoding for properties configurations is
     * defined.
     */
    @Test
    public void testGetDefaultEncodingProperties()
    {
        assertEquals("Wrong default encoding",
                PropertiesConfiguration.DEFAULT_ENCODING,
                FileBasedConfigurationBuilder
                        .getDefaultEncoding(PropertiesConfiguration.class));
    }

    /**
     * Tests whether a default encoding for XML properties configurations is
     * defined.
     */
    @Test
    public void testGetDefaultEncodingXmlProperties()
    {
        assertEquals("Wrong default encoding",
                XMLPropertiesConfiguration.DEFAULT_ENCODING,
                FileBasedConfigurationBuilder
                        .getDefaultEncoding(XMLPropertiesConfiguration.class));
    }

    /**
     * Tests whether a default encoding is find even if a sub class is queried.
     */
    @Test
    public void testGetDefaultEncodingSubClass()
    {
        final PropertiesConfiguration conf = new PropertiesConfiguration()
        {
        };
        assertEquals("Wrong default encodng",
                PropertiesConfiguration.DEFAULT_ENCODING,
                FileBasedConfigurationBuilder.getDefaultEncoding(conf
                        .getClass()));
    }

    /**
     * Tests whether a default encoding can be determined even if it was set for
     * an interface.
     */
    @Test
    public void testGetDefaultEncodingInterface()
    {
        final String encoding = "testEncoding";
        FileBasedConfigurationBuilder.setDefaultEncoding(Configuration.class,
                encoding);
        assertEquals("Wrong default encoding", encoding,
                FileBasedConfigurationBuilder
                        .getDefaultEncoding(XMLConfiguration.class));
        FileBasedConfigurationBuilder.setDefaultEncoding(Configuration.class,
                null);
        assertNull("Default encoding not removed",
                FileBasedConfigurationBuilder
                        .getDefaultEncoding(XMLConfiguration.class));
    }

    /**
     * Tests whether the default encoding is set for the file handler if none is
     * specified.
     */
    @Test
    public void testInitFileHandlerSetDefaultEncoding()
            throws ConfigurationException
    {
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class);
        final FileHandler handler = new FileHandler();
        builder.initFileHandler(handler);
        assertEquals("Wrong encoding",
                PropertiesConfiguration.DEFAULT_ENCODING, handler.getEncoding());
    }

    /**
     * Tests whether the default encoding can be overridden when initializing
     * the file handler.
     */
    @Test
    public void testInitFileHandlerOverrideDefaultEncoding()
            throws ConfigurationException
    {
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class);
        final FileHandler handler = new FileHandler();
        final String encoding = "testEncoding";
        handler.setEncoding(encoding);
        builder.initFileHandler(handler);
        assertEquals("Encoding was changed", encoding, handler.getEncoding());
    }

    /**
     * Tests whether HomeDirectoryLocationStrategy can be properly initialized
     * and that it shouldn't throw <code>ConfigurationException</code> when
     * everything is correctly in place. Without the code fix for
     * <a href="https://issues.apache.org/jira/browse/CONFIGURATION-634">CONFIGURATION-634</a>,
     * this test will throw <code>ConfigurationException</code>
     * @throws IOException              Shouldn't happen
     * @throws ConfigurationException   Shouldn't happen
     */
    @Test
    public void testFileBasedConfigurationBuilderWithHomeDirectoryLocationStrategy()
            throws IOException, ConfigurationException
    {
        final String folderName = "test";
        final String fileName = "sample.properties";
        folder.newFolder(folderName);
        folder.newFile(folderName + File.separatorChar + fileName);
        final FileBasedConfigurationBuilder<FileBasedConfiguration> homeDirConfigurationBuilder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class);
        final PropertiesBuilderParameters homeDirProperties =
                new Parameters().properties();
        final HomeDirectoryLocationStrategy strategy =
                new HomeDirectoryLocationStrategy(
                        folder.getRoot().getAbsolutePath(), true);
        final FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                homeDirConfigurationBuilder.configure(homeDirProperties
                        .setLocationStrategy(strategy).setBasePath(folderName)
                        .setListDelimiterHandler(
                                new DefaultListDelimiterHandler(','))
                        .setFileName(fileName));
        builder.getConfiguration();
    }
}
