/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Configuration converter. Helper class to convert between Configuration,
 * ExtendedProperties and standard Properties.
 *
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @version $Revision$, $Date$
 */
public final class ConfigurationConverter
{
    private ConfigurationConverter()
    {
        // to prevent instanciation...
    }

    /**
     * Convert a ExtendedProperties class into a Configuration class.
     *
     * @param eprops ExtendedProperties object to convert
     * @return Configuration created from the ExtendedProperties
     */
    public static Configuration getConfiguration(ExtendedProperties eprops)
    {
        return new MapConfiguration(eprops);
    }

    /**
     * Convert a standard Properties class into a configuration class.
     *
     * @param props properties object to convert
     * @return Configuration configuration created from the Properties
     */
    public static Configuration getConfiguration(Properties props)
    {
        return new MapConfiguration(props);
    }

    /**
     * Convert a Configuration class into a ExtendedProperties class.
     *
     * @param config Configuration object to convert
     * @return ExtendedProperties created from the Configuration
     */
    public static ExtendedProperties getExtendedProperties(Configuration config)
    {
        ExtendedProperties props = new ExtendedProperties();

        Iterator keys = config.getKeys();

        while (keys.hasNext())
        {
            String key = (String) keys.next();
            Object property = config.getProperty(key);

            // turn lists into vectors
            if (property instanceof List)
            {
                property = new Vector((List) property);
            }

            props.setProperty(key, property);
        }

        return props;
    }

    /**
     * Convert a Configuration class into a Properties class. Multivalue keys
     * will be collapsed into comma separated values.
     *
     * @param config Configuration object to convert
     * @return Properties created from the Configuration
     */
    public static Properties getProperties(Configuration config)
    {
        Properties props = new Properties();

        Iterator keys = config.getKeys();

        while (keys.hasNext())
        {
            String key = (String) keys.next();
            List list = config.getList(key);

            // turn lists into a string
            StringBuffer property = new StringBuffer();
            Iterator it = list.iterator();
            while (it.hasNext())
            {
                property.append(String.valueOf(it.next()));
                if (it.hasNext())
                {
                    property.append(", ");
                }
            }

            props.setProperty(key, property.toString());
        }

        return props;
    }

    /**
     * Convert a Configuration class into a Map class.
     *
     * @param config Configuration object to convert
     * @return Map created from the Configuration
     */
    public static Map getMap(Configuration config)
    {
        return new ConfigurationMap(config);
    }

}
