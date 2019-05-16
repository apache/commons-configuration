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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code InterpolatorSpecification}.
 *
 */
public class TestInterpolatorSpecification
{
    /** Constant for a prefix for a prefix lookup. */
    private static final String PREFIX1 = "p1";

    /** Constant for another prefix for a prefix lookup. */
    private static final String PREFIX2 = "p2";

    /** The builder for creating new instances. */
    private InterpolatorSpecification.Builder builder;

    @Before
    public void setUp() throws Exception
    {
        builder = new InterpolatorSpecification.Builder();
    }

    /**
     * Convenience method for creating a mock object.
     *
     * @param cls the class of the mock
     * @param <T> the type of the mock
     * @return the mock
     */
    private static <T> T createMock(final Class<T> cls)
    {
        final T mock = EasyMock.createMock(cls);
        EasyMock.replay(mock);
        return mock;
    }

    /**
     * Convenience method for creating a mock lookup.
     *
     * @return the mock lookup
     */
    private static Lookup createLookup()
    {
        return createMock(Lookup.class);
    }

    /**
     * Checks whether the given test object contains the expected prefix
     * lookups.
     *
     * @param spec the object to be tested
     * @param prefLook1 prefix lookup 1
     * @param prefLook2 prefix lookup 2
     */
    private static void checkPrefixLookups(final InterpolatorSpecification spec,
            final Lookup prefLook1, final Lookup prefLook2)
    {
        assertEquals("Wrong number of prefix lookups", 2, spec
                .getPrefixLookups().size());
        assertSame("Wrong prefix lookup 1", prefLook1, spec.getPrefixLookups()
                .get(PREFIX1));
        assertSame("Wrong prefix lookup 2", prefLook2, spec.getPrefixLookups()
                .get(PREFIX2));
    }

    /**
     * Checks whether the given test object contains the expected default
     * lookups.
     *
     * @param spec the object to be tested
     * @param defLook1 default lookup 1
     * @param defLook2 default lookup 2
     */
    private static void checkDefaultLookups(final InterpolatorSpecification spec,
            final Lookup defLook1, final Lookup defLook2)
    {
        assertEquals("Wrong number of default lookups", 2, spec
                .getDefaultLookups().size());
        assertTrue("Wrong default lookups", spec.getDefaultLookups()
                .containsAll(Arrays.asList(defLook1, defLook2)));
    }

    /**
     * Tests whether an instance with all possible properties can be set.
     */
    @Test
    public void testCreateInstance()
    {
        final Lookup prefLook1 = createLookup();
        final Lookup prefLook2 = createLookup();
        final Lookup defLook1 = createLookup();
        final Lookup defLook2 = createLookup();
        final ConfigurationInterpolator interpolator =
                createMock(ConfigurationInterpolator.class);
        final ConfigurationInterpolator parent =
                createMock(ConfigurationInterpolator.class);
        final InterpolatorSpecification spec =
                builder.withPrefixLookup(PREFIX1, prefLook1)
                        .withDefaultLookup(defLook1)
                        .withPrefixLookup(PREFIX2, prefLook2)
                        .withParentInterpolator(parent)
                        .withDefaultLookup(defLook2)
                        .withInterpolator(interpolator).create();
        assertSame("Wrong interpolator", interpolator, spec.getInterpolator());
        assertSame("Wrong parent interpolator", parent,
                spec.getParentInterpolator());
        checkPrefixLookups(spec, prefLook1, prefLook2);
        checkDefaultLookups(spec, defLook1, defLook2);
    }

    /**
     * Tests whether lookups can be set passing in full collections.
     */
    @Test
    public void testCreateInstanceCollections()
    {
        final Lookup prefLook1 = createLookup();
        final Lookup prefLook2 = createLookup();
        final Lookup defLook1 = createLookup();
        final Lookup defLook2 = createLookup();
        final Map<String, Lookup> prefixLookups = new HashMap<>();
        prefixLookups.put(PREFIX1, prefLook1);
        prefixLookups.put(PREFIX2, prefLook2);
        final InterpolatorSpecification spec =
                builder.withPrefixLookups(prefixLookups)
                        .withDefaultLookups(Arrays.asList(defLook1, defLook2))
                        .create();
        checkPrefixLookups(spec, prefLook1, prefLook2);
        checkDefaultLookups(spec, defLook1, defLook2);
    }

    /**
     * Tests whether a null map with prefix lookups is accepted.
     */
    @Test
    public void testWithPrefixLookupsNull()
    {
        final InterpolatorSpecification spec =
                builder.withPrefixLookups(null).create();
        assertTrue("No empty map with prefix lookups", spec.getPrefixLookups()
                .isEmpty());
    }

    /**
     * Tests whether a null collection with default lookups is accepted.
     */
    @Test
    public void testWithDefaultLookupsNull()
    {
        final InterpolatorSpecification spec =
                builder.withDefaultLookups(null).create();
        assertTrue("No empty default lookups collection", spec
                .getDefaultLookups().isEmpty());
    }

    /**
     * Tests whether a null prefix causes an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWithPrefixLookupNoPrefix()
    {
        builder.withPrefixLookup(null, createLookup());
    }

    /**
     * Tests whether a null prefix lookup causes an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWithPrefixLookupNoLookup()
    {
        builder.withPrefixLookup(PREFIX1, null);
    }

    /**
     * Tests whether a null default lookup causes an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWithDefaultLookupNull()
    {
        builder.withDefaultLookup(null);
    }

    /**
     * Tests that the map with prefix lookups cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetPrefixLookupsModify()
    {
        final InterpolatorSpecification spec =
                builder.withPrefixLookup(PREFIX1, createLookup()).create();
        spec.getPrefixLookups().put(PREFIX1, createLookup());
    }

    /**
     * Tests that the collection with default lookups cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDefaultLookupsModify()
    {
        final InterpolatorSpecification spec =
                builder.withDefaultLookup(createLookup()).create();
        spec.getDefaultLookups().add(createLookup());
    }

    /**
     * Tests whether a builder can be reused.
     */
    @Test
    public void testBuilderReuse()
    {
        builder.withDefaultLookup(createLookup())
                .withInterpolator(createMock(ConfigurationInterpolator.class))
                .withPrefixLookup("test", createLookup())
                .withParentInterpolator(
                        createMock(ConfigurationInterpolator.class)).create();
        final Lookup prefLook1 = createLookup();
        final Lookup prefLook2 = createLookup();
        final Lookup defLook1 = createLookup();
        final Lookup defLook2 = createLookup();
        final ConfigurationInterpolator parent =
                createMock(ConfigurationInterpolator.class);
        final InterpolatorSpecification spec =
                builder.withPrefixLookup(PREFIX1, prefLook1)
                        .withPrefixLookup(PREFIX2, prefLook2)
                        .withDefaultLookups(Arrays.asList(defLook1, defLook2))
                        .withParentInterpolator(parent).create();
        assertNull("Got an interpolator", spec.getInterpolator());
        assertSame("Wrong parent interpolator", parent,
                spec.getParentInterpolator());
        checkPrefixLookups(spec, prefLook1, prefLook2);
        checkDefaultLookups(spec, defLook1, defLook2);
    }
}
