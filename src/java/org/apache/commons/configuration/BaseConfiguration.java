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

import java.util.Iterator;

import org.apache.commons.collections.SequencedHashMap;

/**
 * Basic configuration classe. Stores the configuration data but does not
 * provide any load or save functions. If you want to load your Configuration
 * from a file use PropertiesConfiguration or XmlConfiguration.
 *
 * This class extends normal Java properties by adding the possibility
 * to use the same key many times concatenating the value strings
 * instead of overwriting them.
 *
 *
 * @version $Id: BaseConfiguration.java,v 1.4 2004/02/27 17:41:35 epugh Exp $
 */
public class BaseConfiguration extends AbstractConfiguration
{
    /** stores the configuration key-value pairs */
    private SequencedHashMap store = new SequencedHashMap();
    
    /**
     * Empty constructor.  You must add all the values to this configuration.
     */
    public BaseConfiguration()
    {
        super();
    }
    

    /**
     * Adds a key/value pair to the map.  This routine does no magic morphing.
     * It ensures the keylist is maintained
     *
     * @param key key to use for mapping
     * @param obj object to store
     */
    protected void addPropertyDirect(String key, Object obj)
    {
        Object o = getPropertyDirect(key);
        Object objAdd = null;
        
        if(o == null)
        {
            objAdd = obj;
        }
        else
        {
            if (o instanceof Container)
            {
                ((Container) o).add(obj);
            }
            else
            {
                // The token key is not a container.
                Container c = new Container();

                // There is an element. Put it into the container
                // at the first position
                c.add(o);

                // Now gobble up the supplied object
                c.add(obj);

                objAdd = c;
            }
        }
        
        if(objAdd != null)
        {
            store.put(key, objAdd);
        }
    }

    /**
     * Read property from underlying map.
     *
     * @param key key to use for mapping
     *
     * @return object associated with the given configuration key.
     */
    protected Object getPropertyDirect(String key) 
    {
        return store.get(key);
    }

    /**
     * Check if the configuration is empty
     *
     * @return <code>true</code> if Configuration is empty,
     * <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        return store.isEmpty();
    }

    /**
     * check if the configuration contains the key
     *
     * @param key the configuration key
     * 
     * @return <code>true</code> if Configuration contain given key,
     * <code>false</code> otherwise.
     */
    public boolean containsKey(String key)
    {
        return store.containsKey(key);
        
    }

    /**
     * Clear a property in the configuration.
     *
     * @param key the key to remove along with corresponding value.
     */
    public void clearProperty(String key)
    {
        if (containsKey(key))
        {
            store.remove(key);            
        }
    }

    /**
     * Get the list of the keys contained in the configuration
     * repository.
     *
     * @return An Iterator.
     */
    public Iterator getKeys()
    {        
        return store.iterator();        
    }
}
