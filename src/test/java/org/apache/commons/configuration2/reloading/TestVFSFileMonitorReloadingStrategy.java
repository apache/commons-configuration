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

package org.apache.commons.configuration2.reloading;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.configuration2.ConfigurationException;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.FileSystem;
import org.apache.commons.configuration2.VFSFileSystem;
import org.apache.commons.configuration2.AbstractFileConfiguration;
import org.apache.commons.configuration2.event.ConfigurationListener;
import org.apache.commons.configuration2.event.ConfigurationEvent;

/**
 * Test case for the VFSFileMonitorReloadingStrategy class.
 *
 * @author Ralph Goers
 * @version $Revision: $
 */
public class TestVFSFileMonitorReloadingStrategy extends TestCase
        implements ConfigurationListener
{
    /** true when a file is changed */
    private boolean configChanged = false;

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
        config.addConfigurationListener(this);
        VFSFileMonitorReloadingStrategy strategy = new VFSFileMonitorReloadingStrategy();
        strategy.setDelay(500);
        config.setReloadingStrategy(strategy);

        assertNull("Initial value", config.getString("string"));

        // change the file
        FileWriter out = new FileWriter(file);
        out.write("string=value1");
        out.flush();
        out.close();

        waitForChange();

        // test the automatic reloading
        try
        {
            assertEquals("Modified value with enabled reloading", "value1", config.getString("string"));
        }
        finally
        {
            strategy.stopMonitor();
            if (file.exists())
            {
                file.delete();
            }
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
     private void waitForChange()
    {
        synchronized(this)
        {
            try
            {
                int count = 0;
                while (!configChanged && count++ <= 3)
                {
                    this.wait(5000);
                }
            }
            catch (InterruptedException ie)
            {
                throw new IllegalStateException("wait timed out");
            }
            finally
            {
                configChanged = false;
            }
        }
    }

    public void configurationChanged(ConfigurationEvent event)
    {
        if (event.getType() == AbstractFileConfiguration.EVENT_CONFIG_CHANGED)
        {
            synchronized(this)
            {
                configChanged = true;
                this.notify();
            }
        }
    }

}