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
package org.apache.commons.configuration.builder.combined;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.builder.BuilderParameters;
import org.apache.commons.configuration.builder.ConfigurationBuilder;
import org.apache.commons.configuration.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration.reloading.CombinedReloadingController;
import org.apache.commons.configuration.reloading.ReloadingController;
import org.apache.commons.configuration.reloading.ReloadingControllerSupport;

/**
 * <p>
 * An extension of {@code CombinedConfigurationBuilder} which also supports
 * reloading operations.
 * </p>
 * <p>
 * This class differs from its super class in the following aspects:
 * <ul>
 * <li>A {@link ReloadingController} is created which manages all child
 * configuration builders supporting reloading operations.</li>
 * <li>If no {@code ConfigurationBuilder} is provided for the definition
 * configuration, a builder with reloading support is created.</li>
 * </ul>
 * </p>
 * <p>
 * This class can be used exactly as its super class for creating combined
 * configurations from multiple configuration sources. In addition, the combined
 * reloading controller managed by an instance can be used to react on changes
 * in one of these configuration sources or in the definition configuration.
 * </p>
 *
 * @version $Id$
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
    public ReloadingCombinedConfigurationBuilder(Map<String, Object> params,
            boolean allowFailOnInit)
    {
        super(params, allowFailOnInit);
    }

    /**
     * Creates a new instance of {@code ReloadingCombinedConfigurationBuilder}
     * and sets the specified initialization parameters.
     *
     * @param params a map with initialization parameters
     */
    public ReloadingCombinedConfigurationBuilder(Map<String, Object> params)
    {
        super(params);
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
    public synchronized ReloadingController getReloadingController()
    {
        return reloadingController;
    }

    /**
     * {@inheritDoc} This implementation creates a builder for XML
     * configurations with reloading support.
     */
    @Override
    protected ConfigurationBuilder<? extends HierarchicalConfiguration> createXMLDefinitionBuilder(
            BuilderParameters builderParams)
    {
        return new ReloadingFileBasedConfigurationBuilder<XMLConfiguration>(
                XMLConfiguration.class).configure(builderParams);
    }

    /**
     * {@inheritDoc} This implementation first calls the super method to
     * actually initialize the result configuration. Then it creates the
     * {@link CombinedReloadingController} for all child configuration sources
     * with reloading support.
     */
    @Override
    protected void initResultInstance(CombinedConfiguration result)
            throws ConfigurationException
    {
        super.initResultInstance(result);
        reloadingController = createReloadingController();
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
        Collection<ReloadingController> subControllers =
                new LinkedList<ReloadingController>();
        ConfigurationBuilder<? extends HierarchicalConfiguration> defBuilder =
                getDefinitionBuilder();
        obtainReloadingController(subControllers, defBuilder);

        for (ConfigurationBuilder<? extends Configuration> b : getChildBuilders())
        {
            obtainReloadingController(subControllers, b);
        }

        return new CombinedReloadingController(subControllers);
    }

    /**
     * Checks whether the passed in builder object supports reloading. If yes,
     * its reloading controller is obtained and added to the given list.
     *
     * @param subControllers the list with sub controllers
     * @param builder the builder object to be checked
     */
    public static void obtainReloadingController(
            Collection<ReloadingController> subControllers, Object builder)
    {
        if (builder instanceof ReloadingControllerSupport)
        {
            subControllers.add(((ReloadingControllerSupport) builder)
                    .getReloadingController());
        }
    }
}
