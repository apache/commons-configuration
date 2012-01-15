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

package org.apache.commons.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * This Configuration class allows you to add multiple different types of Configuration
 * to this CompositeConfiguration.  If you add Configuration1, and then Configuration2,
 * any properties shared will mean that Configuration1 will be returned.
 * You can add multiple different types or the same type of properties file.
 * If Configuration1 doesn't have the property, then Configuration2 will be checked.
 *
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class CompositeConfiguration extends AbstractConfiguration
implements Cloneable
{
    /** List holding all the configuration */
    private List<Configuration> configList = new LinkedList<Configuration>();

    /**
     * Configuration that holds in memory stuff.  Inserted as first so any
     * setProperty() override anything else added.
     */
    private Configuration inMemoryConfiguration;

    /**
     * Creates an empty CompositeConfiguration object which can then
     * be added some other Configuration files
     */
    public CompositeConfiguration()
    {
        clear();
    }

    /**
     * Creates a CompositeConfiguration object with a specified in memory
     * configuration. This configuration will store any changes made to
     * the CompositeConfiguration.
     *
     * @param inMemoryConfiguration the in memory configuration to use
     */
    public CompositeConfiguration(Configuration inMemoryConfiguration)
    {
        configList.clear();
        this.inMemoryConfiguration = inMemoryConfiguration;
        configList.add(inMemoryConfiguration);
    }

    /**
     * Create a CompositeConfiguration with an empty in memory configuration
     * and adds the collection of configurations specified.
     *
     * @param configurations the collection of configurations to add
     */
    public CompositeConfiguration(Collection<? extends Configuration> configurations)
    {
        this(new BaseConfiguration(), configurations);
    }

    /**
     * Creates a CompositeConfiguration with a specified in memory
     * configuration, and then adds the given collection of configurations.
     *
     * @param inMemoryConfiguration the in memory configuration to use
     * @param configurations        the collection of configurations to add
     */
    public CompositeConfiguration(Configuration inMemoryConfiguration,
            Collection<? extends Configuration> configurations)
    {
        this(inMemoryConfiguration);

        if (configurations != null)
        {
            for (Configuration c : configurations)
            {
                addConfiguration(c);
            }
        }
    }

    /**
     * Add a configuration.
     *
     * @param config the configuration to add
     */
    public void addConfiguration(Configuration config)
    {
        if (!configList.contains(config))
        {
            // As the inMemoryConfiguration contains all manually added keys,
            // we must make sure that it is always last. "Normal", non composed
            // configuration add their keys at the end of the configuration and
            // we want to mimic this behavior.
            configList.add(configList.indexOf(inMemoryConfiguration), config);

            if (config instanceof AbstractConfiguration)
            {
                ((AbstractConfiguration) config).setThrowExceptionOnMissing(isThrowExceptionOnMissing());
            }
        }
    }

    /**
     * Remove a configuration. The in memory configuration cannot be removed.
     *
     * @param config The configuration to remove
     */
    public void removeConfiguration(Configuration config)
    {
        // Make sure that you can't remove the inMemoryConfiguration from
        // the CompositeConfiguration object
        if (!config.equals(inMemoryConfiguration))
        {
            configList.remove(config);
        }
    }

    /**
     * Return the number of configurations.
     *
     * @return the number of configuration
     */
    public int getNumberOfConfigurations()
    {
        return configList.size();
    }

    /**
     * Remove all configuration reinitialize the in memory configuration.
     */
    @Override
    public void clear()
    {
        configList.clear();
        // recreate the in memory configuration
        inMemoryConfiguration = new BaseConfiguration();
        ((BaseConfiguration) inMemoryConfiguration).setThrowExceptionOnMissing(isThrowExceptionOnMissing());
        ((BaseConfiguration) inMemoryConfiguration).setListDelimiter(getListDelimiter());
        ((BaseConfiguration) inMemoryConfiguration).setDelimiterParsingDisabled(isDelimiterParsingDisabled());
        configList.add(inMemoryConfiguration);
    }

    /**
     * Add this property to the inmemory Configuration.
     *
     * @param key The Key to add the property to.
     * @param token The Value to add.
     */
    @Override
    protected void addPropertyDirect(String key, Object token)
    {
        inMemoryConfiguration.addProperty(key, token);
    }

    /**
     * Read property from underlying composite
     *
     * @param key key to use for mapping
     *
     * @return object associated with the given configuration key.
     */
    public Object getProperty(String key)
    {
        Configuration firstMatchingConfiguration = null;
        for (Configuration config : configList)
        {
            if (config.containsKey(key))
            {
                firstMatchingConfiguration = config;
                break;
            }
        }

        if (firstMatchingConfiguration != null)
        {
            return firstMatchingConfiguration.getProperty(key);
        }
        else
        {
            return null;
        }
    }

    public Iterator<String> getKeys()
    {
        Set<String> keys = new LinkedHashSet<String>();
        for (Configuration config : configList)
        {
            for (Iterator<String> it = config.getKeys(); it.hasNext();)
            {
                keys.add(it.next());
            }
        }

        return keys.iterator();
    }

    @Override
    public Iterator<String> getKeys(String key)
    {
        Set<String> keys = new LinkedHashSet<String>();
        for (Configuration config : configList)
        {
            for (Iterator<String> it = config.getKeys(key); it.hasNext();)
            {
                keys.add(it.next());
            }
        }

        return keys.iterator();
    }

    public boolean isEmpty()
    {
        for (Configuration config : configList)
        {
            if (!config.isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void clearPropertyDirect(String key)
    {
        for (Configuration config : configList)
        {
            config.clearProperty(key);
        }
    }

    public boolean containsKey(String key)
    {
        for (Configuration config : configList)
        {
            if (config.containsKey(key))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Object> getList(String key, List<Object> defaultValue)
    {
        List<Object> list = new ArrayList<Object>();

        // add all elements from the first configuration containing the requested key
        Iterator<Configuration> it = configList.iterator();
        while (it.hasNext() && list.isEmpty())
        {
            Configuration config = it.next();
            if (config != inMemoryConfiguration && config.containsKey(key))
            {
                appendListProperty(list, config, key);
            }
        }

        // add all elements from the in memory configuration
        appendListProperty(list, inMemoryConfiguration, key);

        if (list.isEmpty())
        {
            return defaultValue;
        }

        ListIterator<Object> lit = list.listIterator();
        while (lit.hasNext())
        {
            lit.set(interpolate(lit.next()));
        }

        return list;
    }

    @Override
    public String[] getStringArray(String key)
    {
        List<Object> list = getList(key);

        // transform property values into strings
        String[] tokens = new String[list.size()];

        for (int i = 0; i < tokens.length; i++)
        {
            tokens[i] = String.valueOf(list.get(i));
        }

        return tokens;
    }

    /**
     * Return the configuration at the specified index.
     *
     * @param index The index of the configuration to retrieve
     * @return the configuration at this index
     */
    public Configuration getConfiguration(int index)
    {
        return configList.get(index);
    }

    /**
     * Returns the &quot;in memory configuration&quot;. In this configuration
     * changes are stored.
     *
     * @return the in memory configuration
     */
    public Configuration getInMemoryConfiguration()
    {
        return inMemoryConfiguration;
    }

    /**
     * Returns a copy of this object. This implementation will create a deep
     * clone, i.e. all configurations contained in this composite will also be
     * cloned. This only works if all contained configurations support cloning;
     * otherwise a runtime exception will be thrown. Registered event handlers
     * won't get cloned.
     *
     * @return the copy
     * @since 1.3
     */
    @Override
    public Object clone()
    {
        try
        {
            CompositeConfiguration copy = (CompositeConfiguration) super
                    .clone();
            copy.clearConfigurationListeners();
            copy.configList = new LinkedList<Configuration>();
            copy.inMemoryConfiguration = ConfigurationUtils
                    .cloneConfiguration(getInMemoryConfiguration());
            copy.configList.add(copy.inMemoryConfiguration);

            for (Configuration config : configList)
            {
                if (config != getInMemoryConfiguration())
                {
                    copy.addConfiguration(ConfigurationUtils
                            .cloneConfiguration(config));
                }
            }

            return copy;
        }
        catch (CloneNotSupportedException cnex)
        {
            // cannot happen
            throw new ConfigurationRuntimeException(cnex);
        }
    }

    /**
     * Sets a flag whether added values for string properties should be checked
     * for the list delimiter. This implementation ensures that the in memory
     * configuration is correctly initialized.
     *
     * @param delimiterParsingDisabled the new value of the flag
     * @since 1.4
     */
    @Override
    public void setDelimiterParsingDisabled(boolean delimiterParsingDisabled)
    {
        if (inMemoryConfiguration instanceof AbstractConfiguration)
        {
            ((AbstractConfiguration) inMemoryConfiguration)
                    .setDelimiterParsingDisabled(delimiterParsingDisabled);
        }
        super.setDelimiterParsingDisabled(delimiterParsingDisabled);
    }

    /**
     * Sets the character that is used as list delimiter. This implementation
     * ensures that the in memory configuration is correctly initialized.
     *
     * @param listDelimiter the new list delimiter character
     * @since 1.4
     */
    @Override
    public void setListDelimiter(char listDelimiter)
    {
        if (inMemoryConfiguration instanceof AbstractConfiguration)
        {
            ((AbstractConfiguration) inMemoryConfiguration)
                    .setListDelimiter(listDelimiter);
        }
        super.setListDelimiter(listDelimiter);
    }

    /**
     * Returns the configuration source, in which the specified key is defined.
     * This method will iterate over all existing child configurations and check
     * whether they contain the specified key. The following constellations are
     * possible:
     * <ul>
     * <li>If exactly one child configuration contains the key, this
     * configuration is returned as the source configuration. This may be the
     * <em>in memory configuration</em> (this has to be explicitly checked by
     * the calling application).</li>
     * <li>If none of the child configurations contain the key, <b>null</b> is
     * returned.</li>
     * <li>If the key is contained in multiple child configurations or if the
     * key is <b>null</b>, a {@code IllegalArgumentException} is thrown.
     * In this case the source configuration cannot be determined.</li>
     * </ul>
     *
     * @param key the key to be checked
     * @return the source configuration of this key
     * @throws IllegalArgumentException if the source configuration cannot be
     * determined
     * @since 1.5
     */
    public Configuration getSource(String key)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("Key must not be null!");
        }

        Configuration source = null;
        for (Configuration conf : configList)
        {
            if (conf.containsKey(key))
            {
                if (source != null)
                {
                    throw new IllegalArgumentException("The key " + key
                            + " is defined by multiple sources!");
                }
                source = conf;
            }
        }

        return source;
    }

    /**
     * Adds the value of a property to the given list. This method is used by
     * {@code getList()} for gathering property values from the child
     * configurations.
     *
     * @param dest the list for collecting the data
     * @param config the configuration to query
     * @param key the key of the property
     */
    private static void appendListProperty(List<Object> dest, Configuration config,
            String key)
    {
        Object value = config.getProperty(key);
        if (value != null)
        {
            if (value instanceof Collection)
            {
                Collection<?> col = (Collection<?>) value;
                dest.addAll(col);
            }
            else
            {
                dest.add(value);
            }
        }
    }
}
