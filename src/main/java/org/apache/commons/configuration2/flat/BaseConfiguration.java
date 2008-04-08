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

package org.apache.commons.configuration2.flat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ConfigurationRuntimeException;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;

/**
 * Basic configuration class. Stores the configuration data but does not
 * provide any load or save functions. If you want to load your Configuration
 * from a file use {@link PropertiesConfiguration} or {@link XMLConfiguration}.
 *
 * This class extends normal Java properties by adding the possibility
 * to use the same key many times concatenating the value strings
 * instead of overwriting them.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:daveb@miceda-data">Dave Bryson</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 * @author <a href="mailto:kjohnson@transparent.com">Kent Johnson</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:ipriha@surfeu.fi">Ilkka Priha</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author <a href="mailto:ksh@scand.com">Konstantin Shaposhnikov</a>
 * @author Oliver Heger
 * @version $Id$
 */
public class BaseConfiguration extends AbstractFlatConfiguration implements Cloneable
{
    /** stores the configuration key-value pairs */
    private Map<String, Object> store = new LinkedHashMap<String, Object>();

    /**
     * Adds a key/value pair to the map.  This routine does no magic morphing.
     * It ensures the keylist is maintained
     *
     * @param key key to use for mapping
     * @param value object to store
     */
    @SuppressWarnings("unchecked")
    protected void addPropertyDirect(String key, Object value)
    {
        Object previousValue = getProperty(key);

        if (previousValue == null)
        {
            store.put(key, value);
        }
        else if (previousValue instanceof List)
        {
            // the value is added to the existing list
            ((List<Object>) previousValue).add(value);
        }
        else
        {
            // the previous value is replaced by a list containing the previous value and the new value
            List<Object> list = new ArrayList<Object>();
            list.add(previousValue);
            list.add(value);

            store.put(key, list);
        }
    }

    /**
     * Read property from underlying map.
     *
     * @param key key to use for mapping
     *
     * @return object associated with the given configuration key.
     */
    public Object getProperty(String key)
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
    protected void clearPropertyDirect(String key)
    {
        if (containsKey(key))
        {
            store.remove(key);
        }
    }

    public void clear()
    {
        fireEvent(EVENT_CLEAR, null, null, true);
        store.clear();
        fireEvent(EVENT_CLEAR, null, null, false);
    }

    /**
     * Get the list of the keys contained in the configuration
     * repository.
     *
     * @return An Iterator.
     */
    public Iterator<String> getKeys()
    {
        return store.keySet().iterator();
    }

    /**
     * Creates a copy of this object. This implementation will create a deep
     * clone, i.e. the map that stores the properties is cloned, too. So changes
     * performed at the copy won't affect the original and vice versa.
     *
     * @return the copy
     * @since 1.3
     */
    @SuppressWarnings("unchecked")
    public Object clone()
    {
        try
        {
            BaseConfiguration copy = (BaseConfiguration) super.clone();
            copy.store = (Map<String, Object>) ConfigurationUtils.clone(store);
            return copy;
        }
        catch (CloneNotSupportedException cex)
        {
            // should not happen
            throw new ConfigurationRuntimeException(cex);
        }
    }

    /**
     * Returns the maximum index for the property with the given index. If the
     * property has multiple values, the maximum index is the number of values
     * minus 1 (indices are 0-based). If the property has a single value, the
     * maximum index is 0. If the passed in key cannot be resolved, result is
     * -1.
     *
     * @param key the key of the property in question
     * @return the maximum index for this property
     */
    @Override
    public int getMaxIndex(String key)
    {
        Object value = getProperty(key);

        if (value == null)
        {
            return -1;
        }
        else if (value instanceof Collection)
        {
            return ((Collection<?>) value).size() - 1;
        }
        else
        {
            return 0;
        }
    }

    /**
     * Removes a value of a property with multiple values. The value to remove
     * is specified using its (0-based) index. If the index is invalid or if the
     * property does not have multiple values, this method has no effect.
     *
     * @param key the key of the property
     * @param index the index of the value to be removed
     * @return a flag whether the value could be removed
     */
    @Override
    protected boolean clearPropertyValueDirect(String key, int index)
    {
        Object value = getProperty(key);

        if (value instanceof List && index >= 0)
        {
            List<?> col = (List<?>) value;
            if (index < col.size())
            {
                col.remove(index);
                return true;
            }
        }

        return false;
    }

    /**
     * Modifies a value of a property with multiple values. The value to be
     * changed is specified using its (0-based) index. If the property does not
     * exist or the index is invalid, the value is added as if
     * <code>addProperty()</code> was called. If the property has a single
     * value, the passed in index determines what happens: if it is 0, the
     * single value is replaced by the new one; all other indices cause the new
     * value to be added to the old one.
     *
     * @param key the key of the property
     * @param index the index of the value to be manipulated
     * @param value the new value
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void setPropertyValueDirect(String key, int index, Object value)
    {
        Object oldValue = getProperty(key);

        if (oldValue instanceof List)
        {
            List<Object> col = (List<Object>) oldValue;
            if (index >= 0 && index < col.size())
            {
                col.set(index, value);
            }
            else
            {
                addPropertyDirect(key, value);
            }
        }

        else if (oldValue == null || index != 0)
        {
            addPropertyDirect(key, value);
        }
        else
        {
            store.put(key, value);
        }
    }
}
