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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.configuration2.SynchronizerTestImpl.Methods;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.InMemoryNodeModel;
import org.apache.commons.configuration2.tree.NodeStructureHelper;
import org.junit.Before;
import org.junit.Test;

/**
 * A test class for {@code BaseHierarchicalConfiguration} which checks whether
 * the Synchronizer is called correctly by the methods specific for hierarchical
 * configurations.
 *
 */
public class TestBaseHierarchicalConfigurationSynchronization
{
    /** The test synchronizer. */
    private SynchronizerTestImpl sync;

    /** The test file to be read. */
    private File testFile;

    /** The test configuration. */
    private BaseHierarchicalConfiguration config;

    @Before
    public void setUp() throws Exception
    {
        final XMLConfiguration c = new XMLConfiguration();
        testFile = ConfigurationAssert.getTestFile("test.xml");
        new FileHandler(c).load(testFile);
        sync = new SynchronizerTestImpl();
        c.setSynchronizer(sync);
        config = c;
    }

    /**
     * Tests whether getMaxIndex() is correctly synchronized.
     */
    @Test
    public void testGetMaxIndexSynchronized()
    {
        assertTrue("Wrong max index", config.getMaxIndex("list.item") > 0);
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether getRootElementName() is correctly synchronized.
     */
    @Test
    public void testGetRootElementNameSynchronized()
    {
        assertEquals("Wrong root element name", "testconfig",
                config.getRootElementName());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether clone() is correctly synchronized.
     */
    @Test
    public void testCloneSynchronized()
    {
        final BaseHierarchicalConfiguration clone =
                (BaseHierarchicalConfiguration) config.clone();
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
        assertNotSame("Synchronizer was not cloned", config.getSynchronizer(),
                clone.getSynchronizer());
    }

    /**
     * Tests whether addNodes() is correctly synchronized.
     */
    @Test
    public void testAddNodesSynchronized()
    {
        final ImmutableNode node = NodeStructureHelper.createNode("newNode", "true");
        config.addNodes("test.addNodes", Collections.singleton(node));
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests whether clearTree() is correctly synchronized.
     */
    @Test
    public void testClearTreeSynchronized()
    {
        config.clearTree("clear");
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests whether synchronization is performed when copying a configuration.
     */
    @Test
    public void testCopyConstructorSynchronized()
    {
        final BaseHierarchicalConfiguration copy =
                new BaseHierarchicalConfiguration(config);
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
        assertNotSame("Synchronizer was copied", sync, copy.getSynchronizer());
    }

    /**
     * Tests whether synchronization is performed when constructing a
     * SubnodeConfiguration.
     */
    @Test
    public void testConfigurationAtSynchronized()
    {
        final HierarchicalConfiguration<ImmutableNode> sub = config.configurationAt("element2");
        assertEquals("Wrong property", "I'm complex!",
                sub.getString("subelement.subsubelement"));
        sync.verify(Methods.BEGIN_READ, Methods.END_READ, Methods.BEGIN_READ,
                Methods.END_READ);
    }

    /**
     * Tests whether synchronization is performed when constructing multiple
     * SubnodeConfiguration objects.
     */
    @Test
    public void testConfigurationsAtSynchronized()
    {
        final List<HierarchicalConfiguration<ImmutableNode>> subs =
                config.configurationsAt("list.item");
        assertFalse("No subnode configurations", subs.isEmpty());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether childConfigurationsAt() is correctly synchronized.
     */
    @Test
    public void testChildConfigurationsAtSynchronized()
    {
        final List<HierarchicalConfiguration<ImmutableNode>> subs =
                config.childConfigurationsAt("clear");
        assertFalse("No subnode configurations", subs.isEmpty());
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether the specified configuration is detached.
     *
     * @param c the configuration to test
     * @return a flag whether the root node of this configuration is detached
     */
    private static boolean isDetached(final HierarchicalConfiguration<ImmutableNode> c)
    {
        assertTrue("Not a sub configuration", c instanceof SubnodeConfiguration);
        final InMemoryNodeModel nodeModel = ((SubnodeConfiguration) c).getRootNodeModel();
        return nodeModel.isTrackedNodeDetached(((SubnodeConfiguration) c)
                        .getRootSelector());
    }

    /**
     * Tests whether updates on nodes are communicated to all
     * SubnodeConfigurations of a configuration.
     */
    @Test
    public void testSubnodeUpdate()
    {
        config.addProperty("element2.test", Boolean.TRUE);
        final HierarchicalConfiguration<ImmutableNode> sub =
                config.configurationAt("element2", true);
        final HierarchicalConfiguration<ImmutableNode> subsub =
                sub.configurationAt("subelement", true);
        config.clearTree("element2.subelement");
        assertFalse("Sub1 detached", isDetached(sub));
        assertTrue("Sub2 still attached", isDetached(subsub));
    }

    /**
     * Tests whether updates caused by a SubnodeConfiguration are communicated
     * to all other SubnodeConfigurations.
     */
    @Test
    public void testSubnodeUpdateBySubnode()
    {
        final HierarchicalConfiguration<ImmutableNode> sub =
                config.configurationAt("element2", true);
        final HierarchicalConfiguration<ImmutableNode> subsub =
                sub.configurationAt("subelement", true);
        final HierarchicalConfiguration<ImmutableNode> sub2 =
                config.configurationAt("element2.subelement", true);
        sub.clearTree("subelement");
        assertTrue("Sub2 still attached", isDetached(sub2));
        assertTrue("Subsub still attached", isDetached(subsub));
    }

    /**
     * Tests whether a clone() operation also copies the data used to manage
     * SubnodeConfiguration objects.
     */
    @Test
    public void testCloneCopySubnodeData()
    {
        final BaseHierarchicalConfiguration conf2 =
                new BaseHierarchicalConfiguration(config);

        final HierarchicalConfiguration<ImmutableNode> sub =
                conf2.configurationAt("element2.subelement", true);
        @SuppressWarnings("unchecked") // clone retains the type
        final
        HierarchicalConfiguration<ImmutableNode> copy =
                (HierarchicalConfiguration<ImmutableNode>) conf2.clone();
        final HierarchicalConfiguration<ImmutableNode> sub2 =
                copy.configurationAt("element2.subelement", true);
        // This must not cause a validate operation on sub1, but on sub2
        copy.clearTree("element2");
        assertTrue("Sub2 not detached", isDetached(sub2));
        assertFalse("Sub 1 was detached", isDetached(sub));
    }

    /**
     * Tests whether subset() is correctly synchronized.
     */
    @Test
    public void testSubsetSynchronized()
    {
        final Configuration subset = config.subset("test");
        sync.verify(Methods.BEGIN_READ, Methods.END_READ);
        assertSame("Wrong Synchronizer", sync, subset.getSynchronizer());
    }

    /**
     * Tests that access to an initialized configuration's sub configurations is
     * possible without a special synchronizer.
     */
    @Test
    public void testReadOnlyAccessToSubConfigurations()
            throws ConfigurationException
    {
        final FileBasedConfigurationBuilder<XMLConfiguration> builder =
                new FileBasedConfigurationBuilder<>(
                        XMLConfiguration.class);
        builder.configure(new Parameters().fileBased().setFile(testFile));
        config = builder.getConfiguration();

        final CountDownLatch startLatch = new CountDownLatch(1);
        final Collection<SubNodeAccessThread> threads =
                new ArrayList<>();
        for (int i = 0; i < 4; i++)
        {
            final SubNodeAccessThread t =
                    new SubNodeAccessThread(config, startLatch, "element2",
                            "subelement.subsubelement");
            t.start();
            threads.add(t);
        }
        for (int i = 0; i < 4; i++)
        {
            final SubNodeAccessThread t =
                    new SubNodeAccessThread(config, startLatch,
                            "element2.subelement", "subsubelement");
            t.start();
            threads.add(t);
        }

        startLatch.countDown();
        for (final SubNodeAccessThread t : threads)
        {
            t.verify();
        }
    }

    /**
     * A thread class for testing concurrent access to SubNode configurations.
     */
    private static class SubNodeAccessThread extends Thread
    {
        /** The test configuration. */
        private final HierarchicalConfiguration<ImmutableNode> config;

        /** The latch for synchronizing thread start. */
        private final CountDownLatch latch;

        /** The key for the sub configuration. */
        private final String keySub;

        /** The key for the property. */
        private final String keyProp;

        /** The value read by this thread. */
        private String value;

        /**
         * Creates a new instance of {@code SubNodeAccessThread}
         *
         * @param c the test configuration
         * @param startLatch the start latch
         * @param keySubConfig the key for the sub configuration
         * @param keyProperty the key for the property
         */
        public SubNodeAccessThread(final HierarchicalConfiguration<ImmutableNode> c,
                final CountDownLatch startLatch, final String keySubConfig,
                final String keyProperty)
        {
            config = c;
            latch = startLatch;
            keySub = keySubConfig;
            keyProp = keyProperty;
        }

        @Override
        public void run()
        {
            try
            {
                latch.await();
                final HierarchicalConfiguration<ImmutableNode> subConfig =
                        config.configurationAt(keySub, true);
                value = subConfig.getString(keyProp);
            }
            catch (final InterruptedException iex)
            {
                // ignore
            }
        }

        /**
         * Verifies that the correct value was read.
         */
        public void verify()
        {
            try
            {
                join();
            }
            catch (final InterruptedException e)
            {
                fail("Waiting was interrupted: " + e);
            }
            assertEquals("Wrong value", "I'm complex!", value);
        }
    }
}
