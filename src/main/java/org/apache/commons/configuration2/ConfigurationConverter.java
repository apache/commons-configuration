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

package org.apache.commons.configuration2;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * Configuration converter. Helper class to convert between Configuration,
 * ExtendedProperties and standard Properties.
 *
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 */
public final class ConfigurationConverter
{
    /** Constant for the default separator for properties with multiple values. */
    private static final char DEFAULT_SEPARATOR = ',';

    /**
     * Private constructor prevents instances from being created.
     */
    private ConfigurationConverter()
    {
        // to prevent instanciation...
    }

    /**
     * Convert a standard Properties class into a configuration class.
     *
     * @param props properties object to convert
     * @return Configuration configuration created from the Properties
     */
    public static Configuration getConfiguration(final Properties props)
    {
        return new MapConfiguration(props);
    }

    /**
     * Convert a Configuration class into a Properties class. List properties
     * are joined into a string using either the list delimiter handler of the
     * configuration (if it extends AbstractConfiguration) or with a comma as
     * delimiter otherwise.
     *
     * @param config ImmutableConfiguration object to convert
     * @return Properties created from the Configuration
     * @since 2.2
     */
    public static Properties getProperties(final ImmutableConfiguration config)
    {
        final Properties props = new Properties();
        ListDelimiterHandler listHandler;
        boolean useDelimiterHandler;

        if (config instanceof AbstractConfiguration)
        {
            listHandler = ((AbstractConfiguration) config).getListDelimiterHandler();
            useDelimiterHandler = true;
        }
        else
        {
            listHandler = null;
            useDelimiterHandler = false;
        }

        for (final Iterator<String> keys = config.getKeys(); keys.hasNext();)
        {
            final String key = keys.next();
            final List<Object> list = config.getList(key);

            String propValue;
            if (useDelimiterHandler)
            {
                try
                {
                    propValue =
                            String.valueOf(listHandler.escapeList(list,
                                    ListDelimiterHandler.NOOP_TRANSFORMER));
                }
                catch (final Exception ex)
                {
                    // obviously, the list handler does not support splitting
                    useDelimiterHandler = false;
                    propValue = listToString(list);
                }
            }
            else
            {
                propValue = listToString(list);
            }

            props.setProperty(key, propValue);
        }

        return props;
    }

    /**
     * Convert a Configuration class into a Properties class. List properties
     * are joined into a string using either the list delimiter handler of the
     * configuration (if it extends AbstractConfiguration) or with a comma as
     * delimiter otherwise.
     * This version of the method exists only for backwards compatibility reason.
     *
     * @param config Configuration object to convert
     * @return Properties created from the Configuration
     */
    public static Properties getProperties(final Configuration config)
    {
        return getProperties((ImmutableConfiguration) config);
    }

    /**
     * Convert a Configuration class into a Map class.
     *
     * @param config Configuration object to convert
     * @return Map created from the Configuration
     */
    public static Map<Object, Object> getMap(final Configuration config)
    {
        return new ConfigurationMap(config);
    }

    /**
     * Helper method for joining all elements of a list to a string using the
     * default value separator.
     *
     * @param list the list
     * @return the resulting string
     */
    private static String listToString(final List<?> list)
    {
        return StringUtils.join(list, DEFAULT_SEPARATOR);
    }
}
