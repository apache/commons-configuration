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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.event.KeyEvent;

import org.apache.commons.configuration.AbstractConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for ConstantLookup.
 *
 * @version $Id$
 */
public class TestConstantLookup
{
    /** Constant for the name of the test class. */
    private static final String CLS_NAME = AbstractConfiguration.class
            .getName() + '.';

    /** Constant for the name of the test field. */
    private static final String FIELD = "EVENT_READ_PROPERTY";

    /** Constant for the test variable name. */
    private static final String VARNAME = CLS_NAME + FIELD;

    /** The lookup object to be tested. */
    private ConstantLookup lookup;

    @Before
    public void setUp() throws Exception
    {
        lookup = new ConstantLookup();
    }

    /**
     * Clears the test environment. Here the static cache of the constant lookup
     * class is wiped out.
     */
    @After
    public void tearDown() throws Exception
    {
        ConstantLookup.clear();
    }

    /**
     * Tests resolving a valid constant.
     */
    @Test
    public void testLookupConstant()
    {
        assertEquals("Wrong value of constant",
                AbstractConfiguration.EVENT_READ_PROPERTY, lookup
                        .lookup(VARNAME));
    }

    /**
     * Tests resolving a non existing constant. Result should be null.
     */
    @Test
    public void testLookupNonExisting()
    {
        assertNull("Non null return value for non existing constant", lookup
                .lookup(CLS_NAME + "NO_FIELD"));
    }

    /**
     * Tests resolving a private constant. Because a private field cannot be
     * accessed this should again yield null.
     */
    @Test
    public void testLookupPrivate()
    {
        assertNull("Non null return value for non accessable field", lookup
                .lookup(CLS_NAME + "DISABLED_DELIMITER"));
    }

    /**
     * Tests resolving a field from an unknown class.
     */
    @Test
    public void testLookupUnknownClass()
    {
        assertNull("Non null return value for unknown class", lookup
                .lookup("org.apache.commons.configuration.NonExistingConfig."
                        + FIELD));
    }

    /**
     * Tries to resolve a variable with an invalid syntax: The name does not
     * contain a dot as a field separator.
     */
    @Test
    public void testLookupInvalidSyntax()
    {
        assertNull("Non null return value for invalid variable name", lookup
                .lookup("InvalidVariableName"));
    }

    /**
     * Tests looking up a null variable.
     */
    @Test
    public void testLookupNull()
    {
        assertNull("Non null return value for null variable", lookup
                .lookup(null));
    }

    /**
     * Tests accessing the cache by querying a variable twice.
     */
    @Test
    public void testLookupCache()
    {
        testLookupConstant();
        testLookupConstant();
    }

    /**
     * Tests resolving a non string constant. Then looks the same variable up
     * from the cache.
     */
    @Test
    public void testLookupNonStringFromCache()
    {
        final String var = KeyEvent.class.getName() + ".VK_ESCAPE";
        final Object expected = KeyEvent.VK_ESCAPE;
        assertEquals("Wrong result of first lookup", expected, lookup
                .lookup(var));
        assertEquals("Wrong result of 2nd lookup", expected, lookup.lookup(var));
    }
}
