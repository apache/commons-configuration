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

import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.builder.BuilderConfigurationWrapperFactory;
import org.apache.commons.configuration.builder.BuilderConfigurationWrapperFactory.EventSourceSupport;
import org.apache.commons.configuration.builder.BuilderListener;
import org.apache.commons.configuration.builder.ConfigurationBuilder;

/**
 * <p>
 * A specialized {@code ConfigurationBuilderProvider} implementation for
 * integrating {@link MultiFileConfigurationBuilder} with
 * {@code CombinedConfigurationBuilder}.
 * </p>
 * <p>
 * When using a configuration source managed by
 * {@code MultiFileConfigurationBuilder} it is not sufficient to store the
 * configuration once obtained from the builder in the resulting combined
 * configuration. Rather, it has to be ensured that each access to this
 * configuration queries the builder anew so that it can evaluate its file
 * pattern and return a different configuration if necessary. Therefore, this
 * class returns a specialized wrapper over a
 * {@code MultiFileConfigurationBuilder} which returns a configuration wrapping
 * the builder; so accessing the configuration's properties actually calls back
 * to the builder. This constellation is compatible with the way
 * {@code DynamicCombinedConfiguration} manages its data.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public class MultiFileConfigurationBuilderProvider extends
        BaseConfigurationBuilderProvider
{
    /** Constant for the name of the builder class. */
    private static final String BUILDER_CLASS =
            "org.apache.commons.configuration.builder.combined.MultiFileConfigurationBuilder";

    /** Constant for the name of the reloading builder class. */
    private static final String RELOADING_BUILDER_CLASS =
            "org.apache.commons.configuration.builder.combined.ReloadingMultiFileConfigurationBuilder";

    /** Constant for the name of the parameters class. */
    private static final String PARAM_CLASS =
            "org.apache.commons.configuration.builder.combined.MultiFileBuilderParametersImpl";

    /**
     * Creates a new instance of {@code MultiFileConfigurationBuilderProvider}
     * and sets the name of the configuration class to be returned by
     * {@code MultiFileConfigurationBuilder}.
     *
     * @param configCls the name of the managed configuration class
     * @param paramCls the name of the class of the parameters object to
     *        configure the managed configuration
     */
    public MultiFileConfigurationBuilderProvider(String configCls,
            String paramCls)
    {
        super(BUILDER_CLASS, RELOADING_BUILDER_CLASS, configCls, Arrays.asList(
                paramCls, PARAM_CLASS));
    }

    /**
     * {@inheritDoc} This implementation lets the super class create a fully
     * configured builder. Then it returns a special wrapper around it.
     */
    @Override
    public ConfigurationBuilder<? extends Configuration> getConfigurationBuilder(
            ConfigurationDeclaration decl) throws ConfigurationException
    {
        ConfigurationBuilder<? extends Configuration> multiBuilder =
                super.getConfigurationBuilder(decl);
        Configuration wrapConfig = createWrapperConfiguration(multiBuilder);
        return createWrapperBuilder(multiBuilder, wrapConfig);
    }

    /**
     * Creates a configuration which wraps the specified builder.
     *
     * @param builder the builder
     * @return the wrapping configuration
     */
    // It is safe to disable any type checks because we manually determine
    // the interface class to be passed to BuilderConfigurationWrapperFactory
    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    private Configuration createWrapperConfiguration(
            ConfigurationBuilder builder)
    {
        Class<?> configClass =
                ConfigurationUtils.loadClassNoEx(getConfigurationClass());
        Class ifcClass =
                HierarchicalConfiguration.class.isAssignableFrom(configClass) ? HierarchicalConfiguration.class
                        : Configuration.class;
        return BuilderConfigurationWrapperFactory
                .createBuilderConfigurationWrapper(ifcClass, builder,
                        EventSourceSupport.BUILDER_OPTIONAL);
    }

    /**
     * Creates the {@code ConfigurationBuilder} to be returned by this provider.
     * This is a very simple implementation which always returns the same
     * wrapper configuration instance. The handling of builder listeners is
     * delegated to the wrapped {@code MultiFileConfigurationBuilder}.
     *
     * @param multiBuilder the {@code MultiFileConfigurationBuilder}
     * @param wrapConfig the configuration to be returned
     * @return the wrapper builder
     */
    private static ConfigurationBuilder<? extends Configuration> createWrapperBuilder(
            final ConfigurationBuilder<? extends Configuration> multiBuilder,
            final Configuration wrapConfig)
    {
        return new ConfigurationBuilder<Configuration>()
        {
            public Configuration getConfiguration()
                    throws ConfigurationException
            {
                return wrapConfig;
            }

            public void addBuilderListener(BuilderListener l)
            {
                multiBuilder.addBuilderListener(l);
            }

            public void removeBuilderListener(BuilderListener l)
            {
                multiBuilder.removeBuilderListener(l);
            }
        };
    }
}
