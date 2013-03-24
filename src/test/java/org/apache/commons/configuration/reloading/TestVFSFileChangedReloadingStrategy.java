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

package org.apache.commons.configuration.reloading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileSystem;
import org.apache.commons.configuration.VFSFileSystem;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case for the VFSFileMonitorReloadingStrategy class.
 *
 * @author Ralph Goers
 * @version $Id$
 */
public class TestVFSFileChangedReloadingStrategy
{
    /** Constant for the name of a test properties file.*/
    private static final String TEST_FILE = "test.xml";

    /** Constant for the name of the test property. */
    private static final String PROPERTY = "string";

    /** Constant for the XML fragment to be written. */
    private static final String FMT_XML = "<configuration><" + PROPERTY
            + ">%s</" + PROPERTY + "></configuration>";

    /** A helper object for creating temporary files. */
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception
    {
        FileSystem.setDefaultFileSystem(new VFSFileSystem());
    }

    @After
    public void tearDown() throws Exception
    {
        FileSystem.resetDefaultFileSystem();
    }

    /**
     * Writes a test configuration file containing a single property with the
     * given value.
     * @param file the file to be written
     * @param value the value of the test property
     * @throws IOException if an error occurs
     */
    private void writeTestFile(File file, String value) throws IOException
    {
        FileWriter out = new FileWriter(file);
        out.write(String.format(FMT_XML, value));
        out.close();
    }

    @Test
    public void testAutomaticReloading() throws Exception
    {
        // create a new configuration
        File file = folder.newFile();

        // create the configuration file
        writeTestFile(file, "value1");

        // load the configuration
        XMLConfiguration config = new XMLConfiguration();
        config.setFile(file);
        config.load();
        VFSFileChangedReloadingStrategy strategy = new VFSFileChangedReloadingStrategy();
        strategy.setRefreshDelay(500);
        config.setReloadingStrategy(strategy);
        assertEquals("Initial value", "value1", config.getString(PROPERTY));

        Thread.sleep(2000);

        // change the file
        writeTestFile(file, "value2");

        // test the automatic reloading
        assertEquals("Modified value with enabled reloading", "value2", config.getString(PROPERTY));
    }

    @Test
    public void testNewFileReloading() throws Exception
    {
        // create a new configuration
        File file = folder.newFile();

        XMLConfiguration config = new XMLConfiguration();
        config.setFile(file);
        VFSFileChangedReloadingStrategy strategy = new VFSFileChangedReloadingStrategy();
        strategy.setRefreshDelay(500);
        config.setReloadingStrategy(strategy);

        assertNull("Initial value", config.getString(PROPERTY));

        // change the file
        writeTestFile(file, "value1");
        Thread.sleep(2000);

        // test the automatic reloading
        assertEquals("Modified value with enabled reloading", "value1", config.getString(PROPERTY));
    }

    @Test
    public void testGetRefreshDelay() throws Exception
    {
        VFSFileChangedReloadingStrategy strategy = new VFSFileChangedReloadingStrategy();
        strategy.setRefreshDelay(500);
        assertEquals("refresh delay", 500, strategy.getRefreshDelay());
    }

    /**
     * Tests calling reloadingRequired() multiple times before a reload actually
     * happens. This test is related to CONFIGURATION-302.
     */
    @Test
    public void testReloadingRequiredMultipleTimes()
            throws ConfigurationException
    {
        VFSFileChangedReloadingStrategy strategy = new VFSFileChangedReloadingStrategy()
        {
            @Override
            protected boolean hasChanged()
            {
                // signal always a change
                return true;
            }
        };
        strategy.setRefreshDelay(100000);
        XMLConfiguration config = new XMLConfiguration();
        config.setFileName(TEST_FILE);
        config.load();
        config.setReloadingStrategy(strategy);
        assertTrue("Reloading not required", strategy.reloadingRequired());
        assertTrue("Reloading no more required", strategy.reloadingRequired());
        strategy.reloadingPerformed();
        assertFalse("Reloading still required", strategy.reloadingRequired());
    }
}