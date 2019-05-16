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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.io.VFSFileSystem;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.easymock.EasyMock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case for the VFSFileHandlerReloadingDetector class.
 *
 * @author Ralph Goers
 */
public class TestVFSFileHandlerReloadingDetector
{
    /** Constant for the name of the test property. */
    private static final String PROPERTY = "string";

    /** Constant for the XML fragment to be written. */
    private static final String FMT_XML = "<configuration><" + PROPERTY
            + ">%s</" + PROPERTY + "></configuration>";

    /** A helper object for creating temporary files. */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Writes a test configuration file containing a single property with the
     * given value.
     *
     * @param file the file to be written
     * @param value the value of the test property
     * @throws IOException if an error occurs
     */
    private void writeTestFile(final File file, final String value) throws IOException
    {
        final FileWriter out = new FileWriter(file);
        out.write(String.format(FMT_XML, value));
        out.close();
    }

    /**
     * Tests whether the last modification date of an existing file can be
     * obtained.
     */
    @Test
    public void testLastModificationDateExisting() throws IOException
    {
        final File file = folder.newFile();
        writeTestFile(file, "value1");
        final VFSFileHandlerReloadingDetector strategy =
                new VFSFileHandlerReloadingDetector();
        strategy.getFileHandler().setFile(file);
        strategy.getFileHandler().setFileSystem(new VFSFileSystem());
        final long modificationDate = strategy.getLastModificationDate();
        assertEquals("Wrong modification date", file.lastModified(),
                modificationDate);
    }

    /**
     * Tests whether a non existing file is handled correctly.
     */
    @Test
    public void testLastModificationDateNonExisting()
    {
        final File file = ConfigurationAssert.getOutFile("NonExistingFile.xml");
        final FileHandler handler = new FileHandler();
        handler.setFileSystem(new VFSFileSystem());
        handler.setFile(file);
        final VFSFileHandlerReloadingDetector strategy =
                new VFSFileHandlerReloadingDetector(handler);
        assertEquals("Got a modification date", 0,
                strategy.getLastModificationDate());
    }

    /**
     * Tests whether an undefined file handler is handler correctly.
     */
    @Test
    public void testLastModificationDateUndefinedHandler()
    {
        final VFSFileHandlerReloadingDetector strategy =
                new VFSFileHandlerReloadingDetector();
        assertEquals("Got a modification date", 0,
                strategy.getLastModificationDate());
    }

    /**
     * Tests whether a file system exception is handled when accessing the file
     * object.
     */
    @Test
    public void testLastModificationDateFileSystemEx()
            throws FileSystemException
    {
        final FileObject fo = EasyMock.createMock(FileObject.class);
        final FileName name = EasyMock.createMock(FileName.class);
        EasyMock.expect(fo.exists()).andReturn(Boolean.TRUE);
        EasyMock.expect(fo.getContent()).andThrow(
                new FileSystemException("error"));
        EasyMock.expect(fo.getName()).andReturn(name);
        EasyMock.expect(name.getURI()).andReturn("someURI");
        EasyMock.replay(fo, name);
        final VFSFileHandlerReloadingDetector strategy =
                new VFSFileHandlerReloadingDetector()
                {
                    @Override
                    protected FileObject getFileObject()
                    {
                        return fo;
                    }
                };
        assertEquals("Got a modification date", 0,
                strategy.getLastModificationDate());
        EasyMock.verify(fo);
    }

    /**
     * Tests a URI which cannot be resolved.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testLastModificationDateUnresolvableURI()
    {
        final VFSFileHandlerReloadingDetector strategy =
                new VFSFileHandlerReloadingDetector()
                {
                    @Override
                    protected String resolveFileURI()
                    {
                        return null;
                    }
                };
        strategy.getFileHandler().setFileSystem(new VFSFileSystem());
        strategy.getFileHandler().setFileName("test.xml");
        strategy.getLastModificationDate();
    }

    /**
     * Tests whether the refresh delay is correctly passed to the base class.
     */
    @Test
    public void testGetRefreshDelay() throws Exception
    {
        final long delay = 20130325L;
        final VFSFileHandlerReloadingDetector strategy =
                new VFSFileHandlerReloadingDetector(null, delay);
        assertNotNull("No file handler was created", strategy.getFileHandler());
        assertEquals("Wrong refresh delay", delay, strategy.getRefreshDelay());
    }
}
