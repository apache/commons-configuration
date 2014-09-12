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
 * @version $Id$
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
    private static <T> T createMock(Class<T> cls)
    {
        T mock = EasyMock.createMock(cls);
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
    private static void checkPrefixLookups(InterpolatorSpecification spec,
            Lookup prefLook1, Lookup prefLook2)
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
    private static void checkDefaultLookups(InterpolatorSpecification spec,
            Lookup defLook1, Lookup defLook2)
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
        Lookup prefLook1 = createLookup();
        Lookup prefLook2 = createLookup();
        Lookup defLook1 = createLookup();
        Lookup defLook2 = createLookup();
        ConfigurationInterpolator interpolator =
                createMock(ConfigurationInterpolator.class);
        ConfigurationInterpolator parent =
                createMock(ConfigurationInterpolator.class);
        InterpolatorSpecification spec =
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
        Lookup prefLook1 = createLookup();
        Lookup prefLook2 = createLookup();
        Lookup defLook1 = createLookup();
        Lookup defLook2 = createLookup();
        Map<String, Lookup> prefixLookups = new HashMap<String, Lookup>();
        prefixLookups.put(PREFIX1, prefLook1);
        prefixLookups.put(PREFIX2, prefLook2);
        InterpolatorSpecification spec =
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
        InterpolatorSpecification spec =
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
        InterpolatorSpecification spec =
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
        InterpolatorSpecification spec =
                builder.withPrefixLookup(PREFIX1, createLookup()).create();
        spec.getPrefixLookups().put(PREFIX1, createLookup());
    }

    /**
     * Tests that the collection with default lookups cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDefaultLookupsModify()
    {
        InterpolatorSpecification spec =
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
        Lookup prefLook1 = createLookup();
        Lookup prefLook2 = createLookup();
        Lookup defLook1 = createLookup();
        Lookup defLook2 = createLookup();
        ConfigurationInterpolator parent =
                createMock(ConfigurationInterpolator.class);
        InterpolatorSpecification spec =
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
