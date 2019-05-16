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

import java.util.Map;

import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.builder.BasicBuilderParameters;
import org.apache.commons.configuration2.builder.BuilderParameters;

/**
 * <p>
 * A specialized parameters object for {@link MultiFileConfigurationBuilder}.
 * </p>
 * <p>
 * A parameters object of this type is used by a configuration builder with
 * manages multiple file-based configurations. Such a builder is a bit special
 * because it does not create a configuration on its own, but delegates to a
 * file-based builder for this purpose. Therefore, parameters inherited from the
 * super class are treated differently:
 * </p>
 * <ul>
 * <li>The {@link org.apache.commons.configuration2.interpol.ConfigurationInterpolator
 * ConfigurationInterpolator} is needed by a
 * {@code MultiFileConfigurationBuilder} to resolve the file pattern. It is
 * expected to be set and will not be passed to sub configurations created by
 * the builder.</li>
 * <li>All other parameters are evaluated when creating sub configurations.
 * However, it is preferred to use the
 * {@link #setManagedBuilderParameters(BuilderParameters)} method to define all
 * properties of sub configurations in a single place. If such a parameters
 * object is set, its properties take precedence.</li>
 * </ul>
 * <p>
 * This class is not thread-safe. It is intended that an instance is constructed
 * and initialized by a single thread during configuration of a
 * {@code ConfigurationBuilder}.
 * </p>
 *
 * @since 2.0
 */
public class MultiFileBuilderParametersImpl extends BasicBuilderParameters
        implements MultiFileBuilderProperties<MultiFileBuilderParametersImpl>
{
    /** Constant for the key in the parameters map used by this class. */
    private static final String PARAM_KEY = RESERVED_PARAMETER_PREFIX
            + MultiFileBuilderParametersImpl.class.getName();

    /** The parameters object for managed builders. */
    private BuilderParameters managedBuilderParameters;

    /** The file pattern. */
    private String filePattern;

    /**
     * Obtains an instance of this class from the given map with parameters. If
     * this map does not contain an instance, result is <b>null</b>. This is
     * equivalent to {@code fromParameters(params, false)}.
     *
     * @param params the map with parameters (must not be <b>null</b>)
     * @return an instance of this class fetched from the map or <b>null</b>
     * @throws NullPointerException if the map with parameters is <b>null</b>
     */
    public static MultiFileBuilderParametersImpl fromParameters(
            final Map<String, Object> params)
    {
        return fromParameters(params, false);
    }

    /**
     * Obtains an instance of this class from the given map with parameters and
     * creates a new object if such an instance cannot be found. This method can
     * be used to obtain an instance from a map which has been created using the
     * {@code getParameters()} method. If the map does not contain an instance
     * under the expected key and the {@code createIfMissing} parameter is
     * <b>true</b>, a new instance is created. Otherwise, result is <b>null</b>.
     *
     * @param params the map with parameters (must not be <b>null</b>)
     * @param createIfMissing a flag whether a new instance should be created if
     *        necessary
     * @return an instance of this class fetched from the map or <b>null</b>
     * @throws NullPointerException if the map with parameters is <b>null</b>
     */
    public static MultiFileBuilderParametersImpl fromParameters(
            final Map<String, Object> params, final boolean createIfMissing)
    {
        MultiFileBuilderParametersImpl instance =
                (MultiFileBuilderParametersImpl) params.get(PARAM_KEY);
        if (instance == null && createIfMissing)
        {
            instance = new MultiFileBuilderParametersImpl();
        }
        return instance;
    }

    /**
     * Returns the pattern for determining file names for managed
     * configurations.
     *
     * @return the file pattern
     */
    public String getFilePattern()
    {
        return filePattern;
    }

    @Override
    public MultiFileBuilderParametersImpl setFilePattern(final String p)
    {
        filePattern = p;
        return this;
    }

    /**
     * Returns the parameters object for managed configuration builders.
     *
     * @return the parameters for sub configurations
     */
    public BuilderParameters getManagedBuilderParameters()
    {
        return managedBuilderParameters;
    }

    @Override
    public MultiFileBuilderParametersImpl setManagedBuilderParameters(
            final BuilderParameters p)
    {
        managedBuilderParameters = p;
        return this;
    }

    /**
     * {@inheritDoc} This implementation puts a reference to this object under a
     * reserved key in the resulting parameters map.
     */
    @Override
    public Map<String, Object> getParameters()
    {
        final Map<String, Object> params = super.getParameters();
        params.put(PARAM_KEY, this);
        return params;
    }

    /**
     * {@inheritDoc} This implementation also tries to clone the parameters
     * object for managed builders if possible.
     */
    @Override
    public MultiFileBuilderParametersImpl clone()
    {
        final MultiFileBuilderParametersImpl copy =
                (MultiFileBuilderParametersImpl) super.clone();
        copy.setManagedBuilderParameters((BuilderParameters) ConfigurationUtils
                .cloneIfPossible(getManagedBuilderParameters()));
        return copy;
    }
}
