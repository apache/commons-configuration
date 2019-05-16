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
 * A mock implementation of the {@code InitialContextFactory} interface.
 * This implementation will return a mock context that contains some test data.
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 */
public class MockInitialContextFactory implements InitialContextFactory
{
    /**
     * Constant for the use cycles environment property. If this property is
     * present in the environment, a cyclic context will be created.
     */
    public static final String PROP_CYCLES = "useCycles";

    /** Constant for the lookup method. */
    private static final String METHOD_LOOKUP = "lookup";

    /** Constant for the list method. */
    private static final String METHOD_LIST = "list";

    /** Constant for the close method.*/
    private static final String METHOD_CLOSE = "close";

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
     * Creates a {@code Context} object that is backed by a mock object.
     * The mock context can be queried for the values of certain test
     * properties. It also supports listing the contained (sub) properties.
     *
     * @param env the environment
     * @return the context mock
     */
    @Override
    public Context getInitialContext(@SuppressWarnings("rawtypes") final Hashtable env) throws NamingException
    {
        final boolean useCycles = env.containsKey(PROP_CYCLES);

        final Mock mockTopCtx = createCtxMock(PREFIX);
        final Mock mockCycleCtx = createCtxMock("");
        final Mock mockPrfxCtx = createCtxMock("");
        final Mock mockBaseCtx = new Mock(Context.class);
        mockBaseCtx.matchAndReturn(METHOD_LOOKUP, C.eq(""), mockTopCtx.proxy());
        mockBaseCtx.matchAndReturn(METHOD_LOOKUP, C.eq("test"), mockPrfxCtx
                .proxy());
        mockTopCtx.matchAndReturn(METHOD_LOOKUP, C.eq("test"), mockPrfxCtx
                .proxy());
        mockPrfxCtx.matchAndReturn(METHOD_LIST, C.eq(""), createEnumMock(
                mockPrfxCtx, PROP_NAMES, PROP_VALUES).proxy());

        if (useCycles)
        {
            mockTopCtx.matchAndReturn(METHOD_LOOKUP, C.eq("cycle"),
                    mockCycleCtx.proxy());
            mockTopCtx.matchAndReturn(METHOD_LIST, C.eq(""), createEnumMock(
                    mockTopCtx, new String[]
                    { "test", "cycle" }, new Object[]
                    { mockPrfxCtx.proxy(), mockCycleCtx.proxy() }).proxy());
            final Mock mockEnum = createEnumMock(mockCycleCtx, PROP_NAMES,
                    PROP_VALUES, false);
            addEnumPair(mockEnum, "cycleCtx", mockCycleCtx.proxy());
            closeEnum(mockEnum);
            mockCycleCtx
                    .matchAndReturn(METHOD_LIST, C.eq(""), mockEnum.proxy());
            mockCycleCtx.matchAndReturn(METHOD_LOOKUP, C.eq("cycleCtx"),
                    mockCycleCtx.proxy());
        }
        else
        {
            mockTopCtx.matchAndReturn(METHOD_LIST, C.eq(""), createEnumMock(
                    mockTopCtx, new String[]
                    { "test" }, new Object[]
                    { mockPrfxCtx.proxy() }).proxy());
        }
        return (Context) mockBaseCtx.proxy();
    }

    /**
     * Creates a mock for a Context with the specified prefix.
     *
     * @param prefix the prefix
     * @return the mock for the context
     */
    private Mock createCtxMock(final String prefix)
    {
        final Mock mockCtx = new Mock(Context.class);
        for (int i = 0; i < PROP_NAMES.length; i++)
        {
            bind(mockCtx, prefix + PROP_NAMES[i], PROP_VALUES[i]);
            final String errProp = (prefix.length() > 0) ? PROP_NAMES[i] : PREFIX
                    + PROP_NAMES[i];
            bindError(mockCtx, errProp);
        }
        for (final String element : MISSING_NAMES) {
            bindError(mockCtx, element);
        }
        mockCtx.matchAndReturn("hashCode", System.identityHashCode(mockCtx.proxy()));

        return mockCtx;
    }

    /**
     * Binds a property value to the mock context.
     *
     * @param mockCtx the context
     * @param name the name of the property
     * @param value the value of the property
     */
    private void bind(final Mock mockCtx, final String name, final String value)
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
    private void bindError(final Mock mockCtx, final String name)
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
     * @param close a flag whether the enumeration should expect to be closed
     * @return the mock for the enumeration
     */
    private Mock createEnumMock(final Mock mockCtx, final String[] names, final Object[] values,
            final boolean close)
    {
        final Mock mockEnum = new Mock(NamingEnumeration.class);
        for (int i = 0; i < names.length; i++)
        {
            addEnumPair(mockEnum, names[i], values[i]);
        }
        if (close)
        {
            closeEnum(mockEnum);
        }
        return mockEnum;
    }

    /**
     * Creates and initializes a mock for a naming enumeration that expects to
     * be closed. This is a shortcut of createEnumMock(mockCtx, names, values,
     * true);
     *
     * @param mockCtx the mock representing the context
     * @param names the names contained in the iteration
     * @param values the corresponding values
     * @return the mock for the enumeration
     */
    private Mock createEnumMock(final Mock mockCtx, final String[] names, final Object[] values)
    {
        return createEnumMock(mockCtx, names, values, true);
    }

    /**
     * Adds a new name-and-value pair to an enum mock.
     *
     * @param mockEnum the enum mock
     * @param name the name
     * @param value the value
     */
    private void addEnumPair(final Mock mockEnum, final String name, final Object value)
    {
        final NameClassPair ncp = new NameClassPair(name, value.getClass().getName());
        mockEnum.expectAndReturn("hasMore", true);
        mockEnum.expectAndReturn("next", ncp);
    }

    /**
     * Closes an enumeration mock.
     *
     * @param mockEnum the mock
     */
    private void closeEnum(final Mock mockEnum)
    {
        mockEnum.expectAndReturn("hasMore", false);
        mockEnum.expect(METHOD_CLOSE);
    }
}
