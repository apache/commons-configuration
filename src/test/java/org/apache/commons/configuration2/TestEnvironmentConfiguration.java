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

package org.apache.commons.configuration2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for EnvironmentConfiguration.
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 */
public class TestEnvironmentConfiguration
{
    /** Stores the configuration to be tested. */
    private EnvironmentConfiguration config;

    @Before
    public void setUp() throws Exception
    {
        config = new EnvironmentConfiguration();
    }

    /**
     * Tests whether a newly created configuration contains some properties. (We
     * expect that at least some properties are set in each environment.)
     */
    @Test
    public void testInit()
    {
        boolean found = false;
        assertFalse("No properties found", config.isEmpty());
        for (final Iterator<String> it = config.getKeys(); it.hasNext();)
        {
            final String key = it.next();
            assertTrue("Key not found: " + key, config.containsKey(key));
            assertNotNull("No value for property " + key, config.getString(key));
            found = true;
        }
        assertTrue("No property keys returned", found);
    }

    /**
     * Tests removing properties. This should not be possible.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testClearProperty()
    {
        final String key = config.getKeys().next();
        config.clearProperty(key);
    }

    /**
     * Tests removing all properties. This should not be possible.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testClear()
    {
        config.clear();
    }

    /**
     * Tries to add another property. This should cause an exception.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddProperty()
    {
        config.addProperty("JAVA_HOME", "C:\\java");
    }

    /**
     * Tries to set the value of a property. This should cause an exception.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testSetProperty()
    {
        config.setProperty("JAVA_HOME", "C:\\java");
    }
}
