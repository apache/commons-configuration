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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration2.SynchronizerTestImpl.Methods;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.sync.LockMode;
import org.apache.commons.configuration2.sync.ReadWriteSynchronizer;
import org.apache.commons.configuration2.sync.Synchronizer;
import org.apache.commons.configuration2.tree.DefaultExpressionEngine;
import org.apache.commons.configuration2.tree.DefaultExpressionEngineSymbols;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.NodeCombiner;
import org.apache.commons.configuration2.tree.NodeModel;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.apache.commons.configuration2.tree.UnionCombiner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for CombinedConfiguration.
 *
 */
public class TestCombinedConfiguration {
    /**
     * Test event listener class for checking if the expected invalidate events are fired.
     */
    private static class CombinedListener implements EventListener<ConfigurationEvent> {
        int invalidateEvents;

        int otherEvents;

        /**
         * Checks if the expected number of events was fired.
         *
         * @param expectedInvalidate the expected number of invalidate events
         * @param expectedOthers the expected number of other events
         */
        public void checkEvent(final int expectedInvalidate, final int expectedOthers) {
            assertEquals(expectedInvalidate, invalidateEvents);
            assertEquals(expectedOthers, otherEvents);
        }

        @Override
        public void onEvent(final ConfigurationEvent event) {
            if (event.getEventType() == CombinedConfiguration.COMBINED_INVALIDATE) {
                invalidateEvents++;
            } else {
                otherEvents++;
            }
        }
    }

    /**
     * A test thread performing reads on a combined configuration. This thread reads a certain property from the
     * configuration. If everything works well, this property should have at least one and at most two values.
     */
    private static class ReadThread extends Thread {
        /** The configuration to be accessed. */
        private final Configuration config;

        /** The latch for synchronizing thread start. */
        private final CountDownLatch startLatch;

        /** A counter for read errors. */
        private final AtomicInteger errorCount;

        /** The number of reads to be performed. */
        private final int numberOfReads;

        /**
         * Creates a new instance of {@code ReadThread}.
         *
         * @param readConfig the configuration to be read
         * @param latch the latch for synchronizing thread start
         * @param errCnt the counter for read errors
         * @param readCount the number of reads to be performed
         */
        public ReadThread(final Configuration readConfig, final CountDownLatch latch, final AtomicInteger errCnt, final int readCount) {
            config = readConfig;
            startLatch = latch;
            errorCount = errCnt;
            numberOfReads = readCount;
        }

        /**
         * Reads the test property from the associated configuration. Its values are checked.
         */
        private void readConfiguration() {
            final List<Object> values = config.getList(KEY_CONCURRENT);
            if (values.size() < 1 || values.size() > 2) {
                errorCount.incrementAndGet();
            } else {
                boolean ok = true;
                for (final Object value : values) {
                    if (!TEST_NAME.equals(value)) {
                        ok = false;
                    }
                }
                if (!ok) {
                    errorCount.incrementAndGet();
                }
            }
        }

        /**
         * Reads from the test configuration.
         */
        @Override
        public void run() {
            try {
                startLatch.await();
                for (int i = 0; i < numberOfReads; i++) {
                    readConfiguration();
                }
            } catch (final Exception e) {
                errorCount.incrementAndGet();
            }
        }
    }

    /**
     * A test thread performing updates on a test configuration. This thread modifies configurations which are children of a
     * combined configuration. Each update operation adds a value to one of the child configurations and removes it from
     * another one (which contained it before). So if concurrent reads are performed, the test property should always have
     * between 1 and 2 values.
     */
    private static class WriteThread extends Thread {
        /** The list with the child configurations. */
        private final List<Configuration> testConfigs;

        /** The latch for synchronizing thread start. */
        private final CountDownLatch startLatch;

        /** A counter for errors. */
        private final AtomicInteger errorCount;

        /** The number of write operations to be performed. */
        private final int numberOfWrites;

        /** The index of the child configuration containing the test property. */
        private int currentChildConfigIdx;

        /**
         * Creates a new instance of {@code WriteThread}.
         *
         * @param cc the test combined configuration
         * @param latch the latch for synchronizing test start
         * @param errCnt a counter for errors
         * @param writeCount the number of writes to be performed
         */
        public WriteThread(final CombinedConfiguration cc, final CountDownLatch latch, final AtomicInteger errCnt, final int writeCount) {
            testConfigs = cc.getConfigurations();
            startLatch = latch;
            errorCount = errCnt;
            numberOfWrites = writeCount;
        }

        @Override
        public void run() {
            try {
                startLatch.await();
                for (int i = 0; i < numberOfWrites; i++) {
                    updateConfigurations();
                }
            } catch (final InterruptedException e) {
                errorCount.incrementAndGet();
            }
        }

        /**
         * Performs the update operation.
         */
        private void updateConfigurations() {
            final int newIdx = (currentChildConfigIdx + 1) % testConfigs.size();
            testConfigs.get(newIdx).addProperty(KEY_CONCURRENT, TEST_NAME);
            testConfigs.get(currentChildConfigIdx).clearProperty(KEY_CONCURRENT);
            currentChildConfigIdx = newIdx;
        }
    }

    /** Constant for the name of a sub configuration. */
    private static final String TEST_NAME = "SUBCONFIG";

    /** Constant for a test key. */
    private static final String TEST_KEY = "test.value";

    /** Constant for a key to be used for a concurrent test. */
    private static final String KEY_CONCURRENT = "concurrent.access.test";

    /** Constant for the name of the first child configuration. */
    private static final String CHILD1 = TEST_NAME + "1";

    /** Constant for the name of the second child configuration. */
    private static final String CHILD2 = TEST_NAME + "2";

    /** Constant for the key for a sub configuration. */
    private static final String SUB_KEY = "test.sub.config";

    /**
     * Helper method for creating a test configuration to be added to the combined configuration.
     *
     * @return the test configuration
     */
    private static AbstractConfiguration setUpTestConfiguration() {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        config.addProperty(TEST_KEY, Boolean.TRUE);
        config.addProperty("test.comment", "This is a test");
        return config;
    }

    /** The configuration to be tested. */
    private CombinedConfiguration config;

    /** The test event listener. */
    private CombinedListener listener;

    /**
     * Checks if a configuration was correctly added to the combined config.
     *
     * @param c the config to check
     */
    private void checkAddConfig(final AbstractConfiguration c) {
        final Collection<EventListener<? super ConfigurationEvent>> listeners = c.getEventListeners(ConfigurationEvent.ANY);
        assertEquals(1, listeners.size());
        assertTrue(listeners.contains(config));
    }

    /**
     * Helper method for testing that the combined root node has not yet been constructed.
     */
    private void checkCombinedRootNotConstructed() {
        assertTrue(config.getModel().getNodeHandler().getRootNode().getChildren().isEmpty());
    }

    /**
     * Checks the configurationsAt() method.
     *
     * @param withUpdates flag whether updates are supported
     */
    private void checkConfigurationsAt(final boolean withUpdates) {
        setUpSubConfigTest();
        final List<HierarchicalConfiguration<ImmutableNode>> subs = config.configurationsAt(SUB_KEY, withUpdates);
        assertEquals(1, subs.size());
        assertTrue(subs.get(0).getBoolean(TEST_KEY));
    }

    /**
     * Tests whether a configuration was completely removed.
     *
     * @param c the removed configuration
     */
    private void checkRemoveConfig(final AbstractConfiguration c) {
        assertTrue(c.getEventListeners(ConfigurationEvent.ANY).isEmpty());
        assertEquals(0, config.getNumberOfConfigurations());
        assertTrue(config.getConfigurationNames().isEmpty());
        listener.checkEvent(2, 0);
    }

    @BeforeEach
    public void setUp() throws Exception {
        config = new CombinedConfiguration();
        listener = new CombinedListener();
        config.addEventListener(ConfigurationEvent.ANY, listener);
    }

    /**
     * Prepares a test of the getSource() method.
     */
    private void setUpSourceTest() {
        final BaseHierarchicalConfiguration c1 = new BaseHierarchicalConfiguration();
        final PropertiesConfiguration c2 = new PropertiesConfiguration();
        c1.addProperty(TEST_KEY, TEST_NAME);
        c2.addProperty("another.key", "test");
        config.addConfiguration(c1, CHILD1);
        config.addConfiguration(c2, CHILD2);
    }

    /**
     * Prepares the test configuration for a test for sub configurations. Some child configurations are added.
     *
     * @return the sub configuration at the test sub key
     */
    private AbstractConfiguration setUpSubConfigTest() {
        final AbstractConfiguration srcConfig = setUpTestConfiguration();
        config.addConfiguration(srcConfig, "source", SUB_KEY);
        config.addConfiguration(setUpTestConfiguration());
        config.addConfiguration(setUpTestConfiguration(), "otherTest", "other.prefix");
        return srcConfig;
    }

    /**
     * Prepares a test for synchronization. This method installs a test synchronizer and adds some test configurations.
     *
     * @return the test synchronizer
     */
    private SynchronizerTestImpl setUpSynchronizerTest() {
        setUpSourceTest();
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        config.setSynchronizer(sync);
        return sync;
    }

    /**
     * Tests accessing properties if no configurations have been added.
     */
    @Test
    public void testAccessPropertyEmpty() {
        assertFalse(config.containsKey(TEST_KEY));
        assertNull(config.getString("test.comment"));
        assertTrue(config.isEmpty());
    }

    /**
     * Tests accessing properties if multiple configurations have been added.
     */
    @Test
    public void testAccessPropertyMulti() {
        config.addConfiguration(setUpTestConfiguration());
        config.addConfiguration(setUpTestConfiguration(), null, "prefix1");
        config.addConfiguration(setUpTestConfiguration(), null, "prefix2");
        assertTrue(config.getBoolean(TEST_KEY));
        assertTrue(config.getBoolean("prefix1." + TEST_KEY));
        assertTrue(config.getBoolean("prefix2." + TEST_KEY));
        assertFalse(config.isEmpty());
        listener.checkEvent(3, 0);
    }

    /**
     * Tests adding a configuration (without further information).
     */
    @Test
    public void testAddConfiguration() {
        final AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c);
        checkAddConfig(c);
        assertEquals(1, config.getNumberOfConfigurations());
        assertTrue(config.getConfigurationNames().isEmpty());
        assertSame(c, config.getConfiguration(0));
        assertTrue(config.getBoolean(TEST_KEY));
        listener.checkEvent(1, 0);
    }

    /**
     * Tests adding a configuration and specifying an at position.
     */
    @Test
    public void testAddConfigurationAt() {
        final AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c, null, "my");
        checkAddConfig(c);
        assertTrue(config.getBoolean("my." + TEST_KEY));
    }

    /**
     * Tests adding a configuration with a complex at position. Here the at path contains a dot, which must be escaped.
     */
    @Test
    public void testAddConfigurationComplexAt() {
        final AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c, null, "This..is.a.complex");
        checkAddConfig(c);
        assertTrue(config.getBoolean("This..is.a.complex." + TEST_KEY));
    }

    /**
     * Tests whether adding a new configuration is synchronized.
     */
    @Test
    public void testAddConfigurationSynchronized() {
        final SynchronizerTestImpl sync = setUpSynchronizerTest();
        config.addConfiguration(new BaseHierarchicalConfiguration());
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
        checkCombinedRootNotConstructed();
    }

    /**
     * Tests adding a configuration with a name.
     */
    @Test
    public void testAddConfigurationWithName() {
        final AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c, TEST_NAME);
        checkAddConfig(c);
        assertEquals(1, config.getNumberOfConfigurations());
        assertSame(c, config.getConfiguration(0));
        assertSame(c, config.getConfiguration(TEST_NAME));
        final Set<String> names = config.getConfigurationNames();
        assertEquals(1, names.size());
        assertTrue(names.contains(TEST_NAME));
        assertTrue(config.getBoolean(TEST_KEY));
        listener.checkEvent(1, 0);
    }

    /**
     * Tests adding a configuration with a name when this name already exists. This should cause an exception.
     */
    @Test
    public void testAddConfigurationWithNameTwice() {
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME);
        final Configuration configuration = setUpTestConfiguration();
        assertThrows(ConfigurationRuntimeException.class, () -> config.addConfiguration(configuration, TEST_NAME, "prefix"));
    }

    /**
     * Tests adding a null configuration. This should cause an exception to be thrown.
     */
    @Test
    public void testAddNullConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> config.addConfiguration(null));
    }

    /**
     * Tests clearing a combined configuration. This should remove all contained configurations.
     */
    @Test
    public void testClear() {
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME, "test");
        config.addConfiguration(setUpTestConfiguration());

        config.clear();
        assertEquals(0, config.getNumberOfConfigurations());
        assertTrue(config.getConfigurationNames().isEmpty());
        assertTrue(config.isEmpty());

        listener.checkEvent(3, 2);
    }

    /**
     * Tests whether the combined configuration removes itself as change listener from the child configurations on a clear
     * operation. This test is related to CONFIGURATION-572.
     */
    @Test
    public void testClearRemoveChildListener() {
        final AbstractConfiguration child = setUpTestConfiguration();
        config.addConfiguration(child);

        config.clear();
        for (final EventListener<?> listener : child.getEventListeners(ConfigurationEvent.ANY)) {
            assertNotEquals(config, listener);
        }
    }

    /**
     * Tests cloning a combined configuration.
     */
    @Test
    public void testClone() {
        config.addConfiguration(setUpTestConfiguration());
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME, "conf2");
        config.addConfiguration(new PropertiesConfiguration(), "props");

        final CombinedConfiguration cc2 = (CombinedConfiguration) config.clone();
        assertNotNull(cc2.getModel().getNodeHandler().getRootNode());
        assertEquals(config.getNumberOfConfigurations(), cc2.getNumberOfConfigurations());
        assertSame(config.getNodeCombiner(), cc2.getNodeCombiner());
        assertEquals(config.getConfigurationNames().size(), cc2.getConfigurationNames().size());
        assertTrue(Collections.disjoint(cc2.getEventListeners(ConfigurationEvent.ANY), config.getEventListeners(ConfigurationEvent.ANY)));

        final StrictConfigurationComparator comp = new StrictConfigurationComparator();
        for (int i = 0; i < config.getNumberOfConfigurations(); i++) {
            assertNotSame(config.getConfiguration(i), cc2.getConfiguration(i), "Configuration at " + i + " was not cloned");
            assertEquals(config.getConfiguration(i).getClass(), cc2.getConfiguration(i).getClass(), "Wrong config class at " + i);
            assertTrue(comp.compare(config.getConfiguration(i), cc2.getConfiguration(i)), "Configs not equal at " + i);
        }

        assertTrue(comp.compare(config, cc2));
    }

    /**
     * Tests if the cloned configuration is decoupled from the original.
     */
    @Test
    public void testCloneModify() {
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME);
        final CombinedConfiguration cc2 = (CombinedConfiguration) config.clone();
        assertTrue(cc2.getConfigurationNames().contains(TEST_NAME));
        cc2.removeConfiguration(TEST_NAME);
        assertFalse(config.getConfigurationNames().isEmpty());
    }

    /**
     * Tests whether cloning of a configuration is correctly synchronized.
     */
    @Test
    public void testCloneSynchronized() {
        setUpSourceTest();
        config.lock(LockMode.READ); // Causes the root node to be constructed
        config.unlock(LockMode.READ);
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        config.setSynchronizer(sync);
        config.clone();
        // clone() of base class is wrapped by another read lock
        sync.verifyStart(Methods.BEGIN_READ, Methods.BEGIN_READ);
        sync.verifyEnd(Methods.END_READ, Methods.END_READ);
    }

    /**
     * Tests whether a combined configuration can be copied to an XML configuration. This test is related to
     * CONFIGURATION-445.
     */
    @Test
    public void testCombinedCopyToXML() throws ConfigurationException {
        final XMLConfiguration x1 = new XMLConfiguration();
        x1.addProperty("key1", "value1");
        x1.addProperty("key1[@override]", "USER1");
        x1.addProperty("key2", "value2");
        x1.addProperty("key2[@override]", "USER2");
        final XMLConfiguration x2 = new XMLConfiguration();
        x2.addProperty("key2", "value2.2");
        x2.addProperty("key2[@override]", "USER2");
        config.setNodeCombiner(new OverrideCombiner());
        config.addConfiguration(x2);
        config.addConfiguration(x1);
        XMLConfiguration x3 = new XMLConfiguration(config);
        assertEquals("value2.2", x3.getString("key2"));
        assertEquals("USER2", x3.getString("key2[@override]"));
        final StringWriter w = new StringWriter();
        new FileHandler(x3).save(w);
        final String s = w.toString();
        x3 = new XMLConfiguration();
        new FileHandler(x3).load(new StringReader(s));
        assertEquals("value2.2", x3.getString("key2"));
        assertEquals("USER2", x3.getString("key2[@override]"));
    }

    /**
     * Tests concurrent read and write access on a combined configuration. There are multiple reader threads and a single
     * writer thread. It is checked that no inconsistencies occur.
     */
    @Test
    public void testConcurrentAccess() throws ConfigurationException, InterruptedException {
        // populate the test combined configuration
        setUpSourceTest();
        final XMLConfiguration xmlConf = new XMLConfiguration();
        new FileHandler(xmlConf).load(ConfigurationAssert.getTestFile("test.xml"));
        config.addConfiguration(xmlConf);
        final PropertiesConfiguration propConf = new PropertiesConfiguration();
        new FileHandler(propConf).load(ConfigurationAssert.getTestFile("test.properties"));
        for (int i = 0; i < 8; i++) {
            config.addConfiguration(new BaseHierarchicalConfiguration());
        }
        config.getConfiguration(0).addProperty(KEY_CONCURRENT, TEST_NAME);

        // Set a single synchronizer for all involved configurations
        final Synchronizer sync = new ReadWriteSynchronizer();
        config.setSynchronizer(sync);
        for (final Configuration c : config.getConfigurations()) {
            c.setSynchronizer(sync);
        }

        // setup test threads
        final int numberOfReaders = 3;
        final int readCount = 5000;
        final int writeCount = 3000;
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger errorCount = new AtomicInteger();
        final Collection<Thread> threads = new ArrayList<>(numberOfReaders + 1);
        final Thread writeThread = new WriteThread(config, latch, errorCount, writeCount);
        writeThread.start();
        threads.add(writeThread);
        for (int i = 0; i < numberOfReaders; i++) {
            final Thread readThread = new ReadThread(config, latch, errorCount, readCount);
            readThread.start();
            threads.add(readThread);
        }

        // perform test
        latch.countDown();
        for (final Thread t : threads) {
            t.join();
        }
        assertEquals(0, errorCount.get());
    }

    /**
     * Tests whether sub configurations can be created from a key.
     */
    @Test
    public void testConfigurationsAt() {
        checkConfigurationsAt(false);
    }

    /**
     * Tests whether sub configurations can be created which are attached.
     */
    @Test
    public void testConfigurationsAtWithUpdates() {
        checkConfigurationsAt(true);
    }

    /**
     * Tests using a conversion expression engine for child configurations with strange keys. This test is related to
     * CONFIGURATION-336.
     */
    @Test
    public void testConversionExpressionEngine() {
        final PropertiesConfiguration child = new PropertiesConfiguration();
        child.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        child.addProperty("test(a)", "1,2,3");
        config.addConfiguration(child);
        final DefaultExpressionEngine engineQuery = new DefaultExpressionEngine(
            new DefaultExpressionEngineSymbols.Builder(DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS).setIndexStart("<").setIndexEnd(">").create());
        config.setExpressionEngine(engineQuery);
        final DefaultExpressionEngine engineConvert = new DefaultExpressionEngine(
            new DefaultExpressionEngineSymbols.Builder(DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS).setIndexStart("[").setIndexEnd("]").create());
        config.setConversionExpressionEngine(engineConvert);
        assertEquals("1", config.getString("test(a)<0>"));
        assertEquals("2", config.getString("test(a)<1>"));
        assertEquals("3", config.getString("test(a)<2>"));
    }

    /**
     * Tests whether escaped list delimiters are treated correctly.
     */
    @Test
    public void testEscapeListDelimiters() {
        final PropertiesConfiguration sub = new PropertiesConfiguration();
        sub.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        sub.addProperty("test.pi", "3\\,1415");
        config.addConfiguration(sub);
        assertEquals("3,1415", config.getString("test.pi"));
    }

    /**
     * Tests whether access to a configuration by index is correctly synchronized.
     */
    @Test
    public void testGetConfigurationByIdxSynchronized() {
        final SynchronizerTestImpl sync = setUpSynchronizerTest();
        assertNotNull(config.getConfiguration(0));
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
        checkCombinedRootNotConstructed();
    }

    /**
     * Tests whether access to a configuration by name is correctly synchronized.
     */
    @Test
    public void testGetConfigurationByNameSynchronized() {
        final SynchronizerTestImpl sync = setUpSynchronizerTest();
        assertNotNull(config.getConfiguration(CHILD1));
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
        checkCombinedRootNotConstructed();
    }

    @Test
    public void testGetConfigurationNameList() throws Exception {
        config.addConfiguration(setUpTestConfiguration());
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME, "conf2");
        final AbstractConfiguration pc = new PropertiesConfiguration();
        config.addConfiguration(pc, "props");
        final List<String> list = config.getConfigurationNameList();
        assertNotNull(list);
        assertEquals(3, list.size());
        final String name = list.get(1);
        assertNotNull(name);
        assertEquals(TEST_NAME, name);
    }

    /**
     * Tests whether querying the name list of child configurations is synchronized.
     */
    @Test
    public void testGetConfigurationNameListSynchronized() {
        final SynchronizerTestImpl sync = setUpSynchronizerTest();
        assertFalse(config.getConfigurationNameList().isEmpty());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
        checkCombinedRootNotConstructed();
    }

    /**
     * Tests whether querying the name set of child configurations is synchronized.
     */
    @Test
    public void testGetConfigurationNamesSynchronized() {
        final SynchronizerTestImpl sync = setUpSynchronizerTest();
        assertFalse(config.getConfigurationNames().isEmpty());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
        checkCombinedRootNotConstructed();
    }

    @Test
    public void testGetConfigurations() throws Exception {
        config.addConfiguration(setUpTestConfiguration());
        config.addConfiguration(setUpTestConfiguration(), TEST_NAME, "conf2");
        final AbstractConfiguration pc = new PropertiesConfiguration();
        config.addConfiguration(pc, "props");
        final List<Configuration> list = config.getConfigurations();
        assertNotNull(list);
        assertEquals(3, list.size());
        final Configuration c = list.get(2);
        assertSame(pc, c);
    }

    /**
     * Tests whether querying the list of child configurations is synchronized.
     */
    @Test
    public void testGetConfigurationsSynchronized() {
        final SynchronizerTestImpl sync = setUpSynchronizerTest();
        assertFalse(config.getConfigurations().isEmpty());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
        checkCombinedRootNotConstructed();
    }

    /**
     * Tests whether read access to the conversion expression engine is synchronized.
     */
    @Test
    public void testGetConversionExpressionEngineSynchronized() {
        final SynchronizerTestImpl sync = setUpSynchronizerTest();
        assertNull(config.getConversionExpressionEngine());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
        checkCombinedRootNotConstructed();
    }

    /**
     * Tests CONFIGURATION-799.
     */
    @Test
    public void testGetKeys() {
        // Set up
        final BaseConfiguration conf1 = new BaseConfiguration();
        final String key = "x1";
        conf1.addProperty(key, 1);

        final CombinedConfiguration conf2 = new CombinedConfiguration();
        conf2.addConfiguration(conf1, null, "");

        // Actual test
        final Iterator<String> keys = conf2.getKeys();
        assertEquals(key, keys.next());
        assertFalse(keys.hasNext());
    }

    /**
     * Tests whether getNodeCombiner() is correctly synchronized.
     */
    @Test
    public void testGetNodeCombinerSynchronized() {
        final SynchronizerTestImpl sync = setUpSynchronizerTest();
        assertNotNull(config.getNodeCombiner());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
        checkCombinedRootNotConstructed();
    }

    /**
     * Tests whether querying the number of child configurations is synchronized.
     */
    @Test
    public void testGetNumberOfConfigurationsSynchronized() {
        final SynchronizerTestImpl sync = setUpSynchronizerTest();
        assertEquals(2, config.getNumberOfConfigurations());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
        checkCombinedRootNotConstructed();
    }

    /**
     * Tests the getSource() method when the passed in key belongs to the combined configuration itself.
     */
    @Test
    public void testGetSourceCombined() {
        setUpSourceTest();
        final String key = "yet.another.key";
        config.addProperty(key, Boolean.TRUE);
        assertEquals(config, config.getSource(key));
    }

    /**
     * Tests the gestSource() method when the source property is defined in a hierarchical configuration.
     */
    @Test
    public void testGetSourceHierarchical() {
        setUpSourceTest();
        assertEquals(config.getConfiguration(CHILD1), config.getSource(TEST_KEY));
    }

    /**
     * Tests the getSource() method when the passed in key refers to multiple values, which are all defined in the same
     * source configuration.
     */
    @Test
    public void testGetSourceMulti() {
        setUpSourceTest();
        final String key = "list.key";
        config.getConfiguration(CHILD1).addProperty(key, "1,2,3");
        assertEquals(config.getConfiguration(CHILD1), config.getSource(key));
    }

    /**
     * Tests the getSource() method when the passed in key refers to multiple values defined by different sources. This
     * should cause an exception.
     */
    @Test
    public void testGetSourceMultiSources() {
        setUpSourceTest();
        final String key = "list.key";
        config.getConfiguration(CHILD1).addProperty(key, "1,2,3");
        config.getConfiguration(CHILD2).addProperty(key, "a,b,c");
        assertThrows(IllegalArgumentException.class, () -> config.getSource(key));
    }

    /**
     * Tests whether the source configuration can be detected for non hierarchical configurations.
     */
    @Test
    public void testGetSourceNonHierarchical() {
        setUpSourceTest();
        assertEquals(config.getConfiguration(CHILD2), config.getSource("another.key"));
    }

    /**
     * Tests the getSource() method when a null key is passed in. This should cause an exception.
     */
    @Test
    public void testGetSourceNull() {
        assertThrows(IllegalArgumentException.class, () -> config.getSource(null));
    }

    /**
     * Tests whether multiple sources of a key can be retrieved.
     */
    @Test
    public void testGetSourcesMultiSources() {
        setUpSourceTest();
        final String key = "list.key";
        config.getConfiguration(CHILD1).addProperty(key, "1,2,3");
        config.getConfiguration(CHILD2).addProperty(key, "a,b,c");
        final Set<Configuration> sources = config.getSources(key);
        assertEquals(2, sources.size());
        assertTrue(sources.contains(config.getConfiguration(CHILD1)));
        assertTrue(sources.contains(config.getConfiguration(CHILD2)));
    }

    /**
     * Tests getSources() for a non existing key.
     */
    @Test
    public void testGetSourcesUnknownKey() {
        setUpSourceTest();
        assertTrue(config.getSources("non.existing,key").isEmpty());
    }

    /**
     * Tests whether getSource() is correctly synchronized.
     */
    @Test
    public void testGetSourceSynchronized() {
        final SynchronizerTestImpl sync = setUpSynchronizerTest();
        assertNotNull(config.getSource(TEST_KEY));
        sync.verifyStart(Methods.BEGIN_READ);
        sync.verifyEnd(Methods.END_READ);
    }

    /**
     * Tests the getSource() method when the passed in key is not contained. Result should be null in this case.
     */
    @Test
    public void testGetSourceUnknown() {
        setUpSourceTest();
        assertNull(config.getSource("an.unknown.key"));
    }

    /**
     * Tests getSource() if a child configuration is again a combined configuration.
     */
    @Test
    public void testGetSourceWithCombinedChildConfiguration() {
        setUpSourceTest();
        final CombinedConfiguration cc = new CombinedConfiguration();
        cc.addConfiguration(config);
        assertEquals(config, cc.getSource(TEST_KEY));
    }

    /**
     * Tests accessing a newly created combined configuration.
     */
    @Test
    public void testInit() {
        assertEquals(0, config.getNumberOfConfigurations());
        assertTrue(config.getConfigurationNames().isEmpty());
        assertInstanceOf(UnionCombiner.class, config.getNodeCombiner());
        assertNull(config.getConfiguration(TEST_NAME));
    }

    /**
     * Tests whether only a single invalidate event is fired for a change. This test is related to CONFIGURATION-315.
     */
    @Test
    public void testInvalidateEventBeforeAndAfterChange() {
        ConfigurationEvent event = new ConfigurationEvent(config, ConfigurationEvent.ANY, null, null, true);
        config.onEvent(event);
        assertEquals(1, listener.invalidateEvents);
        event = new ConfigurationEvent(config, ConfigurationEvent.ANY, null, null, false);
        config.onEvent(event);
        assertEquals(1, listener.invalidateEvents);
    }

    /**
     * Tests whether invalidate() performs correct synchronization.
     */
    @Test
    public void testInvalidateSynchronized() {
        final SynchronizerTestImpl sync = setUpSynchronizerTest();
        config.invalidate();
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests whether requested locks are freed correctly if an exception occurs while constructing the root node.
     */
    @Test
    public void testLockHandlingWithExceptionWhenConstructingRootNode() {
        final SynchronizerTestImpl sync = setUpSynchronizerTest();
        final RuntimeException testEx = new ConfigurationRuntimeException("Test exception");
        final BaseHierarchicalConfiguration childEx = new BaseHierarchicalConfiguration() {
            @Override
            public NodeModel<ImmutableNode> getModel() {
                throw testEx;
            }
        };
        config.addConfiguration(childEx);
        final Exception ex = assertThrows(Exception.class, () -> config.lock(LockMode.READ));
        assertEquals(testEx, ex);
        // 1 x add configuration, then obtain read lock and create root node
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE, Methods.BEGIN_READ, Methods.END_READ, Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests removing a configuration.
     */
    @Test
    public void testRemoveConfiguration() {
        final AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c);
        checkAddConfig(c);
        assertTrue(config.removeConfiguration(c));
        checkRemoveConfig(c);
    }

    /**
     * Tests removing a configuration by index.
     */
    @Test
    public void testRemoveConfigurationAt() {
        final AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c);
        assertSame(c, config.removeConfigurationAt(0));
        checkRemoveConfig(c);
    }

    /**
     * Tests removing a configuration by name.
     */
    @Test
    public void testRemoveConfigurationByName() {
        final AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c, TEST_NAME);
        assertSame(c, config.removeConfiguration(TEST_NAME));
        checkRemoveConfig(c);
    }

    /**
     * Tests removing a configuration by name, which is not contained.
     */
    @Test
    public void testRemoveConfigurationByUnknownName() {
        assertNull(config.removeConfiguration("unknownName"));
        listener.checkEvent(0, 0);
    }

    /**
     * Tests removing a configuration with a name.
     */
    @Test
    public void testRemoveNamedConfiguration() {
        final AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c, TEST_NAME);
        config.removeConfiguration(c);
        checkRemoveConfig(c);
    }

    /**
     * Tests removing a named configuration by index.
     */
    @Test
    public void testRemoveNamedConfigurationAt() {
        final AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c, TEST_NAME);
        assertSame(c, config.removeConfigurationAt(0));
        checkRemoveConfig(c);
    }

    /**
     * Tests removing a configuration that was not added prior.
     */
    @Test
    public void testRemoveNonContainedConfiguration() {
        assertFalse(config.removeConfiguration(setUpTestConfiguration()));
        listener.checkEvent(0, 0);
    }

    /**
     * Tests whether write access to the conversion expression engine is synchronized.
     */
    @Test
    public void testSetConversionExpressionEngineSynchronized() {
        final SynchronizerTestImpl sync = setUpSynchronizerTest();
        config.setConversionExpressionEngine(new DefaultExpressionEngine(DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS));
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
        checkCombinedRootNotConstructed();
    }

    /**
     * Tests if setting a node combiner causes an invalidation.
     */
    @Test
    public void testSetNodeCombiner() {
        final NodeCombiner combiner = new UnionCombiner();
        config.setNodeCombiner(combiner);
        assertSame(combiner, config.getNodeCombiner());
        listener.checkEvent(1, 0);
    }

    /**
     * Tests whether setNodeCombiner() is correctly synchronized.
     */
    @Test
    public void testSetNodeCombinerSynchronized() {
        final SynchronizerTestImpl sync = setUpSynchronizerTest();
        config.setNodeCombiner(new UnionCombiner());
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
        checkCombinedRootNotConstructed();
    }

    /**
     * Tests setting a null node combiner. This should cause an exception.
     */
    @Test
    public void testSetNullNodeCombiner() {
        assertThrows(IllegalArgumentException.class, () -> config.setNodeCombiner(null));
    }

    /**
     * Tests whether a sub configuration survives updates of its parent.
     */
    @Test
    public void testSubConfigurationWithUpdates() {
        final AbstractConfiguration srcConfig = setUpSubConfigTest();
        final HierarchicalConfiguration<ImmutableNode> sub = config.configurationAt(SUB_KEY, true);
        assertTrue(sub.getBoolean(TEST_KEY));
        srcConfig.setProperty(TEST_KEY, Boolean.FALSE);
        assertFalse(sub.getBoolean(TEST_KEY));
        assertFalse(config.getBoolean(SUB_KEY + '.' + TEST_KEY));
    }

    /**
     * Tests if an update of a contained configuration leeds to an invalidation of the combined configuration.
     */
    @Test
    public void testUpdateContainedConfiguration() {
        final AbstractConfiguration c = setUpTestConfiguration();
        config.addConfiguration(c);
        c.addProperty("test.otherTest", "yes");
        assertEquals("yes", config.getString("test.otherTest"));
        listener.checkEvent(2, 0);
    }
}
