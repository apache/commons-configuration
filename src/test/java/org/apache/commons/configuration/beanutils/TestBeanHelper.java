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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationRuntimeException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for BeanHelper.
 *
 * @since 1.3
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 */
public class TestBeanHelper
{
    /** Constant for the test value of the string property. */
    private static final String TEST_STRING = "testString";

    /** Constant for the test value of the numeric property. */
    private static final int TEST_INT = 42;

    /** Constant for the name of the test bean factory. */
    private static final String TEST_FACTORY = "testFactory";

    /**
     * Stores the default bean factory. Because this is a static field in
     * BeanHelper it is temporarily stored and reset after the tests.
     */
    private BeanFactory tempDefaultBeanFactory;

    @Before
    public void setUp() throws Exception
    {
        tempDefaultBeanFactory = BeanHelper.getDefaultBeanFactory();
        deregisterFactories();
    }

    @After
    public void tearDown() throws Exception
    {
        deregisterFactories();

        // Reset old default bean factory
        BeanHelper.setDefaultBeanFactory(tempDefaultBeanFactory);
    }

    /**
     * Removes all bean factories that might have been registered during a test.
     */
    private void deregisterFactories()
    {
        for (String name : BeanHelper.registeredFactoryNames())
        {
            BeanHelper.deregisterBeanFactory(name);
        }
        assertTrue("Remaining registered bean factories", BeanHelper
                .registeredFactoryNames().isEmpty());
    }

    /**
     * Tests registering a new bean factory.
     */
    @Test
    public void testRegisterBeanFactory()
    {
        assertTrue("List of registered factories is not empty", BeanHelper
                .registeredFactoryNames().isEmpty());
        BeanHelper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        assertEquals("Wrong number of registered factories", 1, BeanHelper
                .registeredFactoryNames().size());
        assertTrue("Test factory is not contained", BeanHelper
                .registeredFactoryNames().contains(TEST_FACTORY));
    }

    /**
     * Tries to register a null factory. This should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterBeanFactoryNull()
    {
        BeanHelper.registerBeanFactory(TEST_FACTORY, null);
    }

    /**
     * Tries to register a bean factory with a null name. This should cause an
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterBeanFactoryNullName()
    {
        BeanHelper.registerBeanFactory(null, new TestBeanFactory());
    }

    /**
     * Tests to deregister a bean factory.
     */
    @Test
    public void testDeregisterBeanFactory()
    {
        assertNull("deregistering non existing factory", BeanHelper
                .deregisterBeanFactory(TEST_FACTORY));
        assertNull("deregistering null factory", BeanHelper
                .deregisterBeanFactory(null));
        BeanFactory factory = new TestBeanFactory();
        BeanHelper.registerBeanFactory(TEST_FACTORY, factory);
        assertSame("Could not deregister factory", factory, BeanHelper
                .deregisterBeanFactory(TEST_FACTORY));
        assertTrue("List of factories is not empty", BeanHelper
                .registeredFactoryNames().isEmpty());
    }

    /**
     * Tests whether the default bean factory is correctly initialized.
     */
    @Test
    public void testGetDefaultBeanFactory()
    {
        assertSame("Incorrect default bean factory",
                DefaultBeanFactory.INSTANCE, tempDefaultBeanFactory);
    }

    /**
     * Tests setting the default bean factory to null. This should caus an
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefaultBeanFactoryNull()
    {
        BeanHelper.setDefaultBeanFactory(null);
    }

    /**
     * Tests initializing a bean.
     */
    @Test
    public void testInitBean()
    {
        BeanHelper.setDefaultBeanFactory(new TestBeanFactory());
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        BeanCreationTestBean bean = new BeanCreationTestBean();
        BeanHelper.initBean(bean, data);
        checkBean(bean);
    }

    /**
     * Tests initializing a bean when the bean declaration does not contain any
     * data.
     */
    @Test
    public void testInitBeanWithNoData()
    {
        BeanDeclarationTestImpl data = new BeanDeclarationTestImpl();
        BeanCreationTestBean bean = new BeanCreationTestBean();
        BeanHelper.initBean(bean, data);
        assertNull("Wrong string property", bean.getStringValue());
        assertEquals("Wrong int property", 0, bean.getIntValue());
        assertNull("Buddy was set", bean.getBuddy());
    }

    /**
     * Tries to initialize a bean with a bean declaration that contains an
     * invalid property value. This should cause an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testInitBeanWithInvalidProperty()
    {
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.getBeanProperties().put("nonExistingProperty", Boolean.TRUE);
        BeanHelper.initBean(new BeanCreationTestBean(), data);
    }

    /**
     * Tests creating a bean. All necessary information is stored in the bean
     * declaration.
     */
    @Test
    public void testCreateBean()
    {
        TestBeanFactory factory = new TestBeanFactory();
        BeanHelper.registerBeanFactory(TEST_FACTORY, factory);
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(BeanCreationTestBean.class.getName());
        checkBean((BeanCreationTestBean) BeanHelper.createBean(data, null));
        assertNull("A parameter was passed", factory.parameter);
    }

    /**
     * Tests whether a bean with a property of type collection can be created.
     */
    @Test
    public void testCreateBeanWithListChildBean()
    {
        TestBeanFactory factory = new TestBeanFactory();
        BeanHelper.registerBeanFactory(TEST_FACTORY, factory);
        BeanDeclarationTestImpl data = setUpBeanDeclarationWithListChild();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(BeanCreationTestBeanWithListChild.class.getName());
        checkBean((BeanCreationTestBeanWithListChild) BeanHelper.createBean(data, null));
        assertNull("A parameter was passed", factory.parameter);
    }

    /**
     * Tests creating a bean when no bean declaration is provided. This should
     * cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateBeanWithNullDeclaration()
    {
        BeanHelper.createBean(null);
    }

    /**
     * Tests creating a bean. The bean's class is specified as the default class
     * argument.
     */
    @Test
    public void testCreateBeanWithDefaultClass()
    {
        BeanHelper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        checkBean((BeanCreationTestBean) BeanHelper.createBean(data, BeanCreationTestBean.class));
    }

    /**
     * Tests creating a bean when the bean's class is specified as the default
     * class of the bean factory.
     */
    @Test
    public void testCreateBeanWithFactoryDefaultClass()
    {
        TestBeanFactory factory = new TestBeanFactory();
        factory.supportsDefaultClass = true;
        BeanHelper.registerBeanFactory(TEST_FACTORY, factory);
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        checkBean((BeanCreationTestBean) BeanHelper.createBean(data, null));
    }

    /**
     * Tries to create a bean when no class is provided. This should cause an
     * exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testCreateBeanWithNoClass()
    {
        BeanHelper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        BeanHelper.createBean(data, null);
    }

    /**
     * Tries to create a bean with a non existing class. This should cause an
     * exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testCreateBeanWithInvalidClass()
    {
        BeanHelper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName("non.existing.ClassName");
        BeanHelper.createBean(data, null);
    }

    /**
     * Tests creating a bean using the default bean factory.
     */
    @Test
    public void testCreateBeanWithDefaultFactory()
    {
        BeanHelper.setDefaultBeanFactory(new TestBeanFactory());
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanClassName(BeanCreationTestBean.class.getName());
        checkBean((BeanCreationTestBean) BeanHelper.createBean(data, null));
    }

    /**
     * Tests creating a bean using a non registered factory.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testCreateBeanWithUnknownFactory()
    {
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(BeanCreationTestBean.class.getName());
        BeanHelper.createBean(data, null);
    }

    /**
     * Tests creating a bean when the factory throws an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testCreateBeanWithException()
    {
        BeanHelper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(getClass().getName());
        BeanHelper.createBean(data, null);
    }

    /**
     * Tests if a parameter is correctly passed to the bean factory.
     */
    @Test
    public void testCreateBeanWithParameter()
    {
        Object param = new Integer(42);
        TestBeanFactory factory = new TestBeanFactory();
        BeanHelper.registerBeanFactory(TEST_FACTORY, factory);
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(BeanCreationTestBean.class.getName());
        checkBean((BeanCreationTestBean) BeanHelper.createBean(data, null, param));
        assertSame("Wrong parameter", param, factory.parameter);
    }

    /**
     * Tests whether the standard constructor can be found.
     */
    @Test
    public void testFindMatchingConstructorNoArgs()
    {
        BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        Constructor<BeanCreationTestBean> ctor =
                BeanHelper.findMatchingConstructor(BeanCreationTestBean.class, decl);
        assertEquals("Not the standard constructor", 0,
                ctor.getParameterTypes().length);
    }

    /**
     * Tests whether a matching constructor is found if the number of arguments
     * is unique.
     */
    @Test
    public void testFindMatchingConstructorArgCount()
    {
        BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        Collection<ConstructorArg> args = new ArrayList<ConstructorArg>();
        args.add(ConstructorArg.forValue(TEST_STRING));
        args.add(ConstructorArg.forValue(String.valueOf(TEST_INT)));
        decl.setConstructorArgs(args);
        Constructor<BeanCreationTestCtorBean> ctor =
                BeanHelper.findMatchingConstructor(BeanCreationTestCtorBean.class, decl);
        Class<?>[] paramTypes = ctor.getParameterTypes();
        assertEquals("Wrong number of parameters", 2, paramTypes.length);
        assertEquals("Wrong parameter type 1", String.class, paramTypes[0]);
        assertEquals("Wrong parameter type 2", Integer.TYPE, paramTypes[1]);
    }

    /**
     * Tests whether ambiguous constructor arguments are detected.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testFindMatchingConstructorAmbiguous()
    {
        BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        Collection<ConstructorArg> args = new ArrayList<ConstructorArg>();
        args.add(ConstructorArg.forValue(TEST_STRING));
        decl.setConstructorArgs(args);
        BeanHelper.findMatchingConstructor(BeanCreationTestCtorBean.class, decl);
    }

    /**
     * Tests whether explicit type declarations are used to resolve ambiguous
     * parameter types.
     */
    @Test
    public void testFindMatchingConstructorExplicitType()
    {
        BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        Collection<ConstructorArg> args = new ArrayList<ConstructorArg>();
        args.add(ConstructorArg.forBeanDeclaration(setUpBeanDeclaration(),
                BeanCreationTestBean.class.getName()));
        decl.setConstructorArgs(args);
        Constructor<BeanCreationTestCtorBean> ctor =
                BeanHelper.findMatchingConstructor(BeanCreationTestCtorBean.class, decl);
        Class<?>[] paramTypes = ctor.getParameterTypes();
        assertEquals("Wrong number of parameters", 1, paramTypes.length);
        assertEquals("Wrong parameter type", BeanCreationTestBean.class, paramTypes[0]);
    }

    /**
     * Tests the case that no matching constructor is found.
     */
    @Test
    public void testFindMatchingConstructorNoMatch()
    {
        BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        Collection<ConstructorArg> args = new ArrayList<ConstructorArg>();
        args.add(ConstructorArg.forValue(TEST_STRING, getClass().getName()));
        decl.setConstructorArgs(args);
        try
        {
            BeanHelper.findMatchingConstructor(BeanCreationTestCtorBean.class, decl);
            fail("No exception thrown!");
        }
        catch (ConfigurationRuntimeException crex)
        {
            String msg = crex.getMessage();
            assertTrue("Bean class not found:" + msg,
                    msg.indexOf(BeanCreationTestCtorBean.class.getName()) > 0);
            assertTrue("Parameter value not found: " + msg,
                    msg.indexOf(TEST_STRING) > 0);
            assertTrue("Parameter type not found: " + msg,
                    msg.indexOf("(" + getClass().getName() + ')') > 0);
        }
    }

    /**
     * Returns an initialized bean declaration.
     *
     * @return the bean declaration
     */
    private BeanDeclarationTestImpl setUpBeanDeclaration()
    {
        BeanDeclarationTestImpl data = new BeanDeclarationTestImpl();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("stringValue", TEST_STRING);
        properties.put("intValue", String.valueOf(TEST_INT));
        data.setBeanProperties(properties);
        BeanDeclarationTestImpl buddyData = new BeanDeclarationTestImpl();
        Map<String, Object> properties2 = new HashMap<String, Object>();
        properties2.put("stringValue", "Another test string");
        properties2.put("intValue", new Integer(100));
        buddyData.setBeanProperties(properties2);
        buddyData.setBeanClassName(BeanCreationTestBean.class.getName());
        if (BeanHelper.getDefaultBeanFactory() == null)
        {
            buddyData.setBeanFactoryName(TEST_FACTORY);
        }

        Map<String, Object> nested = new HashMap<String, Object>();
        nested.put("buddy", buddyData);
        data.setNestedBeanDeclarations(nested);
        return data;
    }

    /**
     * Same as setUpBeanDeclaration, but returns a nested array of beans
     * as a single property. Tests multi-value (Collection<BeanDeclaration>)
     * children construction.
     *
     * @return The bean declaration with a list child bean proerty
     */
    private BeanDeclarationTestImpl setUpBeanDeclarationWithListChild()
    {
        BeanDeclarationTestImpl data = new BeanDeclarationTestImpl();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("stringValue", TEST_STRING);
        properties.put("intValue", String.valueOf(TEST_INT));
        data.setBeanProperties(properties);

        List<BeanDeclaration> childData = new ArrayList<BeanDeclaration>();
        childData.add(createChildBean("child1"));
        childData.add(createChildBean("child2"));
        Map<String, Object> nested = new HashMap<String, Object>();
        nested.put("children", childData);
        data.setNestedBeanDeclarations(nested);
        return data;
    }

    /**
     * Create a simple bean declaration that has no children for testing
     * of nested children bean declarations.
     *
     * @param name A name prefix that can be used to disambiguate the children
     * @return A simple declaration
     */
    private BeanDeclarationTestImpl createChildBean(String name)
    {
        BeanDeclarationTestImpl childBean = new BeanDeclarationTestImpl();
        Map<String, Object> properties2 = new HashMap<String, Object>();
        properties2.put("stringValue", name + " Another test string");
        properties2.put("intValue", new Integer(100));
        childBean.setBeanProperties(properties2);
        childBean.setBeanClassName(BeanCreationTestBean.class.getName());
        if (BeanHelper.getDefaultBeanFactory() == null)
        {
            childBean.setBeanFactoryName(TEST_FACTORY);
        }

        return childBean;
    }

    /**
     * Tests if the bean was correctly initialized from the data of the test
     * bean declaration.
     *
     * @param bean the bean to be checked
     */
    private void checkBean(BeanCreationTestBean bean)
    {
        assertEquals("Wrong string property", TEST_STRING, bean
                .getStringValue());
        assertEquals("Wrong int property", TEST_INT, bean.getIntValue());
        BeanCreationTestBean buddy = bean.getBuddy();
        assertNotNull("Buddy was not set", buddy);
        assertEquals("Wrong string property in buddy", "Another test string",
                buddy.getStringValue());
        assertEquals("Wrong int property in buddy", 100, buddy.getIntValue());
    }

    /**
     * Tests if the bean was correctly initialized from the data of the test
     * bean declaration.
     *
     * @param bean the bean to be checked
     */
    private void checkBean(BeanCreationTestBeanWithListChild bean)
    {
        assertEquals("Wrong string property", TEST_STRING, bean
                .getStringValue());
        assertEquals("Wrong int property", TEST_INT, bean.getIntValue());
        List<BeanCreationTestBean> children = bean.getChildren();
        assertNotNull("Children were not set", children);
        assertEquals("Wrong number of children created", children.size(), 2);
        assertNotNull("First child was set as null", children.get(0));
        assertNotNull("Second child was set as null", children.get(1));
    }

    /**
     * An implementation of the BeanFactory interface used for testing. This
     * implementation is really simple: If the BeanCreationTestBean class is provided, a new
     * instance will be created. Otherwise an exception is thrown.
     */
    static class TestBeanFactory implements BeanFactory
    {
        Object parameter;

        boolean supportsDefaultClass;

        public Object createBean(BeanCreationContext bcc) throws Exception
        {
            parameter = bcc.getParameter();
            if (BeanCreationTestBean.class.equals(bcc.getBeanClass()))
            {
                BeanCreationTestBean bean = new BeanCreationTestBean();
                BeanHelper.initBean(bean, bcc.getBeanDeclaration());
                return bean;
            }
            else if (BeanCreationTestBeanWithListChild.class.equals(bcc.getBeanClass()))
            {
                BeanCreationTestBeanWithListChild bean = new BeanCreationTestBeanWithListChild();
                BeanHelper.initBean(bean, bcc.getBeanDeclaration());
                return bean;
            }
            else
            {
                throw new IllegalArgumentException("Unsupported class: "
                        + bcc.getBeanClass());
            }
        }

        /**
         * Returns the default class, but only if the supportsDefaultClass flag
         * is set.
         */
        public Class<?> getDefaultBeanClass()
        {
            return supportsDefaultClass ? BeanCreationTestBean.class : null;
        }
    }
}
