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
package org.apache.commons.configuration2.tree.xpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code ConfigurationNodeIteratorAttributes}.
 *
 */
public class TestConfigurationIteratorAttributes extends AbstractXPathTest
{
    /** Constant for the name of another test attribute.*/
    private static final String TEST_ATTR = "test";

    /** Constant for a namespace prefix. */
    private static final String NAMESPACE = "commons";

    /** Constant for an attribute with a namespace prefix. */
    private static final String NS_ATTR = NAMESPACE + ":attr";

    /** Stores the node pointer of the test node.*/
    private ConfigurationNodePointer<ImmutableNode> pointer;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        // Adds further attributes to the test node
        final ImmutableNode orgNode = root.getChildren().get(1);
        final ImmutableNode testNode =
                orgNode.setAttribute(TEST_ATTR, "yes").setAttribute(NS_ATTR,
                        "configuration");
        pointer =
                new ConfigurationNodePointer<>(testNode,
                        Locale.getDefault(), handler);
    }

    /**
     * Tests to iterate over all attributes.
     */
    @Test
    public void testIterateAllAttributes()
    {
        final ConfigurationNodeIteratorAttribute<ImmutableNode> it =
                new ConfigurationNodeIteratorAttribute<>(pointer,
                        new QName(null, "*"));
        assertEquals("Wrong number of attributes", 3, iteratorSize(it));
        final List<NodePointer> attrs = iterationElements(it);
        final Set<String> attrNames = new HashSet<>();
        for (final NodePointer np : attrs)
        {
            attrNames.add(np.getName().getName());
        }
        assertTrue("First attribute not found", attrNames.contains(ATTR_NAME));
        assertTrue("Second attribute not found", attrNames.contains(TEST_ATTR));
        assertTrue("Namespace attribute not found", attrNames.contains(NS_ATTR));
    }

    /**
     * Tests to iterate over attributes with a specific name.
     */
    @Test
    public void testIterateSpecificAttribute()
    {
        final ConfigurationNodeIteratorAttribute<ImmutableNode> it =
                new ConfigurationNodeIteratorAttribute<>(pointer,
                        new QName(null, TEST_ATTR));
        assertEquals("Wrong number of attributes", 1, iteratorSize(it));
        assertEquals("Wrong attribute", TEST_ATTR, iterationElements(it).get(0)
                .getName().getName());
    }

    /**
     * Tests to iterate over non existing attributes.
     */
    @Test
    public void testIterateUnknownAttribute()
    {
        final ConfigurationNodeIteratorAttribute<ImmutableNode> it =
                new ConfigurationNodeIteratorAttribute<>(pointer,
                        new QName(null, "unknown"));
        assertEquals("Found attributes", 0, iteratorSize(it));
    }

    /**
     * Tests iteration if an unknown namespace is specified.
     */
    @Test
    public void testIterateNamespaceUnknown()
    {
        final ConfigurationNodeIteratorAttribute<ImmutableNode> it =
                new ConfigurationNodeIteratorAttribute<>(pointer,
                        new QName("test", "*"));
        assertEquals("Found attributes", 0, iteratorSize(it));
    }

    /**
     * Tests whether a specific attribute with a namespace can be selected.
     */
    @Test
    public void testIterateNamespaceAttribute()
    {
        final ConfigurationNodeIteratorAttribute<ImmutableNode> it =
                new ConfigurationNodeIteratorAttribute<>(pointer,
                        new QName(NAMESPACE, "attr"));
        assertEquals("Wrong number of attributes", 1, iteratorSize(it));
        assertEquals("Wrong attribute", NS_ATTR, iterationElements(it).get(0)
                .getName().getName());
    }

    /**
     * Tests whether a wildcard can be used together with a namespace.
     */
    @Test
    public void testIterateNamespaceWildcard()
    {
        final ConfigurationNodeIteratorAttribute<ImmutableNode> it =
                new ConfigurationNodeIteratorAttribute<>(pointer,
                        new QName(NAMESPACE, "*"));
        assertEquals("Wrong number of attributes", 1, iteratorSize(it));
        assertEquals("Wrong attribute", NS_ATTR, iterationElements(it).get(0)
                .getName().getName());
    }
}
