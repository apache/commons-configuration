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
package org.apache.commons.configuration.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test class for {@code FileBasedConfigurationBuilder}.
 *
 * @version $Id$
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
    private File createTestFile(int value)
    {
        Writer out = null;
        File file;
        try
        {
            file = folder.newFile();
            out = new FileWriter(file);
            out.write(String.format("%s=%d", PROP, value));
        }
        catch (IOException ioex)
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
                catch (IOException ioex)
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
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("throwExceptionOnMissing", Boolean.TRUE);
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class, params);
        PropertiesConfiguration conf = builder.getConfiguration();
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
        File file = createTestFile(1);
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class);
        builder.configure(new FileBasedBuilderParameters().setFile(file));
        PropertiesConfiguration config = builder.getConfiguration();
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
        File file = createTestFile(1);
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class);
        builder.configure(new FileBasedBuilderParameters().setFile(file));
        PropertiesConfiguration config = builder.getConfiguration();
        builder.resetResult();
        PropertiesConfiguration config2 = builder.getConfiguration();
        assertNotSame("Same configuration", config, config2);
        assertEquals("Not read from file", 1, config2.getInt(PROP));
    }

    /**
     * Tests whether the location can be changed after a configuration has been
     * created.
     */
    @Test
    public void testChangeLocationAfterCreation() throws ConfigurationException
    {
        File file1 = createTestFile(1);
        File file2 = createTestFile(2);
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class);
        builder.configure(new FileBasedBuilderParameters().setFile(file1));
        builder.getConfiguration();
        builder.getFileHandler().setFile(file2);
        builder.resetResult();
        PropertiesConfiguration config = builder.getConfiguration();
        assertEquals("Not read from file 2", 2, config.getInt(PROP));
    }

    /**
     * Tests whether a reset of the builder's initialization parameters also
     * resets the file location.
     */
    @Test
    public void testResetLocation() throws ConfigurationException
    {
        File file = createTestFile(1);
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class);
        builder.configure(new FileBasedBuilderParameters().setFile(file));
        builder.getConfiguration();
        builder.reset();
        PropertiesConfiguration config = builder.getConfiguration();
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
        File file1 = createTestFile(1);
        File file2 = createTestFile(2);
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class);
        builder.configure(new FileBasedBuilderParameters().setFile(file1));
        builder.getConfiguration();
        builder.getFileHandler().setFile(file2);
        builder.reset();
        builder.configure(new FileBasedBuilderParameters().setFile(file1));
        PropertiesConfiguration config = builder.getConfiguration();
        assertEquals("Not read from file 1", 1, config.getInt(PROP));
        builder.getFileHandler().setFile(file2);
        builder.resetResult();
        config = builder.getConfiguration();
        assertEquals("Not read from file 2", 2, config.getInt(PROP));
    }
}
