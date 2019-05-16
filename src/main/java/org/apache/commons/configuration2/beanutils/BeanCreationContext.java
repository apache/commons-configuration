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
 * Definition of a context object storing all required information for the
 * creation of a bean.
 * </p>
 * <p>
 * An object implementing this interface is passed to a {@link BeanFactory}. The
 * interface also contains methods for the creation and initialization of nested
 * beans (e.g. constructor arguments or complex properties of the bean to be
 * created).
 * </p>
 *
 * @since 2.0
 */
public interface BeanCreationContext
{
    /**
     * Returns the class of the bean to be created.
     *
     * @return the bean class
     */
    Class<?> getBeanClass();

    /**
     * Returns the {@code BeanDeclaration} with the data for the new bean. This
     * data is used to initialize the bean's properties.
     *
     * @return the {@code BeanDeclaration} defining the bean to be created
     */
    BeanDeclaration getBeanDeclaration();

    /**
     * Returns the (optional) parameter object for the bean factory. This is a
     * mechanism which can be used to pass custom parameters to a
     * {@link BeanFactory}.
     *
     * @return the parameter for the bean factory
     */
    Object getParameter();

    /**
     * Initializes a bean's property based on the given {@code BeanDeclaration}.
     *
     * @param bean the bean to be initialized
     * @param data the {@code BeanDeclaration} with initialization data for this
     *        bean
     */
    void initBean(Object bean, BeanDeclaration data);

    /**
     * Creates a bean based on the given {@code BeanDeclaration}. This method
     * can be used to create dependent beans needed for the initialization of
     * the bean that is actually created.
     *
     * @param data the {@code BeanDeclaration} describing the bean
     * @return the bean created based on this declaration
     */
    Object createBean(BeanDeclaration data);
}
