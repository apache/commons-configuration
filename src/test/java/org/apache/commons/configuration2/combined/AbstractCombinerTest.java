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
package org.apache.commons.configuration2.combined;

import java.io.File;
import java.util.Collection;

import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.ConfigurationException;
import org.apache.commons.configuration2.InMemoryConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.expr.ConfigurationNodeHandler;
import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;

import junit.framework.TestCase;

/**
 * A base class for testing combiner implementations. This base class provides
 * some functionality for loading the test configurations, which are to be
 * combined. Concrete sub classes only need to create the correct combiner
 * object.
 *
 * @version $Id$
 */
public abstract class AbstractCombinerTest extends TestCase
{
    /** Constant for the first test configuration. */
    private static File CONF1 = ConfigurationAssert
            .getTestFile("testcombine1.xml");

    /** Constant for the second test configuration. */
    private static File CONF2 = ConfigurationAssert
            .getTestFile("testcombine2.xml");

    /** The node handler for configuration nodes. */
    protected NodeHandler<ConfigurationNode> handler;

    /** The combiner to be tested. */
    protected NodeCombiner combiner;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        handler = new ConfigurationNodeHandler();
        combiner = createCombiner();
    }

    /**
     * Creates the combiner to be tested. This method is called by
     * <code>setUp()</code>. It must be implemented in concrete sub classes.
     *
     * @return the combiner to be tested
     */
    protected abstract NodeCombiner createCombiner();

    /**
     * Constructs a union configuration based on the source configurations.
     *
     * @return the union configuration
     * @throws ConfigurationException if an error occurs
     */
    protected InMemoryConfiguration createCombinedConfiguration()
            throws ConfigurationException
    {
        XMLConfiguration conf1 = new XMLConfiguration(CONF1);
        XMLConfiguration conf2 = new XMLConfiguration(CONF2);
        CombinedNode cn = combiner.combine(conf1.getRootNode(), handler, conf2
                .getRootNode(), handler);

        InMemoryConfiguration result = new InMemoryConfiguration();
        result.setRootNode(convert(cn));

        return result;
    }

    /**
     * Converts a node hierarchy containing combined nodes and configuration
     * nodes into a structure that only consists of configuration nodes.
     *
     * @param node the root combined node
     * @return the corresponding root configuration node
     */
    private ConfigurationNode convert(Object node)
    {
        if (node instanceof ConfigurationNode)
        {
            return (ConfigurationNode) node;
        }

        CombinedNode cn = (CombinedNode) node;
        ConfigurationNode result = new DefaultConfigurationNode(cn.getName(),
                cn.getValue());

        for (String n : cn.getAttributes())
        {
            Object v = cn.getAttribute(n);
            if (v instanceof Collection)
            {
                for (Object av : (Collection<?>) v)
                {
                    result.addAttribute(new DefaultConfigurationNode(n, av));
                }
            }
            else
            {
                result.addAttribute(new DefaultConfigurationNode(n, v));
            }
        }

        for (Object c : cn.getChildren())
        {
            result.addChild(convert(c));
        }

        return result;
    }

    /**
     * Tests a newly created combiner.
     */
    public void testInit()
    {
        assertTrue("Combiner has list nodes", combiner.getListNodes().isEmpty());
        assertFalse("Node is list node", combiner.isListNode(
                new DefaultConfigurationNode("test"), handler));
    }
}
