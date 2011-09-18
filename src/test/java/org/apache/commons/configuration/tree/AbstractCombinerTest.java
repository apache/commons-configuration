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
package org.apache.commons.configuration.tree;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.configuration.ConfigurationAssert;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

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
    static File CONF1 = ConfigurationAssert.getTestFile("testcombine1.xml");

    /** Constant for the second test configuration. */
    static File CONF2 = ConfigurationAssert.getTestFile("testcombine2.xml");

    /** The combiner to be tested. */
    protected NodeCombiner combiner;

    protected void setUp() throws Exception
    {
        super.setUp();
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
    protected HierarchicalConfiguration createCombinedConfiguration()
            throws ConfigurationException
    {
        XMLConfiguration conf1 = new XMLConfiguration(CONF1);
        XMLConfiguration conf2 = new XMLConfiguration(CONF2);
        ConfigurationNode cn = combiner.combine(conf1.getRootNode(), conf2
                .getRootNode());

        HierarchicalConfiguration result = new HierarchicalConfiguration();
        result.setRootNode(cn);

        return result;
    }

    /**
     * Tests a newly created combiner.
     */
    public void testInit()
    {
        assertTrue("Combiner has list nodes", combiner.getListNodes().isEmpty());
        assertFalse("Node is list node", combiner
                .isListNode(new DefaultConfigurationNode("test")));
    }

}
