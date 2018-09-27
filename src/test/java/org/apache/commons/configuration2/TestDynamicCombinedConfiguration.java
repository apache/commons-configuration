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

package org.apache.commons.configuration2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Random;

import org.apache.commons.configuration2.SynchronizerTestImpl.Methods;
import org.apache.commons.configuration2.builder.BuilderConfigurationWrapperFactory;
import org.apache.commons.configuration2.builder.ConfigurationBuilder;
import org.apache.commons.configuration2.builder.CopyObjectDefaultHandler;
import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.FileBasedBuilderProperties;
import org.apache.commons.configuration2.builder.combined.CombinedConfigurationBuilder;
import org.apache.commons.configuration2.builder.combined.MultiFileConfigurationBuilder;
import org.apache.commons.configuration2.builder.combined.ReloadingCombinedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.sync.LockMode;
import org.apache.commons.configuration2.sync.ReadWriteSynchronizer;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class TestDynamicCombinedConfiguration
{
    private static String PATTERN = "${sys:Id}";
    private static String PATTERN1 = "target/test-classes/testMultiConfiguration_${sys:Id}.xml";
    private static String DEFAULT_FILE = "target/test-classes/testMultiConfiguration_default.xml";
    private static final File MULTI_TENENT_FILE = ConfigurationAssert
            .getTestFile("testMultiTenentConfigurationBuilder4.xml");
    private static final File MULTI_DYNAMIC_FILE = ConfigurationAssert
            .getTestFile("testMultiTenentConfigurationBuilder5.xml");

    /** Constant for the number of test threads. */
    private static final int THREAD_COUNT = 3;

    /** Constant for the number of loops in the multi-thread tests. */
    private static final int LOOP_COUNT = 100;

    /** A helper object for creating builder parameters. */
    private static Parameters parameters;

    /** Helper object for creating temporary files. */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void setUpOnce()
    {
        parameters = new Parameters();
    }

    @Test
    public void testConfiguration() throws Exception
    {
        final DynamicCombinedConfiguration config = new DynamicCombinedConfiguration();
        final DefaultListDelimiterHandler listHandler = new DefaultListDelimiterHandler(',');
        config.setListDelimiterHandler(listHandler);
        final XPathExpressionEngine engine = new XPathExpressionEngine();
        config.setExpressionEngine(engine);
        config.setKeyPattern(PATTERN);
        final ConfigurationBuilder<XMLConfiguration> multiBuilder =
                new MultiFileConfigurationBuilder<>(
                        XMLConfiguration.class).configure(parameters
                        .multiFile()
                        .setFilePattern(PATTERN1)
                        .setPrefixLookups(
                                ConfigurationInterpolator
                                        .getDefaultPrefixLookups())
                        .setManagedBuilderParameters(
                                parameters.xml().setExpressionEngine(engine)
                                        .setListDelimiterHandler(listHandler)));
        final BuilderConfigurationWrapperFactory wrapFactory =
                new BuilderConfigurationWrapperFactory();
        config.addConfiguration(wrapFactory.createBuilderConfigurationWrapper(
                HierarchicalConfiguration.class, multiBuilder), "Multi");
        final XMLConfiguration xml = new XMLConfiguration();
        xml.setExpressionEngine(engine);
        final FileHandler handler = new FileHandler(xml);
        handler.setFile(new File(DEFAULT_FILE));
        handler.load();
        config.addConfiguration(xml, "Default");

        verify("1001", config, 15);
        verify("1002", config, 25);
        verify("1003", config, 35);
        verify("1004", config, 50);
        assertEquals("a,b,c", config.getString("split/list3/@values"));
        assertEquals(0, config.getMaxIndex("split/list3/@values"));
        assertEquals("a\\,b\\,c", config.getString("split/list4/@values"));
        assertEquals("OK-1", config.getString("buttons/name"));
        assertEquals(3, config.getMaxIndex("buttons/name"));
        assertEquals("a\\,b\\,c", config.getString("split/list2"));
        assertEquals("Wrong size", 18, config.size());
        config.addProperty("listDelimiterTest", "1,2,3");
        assertEquals("List delimiter not detected", "1", config.getString("listDelimiterTest"));
    }

    /**
     * Tests whether a configuration can be updated.
     */
    @Test
    public void testUpdateConfiguration() throws ConfigurationException
    {
        System.getProperties().remove("Id");
        final CombinedConfigurationBuilder builder =
                new CombinedConfigurationBuilder();
        builder.configure(parameters.fileBased().setFile(MULTI_TENENT_FILE)
                .setSynchronizer(new ReadWriteSynchronizer()));
        final CombinedConfiguration config = builder.getConfiguration();
        config.getConfiguration(1).setProperty("rowsPerPage", "25");
        assertEquals("Value not changed", "25", config.getString("rowsPerPage"));
    }

    /**
     * Prepares a test for calling the Synchronizer. This method creates a test
     * Synchronizer, installs it at the configuration and returns it.
     *
     * @param config the configuration
     * @return the test Synchronizer
     */
    private SynchronizerTestImpl prepareSynchronizerTest(final Configuration config)
    {
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        config.setSynchronizer(sync);
        config.lock(LockMode.READ);
        config.unlock(LockMode.READ); // ensure that root node is constructed
        sync.clear();
        return sync;
    }

    /**
     * Tests whether adding a configuration is synchronized.
     */
    @Test
    public void testAddConfigurationSynchronized()
    {
        final DynamicCombinedConfiguration config =
                new DynamicCombinedConfiguration();
        final SynchronizerTestImpl sync = prepareSynchronizerTest(config);
        config.addConfiguration(new PropertiesConfiguration());
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests whether querying the number of configurations is synchronized.
     */
    @Test
    public void testGetNumberOfConfigurationsSynchronized()
    {
        final DynamicCombinedConfiguration config =
                new DynamicCombinedConfiguration();
        final SynchronizerTestImpl sync = prepareSynchronizerTest(config);
        config.getNumberOfConfigurations();
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether querying a configuration by index is synchronized.
     */
    @Test
    public void testGetConfigurationByIdxSynchronized()
    {
        final DynamicCombinedConfiguration config =
                new DynamicCombinedConfiguration();
        final Configuration child = new PropertiesConfiguration();
        config.addConfiguration(child);
        final SynchronizerTestImpl sync = prepareSynchronizerTest(config);
        assertSame("Wrong configuration", child, config.getConfiguration(0));
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether querying a configuration by name is synchronized.
     */
    @Test
    public void testGetConfigurationByNameSynchronized()
    {
        final DynamicCombinedConfiguration config =
                new DynamicCombinedConfiguration();
        final SynchronizerTestImpl sync = prepareSynchronizerTest(config);
        assertNull("Wrong result", config.getConfiguration("unknown config"));
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether querying the set of configuration names is synchronized.
     */
    @Test
    public void testGetConfigurationNamesSynchronized()
    {
        final DynamicCombinedConfiguration config =
                new DynamicCombinedConfiguration();
        final SynchronizerTestImpl sync = prepareSynchronizerTest(config);
        config.getConfigurationNames();
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether removing a child configuration is synchronized.
     */
    @Test
    public void testRemoveConfigurationSynchronized()
    {
        final DynamicCombinedConfiguration config =
                new DynamicCombinedConfiguration();
        final String configName = "testConfig";
        config.addConfiguration(new PropertiesConfiguration(), configName);
        final SynchronizerTestImpl sync = prepareSynchronizerTest(config);
        config.removeConfiguration(configName);
        sync.verifyContains(Methods.BEGIN_WRITE);
    }

    @Test
    public void testConcurrentGetAndReload() throws Exception
    {
        System.getProperties().remove("Id");
        final CombinedConfigurationBuilder builder = new CombinedConfigurationBuilder();
        builder.configure(parameters.fileBased().setFile(MULTI_TENENT_FILE)
                .setSynchronizer(new ReadWriteSynchronizer()));
        final CombinedConfiguration config = builder.getConfiguration();

        assertEquals("Wrong value", "50", config.getString("rowsPerPage"));
        final Thread testThreads[] = new Thread[THREAD_COUNT];
        final int failures[] = new int[THREAD_COUNT];

        for (int i = 0; i < testThreads.length; ++i)
        {
            testThreads[i] = new ReloadThread(builder, failures, i, LOOP_COUNT, false, null, "50");
            testThreads[i].start();
        }

        int totalFailures = 0;
        for (int i = 0; i < testThreads.length; ++i)
        {
            testThreads[i].join();
            totalFailures += failures[i];
        }
        assertEquals(totalFailures + " failures Occurred", 0, totalFailures);
    }

    @Test
    public void testConcurrentGetAndReload2() throws Exception
    {
        System.getProperties().remove("Id");
        final CombinedConfigurationBuilder builder = new CombinedConfigurationBuilder();
        builder.configure(parameters.fileBased().setFile(MULTI_TENENT_FILE)
                .setSynchronizer(new ReadWriteSynchronizer()));
        final CombinedConfiguration config = builder.getConfiguration();

        assertEquals(config.getString("rowsPerPage"), "50");

        final Thread testThreads[] = new Thread[THREAD_COUNT];
        final int failures[] = new int[THREAD_COUNT];
        System.setProperty("Id", "2002");
        assertEquals("Wrong value", "25", config.getString("rowsPerPage"));
        for (int i = 0; i < testThreads.length; ++i)
        {
            testThreads[i] = new ReloadThread(builder, failures, i, LOOP_COUNT, false, null, "25");
            testThreads[i].start();
        }

        int totalFailures = 0;
        for (int i = 0; i < testThreads.length; ++i)
        {
            testThreads[i].join();
            totalFailures += failures[i];
        }
        System.getProperties().remove("Id");
        assertEquals(totalFailures + " failures Occurred", 0, totalFailures);
    }

    @Test
    public void testConcurrentGetAndReloadMultipleClients() throws Exception
    {
        System.getProperties().remove("Id");
        final CombinedConfigurationBuilder builder = new CombinedConfigurationBuilder();
        builder.configure(parameters.fileBased().setFile(MULTI_TENENT_FILE)
                .setSynchronizer(new ReadWriteSynchronizer()));
        final CombinedConfiguration config = builder.getConfiguration();

        assertEquals(config.getString("rowsPerPage"), "50");

        final Thread testThreads[] = new Thread[THREAD_COUNT];
        final int failures[] = new int[THREAD_COUNT];
        final String[] ids = new String[] {null, "2002", "3001", "3002", "3003"};
        final String[] expected = new String[] {"50", "25", "15", "25", "50"};
        for (int i = 0; i < testThreads.length; ++i)
        {
            testThreads[i] = new ReloadThread(builder, failures, i, LOOP_COUNT, true, ids[i], expected[i]);
            testThreads[i].start();
        }

        int totalFailures = 0;
        for (int i = 0; i < testThreads.length; ++i)
        {
            testThreads[i].join();
            totalFailures += failures[i];
        }
        System.getProperties().remove("Id");
        if (totalFailures != 0)
        {
            System.out.println("Failures:");
            for (int i = 0; i < testThreads.length; ++i)
            {
                System.out.println("Thread " + i + " " + failures[i]);
            }
        }
        assertEquals(totalFailures + " failures Occurred", 0, totalFailures);
    }

    @Test
    public void testConcurrentGetAndReloadFile() throws Exception
    {
        final int threadCount = 25;
        System.getProperties().remove("Id");
        System.setProperty("TemporaryFolder", folder.getRoot().getAbsolutePath());
        // create a new configuration
        File input = new File("target/test-classes/testMultiDynamic_default.xml");
        final File output = folder.newFile("testMultiDynamic_default.xml");
        output.delete();
        output.getParentFile().mkdir();
        copyFile(input, output);

        final ReloadingCombinedConfigurationBuilder builder =
                new ReloadingCombinedConfigurationBuilder();
        builder.configure(parameters
                .combined()
                .setSynchronizer(new ReadWriteSynchronizer())
                .setDefinitionBuilderParameters(
                        new FileBasedBuilderParametersImpl()
                                .setFile(MULTI_DYNAMIC_FILE))
                .registerChildDefaultsHandler(
                        FileBasedBuilderProperties.class,
                        new CopyObjectDefaultHandler(
                                new FileBasedBuilderParametersImpl()
                                        .setReloadingRefreshDelay(1L))));
        CombinedConfiguration config = builder.getConfiguration();
        assertEquals("Wrong property value (1)", "ID0001",
                config.getString("Product/FIIndex/FI[@id='123456781']"));

        final ReaderThread testThreads[] = new ReaderThread[threadCount];
        for (int i = 0; i < testThreads.length; ++i)
        {
            testThreads[i] = new ReaderThread(builder);
            testThreads[i].start();
        }

        builder.getReloadingController().checkForReloading(null);
        Thread.sleep(2000);

        input = new File("target/test-classes/testMultiDynamic_default2.xml");
        copyFile(input, output);

        Thread.sleep(2000);
        assertTrue("Changed file not detected", builder
                .getReloadingController().checkForReloading(null));
        config = builder.getConfiguration();
        final String id = config.getString("Product/FIIndex/FI[@id='123456782']");
        assertNotNull("File did not reload, id is null", id);
        final String rows = config.getString("rowsPerPage");
        assertEquals("Incorrect value for rowsPerPage", "25", rows);

        for (final ReaderThread testThread : testThreads) {
            testThread.shutdown();
            testThread.join();
        }
        for (final ReaderThread testThread : testThreads) {
            assertFalse(testThread.failed());
        }
        assertEquals("ID0002", config.getString("Product/FIIndex/FI[@id='123456782']"));
        output.delete();
    }


    private class ReloadThread extends Thread
    {
        private final CombinedConfigurationBuilder builder;
        private final int[] failures;
        private final int index;
        private final int count;
        private final String expected;
        private final String id;
        private final boolean useId;
        private final Random random;

        ReloadThread(final CombinedConfigurationBuilder b, final int[] failures, final int index, final int count,
                     final boolean useId, final String id, final String expected)
        {
            builder = b;
            this.failures = failures;
            this.index = index;
            this.count = count;
            this.expected = expected;
            this.id = id;
            this.useId = useId;
            random = new Random();
        }

        @Override
        public void run()
        {
            failures[index] = 0;

            if (useId)
            {
                ThreadLookup.setId(id);
            }
            for (int i = 0; i < count; i++)
            {
                try
                {
                    if(random.nextBoolean())
                    {
                        // simulate a reload
                        builder.resetResult();
                    }
                    final CombinedConfiguration combined = builder.getConfiguration();
                    final String value = combined.getString("rowsPerPage", null);
                    if (value == null || !value.equals(expected))
                    {
                        ++failures[index];
                    }
                }
                catch (final Exception ex)
                {
                    ++failures[index];
                }
            }
        }
    }

    private class ReaderThread extends Thread
    {
        private volatile boolean running = true;
        private volatile boolean failed = false;
        private final CombinedConfigurationBuilder builder;
        private final Random random;

        public ReaderThread(final CombinedConfigurationBuilder b)
        {
            builder = b;
            random = new Random();
        }

        @Override
        public void run()
        {
            try
            {
                while (running)
                {
                    final CombinedConfiguration combined = builder.getConfiguration();
                    final String bcId =
                            combined.getString("Product/FIIndex/FI[@id='123456781']");
                    if ("ID0001".equalsIgnoreCase(bcId))
                    {
                        if (failed)
                        {
                            System.out.println("Thread failed, but recovered");
                        }
                        failed = false;
                    }
                    else
                    {
                        failed = true;
                    }
                    final int sleepTime = random.nextInt(75);
                    Thread.sleep(sleepTime);
                }
            }
            catch (final ConfigurationException cex)
            {
                failed = true;
            }
            catch(final InterruptedException iex)
            {
                Thread.currentThread().interrupt();
            }
        }

        public boolean failed()
        {
            return failed;
        }

        public void shutdown()
        {
            running = false;
            interrupt();
        }

    }

    private void verify(final String key, final DynamicCombinedConfiguration config, final int rows)
    {
        System.setProperty("Id", key);
        assertTrue(config.getInt("rowsPerPage") == rows);
    }

    private void copyFile(final File input, final File output) throws IOException
    {
        final Reader reader = new FileReader(input);
        final Writer writer = new FileWriter(output);
        final char[] buffer = new char[4096];
        int n = 0;
        while (-1 != (n = reader.read(buffer)))
        {
            writer.write(buffer, 0, n);
        }
        reader.close();
        writer.close();
    }

    public static class ThreadLookup implements Lookup
    {
        private static ThreadLocal<String> id = new ThreadLocal<>();

        public ThreadLookup()
        {
        }

        public static void setId(final String value)
        {
            id.set(value);
        }

        @Override
        public String lookup(final String key)
        {
            if (key == null || !key.equals("Id"))
            {
                return null;
            }
            final String value = System.getProperty("Id");
            if (value != null)
            {
                return value;
            }
            return id.get();

        }
    }
}
