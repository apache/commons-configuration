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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;

/**
 * <p>
 * An implementation of {@code DynaClass} which combines the properties of
 * multiple other {@code DynaClass} instances.
 * </p>
 *
 * @since 2.0
 */
class MultiWrapDynaClass implements DynaClass
{
    /** An empty array for converting the properties collection to an array. */
    private static final DynaProperty[] EMPTY_PROPS = new DynaProperty[0];

    /** A collection with all properties of this class. */
    private final Collection<DynaProperty> properties;

    /** A map for accessing properties by name. */
    private final Map<String, DynaProperty> namedProperties;

    /**
     * Creates a new instance of {@code MultiWrapDynaClass} and initializes it
     * with the collection of classes to be wrapped.
     *
     * @param wrappedCls the collection with wrapped classes
     */
    public MultiWrapDynaClass(final Collection<? extends DynaClass> wrappedCls)
    {
        properties = new LinkedList<>();
        namedProperties = new HashMap<>();
        initProperties(wrappedCls);
    }

    /**
     * {@inheritDoc} The name of this class is not relevant.
     */
    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public DynaProperty getDynaProperty(final String name)
    {
        return namedProperties.get(name);
    }

    @Override
    public DynaProperty[] getDynaProperties()
    {
        return properties.toArray(EMPTY_PROPS);
    }

    /**
     * {@inheritDoc} This implementation always throws an exception because it
     * is not possible to instantiate a bean of multiple classes.
     */
    @Override
    public DynaBean newInstance() throws IllegalAccessException,
            InstantiationException
    {
        throw new UnsupportedOperationException(
                "Cannot create an instance of MultiWrapDynaBean!");
    }

    /**
     * Initializes the members related to the properties of the wrapped classes.
     *
     * @param wrappedCls the collection with the wrapped classes
     */
    private void initProperties(final Collection<? extends DynaClass> wrappedCls)
    {
        for (final DynaClass cls : wrappedCls)
        {
            final DynaProperty[] props = cls.getDynaProperties();
            for (final DynaProperty p : props)
            {
                properties.add(p);
                namedProperties.put(p.getName(), p);
            }
        }
    }
}
