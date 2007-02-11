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
package org.apache.commons.configuration;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;

/**
 * A mock implementation of the <code>InitialContextFactory</code> interface.
 * This implementation will return a mock context that contains some test data.
 *
 * @author <a
 * href="http://jakarta.apache.org/commons/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 */
public class MockInitialContextFactory implements InitialContextFactory
{
    /** Constant for the lookup method. */
    private static final String METHOD_LOOKUP = "lookup";

    /** Constant for the list method. */
    private static final String METHOD_LIST = "list";

    /** Constant for the name of the missing property. */
    private static final String MISSING_PROP = "/missing";

    /** Constant for the name of the prefix. */
    private static final String PREFIX = "test/";

    /** An array with the names of the supported properties. */
    private static final String[] PROP_NAMES =
    { "key", "key2", "short", "boolean", "byte", "double", "float", "integer",
            "long", "onlyinjndi" };

    /** An array with the values of the supported properties. */
    private static final String[] PROP_VALUES =
    { "jndivalue", "jndivalue2", "1", "true", "10", "10.25", "20.25", "10",
            "1000000", "true" };

    /** An array with properties that are requested, but are not in the context. */
    private static final String[] MISSING_NAMES =
    { "missing/list", "test/imaginarykey", "foo/bar" };

    /**
     * Creates a <code>Context</code> object that is backed by a mock object.
     * The mock context can be queried for the values of certain test
     * properties. It also supports listing the contained (sub) properties.
     *
     * @param env the environment
     * @return the context mock
     */
    public Context getInitialContext(Hashtable env) throws NamingException
    {
        Mock mockTopCtx = createCtxMock(PREFIX);
        Mock mockPrfxCtx = createCtxMock("");
        Mock mockBaseCtx = new Mock(Context.class);
        mockBaseCtx.matchAndReturn(METHOD_LOOKUP, C.eq(""), mockTopCtx.proxy());
        mockBaseCtx.matchAndReturn(METHOD_LOOKUP, C.eq("test"), mockPrfxCtx
                .proxy());
        mockTopCtx.matchAndReturn(METHOD_LOOKUP, C.eq("test"), mockPrfxCtx
                .proxy());
        mockTopCtx.matchAndReturn(METHOD_LIST, C.eq(""), createEnumMock(
                mockTopCtx, new String[]
                { "test" }, new Object[]
                { mockPrfxCtx.proxy() }).proxy());
        mockPrfxCtx.matchAndReturn(METHOD_LIST, C.eq(""), createEnumMock(
                mockPrfxCtx, PROP_NAMES, PROP_VALUES).proxy());
        return (Context) mockBaseCtx.proxy();
    }

    /**
     * Creates a mock for a Context with the specified prefix.
     *
     * @param prefix the prefix
     * @return the mock for the context
     */
    private Mock createCtxMock(String prefix)
    {
        Mock mockCtx = new Mock(Context.class);
        for (int i = 0; i < PROP_NAMES.length; i++)
        {
            bind(mockCtx, prefix + PROP_NAMES[i], PROP_VALUES[i]);
            String errProp = (prefix.length() > 0) ? PROP_NAMES[i] : PREFIX
                    + PROP_NAMES[i];
            bindError(mockCtx, errProp);
        }
        for (int i = 0; i < MISSING_NAMES.length; i++)
        {
            bindError(mockCtx, MISSING_NAMES[i]);
        }
        return mockCtx;
    }

    /**
     * Binds a property value to the mock context.
     *
     * @param mockCtx the context
     * @param name the name of the property
     * @param value the value of the property
     */
    private void bind(Mock mockCtx, String name, String value)
    {
        mockCtx.matchAndReturn(METHOD_LOOKUP, C.eq(name), value);
        bindError(mockCtx, name + MISSING_PROP);
    }

    /**
     * Configures the mock to expect a call for a non existing property.
     *
     * @param mockCtx the mock
     * @param name the name of the property
     */
    private void bindError(Mock mockCtx, String name)
    {
        mockCtx.matchAndThrow(METHOD_LOOKUP, C.eq(name),
                new NameNotFoundException("unknown property"));
    }

    /**
     * Creates and initializes a mock for a naming enumeration.
     *
     * @param mockCtx the mock representing the context
     * @param names the names contained in the iteration
     * @param values the corresponding values
     * @return the mock for the enumeration
     */
    private Mock createEnumMock(Mock mockCtx, String[] names, Object[] values)
    {
        Mock mockEnum = new Mock(NamingEnumeration.class);
        for (int i = 0; i < names.length; i++)
        {
            NameClassPair ncp = new NameClassPair(names[i], values[i]
                    .getClass().getName());
            mockEnum.expectAndReturn("hasMore", true);
            mockEnum.expectAndReturn("next", ncp);
        }
        mockEnum.expectAndReturn("hasMore", false);
        mockEnum.expect("close");
        return mockEnum;
    }
}
