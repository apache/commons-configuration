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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.convert.ConversionHandler;
import org.apache.commons.configuration.convert.DefaultConversionHandler;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for DefaultBeanFactory.
 *
 * @since 1.3
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 */
public class TestDefaultBeanFactory
{
    /** The object to be tested. */
    private DefaultBeanFactory factory;

    @Before
    public void setUp() throws Exception
    {
        factory = new DefaultBeanFactory();
    }

    /**
     * Creates a bean creation context for a create operation.
     *
     * @param cls the bean class
     * @param decl the bean declaration
     * @return the new creation context
     */
    private static BeanCreationContext createBcc(final Class<?> cls,
            final BeanDeclaration decl)
    {
        return new BeanCreationContext()
        {
            public void initBean(Object bean, BeanDeclaration data)
            {
                BeanHelper.initBean(bean, data);
            }

            public Object getParameter()
            {
                return null;
            }

            public BeanDeclaration getBeanDeclaration()
            {
                return decl;
            }

            public Class<?> getBeanClass()
            {
                return cls;
            }

            public Object createBean(BeanDeclaration data)
            {
                return BeanHelper.createBean(data);
            }
        };
    }

    /**
     * Tests obtaining the default class. This should be null.
     */
    @Test
    public void testGetDefaultBeanClass()
    {
        assertNull("Default class is not null", factory.getDefaultBeanClass());
    }

    /**
     * Tests whether a correct default conversion handler is set.
     */
    @Test
    public void testDefaultConversionHandler()
    {
        assertSame("Wrong default conversion handler",
                DefaultConversionHandler.INSTANCE,
                factory.getConversionHandler());
    }

    /**
     * Tests whether a custom conversion handler can be passed to the
     * constructor.
     */
    @Test
    public void testInitWithConversionHandler()
    {
        ConversionHandler handler =
                EasyMock.createMock(ConversionHandler.class);
        EasyMock.replay(handler);
        factory = new DefaultBeanFactory(handler);
        assertSame("Wrong conversion handler", handler,
                factory.getConversionHandler());
    }

    /**
     * Tests creating a bean.
     */
    @Test
    public void testCreateBean() throws Exception
    {
        BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("throwExceptionOnMissing", Boolean.TRUE);
        decl.setBeanProperties(props);
        Object bean = factory.createBean(createBcc(PropertiesConfiguration.class, decl));
        assertNotNull("New bean is null", bean);
        assertEquals("Bean is of wrong class", PropertiesConfiguration.class,
                bean.getClass());
        PropertiesConfiguration config = (PropertiesConfiguration) bean;
        assertTrue("Bean was not initialized", config
                .isThrowExceptionOnMissing());
    }

    /**
     * Tests whether a bean can be created by calling its constructor.
     */
    @Test
    public void testCreateBeanConstructor() throws Exception
    {
        BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        Collection<ConstructorArg> args = new ArrayList<ConstructorArg>();
        args.add(ConstructorArg.forValue("test"));
        args.add(ConstructorArg.forValue("42"));
        decl.setConstructorArgs(args);
        BeanCreationTestCtorBean bean =
                (BeanCreationTestCtorBean) factory.createBean(createBcc(
                        BeanCreationTestCtorBean.class, decl));
        assertEquals("Wrong string property", "test", bean.getStringValue());
        assertEquals("Wrong int property", 42, bean.getIntValue());
    }

    /**
     * Tests whether nested bean declarations in constructor arguments are taken
     * into account.
     */
    @Test
    public void testCreateBeanConstructorNestedBean() throws Exception
    {
        BeanDeclarationTestImpl declNested = new BeanDeclarationTestImpl();
        Collection<ConstructorArg> args = new ArrayList<ConstructorArg>();
        args.add(ConstructorArg.forValue("test", String.class.getName()));
        declNested.setConstructorArgs(args);
        declNested.setBeanClassName(BeanCreationTestCtorBean.class.getName());
        BeanDeclarationTestImpl decl = new BeanDeclarationTestImpl();
        decl.setConstructorArgs(Collections.singleton(ConstructorArg
                .forBeanDeclaration(declNested,
                        BeanCreationTestBean.class.getName())));
        BeanCreationTestCtorBean bean =
                (BeanCreationTestCtorBean) factory.createBean(createBcc(
                        BeanCreationTestCtorBean.class, decl));
        assertNotNull("Buddy bean was not set", bean.getBuddy());
        assertEquals("Wrong property of buddy bean", "test", bean.getBuddy()
                .getStringValue());
    }
}
