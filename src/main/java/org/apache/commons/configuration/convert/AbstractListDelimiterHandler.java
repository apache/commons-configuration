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
package org.apache.commons.configuration.convert;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.configuration.ListDelimiterHandler;
import org.apache.commons.configuration.ValueTransformer;

/**
 * <p>
 * An abstract base class for concrete {@code ListDelimiterHandler}
 * implementations.
 * </p>
 * <p>
 * This base class provides a fully functional implementation for parsing a
 * value object which can deal with different cases like collections, arrays,
 * iterators, etc. This logic is typically needed by every concrete subclass.
 * Other methods are partly implemented handling special corner cases like
 * <b>null</b> values; concrete subclasses do not have do implement the
 * corresponding checks.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public abstract class AbstractListDelimiterHandler implements
        ListDelimiterHandler
{
    /**
     * {@inheritDoc} Depending on the type of the passed in object the following
     * things happen:
     * <ul>
     * <li>Strings are checked for delimiter characters and split if necessary.
     * This is done by calling the {@code split()} method.</li>
     * <li>For objects implementing the {@code Iterable} interface, the
     * corresponding {@code Iterator} is obtained, and contained elements are
     * added to the resulting iteration.</li>
     * <li>Arrays are treated as {@code Iterable} objects.</li>
     * <li>All other types are directly inserted.</li>
     * <li>Recursive combinations are supported, e.g. a collection containing an
     * array that contains strings: The resulting collection will only contain
     * primitive objects.</li>
     * </ul>
     */
    public Iterator<?> parse(Object value)
    {
        return flatten(value).iterator();
    }

    /**
     * {@inheritDoc} This implementation handles the case that the passed in
     * string is <b>null</b>. In this case, an empty collection is returned.
     * Otherwise, this method delegates to {@link #splitString(String, boolean)}.
     */
    public Collection<String> split(String s, boolean trim)
    {
        if (s == null)
        {
            return new ArrayList<String>(0);
        }
        return splitString(s, trim);
    }

    /**
     * {@inheritDoc} This implementation checks whether the object to be escaped
     * is a string. If yes, it delegates to {@link #escapeString(String)},
     * otherwise no escaping is performed. Eventually, the passed in transformer
     * is invoked so that additional encoding can be performed.
     */
    public Object escape(Object value, ValueTransformer transformer)
    {
        Object escValue =
                (value instanceof String) ? escapeString((String) value)
                        : value;
        return transformer.transformValue(escValue);
    }

    /**
     * Actually splits the passed in string which is guaranteed to be not
     * <b>null</b>. This method is called by the base implementation of the
     * {@code split()} method. Here the actual splitting logic has to be
     * implemented.
     *
     * @param s the string to be split (not <b>null</b>)
     * @param trim a flag whether the single components have to be trimmed
     * @return a collection with the extracted components of the passed in
     *         string
     */
    protected abstract Collection<String> splitString(String s, boolean trim);

    /**
     * Escapes the specified string. This method is called by {@code escape()}
     * if the passed in object is a string. Concrete subclasses have to
     * implement their specific escaping logic here, so that the list delimiters
     * they support are properly escaped.
     *
     * @param s the string to be escaped (not <b>null</b>)
     * @return the escaped string
     */
    protected abstract String escapeString(String s);

    /**
     * Performs the actual work as advertised by the {@code parse()} method. The
     * passed in object is evaluated (if necessary, in a recursive way), and all
     * simple value objects it contains are extracted. They are returned as a
     * collection.
     *
     * @param value the value to be processed
     * @return a &quot;flat&quot; collection containing all primitive values of
     *         the passed in object
     */
    private Collection<?> flatten(Object value)
    {
        if (value instanceof String)
        {
            return split((String) value, true);
        }

        Collection<Object> result = new LinkedList<Object>();
        if (value instanceof Iterable)
        {
            flattenIterator(result, ((Iterable<?>) value).iterator());
        }
        else if (value instanceof Iterator)
        {
            flattenIterator(result, (Iterator<?>) value);
        }
        else if (value != null)
        {
            if (value.getClass().isArray())
            {
                for (int len = Array.getLength(value), idx = 0; idx < len; idx++)
                {
                    result.addAll(flatten(Array.get(value, idx)));
                }
            }
            else
            {
                result.add(value);
            }
        }

        return result;
    }

    /**
     * Flattens the given iterator. For each element in the iteration
     * {@code flatten()} is called recursively.
     *
     * @param target the target collection
     * @param it the iterator to process
     */
    private void flattenIterator(Collection<Object> target, Iterator<?> it)
    {
        while (it.hasNext())
        {
            target.addAll(flatten(it.next()));
        }
    }
}
