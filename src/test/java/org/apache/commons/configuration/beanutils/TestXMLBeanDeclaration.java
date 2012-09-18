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
package org.apache.commons.configuration.beanutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.junit.Test;

/**
 * Test class for XMLBeanDeclaration.
 *
 * @since 1.3
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 */
public class TestXMLBeanDeclaration
{
    /** An array with some test properties. */
    static final String[] TEST_PROPS =
    { "firstName", "lastName", "department", "age", "hobby"};

    /** An array with the values for the test properties. */
    static final String[] TEST_VALUES =
    { "John", "Smith", "Engineering", "42", "TV"};

    /** An array with the names of nested (complex) properties. */
    static final String[] COMPLEX_PROPS =
    { "address", "car"};

    /** An array with the names of the classes of the complex properties. */
    static final String[] COMPLEX_CLASSES =
    { "org.apache.commons.configuration.test.AddressTest",
            "org.apache.commons.configuration.test.CarTest"};

    /** An array with the property names of the complex properties. */
    static final String[][] COMPLEX_ATTRIBUTES =
    {
    { "street", "zip", "city", "country"},
    { "brand", "color"}};

    /** An array with the values of the complex properties. */
    static final String[][] COMPLEX_VALUES =
    {
    { "Baker Street", "12354", "London", "UK"},
    { "Bentley", "silver"}};

    /** Constant for the key with the bean declaration. */
    static final String KEY = "myBean";

    /** Constant for the section with the variables.*/
    static final String VARS = "variables.";

    /** Stores the object to be tested. */
    XMLBeanDeclaration decl;

    /**
     * Tests creating a declaration from a null node. This should cause an
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitFromNullNode()
    {
        decl = new XMLBeanDeclaration(new HierarchicalConfiguration().configurationAt(null),
                (ConfigurationNode) null);
    }

    /**
     * Tests creating a declaration from a null configuration. This should cause
     * an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitFromNullConfiguration()
    {
        decl = new XMLBeanDeclaration((HierarchicalConfiguration) null);
    }

    /**
     * Tests creating a declaration from a null configuration with a key. This
     * should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitFromNullConfigurationAndKey()
    {
        decl = new XMLBeanDeclaration(null, KEY);
    }

    /**
     * Tests creating a declaration from a null configuration with a node. This
     * should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitFromNullConfigurationAndNode()
    {
        decl = new XMLBeanDeclaration(null, new HierarchicalConfiguration()
                .getRootNode());
    }

    /**
     * Tests fetching the bean's class name.
     */
    @Test
    public void testGetBeanClassName()
    {
        HierarchicalConfiguration config = new HierarchicalConfiguration();
        config.addProperty(KEY + "[@config-class]", getClass().getName());
        decl = new XMLBeanDeclaration(config, KEY);
        assertEquals("Wrong class name", getClass().getName(), decl
                .getBeanClassName());
    }

    /**
     * Tests fetching the bean's class name if it is undefined.
     */
    @Test
    public void testGetBeanClassNameUndefined()
    {
        decl = new XMLBeanDeclaration(new HierarchicalConfiguration());
        assertNull(decl.getBeanClassName());
    }

    /**
     * Tests fetching the name of the bean factory.
     */
    @Test
    public void testGetBeanFactoryName()
    {
        HierarchicalConfiguration config = new HierarchicalConfiguration();
        config.addProperty(KEY + "[@config-factory]", "myFactory");
        decl = new XMLBeanDeclaration(config, KEY);
        assertEquals("Wrong factory name", "myFactory", decl
                .getBeanFactoryName());
    }

    /**
     * Tests fetching the name of the bean factory if it is undefined.
     */
    @Test
    public void testGetBeanFactoryNameUndefined()
    {
        decl = new XMLBeanDeclaration(new HierarchicalConfiguration());
        assertNull(decl.getBeanFactoryName());
    }

    /**
     * Tests fetching the parameter for the bean factory.
     */
    @Test
    public void testGetBeanFactoryParameter()
    {
        HierarchicalConfiguration config = new HierarchicalConfiguration();
        config
                .addProperty(KEY + "[@config-factoryParam]",
                        "myFactoryParameter");
        decl = new XMLBeanDeclaration(config, KEY);
        assertEquals("Wrong factory parameter", "myFactoryParameter", decl
                .getBeanFactoryParameter());
    }

    /**
     * Tests fetching the parameter for the bean factory if it is undefined.
     */
    @Test
    public void testGetBeanFactoryParameterUndefined()
    {
        decl = new XMLBeanDeclaration(new HierarchicalConfiguration());
        assertNull(decl.getBeanFactoryParameter());
    }

    /**
     * Tests if the bean's properties are correctly extracted from the
     * configuration object.
     */
    @Test
    public void testGetBeanProperties()
    {
        HierarchicalConfiguration config = new HierarchicalConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        decl = new XMLBeanDeclaration(config, KEY);
        checkProperties(decl, TEST_PROPS, TEST_VALUES);
    }

    /**
     * Tests obtaining the bean's properties when reserved attributes are
     * involved. These should be ignored.
     */
    @Test
    public void testGetBeanPropertiesWithReservedAttributes()
    {
        HierarchicalConfiguration config = new HierarchicalConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        config.addProperty(KEY + "[@config-testattr]", "yes");
        config.addProperty(KEY + "[@config-anothertest]", "this, too");
        decl = new XMLBeanDeclaration(config, KEY);
        checkProperties(decl, TEST_PROPS, TEST_VALUES);
    }

    /**
     * Tests fetching properties if none are defined.
     */
    @Test
    public void testGetBeanPropertiesEmpty()
    {
        decl = new XMLBeanDeclaration(new HierarchicalConfiguration());
        Map<String, Object> props = decl.getBeanProperties();
        assertTrue("Properties found", props == null || props.isEmpty());
    }

    /**
     * Creates a configuration with data for testing nested bean declarations.
     * @return the initialized test configuration
     */
    private HierarchicalConfiguration prepareNestedBeanDeclarations()
    {
        HierarchicalConfiguration config = new HierarchicalConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        for (int i = 0; i < COMPLEX_PROPS.length; i++)
        {
            setupBeanDeclaration(config, KEY + '.' + COMPLEX_PROPS[i],
                    COMPLEX_ATTRIBUTES[i], COMPLEX_VALUES[i]);
            config.addProperty(
                    KEY + '.' + COMPLEX_PROPS[i] + "[@config-class]",
                    COMPLEX_CLASSES[i]);
        }
        return config;
    }

    /**
     * Tests fetching nested bean declarations.
     */
    @Test
    public void testGetNestedBeanDeclarations()
    {
        HierarchicalConfiguration config = prepareNestedBeanDeclarations();
        decl = new XMLBeanDeclaration(config, KEY);
        checkProperties(decl, TEST_PROPS, TEST_VALUES);

        Map<String, Object> nested = decl.getNestedBeanDeclarations();
        assertEquals("Wrong number of nested declarations",
                COMPLEX_PROPS.length, nested.size());
        for (int i = 0; i < COMPLEX_PROPS.length; i++)
        {
            XMLBeanDeclaration d = (XMLBeanDeclaration) nested
                    .get(COMPLEX_PROPS[i]);
            assertNotNull("No declaration found for " + COMPLEX_PROPS[i], d);
            checkProperties(d, COMPLEX_ATTRIBUTES[i], COMPLEX_VALUES[i]);
            assertEquals("Wrong bean class", COMPLEX_CLASSES[i], d
                    .getBeanClassName());
        }
    }

    /**
     * Tests whether the factory method for creating nested bean declarations
     * gets called.
     */
    @Test
    public void testGetNestedBeanDeclarationsFactoryMethod()
    {
        HierarchicalConfiguration config = prepareNestedBeanDeclarations();
        decl = new XMLBeanDeclaration(config, KEY)
        {
            @Override
            protected BeanDeclaration createBeanDeclaration(
                    ConfigurationNode node)
            {
                return new XMLBeanDeclarationTestImpl(getConfiguration()
                        .configurationAt(node.getName()), node);
            }
        };
        Map<String, Object> nested = decl.getNestedBeanDeclarations();
        for (int i = 0; i < COMPLEX_PROPS.length; i++)
        {
            Object d = nested.get(COMPLEX_PROPS[i]);
            assertTrue("Wrong class for bean declaration: " + d,
                    d instanceof XMLBeanDeclarationTestImpl);
        }
    }

    /**
     * Tests fetching nested bean declarations if none are defined.
     */
    @Test
    public void testGetNestedBeanDeclarationsEmpty()
    {
        HierarchicalConfiguration config = new HierarchicalConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        decl = new XMLBeanDeclaration(config, KEY);
        Map<String, Object> nested = decl.getNestedBeanDeclarations();
        assertTrue("Found nested declarations", nested == null
                || nested.isEmpty());
    }

    /**
     * Tests whether interpolation of bean properties works.
     */
    @Test
    public void testGetInterpolatedBeanProperties()
    {
        HierarchicalConfiguration config = new HierarchicalConfiguration();
        String[] varValues = new String[TEST_PROPS.length];
        for(int i = 0; i < TEST_PROPS.length; i++)
        {
            varValues[i] = "${" + VARS + TEST_PROPS[i] + "}";
            config.addProperty(VARS + TEST_PROPS[i], TEST_VALUES[i]);
        }
        setupBeanDeclaration(config, KEY, TEST_PROPS, varValues);
        decl = new XMLBeanDeclaration(config, KEY);
        checkProperties(decl, TEST_PROPS, TEST_VALUES);
    }

    /**
     * Tests constructing a bean declaration from an undefined key. This should
     * cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitFromUndefinedKey()
    {
        HierarchicalConfiguration config = new HierarchicalConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        decl = new XMLBeanDeclaration(config, "undefined_key");
    }

    /**
     * Tests constructing a bean declaration from a key, which is undefined when
     * the optional flag is set. In this case an empty declaration should be
     * created, which can be used for creating beans as long as a default class
     * is provided.
     */
    @Test
    public void testInitFromUndefinedKeyOptional()
    {
        HierarchicalConfiguration config = new HierarchicalConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        decl = new XMLBeanDeclaration(config, "undefined_key", true);
        assertNull("Found a bean class", decl.getBeanClassName());
    }

    /**
     * Tests constructing a bean declaration from a key with multiple values.
     * This should cause an exception because keys must be unique.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitFromMultiValueKey()
    {
        HierarchicalConfiguration config = new HierarchicalConfiguration();
        config.addProperty(KEY, "myFirstKey");
        config.addProperty(KEY, "mySecondKey");
        decl = new XMLBeanDeclaration(config, KEY);
    }

    /**
     * Initializes a configuration object with a bean declaration. Under the
     * specified key the given properties will be added.
     *
     * @param config the configuration to initialize
     * @param key the key of the bean declaration
     * @param names an array with the names of the properties
     * @param values an array with the corresponding values
     */
    private void setupBeanDeclaration(HierarchicalConfiguration config,
            String key, String[] names, String[] values)
    {
        for (int i = 0; i < names.length; i++)
        {
            config.addProperty(key + "[@" + names[i] + "]", values[i]);
        }
    }

    /**
     * Checks the properties returned by a bean declaration.
     *
     * @param beanDecl the bean declaration
     * @param names an array with the expected property names
     * @param values an array with the expected property values
     */
    private void checkProperties(BeanDeclaration beanDecl, String[] names,
            String[] values)
    {
        Map<String, Object> props = beanDecl.getBeanProperties();
        assertEquals("Wrong number of properties", names.length, props.size());
        for (int i = 0; i < names.length; i++)
        {
            assertTrue("Property " + names[i] + " not contained", props
                    .containsKey(names[i]));
            assertEquals("Wrong value for property " + names[i], values[i],
                    props.get(names[i]));
        }
    }

    /**
     * A helper class used for testing the createBeanDeclaration() factory
     * method.
     */
    private static class XMLBeanDeclarationTestImpl extends XMLBeanDeclaration
    {
        public XMLBeanDeclarationTestImpl(SubnodeConfiguration config,
                ConfigurationNode node)
        {
            super(config, node);
        }
    }
}
