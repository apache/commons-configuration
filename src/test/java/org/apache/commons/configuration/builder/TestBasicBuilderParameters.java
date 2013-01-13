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
package org.apache.commons.configuration.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration.interpol.Lookup;
import org.apache.commons.logging.Log;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code BasicBuilderParameters}.
 *
 * @version $Id$
 */
public class TestBasicBuilderParameters
{
    /** The instance to be tested. */
    private BasicBuilderParameters params;

    @Before
    public void setUp() throws Exception
    {
        params = new BasicBuilderParameters();
    }

    /**
     * Tests the default parameter values.
     */
    @Test
    public void testDefaults()
    {
        Map<String, Object> paramMap = params.getParameters();
        assertEquals("Wrong number of parameters", 1, paramMap.size());
        assertEquals("Delimiter flag not set", Boolean.TRUE,
                paramMap.get("delimiterParsingDisabled"));
    }

    /**
     * Tests whether a defensive copy is created when the parameter map is
     * returned.
     */
    @Test
    public void testGetParametersDefensiveCopy()
    {
        Map<String, Object> map1 = params.getParameters();
        Map<String, Object> mapCopy = new HashMap<String, Object>(map1);
        map1.put("otherProperty", "value");
        Map<String, Object> map2 = params.getParameters();
        assertNotSame("Same map returned", map1, map2);
        assertEquals("Different properties", mapCopy, map2);
    }

    /**
     * Tests whether the logger parameter can be set.
     */
    @Test
    public void testSetLogger()
    {
        Log log = EasyMock.createMock(Log.class);
        EasyMock.replay(log);
        assertSame("Wrong result", params, params.setLogger(log));
        assertSame("Wrong logger parameter", log,
                params.getParameters().get("logger"));
    }

    /**
     * Tests whether the delimiter parsing disabled property can be set.
     */
    @Test
    public void testSetDelimiterParsingDisabled()
    {
        assertSame("Wrong result", params,
                params.setDelimiterParsingDisabled(false));
        assertEquals("Wrong flag value", Boolean.FALSE, params.getParameters()
                .get("delimiterParsingDisabled"));
    }

    /**
     * Tests whether the throw exception on missing property can be set.
     */
    @Test
    public void testSetThrowExceptionOnMissing()
    {
        assertSame("Wrong result", params,
                params.setThrowExceptionOnMissing(true));
        assertEquals("Wrong flag value", Boolean.TRUE, params.getParameters()
                .get("throwExceptionOnMissing"));
    }

    /**
     * Tests whether the list delimiter property can be set.
     */
    @Test
    public void testSetListDelimiter()
    {
        assertSame("Wrong result", params, params.setListDelimiter(';'));
        assertEquals("Wrong delimiter", Character.valueOf(';'), params
                .getParameters().get("listDelimiter"));
    }

    /**
     * Tests whether a {@code ConfigurationInterpolator} can be set.
     */
    @Test
    public void testSetInterpolator()
    {
        ConfigurationInterpolator ci =
                EasyMock.createMock(ConfigurationInterpolator.class);
        EasyMock.replay(ci);
        assertSame("Wrong result", params, params.setInterpolator(ci));
        assertSame("Wrong interpolator", ci,
                params.getParameters().get("interpolator"));
    }

    /**
     * Tests whether prefix lookups can be set.
     */
    @Test
    public void testSetPrefixLookups()
    {
        Lookup look = EasyMock.createMock(Lookup.class);
        Map<String, Lookup> lookups = Collections.singletonMap("test", look);
        assertSame("Wrong result", params, params.setPrefixLookups(lookups));
        Map<?, ?> map = (Map<?, ?>) params.getParameters().get("prefixLookups");
        assertNotSame("No copy was created", lookups, map);
        assertEquals("Wrong lookup", look, map.get("test"));
        assertEquals("Wrong number of lookups", 1, map.size());
    }

    /**
     * Tests whether null values are handled by setPrefixLookups().
     */
    @Test
    public void testSetPrefixLookupsNull()
    {
        params.setPrefixLookups(new HashMap<String, Lookup>());
        params.setPrefixLookups(null);
        assertFalse("Found key",
                params.getParameters().containsKey("prefixLookups"));
    }

    /**
     * Tests whether default lookups can be set.
     */
    @Test
    public void testSetDefaultLookups()
    {
        Lookup look = EasyMock.createMock(Lookup.class);
        Collection<Lookup> looks = Collections.singleton(look);
        assertSame("Wrong result", params, params.setDefaultLookups(looks));
        Collection<?> col =
                (Collection<?>) params.getParameters().get("defaultLookups");
        assertNotSame("No copy was created", col, looks);
        assertEquals("Wrong number of lookups", 1, col.size());
        assertSame("Wrong lookup", look, col.iterator().next());
    }

    /**
     * Tests whether null values are handled by setDefaultLookups().
     */
    @Test
    public void testSetDefaultLookupsNull()
    {
        params.setDefaultLookups(new ArrayList<Lookup>());
        params.setDefaultLookups(null);
        assertFalse("Found key",
                params.getParameters().containsKey("defaultLookups"));
    }

    /**
     * Tests whether a parent {@code ConfigurationInterpolator} can be set.
     */
    @Test
    public void testSetParentInterpolator()
    {
        ConfigurationInterpolator parent =
                EasyMock.createMock(ConfigurationInterpolator.class);
        EasyMock.replay(parent);
        assertSame("Wrong result", params, params.setParentInterpolator(parent));
        assertSame("Wrong parent", parent,
                params.getParameters().get("parentInterpolator"));
    }

    /**
     * Tests whether a custom {@code ConfigurationInterpolator} overrides
     * settings for custom lookups.
     */
    @Test
    public void testSetLookupsAndInterpolator()
    {
        Lookup look1 = EasyMock.createMock(Lookup.class);
        Lookup look2 = EasyMock.createMock(Lookup.class);
        ConfigurationInterpolator parent =
                EasyMock.createMock(ConfigurationInterpolator.class);
        ConfigurationInterpolator ci =
                EasyMock.createMock(ConfigurationInterpolator.class);
        params.setDefaultLookups(Collections.singleton(look1));
        params.setPrefixLookups(Collections.singletonMap("test", look2));
        params.setInterpolator(ci);
        params.setParentInterpolator(parent);
        Map<String, Object> map = params.getParameters();
        assertFalse("Got prefix lookups", map.containsKey("prefixLookups"));
        assertFalse("Got default lookups", map.containsKey("defaultLookups"));
        assertFalse("Got a parent interpolator",
                map.containsKey("parentInterpolator"));
    }

    /**
     * Tries a merge with a null object.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMergeNull()
    {
        params.merge(null);
    }

    /**
     * Tests whether properties of other parameter objects can be merged.
     */
    @Test
    public void testMerge()
    {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("throwExceptionOnMissing", Boolean.TRUE);
        props.put("listDelimiter", Character.valueOf('-'));
        props.put("other", "test");
        props.put(BuilderParameters.RESERVED_PARAMETER_PREFIX + "test",
                "reserved");
        BuilderParameters p = EasyMock.createMock(BuilderParameters.class);
        EasyMock.expect(p.getParameters()).andReturn(props);
        EasyMock.replay(p);
        params.setListDelimiter('+');
        params.merge(p);
        Map<String, Object> map = params.getParameters();
        assertEquals("Wrong list delimiter", Character.valueOf('+'),
                map.get("listDelimiter"));
        assertEquals("Wrong exception flag", Boolean.TRUE,
                map.get("throwExceptionOnMissing"));
        assertEquals("Wrong other property", "test", map.get("other"));
        assertFalse(
                "Reserved property was copied",
                map.containsKey(BuilderParameters.RESERVED_PARAMETER_PREFIX
                        + "test"));
    }

    /**
     * Tests fetchInterpolator() if the map does not contain an object.
     */
    @Test
    public void testFetchInterpolatorNotFound()
    {
        Map<String, Object> params = new HashMap<String, Object>();
        assertNull("Got an interpolator",
                BasicBuilderParameters.fetchInterpolator(params));
    }

    /**
     * Tests whether a {@code ConfigurationInterpolator} can be obtained from a
     * parameters map.
     */
    @Test
    public void testFetchInterpolatorFound()
    {
        ConfigurationInterpolator ci = new ConfigurationInterpolator();
        params.setInterpolator(ci);
        Map<String, Object> map = params.getParameters();
        assertSame("Wrong interpolator", ci,
                BasicBuilderParameters.fetchInterpolator(map));
    }
}
