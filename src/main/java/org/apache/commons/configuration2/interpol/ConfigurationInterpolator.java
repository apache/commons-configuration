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
package org.apache.commons.configuration2.interpol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * <p>
 * A class that handles interpolation (variable substitution) for configuration
 * objects.
 * </p>
 * <p>
 * Each instance of {@code AbstractConfiguration} is associated with an object
 * of this class. All interpolation tasks are delegated to this object.
 * </p>
 * <p>
 * {@code ConfigurationInterpolator} internally uses the {@code StrSubstitutor}
 * class from <a href="http://commons.apache.org/lang">Commons Lang</a>. Thus it
 * supports the same syntax of variable expressions.
 * </p>
 * <p>
 * The basic idea of this class is that it can maintain a set of primitive
 * {@link Lookup} objects, each of which is identified by a special prefix. The
 * variables to be processed have the form <code>${prefix:name}</code>.
 * {@code ConfigurationInterpolator} will extract the prefix and determine,
 * which primitive lookup object is registered for it. Then the name of the
 * variable is passed to this object to obtain the actual value. It is also
 * possible to define an arbitrary number of default lookup objects, which are
 * used for variables that do not have a prefix or that cannot be resolved by
 * their associated lookup object. When adding default lookup objects their
 * order matters; they are queried in this order, and the first non-<b>null</b>
 * variable value is used.
 * </p>
 * <p>
 * After an instance has been created it does not contain any {@code Lookup}
 * objects. The current set of lookup objects can be modified using the
 * {@code registerLookup()} and {@code deregisterLookup()} methods. Default
 * lookup objects (that are invoked for variables without a prefix) can be added
 * or removed with the {@code addDefaultLookup()} and
 * {@code removeDefaultLookup()} methods respectively. (When a
 * {@code ConfigurationInterpolator} instance is created by a configuration
 * object, a default lookup object is added pointing to the configuration
 * itself, so that variables are resolved using the configuration's properties.)
 * </p>
 * <p>
 * The default usage scenario is that on a fully initialized instance the
 * {@code interpolate()} method is called. It is passed an object value which
 * may contain variables. All these variables are substituted if they can be
 * resolved. The result is the passed in value with variables replaced.
 * Alternatively, the {@code resolve()} method can be called to obtain the
 * values of specific variables without performing interpolation.
 * </p>
 * <p>
 * Implementation node: This class is thread-safe. Lookup objects can be added
 * or removed at any time concurrent to interpolation operations.
 * </p>
 *
 * @version $Id$
 * @since 1.4
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 */
public class ConfigurationInterpolator
{
    /** Constant for the prefix separator. */
    private static final char PREFIX_SEPARATOR = ':';

    /** The variable prefix. */
    private static final String VAR_START = "${";

    /** The variable suffix. */
    private static final String VAR_END = "}";

    /** A map containing the default prefix lookups. */
    private static final Map<String, Lookup> DEFAULT_PREFIX_LOOKUPS;

    /** A map with the currently registered lookup objects. */
    private final Map<String, Lookup> prefixLookups;

    /** Stores the default lookup objects. */
    private final List<Lookup> defaultLookups;

    /** The helper object performing variable substitution. */
    private final StrSubstitutor substitutor;

    /** Stores a parent interpolator objects if the interpolator is nested hierarchically. */
    private volatile ConfigurationInterpolator parentInterpolator;

    /**
     * Creates a new instance of {@code ConfigurationInterpolator}.
     */
    public ConfigurationInterpolator()
    {
        prefixLookups = new ConcurrentHashMap<>();
        defaultLookups = new CopyOnWriteArrayList<>();
        substitutor = initSubstitutor();
    }

    /**
     * Creates a new {@code ConfigurationInterpolator} instance based on the
     * passed in specification object. If the {@code InterpolatorSpecification}
     * already contains a {@code ConfigurationInterpolator} object, it is used
     * directly. Otherwise, a new instance is created and initialized with the
     * properties stored in the specification.
     *
     * @param spec the {@code InterpolatorSpecification} (must not be
     *        <b>null</b>)
     * @return the {@code ConfigurationInterpolator} obtained or created based
     *         on the given specification
     * @throws IllegalArgumentException if the specification is <b>null</b>
     * @since 2.0
     */
    public static ConfigurationInterpolator fromSpecification(
            InterpolatorSpecification spec)
    {
        if (spec == null)
        {
            throw new IllegalArgumentException(
                    "InterpolatorSpecification must not be null!");
        }
        return (spec.getInterpolator() != null) ? spec.getInterpolator()
                : createInterpolator(spec);
    }

    /**
     * Returns a map containing the default prefix lookups. Every configuration
     * object derived from {@code AbstractConfiguration} is by default
     * initialized with a {@code ConfigurationInterpolator} containing these
     * {@code Lookup} objects and their prefixes. The map cannot be modified
     *
     * @return a map with the default prefix {@code Lookup} objects and their
     *         prefixes
     * @since 2.0
     */
    public static Map<String, Lookup> getDefaultPrefixLookups()
    {
        return DEFAULT_PREFIX_LOOKUPS;
    }

    /**
     * Utility method for obtaining a {@code Lookup} object in a safe way. This
     * method always returns a non-<b>null</b> {@code Lookup} object. If the
     * passed in {@code Lookup} is not <b>null</b>, it is directly returned.
     * Otherwise, result is a dummy {@code Lookup} which does not provide any
     * values.
     *
     * @param lookup the {@code Lookup} to check
     * @return a non-<b>null</b> {@code Lookup} object
     * @since 2.0
     */
    public static Lookup nullSafeLookup(Lookup lookup)
    {
        if (lookup == null)
        {
            lookup = DummyLookup.INSTANCE;
        }
        return lookup;
    }

    /**
     * Returns a map with the currently registered {@code Lookup} objects and
     * their prefixes. This is a snapshot copy of the internally used map. So
     * modifications of this map do not effect this instance.
     *
     * @return a copy of the map with the currently registered {@code Lookup}
     *         objects
     */
    public Map<String, Lookup> getLookups()
    {
        return new HashMap<>(prefixLookups);
    }

    /**
     * Registers the given {@code Lookup} object for the specified prefix at
     * this instance. From now on this lookup object will be used for variables
     * that have the specified prefix.
     *
     * @param prefix the variable prefix (must not be <b>null</b>)
     * @param lookup the {@code Lookup} object to be used for this prefix (must
     *        not be <b>null</b>)
     * @throws IllegalArgumentException if either the prefix or the
     *         {@code Lookup} object is <b>null</b>
     */
    public void registerLookup(String prefix, Lookup lookup)
    {
        if (prefix == null)
        {
            throw new IllegalArgumentException(
                    "Prefix for lookup object must not be null!");
        }
        if (lookup == null)
        {
            throw new IllegalArgumentException(
                    "Lookup object must not be null!");
        }
        prefixLookups.put(prefix, lookup);
    }

    /**
     * Registers all {@code Lookup} objects in the given map with their prefixes
     * at this {@code ConfigurationInterpolator}. Using this method multiple
     * {@code Lookup} objects can be registered at once. If the passed in map is
     * <b>null</b>, this method does not have any effect.
     *
     * @param lookups the map with lookups to register (may be <b>null</b>)
     * @throws IllegalArgumentException if the map contains <b>entries</b>
     */
    public void registerLookups(Map<String, ? extends Lookup> lookups)
    {
        if (lookups != null)
        {
            prefixLookups.putAll(lookups);
        }
    }

    /**
     * Deregisters the {@code Lookup} object for the specified prefix at this
     * instance. It will be removed from this instance.
     *
     * @param prefix the variable prefix
     * @return a flag whether for this prefix a lookup object had been
     *         registered
     */
    public boolean deregisterLookup(String prefix)
    {
        return prefixLookups.remove(prefix) != null;
    }

    /**
     * Returns an unmodifiable set with the prefixes, for which {@code Lookup}
     * objects are registered at this instance. This means that variables with
     * these prefixes can be processed.
     *
     * @return a set with the registered variable prefixes
     */
    public Set<String> prefixSet()
    {
        return Collections.unmodifiableSet(prefixLookups.keySet());
    }

    /**
     * Returns a collection with the default {@code Lookup} objects
     * added to this {@code ConfigurationInterpolator}. These objects are not
     * associated with a variable prefix. The returned list is a snapshot copy
     * of the internal collection of default lookups; so manipulating it does
     * not affect this instance.
     *
     * @return the default lookup objects
     */
    public List<Lookup> getDefaultLookups()
    {
        return new ArrayList<>(defaultLookups);
    }

    /**
     * Adds a default {@code Lookup} object. Default {@code Lookup} objects are
     * queried (in the order they were added) for all variables without a
     * special prefix. If no default {@code Lookup} objects are present, such
     * variables won't be processed.
     *
     * @param defaultLookup the default {@code Lookup} object to be added (must
     *        not be <b>null</b>)
     * @throws IllegalArgumentException if the {@code Lookup} object is
     *         <b>null</b>
     */
    public void addDefaultLookup(Lookup defaultLookup)
    {
        defaultLookups.add(defaultLookup);
    }

    /**
     * Adds all {@code Lookup} objects in the given collection as default
     * lookups. The collection can be <b>null</b>, then this method has no
     * effect. It must not contain <b>null</b> entries.
     *
     * @param lookups the {@code Lookup} objects to be added as default lookups
     * @throws IllegalArgumentException if the collection contains a <b>null</b>
     *         entry
     */
    public void addDefaultLookups(Collection<? extends Lookup> lookups)
    {
        if (lookups != null)
        {
            defaultLookups.addAll(lookups);
        }
    }

    /**
     * Removes the specified {@code Lookup} object from the list of default
     * {@code Lookup}s.
     *
     * @param lookup the {@code Lookup} object to be removed
     * @return a flag whether this {@code Lookup} object actually existed and
     *         was removed
     */
    public boolean removeDefaultLookup(Lookup lookup)
    {
        return defaultLookups.remove(lookup);
    }

    /**
     * Sets the parent {@code ConfigurationInterpolator}. This object is used if
     * the {@code Lookup} objects registered at this object cannot resolve a
     * variable.
     *
     * @param parentInterpolator the parent {@code ConfigurationInterpolator}
     *        object (can be <b>null</b>)
     */
    public void setParentInterpolator(
            ConfigurationInterpolator parentInterpolator)
    {
        this.parentInterpolator = parentInterpolator;
    }

    /**
     * Returns the parent {@code ConfigurationInterpolator}.
     *
     * @return the parent {@code ConfigurationInterpolator} (can be <b>null</b>)
     */
    public ConfigurationInterpolator getParentInterpolator()
    {
        return this.parentInterpolator;
    }

    /**
     * Sets a flag that variable names can contain other variables. If enabled,
     * variable substitution is also done in variable names.
     *
     * @return the substitution in variables flag
     */
    public boolean isEnableSubstitutionInVariables()
    {
        return substitutor.isEnableSubstitutionInVariables();
    }

    /**
     * Sets the flag whether variable names can contain other variables. This
     * flag corresponds to the {@code enableSubstitutionInVariables} property of
     * the underlying {@code StrSubstitutor} object.
     *
     * @param f the new value of the flag
     */
    public void setEnableSubstitutionInVariables(boolean f)
    {
        substitutor.setEnableSubstitutionInVariables(f);
    }

    /**
     * Performs interpolation of the passed in value. If the value is of type
     * String, this method checks whether it contains variables. If so, all
     * variables are replaced by their current values (if possible). For non
     * string arguments, the value is returned without changes.
     *
     * @param value the value to be interpolated
     * @return the interpolated value
     */
    public Object interpolate(Object value)
    {
        if (value instanceof String)
        {
            String strValue = (String) value;
            if (looksLikeSingleVariable(strValue))
            {
                Object resolvedValue = resolveSingleVariable(strValue);
                if (resolvedValue != null && !(resolvedValue instanceof String))
                {
                    // If the value is again a string, it needs no special
                    // treatment; it may also contain further variables which
                    // must be resolved; therefore, the default mechanism is
                    // applied.
                    return resolvedValue;
                }
            }
            return substitutor.replace(strValue);
        }
        return value;
    }

    /**
     * Resolves the specified variable. This implementation tries to extract
     * a variable prefix from the given variable name (the first colon (':') is
     * used as prefix separator). It then passes the name of the variable with
     * the prefix stripped to the lookup object registered for this prefix. If
     * no prefix can be found or if the associated lookup object cannot resolve
     * this variable, the default lookup objects are used. If this is not
     * successful either and a parent {@code ConfigurationInterpolator} is
     * available, this object is asked to resolve the variable.
     *
     * @param var the name of the variable whose value is to be looked up
     * @return the value of this variable or <b>null</b> if it cannot be
     * resolved
     */
    public Object resolve(String var)
    {
        if (var == null)
        {
            return null;
        }

        int prefixPos = var.indexOf(PREFIX_SEPARATOR);
        if (prefixPos >= 0)
        {
            String prefix = var.substring(0, prefixPos);
            String name = var.substring(prefixPos + 1);
            Object value = fetchLookupForPrefix(prefix).lookup(name);
            if (value != null)
            {
                return value;
            }
        }

        for (Lookup l : defaultLookups)
        {
            Object value = l.lookup(var);
            if (value != null)
            {
                return value;
            }
        }

        ConfigurationInterpolator parent = getParentInterpolator();
        if (parent != null)
        {
            return getParentInterpolator().resolve(var);
        }
        return null;
    }

    /**
     * Obtains the lookup object for the specified prefix. This method is called
     * by the {@code lookup()} method. This implementation will check
     * whether a lookup object is registered for the given prefix. If not, a
     * <b>null</b> lookup object will be returned (never <b>null</b>).
     *
     * @param prefix the prefix
     * @return the lookup object to be used for this prefix
     */
    protected Lookup fetchLookupForPrefix(String prefix)
    {
        return nullSafeLookup(prefixLookups.get(prefix));
    }

    /**
     * Creates and initializes a {@code StrSubstitutor} object which is used for
     * variable substitution. This {@code StrSubstitutor} is assigned a
     * specialized lookup object implementing the correct variable resolving
     * algorithm.
     *
     * @return the {@code StrSubstitutor} used by this object
     */
    private StrSubstitutor initSubstitutor()
    {
        return new StrSubstitutor(new StrLookup<Object>()
        {
            @Override
            public String lookup(String key)
            {
                Object result = resolve(key);
                return (result != null) ? result.toString() : null;
            }
        });
    }

    /**
     * Interpolates a string value that seems to be a single variable.
     *
     * @param strValue the string to be interpolated
     * @return the resolved value or <b>null</b> if resolving failed
     */
    private Object resolveSingleVariable(String strValue)
    {
        return resolve(extractVariableName(strValue));
    }

    /**
     * Checks whether a value to be interpolated seems to be a single variable.
     * In this case, it is resolved directly without using the
     * {@code StrSubstitutor}. Note that it is okay if this method returns a
     * false positive: In this case, resolving is going to fail, and standard
     * mechanism is used.
     *
     * @param strValue the value to be interpolated
     * @return a flag whether this value seems to be a single variable
     */
    private static boolean looksLikeSingleVariable(String strValue)
    {
        return strValue.startsWith(VAR_START) && strValue.endsWith(VAR_END);
    }

    /**
     * Extracts the variable name from a value that consists of a single
     * variable.
     *
     * @param strValue the value
     * @return the extracted variable name
     */
    private static String extractVariableName(String strValue)
    {
        return strValue.substring(VAR_START.length(),
                strValue.length() - VAR_END.length());
    }

    /**
     * Creates a new instance based on the properties in the given specification
     * object.
     *
     * @param spec the {@code InterpolatorSpecification}
     * @return the newly created instance
     */
    private static ConfigurationInterpolator createInterpolator(
            InterpolatorSpecification spec)
    {
        ConfigurationInterpolator ci = new ConfigurationInterpolator();
        ci.addDefaultLookups(spec.getDefaultLookups());
        ci.registerLookups(spec.getPrefixLookups());
        ci.setParentInterpolator(spec.getParentInterpolator());
        return ci;
    }

    static
    {
        Map<String, Lookup> lookups = new HashMap<>();
        for (DefaultLookups l : DefaultLookups.values())
        {
            lookups.put(l.getPrefix(), l.getLookup());
        }
        DEFAULT_PREFIX_LOOKUPS = Collections.unmodifiableMap(lookups);
    }
}
