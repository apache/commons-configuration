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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration.interpol.Lookup;
import org.apache.commons.logging.Log;

/**
 * <p>
 * An implementation of {@code BuilderParameters} which handles the parameters
 * of a {@link ConfigurationBuilder} common to all concrete
 * {@code Configuration} implementations.
 * </p>
 * <p>
 * This class provides methods for setting standard properties supported by the
 * {@code AbstractConfiguration} base class. A fluent interface can be used to
 * set property values.
 * </p>
 * <p>
 * This class is not thread-safe. It is intended that an instance is constructed
 * and initialized by a single thread during configuration of a
 * {@code ConfigurationBuilder}.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public class BasicBuilderParameters implements BuilderParameters,
        BasicBuilderProperties<BasicBuilderParameters>
{
    /** The key of the <em>throwExceptionOnMissing</em> property. */
    private static final String PROP_THROW_EXCEPTION_ON_MISSING =
            "throwExceptionOnMissing";

    /** The key of the <em>delimiterParsingDisabled</em> property. */
    private static final String PROP_DELIMITER_PARSING_DISABLED =
            "delimiterParsingDisabled";

    /** The key of the <em>listDelimiter</em> property. */
    private static final String PROP_LIST_DELIMITER = "listDelimiter";

    /** The key of the <em>logger</em> property. */
    private static final String PROP_LOGGER = "logger";

    /** The key for the <em>interpolator</em> property. */
    private static final String PROP_INTERPOLATOR = "interpolator";

    /** The key for the <em>prefixLookups</em> property. */
    private static final String PROP_PREFIX_LOOKUPS = "prefixLookups";

    /** The key for the <em>defaultLookups</em> property. */
    private static final String PROP_DEFAULT_LOOKUPS = "defaultLookups";

    /** The key for the <em>parentInterpolator</em> property. */
    private static final String PROP_PARENT_INTERPOLATOR = "parentInterpolator";

    /** The map for storing the current property values. */
    private final Map<String, Object> properties;

    /**
     * Creates a new instance of {@code BasicBuilderParameters}.
     */
    public BasicBuilderParameters()
    {
        properties = new HashMap<String, Object>();
        initDefaults();
    }

    /**
     * {@inheritDoc} This implementation returns a copy of the internal
     * parameters map with the values set so far.
     */
    public Map<String, Object> getParameters()
    {
        HashMap<String, Object> result =
                new HashMap<String, Object>(properties);
        if (result.containsKey(PROP_INTERPOLATOR))
        {
            // A custom ConfigurationInterpolator overrides lookups
            result.remove(PROP_PREFIX_LOOKUPS);
            result.remove(PROP_DEFAULT_LOOKUPS);
            result.remove(PROP_PARENT_INTERPOLATOR);
        }
        return result;
    }

    /**
     * Sets the <em>logger</em> property. With this property a concrete
     * {@code Log} object can be set for the configuration. Thus logging
     * behavior can be controlled.
     *
     * @param log the {@code Log} for the configuration produced by this builder
     * @return a reference to this object for method chaining
     */
    public BasicBuilderParameters setLogger(Log log)
    {
        return setProperty(PROP_LOGGER, log);
    }

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
    public BasicBuilderParameters setDelimiterParsingDisabled(boolean b)
    {
        return setProperty(PROP_DELIMITER_PARSING_DISABLED, Boolean.valueOf(b));
    }

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
    public BasicBuilderParameters setThrowExceptionOnMissing(boolean b)
    {
        return setProperty(PROP_THROW_EXCEPTION_ON_MISSING, Boolean.valueOf(b));
    }

    /**
     * Sets the value of the <em>listDelimiter</em> property. This property
     * defines the list delimiter character. It is evaluated only if the
     * <em>delimiterParsingDisabled</em> property is set to <b>false</b>.
     *
     * @param c the list delimiter character
     * @return a reference to this object for method chaining
     * @see #setDelimiterParsingDisabled(boolean)
     */
    public BasicBuilderParameters setListDelimiter(char c)
    {
        return setProperty(PROP_LIST_DELIMITER, Character.valueOf(c));
    }

    /**
     * {@inheritDoc} The passed in {@code ConfigurationInterpolator} is set
     * without modifications.
     */
    public BasicBuilderParameters setInterpolator(ConfigurationInterpolator ci)
    {
        return setProperty(PROP_INTERPOLATOR, ci);
    }

    /**
     * {@inheritDoc} A defensive copy of the passed in map is created. A
     * <b>null</b> argument causes all prefix lookups to be removed from the
     * internal parameters map.
     */
    public BasicBuilderParameters setPrefixLookups(
            Map<String, ? extends Lookup> lookups)
    {
        if (lookups == null)
        {
            properties.remove(PROP_PREFIX_LOOKUPS);
            return this;
        }
        else
        {
            return setProperty(PROP_PREFIX_LOOKUPS,
                    new HashMap<String, Lookup>(lookups));
        }
    }

    /**
     * {@inheritDoc} A defensive copy of the passed in collection is created. A
     * <b>null</b> argument causes all default lookups to be removed from the
     * internal parameters map.
     */
    public BasicBuilderParameters setDefaultLookups(
            Collection<? extends Lookup> lookups)
    {
        if (lookups == null)
        {
            properties.remove(PROP_DEFAULT_LOOKUPS);
            return this;
        }
        else
        {
            return setProperty(PROP_DEFAULT_LOOKUPS, new ArrayList<Lookup>(
                    lookups));
        }
    }

    /**
     * {@inheritDoc} This implementation stores the passed in
     * {@code ConfigurationInterpolator} object in the internal parameters map.
     */
    public BasicBuilderParameters setParentInterpolator(
            ConfigurationInterpolator parent)
    {
        return setProperty(PROP_PARENT_INTERPOLATOR, parent);
    }

    /**
     * Merges this object with the given parameters object. This method adds all
     * property values defined by the passed in parameters object to the
     * internal storage which are not already in. So properties already defined
     * in this object take precedence. Property names starting with the reserved
     * parameter prefix are ignored.
     *
     * @param p the object whose properties should be merged (must not be
     *        <b>null</b>)
     * @throws IllegalArgumentException if the passed in object is <b>null</b>
     */
    public void merge(BuilderParameters p)
    {
        if (p == null)
        {
            throw new IllegalArgumentException(
                    "Parameters to merge must not be null!");
        }

        for (Map.Entry<String, Object> e : p.getParameters().entrySet())
        {
            if (!properties.containsKey(e.getKey())
                    && !e.getKey().startsWith(RESERVED_PARAMETER_PREFIX))
            {
                storeProperty(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Obtains the {@code ConfigurationInterpolator} from the given map with
     * parameters. If such an object is stored in the map under the correct key,
     * it is returned. Otherwise, result is <b>null</b>.
     *
     * @param params the map with parameters (must not be <b>null</b>)
     * @return the {@code ConfigurationInterpolator} obtained from this map or
     *         <b>null</b>
     * @throws NullPointerException if the map is <b>null</b>
     */
    public static ConfigurationInterpolator fetchInterpolator(
            Map<String, Object> params)
    {
        return (ConfigurationInterpolator) params.get(PROP_INTERPOLATOR);
    }

    /**
     * Sets a property for this parameters object. Properties are stored in an
     * internal map. With this method a new entry can be added to this map. If
     * the value is <b>null</b>, the key is removed from the internal map. This
     * method can be used by sub classes which also store properties in a map.
     *
     * @param key the key of the property
     * @param value the value of the property
     */
    protected void storeProperty(String key, Object value)
    {
        if (value == null)
        {
            properties.remove(key);
        }
        else
        {
            properties.put(key, value);
        }
    }

    /**
     * Obtains the value of the specified property from the internal map. This
     * method can be used by derived classes if a specific property is to be
     * accessed. If the given key is not found, result is <b>null</b>.
     *
     * @param key the key of the property in question
     * @return the value of the property with this key or <b>null</b>
     */
    protected Object fetchProperty(String key)
    {
        return properties.get(key);
    }

    /**
     * Sets default parameter values.
     */
    private void initDefaults()
    {
        properties.put(PROP_DELIMITER_PARSING_DISABLED, Boolean.TRUE);
    }

    /**
     * Helper method for setting a property value.
     *
     * @param key the key of the property
     * @param value the value of the property
     * @return a reference to this object
     */
    private BasicBuilderParameters setProperty(String key, Object value)
    {
        storeProperty(key, value);
        return this;
    }
}
