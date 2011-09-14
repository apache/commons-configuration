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

import java.io.File;
import java.io.FileWriter;

import junit.framework.TestCase;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.FileSystem;
import org.apache.commons.configuration.VFSFileSystem;

/**
 * Test case for the VFSFileMonitorReloadingStrategy class.
 *
 * @author Ralph Goers
 * @version $Revision$
 */
public class TestVFSFileChangedReloadingStrategy extends TestCase
{
    /** Constant for the name of a test properties file.*/
    private static final String TEST_FILE = "test.properties";

    protected void setUp() throws Exception
    {
        super.setUp();
        FileSystem.setDefaultFileSystem(new VFSFileSystem());
    }

    protected void tearDown() throws Exception
    {
        FileSystem.resetDefaultFileSystem();
        super.tearDown();
    }

    public void testAutomaticReloading() throws Exception
    {
        // create a new configuration
        File file = new File("target/testReload.properties");

        if (file.exists())
        {
            file.delete();
        }

        // create the configuration file
        FileWriter out = new FileWriter(file);
        out.write("string=value1");
        out.flush();
        out.close();

        // load the configuration
        PropertiesConfiguration config = new PropertiesConfiguration("target/testReload.properties");
        VFSFileChangedReloadingStrategy strategy = new VFSFileChangedReloadingStrategy();
        strategy.setRefreshDelay(500);
        config.setReloadingStrategy(strategy);
        assertEquals("Initial value", "value1", config.getString("string"));

        Thread.sleep(2000);

        // change the file
        out = new FileWriter(file);
        out.write("string=value2");
        out.flush();
        out.close();

        // test the automatic reloading
        assertEquals("Modified value with enabled reloading", "value2", config.getString("string"));
    }

    public void testNewFileReloading() throws Exception
    {
        // create a new configuration
        File file = new File("target/testReload.properties");

        if (file.exists())
        {
            file.delete();
        }

        PropertiesConfiguration config = new PropertiesConfiguration();
        config.setFile(file);
        VFSFileChangedReloadingStrategy strategy = new VFSFileChangedReloadingStrategy();
        strategy.setRefreshDelay(500);
        config.setReloadingStrategy(strategy);

        assertNull("Initial value", config.getString("string"));

        // change the file
        FileWriter out = new FileWriter(file);
        out.write("string=value1");
        out.flush();
        out.close();

        Thread.sleep(2000);

        // test the automatic reloading
        assertEquals("Modified value with enabled reloading", "value1", config.getString("string"));
    }

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
    public void testReloadingRequiredMultipleTimes()
            throws ConfigurationException
    {
        VFSFileChangedReloadingStrategy strategy = new VFSFileChangedReloadingStrategy()
        {
            protected boolean hasChanged()
            {
                // signal always a change
                return true;
            }
        };
        strategy.setRefreshDelay(100000);
        PropertiesConfiguration config = new PropertiesConfiguration(TEST_FILE);
        config.setReloadingStrategy(strategy);
        assertTrue("Reloading not required", strategy.reloadingRequired());
        assertTrue("Reloading no more required", strategy.reloadingRequired());
        strategy.reloadingPerformed();
        assertFalse("Reloading still required", strategy.reloadingRequired());
    }
}