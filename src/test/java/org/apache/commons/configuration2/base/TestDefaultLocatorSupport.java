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
package org.apache.commons.configuration2.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.ConfigurationException;
import org.apache.commons.configuration2.fs.FileSystem;
import org.apache.commons.configuration2.fs.Locator;
import org.apache.commons.configuration2.fs.URLLocator;
import org.apache.commons.configuration2.fs.VFSFileSystem;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@code DefaultLocatorSupport}.
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class TestDefaultLocatorSupport
{
    /** Constant defining some test data to be loaded or stored. */
    private static final String TEST_DATA = "Test data for TestDefaultLocatorSupport";

    /** Constant for the name of the test file. */
    private static final String TEST_FILE = "TestDefaultLocatorSupport.txt";

    /** Constant for the encoding. */
    private static final String ENCODING = "UTF8";

    /** The input locator to the test file. */
    private static Locator inputLocator;

    /** The output locator for the test file. */
    private static Locator outputLocator;

    /** The stream based source used for testing. */
    private StreamBasedSourceTestImpl source;

    /** The object to be tested. */
    private DefaultLocatorSupport locatorSupport;

    /** A test file. */
    private File testFile;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        URL url = ConfigurationAssert.getOutURL(TEST_FILE);
        URL urlNonExisting = ConfigurationAssert
                .getOutURL("NonExistingFile.txt");
        inputLocator = new URLLocator(url, urlNonExisting);
        outputLocator = new URLLocator(urlNonExisting, url);
    }

    @Before
    public void setUp() throws Exception
    {
        testFile = ConfigurationAssert.getOutFile(TEST_FILE);
        source = new StreamBasedSourceTestImpl();
        locatorSupport = new DefaultLocatorSupport(source);
    }

    /**
     * Performs cleanup. Removes the test file if it exists.
     */
    @After
    public void tearDown() throws Exception
    {
        if (testFile != null && testFile.exists())
        {
            assertTrue("Cannot remove test file", testFile.delete());
        }
    }

    /**
     * Helper method for reading the content of a reader into a string.
     *
     * @param reader the reader to read
     * @return the content of the reader as string
     * @throws IOException if an I/O error occurs
     */
    private static String read(Reader reader) throws IOException
    {
        StringBuilder buf = new StringBuilder();
        BufferedReader in = new BufferedReader(reader);
        String line;
        while ((line = in.readLine()) != null)
        {
            buf.append(line);
        }
        return buf.toString();
    }

    /**
     * Helper method for writing test data into the given writer.
     *
     * @param writer the writer
     * @throws IOException if an I/O error occurs
     */
    private static void write(Writer writer) throws IOException
    {
        PrintWriter out = new PrintWriter(writer);
        out.print(TEST_DATA);
        out.flush();
    }

    /**
     * Helper method for checking the encoding that was passed to the source.
     *
     * @param expEnc the expected encoding
     */
    private void checkEncoding(String expEnc)
    {
        Charset expected = (expEnc == null) ? Charset.defaultCharset()
                : Charset.forName(expEnc);
        Charset actual = Charset.forName(source.encoding);
        assertEquals("Wrong encoding", expected, actual);
    }

    /**
     * Tests whether the test file was correctly written.
     *
     * @throws IOException if an error occurs
     */
    private void checkTestFile() throws IOException
    {
        FileReader in = new FileReader(testFile);
        try
        {
            assertEquals("Wrong content of file", TEST_DATA, read(in));
        }
        finally
        {
            in.close();
        }
    }

    /**
     * Creates a test file with test data.
     *
     * @throws IOException if an I/O error occurs
     */
    private void createTestFile() throws IOException
    {
        FileWriter writer = new FileWriter(testFile);
        try
        {
            write(writer);
        }
        finally
        {
            writer.close();
        }
    }

    /**
     * Tries to create an instance without a source. This should cause an
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoSource()
    {
        new DefaultLocatorSupport(null);
    }

    /**
     * Tests a newly created instance.
     */
    @Test
    public void testInit()
    {
        assertNull("Got an encoding", locatorSupport.getEncoding());
        assertNull("Got a locator", locatorSupport.getLocator());
        assertEquals("Wrong file system", FileSystem.getDefaultFileSystem(),
                locatorSupport.getFileSystem());
    }

    /**
     * Tests whether a file system can be set.
     */
    @Test
    public void testSetFileSystem()
    {
        VFSFileSystem fs = new VFSFileSystem();
        locatorSupport.setFileSystem(fs);
        assertEquals("FileSystem not changed", fs, locatorSupport
                .getFileSystem());
    }

    /**
     * Tries to set a null file system. This should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetFileSystemNull()
    {
        locatorSupport.setFileSystem(null);
    }

    /**
     * Tests whether the file system can be reset to the default one.
     */
    @Test
    public void testResetFileSystem()
    {
        locatorSupport.setFileSystem(new VFSFileSystem());
        locatorSupport.resetFileSystem();
        assertEquals("Wrong file system", FileSystem.getDefaultFileSystem(),
                locatorSupport.getFileSystem());
    }

    /**
     * Tests the close() method if an exception occurs.
     */
    @Test
    public void testCloseEx() throws IOException
    {
        Closeable c = EasyMock.createMock(Closeable.class);
        c.close();
        IOException ioex = new IOException();
        EasyMock.expectLastCall().andThrow(ioex);
        EasyMock.replay(c);
        try
        {
            DefaultLocatorSupport.close(c);
            fail("Exception not thrown!");
        }
        catch (ConfigurationException cex)
        {
            assertEquals("Wrong cause", ioex, cex.getCause());
            EasyMock.verify(c);
        }
    }

    /**
     * Helper method for testing the results of a load operation.
     *
     * @param expEnc the expected encoding
     */
    private void checkLoad(String expEnc)
    {
        assertEquals("Wrong data", TEST_DATA, source.loadData);
        checkEncoding(expEnc);
    }

    /**
     * Tests the load() method if no locator was set. This should cause an
     * exception.
     */
    @Test(expected = IllegalStateException.class)
    public void testLoadNoLocator() throws ConfigurationException
    {
        locatorSupport.load();
    }

    /**
     * Tests loading data from the default locator.
     */
    @Test
    public void testLoadDefLocator() throws IOException, ConfigurationException
    {
        createTestFile();
        locatorSupport.setLocator(inputLocator);
        locatorSupport.load();
        checkLoad(null);
    }

    /**
     * Tests whether data from a specific locator can be loaded.
     */
    @Test
    public void testLoadSpecificLocator() throws IOException,
            ConfigurationException
    {
        Locator defloc = EasyMock.createMock(Locator.class);
        EasyMock.replay(defloc);
        locatorSupport.setLocator(defloc);
        createTestFile();
        locatorSupport.load(inputLocator);
        checkLoad(null);
        EasyMock.verify(defloc);
    }

    /**
     * Tests whether the encoding is taken into account when loading from a
     * locator.
     */
    @Test
    public void testLoadLocatorEnc() throws IOException, ConfigurationException
    {
        createTestFile();
        locatorSupport.setEncoding(ENCODING);
        locatorSupport.setLocator(inputLocator);
        locatorSupport.load();
        checkLoad(ENCODING);
    }

    /**
     * Tries to load data from a null locator. This should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLoadLocatorNull() throws ConfigurationException
    {
        locatorSupport.load((Locator) null);
    }

    /**
     * Tests whether exceptions are correctly handled when loading data from a
     * locator.
     */
    @Test
    public void testLoadLocatorEx() throws IOException, ConfigurationException
    {
        createTestFile();
        source.ex = new IOException();
        try
        {
            locatorSupport.load(inputLocator);
            fail("Exception not thrown!");
        }
        catch (ConfigurationException cex)
        {
            assertEquals("Wrong cause", source.ex, cex.getCause());
        }
    }

    /**
     * Tests whether a stream can be loaded if the encoding is specified.
     */
    @Test
    public void testLoadStreamEnc() throws ConfigurationException, IOException
    {
        createTestFile();
        FileInputStream in = new FileInputStream(testFile);
        try
        {
            locatorSupport.load(in, ENCODING);
            checkLoad(ENCODING);
        }
        finally
        {
            in.close();
        }
    }

    /**
     * Tests whether a stream can be loaded with the default encoding.
     */
    @Test
    public void testLoadStreamDefEnc() throws IOException,
            ConfigurationException
    {
        createTestFile();
        locatorSupport.setEncoding(ENCODING);
        FileInputStream in = new FileInputStream(testFile);
        try
        {
            locatorSupport.load(in);
            checkLoad(ENCODING);
        }
        finally
        {
            in.close();
        }
    }

    /**
     * Tries to load data using an unsupported encoding.
     */
    @Test
    public void testLoadStreamUnsupportedEnc() throws IOException,
            ConfigurationException
    {
        createTestFile();
        locatorSupport.setEncoding("an unsupported encoding!");
        FileInputStream in = new FileInputStream(testFile);
        try
        {
            locatorSupport.load(in);
            fail("Unsupported encoding not detected!");
        }
        catch (ConfigurationException cex)
        {
            // ok
        }
        finally
        {
            in.close();
        }
    }

    /**
     * Tests whether IOExceptions thrown by the source on reading are converted
     * to configuration exceptions.
     */
    @Test
    public void testLoadReaderEx()
    {
        source.ex = new IOException();
        try
        {
            locatorSupport.load(new StringReader(TEST_DATA));
            fail("Exception not thrown!");
        }
        catch (ConfigurationException cex)
        {
            assertEquals("Wrong cause", source.ex, cex.getCause());
        }
    }

    /**
     * Tests whether IOExceptions thrown by the source on writing are converted
     * to configuration exceptions.
     */
    @Test
    public void testSaveWriterEx()
    {
        source.ex = new IOException();
        try
        {
            locatorSupport.save(new StringWriter());
            fail("Exception not thrown!");
        }
        catch (ConfigurationException cex)
        {
            assertEquals("Wrong cause", source.ex, cex.getCause());
        }
    }

    /**
     * Tests whether data can be saved to a stream if the encoding is specified.
     */
    @Test
    public void testSaveStreamEnc() throws IOException, ConfigurationException
    {
        FileOutputStream out = new FileOutputStream(testFile);
        try
        {
            locatorSupport.save(out, ENCODING);
        }
        finally
        {
            out.close();
        }
        checkTestFile();
        checkEncoding(ENCODING);
    }

    /**
     * Tests whether the default encoding is taken into account when saving to a
     * stream.
     */
    @Test
    public void testSaveStreamDefEnc() throws IOException,
            ConfigurationException
    {
        locatorSupport.setEncoding(ENCODING);
        FileOutputStream out = new FileOutputStream(testFile);
        try
        {
            locatorSupport.save(out);
        }
        finally
        {
            out.close();
        }
        checkTestFile();
        checkEncoding(ENCODING);
    }

    /**
     * Tries to save to a stream with an unsupported encoding. This should cause
     * an exception.
     */
    @Test
    public void testSaveStreamUnsupportedEnc() throws IOException,
            ConfigurationException
    {
        FileOutputStream out = new FileOutputStream(testFile);
        try
        {
            locatorSupport.save(out, "an unknown encoding!");
            fail("Unsupported encoding not detected!");
        }
        catch (ConfigurationException cex)
        {
            // ok
        }
        finally
        {
            out.close();
        }
    }

    /**
     * Tries to save data is no locator is specified. This should cause an
     * exception.
     */
    @Test(expected = IllegalStateException.class)
    public void testSaveNoLocator() throws ConfigurationException
    {
        locatorSupport.save();
    }

    /**
     * Tries to save data to a null locator. This should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSaveLocatorNull() throws ConfigurationException
    {
        locatorSupport.save((Locator) null);
    }

    /**
     * Tests the behavior of save(Locator) if an exception is thrown.
     */
    @Test
    public void testSaveLocatorEx()
    {
        locatorSupport.setLocator(outputLocator);
        source.ex = new IOException();
        try
        {
            locatorSupport.save();
            fail("Exception not thrown!");
        }
        catch (ConfigurationException cex)
        {
            assertEquals("Wrong cause", source.ex, cex.getCause());
        }
    }

    /**
     * Tests whether data can be saved using the default locator.
     */
    @Test
    public void testSaveDefLocator() throws ConfigurationException, IOException
    {
        locatorSupport.setLocator(outputLocator);
        locatorSupport.save();
        checkTestFile();
        checkEncoding(null);
    }

    /**
     * Tests whether data can be saved using a specific locator.
     */
    @Test
    public void testSaveSpecificLocator() throws ConfigurationException,
            IOException
    {
        Locator defLoc = EasyMock.createMock(Locator.class);
        EasyMock.replay(defLoc);
        locatorSupport.setLocator(defLoc);
        locatorSupport.save(outputLocator);
        checkTestFile();
        checkEncoding(null);
        EasyMock.verify(defLoc);
    }

    /**
     * Tests whether the encoding is taken into account when saving to a
     * locator.
     */
    @Test
    public void testSaveLocatorEnc() throws ConfigurationException, IOException
    {
        locatorSupport.setLocator(outputLocator);
        locatorSupport.setEncoding(ENCODING);
        locatorSupport.save();
        checkTestFile();
        checkEncoding(ENCODING);
    }

    /**
     * A simple test implementation of the {@code StreamBasedSource} interface.
     * This implementation implements the load() operation by the reading in an
     * internal buffer. save() is implemented by writing a test text into the
     * writer.
     */
    private static class StreamBasedSourceTestImpl implements StreamBasedSource
    {
        /** Stores text read by the load() method. */
        String loadData;

        /** Stores the encoding used by load() or save(). */
        String encoding;

        /** An exception to be thrown. */
        IOException ex;

        public void load(Reader reader) throws ConfigurationException,
                IOException
        {
            if (ex != null)
            {
                throw ex;
            }
            loadData = read(reader);
            if (reader instanceof InputStreamReader)
            {
                encoding = ((InputStreamReader) reader).getEncoding();
            }
        }

        public void save(Writer writer) throws ConfigurationException,
                IOException
        {
            if (ex != null)
            {
                throw ex;
            }
            write(writer);
            if (writer instanceof OutputStreamWriter)
            {
                encoding = ((OutputStreamWriter) writer).getEncoding();
            }
        }
    }
}
