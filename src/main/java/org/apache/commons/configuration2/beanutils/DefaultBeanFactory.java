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

/**
 * <p>
 * The default implementation of the <code>BeanFactory</code> interface.
 * </p>
 * <p>
 * This class creates beans of arbitrary types using reflection. Each time the
 * <code>createBean()</code> method is invoked, a new bean instance is
 * created. A default bean class is not supported.
 * </p>
 * <p>
 * An instance of this factory class will be set as the default bean factory for
 * the <code>{@link BeanHelper}</code> class. This means that if not bean
 * factory is specified in a <code>{@link BeanDeclaration}</code>, this
 * default instance will be used.
 * </p>
 *
 * @since 1.3
 * @author Oliver Heger
 * @version $Id$
 */
public class DefaultBeanFactory implements BeanFactory
{
    /** Stores the default instance of this class. */
    public static final DefaultBeanFactory INSTANCE = new DefaultBeanFactory();

    /**
     * Creates a new bean instance. This implementation delegates to the
     * protected methods <code>createBeanInstance()</code> and
     * <code>initBeanInstance()</code> for creating and initializing the bean.
     * This makes it easier for derived classes that need to change specific
     * functionality of the base class.
     *
     * @param beanClass the class of the bean, from which an instance is to be
     * created
     * @param data the bean declaration object
     * @param parameter an additional parameter (ignored by this implementation)
     * @return the new bean instance
     * @throws Exception if an error occurs
     */
    public Object createBean(Class<?> beanClass, BeanDeclaration data,
            Object parameter) throws Exception
    {
        Object result = createBeanInstance(beanClass, data);
        initBeanInstance(result, data);
        return result;
    }

    /**
     * Returns the default bean class used by this factory. This is always
     * <b>null</b> for this implementation.
     *
     * @return the default bean class
     */
    public Class<?> getDefaultBeanClass()
    {
        return null;
    }

    /**
     * Creates the bean instance. This method is called by
     * <code>createBean()</code>. It uses reflection to create a new instance
     * of the specified class.
     *
     * @param beanClass the class of the bean to be created
     * @param data the bean declaration
     * @return the new bean instance
     * @throws Exception if an error occurs
     */
    protected Object createBeanInstance(Class<?> beanClass, BeanDeclaration data)
            throws Exception
    {
        return beanClass.newInstance();
    }

    /**
     * Initializes the newly created bean instance. This method is called by
     * <code>createBean()</code>. It calls the
     * <code>{@link BeanHelper#initBean(Object, BeanDeclaration) initBean()}</code>
     * of <code>{@link BeanHelper}</code> for performing the initialization.
     *
     * @param bean the newly created bean instance
     * @param data the bean declaration object
     * @throws Exception if an error occurs
     */
    protected void initBeanInstance(Object bean, BeanDeclaration data)
            throws Exception
    {
        BeanHelper.initBean(bean, data);
    }
}
