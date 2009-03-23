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
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.FileSystem;
import org.apache.commons.configuration.VFSFileSystem;

/**
 * Test case for the VFSFileMonitorReloadingStrategy class.
 *
 * @author Ralph Goers
 * @version $Revision$
 */
public class TestVFSFileMonitorReloadingStrategy extends TestCase
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
        VFSFileMonitorReloadingStrategy strategy = new VFSFileMonitorReloadingStrategy();
        strategy.setDelay(500);
        config.setReloadingStrategy(strategy);
        assertEquals("Initial value", "value1", config.getString("string"));

        Thread.sleep(1000);

        // change the file
        out = new FileWriter(file);
        out.write("string=value2");
        out.flush();
        out.close();

        Thread.sleep(2000);

        // test the automatic reloading
        assertEquals("Modified value with enabled reloading", "value2", config.getString("string"));
        strategy.stopMonitor();
        if (file.exists())
        {
            file.delete();
        }
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
        VFSFileMonitorReloadingStrategy strategy = new VFSFileMonitorReloadingStrategy();
        strategy.setDelay(500);
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
        strategy.stopMonitor();
        if (file.exists())
        {
            file.delete();
        }
    }

    public void testGetRefreshDelay() throws Exception
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

        PropertiesConfiguration config = new PropertiesConfiguration("target/testReload.properties");
        VFSFileMonitorReloadingStrategy strategy = new VFSFileMonitorReloadingStrategy();
        strategy.setDelay(500);
        config.setReloadingStrategy(strategy);
        // Minimum is 1 second.
        assertEquals("refresh delay", 1000, strategy.getDelay());

        config = new PropertiesConfiguration("target/testReload.properties");
        strategy = new VFSFileMonitorReloadingStrategy();
        strategy.setDelay(1500);
        config.setReloadingStrategy(strategy);
        // Can be made longer
        assertEquals("refresh delay", 1500, strategy.getDelay());

        config = new PropertiesConfiguration("target/testReload.properties");
        strategy = new VFSFileMonitorReloadingStrategy();
        strategy.setDelay(500);
        config.setReloadingStrategy(strategy);
        // Can't be made shorter
        assertEquals("refresh delay", 1500, strategy.getDelay());

        strategy.stopMonitor();
        // Reset and verify everything clears
        config = new PropertiesConfiguration("target/testReload.properties");
        strategy = new VFSFileMonitorReloadingStrategy();
        strategy.setDelay(1100);
        config.setReloadingStrategy(strategy);
        assertEquals("refresh delay", 1100, strategy.getDelay());
        strategy.stopMonitor();
        if (file.exists())
        {
            file.delete();
        }
    }
}