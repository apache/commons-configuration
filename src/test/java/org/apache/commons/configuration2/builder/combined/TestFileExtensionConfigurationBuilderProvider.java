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
package org.apache.commons.configuration2.builder.combined;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLPropertiesConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test class for {@code FileExtensionConfigurationBuilderProvider}.
 */
public class TestFileExtensionConfigurationBuilderProvider {
    /** Constant for the name of the default configuration class. */
    private static final String DEF_CLASS = PropertiesConfiguration.class.getName();

    /** Constant for the name of the matching class. */
    private static final String MATCH_CLASS = XMLPropertiesConfiguration.class.getName();

    /** The test file extension. */
    private static final String EXT = "xml";

    /**
     * Creates a test instance with default settings.
     *
     * @return the test object
     */
    private static FileExtensionConfigurationBuilderProvider setUpProvider() {
        return new FileExtensionConfigurationBuilderProvider(BasicConfigurationBuilder.class.getName(), null, MATCH_CLASS, DEF_CLASS, EXT, null);
    }

    /**
     * Creates a mock for the configuration declaration.
     *
     * @return the mock object
     */
    private ConfigurationDeclaration setUpDecl() {
        return Mockito.mock(ConfigurationDeclaration.class);
    }

    /**
     * Tests whether the correct configuration class is selected if the file extension matches.
     */
    @Test
    public void testDetermineConfigurationClassExtensionMatch() throws ConfigurationException {
        final ConfigurationDeclaration decl = setUpDecl();
        final BuilderParameters params = new FileBasedBuilderParametersImpl().setPath("C:\\Test\\someTestConfiguration." + EXT);
        final FileExtensionConfigurationBuilderProvider provider = setUpProvider();
        assertEquals(MATCH_CLASS, provider.determineConfigurationClass(decl, Collections.singleton(params)));
    }

    /**
     * Tests whether the correct configuration class is selected if the file extension does not match.
     */
    @Test
    public void testDetermineConfigurationClassExtensionNoMatch() throws ConfigurationException {
        final ConfigurationDeclaration decl = setUpDecl();
        final BuilderParameters params = new FileBasedBuilderParametersImpl().setPath("C:\\Test\\someTestConfiguration.properties");
        final FileExtensionConfigurationBuilderProvider provider = setUpProvider();
        assertEquals(DEF_CLASS, provider.determineConfigurationClass(decl, Collections.singleton(params)));
    }

    /**
     * Tests that matches of file extensions are case insensitive.
     */
    @Test
    public void testDetermineConfigurationClassMatchCase() throws ConfigurationException {
        final ConfigurationDeclaration decl = setUpDecl();
        final BuilderParameters params = new FileBasedBuilderParametersImpl().setPath("C:\\Test\\someTestConfiguration." + EXT.toUpperCase(Locale.ENGLISH));
        final FileExtensionConfigurationBuilderProvider provider = setUpProvider();
        assertEquals(MATCH_CLASS, provider.determineConfigurationClass(decl, Collections.singleton(params)));
    }

    /**
     * Tests whether the correct configuration class is selected if the file name does not have an extension.
     */
    @Test
    public void testDetermineConfigurationClassNoExtension() throws ConfigurationException {
        final ConfigurationDeclaration decl = setUpDecl();
        final BuilderParameters params = new FileBasedBuilderParametersImpl().setPath("C:\\Test\\someTestConfiguration");
        final FileExtensionConfigurationBuilderProvider provider = setUpProvider();
        assertEquals(DEF_CLASS, provider.determineConfigurationClass(decl, Collections.singleton(params)));
    }

    /**
     * Tests whether the correct configuration class is selected if no file-based parameters are provided.
     */
    @Test
    public void testDetermineConfigurationClassNoParams() throws ConfigurationException {
        final ConfigurationDeclaration decl = setUpDecl();
        final FileExtensionConfigurationBuilderProvider provider = setUpProvider();
        assertEquals(DEF_CLASS, provider.determineConfigurationClass(decl, new ArrayList<>()));
    }

    /**
     * Tests whether the correct configuration class is selected if no file name is set.
     */
    @Test
    public void testDeterminieConfigurationClassNoFileName() throws ConfigurationException {
        final ConfigurationDeclaration decl = setUpDecl();
        final BuilderParameters params = new FileBasedBuilderParametersImpl();
        final FileExtensionConfigurationBuilderProvider provider = setUpProvider();
        assertEquals(DEF_CLASS, provider.determineConfigurationClass(decl, Collections.singleton(params)));
    }

    /**
     * Tries to create an instance without the default configuration class.
     */
    @Test
    public void testInitNoDefaultConfigClass() {
        final String builderClass = BasicConfigurationBuilder.class.getName();
        assertThrows(IllegalArgumentException.class,
                () -> new FileExtensionConfigurationBuilderProvider(builderClass, null, MATCH_CLASS, null, EXT, null));
    }

    /**
     * Tries to create an instance without a file extension.
     */
    @Test
    public void testInitNoExt() {
        final String builderClass = BasicConfigurationBuilder.class.getName();
        assertThrows(IllegalArgumentException.class,
                () -> new FileExtensionConfigurationBuilderProvider(builderClass, null, MATCH_CLASS, DEF_CLASS, null, null));
    }

    /**
     * Tries to create an instance without the matching configuration class.
     */
    @Test
    public void testInitNoMatchingConfigClass() {
        final String builderClass = BasicConfigurationBuilder.class.getName();
        assertThrows(IllegalArgumentException.class,
                () -> new FileExtensionConfigurationBuilderProvider(builderClass, null, null, DEF_CLASS, EXT, null));
    }

    /**
     * Tests whether the super class is correctly initialized.
     */
    @Test
    public void testInitSuper() {
        final FileExtensionConfigurationBuilderProvider provider = new FileExtensionConfigurationBuilderProvider(BasicConfigurationBuilder.class.getName(),
            ReloadingFileBasedConfigurationBuilder.class.getName(), MATCH_CLASS, DEF_CLASS, EXT, null);
        assertEquals(BasicConfigurationBuilder.class.getName(), provider.getBuilderClass());
        assertEquals(ReloadingFileBasedConfigurationBuilder.class.getName(), provider.getReloadingBuilderClass());
        assertEquals(DEF_CLASS, provider.getConfigurationClass());
    }
}
