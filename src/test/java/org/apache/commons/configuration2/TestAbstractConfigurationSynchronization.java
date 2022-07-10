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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.apache.commons.configuration2.SynchronizerTestImpl.Methods;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.sync.LockMode;
import org.apache.commons.configuration2.sync.NoOpSynchronizer;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A test class for the synchronization capabilities of {@code AbstractConfiguration}. This class mainly checks the
 * collaboration between a configuration object and its {@code Synchronizer}.
 *
 */
public class TestAbstractConfigurationSynchronization {
    /** Constant for the test property accessed by all tests. */
    private static final String PROP = "configuration.loaded";

    /**
     * Prepares a mock configuration for a copy operation.
     *
     * @return the mock configuration
     */
    private static Configuration prepareConfigurationMockForCopy() {
        final Configuration config2 = EasyMock.createStrictMock(Configuration.class);
        config2.lock(LockMode.READ);
        EasyMock.expect(config2.getKeys()).andReturn(Collections.<String>emptySet().iterator());
        config2.unlock(LockMode.READ);
        EasyMock.replay(config2);
        return config2;
    }

    /** The synchronizer used for testing. */
    private SynchronizerTestImpl sync;

    /** A test configuration. */
    private AbstractConfiguration config;

    @BeforeEach
    public void setUp() throws Exception {
        // any concrete class will do
        final PropertiesConfiguration c = new PropertiesConfiguration();
        new FileHandler(c).load(ConfigurationAssert.getTestFile("test.properties"));
        sync = new SynchronizerTestImpl();
        c.setSynchronizer(sync);
        config = c;
    }

    /**
     * Tests the correct synchronization of addProperty().
     */
    @Test
    public void testAddPropertySynchronized() {
        config.addProperty(PROP, "of course");
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests whether the append() method uses synchronization.
     */
    @Test
    public void testAppendSynchronized() {
        final Configuration config2 = prepareConfigurationMockForCopy();
        config.append(config2);
        EasyMock.verify(config2);
    }

    /**
     * Tests the correct synchronization of clearProperty().
     */
    @Test
    public void testClearPropertySynchronized() {
        config.clearProperty(PROP);
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests the correct synchronization of clear().
     */
    @Test
    public void testClearSynchronized() {
        config.clear();
        sync.verifyStart(Methods.BEGIN_WRITE);
        sync.verifyEnd(Methods.END_WRITE);
    }

    /**
     * Tests whether containsKey() is correctly synchronized.
     */
    @Test
    public void testContainsKeySychronized() {
        assertTrue(config.containsKey(PROP), "Wrong result");
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether the copy() method uses synchronization.
     */
    @Test
    public void testCopySynchronized() {
        final Configuration config2 = prepareConfigurationMockForCopy();
        config.copy(config2);
        EasyMock.verify(config2);
    }

    /**
     * Tests the Synchronizer used by default.
     */
    @Test
    public void testDefaultSynchronizer() {
        assertSame(NoOpSynchronizer.INSTANCE, new PropertiesConfiguration().getSynchronizer(), "Wrong default synchronizer");
    }

    /**
     * Tests whether getKeys(String prefix) is correctly synchronized.
     */
    @Test
    public void testGetKeysPrefixSynchronized() {
        config.getKeys("test");
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether getKeys() is correctly synchronized.
     */
    @Test
    public void testGetKeysSynchronized() {
        assertTrue(config.getKeys().hasNext(), "No keys");
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether read access to properties is synchronized.
     */
    @Test
    public void testGetPropertySynchronized() {
        assertEquals("true", config.getProperty(PROP), "Wrong raw value");
        assertTrue(config.getBoolean(PROP), "Wrong boolean value");
        sync.verify(Methods.BEGIN_READ, Methods.END_READ, Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether isEmpty() is correctly synchronized.
     */
    @Test
    public void testIsEmptySynchronized() {
        assertFalse(config.isEmpty(), "Configuration is empty");
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests lock() with a null argument.
     */
    @Test
    public void testLockNull() {
        assertThrows(NullPointerException.class, () -> config.lock(null));
    }

    /**
     * Tests whether a read lock can be obtained.
     */
    @Test
    public void testLockRead() {
        config.lock(LockMode.READ);
        sync.verify(Methods.BEGIN_READ);
    }

    /**
     * Tests whether a write lock can be obtained.
     */
    @Test
    public void testLockWrite() {
        config.lock(LockMode.WRITE);
        sync.verify(Methods.BEGIN_WRITE);
    }

    /**
     * Tests the correct synchronization of setProperty().
     */
    @Test
    public void testSetPropertySynchronized() {
        config.setProperty(PROP, "yes");
        sync.verifyStart(Methods.BEGIN_WRITE);
        sync.verifyEnd(Methods.END_WRITE);
    }

    /**
     * Tests whether size() is correctly synchronized.
     */
    @Test
    public void testSizeSynchronized() {
        assertFalse(config.isEmpty(), "Wrong size");
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests synchronization of subset().
     */
    @Test
    public void testSubsetSynchronized() {
        final AbstractConfiguration subset = (AbstractConfiguration) config.subset("configuration");
        sync.verify();
        assertEquals(NoOpSynchronizer.INSTANCE, subset.getSynchronizer(), "Wrong synchronizer for subset");
    }

    /**
     * Tests whether a read lock can be released.
     */
    @Test
    public void testUnlockRead() {
        config.unlock(LockMode.READ);
        sync.verify(Methods.END_READ);
    }

    /**
     * Tests whether a write lock can be released.
     */
    @Test
    public void testUnlockWrite() {
        config.unlock(LockMode.WRITE);
        sync.verify(Methods.END_WRITE);
    }
}
