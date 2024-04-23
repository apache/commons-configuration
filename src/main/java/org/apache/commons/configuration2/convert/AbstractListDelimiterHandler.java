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
package org.apache.commons.configuration2.convert;

import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * <p>
 * An abstract base class for concrete {@code ListDelimiterHandler} implementations.
 * </p>
 * <p>
 * This base class provides a fully functional implementation for parsing a value object which can deal with different
 * cases like collections, arrays, iterators, etc. This logic is typically needed by every concrete subclass. Other
 * methods are partly implemented handling special corner cases like <b>null</b> values; concrete subclasses do not have
 * do implement the corresponding checks.
 * </p>
 *
 * @since 2.0
 */
public abstract class AbstractListDelimiterHandler implements ListDelimiterHandler {

    static Collection<?> flatten(final ListDelimiterHandler handler, final Object value, final int limit, final Set<Object> dejaVu) {
        dejaVu.add(value);
        if (value instanceof String) {
            return handler.split((String) value, true);
        }
        final Collection<Object> result = new LinkedList<>();
        if (value instanceof Path) {
            // Don't handle as an Iterable.
            result.add(value);
        } else if (value instanceof Iterable) {
            AbstractListDelimiterHandler.flattenIterator(handler, result, ((Iterable<?>) value).iterator(), limit, dejaVu);
        } else if (value instanceof Iterator) {
            AbstractListDelimiterHandler.flattenIterator(handler, result, (Iterator<?>) value, limit, dejaVu);
        } else if (value != null) {
            if (value.getClass().isArray()) {
                for (int len = Array.getLength(value), idx = 0, size = 0; idx < len && size < limit; idx++, size = result.size()) {
                    result.addAll(handler.flatten(Array.get(value, idx), limit - size));
                }
            } else {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Flattens the given iterator. For each element in the iteration {@code flatten()} is called recursively.
     *
     * @param handler the working handler
     * @param target the target collection
     * @param iterator the iterator to process
     * @param limit a limit for the number of elements to extract
     * @param dejaVue Previously visited objects.
     */
    static void flattenIterator(final ListDelimiterHandler handler, final Collection<Object> target, final Iterator<?> iterator, final int limit,
            final Set<Object> dejaVue) {
        int size = target.size();
        while (size < limit && iterator.hasNext()) {
            final Object next = iterator.next();
            if (!dejaVue.contains(next)) {
                target.addAll(flatten(handler, next, limit - size, dejaVue));
                size = target.size();
            }
        }
    }

    /**
     * {@inheritDoc} This implementation checks whether the object to be escaped is a string. If yes, it delegates to
     * {@link #escapeString(String)}, otherwise no escaping is performed. Eventually, the passed in transformer is invoked
     * so that additional encoding can be performed.
     */
    @Override
    public Object escape(final Object value, final ValueTransformer transformer) {
        return transformer.transformValue(value instanceof String ? escapeString((String) value) : value);
    }

    /**
     * Escapes the specified string. This method is called by {@code escape()} if the passed in object is a string. Concrete
     * subclasses have to implement their specific escaping logic here, so that the list delimiters they support are
     * properly escaped.
     *
     * @param s the string to be escaped (not <b>null</b>)
     * @return the escaped string
     */
    protected abstract String escapeString(String s);

    /**
     * Performs the actual work as advertised by the {@code parse()} method. This method delegates to
     * {@link #flatten(Object, int)} without specifying a limit.
     *
     * @param value the value to be processed
     * @return a &quot;flat&quot; collection containing all primitive values of the passed in object
     */
    private Collection<?> flatten(final Object value) {
        return flatten(value, Integer.MAX_VALUE);
    }

    /**
     * {@inheritDoc} Depending on the type of the passed in object the following things happen:
     * <ul>
     * <li>Strings are checked for delimiter characters and split if necessary. This is done by calling the {@code split()}
     * method.</li>
     * <li>For objects implementing the {@code Iterable} interface, the corresponding {@code Iterator} is obtained, and
     * contained elements are added to the resulting iteration.</li>
     * <li>Arrays are treated as {@code Iterable} objects.</li>
     * <li>All other types are directly inserted.</li>
     * <li>Recursive combinations are supported, e.g. a collection containing an array that contains strings: The resulting
     * collection will only contain primitive objects.</li>
     * </ul>
     */
    @Override
    public Iterable<?> parse(final Object value) {
        return flatten(value);
    }

    /**
     * {@inheritDoc} This implementation handles the case that the passed in string is <b>null</b>. In this case, an empty
     * collection is returned. Otherwise, this method delegates to {@link #splitString(String, boolean)}.
     */
    @Override
    public Collection<String> split(final String s, final boolean trim) {
        return s == null ? new ArrayList<>(0) : splitString(s, trim);
    }

    /**
     * Actually splits the passed in string which is guaranteed to be not <b>null</b>. This method is called by the base
     * implementation of the {@code split()} method. Here the actual splitting logic has to be implemented.
     *
     * @param s the string to be split (not <b>null</b>)
     * @param trim a flag whether the single components have to be trimmed
     * @return a collection with the extracted components of the passed in string
     */
    protected abstract Collection<String> splitString(String s, boolean trim);
}
