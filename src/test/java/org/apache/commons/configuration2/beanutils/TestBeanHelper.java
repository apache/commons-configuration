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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for BeanHelper.
 */
public class TestBeanHelper {
    /**
     * An implementation of the BeanFactory interface used for testing. This implementation is really simple: If the
     * BeanCreationTestBean class is provided, a new instance will be created. Otherwise an exception is thrown.
     */
    private final class TestBeanFactory implements BeanFactory {
        Object parameter;

        boolean supportsDefaultClass;

        /** A counter for the created instances. */
        private int createBeanCount;

        @Override
        public Object createBean(final BeanCreationContext bcc) {
            createBeanCount++;
            parameter = bcc.getParameter();
            if (BeanCreationTestBean.class.equals(bcc.getBeanClass())) {
                final BeanCreationTestBean bean = new BeanCreationTestBean();
                helper.initBean(bean, bcc.getBeanDeclaration());
                return bean;
            }
            if (BeanCreationTestBeanWithListChild.class.equals(bcc.getBeanClass())) {
                final BeanCreationTestBeanWithListChild bean = new BeanCreationTestBeanWithListChild();
                helper.initBean(bean, bcc.getBeanDeclaration());
                return bean;
            }
            throw new IllegalArgumentException("Unsupported class: " + bcc.getBeanClass());
        }

        /**
         * Returns the number of beans created via this factory.
         *
         * @return the number of created beans
         */
        public int getCreateBeanCount() {
            return createBeanCount;
        }

        /**
         * Returns the default class, but only if the supportsDefaultClass flag is set.
         */
        @Override
        public Class<?> getDefaultBeanClass() {
            return supportsDefaultClass ? BeanCreationTestBean.class : null;
        }
    }

    /** Constant for the test value of the string property. */
    private static final String TEST_STRING = "testString";

    /** Constant for the test value of the numeric property. */
    private static final int TEST_INT = 42;

    /** Constant for the name of the test bean factory. */
    private static final String TEST_FACTORY = "testFactory";

    /** The test bean helper instance. */
    private BeanHelper helper;

    /**
     * Tests if the bean was correctly initialized from the data of the test bean declaration.
     *
     * @param bean the bean to be checked
     */
    private void checkBean(final BeanCreationTestBean bean) {
        assertEquals(TEST_STRING, bean.getStringValue());
        assertEquals(TEST_INT, bean.getIntValue());
        final BeanCreationTestBean buddy = bean.getBuddy();
        assertNotNull(buddy);
        assertEquals("Another test string", buddy.getStringValue());
        assertEquals(100, buddy.getIntValue());
    }

    /**
     * Tests if the bean was correctly initialized from the data of the test bean declaration.
     *
     * @param bean the bean to be checked
     */
    private void checkBean(final BeanCreationTestBeanWithListChild bean) {
        assertEquals(TEST_STRING, bean.getStringValue());
        assertEquals(TEST_INT, bean.getIntValue());
        final List<BeanCreationTestBean> children = bean.getChildren();
        assertNotNull(children);
        assertEquals(2, children.size());
        assertNotNull(children.get(0));
        assertNotNull(children.get(1));
    }

    /**
     * Create a simple bean declaration that has no children for testing of nested children bean declarations.
     *
     * @param name A name prefix that can be used to disambiguate the children
     * @return A simple declaration
     */
    private BeanDeclarationTestImpl createChildBean(final String name) {
        final BeanDeclarationTestImpl childBean = new BeanDeclarationTestImpl();
        final Map<String, Object> properties2 = new HashMap<>();
        properties2.put("stringValue", name + " Another test string");
        properties2.put("intValue", 100);
        childBean.setBeanProperties(properties2);
        childBean.setBeanClassName(BeanCreationTestBean.class.getName());

        return childBean;
    }

    @BeforeEach
    public void setUp() throws Exception {
        helper = new BeanHelper(new TestBeanFactory());
    }

    /**
     * Returns an initialized bean declaration.
     *
     * @return the bean declaration
     */
    private BeanDeclarationTestImpl setUpBeanDeclaration() {
        final BeanDeclarationTestImpl data = new BeanDeclarationTestImpl();
        final Map<String, Object> properties = new HashMap<>();
        properties.put("stringValue", TEST_STRING);
        properties.put("intValue", String.valueOf(TEST_INT));
        data.setBeanProperties(properties);
        final BeanDeclarationTestImpl buddyData = new BeanDeclarationTestImpl();
        final Map<String, Object> properties2 = new HashMap<>();
        properties2.put("stringValue", "Another test string");
        properties2.put("intValue", 100);
        buddyData.setBeanProperties(properties2);
        buddyData.setBeanClassName(BeanCreationTestBean.class.getName());

        final Map<String, Object> nested = new HashMap<>();
        nested.put("buddy", buddyData);
        data.setNestedBeanDeclarations(nested);
        return data;
    }

    /**
     * Same as setUpBeanDeclaration, but returns a nested array of beans as a single property. Tests multi-value
     * (Collection<BeanDeclaration>) children construction.
     *
     * @return The bean declaration with a list child bean proerty
     */
    private BeanDeclarationTestImpl setUpBeanDeclarationWithListChild() {
        final BeanDeclarationTestImpl data = new BeanDeclarationTestImpl();
        final Map<String, Object> properties = new HashMap<>();
        properties.put("stringValue", TEST_STRING);
        properties.put("intValue", String.valueOf(TEST_INT));
        data.setBeanProperties(properties);

        final List<BeanDeclaration> childData = new ArrayList<>();
        childData.add(createChildBean("child1"));
        childData.add(createChildBean("child2"));
        final Map<String, Object> nested = new HashMap<>();
        nested.put("children", childData);
        data.setNestedBeanDeclarations(nested);
        return data;
    }

    /**
     * Tests whether properties from one bean to another can be copied.
     */
    @Test
    public void testCopyProperties() throws Exception {
        final PropertiesConfiguration src = new PropertiesConfiguration();
        src.setHeader("TestHeader");
        src.setFooter("TestFooter");
        final LazyDynaBean dest = new LazyDynaBean();
        BeanHelper.copyProperties(dest, src);
        assertEquals("TestFooter", dest.get("footer"));
        assertEquals("TestHeader", dest.get("header"));
    }

    /**
     * Tests creating a bean. All necessary information is stored in the bean declaration.
     */
    @Test
    public void testCreateBean() {
        final TestBeanFactory factory = new TestBeanFactory();
        helper.registerBeanFactory(TEST_FACTORY, factory);
        final BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(BeanCreationTestBean.class.getName());
        checkBean((BeanCreationTestBean) helper.createBean(data, null));
        assertNull(factory.parameter);
        assertEquals(1, factory.getCreateBeanCount());
    }

    /**
     * Tests creating a bean. The bean's class is specified as the default class argument.
     */
    @Test
    public void testCreateBeanWithDefaultClass() {
        helper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        final BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        checkBean((BeanCreationTestBean) helper.createBean(data, BeanCreationTestBean.class));
    }

    /**
     * Tests creating a bean using the default bean factory.
     */
    @Test
    public void testCreateBeanWithDefaultFactory() {
        final BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanClassName(BeanCreationTestBean.class.getName());
        checkBean((BeanCreationTestBean) helper.createBean(data, null));
        final TestBeanFactory factory = (TestBeanFactory) helper.getDefaultBeanFactory();
        assertTrue(factory.getCreateBeanCount() > 0);
    }

    /**
     * Tests creating a bean when the factory throws an exception.
     */
    @Test
    public void testCreateBeanWithException() {
        helper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        final BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(getClass().getName());
        assertThrows(ConfigurationRuntimeException.class, () -> helper.createBean(data, null));
    }

    /**
     * Tests creating a bean when the bean's class is specified as the default class of the bean factory.
     */
    @Test
    public void testCreateBeanWithFactoryDefaultClass() {
        final TestBeanFactory factory = new TestBeanFactory();
        factory.supportsDefaultClass = true;
        helper.registerBeanFactory(TEST_FACTORY, factory);
        final BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        checkBean((BeanCreationTestBean) helper.createBean(data, null));
        assertEquals(1, factory.getCreateBeanCount());
    }

    /**
     * Tries to create a bean with a non existing class. This should cause an exception.
     */
    @Test
    public void testCreateBeanWithInvalidClass() {
        helper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        final BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName("non.existing.ClassName");
        assertThrows(ConfigurationRuntimeException.class, () -> helper.createBean(data, null));
    }

    /**
     * Tests whether a bean with a property of type collection can be created.
     */
    @Test
    public void testCreateBeanWithListChildBean() {
        final TestBeanFactory factory = new TestBeanFactory();
        helper.registerBeanFactory(TEST_FACTORY, factory);
        final BeanDeclarationTestImpl data = setUpBeanDeclarationWithListChild();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(BeanCreationTestBeanWithListChild.class.getName());
        checkBean((BeanCreationTestBeanWithListChild) helper.createBean(data, null));
        assertNull(factory.parameter);
        assertEquals(1, factory.getCreateBeanCount());
    }

    /**
     * Tries to create a bean if no class is provided. This should cause an exception.
     */
    @Test
    public void testCreateBeanWithNoClass() {
        helper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        final BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        assertThrows(ConfigurationRuntimeException.class, () -> helper.createBean(data, null));
    }

    /**
     * Tests creating a bean when no bean declaration is provided. This should cause an exception.
     */
    @Test
    public void testCreateBeanWithNullDeclaration() {
        assertThrows(IllegalArgumentException.class, () -> helper.createBean(null));
    }

    /**
     * Tests if a parameter is correctly passed to the bean factory.
     */
    @Test
    public void testCreateBeanWithParameter() {
        final Object param = 42;
        final TestBeanFactory factory = new TestBeanFactory();
        helper.registerBeanFactory(TEST_FACTORY, factory);
        final BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(BeanCreationTestBean.class.getName());
        checkBean((BeanCreationTestBean) helper.createBean(data, null, param));
        assertSame(param, factory.parameter);
    }

    /**
     * Tests creating a bean using a non registered factory.
     */
    @Test
    public void testCreateBeanWithUnknownFactory() {
        final BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(BeanCreationTestBean.class.getName());
        assertThrows(ConfigurationRuntimeException.class, () -> helper.createBean(data, null));
    }

    /**
     * Tests whether a wrapper DynaBean for a Java bean can be created.
     */
    @Test
    public void testCreateWrapDynaBean() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        final DynaBean bean = BeanHelper.createWrapDynaBean(config);
        final String value = "TestFooter";
        bean.set("footer", value);
        assertEquals(value, config.getFooter());
    }

    /**
     * Tries to create a wrapper DynaBean for a null bean.
     */
    @Test
    public void testCreateWrapDynaBeanNull() {
        assertThrows(IllegalArgumentException.class, () -> BeanHelper.createWrapDynaBean(null));
    }

    /**
     * Tests whether the correct default bean factory is set.
     */
    @Test
    public void testDefaultBeanFactory() {
        helper = new BeanHelper();
        assertSame(DefaultBeanFactory.INSTANCE, helper.getDefaultBeanFactory());
    }

    /**
     * Tests the default instance of BeanHelper.
     */
    @Test
    public void testDefaultInstance() {
        assertSame(DefaultBeanFactory.INSTANCE, BeanHelper.INSTANCE.getDefaultBeanFactory());
    }

    /**
     * Tests to deregister a bean factory.
     */
    @Test
    public void testDeregisterBeanFactory() {
        final BeanFactory factory = new TestBeanFactory();
        helper.registerBeanFactory(TEST_FACTORY, factory);
        assertSame(factory, helper.deregisterBeanFactory(TEST_FACTORY));
        assertEquals(Collections.emptySet(), helper.registeredFactoryNames());
    }

    /**
     * Tests deregisterBeanFactory() for a non-existing factory name.
     */
    @Test
    public void testDeregisterBeanFactoryNonExisting() {
        assertNull(helper.deregisterBeanFactory(TEST_FACTORY));
    }

    /**
     * Tests deregisterBeanFactory() for a null factory name.
     */
    @Test
    public void testDeregisterBeanFactoryNull() {
        assertNull(helper.deregisterBeanFactory(null));
    }

    /**
     * Tests initializing a bean.
     */
    @Test
    public void testInitBean() {
        final BeanDeclarationTestImpl data = setUpBeanDeclaration();
        final BeanCreationTestBean bean = new BeanCreationTestBean();
        helper.initBean(bean, data);
        checkBean(bean);
    }

    /**
     * Tries to initialize a bean with a bean declaration that contains an invalid property value. This should cause an
     * exception.
     */
    @Test
    public void testInitBeanWithInvalidProperty() {
        final BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.getBeanProperties().put("nonExistingProperty", Boolean.TRUE);
        final BeanCreationTestBean bean = new BeanCreationTestBean();
        assertThrows(ConfigurationRuntimeException.class, () -> helper.initBean(bean, data));
    }

    /**
     * Tests initializing a bean when the bean declaration does not contain any data.
     */
    @Test
    public void testInitBeanWithNoData() {
        final BeanDeclarationTestImpl data = new BeanDeclarationTestImpl();
        final BeanCreationTestBean bean = new BeanCreationTestBean();
        helper.initBean(bean, data);
        assertNull(bean.getStringValue());
        assertEquals(0, bean.getIntValue());
        assertNull(bean.getBuddy());
    }

    /**
     * Tests whether a specific default bean factory can be set when constructing an instance.
     */
    @Test
    public void testInitWithBeanFactory() {
        final BeanFactory factory = mock(BeanFactory.class);
        helper = new BeanHelper(factory);
        assertSame(factory, helper.getDefaultBeanFactory());
    }

    /**
     * Tests registering a new bean factory.
     */
    @Test
    public void testRegisterBeanFactory() {
        helper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        assertEquals(Collections.singleton(TEST_FACTORY), helper.registeredFactoryNames());
    }

    /**
     * Tries to register a null factory. This should cause an exception.
     */
    @Test
    public void testRegisterBeanFactoryNull() {
        assertThrows(IllegalArgumentException.class, () -> helper.registerBeanFactory(TEST_FACTORY, null));
    }

    /**
     * Tries to register a bean factory with a null name. This should cause an exception.
     */
    @Test
    public void testRegisterBeanFactoryNullName() {
        final BeanFactory beanFactory = new TestBeanFactory();
        assertThrows(IllegalArgumentException.class, () -> helper.registerBeanFactory(null, beanFactory));
    }

    /**
     * Tests that a newly created instance does not have any bean factories registered.
     */
    @Test
    public void testRegisteredFactoriesEmptyForNewInstance() {
        assertEquals(Collections.emptySet(), helper.registeredFactoryNames());
    }
}
