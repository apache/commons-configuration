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

import java.util.Iterator;

import org.apache.commons.configuration2.EnvironmentConfiguration;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for EnvironmentLookup.
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 */
public class TestEnvironmentLookup
{
    /** The lookup to be tested. */
    private Lookup lookup;

    @Before
    public void setUp() throws Exception
    {
        lookup = DefaultLookups.ENVIRONMENT.getLookup();
    }

    /**
     * Tests whether environment variables can be queried.
     */
    @Test
    public void testLookup()
    {
        final EnvironmentConfiguration envConf = new EnvironmentConfiguration();
        for (final Iterator<String> it = envConf.getKeys(); it.hasNext();)
        {
            final String var = it.next();
            assertEquals("Wrong value for " + var, envConf.getString(var),
                    lookup.lookup(var));
        }
    }

    /**
     * Tries to lookup a non existing property.
     */
    @Test
    public void testLookupNonExisting()
    {
        assertNull("Got result for non existing environment variable", lookup
                .lookup("a non existing variable!"));
    }
}
