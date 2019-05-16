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

import static org.junit.Assert.assertEquals;

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
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test class for {@code FileExtensionConfigurationBuilderProvider}.
 *
 */
public class TestFileExtensionConfigurationBuilderProvider
{
    /** Constant for the name of the default configuration class. */
    private static final String DEF_CLASS = PropertiesConfiguration.class
            .getName();

    /** Constant for the name of the matching class. */
    private static final String MATCH_CLASS = XMLPropertiesConfiguration.class
            .getName();

    /** The test file extension. */
    private static final String EXT = "xml";

    /**
     * Creates a test instance with default settings.
     *
     * @return the test object
     */
    private static FileExtensionConfigurationBuilderProvider setUpProvider()
    {
        final FileExtensionConfigurationBuilderProvider provider =
                new FileExtensionConfigurationBuilderProvider(
                        BasicConfigurationBuilder.class.getName(), null,
                        MATCH_CLASS, DEF_CLASS, EXT, null);
        return provider;
    }

    /**
     * Creates a mock for the configuration declaration.
     *
     * @return the mock object
     */
    private ConfigurationDeclaration setUpDecl()
    {
        final ConfigurationDeclaration decl =
                EasyMock.createMock(ConfigurationDeclaration.class);
        EasyMock.replay(decl);
        return decl;
    }

    /**
     * Tries to create an instance without the matching configuration class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoMatchingConfigClass()
    {
        new FileExtensionConfigurationBuilderProvider(
                BasicConfigurationBuilder.class.getName(), null, null,
                DEF_CLASS, EXT, null);
    }

    /**
     * Tries to create an instance without the default configuration class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoDefaultConfigClass()
    {
        new FileExtensionConfigurationBuilderProvider(
                BasicConfigurationBuilder.class.getName(), null, MATCH_CLASS,
                null, EXT, null);
    }

    /**
     * Tries to create an instance without a file extension.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoExt()
    {
        new FileExtensionConfigurationBuilderProvider(
                BasicConfigurationBuilder.class.getName(), null, MATCH_CLASS,
                DEF_CLASS, null, null);
    }

    /**
     * Tests whether the super class is correctly initialized.
     */
    @Test
    public void testInitSuper()
    {
        final FileExtensionConfigurationBuilderProvider provider =
                new FileExtensionConfigurationBuilderProvider(
                        BasicConfigurationBuilder.class.getName(),
                        ReloadingFileBasedConfigurationBuilder.class.getName(),
                        MATCH_CLASS, DEF_CLASS, EXT, null);
        assertEquals("Wrong builder class",
                BasicConfigurationBuilder.class.getName(),
                provider.getBuilderClass());
        assertEquals("Wrong reloading builder class",
                ReloadingFileBasedConfigurationBuilder.class.getName(),
                provider.getReloadingBuilderClass());
        assertEquals("Wrong configuration class", DEF_CLASS,
                provider.getConfigurationClass());
    }

    /**
     * Tests whether the correct configuration class is selected if no
     * file-based parameters are provided.
     */
    @Test
    public void testDetermineConfigurationClassNoParams()
            throws ConfigurationException
    {
        final ConfigurationDeclaration decl = setUpDecl();
        final FileExtensionConfigurationBuilderProvider provider = setUpProvider();
        assertEquals("Wrong class", DEF_CLASS,
                provider.determineConfigurationClass(decl,
                        new ArrayList<BuilderParameters>()));
    }

    /**
     * Tests whether the correct configuration class is selected if the file
     * name does not have an extension.
     */
    @Test
    public void testDetermineConfigurationClassNoExtension()
            throws ConfigurationException
    {
        final ConfigurationDeclaration decl = setUpDecl();
        final BuilderParameters params =
                new FileBasedBuilderParametersImpl()
                        .setPath("C:\\Test\\someTestConfiguration");
        final FileExtensionConfigurationBuilderProvider provider = setUpProvider();
        assertEquals(
                "Wrong class",
                DEF_CLASS,
                provider.determineConfigurationClass(decl,
                        Collections.singleton(params)));
    }

    /**
     * Tests whether the correct configuration class is selected if the file
     * extension does not match.
     */
    @Test
    public void testDetermineConfigurationClassExtensionNoMatch()
            throws ConfigurationException
    {
        final ConfigurationDeclaration decl = setUpDecl();
        final BuilderParameters params =
                new FileBasedBuilderParametersImpl()
                        .setPath("C:\\Test\\someTestConfiguration.properties");
        final FileExtensionConfigurationBuilderProvider provider = setUpProvider();
        assertEquals(
                "Wrong class",
                DEF_CLASS,
                provider.determineConfigurationClass(decl,
                        Collections.singleton(params)));
    }

    /**
     * Tests whether the correct configuration class is selected if no file name
     * is set.
     */
    @Test
    public void testDeterminieConfigurationClassNoFileName()
            throws ConfigurationException
    {
        final ConfigurationDeclaration decl = setUpDecl();
        final BuilderParameters params = new FileBasedBuilderParametersImpl();
        final FileExtensionConfigurationBuilderProvider provider = setUpProvider();
        assertEquals(
                "Wrong class",
                DEF_CLASS,
                provider.determineConfigurationClass(decl,
                        Collections.singleton(params)));
    }

    /**
     * Tests whether the correct configuration class is selected if the file
     * extension matches.
     */
    @Test
    public void testDetermineConfigurationClassExtensionMatch()
            throws ConfigurationException
    {
        final ConfigurationDeclaration decl = setUpDecl();
        final BuilderParameters params =
                new FileBasedBuilderParametersImpl()
                        .setPath("C:\\Test\\someTestConfiguration." + EXT);
        final FileExtensionConfigurationBuilderProvider provider = setUpProvider();
        assertEquals(
                "Wrong class",
                MATCH_CLASS,
                provider.determineConfigurationClass(decl,
                        Collections.singleton(params)));
    }

    /**
     * Tests that matches of file extensions are case insensitive.
     */
    @Test
    public void testDetermineConfigurationClassMatchCase()
            throws ConfigurationException
    {
        final ConfigurationDeclaration decl = setUpDecl();
        final BuilderParameters params =
                new FileBasedBuilderParametersImpl()
                        .setPath("C:\\Test\\someTestConfiguration."
                                + EXT.toUpperCase(Locale.ENGLISH));
        final FileExtensionConfigurationBuilderProvider provider = setUpProvider();
        assertEquals(
                "Wrong class",
                MATCH_CLASS,
                provider.determineConfigurationClass(decl,
                        Collections.singleton(params)));
    }
}
