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

import java.util.Iterator;

/**
 * <p>
 * An interface defining a source for configuration settings.
 * </p>
 * <p>
 * This interfaces defines a set of fundamental methods for dealing with
 * configuration properties. The (raw) values of properties can be queried or
 * set, new properties can be added, or existing properties can be removed.
 * Further, it is possible to iterate over the property keys stored in this
 * configuration source.
 * </p>
 * <p>
 * All of the operations provided by this interface are on a very basic level.
 * There are no convenience methods or operations performing sophisticated
 * transformations on properties. The interface solely focuses on providing
 * access to configuration properties. But it is possible to implement such
 * high-level operations on top of this interface.
 * </p>
 * <p>
 * So this interface serves the purpose of achieving a separation of concerns:
 * Concrete implementations support specific ways of storing configuration data.
 * Because this interface is pretty lean these implementations can focus on the
 * essentials of property access. In addition to that there can be convenience
 * classes implementing enhanced functionality on top of this interface. This
 * includes, but is not limited to things like data conversion, interpolation,
 * or enhanced query facilities.
 * </p>
 * <p>
 * This interface contains some methods that are marked as optional. This means
 * that an implementation is free to ignore these methods and throw an {@code
 * UnsupportedOperationException}. The background is that it is possible to
 * implement these methods using a combination of other (non optional) methods
 * provided by {@code ConfigurationSource}. For instance, the {@code size()}
 * method can be implemented by iterating over the keys contained in this
 * configuration source, calling {@code getProperty()} on each, and counting the
 * values returned. If an implementation is able to provide more efficient
 * implementations of optional methods, it should override the methods in
 * question.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public interface ConfigurationSource
{
    /**
     * Checks if the configuration is empty. This is an optional operation. It
     * may be implemented by querying the keys contained in this configuration
     * source and checking whether this iterator is empty.
     *
     * @return <code>true</code> if the configuration contains no property,
     *         <code>false</code> otherwise.
     * @throws UnsupportedOperationException if this operation is not implemented
     */
    boolean isEmpty();

    /**
     * Checks if the configuration contains the specified key.
     *
     * @param key the key whose presence in this configuration is to be tested
     * @return <code>true</code> if the configuration contains a value for this
     *         key, <code>false</code> otherwise
     */
    boolean containsKey(String key);

    /**
     * Adds a property to the configuration. If it already exists, then the value
     * stated here will be added to the configuration entry. For example, if the
     * property:
     *
     * <pre>
     * resource.loader = file
     * </pre>
     *
     * is already present in the configuration and you call
     *
     * <pre>
     * addProperty(&quot;resource.loader&quot;, &quot;classpath&quot;)
     * </pre>
     *
     * Then you will end up with a List like the following:
     *
     * <pre>
     * [&quot;file&quot;, &quot;classpath&quot;]
     * </pre>
     *
     * @param key The key to add the property to.
     * @param value The value to add.
     */
    void addProperty(String key, Object value);

    /**
     * Sets the value of a property. The new value will replace any previously
     * set values. It is treated as a single value.
     *
     * @param key The key of the property to change
     * @param value The new value
     */
    void setProperty(String key, Object value);

    /**
     * Removes a property from the configuration.
     *
     * @param key the key to remove along with corresponding value.
     */
    void clearProperty(String key);

    /**
     * Removes all properties from the configuration.
     */
    void clear();

    /**
     * Gets a property from this configuration source. The return value is the
     * "raw" value of this property or <b>null</b> if the property is not
     * contained in this configuration source. For properties with multiple
     * values a collection with all values will be returned. If the object
     * returned by this method is mutable (e.g. a collection with multiple
     * property values), an implementation should take actions to prevent that
     * callers can directly modify the internal state of this configuration
     * source (e.g. by removing elements from the values collection). A way to
     * achieve this could be the creation of defensive copies.
     *
     * @param key property to retrieve
     * @return the value to which this configuration maps the specified key, or
     *         null if the configuration contains no mapping for this key.
     */
    Object getProperty(String key);

    /**
     * Gets the list of the keys contained in this configuration source. The
     * returned iterator can be used to obtain all defined keys. Note that the
     * exact behavior of the iterator's <code>remove()</code> method is specific
     * to a concrete implementation. It <em>may</em> remove the corresponding
     * property from the configuration, but this is not guaranteed. In any case
     * it is no replacement for calling
     * <code>{@link #clearProperty(String)}</code> for this property. So it is
     * highly recommended to avoid using the iterator's <code>remove()</code>
     * method.
     *
     * @return an {@code Iterator} for the keys stored in this configuration
     *         source
     */
    Iterator<String> getKeys();

    /**
     * Gets the list of the keys contained in this configuration source that
     * match the specified prefix. This is an optional operation. It may be
     * implemented using {@code getKeys()} and filtering all keys that do not
     * start with the prefix.
     *
     * @param prefix The prefix to test against.
     * @return An Iterator of keys that match the prefix.
     * @throws UnsupportedOperationException if this operation is not
     *         implemented
     * @see #getKeys()
     */
    Iterator<String> getKeys(String prefix);

    /**
     * Returns the size of this configuration source. This is the number of
     * values stored in this configuration source. This is an optional
     * operation. It may be implemented by iterating over the keys contained in
     * this configuration source and constructing the sum of the values stored
     * for each key. Depending on an implementation, there is no guarantee that
     * this method returns in constant time. Especially for tests whether a
     * configuration source contains any properties, the {@code isEmpty()}
     * method will in most cases be more efficient that a test for {@code size()
     * == 0}.
     *
     * @return the size of this configuration source
     * @throws UnsupportedOperationException if this operation is not
     *         implemented
     */
    int size();

    /**
     * Returns the number of values stored for the property with the given key.
     * If the property is not contained in this configuration source, return
     * value is 0. If it has a single value, result is 1. For properties with
     * multiple values the number of values is returned. This is an optional
     * operation. It can be implemented by evaluating the object returned by
     * {@code getProperty()}.
     *
     * @param key the key of the property in question
     * @return the number of values stored for this property
     * @throws UnsupportedOperationException if this operation is not
     *         implemented
     * @see #getProperty(String)
     */
    int valueCount(String key);

    /**
     * Adds a {@code ConfigurationSourceListener} for this {@code
     * ConfigurationSource}. This listener will be notified about manipulations
     * on this source. Support for event listeners is optional. An
     * implementation can throw an {@code UnsupportedOperationException}
     * exception. By using a wrapper that supports event notifications it is
     * possible to monitor such a {@code ConfigurationSource}.
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
}
