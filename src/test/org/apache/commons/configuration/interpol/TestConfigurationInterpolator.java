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
package org.apache.commons.configuration.interpol;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.text.StrLookup;

import junit.framework.TestCase;

/**
 * Test class for ConfigurationInterpolator.
 *
 * @version $Id$
 */
public class TestConfigurationInterpolator extends TestCase
{
    /** Constant for a test variable prefix. */
    private static final String TEST_PREFIX = "prefix";

    /** Constant for a test variable name. */
    private static final String TEST_NAME = "varname";

    /** Constant for the value of the test variable. */
    private static final String TEST_VALUE = "TestVariableValue";

    /** Stores the object to be tested. */
    private ConfigurationInterpolator interpolator;

    protected void setUp() throws Exception
    {
        super.setUp();
        interpolator = new ConfigurationInterpolator();
    }

    /**
     * Cleans the test environment. Deregisters the test lookup object if
     * necessary.
     */
    protected void tearDown() throws Exception
    {
        ConfigurationInterpolator.deregisterGlobalLookup(TEST_PREFIX);
    }

    /**
     * Tests creating an instance. Does it contain some predefined lookups?
     */
    public void testInit()
    {
        assertNull("A default lookup is set", interpolator.getDefaultLookup());
        assertFalse("No predefined lookups", interpolator.prefixSet().isEmpty());
    }

    /**
     * Tries to register a global lookup for a null prefix. This should cause an
     * exception.
     */
    public void testRegisterGlobalLookupNullPrefix()
    {
        try
        {
            ConfigurationInterpolator.registerGlobalLookup(null, StrLookup
                    .noneLookup());
            fail("Could register global lookup with null prefix!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tries to register a global null lookup. This should cause an exception.
     */
    public void testRegisterGlobalLookupNull()
    {
        try
        {
            ConfigurationInterpolator.registerGlobalLookup(TEST_PREFIX, null);
            fail("Could register global null lookup!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests registering a global lookup object. This lookup object should then
     * be available for instances created later on.
     */
    public void testRegisterGlobalLookup()
    {
        ConfigurationInterpolator.registerGlobalLookup(TEST_PREFIX, StrLookup
                .noneLookup());
        ConfigurationInterpolator int2 = new ConfigurationInterpolator();
        assertTrue("No lookup registered for test prefix", int2.prefixSet()
                .contains(TEST_PREFIX));
        assertFalse("Existing instance was modified", interpolator.prefixSet()
                .contains(TEST_PREFIX));
    }

    /**
     * Tests deregistering a global lookup object.
     */
    public void testDeregisterGlobalLookup()
    {
        ConfigurationInterpolator.registerGlobalLookup(TEST_PREFIX, StrLookup
                .noneLookup());
        assertTrue("Lookup could not be deregistered",
                ConfigurationInterpolator.deregisterGlobalLookup(TEST_PREFIX));
        ConfigurationInterpolator int2 = new ConfigurationInterpolator();
        assertFalse("Deregistered lookup still available", int2.prefixSet()
                .contains(TEST_PREFIX));
    }

    /**
     * Tests deregistering an unknown lookup.
     */
    public void testDeregisterGlobalLookupNonExisting()
    {
        assertFalse("Could deregister unknown global lookup",
                ConfigurationInterpolator.deregisterGlobalLookup(TEST_PREFIX));
    }

    /**
     * Tests registering a lookup object at an instance.
     */
    public void testRegisterLookup()
    {
        int cnt = interpolator.prefixSet().size();
        interpolator.registerLookup(TEST_PREFIX, StrLookup.noneLookup());
        assertTrue("New lookup not registered", interpolator.prefixSet()
                .contains(TEST_PREFIX));
        assertEquals("Wrong number of registered lookups", cnt + 1,
                interpolator.prefixSet().size());
        ConfigurationInterpolator int2 = new ConfigurationInterpolator();
        assertFalse("Local registration has global impact", int2.prefixSet()
                .contains(TEST_PREFIX));
    }

    /**
     * Tests registering a null lookup object. This should cause an exception.
     */
    public void testRegisterLookupNull()
    {
        try
        {
            interpolator.registerLookup(TEST_PREFIX, null);
            fail("Could register null lookup!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests registering a lookup object for an undefined prefix. This should
     * cause an exception.
     */
    public void testRegisterLookupNullPrefix()
    {
        try
        {
            interpolator.registerLookup(null, StrLookup.noneLookup());
            fail("Could register lookup for null prefix!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests deregistering a local lookup object.
     */
    public void testDeregisterLookup()
    {
        interpolator.registerLookup(TEST_PREFIX, StrLookup.noneLookup());
        assertTrue("Derigstration not successfull", interpolator
                .deregisterLookup(TEST_PREFIX));
        assertFalse("Deregistered prefix still contained", interpolator
                .prefixSet().contains(TEST_PREFIX));
    }

    /**
     * Tests deregistering an unknown lookup object.
     */
    public void testDeregisterLookupNonExisting()
    {
        assertFalse("Could deregister unknown lookup", interpolator
                .deregisterLookup(TEST_PREFIX));
    }

    /**
     * Tests whether a variable can be resolved using the associated lookup
     * object. The lookup is identified by the variable's prefix.
     */
    public void testLookupWithPrefix()
    {
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup());
        assertEquals("Wrong variable value", TEST_VALUE, interpolator
                .lookup(TEST_PREFIX + ':' + TEST_NAME));
    }

    /**
     * Tests the behavior of the lookup method for variables with an unknown
     * prefix. These variables should not be resolved.
     */
    public void testLookupWithUnknownPrefix()
    {
        interpolator.registerLookup(TEST_PREFIX, setUpTestLookup());
        assertNull("Variable could be resolved", interpolator
                .lookup("UnknownPrefix:" + TEST_NAME));
        assertNull("Variable with empty prefix could be resolved", interpolator
                .lookup(":" + TEST_NAME));
    }

    /**
     * Tests looking up a variable without a prefix. This should trigger the
     * default lookup object.
     */
    public void testLookupDefault()
    {
        interpolator.setDefaultLookup(setUpTestLookup());
        assertEquals("Wrong variable value", TEST_VALUE, interpolator
                .lookup(TEST_NAME));
    }

    /**
     * Tests looking up a variable without a prefix when no default lookup is
     * specified. Result should be null in this case.
     */
    public void testLookupNoDefault()
    {
        assertNull("Variable could be resolved", interpolator.lookup(TEST_NAME));
    }

    /**
     * Tests the empty variable prefix. This is a special case, but legal.
     */
    public void testLookupEmptyPrefix()
    {
        interpolator.registerLookup("", setUpTestLookup());
        assertEquals("Wrong variable value", TEST_VALUE, interpolator
                .lookup(":" + TEST_NAME));
    }

    /**
     * Tests an empty variable name.
     */
    public void testLookupEmptyVarName()
    {
        Map map = new HashMap();
        map.put("", TEST_VALUE);
        interpolator.registerLookup(TEST_PREFIX, StrLookup.mapLookup(map));
        assertEquals("Wrong variable value", TEST_VALUE, interpolator
                .lookup(TEST_PREFIX + ":"));
    }

    /**
     * Tests an empty variable name without a prefix.
     */
    public void testLookupDefaultEmptyVarName()
    {
        Map map = new HashMap();
        map.put("", TEST_VALUE);
        interpolator.setDefaultLookup(StrLookup.mapLookup(map));
        assertEquals("Wrong variable value", TEST_VALUE, interpolator
                .lookup(""));
    }

    /**
     * Tests looking up a null variable. Result shoult be null, too.
     */
    public void testLookupNull()
    {
        assertNull("Could resolve null variable", interpolator.lookup(null));
    }

    /**
     * Creates a lookup object that can resolve the test variable.
     *
     * @return the test lookup object
     */
    private StrLookup setUpTestLookup()
    {
        Map map = new HashMap();
        map.put(TEST_NAME, TEST_VALUE);
        return StrLookup.mapLookup(map);
    }

    /**
     * Tests whether system properties can be correctly resolved.
     */
    public void testLookupSysProperties()
    {
        Properties sysProps = System.getProperties();
        for (Iterator it = sysProps.keySet().iterator(); it.hasNext();)
        {
            String key = (String) it.next();
            assertEquals("Wrong value for system property " + key, sysProps
                    .getProperty(key), interpolator
                    .lookup(ConfigurationInterpolator.PREFIX_SYSPROPERTIES
                            + ":" + key));
        }
    }
}
