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
package org.apache.commons.configuration2.builder;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.configuration2.ConfigurationDecoder;
import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.configuration2.convert.ConversionHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.configuration2.sync.Synchronizer;

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
 * <p>
 * <strong>Important note:</strong> This interface is not intended to be
 * implemented by client code! It defines a set of available properties and may
 * be extended even in minor releases.
 * </p>
 *
 * @since 2.0
 * @param <T> the type of the result of all set methods for method chaining
 */
public interface BasicBuilderProperties<T>
{
    /**
     * Sets the <em>logger</em> property. With this property a concrete
     * {@code ConfigurationLogger} object can be set for the configuration. Thus
     * logging behavior can be controlled.
     *
     * @param log the {@code Log} for the configuration produced by this builder
     * @return a reference to this object for method chaining
     */
    T setLogger(ConfigurationLogger log);

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
     * Sets the value of the <em>listDelimiterHandler</em> property. This
     * property defines the object responsible for dealing with list delimiter
     * and escaping characters. Note:
     * {@link org.apache.commons.configuration2.AbstractConfiguration AbstractConfiguration}
     * does not allow setting this property to <b>null</b>. If the default
     * {@code ListDelimiterHandler} is to be used, do not call this method.
     *
     * @param handler the {@code ListDelimiterHandler}
     * @return a reference to this object for method chaining
     */
    T setListDelimiterHandler(ListDelimiterHandler handler);

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

    /**
     * Sets the parent {@code ConfigurationInterpolator} for this
     * configuration's {@code ConfigurationInterpolator}. Setting a parent
     * {@code ConfigurationInterpolator} can be used for defining a default
     * behavior for variables which cannot be resolved.
     *
     * @param parent the new parent {@code ConfigurationInterpolator}
     * @return a reference to this object for method chaining
     * @see ConfigurationInterpolator#setParentInterpolator(ConfigurationInterpolator)
     */
    T setParentInterpolator(ConfigurationInterpolator parent);

    /**
     * Sets the {@code Synchronizer} object for this configuration. This object
     * is used to protect this configuration instance against concurrent access.
     * The concrete {@code Synchronizer} implementation used determines whether
     * a configuration instance is thread-safe or not.
     *
     * @param sync the {@code Synchronizer} to be used (a value of <b>null</b>
     *        means that a default {@code Synchronizer} is used)
     * @return a reference to this object for method chaining
     */
    T setSynchronizer(Synchronizer sync);

    /**
     * Sets the {@code ConversionHandler} object for this configuration. This
     * object is responsible for all data type conversions required for
     * accessing configuration properties in a specific target type. If this
     * property is not set, a default {@code ConversionHandler} is used.
     *
     * @param handler the {@code ConversionHandler} to be used
     * @return a reference to this object for method chaining
     */
    T setConversionHandler(ConversionHandler handler);

    /**
     * Sets the {@code ConfigurationDecoder} object for this configuration. This
     * object is called when encoded properties are queried using the
     * {@code getEncodedString()} method.
     *
     * @param decoder the {@code ConfigurationDecoder} to be used
     * @return a reference to this object for method chaining
     */
    T setConfigurationDecoder(ConfigurationDecoder decoder);

    /**
     * Sets a {@code BeanHelper} object to be used by the configuration builder.
     * The {@code BeanHelper} is used to create the managed configuration
     * instance dynamically. It is not a property of the configuration as most
     * other properties defined by this interface. By setting an alternative
     * {@code BeanHelper} the process of creating configuration instances via
     * reflection can be adapted. (Some specialized configuration builder
     * implementations also use a {@code BeanHelper} to create complex helper
     * objects during construction of their result object.
     * {@code CombinedConfigurationBuilder} for instance supports a complex
     * configuration definition format which may contain several specialized
     * bean declarations.) If no specific {@code BeanHelper} is set, the builder
     * uses the default instance.
     *
     * @param beanHelper the {@code BeanHelper} to be used by the builder
     * @return a reference to this object for method chaining
     */
    T setBeanHelper(BeanHelper beanHelper);
}
