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

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration2.convert.ConversionHandler;
import org.apache.commons.configuration2.convert.DefaultConversionHandler;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;

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
 */
public class DefaultBeanFactory implements BeanFactory
{
    /** Stores the default instance of this class. */
    public static final DefaultBeanFactory INSTANCE = new DefaultBeanFactory();

    /** A format string for generating error messages for constructor matching. */
    private static final String FMT_CTOR_ERROR =
            "%s! Bean class = %s, constructor arguments = %s";

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
    public DefaultBeanFactory(final ConversionHandler convHandler)
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
    @Override
    public Object createBean(final BeanCreationContext bcc) throws Exception
    {
        final Object result = createBeanInstance(bcc);
        initBeanInstance(result, bcc);
        return result;
    }

    /**
     * Returns the default bean class used by this factory. This is always
     * <b>null</b> for this implementation.
     *
     * @return the default bean class
     */
    @Override
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
    protected Object createBeanInstance(final BeanCreationContext bcc)
            throws Exception
    {
        final Constructor<?> ctor =
                findMatchingConstructor(bcc.getBeanClass(),
                        bcc.getBeanDeclaration());
        final Object[] args = fetchConstructorArgs(ctor, bcc);
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
    protected void initBeanInstance(final Object bean, final BeanCreationContext bcc) throws Exception
    {
        bcc.initBean(bean, bcc.getBeanDeclaration());
    }

    /**
     * Evaluates constructor arguments in the specified {@code BeanDeclaration}
     * and tries to find a unique matching constructor. If this is not possible,
     * an exception is thrown. Note: This method is intended to be used by
     * concrete {@link BeanFactory} implementations and not by client code.
     *
     * @param beanClass the class of the bean to be created
     * @param data the current {@code BeanDeclaration}
     * @param <T> the type of the bean to be created
     * @return the single matching constructor
     * @throws ConfigurationRuntimeException if no single matching constructor
     *         can be found
     * @throws NullPointerException if the bean class or bean declaration are
     *         <b>null</b>
     */
    protected static <T> Constructor<T> findMatchingConstructor(
            final Class<T> beanClass, final BeanDeclaration data)
    {
        final List<Constructor<T>> matchingConstructors =
                findMatchingConstructors(beanClass, data);
        checkSingleMatchingConstructor(beanClass, data, matchingConstructors);
        return matchingConstructors.get(0);
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
    private Object[] fetchConstructorArgs(final Constructor<?> ctor,
            final BeanCreationContext bcc)
    {
        final Class<?>[] types = ctor.getParameterTypes();
        assert types.length == nullSafeConstructorArgs(bcc.getBeanDeclaration()).size()
                : "Wrong number of constructor arguments!";
        final Object[] args = new Object[types.length];
        int idx = 0;

        for (final ConstructorArg arg : nullSafeConstructorArgs(bcc.getBeanDeclaration()))
        {
            final Object val =
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
            final BeanDeclaration data)
    {
        Collection<ConstructorArg> args = data.getConstructorArgs();
        if (args == null)
        {
            args = Collections.emptySet();
        }
        return args;
    }

    /**
     * Returns a list with all constructors which are compatible with the
     * constructor arguments specified by the given {@code BeanDeclaration}.
     *
     * @param beanClass the bean class to be instantiated
     * @param data the current {@code BeanDeclaration}
     * @return a list with all matching constructors
     */
    private static <T> List<Constructor<T>> findMatchingConstructors(
            final Class<T> beanClass, final BeanDeclaration data)
    {
        final List<Constructor<T>> result = new LinkedList<>();
        final Collection<ConstructorArg> args = getConstructorArgs(data);
        for (final Constructor<?> ctor : beanClass.getConstructors())
        {
            if (matchesConstructor(ctor, args))
            {
                // cast should be okay according to the Javadocs of
                // getConstructors()
                @SuppressWarnings("unchecked")
                final
                Constructor<T> match = (Constructor<T>) ctor;
                result.add(match);
            }
        }
        return result;
    }

    /**
     * Checks whether the given constructor is compatible with the given list of
     * arguments.
     *
     * @param ctor the constructor to be checked
     * @param args the collection of constructor arguments
     * @return a flag whether this constructor is compatible with the given
     *         arguments
     */
    private static boolean matchesConstructor(final Constructor<?> ctor,
            final Collection<ConstructorArg> args)
    {
        final Class<?>[] types = ctor.getParameterTypes();
        if (types.length != args.size())
        {
            return false;
        }

        int idx = 0;
        for (final ConstructorArg arg : args)
        {
            if (!arg.matches(types[idx++]))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Helper method for extracting constructor arguments from a bean
     * declaration. Deals with <b>null</b> values.
     *
     * @param data the bean declaration
     * @return the collection with constructor arguments (never <b>null</b>)
     */
    private static Collection<ConstructorArg> getConstructorArgs(
            final BeanDeclaration data)
    {
        Collection<ConstructorArg> args = data.getConstructorArgs();
        if (args == null)
        {
            args = Collections.emptySet();
        }
        return args;
    }

    /**
     * Helper method for testing whether exactly one matching constructor was
     * found. Throws a meaningful exception if there is not a single matching
     * constructor.
     *
     * @param beanClass the bean class
     * @param data the bean declaration
     * @param matchingConstructors the list with matching constructors
     * @throws ConfigurationRuntimeException if there is not exactly one match
     */
    private static <T> void checkSingleMatchingConstructor(final Class<T> beanClass,
            final BeanDeclaration data, final List<Constructor<T>> matchingConstructors)
    {
        if (matchingConstructors.isEmpty())
        {
            throw constructorMatchingException(beanClass, data,
                    "No matching constructor found");
        }
        if (matchingConstructors.size() > 1)
        {
            throw constructorMatchingException(beanClass, data,
                    "Multiple matching constructors found");
        }
    }

    /**
     * Creates an exception if no single matching constructor was found with a
     * meaningful error message.
     *
     * @param beanClass the affected bean class
     * @param data the bean declaration
     * @param msg an error message
     * @return the exception with the error message
     */
    private static ConfigurationRuntimeException constructorMatchingException(
            final Class<?> beanClass, final BeanDeclaration data, final String msg)
    {
        return new ConfigurationRuntimeException(FMT_CTOR_ERROR,
                msg, beanClass.getName(), getConstructorArgs(data).toString());
    }
}
