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

import java.util.ArrayList;
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
        TestBeanDeclaration data = setUpBeanDeclaration();
        TestBean bean = new TestBean();
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
        TestBeanDeclaration data = new TestBeanDeclaration();
        TestBean bean = new TestBean();
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
        TestBeanDeclaration data = setUpBeanDeclaration();
        data.getBeanProperties().put("nonExistingProperty", Boolean.TRUE);
        BeanHelper.initBean(new TestBean(), data);
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
        TestBeanDeclaration data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(TestBean.class.getName());
        checkBean((TestBean) BeanHelper.createBean(data, null));
        assertNull("A parameter was passed", factory.parameter);
    }

    @Test
    public void testCreateBeanWithListChildBean()
    {
        TestBeanFactory factory = new TestBeanFactory();
        BeanHelper.registerBeanFactory(TEST_FACTORY, factory);
        TestBeanDeclaration data = setUpBeanDeclarationWithListChild();
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
        TestBeanDeclaration data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        checkBean((TestBean) BeanHelper.createBean(data, TestBean.class));
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
        TestBeanDeclaration data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        checkBean((TestBean) BeanHelper.createBean(data, null));
    }

    /**
     * Tries to create a bean when no class is provided. This should cause an
     * exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testCreateBeanWithNoClass()
    {
        BeanHelper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        TestBeanDeclaration data = setUpBeanDeclaration();
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
        TestBeanDeclaration data = setUpBeanDeclaration();
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
        TestBeanDeclaration data = setUpBeanDeclaration();
        data.setBeanClassName(TestBean.class.getName());
        checkBean((TestBean) BeanHelper.createBean(data, null));
    }

    /**
     * Tests creating a bean using a non registered factory.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testCreateBeanWithUnknownFactory()
    {
        TestBeanDeclaration data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(TestBean.class.getName());
        BeanHelper.createBean(data, null);
    }

    /**
     * Tests creating a bean when the factory throws an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testCreateBeanWithException()
    {
        BeanHelper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        TestBeanDeclaration data = setUpBeanDeclaration();
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
        TestBeanDeclaration data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(TestBean.class.getName());
        checkBean((TestBean) BeanHelper.createBean(data, null, param));
        assertSame("Wrong parameter", param, factory.parameter);
    }

    /**
     * Returns an initialized bean declaration.
     *
     * @return the bean declaration
     */
    private TestBeanDeclaration setUpBeanDeclaration()
    {
        TestBeanDeclaration data = new TestBeanDeclaration();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("stringValue", "testString");
        properties.put("intValue", "42");
        data.setBeanProperties(properties);
        TestBeanDeclaration buddyData = new TestBeanDeclaration();
        Map<String, Object> properties2 = new HashMap<String, Object>();
        properties2.put("stringValue", "Another test string");
        properties2.put("intValue", new Integer(100));
        buddyData.setBeanProperties(properties2);
        buddyData.setBeanClassName(TestBean.class.getName());
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
    private TestBeanDeclaration setUpBeanDeclarationWithListChild()
    {
        TestBeanDeclaration data = new TestBeanDeclaration();
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
    private TestBeanDeclaration createChildBean(String name)
    {
        TestBeanDeclaration childBean = new TestBeanDeclaration();
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
    private void checkBean(TestBean bean)
    {
        assertEquals("Wrong string property", "testString", bean
                .getStringValue());
        assertEquals("Wrong int property", 42, bean.getIntValue());
        TestBean buddy = bean.getBuddy();
        assertNotNull("Buddy was not set", buddy);
        assertEquals("Wrong string property in buddy", "Another test string",
                buddy.getStringValue());
        assertEquals("Wrong int property in buddy", 100, buddy.getIntValue());
    }

    /**
     * A simple bean class used for testing creation operations.
     */
    public static class TestBean
    {
        private String stringValue;

        private int intValue;

        private TestBean buddy;

        public TestBean getBuddy()
        {
            return buddy;
        }

        public void setBuddy(TestBean buddy)
        {
            this.buddy = buddy;
        }

        public int getIntValue()
        {
            return intValue;
        }

        public void setIntValue(int intValue)
        {
            this.intValue = intValue;
        }

        public String getStringValue()
        {
            return stringValue;
        }

        public void setStringValue(String stringValue)
        {
            this.stringValue = stringValue;
        }
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
     * implementation is really simple: If the TestBean class is provided, a new
     * instance will be created. Otherwise an exception is thrown.
     */
    static class TestBeanFactory implements BeanFactory
    {
        Object parameter;

        boolean supportsDefaultClass;

        public Object createBean(Class<?> beanClass, BeanDeclaration data, Object param)
                throws Exception
        {
            parameter = param;
            if (TestBean.class.equals(beanClass))
            {
                TestBean bean = new TestBean();
                BeanHelper.initBean(bean, data);
                return bean;
            }
            else if (BeanCreationTestBeanWithListChild.class.equals(beanClass))
            {
                BeanCreationTestBeanWithListChild bean = new BeanCreationTestBeanWithListChild();
                BeanHelper.initBean(bean, data);
                return bean;
            }
            else
            {
                throw new IllegalArgumentException("Unsupported class: "
                        + beanClass);
            }
        }

        /**
         * Returns the default class, but only if the supportsDefaultClass flag
         * is set.
         */
        public Class<?> getDefaultBeanClass()
        {
            return supportsDefaultClass ? TestBean.class : null;
        }
    }

    /**
     * A test implementation of the BeanDeclaration interface. This
     * implementation allows to set the values directly, which should be
     * returned by the methods required by the BeanDeclaration interface.
     */
    static class TestBeanDeclaration implements BeanDeclaration
    {
        private String beanClassName;

        private String beanFactoryName;

        private Object beanFactoryParameter;

        private Map<String, Object> beanProperties;

        private Map<String, Object> nestedBeanDeclarations;

        public String getBeanClassName()
        {
            return beanClassName;
        }

        public void setBeanClassName(String beanClassName)
        {
            this.beanClassName = beanClassName;
        }

        public String getBeanFactoryName()
        {
            return beanFactoryName;
        }

        public void setBeanFactoryName(String beanFactoryName)
        {
            this.beanFactoryName = beanFactoryName;
        }

        public Object getBeanFactoryParameter()
        {
            return beanFactoryParameter;
        }

        public void setBeanFactoryParameter(Object beanFactoryParameter)
        {
            this.beanFactoryParameter = beanFactoryParameter;
        }

        public Map<String, Object> getBeanProperties()
        {
            return beanProperties;
        }

        public void setBeanProperties(Map<String, Object> beanProperties)
        {
            this.beanProperties = beanProperties;
        }

        public Map<String, Object> getNestedBeanDeclarations()
        {
            return nestedBeanDeclarations;
        }

        public void setNestedBeanDeclarations(Map<String, Object> nestedBeanDeclarations)
        {
            this.nestedBeanDeclarations = nestedBeanDeclarations;
        }
    }
}
