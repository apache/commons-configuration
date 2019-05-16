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

import java.util.Collection;
import java.util.Map;

/**
 * A test implementation of the BeanDeclaration interface. This implementation
 * allows setting the values directly, which should be returned by the methods
 * required by the BeanDeclaration interface. It is used by multiple test
 * classes.
 *
 */
class BeanDeclarationTestImpl implements BeanDeclaration
{
    private String beanClassName;

    private String beanFactoryName;

    private Object beanFactoryParameter;

    private Map<String, Object> beanProperties;

    private Map<String, Object> nestedBeanDeclarations;

    private Collection<ConstructorArg> constructorArgs;

    @Override
    public String getBeanClassName()
    {
        return beanClassName;
    }

    public void setBeanClassName(final String beanClassName)
    {
        this.beanClassName = beanClassName;
    }

    @Override
    public String getBeanFactoryName()
    {
        return beanFactoryName;
    }

    public void setBeanFactoryName(final String beanFactoryName)
    {
        this.beanFactoryName = beanFactoryName;
    }

    @Override
    public Object getBeanFactoryParameter()
    {
        return beanFactoryParameter;
    }

    public void setBeanFactoryParameter(final Object beanFactoryParameter)
    {
        this.beanFactoryParameter = beanFactoryParameter;
    }

    @Override
    public Map<String, Object> getBeanProperties()
    {
        return beanProperties;
    }

    public void setBeanProperties(final Map<String, Object> beanProperties)
    {
        this.beanProperties = beanProperties;
    }

    @Override
    public Map<String, Object> getNestedBeanDeclarations()
    {
        return nestedBeanDeclarations;
    }

    public void setNestedBeanDeclarations(
            final Map<String, Object> nestedBeanDeclarations)
    {
        this.nestedBeanDeclarations = nestedBeanDeclarations;
    }

    @Override
    public Collection<ConstructorArg> getConstructorArgs()
    {
        return constructorArgs;
    }

    public void setConstructorArgs(final Collection<ConstructorArg> args)
    {
        constructorArgs = args;
    }
}
