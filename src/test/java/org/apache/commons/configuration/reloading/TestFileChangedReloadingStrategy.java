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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.junit.Test;

/**
 * Test case for the ReloadableConfiguration class.
 *
 * @author Emmanuel Bourg
 * @version $Id$
 */
public class TestFileChangedReloadingStrategy
{
    /** Constant for the name of a test properties file.*/
    private static final String TEST_FILE = "test.properties";

    @Test
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
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.setFileName("target/testReload.properties");
        config.load();
        FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
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

    @Test
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
        FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
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

    @Test
    public void testGetRefreshDelay()
    {
        FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
        strategy.setRefreshDelay(500);
        assertEquals("refresh delay", 500, strategy.getRefreshDelay());
    }

    /**
     * Tests if a file from the classpath can be monitored.
     */
    @Test
    public void testFromClassPath() throws Exception
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.setFileName(TEST_FILE);
        config.load();
        assertTrue(config.getBoolean("configuration.loaded"));
        FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
        config.setReloadingStrategy(strategy);
        assertEquals(config.getURL().toExternalForm(), strategy.getFile().toURI().toURL().toExternalForm());
    }

    /**
     * Tests to watch a configuration file in a jar. In this case the jar file
     * itself should be monitored.
     */
    @Test
    public void testFromJar() throws Exception
    {
        XMLConfiguration config = new XMLConfiguration();
        // use some jar: URL
        config.setURL(new URL("jar:" + new File("conf/resources.jar").getAbsoluteFile().toURI().toURL() + "!/test-jar.xml"));
        FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
        config.setReloadingStrategy(strategy);
        File file = strategy.getFile();
        assertNotNull("Strategy's file is null", file);
        assertEquals("Strategy does not monitor the jar file", "resources.jar", file.getName());
    }

    /**
     * Tests calling reloadingRequired() multiple times before a reload actually
     * happens. This test is related to CONFIGURATION-302.
     */
    @Test
    public void testReloadingRequiredMultipleTimes()
            throws ConfigurationException
    {
        FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy()
        {
            @Override
            protected boolean hasChanged()
            {
                // signal always a change
                return true;
            }
        };
        strategy.setRefreshDelay(100000);
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.setFileName(TEST_FILE);
        config.load();
        config.setReloadingStrategy(strategy);
        assertTrue("Reloading not required", strategy.reloadingRequired());
        assertTrue("Reloading no more required", strategy.reloadingRequired());
        strategy.reloadingPerformed();
        assertFalse("Reloading still required", strategy.reloadingRequired());
    }

    @Test
    public void testFileDeletion() throws Exception
    {
        Logger logger = Logger.getLogger(FileChangedReloadingStrategy.class.getName());
        Layout layout = new PatternLayout("%p - %m%n");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Appender appender = new WriterAppender(layout, os);
        logger.addAppender(appender);
        logger.setLevel(Level.WARN);
        logger.setAdditivity(false);
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
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.setFileName("target/testReload.properties");
        config.load();
        FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
        strategy.setRefreshDelay(500);
        config.setReloadingStrategy(strategy);
        assertEquals("Initial value", "value1", config.getString("string"));

        Thread.sleep(2000);

        // Delete the file.
        file.delete();
        //Old value should still be returned.
        assertEquals("Initial value", "value1", config.getString("string"));
        logger.removeAppender(appender);
        String str = os.toString();
        //System.out.println(str);
        assertTrue("No error was logged", str != null && str.length() > 0);
    }
}
