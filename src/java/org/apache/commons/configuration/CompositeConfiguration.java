package org.apache.commons.configuration;
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * This Configuration class allows you to add multiple different types of Configuration
 * to this CompositeConfiguration.  If you add Configuration1, and then Configuration2, 
 * any properties shared will mean that Configuration1 will be returned.
 * You can add multiple different types or the same type of properties file.
 * If Configuration1 doesn't have the property, then Configuration2 will be checked.
 * 
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id: CompositeConfiguration.java,v 1.5 2004/02/12 12:59:19 epugh Exp $
 */
public class CompositeConfiguration implements Configuration
{
    /** Array holding all the configuration */
    private LinkedList configList = new LinkedList();

    /** 
     * Configuration that holds in memory stuff.  Inserted as first so any
     * setProperty() override anything else added. 
     */
    private BaseConfiguration inMemoryConfiguration;

    /**
     * Creates an empty CompositeConfiguration object which can then
     * be added some other Configuration files
     */
    public CompositeConfiguration()
    {
        clear();
    }

    public void addConfiguration(Configuration config)
    {
        if (!configList.contains(config))
        {
            // As the inMemoryConfiguration contains all manually added keys,
            // we must make sure that it is always last. "Normal", non composed
            // configuration add their keys at the end of the configuration and
            // we want to mimic this behaviour.
            configList.add(configList.indexOf(inMemoryConfiguration), config);
        }
    }
    public void removeConfiguration(Configuration config)
    {
        // Make sure that you can't remove the inMemoryConfiguration from
        // the CompositeConfiguration object
        if (!config.equals(inMemoryConfiguration))
        {
            configList.remove(config);
        }
    }
    public int getNumberOfConfigurations()
    {
        return configList.size();
    }
    public void clear()
    {
        configList.clear();
        // recreate the in memory configuration
        inMemoryConfiguration = new BaseConfiguration();
        configList.addLast(inMemoryConfiguration);
    }
    /**
     * CompositeConfigurations can not be added to
     *
     * @param key The Key to add the property to.
     * @param token The Value to add.
     */
    public void addProperty(String key, Object token)
    {
        inMemoryConfiguration.addProperty(key, token);
    }
    /**
     * Get the list of the keys contained in the configuration
     * repository.
     *
     * @return An Iterator.
     */
    public Iterator getKeys()
    {
        List keys = new ArrayList();
        for (ListIterator i = configList.listIterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();
            for (Iterator j = config.getKeys(); j.hasNext();)
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
     * Get the list of the keys contained in the configuration
     * repository.
     *
     * @return An Iterator.
     */
    public Iterator getKeys(String key)
    {
        List keys = new ArrayList();
        for (ListIterator i = configList.listIterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();
            for (Iterator j = config.getKeys(key); j.hasNext();)
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
     * Get a list of properties associated with the given
     * configuration key.
     *
     * @param key The configuration key.
     * @return The associated properties if key is found.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a String/List.
     * @exception IllegalArgumentException if one of the tokens is
     * malformed (does not contain an equals sign).
     * @see #getProperties(String, Properties)
     */
    public Properties getProperties(String key)
    {
        return getFirstMatchingConfig(key).getProperties(key);
    }
    public boolean isEmpty()
    {
        boolean isEmpty = true;
        for (ListIterator i = configList.listIterator(); i.hasNext();)
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
     *  Gets a property from the configuration.
     *
     *  @param key property to retrieve
     *  @return value as object. Will return user value if exists,
     *          if not then default value if exists, otherwise null
     */
    public Object getProperty(String key)
    {
        return getFirstMatchingConfig(key).getProperty(key);
    }
    /**
     * Set a property, this will replace any previously
     * set values. Set values is implicitly a call
     * to clearProperty(key), addProperty(key,value).
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, Object value)
    {
        clearProperty(key);
        addProperty(key, value);
    }
    /**
     * Clear a property in the configuration.
     *
     * @param key the key to remove along with corresponding value.
     */
    public void clearProperty(String key)
    {
        for (ListIterator i = configList.listIterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();
            config.clearProperty(key);
        }
    }
    /**
     * check if the configuration contains the key
     */
    public boolean containsKey(String key)
    {
        for (ListIterator i = configList.listIterator(); i.hasNext();)
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
     * Create a CompositeConfiguration object that is a subset
     * of this one. Cycles over all the config objects, and calls
     * their subset method and then just adds that.
     *
     * @param prefix
     */
    public Configuration subset(String prefix)
    {
        CompositeConfiguration subsetCompositeConfiguration =
            new CompositeConfiguration();
        for (ListIterator i = configList.listIterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();
            Configuration subset = config.subset(prefix);
            if (subset != null)
            {
                subsetCompositeConfiguration.addConfiguration(subset);
            }
        }
        return subsetCompositeConfiguration;
    }
    /**
    * Get a float associated with the given configuration key.
    * 
    * @param key The configuration key.
    * @return The associated float.
    * @exception NoSuchElementException is thrown if the key doesn 't
    * map to an existing object.
    * @exception ClassCastException is thrown if the key maps to an
    * object that is not a Float.
    * @exception NumberFormatException is thrown if the value mapped
    * by the key has not a valid number format.
     */
    public float getFloat(String key)
    {
        return getFirstMatchingConfig(key).getFloat(key);
    }
    /**
     * Get a boolean associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated boolean.
     * @exception NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Boolean.
     */
    public boolean getBoolean(String key)
    {
        return getFirstMatchingConfig(key).getBoolean(key);
    }
    /**
     * Get a boolean associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated boolean.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Boolean.
     */
    public boolean getBoolean(String key, boolean defaultValue)
    {
        return getBoolean(key, new Boolean(defaultValue)).booleanValue();
    }
    /**
     * Get a boolean associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated boolean if key is found and has valid
     * format, default value otherwise.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Boolean.
     */
    public Boolean getBoolean(String key, Boolean defaultValue)
    {
        try
        {
            return getFirstMatchingConfig(key).getBoolean(key, defaultValue);
        }
        catch (NoSuchElementException nsee)
        {
            return defaultValue;
        }
    }
    /**
     * Get a byte associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated byte.
     * @exception NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Byte.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public byte getByte(String key)
    {
        return getFirstMatchingConfig(key).getByte(key);
    }
    /**
     * Get a byte associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated byte.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Byte.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public byte getByte(String key, byte defaultValue)
    {
        return getByte(key, new Byte(defaultValue).byteValue());
    }
    /**
     * Get a byte associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated byte if key is found and has valid format, default
     *         value otherwise.
     * @exception ClassCastException is thrown if the key maps to an object that
     *            is not a Byte.
     * @exception NumberFormatException is thrown if the value mapped by the key
     *            has not a valid number format.
     */
    public Byte getByte(String key, Byte defaultValue)
    {
        try
        {
            return getFirstMatchingConfig(key).getByte(key, defaultValue);
        }
        catch (NoSuchElementException nsee)
        {
            return defaultValue;
        }
    }
    /**
     * Get a double associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated double.
     * @exception NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Double.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public double getDouble(String key)
    {
        return getFirstMatchingConfig(key).getDouble(key);
    }
    /**
     * Get a double associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated double.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Double.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public double getDouble(String key, double defaultValue)
    {
        return getDouble(key, new Double(defaultValue)).doubleValue();
    }
    /**
     * Get a double associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated double if key is found and has valid
     * format, default value otherwise.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Double.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public Double getDouble(String key, Double defaultValue)
    {
        try
        {
            return getFirstMatchingConfig(key).getDouble(key, defaultValue);
        }
        catch (NoSuchElementException nsee)
        {
            return defaultValue;
        }
    }
    /**
     * Get a float associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated float.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Float.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public float getFloat(String key, float defaultValue)
    {
        return getFloat(key, new Float(defaultValue)).floatValue();
    }
    /**
     * Get a float associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated float if key is found and has valid
     * format, default value otherwise.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Float.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public Float getFloat(String key, Float defaultValue)
    {
        try
        {
            return getFirstMatchingConfig(key).getFloat(key, defaultValue);
        }
        catch (NoSuchElementException nsee)
        {
            return defaultValue;
        }
    }
    /**
     * Get a int associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated int.
     * @exception NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Integer.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public int getInt(String key)
    {
        return getFirstMatchingConfig(key).getInt(key);
    }
    /**
     * Get a int associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated int.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Integer.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public int getInt(String key, int defaultValue)
    {
        return getInteger(key, new Integer(defaultValue)).intValue();
    }
    /**
     * Get a int associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated int if key is found and has valid format, default
     *         value otherwise.
     * @exception ClassCastException is thrown if the key maps to an object that
     *         is not a Integer.
     * @exception NumberFormatException is thrown if the value mapped by the key
     *         has not a valid number format.
     */
    public Integer getInteger(String key, Integer defaultValue)
    {
        try
        {
            return getFirstMatchingConfig(key).getInteger(key, defaultValue);
        }
        catch (NoSuchElementException nsee)
        {
            return defaultValue;
        }
    }
    /**
     * Get a long associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated long.
     * @exception NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Long.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public long getLong(String key)
    {
        return getFirstMatchingConfig(key).getLong(key);
    }
    /**
     * Get a long associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated long.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Long.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public long getLong(String key, long defaultValue)
    {
        return getLong(key, new Long(defaultValue)).longValue();
    }
    /**
     * Get a long associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated long if key is found and has valid
     * format, default value otherwise.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Long.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public Long getLong(String key, Long defaultValue)
    {
        try
        {
            return getFirstMatchingConfig(key).getLong(key, defaultValue);
        }
        catch (NoSuchElementException nsee)
        {
            return defaultValue;
        }
    }
    /**
     * Get a short associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated short.
     * @exception NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Short.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public short getShort(String key)
    {
        return getFirstMatchingConfig(key).getShort(key);
    }
    /**
     * Get a short associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated short.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Short.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public short getShort(String key, short defaultValue)
    {
        return getShort(key, new Short(defaultValue)).shortValue();
    }
    /**
     * Get a short associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated short if key is found and has valid
     * format, default value otherwise.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Short.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public Short getShort(String key, Short defaultValue)
    {
        try
        {
            return getFirstMatchingConfig(key).getShort(key, defaultValue);
        }
        catch (NoSuchElementException nsee)
        {
            return defaultValue;
        }
    }

    public BigDecimal getBigDecimal(String key) throws NoSuchElementException {
        return getFirstMatchingConfig(key).getBigDecimal(key);
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue)
    {
        try
        {
            return getFirstMatchingConfig(key).getBigDecimal(key, defaultValue);
        }
        catch (NoSuchElementException nsee)
        {
            return defaultValue;
        }
    }

    public BigInteger getBigInteger(String key) throws NoSuchElementException {
        return getFirstMatchingConfig(key).getBigInteger(key);
    }

    public BigInteger getBigInteger(String key, BigInteger defaultValue)
    {
        try
        {
            return getFirstMatchingConfig(key).getBigInteger(key, defaultValue);
        }
        catch (NoSuchElementException nsee)
        {
            return defaultValue;
        }
    }

    /**
     * Get a string associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated string.
     * @exception ClassCastException is thrown if the key maps to an object that
     *            is not a String.
     */
    public String getString(String key)
    {
        return getString(key, null);
    }
    /**
     * Get a string associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated string if key is found, default value otherwise.
     * @exception ClassCastException is thrown if the key maps to an object that
     *            is not a String.
     */
    public String getString(String key, String defaultValue)
    {
        try
        {
            return getFirstMatchingConfig(key).getString(key, defaultValue);
        }
        catch (NoSuchElementException nsee)
        {
            return defaultValue;
        }
    }
    /**
     * Get an array of strings associated with the given configuration
     * key.
     *
     * @param key The configuration key.
     * @return The associated string array if key is found.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a String/List of Strings.
     */
    public String[] getStringArray(String key)
    {
        List list = getList(key);
        return (String []) list.toArray(new String [0]);
    }

    /**
     * Get a List of strings associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated List.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a List.
     */
    public List getList(String key)
    {
        List list = new ArrayList();

        for (ListIterator li = configList.listIterator(); li.hasNext();)
        {
            Configuration config = (Configuration) li.next();
            if (config.containsKey(key))
            {
                list.addAll(config.getList(key));
            }
        }

        return list;
    }

    /**
     * Get a List of strings associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a List.
     */
    public List getList(String key, List defaultValue)
    {
        List list = getList(key);

        return (list.size() == 0) ? defaultValue : list;
    }

    private Configuration getFirstMatchingConfig(String key)
    {
        for (ListIterator i = configList.listIterator(); i.hasNext();)
        {
            Configuration config = (Configuration) i.next();
            if (config.containsKey(key))
            {
                return config;
            }
        }
        throw new NoSuchElementException(
            '\'' + key + "' doesn't map to an existing object");
    }

    public Configuration getConfiguration(int index)
    {
        return (Configuration) configList.get(index);
    }
}
