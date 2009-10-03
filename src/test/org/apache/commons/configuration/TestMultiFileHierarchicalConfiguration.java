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

package org.apache.commons.configuration;

import junit.framework.TestCase;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.xml.sax.SAXParseException;

import java.io.*;

/**
 * Unit test for simple MultiConfigurationTest.
 */
public class TestMultiFileHierarchicalConfiguration extends TestCase
{
    private static String PATTERN1 = "target/test-classes/testMultiConfiguration_${sys:Id}.xml";

    private static final File MULTI_TENENT_FILE = new File(
            "conf/testMultiTenentConfigurationBuilder2.xml");

    private static final File MULTI_TENENT_FILE2 = new File(
            "target/test-classes/testMultiTenentConfigurationBuilder2.xml");

    private static final File MULTI_RELOAD_FILE = new File(
            "conf/testMultiTenentConfigurationBuilder3.xml");

    /**
     * Rigourous Test :-)
     */
    public void testMultiConfiguration()
    {
        //set up a reloading strategy
        FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
        strategy.setRefreshDelay(10000);

        MultiFileHierarchicalConfiguration config = new MultiFileHierarchicalConfiguration(PATTERN1);
        config.setReloadingStrategy(strategy);

        System.setProperty("Id", "1001");
        assertTrue(config.getInt("rowsPerPage") == 15);

        System.setProperty("Id", "1002");
        assertTrue(config.getInt("rowsPerPage") == 25);

        System.setProperty("Id", "1003");
        assertTrue(config.getInt("rowsPerPage") == 35);
    }

    public void testSchemaValidationError() throws Exception
    {
        System.clearProperty("Id");
        DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder();
        factory.setFile(MULTI_TENENT_FILE);
        CombinedConfiguration config = factory.getConfiguration(true);
        try
        {
            System.setProperty("Id", "2001");
            config.getInt("rowsPerPage");
            fail("No exception thrown");
        }
        catch (Exception ex)
        {
            Throwable cause = ex.getCause();
            while (cause != null && !(cause instanceof SAXParseException))
            {
                cause = cause.getCause();
            }
            assertTrue("SAXParseException was not thrown", cause instanceof SAXParseException);
        }
    }

    public void testSchemaValidation() throws Exception
    {
        System.clearProperty("Id");
        DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder();
        factory.setFile(MULTI_TENENT_FILE);
        CombinedConfiguration config = factory.getConfiguration(true);
        System.setProperty("Id", "2002");
        int rows = config.getInt("rowsPerPage");
        assertTrue("expected: " + rows + " actual: " + "25", 25 == rows);
    }

    public void testMissingFile() throws Exception
    {
        System.clearProperty("Id");
        DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder();
        factory.setFile(MULTI_TENENT_FILE);
        CombinedConfiguration config = factory.getConfiguration(true);
        System.setProperty("Id", "3099");
        int rows = config.getInt("rowsPerPage");
        assertTrue("expected: " + rows + " actual: " + "50", 50 == rows);

    }

    public void testFileReload1() throws Exception
    {
        System.getProperties().remove("Id");
        DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder();
        factory.setFile(MULTI_RELOAD_FILE);
        CombinedConfiguration config = factory.getConfiguration(true);

        // create a new configuration
        File input = new File("target/test-classes/testMultiConfiguration_3001.xml");
        File output = new File("target/test-classes/testwrite/testMultiConfiguration_3001.xml");
        output.delete();
        output.getParentFile().mkdir();
        copyFile(input, output);

        assertNotNull(config);
        verify("3001", config, 15);
        Thread.sleep(1100);
        XMLConfiguration x = new XMLConfiguration();
        x.setFile(output);
        x.setAttributeSplittingDisabled(true);
        x.setDelimiterParsingDisabled(true);
        x.load();
        x.setProperty("rowsPerPage", "35");
        //Insure orginal timestamp and new timestamp aren't the same second.
        Thread.sleep(1100);
        x.save();
        verify("3001", config, 35);
        output.delete();
    }

    public void testFileReload2() throws Exception
    {
        // create a new configuration
        File input = new File("target/test-classes/testMultiConfiguration_3002.xml");
        File output = new File("target/test-classes/testwrite/testMultiConfiguration_3002.xml");
        output.delete();

        System.getProperties().remove("Id");
        DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder();
        factory.setFile(MULTI_RELOAD_FILE);
        CombinedConfiguration config = factory.getConfiguration(true);
        assertNotNull(config);
        // The file should not exist yet.
        verify("3002", config, 50);

        output.getParentFile().mkdir();
        copyFile(input, output);
        Thread.sleep(600);
        verify("3002", config, 25);
        output.delete();
    }

    public void testFileReload3() throws Exception
    {
        // create a new configuration
        File input = new File("target/test-classes/testMultiConfiguration_3001.xml");
        File output = new File("target/test-classes/testwrite/testMultiConfiguration_3001.xml");
        output.delete();
        output.getParentFile().mkdir();

        System.getProperties().remove("Id");
        DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder();
        factory.setFile(MULTI_RELOAD_FILE);
        CombinedConfiguration config = factory.getConfiguration(true);
        assertNotNull(config);
        //The file does not exist yet.
        verify("3001", config, 50);
        copyFile(input, output);
        //Sleep so refreshDelay elapses
        Thread.sleep(600);
        verify("3001", config, 15);
        Thread.sleep(500);
        XMLConfiguration x = new XMLConfiguration();
        x.setFile(output);
        x.setAttributeSplittingDisabled(true);
        x.setDelimiterParsingDisabled(true);
        x.load();
        x.setProperty("rowsPerPage", "35");
        // Insure original timestamp and new timestamp are not the same second.
        Thread.sleep(1100);
        x.save();
        verify("3001", config, 35);
        output.delete();
    }


    public void testReloadDefault() throws Exception
    {
        // create a new configuration
        String defaultName = "target/test-classes/testMultiConfiguration_default.xml";
        File input = new File(defaultName);

        System.getProperties().remove("Id");
        DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder();
        factory.setFile(MULTI_TENENT_FILE2);
        CombinedConfiguration config = factory.getConfiguration(true);
        assertNotNull(config);
        verify("3001", config, 15);
        verify("3002", config, 25);
        System.setProperty("Id", "3002");
        config.addProperty("/ TestProp", "Test");
        assertTrue("Property not added", "Test".equals(config.getString("TestProp")));
        System.getProperties().remove("Id");
        //Sleep so refreshDelay elapses
        Thread.sleep(600);
        long time = System.currentTimeMillis();
        long original = input.lastModified();
        input.setLastModified(time);
        File defaultFile = new File(defaultName);
        long newTime = defaultFile.lastModified();
        assertTrue("time mismatch", original != newTime);
        Thread.sleep(600);
        verify("3001", config, 15);
        verify("3002", config, 25);
        System.setProperty("Id", "3002");
        String test = config.getString("TestProp");
        assertNull("Property was not cleared by reload", test);
    }


    public void testFileReloadSchemaValidationError() throws Exception
    {
        System.getProperties().remove("Id");
        DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder();
        factory.setFile(MULTI_RELOAD_FILE);
        CombinedConfiguration config = factory.getConfiguration(true);

        // create a new configuration
        File input = new File("target/test-classes/testMultiConfiguration_3001.xml");
        File output = new File("target/test-classes/testwrite/testMultiConfiguration_3001.xml");
        output.delete();
        output.getParentFile().mkdir();
        copyFile(input, output);

        assertNotNull(config);
        verify("3001", config, 15);
        Thread.sleep(1100);
        XMLConfiguration x = new XMLConfiguration();
        x.setFile(output);
        x.setAttributeSplittingDisabled(true);
        x.setDelimiterParsingDisabled(true);
        x.load();
        x.setProperty("rowsPerPage", "test");
        //Insure orginal timestamp and new timestamp aren't the same second.
        Thread.sleep(1100);
        x.save();
        System.setProperty("Id", "3001");
        try
        {
            config.getInt("rowsPerPage");
            fail("No exception was thrown");
        }
        catch (Exception ex)
        {

        }

        output.delete();
    }    

    private void copyFile(File input, File output) throws IOException
    {
        Reader reader = new FileReader(input);
        Writer writer = new FileWriter(output);
        char[] buffer = new char[4096];
        int n = 0;
        while (-1 != (n = reader.read(buffer)))
        {
            writer.write(buffer, 0, n);
        }
        reader.close();
        writer.close();
    }

    private void verify(String key, CombinedConfiguration config, int rows)
    {
        if (key == null)
        {
            System.getProperties().remove("Id");
        }
        else
        {
            System.setProperty("Id", key);
        }
        int actual = config.getInt("rowsPerPage");
        assertTrue("expected: " + rows + " actual: " + actual, actual == rows);
    }
}
