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

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.configuration.convert.ConversionHandler;
import org.apache.commons.configuration.convert.DefaultConversionHandler;

/**
 * <p>
 * The default implementation of the {@code BeanFactory} interface.
 * </p>
 * <p>
 * This class creates beans of arbitrary types using reflection. Each time the
 * {@code createBean()} method is invoked, a new bean instance is created. A
 * default bean class is not supported.
 * </p>
 * <p>
 * For data type conversions (which may be needed before invoking methods
 * through reflection to ensure that the current parameters match their declared
 * types) a {@link ConversionHandler} object is used. An instance of this class
 * can be passed to the constructor. Alternatively, a default
 * {@code ConversionHandler} instance is used.
 * </p>
 * <p>
 * An instance of this factory class will be set as the default bean factory for
 * the {@link BeanHelper} class. This means that if not bean factory is
 * specified in a {@link BeanDeclaration}, this default instance will be used.
 * </p>
 *
 * @since 1.3
 * @version $Id$
 */
public class DefaultBeanFactory implements BeanFactory
{
    /** Stores the default instance of this class. */
    public static final DefaultBeanFactory INSTANCE = new DefaultBeanFactory();

    /** The conversion handler used by this instance. */
    private final ConversionHandler conversionHandler;

    /**
     * Creates a new instance of {@code DefaultBeanFactory} using a default
     * {@code ConversionHandler}.
     */
    public DefaultBeanFactory()
    {
        this(null);
    }

    /**
     * Creates a new instance of {@code DefaultBeanFactory} using the specified
     * {@code ConversionHandler} for data type conversions.
     *
     * @param convHandler the {@code ConversionHandler}; can be <b>null</b>,
     *        then a default handler is used
     * @since 2.0
     */
    public DefaultBeanFactory(ConversionHandler convHandler)
    {
        conversionHandler =
                (convHandler != null) ? convHandler
                        : DefaultConversionHandler.INSTANCE;
    }

    /**
     * Returns the {@code ConversionHandler} used by this object.
     *
     * @return the {@code ConversionHandler}
     * @since 2.0
     */
    public ConversionHandler getConversionHandler()
    {
        return conversionHandler;
    }

    /**
     * Creates a new bean instance. This implementation delegates to the
     * protected methods {@code createBeanInstance()} and
     * {@code initBeanInstance()} for creating and initializing the bean.
     * This makes it easier for derived classes that need to change specific
     * functionality of the base class.
     *
     * @param bcc the context object defining the bean to be created
     * @return the new bean instance
     * @throws Exception if an error occurs
     */
    public Object createBean(BeanCreationContext bcc) throws Exception
    {
        Object result = createBeanInstance(bcc);
        initBeanInstance(result, bcc);
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
     * {@code createBean()}. It uses reflection to create a new instance
     * of the specified class.
     *
     * @param bcc the context object defining the bean to be created
     * @return the new bean instance
     * @throws Exception if an error occurs
     */
    protected Object createBeanInstance(BeanCreationContext bcc)
            throws Exception
    {
        Constructor<?> ctor =
                BeanHelper.findMatchingConstructor(bcc.getBeanClass(),
                        bcc.getBeanDeclaration());
        Object[] args = fetchConstructorArgs(ctor, bcc);
        return ctor.newInstance(args);
    }

    /**
     * Initializes the newly created bean instance. This method is called by
     * {@code createBean()}. It calls the {@code initBean()} method of the
     * context object for performing the initialization.
     *
     * @param bean the newly created bean instance
     * @param bcc the context object defining the bean to be created
     * @throws Exception if an error occurs
     */
    protected void initBeanInstance(Object bean, BeanCreationContext bcc) throws Exception
    {
        bcc.initBean(bean, bcc.getBeanDeclaration());
    }

    /**
     * Obtains the arguments for a constructor call to create a bean. This method
     * resolves nested bean declarations and performs necessary type
     * conversions.
     *
     * @param ctor the constructor to be invoked
     * @param bcc the context object defining the bean to be created
     * @return an array with constructor arguments
     */
    private Object[] fetchConstructorArgs(Constructor<?> ctor,
            BeanCreationContext bcc)
    {
        Class<?>[] types = ctor.getParameterTypes();
        assert types.length == nullSafeConstructorArgs(bcc.getBeanDeclaration()).size() :
            "Wrong number of constructor arguments!";
        Object[] args = new Object[types.length];
        int idx = 0;

        for (ConstructorArg arg : nullSafeConstructorArgs(bcc.getBeanDeclaration()))
        {
            Object val =
                    arg.isNestedBeanDeclaration() ? bcc.createBean(arg
                            .getBeanDeclaration()) : arg.getValue();
            args[idx] = getConversionHandler().to(val, types[idx], null);
            idx++;
        }

        return args;
    }

    /**
     * Fetches constructor arguments from the given bean declaration. Handles
     * <b>null</b> values safely.
     *
     * @param data the bean declaration
     * @return the collection with constructor arguments (never <b>null</b>)
     */
    private static Collection<ConstructorArg> nullSafeConstructorArgs(
            BeanDeclaration data)
    {
        Collection<ConstructorArg> args = data.getConstructorArgs();
        if (args == null)
        {
            args = Collections.emptySet();
        }
        return args;
    }
}
