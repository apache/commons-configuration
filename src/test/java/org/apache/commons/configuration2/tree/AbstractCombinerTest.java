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
package org.apache.commons.configuration2.tree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.ConfigurationAssert;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.junit.Before;
import org.junit.Test;

/**
 * A base class for testing combiner implementations. This base class provides
 * some functionality for loading the test configurations, which are to be
 * combined. Concrete sub classes only need to create the correct combiner
 * object.
 *
 */
public abstract class AbstractCombinerTest
{
    /** Constant for the first test configuration. */
    private static final File CONF1 = ConfigurationAssert.getTestFile("testcombine1.xml");

    /** Constant for the second test configuration. */
    private static final File CONF2 = ConfigurationAssert.getTestFile("testcombine2.xml");

    /** The combiner to be tested. */
    protected NodeCombiner combiner;

    @Before
    public void setUp() throws Exception
    {
        combiner = createCombiner();
    }

    /**
     * Creates the combiner to be tested. This method is called by
     * {@code setUp()}. It must be implemented in concrete sub classes.
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
    protected BaseHierarchicalConfiguration createCombinedConfiguration()
            throws ConfigurationException
    {
        final XMLConfiguration conf1 = new XMLConfiguration();
        new FileHandler(conf1).load(CONF1);
        final XMLConfiguration conf2 = new XMLConfiguration();
        new FileHandler(conf2).load(CONF2);
        final ImmutableNode cn =
                combiner.combine(conf1.getNodeModel().getNodeHandler()
                        .getRootNode(), conf2.getNodeModel().getNodeHandler()
                        .getRootNode());

        final BaseHierarchicalConfiguration result = new BaseHierarchicalConfiguration();
        result.getNodeModel().setRootNode(cn);

        return result;
    }

    /**
     * Tests a newly created combiner.
     */
    @Test
    public void testInit()
    {
        assertTrue("Combiner has list nodes", combiner.getListNodes().isEmpty());
        assertFalse("Node is list node", combiner
                .isListNode(NodeStructureHelper.createNode("test", null)));
    }
}
