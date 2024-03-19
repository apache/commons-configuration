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
package org.apache.commons.configuration2.io;

import static org.apache.commons.configuration2.TempDirUtils.newFile;
import static org.apache.commons.configuration2.TempDirUtils.newFolder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test class for {@code HomeDirectoryLocationStrategy}.
 */
public class TestHomeDirectoryLocationStrategy {
    /** Constant for a test file name. */
    private static final String FILE_NAME = "test.tst";

    /** Constant for a base path to be used. */
    private static final String BASE_PATH = "sub";

    /** A folder for temporary files. */
    @TempDir
    public File tempFolder;

    /** A mock for the file system. */
    private FileSystem fileSystem;

    @BeforeEach
    public void setUp() throws Exception {
        fileSystem = mock(FileSystem.class);
    }

    /**
     * Creates a strategy test object which uses the temporary root directory as its home directory.
     *
     * @param withBasePath the base path flag
     * @return the test strategy
     */
    private HomeDirectoryLocationStrategy setUpStrategy(final boolean withBasePath) {
        return new HomeDirectoryLocationStrategy(tempFolder.getAbsolutePath(), withBasePath);
    }

    /**
     * Tests whether default values are correctly set by the constructor.
     */
    @Test
    public void testInitDefaults() {
        final HomeDirectoryLocationStrategy strategy = new HomeDirectoryLocationStrategy();
        assertEquals(System.getProperty("user.home"), strategy.getHomeDirectory());
        assertFalse(strategy.isEvaluateBasePath());
    }

    /**
     * Tests whether the base is actually evaluated if the flag is set.
     */
    @Test
    public void testLocateFailedWithBasePath() throws IOException {
        newFile(FILE_NAME, tempFolder);
        final FileLocator locator = FileLocatorUtils.fileLocator().basePath(BASE_PATH).fileName(FILE_NAME).create();
        final HomeDirectoryLocationStrategy strategy = setUpStrategy(true);
        assertNull(strategy.locate(fileSystem, locator));
    }

    /**
     * Tests whether a file can be located if the base path is ignored.
     */
    @Test
    public void testLocateSuccessIgnoreBasePath() throws IOException {
        final File file = newFile(FILE_NAME, tempFolder);
        final FileLocator locator = FileLocatorUtils.fileLocator().basePath(BASE_PATH).fileName(FILE_NAME).create();
        final HomeDirectoryLocationStrategy strategy = setUpStrategy(false);
        final URL url = strategy.locate(fileSystem, locator);
        assertEquals(file.getAbsoluteFile(), FileLocatorUtils.fileFromURL(url).getAbsoluteFile());
    }

    /**
     * Tests whether a file in a sub folder can be located.
     */
    @Test
    public void testLocateSuccessInSubFolder() throws IOException {
        final File sub = newFolder(BASE_PATH, tempFolder);
        final File file = new File(sub, FILE_NAME);
        assertTrue(file.createNewFile());
        final FileLocator locator = FileLocatorUtils.fileLocator().basePath(BASE_PATH).fileName(FILE_NAME).create();
        final HomeDirectoryLocationStrategy strategy = setUpStrategy(true);
        final URL url = strategy.locate(fileSystem, locator);
        assertEquals(file.getAbsoluteFile(), FileLocatorUtils.fileFromURL(url).getAbsoluteFile());
    }

    /**
     * Tests a locate() operation which evaluates the base path if no base path is set.
     */
    @Test
    public void testLocateSuccessNoBasePath() throws IOException {
        final File file = newFile(FILE_NAME, tempFolder);
        final FileLocator locator = FileLocatorUtils.fileLocator().fileName(FILE_NAME).create();
        final HomeDirectoryLocationStrategy strategy = setUpStrategy(true);
        final URL url = strategy.locate(fileSystem, locator);
        assertEquals(file.getAbsoluteFile(), FileLocatorUtils.fileFromURL(url).getAbsoluteFile());
    }

    /**
     * Tests a locate() operation if no file name is specified.
     */
    @Test
    public void testNoFileName() {
        final FileLocator locator = FileLocatorUtils.fileLocator().basePath(BASE_PATH).create();
        final HomeDirectoryLocationStrategy strategy = setUpStrategy(true);
        assertNull(strategy.locate(fileSystem, locator));
    }
}
