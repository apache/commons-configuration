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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationAssert;
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
    private static final String FILE_NAME = "testFile.dat";

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
        sourceURL = ConfigurationAssert.getTestURL("test.xml");
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
     * Tests whether an undefined file locator can be created.
     */
    @Test
    public void testCreateFileLocatorUndefined()
    {
        FileLocator locator = FileLocatorUtils.fileLocator().create();
        assertNull("Got a base path", locator.getBasePath());
        assertNull("Got a file name", locator.getFileName());
        assertNull("Got a URL", locator.getSourceURL());
        assertNull("Got an encoding", locator.getEncoding());
        assertNull("Got a file system", locator.getFileSystem());
    }

    /**
     * Tests whether a locator has the expected properties.
     *
     * @param locator the locator to check
     */
    private static void checkLocator(FileLocator locator)
    {
        assertEquals("Wrong base path", BASE_PATH, locator.getBasePath());
        assertEquals("Wrong file name", FILE_NAME, locator.getFileName());
        assertEquals("Wrong encoding", ENCODING, locator.getEncoding());
        assertEquals("Wrong URL", sourceURL.toExternalForm(), locator
                .getSourceURL().toExternalForm());
        assertSame("Wrong file system", fileSystem, locator.getFileSystem());
    }

    /**
     * Tests the creation of a file locator.
     */
    @Test
    public void testCreateFileLocator()
    {
        FileLocator locator =
                FileLocatorUtils.fileLocator().basePath(BASE_PATH)
                        .fileName(FILE_NAME).encoding(ENCODING)
                        .fileSystem(fileSystem).sourceURL(sourceURL).create();
        checkLocator(locator);
    }

    /**
     * Tests whether a file locator can be created from a source locator.
     */
    @Test
    public void testCreateFileLocatorFromSource()
    {
        FileLocator locatorSrc =
                FileLocatorUtils.fileLocator().basePath(BASE_PATH)
                        .fileName("someFile").encoding(ENCODING)
                        .fileSystem(fileSystem).sourceURL(sourceURL).create();
        FileLocator locator =
                FileLocatorUtils.fileLocator(locatorSrc).fileName(FILE_NAME)
                        .create();
        checkLocator(locator);
    }

    /**
     * Tests the equals() implementation of FileLocator if the expected result
     * is true.
     */
    @Test
    public void testFileLocatorEqualsTrue()
    {
        FileLocator loc1 = FileLocatorUtils.fileLocator().create();
        ConfigurationAssert.checkEquals(loc1, loc1, true);
        FileLocator loc2 = FileLocatorUtils.fileLocator().create();
        ConfigurationAssert.checkEquals(loc1, loc2, true);
        loc1 =
                FileLocatorUtils.fileLocator().basePath(BASE_PATH)
                        .fileName(FILE_NAME).encoding(ENCODING)
                        .fileSystem(fileSystem).sourceURL(sourceURL).create();
        loc2 =
                FileLocatorUtils.fileLocator().basePath(BASE_PATH)
                        .fileName(FILE_NAME).encoding(ENCODING)
                        .fileSystem(fileSystem).sourceURL(sourceURL).create();
        ConfigurationAssert.checkEquals(loc1, loc2, true);
    }

    /**
     * Tests the equals() implementation of FileLocator if the expected result
     * is false.
     */
    @Test
    public void testFileLocatorEqualsFalse()
    {
        FileLocator loc1 =
                FileLocatorUtils.fileLocator().basePath(BASE_PATH)
                        .fileName(FILE_NAME).encoding(ENCODING)
                        .fileSystem(fileSystem).sourceURL(sourceURL).create();
        FileLocator loc2 =
                FileLocatorUtils.fileLocator(loc1)
                        .basePath(BASE_PATH + "_other").create();
        ConfigurationAssert.checkEquals(loc1, loc2, false);
        loc2 =
                FileLocatorUtils.fileLocator(loc1)
                        .fileName(FILE_NAME + "_other").create();
        ConfigurationAssert.checkEquals(loc1, loc2, false);
        loc2 =
                FileLocatorUtils.fileLocator(loc1)
                        .encoding(ENCODING + "_other").create();
        ConfigurationAssert.checkEquals(loc1, loc2, false);
        loc2 =
                FileLocatorUtils.fileLocator(loc1)
                        .fileSystem(EasyMock.createMock(FileSystem.class))
                        .create();
        ConfigurationAssert.checkEquals(loc1, loc2, false);
        loc2 =
                FileLocatorUtils
                        .fileLocator(loc1)
                        .sourceURL(
                                ConfigurationAssert
                                        .getTestURL("test.properties"))
                        .create();
        ConfigurationAssert.checkEquals(loc1, loc2, false);
    }

    /**
     * Tests equals() with a null object.
     */
    @Test
    public void testFileLocatorEqualsNull()
    {
        FileLocator loc =
                FileLocatorUtils.fileLocator().fileName(FILE_NAME).create();
        assertFalse("Wrong result", loc.equals(null));
    }

    /**
     * Tests equals() with an object from another class.
     */
    @Test
    public void testFileLocatorEqualsOtherClass()
    {
        FileLocator loc =
                FileLocatorUtils.fileLocator().fileName(FILE_NAME).create();
        assertFalse("Wrong result", loc.equals(this));
    }

    /**
     * Tests the string representation of a locator.
     */
    @Test
    public void testFileLocatorToString()
    {
        FileLocator loc =
                FileLocatorUtils.fileLocator().basePath(BASE_PATH)
                        .fileName(FILE_NAME).encoding(ENCODING)
                        .fileSystem(fileSystem).sourceURL(sourceURL).create();
        String s = loc.toString();
        assertThat(s, containsString("fileName=" + FILE_NAME));
        assertThat(s, containsString("basePath=" + BASE_PATH));
        assertThat(s, containsString("sourceURL=" + sourceURL));
        assertThat(s, containsString("encoding=" + ENCODING));
        assertThat(s, containsString("fileSystem=" + fileSystem));
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
                        .sourceURL(ConfigurationAssert.getTestURL("test.xml"))
                        .create();
        assertTrue("Wrong result", FileLocatorUtils.isLocationDefined(locator));
    }
}
