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

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.ConfigurationBuilder;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.reloading.CombinedReloadingController;
import org.apache.commons.configuration2.reloading.ReloadingController;
import org.apache.commons.configuration2.reloading.ReloadingControllerSupport;

/**
 * <p>
 * An extension of {@code CombinedConfigurationBuilder} which also supports
 * reloading operations.
 * </p>
 * <p>
 * This class differs from its super class in the following aspects:
 * </p>
 * <ul>
 * <li>A {@link ReloadingController} is created which manages all child
 * configuration builders supporting reloading operations.</li>
 * <li>If no {@code ConfigurationBuilder} is provided for the definition
 * configuration, a builder with reloading support is created.</li>
 * </ul>
 * <p>
 * This class can be used exactly as its super class for creating combined
 * configurations from multiple configuration sources. In addition, the combined
 * reloading controller managed by an instance can be used to react on changes
 * in one of these configuration sources or in the definition configuration.
 * </p>
 *
 * @since 2.0
 */
public class ReloadingCombinedConfigurationBuilder extends
        CombinedConfigurationBuilder implements ReloadingControllerSupport
{
    /** The reloading controller used by this builder. */
    private ReloadingController reloadingController;

    /**
     * Creates a new instance of {@code ReloadingCombinedConfigurationBuilder}.
     * No parameters are set.
     */
    public ReloadingCombinedConfigurationBuilder()
    {
        super();
    }

    /**
     * Creates a new instance of {@code ReloadingCombinedConfigurationBuilder}
     * and sets the specified initialization parameters and the
     * <em>allowFailOnInit</em> flag.
     *
     * @param params a map with initialization parameters
     * @param allowFailOnInit the <em>allowFailOnInit</em> flag
     */
    public ReloadingCombinedConfigurationBuilder(final Map<String, Object> params,
            final boolean allowFailOnInit)
    {
        super(params, allowFailOnInit);
    }

    /**
     * Creates a new instance of {@code ReloadingCombinedConfigurationBuilder}
     * and sets the specified initialization parameters.
     *
     * @param params a map with initialization parameters
     */
    public ReloadingCombinedConfigurationBuilder(final Map<String, Object> params)
    {
        super(params);
    }

    /**
     * {@inheritDoc} This method is overridden to adapt the return type.
     */
    @Override
    public ReloadingCombinedConfigurationBuilder configure(final BuilderParameters... params)
    {
        super.configure(params);
        return this;
    }

    /**
     * {@inheritDoc} This implementation returns a
     * {@link CombinedReloadingController} which contains sub controllers for
     * all child configuration sources with reloading support. If the definition
     * builder supports reloading, its controller is contained, too. Note that
     * the combined reloading controller is initialized when the result
     * configuration is created (i.e. when calling {@code getConfiguration()}
     * for the first time). So this method does not return a meaningful result
     * before.
     */
    @Override
    public synchronized ReloadingController getReloadingController()
    {
        return reloadingController;
    }

    /**
     * {@inheritDoc} This implementation makes sure that the reloading state of
     * the managed reloading controller is reset. Note that this has to be done
     * here and not in {@link #initResultInstance(CombinedConfiguration)}
     * because it must be outside of a synchronized block; otherwise, a
     * dead-lock situation can occur.
     */
    @Override
    public CombinedConfiguration getConfiguration() throws ConfigurationException
    {
        final CombinedConfiguration result = super.getConfiguration();
        reloadingController.resetReloadingState();
        return result;
    }

    /**
     * {@inheritDoc} This implementation creates a builder for XML
     * configurations with reloading support.
     */
    @Override
    protected ConfigurationBuilder<? extends HierarchicalConfiguration<?>> createXMLDefinitionBuilder(
            final BuilderParameters builderParams)
    {
        return new ReloadingFileBasedConfigurationBuilder<>(
                XMLConfiguration.class).configure(builderParams);
    }

    /**
     * {@inheritDoc} This implementation first calls the super method to
     * actually initialize the result configuration. Then it creates the
     * {@link CombinedReloadingController} for all child configuration sources
     * with reloading support.
     */
    @Override
    protected void initResultInstance(final CombinedConfiguration result)
            throws ConfigurationException
    {
        super.initResultInstance(result);
        if (reloadingController == null)
        {
            reloadingController = createReloadingController();
        }
    }

    /**
     * Creates the {@code ReloadingController} for this builder. This method is
     * called after the result configuration has been created and initialized.
     * It is called from a synchronized block. This implementation creates a
     * {@link CombinedReloadingController}.
     *
     * @return the {@code ReloadingController} for this builder
     * @throws ConfigurationException if an error occurs
     */
    protected ReloadingController createReloadingController()
            throws ConfigurationException
    {
        final Collection<ReloadingController> subControllers =
                new LinkedList<>();
        final ConfigurationBuilder<? extends HierarchicalConfiguration<?>> defBuilder =
                getDefinitionBuilder();
        obtainReloadingController(subControllers, defBuilder);

        for (final ConfigurationBuilder<? extends Configuration> b : getChildBuilders())
        {
            obtainReloadingController(subControllers, b);
        }

        final CombinedReloadingController ctrl =
                new CombinedReloadingController(subControllers);
        ctrl.resetInitialReloadingState();
        return ctrl;
    }

    /**
     * Checks whether the passed in builder object supports reloading. If yes,
     * its reloading controller is obtained and added to the given list.
     *
     * @param subControllers the list with sub controllers
     * @param builder the builder object to be checked
     */
    public static void obtainReloadingController(
            final Collection<ReloadingController> subControllers, final Object builder)
    {
        if (builder instanceof ReloadingControllerSupport)
        {
            subControllers.add(((ReloadingControllerSupport) builder)
                    .getReloadingController());
        }
    }
}
