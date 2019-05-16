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
package org.apache.commons.configuration2.builder.combined;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.reloading.CombinedReloadingController;
import org.apache.commons.configuration2.reloading.ReloadingController;
import org.apache.commons.configuration2.reloading.ReloadingControllerSupport;

/**
 * <p>
 * A specialized {@code MultiFileConfigurationBuilder} implementation which adds
 * support for reloading.
 * </p>
 * <p>
 * This class - as its super class - allows operating on multiple configuration
 * files whose file names are determined using a file name pattern and a
 * {@code ConfigurationInterpolator} object. It provides the following
 * additional features:
 * </p>
 * <ul>
 * <li>Configuration builder for managed configurations have reloading support.
 * So reloading is possible for all configuration sources loaded by this builder
 * instance.</li>
 * <li>A {@link ReloadingController} is provided which can be used to trigger
 * reload checks on all managed configurations.</li>
 * </ul>
 * <p>
 * Although this builder manages an arbitrary number of child configurations, to
 * clients only a single configuration is visible - the one selected by the
 * evaluation of the file name pattern. Builder reset notifications triggered by
 * the reloading mechanism do not really take this fact into account; they are
 * not limited to the currently selected child configuration, but occur for each
 * of the managed configuration.
 * </p>
 *
 * @since 2.0
 * @param <T> the concrete type of {@code Configuration} objects created by this
 *        builder
 */
public class ReloadingMultiFileConfigurationBuilder<T extends FileBasedConfiguration>
        extends MultiFileConfigurationBuilder<T> implements
        ReloadingControllerSupport
{
    /** The reloading controller used by this builder. */
    private final ReloadingController reloadingController =
            createReloadingController();

    /**
     * Creates a new instance of {@code ReloadingMultiFileConfigurationBuilder}
     * and sets initialization parameters and a flag whether initialization
     * failures should be ignored.
     *
     * @param resCls the result configuration class
     * @param params a map with initialization parameters
     * @param allowFailOnInit a flag whether initialization errors should be
     *        ignored
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public ReloadingMultiFileConfigurationBuilder(final Class<T> resCls,
            final Map<String, Object> params, final boolean allowFailOnInit)
    {
        super(resCls, params, allowFailOnInit);
    }

    /**
     * Creates a new instance of {@code ReloadingMultiFileConfigurationBuilder}
     * and sets initialization parameters.
     *
     * @param resCls the result configuration class
     * @param params a map with initialization parameters
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public ReloadingMultiFileConfigurationBuilder(final Class<T> resCls,
            final Map<String, Object> params)
    {
        super(resCls, params);
    }

    /**
     * Creates a new instance of {@code ReloadingMultiFileConfigurationBuilder}
     * without setting initialization parameters.
     *
     * @param resCls the result configuration class
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public ReloadingMultiFileConfigurationBuilder(final Class<T> resCls)
    {
        super(resCls);
    }

    /**
     * {@inheritDoc} This implementation returns a special
     * {@code ReloadingController} that delegates to the reloading controllers
     * of the managed builders created so far.
     */
    @Override
    public ReloadingController getReloadingController()
    {
        return reloadingController;
    }

    /**
     * {@inheritDoc} This implementation returns a file-based configuration
     * builder with reloading support.
     */
    @Override
    protected FileBasedConfigurationBuilder<T> createManagedBuilder(
            final String fileName, final Map<String, Object> params)
            throws ConfigurationException
    {
        return new ReloadingFileBasedConfigurationBuilder<>(getResultClass(),
                params, isAllowFailOnInit());
    }

    /**
     * Creates the reloading controller used by this builder. This method
     * creates a specialized {@link CombinedReloadingController} which operates
     * on the reloading controllers of the managed builders created so far.
     *
     * @return the newly created {@code ReloadingController}
     */
    private ReloadingController createReloadingController()
    {
        final Set<ReloadingController> empty = Collections.emptySet();
        return new CombinedReloadingController(empty)
        {
            @Override
            public Collection<ReloadingController> getSubControllers()
            {
                final Collection<FileBasedConfigurationBuilder<T>> builders =
                        getManagedBuilders().values();
                final Collection<ReloadingController> controllers =
                        new ArrayList<>(builders.size());
                for (final FileBasedConfigurationBuilder<T> b : builders)
                {
                    controllers.add(((ReloadingControllerSupport) b)
                            .getReloadingController());
                }
                return controllers;
            }
        };
    }
}
