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

import static org.apache.commons.configuration2.TempDirUtils.newFile;
import static org.apache.commons.configuration2.TempDirUtils.newFolder;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.apache.commons.configuration2.io.URLConnectionOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test class for {@code FileBasedConfigurationBuilder}.
 */
public class TestFileBasedConfigurationBuilder {
    /** Constant for a test property name. */
    private static final String PROP = "testProperty";

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
        assertEquals(expValue, config.getInt(PROP));
    }

    /** A folder for temporary files. */
    @TempDir
    public File tempFolder;

    /**
     * Creates a test properties file with the given property value
     *
     * @param value the value for the test property
     * @return the File object pointing to the test file
     */
    private File createTestFile(final int value) {
        return assertDoesNotThrow(() -> {
            final File file = newFile(tempFolder);
            try (Writer out = new FileWriter(file)) {
                out.write(String.format("%s=%d", PROP, value));
            }
            return file;
        });
    }

    /**
     * Tests whether auto save mode works.
     */
    @Test
    public void testAutoSave() throws ConfigurationException {
        final File file = createTestFile(0);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class)
                        .configure(new FileBasedBuilderParametersImpl()
                                .setFile(file));
        assertFalse(builder.isAutoSave());
        builder.setAutoSave(true);
        assertTrue(builder.isAutoSave());
        builder.setAutoSave(true); // should have no effect
        final PropertiesConfiguration config = builder.getConfiguration();
        config.setProperty(PROP, 1);
        checkSavedConfig(file, 1);
    }

    /**
     * Tests whether auto save mode works with a properties configuration.
     * This is related to CONFIGURATION-646.
     */
    @Test
    public void testAutoSaveWithPropertiesConfiguration() throws ConfigurationException, IOException {
        final File file = newFile(tempFolder);
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
     * Tests that the auto save mechanism survives a reset of the builder's
     * configuration.
     */
    @Test
    public void testAutoSaveWithReset() throws ConfigurationException {
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
        assertNotSame(config1, config2);
        config2.setProperty(PROP, 1);
        config1.setProperty(PROP, 2);
        checkSavedConfig(file, 1);
    }

    /**
     * Tests whether the location can be changed after a configuration has been
     * created.
     */
    @Test
    public void testChangeLocationAfterCreation() throws ConfigurationException {
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
        assertEquals(2, config.getInt(PROP));
    }

    /**
     * Tests whether it is possible to permanently change the location after a
     * reset of parameters.
     */
    @Test
    public void testChangeLocationAfterReset() throws ConfigurationException {
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
        assertEquals(1, config.getInt(PROP));
        builder.getFileHandler().setFile(file2);
        builder.resetResult();
        config = builder.getConfiguration();
        assertEquals(2, config.getInt(PROP));
    }

    /**
     * Tests whether a configuration can be created and associated with a file that does
     * not yet exist. Later the configuration is saved to this file.
     */
    @Test
    public void testCreateConfigurationNonExistingFileAndThenSave() throws ConfigurationException {
        final File outFile = ConfigurationAssert.getOutFile("save.properties");
        final Parameters parameters = new Parameters();
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(
                PropertiesConfiguration.class, null, true).configure(parameters
                .properties().setFile(outFile));
        final Configuration config = builder.getConfiguration();
        config.setProperty(PROP, 1);
        builder.save();
        checkSavedConfig(outFile, 1);
        assertTrue(outFile.delete());
    }

    /**
     * Tests whether auto save mode can be disabled again.
     */
    @Test
    public void testDisableAutoSave() throws ConfigurationException {
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
     * Tests whether HomeDirectoryLocationStrategy can be properly initialized
     * and that it shouldn't throw {@code ConfigurationException} when
     * everything is correctly in place. Without the code fix for
     * <a href="https://issues.apache.org/jira/browse/CONFIGURATION-634">CONFIGURATION-634</a>,
     * this test will throw {@code ConfigurationException}
     * @throws IOException              Shouldn't happen
     * @throws ConfigurationException   Shouldn't happen
     */
    @Test
    public void testFileBasedConfigurationBuilderWithHomeDirectoryLocationStrategy() throws IOException, ConfigurationException {
        final String folderName = "test";
        final String fileName = "sample.properties";
        newFolder(folderName, tempFolder);
        newFile(folderName + File.separator + fileName, tempFolder);
        final FileBasedConfigurationBuilder<FileBasedConfiguration> homeDirConfigurationBuilder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class);
        final PropertiesBuilderParameters homeDirProperties =
                new Parameters().properties();
        final HomeDirectoryLocationStrategy strategy =
                new HomeDirectoryLocationStrategy(
                        tempFolder.getAbsolutePath(), true);
        final FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                homeDirConfigurationBuilder.configure(homeDirProperties
                        .setLocationStrategy(strategy).setBasePath(folderName)
                        .setListDelimiterHandler(
                                new DefaultListDelimiterHandler(','))
                        .setFileName(fileName));
        assertDoesNotThrow(builder::getConfiguration);
    }

    /**
     * Tests whether a configuration is loaded from file if a location is provided.
     */
    @Test
    public void testGetConfigurationLoadFromFile() throws ConfigurationException {
        final File file = createTestFile(1);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
            .configure(new FileBasedBuilderParametersImpl().setFile(file));
        final PropertiesConfiguration config = builder.getConfiguration();
        assertEquals(1, config.getInt(PROP));
        assertSame(config, builder.getFileHandler().getContent());
    }

    /**
     * Tests whether a configuration is loaded from a JAR file if a location is provided. CONFIGURATION-794: Unclosed file
     * handle when reading config from JAR file URL.
     */
    @Test
    public void testGetConfigurationLoadFromJarFile() throws ConfigurationException, IOException {
        final URL jarResourceUrl = getClass().getClassLoader().getResource("org/apache/commons/configuration2/test.jar");
        assertNotNull(jarResourceUrl);
        final Path testJar = Paths.get(tempFolder.getAbsolutePath(), "test.jar");
        try (InputStream inputStream = jarResourceUrl.openStream()) {
            Files.copy(inputStream, testJar);
        }
        final URL url = new URL("jar:" + testJar.toUri() + "!/configuration.properties");

        //@formatter:off
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
            new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                .configure(new FileBasedBuilderParametersImpl()
                .setURL(url, new URLConnectionOptions().setUseCaches(false)));
        //@formatter:off

// CONFIGURATION-794
// the next line causes:
//        java.lang.AssertionError: Unable to clean up temporary folder C:\Users\ggregory\AppData\Local\Temp\junit7789840233804508643
//        at org.junit.Assert.fail(Assert.java:89)
//        at org.junit.rules.TemporaryFolder.delete(TemporaryFolder.java:274)
//        at org.junit.rules.TemporaryFolder.after(TemporaryFolder.java:138)
//        at org.junit.rules.ExternalResource$1.evaluate(ExternalResource.java:59)
//        at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
//        at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
//        at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)
//        at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
//        at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:63)
//        at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)
//        at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)
//        at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)
//        at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)
//        at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
//        at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
//        at org.junit.runners.ParentRunner.run(ParentRunner.java:413)
//        at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:89)
//        at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:41)
//        at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:542)
//        at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:770)
//        at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:464)
//        at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:210)

        // builder contains the current FileHandler which loads the file.
        final PropertiesConfiguration config = builder.getConfiguration();
        assertEquals(1, config.getInt(PROP));
        assertSame(config, builder.getFileHandler().getContent());
    }

    /**
     * Tests whether a configuration can be created if no location is set.
     */
    @Test
    public void testGetConfigurationNoLocation() throws ConfigurationException {
        final Map<String, Object> params = new HashMap<>();
        params.put("throwExceptionOnMissing", Boolean.TRUE);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class, params);
        final PropertiesConfiguration conf = builder.getConfiguration();
        assertTrue(conf.isThrowExceptionOnMissing());
        assertTrue(conf.isEmpty());
    }

    /**
     * Tests whether a default encoding can be determined even if it was set for
     * an interface.
     */
    @Test
    public void testGetDefaultEncodingInterface() {
        final String encoding = "testEncoding";
        FileBasedConfigurationBuilder.setDefaultEncoding(Configuration.class, encoding);
        assertEquals(encoding, FileBasedConfigurationBuilder.getDefaultEncoding(XMLConfiguration.class));
        FileBasedConfigurationBuilder.setDefaultEncoding(Configuration.class, null);
        assertNull(FileBasedConfigurationBuilder.getDefaultEncoding(XMLConfiguration.class));
    }

    /**
     * Tests whether a default encoding for properties configurations is
     * defined.
     */
    @Test
    public void testGetDefaultEncodingProperties() {
        assertEquals(PropertiesConfiguration.DEFAULT_ENCODING, FileBasedConfigurationBuilder.getDefaultEncoding(PropertiesConfiguration.class));
    }

    /**
     * Tests whether a default encoding is find even if a sub class is queried.
     */
    @Test
    public void testGetDefaultEncodingSubClass() {
        final PropertiesConfiguration conf = new PropertiesConfiguration()
        {
        };
        assertEquals(PropertiesConfiguration.DEFAULT_ENCODING, FileBasedConfigurationBuilder.getDefaultEncoding(conf.getClass()));
    }

    /**
     * Tests whether a default encoding for XML properties configurations is
     * defined.
     */
    @Test
    public void testGetDefaultEncodingXmlProperties() {
        assertEquals(XMLPropertiesConfiguration.DEFAULT_ENCODING, FileBasedConfigurationBuilder.getDefaultEncoding(XMLPropertiesConfiguration.class));
    }

    /**
     * Tests whether the allowFailOnInit flag is correctly initialized.
     */
    @Test
    public void testInitAllowFailOnInitFlag() {
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class, null, true);
        assertTrue(builder.isAllowFailOnInit());
    }

    /**
     * Tests whether the default encoding can be overridden when initializing
     * the file handler.
     */
    @Test
    public void testInitFileHandlerOverrideDefaultEncoding() throws ConfigurationException {
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class);
        final FileHandler handler = new FileHandler();
        final String encoding = "testEncoding";
        handler.setEncoding(encoding);
        builder.initFileHandler(handler);
        assertEquals(encoding, handler.getEncoding());
    }

    /**
     * Tests whether the default encoding is set for the file handler if none is
     * specified.
     */
    @Test
    public void testInitFileHandlerSetDefaultEncoding() throws ConfigurationException {
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class);
        final FileHandler handler = new FileHandler();
        builder.initFileHandler(handler);
        assertEquals(PropertiesConfiguration.DEFAULT_ENCODING, handler.getEncoding());
    }

    /**
     * Tests whether the location in the FileHandler is fully defined. This
     * ensures that saving writes to the expected file.
     */
    @Test
    public void testLocationIsFullyDefined() throws ConfigurationException {
        final File file = createTestFile(1);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class)
                        .configure(new FileBasedBuilderParametersImpl()
                                .setFile(file));
        builder.getConfiguration();
        final FileLocator locator = builder.getFileHandler().getFileLocator();
        assertTrue(FileLocatorUtils.isFullyInitialized(locator));
    }

    /**
     * Tests that the location in the FileHandler remains the same if the
     * builder's result is reset.
     */
    @Test
    public void testLocationSurvivesResetResult() throws ConfigurationException {
        final File file = createTestFile(1);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class)
                        .configure(new FileBasedBuilderParametersImpl()
                                .setFile(file));
        final PropertiesConfiguration config = builder.getConfiguration();
        builder.resetResult();
        final PropertiesConfiguration config2 = builder.getConfiguration();
        assertNotSame(config, config2);
        assertEquals(1, config2.getInt(PROP));
    }

    /**
     * Tests whether a reset of the builder's initialization parameters also
     * resets the file location.
     */
    @Test
    public void testResetLocation() throws ConfigurationException {
        final File file = createTestFile(1);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class)
                        .configure(new FileBasedBuilderParametersImpl()
                                .setFile(file));
        builder.getConfiguration();
        builder.reset();
        final PropertiesConfiguration config = builder.getConfiguration();
        assertTrue(config.isEmpty());
        assertFalse(builder.getFileHandler().isLocationDefined());
    }

    /**
     * Tests whether the managed configuration can be saved.
     */
    @Test
    public void testSave() throws ConfigurationException {
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
    public void testSaveNewFile() throws ConfigurationException, IOException {
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class);
        final PropertiesConfiguration config = builder.getConfiguration();
        config.setProperty(PROP, 2);
        final File file = newFile(tempFolder);
        builder.getFileHandler().setFile(file);
        builder.save();
        checkSavedConfig(file, 2);
    }

    /**
     * Tries to set a default encoding for a null class.
     */
    @Test
    public void testSetDefaultEncodingNull() {
        assertThrows(IllegalArgumentException.class, () -> FileBasedConfigurationBuilder.setDefaultEncoding(null, StandardCharsets.UTF_8.name()));
    }

    /**
     * Tests whether a file handler can be accessed and manipulated even if no
     * file-based parameters are part of the initialization parameters.
     */
    @Test
    public void testSetLocationNoFileHandler() throws ConfigurationException {
        final File file = createTestFile(1);
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        PropertiesConfiguration.class);
        builder.getFileHandler().setFile(file);
        final PropertiesConfiguration config = builder.getConfiguration();
        assertFalse(config.isEmpty());
    }
}
