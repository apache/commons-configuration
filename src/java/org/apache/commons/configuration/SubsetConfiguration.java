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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.iterators.TransformIterator;

/**
 * A subset of another configuration. The new Configuration object contains
 * every key from the parent Configuration that starts with prefix. The prefix
 * is removed from the keys in the subset.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 1.1 $, $Date: 2004/03/09 10:31:31 $
 */
public class SubsetConfiguration implements Configuration {

    protected Configuration parent;
    protected String prefix;
    protected String delimiter;

    /**
     * Create a subset of the specified configuration
     *
     * @param parent The parent configuration
     * @param prefix The prefix used to select the properties.
     */
    public SubsetConfiguration(Configuration parent, String prefix) {
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
    public SubsetConfiguration(Configuration parent, String prefix, String delimiter) {
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
    protected String getParentKey(String key) {
        if ("".equals(key) || key == null) {
            return prefix;
        } else {
            return delimiter == null ? prefix + key : prefix + delimiter + key;
        }
    }

    /**
     * Return the key in the subset configuration associated to the specified
     * key in the parent configuration.
     *
     * @param key The key in the parent configuration.
     */
    protected String getChildKey(String key) {
        if (!key.startsWith(prefix)) {
            throw new IllegalArgumentException("The parent key '" + key + "' is not in the subset.");
        } else {
            String modifiedKey = null;
            if (key.length() == prefix.length()) {
                modifiedKey = "";
            } else {
                int i = prefix.length() + (delimiter != null ? delimiter.length() : 0);
                modifiedKey = key.substring(i);
            }

            return modifiedKey;
        }
    }

    /**
     * Return the parent configuation for this subset.
     */
    public Configuration getParent() {
        return parent;
    }

    /**
     * Return the prefix used to select the properties in the parent configuration.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the prefix used to select the properties in the parent configuration.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Configuration subset(String prefix) {
        return parent.subset(getParentKey(prefix));
    }

    public boolean isEmpty() {
        return !getKeys().hasNext();
    }

    public boolean containsKey(String key) {
        return parent.containsKey(getParentKey(key));
    }

    public void addProperty(String key, Object value) {
        parent.addProperty(getParentKey(key), value);
    }

    public void setProperty(String key, Object value) {
        parent.setProperty(getParentKey(key), value);
    }

    public void clearProperty(String key) {
        parent.clearProperty(getParentKey(key));
    }

    public Object getProperty(String key) {
        return parent.getProperty(getParentKey(key));
    }

    public Iterator getKeys(String prefix) {
        return new TransformIterator(parent.getKeys(getParentKey(prefix)), new Transformer() {
            public Object transform(Object obj) {
                return getChildKey((String) obj);
            }
        });
    }

    public Iterator getKeys() {
        return new TransformIterator(parent.getKeys(prefix), new Transformer() {
            public Object transform(Object obj) {
                return getChildKey((String) obj);
            }
        });
    }

    public Properties getProperties(String key) {
        return parent.getProperties(getParentKey(key));
    }

    public boolean getBoolean(String key) {
        return parent.getBoolean(getParentKey(key));
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return parent.getBoolean(getParentKey(key), defaultValue);
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        return parent.getBoolean(getParentKey(key), defaultValue);
    }

    public byte getByte(String key) {
        return parent.getByte(getParentKey(key));
    }

    public byte getByte(String key, byte defaultValue) {
        return parent.getByte(getParentKey(key), defaultValue);
    }

    public Byte getByte(String key, Byte defaultValue) {
        return parent.getByte(getParentKey(key), defaultValue);
    }

    public double getDouble(String key) {
        return parent.getDouble(getParentKey(key));
    }

    public double getDouble(String key, double defaultValue) {
        return parent.getDouble(getParentKey(key), defaultValue);
    }

    public Double getDouble(String key, Double defaultValue) {
        return parent.getDouble(getParentKey(key), defaultValue);
    }

    public float getFloat(String key) {
        return parent.getFloat(getParentKey(key));
    }

    public float getFloat(String key, float defaultValue) {
        return parent.getFloat(getParentKey(key), defaultValue);
    }

    public Float getFloat(String key, Float defaultValue) {
        return parent.getFloat(getParentKey(key), defaultValue);
    }

    public int getInt(String key) {
        return parent.getInt(getParentKey(key));
    }

    public int getInt(String key, int defaultValue) {
        return parent.getInt(getParentKey(key), defaultValue);
    }

    public Integer getInteger(String key, Integer defaultValue) {
        return parent.getInteger(getParentKey(key), defaultValue);
    }

    public long getLong(String key) {
        return parent.getLong(getParentKey(key));
    }

    public long getLong(String key, long defaultValue) {
        return parent.getLong(getParentKey(key), defaultValue);
    }

    public Long getLong(String key, Long defaultValue) {
        return parent.getLong(getParentKey(key), defaultValue);
    }

    public short getShort(String key) {
        return parent.getShort(getParentKey(key));
    }

    public short getShort(String key, short defaultValue) {
        return parent.getShort(getParentKey(key), defaultValue);
    }

    public Short getShort(String key, Short defaultValue) {
        return parent.getShort(getParentKey(key), defaultValue);
    }

    public BigDecimal getBigDecimal(String key) {
        return parent.getBigDecimal(getParentKey(key));
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        return parent.getBigDecimal(getParentKey(key), defaultValue);
    }

    public BigInteger getBigInteger(String key) {
        return parent.getBigInteger(getParentKey(key));
    }

    public BigInteger getBigInteger(String key, BigInteger defaultValue) {
        return parent.getBigInteger(getParentKey(key), defaultValue);
    }

    public String getString(String key) {
        return parent.getString(getParentKey(key));
    }

    public String getString(String key, String defaultValue) {
        return parent.getString(getParentKey(key), defaultValue);
    }

    public String[] getStringArray(String key) {
        return parent.getStringArray(getParentKey(key));
    }

    public List getList(String key) {
        return parent.getList(getParentKey(key));
    }

    public List getList(String key, List defaultValue) {
        return parent.getList(getParentKey(key), defaultValue);
    }



}
