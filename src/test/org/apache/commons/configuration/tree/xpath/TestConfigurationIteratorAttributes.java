/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration.tree.xpath;

import java.util.List;
import java.util.Locale;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * Test class for ConfigurationIteratorAttributes.
 * 
 * @author Oliver Heger
 * @version $Id$
 */
public class TestConfigurationIteratorAttributes extends XPathTest
{
    /** Constant for the name of another test attribute.*/
    private static final String TEST_ATTR = "test";
    
    /** Stores the node pointer of the test node.*/
    NodePointer pointer;
    
    protected void setUp() throws Exception
    {
        super.setUp();
        
        // Adds further attributes to the test node
        ConfigurationNode testNode = root.getChild(1);
        testNode.addAttribute(new DefaultConfigurationNode(TEST_ATTR, "yes"));
        pointer = new ConfigurationNodePointer(testNode, Locale.getDefault());
    }

    /**
     * Tests to iterate over all attributes.
     */
    public void testIterateAllAttributes()
    {
        ConfigurationNodeIteratorAttribute it = new ConfigurationNodeIteratorAttribute(pointer, new QName(null, "*"));
        assertEquals("Wrong number of attributes", 2, iteratorSize(it));
        List attrs = iterationElements(it);
        assertEquals("Wrong first attribute", ATTR_NAME, ((ConfigurationNode) attrs.get(0)).getName());
        assertEquals("Wrong first attribute", TEST_ATTR, ((ConfigurationNode) attrs.get(1)).getName());
    }
    
    /**
     * Tests to iterate over attributes with a specific name.
     */
    public void testIterateSpecificAttribute()
    {
        ConfigurationNodeIteratorAttribute it = new ConfigurationNodeIteratorAttribute(pointer, new QName(null, TEST_ATTR));
        assertEquals("Wrong number of attributes", 1, iteratorSize(it));
        assertEquals("Wrong attribute", TEST_ATTR, ((ConfigurationNode) iterationElements(it).get(0)).getName());
    }
    
    /**
     * Tests to iterate over non existing attributes.
     */
    public void testIterateUnknownAttribute()
    {
        ConfigurationNodeIteratorAttribute it = new ConfigurationNodeIteratorAttribute(pointer, new QName(null, "unknown"));
        assertEquals("Found attributes", 0, iteratorSize(it));
    }
    
    /**
     * Tests iteration when a namespace is specified. This is not supported, so
     * the iteration should be empty.
     */
    public void testIterateNamespace()
    {
        ConfigurationNodeIteratorAttribute it = new ConfigurationNodeIteratorAttribute(pointer, new QName("test", "*"));
        assertEquals("Found attributes", 0, iteratorSize(it));
    }
}
