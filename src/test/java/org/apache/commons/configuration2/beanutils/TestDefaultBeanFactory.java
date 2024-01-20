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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.ConversionHandler;
import org.apache.commons.configuration2.convert.DefaultConversionHandler;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for DefaultBeanFactory.
 */
public class TestDefaultBeanFactory {
    /** Constant for the test value of the string property. */
    private static final String TEST_STRING = "testString";

    /** Constant for the test value of the numeric property. */
    private static final int TEST_INT = 42;

    /**
     * Creates a bean creation context for a create operation.
     *
     * @param cls the bean class
     * @param decl the bean declaration
     * @return the new creation context
     */
    private static BeanCreationContext createBcc(final Class<?> cls, final BeanDeclaration decl) {
        return new BeanCreationContext() {
            private final BeanHelper beanHelper = new BeanHelper();

            @Override
            public Object createBean(final BeanDeclaration data) {
                return beanHelper.createBean(data);
            }

            @Override
            public Class<?> getBeanClass() {
                return cls;
            }

            @Override
            public BeanDeclaration getBeanDeclaration() {
                return decl;
            }

            @Override
            public Object getParameter() {
                return null;
            }

            @Override
            public void initBean(final Object bean, final BeanDeclaration data) {
                beanHelper.initBean(bean, data);
            }
        };
    }

    /**
     * Returns an initialized bean declaration.
     *
     * @return the bean declaration
     */
    private static BeanDeclarationTestImpl setUpBeanDeclaration() {
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

    /** The object to be tested. */
    private DefaultBeanFactory factory;

    @BeforeEach
    public void setUp() {
        factory = new DefaultBeanFactory();
    }

    /**
     * Tests creating a bean.
     */
    @Test
    public void testCreateBean() throws Exception {
        final BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        final Map<String, Object> props = new HashMap<>();
        props.put("throwExceptionOnMissing", Boolean.TRUE);
        decl.setBeanProperties(props);
        final Object bean = factory.createBean(createBcc(PropertiesConfiguration.class, decl));
        assertNotNull(bean);
        assertEquals(PropertiesConfiguration.class, bean.getClass());
        final PropertiesConfiguration config = (PropertiesConfiguration) bean;
        assertTrue(config.isThrowExceptionOnMissing());
    }

    /**
     * Tests whether a bean can be created by calling its constructor.
     */
    @Test
    public void testCreateBeanConstructor() throws Exception {
        final BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        final Collection<ConstructorArg> args = new ArrayList<>();
        args.add(ConstructorArg.forValue("test"));
        args.add(ConstructorArg.forValue("42"));
        decl.setConstructorArgs(args);
        final BeanCreationTestCtorBean bean = (BeanCreationTestCtorBean) factory.createBean(createBcc(BeanCreationTestCtorBean.class, decl));
        assertEquals("test", bean.getStringValue());
        assertEquals(42, bean.getIntValue());
    }

    /**
     * Tests whether nested bean declarations in constructor arguments are taken into account.
     */
    @Test
    public void testCreateBeanConstructorNestedBean() throws Exception {
        final BeanDeclarationTestImpl declNested = new BeanDeclarationTestImpl();
        final Collection<ConstructorArg> args = new ArrayList<>();
        args.add(ConstructorArg.forValue("test", String.class.getName()));
        declNested.setConstructorArgs(args);
        declNested.setBeanClassName(BeanCreationTestCtorBean.class.getName());
        final BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        decl.setConstructorArgs(Collections.singleton(ConstructorArg.forBeanDeclaration(declNested, BeanCreationTestBean.class.getName())));
        final BeanCreationTestCtorBean bean = (BeanCreationTestCtorBean) factory.createBean(createBcc(BeanCreationTestCtorBean.class, decl));
        assertNotNull(bean.getBuddy());
        assertEquals("test", bean.getBuddy().getStringValue());
    }

    /**
     * Tests whether a correct default conversion handler is set.
     */
    @Test
    public void testDefaultConversionHandler() {
        assertSame(DefaultConversionHandler.INSTANCE, factory.getConversionHandler());
    }

    /**
     * Tests whether ambiguous constructor arguments are detected.
     */
    @Test
    public void testFindMatchingConstructorAmbiguous() {
        final BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        final Collection<ConstructorArg> args = new ArrayList<>();
        args.add(ConstructorArg.forValue(TEST_STRING));
        decl.setConstructorArgs(args);
        assertThrows(ConfigurationRuntimeException.class, () -> DefaultBeanFactory.findMatchingConstructor(BeanCreationTestCtorBean.class, decl));
    }

    /**
     * Tests whether a matching constructor is found if the number of arguments is unique.
     */
    @Test
    public void testFindMatchingConstructorArgCount() {
        final BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        final Collection<ConstructorArg> args = new ArrayList<>();
        args.add(ConstructorArg.forValue(TEST_STRING));
        args.add(ConstructorArg.forValue(String.valueOf(TEST_INT)));
        decl.setConstructorArgs(args);
        final Constructor<BeanCreationTestCtorBean> ctor = DefaultBeanFactory.findMatchingConstructor(BeanCreationTestCtorBean.class, decl);
        final Class<?>[] paramTypes = ctor.getParameterTypes();
        assertArrayEquals(new Class<?>[] {String.class, Integer.TYPE}, paramTypes);
    }

    /**
     * Tests whether explicit type declarations are used to resolve ambiguous parameter types.
     */
    @Test
    public void testFindMatchingConstructorExplicitType() {
        final BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        final Collection<ConstructorArg> args = new ArrayList<>();
        args.add(ConstructorArg.forBeanDeclaration(setUpBeanDeclaration(), BeanCreationTestBean.class.getName()));
        decl.setConstructorArgs(args);
        final Constructor<BeanCreationTestCtorBean> ctor = DefaultBeanFactory.findMatchingConstructor(BeanCreationTestCtorBean.class, decl);
        final Class<?>[] paramTypes = ctor.getParameterTypes();
        assertArrayEquals(new Class<?>[] {BeanCreationTestBean.class}, paramTypes);
    }

    /**
     * Tests whether the standard constructor can be found.
     */
    @Test
    public void testFindMatchingConstructorNoArgs() {
        final BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        final Constructor<BeanCreationTestBean> ctor = DefaultBeanFactory.findMatchingConstructor(BeanCreationTestBean.class, decl);
        assertEquals(0, ctor.getParameterTypes().length);
    }

    /**
     * Tests the case that no matching constructor is found.
     */
    @Test
    public void testFindMatchingConstructorNoMatch() {
        final BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        final Collection<ConstructorArg> args = new ArrayList<>();
        args.add(ConstructorArg.forValue(TEST_STRING, getClass().getName()));
        decl.setConstructorArgs(args);
        final ConfigurationRuntimeException crex = assertThrows(ConfigurationRuntimeException.class,
                () -> DefaultBeanFactory.findMatchingConstructor(BeanCreationTestCtorBean.class, decl));
        final String msg = crex.getMessage();
        assertThat(msg, containsString(BeanCreationTestCtorBean.class.getName()));
        assertThat(msg, containsString(TEST_STRING));
        assertThat(msg, containsString("(" + getClass().getName() + ')'));
    }

    /**
     * Tests obtaining the default class. This should be null.
     */
    @Test
    public void testGetDefaultBeanClass() {
        assertNull(factory.getDefaultBeanClass());
    }

    /**
     * Tests whether a custom conversion handler can be passed to the constructor.
     */
    @Test
    public void testInitWithConversionHandler() {
        final ConversionHandler handler = mock(ConversionHandler.class);
        factory = new DefaultBeanFactory(handler);
        assertSame(handler, factory.getConversionHandler());
    }
}
