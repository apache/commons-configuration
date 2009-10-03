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

/**
 * <p>
 * A class that stores a capability object.
 * </p>
 * <p>
 * A <em>capability</em> is a kind of extension of the
 * {@link ConfigurationSource} interface that consists of a (interface) class
 * and a corresponding implementation object. A client can request a capability
 * of a specific type and gets the implementation object if it is available.
 * </p>
 * <p>
 * This class stores the (interface) class and the implementation object. It
 * provides a method to check whether it matches the request for a specific
 * capability interface. An instance is fully initialized at construction time
 * and immutable.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class Capability
{
    /** The capability class. */
    private final Class<?> capabilityClass;

    /** The capability implementation object. */
    private final Object capabilityObject;

    /**
     * Creates a new instance of {@code Capability} and initializes it.
     *
     * @param cls the class of this capability (must not be <b>null</b>)
     * @param obj the implementation object (must not be <b>null</b>)
     * @throws IllegalArgumentException if a required parameter is missing
     */
    public <T> Capability(Class<T> cls, T obj)
    {
        if (cls == null)
        {
            throw new IllegalArgumentException(
                    "Capability class must not be null!");
        }
        if (obj == null)
        {
            throw new IllegalArgumentException(
                    "Capability object must not be null!");
        }

        capabilityClass = cls;
        capabilityObject = obj;
    }

    /**
     * Returns the class of this capability.
     *
     * @return the capability class
     */
    public Class<?> getCapabilityClass()
    {
        return capabilityClass;
    }

    /**
     * Returns the object implementing this capability.
     *
     * @return the implementation object
     */
    public Object getCapabilityObject()
    {
        return capabilityObject;
    }

    /**
     * Returns a flag whether this capability matches the specified class. This
     * means that the implementation object stored by this instance can be
     * assigned to a variable of the given class.
     *
     * @param cls the requested class
     * @return <b>true</b> if this capability matches this class, <b>false</b>
     *         otherwise
     */
    public boolean matches(Class<?> cls)
    {
        if (cls == null)
        {
            return false;
        }

        return cls.isAssignableFrom(getCapabilityClass());
    }

    /**
     * Returns a string representation of this object. This string contains the
     * capability class name and a string representation of the capability
     * object.
     *
     * @return a string for this object
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("Capability [ class = ");
        buf.append(getCapabilityClass().getName());
        buf.append(", object = ").append(getCapabilityObject());
        buf.append(" ]");
        return buf.toString();
    }
}
