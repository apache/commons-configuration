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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code FileLocatorUtils}.
 */
public class TestFileLocatorUtils {
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

    /**
     * Checks whether the specified locator points to the expected test configuration file.
     *
     * @param locator the locator to check
     * @throws ConfigurationException if an error occurs
     */
    private static void checkFullyInitializedLocator(final FileLocator locator) throws ConfigurationException {
        assertNotNull(locator.getBasePath());
        assertNotNull(locator.getFileName());
        assertNotNull(locator.getSourceURL());

        FileHandler handler = new FileHandler();
        handler.setBasePath(locator.getBasePath());
        handler.setFileName(locator.getFileName());
        checkTestConfiguration(handler);

        handler = new FileHandler();
        handler.setURL(locator.getSourceURL());
        checkTestConfiguration(handler);
    }

    /**
     * Checks whether the expected test configuration can be loaded using the specified handler.
     *
     * @param handler the file handler
     * @throws ConfigurationException if an error occurs
     */
    private static void checkTestConfiguration(final FileHandler handler) throws ConfigurationException {
        final XMLConfiguration config = new XMLConfiguration();
        final FileHandler h2 = new FileHandler(config, handler);
        h2.load();
        assertEquals("value", config.getString("element"));
    }

    @BeforeAll
    public static void setUpOnce() throws Exception {
        sourceURL = ConfigurationAssert.getTestURL(FILE_NAME);
        fileSystem = mock(FileSystem.class);
    }

    /**
     * Tests a successful conversion from a file to a URL.
     */
    @Test
    public void testConvertFileToURL() throws ConfigurationException {
        final File file = ConfigurationAssert.getTestFile(FILE_NAME);
        final FileHandler handler = new FileHandler();
        handler.setURL(FileLocatorUtils.convertFileToURL(file));
        checkTestConfiguration(handler);
    }

    /**
     * Tests whether exceptions are handled when converting a URI to a URL.
     */
    @Test
    public void testConvertToURIException() throws URISyntaxException {
        final URI uri = new URI("test://test/path/file.tst");
        assertNull(FileLocatorUtils.convertURIToURL(uri));
    }

    /**
     * Tests the definition of the default location strategy.
     */
    @Test
    public void testDefaultFileLocationStrategy() {
        final CombinedLocationStrategy strategy = (CombinedLocationStrategy) FileLocatorUtils.DEFAULT_LOCATION_STRATEGY;
        final Iterator<FileLocationStrategy> it = strategy.getSubStrategies().iterator();
        assertInstanceOf(ProvidedURLLocationStrategy.class, it.next());
        assertInstanceOf(FileSystemLocationStrategy.class, it.next());
        assertInstanceOf(AbsoluteNameLocationStrategy.class, it.next());
        assertInstanceOf(BasePathLocationStrategy.class, it.next());
        FileLocationStrategy sub = it.next();
        HomeDirectoryLocationStrategy subStrategy = assertInstanceOf(HomeDirectoryLocationStrategy.class, sub);
        assertTrue(subStrategy.isEvaluateBasePath());
        sub = it.next();
        subStrategy = assertInstanceOf(HomeDirectoryLocationStrategy.class, sub);
        assertFalse(subStrategy.isEvaluateBasePath());
        assertInstanceOf(ClasspathLocationStrategy.class, it.next());
    }

    /**
     * Tests whether fileFromURL() handles null URLs correctly.
     */
    @Test
    public void testFileFromURLNull() throws Exception {
        assertNull(FileLocatorUtils.fileFromURL(null));
    }

    /**
     * Tests whether an encoded "%" character in the file name is handled correctly by fileFromURL(). This test is related
     * to CONFIGURATION-521.
     */
    @Test
    public void testFileFromURLWithEncodedPercent() throws MalformedURLException {
        final File file = new File("https%3A%2F%2Fwww.apache.org%2F.url").getAbsoluteFile();
        final URL fileURL = file.toURI().toURL();
        final File file2 = FileLocatorUtils.fileFromURL(fileURL);
        assertEquals(file, file2);
    }

    /**
     * Tests whether a "+" character in the file name is handled correctly by fileFromURL(). This test is related to
     * CONFIGURATION-415.
     */
    @Test
    public void testFileFromURLWithPlus() throws MalformedURLException {
        final File file = new File(new File("target"), "foo+bar.txt").getAbsoluteFile();
        final URL fileURL = file.toURI().toURL();
        final File file2 = FileLocatorUtils.fileFromURL(fileURL);
        assertEquals(file, file2);
    }

    /**
     * Tests whether fromMap() can handle a null map.
     */
    @Test
    public void testFromMapNoMap() {
        final FileLocator fileLocator = FileLocatorUtils.fromMap(null);
        assertEquals(FileLocatorUtils.fileLocator().create(), fileLocator);
    }

    /**
     * Tests fullyInitializedLocator() if the locator is already fully initialized.
     */
    @Test
    public void testFullyInitializedLocatorAlreadyComplete() {
        final FileLocator locator = FileLocatorUtils.fileLocator().fileName(FILE_NAME).create();
        final FileLocator fullLocator = FileLocatorUtils.fullyInitializedLocator(locator);
        assertSame(fullLocator, FileLocatorUtils.fullyInitializedLocator(fullLocator));
    }

    /**
     * Tests whether a fully initialized locator can be obtained if a file name is available.
     */
    @Test
    public void testFullyInitializedLocatorFileName() throws ConfigurationException {
        final FileLocator locator = FileLocatorUtils.fileLocator().fileName(FILE_NAME).create();
        checkFullyInitializedLocator(FileLocatorUtils.fullyInitializedLocator(locator));
    }

    /**
     * Tests fullyInitializedLocator() if a locate() operation fails.
     */
    @Test
    public void testFullyInitializedLocatorLocateFails() {
        final FileLocator locator = FileLocatorUtils.fileLocator().fileName("non existing file").create();
        assertNull(FileLocatorUtils.fullyInitializedLocator(locator));
    }

    /**
     * Tries to obtain a fully initialized locator if the source locator is not defined.
     */
    @Test
    public void testFullyInitializedLocatorUndefined() {
        assertNull(FileLocatorUtils.fullyInitializedLocator(FileLocatorUtils.fileLocator().create()));
    }

    /**
     * Tests whether a fully initialized locator can be obtained if a URL is available.
     */
    @Test
    public void testFullyInitializedLocatorURL() throws ConfigurationException {
        final FileLocator locator = FileLocatorUtils.fileLocator().sourceURL(sourceURL).create();
        checkFullyInitializedLocator(FileLocatorUtils.fullyInitializedLocator(locator));
    }

    @Test
    public void testGetBasePath() throws Exception {
        URL url = new URL("http://xyz.net/foo/bar.xml");
        assertEquals("http://xyz.net/foo/", FileLocatorUtils.getBasePath(url));

        url = new URL("http://xyz.net/foo/");
        assertEquals("http://xyz.net/foo/", FileLocatorUtils.getBasePath(url));

        url = new URL("http://xyz.net/foo");
        assertEquals("http://xyz.net/", FileLocatorUtils.getBasePath(url));

        url = new URL("http://xyz.net/");
        assertEquals("http://xyz.net/", FileLocatorUtils.getBasePath(url));

        url = new URL("http://xyz.net");
        assertEquals("http://xyz.net", FileLocatorUtils.getBasePath(url));
    }

    @Test
    public void testGetFile() throws Exception {
        final File directory = new File("target");
        final File reference = new File(directory, "test.txt").getAbsoluteFile();

        assertEquals(FileLocatorUtils.getFile(null, reference.getAbsolutePath()), reference);
        assertEquals(FileLocatorUtils.getFile(directory.getAbsolutePath(), reference.getAbsolutePath()), reference);
        assertEquals(FileLocatorUtils.getFile(directory.getAbsolutePath(), reference.getName()), reference);
        assertEquals(FileLocatorUtils.getFile(directory.toURI().toURL().toString(), reference.getName()), reference);
        assertEquals(FileLocatorUtils.getFile("invalid", reference.toURI().toURL().toString()), reference);
        assertEquals(FileLocatorUtils.getFile("jar:file:/C:/myjar.jar!/my-config.xml/someprops.properties", reference.getAbsolutePath()), reference);
    }

    @Test
    public void testGetFileName() throws Exception {
        assertNull(FileLocatorUtils.getFileName(null));

        URL url = new URL("http://xyz.net/foo/");
        assertNull(FileLocatorUtils.getFileName(url));

        url = new URL("http://xyz.net/foo/bar.xml");
        assertEquals("bar.xml", FileLocatorUtils.getFileName(url));
    }

    /**
     * Tests whether a missing base path is detected when checking for a fully initialized locator.
     */
    @Test
    public void testIsFullyInitializedNoBasePath() {
        final FileLocator locator = FileLocatorUtils.fileLocator().sourceURL(ConfigurationAssert.getTestURL(FILE_NAME)).fileName(FILE_NAME).create();
        assertFalse(FileLocatorUtils.isFullyInitialized(locator));
    }

    /**
     * Tests isFullyInitialized() for null input.
     */
    @Test
    public void testIsFullyInitializedNull() {
        assertFalse(FileLocatorUtils.isFullyInitialized(null));
    }

    /**
     * Tests isLocationDefined() if no location is defined.
     */
    @Test
    public void testIsLocationDefinedFalse() {
        final FileLocator locator = FileLocatorUtils.fileLocator().encoding(ENCODING).basePath(BASE_PATH).fileSystem(FileLocatorUtils.DEFAULT_FILE_SYSTEM)
            .create();
        assertFalse(FileLocatorUtils.isLocationDefined(locator));
    }

    /**
     * Tests isLocationDefined() if a file name is set.
     */
    @Test
    public void testIsLocationDefinedFileName() {
        final FileLocator locator = FileLocatorUtils.fileLocator().fileName(FILE_NAME).create();
        assertTrue(FileLocatorUtils.isLocationDefined(locator));
    }

    /**
     * Tests whether isLocationDefined() can handle null input.
     */
    @Test
    public void testIsLocationDefinedNull() {
        assertFalse(FileLocatorUtils.isLocationDefined(null));
    }

    /**
     * Tests isLocationDefined() if a URL is set.
     */
    @Test
    public void testIsLocationDefinedURL() {
        final FileLocator locator = FileLocatorUtils.fileLocator().sourceURL(ConfigurationAssert.getTestURL(FILE_NAME)).create();
        assertTrue(FileLocatorUtils.isLocationDefined(locator));
    }

    /**
     * Tests a locate() operation with a null locator.
     */
    @Test
    public void testLocateNullLocator() {
        assertNull(FileLocatorUtils.locate(null));
    }

    /**
     * Tests whether an exception is thrown for a failed locate() operation.
     */
    @Test
    public void testLocateOrThrowFailed() {
        final FileLocationStrategy strategy = mock(FileLocationStrategy.class);

        when(strategy.locate(any(), any())).thenReturn(null);

        final FileLocator locator = FileLocatorUtils.fileLocator().locationStrategy(strategy).create();
        assertThrows(ConfigurationException.class, () -> FileLocatorUtils.locateOrThrow(locator));
    }

    /**
     * Tests a successful locate() operation that uses defaults for location strategy and file system.
     */
    @Test
    public void testLocateSuccessWithDefaults() {
        final FileLocator locator = FileLocatorUtils.fileLocator().sourceURL(sourceURL).create();
        assertSame(sourceURL, FileLocatorUtils.locate(locator));
    }

    /**
     * Tests a successful locate() operation if the passed in locator contains a strategy and a file system.
     */
    @Test
    public void testLocateSuccessWithStrategyAndFileSystem() throws ConfigurationException {
        final FileSystem fs = mock(FileSystem.class);
        final FileLocationStrategy strategy = mock(FileLocationStrategy.class);
        final FileLocator locator = FileLocatorUtils.fileLocator().fileSystem(fs).locationStrategy(strategy).create();

        when(strategy.locate(fs, locator)).thenReturn(sourceURL);

        assertSame(sourceURL, FileLocatorUtils.locateOrThrow(locator));

        verify(strategy).locate(fs, locator);
        verifyNoMoreInteractions(strategy);
    }

    /**
     * Tests a successful locate() operation if the passed in locator contains a strategy, but no file system.
     */
    @Test
    public void testLocateSuccessWithStrategyDefaultFileSystem() throws ConfigurationException {
        final FileLocationStrategy strategy = mock(FileLocationStrategy.class);
        final FileLocator locator = FileLocatorUtils.fileLocator().locationStrategy(strategy).create();

        when(strategy.locate(FileLocatorUtils.DEFAULT_FILE_SYSTEM, locator)).thenReturn(sourceURL);

        assertSame(sourceURL, FileLocatorUtils.locateOrThrow(locator));

        verify(strategy).locate(FileLocatorUtils.DEFAULT_FILE_SYSTEM, locator);
        verifyNoMoreInteractions(strategy);
    }

    @Test
    public void testLocateWithNullTCCL() throws Exception {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(null);
            assertNull(FileLocatorUtils.locate(FileLocatorUtils.fileLocator().basePath("abase").fileName("aname").create()));
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    /**
     * Tests whether the default file system is returned if it is not set in a locator.
     */
    @Test
    public void testObtainFileSystemNotSetInLocator() {
        assertSame(FileLocatorUtils.DEFAULT_FILE_SYSTEM, FileLocatorUtils.getFileSystem(FileLocatorUtils.fileLocator().create()));
    }

    /**
     * Tests whether obtainFileSystem() can handle a null locator.
     */
    @Test
    public void testObtainFileSystemNullLocator() {
        assertSame(FileLocatorUtils.DEFAULT_FILE_SYSTEM, FileLocatorUtils.getFileSystem(null));
    }

    /**
     * Tests whether obtainFileSystem() returns the file system stored in the locator.
     */
    @Test
    public void testObtainFileSystemSetInLocator() {
        final FileSystem fs = mock(FileSystem.class);
        final FileLocator locator = FileLocatorUtils.fileLocator().fileSystem(fs).create();
        assertSame(fs, FileLocatorUtils.getFileSystem(locator));
    }

    /**
     * Tests whether a location strategy can be obtained if it is not defined by the locator.
     */
    @Test
    public void testObtainLocationStrategyNotSetInLocator() {
        final FileLocator locator = FileLocatorUtils.fileLocator().create();
        assertSame(FileLocatorUtils.DEFAULT_LOCATION_STRATEGY, FileLocatorUtils.getLocationStrategy(locator));
    }

    /**
     * Tests whether a location strategy can be obtained if a null locator is passed.
     */
    @Test
    public void testObtainLocationStrategyNullLocator() {
        assertSame(FileLocatorUtils.DEFAULT_LOCATION_STRATEGY, FileLocatorUtils.getLocationStrategy(null));
    }

    /**
     * Tests whether a location strategy can be obtained if it is defined by the locator.
     */
    @Test
    public void testObtainLocationStrategySetInLocator() {
        final FileLocationStrategy strategy = mock(FileLocationStrategy.class);
        final FileLocator locator = FileLocatorUtils.fileLocator().locationStrategy(strategy).create();
        assertSame(strategy, FileLocatorUtils.getLocationStrategy(locator));
    }

    /**
     * Tests whether put() deals with a null locator.
     */
    @Test
    public void testPutNoLocator() {
        final Map<String, Object> map = new HashMap<>();
        FileLocatorUtils.put(null, map);
        assertTrue(map.isEmpty());
    }

    /**
     * Tries to call put() without a map.
     */
    @Test
    public void testPutNoMap() {
        final FileLocator fileLocator = FileLocatorUtils.fileLocator().create();
        assertThrows(IllegalArgumentException.class, () -> FileLocatorUtils.put(fileLocator, null));
    }

    /**
     * Tests whether a file locator can be stored in a map and read again from there.
     */
    @Test
    public void testStoreFileLocatorInMap() {
        final FileLocationStrategy strategy = mock(FileLocationStrategy.class);
        final FileLocator locator = FileLocatorUtils.fileLocator().basePath(BASE_PATH).encoding(ENCODING).fileName(FILE_NAME).fileSystem(fileSystem)
            .locationStrategy(strategy).sourceURL(sourceURL).create();
        final Map<String, Object> map = new HashMap<>();
        FileLocatorUtils.put(locator, map);
        final FileLocator locator2 = FileLocatorUtils.fromMap(map);
        assertEquals(locator, locator2);
    }
}
