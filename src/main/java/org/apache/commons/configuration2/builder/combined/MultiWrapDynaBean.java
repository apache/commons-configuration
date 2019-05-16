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
package org.apache.commons.configuration2.builder.combined;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.configuration2.beanutils.BeanHelper;

/**
 * <p>
 * An implementation of the {@code DynaBean} interfaces which wraps multiple
 * other beans.
 * </p>
 * <p>
 * An instance of this class is constructed with a collection of beans to be
 * wrapped. When reading or writing a property the wrapped bean which defines
 * this property is determined, and the operation is executed on this bean.
 * </p>
 * <p>
 * The wrapped beans should have disjunct properties. Otherwise, it is undefined
 * which bean property is read or written.
 * </p>
 *
 * @since 2.0
 */
class MultiWrapDynaBean implements DynaBean
{
    /** Stores the class of this DynaBean. */
    private final DynaClass dynaClass;

    /** A map which associates property names with their defining beans. */
    private final Map<String, DynaBean> propsToBeans;

    /**
     * Creates a new instance of {@code MultiWrapDynaBean} and initializes it
     * with the given collections of beans to be wrapped.
     *
     * @param beans the wrapped beans
     */
    public MultiWrapDynaBean(final Collection<?> beans)
    {
        propsToBeans = new HashMap<>();
        final Collection<DynaClass> beanClasses =
                new ArrayList<>(beans.size());

        for (final Object bean : beans)
        {
            final DynaBean dynaBean = createDynaBean(bean);
            final DynaClass beanClass = dynaBean.getDynaClass();
            for (final DynaProperty prop : beanClass.getDynaProperties())
            {
                // ensure an order of properties
                if (!propsToBeans.containsKey(prop.getName()))
                {
                    propsToBeans.put(prop.getName(), dynaBean);
                }
            }
            beanClasses.add(beanClass);
        }

        dynaClass = new MultiWrapDynaClass(beanClasses);
    }

    /**
     * {@inheritDoc} This operation is not supported by the {@code WrapDynaBean}
     * objects used internally by this class. Therefore, just an exception is
     * thrown.
     */
    @Override
    public boolean contains(final String name, final String key)
    {
        throw new UnsupportedOperationException(
                "contains() operation not supported!");
    }

    @Override
    public Object get(final String name)
    {
        return fetchBean(name).get(name);
    }

    @Override
    public Object get(final String name, final int index)
    {
        return fetchBean(name).get(name, index);
    }

    @Override
    public Object get(final String name, final String key)
    {
        return fetchBean(name).get(name, key);
    }

    /**
     * {@inheritDoc} This implementation returns an instance of
     * {@code MultiWrapDynaClass}.
     */
    @Override
    public DynaClass getDynaClass()
    {
        return dynaClass;
    }

    /**
     * {@inheritDoc} This operation is not supported by the {@code WrapDynaBean}
     * objects used internally by this class. Therefore, just an exception is
     * thrown.
     */
    @Override
    public void remove(final String name, final String key)
    {
        throw new UnsupportedOperationException(
                "remove() operation not supported!");
    }

    @Override
    public void set(final String name, final Object value)
    {
        fetchBean(name).set(name, value);
    }

    @Override
    public void set(final String name, final int index, final Object value)
    {
        fetchBean(name).set(name, index, value);
    }

    @Override
    public void set(final String name, final String key, final Object value)
    {
        fetchBean(name).set(name, key, value);
    }

    /**
     * Returns the bean instance to which the given property belongs. If no such
     * bean is found, an arbitrary bean is returned. (This causes the operation
     * on this bean to fail with a meaningful error message.)
     *
     * @param property the property name
     * @return the bean defining this property
     */
    private DynaBean fetchBean(final String property)
    {
        DynaBean dynaBean = propsToBeans.get(property);
        if (dynaBean == null)
        {
            dynaBean = propsToBeans.values().iterator().next();
        }
        return dynaBean;
    }

    /**
     * Creates a {@code DynaBean} object for the given bean.
     *
     * @param bean the bean
     * @return the {@code DynaBean} for this bean
     */
    private static DynaBean createDynaBean(final Object bean)
    {
        if (bean instanceof DynaBean)
        {
            return (DynaBean) bean;
        }
        return BeanHelper.createWrapDynaBean(bean);
    }
}
