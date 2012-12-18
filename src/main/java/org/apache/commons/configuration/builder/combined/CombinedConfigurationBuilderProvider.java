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
import java.util.Collection;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.builder.BasicBuilderParameters;
import org.apache.commons.configuration.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration.builder.BuilderParameters;

/**
 * <p>
 * A specialized {@code ConfigurationBuilderProvider} implementation which deals
 * with combined configuration builders.
 * </p>
 * <p>
 * This class is used to support {@code <configuration>} elements in
 * configuration definition files. The provider creates another
 * {@link CombinedConfigurationBuilder} which inherits some of the properties
 * from its parent builder.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public class CombinedConfigurationBuilderProvider extends
        BaseConfigurationBuilderProvider
{
    /** Constant for the name of the supported builder class. */
    private static final String BUILDER_CLASS =
            "org.apache.commons.configuration.builder.combined.CombinedConfigurationBuilder";

    /** Constant for the name of the supported configuration class. */
    private static final String CONFIGURATION_CLASS =
            "org.apache.commons.configuration.CombinedConfiguration";

    /** Constant for the combined configuration builder parameters class. */
    private static final String COMBINED_PARAMS =
            "org.apache.commons.configuration.builder.combined.CombinedBuilderParametersImpl";

    /** Constant for the name of the file-based builder parameters class. */
    private static final String FILE_PARAMS =
            "org.apache.commons.configuration.builder.FileBasedBuilderParametersImpl";

    /**
     * Creates a new instance of {@code CombinedConfigurationBuilderProvider}.
     */
    public CombinedConfigurationBuilderProvider()
    {
        super(BUILDER_CLASS, BUILDER_CLASS, CONFIGURATION_CLASS, Arrays.asList(
                COMBINED_PARAMS, FILE_PARAMS));
    }

    /**
     * {@inheritDoc} This implementation creates the result builder object
     * directly, not using reflection. (The reflection-based approach of the
     * base class does not work here because a combined configuration builder
     * has constructors with a different signature.) It also performs some
     * additional initializations.
     */
    @Override
    protected BasicConfigurationBuilder<? extends Configuration> createBuilder(
            ConfigurationDeclaration decl, Collection<BuilderParameters> params)
            throws Exception
    {
        CombinedConfigurationBuilder builder =
                new CombinedConfigurationBuilder();
        decl.getConfigurationBuilder().initChildEventListeners(builder);
        return builder;
    }

    /**
     * {@inheritDoc} This implementation pre-fills basic parameters from the
     * basic properties of the parent builder's result configuration.
     */
    @Override
    protected void initializeParameterObjects(ConfigurationDeclaration decl,
            Collection<BuilderParameters> params) throws Exception
    {
        // we know that the first object is the combined builder parameters
        // object
        BasicBuilderParameters basicParams =
                (BasicBuilderParameters) params.iterator().next();
        setUpBasicParameters(decl.getConfigurationBuilder()
                .getConfigurationUnderConstruction(), basicParams);
        // now properties set explicitly can be overridden
        super.initializeParameterObjects(decl, params);
    }

    /**
     * Populates the specified parameters object with properties from the given
     * configuration. This method is used to set default values for basic
     * properties based on the result configuration of the parent builder.
     *
     * @param config the configuration whose properties are to be copied
     * @param params the target parameters object
     */
    private static void setUpBasicParameters(CombinedConfiguration config,
            BasicBuilderParameters params)
    {
        params.setDelimiterParsingDisabled(config.isDelimiterParsingDisabled())
                .setListDelimiter(config.getListDelimiter())
                .setLogger(config.getLogger())
                .setThrowExceptionOnMissing(config.isThrowExceptionOnMissing());
    }
}
