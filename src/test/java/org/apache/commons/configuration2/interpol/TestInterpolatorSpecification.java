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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code InterpolatorSpecification}.
 */
public class TestInterpolatorSpecification {
    /** Constant for a prefix for a prefix lookup. */
    private static final String PREFIX1 = "p1";

    /** Constant for another prefix for a prefix lookup. */
    private static final String PREFIX2 = "p2";

    /**
     * Checks whether the given test object contains the expected default lookups.
     *
     * @param spec the object to be tested
     * @param defLook1 default lookup 1
     * @param defLook2 default lookup 2
     */
    private static void checkDefaultLookups(final InterpolatorSpecification spec, final Lookup defLook1, final Lookup defLook2) {
        assertEquals(2, spec.getDefaultLookups().size());
        assertTrue(spec.getDefaultLookups().containsAll(Arrays.asList(defLook1, defLook2)));
    }

    /**
     * Checks whether the given test object contains the expected prefix lookups.
     *
     * @param spec the object to be tested
     * @param prefLook1 prefix lookup 1
     * @param prefLook2 prefix lookup 2
     */
    private static void checkPrefixLookups(final InterpolatorSpecification spec, final Lookup prefLook1, final Lookup prefLook2) {
        assertEquals(2, spec.getPrefixLookups().size());
        assertSame(prefLook1, spec.getPrefixLookups().get(PREFIX1));
        assertSame(prefLook2, spec.getPrefixLookups().get(PREFIX2));
    }

    /**
     * Convenience method for creating a mock lookup.
     *
     * @return the mock lookup
     */
    private static Lookup createLookup() {
        return mock(Lookup.class);
    }

    /** The builder for creating new instances. */
    private InterpolatorSpecification.Builder builder;

    @BeforeEach
    public void setUp() throws Exception {
        builder = new InterpolatorSpecification.Builder();
    }

    /**
     * Tests whether a builder can be reused.
     */
    @Test
    public void testBuilderReuse() {
        builder
            .withDefaultLookup(createLookup())
            .withInterpolator(mock(ConfigurationInterpolator.class))
            .withPrefixLookup("test", createLookup())
            .withParentInterpolator(mock(ConfigurationInterpolator.class))
            .withStringConverter(obj -> "test")
            .create();
        final Lookup prefLook1 = createLookup();
        final Lookup prefLook2 = createLookup();
        final Lookup defLook1 = createLookup();
        final Lookup defLook2 = createLookup();
        final ConfigurationInterpolator parent = mock(ConfigurationInterpolator.class);
        final Function<Object, String> stringConverter = Objects::toString;
        final InterpolatorSpecification spec = builder
            .withPrefixLookup(PREFIX1, prefLook1)
            .withPrefixLookup(PREFIX2, prefLook2)
            .withDefaultLookups(Arrays.asList(defLook1, defLook2))
            .withParentInterpolator(parent)
            .withStringConverter(stringConverter)
            .create();
        assertNull(spec.getInterpolator());
        assertSame(parent, spec.getParentInterpolator());
        assertSame(stringConverter, spec.getStringConverter());
        checkPrefixLookups(spec, prefLook1, prefLook2);
        checkDefaultLookups(spec, defLook1, defLook2);
    }

    /**
     * Tests whether an instance with all possible properties can be set.
     */
    @Test
    public void testCreateInstance() {
        final Lookup prefLook1 = createLookup();
        final Lookup prefLook2 = createLookup();
        final Lookup defLook1 = createLookup();
        final Lookup defLook2 = createLookup();
        final ConfigurationInterpolator interpolator = mock(ConfigurationInterpolator.class);
        final ConfigurationInterpolator parent = mock(ConfigurationInterpolator.class);
        final Function<Object, String> stringConverter = Objects::toString;
        final InterpolatorSpecification spec = builder
            .withPrefixLookup(PREFIX1, prefLook1)
            .withDefaultLookup(defLook1)
            .withPrefixLookup(PREFIX2, prefLook2)
            .withParentInterpolator(parent)
            .withDefaultLookup(defLook2)
            .withInterpolator(interpolator)
            .withStringConverter(stringConverter)
            .create();
        assertSame(interpolator, spec.getInterpolator());
        assertSame(parent, spec.getParentInterpolator());
        assertSame(stringConverter, spec.getStringConverter());
        checkPrefixLookups(spec, prefLook1, prefLook2);
        checkDefaultLookups(spec, defLook1, defLook2);
    }

    /**
     * Tests whether lookups can be set passing in full collections.
     */
    @Test
    public void testCreateInstanceCollections() {
        final Lookup prefLook1 = createLookup();
        final Lookup prefLook2 = createLookup();
        final Lookup defLook1 = createLookup();
        final Lookup defLook2 = createLookup();
        final Map<String, Lookup> prefixLookups = new HashMap<>();
        prefixLookups.put(PREFIX1, prefLook1);
        prefixLookups.put(PREFIX2, prefLook2);
        final InterpolatorSpecification spec = builder
            .withPrefixLookups(prefixLookups)
            .withDefaultLookups(Arrays.asList(defLook1, defLook2))
            .create();
        checkPrefixLookups(spec, prefLook1, prefLook2);
        checkDefaultLookups(spec, defLook1, defLook2);
    }

    /**
     * Tests that the collection with default lookups cannot be modified.
     */
    @Test
    public void testGetDefaultLookupsModify() {
        final InterpolatorSpecification spec = builder.withDefaultLookup(createLookup()).create();
        final Collection<Lookup> lookups = spec.getDefaultLookups();
        final Lookup lookup = createLookup();
        assertThrows(UnsupportedOperationException.class, () -> lookups.add(lookup));
    }

    /**
     * Tests that the map with prefix lookups cannot be modified.
     */
    @Test
    public void testGetPrefixLookupsModify() {
        final InterpolatorSpecification spec = builder.withPrefixLookup(PREFIX1, createLookup()).create();
        final Lookup lookup = createLookup();
        assertThrows(UnsupportedOperationException.class, () -> spec.getPrefixLookups().put(PREFIX1, lookup));
    }

    /**
     * Tests whether a null default lookup causes an exception.
     */
    @Test
    public void testWithDefaultLookupNull() {
        assertThrows(IllegalArgumentException.class, () -> builder.withDefaultLookup(null));
    }

    /**
     * Tests whether a null collection with default lookups is accepted.
     */
    @Test
    public void testWithDefaultLookupsNull() {
        final InterpolatorSpecification spec = builder.withDefaultLookups(null).create();
        assertTrue(spec.getDefaultLookups().isEmpty());
    }

    /**
     * Tests whether a null prefix lookup causes an exception.
     */
    @Test
    public void testWithPrefixLookupNoLookup() {
        assertThrows(IllegalArgumentException.class, () -> builder.withPrefixLookup(PREFIX1, null));
    }

    /**
     * Tests whether a null prefix causes an exception.
     */
    @Test
    public void testWithPrefixLookupNoPrefix() {
        final Lookup lookup = createLookup();
        assertThrows(IllegalArgumentException.class, () -> builder.withPrefixLookup(null, lookup));
    }

    /**
     * Tests whether a null map with prefix lookups is accepted.
     */
    @Test
    public void testWithPrefixLookupsNull() {
        final InterpolatorSpecification spec = builder.withPrefixLookups(null).create();
        assertTrue(spec.getPrefixLookups().isEmpty());
    }
}
