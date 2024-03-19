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

import static org.apache.commons.configuration2.TempDirUtils.newFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.VFSFileSystem;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for the VFSFileHandlerReloadingDetector class.
 */
public class TestVFSFileHandlerReloadingDetector {
    /** Constant for the name of the test property. */
    private static final String PROPERTY = "string";

    /** Constant for the XML fragment to be written. */
    private static final String FMT_XML = "<configuration><" + PROPERTY + ">%s</" + PROPERTY + "></configuration>";

    /** A folder for temporary files. */
    @TempDir
    public File tempFolder;

    /**
     * Tests whether the refresh delay is correctly passed to the base class.
     */
    @Test
    public void testGetRefreshDelay() throws Exception {
        final long delay = 20130325L;
        final VFSFileHandlerReloadingDetector strategy = new VFSFileHandlerReloadingDetector(null, delay);
        assertNotNull(strategy.getFileHandler());
        assertEquals(delay, strategy.getRefreshDelay());
    }

    /**
     * Tests whether the last modification date of an existing file can be obtained.
     */
    @Test
    public void testLastModificationDateExisting() throws IOException {
        final File file = newFile(tempFolder);
        writeTestFile(file, "value1");
        final VFSFileHandlerReloadingDetector strategy = new VFSFileHandlerReloadingDetector();
        strategy.getFileHandler().setFile(file);
        strategy.getFileHandler().setFileSystem(new VFSFileSystem());
        final long modificationDate = strategy.getLastModificationDate();
        // Workaround OpenJDK 8 and 9 bug JDK-8177809
        // https://bugs.openjdk.java.net/browse/JDK-8177809
        final long expectedMillis = Files.getLastModifiedTime(file.toPath()).toMillis();
        assertEquals(expectedMillis, modificationDate);
    }

    /**
     * Tests whether a file system exception is handled when accessing the file object.
     */
    @Test
    public void testLastModificationDateFileSystemEx() throws FileSystemException {
        final FileObject fo = mock(FileObject.class);
        final FileName name = mock(FileName.class);

        when(fo.exists()).thenReturn(Boolean.TRUE);
        when(fo.getContent()).thenThrow(new FileSystemException("error"));
        when(fo.getName()).thenReturn(name);
        when(name.getURI()).thenReturn("someURI");

        final VFSFileHandlerReloadingDetector strategy = new VFSFileHandlerReloadingDetector() {
            @Override
            protected FileObject getFileObject() {
                return fo;
            }
        };
        assertEquals(0, strategy.getLastModificationDate());

        verify(fo).exists();
        verify(fo).getContent();
        verify(fo).getName();
        verify(name).getURI();
        verifyNoMoreInteractions(fo, name);
    }

    /**
     * Tests whether a non existing file is handled correctly.
     */
    @Test
    public void testLastModificationDateNonExisting() {
        final File file = ConfigurationAssert.getOutFile("NonExistingFile.xml");
        final FileHandler handler = new FileHandler();
        handler.setFileSystem(new VFSFileSystem());
        handler.setFile(file);
        final VFSFileHandlerReloadingDetector strategy = new VFSFileHandlerReloadingDetector(handler);
        assertEquals(0, strategy.getLastModificationDate());
    }

    /**
     * Tests whether an undefined file handler is handler correctly.
     */
    @Test
    public void testLastModificationDateUndefinedHandler() {
        final VFSFileHandlerReloadingDetector strategy = new VFSFileHandlerReloadingDetector();
        assertEquals(0, strategy.getLastModificationDate());
    }

    /**
     * Tests a URI which cannot be resolved.
     */
    @Test
    public void testLastModificationDateUnresolvableURI() {
        final VFSFileHandlerReloadingDetector strategy = new VFSFileHandlerReloadingDetector() {
            @Override
            protected String resolveFileURI() {
                return null;
            }
        };
        strategy.getFileHandler().setFileSystem(new VFSFileSystem());
        strategy.getFileHandler().setFileName("test.xml");
        assertThrows(ConfigurationRuntimeException.class, strategy::getLastModificationDate);
    }

    /**
     * Writes a test configuration file containing a single property with the given value.
     *
     * @param file the file to be written
     * @param value the value of the test property
     * @throws IOException if an error occurs
     */
    private void writeTestFile(final File file, final String value) throws IOException {
        try (FileWriter out = new FileWriter(file)) {
            out.write(String.format(FMT_XML, value));
        }
    }
}
