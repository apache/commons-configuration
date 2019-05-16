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
package org.apache.commons.configuration2.io;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * <p>
 * A specialized implementation of a {@code FileLocationStrategy} which
 * encapsulates an arbitrary number of {@code FileLocationStrategy} objects.
 * </p>
 * <p>
 * A collection with the wrapped {@code FileLocationStrategy} objects is passed
 * at construction time. During a [{@code locate()} operation the wrapped
 * strategies are called one after the other until one returns a non <b>null</b>
 * URL. This URL is returned. If none of the wrapped strategies is able to
 * resolve the passed in {@link FileLocator}, result is <b>null</b>. This is
 * similar to the <em>chain of responsibility</em> design pattern.
 * </p>
 * <p>
 * This class, together with the provided concrete {@code FileLocationStrategy}
 * implementations, offers a convenient way to customize the lookup for
 * configuration files: Just add the desired concrete strategies to a
 * {@code CombinedLocationStrategy} object. If necessary, custom strategies can
 * be implemented if there are specific requirements. Note that the order in
 * which strategies are added to a {@code CombinedLocationStrategy} matters: sub
 * strategies are queried in the same order as they appear in the collection
 * passed to the constructor.
 * </p>
 *
 * @since 2.0
 */
public class CombinedLocationStrategy implements FileLocationStrategy
{
    /** A collection with all sub strategies managed by this object. */
    private final Collection<FileLocationStrategy> subStrategies;

    /**
     * Creates a new instance of {@code CombinedLocationStrategy} and
     * initializes it with the provided sub strategies. The passed in collection
     * must not be <b>null</b> or contain <b>null</b> elements.
     *
     * @param subs the collection with sub strategies
     * @throws IllegalArgumentException if the collection is <b>null</b> or has
     *         <b>null</b> elements
     */
    public CombinedLocationStrategy(
            final Collection<? extends FileLocationStrategy> subs)
    {
        if (subs == null)
        {
            throw new IllegalArgumentException(
                    "Collection with sub strategies must not be null!");
        }
        subStrategies =
                Collections
                        .unmodifiableCollection(new ArrayList<>(
                                subs));
        if (subStrategies.contains(null))
        {
            throw new IllegalArgumentException(
                    "Collection with sub strategies contains null entry!");
        }
    }

    /**
     * Returns a (unmodifiable) collection with the sub strategies managed by
     * this object.
     *
     * @return the sub {@code FileLocationStrategy} objects
     */
    public Collection<FileLocationStrategy> getSubStrategies()
    {
        return subStrategies;
    }

    /**
     * {@inheritDoc} This implementation tries to locate the file by delegating
     * to the managed sub strategies.
     */
    @Override
    public URL locate(final FileSystem fileSystem, final FileLocator locator)
    {
        for (final FileLocationStrategy sub : getSubStrategies())
        {
            final URL url = sub.locate(fileSystem, locator);
            if (url != null)
            {
                return url;
            }
        }

        return null;
    }
}
