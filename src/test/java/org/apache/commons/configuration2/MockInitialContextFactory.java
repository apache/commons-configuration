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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * A mock implementation of the {@code InitialContextFactory} interface. This implementation will return a mock context
 * that contains some test data.
 */
public class MockInitialContextFactory implements InitialContextFactory {

    /**
     * A {@link NamingEnumeration} implementation that's backed by an iterator.
     */
    private static final class ListBasedNamingEnumeration implements NamingEnumeration<NameClassPair> {

        private final Iterator<NameClassPair> iterator;

        private ListBasedNamingEnumeration(final List<NameClassPair> pairs) {
            this.iterator = pairs.iterator();
        }

        @Override
        public void close() throws NamingException {
        }

        @Override
        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        @Override
        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        @Override
        public NameClassPair next() throws NamingException {
            return nextElement();
        }

        @Override
        public NameClassPair nextElement() {
            return iterator.next();
        }
    }

    /**
     * Constant for the use cycles environment property. If this property is present in the environment, a cyclic context
     * will be created.
     */
    public static final String PROP_CYCLES = "useCycles";

    /** Constant for the name of the missing property. */
    private static final String MISSING_PROP = "/missing";

    /** Constant for the name of the prefix. */
    private static final String PREFIX = "test/";

    /** An array with the names of the supported properties. */
    private static final String[] PROP_NAMES = {"key", "key2", "short", "boolean", "byte", "double", "float", "integer", "long", "onlyinjndi"};

    /** An array with the values of the supported properties. */
    private static final String[] PROP_VALUES = {"jndivalue", "jndivalue2", "1", "true", "10", "10.25", "20.25", "10", "1000000", "true"};

    /** An array with properties that are requested, but are not in the context. */
    private static final String[] MISSING_NAMES = {"missing/list", "test/imaginarykey", "foo/bar"};

    /**
     * Adds a new name-and-value pair to list of {@link NameClassPair}s.
     *
     * @param pairs the list to add to
     * @param name the name
     * @param value the value
     */
    private void addEnumPair(final List<NameClassPair> pairs, final String name, final Object value) {
        final NameClassPair ncp = new NameClassPair(name, value.getClass().getName());
        pairs.add(ncp);
    }

    /**
     * Binds a property value to the mock context.
     *
     * @param mockCtx the context
     * @param name the name of the property
     * @param value the value of the property
     */
    private void bind(final Context mockCtx, final String name, final String value) throws NamingException {
        when(mockCtx.lookup(name)).thenReturn(value);
        bindError(mockCtx, name + MISSING_PROP);
    }

    /**
     * Configures the mock to expect a call for a non existing property.
     *
     * @param mockCtx the mock
     * @param name the name of the property
     */
    private void bindError(final Context mockCtx, final String name) throws NamingException {
        when(mockCtx.lookup(name)).thenThrow(new NameNotFoundException("unknown property"));
    }

    /**
     * Creates a mock for a Context with the specified prefix.
     *
     * @param prefix the prefix
     * @return the mock for the context
     */
    private Context createCtxMock(final String prefix) throws NamingException {
        final Context mockCtx = mock(Context.class);
        for (int i = 0; i < PROP_NAMES.length; i++) {
            bind(mockCtx, prefix + PROP_NAMES[i], PROP_VALUES[i]);
            final String errProp = prefix.isEmpty() ? PREFIX + PROP_NAMES[i] : PROP_NAMES[i];
            bindError(mockCtx, errProp);
        }
        for (final String element : MISSING_NAMES) {
            bindError(mockCtx, element);
        }

        return mockCtx;
    }

    /**
     * Creates and initializes a list of {@link NameClassPair}s that can be used to create a naming enumeration.
     *
     * @param names the names contained in the iteration
     * @param values the corresponding values
     * @return the mock for the enumeration
     */
    private List<NameClassPair> createNameClassPairs(final String[] names, final Object[] values) {
        final List<NameClassPair> pairs = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            addEnumPair(pairs, names[i], values[i]);
        }
        return pairs;
    }

    /**
     * Creates and initializes a naming enumeration. This is a shortcut of wrapping the result of
     * {@link #createNameClassPairs(String[], Object[])} in an instance of {@link ListBasedNamingEnumeration}.
     *
     * @param names the names contained in the iteration
     * @param values the corresponding values
     * @return the mock for the enumeration
     */
    private NamingEnumeration<NameClassPair> createNamingEnumeration(final String[] names, final Object[] values) {
        return new ListBasedNamingEnumeration(createNameClassPairs(names, values));
    }

    /**
     * Creates a {@code Context} object that is backed by a mock object. The mock context can be queried for the values of
     * certain test properties. It also supports listing the contained (sub) properties.
     *
     * @param env the environment
     * @return the context mock
     */
    @Override
    public Context getInitialContext(@SuppressWarnings("rawtypes") final Hashtable env) throws NamingException {
        final boolean useCycles = env.containsKey(PROP_CYCLES);

        final Context mockTopCtx = createCtxMock(PREFIX);
        final Context mockCycleCtx = createCtxMock("");
        final Context mockPrfxCtx = createCtxMock("");
        final Context mockBaseCtx = mock(Context.class);

        when(mockBaseCtx.lookup("")).thenReturn(mockTopCtx);
        when(mockBaseCtx.lookup("test")).thenReturn(mockPrfxCtx);
        when(mockTopCtx.lookup("test")).thenReturn(mockPrfxCtx);
        when(mockPrfxCtx.list("")).thenAnswer(invocation -> createNamingEnumeration(PROP_NAMES, PROP_VALUES));

        if (useCycles) {
            when(mockTopCtx.lookup("cycle")).thenReturn(mockCycleCtx);
            when(mockTopCtx.list("")).thenAnswer(invocation ->
                    createNamingEnumeration(new String[] {"test", "cycle"}, new Object[] {mockPrfxCtx, mockCycleCtx}));
            when(mockCycleCtx.list("")).thenAnswer(invocation -> {
                final List<NameClassPair> pairs = createNameClassPairs(PROP_NAMES, PROP_VALUES);
                addEnumPair(pairs, "cycleCtx", mockCycleCtx);
                return new ListBasedNamingEnumeration(pairs);
            });
            when(mockCycleCtx.lookup("cycleCtx")).thenReturn(mockCycleCtx);
        } else {
            when(mockTopCtx.list("")).thenAnswer(invocation -> createNamingEnumeration(new String[] {"test"}, new Object[] {mockPrfxCtx}));
        }
        return mockBaseCtx;
    }
}
