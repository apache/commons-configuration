/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration2.io;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A specialized implementation of a {@code FileLocationStrategy} which encapsulates an arbitrary number of
 * {@code FileLocationStrategy} objects.
 * <p>
 * A collection with the wrapped {@code FileLocationStrategy} objects is passed at construction time. During a
 * [{@code locate()} operation the wrapped strategies are called one after the other until one returns a non <strong>null</strong>
 * URL. This URL is returned. If none of the wrapped strategies is able to resolve the passed in {@link FileLocator},
 * result is <strong>null</strong>. This is similar to the <em>chain of responsibility</em> design pattern.
 * </p>
 * <p>
 * This class, together with the provided concrete {@code FileLocationStrategy} implementations, offers a convenient way
 * to customize the lookup for configuration files: Just add the desired concrete strategies to a
 * {@code CombinedLocationStrategy} object. If necessary, custom strategies can be implemented if there are specific
 * requirements. Note that the order in which strategies are added to a {@code CombinedLocationStrategy} matters: sub
 * strategies are queried in the same order as they appear in the collection passed to the constructor.
 * </p>
 * <p>
 * See {@link AbstractFileLocationStrategy} learn how to grant an deny URL schemes and hosts.
 * </p>
 *
 * @see AbstractFileLocationStrategy
 * @since 2.0
 */
public class CombinedLocationStrategy extends AbstractFileLocationStrategy {

    /**
     * Builds new instances of {@link CombinedLocationStrategy}.
     *
     * @since 2.15.0
     */
    public static class Builder extends AbstractBuilder<CombinedLocationStrategy, Builder> {

        /** A collection with all sub strategies managed by this object. */
        private Collection<? extends FileLocationStrategy> subStrategies;

        /**
         * Constructs a new instance.
         */
        public Builder() {
            // empty
        }

        @Override
        public CombinedLocationStrategy get() throws IOException {
            return new CombinedLocationStrategy(this);
        }

        /**
         * Propagates properties of the parent builder scheme and host to subStrategies.
         *
         * @return {@code this} instance.
         */
        public Builder propagate() {
            if (subStrategies != null) {
                subStrategies.forEach(e -> {
                    if (e instanceof AbstractFileLocationStrategy) {
                        final AbstractFileLocationStrategy afls = (AbstractFileLocationStrategy) e;
                        final Set<String> schemes = afls.getSchemes();
                        schemes.clear();
                        schemes.addAll(getSchemes());
                        final Set<Pattern> hosts = afls.getHosts();
                        hosts.clear();
                        hosts.addAll(getHosts());
                    }
                });
            }
            return asThis();
        }

        /**
         * Sets the collection with sub strategies.
         *
         * @param subStrategies The collection with sub strategies.
         * @return {@code this} instance.
         */
        public Builder setSubStrategies(final Collection<FileLocationStrategy> subStrategies) {
            this.subStrategies = subStrategies;
            return asThis();
        }

    }

    /** A collection with all sub strategies managed by this object. */
    private final Collection<FileLocationStrategy> subStrategies;

    /**
     * Constructs a new instance.
     *
     * @param builder How to build the instance.
     */
    private CombinedLocationStrategy(final Builder builder) {
        super(builder);
        if (builder.subStrategies == null) {
            throw new IllegalArgumentException("Collection with sub strategies must not be null.");
        }
        List<FileLocationStrategy> subStrategiesCopy = new ArrayList<>(builder.subStrategies);
        if (subStrategiesCopy.contains(null)) {
            throw new IllegalArgumentException("Collection with sub strategies contains null entry.");
        }
        subStrategies = Collections.unmodifiableCollection(subStrategiesCopy);
    }

    /**
     * Creates a new instance of {@code CombinedLocationStrategy} and initializes it with the provided sub strategies. The
     * passed in collection must not be <strong>null</strong> or contain <strong>null</strong> elements.
     *
     * @param subs The collection with sub strategies.
     * @throws IllegalArgumentException if the collection is <strong>null</strong> or has <strong>null</strong> elements.
     */
    public CombinedLocationStrategy(final Collection<FileLocationStrategy> subs) {
        this(new Builder().setSubStrategies(subs));
    }

    /**
     * Gets a (unmodifiable) collection with the sub strategies managed by this object.
     *
     * @return The sub {@code FileLocationStrategy} objects
     */
    public Collection<FileLocationStrategy> getSubStrategies() {
        return subStrategies;
    }

    /**
     * {@inheritDoc} This implementation tries to locate the file by delegating to the managed sub strategies.
     */
    @Override
    public URL locate(final FileSystem fileSystem, final FileLocator locator) {
        for (final FileLocationStrategy sub : getSubStrategies()) {
            final URL url = sub.locate(fileSystem, locator);
            if (url != null) {
                return check(url);
            }
        }
        return null;
    }
}
