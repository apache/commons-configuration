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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code BasePathLocationStrategy}.
 *
 */
public class TestBasePathLocationStrategy
{
    /** Constant for the name of the test file. */
    private static final String TEST_FILE = "test.xml";

    /** A mock for the file system. */
    private FileSystem fileSystem;

    /** The strategy to be tested. */
    private BasePathLocationStrategy strategy;

    @Before
    public void setUp() throws Exception
    {
        fileSystem = EasyMock.createMock(FileSystem.class);
        EasyMock.replay(fileSystem);
        strategy = new BasePathLocationStrategy();
    }

    /**
     * Checks whether the passed in URL points to the expected test file.
     *
     * @param url the URL to be checked
     */
    private static void checkURL(final URL url)
    {
        assertEquals("Wrong URL", FileLocatorUtils.fileFromURL(url)
                .getAbsoluteFile(), ConfigurationAssert.getTestFile(TEST_FILE)
                .getAbsoluteFile());
    }

    /**
     * Tests a successful locate() operation with a valid base path and file
     * name.
     */
    @Test
    public void testLocateSuccess()
    {
        final File path = ConfigurationAssert.TEST_DIR;
        final FileLocator locator =
                FileLocatorUtils.fileLocator().basePath(path.getAbsolutePath())
                        .fileName(TEST_FILE).create();
        checkURL(strategy.locate(fileSystem, locator));
    }

    /**
     * Tests whether a prefix for relative file names is handled correctly.
     */
    @Test
    public void testLocateSuccessRelativePrefix()
    {
        final File path = ConfigurationAssert.TEST_DIR;
        final FileLocator locator =
                FileLocatorUtils.fileLocator().basePath(path.getAbsolutePath())
                        .fileName("." + File.separator + TEST_FILE).create();
        checkURL(strategy.locate(fileSystem, locator));
    }

    /**
     * Tests a locate() operation if no file name is provided.
     */
    @Test
    public void testNullFileName()
    {
        final FileLocator locator =
                FileLocatorUtils
                        .fileLocator()
                        .basePath(
                                ConfigurationAssert.getTestFile(TEST_FILE)
                                        .getAbsolutePath()).create();
        assertNull("Got a URL", strategy.locate(fileSystem, locator));
    }

    /**
     * Tests whether a null base path is handled correctly.
     */
    @Test
    public void testNullBasePath()
    {
        final FileLocator locator =
                FileLocatorUtils.fileLocator().fileName(TEST_FILE).create();
        assertNull("Got a URL", strategy.locate(fileSystem, locator));
    }
}
