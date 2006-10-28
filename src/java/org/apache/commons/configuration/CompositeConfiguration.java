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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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
    private List configList = new LinkedList();

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
    public CompositeConfiguration(Collection configurations)
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
    public CompositeConfiguration(Configuration inMemoryConfiguration, Collection configurations)
    {
        this(inMemoryConfiguration);

        if (configurations != null)
        {
            Iterator it = configurations.iterator();
            while (it.hasNext())
            {
                addConfiguration((Configuration) it.next());
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
            // we want to mimic this behaviour.
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
    public void clear()
    {
        configList.clear();
        // recreate the in memory configuration
        inMemoryConfiguration = new BaseConfiguration();
        ((BaseConfiguration) inMemoryConfiguration).setThrowExceptionOnMissing(isThrowExceptionOnMissing());
        configList.add(inMemoryConfiguration);
    }

    /**
     * Add this property to the inmemory Configuration.
     *
     * @param key The Key to add the property to.
     * @param token The Value to add.
     */
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
        for (Iterator i = configList.iterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();
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

    /**
     * {@inheritDoc}
     */
    public Iterator getKeys()
    {
        List keys = new ArrayList();
        for (Iterator i = configList.iterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();

            Iterator j = config.getKeys();
            while (j.hasNext())
            {
                String key = (String) j.next();
                if (!keys.contains(key))
                {
                    keys.add(key);
                }
            }
        }

        return keys.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public Iterator getKeys(String key)
    {
        List keys = new ArrayList();
        for (Iterator i = configList.iterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();

            Iterator j = config.getKeys(key);
            while (j.hasNext())
            {
                String newKey = (String) j.next();
                if (!keys.contains(newKey))
                {
                    keys.add(newKey);
                }
            }
        }

        return keys.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty()
    {
        boolean isEmpty = true;
        for (Iterator i = configList.iterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();
            if (!config.isEmpty())
            {
                return false;
            }
        }

        return isEmpty;
    }

    /**
     * {@inheritDoc}
     */
    public void clearProperty(String key)
    {
        for (Iterator i = configList.iterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();
            config.clearProperty(key);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsKey(String key)
    {
        for (Iterator i = configList.iterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();
            if (config.containsKey(key))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public List getList(String key, List defaultValue)
    {
        List list = new ArrayList();

        // add all elements from the first configuration containing the requested key
        Iterator it = configList.iterator();
        while (it.hasNext() && list.isEmpty())
        {
            Configuration config = (Configuration) it.next();
            if (config != inMemoryConfiguration && config.containsKey(key))
            {
                list.addAll(config.getList(key));
            }
        }

        // add all elements from the in memory configuration
        list.addAll(inMemoryConfiguration.getList(key));

        if (list.isEmpty())
        {
            return defaultValue;
        }
        
        ListIterator lit = list.listIterator();
        while (lit.hasNext())
        {
        	lit.set(interpolate(lit.next()));
        }

        return list;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getStringArray(String key)
    {
        List list = getList(key);

        // interpolate the strings
        String[] tokens = new String[list.size()];

        for (int i = 0; i < tokens.length; i++)
        {
            tokens[i] = interpolate(String.valueOf(list.get(i)));
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
        return (Configuration) configList.get(index);
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
    public Object clone()
    {
        try
        {
            CompositeConfiguration copy = (CompositeConfiguration) super
                    .clone();
            copy.clearConfigurationListeners();
            copy.configList = new LinkedList();
            copy.inMemoryConfiguration = ConfigurationUtils
                    .cloneConfiguration(getInMemoryConfiguration());
            copy.configList.add(copy.inMemoryConfiguration);

            for (int i = 0; i < getNumberOfConfigurations(); i++)
            {
                Configuration config = getConfiguration(i);
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
}
