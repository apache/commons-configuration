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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code AbsoluteNameLocationStrategy}.
 *
 */
public class TestAbsoluteNameLocationStrategy {
    /** A mock for the file system. */
    private FileSystem fileSystem;

    /** The strategy to be tested. */
    private AbsoluteNameLocationStrategy strategy;

    @BeforeEach
    public void setUp() throws Exception {
        fileSystem = mock(FileSystem.class);
        strategy = new AbsoluteNameLocationStrategy();
    }

    /**
     * Tests a successful locate() operation.
     */
    @Test
    public void testExistingAbsoluteFile() {
        final File file = ConfigurationAssert.getTestFile("test.xml");
        final FileLocator locator = FileLocatorUtils.fileLocator().fileName(file.getAbsolutePath()).create();
        final URL url = strategy.locate(fileSystem, locator);
        assertEquals(file.getAbsoluteFile(), FileLocatorUtils.fileFromURL(url).getAbsoluteFile());
    }

    /**
     * Tests a locate() operation if no absolute file name is provided.
     */
    @Test
    public void testNoAbsoluteFileName() {
        final FileLocator locator = FileLocatorUtils.fileLocator().fileName("test.xml").create();
        assertNull(strategy.locate(fileSystem, locator));
    }

    /**
     * Tests a locate() operation if no file name is provided.
     */
    @Test
    public void testNoFileName() {
        final FileLocator locator = FileLocatorUtils.fileLocator().create();
        assertNull(strategy.locate(fileSystem, locator));
    }

    /**
     * Tests a locate() operation if an absolute file name is provided, but this file does not exist.
     */
    @Test
    public void testNonExistingAbsoluteFile() {
        final File file = ConfigurationAssert.getOutFile("NotExistingFile.tst");
        final FileLocator locator = FileLocatorUtils.fileLocator().fileName(file.getAbsolutePath()).create();
        assertNull(strategy.locate(fileSystem, locator));
    }
}
