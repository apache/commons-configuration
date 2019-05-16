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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.FluentPropertyBeanIntrospector;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.WrapDynaBean;
import org.apache.commons.beanutils.WrapDynaClass;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.lang3.ClassUtils;

/**
 * <p>
 * A helper class for creating bean instances that are defined in configuration
 * files.
 * </p>
 * <p>
 * This class provides utility methods related to bean creation
 * operations. These methods simplify such operations because a client need not
 * deal with all involved interfaces. Usually, if a bean declaration has already
 * been obtained, a single method call is necessary to create a new bean
 * instance.
 * </p>
 * <p>
 * This class also supports the registration of custom bean factories.
 * Implementations of the {@link BeanFactory} interface can be
 * registered under a symbolic name using the {@code registerBeanFactory()}
 * method. In the configuration file the name of the bean factory can be
 * specified in the bean declaration. Then this factory will be used to create
 * the bean.
 * </p>
 * <p>
 * In order to create beans using {@code BeanHelper}, create and instance of
 * this class and initialize it accordingly - a default {@link BeanFactory}
 * can be passed to the constructor, and additional bean factories can be
 * registered (see above). Then this instance can be used to create beans from
 * {@link BeanDeclaration} objects. {@code BeanHelper} is thread-safe. So an
 * instance can be passed around in an application and shared between multiple
 * components.
 * </p>
 *
 * @since 1.3
 */
public final class BeanHelper
{
    /**
     * A default instance of {@code BeanHelper} which can be shared between
     * arbitrary components. If no special configuration is needed, this
     * instance can be used throughout an application. Otherwise, new instances
     * can be created with their own configuration.
     */
    public static final BeanHelper INSTANCE = new BeanHelper();

    /**
     * A special instance of {@code BeanUtilsBean} which is used for all
     * property set and copy operations. This instance was initialized with
     * {@code BeanIntrospector} objects which support fluent interfaces. This is
     * required for handling builder parameter objects correctly.
     */
    private static final BeanUtilsBean BEAN_UTILS_BEAN = initBeanUtilsBean();

    /** Stores a map with the registered bean factories. */
    private final Map<String, BeanFactory> beanFactories = Collections
            .synchronizedMap(new HashMap<String, BeanFactory>());

    /**
     * Stores the default bean factory, which is used if no other factory
     * is provided in a bean declaration.
     */
    private final BeanFactory defaultBeanFactory;

    /**
     * Creates a new instance of {@code BeanHelper} with the default instance of
     * {@link DefaultBeanFactory} as default {@link BeanFactory}.
     */
    public BeanHelper()
    {
        this(null);
    }

    /**
     * Creates a new instance of {@code BeanHelper} and sets the specified
     * default {@code BeanFactory}.
     *
     * @param defFactory the default {@code BeanFactory} (can be <b>null</b>,
     *        then a default instance is used)
     */
    public BeanHelper(final BeanFactory defFactory)
    {
        defaultBeanFactory =
                (defFactory != null) ? defFactory : DefaultBeanFactory.INSTANCE;
    }

    /**
     * Register a bean factory under a symbolic name. This factory object can
     * then be specified in bean declarations with the effect that this factory
     * will be used to obtain an instance for the corresponding bean
     * declaration.
     *
     * @param name the name of the factory
     * @param factory the factory to be registered
     */
    public void registerBeanFactory(final String name, final BeanFactory factory)
    {
        if (name == null)
        {
            throw new IllegalArgumentException(
                    "Name for bean factory must not be null!");
        }
        if (factory == null)
        {
            throw new IllegalArgumentException("Bean factory must not be null!");
        }

        beanFactories.put(name, factory);
    }

    /**
     * Deregisters the bean factory with the given name. After that this factory
     * cannot be used any longer.
     *
     * @param name the name of the factory to be deregistered
     * @return the factory that was registered under this name; <b>null</b> if
     * there was no such factory
     */
    public BeanFactory deregisterBeanFactory(final String name)
    {
        return beanFactories.remove(name);
    }

    /**
     * Returns a set with the names of all currently registered bean factories.
     *
     * @return a set with the names of the registered bean factories
     */
    public Set<String> registeredFactoryNames()
    {
        return beanFactories.keySet();
    }

    /**
     * Returns the default bean factory.
     *
     * @return the default bean factory
     */
    public BeanFactory getDefaultBeanFactory()
    {
        return defaultBeanFactory;
    }

    /**
     * Initializes the passed in bean. This method will obtain all the bean's
     * properties that are defined in the passed in bean declaration. These
     * properties will be set on the bean. If necessary, further beans will be
     * created recursively.
     *
     * @param bean the bean to be initialized
     * @param data the bean declaration
     * @throws ConfigurationRuntimeException if a property cannot be set
     */
    public void initBean(final Object bean, final BeanDeclaration data)
    {
        initBeanProperties(bean, data);

        final Map<String, Object> nestedBeans = data.getNestedBeanDeclarations();
        if (nestedBeans != null)
        {
            if (bean instanceof Collection)
            {
                // This is safe because the collection stores the values of the
                // nested beans.
                @SuppressWarnings("unchecked")
                final
                Collection<Object> coll = (Collection<Object>) bean;
                if (nestedBeans.size() == 1)
                {
                    final Map.Entry<String, Object> e = nestedBeans.entrySet().iterator().next();
                    final String propName = e.getKey();
                    final Class<?> defaultClass = getDefaultClass(bean, propName);
                    if (e.getValue() instanceof List)
                    {
                        // This is safe, provided that the bean declaration is implemented
                        // correctly.
                        @SuppressWarnings("unchecked")
                        final
                        List<BeanDeclaration> decls = (List<BeanDeclaration>) e.getValue();
                        for (final BeanDeclaration decl : decls)
                        {
                            coll.add(createBean(decl, defaultClass));
                        }
                    }
                    else
                    {
                        final BeanDeclaration decl = (BeanDeclaration) e.getValue();
                        coll.add(createBean(decl, defaultClass));
                    }
                }
            }
            else
            {
                for (final Map.Entry<String, Object> e : nestedBeans.entrySet())
                {
                    final String propName = e.getKey();
                    final Class<?> defaultClass = getDefaultClass(bean, propName);

                    final Object prop = e.getValue();

                    if (prop instanceof Collection)
                    {
                        final Collection<Object> beanCollection =
                                createPropertyCollection(propName, defaultClass);

                        for (final Object elemDef : (Collection<?>) prop)
                        {
                            beanCollection
                                    .add(createBean((BeanDeclaration) elemDef));
                        }

                        initProperty(bean, propName, beanCollection);
                    }
                    else
                    {
                        initProperty(bean, propName, createBean(
                            (BeanDeclaration) e.getValue(), defaultClass));
                    }
                }
            }
        }
    }

    /**
     * Initializes the beans properties.
     *
     * @param bean the bean to be initialized
     * @param data the bean declaration
     * @throws ConfigurationRuntimeException if a property cannot be set
     */
    public static void initBeanProperties(final Object bean, final BeanDeclaration data)
    {
        final Map<String, Object> properties = data.getBeanProperties();
        if (properties != null)
        {
            for (final Map.Entry<String, Object> e : properties.entrySet())
            {
                final String propName = e.getKey();
                initProperty(bean, propName, e.getValue());
            }
        }
    }

    /**
     * Creates a {@code DynaBean} instance which wraps the passed in bean.
     *
     * @param bean the bean to be wrapped (must not be <b>null</b>)
     * @return a {@code DynaBean} wrapping the passed in bean
     * @throws IllegalArgumentException if the bean is <b>null</b>
     * @since 2.0
     */
    public static DynaBean createWrapDynaBean(final Object bean)
    {
        if (bean == null)
        {
            throw new IllegalArgumentException("Bean must not be null!");
        }
        final WrapDynaClass dynaClass =
                WrapDynaClass.createDynaClass(bean.getClass(),
                        BEAN_UTILS_BEAN.getPropertyUtils());
        return new WrapDynaBean(bean, dynaClass);
    }

    /**
     * Copies matching properties from the source bean to the destination bean
     * using a specially configured {@code PropertyUtilsBean} instance. This
     * method ensures that enhanced introspection is enabled when doing the copy
     * operation.
     *
     * @param dest the destination bean
     * @param orig the source bean
     * @throws NoSuchMethodException exception thrown by
     *         {@code PropertyUtilsBean}
     * @throws InvocationTargetException exception thrown by
     *         {@code PropertyUtilsBean}
     * @throws IllegalAccessException exception thrown by
     *         {@code PropertyUtilsBean}
     * @since 2.0
     */
    public static void copyProperties(final Object dest, final Object orig)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException
    {
        BEAN_UTILS_BEAN.getPropertyUtils().copyProperties(dest, orig);
    }

    /**
     * Return the Class of the property if it can be determined.
     * @param bean The bean containing the property.
     * @param propName The name of the property.
     * @return The class associated with the property or null.
     */
    private static Class<?> getDefaultClass(final Object bean, final String propName)
    {
        try
        {
            final PropertyDescriptor desc =
                    BEAN_UTILS_BEAN.getPropertyUtils().getPropertyDescriptor(
                            bean, propName);
            if (desc == null)
            {
                return null;
            }
            return desc.getPropertyType();
        }
        catch (final Exception ex)
        {
            return null;
        }
    }

    /**
     * Sets a property on the given bean using Common Beanutils.
     *
     * @param bean the bean
     * @param propName the name of the property
     * @param value the property's value
     * @throws ConfigurationRuntimeException if the property is not writeable or
     * an error occurred
     */
    private static void initProperty(final Object bean, final String propName, final Object value)
    {
        if (!isPropertyWriteable(bean, propName))
        {
            throw new ConfigurationRuntimeException("Property " + propName
                    + " cannot be set on " + bean.getClass().getName());
        }

        try
        {
            BEAN_UTILS_BEAN.setProperty(bean, propName, value);
        }
        catch (final IllegalAccessException iaex)
        {
            throw new ConfigurationRuntimeException(iaex);
        }
        catch (final InvocationTargetException itex)
        {
            throw new ConfigurationRuntimeException(itex);
        }
    }

    /**
     * Creates a concrete collection instance to populate a property of type
     * collection. This method tries to guess an appropriate collection type.
     * Mostly the type of the property will be one of the collection interfaces
     * rather than a concrete class; so we have to create a concrete equivalent.
     *
     * @param propName the name of the collection property
     * @param propertyClass the type of the property
     * @return the newly created collection
     */
    private static Collection<Object> createPropertyCollection(final String propName,
            final Class<?> propertyClass)
    {
        Collection<Object> beanCollection;

        if (List.class.isAssignableFrom(propertyClass))
        {
            beanCollection = new ArrayList<>();
        }
        else if (Set.class.isAssignableFrom(propertyClass))
        {
            beanCollection = new TreeSet<>();
        }
        else
        {
            throw new UnsupportedOperationException(
                    "Unable to handle collection of type : "
                            + propertyClass.getName() + " for property "
                            + propName);
        }
        return beanCollection;
    }

    /**
     * Set a property on the bean only if the property exists
     *
     * @param bean the bean
     * @param propName the name of the property
     * @param value the property's value
     * @throws ConfigurationRuntimeException if the property is not writeable or
     *         an error occurred
     */
    public static void setProperty(final Object bean, final String propName, final Object value)
    {
        if (isPropertyWriteable(bean, propName))
        {
            initProperty(bean, propName, value);
        }
    }

    /**
     * The main method for creating and initializing beans from a configuration.
     * This method will return an initialized instance of the bean class
     * specified in the passed in bean declaration. If this declaration does not
     * contain the class of the bean, the passed in default class will be used.
     * From the bean declaration the factory to be used for creating the bean is
     * queried. The declaration may here return <b>null</b>, then a default
     * factory is used. This factory is then invoked to perform the create
     * operation.
     *
     * @param data the bean declaration
     * @param defaultClass the default class to use
     * @param param an additional parameter that will be passed to the bean
     * factory; some factories may support parameters and behave different
     * depending on the value passed in here
     * @return the new bean
     * @throws ConfigurationRuntimeException if an error occurs
     */
    public Object createBean(final BeanDeclaration data, final Class<?> defaultClass,
            final Object param)
    {
        if (data == null)
        {
            throw new IllegalArgumentException(
                    "Bean declaration must not be null!");
        }

        final BeanFactory factory = fetchBeanFactory(data);
        final BeanCreationContext bcc =
                createBeanCreationContext(data, defaultClass, param, factory);
        try
        {
            return factory.createBean(bcc);
        }
        catch (final Exception ex)
        {
            throw new ConfigurationRuntimeException(ex);
        }
    }

    /**
     * Returns a bean instance for the specified declaration. This method is a
     * short cut for {@code createBean(data, null, null);}.
     *
     * @param data the bean declaration
     * @param defaultClass the class to be used when in the declaration no class
     * is specified
     * @return the new bean
     * @throws ConfigurationRuntimeException if an error occurs
     */
    public Object createBean(final BeanDeclaration data, final Class<?> defaultClass)
    {
        return createBean(data, defaultClass, null);
    }

    /**
     * Returns a bean instance for the specified declaration. This method is a
     * short cut for {@code createBean(data, null);}.
     *
     * @param data the bean declaration
     * @return the new bean
     * @throws ConfigurationRuntimeException if an error occurs
     */
    public Object createBean(final BeanDeclaration data)
    {
        return createBean(data, null);
    }

    /**
     * Returns a {@code java.lang.Class} object for the specified name.
     * Because class loading can be tricky in some environments the code for
     * retrieving a class by its name was extracted into this helper method. So
     * if changes are necessary, they can be made at a single place.
     *
     * @param name the name of the class to be loaded
     * @return the class object for the specified name
     * @throws ClassNotFoundException if the class cannot be loaded
     */
    static Class<?> loadClass(final String name) throws ClassNotFoundException
    {
        return ClassUtils.getClass(name);
    }

    /**
     * Checks whether the specified property of the given bean instance supports
     * write access.
     *
     * @param bean the bean instance
     * @param propName the name of the property in question
     * @return <b>true</b> if this property can be written, <b>false</b>
     *         otherwise
     */
    private static boolean isPropertyWriteable(final Object bean, final String propName)
    {
        return BEAN_UTILS_BEAN.getPropertyUtils().isWriteable(bean, propName);
    }

    /**
     * Determines the class of the bean to be created. If the bean declaration
     * contains a class name, this class is used. Otherwise it is checked
     * whether a default class is provided. If this is not the case, the
     * factory's default class is used. If this class is undefined, too, an
     * exception is thrown.
     *
     * @param data the bean declaration
     * @param defaultClass the default class
     * @param factory the bean factory to use
     * @return the class of the bean to be created
     * @throws ConfigurationRuntimeException if the class cannot be determined
     */
    private static Class<?> fetchBeanClass(final BeanDeclaration data,
            final Class<?> defaultClass, final BeanFactory factory)
    {
        final String clsName = data.getBeanClassName();
        if (clsName != null)
        {
            try
            {
                return loadClass(clsName);
            }
            catch (final ClassNotFoundException cex)
            {
                throw new ConfigurationRuntimeException(cex);
            }
        }

        if (defaultClass != null)
        {
            return defaultClass;
        }

        final Class<?> clazz = factory.getDefaultBeanClass();
        if (clazz == null)
        {
            throw new ConfigurationRuntimeException(
                    "Bean class is not specified!");
        }
        return clazz;
    }

    /**
     * Obtains the bean factory to use for creating the specified bean. This
     * method will check whether a factory is specified in the bean declaration.
     * If this is not the case, the default bean factory will be used.
     *
     * @param data the bean declaration
     * @return the bean factory to use
     * @throws ConfigurationRuntimeException if the factory cannot be determined
     */
    private BeanFactory fetchBeanFactory(final BeanDeclaration data)
    {
        final String factoryName = data.getBeanFactoryName();
        if (factoryName != null)
        {
            final BeanFactory factory = beanFactories.get(factoryName);
            if (factory == null)
            {
                throw new ConfigurationRuntimeException(
                        "Unknown bean factory: " + factoryName);
            }
            return factory;
        }
        return getDefaultBeanFactory();
    }

    /**
     * Creates a {@code BeanCreationContext} object for the creation of the
     * specified bean.
     *
     * @param data the bean declaration
     * @param defaultClass the default class to use
     * @param param an additional parameter that will be passed to the bean
     *        factory; some factories may support parameters and behave
     *        different depending on the value passed in here
     * @param factory the current bean factory
     * @return the {@code BeanCreationContext}
     * @throws ConfigurationRuntimeException if the bean class cannot be
     *         determined
     */
    private BeanCreationContext createBeanCreationContext(
            final BeanDeclaration data, final Class<?> defaultClass,
            final Object param, final BeanFactory factory)
    {
        final Class<?> beanClass = fetchBeanClass(data, defaultClass, factory);
        return new BeanCreationContextImpl(this, beanClass, data, param);
    }

    /**
     * Initializes the shared {@code BeanUtilsBean} instance. This method sets
     * up custom bean introspection in a way that fluent parameter interfaces
     * are supported.
     *
     * @return the {@code BeanUtilsBean} instance to be used for all property
     *         set operations
     */
    private static BeanUtilsBean initBeanUtilsBean()
    {
        final PropertyUtilsBean propUtilsBean = new PropertyUtilsBean();
        propUtilsBean.addBeanIntrospector(new FluentPropertyBeanIntrospector());
        return new BeanUtilsBean(new ConvertUtilsBean(), propUtilsBean);
    }

    /**
     * An implementation of the {@code BeanCreationContext} interface used by
     * {@code BeanHelper} to communicate with a {@code BeanFactory}. This class
     * contains all information required for the creation of a bean. The methods
     * for creating and initializing bean instances are implemented by calling
     * back to the provided {@code BeanHelper} instance (which is the instance
     * that created this object).
     */
    private static final class BeanCreationContextImpl implements BeanCreationContext
    {
        /** The association BeanHelper instance. */
        private final BeanHelper beanHelper;

        /** The class of the bean to be created. */
        private final Class<?> beanClass;

        /** The underlying bean declaration. */
        private final BeanDeclaration data;

        /** The parameter for the bean factory. */
        private final Object param;

        private BeanCreationContextImpl(final BeanHelper helper, final Class<?> beanClass,
                final BeanDeclaration data, final Object param)
        {
            beanHelper = helper;
            this.beanClass = beanClass;
            this.param = param;
            this.data = data;
        }

        @Override
        public void initBean(final Object bean, final BeanDeclaration data)
        {
            beanHelper.initBean(bean, data);
        }

        @Override
        public Object getParameter()
        {
            return param;
        }

        @Override
        public BeanDeclaration getBeanDeclaration()
        {
            return data;
        }

        @Override
        public Class<?> getBeanClass()
        {
            return beanClass;
        }

        @Override
        public Object createBean(final BeanDeclaration data)
        {
            return beanHelper.createBean(data);
        }
    }
}
