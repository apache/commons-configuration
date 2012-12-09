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
import static org.junit.Assert.assertSame;

import java.util.Map;

import javax.naming.Context;

import org.apache.commons.beanutils.PropertyUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@code JndiBuilderParametersImpl}.
 *
 * @version $Id$
 */
public class TestJndiBuilderParametersImpl
{
    /** The parameters object to be tested. */
    private JndiBuilderParametersImpl params;

    @Before
    public void setUp() throws Exception
    {
        params = new JndiBuilderParametersImpl();
    }

    /**
     * Tests whether a JNDI context can be set.
     */
    @Test
    public void testSetContext()
    {
        Context ctx = EasyMock.createMock(Context.class);
        EasyMock.replay(ctx);
        assertSame("Wrong result", params, params.setContext(ctx));
        Map<String, Object> paramsMap = params.getParameters();
        assertSame("Context not in map", ctx, paramsMap.get("context"));
    }

    /**
     * Tests whether a prefix can be set.
     */
    @Test
    public void testSetPrefix()
    {
        String prefix = "testJndiPrefix";
        assertSame("Wrong result", params, params.setPrefix(prefix));
        Map<String, Object> paramsMap = params.getParameters();
        assertEquals("Prefix not in map", prefix, paramsMap.get("prefix"));
    }

    /**
     * Tests whether the parameters map contains inherited properties, too.
     */
    @Test
    public void testGetParametersBaseProperties()
    {
        params.setPrefix("somePrefix");
        params.setListDelimiter('?');
        Map<String, Object> paramsMap = params.getParameters();
        assertEquals("Wrong delimiter parsing flag", Boolean.TRUE,
                paramsMap.get("delimiterParsingDisabled"));
        assertEquals("Wrong list delimiter", Character.valueOf('?'),
                paramsMap.get("listDelimiter"));
    }

    /**
     * Tests whether properties can be set through BeanUtils.
     */
    @Test
    public void testSetBeanProperties() throws Exception
    {
        Context ctx = EasyMock.createMock(Context.class);
        EasyMock.replay(ctx);
        String prefix = "testJndiPrefix";
        PropertyUtils.setProperty(params, "context", ctx);
        PropertyUtils.setProperty(params, "prefix", prefix);
        Map<String, Object> paramsMap = params.getParameters();
        assertSame("Context not in map", ctx, paramsMap.get("context"));
        assertEquals("Prefix not in map", prefix, paramsMap.get("prefix"));
    }
}
