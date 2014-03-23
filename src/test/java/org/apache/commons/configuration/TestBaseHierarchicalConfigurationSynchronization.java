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
package org.apache.commons.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.configuration.SynchronizerTestImpl.Methods;
import org.apache.commons.configuration.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration.builder.fluent.Parameters;
import org.apache.commons.configuration.ex.ConfigurationException;
import org.apache.commons.configuration.io.FileHandler;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.junit.Before;
import org.junit.Test;

/**
 * A test class for {@code BaseHierarchicalConfiguration} which checks whether
 * the Synchronizer is called correctly by the methods specific for hierarchical
 * configurations.
 *
 * @version $Id$
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
        XMLConfiguration c = new XMLConfiguration();
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
        BaseHierarchicalConfiguration clone =
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
        DefaultConfigurationNode node =
                new DefaultConfigurationNode("newNode", "true");
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
     * Tests whether setRootNode() is correctly synchronized.
     */
    @Test
    public void testSetRootNodeSynchronized()
    {
        config.setRootNode(new DefaultConfigurationNode("testRoot"));
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests whether synchronization is performed when copying a configuration.
     */
    @Test
    public void testCopyConstructorSynchronized()
    {
        BaseHierarchicalConfiguration copy =
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
        SubnodeConfiguration sub = config.configurationAt("element2");
        assertEquals("Wrong property", "I'm complex!",
                sub.getString("subelement.subsubelement"));
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE, Methods.BEGIN_READ,
                Methods.END_READ);
    }

    /**
     * Tests whether synchronization is performed when constructing multiple
     * SubnodeConfiguration objects.
     */
    @Test
    public void testConfigurationsAtSynchronized()
    {
        List<SubnodeConfiguration> subs = config.configurationsAt("list.item");
        assertFalse("No subnode configurations", subs.isEmpty());
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests whether childConfigurationsAt() is correctly synchronized.
     */
    @Test
    public void testChildConfigurationsAtSynchronized()
    {
        List<SubnodeConfiguration> subs = config.childConfigurationsAt("clear");
        assertFalse("No subnode configurations", subs.isEmpty());
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests whether synchronization is performed when setting the key of a
     * SubnodeConfiguration.
     */
    @Test
    public void testSetSubnodeKeySynchronized()
    {
        SubnodeConfiguration sub = config.configurationAt("element2");
        assertNull("Got a subnode key", sub.getSubnodeKey());
        sub.setSubnodeKey("element2");
        // 1 x configurationAt(), 1 x getSubnodeKey(), 1 x setSubnodeKey()
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE, Methods.BEGIN_READ,
                Methods.END_READ, Methods.BEGIN_WRITE, Methods.END_WRITE);
    }

    /**
     * Tests whether synchronization is performed when querying the key of a
     * SubnodeConfiguration.
     */
    @Test
    public void testGetSubnodeKeySynchronized()
    {
        SubnodeConfiguration sub = config.configurationAt("element2", true);
        assertEquals("Wrong subnode key", "element2", sub.getSubnodeKey());
        // 1 x configurationAt(), 1 x getSubnodeKey()
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE,
                Methods.BEGIN_READ, Methods.END_READ);
    }

    /**
     * Tests whether updates on nodes are communicated to all
     * SubnodeConfigurations of a configuration.
     */
    @Test
    public void testSubnodeUpdate()
    {
        config.addProperty("element2.test", Boolean.TRUE);
        SubnodeConfiguration sub = config.configurationAt("element2", true);
        SubnodeConfiguration subsub = sub.configurationAt("subelement", true);
        config.clearTree("element2.subelement");
        assertNotNull("Sub1 detached", sub.getSubnodeKey());
        assertNull("Sub2 still attached", subsub.getSubnodeKey());
    }

    /**
     * Tests whether updates caused by a SubnodeConfiguration are communicated
     * to all other SubnodeConfigurations.
     */
    @Test
    public void testSubnodeUpdateBySubnode()
    {
        SubnodeConfiguration sub = config.configurationAt("element2", true);
        SubnodeConfiguration subsub = sub.configurationAt("subelement", true);
        SubnodeConfiguration sub2 =
                config.configurationAt("element2.subelement", true);
        sub.clearTree("subelement");
        // 3 x configurationAt(), 1 x clearTree()
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE,
                Methods.BEGIN_WRITE, Methods.END_WRITE, Methods.BEGIN_WRITE,
                Methods.END_WRITE, Methods.BEGIN_WRITE, Methods.END_WRITE);
        assertNull("Sub2 still attached", sub2.getSubnodeKey());
        assertNull("Subsub still attached", subsub.getSubnodeKey());
    }

    /**
     * Tests whether a clone() operation also copies the data used to manage
     * SubnodeConfiguration objects.
     */
    @Test
    public void testCloneCopySubnodeData()
    {
        final Collection<SubnodeConfiguration> validatedConfigs =
                new LinkedList<SubnodeConfiguration>();

        // A special configuration class which creates SubConfigurations that
        // record validation operations
        BaseHierarchicalConfiguration conf2 =
                new BaseHierarchicalConfiguration(config)
                {
                    private static final long serialVersionUID = 1L;

                };

        SubnodeConfiguration sub =
                conf2.configurationAt("element2.subelement", true);
        HierarchicalConfiguration copy =
                (HierarchicalConfiguration) conf2.clone();
        SubnodeConfiguration sub2 =
                copy.configurationAt("element2.subelement", true);
        // This must not cause a validate operation on sub1, but on sub2
        copy.clearTree("element2");
        assertNull("Sub2 not detached", sub2.getSubnodeKey());
        assertNotNull("Sub 1 was detached", sub.getSubnodeKey());
        assertEquals("Wrong number of validated configs", 1,
                validatedConfigs.size());
        assertSame("Wrong validated config", sub2, validatedConfigs.iterator()
                .next());
    }

    /**
     * Tests whether a SubnodeConfiguration's clearAndDetachFromParent() method
     * is correctly synchronized.
     */
    @Test
    public void testSubnodeClearAndDetachFromParentSynchronized()
    {
        SubnodeConfiguration sub = config.configurationAt("element2", true);
        sub.clearAndDetachFromParent();
        assertFalse("Node not removed", config.containsKey("element2"));
        // configurationAt() + clearTree() + containsKey()
        sync.verify(Methods.BEGIN_WRITE, Methods.END_WRITE,
                Methods.BEGIN_WRITE, Methods.END_WRITE, Methods.BEGIN_READ,
                Methods.END_READ);
    }

    /**
     * Tests whether subset() is correctly synchronized.
     */
    @Test
    public void testSubsetSynchronized()
    {
        Configuration subset = config.subset("test");
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
        FileBasedConfigurationBuilder<XMLConfiguration> builder =
                new FileBasedConfigurationBuilder<XMLConfiguration>(
                        XMLConfiguration.class);
        builder.configure(new Parameters().fileBased().setFile(testFile));
        config = builder.getConfiguration();

        CountDownLatch startLatch = new CountDownLatch(1);
        Collection<SubNodeAccessThread> threads =
                new ArrayList<SubNodeAccessThread>();
        for (int i = 0; i < 4; i++)
        {
            SubNodeAccessThread t =
                    new SubNodeAccessThread(config, startLatch, "element2",
                            "subelement.subsubelement");
            t.start();
            threads.add(t);
        }
        for (int i = 0; i < 4; i++)
        {
            SubNodeAccessThread t =
                    new SubNodeAccessThread(config, startLatch,
                            "element2.subelement", "subsubelement");
            t.start();
            threads.add(t);
        }

        startLatch.countDown();
        for (SubNodeAccessThread t : threads)
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
        private final HierarchicalConfiguration config;

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
        public SubNodeAccessThread(HierarchicalConfiguration c,
                CountDownLatch startLatch, String keySubConfig,
                String keyProperty)
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
                SubnodeConfiguration subConfig = config.configurationAt(keySub);
                value = subConfig.getString(keyProp);
            }
            catch (InterruptedException iex)
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
            catch (InterruptedException e)
            {
                fail("Waiting was interrupted: " + e);
            }
            assertEquals("Wrong value", "I'm complex!", value);
        }
    }
}
