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

package org.apache.commons.configuration2;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.configuration2.sync.SynchronizerSupport;


/**
 * <p>The main Configuration interface.</p>
 * <p>This interface allows accessing and manipulating a configuration object.
 * The major part of the methods defined in this interface deals with accessing
 * properties of various data types. There is a generic {@code getProperty()}
 * method, which returns the value of the queried property in its raw data
 * type. Other getter methods try to convert this raw data type into a specific
 * data type. If this fails, a {@code ConversionException} will be thrown.</p>
 * <p>For most of the property getter methods an overloaded version exists that
 * allows to specify a default value, which will be returned if the queried
 * property cannot be found in the configuration. The behavior of the methods
 * that do not take a default value in case of a missing property is not defined
 * by this interface and depends on a concrete implementation. E.g. the
 * {@link AbstractConfiguration} class, which is the base class
 * of most configuration implementations provided by this package, per default
 * returns <b>null</b> if a property is not found, but provides the
 * {@link AbstractConfiguration#setThrowExceptionOnMissing(boolean)
 * setThrowExceptionOnMissing()}
 * method, with which it can be configured to throw a {@code NoSuchElementException}
 * exception in that case. (Note that getter methods for primitive types in
 * {@code AbstractConfiguration} always throw an exception for missing
 * properties because there is no way of overloading the return value.)</p>
 * <p>With the {@code addProperty()} and {@code setProperty()} methods
 * new properties can be added to a configuration or the values of properties
 * can be changed. With {@code clearProperty()} a property can be removed.
 * Other methods allow to iterate over the contained properties or to create
 * a subset configuration.</p>
 *
 * @author Commons Configuration team
 */
public interface Configuration extends ImmutableConfiguration, SynchronizerSupport
{
    /**
     * Return a decorator Configuration containing every key from the current
     * Configuration that starts with the specified prefix. The prefix is
     * removed from the keys in the subset. For example, if the configuration
     * contains the following properties:
     *
     * <pre>
     *    prefix.number = 1
     *    prefix.string = Apache
     *    prefixed.foo = bar
     *    prefix = Jakarta</pre>
     *
     * the Configuration returned by {@code subset("prefix")} will contain
     * the properties:
     *
     * <pre>
     *    number = 1
     *    string = Apache
     *    = Jakarta</pre>
     *
     * (The key for the value "Jakarta" is an empty string)
     * <p>
     * Since the subset is a decorator and not a modified copy of the initial
     * Configuration, any change made to the subset is available to the
     * Configuration, and reciprocally.
     *
     * @param prefix The prefix used to select the properties.
     * @return a subset configuration
     *
     * @see SubsetConfiguration
     */
    Configuration subset(String prefix);

    /**
     * Add a property to the configuration. If it already exists then the value
     * stated here will be added to the configuration entry. For example, if
     * the property:
     *
     * <pre>resource.loader = file</pre>
     *
     * is already present in the configuration and you call
     *
     * <pre>addProperty("resource.loader", "classpath")</pre>
     *
     * Then you will end up with a List like the following:
     *
     * <pre>["file", "classpath"]</pre>
     *
     * @param key The key to add the property to.
     * @param value The value to add.
     */
    void addProperty(String key, Object value);

    /**
     * Set a property, this will replace any previously set values. Set values
     * is implicitly a call to clearProperty(key), addProperty(key, value).
     *
     * @param key The key of the property to change
     * @param value The new value
     */
    void setProperty(String key, Object value);

    /**
     * Remove a property from the configuration.
     *
     * @param key the key to remove along with corresponding value.
     */
    void clearProperty(String key);

    /**
     * Remove all properties from the configuration.
     */
    void clear();

    /**
     * Returns the {@code ConfigurationInterpolator} object used by this
     * {@code Configuration}. This object is responsible for variable
     * substitution.
     *
     * @return the {@code ConfigurationInterpolator} (can be <b>null</b>)
     */
    ConfigurationInterpolator getInterpolator();

    /**
     * Sets the {@code ConfigurationInterpolator} object to be used by this
     * {@code Configuration}. This object is invoked for each access of a string
     * property in order to substitute variables which may be contained. The
     * argument can be <b>null</b> to disable interpolation at all.
     *
     * @param ci the new {@code ConfigurationInterpolator}
     */
    void setInterpolator(ConfigurationInterpolator ci);

    /**
     * Creates and installs a new {@code ConfigurationInterpolator} for this
     * {@code Configuration} based on the passed in arguments. This method
     * creates a default {@code ConfigurationInterpolator} instance and
     * initializes it with the passed in {@code Lookup} objects. It also adds a
     * special default {@code Lookup} object that tries to resolve variables by
     * matching them with properties contained in this {@code Configuration}.
     * This is also the main difference to the
     * {@link #setInterpolator(ConfigurationInterpolator)} method
     * which sets the passed in object as is without adding this special lookup.
     *
     * @param prefixLookups the map with {@code Lookup} objects associated with
     *        specific prefixes (can be <b>null</b>)
     * @param defLookups a collection with default {@code Lookup} objects (can
     *        be <b>null</b>)
     * @see ConfigurationInterpolator
     */
    void installInterpolator(Map<String, ? extends Lookup> prefixLookups,
            Collection<? extends Lookup> defLookups);
}
