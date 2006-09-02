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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;

import junit.framework.TestCase;

/**
 * Test class for DefaultBeanFactory.
 *
 * @since 1.3
 * @author Oliver Heger
 * @version $Id$
 */
public class TestDefaultBeanFactory extends TestCase
{
    /** The object to be tested. */
    DefaultBeanFactory factory;

    protected void setUp() throws Exception
    {
        super.setUp();
        factory = new DefaultBeanFactory();
    }

    /**
     * Tests obtaining the default class. This should be null.
     */
    public void testGetDefaultBeanClass()
    {
        assertNull("Default class is not null", factory.getDefaultBeanClass());
    }

    /**
     * Tests creating a bean.
     */
    public void testCreateBean() throws Exception
    {
        Object bean = factory.createBean(PropertiesConfiguration.class,
                new TestBeanDeclaration(), null);
        assertNotNull("New bean is null", bean);
        assertEquals("Bean is of wrong class", PropertiesConfiguration.class,
                bean.getClass());
        PropertiesConfiguration config = (PropertiesConfiguration) bean;
        assertTrue("Bean was not initialized", config
                .isThrowExceptionOnMissing());
    }

    /**
     * A simple implementation of BeanDeclaration used for testing purposes.
     */
    static class TestBeanDeclaration implements BeanDeclaration
    {
        public String getBeanFactoryName()
        {
            return null;
        }

        public Object getBeanFactoryParameter()
        {
            return null;
        }

        public String getBeanClassName()
        {
            return null;
        }

        public Map getBeanProperties()
        {
            Map props = new HashMap();
            props.put("throwExceptionOnMissing", Boolean.TRUE);
            return props;
        }

        public Map getNestedBeanDeclarations()
        {
            return null;
        }
    }
}
