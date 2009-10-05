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

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * An abstract base class for implementations of the {@code ConfigurationSource}
 * interface.
 * </p>
 * <p>
 * This class can be used as super class to simplify the implementation of a
 * concrete {@code ConfigurationSource}. It already provides basic
 * implementations of some of the methods defined by the {@code
 * ConfigurationSource} interface. Especially the handling of
 * <em>capabilities</em> is fully implemented. Derived classes can hook into
 * this mechanism by defining custom capabilities in the
 * {@link #appendCapabilities(Collection)} method. Optional methods, e.g. the
 * methods for registering event listeners, are implemented by throwing an
 * {@code UnsupportedOperationException}.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public abstract class AbstractConfigurationSource implements
        ConfigurationSource
{
    /** Stores the capabilities associated with this source. */
    private final AtomicReference<Capabilities> capabilities;

    /**
     * Creates a new instance of {@code AbstractConfigurationSource}.
     */
    protected AbstractConfigurationSource()
    {
        capabilities = new AtomicReference<Capabilities>();
    }

    /**
     * Adds a {@code ConfigurationSourceListener} to this object. This is just a
     * dummy implementation that throws a {@code UnsupportedOperationException}.
     *
     * @param l the listener to be added
     */
    public void addConfigurationSourceListener(ConfigurationSourceListener l)
    {
        throw new UnsupportedOperationException("Not implemented!");
    }

    /**
     * Returns the capability of the the specified type. This implementation
     * delegates to a {@link Capabilities} object maintained internally which is
     * created on first access by {@link #createCapabilities()}. It takes care
     * for proper synchronization.
     *
     * @param <T> the type of the capability requested
     * @param cls the class of the capability interface
     * @return the object implementing the desired capability or <b>null</b> if
     *         this capability is not supported
     */
    public <T> T getCapability(Class<T> cls)
    {
        return getCapabilities().getCapability(cls);
    }

    /**
     * Removes the specified {@code ConfigurationSourceListener} from this
     * object. This is just a dummy implementation that throws a {@code
     * UnsupportedOperationException}.
     *
     * @param l the listener to be removed
     */
    public boolean removeConfigurationSourceListener(
            ConfigurationSourceListener l)
    {
        throw new UnsupportedOperationException("Not implemented!");
    }

    /**
     * Returns the {@code Capabilities} object that manages the capabilities
     * supported by this {@code ConfigurationSource}. The object is created on
     * first access. <em>Implementation note:</em> For synchronizing access to
     * the {@code Capabilities} object an atomic variable is used. If no object
     * has been created yet, {@link #createCapabilities()} is called. If this
     * method is called by multiple threads, it is possible that
     * {@link #createCapabilities()} is invoked multiple times. However, it is
     * ensured that this method always returns the same {@code Capabilities}
     * instance.
     *
     * @return the {@code Capabilities} object associated with this {@code
     *         ConfigurationSource}
     */
    protected Capabilities getCapabilities()
    {
        Capabilities caps = capabilities.get();

        if (caps == null)
        {
            Capabilities capsNew = createCapabilities();
            if (capabilities.compareAndSet(null, capsNew))
            {
                caps = capsNew;
            }
            else
            {
                caps = capabilities.get();
            }
        }

        return caps;
    }

    /**
     * Creates a {@code Capabilities} object for managing the capabilities
     * supported by this {@code ConfigurationSource}. This method is called by
     * {@link #getCapability(Class)} on first access. This implementation calls
     * {@link #appendCapabilities(Collection)} to obtain a list of additional
     * capabilities. Then it creates a {@code Capabilities} instance with this
     * list and this {@code ConfigurationSource} object as owner.
     *
     * @return the {@code Capabilities} supported by this {@code
     *         ConfigurationSource}
     */
    protected Capabilities createCapabilities()
    {
        Collection<Capability> caps = new LinkedList<Capability>();
        appendCapabilities(caps);
        return new Capabilities(this, caps);
    }

    /**
     * Creates additional {@code Capability} objects and adds them to the passed
     * in list. This method can be overridden by derived classes to add {@code
     * Capability} instances to the internal capability management for
     * interfaces that are not implemented by this object. This base
     * implementation is empty. Classes overriding this method should always
     * call the super method to ensure that capabilities of the base classes do
     * not get lost.
     *
     * @param caps a collection for adding additional {@code Capability}
     *        instances
     */
    protected void appendCapabilities(Collection<Capability> caps)
    {
    }
}
