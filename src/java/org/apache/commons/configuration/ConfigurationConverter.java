package org.apache.commons.configuration;

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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.List;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;


/**
 * Configuration converter. <br>
 * Helper class to convert between Configuration, ExtendedProperties and
 * standard Properties.
 *
 * @version $Id: ConfigurationConverter.java,v 1.4 2004/06/02 16:42:24 ebourg Exp $
 */
public class ConfigurationConverter
{
    /**
     * Convert a ExtendedProperties class into a Configuration class.
     *
     * @param ep ExtendedProperties object to convert
     * @return Configuration created from the ExtendedProperties
     */
    public static Configuration getConfiguration(ExtendedProperties ep)
    {
        Configuration config = new BaseConfiguration();
        for (Iterator i = ep.getKeys(); i.hasNext();)
        {
            String key = (String) i.next();
            config.setProperty(key, ep.getProperty(key));
        }
        return config;
    }

    /**
     * Convert a standard properties class into a configuration class.
     *
     * @param p properties object to convert
     * @return Configuration configuration created from the Properties
     */
    public static Configuration getConfiguration(Properties p)
    {
        Configuration config = new BaseConfiguration();
        for (Enumeration e = p.keys(); e.hasMoreElements();)
        {
            String key = (String) e.nextElement();
            config.setProperty(key, p.getProperty(key));
        }
        return config;
    }

    /**
     * Convert a Configuration class into a ExtendedProperties class.
     *
     * @param c Configuration object to convert
     * @return ExtendedProperties created from the Configuration
     */
    public static ExtendedProperties getExtendedProperties(Configuration c)
    {
        ExtendedProperties props = new ExtendedProperties();
        for (Iterator i = c.getKeys(); i.hasNext();)
        {
            String key = (String) i.next();
            Object property = c.getProperty(key);

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
     * Convert a Configuration class into a Properties class. Multvalue keys
     * will be collapsed by {@link Configuration#getString}.
     *
     * @param c Configuration object to convert
     * @return Properties created from the Configuration
     */
    public static Properties getProperties(Configuration c)
    {
        Properties props = new Properties();

        Iterator iter = c.getKeys();

        while (iter.hasNext())
        {
            String key = (String) iter.next();
            props.setProperty(key, c.getString(key));
        }

        return props;
    }
}
