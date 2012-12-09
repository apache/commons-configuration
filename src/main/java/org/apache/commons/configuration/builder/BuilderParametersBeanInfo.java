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
package org.apache.commons.configuration.builder;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * A base class for providing additional {@code BeanInfo} information for
 * parameter classes for {@link BasicConfigurationBuilder}.
 * </p>
 * <p>
 * Because parameter classes typically use a fluent API their properties are not
 * detected by standard introspection mechanisms. To make them available to
 * <em>Commons BeanUtils</em> (which is used to process bean declarations), the
 * properties have to be explicitly listed in custom property descriptors. This
 * base class provides functionality to do this.
 * </p>
 * <p>
 * An instance of this class is passed the class of the associated bean at
 * construction time. It then obtains all properties of the bean class using
 * standard introspection. In a second step, all methods starting with the
 * prefix {@code set} are searched. Such methods are typically used to set
 * properties in fluent API style. If a method is found whose name is not
 * contained in the list of properties, it is added as an additional property.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public abstract class BuilderParametersBeanInfo extends SimpleBeanInfo
{
    /** Constant for the prefix for set methods. */
    private static final String PREFIX_SET_METHOD = "set";

    /** The logger. */
    private static final Log LOG = LogFactory
            .getLog(BuilderParametersBeanInfo.class);

    /** The property descriptors supported by the associated bean. */
    private final PropertyDescriptor[] propertyDescriptors;

    /** An array with additional BeanInfo objects. */
    private final BeanInfo[] additionalBeanInfo;

    /**
     * Creates a new instance of {@code BuilderParametersBeanInfo} and
     * initializes it from the specified bean class.
     *
     * @param beanClass the associated bean class
     */
    protected BuilderParametersBeanInfo(Class<?> beanClass)
    {
        LOG.info("Initializing BeanInfo for " + beanClass);
        BeanInfo stdInfo = obtainStandardBeanInfo(beanClass);
        if (stdInfo != null)
        {
            additionalBeanInfo = new BeanInfo[1];
            additionalBeanInfo[0] = stdInfo;
        }
        else
        {
            additionalBeanInfo = null;
        }
        propertyDescriptors = extractPropertyDescriptors(beanClass, stdInfo);
    }

    @Override
    public BeanInfo[] getAdditionalBeanInfo()
    {
        return additionalBeanInfo;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return propertyDescriptors;
    }

    /**
     * Performs standard introspection on the specified bean class.
     *
     * @param beanClass the bean class in question
     * @return the {@code BeanInfo} for this bean class (<b>null</b> if an error
     *         occurs)
     */
    private static BeanInfo obtainStandardBeanInfo(Class<?> beanClass)
    {
        try
        {
            return Introspector.getBeanInfo(beanClass,
                    Introspector.IGNORE_IMMEDIATE_BEANINFO);
        }
        catch (IntrospectionException e)
        {
            return null;
        }
    }

    /**
     * Determines all property descriptors for fluent API properties which are
     * not detected by standard bean introspection.
     *
     * @param beanClass the bean class to be processed
     * @param stdBeanInfo the {@code BeanInfo} obtained by standard bean
     *        introspection
     * @return an array with property descriptors for the properties discovered
     */
    private static PropertyDescriptor[] extractPropertyDescriptors(
            Class<?> beanClass, BeanInfo stdBeanInfo)
    {
        Map<String, PropertyDescriptor> propertyDescs =
                fetchPropertyDescriptors(stdBeanInfo);
        Collection<PropertyDescriptor> descriptors =
                new LinkedList<PropertyDescriptor>();

        for (Method m : beanClass.getMethods())
        {
            if (m.getName().startsWith(PREFIX_SET_METHOD))
            {
                String propertyName = propertyName(m);
                PropertyDescriptor pd = propertyDescs.get(propertyName);
                try
                {
                    if (pd == null)
                    {
                        descriptors.add(createFluentPropertyDescritor(m,
                                propertyName));
                    }
                    else if (pd.getWriteMethod() == null)
                    {
                        pd.setWriteMethod(m);
                    }
                }
                catch (IntrospectionException e)
                {
                    LOG.warn("Error when creating PropertyDescriptor for " + m
                            + "! Ignoring this property.", e);
                }
            }
        }

        return descriptors.toArray(new PropertyDescriptor[descriptors.size()]);
    }

    /**
     * Obtains a map of all properties from the given {@code BeanInfo} object.
     *
     * @param info the {@code BeanInfo} (may be <b>null</b>)
     * @return a map allowing direct access to property descriptors by name
     */
    private static Map<String, PropertyDescriptor> fetchPropertyDescriptors(
            BeanInfo info)
    {
        Map<String, PropertyDescriptor> propDescs =
                new HashMap<String, PropertyDescriptor>();
        if (info != null)
        {
            PropertyDescriptor[] descs = info.getPropertyDescriptors();
            if (descs != null)
            {
                for (PropertyDescriptor pd : descs)
                {
                    propDescs.put(pd.getName(), pd);
                }
            }
        }

        return propDescs;
    }

    /**
     * Creates a property descriptor for a fluent API property.
     *
     * @param m the set method for the fluent API property
     * @param propertyName the name of the corresponding property
     * @return the descriptor
     * @throws IntrospectionException if an error occurs
     */
    private static PropertyDescriptor createFluentPropertyDescritor(Method m,
            String propertyName) throws IntrospectionException
    {
        return new PropertyDescriptor(propertyName(m), null, m);
    }

    /**
     * Derives the name of a property from the given set method.
     *
     * @param m the method
     * @return the corresponding property name
     */
    private static String propertyName(Method m)
    {
        String methodName = m.getName().substring(PREFIX_SET_METHOD.length());
        return (methodName.length() > 1) ? Character.toLowerCase(methodName
                .charAt(0)) + methodName.substring(1) : methodName
                .toLowerCase(Locale.ENGLISH);
    }
}
