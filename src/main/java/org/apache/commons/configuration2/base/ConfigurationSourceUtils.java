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

import java.util.Collection;
import java.util.Iterator;

/**
 * <p>
 * A class with utility methods for dealing with configuration sources.
 * </p>
 * <p>
 * This class can be used for safely calling methods on a
 * {@link ConfigurationSource} object that are optional. If the
 * {@link ConfigurationSource} implements the method, it is invoked directly.
 * Otherwise, a default implementation is provided that will be called.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public final class ConfigurationSourceUtils
{
    /**
     * Private constructor. This class contains only static utility methods, so
     * no instances need to be created.
     */
    private ConfigurationSourceUtils()
    {
    }

    /**
     * Calls {@code isEmpty()} on the specified {@code ConfigurationSource}. If
     * the source does not implement this method, a default algorithm is used
     * for finding out whether the source is empty.
     *
     * @param source the {@code ConfigurationSource} to be tested (must not be
     *        <b>null</b>
     * @return <b>true</b> if the configuration source is empty, <b>false</b>
     *         otherwise
     * @throws IllegalArgumentException if the source is <b>null</b>
     * @see ConfigurationSource#isEmpty()
     */
    public static boolean isEmpty(ConfigurationSource source)
    {
        checkNullSource(source);
        try
        {
            return source.isEmpty();
        }
        catch (UnsupportedOperationException uoex)
        {
            Iterator<String> it = source.getKeys();
            return !it.hasNext();
        }
    }

    /**
     * Calls the {@code size()} method on the specified {@code
     * ConfigurationSource}. If the source does not implement this method, a
     * default algorithm is used for determining the size of the configuration
     * source.
     *
     * @param source the {@code ConfigurationSource} (must not be <b>null</b>)
     * @return the size of the {@code ConfigurationSource}
     * @throws IllegalArgumentException if the source is <b>null</b>
     * @see ConfigurationSource#size()
     */
    public static int size(ConfigurationSource source)
    {
        checkNullSource(source);

        try
        {
            return source.size();
        }
        catch (UnsupportedOperationException uoex)
        {
            int count = 0;
            for (Iterator<String> it = source.getKeys(); it.hasNext();)
            {
                count += valueCount(source, it.next());
            }
            return count;
        }
    }

    /**
     * Calls the {@code valueCount()} method on the specified {@code
     * ConfigurationSource}. If the source does not implement this method, a
     * default algorithm is used to determine the number of values stored for
     * this property.
     *
     * @param source the {@code ConfigurationSource} (must not be <b>null</b>)
     * @param key the key of the property to be tested
     * @return the number of values stored for this property
     * @throws IllegalArgumentException if the source is <b>null</b>
     * @see ConfigurationSource#valueCount(String)
     */
    public static int valueCount(ConfigurationSource source, String key)
    {
        checkNullSource(source);

        try
        {
            return source.valueCount(key);
        }
        catch (UnsupportedOperationException uoex)
        {
            Object value = source.getProperty(key);
            if (value instanceof Collection)
            {
                return ((Collection<?>) value).size();
            }
            else
            {
                return (value == null) ? 0 : 1;
            }
        }
    }

    /**
     * Calls the {@code getKeys(String prefix)} method on the specified {@code
     * ConfigurationSource}. If the source does not implement this method, a
     * default algorithm is used to obtain an iteration over all keys starting
     * with the specified prefix.
     *
     * @param source the {@code ConfigurationSource} (must not be <b>null</b>)
     * @param prefix the prefix of the desired keys
     * @return an {@code Iterator} over all keys contained in this {@code
     *         ConfigurationSource} starting with the given prefix
     * @throws IllegalArgumentException if the source is <b>null</b>
     * @see ConfigurationSource#getKeys(String)
     */
    public static Iterator<String> getKeys(ConfigurationSource source,
            String prefix)
    {
        checkNullSource(source);

        try
        {
            return source.getKeys(prefix);
        }
        catch (UnsupportedOperationException uoex)
        {
            return new PrefixedKeysIterator(source.getKeys(), prefix);
        }
    }

    /**
     * Helper method for checking for a <b>null</b> parameter. If the specified
     * source is <b>null</b>, an exception is thrown.
     *
     * @param source the source in question
     * @throws IllegalArgumentException if the source is <b>null</b>
     */
    private static void checkNullSource(ConfigurationSource source)
    {
        if (source == null)
        {
            throw new IllegalArgumentException(
                    "ConfigurationSource must not be null!");
        }
    }
}
