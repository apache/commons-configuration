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

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.commons.configuration.EnvironmentConfiguration;

/**
 * Test class for EnvironmentLookup.
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public class TestEnvironmentLookup extends TestCase
{
    /** The lookup to be tested. */
    private EnvironmentLookup lookup;

    protected void setUp() throws Exception
    {
        super.setUp();
        lookup = new EnvironmentLookup();
    }

    /**
     * Tests whether environment variables can be queried.
     */
    public void testLookup()
    {
        EnvironmentConfiguration envConf = new EnvironmentConfiguration();
        for (Iterator it = envConf.getKeys(); it.hasNext();)
        {
            String var = (String) it.next();
            assertEquals("Wrong value for " + var, envConf.getString(var),
                    lookup.lookup(var));
        }
    }

    /**
     * Tries to lookup a non existing property.
     */
    public void testLookupNonExisting()
    {
        assertNull("Got result for non existing environment variable", lookup
                .lookup("a non existing variable!"));
    }
}
