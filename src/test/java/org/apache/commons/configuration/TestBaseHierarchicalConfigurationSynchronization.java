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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.apache.commons.configuration.SynchronizerTestImpl.Methods;
import org.apache.commons.configuration.io.FileHandler;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.junit.Before;
import org.junit.Test;

/**
 * A test class for {@code BaseHierarchicalConfiguration} which checks whether
 * the Synchronizer is called correctly by the methods specific for hierarchical
 * configurations.
 *
 * @version $Id: $
 */
public class TestBaseHierarchicalConfigurationSynchronization
{
    /** The test synchronizer. */
    private SynchronizerTestImpl sync;

    /** The test configuration. */
    private BaseHierarchicalConfiguration config;

    @Before
    public void setUp() throws Exception
    {
        XMLConfiguration c = new XMLConfiguration();
        new FileHandler(c).load(ConfigurationAssert.getTestFile("test.xml"));
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
}
