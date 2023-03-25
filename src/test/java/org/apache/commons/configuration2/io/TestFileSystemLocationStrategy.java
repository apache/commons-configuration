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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.URL;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code FileSystemLocationStrategy}.
 */
public class TestFileSystemLocationStrategy {
    /** The strategy to be tested. */
    private FileSystemLocationStrategy strategy;

    @BeforeEach
    public void setUp() throws Exception {
        strategy = new FileSystemLocationStrategy();
    }

    /**
     * Tests a locate() operation.
     */
    @Test
    public void testLocate() {
        final FileSystem fs = mock(FileSystem.class);
        final URL url = ConfigurationAssert.getTestURL("test.xml");
        final String basePath = "testBasePath";
        final String fileName = "testFileName.txt";

        when(fs.locateFromURL(basePath, fileName)).thenReturn(url);

        final FileLocator locator = FileLocatorUtils.fileLocator().basePath(basePath).fileName(fileName).fileSystem(FileLocatorUtils.DEFAULT_FILE_SYSTEM)
            .sourceURL(ConfigurationAssert.getTestURL("test.properties")).create();

        assertSame(url, strategy.locate(fs, locator));

        verify(fs).locateFromURL(basePath, fileName);
        verifyNoMoreInteractions(fs);
    }
}
