/*
 * Copyright 2004 The Apache Software Foundation.
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

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.iterators.TransformIterator;

/**
 * A subset of another configuration. The new Configuration object contains
 * every key from the parent Configuration that starts with prefix. The prefix
 * is removed from the keys in the subset.
 *
 * @see Configuration#subset(String)
 *
 * @author Emmanuel Bourg
 * @version $Revision: 1.4 $, $Date: 2004/06/23 11:15:45 $
 */
public class SubsetConfiguration extends AbstractConfiguration
{
    protected Configuration parent;
    protected String prefix;
    protected String delimiter;

    /**
     * Create a subset of the specified configuration
     *
     * @param parent The parent configuration
     * @param prefix The prefix used to select the properties.
     */
    public SubsetConfiguration(Configuration parent, String prefix)
    {
        this.parent = parent;
        this.prefix = prefix;
    }

    /**
     * Create a subset of the specified configuration
     *
     * @param parent    The parent configuration
     * @param prefix    The prefix used to select the properties.
     * @param delimiter The prefix delimiter
     */
    public SubsetConfiguration(Configuration parent, String prefix, String delimiter)
    {
        this.parent = parent;
        this.prefix = prefix;
        this.delimiter = delimiter;
    }

    /**
     * Return the key in the parent configuration associated to the specified
     * key in this subset.
     *
     * @param key The key in the subset.
     */
    protected String getParentKey(String key)
    {
        if ("".equals(key) || key == null)
        {
            return prefix;
        }
        else
        {
            return delimiter == null ? prefix + key : prefix + delimiter + key;
        }
    }

    /**
     * Return the key in the subset configuration associated to the specified
     * key in the parent configuration.
     *
     * @param key The key in the parent configuration.
     */
    protected String getChildKey(String key)
    {
        if (!key.startsWith(prefix))
        {
            throw new IllegalArgumentException("The parent key '" + key + "' is not in the subset.");
        }
        else
        {
            String modifiedKey = null;
            if (key.length() == prefix.length())
            {
                modifiedKey = "";
            }
            else
            {
                int i = prefix.length() + (delimiter != null ? delimiter.length() : 0);
                modifiedKey = key.substring(i);
            }

            return modifiedKey;
        }
    }

    /**
     * Return the parent configuation for this subset.
     */
    public Configuration getParent()
    {
        return parent;
    }

    /**
     * Return the prefix used to select the properties in the parent configuration.
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * Set the prefix used to select the properties in the parent configuration.
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public Configuration subset(String prefix)
    {
        return parent.subset(getParentKey(prefix));
    }

    public boolean isEmpty()
    {
        return !getKeys().hasNext();
    }

    public boolean containsKey(String key)
    {
        return parent.containsKey(getParentKey(key));
    }

    public void addPropertyDirect(String key, Object value)
    {
        parent.addProperty(getParentKey(key), value);
    }

    public void setProperty(String key, Object value)
    {
        parent.setProperty(getParentKey(key), value);
    }

    public void clearProperty(String key)
    {
        parent.clearProperty(getParentKey(key));
    }

    public Object getPropertyDirect(String key)
    {
        return parent.getProperty(getParentKey(key));
    }

    public Iterator getKeys(String prefix)
    {
        return new TransformIterator(parent.getKeys(getParentKey(prefix)), new Transformer()
        {
            public Object transform(Object obj)
            {
                return getChildKey((String) obj);
            }
        });
    }

    public Iterator getKeys()
    {
        return new TransformIterator(parent.getKeys(prefix), new Transformer()
        {
            public Object transform(Object obj)
            {
                return getChildKey((String) obj);
            }
        });
    }

    protected String interpolate(String base)
    {
        if (delimiter == null && "".equals(prefix))
        {
            return super.interpolate(base);
        }
        else
        {
            SubsetConfiguration config = new SubsetConfiguration(parent, "");
            return config.interpolate(base);
        }
    }
}
