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
 * A base interface definition for sources of configuration settings.
 * </p>
 * <p>
 * A {@code ConfigurationSource} provides access to configuration settings that
 * are stored in a specific way. The actual storage scheme used by a concrete
 * configuration source determines the way the properties can be accessed. For
 * instance, some sources store their data as key-value pairs. They typically
 * provide a map-like interface for accessing their content. Other sources are
 * hierarchically organized and require a different interface.
 * </p>
 * <p>
 * This base interface defines methods that are common to all variants of
 * configuration sources. It allows treating different types of configuration
 * sources in a uniform way as long as only these fundamental methods are
 * involved. Because there are differences in the way the properties contained
 * in the source are accessed, this interface does not define methods for
 * reading or writing property values.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public interface ConfigurationSource
{
    /**
     * Removes all properties contained in this {@code ConfigurationSource}.
     */
    void clear();

    /**
     * Adds a {@code ConfigurationSourceListener} for this {@code
     * ConfigurationSource}. This listener will be notified about manipulations
     * on this source. Support for event listeners is optional. An
     * implementation can throw an {@code UnsupportedOperationException}
     * exception.
     *
     * @param l the listener to be added (must not be <b>null</b>)
     * @throws IllegalArgumentException if the listener is <b>null</b>
     * @throws UnsupportedOperationException if this operation is not
     *         implemented
     */
    void addConfigurationSourceListener(ConfigurationSourceListener l);

    /**
     * Removes the specified {@code ConfigurationSourceListener} from this
     * {@code ConfigurationSource}. It will not receive notifications about
     * changes on this source any more. The return value indicates whether the
     * listener existed and could be removed. As was pointed out for
     * {@link #addConfigurationSourceListener(ConfigurationSourceListener)},
     * this is an optional operation.
     *
     * @param l the listener to be removed
     * @return a flag whether the listener could be removed
     * @throws UnsupportedOperationException if this operation is not
     *         implemented
     */
    boolean removeConfigurationSourceListener(ConfigurationSourceListener l);

    /**
     * <p>
     * Returns the capability of the the specified type.
     * </p>
     * <p>
     * Beyond the basic set of methods defined by the {@code
     * ConfigurationSource} interface and its sub interfaces a concrete source
     * implementation can provide additional features (e.g. persistence
     * operations). The interfaces required for controlling these features can
     * be queried using this generic method.
     * </p>
     * <p>
     * This is an application of the <em>capability pattern</em>. It allows
     * keeping the basic interfaces for configuration sources lean, but
     * flexible. Additional functionality can be added later without the need to
     * extend the interfaces. Note that this method can return <b>null</b> if
     * the capability requested is not available. Callers should always check
     * for <b>null</b> results.
     * </p>
     *
     * @param <T> the type of the capability requested
     * @param cls the class of the capability interface
     * @return the object implementing the desired capability or <b>null</b> if
     *         this capability is not supported
     */
    <T> T getCapability(Class<T> cls);
}
