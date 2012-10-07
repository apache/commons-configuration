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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;

import org.apache.commons.configuration.io.FileHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test class for {@code FileHandlerReloadingDetector}.
 *
 * @version $Id$
 */
public class TestFileHandlerReloadingDetector
{
    /** The content of a test file. */
    private static final String CONTENT = "Test file content ";

    /** Constant for a sleep interval. */
    private static final long SLEEP_TIME = 200;

    /** Helper object for managing temporary files. */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** A counter for generating test file content. */
    private int count;

    /** The detector to be tested. */
    private FileHandlerReloadingDetector detector;

    @Before
    public void setUp() throws Exception
    {
        detector = new FileHandlerReloadingDetector(null, 0);
    }

    /**
     * Writes a test file at the specified location.
     *
     * @param f points to the file to be written
     */
    private void writeTestFile(File f)
    {
        Writer out = null;
        try
        {
            out = new FileWriter(f);
            out.write(CONTENT);
            out.write(String.valueOf(count++));
        }
        catch (IOException ioex)
        {
            fail("Could not create test file: " + ioex);
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
    }

    /**
     * Tests whether an instance can be created with a file handler.
     */
    @Test
    public void testInitWithFileHandler()
    {
        FileHandler handler = new FileHandler();
        detector = new FileHandlerReloadingDetector(handler);
        assertSame("Different file handler", handler, detector.getFileHandler());
    }

    /**
     * Tests the default refresh delay.
     */
    @Test
    public void testDefaultRefreshDelay()
    {
        detector = new FileHandlerReloadingDetector();
        assertEquals("Wrong delay", 5000, detector.getRefreshDelay());
    }

    /**
     * Tests that a newly created instance does not have a location.
     */
    @Test
    public void testLocationAfterInit()
    {
        assertFalse("Got a location", detector.getFileHandler()
                .isLocationDefined());
    }

    /**
     * Tests isReloadingRequired() if no location has been set.
     */
    @Test
    public void testIsReloadingRequiredNoLocation()
    {
        assertFalse("Reloading", detector.isReloadingRequired());
    }

    /**
     * Helper method for testing whether the need for a reload operation is
     * detected.
     *
     * @return the test file used by this method
     */
    private File checkReloadingDetect() throws IOException,
            InterruptedException
    {
        File f = folder.newFile();
        detector.getFileHandler().setFile(f);
        writeTestFile(f);
        assertFalse("Reloading required", detector.isReloadingRequired());
        Thread.sleep(SLEEP_TIME);
        writeTestFile(f);
        assertTrue("Reloading not detected", detector.isReloadingRequired());
        return f;
    }

    /**
     * Tests whether a changed file is detected.
     */
    @Test
    public void testIsReloadingRequiredTrue() throws Exception
    {
        checkReloadingDetect();
    }

    /**
     * Tests a cycle with a detected reload operation and a notification that
     * reloading was performed.
     */
    @Test
    public void testReloadingAndReset() throws Exception
    {
        File f = checkReloadingDetect();
        detector.reloadingPerformed();
        assertFalse("Still reloading required", detector.isReloadingRequired());
        Thread.sleep(SLEEP_TIME);
        writeTestFile(f);
        assertTrue("Next reloading not detected",
                detector.isReloadingRequired());
    }

    /**
     * Tests whether the refresh delay is taken into account.
     */
    @Test
    public void testRefreshDelay() throws Exception
    {
        FileHandler handler = new FileHandler();
        detector = new FileHandlerReloadingDetector(handler, 60 * 60 * 1000L);
        File f = folder.newFile();
        handler.setFile(f);
        writeTestFile(f);
        detector.reloadingPerformed();
        assertFalse("Reloading initially required",
                detector.isReloadingRequired());
        Thread.sleep(SLEEP_TIME);
        writeTestFile(f);
        assertFalse("Reloading required", detector.isReloadingRequired());
    }

    /**
     * Tests whether a non-existing file is handled correctly.
     */
    @Test
    public void testIsReloadingRequiredFileDoesNotExist()
    {
        detector.getFileHandler().setFile(new File("NonExistingFile.txt"));
        detector.reloadingPerformed();
        assertFalse("Reloading required", detector.isReloadingRequired());
    }

    /**
     * Tests whether a jar URL is handled correctly.
     */
    @Test
    public void testGetFileJarURL() throws Exception
    {
        URL url =
                new URL("jar:"
                        + new File("conf/resources.jar").getAbsoluteFile()
                                .toURI().toURL() + "!/test-jar.xml");
        detector.getFileHandler().setURL(url);
        File file = detector.getFile();
        assertNotNull("Detector's file is null", file);
        assertEquals("Detector does not monitor the jar file", "resources.jar",
                file.getName());
    }
}
