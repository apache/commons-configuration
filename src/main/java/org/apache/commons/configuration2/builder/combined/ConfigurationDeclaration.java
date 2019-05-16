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

import java.util.Set;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.beanutils.XMLBeanDeclaration;

/**
 * <p>
 * A specialized {@code BeanDeclaration} implementation that represents the
 * declaration of a configuration source.
 * </p>
 * <p>
 * Instances of this class are able to extract all information about a
 * configuration source from the configuration definition file. The declaration
 * of a configuration source is very similar to a bean declaration processed by
 * {@code XMLBeanDeclaration}. There are very few differences, e.g. some
 * reserved attributes like {@code optional} and {@code at}, and the fact that a
 * bean factory is never needed.
 * </p>
 *
 * @since 2.0
 */
public class ConfigurationDeclaration extends XMLBeanDeclaration
{
    /** Stores a reference to the associated configuration builder. */
    private final CombinedConfigurationBuilder configurationBuilder;

    /**
     * Creates a new instance of {@code ConfigurationDeclaration} and
     * initializes it.
     *
     * @param builder the associated configuration builder
     * @param config the configuration this declaration is based onto
     */
    public ConfigurationDeclaration(final CombinedConfigurationBuilder builder,
            final HierarchicalConfiguration<?> config)
    {
        super(config);
        configurationBuilder = builder;
    }

    /**
     * Returns the associated configuration builder.
     *
     * @return the configuration builder
     */
    public CombinedConfigurationBuilder getConfigurationBuilder()
    {
        return configurationBuilder;
    }

    /**
     * Returns the value of the {@code at} attribute.
     *
     * @return the value of the {@code at} attribute (can be <b>null</b>)
     */
    public String getAt()
    {
        final String result =
                this.getConfiguration().getString(
                        CombinedConfigurationBuilder.ATTR_AT_RES);
        return (result == null) ? this.getConfiguration().getString(
                CombinedConfigurationBuilder.ATTR_AT) : result;
    }

    /**
     * Returns a flag whether this is an optional configuration.
     *
     * @return a flag if this declaration points to an optional configuration
     */
    public boolean isOptional()
    {
        Boolean value =
                this.getConfiguration().getBoolean(
                        CombinedConfigurationBuilder.ATTR_OPTIONAL_RES, null);
        if (value == null)
        {
            value =
                    this.getConfiguration().getBoolean(
                            CombinedConfigurationBuilder.ATTR_OPTIONAL,
                            Boolean.FALSE);
        }
        return value.booleanValue();
    }

    /**
     * Returns a flag whether this configuration should always be created and
     * added to the resulting combined configuration. This flag is evaluated
     * only for optional configurations whose normal creation has caused an
     * error. If for such a configuration the {@code forceCreate} attribute is
     * set and the corresponding configuration provider supports this mode, an
     * empty configuration will be created and added to the resulting combined
     * configuration.
     *
     * @return the value of the {@code forceCreate} attribute
     */
    public boolean isForceCreate()
    {
        return this.getConfiguration().getBoolean(
                CombinedConfigurationBuilder.ATTR_FORCECREATE, false);
    }

    /**
     * Returns a flag whether a builder with reloading support should be
     * created. This may not be supported by all configuration builder
     * providers.
     *
     * @return a flag whether a reloading builder should be created
     */
    public boolean isReload()
    {
        return getConfiguration().getBoolean(
                CombinedConfigurationBuilder.ATTR_RELOAD, false);
    }

    /**
     * Returns the name for the represented configuration source. The name is
     * optional, so this method can return <b>null</b>.
     *
     * @return the name of the associated configuration source or <b>null</b>
     */
    public String getName()
    {
        return getConfiguration().getString(
                CombinedConfigurationBuilder.ATTR_NAME);
    }

    /**
     * Returns the name of the bean factory. For configuration source
     * declarations always a reserved factory is used. This factory's name is
     * returned by this implementation.
     *
     * @return the name of the bean factory
     */
    @Override
    public String getBeanFactoryName()
    {
        return CombinedConfigurationBuilder.CONFIG_BEAN_FACTORY_NAME;
    }

    /**
     * Returns the bean's class name. This implementation will always return
     * <b>null</b>.
     *
     * @return the name of the bean's class
     */
    @Override
    public String getBeanClassName()
    {
        return null;
    }

    /**
     * {@inheritDoc} This implementation checks for additional reserved
     * attribute names. Note that in some cases the presence of other attribute
     * names determine whether a name is reserved or not. For instance, per
     * default the attribute {@code config-at} is reserved. However, if this
     * attribute is not present, the attribute {@code at} is also considered as
     * a reserved attribute. (This is mainly done for dealing with legacy
     * configuration files supported by earlier versions of this library.)
     */
    @Override
    protected boolean isReservedAttributeName(final String name)
    {
        if (super.isReservedAttributeName(name))
        {
            return true;
        }

        final Set<String> attributes = getAttributeNames();
        return (CombinedConfigurationBuilder.ATTR_ATNAME.equals(name) && !attributes
                .contains(RESERVED_PREFIX
                        + CombinedConfigurationBuilder.ATTR_ATNAME))
                || (CombinedConfigurationBuilder.ATTR_OPTIONALNAME.equals(name) && !attributes
                        .contains(RESERVED_PREFIX
                                + CombinedConfigurationBuilder.ATTR_OPTIONALNAME));
    }
}
