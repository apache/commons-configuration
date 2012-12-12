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
import static org.junit.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;

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
}
