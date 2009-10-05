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
package org.apache.commons.configuration2.base;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.ClassUtils;

/**
 * <p>
 * A class for managing <em>capabilities</em>.
 * </p>
 * <p>
 * This class is intended to support the implementation of the {@code
 * getCapability()} method defined by the {@link ConfigurationSource} interface.
 * It maintains the capabilities provided by a concrete configuration source
 * implementation.
 * </p>
 * <p>
 * An instance is initialized with a reference to an owner and a collection of
 * {@link Capability} objects. The passed in {@link Capability} objects are
 * stored directly. From the owner object a list of all implemented interfaces
 * is obtained, and corresponding {@link Capability} objects are created for
 * them. The {@code getCapability()} method queries the internal list of
 * {@link Capability} objects created this way and returns the implementation
 * objects if a matching class is found. This is exactly what is needed for the
 * implementation of the {@link ConfigurationSource#getCapability(Class)}
 * method.
 * </p>
 * <p>
 * Implementation note: This class is immutable.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class Capabilities
{
    /** The capabilities managed by this instance. */
    private final Collection<Capability> capabilities;

    /**
     * Creates a new instance of {@code Capabilities} and initializes it with an
     * owner and a collection of {@code Capability} objects.
     *
     * @param owner the owner object (can be <b>null</b>)
     * @param caps the collection with {@code Capability} objects (can be
     *        <b>null</b>)
     * @throws IllegalArgumentException if one of the {@code Capability} objects
     *         in the collection is <b>null</b>
     */
    public Capabilities(Object owner, Collection<? extends Capability> caps)
    {
        if (caps == null)
        {
            capabilities = new ArrayList<Capability>();
        }
        else
        {
            capabilities = new ArrayList<Capability>(caps);
            for (Capability c : capabilities)
            {
                if (c == null)
                {
                    throw new IllegalArgumentException(
                            "Capability must not be null!");
                }
            }
        }

        if (owner != null)
        {
            extractOwnerCapabilities(capabilities, owner);
        }
    }

    /**
     * Returns the capability implementation object for the specified capability
     * (interface) class. This method queries all {@link Capability} objects
     * stored internally whether they match the specified class. If a match is
     * found, the corresponding implementation object is returned.
     * {@link Capability} objects directly passed to the constructor are queried
     * first (in the order they are provided), then the capabilities obtained
     * from the interfaces of the owner object are searched. Therefore it is
     * possible to override interfaces implemented by the owner object by
     * providing corresponding {@link Capability} instances.
     *
     * @param <T> the type of the capability
     * @param capabilityClass the capability class
     * @return the object implementing this capability or <b>null</b> if this
     *         capability is not supported
     */
    public <T> T getCapability(Class<T> capabilityClass)
    {
        for (Capability cap : capabilities)
        {
            if (cap.matches(capabilityClass))
            {
                return capabilityClass.cast(cap.getCapabilityObject());
            }
        }

        return null;
    }

    /**
     * Returns a string representation of this object. This string contains the
     * string representations of all {@code Capability} objects managed by this
     * instance.
     *
     * @return a string for this object
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("Capabilities [ capabilities = ");
        buf.append(capabilities);
        buf.append(" ]");
        return buf.toString();
    }

    /**
     * Creates {@code Capability} objects for the interfaces implemented by the
     * owner object. This method is called by the constructor if an owner object
     * is provided. Note: It is safe to use raw types here because it is ensured
     * that the owner object actually can be casted to the interfaces it
     * implements.
     *
     * @param caps the list with the capabilities
     * @param owner the owner object
     */
    @SuppressWarnings("unchecked")
    private static void extractOwnerCapabilities(Collection<Capability> caps,
            Object owner)
    {
        for (Object capCls : ClassUtils.getAllInterfaces(owner.getClass()))
        {
            caps.add(new Capability((Class) capCls, owner));
        }
    }
}
