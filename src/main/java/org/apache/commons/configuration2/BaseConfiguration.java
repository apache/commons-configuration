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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;

/**
 * Basic configuration class. Stores the configuration data but does not
 * provide any load or save functions. If you want to load your Configuration
 * from a file use PropertiesConfiguration or XmlConfiguration.
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
 */
public class BaseConfiguration extends AbstractConfiguration implements Cloneable
{
    /** stores the configuration key-value pairs */
    private Map<String, Object> store = new LinkedHashMap<>();

    /**
     * Adds a key/value pair to the map.  This routine does no magic morphing.
     * It ensures the keylist is maintained
     *
     * @param key key to use for mapping
     * @param value object to store
     */
    @Override
    protected void addPropertyDirect(final String key, final Object value)
    {
        final Object previousValue = getPropertyInternal(key);

        if (previousValue == null)
        {
            store.put(key, value);
        }
        else if (previousValue instanceof List)
        {
            // safe to case because we have created the lists ourselves
            @SuppressWarnings("unchecked")
            final
            List<Object> valueList = (List<Object>) previousValue;
            // the value is added to the existing list
            valueList.add(value);
        }
        else
        {
            // the previous value is replaced by a list containing the previous value and the new value
            final List<Object> list = new ArrayList<>();
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
    @Override
    protected Object getPropertyInternal(final String key)
    {
        return store.get(key);
    }

    /**
     * Check if the configuration is empty
     *
     * @return {@code true} if Configuration is empty,
     * {@code false} otherwise.
     */
    @Override
    protected boolean isEmptyInternal()
    {
        return store.isEmpty();
    }

    /**
     * check if the configuration contains the key
     *
     * @param key the configuration key
     *
     * @return {@code true} if Configuration contain given key,
     * {@code false} otherwise.
     */
    @Override
    protected boolean containsKeyInternal(final String key)
    {
        return store.containsKey(key);
    }

    /**
     * Clear a property in the configuration.
     *
     * @param key the key to remove along with corresponding value.
     */
    @Override
    protected void clearPropertyDirect(final String key)
    {
        store.remove(key);
    }

    @Override
    protected void clearInternal()
    {
        store.clear();
    }

    /**
     * {@inheritDoc} This implementation obtains the size directly from the map
     * used as data store. So this is a rather efficient implementation.
     */
    @Override
    protected int sizeInternal()
    {
        return store.size();
    }

    /**
     * Get the list of the keys contained in the configuration
     * repository.
     *
     * @return An Iterator.
     */
    @Override
    protected Iterator<String> getKeysInternal()
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
    @Override
    public Object clone()
    {
        try
        {
            final BaseConfiguration copy = (BaseConfiguration) super.clone();
            cloneStore(copy);
            copy.cloneInterpolator(this);

            return copy;
        }
        catch (final CloneNotSupportedException cex)
        {
            // should not happen
            throw new ConfigurationRuntimeException(cex);
        }
    }

    /**
     * Clones the internal map with the data of this configuration.
     *
     * @param copy the copy created by the {@code clone()} method
     * @throws CloneNotSupportedException if the map cannot be cloned
     */
    private void cloneStore(final BaseConfiguration copy)
            throws CloneNotSupportedException
    {
        // This is safe because the type of the map is known
        @SuppressWarnings("unchecked")
        final
        Map<String, Object> clonedStore = (Map<String, Object>) ConfigurationUtils.clone(store);
        copy.store = clonedStore;

        // Handle collections in the map; they have to be cloned, too
        for (final Map.Entry<String, Object> e : store.entrySet())
        {
            if (e.getValue() instanceof Collection)
            {
                // This is safe because the collections were created by ourselves
                @SuppressWarnings("unchecked")
                final
                Collection<String> strList = (Collection<String>) e.getValue();
                copy.store.put(e.getKey(), new ArrayList<>(strList));
            }
        }
    }
}
