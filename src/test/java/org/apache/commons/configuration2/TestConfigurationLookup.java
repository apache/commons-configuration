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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Test class for {@code ConfigurationLookup}.
 *
 */
public class TestConfigurationLookup
{
    /** Constant for a test variable name. */
    private static final String VAR = "testVariable";

    /** Constant for the value of the test variable. */
    private static final Object VALUE = "SomeTestValue";

    /**
     * Tries to create an instance without a configuration.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoConfig()
    {
        new ConfigurationLookup(null);
    }

    /**
     * Tests whether an existing variable can be resolved.
     */
    @Test
    public void testLookupSuccess()
    {
        final Configuration conf = new BaseConfiguration();
        conf.addProperty(VAR, VALUE);
        final ConfigurationLookup lookup = new ConfigurationLookup(conf);
        assertEquals("Wrong result", VALUE, lookup.lookup(VAR));
    }

    /**
     * Tests lookup() if the variable cannot be resolved.
     */
    @Test
    public void testLookupNotFound()
    {
        final Configuration conf = new BaseConfiguration();
        final ConfigurationLookup lookup = new ConfigurationLookup(conf);
        assertNull("Got a value", lookup.lookup(VAR));
    }

    /**
     * Tests lookup() if the variable cannot be resolved, and the configuration
     * throws an exception.
     */
    @Test
    public void testLookupNotFoundEx()
    {
        final BaseConfiguration conf = new BaseConfiguration();
        conf.setThrowExceptionOnMissing(true);
        final ConfigurationLookup lookup = new ConfigurationLookup(conf);
        assertNull("Got a value", lookup.lookup(VAR));
    }

    /**
     * Tests lookup() for a complex property value.
     */
    @Test
    public void testLookupComplex()
    {
        final int count = 5;
        final Configuration conf = new BaseConfiguration();
        for (int i = 0; i < count; i++)
        {
            conf.addProperty(VAR, String.valueOf(VALUE) + i);
        }
        final ConfigurationLookup lookup = new ConfigurationLookup(conf);
        final Collection<?> col = (Collection<?>) lookup.lookup(VAR);
        assertEquals("Wrong number of elements", count, col.size());
        final Iterator<?> it = col.iterator();
        for (int i = 0; i < count; i++)
        {
            assertEquals("Wrong element at " + i, String.valueOf(VALUE) + i,
                    it.next());
        }
    }
}
