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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.expr.ConfigurationNodeHandler;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link InMemoryConfigurationSource}.
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class TestInMemoryConfigurationSource
{
    /** The source to be tested. */
    private InMemoryConfigurationSource source;

    /**
     * Initializes the configuration source with the following structure: tables
     * table name fields field name field name
     */
    @Before
    public void setUp() throws Exception
    {
        source = new InMemoryConfigurationSource();
    }

    /**
     * Helper method for checking an empty root node.
     */
    private void checkEmptyRoot()
    {
        ConfigurationNode root = source.getRootNode();
        assertNull("Root node has a name", root.getName());
        assertNull("Root node has a value", root.getValue());
        assertNull("Root node has a reference", root.getReference());
        assertEquals("Root node has attributes", 0, root.getAttributeCount());
        assertEquals("Root node has children", 0, root.getChildrenCount());
    }

    /**
     * Adds some test properties to the configuration source to be tested.
     */
    private void populate()
    {
        Configuration<ConfigurationNode> config = new ConfigurationImpl<ConfigurationNode>(source);
        config.addProperty("test", Boolean.TRUE);
        config.addProperty("test.list", Arrays.asList("value1", "value2", "value3"));
        config.addProperty("test[@mode]", "full");
        config.addProperty("answer", 42);
        config.addProperty("database.connection", "jdbc:test:local:db");
        config.addProperty("database.usr", "scott");
        config.addProperty("database.pwd", "tiger");
        config.addProperty("[@rootAttr]", "set");
    }

    /**
     * Tests a newly created instance.
     */
    @Test
    public void testInit()
    {
        checkEmptyRoot();
    }

    /**
     * Tests setting a new root node.
     */
    @Test
    public void testSetRootNode()
    {
        DefaultConfigurationNode node = new DefaultConfigurationNode();
        source.setRootNode(node);
        assertSame("Root node was not changed", node, source.getRootNode());
    }

    /**
     * Tests setting the root node to null. This should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetRootNodeNull()
    {
        source.setRootNode(null);
    }

    /**
     * Tests the clear() implementation.
     */
    @Test
    public void testClear()
    {
        source.getRootNode().setValue("Test");
        source.getRootNode().addChild(
                new DefaultConfigurationNode("test", "value"));
        source.getRootNode().addAttribute(
                new DefaultConfigurationNode("attr", "attrValue"));
        source.clear();
        checkEmptyRoot();
    }

    /**
     * Tests the node handler used by the model.
     */
    @Test
    public void testGetNodeHandler()
    {
        assertTrue("Wrong node handler",
                source.getNodeHandler() instanceof ConfigurationNodeHandler);
    }

    /**
     * Tests the constructor which copies another configuration source.
     */
    @Test
    public void testInitCopy()
    {
        populate();
        InMemoryConfigurationSource src2 = new InMemoryConfigurationSource(
                source);
        ConfigurationAssert.assertEquals(source, src2);
    }

    /**
     * Tests the copy constructor if null is passed in.
     */
    @Test
    public void testInitCopyNull()
    {
        InMemoryConfigurationSource src2 = new InMemoryConfigurationSource(null);
        Configuration<ConfigurationNode> conf = new ConfigurationImpl<ConfigurationNode>(
                src2);
        assertTrue("Not empty", conf.isEmpty());
    }
}
