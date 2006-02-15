/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
package org.apache.commons.configuration.beanutils;

import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;

import junit.framework.TestCase;

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

    /** Stores the object to be tested. */
    XMLBeanDeclaration decl;

    /**
     * Tests creating a declaration from a null node. This should cause an
     * exception.
     */
    public void testInitFromNullNode()
    {
        try
        {
            decl = new XMLBeanDeclaration((ConfigurationNode) null);
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
            decl = new XMLBeanDeclaration((HierarchicalConfiguration) null);
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
            decl = new XMLBeanDeclaration(null, KEY);
            fail("Could init declaration with null configuration and key!");
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
        HierarchicalConfiguration config = new HierarchicalConfiguration();
        config.addProperty(KEY + "[@config-class]", getClass().getName());
        decl = new XMLBeanDeclaration(config, KEY);
        assertEquals("Wrong class name", getClass().getName(), decl
                .getBeanClassName());
    }

    /**
     * Tests fetching the bean's class name if it is undefined.
     */
    public void testGetBeanClassNameUndefined()
    {
        decl = new XMLBeanDeclaration(new HierarchicalConfiguration());
        assertNull(decl.getBeanClassName());
    }

    /**
     * Tests fetching the name of the bean factory.
     */
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
    public void testGetBeanFactoryNameUndefined()
    {
        decl = new XMLBeanDeclaration(new HierarchicalConfiguration());
        assertNull(decl.getBeanFactoryName());
    }

    /**
     * Tests fetching the paramter for the bean factory.
     */
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
     * Tests fetching the paramter for the bean factory if it is undefined.
     */
    public void testGetBeanFactoryParameterUndefined()
    {
        decl = new XMLBeanDeclaration(new HierarchicalConfiguration());
        assertNull(decl.getBeanFactoryParameter());
    }

    /**
     * Tests if the bean's properties are correctly extracted from the
     * configuration object.
     */
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
    public void testGetBeanPropertiesEmpty()
    {
        decl = new XMLBeanDeclaration(new HierarchicalConfiguration());
        Map props = decl.getBeanProperties();
        assertTrue("Properties found", props == null || props.isEmpty());
    }

    /**
     * Tests fetching nested bean declarations.
     */
    public void testGetNestedBeanDeclarations()
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

        decl = new XMLBeanDeclaration(config, KEY);
        checkProperties(decl, TEST_PROPS, TEST_VALUES);

        Map nested = decl.getNestedBeanDeclarations();
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
     * Tests fetching nested bean declarations if none are defined.
     */
    public void testGetNestedBeanDeclarationsEmpty()
    {
        HierarchicalConfiguration config = new HierarchicalConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        decl = new XMLBeanDeclaration(config, KEY);
        Map nested = decl.getNestedBeanDeclarations();
        assertTrue("Found nested declarations", nested == null
                || nested.isEmpty());
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
        Map props = beanDecl.getBeanProperties();
        assertEquals("Wrong number of properties", names.length, props.size());
        for (int i = 0; i < names.length; i++)
        {
            assertTrue("Property " + names[i] + " not contained", props
                    .containsKey(names[i]));
            assertEquals("Wrong value for property " + names[i], values[i],
                    props.get(names[i]));
        }
    }
}
