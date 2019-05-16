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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for ConfigurationInterpolator.
 *
 */
public class TestConfigurationInterpolator
{
    /** Constant for a test variable prefix. */
    private static final String TEST_PREFIX = "prefix";

    /** Constant for a test variable name. */
    private static final String TEST_NAME = "varname";

    /** Constant for the value of the test variable. */
    private static final String TEST_VALUE = "TestVariableValue";

    /** Stores the object to be tested. */
    private ConfigurationInterpolator interpolator;

    @Before
    public void setUp() throws Exception
    {
        interpolator = new ConfigurationInterpolator();
    }

    /**
     * Creates a lookup object that can resolve the test variable (and nothing else).
     *
     * @return the test lookup object
     */
    private static Lookup setUpTestLookup()
    {
        return setUpTestLookup(TEST_NAME, TEST_VALUE);
    }

    /**
     * Creates a lookup object that can resolve the specified variable (and
     * nothing else).
     *
     * @param var the variable name
     * @param value the value of this variable
     * @return the test lookup object
     */
    private static Lookup setUpTestLookup(final String var, final Object value)
    {
        final Lookup lookup = EasyMock.createMock(Lookup.class);
        EasyMock.expect(lookup.lookup(EasyMock.anyObject(String.class)))
                .andAnswer(new IAnswer<Object>()
                {
                    @Override
                    public Object answer() throws Throwable
                    {
                        if (var.equals(EasyMock.getCurrentArguments()[0]))
                        {
                            return value;
                        }
                        return null;
                    }
                }).anyTimes();
        EasyMock.replay(lookup);
        return lookup;
    }

    /**
     * Tests creating an instance. Does it contain some predefined lookups?
     */
    @Test
    public void testInit()
    {
        assertTrue("A default lookup is set", interpolator.getDefaultLookups().isEmpty());
        assertTrue("Got predefined lookups", interpolator.getLookups().isEmpty());
        assertNull("Got a parent interpolator", interpolator.getParentInterpolator());
    }

    /**
     * Tests registering a lookup object at an instance.
     */
    @Test
    public void testRegisterLookup()
    {
        final Lookup lookup = EasyMock.createMock(Lookup.class);
        EasyMock.replay(lookup);
        interpolator.registerLookup(TEST_PREFIX, lookup);
        assertSame("New lookup not registered", lookup, interpolator
                .getLookups().get(TEST_PREFIX));
        assertTrue("Not in prefix set",
                interpolator.prefixSet().contains(TEST_PREFIX));
        assertTrue("Default lookups were changed", interpolator
                .getDefaultLookups().isEmpty());
    }

    /**
     * Tests registering a null lookup object. This should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterLookupNull()
    {
        interpolator.registerLookup(TEST_PREFIX, null);
    }

    /**
     * Tests registering a lookup object for an undefined prefix. This should
     * cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterLookupNullPrefix()
    {
        interpolator.registerLookup(null, EasyMock.createMock(Lookup.class));
    }

    /**
     * Tests deregistering a lookup object.
     */
    @Test
    public void testDeregisterLookup()
    {
        final Lookup lookup = EasyMock.createMock(Lookup.class);
        EasyMock.replay(lookup);
        interpolator.registerLookup(TEST_PREFIX, lookup);
        assertTrue("Derigstration not successfull", interpolator
                .deregisterLookup(TEST_PREFIX));
        assertFalse("Deregistered prefix still contained", interpolator
                .prefixSet().contains(TEST_PREFIX));
        assertTrue("Lookups not empty", interpolator.getLookups().isEmpty());
    }

    /**
     * Tests deregistering an unknown lookup object.
     */
    @Test
    public void testDeregisterLookupNonExisting()
    {
        assertFalse("Could deregister unknown lookup", interpolator
                .deregisterLookup(TEST_PREFIX));
    }

    /**
     * Tests whether a variable can be resolved using the associated lookup
     * object. The lookup is identified by the variable's prefix.
     */
    @Test
    public void testResolveWithPrefix()
    {
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup());
        assertEquals("Wrong variable value", TEST_VALUE, interpolator
                .resolve(TEST_PREFIX + ':' + TEST_NAME));
    }

    /**
     * Tests the behavior of the lookup method for variables with an unknown
     * prefix. These variables should not be resolved.
     */
    @Test
    public void testResolveWithUnknownPrefix()
    {
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup());
        assertNull("Variable could be resolved", interpolator
                .resolve("UnknownPrefix:" + TEST_NAME));
        assertNull("Variable with empty prefix could be resolved", interpolator
                .resolve(":" + TEST_NAME));
    }

    /**
     * Tests looking up a variable without a prefix. This should trigger the
     * default lookup object.
     */
    @Test
    public void testResolveDefault()
    {
        final Lookup l1 = EasyMock.createMock(Lookup.class);
        final Lookup l2 = EasyMock.createMock(Lookup.class);
        final Lookup l3 = EasyMock.createMock(Lookup.class);
        EasyMock.expect(l1.lookup(TEST_NAME)).andReturn(null);
        EasyMock.expect(l2.lookup(TEST_NAME)).andReturn(TEST_VALUE);
        EasyMock.replay(l1, l2, l3);
        interpolator.addDefaultLookups(Arrays.asList(l1, l2, l3));
        assertEquals("Wrong variable value", TEST_VALUE, interpolator
                .resolve(TEST_NAME));
        EasyMock.verify(l1, l2, l3);
    }

    /**
     * Tests looking up a variable without a prefix when no default lookup is
     * specified. Result should be null in this case.
     */
    @Test
    public void testResolveNoDefault()
    {
        assertNull("Variable could be resolved", interpolator.resolve(TEST_NAME));
    }

    /**
     * Tests the empty variable prefix. This is a special case, but legal.
     */
    @Test
    public void testResolveEmptyPrefix()
    {
        interpolator.registerLookup("", setUpTestLookup());
        assertEquals("Wrong variable value", TEST_VALUE, interpolator
                .resolve(":" + TEST_NAME));
    }

    /**
     * Tests an empty variable name.
     */
    @Test
    public void testResolveEmptyVarName()
    {
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup("", TEST_VALUE));
        assertEquals("Wrong variable value", TEST_VALUE, interpolator
                .resolve(TEST_PREFIX + ":"));
    }

    /**
     * Tests an empty variable name without a prefix.
     */
    @Test
    public void testResolveDefaultEmptyVarName()
    {
        interpolator.addDefaultLookup(setUpTestLookup("", TEST_VALUE));
        assertEquals("Wrong variable value", TEST_VALUE, interpolator
                .resolve(""));
    }

    /**
     * Tests looking up a null variable. Result should be null, too.
     */
    @Test
    public void testResolveNull()
    {
        assertNull("Could resolve null variable", interpolator.resolve(null));
    }

    /**
     * Tests whether the default lookup is called for variables with a prefix
     * when the lookup that was registered for this prefix is not able to
     * resolve the variable.
     */
    @Test
    public void testResolveDefaultAfterPrefixFails()
    {
        final String varName = TEST_PREFIX + ':' + TEST_NAME + "2";
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup());
        interpolator.addDefaultLookup(setUpTestLookup(varName, TEST_VALUE));
        assertEquals("Variable is not resolved by default lookup", TEST_VALUE,
                interpolator.resolve(varName));
    }

    /**
     * Tests whether a map with lookup objects can be registered.
     */
    @Test
    public void testRegisterLookups()
    {
        final Lookup l1 = setUpTestLookup();
        final Lookup l2 = setUpTestLookup("someVar", "someValue");
        final Map<String, Lookup> lookups = new HashMap<>();
        lookups.put(TEST_PREFIX, l1);
        final String prefix2 = TEST_PREFIX + "_other";
        lookups.put(prefix2, l2);
        interpolator.registerLookups(lookups);
        final Map<String, Lookup> lookups2 = interpolator.getLookups();
        assertEquals("Wrong number of lookups", 2, lookups2.size());
        assertEquals("Wrong l1", l1, lookups2.get(TEST_PREFIX));
        assertEquals("Wrong l2", l2, lookups2.get(prefix2));
    }

    /**
     * Tests whether a null map with lookup objects is handled correctly.
     */
    @Test
    public void testRegisterLookupsNull()
    {
        interpolator.registerLookups(null);
        assertTrue("Got lookups", interpolator.getLookups().isEmpty());
    }

    /**
     * Tests that modification of the map with lookups does not affect the object.
     */
    @Test
    public void testGetLookupsModify()
    {
        final Map<String, Lookup> lookups = interpolator.getLookups();
        lookups.put(TEST_PREFIX, setUpTestLookup());
        assertTrue("Map was modified", interpolator.getLookups().isEmpty());
    }

    /**
     * Tests whether multiple default lookups can be added.
     */
    @Test
    public void testAddDefaultLookups()
    {
        final List<Lookup> lookups = new ArrayList<>();
        lookups.add(setUpTestLookup());
        lookups.add(setUpTestLookup("test", "value"));
        interpolator.addDefaultLookups(lookups);
        final List<Lookup> lookups2 = interpolator.getDefaultLookups();
        assertEquals("Wrong number of default lookups", 2, lookups2.size());
        assertTrue("Wrong content", lookups2.containsAll(lookups));
    }

    /**
     * Tests whether a null collection of default lookups is handled correctly.
     */
    @Test
    public void testAddDefaultLookupsNull()
    {
        interpolator.addDefaultLookups(null);
        assertTrue("Got default lookups", interpolator.getDefaultLookups()
                .isEmpty());
    }

    /**
     * Tests whether modification of the list of default lookups does not affect
     * the object.
     */
    @Test
    public void testGetDefaultLookupsModify()
    {
        final List<Lookup> lookups = interpolator.getDefaultLookups();
        lookups.add(setUpTestLookup());
        assertTrue("List was modified", interpolator.getDefaultLookups()
                .isEmpty());
    }

    /**
     * Tests whether a default lookup object can be removed.
     */
    @Test
    public void testRemoveDefaultLookup()
    {
        final List<Lookup> lookups = new ArrayList<>();
        lookups.add(setUpTestLookup());
        lookups.add(setUpTestLookup("test", "value"));
        interpolator.addDefaultLookups(lookups);
        assertTrue("Wrong result",
                interpolator.removeDefaultLookup(lookups.get(0)));
        assertFalse("Lookup still available", interpolator.getDefaultLookups()
                .contains(lookups.get(0)));
        assertEquals("Wrong number of default lookups", 1, interpolator
                .getDefaultLookups().size());
    }

    /**
     * Tests whether a non existing default lookup object can be removed.
     */
    @Test
    public void testRemoveDefaultLookupNonExisting()
    {
        assertFalse("Wrong result",
                interpolator.removeDefaultLookup(setUpTestLookup()));
    }

    /**
     * Tests that the prefix set cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testPrefixSetModify()
    {
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup());
        final Iterator<String> it = interpolator.prefixSet().iterator();
        it.next();
        it.remove();
    }

    /**
     * Tests handling of a parent {@code ConfigurationInterpolator} if the
     * variable can already be resolved by the current instance.
     */
    @Test
    public void testResolveParentVariableFound()
    {
        final ConfigurationInterpolator parent =
                EasyMock.createMock(ConfigurationInterpolator.class);
        EasyMock.replay(parent);
        interpolator.setParentInterpolator(parent);
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup());
        assertEquals("Wrong value", TEST_VALUE,
                interpolator.resolve(TEST_PREFIX + ':' + TEST_NAME));
    }

    /**
     * Tests whether the parent {@code ConfigurationInterpolator} is invoked if
     * the test instance cannot resolve a variable.
     */
    @Test
    public void testResolveParentVariableNotFound()
    {
        final ConfigurationInterpolator parent =
                EasyMock.createMock(ConfigurationInterpolator.class);
        EasyMock.expect(parent.resolve(TEST_NAME)).andReturn(TEST_VALUE);
        EasyMock.replay(parent);
        interpolator.setParentInterpolator(parent);
        assertEquals("Wrong value", TEST_VALUE, interpolator.resolve(TEST_NAME));
        EasyMock.verify(parent);
    }

    /**
     * Tests interpolation of a non string argument.
     */
    @Test
    public void testInterpolateObject()
    {
        final Object value = 42;
        assertSame("Value was changed", value, interpolator.interpolate(value));
    }

    /**
     * Tests a successful interpolation of a string value.
     */
    @Test
    public void testInterpolateString()
    {
        final String value = "${" + TEST_PREFIX + ':' + TEST_NAME + "}";
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup());
        assertEquals("Wrong result", TEST_VALUE,
                interpolator.interpolate(value));
    }

    /**
     * Tests interpolation with a variable which cannot be resolved.
     */
    @Test
    public void testInterpolateStringUnknownVariable()
    {
        final String value = "${unknownVariable}";
        assertEquals("Wrong result", value, interpolator.interpolate(value));
    }

    /**
     * Tests whether the flag for substitution in variable names can be
     * modified.
     */
    @Test
    public void testEnableSubstitutionInVariables()
    {
        assertFalse("Variable substitution enabled",
                interpolator.isEnableSubstitutionInVariables());
        interpolator.addDefaultLookup(setUpTestLookup("java.version", "1.4"));
        interpolator.addDefaultLookup(setUpTestLookup("jre-1.4",
                "C:\\java\\1.4"));
        final String var = "${jre-${java.version}}";
        assertEquals("Wrong result (1)", var, interpolator.interpolate(var));
        interpolator.setEnableSubstitutionInVariables(true);
        assertTrue("Variable substitution not enabled",
                interpolator.isEnableSubstitutionInVariables());
        assertEquals("Wrong result (2)", "C:\\java\\1.4",
                interpolator.interpolate(var));
    }

    /**
     * Tests a property value consisting of multiple variables.
     */
    @Test
    public void testInterpolationMultipleVariables()
    {
        final String value = "The ${subject} jumps over ${object}.";
        interpolator.addDefaultLookup(setUpTestLookup("subject", "quick brown fox"));
        interpolator.addDefaultLookup(setUpTestLookup("object", "the lazy dog"));
        assertEquals("Wrong result", "The quick brown fox jumps over the lazy dog.",
                interpolator.interpolate(value));
    }

    /**
     * Tests an interpolation that consists of a single variable only. The
     * variable's value should be returned verbatim.
     */
    @Test
    public void testInterpolationSingleVariable()
    {
        final Object value = 42;
        interpolator.addDefaultLookup(setUpTestLookup(TEST_NAME, value));
        assertEquals("Wrong result", value,
                interpolator.interpolate("${" + TEST_NAME + "}"));
    }

    /**
     * Tests a variable declaration which lacks the trailing closing bracket.
     */
    @Test
    public void testInterpolationVariableIncomplete()
    {
        final String value = "${" + TEST_NAME;
        interpolator.addDefaultLookup(setUpTestLookup(TEST_NAME, "someValue"));
        assertEquals("Wrong result", value, interpolator.interpolate(value));
    }

    /**
     * Tests that an empty variable definition does not cause problems.
     */
    @Test
    public void testInterpolateEmptyVariable()
    {
        final String value = "${}";
        assertEquals("Wrong result", value, interpolator.interpolate(value));
    }

    /**
     * Tries to obtain an instance from a null specification.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFromSpecificationNull()
    {
        ConfigurationInterpolator.fromSpecification(null);
    }

    /**
     * Tests fromSpecification() if the specification contains an instance.
     */
    @Test
    public void testFromSpecificationInterpolator()
    {
        final ConfigurationInterpolator ci =
                EasyMock.createMock(ConfigurationInterpolator.class);
        EasyMock.replay(ci);
        final InterpolatorSpecification spec =
                new InterpolatorSpecification.Builder()
                        .withDefaultLookup(EasyMock.createMock(Lookup.class))
                        .withParentInterpolator(interpolator)
                        .withInterpolator(ci).create();
        assertSame("Wrong result", ci,
                ConfigurationInterpolator.fromSpecification(spec));
    }

    /**
     * Tests fromSpecification() if a new instance has to be created.
     */
    @Test
    public void testFromSpecificationNewInstance()
    {
        final Lookup defLookup = EasyMock.createMock(Lookup.class);
        final Lookup preLookup = EasyMock.createMock(Lookup.class);
        EasyMock.replay(defLookup, preLookup);
        final InterpolatorSpecification spec =
                new InterpolatorSpecification.Builder()
                        .withDefaultLookup(defLookup)
                        .withPrefixLookup("p", preLookup)
                        .withParentInterpolator(interpolator).create();
        final ConfigurationInterpolator ci =
                ConfigurationInterpolator.fromSpecification(spec);
        assertEquals("Wrong number of default lookups", 1, ci
                .getDefaultLookups().size());
        assertTrue("Wrong default lookup",
                ci.getDefaultLookups().contains(defLookup));
        assertEquals("Wrong number of prefix lookups", 1, ci.getLookups()
                .size());
        assertSame("Wrong prefix lookup", preLookup, ci.getLookups().get("p"));
        assertSame("Wrong parent", interpolator, ci.getParentInterpolator());
    }

    /**
     * Tests whether default prefix lookups can be queried as a map.
     */
    @Test
    public void testGetDefaultPrefixLookups()
    {
        final Map<String, Lookup> lookups =
                ConfigurationInterpolator.getDefaultPrefixLookups();
        assertEquals("Wrong number of lookups", DefaultLookups.values().length,
                lookups.size());
        for (final DefaultLookups l : DefaultLookups.values())
        {
            assertSame("Wrong entry for " + l, l.getLookup(),
                    lookups.get(l.getPrefix()));
        }
    }

    /**
     * Tests that the map with default lookups cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDefaultPrefixLookupsModify()
    {
        ConfigurationInterpolator.getDefaultPrefixLookups().put("test",
                EasyMock.createMock(Lookup.class));
    }

    /**
     * Tests nullSafeLookup() if a lookup object was provided.
     */
    @Test
    public void testNullSafeLookupExisting()
    {
        final Lookup look = EasyMock.createMock(Lookup.class);
        EasyMock.replay(look);
        assertSame("Wrong result", look,
                ConfigurationInterpolator.nullSafeLookup(look));
    }

    /**
     * Tests whether nullSafeLookup() can handle null input.
     */
    @Test
    public void testNullSafeLookupNull()
    {
        final Lookup lookup = ConfigurationInterpolator.nullSafeLookup(null);
        assertNull("Got a lookup result", lookup.lookup("someVar"));
    }
}
