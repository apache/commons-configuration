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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.junit.Test;

/**
 * Test class for XMLBeanDeclaration.
 *
 * @since 1.3
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 */
public class TestXMLBeanDeclaration
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
    { "org.apache.commons.configuration.test.AddressTest",
            "org.apache.commons.configuration.test.CarTest"};

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

    /** An array with property names for a complex constructor argument. */
    private static final String[] CTOR_COMPLEX_ATTRIBUTES = {
            "secCode", "validTo"
    };

    /** An array with values of a complex constructor argument. */
    private static final String[] CTOR_COMPLEX_VALUES = {
            "20121110181559", "2015-01-31"
    };

    /** Constant for an ID value passed as constructor argument. */
    private static final String CTOR_ID = "20121110182006";

    /** Constant for the key with the bean declaration. */
    private static final String KEY = "myBean";

    /** Constant for the section with the variables.*/
    private static final String VARS = "variables.";

    /**
     * Tests creating a declaration from a null configuration. This should cause
     * an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitFromNullConfiguration()
    {
        new XMLBeanDeclaration(null);
    }

    /**
     * Tests creating a declaration from a null configuration with a key. This
     * should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitFromNullConfigurationAndKey()
    {
        new XMLBeanDeclaration(null, KEY);
    }

    /**
     * Tests fetching the bean's class name.
     */
    @Test
    public void testGetBeanClassName()
    {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        config.addProperty(KEY + "[@config-class]", getClass().getName());
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config, KEY);
        assertEquals("Wrong class name", getClass().getName(), decl
                .getBeanClassName());
    }

    /**
     * Tests fetching the bean's class name if it is undefined.
     */
    @Test
    public void testGetBeanClassNameUndefined()
    {
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(new BaseHierarchicalConfiguration());
        assertNull(decl.getBeanClassName());
    }

    /**
     * Tests that a missing bean class name does not cause an exception.
     */
    @Test
    public void testGetBeanClassNameUndefinedWithEx()
    {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        config.setThrowExceptionOnMissing(true);
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config);
        assertNull("Got a bean class name", decl.getBeanClassName());
    }

    /**
     * Tests whether a default bean class name is taken into account.
     */
    @Test
    public void testGetBeanClassNameFromDefault()
    {
        final BaseHierarchicalConfiguration config =
                new BaseHierarchicalConfiguration();
        config.addProperty(KEY + "[@someProperty]", Boolean.TRUE);
        final XMLBeanDeclaration decl =
                new XMLBeanDeclaration(config, KEY, false, getClass().getName());
        assertEquals("Wrong class name", getClass().getName(),
                decl.getBeanClassName());
    }

    /**
     * Tests whether a default bean class name is overridden by a value in the
     * configuration.
     */
    @Test
    public void tetGetBeanClassNameDefaultOverride()
    {
        final BaseHierarchicalConfiguration config =
                new BaseHierarchicalConfiguration();
        config.addProperty(KEY + "[@config-class]", getClass().getName());
        final XMLBeanDeclaration decl =
                new XMLBeanDeclaration(config, KEY, false,
                        "someDefaultClassName");
        assertEquals("Wrong class name", getClass().getName(),
                decl.getBeanClassName());
    }

    /**
     * Tests fetching the name of the bean factory.
     */
    @Test
    public void testGetBeanFactoryName()
    {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        config.addProperty(KEY + "[@config-factory]", "myFactory");
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config, KEY);
        assertEquals("Wrong factory name", "myFactory", decl
                .getBeanFactoryName());
    }

    /**
     * Tests fetching the name of the bean factory if it is undefined.
     */
    @Test
    public void testGetBeanFactoryNameUndefined()
    {
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(new BaseHierarchicalConfiguration());
        assertNull(decl.getBeanFactoryName());
    }

    /**
     * Tests that a missing bean factory name does not throw an exception.
     */
    @Test
    public void testGetBeanFactoryNameUndefinedWithEx()
    {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        config.setThrowExceptionOnMissing(true);
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config);
        assertNull("Got a factory name", decl.getBeanFactoryName());
    }

    /**
     * Tests fetching the parameter for the bean factory.
     */
    @Test
    public void testGetBeanFactoryParameter()
    {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        config
                .addProperty(KEY + "[@config-factoryParam]",
                        "myFactoryParameter");
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config, KEY);
        assertEquals("Wrong factory parameter", "myFactoryParameter", decl
                .getBeanFactoryParameter());
    }

    /**
     * Tests fetching the parameter for the bean factory if it is undefined.
     */
    @Test
    public void testGetBeanFactoryParameterUndefined()
    {
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(new BaseHierarchicalConfiguration());
        assertNull(decl.getBeanFactoryParameter());
    }

    /**
     * Tests that an undefined bean factory parameter does not cause an exception.
     */
    @Test
    public void testGetBeanFactoryParameterUndefinedWithEx()
    {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        config.setThrowExceptionOnMissing(true);
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config);
        assertNull("Got a factory parameter", decl.getBeanFactoryParameter());
    }

    /**
     * Tests if the bean's properties are correctly extracted from the
     * configuration object.
     */
    @Test
    public void testGetBeanProperties()
    {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config, KEY);
        checkProperties(decl, TEST_PROPS, TEST_VALUES);
    }

    /**
     * Tests obtaining the bean's properties when reserved attributes are
     * involved. These should be ignored.
     */
    @Test
    public void testGetBeanPropertiesWithReservedAttributes()
    {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        config.addProperty(KEY + "[@config-testattr]", "yes");
        config.addProperty(KEY + "[@config-anothertest]", "this, too");
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config, KEY);
        checkProperties(decl, TEST_PROPS, TEST_VALUES);
    }

    /**
     * Tests fetching properties if none are defined.
     */
    @Test
    public void testGetBeanPropertiesEmpty()
    {
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(new BaseHierarchicalConfiguration());
        final Map<String, Object> props = decl.getBeanProperties();
        assertTrue("Properties found", props == null || props.isEmpty());
    }

    /**
     * Creates a configuration with data for testing nested bean declarations
     * including constructor arguments.
     *
     * @return the initialized test configuration
     */
    private static BaseHierarchicalConfiguration prepareNestedBeanDeclarations()
    {
        final BaseHierarchicalConfiguration config =
                new BaseHierarchicalConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        final String keyCtorArg = KEY + ".config-constrarg";
        setupBeanDeclaration(config, keyCtorArg, CTOR_COMPLEX_ATTRIBUTES,
                CTOR_COMPLEX_VALUES);
        config.addProperty(keyCtorArg + "[@config-class]", "TestClass");
        config.addProperty(keyCtorArg + "(-1)[@config-value]", CTOR_ID);
        config.addProperty(keyCtorArg + "[@config-type]", "long");
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
        final BaseHierarchicalConfiguration config = prepareNestedBeanDeclarations();
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config, KEY);
        checkProperties(decl, TEST_PROPS, TEST_VALUES);

        final Map<String, Object> nested = decl.getNestedBeanDeclarations();
        assertEquals("Wrong number of nested declarations",
                COMPLEX_PROPS.length, nested.size());
        for (int i = 0; i < COMPLEX_PROPS.length; i++)
        {
            final XMLBeanDeclaration d = (XMLBeanDeclaration) nested
                    .get(COMPLEX_PROPS[i]);
            assertNotNull("No declaration found for " + COMPLEX_PROPS[i], d);
            checkProperties(d, COMPLEX_ATTRIBUTES[i], COMPLEX_VALUES[i]);
            assertEquals("Wrong bean class", COMPLEX_CLASSES[i], d
                    .getBeanClassName());
        }
    }

    /**
     * Tests whether reserved characters in the node names of nested bean declarations
     * are handled correctly. This is related to CONFIGURATION-567.
     */
    @Test
    public void testGetNestedBeanDeclarationsReservedCharacter()
    {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        final String key = KEY + ".address..private";
        setupBeanDeclaration(config, key, COMPLEX_ATTRIBUTES[0], COMPLEX_VALUES[0]);
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config, KEY);

        final Map<String, Object> nested = decl.getNestedBeanDeclarations();
        assertTrue("Key not found", nested.containsKey("address.private"));
    }

    /**
     * Tests whether the factory method for creating nested bean declarations
     * gets called.
     */
    @Test
    public void testGetNestedBeanDeclarationsFactoryMethod()
    {
        final BaseHierarchicalConfiguration config = prepareNestedBeanDeclarations();
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config, KEY)
        {
            @Override
            BeanDeclaration createBeanDeclaration(final NodeData<?> node)
            {
                return new XMLBeanDeclarationTestImpl(getConfiguration()
                        .configurationAt(node.nodeName()), node);
            }
        };
        final Map<String, Object> nested = decl.getNestedBeanDeclarations();
        for (final String element : COMPLEX_PROPS) {
            final Object d = nested.get(element);
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
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config, KEY);
        final Map<String, Object> nested = decl.getNestedBeanDeclarations();
        assertTrue("Found nested declarations", nested == null
                || nested.isEmpty());
    }

    /**
     * Tests whether interpolation of bean properties works.
     */
    @Test
    public void testGetInterpolatedBeanProperties()
    {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        final String[] varValues = new String[TEST_PROPS.length];
        for(int i = 0; i < TEST_PROPS.length; i++)
        {
            varValues[i] = "${" + VARS + TEST_PROPS[i] + "}";
            config.addProperty(VARS + TEST_PROPS[i], TEST_VALUES[i]);
        }
        setupBeanDeclaration(config, KEY, TEST_PROPS, varValues);
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config, KEY);
        checkProperties(decl, TEST_PROPS, TEST_VALUES);
    }

    /**
     * Tests constructing a bean declaration from an undefined key. This should
     * cause an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testInitFromUndefinedKey()
    {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        new XMLBeanDeclaration(config, "undefined_key");
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
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config, "undefined_key", true);
        assertNull("Found a bean class", decl.getBeanClassName());
    }

    /**
     * Tests constructing a bean declaration from a key with multiple values.
     * This should cause an exception because keys must be unique.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testInitFromMultiValueKey()
    {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        config.addProperty(KEY, "myFirstKey");
        config.addProperty(KEY, "mySecondKey");
        new XMLBeanDeclaration(config, KEY);
    }

    /**
     * Tests whether constructor arguments can be queried.
     */
    @Test
    public void testGetConstructorArgs()
    {
        final BaseHierarchicalConfiguration config = prepareNestedBeanDeclarations();
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config, KEY);
        final Collection<ConstructorArg> args = decl.getConstructorArgs();
        assertEquals("Wrong number of constructor arguments", 2, args.size());
        final Iterator<ConstructorArg> it = args.iterator();
        final ConstructorArg arg1 = it.next();
        assertTrue("No bean declaration", arg1.isNestedBeanDeclaration());
        checkProperties(arg1.getBeanDeclaration(), CTOR_COMPLEX_ATTRIBUTES,
                CTOR_COMPLEX_VALUES);
        assertNull("Got a type", arg1.getTypeName());
        assertEquals("Wrong class name", "TestClass", arg1.getBeanDeclaration()
                .getBeanClassName());
        final ConstructorArg arg2 = it.next();
        assertFalse("A bean declaration", arg2.isNestedBeanDeclaration());
        assertEquals("Wrong value", CTOR_ID, arg2.getValue());
        assertEquals("Wrong type", "long", arg2.getTypeName());
    }

    /**
     * Tests whether a constructor argument with a null value can be defined.
     */
    @Test
    public void testGetConstructorArgsNullArg()
    {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        config.addProperty(KEY + ".config-constrarg", "");
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config, KEY);
        final Collection<ConstructorArg> args = decl.getConstructorArgs();
        assertEquals("Wrong number of constructor arguments", 1, args.size());
        final ConstructorArg arg = args.iterator().next();
        assertFalse("A bean declaration", arg.isNestedBeanDeclaration());
        assertNull("Got a value", arg.getValue());
    }

    /**
     * Tests whether interpolation is done on constructor arguments.
     */
    @Test
    public void testGetInterpolatedConstructorArgs()
    {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        final String expectedValue = "ctorArg";
        config.addProperty("value", expectedValue);
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        config.addProperty(KEY + ".config-constrarg[@config-value]", "${value}");
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config, KEY);
        final Collection<ConstructorArg> args = decl.getConstructorArgs();
        final ConstructorArg arg = args.iterator().next();
        assertEquals("Wrong interpolated value", expectedValue, arg.getValue());
    }

    /**
     * Tests interpolate() if no ConfigurationInterpolator is available.
     */
    @Test
    public void testInterpolateNoInterpolator()
    {
        final BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
        config.addProperty("value", "expectedValue");
        setupBeanDeclaration(config, KEY, TEST_PROPS, TEST_VALUES);
        final String value = "${value}";
        config.addProperty(KEY + ".config-constrarg[@config-value]", value);
        config.setInterpolator(null);
        final XMLBeanDeclaration decl = new XMLBeanDeclaration(config, KEY);
        final Collection<ConstructorArg> args = decl.getConstructorArgs();
        final ConstructorArg arg = args.iterator().next();
        assertEquals("Value was changed", value, arg.getValue());
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
    private static void setupBeanDeclaration(final HierarchicalConfiguration<?> config,
            final String key, final String[] names, final String[] values)
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
    private static void checkProperties(final BeanDeclaration beanDecl, final String[] names,
            final String[] values)
    {
        final Map<String, Object> props = beanDecl.getBeanProperties();
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
        public XMLBeanDeclarationTestImpl(final HierarchicalConfiguration<?> config,
                final NodeData<?> node)
        {
            super(config, node);
        }
    }
}
