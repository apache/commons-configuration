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
import java.util.LinkedList;
import java.util.Map;

/**
 * <p>
 * A simple value class defining a {@link ConfigurationInterpolator}.
 * </p>
 * <p>
 * Objects of this class can be used for creating new
 * {@code ConfigurationInterpolator} instances; they contain all required
 * properties. It is either possible to set a fully initialized
 * {@code ConfigurationInterpolator} directly which can be used as is.
 * Alternatively, some or all properties of an instance to be newly created can
 * be set. These properties include
 * </p>
 * <ul>
 * <li>a map with {@code Lookup} objects associated with a specific prefix</li>
 * <li>a collection with default {@code Lookup} objects (without a prefix)</li>
 * <li>a parent {@code ConfigurationInterpolator}</li>
 * </ul>
 * <p>
 * When setting up a configuration it is possible to define the
 * {@code ConfigurationInterpolator} in terms of this class. The configuration
 * will then either use the {@code ConfigurationInterpolator} instance
 * explicitly defined in the {@code InterpolatorSpecification} instance or
 * create a new one.
 * </p>
 * <p>
 * Instances are not created directly, but using the nested {@code Builder}
 * class. They are then immutable.
 * </p>
 *
 * @since 2.0
 */
public final class InterpolatorSpecification
{
    /** The {@code ConfigurationInterpolator} instance to be used directly. */
    private final ConfigurationInterpolator interpolator;

    /** The parent {@code ConfigurationInterpolator}. */
    private final ConfigurationInterpolator parentInterpolator;

    /** The map with prefix lookups. */
    private final Map<String, Lookup> prefixLookups;

    /** The collection with default lookups. */
    private final Collection<Lookup> defaultLookups;

    /**
     * Creates a new instance of {@code InterpolatorSpecification} with the
     * properties defined by the given builder object.
     *
     * @param builder the builder
     */
    private InterpolatorSpecification(final Builder builder)
    {
        interpolator = builder.interpolator;
        parentInterpolator = builder.parentInterpolator;
        prefixLookups =
                Collections.unmodifiableMap(new HashMap<>(
                        builder.prefixLookups));
        defaultLookups =
                Collections.unmodifiableCollection(new ArrayList<>(
                        builder.defLookups));
    }

    /**
     * Returns the {@code ConfigurationInterpolator} instance to be used
     * directly.
     *
     * @return the {@code ConfigurationInterpolator} (can be <b>null</b>)
     */
    public ConfigurationInterpolator getInterpolator()
    {
        return interpolator;
    }

    /**
     * Returns the parent {@code ConfigurationInterpolator} object.
     *
     * @return the parent {@code ConfigurationInterpolator} (can be <b>null</b>)
     */
    public ConfigurationInterpolator getParentInterpolator()
    {
        return parentInterpolator;
    }

    /**
     * Returns a map with prefix lookups. The keys of the map are the prefix
     * strings, its values are the corresponding {@code Lookup} objects.
     *
     * @return the prefix lookups for a new {@code ConfigurationInterpolator}
     *         instance (never <b>null</b>)
     */
    public Map<String, Lookup> getPrefixLookups()
    {
        return prefixLookups;
    }

    /**
     * Returns a collection with the default lookups.
     *
     * @return the default lookups for a new {@code ConfigurationInterpolator}
     *         instance (never <b>null</b>)
     */
    public Collection<Lookup> getDefaultLookups()
    {
        return defaultLookups;
    }

    /**
     * <p>A <em>builder</em> class for creating instances of
     * {@code InterpolatorSpecification}.</p>
     * <p>
     * This class provides a fluent API for defining the various properties of
     * an {@code InterpolatorSpecification} object. <em>Note:</em> This builder
     * class is not thread-safe.
     * </p>
     */
    public static class Builder
    {
        /** A map with prefix lookups. */
        private final Map<String, Lookup> prefixLookups;

        /** A collection with default lookups. */
        private final Collection<Lookup> defLookups;

        /** The {@code ConfigurationInterpolator}. */
        private ConfigurationInterpolator interpolator;

        /** The parent {@code ConfigurationInterpolator}. */
        private ConfigurationInterpolator parentInterpolator;

        public Builder()
        {
            prefixLookups = new HashMap<>();
            defLookups = new LinkedList<>();
        }

        /**
         * Adds a {@code Lookup} object for a given prefix.
         *
         * @param prefix the prefix (must not be <b>null</b>)
         * @param lookup the {@code Lookup} (must not be <b>null</b>)
         * @return a reference to this builder for method chaining
         * @throws IllegalArgumentException if a required parameter is missing
         */
        public Builder withPrefixLookup(final String prefix, final Lookup lookup)
        {
            if (prefix == null)
            {
                throw new IllegalArgumentException("Prefix must not be null!");
            }
            checkLookup(lookup);
            prefixLookups.put(prefix, lookup);
            return this;
        }

        /**
         * Adds the content of the given map to the prefix lookups managed by
         * this builder. The map can be <b>null</b>, then this method has no
         * effect.
         *
         * @param lookups the map with prefix lookups to be added
         * @return a reference to this builder for method chaining
         * @throws IllegalArgumentException if the map contains <b>null</b>
         *         values
         */
        public Builder withPrefixLookups(final Map<String, ? extends Lookup> lookups)
        {
            if (lookups != null)
            {
                for (final Map.Entry<String, ? extends Lookup> e : lookups.entrySet())
                {
                    withPrefixLookup(e.getKey(), e.getValue());
                }
            }
            return this;
        }

        /**
         * Adds the given {@code Lookup} object to the list of default lookups.
         *
         * @param lookup the {@code Lookup} (must not be <b>null</b>)
         * @return a reference to this builder for method chaining
         * @throws IllegalArgumentException if the {@code Lookup} is <b>null</b>
         */
        public Builder withDefaultLookup(final Lookup lookup)
        {
            checkLookup(lookup);
            defLookups.add(lookup);
            return this;
        }

        /**
         * Adds the content of the given collection to the default lookups
         * managed by this builder. The collection can be <b>null</b>, then this
         * method has no effect.
         *
         * @param lookups the collection with lookups to be added
         * @return a reference to this builder for method chaining
         * @throws IllegalArgumentException if the collection contains
         *         <b>null</b> entries
         */
        public Builder withDefaultLookups(final Collection<? extends Lookup> lookups)
        {
            if (lookups != null)
            {
                for (final Lookup l : lookups)
                {
                    withDefaultLookup(l);
                }
            }
            return this;
        }

        /**
         * Sets the {@code ConfigurationInterpolator} instance for the
         * {@code InterpolatorSpecification}. This means that a
         * {@code ConfigurationInterpolator} has been created and set up
         * externally and can be used directly.
         *
         * @param ci the {@code ConfigurationInterpolator} (can be <b>null</b>)
         * @return a reference to this builder for method chaining
         */
        public Builder withInterpolator(final ConfigurationInterpolator ci)
        {
            interpolator = ci;
            return this;
        }

        /**
         * Sets an optional parent {@code ConfigurationInterpolator}. If
         * defined, this object is set as parent of a newly created
         * {@code ConfigurationInterpolator} instance.
         *
         * @param parent the parent {@code ConfigurationInterpolator} (can be
         *        <b>null</b>)
         * @return a reference to this builder for method chaining
         */
        public Builder withParentInterpolator(final ConfigurationInterpolator parent)
        {
            parentInterpolator = parent;
            return this;
        }

        /**
         * Creates a new {@code InterpolatorSpecification} instance with the
         * properties set so far. After that this builder instance is reset so
         * that it can be reused for creating further specification objects.
         *
         * @return the newly created {@code InterpolatorSpecification}
         */
        public InterpolatorSpecification create()
        {
            final InterpolatorSpecification spec =
                    new InterpolatorSpecification(this);
            reset();
            return spec;
        }

        /**
         * Removes all data from this builder. Afterwards it can be used to
         * define a brand new {@code InterpolatorSpecification} object.
         */
        public void reset()
        {
            interpolator = null;
            parentInterpolator = null;
            prefixLookups.clear();
            defLookups.clear();
        }

        /**
         * Helper method for checking a lookup. Throws an exception if the
         * lookup is <b>null</b>.
         *
         * @param lookup the lookup to be checked
         * @throws IllegalArgumentException if the lookup is <b>null</b>
         */
        private static void checkLookup(final Lookup lookup)
        {
            if (lookup == null)
            {
                throw new IllegalArgumentException("Lookup must not be null!");
            }
        }
    }
}
