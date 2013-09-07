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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.configuration.ConfigurationRuntimeException;
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
 * @version $Id$
 */
public final class BeanHelper
{
    /** Stores a map with the registered bean factories. */
    private final Map<String, BeanFactory> BEAN_FACTORIES = Collections
            .synchronizedMap(new HashMap<String, BeanFactory>());

    /** A format string for generating error messages for constructor matching. */
    private static final String FMT_CTOR_ERROR =
            "%s! Bean class = %s, constructor arguments = %s";

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
    public BeanHelper(BeanFactory defFactory)
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
    public void registerBeanFactory(String name, BeanFactory factory)
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

        BEAN_FACTORIES.put(name, factory);
    }

    /**
     * Deregisters the bean factory with the given name. After that this factory
     * cannot be used any longer.
     *
     * @param name the name of the factory to be deregistered
     * @return the factory that was registered under this name; <b>null</b> if
     * there was no such factory
     */
    public BeanFactory deregisterBeanFactory(String name)
    {
        return BEAN_FACTORIES.remove(name);
    }

    /**
     * Returns a set with the names of all currently registered bean factories.
     *
     * @return a set with the names of the registered bean factories
     */
    public Set<String> registeredFactoryNames()
    {
        return BEAN_FACTORIES.keySet();
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
    public void initBean(Object bean, BeanDeclaration data)
            throws ConfigurationRuntimeException
    {
        initBeanProperties(bean, data);

        Map<String, Object> nestedBeans = data.getNestedBeanDeclarations();
        if (nestedBeans != null)
        {
            if (bean instanceof Collection)
            {
                // This is safe because the collection stores the values of the
                // nested beans.
                @SuppressWarnings("unchecked")
                Collection<Object> coll = (Collection<Object>) bean;
                if (nestedBeans.size() == 1)
                {
                    Map.Entry<String, Object> e = nestedBeans.entrySet().iterator().next();
                    String propName = e.getKey();
                    Class<?> defaultClass = getDefaultClass(bean, propName);
                    if (e.getValue() instanceof List)
                    {
                        // This is safe, provided that the bean declaration is implemented
                        // correctly.
                        @SuppressWarnings("unchecked")
                        List<BeanDeclaration> decls = (List<BeanDeclaration>) e.getValue();
                        for (BeanDeclaration decl : decls)
                        {
                            coll.add(createBean(decl, defaultClass));
                        }
                    }
                    else
                    {
                        BeanDeclaration decl = (BeanDeclaration) e.getValue();
                        coll.add(createBean(decl, defaultClass));
                    }
                }
            }
            else
            {
                for (Map.Entry<String, Object> e : nestedBeans.entrySet())
                {
                    String propName = e.getKey();
                    Class<?> defaultClass = getDefaultClass(bean, propName);

                    Object prop = e.getValue();

                    if (prop instanceof Collection)
                    {
                        Collection<Object> beanCollection =
                                createPropertyCollection(propName, defaultClass);

                        for (Object elemDef : (Collection<?>) prop)
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
    public static void initBeanProperties(Object bean, BeanDeclaration data)
            throws ConfigurationRuntimeException
    {
        Map<String, Object> properties = data.getBeanProperties();
        if (properties != null)
        {
            for (Map.Entry<String, Object> e : properties.entrySet())
            {
                String propName = e.getKey();
                initProperty(bean, propName, e.getValue());
            }
        }
    }

    /**
     * Return the Class of the property if it can be determined.
     * @param bean The bean containing the property.
     * @param propName The name of the property.
     * @return The class associated with the property or null.
     */
    private static Class<?> getDefaultClass(Object bean, String propName)
    {
        try
        {
            PropertyDescriptor desc = PropertyUtils.getPropertyDescriptor(bean, propName);
            if (desc == null)
            {
                return null;
            }
            return desc.getPropertyType();
        }
        catch (Exception ex)
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
    private static void initProperty(Object bean, String propName, Object value)
            throws ConfigurationRuntimeException
    {
        if (!PropertyUtils.isWriteable(bean, propName))
        {
            throw new ConfigurationRuntimeException("Property " + propName
                    + " cannot be set on " + bean.getClass().getName());
        }

        try
        {
            BeanUtils.setProperty(bean, propName, value);
        }
        catch (IllegalAccessException iaex)
        {
            throw new ConfigurationRuntimeException(iaex);
        }
        catch (InvocationTargetException itex)
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
    private static Collection<Object> createPropertyCollection(String propName,
            Class<?> propertyClass)
    {
        Collection<Object> beanCollection = null;

        if (List.class.isAssignableFrom(propertyClass))
        {
            beanCollection = new ArrayList<Object>();
        }
        else if (Set.class.isAssignableFrom(propertyClass))
        {
            beanCollection = new TreeSet<Object>();
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
    public static void setProperty(Object bean, String propName, Object value)
    {
        if (PropertyUtils.isWriteable(bean, propName))
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
    public Object createBean(BeanDeclaration data, Class<?> defaultClass,
            Object param) throws ConfigurationRuntimeException
    {
        if (data == null)
        {
            throw new IllegalArgumentException(
                    "Bean declaration must not be null!");
        }

        BeanFactory factory = fetchBeanFactory(data);
        BeanCreationContext bcc =
                createBeanCreationContext(data, defaultClass, param, factory);
        try
        {
            return factory.createBean(bcc);
        }
        catch (Exception ex)
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
    public Object createBean(BeanDeclaration data, Class<?> defaultClass)
            throws ConfigurationRuntimeException
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
    public Object createBean(BeanDeclaration data)
            throws ConfigurationRuntimeException
    {
        return createBean(data, null);
    }

    /**
     * Evaluates constructor arguments in the specified {@code BeanDeclaration}
     * and tries to find a unique matching constructor. If this is not possible,
     * an exception is thrown. Note: This method is intended to be used by
     * concrete {@link BeanFactory} implementations and not by client code.
     *
     * @param beanClass the class of the bean to be created
     * @param data the current {@code BeanDeclaration}
     * @return the single matching constructor
     * @throws ConfigurationRuntimeException if no single matching constructor
     *         can be found
     * @throws NullPointerException if the bean class or bean declaration are
     *         <b>null</b>
     */
    public static <T> Constructor<T> findMatchingConstructor(
            Class<T> beanClass, BeanDeclaration data)
    {
        List<Constructor<T>> matchingConstructors =
                findMatchingConstructors(beanClass, data);
        checkSingleMatchingConstructor(beanClass, data, matchingConstructors);
        return matchingConstructors.get(0);
    }

    /**
     * Returns a {@code java.lang.Class} object for the specified name.
     * Because class loading can be tricky in some environments the code for
     * retrieving a class by its name was extracted into this helper method. So
     * if changes are necessary, they can be made at a single place.
     *
     * @param name the name of the class to be loaded
     * @param callingClass the calling class
     * @return the class object for the specified name
     * @throws ClassNotFoundException if the class cannot be loaded
     */
    static Class<?> loadClass(String name, Class<?> callingClass)
            throws ClassNotFoundException
    {
        return ClassUtils.getClass(name);
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
    private static Class<?> fetchBeanClass(BeanDeclaration data,
            Class<?> defaultClass, BeanFactory factory)
            throws ConfigurationRuntimeException
    {
        String clsName = data.getBeanClassName();
        if (clsName != null)
        {
            try
            {
                return loadClass(clsName, factory.getClass());
            }
            catch (ClassNotFoundException cex)
            {
                throw new ConfigurationRuntimeException(cex);
            }
        }

        if (defaultClass != null)
        {
            return defaultClass;
        }

        Class<?> clazz = factory.getDefaultBeanClass();
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
    private BeanFactory fetchBeanFactory(BeanDeclaration data)
            throws ConfigurationRuntimeException
    {
        String factoryName = data.getBeanFactoryName();
        if (factoryName != null)
        {
            BeanFactory factory = BEAN_FACTORIES.get(factoryName);
            if (factory == null)
            {
                throw new ConfigurationRuntimeException(
                        "Unknown bean factory: " + factoryName);
            }
            else
            {
                return factory;
            }
        }
        else
        {
            return getDefaultBeanFactory();
        }
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
            final BeanDeclaration data, Class<?> defaultClass,
            final Object param, BeanFactory factory)
    {
        final Class<?> beanClass = fetchBeanClass(data, defaultClass, factory);
        return new BeanCreationContextImpl(this, beanClass, data, param);
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
            Class<T> beanClass, BeanDeclaration data)
    {
        List<Constructor<T>> result = new LinkedList<Constructor<T>>();
        Collection<ConstructorArg> args = getConstructorArgs(data);
        for (Constructor<?> ctor : beanClass.getConstructors())
        {
            if (matchesConstructor(ctor, args))
            {
                // cast should be okay according to the JavaDocs of
                // getConstructors()
                @SuppressWarnings("unchecked")
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
    private static boolean matchesConstructor(Constructor<?> ctor,
            Collection<ConstructorArg> args)
    {
        Class<?>[] types = ctor.getParameterTypes();
        if (types.length != args.size())
        {
            return false;
        }

        int idx = 0;
        for (ConstructorArg arg : args)
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
            BeanDeclaration data)
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
    private static <T> void checkSingleMatchingConstructor(Class<T> beanClass,
            BeanDeclaration data, List<Constructor<T>> matchingConstructors)
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
            Class<?> beanClass, BeanDeclaration data, String msg)
    {
        return new ConfigurationRuntimeException(String.format(FMT_CTOR_ERROR,
                msg, beanClass.getName(), getConstructorArgs(data).toString()));
    }

    /**
     * An implementation of the {@code BeanCreationContext} interface used by
     * {@code BeanHelper} to communicate with a {@code BeanFactory}. This class
     * contains all information required for the creation of a bean. The methods
     * for creating and initializing bean instances are implemented by calling
     * back to the provided {@code BeanHelper} instance (which is the instance
     * that created this object).
     */
    private static class BeanCreationContextImpl implements BeanCreationContext
    {
        /** The association BeanHelper instance. */
        private final BeanHelper beanHelper;

        /** The class of the bean to be created. */
        private final Class<?> beanClass;

        /** The underlying bean declaration. */
        private final BeanDeclaration data;

        /** The parameter for the bean factory. */
        private final Object param;

        private BeanCreationContextImpl(BeanHelper helper, Class<?> beanClass,
                BeanDeclaration data, Object param)
        {
            beanHelper = helper;
            this.beanClass = beanClass;
            this.param = param;
            this.data = data;
        }

        public void initBean(Object bean, BeanDeclaration data)
        {
            beanHelper.initBean(bean, data);
        }

        public Object getParameter()
        {
            return param;
        }

        public BeanDeclaration getBeanDeclaration()
        {
            return data;
        }

        public Class<?> getBeanClass()
        {
            return beanClass;
        }

        public Object createBean(BeanDeclaration data)
        {
            return beanHelper.createBean(data);
        }
    }
}
