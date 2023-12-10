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

import static org.apache.commons.configuration2.TempDirUtils.newFile;
import static org.apache.commons.configuration2.TempDirUtils.newFolder;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SynchronizerTestImpl;
import org.apache.commons.configuration2.SynchronizerTestImpl.Methods;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test class for {@code FileHandler}.
 */
public class TestFileHandler {
    /**
     * A FileBased implementation which also implements FileLocatorAware. This class adds information about the current file
     * locator to the content read and written.
     */
    private static final class FileBasedFileLocatorAwareTestImpl extends FileBasedTestImpl implements FileLocatorAware {
        /** Stores the passed in file locator. */
        private FileLocator locator;

        /**
         * Returns the locator.
         *
         * @return the file locator
         */
        public FileLocator getLocator() {
            return locator;
        }

        @Override
        public void initFileLocator(final FileLocator loc) {
            this.locator = loc;
        }

        @Override
        public void read(final Reader in) throws ConfigurationException, IOException {
            super.read(in);
            setContent(String.valueOf(locator.getSourceURL()) + ": " + getContent());
        }

        @Override
        public void write(final Writer out) throws ConfigurationException, IOException {
            out.write(String.valueOf(locator.getSourceURL()) + ": ");
            super.write(out);
        }
    }

    /**
     * A test implementation of FileBased which can also read from input streams.
     * <p>
     * Cannot be final for Mockito.
     * </p>
     */
    private static class FileBasedInputStreamSupportTestImpl extends FileBasedTestImpl implements InputStreamSupport {
        @Override
        public void read(final InputStream in) throws ConfigurationException, IOException {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int c;
            while ((c = in.read()) != -1) {
                bos.write(c);
            }
            setContent("InputStream = " + bos.toString());
        }
    }

    /**
     * An implementation of the FileBased interface used for test purposes.
     */
    private static class FileBasedTestImpl implements FileBased {
        /** The content read from a reader. */
        private String content = CONTENT;

        /**
         * Returns the content read from a reader.
         *
         * @return the read content
         */
        public String getContent() {
            return content;
        }

        @Override
        public void read(final Reader in) throws ConfigurationException, IOException {
            content = readReader(in);
        }

        /**
         * Allows setting the content.
         *
         * @param content the content
         */
        public void setContent(final String content) {
            this.content = content;
        }

        @Override
        public void write(final Writer out) throws ConfigurationException, IOException {
            out.write(getContent());
            out.flush();
        }
    }

    /**
     * A test listener implementation.
     */
    private static final class FileHandlerListenerTestImpl extends FileHandlerListenerAdapter {
        /** The expected file handler. */
        private final FileHandler expHandler;

        /** A buffer for recording method invocations. */
        private final StringBuilder methods;

        public FileHandlerListenerTestImpl(final FileHandler fh) {
            expHandler = fh;
            methods = new StringBuilder();
        }

        /**
         * Tests whether the expected listener methods have been called.
         *
         * @param expMethods the expected methods as plain string
         */
        public void checkMethods(final String expMethods) {
            assertEquals(expMethods, methods.toString());
        }

        @Override
        public void loaded(final FileHandler handler) {
            super.loaded(handler);
            methodCalled(handler, "loaded");
        }

        @Override
        public void loading(final FileHandler handler) {
            super.loading(handler);
            methodCalled(handler, "loading");
        }

        @Override
        public void locationChanged(final FileHandler handler) {
            super.locationChanged(handler);
            methodCalled(handler, "locationChanged");
        }

        /**
         * One of the listener methods was called. Records this invocation.
         *
         * @param handler the file handler
         * @param method the called method
         */
        private void methodCalled(final FileHandler handler, final String method) {
            assertEquals(expHandler, handler);
            methods.append(method);
        }

        @Override
        public void saved(final FileHandler handler) {
            super.saved(handler);
            methodCalled(handler, "saved");
        }

        @Override
        public void saving(final FileHandler handler) {
            super.saving(handler);
            methodCalled(handler, "saving");
        }
    }

    /** Constant for the name of a test file. */
    private static final String TEST_FILENAME = "test.properties";

    /** Constant for content of the test file. */
    private static final String CONTENT = "TestFileHandler: This is test content.";

    /**
     * Checks a FileLocator which is expected to contain no data.
     *
     * @param content the data object which was passed the locator
     */
    private static void checkEmptyLocator(final FileBasedFileLocatorAwareTestImpl content) {
        assertNull(content.getLocator().getSourceURL());
        assertNull(content.getLocator().getBasePath());
        assertNull(content.getLocator().getFileName());
    }

    /**
     * Reads the content of the specified file into a string
     *
     * @param f the file to be read
     * @return the content of this file
     */
    private static String readFile(final File f) {
        return assertDoesNotThrow(() -> {
            try (Reader in = new FileReader(f)) {
                return readReader(in);
            }
        });
    }

    /**
     * Reads the content of the specified reader into a string.
     *
     * @param in the reader
     * @return the read content
     * @throws IOException if an error occurs
     */
    private static String readReader(final Reader in) throws IOException {
        final StringBuilder buf = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            buf.append((char) c);
        }
        return buf.toString();
    }

    /** A folder for temporary files. */
    @TempDir
    public File tempFolder;

    /**
     * Creates a test file with the test content.
     *
     * @return the File object pointing to the test file
     */
    private File createTestFile() {
        return createTestFile(null);
    }

    /**
     * Creates a test file with test content and allows specifying a file name.
     *
     * @param f the file to be created (may be <b>null</b>)
     * @return the File object pointing to the test file
     */
    private File createTestFile(final File f) {
        return assertDoesNotThrow(() -> {
            File file = f;
            if (file == null) {
                file = newFile(tempFolder);
            }
            try (Writer out = new FileWriter(file)) {
                out.write(CONTENT);
            }
            return file;
        });
    }

    /**
     * Tries to add a null listener.
     */
    @Test
    public void testAddFileHandlerListenerNull() {
        final FileHandler fileHandler = new FileHandler();
        assertThrows(IllegalArgumentException.class, () -> fileHandler.addFileHandlerListener(null));
    }

    /**
     * Tries to invoke the assignment constructor with a null handler.
     */
    @Test
    public void testAssignNullHandler() {
        final FileBased obj = new FileBasedTestImpl();
        assertThrows(IllegalArgumentException.class, () -> new FileHandler(obj, null));
    }

    /**
     * Tests whether a FileHandler object can be used to specify a location and later be assigned to a FileBased object.
     */
    @Test
    public void testAssignWithFileBased() {
        final FileHandler h1 = new FileHandler();
        final File f = new File("testfile.txt");
        h1.setFile(f);
        final FileBased content = new FileBasedTestImpl();
        final FileHandler h2 = new FileHandler(content, h1);
        h1.setFileName("someOtherFile.txt");
        assertSame(content, h2.getContent());
        assertEquals(f, h2.getFile());
    }

    /**
     * Tests whether the location can be cleared.
     */
    @Test
    public void testClearLocation() {
        final FileHandler handler = new FileHandler();
        handler.setFile(createTestFile());
        handler.clearLocation();
        assertFalse(handler.isLocationDefined());
        assertNull(handler.getFile());
        assertNull(handler.getURL());
        assertNull(handler.getBasePath());
        assertNull(handler.getPath());
    }

    /**
     * Tests getBasePath() if no information is available.
     */
    @Test
    public void testGetBasePathUndefined() {
        assertNull(new FileHandler().getBasePath());
    }

    /**
     * Tests getFileName() if no information is set.
     */
    @Test
    public void testGetFileNameUndefined() {
        assertNull(new FileHandler().getFileName());
    }

    /**
     * Tests whether a newly created instance has a default file system.
     */
    @Test
    public void testGetFileSystemDefault() {
        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        assertEquals(FileLocatorUtils.DEFAULT_FILE_SYSTEM, handler.getFileSystem());
    }

    /**
     * Tests whether a newly created instance uses the default location strategy.
     */
    @Test
    public void testGetLocationStrategyDefault() {
        final FileHandler handler = new FileHandler();
        assertNull(handler.getFileLocator().getLocationStrategy());
        assertSame(FileLocatorUtils.DEFAULT_LOCATION_STRATEGY, handler.getLocationStrategy());
    }

    /**
     * Tests whether an instance can be created from a map with the properties of a FileLocator.
     */
    @Test
    public void testInitFromMap() {
        final FileLocator locator = FileLocatorUtils.fileLocator().fileName(TEST_FILENAME).basePath("someBasePath").encoding("someEncoding").create();
        final Map<String, Object> map = new HashMap<>();
        FileLocatorUtils.put(locator, map);
        final FileHandler handler = FileHandler.fromMap(map);
        assertEquals(locator, handler.getFileLocator());
    }

    /**
     * Tests whether the initialization of properties is safe even if performed in multiple threads.
     */
    @Test
    public void testInitPropertiesMultiThreaded() throws InterruptedException {
        final String encoding = "TestEncoding";
        final FileSystem fileSystem = new DefaultFileSystem();
        final FileLocationStrategy locationStrategy = new ProvidedURLLocationStrategy();
        final int loops = 8;

        for (int i = 0; i < loops; i++) {
            final FileHandler handler = new FileHandler();
            final Thread t1 = new Thread(() -> handler.setFileSystem(fileSystem));
            final Thread t2 = new Thread(() -> handler.setFileName(TEST_FILENAME));
            final Thread t3 = new Thread(() -> handler.setEncoding(encoding));
            final Thread t4 = new Thread(() -> handler.setLocationStrategy(locationStrategy));
            final List<Thread> threads = Arrays.asList(t1, t2, t3, t4);
            for (final Thread t : threads) {
                t.start();
            }
            for (final Thread t : threads) {
                t.join();
            }
            final FileLocator locator = handler.getFileLocator();
            assertEquals(TEST_FILENAME, locator.getFileName());
            assertNull(locator.getSourceURL());
            assertEquals(encoding, locator.getEncoding());
            assertSame(fileSystem, locator.getFileSystem());
            assertSame(locationStrategy, locator.getLocationStrategy());
        }
    }

    /**
     * Tests isLocationDefined() if only a base path is set.
     */
    @Test
    public void testIsLocationDefinedBasePathOnly() {
        final FileHandler handler = new FileHandler();
        handler.setBasePath(createTestFile().getParent());
        assertFalse(handler.isLocationDefined());
    }

    /**
     * Tests whether an undefined location can be queried.
     */
    @Test
    public void testIsLocationDefinedFalse() {
        final FileHandler handler = new FileHandler();
        assertFalse(handler.isLocationDefined());
    }

    /**
     * Tests isLocationDefined() if a File has been set.
     */
    @Test
    public void testIsLocationDefinedFile() {
        final FileHandler handler = new FileHandler();
        handler.setFile(createTestFile());
        assertTrue(handler.isLocationDefined());
    }

    /**
     * Tests isLocationDefined() if a file name has been set.
     */
    @Test
    public void testIsLocationDefinedFileName() {
        final FileHandler handler = new FileHandler();
        handler.setFileName(createTestFile().getName());
        assertTrue(handler.isLocationDefined());
    }

    /**
     * Tests isLocationDefined() if a path has been set.
     */
    @Test
    public void testIsLocationDefinedPath() {
        final FileHandler handler = new FileHandler();
        handler.setPath(createTestFile().getAbsolutePath());
        assertTrue(handler.isLocationDefined());
    }

    /**
     * Tests isLocationDefined() if a URL has been set.
     */
    @Test
    public void testIsLocationDefinedURL() throws IOException {
        final FileHandler handler = new FileHandler();
        handler.setURL(createTestFile().toURI().toURL());
        assertTrue(handler.isLocationDefined());
    }

    /**
     * Tests that it is not possible to load a directory using the load() method which expects a File.
     */
    @Test
    public void testLoadDirectoryFile() {
        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        assertThrows(ConfigurationException.class, () -> handler.load(ConfigurationAssert.TEST_DIR));
    }

    /**
     * Checks that loading a directory instead of a file throws an exception.
     */
    @Test
    public void testLoadDirectoryString() {
        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        final String fileName = ConfigurationAssert.TEST_DIR.getAbsolutePath();
        assertThrows(ConfigurationException.class, () -> handler.load(fileName));
    }

    /**
     * Tests notifications about load operations.
     */
    @Test
    public void testLoadEvents() throws ConfigurationException {
        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        final FileHandlerListenerTestImpl listener = new FileHandlerListenerTestImpl(handler);
        handler.addFileHandlerListener(listener);
        handler.load(createTestFile());
        listener.checkMethods("loadingloaded");
    }

    /**
     * Tests whether a FileLocatorAware object is initialized correctly when loading data.
     */
    @Test
    public void testLoadFileLocatorAware() throws IOException, ConfigurationException {
        final File file = createTestFile();
        final FileBasedFileLocatorAwareTestImpl content = new FileBasedFileLocatorAwareTestImpl();
        final FileHandler handler = new FileHandler(content);
        handler.setFile(file);
        handler.load();
        assertEquals(file.toURI().toURL().toString() + ": " + CONTENT, content.getContent());
    }

    /**
     * Tests a load operation with a FileLocatorAware object if data is loaded from a reader.
     */
    @Test
    public void testLoadFileLocatorAwareReader() throws ConfigurationException {
        final FileBasedFileLocatorAwareTestImpl content = new FileBasedFileLocatorAwareTestImpl();
        final FileHandler handler = new FileHandler(content);
        handler.load(new StringReader(CONTENT));
        checkEmptyLocator(content);
    }

    /**
     * Tests loading with a FileLocatorAware object if data is loaded from a stream.
     */
    @Test
    public void testLoadFileLocatorAwareStream() throws ConfigurationException {
        final FileBasedFileLocatorAwareTestImpl content = new FileBasedFileLocatorAwareTestImpl();
        final FileHandler handler = new FileHandler(content);
        final ByteArrayInputStream bos = new ByteArrayInputStream(CONTENT.getBytes());
        handler.load(bos);
        checkEmptyLocator(content);
    }

    /**
     * Tests whether whether data can be loaded from class path.
     */
    @Test
    public void testLoadFromClassPath() throws ConfigurationException {
        final FileBasedTestImpl content = new FileBasedTestImpl();
        final FileHandler config1 = new FileHandler(content);
        config1.setFileName("config/deep/deeptest.properties");
        config1.load();
        assertFalse(content.getContent().isEmpty());
    }

    /**
     * Tests whether data from a File can be loaded.
     */
    @Test
    public void testLoadFromFile() throws ConfigurationException {
        final FileBasedTestImpl content = new FileBasedTestImpl();
        final File file = createTestFile();
        final FileHandler handler = new FileHandler(content);
        handler.load(file);
        assertEquals(CONTENT, content.getContent());
    }

    /**
     * Tests a load operation using the current location which is a file name.
     */
    @Test
    public void testLoadFromFileNameLocation() throws ConfigurationException {
        final File file = createTestFile();
        final FileBasedTestImpl content = new FileBasedTestImpl();
        final FileHandler handler = new FileHandler(content);
        handler.setBasePath(file.getParentFile().getAbsolutePath());
        handler.setFileName(file.getName());
        handler.load();
        assertEquals(CONTENT, content.getContent());
    }

    /**
     * Tries to load data from a File if no content object was set.
     */
    @Test
    public void testLoadFromFileNoContent() {
        final FileHandler handler = new FileHandler();
        final File file = createTestFile();
        final ConfigurationException cex = assertThrows(ConfigurationException.class, () -> handler.load(file));
        assertEquals("No content available!", cex.getMessage());
    }

    /**
     * Tests whether data from an absolute path can be loaded.
     */
    @Test
    public void testLoadFromFilePath() throws ConfigurationException {
        final File file = createTestFile();
        final FileBasedTestImpl content = new FileBasedTestImpl();
        final FileHandler handler = new FileHandler(content);
        handler.load(file.getAbsolutePath());
        assertEquals(CONTENT, content.getContent());
    }

    /**
     * Tests that a load() operation with a file path overrides a URL which might have been set.
     */
    @Test
    public void testLoadFromFilePathWithURLDefined() throws ConfigurationException {
        final File file = createTestFile();
        final FileBasedTestImpl content = new FileBasedTestImpl();
        final FileHandler handler = new FileHandler(content);
        handler.setURL(ConfigurationAssert.getTestURL("test.xml"));
        handler.load(file.getAbsolutePath());
        assertEquals(CONTENT, content.getContent());
    }

    /**
     * Tests whether data from a reader can be read.
     */
    @Test
    public void testLoadFromReader() throws Exception {
        final File file = createTestFile();
        final FileBasedTestImpl content = new FileBasedTestImpl();
        final FileHandler handler = new FileHandler(content);
        try (Reader in = new FileReader(file)) {
            handler.load(in);
        }
        assertEquals(CONTENT, content.getContent());
    }

    /**
     * Tests whether an IOException is handled when loading data from a reader.
     */
    @Test
    public void testLoadFromReaderIOException() throws IOException, ConfigurationException {
        final FileBased content = mock(FileBased.class);
        final Reader in = new StringReader(CONTENT);
        final IOException ioex = new IOException("Test exception");

        doThrow(ioex).when(content).read(in);

        final FileHandler handler = new FileHandler(content);
        final ConfigurationException cex = assertThrows(ConfigurationException.class, () -> handler.load(in));
        assertEquals(ioex, cex.getCause());

        verify(content).read(in);
        verifyNoMoreInteractions(content);
    }

    /**
     * Tests whether data from an input stream can be read.
     */
    @Test
    public void testLoadFromStream() throws Exception {
        final File file = createTestFile();
        final FileBasedTestImpl content = new FileBasedTestImpl();
        final FileHandler handler = new FileHandler(content);
        try (FileInputStream in = new FileInputStream(file)) {
            handler.load(in);
        }
        assertEquals(CONTENT, content.getContent());
    }

    /**
     * Tests whether data from a URL can be loaded.
     */
    @Test
    public void testLoadFromURL() throws Exception {
        final File file = createTestFile();
        final FileBasedTestImpl content = new FileBasedTestImpl();
        final FileHandler handler = new FileHandler(content);
        handler.load(file.toURI().toURL());
        assertEquals(CONTENT, content.getContent());
    }

    /**
     * Tests a load operation using the current location which is a URL.
     */
    @Test
    public void testLoadFromURLLocation() throws Exception {
        final File file = createTestFile();
        final FileBasedTestImpl content = new FileBasedTestImpl();
        final FileHandler handler = new FileHandler(content);
        handler.setURL(file.toURI().toURL());
        handler.load();
        assertEquals(CONTENT, content.getContent());
    }

    /**
     * Tests whether data can be read from an input stream.
     */
    @Test
    public void testLoadInputStreamSupport() throws ConfigurationException {
        final FileBasedInputStreamSupportTestImpl content = new FileBasedInputStreamSupportTestImpl();
        final FileHandler handler = new FileHandler(content);
        final ByteArrayInputStream bin = new ByteArrayInputStream(CONTENT.getBytes());
        handler.load(bin);
        assertEquals("InputStream = " + CONTENT, content.getContent());
    }

    /**
     * Tests whether an IOException is handled when reading from an input stream.
     */
    @Test
    public void testLoadInputStreamSupportIOException() throws ConfigurationException, IOException {
        final FileBasedInputStreamSupportTestImpl content = mock(FileBasedInputStreamSupportTestImpl.class);
        final ByteArrayInputStream bin = new ByteArrayInputStream(CONTENT.getBytes());
        final IOException ioex = new IOException();

        doThrow(ioex).when(content).read(bin);

        final FileHandler handler = new FileHandler(content);
        final ConfigurationException cex = assertThrows(ConfigurationException.class, () -> handler.load(bin));
        assertEquals(ioex, cex.getCause());

        verify(content).read(bin);
        verifyNoMoreInteractions(content);
    }

    /**
     * Tries to call a load() method if no content object is available.
     */
    @Test
    public void testLoadNoContent() {
        final FileHandler handler = new FileHandler();
        final StringReader reader = new StringReader(CONTENT);
        assertThrows(ConfigurationException.class, () -> handler.load(reader));
    }

    /**
     * Tries to load data if no location has been set.
     */
    @Test
    public void testLoadNoLocation() {
        final FileBasedTestImpl content = new FileBasedTestImpl();
        final FileHandler handler = new FileHandler(content);
        assertThrows(ConfigurationException.class, handler::load);
    }

    /**
     * Tests whether a load() operation is correctly synchronized.
     */
    @Test
    public void testLoadSynchronized() throws ConfigurationException {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        config.setSynchronizer(sync);
        final FileHandler handler = new FileHandler(config);
        handler.load(ConfigurationAssert.getTestFile("test.properties"));
        sync.verifyStart(Methods.BEGIN_WRITE);
        sync.verifyEnd(Methods.END_WRITE);
    }

    /**
     * Tests a successful locate() operation.
     */
    @Test
    public void testLocateSuccess() throws ConfigurationException {
        final FileHandler handler = new FileHandler();
        handler.setFileName(TEST_FILENAME);
        assertTrue(handler.locate());
        final FileLocator locator = handler.getFileLocator();
        assertNotNull(locator.getSourceURL());
        assertNotNull(locator.getBasePath());
        assertEquals(TEST_FILENAME, locator.getFileName());

        // check whether the correct URL was obtained
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final FileHandler h2 = new FileHandler(config);
        h2.setURL(locator.getSourceURL());
        h2.load();
        assertTrue(config.getBoolean("configuration.loaded"));
    }

    /**
     * Tests a locate() operation if there is not enough information.
     */
    @Test
    public void testLocateUndefinedLocator() {
        final FileHandler handler = new FileHandler();
        handler.setBasePath("only/a/base/path");
        final FileLocator locator = handler.getFileLocator();
        assertFalse(handler.locate());
        assertSame(locator, handler.getFileLocator());
    }

    /**
     * Tests a locate() operation if the specified file cannot be resolved.
     */
    @Test
    public void testLocateUnknownFile() {
        final FileHandler handler = new FileHandler();
        handler.setFileName("unknown file");
        final FileLocator locator = handler.getFileLocator();
        assertFalse(handler.locate());
        assertSame(locator, handler.getFileLocator());
    }

    /**
     * Tests a notification about a changed base path.
     */
    @Test
    public void testLocationChangedBasePath() {
        final FileHandler handler = new FileHandler();
        final FileHandlerListenerTestImpl listener = new FileHandlerListenerTestImpl(handler);
        handler.addFileHandlerListener(listener);
        handler.setBasePath(TEST_FILENAME);
        listener.checkMethods("locationChanged");
    }

    /**
     * Tests a notification about a changed encoding.
     */
    @Test
    public void testLocationChangedEncoding() {
        final FileHandler handler = new FileHandler();
        final FileHandlerListenerTestImpl listener = new FileHandlerListenerTestImpl(handler);
        handler.addFileHandlerListener(listener);
        handler.setEncoding(StandardCharsets.UTF_8.name());
        listener.checkMethods("locationChanged");
    }

    /**
     * Tests a notification about a changed file.
     */
    @Test
    public void testLocationChangedFile() throws IOException {
        final FileHandler handler = new FileHandler();
        final FileHandlerListenerTestImpl listener = new FileHandlerListenerTestImpl(handler);
        handler.addFileHandlerListener(listener);
        handler.setFile(newFile(tempFolder));
        listener.checkMethods("locationChanged");
    }

    /**
     * Tests a notification about a changed file name.
     */
    @Test
    public void testLocationChangedFileName() {
        final FileHandler handler = new FileHandler();
        final FileHandlerListenerTestImpl listener = new FileHandlerListenerTestImpl(handler);
        handler.addFileHandlerListener(listener);
        handler.setFileName(TEST_FILENAME);
        listener.checkMethods("locationChanged");
    }

    /**
     * Tests a notification about a changed file system.
     */
    @Test
    public void testLocationChangedFileSystem() {
        final FileSystem fs = mock(FileSystem.class);
        final FileHandler handler = new FileHandler();
        final FileHandlerListenerTestImpl listener = new FileHandlerListenerTestImpl(handler);
        handler.addFileHandlerListener(listener);
        handler.setFileSystem(fs);
        listener.checkMethods("locationChanged");
    }

    /**
     * Tests whether a notification is sent if the whole locator was changed.
     */
    @Test
    public void testLocationChangedLocator() {
        final FileHandler handler = new FileHandler();
        final FileHandlerListenerTestImpl listener = new FileHandlerListenerTestImpl(handler);
        handler.addFileHandlerListener(listener);
        handler.setFileLocator(FileLocatorUtils.fileLocator().fileName(TEST_FILENAME).create());
        listener.checkMethods("locationChanged");
    }

    /**
     * Tests a notification about a changed path.
     */
    @Test
    public void testLocationChangedPath() {
        final FileHandler handler = new FileHandler();
        final FileHandlerListenerTestImpl listener = new FileHandlerListenerTestImpl(handler);
        handler.addFileHandlerListener(listener);
        handler.setPath(TEST_FILENAME);
        listener.checkMethods("locationChanged");
    }

    /**
     * Tests a notification about a changed URL.
     */
    @Test
    public void testLocationChangedURL() throws IOException {
        final FileHandler handler = new FileHandler();
        final FileHandlerListenerTestImpl listener = new FileHandlerListenerTestImpl(handler);
        handler.addFileHandlerListener(listener);
        final URL url = newFile(tempFolder).toURI().toURL();
        handler.setURL(url);
        listener.checkMethods("locationChanged");
    }

    /**
     * Tests that the locator injected into the content object has an encoding set.
     */
    @Test
    public void testLocatorAwareEncoding() throws ConfigurationException {
        final FileBasedFileLocatorAwareTestImpl content = new FileBasedFileLocatorAwareTestImpl();
        final FileHandler handler = new FileHandler(content);
        final String encoding = "testEncoding";
        handler.setEncoding(encoding);
        handler.save(new StringWriter());
        assertEquals(encoding, content.getLocator().getEncoding());
    }

    /**
     * Tests whether file names containing a "+" character are handled correctly. This test is related to CONFIGURATION-415.
     */
    @Test
    public void testPathWithPlus() throws ConfigurationException, IOException {
        final File saveFile = newFile("test+config.properties", tempFolder);
        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        handler.setFile(saveFile);
        handler.save();
        assertEquals(CONTENT, readFile(saveFile));
    }

    /**
     * Tests loading and saving a configuration file with a complicated path name including spaces. (related to issue 35210)
     */
    @Test
    public void testPathWithSpaces() throws ConfigurationException, IOException {
        final File path = newFolder("path with spaces", tempFolder);
        final File confFile = new File(path, "config-test.properties");
        final File testFile = createTestFile(confFile);
        final URL url = testFile.toURI().toURL();
        final FileBasedTestImpl content = new FileBasedTestImpl();
        final FileHandler handler = new FileHandler(content);
        handler.setURL(url);
        handler.load();
        assertEquals(CONTENT, content.getContent());
        final File out = new File(path, "out.txt");
        handler.save(out);
        assertEquals(CONTENT, readFile(out));
    }

    /**
     * Tests whether the file system can be reset.
     */
    @Test
    public void testResetFileSystem() {
        final FileSystem sys = mock(FileSystem.class);
        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        handler.setFileSystem(sys);
        handler.resetFileSystem();
        assertEquals(FileLocatorUtils.DEFAULT_FILE_SYSTEM, handler.getFileSystem());
    }

    /**
     * Tests notifications about save operations.
     */
    @Test
    public void testSaveEvents() throws IOException, ConfigurationException {
        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        final FileHandlerListenerTestImpl listener = new FileHandlerListenerTestImpl(handler);
        handler.addFileHandlerListener(listener);
        final File f = newFile(tempFolder);
        handler.save(f);
        listener.checkMethods("savingsaved");
    }

    /**
     * Tests whether a FileLocatorAware is correctly handled when saving data.
     */
    @Test
    public void testSaveFileLocatorAware() throws ConfigurationException, IOException {
        final File file = newFile(tempFolder);
        final FileBasedFileLocatorAwareTestImpl content = new FileBasedFileLocatorAwareTestImpl();
        final FileHandler handler = new FileHandler(content);
        handler.save(file);
        assertEquals(file.toURI().toURL() + ": " + CONTENT, readFile(file));
    }

    /**
     * Tests a save operation with a FileLocatorAware object if the target is a stream.
     */
    @Test
    public void testSaveFileLocatorAwareToStream() throws ConfigurationException {
        final FileBasedFileLocatorAwareTestImpl content = new FileBasedFileLocatorAwareTestImpl();
        final FileHandler handler = new FileHandler(content);
        handler.save(new ByteArrayOutputStream());
        checkEmptyLocator(content);
    }

    /**
     * Tests a save operation with a FileLocatorAware object if the target is a writer.
     */
    @Test
    public void testSaveFileLocatorAwareToWriter() throws ConfigurationException {
        final FileBasedFileLocatorAwareTestImpl content = new FileBasedFileLocatorAwareTestImpl();
        final FileHandler handler = new FileHandler(content);
        handler.save(new StringWriter());
        checkEmptyLocator(content);
    }

    /**
     * Tries to save the locator if no location has been set.
     */
    @Test
    public void testSaveNoLocation() {
        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        assertThrows(ConfigurationException.class, handler::save);
    }

    /**
     * Tests whether a save() operation is correctly synchronized.
     */
    @Test
    public void testSaveSynchronized() throws ConfigurationException, IOException {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("test.synchronized", Boolean.TRUE);
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        config.setSynchronizer(sync);
        final FileHandler handler = new FileHandler(config);
        final File f = newFile(tempFolder);
        handler.save(f);
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests whether data can be saved to a file.
     */
    @Test
    public void testSaveToFile() throws ConfigurationException, IOException {
        final File file = newFile(tempFolder);
        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        handler.save(file);
        assertEquals(CONTENT, readFile(file));
    }

    /**
     * Tests whether data can be saved to a file name.
     */
    @Test
    public void testSaveToFileName() throws ConfigurationException, IOException {
        final File file = newFile(tempFolder);
        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        handler.save(file.getAbsolutePath());
        assertEquals(CONTENT, readFile(file));
    }

    /**
     * Tests whether data can be saved to the internal location if it is a file name.
     */
    @Test
    public void testSaveToFileNameLocation() throws ConfigurationException, IOException {
        final File file = newFile(tempFolder);
        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        handler.setFileName(file.getAbsolutePath());
        handler.save();
        assertEquals(CONTENT, readFile(file));
    }

    /**
     * Tests whether a URL exception is handled when saving a file to a file name.
     */
    @Test
    public void testSaveToFileNameURLException() throws IOException {
        final FileSystem fs = mock(FileSystem.class);
        final File file = newFile(tempFolder);
        final String basePath = "some base path";
        final MalformedURLException urlex = new MalformedURLException("Test exception");
        final String fileName = file.getName();

        when(fs.getURL(basePath, fileName)).thenThrow(urlex);

        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        handler.setBasePath(basePath);
        handler.setFileSystem(fs);
        final ConfigurationException cex = assertThrows(ConfigurationException.class, () -> handler.save(fileName));
        assertEquals(urlex, cex.getCause());

        verify(fs).getURL(basePath, fileName);
        verifyNoMoreInteractions(fs);
    }

    /**
     * Tries to save data to a file name if the name cannot be located.
     */
    @Test
    public void testSaveToFileNameURLNotResolved() throws IOException {
        final FileSystem fs = mock(FileSystem.class);
        final File file = newFile(tempFolder);
        final String fileName = file.getName();

        when(fs.getURL(null, fileName)).thenReturn(null);

        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        handler.setFileSystem(fs);
        assertThrows(ConfigurationException.class, () -> handler.save(fileName));

        verify(fs).getURL(null, fileName);
        verifyNoMoreInteractions(fs);
    }

    /**
     * Tests whether data can be saved to a stream.
     */
    @Test
    public void testSaveToStream() throws ConfigurationException, IOException {
        final File file = newFile(tempFolder);
        try (FileOutputStream out = new FileOutputStream(file)) {
            final FileHandler handler = new FileHandler(new FileBasedTestImpl());
            handler.save(out);
        }
        assertEquals(CONTENT, readFile(file));
    }

    /**
     * Tests whether data can be saved to a URL.
     */
    @Test
    public void testSaveToURL() throws Exception {
        final File file = newFile(tempFolder);
        final URL url = file.toURI().toURL();
        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        handler.save(url);
        assertEquals(CONTENT, readFile(file));
    }

    /**
     * Tests whether data can be saved to the internal location if it is a URL.
     */
    @Test
    public void testSaveToURLLocation() throws ConfigurationException, IOException {
        final File file = newFile(tempFolder);
        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        handler.setURL(file.toURI().toURL());
        handler.save();
        assertEquals(CONTENT, readFile(file));
    }

    /**
     * Tests whether data can be saved into a Writer.
     */
    @Test
    public void testSaveToWriter() throws ConfigurationException {
        final FileBasedTestImpl content = new FileBasedTestImpl();
        final FileHandler handler = new FileHandler(content);
        final StringWriter out = new StringWriter();
        handler.save(out);
        assertEquals(CONTENT, out.toString());
    }

    /**
     * Tests whether an I/O exception during a save operation to a Writer is handled correctly.
     */
    @Test
    public void testSaveToWriterIOException() throws ConfigurationException, IOException {
        final FileBased content = mock(FileBased.class);
        final StringWriter out = new StringWriter();
        final IOException ioex = new IOException("Test exception!");

        doThrow(ioex).when(content).write(out);

        final FileHandler handler = new FileHandler(content);
        final ConfigurationException cex = assertThrows(ConfigurationException.class, () -> handler.save(out));
        assertEquals(ioex, cex.getCause());

        verify(content).write(out);
        verifyNoMoreInteractions(content);
    }

    /**
     * Tries to save something to a Writer if no content is set.
     */
    @Test
    public void testSaveToWriterNoContent() {
        final FileHandler handler = new FileHandler();
        final StringWriter writer = new StringWriter();
        assertThrows(ConfigurationException.class, () -> handler.save(writer));
    }

    /**
     * Tests whether a base path can be set and whether this removes an already set URL.
     */
    @Test
    public void testSetBasePath() {
        final FileHandler handler = new FileHandler();
        handler.setURL(ConfigurationAssert.getTestURL(TEST_FILENAME));
        final String basePath = ConfigurationAssert.TEST_DIR_NAME;
        handler.setBasePath(basePath);
        final FileLocator locator = handler.getFileLocator();
        assertEquals(basePath, locator.getBasePath());
        assertNull(locator.getSourceURL());
        assertNull(locator.getFileName());
    }

    /**
     * Tests whether the file scheme is corrected when setting the base path.
     */
    @Test
    public void testSetBasePathFileScheme() {
        final FileHandler handler = new FileHandler();
        handler.setBasePath("file:/test/path/");
        assertEquals("file:///test/path/", handler.getFileLocator().getBasePath());
    }

    /**
     * Tests whether the location can be set as a file.
     */
    @Test
    public void testSetFile() {
        final FileHandler handler = new FileHandler();
        final File directory = ConfigurationAssert.TEST_DIR;
        final File file = ConfigurationAssert.getTestFile(TEST_FILENAME);
        handler.setFile(file);
        assertEquals(directory.getAbsolutePath(), handler.getBasePath());
        assertEquals(TEST_FILENAME, handler.getFileName());
        assertEquals(file.getAbsolutePath(), handler.getPath());
    }

    /**
     * Tests whether the handler can be initialized using a FileLocator.
     */
    @Test
    public void testSetFileLocator() {
        final FileLocator locator = FileLocatorUtils.fileLocator().fileName(TEST_FILENAME).create();
        final FileHandler handler = new FileHandler();
        handler.setFileLocator(locator);
        assertEquals(TEST_FILENAME, handler.getFileName());
    }

    /**
     * Tries to set the FileLocator to null.
     */
    @Test
    public void testSetFileLocatorNull() {
        final FileHandler handler = new FileHandler();
        assertThrows(IllegalArgumentException.class, () -> handler.setFileLocator(null));
    }

    /**
     * Tests whether the location can be set using file name and base path.
     */
    @Test
    public void testSetFileName() {
        final FileHandler handler = new FileHandler();
        handler.setURL(ConfigurationAssert.getTestURL(TEST_FILENAME));
        handler.setFileName(TEST_FILENAME);
        assertNull(handler.getBasePath());
        assertEquals(TEST_FILENAME, handler.getFileName());
        assertEquals(TEST_FILENAME, handler.getFileLocator().getFileName());
        assertNull(handler.getFileLocator().getSourceURL());
    }

    /**
     * Tests whether the file scheme is corrected when setting the file name.
     */
    @Test
    public void testSetFileNameFileScheme() {
        final FileHandler handler = new FileHandler();
        handler.setFileName("file:/test/path/test.txt");
        assertEquals("file:///test/path/test.txt", handler.getFileLocator().getFileName());
    }

    /**
     * Tests whether a null file system can be set to reset this property.
     */
    @Test
    public void testSetFileSystemNull() {
        final FileSystem sys = mock(FileSystem.class);
        final FileHandler handler = new FileHandler(new FileBasedTestImpl());
        handler.setFileSystem(sys);
        assertSame(sys, handler.getFileSystem());
        handler.setFileSystem(null);
        assertEquals(FileLocatorUtils.DEFAULT_FILE_SYSTEM, handler.getFileSystem());
    }

    /**
     * Tests whether the location strategy can be changed.
     */
    @Test
    public void testSetLocationStrategy() {
        final FileLocationStrategy strategy = mock(FileLocationStrategy.class);
        final FileHandler handler = new FileHandler();
        handler.setLocationStrategy(strategy);
        assertSame(strategy, handler.getFileLocator().getLocationStrategy());
        assertSame(strategy, handler.getLocationStrategy());
    }

    /**
     * Tests whether the location can be set as a file.
     */
    @Test
    public void testSetPath() throws MalformedURLException {
        final FileHandler handler = new FileHandler();
        handler.setPath(ConfigurationAssert.TEST_DIR_NAME + File.separator + TEST_FILENAME);
        assertEquals(TEST_FILENAME, handler.getFileName());
        assertEquals(ConfigurationAssert.TEST_DIR.getAbsolutePath(), handler.getBasePath());
        final File file = ConfigurationAssert.getTestFile(TEST_FILENAME);
        assertEquals(file.getAbsolutePath(), handler.getPath());
        assertEquals(file.toURI().toURL(), handler.getURL());
        assertNull(handler.getFileLocator().getSourceURL());
    }

    /**
     * Additional tests for setting file names in various ways. (Copied from the test for XMLConfiguration)
     */
    @Test
    public void testSettingFileNames() {
        final String testProperties = ConfigurationAssert.getTestFile("test.xml").getAbsolutePath();
        final String testBasePath = ConfigurationAssert.TEST_DIR.getAbsolutePath();

        final FileHandler handler = new FileHandler();
        handler.setFileName(testProperties);
        assertEquals(testProperties.toString(), handler.getFileName());

        handler.setBasePath(testBasePath);
        handler.setFileName("hello.xml");
        assertEquals("hello.xml", handler.getFileName());
        assertEquals(testBasePath.toString(), handler.getBasePath());
        assertEquals(new File(testBasePath, "hello.xml"), handler.getFile());

        handler.setBasePath(testBasePath);
        handler.setFileName("subdir/hello.xml");
        assertEquals("subdir/hello.xml", handler.getFileName());
        assertEquals(testBasePath.toString(), handler.getBasePath());
        assertEquals(new File(testBasePath, "subdir/hello.xml"), handler.getFile());
    }

    /**
     * Tests whether a URL can be set.
     */
    @Test
    public void testSetURL() throws Exception {
        final FileHandler handler = new FileHandler();
        handler.setURL(new URL("https://commons.apache.org/configuration/index.html"));

        assertEquals("https://commons.apache.org/configuration/", handler.getBasePath());
        assertEquals("index.html", handler.getFileName());
        assertNull(handler.getFileLocator().getFileName());
    }

    /**
     * Tests whether the correct file scheme is applied.
     */
    @Test
    public void testSetURLFileScheme() throws MalformedURLException {
        final FileHandler handler = new FileHandler();
        // file URL - This url is invalid, a valid url would be
        // file:///temp/test.properties.
        handler.setURL(new URL("file:/temp/test.properties"));
        assertEquals("file:///temp/", handler.getBasePath());
        assertEquals(TEST_FILENAME, handler.getFileName());
    }

    /**
     * Tests whether a null URL can be set.
     */
    @Test
    public void testSetURLNull() {
        final FileHandler handler = new FileHandler();
        handler.setURL(ConfigurationAssert.getTestURL(TEST_FILENAME));
        handler.setURL(null);
        final FileLocator locator = handler.getFileLocator();
        assertNull(locator.getBasePath());
        assertNull(locator.getFileName());
        assertNull(locator.getSourceURL());
    }

    /**
     * Tests whether a URL with parameters can be set.
     */
    @Test
    public void testSetURLWithParams() throws Exception {
        final FileHandler handler = new FileHandler();
        final URL url = new URL("https://issues.apache.org/bugzilla/show_bug.cgi?id=37886");
        handler.setURL(url);
        assertEquals("https://issues.apache.org/bugzilla/", handler.getBasePath());
        assertEquals("show_bug.cgi", handler.getFileName());
        assertEquals(url, handler.getURL());
    }
}
