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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration2.io.FileHandler;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code FileHandlerReloadingDetector}.
 */
public class TestFileHandlerReloadingDetector {
    /**
     * A test implementation which allows mocking the monitored file.
     */
    private static final class FileHandlerReloadingDetectorTestImpl extends FileHandlerReloadingDetector {
        /** The mock file. */
        private final File mockFile;

        /**
         * Creates a new instance of {@code FileHandlerReloadingDetectorTestImpl} and initializes it with the mock file.
         *
         * @param file the mock file
         */
        public FileHandlerReloadingDetectorTestImpl(final File file) {
            this(file, 0);
        }

        /**
         * Creates a new instance of {@code FileHandlerReloadingDetectorTestImpl} and initializes it with the mock file and a
         * refresh delay.
         *
         * @param file the mock file
         * @param delay the delay
         */
        public FileHandlerReloadingDetectorTestImpl(final File file, final long delay) {
            super(null, delay);
            mockFile = file;
        }

        /**
         * Always returns the mock file.
         */
        @Override
        protected File getFile() {
            return mockFile;
        }
    }

    /** Constant for a file's modification time. */
    private static final long LAST_MODIFIED = 20121008215654L;

    /**
     * Tests the default refresh delay.
     */
    @Test
    public void testDefaultRefreshDelay() {
        final FileHandlerReloadingDetector detector = new FileHandlerReloadingDetector();
        assertEquals(5000, detector.getRefreshDelay());
    }

    /**
     * Tests whether a jar URL is handled correctly.
     */
    @Test
    public void testGetFileJarURL() throws Exception {
        final FileHandlerReloadingDetector detector = new FileHandlerReloadingDetector();
        final URL url = new URL("jar:" + new File("conf/resources.jar").getAbsoluteFile().toURI().toURL() + "!/test-jar.xml");
        detector.getFileHandler().setURL(url);
        final File file = detector.getFile();
        assertNotNull(file);
        assertEquals("resources.jar", file.getName());
    }

    /** The detector to be tested. */
    /**
     * Tests whether an instance can be created with a file handler.
     */
    @Test
    public void testInitWithFileHandler() {
        final FileHandler handler = new FileHandler();
        final FileHandlerReloadingDetector detector = new FileHandlerReloadingDetector(handler);
        assertSame(handler, detector.getFileHandler());
    }

    /**
     * Tests whether a non-existing file is handled correctly.
     */
    @Test
    public void testIsReloadingRequiredFileDoesNotExist() {
        final FileHandlerReloadingDetector detector = new FileHandlerReloadingDetector();
        detector.getFileHandler().setFile(new File("NonExistingFile.txt"));
        detector.reloadingPerformed();
        assertFalse(detector.isReloadingRequired());
    }

    /**
     * Tests isReloadingRequired() if no location has been set.
     */
    @Test
    public void testIsReloadingRequiredNoLocation() {
        final FileHandlerReloadingDetector detector = new FileHandlerReloadingDetector();
        assertFalse(detector.isReloadingRequired());
    }

    /**
     * Tests whether a changed file is detected.
     */
    @Test
    public void testIsReloadingRequiredTrue() throws Exception {
        final File f = mock(File.class);

        when(f.exists()).thenReturn(Boolean.TRUE);
        when(f.lastModified()).thenReturn(LAST_MODIFIED, LAST_MODIFIED + 1);

        final FileHandlerReloadingDetector detector = new FileHandlerReloadingDetectorTestImpl(f);
        assertFalse(detector.isReloadingRequired());
        assertTrue(detector.isReloadingRequired());
    }

    /**
     * Tests that a newly created instance does not have a location.
     */
    @Test
    public void testLocationAfterInit() {
        final FileHandlerReloadingDetector detector = new FileHandlerReloadingDetector();
        assertFalse(detector.getFileHandler().isLocationDefined());
    }

    /**
     * Tests whether the refresh delay is taken into account.
     */
    @Test
    public void testRefreshDelay() throws Exception {
        final File f = mock(File.class);

        when(f.exists()).thenReturn(Boolean.TRUE);
        when(f.lastModified()).thenReturn(LAST_MODIFIED, LAST_MODIFIED);

        final FileHandlerReloadingDetector detector = new FileHandlerReloadingDetectorTestImpl(f, 60 * 60 * 1000L);
        detector.reloadingPerformed();
        assertFalse(detector.isReloadingRequired());
        assertFalse(detector.isReloadingRequired());
    }

    /**
     * Tests whether a changed file is detected after initialization and invoking refresh.
     */
    @Test
    public void testRefreshIsReloadingRequiredTrue() throws Exception {
        final File f = mock(File.class);

        when(f.exists()).thenReturn(Boolean.TRUE);
        when(f.lastModified()).thenReturn(LAST_MODIFIED, LAST_MODIFIED + 1);

        final FileHandlerReloadingDetector detector = new FileHandlerReloadingDetectorTestImpl(f);
        detector.refresh();
        assertTrue(detector.isReloadingRequired());
    }

    /**
     * Tests a refresh cycle with a detected reload operation and a notification that reloading was performed.
     */
    @Test
    public void testRefreshReloadingAndReset() throws Exception {
        final File f = mock(File.class);

        when(f.exists()).thenReturn(Boolean.TRUE);
        when(f.lastModified()).thenReturn(
                LAST_MODIFIED, LAST_MODIFIED, // 2 times
                LAST_MODIFIED + 1, LAST_MODIFIED + 1, LAST_MODIFIED + 1, // 3 times
                LAST_MODIFIED + 2);

        final FileHandlerReloadingDetector detector = new FileHandlerReloadingDetectorTestImpl(f);
        detector.refresh();
        assertFalse(detector.isReloadingRequired());
        assertTrue(detector.isReloadingRequired());
        detector.reloadingPerformed();
        assertFalse(detector.isReloadingRequired());
        assertTrue(detector.isReloadingRequired());
    }

    /**
     * Tests a cycle with a detected reload operation and a notification that reloading was performed.
     */
    @Test
    public void testReloadingAndReset() throws Exception {
        final File f = mock(File.class);

        when(f.exists()).thenReturn(Boolean.TRUE);
        when(f.lastModified()).thenReturn(
                LAST_MODIFIED,
                LAST_MODIFIED + 1, LAST_MODIFIED + 1, LAST_MODIFIED + 1, // 3 times
                LAST_MODIFIED + 2);

        final FileHandlerReloadingDetector detector = new FileHandlerReloadingDetectorTestImpl(f);
        assertFalse(detector.isReloadingRequired());
        assertTrue(detector.isReloadingRequired());
        detector.reloadingPerformed();
        assertFalse(detector.isReloadingRequired());
        assertTrue(detector.isReloadingRequired());
    }
}
