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
package org.apache.commons.configuration2.base;

import junit.framework.TestCase;

/**
 * Test class for {@code Capability}.
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class TestCapability extends TestCase
{
    /**
     * Tries to create an instance without a class. This should cause an
     * exception.
     */
    public void testInitNoClass()
    {
        try
        {
            new Capability(null, this);
            fail("Could create an instance without a class!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tries to create an instance without an implementation object. This should
     * cause an exception.
     */
    public void testInitNoObj()
    {
        try
        {
            new Capability(Object.class, null);
            fail("Could create an instance without an instance!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests whether an object can be created correctly.
     */
    public void testInit()
    {
        MapConfigurationSource src = new MapConfigurationSource();
        Capability c = new Capability(ConfigurationSource.class, src);
        assertEquals("Wrong class", ConfigurationSource.class, c
                .getCapabilityClass());
        assertSame("Wrong object", src, c.getCapabilityObject());
    }

    /**
     * Tests the matches() method for an exact match.
     */
    public void testMatchesExact()
    {
        Capability c = new Capability(FlatConfigurationSource.class,
                new MapConfigurationSource());
        assertTrue("No match", c.matches(FlatConfigurationSource.class));
    }

    /**
     * Tests the matches() method if the passed in class is a super class of the
     * capability class.
     */
    public void testMatchesSuper()
    {
        Capability c = new Capability(FlatConfigurationSource.class,
                new MapConfigurationSource());
        assertTrue("No match", c.matches(ConfigurationSource.class));
    }

    /**
     * Tests matches() if the class is not compatible.
     */
    public void testMatchesOther()
    {
        Capability c = new Capability(FlatConfigurationSource.class,
                new MapConfigurationSource());
        assertFalse("A match", c.matches(HierarchicalConfigurationSource.class));
    }

    /**
     * Tests matches() for a null class.
     */
    public void testMatchesNull()
    {
        Capability c = new Capability(FlatConfigurationSource.class,
                new MapConfigurationSource());
        assertFalse("A match", c.matches(null));
    }

    /**
     * Tests the string representation.
     */
    public void testToString()
    {
        MapConfigurationSource src = new MapConfigurationSource();
        Capability c = new Capability(ConfigurationSource.class, src);
        String s = c.toString();
        assertEquals("Wrong string", String.format(
                "Capability [ class = %s, object = %s ]",
                ConfigurationSource.class.getName(), src.toString()), s);
    }
}
