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
package org.apache.commons.configuration2.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.util.Map;

import javax.naming.Context;

import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code JndiBuilderParametersImpl}.
 */
public class TestJndiBuilderParametersImpl {
    /** The parameters object to be tested. */
    private JndiBuilderParametersImpl params;

    @BeforeEach
    public void setUp() throws Exception {
        params = new JndiBuilderParametersImpl();
    }

    /**
     * Tests whether the parameters map contains inherited properties, too.
     */
    @Test
    public void testGetParametersBaseProperties() {
        params.setPrefix("somePrefix");
        params.setThrowExceptionOnMissing(true);
        final Map<String, Object> paramsMap = params.getParameters();
        assertEquals(Boolean.TRUE, paramsMap.get("throwExceptionOnMissing"));
    }

    /**
     * Tests whether properties can be set through BeanUtils.
     */
    @Test
    public void testSetBeanProperties() throws Exception {
        final Context ctx = mock(Context.class);
        final String prefix = "testJndiPrefix";
        BeanHelper.setProperty(params, "context", ctx);
        BeanHelper.setProperty(params, "prefix", prefix);
        final Map<String, Object> paramsMap = params.getParameters();
        assertSame(ctx, paramsMap.get("context"));
        assertEquals(prefix, paramsMap.get("prefix"));
    }

    /**
     * Tests whether a JNDI context can be set.
     */
    @Test
    public void testSetContext() {
        final Context ctx = mock(Context.class);
        assertSame(params, params.setContext(ctx));
        final Map<String, Object> paramsMap = params.getParameters();
        assertSame(ctx, paramsMap.get("context"));
    }

    /**
     * Tests whether a prefix can be set.
     */
    @Test
    public void testSetPrefix() {
        final String prefix = "testJndiPrefix";
        assertSame(params, params.setPrefix(prefix));
        final Map<String, Object> paramsMap = params.getParameters();
        assertEquals(prefix, paramsMap.get("prefix"));
    }
}
