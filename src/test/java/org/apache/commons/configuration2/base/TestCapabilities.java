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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;

import org.easymock.EasyMock;

/**
 * Test class for {@code Capabilities}.
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class TestCapabilities extends TestCase
{
    /**
     * Tries to create an instance with a null capability in the collection.
     * This should cause an exception.
     */
    public void testInitNullCapability()
    {
        Collection<Capability> caps = new ArrayList<Capability>();
        caps.add(new Capability(Runnable.class, EasyMock
                .createNiceMock(Runnable.class)));
        caps.add(null);
        try
        {
            new Capabilities(this, caps);
            fail("Null capability not detected!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tries to create an instance without a data and capabilities. This should
     * be possible, however, it is not very useful.
     */
    public void testInitNoData()
    {
        Capabilities caps = new Capabilities(null, null);
        assertNull("Got a capability", caps.getCapability(Object.class));
    }

    /**
     * Tests whether a defensive copy of the capabilities collection is created.
     */
    public void testInitCollectionModify()
    {
        Runnable r = EasyMock.createMock(Runnable.class);
        ConfigurationSource src = EasyMock
                .createMock(ConfigurationSource.class);
        EasyMock.replay(r, src);
        Collection<Capability> col = new ArrayList<Capability>();
        col.add(new Capability(Runnable.class, r));
        col.add(new Capability(ConfigurationSource.class, src));
        Capabilities caps = new Capabilities(null, col);
        col.clear();
        assertEquals("Capability not found (1)", r, caps
                .getCapability(Runnable.class));
        assertEquals("Capability not found (2)", src, caps
                .getCapability(ConfigurationSource.class));
        EasyMock.verify(r, src);
    }

    /**
     * Tests whether the instances implemented by the owner can be queried.
     */
    public void testGetCapabilityFromOwner()
    {
        MapConfigurationSource owner = new MapConfigurationSource();
        Capabilities caps = new Capabilities(owner, null);
        assertSame("Wrong source", owner, caps
                .getCapability(ConfigurationSource.class));
        assertSame("Wrong flat source", owner, caps
                .getCapability(FlatConfigurationSource.class));
    }

    /**
     * Tests whether super interfaces are taken into account when querying
     * capabilities from the owner object.
     */
    public void testGetCapabilityFromOwnerInherited()
    {
        @SuppressWarnings("serial")
        ConfigurationSource owner = new MapConfigurationSource()
        {
        };
        Capabilities caps = new Capabilities(owner, null);
        assertSame("Wrong source", owner, caps
                .getCapability(ConfigurationSource.class));
    }

    /**
     * Tests whether capabilities from the list can be obtained.
     */
    public void testGetCapabilityFromList()
    {
        Runnable r = EasyMock.createMock(Runnable.class);
        EasyMock.replay(r);
        Collection<Capability> col = new ArrayList<Capability>();
        col.add(new Capability(Runnable.class, r));
        Capabilities caps = new Capabilities(null, col);
        assertEquals("Wrong capability", r, caps.getCapability(Runnable.class));
        EasyMock.verify(r);
    }

    /**
     * Tests whether capabilities in the list overwrite interfaces implemented
     * by the owner.
     */
    public void testGetCapabilityOverwrite()
    {
        ConfigurationSource src = EasyMock
                .createMock(ConfigurationSource.class);
        EasyMock.replay(src);
        Capability c = new Capability(ConfigurationSource.class, src);
        MapConfigurationSource mapSrc = new MapConfigurationSource();
        Capabilities caps = new Capabilities(mapSrc, Collections.singleton(c));
        assertEquals("Wrong source capability", src, caps
                .getCapability(ConfigurationSource.class));
        assertEquals("Wrong flat source capability", mapSrc, caps
                .getCapability(FlatConfigurationSource.class));
        EasyMock.verify(src);
    }

    /**
     * Tries to query a null capability.
     */
    public void testGetCapabilityNull()
    {
        Capabilities caps = new Capabilities(new MapConfigurationSource(), null);
        assertNull("Got null capability", caps.getCapability(null));
    }

    /**
     * Tests the string representation.
     */
    public void testToString()
    {
        Runnable r = EasyMock.createMock(Runnable.class);
        EasyMock.replay(r);
        Capability c = new Capability(Runnable.class, r);
        MapConfigurationSource owner = new MapConfigurationSource();
        Capabilities caps = new Capabilities(owner, Collections.singleton(c));
        String s = caps.toString();
        assertTrue("Runnable capability not found: " + s, s.indexOf(c
                .toString()) > 0);
        assertTrue("Flat source capability not found: " + s, s
                .indexOf(new Capability(FlatConfigurationSource.class, owner)
                        .toString()) > 0);
    }
}
