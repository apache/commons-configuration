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
package org.apache.commons.configuration2.expr.xpath;

import java.util.List;
import java.util.Locale;

import org.apache.commons.configuration2.expr.ConfigurationNodeHandler;
import org.apache.commons.configuration2.tree.ConfigurationNode;
import org.apache.commons.configuration2.tree.DefaultConfigurationNode;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * Test class for ConfigurationIteratorAttributes.
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class TestConfigurationIteratorAttributes extends AbstractXPathTest
{
    /** Constant for the name of another test attribute.*/
    private static final String TEST_ATTR = "test";

    /** Stores the node pointer of the test node.*/
    private ConfigurationNodePointer<ConfigurationNode> pointer;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // Adds further attributes to the test node
        ConfigurationNode testNode = root.getChild(1);
        testNode.addAttribute(new DefaultConfigurationNode(TEST_ATTR, "yes"));
        pointer = new ConfigurationNodePointer<ConfigurationNode>(testNode,
                new ConfigurationNodeHandler(), Locale.getDefault());
    }

    /**
     * Tests to iterate over all attributes.
     */
    public void testIterateAllAttributes()
    {
        ConfigurationNodeIteratorAttribute<ConfigurationNode> it = new ConfigurationNodeIteratorAttribute<ConfigurationNode>(
                pointer, new QName(null, "*"));
        assertEquals("Wrong number of attributes", 2, iteratorSize(it));
        List<NodePointer> attrs = iterationElements(it);
        assertEquals("Wrong number of attributes", 2, attrs.size());
        assertEquals("Wrong 1st attribute", ATTR_NAME, attrs.get(0).getName()
                .getName());
        assertEquals("Wrong 2nd attribute", TEST_ATTR, attrs.get(1).getName()
                .getName());
        assertEquals("Wrong value of 2nd attribute", "yes", attrs.get(1).getValue());
    }

    /**
     * Tests to iterate over attributes with a specific name.
     */
    public void testIterateSpecificAttribute()
    {
        ConfigurationNodeIteratorAttribute<ConfigurationNode> it = new ConfigurationNodeIteratorAttribute<ConfigurationNode>(
                pointer, new QName(null, TEST_ATTR));
        assertEquals("Wrong number of attributes", 1, iteratorSize(it));
        assertEquals("Wrong attribute", TEST_ATTR, iterationElements(it).get(0)
                .getName().getName());
    }

    /**
     * Tests to iterate over non existing attributes.
     */
    public void testIterateUnknownAttribute()
    {
        ConfigurationNodeIteratorAttribute<ConfigurationNode> it = new ConfigurationNodeIteratorAttribute<ConfigurationNode>(
                pointer, new QName(null, "unknown"));
        assertEquals("Found attributes", 0, iteratorSize(it));
    }

    /**
     * Tests iteration when a namespace is specified. This is not supported, so
     * the iteration should be empty.
     */
    public void testIterateNamespace()
    {
        ConfigurationNodeIteratorAttribute<ConfigurationNode> it = new ConfigurationNodeIteratorAttribute<ConfigurationNode>(
                pointer, new QName("test", "*"));
        assertEquals("Found attributes", 0, iteratorSize(it));
    }
}
