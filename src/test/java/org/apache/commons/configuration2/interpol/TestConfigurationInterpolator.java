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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.text.lookup.StringLookupFactory;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for ConfigurationInterpolator.
 *
 */
public class TestConfigurationInterpolator {
    /** Constant for a test variable name. */
    private static final String TEST_NAME = "varname";

    /** Constant for a test variable prefix. */
    private static final String TEST_PREFIX = "prefix";

    /** Constant for the value of the test variable. */
    private static final String TEST_VALUE = "TestVariableValue";

    /**
     * Creates a lookup object that can resolve the test variable (and nothing else).
     *
     * @return the test lookup object
     */
    private static Lookup setUpTestLookup() {
        return setUpTestLookup(TEST_NAME, TEST_VALUE);
    }

    /**
     * Creates a lookup object that can resolve the specified variable (and nothing else).
     *
     * @param var the variable name
     * @param value the value of this variable
     * @return the test lookup object
     */
    private static Lookup setUpTestLookup(final String var, final Object value) {
        final Lookup lookup = EasyMock.createMock(Lookup.class);
        EasyMock.expect(lookup.lookup(EasyMock.anyObject(String.class))).andAnswer(() -> {
            if (var.equals(EasyMock.getCurrentArguments()[0])) {
                return value;
            }
            return null;
        }).anyTimes();
        EasyMock.replay(lookup);
        return lookup;
    }

    /** Stores the object to be tested. */
    private ConfigurationInterpolator interpolator;

    @BeforeEach
    public void setUp() throws Exception {
        interpolator = new ConfigurationInterpolator();
    }

    /**
     * Tests whether multiple default lookups can be added.
     */
    @Test
    public void testAddDefaultLookups() {
        final List<Lookup> lookups = new ArrayList<>();
        lookups.add(setUpTestLookup());
        lookups.add(setUpTestLookup("test", "value"));
        interpolator.addDefaultLookups(lookups);
        final List<Lookup> lookups2 = interpolator.getDefaultLookups();
        assertEquals(2, lookups2.size(), "Wrong number of default lookups");
        assertTrue(lookups2.containsAll(lookups), "Wrong content");
    }

    /**
     * Tests whether a null collection of default lookups is handled correctly.
     */
    @Test
    public void testAddDefaultLookupsNull() {
        interpolator.addDefaultLookups(null);
        assertTrue(interpolator.getDefaultLookups().isEmpty(), "Got default lookups");
    }

    /**
     * Tests deregistering a lookup object.
     */
    @Test
    public void testDeregisterLookup() {
        final Lookup lookup = EasyMock.createMock(Lookup.class);
        EasyMock.replay(lookup);
        interpolator.registerLookup(TEST_PREFIX, lookup);
        assertTrue(interpolator.deregisterLookup(TEST_PREFIX), "Derigstration not successfull");
        assertFalse(interpolator.prefixSet().contains(TEST_PREFIX), "Deregistered prefix still contained");
        assertTrue(interpolator.getLookups().isEmpty(), "Lookups not empty");
    }

    /**
     * Tests deregistering an unknown lookup object.
     */
    @Test
    public void testDeregisterLookupNonExisting() {
        assertFalse(interpolator.deregisterLookup(TEST_PREFIX), "Could deregister unknown lookup");
    }

    /**
     * Tests whether the flag for substitution in variable names can be modified.
     */
    @Test
    public void testEnableSubstitutionInVariables() {
        assertFalse(interpolator.isEnableSubstitutionInVariables(), "Variable substitution enabled");
        interpolator.addDefaultLookup(setUpTestLookup("java.version", "1.4"));
        interpolator.addDefaultLookup(setUpTestLookup("jre-1.4", "C:\\java\\1.4"));
        final String var = "${jre-${java.version}}";
        assertEquals(var, interpolator.interpolate(var), "Wrong result (1)");
        interpolator.setEnableSubstitutionInVariables(true);
        assertTrue(interpolator.isEnableSubstitutionInVariables(), "Variable substitution not enabled");
        assertEquals("C:\\java\\1.4", interpolator.interpolate(var), "Wrong result (2)");
    }

    /**
     * Tests fromSpecification() if the specification contains an instance.
     */
    @Test
    public void testFromSpecificationInterpolator() {
        final ConfigurationInterpolator ci = EasyMock.createMock(ConfigurationInterpolator.class);
        EasyMock.replay(ci);
        final InterpolatorSpecification spec = new InterpolatorSpecification.Builder().withDefaultLookup(EasyMock.createMock(Lookup.class))
            .withParentInterpolator(interpolator).withInterpolator(ci).create();
        assertSame(ci, ConfigurationInterpolator.fromSpecification(spec), "Wrong result");
    }

    /**
     * Tests fromSpecification() if a new instance has to be created.
     */
    @Test
    public void testFromSpecificationNewInstance() {
        final Lookup defLookup = EasyMock.createMock(Lookup.class);
        final Lookup preLookup = EasyMock.createMock(Lookup.class);
        EasyMock.replay(defLookup, preLookup);
        final Function<Object, String> stringConverter = obj -> Objects.toString(obj, null);
        final InterpolatorSpecification spec = new InterpolatorSpecification.Builder()
            .withDefaultLookup(defLookup)
            .withPrefixLookup("p", preLookup)
            .withParentInterpolator(interpolator)
            .withStringConverter(stringConverter)
            .create();
        final ConfigurationInterpolator ci = ConfigurationInterpolator.fromSpecification(spec);
        assertEquals(1, ci.getDefaultLookups().size(), "Wrong number of default lookups");
        assertTrue(ci.getDefaultLookups().contains(defLookup), "Wrong default lookup");
        assertEquals(1, ci.getLookups().size(), "Wrong number of prefix lookups");
        assertSame(preLookup, ci.getLookups().get("p"), "Wrong prefix lookup");
        assertSame(interpolator, ci.getParentInterpolator(), "Wrong parent");
        assertSame(stringConverter, ci.getStringConverter(), "Wrong string converter");
    }

    /**
     * Tries to obtain an instance from a null specification.
     */
    @Test
    public void testFromSpecificationNull() {
        assertThrows(IllegalArgumentException.class, () -> ConfigurationInterpolator.fromSpecification(null));
    }

    /**
     * Tests whether modification of the list of default lookups does not affect the object.
     */
    @Test
    public void testGetDefaultLookupsModify() {
        final List<Lookup> lookups = interpolator.getDefaultLookups();
        lookups.add(setUpTestLookup());
        assertTrue(interpolator.getDefaultLookups().isEmpty(), "List was modified");
    }

    /**
     * Tests whether default prefix lookups can be queried as a map.
     */
    @Test
    public void testGetDefaultPrefixLookups() {
        final EnumSet<DefaultLookups> excluded = EnumSet.of(
                DefaultLookups.DNS,
                DefaultLookups.URL,
                DefaultLookups.SCRIPT);

        final EnumSet<DefaultLookups> included = EnumSet.complementOf(excluded);

        final Map<String, Lookup> lookups = ConfigurationInterpolator.getDefaultPrefixLookups();

        assertEquals(included.size(), lookups.size(), "Wrong number of lookups");
        for (final DefaultLookups l : included) {
            assertSame(l.getLookup(), lookups.get(l.getPrefix()), "Wrong entry for " + l);
        }

        for (final DefaultLookups l : excluded) {
            assertNull(lookups.get(l.getPrefix()), "Unexpected entry for " + l);
        }
    }

    /**
     * Tests that the map with default lookups cannot be modified.
     */
    @Test
    public void testGetDefaultPrefixLookupsModify() {
        final Map<String, Lookup> lookups = ConfigurationInterpolator.getDefaultPrefixLookups();
        final Lookup lookup = EasyMock.createMock(Lookup.class);
        assertThrows(UnsupportedOperationException.class, () -> lookups.put("test", lookup));
    }

    /**
     * Tests that modification of the map with lookups does not affect the object.
     */
    @Test
    public void testGetLookupsModify() {
        final Map<String, Lookup> lookups = interpolator.getLookups();
        lookups.put(TEST_PREFIX, setUpTestLookup());
        assertTrue(interpolator.getLookups().isEmpty(), "Map was modified");
    }

    /**
     * Tests that a custom string converter can be used.
     */
    @Test
    public void testSetStringConverter() {
        final Function<Object, String> stringConverter = obj -> "'" + obj + "'";
        interpolator.addDefaultLookup(setUpTestLookup("x", Arrays.asList(1, 2)));
        interpolator.addDefaultLookup(setUpTestLookup("y", "abc"));
        interpolator.setStringConverter(stringConverter);
        assertSame(stringConverter, interpolator.getStringConverter(), "Wrong string converter");
        assertEquals("'abc': '[1, 2]'", interpolator.interpolate("${y}: ${x}"), "Wrong value");
    }

    /**
     * Tests that the default string converter can be reapplied by passing {@code null}.
     */
    @Test
    public void testSetStringConverterNullArgumentUsesDefault() {
        final Function<Object, String> stringConverter = obj -> "'" + obj + "'";
        interpolator.addDefaultLookup(setUpTestLookup("x", Arrays.asList(1, 2)));
        interpolator.addDefaultLookup(setUpTestLookup("y", "abc"));
        interpolator.setStringConverter(stringConverter);
        interpolator.setStringConverter(null);
        assertNotSame(stringConverter, interpolator.getStringConverter(), "Wrong string converter");
        assertEquals("abc: 1", interpolator.interpolate("${y}: ${x}"), "Wrong value");
    }

    /**
     * Tests creating an instance. Does it contain some predefined lookups and a default string converter?
     */
    @Test
    public void testInit() {
        assertTrue(interpolator.getDefaultLookups().isEmpty(), "A default lookup is set");
        assertTrue(interpolator.getLookups().isEmpty(), "Got predefined lookups");
        assertNull(interpolator.getParentInterpolator(), "Got a parent interpolator");
        assertNotNull(interpolator.getStringConverter(), "Missing string converter");
        assertEquals("1", interpolator.getStringConverter().apply(Arrays.asList(1, 2)), "Incorrect string converter value");
    }

    /**
     * Tests that an empty variable definition does not cause problems.
     */
    @Test
    public void testInterpolateEmptyVariable() {
        final String value = "${}";
        assertEquals(value, interpolator.interpolate(value), "Wrong result");
    }

    /**
     * Tests that a blank variable definition does not cause problems.
     */
    @Test
    public void testInterpolateBlankVariable() {
        final String value = "${ }";
        assertEquals(value, interpolator.interpolate(value), "Wrong result");
    }

    /**
     * Tests interpolation of a non string argument.
     */
    @Test
    public void testInterpolateObject() {
        final Object value = 42;
        assertSame(value, interpolator.interpolate(value), "Value was changed");
    }

    /**
     * Tests interpolation of a collection argument.
     */
    @Test
    public void testInterpolateCollection() {
        final List<Integer> value = Arrays.asList(1, 2);
        assertSame(value, interpolator.interpolate(value), "Value was changed");
    }

    /**
     * Tests interpolation of an array argument.
     */
    @Test
    public void testInterpolateArray() {
        final int[] value = {1, 2};
        assertSame(value, interpolator.interpolate(value), "Value was changed");
    }

    /**
     * Tests a successful interpolation of a string value.
     */
    @Test
    public void testInterpolateString() {
        final String value = "${" + TEST_PREFIX + ':' + TEST_NAME + "}";
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup());
        assertEquals(TEST_VALUE, interpolator.interpolate(value), "Wrong result");
    }

    /**
     * Tests interpolation with a variable which cannot be resolved.
     */
    @Test
    public void testInterpolateStringUnknownVariable() {
        final String value = "${unknownVariable}";
        assertEquals(value, interpolator.interpolate(value), "Wrong result");
    }

    /**
     * Tests a property value consisting of multiple variables.
     */
    @Test
    public void testInterpolationMultipleVariables() {
        final String value = "The ${subject} jumps over ${object}.";
        interpolator.addDefaultLookup(setUpTestLookup("subject", "quick brown fox"));
        interpolator.addDefaultLookup(setUpTestLookup("object", "the lazy dog"));
        assertEquals("The quick brown fox jumps over the lazy dog.", interpolator.interpolate(value), "Wrong result");
    }

    /**
     * Tests interpolation with variables containing multiple simple non-string variables.
     */
    @Test
    public void testInterpolationMultipleSimpleNonStringVariables() {
        final String value = "${x} = ${y} is ${result}";
        interpolator.addDefaultLookup(setUpTestLookup("x", 1));
        interpolator.addDefaultLookup(setUpTestLookup("y", 2));
        interpolator.addDefaultLookup(setUpTestLookup("result", false));
        assertEquals("1 = 2 is false", interpolator.interpolate(value), "Wrong result");
    }

    /**
     * Tests interpolation with multiple variables containing collections and iterators.
     */
    @Test
    public void testInterpolationMultipleCollectionVariables() {
        final String value = "${single}bc${multi}23${empty}${null}${multiIt}${emptyIt}${nullIt}";
        final List<Integer> multi = Arrays.asList(1, 0, 0);
        final List<String> single = Arrays.asList("a");
        final List<Object> empty = Collections.emptyList();
        final List<Object> containsNull = Arrays.asList((Object) null);
        interpolator.addDefaultLookup(setUpTestLookup("multi", multi));
        interpolator.addDefaultLookup(setUpTestLookup("multiIt", multi.iterator()));
        interpolator.addDefaultLookup(setUpTestLookup("single", single));
        interpolator.addDefaultLookup(setUpTestLookup("empty", empty));
        interpolator.addDefaultLookup(setUpTestLookup("emptyIt", empty.iterator()));
        interpolator.addDefaultLookup(setUpTestLookup("null", containsNull));
        interpolator.addDefaultLookup(setUpTestLookup("nullIt", containsNull.iterator()));
        assertEquals("abc123${empty}${null}1${emptyIt}${nullIt}", interpolator.interpolate(value), "Wrong result");
    }

    /**
     * Tests interpolation with multiple variables containing arrays.
     */
    @Test
    public void testInterpolationMultipleArrayVariables() {
        final String value = "${single}bc${multi}23${empty}${null}";
        final int[] multi = {1, 0, 0};
        final String[] single = {"a"};
        final int[] empty = {};
        final Object[] containsNull = {null};
        interpolator.addDefaultLookup(setUpTestLookup("multi", multi));
        interpolator.addDefaultLookup(setUpTestLookup("single", single));
        interpolator.addDefaultLookup(setUpTestLookup("empty", empty));
        interpolator.addDefaultLookup(setUpTestLookup("null", containsNull));
        assertEquals("abc123${empty}${null}", interpolator.interpolate(value), "Wrong result");
    }

    /**
     * Tests an interpolation that consists of a single variable only. The variable's value should be returned verbatim.
     */
    @Test
    public void testInterpolationSingleVariable() {
        final Object value = 42;
        interpolator.addDefaultLookup(setUpTestLookup(TEST_NAME, value));
        assertEquals(value, interpolator.interpolate("${" + TEST_NAME + "}"), "Wrong result");
    }

    /**
     * Tests an interpolation that consists of a single collection variable only. The variable's value
     * should be returned verbatim.
     */
    @Test
    public void testInterpolationSingleCollectionVariable() {
        final List<Integer> value = Arrays.asList(42);
        interpolator.addDefaultLookup(setUpTestLookup(TEST_NAME, value));
        assertEquals(value, interpolator.interpolate("${" + TEST_NAME + "}"), "Wrong result");
    }

    /**
     * Tests an interpolation that consists of a single array variable only. The variable's value
     * should be returned verbatim.
     */
    @Test
    public void testInterpolationSingleArrayVariable() {
        final int[] value = {42, -1};
        interpolator.addDefaultLookup(setUpTestLookup(TEST_NAME, value));
        assertEquals(value, interpolator.interpolate("${" + TEST_NAME + "}"), "Wrong result");
    }

    /**
     * Tests an interpolation that consists of a single undefined variable only with and without a default value.
     */
    @Test
    public void testInterpolationSingleVariableDefaultValue() {
        final Object value = 42;
        interpolator.addDefaultLookup(setUpTestLookup(TEST_NAME, value));
        assertEquals("${I_am_not_defined}", interpolator.interpolate("${I_am_not_defined}"), "Wrong result");
        assertEquals("42", interpolator.interpolate("${I_am_not_defined:-42}"), "Wrong result");
        assertEquals("", interpolator.interpolate("${I_am_not_defined:-}"), "Wrong result");
    }

    /**
     * Tests a variable declaration which lacks the trailing closing bracket.
     */
    @Test
    public void testInterpolationVariableIncomplete() {
        final String value = "${" + TEST_NAME;
        interpolator.addDefaultLookup(setUpTestLookup(TEST_NAME, "someValue"));
        assertEquals(value, interpolator.interpolate(value), "Wrong result");
    }

    /**
     * Tests an interpolated string that begins and ends with variable lookups that have
     * the potential to fail. Part of CONFIGURATION-764.
     */
    @Test
    public void testInterpolationBeginningAndEndingRiskyVariableLookups() {
        interpolator.registerLookups(ConfigurationInterpolator.getDefaultPrefixLookups());
        final String result = (String) interpolator.interpolate("${date:yyyy-MM}-${date:dd}");
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"), "Wrong result: " + result);
    }

    /**
     * Tests nullSafeLookup() if a lookup object was provided.
     */
    @Test
    public void testNullSafeLookupExisting() {
        final Lookup look = EasyMock.createMock(Lookup.class);
        EasyMock.replay(look);
        assertSame(look, ConfigurationInterpolator.nullSafeLookup(look), "Wrong result");
    }

    /**
     * Tests whether nullSafeLookup() can handle null input.
     */
    @Test
    public void testNullSafeLookupNull() {
        final Lookup lookup = ConfigurationInterpolator.nullSafeLookup(null);
        assertNull(lookup.lookup("someVar"), "Got a lookup result");
    }

    /**
     * Tests that the prefix set cannot be modified.
     */
    @Test
    public void testPrefixSetModify() {
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup());
        final Iterator<String> it = interpolator.prefixSet().iterator();
        it.next();
        assertThrows(UnsupportedOperationException.class, it::remove);
    }

    /**
     * Tests registering a lookup object at an instance.
     */
    @Test
    public void testRegisterLookup() {
        final Lookup lookup = EasyMock.createMock(Lookup.class);
        EasyMock.replay(lookup);
        interpolator.registerLookup(TEST_PREFIX, lookup);
        assertSame(lookup, interpolator.getLookups().get(TEST_PREFIX), "New lookup not registered");
        assertTrue(interpolator.prefixSet().contains(TEST_PREFIX), "Not in prefix set");
        assertTrue(interpolator.getDefaultLookups().isEmpty(), "Default lookups were changed");
    }

    /**
     * Tests registering a null lookup object. This should cause an exception.
     */
    @Test
    public void testRegisterLookupNull() {
        assertThrows(IllegalArgumentException.class, () -> interpolator.registerLookup(TEST_PREFIX, null));
    }

    /**
     * Tests registering a lookup object for an undefined prefix. This should cause an exception.
     */
    @Test
    public void testRegisterLookupNullPrefix() {
        final Lookup lookup = EasyMock.createMock(Lookup.class);
        assertThrows(IllegalArgumentException.class, () -> interpolator.registerLookup(null, lookup));
    }

    /**
     * Tests whether a map with lookup objects can be registered.
     */
    @Test
    public void testRegisterLookups() {
        final Lookup l1 = setUpTestLookup();
        final Lookup l2 = setUpTestLookup("someVar", "someValue");
        final Map<String, Lookup> lookups = new HashMap<>();
        lookups.put(TEST_PREFIX, l1);
        final String prefix2 = TEST_PREFIX + "_other";
        lookups.put(prefix2, l2);
        interpolator.registerLookups(lookups);
        final Map<String, Lookup> lookups2 = interpolator.getLookups();
        assertEquals(2, lookups2.size(), "Wrong number of lookups");
        assertEquals(l1, lookups2.get(TEST_PREFIX), "Wrong l1");
        assertEquals(l2, lookups2.get(prefix2), "Wrong l2");
    }

    /**
     * Tests whether a null map with lookup objects is handled correctly.
     */
    @Test
    public void testRegisterLookupsNull() {
        interpolator.registerLookups(null);
        assertTrue(interpolator.getLookups().isEmpty(), "Got lookups");
    }

    /**
     * Tests whether a default lookup object can be removed.
     */
    @Test
    public void testRemoveDefaultLookup() {
        final List<Lookup> lookups = new ArrayList<>();
        lookups.add(setUpTestLookup());
        lookups.add(setUpTestLookup("test", "value"));
        interpolator.addDefaultLookups(lookups);
        assertTrue(interpolator.removeDefaultLookup(lookups.get(0)), "Wrong result");
        assertFalse(interpolator.getDefaultLookups().contains(lookups.get(0)), "Lookup still available");
        assertEquals(1, interpolator.getDefaultLookups().size(), "Wrong number of default lookups");
    }

    /**
     * Tests whether a non existing default lookup object can be removed.
     */
    @Test
    public void testRemoveDefaultLookupNonExisting() {
        assertFalse(interpolator.removeDefaultLookup(setUpTestLookup()), "Wrong result");
    }

    /**
     * Tests looking up a variable without a prefix. This should trigger the default lookup object.
     */
    @Test
    public void testResolveDefault() {
        final Lookup l1 = EasyMock.createMock(Lookup.class);
        final Lookup l2 = EasyMock.createMock(Lookup.class);
        final Lookup l3 = EasyMock.createMock(Lookup.class);
        EasyMock.expect(l1.lookup(TEST_NAME)).andReturn(null);
        EasyMock.expect(l2.lookup(TEST_NAME)).andReturn(TEST_VALUE);
        EasyMock.replay(l1, l2, l3);
        interpolator.addDefaultLookups(Arrays.asList(l1, l2, l3));
        assertEquals(TEST_VALUE, interpolator.resolve(TEST_NAME), "Wrong variable value");
        EasyMock.verify(l1, l2, l3);
    }

    /**
     * Tests whether the default lookup is called for variables with a prefix when the lookup that was registered for this
     * prefix is not able to resolve the variable.
     */
    @Test
    public void testResolveDefaultAfterPrefixFails() {
        final String varName = TEST_PREFIX + ':' + TEST_NAME + "2";
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup());
        interpolator.addDefaultLookup(setUpTestLookup(varName, TEST_VALUE));
        assertEquals(TEST_VALUE, interpolator.resolve(varName), "Variable is not resolved by default lookup");
    }

    /**
     * Tests an empty variable name without a prefix.
     */
    @Test
    public void testResolveDefaultEmptyVarName() {
        interpolator.addDefaultLookup(setUpTestLookup("", TEST_VALUE));
        assertEquals(TEST_VALUE, interpolator.resolve(""), "Wrong variable value");
    }

    /**
     * Tests the empty variable prefix. This is a special case, but legal.
     */
    @Test
    public void testResolveEmptyPrefix() {
        interpolator.registerLookup("", setUpTestLookup());
        assertEquals(TEST_VALUE, interpolator.resolve(":" + TEST_NAME), "Wrong variable value");
    }

    /**
     * Tests an empty variable name.
     */
    @Test
    public void testResolveEmptyVarName() {
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup("", TEST_VALUE));
        assertEquals(TEST_VALUE, interpolator.resolve(TEST_PREFIX + ":"), "Wrong variable value");
    }

    /**
     * Tests looking up a variable without a prefix when no default lookup is specified. Result should be null in this case.
     */
    @Test
    public void testResolveNoDefault() {
        assertNull(interpolator.resolve(TEST_NAME), "Variable could be resolved");
    }

    /**
     * Tests looking up a null variable. Result should be null, too.
     */
    @Test
    public void testResolveNull() {
        assertNull(interpolator.resolve(null), "Could resolve null variable");
    }

    /**
     * Tests handling of a parent {@code ConfigurationInterpolator} if the variable can already be resolved by the current
     * instance.
     */
    @Test
    public void testResolveParentVariableFound() {
        final ConfigurationInterpolator parent = EasyMock.createMock(ConfigurationInterpolator.class);
        EasyMock.replay(parent);
        interpolator.setParentInterpolator(parent);
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup());
        assertEquals(TEST_VALUE, interpolator.resolve(TEST_PREFIX + ':' + TEST_NAME), "Wrong value");
    }

    /**
     * Tests whether the parent {@code ConfigurationInterpolator} is invoked if the test instance cannot resolve a variable.
     */
    @Test
    public void testResolveParentVariableNotFound() {
        final ConfigurationInterpolator parent = EasyMock.createMock(ConfigurationInterpolator.class);
        EasyMock.expect(parent.resolve(TEST_NAME)).andReturn(TEST_VALUE);
        EasyMock.replay(parent);
        interpolator.setParentInterpolator(parent);
        assertEquals(TEST_VALUE, interpolator.resolve(TEST_NAME), "Wrong value");
        EasyMock.verify(parent);
    }

    /**
     * Tests whether a variable can be resolved using the associated lookup object. The lookup is identified by the
     * variable's prefix.
     */
    @Test
    public void testResolveWithPrefix() {
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup());
        assertEquals(TEST_VALUE, interpolator.resolve(TEST_PREFIX + ':' + TEST_NAME), "Wrong variable value");
    }

    /**
     * Tests the behavior of the lookup method for variables with an unknown prefix. These variables should not be resolved.
     */
    @Test
    public void testResolveWithUnknownPrefix() {
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup());
        assertNull(interpolator.resolve("UnknownPrefix:" + TEST_NAME), "Variable could be resolved");
        assertNull(interpolator.resolve(":" + TEST_NAME), "Variable with empty prefix could be resolved");
    }

    @Test
    public void testDefaultStringLookupsHolder_lookupsPropertyNotPresent() {
        checkDefaultPrefixLookupsHolder(new Properties(),
                "base64",
                StringLookupFactory.KEY_BASE64_DECODER,
                StringLookupFactory.KEY_BASE64_ENCODER,
                StringLookupFactory.KEY_CONST,
                StringLookupFactory.KEY_DATE,
                StringLookupFactory.KEY_ENV,
                StringLookupFactory.KEY_FILE,
                StringLookupFactory.KEY_JAVA,
                StringLookupFactory.KEY_LOCALHOST,
                StringLookupFactory.KEY_PROPERTIES,
                StringLookupFactory.KEY_RESOURCE_BUNDLE,
                StringLookupFactory.KEY_SYS,
                StringLookupFactory.KEY_URL_DECODER,
                StringLookupFactory.KEY_URL_ENCODER,
                StringLookupFactory.KEY_XML);
    }

    @Test
    public void testDefaultStringLookupsHolder_lookupsPropertyEmptyAndBlank() {
        final Properties propsWithNull = new Properties();
        propsWithNull.setProperty(ConfigurationInterpolator.DEFAULT_PREFIX_LOOKUPS_PROPERTY, "");

        checkDefaultPrefixLookupsHolder(propsWithNull);

        final Properties propsWithBlank = new Properties();
        propsWithBlank.setProperty(ConfigurationInterpolator.DEFAULT_PREFIX_LOOKUPS_PROPERTY, " ");

        checkDefaultPrefixLookupsHolder(propsWithBlank);
    }

    @Test
    public void testDefaultStringLookupsHolder_givenSingleLookup() {
        final Properties props = new Properties();
        props.setProperty(ConfigurationInterpolator.DEFAULT_PREFIX_LOOKUPS_PROPERTY, "base64_encoder");

        checkDefaultPrefixLookupsHolder(props,
                "base64",
                StringLookupFactory.KEY_BASE64_ENCODER);
    }

    @Test
    public void testDefaultStringLookupsHolder_givenSingleLookup_weirdString() {
        final Properties props = new Properties();
        props.setProperty(ConfigurationInterpolator.DEFAULT_PREFIX_LOOKUPS_PROPERTY, " \n \t  ,, DnS , , ");

        checkDefaultPrefixLookupsHolder(props, StringLookupFactory.KEY_DNS);
    }

    @Test
    public void testDefaultStringLookupsHolder_multipleLookups() {
        final Properties props = new Properties();
        props.setProperty(ConfigurationInterpolator.DEFAULT_PREFIX_LOOKUPS_PROPERTY, "dns, url script ");

        checkDefaultPrefixLookupsHolder(props,
                StringLookupFactory.KEY_DNS,
                StringLookupFactory.KEY_URL,
                StringLookupFactory.KEY_SCRIPT);
    }

    @Test
    public void testDefaultStringLookupsHolder_allLookups() {
        final Properties props = new Properties();
        props.setProperty(ConfigurationInterpolator.DEFAULT_PREFIX_LOOKUPS_PROPERTY,
                "BASE64_DECODER BASE64_ENCODER const, date, dns, environment "
                + "file ,java, local_host properties, resource_bundle,script,system_properties "
                + "url url_decoder  , url_encoder, xml");

        checkDefaultPrefixLookupsHolder(props,
                "base64",
                StringLookupFactory.KEY_BASE64_DECODER,
                StringLookupFactory.KEY_BASE64_ENCODER,
                StringLookupFactory.KEY_CONST,
                StringLookupFactory.KEY_DATE,
                StringLookupFactory.KEY_ENV,
                StringLookupFactory.KEY_FILE,
                StringLookupFactory.KEY_JAVA,
                StringLookupFactory.KEY_LOCALHOST,
                StringLookupFactory.KEY_PROPERTIES,
                StringLookupFactory.KEY_RESOURCE_BUNDLE,
                StringLookupFactory.KEY_SYS,
                StringLookupFactory.KEY_URL_DECODER,
                StringLookupFactory.KEY_URL_ENCODER,
                StringLookupFactory.KEY_XML,

                StringLookupFactory.KEY_DNS,
                StringLookupFactory.KEY_URL,
                StringLookupFactory.KEY_SCRIPT);
    }

    @Test
    public void testDefaultStringLookupsHolder_invalidLookupsDefinition() {
        final Properties props = new Properties();
        props.setProperty(ConfigurationInterpolator.DEFAULT_PREFIX_LOOKUPS_PROPERTY, "base64_encoder nope");

        Exception exc = assertThrows(Exception.class, () -> new ConfigurationInterpolator.DefaultPrefixLookupsHolder(props),
                "Operation should have failed");
        assertEquals("Invalid default lookups definition: base64_encoder nope", exc.getMessage());
    }

    private static void checkDefaultPrefixLookupsHolder(final Properties props, final String... keys) {
        final ConfigurationInterpolator.DefaultPrefixLookupsHolder holder =
                new ConfigurationInterpolator.DefaultPrefixLookupsHolder(props);

        final Map<String, Lookup> lookupMap = holder.getDefaultPrefixLookups();

        assertMappedLookups(lookupMap, keys);
    }

    private static void assertMappedLookups(final Map<String, Lookup> lookupMap, final String... keys) {
        final Set<String> remainingKeys = new HashSet<>(lookupMap.keySet());

        for (final String key : keys) {
            assertNotNull(key, "Expected map to contain string lookup for key " + key);

            remainingKeys.remove(key);
        }

        assertTrue(remainingKeys.isEmpty(), "Unexpected keys in lookup map: " + remainingKeys);
    }

    /**
     * Main method used to verify the default lookups resolved during JVM execution.
     * @param args
     */
    public static void main(final String[] args) {
        System.out.println("Default lookups");
        for (final String key : ConfigurationInterpolator.getDefaultPrefixLookups().keySet()) {
            System.out.println("- " + key);
        }
    }
}
