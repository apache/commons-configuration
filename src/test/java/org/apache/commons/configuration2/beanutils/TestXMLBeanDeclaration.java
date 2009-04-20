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
package org.apache.commons.configuration2.beanutils;

import java.util.Map;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration2.InMemoryConfiguration;
import org.apache.commons.configuration2.SubConfiguration;
import org.apache.commons.configuration2.tree.ConfigurationNode;

/**
 * Test class for XMLBeanDeclaration.
 *
 * @since 1.3
 * @author Oliver Heger
 * @version $Id$
 */
public class TestXMLBeanDeclaration extends TestCase
{
    /** An array with some test properties. */
    private static final String[] TEST_PROPS =
    { "firstName", "lastName", "department", "age", "hobby"};

    /** An array with the values for the test properties. */
    private static final String[] TEST_VALUES =
    { "John", "Smith", "Engineering", "42", "TV"};

    /** An array with the names of nested (complex) properties. */
    private static final String[] COMPLEX_PROPS =
    { "address", "car"};

    /** An array with the names of the classes of the complex properties. */
    private static final String[] COMPLEX_CLASSES =
    { "org.apache.commons.configuration2.test.AddressTest",
            "org.apache.commons.configuration2.test.CarTest"};

    /** An array with the property names of the complex properties. */
    private static final String[][] COMPLEX_ATTRIBUTES =
    {
    { "street", "zip", "city", "country"},
    { "brand", "color"}};

    /** An array with the values of the complex properties. */
    private static final String[][] COMPLEX_VALUES =
    {
    { "Baker Street", "12354", "London", "UK"},
    { "Bentley", "silver"}};

    /** Constant for the key with the bean declaration. */
    private static final String KEY = "myBean";

    /** Constant for the section with the variables.*/
    private static final String VARS = "variables.";

    /** Stores the object to be tested. */
    private XMLBeanDeclaration<ConfigurationNode> decl;

    /**
     * Tests creating a declaration from a null node. This should cause an
     * exception.
     */
    public void testInitFromNullNode()
    {
        try
        {
            decl = new XMLBeanDeclaration<ConfigurationNode>(
                    new InMemoryConfiguration().configurationAt(null),
                    (ConfigurationNode) null);
            fail("Could init declaration with null node!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests creating a declaration from a null configuration. This should cause
     * an exception.
     */
    public void testInitFromNullConfiguration()
    {
        try
        {
            decl = new XMLBeanDeclaration<ConfigurationNode>((InMemoryConfiguration) null);
            fail("Could init declaration with null configuration!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests creating a declaration from a null configuration with a key. This
     * should cause an exception.
     */
    public void testInitFromNullConfigurationAndKey()
    {
        try
        {
            decl = new XMLBeanDeclaration<ConfigurationNode>(null, KEY);
            fail("Could init declaration with null configuration and key!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests creating a declaration from a null configuration with a node. This
     * should cause an exception.
     */
    public void testInitFromNullConfigurationAndNode()
    {
        try
        {
            decl = new XMLBeanDeclaration<ConfigurationNode>(null, new InMemoryConfiguration()
                    .getRootNode());
            fail("Could init declaration with null configuration and node!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests fetching the bean's class name.
     */
    public void testGetBeanClassName()
    {
        InMemoryConfiguration config = new InMemoryConfiguration();
        config.addProperty(KEY + "[@config-class]", getClass().getName());
        decl = new XMLBeanDeclaration<ConfigurationNode>(config, KEY);
        assertEquals("Wrong class name", getClass().getName(), decl
                .getBeanClassName());
    }

    /**
     * Tests fetching the bean's class name if it is undefined.
     */
    public void testGetBeanClassNameUndefined()
    {
        decl = new XMLBeanDeclaration<ConfigurationNode>(new InMemoryConfiguration());
        assertNull(decl.getBeanClassName());
    }

    /**
     * Tests fetching the name of the bean factory.
     */
    public void testGetBeanFactoryName()
    {
        InMemoryConfiguration config = new InMemoryConfiguration();
        config.addProperty(KEY + "[@config-factory]", "myFactory");
        decl = new XMLBeanDeclaration<ConfigurationNode>(config, KEY);
        assertEquals("Wrong factory name", "myFactory", decl
                .getBeanFactoryName());
    }

    /**
     * Tests fetching the name of the bean factory if it is undefined.
     */
    public void testGetBeanFactoryNameUndefined()
    {
        decl = new XMLBeanDeclaration<ConfigurationNode>(new InMemoryConfiguration());
        assertNull(decl.getBeanFactoryName());
    }

    /**
     * Tests fetching the paramter for the bean factory.
     */
    public void testGetBeanFactoryParameter()
    {
        InMemoryConfiguration config = new InMemoryConfiguration();
        config
                .addProperty(KEY + "[@config-factoryParam]",
                        "myFactoryParameter");
        decl = new XMLBeanDeclaration<ConfigurationNode>(config, KEY);
        assertEquals("Wrong factory parameter", "myFactoryParameter", decl
                .getBeanFactoryParameter());
    }

    /**
     * Tests fetching the paramter for the bean factory if it is undefined.
     */
    public void testGetBeanFactoryParameterUndefined()
    {
        decl = new XMLBeanDeclaration<ConfigurationNode>(new InMemoryConfiguration());
        assertNull(decl.getBeanFactoryParameter());
    }

    /**
     * Tests if the bean's properties are correctly extracted from the
     * configuration object.
     */
    public void testGetBeanProperties()
    {
        InMemoryConfiguration config = new InMemoryConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        decl = new XMLBeanDeclaration<ConfigurationNode>(config, KEY);
        checkProperties(decl, TEST_PROPS, TEST_VALUES);
    }

    /**
     * Tests obtaining the bean's properties when reserved attributes are
     * involved. These should be ignored.
     */
    public void testGetBeanPropertiesWithReservedAttributes()
    {
        InMemoryConfiguration config = new InMemoryConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        config.addProperty(KEY + "[@config-testattr]", "yes");
        config.addProperty(KEY + "[@config-anothertest]", "this, too");
        decl = new XMLBeanDeclaration<ConfigurationNode>(config, KEY);
        checkProperties(decl, TEST_PROPS, TEST_VALUES);
    }

    /**
     * Tests fetching properties if none are defined.
     */
    public void testGetBeanPropertiesEmpty()
    {
        decl = new XMLBeanDeclaration<ConfigurationNode>(new InMemoryConfiguration());
        Map<String, Object> props = decl.getBeanProperties();
        assertTrue("Properties found", props == null || props.isEmpty());
    }

    /**
     * Creates a configuration with data for testing nested bean declarations.
     * @return the initialized test configuration
     */
    private InMemoryConfiguration prepareNestedBeanDeclarations()
    {
        InMemoryConfiguration config = new InMemoryConfiguration();
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
    @SuppressWarnings("unchecked")
    public void testGetNestedBeanDeclarations()
    {
        InMemoryConfiguration config = prepareNestedBeanDeclarations();
        decl = new XMLBeanDeclaration<ConfigurationNode>(config, KEY);
        checkProperties(decl, TEST_PROPS, TEST_VALUES);

        Map<String, List<BeanDeclaration>> nested = decl.getNestedBeanDeclarations();
        assertEquals("Wrong number of nested declarations",
                COMPLEX_PROPS.length, nested.size());
        for (int i = 0; i < COMPLEX_PROPS.length; i++)
        {
            XMLBeanDeclaration<ConfigurationNode> d = (XMLBeanDeclaration<ConfigurationNode>) nested
                    .get(COMPLEX_PROPS[i]).get(0);
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
    public void testGetNestedBeanDeclarationsFactoryMethod()
    {
        InMemoryConfiguration config = prepareNestedBeanDeclarations();
        decl = new XMLBeanDeclaration<ConfigurationNode>(config, KEY)
        {
            @Override
            protected BeanDeclaration createBeanDeclaration(
                    ConfigurationNode node)
            {
                return new XMLBeanDeclarationTestImpl(getConfiguration()
                        .configurationAt(node.getName()), node);
            }
        };

        Map<String, List<BeanDeclaration>> nested = decl.getNestedBeanDeclarations();
        assertEquals("Wrong number of nested declarations",
                COMPLEX_PROPS.length, nested.size());
        for (int i = 0; i < COMPLEX_PROPS.length; i++)
        {
            BeanDeclaration d = nested.get(COMPLEX_PROPS[i]).get(0);
            assertTrue("Wrong declaration class: " + d,
                    d instanceof XMLBeanDeclarationTestImpl);
        }
    }

    /**
     * Tests fetching nested bean declarations if none are defined.
     */
    public void testGetNestedBeanDeclarationsEmpty()
    {
        InMemoryConfiguration config = new InMemoryConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        decl = new XMLBeanDeclaration<ConfigurationNode>(config, KEY);
        Map<String, List<BeanDeclaration>> nested = decl.getNestedBeanDeclarations();
        assertTrue("Found nested declarations", nested == null
                || nested.isEmpty());
    }

    /**
     * Tests whether interpolation of bean properties works.
     */
    public void testGetInterpolatedBeanProperties()
    {
        InMemoryConfiguration config = new InMemoryConfiguration();
        String[] varValues = new String[TEST_PROPS.length];
        for(int i = 0; i < TEST_PROPS.length; i++)
        {
            varValues[i] = "${" + VARS + TEST_PROPS[i] + "}";
            config.addProperty(VARS + TEST_PROPS[i], TEST_VALUES[i]);
        }
        setupBeanDeclaration(config, KEY, TEST_PROPS, varValues);
        decl = new XMLBeanDeclaration<ConfigurationNode>(config, KEY);
        checkProperties(decl, TEST_PROPS, TEST_VALUES);
    }

    /**
     * Tests constructing a bean declaration from an undefined key. This should
     * cause an exception.
     */
    public void testInitFromUndefinedKey()
    {
        InMemoryConfiguration config = new InMemoryConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        try
        {
            decl = new XMLBeanDeclaration<ConfigurationNode>(config, "undefined_key");
            fail("Could create declaration from an undefined key!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests constructing a bean declaration from a key, which is undefined when
     * the optional flag is set. In this case an empty declaration should be
     * created, which can be used for creating beans as long as a default class
     * is provided.
     */
    public void testInitFromUndefinedKeyOptional()
    {
        InMemoryConfiguration config = new InMemoryConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        decl = new XMLBeanDeclaration<ConfigurationNode>(config, "undefined_key", true);
        assertNull("Found a bean class", decl.getBeanClassName());
    }

    /**
     * Tests constructing a bean declaration from a key with multiple values.
     * This should cause an exception because keys must be unique.
     */
    public void testInitFromMultiValueKey()
    {
        InMemoryConfiguration config = new InMemoryConfiguration();
        config.addProperty(KEY, "myFirstKey");
        config.addProperty(KEY, "mySecondKey");
        try
        {
            decl = new XMLBeanDeclaration<ConfigurationNode>(config, KEY);
            fail("Could create declaration from multi-valued property!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
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
    private void setupBeanDeclaration(InMemoryConfiguration config,
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
    private static class XMLBeanDeclarationTestImpl extends
            XMLBeanDeclaration<ConfigurationNode>
    {
        public XMLBeanDeclarationTestImpl(
                SubConfiguration<ConfigurationNode> config,
                ConfigurationNode node)
        {
            super(config, node);
        }
    }
}
