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

import java.util.Collection;
import java.util.Map;

import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration.interpol.Lookup;
import org.apache.commons.logging.Log;

/**
 * <p>
 * Definition of a properties interface for basic parameters which are supported
 * by all {@link ConfigurationBuilder} implementations derived from
 * {@link BasicConfigurationBuilder}.
 * </p>
 * <p>
 * This interface defines the single properties supported by a parameters
 * object. Properties can be set using a fluent API making it convenient for
 * client code to specify concrete property values in a single statement.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 * @param <T> the type of the result of all set methods for method chaining
 */
public interface BasicBuilderProperties<T>
{
    /**
     * Sets the <em>logger</em> property. With this property a concrete
     * {@code Log} object can be set for the configuration. Thus logging
     * behavior can be controlled.
     *
     * @param log the {@code Log} for the configuration produced by this builder
     * @return a reference to this object for method chaining
     */
    T setLogger(Log log);

    /**
     * Sets the value of the <em>delimiterParsingDisabled</em> property. This
     * property controls whether the configuration should look for list
     * delimiter characters in the values of newly added properties. If the
     * property value is <b>true</b> and such characters are encountered,
     * multiple values are stored for the affected property.
     *
     * @param b the value of the property
     * @return a reference to this object for method chaining
     */
    T setDelimiterParsingDisabled(boolean b);

    /**
     * Sets the value of the <em>throwExceptionOnMissing</em> property. This
     * property controls the configuration's behavior if missing properties are
     * queried: a value of <b>true</b> causes the configuration to throw an
     * exception, for a value of <b>false</b> it will return <b>null</b> values.
     * (Note: Methods returning a primitive data type will always throw an
     * exception if the property is not defined.)
     *
     * @param b the value of the property
     * @return a reference to this object for method chaining
     */
    T setThrowExceptionOnMissing(boolean b);

    /**
     * Sets the value of the <em>listDelimiter</em> property. This property
     * defines the list delimiter character. It is evaluated only if the
     * <em>delimiterParsingDisabled</em> property is set to <b>false</b>.
     *
     * @param c the list delimiter character
     * @return a reference to this object for method chaining
     * @see #setDelimiterParsingDisabled(boolean)
     */
    T setListDelimiter(char c);

    /**
     * Sets the {@code ConfigurationInterpolator} to be used for this
     * configuration. Using this method a custom
     * {@code ConfigurationInterpolator} can be set which can be freely
     * configured. Alternatively, it is possible to add custom {@code Lookup}
     * objects using other methods provided by this interface.
     *
     * @param ci the {@code ConfigurationInterpolator} for this configuration
     * @return a reference to this object for method chaining
     */
    T setInterpolator(ConfigurationInterpolator ci);

    /**
     * Sets additional {@code Lookup} objects for specific prefixes for this
     * configuration object. All {@code Lookup} objects contained in the given
     * map are added to the configuration's {@code ConfigurationInterpolator}.
     * Note: This method only takes effect if no
     * {@code ConfigurationInterpolator} is set using the
     * {@link #setInterpolator(ConfigurationInterpolator)} method.
     *
     * @param lookups a map with {@code Lookup} objects and their associated
     *        prefixes
     * @return a reference to this object for method chaining
     * @see ConfigurationInterpolator#registerLookups(Map)
     */
    T setPrefixLookups(Map<String, ? extends Lookup> lookups);

    /**
     * Adds additional default {@code Lookup} objects (i.e. lookups which are
     * not associated with a specific prefix) to this configuration object.
     * Note: This method only takes effect if no
     * {@code ConfigurationInterpolator} is set using the
     * {@link #setInterpolator(ConfigurationInterpolator)} method.
     *
     * @param lookups a collection with {@code Lookup} objects to be added as
     *        default lookups at the configuration's
     *        {@code ConfigurationInterpolator}
     * @return a reference to this object for method chaining
     * @see ConfigurationInterpolator#addDefaultLookups(Collection)
     */
    T setDefaultLookups(Collection<? extends Lookup> lookups);
}
