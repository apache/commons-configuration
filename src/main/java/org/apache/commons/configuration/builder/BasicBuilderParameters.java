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
import org.apache.commons.configuration.interpol.InterpolatorSpecification;
import org.apache.commons.configuration.interpol.Lookup;
import org.apache.commons.configuration.sync.Synchronizer;
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
public class BasicBuilderParameters implements Cloneable, BuilderParameters,
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

    /** The key for the <em>synchronizer</em> property. */
    private static final String PROP_SYNCHRONIZER = "synchronizer";

    /** The map for storing the current property values. */
    private Map<String, Object> properties;

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
     * parameters map with the values set so far. Collection structures
     * (e.g. for lookup objects) are stored as defensive copies, so the
     * original data cannot be modified.
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

        createDefensiveCopies(result);
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
     * {@inheritDoc} This implementation stores the passed in
     * {@code Synchronizer} object in the internal parameters map.
     */
    public BasicBuilderParameters setSynchronizer(Synchronizer sync)
    {
        return setProperty(PROP_SYNCHRONIZER, sync);
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
     * Obtains a specification for a {@link ConfigurationInterpolator} from the
     * specified map with parameters. All properties related to interpolation
     * are evaluated and added to the specification object.
     *
     * @param params the map with parameters (must not be <b>null</b>)
     * @return an {@code InterpolatorSpecification} object constructed with data
     *         from the map
     * @throws IllegalArgumentException if the map is <b>null</b> or contains
     *         invalid data
     */
    public static InterpolatorSpecification fetchInterpolatorSpecification(
            Map<String, Object> params)
    {
        checkParameters(params);
        return new InterpolatorSpecification.Builder()
                .withInterpolator(
                        fetchParameter(params, PROP_INTERPOLATOR,
                                ConfigurationInterpolator.class))
                .withParentInterpolator(
                        fetchParameter(params, PROP_PARENT_INTERPOLATOR,
                                ConfigurationInterpolator.class))
                .withPrefixLookups(fetchAndCheckPrefixLookups(params))
                .withDefaultLookups(fetchAndCheckDefaultLookups(params))
                .create();
    }

    /**
     * Clones this object. This is useful because multiple builder instances may
     * use a similar set of parameters. However, single instances of parameter
     * objects must not assigned to multiple builders. Therefore, cloning a
     * parameters object provides a solution for this use case. This method
     * creates a new parameters object with the same content as this one. The
     * internal map storing the parameter values is cloned, too, also collection
     * structures contained in this map. However, no a full deep clone operation
     * is performed. Objects like a {@code ConfigurationInterpolator} or
     * {@code Lookup}s are shared between this and the newly created instance.
     *
     * @return a clone of this object
     */
    @Override
    public BasicBuilderParameters clone()
    {
        try
        {
            BasicBuilderParameters copy =
                    (BasicBuilderParameters) super.clone();
            copy.properties = getParameters();
            return copy;
        }
        catch (CloneNotSupportedException cnex)
        {
            // should not happen
            throw new AssertionError(cnex);
        }
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

    /**
     * Creates defensive copies for collection structures when constructing the
     * map with parameters. It should not be possible to modify this object's
     * internal state when having access to the parameters map.
     *
     * @param params the map with parameters to be passed to the caller
     */
    private static void createDefensiveCopies(HashMap<String, Object> params)
    {
        Map<String, ? extends Lookup> prefixLookups =
                fetchPrefixLookups(params);
        if (prefixLookups != null)
        {
            params.put(PROP_PREFIX_LOOKUPS, new HashMap<String, Lookup>(
                    prefixLookups));
        }
        Collection<? extends Lookup> defLookups = fetchDefaultLookups(params);
        if (defLookups != null)
        {
            params.put(PROP_DEFAULT_LOOKUPS, new ArrayList<Lookup>(defLookups));
        }
    }

    /**
     * Obtains the map with prefix lookups from the parameters map.
     *
     * @param params the map with parameters
     * @return the map with prefix lookups (may be <b>null</b>)
     */
    private static Map<String, ? extends Lookup> fetchPrefixLookups(
            Map<String, Object> params)
    {
        // This is safe to cast because we either have full control over the map
        // and thus know the types of the contained values or have checked
        // the content before
        @SuppressWarnings("unchecked")
        Map<String, ? extends Lookup> prefixLookups =
                (Map<String, ? extends Lookup>) params.get(PROP_PREFIX_LOOKUPS);
        return prefixLookups;
    }

    /**
     * Tests whether the passed in map with parameters contains a map with
     * prefix lookups. This method is used if the parameters map is from an
     * insecure source and we cannot be sure that it contains valid data.
     * Therefore, we have to map that the key for the prefix lookups actually
     * points to a map containing keys and values of expected data types.
     *
     * @param params the parameters map
     * @return the obtained map with prefix lookups
     * @throws IllegalArgumentException if the map contains invalid data
     */
    private static Map<String, ? extends Lookup> fetchAndCheckPrefixLookups(
            Map<String, Object> params)
    {
        Map<?, ?> prefixes =
                fetchParameter(params, PROP_PREFIX_LOOKUPS, Map.class);
        if (prefixes == null)
        {
            return null;
        }

        for (Map.Entry<?, ?> e : prefixes.entrySet())
        {
            if (!(e.getKey() instanceof String)
                    || !(e.getValue() instanceof Lookup))
            {
                throw new IllegalArgumentException(
                        "Map with prefix lookups contains invalid data: "
                                + prefixes);
            }
        }
        return fetchPrefixLookups(params);
    }

    /**
     * Obtains the collection with default lookups from the parameters map.
     *
     * @param params the map with parameters
     * @return the collection with default lookups (may be <b>null</b>)
     */
    private static Collection<? extends Lookup> fetchDefaultLookups(
            Map<String, Object> params)
    {
        // This is safe to cast because we either have full control over the map
        // and thus know the types of the contained values or have checked
        // the content before
        @SuppressWarnings("unchecked")
        Collection<? extends Lookup> defLookups =
                (Collection<? extends Lookup>) params.get(PROP_DEFAULT_LOOKUPS);
        return defLookups;
    }

    /**
     * Tests whether the passed in map with parameters contains a valid
     * collection with default lookups. This method works like
     * {@link #fetchAndCheckPrefixLookups(Map)}, but tests the default lookups
     * collection.
     *
     * @param params the map with parameters
     * @return the collection with default lookups (may be <b>null</b>)
     * @throws IllegalArgumentException if invalid data is found
     */
    private static Collection<? extends Lookup> fetchAndCheckDefaultLookups(
            Map<String, Object> params)
    {
        Collection<?> col =
                fetchParameter(params, PROP_DEFAULT_LOOKUPS, Collection.class);
        if (col == null)
        {
            return null;
        }

        for (Object o : col)
        {
            if (!(o instanceof Lookup))
            {
                throw new IllegalArgumentException(
                        "Collection with default lookups contains invalid data: "
                                + col);
            }
        }
        return fetchDefaultLookups(params);
    }

    /**
     * Obtains a parameter from a map and performs a type check.
     *
     * @param params the map with parameters
     * @param key the key of the parameter
     * @param expClass the expected class of the parameter value
     * @param <T> the parameter type
     * @return the value of the parameter in the correct data type
     * @throws IllegalArgumentException if the parameter is not of the expected
     *         type
     */
    private static <T> T fetchParameter(Map<String, Object> params, String key,
            Class<T> expClass)
    {
        Object value = params.get(key);
        if (value == null)
        {
            return null;
        }
        if (!expClass.isInstance(value))
        {
            throw new IllegalArgumentException(String.format(
                    "Parameter %s is not of type %s!", key,
                    expClass.getSimpleName()));
        }
        return expClass.cast(value);
    }

    /**
     * Checks whether a map with parameters is present. Throws an exception if
     * not.
     *
     * @param params the map with parameters to check
     * @throws IllegalArgumentException if the map is <b>null</b>
     */
    private static void checkParameters(Map<String, Object> params)
    {
        if (params == null)
        {
            throw new IllegalArgumentException(
                    "Parameters map must not be null!");
        }
    }
}
