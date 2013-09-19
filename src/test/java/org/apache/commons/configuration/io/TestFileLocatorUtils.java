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
package org.apache.commons.configuration.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationAssert;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@code FileLocatorUtils}.
 *
 * @version $Id: $
 */
public class TestFileLocatorUtils
{
    /** Constant for a file name. */
    private static final String FILE_NAME = "test.xml";

    /** Constant for a base path. */
    private static final String BASE_PATH = "/etc/test/path/";

    /** Constant for a test encoding. */
    private static final String ENCODING = "utf-8";

    /** A test URL. */
    private static URL sourceURL;

    /** A test file system. */
    private static FileSystem fileSystem;

    @BeforeClass
    public static void setUpOnce() throws Exception
    {
        sourceURL = ConfigurationAssert.getTestURL(FILE_NAME);
        fileSystem = EasyMock.createMock(FileSystem.class);
        EasyMock.replay(fileSystem);
    }

    /**
     * Tests whether an encoded "%" character in the file name is handled correctly by
     * fileFromURL(). This test is related to CONFIGURATION-521.
     */
    @Test
    public void testFileFromURLWithEncodedPercent() throws MalformedURLException
    {
        File file = new File("https%3A%2F%2Fwww.apache.org%2F.url").getAbsoluteFile();
        URL fileURL = file.toURI().toURL();
        File file2 = FileLocatorUtils.fileFromURL(fileURL);
        assertEquals("Wrong file", file, file2);
    }

    /**
     * Tests whether a "+" character in the file name is handled correctly by
     * fileFromURL(). This test is related to CONFIGURATION-415.
     */
    @Test
    public void testFileFromURLWithPlus() throws MalformedURLException
    {
        File file = new File(new File("target"), "foo+bar.txt")
                .getAbsoluteFile();
        URL fileURL = file.toURI().toURL();
        File file2 = FileLocatorUtils.fileFromURL(fileURL);
        assertEquals("Wrong file", file, file2);
    }

    /**
     * Tests whether fileFromURL() handles null URLs correctly.
     */
    @Test
    public void testFileFromURLNull() throws Exception
    {
        assertNull("Wrong file for null URL", FileLocatorUtils
                .fileFromURL(null));
    }

    @Test
    public void testGetBasePath() throws Exception
    {
        URL url = new URL("http://xyz.net/foo/bar.xml");
        assertEquals("base path of " + url, "http://xyz.net/foo/", FileLocatorUtils.getBasePath(url));

        url = new URL("http://xyz.net/foo/");
        assertEquals("base path of " + url, "http://xyz.net/foo/", FileLocatorUtils.getBasePath(url));

        url = new URL("http://xyz.net/foo");
        assertEquals("base path of " + url, "http://xyz.net/", FileLocatorUtils.getBasePath(url));

        url = new URL("http://xyz.net/");
        assertEquals("base path of " + url, "http://xyz.net/", FileLocatorUtils.getBasePath(url));

        url = new URL("http://xyz.net");
        assertEquals("base path of " + url, "http://xyz.net", FileLocatorUtils.getBasePath(url));
    }

    @Test
    public void testGetFileName() throws Exception
    {
        assertEquals("file name for a null URL", null, FileLocatorUtils.getFileName(null));

        URL url = new URL("http://xyz.net/foo/");
        assertEquals("file for a directory URL " + url, null, FileLocatorUtils.getFileName(url));

        url = new URL("http://xyz.net/foo/bar.xml");
        assertEquals("file name for a valid URL " + url, "bar.xml", FileLocatorUtils.getFileName(url));
    }

    @Test
    public void testGetFile() throws Exception
    {
        File directory = new File("target");
        File reference = new File(directory, "test.txt").getAbsoluteFile();

        assertEquals(reference, FileLocatorUtils.getFile(null, reference.getAbsolutePath()));
        assertEquals(reference, FileLocatorUtils.getFile(directory.getAbsolutePath(), reference.getAbsolutePath()));
        assertEquals(reference, FileLocatorUtils.getFile(directory.getAbsolutePath(), reference.getName()));
        assertEquals(reference, FileLocatorUtils.getFile(directory.toURI().toURL().toString(), reference.getName()));
        assertEquals(reference, FileLocatorUtils.getFile("invalid", reference.toURI().toURL().toString()));
        assertEquals(reference, FileLocatorUtils.getFile(
                "jar:file:/C:/myjar.jar!/my-config.xml/someprops.properties",
                reference.getAbsolutePath()));
    }

    @Test
    public void testLocateWithNullTCCL() throws Exception
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(null);
            assertNull(FileLocatorUtils.locate(new DefaultFileSystem(), "abase", "aname"));
            // This assert fails when maven 2 is used, so commented out
            //assertNotNull(FileLocatorUtils.locate("test.xml"));
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    /**
     * Tests whether obtainFileSystem() can handle a null locator.
     */
    @Test
    public void testObtainFileSystemNullLocator()
    {
        assertSame("Wrong file system", FileLocatorUtils.DEFAULT_FILE_SYSTEM,
                FileLocatorUtils.obtainFileSystem(null));
    }

    /**
     * Tests whether the default file system is returned if it is not set in a
     * locator.
     */
    @Test
    public void testObtainFileSystemNotSetInLocator()
    {
        assertSame("Wrong file system", FileLocatorUtils.DEFAULT_FILE_SYSTEM,
                FileLocatorUtils.obtainFileSystem(FileLocatorUtils
                        .fileLocator().create()));
    }

    /**
     * Tests whether obtainFileSystem() returns the file system stored in the
     * locator.
     */
    @Test
    public void testObtainFileSystemSetInLocator()
    {
        FileSystem fs = EasyMock.createMock(FileSystem.class);
        FileLocator locator =
                FileLocatorUtils.fileLocator().fileSystem(fs).create();
        assertSame("Wrong file system", fs,
                FileLocatorUtils.obtainFileSystem(locator));
    }

    /**
     * Tests whether isLocationDefined() can handle null input.
     */
    @Test
    public void testIsLocationDefinedNull()
    {
        assertFalse("Wrong result", FileLocatorUtils.isLocationDefined(null));
    }

    /**
     * Tests isLocationDefined() if no location is defined.
     */
    @Test
    public void testIsLocationDefinedFalse()
    {
        FileLocator locator =
                FileLocatorUtils.fileLocator().encoding(ENCODING)
                        .basePath(BASE_PATH)
                        .fileSystem(FileLocatorUtils.DEFAULT_FILE_SYSTEM)
                        .create();
        assertFalse("Wrong result", FileLocatorUtils.isLocationDefined(locator));
    }

    /**
     * Tests isLocationDefined() if a file name is set.
     */
    @Test
    public void testIsLocationDefinedFileName()
    {
        FileLocator locator =
                FileLocatorUtils.fileLocator().fileName(FILE_NAME).create();
        assertTrue("Wrong result", FileLocatorUtils.isLocationDefined(locator));
    }

    /**
     * Tests isLocationDefined() if a URL is set.
     */
    @Test
    public void testIsLocationDefinedURL()
    {
        FileLocator locator =
                FileLocatorUtils.fileLocator()
                        .sourceURL(ConfigurationAssert.getTestURL(FILE_NAME))
                        .create();
        assertTrue("Wrong result", FileLocatorUtils.isLocationDefined(locator));
    }

    /**
     * Tries to obtain a fully initialized locator if the source locator is not
     * defined.
     */
    @Test
    public void testFullyInitializedLocatorUndefined()
    {
        assertNull("Got a result",
                FileLocatorUtils.fullyInitializedLocator(FileLocatorUtils
                        .fileLocator().create()));
    }

    /**
     * Checks whether the expected test configuration can be loaded using the
     * specified handler.
     *
     * @param handler the file handler
     * @throws ConfigurationException if an error occurs
     */
    private static void checkTestConfiguration(FileHandler handler)
            throws ConfigurationException
    {
        XMLConfiguration config = new XMLConfiguration();
        FileHandler h2 = new FileHandler(config, handler);
        h2.load();
        assertEquals("Wrong content", "value", config.getString("element"));
    }

    /**
     * Checks whether the specified locator points to the expected test
     * configuration file.
     *
     * @param locator the locator to check
     * @throws ConfigurationException if an error occurs
     */
    private static void checkFullyInitializedLocator(FileLocator locator)
            throws ConfigurationException
    {
        assertNotNull("No base path", locator.getBasePath());
        assertNotNull("No file name", locator.getFileName());
        assertNotNull("No source URL", locator.getSourceURL());

        FileHandler handler = new FileHandler();
        handler.setBasePath(locator.getBasePath());
        handler.setFileName(locator.getFileName());
        checkTestConfiguration(handler);

        handler = new FileHandler();
        handler.setURL(locator.getSourceURL());
        checkTestConfiguration(handler);
    }

    /**
     * Tests whether a fully initialized locator can be obtained if a file name
     * is available.
     */
    @Test
    public void testFullyInitializedLocatorFileName()
            throws ConfigurationException
    {
        FileLocator locator =
                FileLocatorUtils.fileLocator().fileName(FILE_NAME).create();
        checkFullyInitializedLocator(FileLocatorUtils
                .fullyInitializedLocator(locator));
    }

    /**
     * Tests whether a fully initialized locator can be obtained if a URL is
     * available.
     */
    @Test
    public void testFullyInitializedLocatorURL() throws ConfigurationException
    {
        FileLocator locator =
                FileLocatorUtils.fileLocator().sourceURL(sourceURL).create();
        checkFullyInitializedLocator(FileLocatorUtils
                .fullyInitializedLocator(locator));
    }

    /**
     * Tests fullyInitializedLocator() if the locator is already fully
     * initialized.
     */
    @Test
    public void testFullyInitializedLocatorAlreadyComplete()
    {
        FileLocator locator =
                FileLocatorUtils.fileLocator().fileName(FILE_NAME).create();
        FileLocator fullLocator =
                FileLocatorUtils.fullyInitializedLocator(locator);
        assertSame("Different instance", fullLocator,
                FileLocatorUtils.fullyInitializedLocator(fullLocator));
    }

    /**
     * Tests fullyInitializedLocator() if a locate() operation fails.
     */
    @Test
    public void testFullyInitializedLocatorLocateFails()
    {
        FileLocator locator =
                FileLocatorUtils.fileLocator().fileName("non existing file")
                        .create();
        assertSame("Wrong result", locator,
                FileLocatorUtils.fullyInitializedLocator(locator));
    }

    /**
     * Tests isFullyInitialized() for null input.
     */
    @Test
    public void testIsFullyInitializedNull()
    {
        assertFalse("Wrong result", FileLocatorUtils.isFullyInitialized(null));
    }

    /**
     * Tests whether a missing base path is detected when checking for a fully
     * initialized locator.
     */
    @Test
    public void testIsFullyInitializedNoBasePath()
    {
        FileLocator locator =
                FileLocatorUtils.fileLocator()
                        .sourceURL(ConfigurationAssert.getTestURL(FILE_NAME))
                        .fileName(FILE_NAME).create();
        assertFalse("Wrong result",
                FileLocatorUtils.isFullyInitialized(locator));
    }
}
