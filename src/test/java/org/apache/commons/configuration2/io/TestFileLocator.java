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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code FileLocator}.
 */
public class TestFileLocator {
    /** Constant for a file name. */
    private static final String FILE_NAME = "test.xml";

    /** Constant for a base path. */
    private static final String BASE_PATH = "/etc/test/path/";

    /** Constant for a test encoding. */
    private static final String ENCODING = StandardCharsets.UTF_8.name();

    /** A test URL. */
    private static URL sourceURL;

    /** A test file system. */
    private static FileSystem fileSystem;

    /** A test location strategy. */
    private static FileLocationStrategy locationStrategy;

    /**
     * Tests whether a locator has the expected properties.
     *
     * @param locator the locator to check
     */
    private static void checkLocator(final FileLocator locator) {
        assertEquals(BASE_PATH, locator.getBasePath());
        assertEquals(FILE_NAME, locator.getFileName());
        assertEquals(ENCODING, locator.getEncoding());
        assertEquals(sourceURL.toExternalForm(), locator.getSourceURL().toExternalForm());
        assertSame(fileSystem, locator.getFileSystem());
        assertSame(locationStrategy, locator.getLocationStrategy());
    }

    @BeforeAll
    public static void setUpOnce() throws Exception {
        sourceURL = ConfigurationAssert.getTestURL(FILE_NAME);
        fileSystem = mock(FileSystem.class);
        locationStrategy = mock(FileLocationStrategy.class);
    }

    /**
     * Tests the creation of a file locator.
     */
    @Test
    public void testCreateFileLocator() {
        final FileLocator locator = FileLocatorUtils.fileLocator().basePath(BASE_PATH).fileName(FILE_NAME).encoding(ENCODING).fileSystem(fileSystem)
            .sourceURL(sourceURL).locationStrategy(locationStrategy).create();
        checkLocator(locator);
    }

    /**
     * Tests whether a file locator can be created from a source locator.
     */
    @Test
    public void testCreateFileLocatorFromSource() {
        final FileLocator locatorSrc = FileLocatorUtils.fileLocator().basePath(BASE_PATH).fileName("someFile").encoding(ENCODING).fileSystem(fileSystem)
            .sourceURL(sourceURL).locationStrategy(locationStrategy).create();
        final FileLocator locator = FileLocatorUtils.fileLocator(locatorSrc).fileName(FILE_NAME).create();
        checkLocator(locator);
    }

    /**
     * Tests whether an undefined file locator can be created.
     */
    @Test
    public void testCreateFileLocatorUndefined() {
        final FileLocator locator = FileLocatorUtils.fileLocator().create();
        assertNull(locator.getBasePath());
        assertNull(locator.getFileName());
        assertNull(locator.getSourceURL());
        assertNull(locator.getEncoding());
        assertNull(locator.getFileSystem());
        assertNull(locator.getLocationStrategy());
    }

    /**
     * Tests the equals() implementation of FileLocator if the expected result is false.
     */
    @Test
    public void testFileLocatorEqualsFalse() {
        final FileLocator loc1 = FileLocatorUtils.fileLocator().basePath(BASE_PATH).fileName(FILE_NAME).encoding(ENCODING).fileSystem(fileSystem)
            .sourceURL(sourceURL).locationStrategy(locationStrategy).create();
        FileLocator loc2 = FileLocatorUtils.fileLocator(loc1).basePath(BASE_PATH + "_other").create();
        ConfigurationAssert.checkEquals(loc1, loc2, false);
        loc2 = FileLocatorUtils.fileLocator(loc1).fileName(FILE_NAME + "_other").create();
        ConfigurationAssert.checkEquals(loc1, loc2, false);
        loc2 = FileLocatorUtils.fileLocator(loc1).encoding(ENCODING + "_other").create();
        ConfigurationAssert.checkEquals(loc1, loc2, false);
        loc2 = FileLocatorUtils.fileLocator(loc1).fileSystem(mock(FileSystem.class)).create();
        ConfigurationAssert.checkEquals(loc1, loc2, false);
        loc2 = FileLocatorUtils.fileLocator(loc1).sourceURL(ConfigurationAssert.getTestURL("test.properties")).create();
        ConfigurationAssert.checkEquals(loc1, loc2, false);
        loc2 = FileLocatorUtils.fileLocator(loc1).locationStrategy(mock(FileLocationStrategy.class)).create();
        ConfigurationAssert.checkEquals(loc1, loc2, false);
    }

    /**
     * Tests equals() with a null object.
     */
    @Test
    public void testFileLocatorEqualsNull() {
        final FileLocator loc = FileLocatorUtils.fileLocator().fileName(FILE_NAME).create();
        assertNotEquals(null, loc);
    }

    /**
     * Tests equals() with an object from another class.
     */
    @Test
    public void testFileLocatorEqualsOtherClass() {
        final FileLocator loc = FileLocatorUtils.fileLocator().fileName(FILE_NAME).create();
        assertNotEquals(loc, this);
    }

    /**
     * Tests the equals() implementation of FileLocator if the expected result is true.
     */
    @Test
    public void testFileLocatorEqualsTrue() {
        FileLocator loc1 = FileLocatorUtils.fileLocator().create();
        ConfigurationAssert.checkEquals(loc1, loc1, true);
        FileLocator loc2 = FileLocatorUtils.fileLocator().create();
        ConfigurationAssert.checkEquals(loc1, loc2, true);
        loc1 = FileLocatorUtils.fileLocator().basePath(BASE_PATH).fileName(FILE_NAME).encoding(ENCODING).fileSystem(fileSystem).sourceURL(sourceURL)
            .locationStrategy(locationStrategy).create();
        loc2 = FileLocatorUtils.fileLocator().basePath(BASE_PATH).fileName(FILE_NAME).encoding(ENCODING).fileSystem(fileSystem).sourceURL(sourceURL)
            .locationStrategy(locationStrategy).create();
        ConfigurationAssert.checkEquals(loc1, loc2, true);
    }

    /**
     * Tests the string representation of a locator.
     */
    @Test
    public void testFileLocatorToString() {
        final FileLocator loc = FileLocatorUtils.fileLocator().basePath(BASE_PATH).fileName(FILE_NAME).encoding(ENCODING).fileSystem(fileSystem)
            .sourceURL(sourceURL).locationStrategy(locationStrategy).create();
        final String s = loc.toString();
        assertThat(s, containsString("fileName=" + FILE_NAME));
        assertThat(s, containsString("basePath=" + BASE_PATH));
        assertThat(s, containsString("sourceURL=" + sourceURL));
        assertThat(s, containsString("encoding=" + ENCODING));
        assertThat(s, containsString("fileSystem=" + fileSystem));
        assertThat(s, containsString("locationStrategy=" + locationStrategy));
    }
}
