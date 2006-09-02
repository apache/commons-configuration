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

import java.awt.Color;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.io.Serializable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Decorator providing additional getters for any Configuration. This extended
 * Configuration supports more types: URL, Locale, Date, Calendar, Color, as
 * well as lists and arrays for all types.
 *
 * <p>Let us know if you find this useful, the most frequently used getters
 * are likely to be integrated in the Configuration interface in a future
 * version.</p>
 *
 * @author <a href="ebourg@apache.org">Emmanuel Bourg</a>
 * @version $Revision$, $Date$
 * @since 1.1
 */
public class DataConfiguration extends AbstractConfiguration implements Serializable
{
    /** The key of the property storing the user defined date format. */
    public static final String DATE_FORMAT_KEY = "org.apache.commons.configuration.format.date";

    /** The default format for dates. */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = -69011336405718640L;

    /** Stores the wrapped configuration.*/
    protected Configuration configuration;

    /**
     * Creates a new instance of <code>DataConfiguration</code> and sets the
     * wrapped configuration.
     *
     * @param configuration the wrapped configuration
     */
    public DataConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Return the configuration decorated by this DataConfiguration.
     *
     * @return the wrapped configuration
     */
    public Configuration getConfiguration()
    {
        return configuration;
    }

    public Object getProperty(String key)
    {
        return configuration.getProperty(key);
    }

    protected void addPropertyDirect(String key, Object obj)
    {
        configuration.addProperty(key, obj);
    }

    public boolean isEmpty()
    {
        return configuration.isEmpty();
    }

    public boolean containsKey(String key)
    {
        return configuration.containsKey(key);
    }

    public void clearProperty(String key)
    {
        configuration.clearProperty(key);
    }

    public Iterator getKeys()
    {
        return configuration.getKeys();
    }

    /**
     * Get a list of Boolean objects associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty list is returned.
     *
     * @param key The configuration key.
     * @return The associated Boolean list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of booleans.
     */
    public List getBooleanList(String key)
    {
        return getBooleanList(key, new ArrayList());
    }

    /**
     * Get a list of Boolean objects associated with the given
     * configuration key. If the key doesn't map to an existing object,
     * the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of strings.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of booleans.
     */
    public List getBooleanList(String key, List defaultValue)
    {
        Object value = getProperty(key);

        List list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value instanceof boolean[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, ArrayUtils.toObject((boolean[]) value));
        }
        else if (value instanceof Boolean[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, (Boolean[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            list = new ArrayList();

            Iterator it = values.iterator();
            while (it.hasNext())
            {
                list.add(PropertyConverter.toBoolean(interpolate(it.next())));
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                list = new ArrayList();
                list.add(PropertyConverter.toBoolean(interpolate(value)));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of booleans", e);
            }
        }

        return list;
    }

    /**
     * Get an array of boolean primitives associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @return The associated boolean array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of booleans.
     */
    public boolean[] getBooleanArray(String key)
    {
        return getBooleanArray(key, new boolean[0]);
    }

    /**
     * Get an array of boolean primitives associated with the given
     * configuration key. If the key doesn't map to an existing object,
     * the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated boolean array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of booleans.
     */
    public boolean[] getBooleanArray(String key, boolean[] defaultValue)
    {
        Object value = getProperty(key);

        boolean[] array;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            array = defaultValue;
        }
        else if (value instanceof boolean[])
        {
            array = (boolean[]) value;
        }
        else if (value instanceof Boolean[])
        {
            array = ArrayUtils.toPrimitive((Boolean[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            array = new boolean[values.size()];

            int i = 0;
            Iterator it = values.iterator();
            while (it.hasNext())
            {
                array[i++] = PropertyConverter.toBoolean(interpolate(it.next())).booleanValue();
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                array = new boolean[1];
                array[0] = PropertyConverter.toBoolean(interpolate(value)).booleanValue();
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of booleans", e);
            }
        }

        return array;
    }

    /**
     * Get a list of Byte objects associated with the given configuration key.
     * If the key doesn't map to an existing object an empty list is returned.
     *
     * @param key The configuration key.
     * @return The associated Byte list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of bytes.
     */
    public List getByteList(String key)
    {
        return getByteList(key, new ArrayList());
    }

    /**
     * Get a list of Byte objects associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of Bytes.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of bytes.
     */
    public List getByteList(String key, List defaultValue)
    {
        Object value = getProperty(key);

        List list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value instanceof byte[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, ArrayUtils.toObject((byte[]) value));
        }
        else if (value instanceof Byte[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, (Byte[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            list = new ArrayList();

            Iterator it = values.iterator();
            while (it.hasNext())
            {
                list.add(PropertyConverter.toByte(interpolate(it.next())));
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                list = new ArrayList();
                list.add(PropertyConverter.toByte(interpolate(value)));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of bytes", e);
            }
        }

        return list;
    }

    /**
     * Get an array of byte primitives associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @return The associated byte array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of bytes.
     */
    public byte[] getByteArray(String key)
    {
        return getByteArray(key, new byte[0]);
    }

    /**
     * Get an array of byte primitives associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @param defaultValue the default value, which will be returned if the property is not found
     * @return The associated byte array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of bytes.
     */
    public byte[] getByteArray(String key, byte[] defaultValue)
    {
        Object value = getProperty(key);

        byte[] array;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            array = defaultValue;
        }
        else if (value instanceof byte[])
        {
            array = (byte[]) value;
        }
        else if (value instanceof Byte[])
        {
            array = ArrayUtils.toPrimitive((Byte[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            array = new byte[values.size()];

            int i = 0;
            Iterator it = values.iterator();
            while (it.hasNext())
            {
                array[i++] = PropertyConverter.toByte(interpolate(it.next())).byteValue();
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                array = new byte[1];
                array[0] = PropertyConverter.toByte(interpolate(value)).byteValue();
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of bytes", e);
            }
        }

        return array;
    }

    /**
     * Get a list of Short objects associated with the given configuration key.
     * If the key doesn't map to an existing object an empty list is returned.
     *
     * @param key The configuration key.
     * @return The associated Short list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of shorts.
     */
    public List getShortList(String key)
    {
        return getShortList(key, new ArrayList());
    }

    /**
     * Get a list of Short objects associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of Shorts.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of shorts.
     */
    public List getShortList(String key, List defaultValue)
    {
        Object value = getProperty(key);

        List list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value instanceof short[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, ArrayUtils.toObject((short[]) value));
        }
        else if (value instanceof Short[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, (Short[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            list = new ArrayList();

            Iterator it = values.iterator();
            while (it.hasNext())
            {
                list.add(PropertyConverter.toShort(interpolate(it.next())));
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                list = new ArrayList();
                list.add(PropertyConverter.toShort(interpolate(value)));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of shorts", e);
            }
        }

        return list;
    }

    /**
     * Get an array of short primitives associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @return The associated short array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of shorts.
     */
    public short[] getShortArray(String key)
    {
        return getShortArray(key, new short[0]);
    }

    /**
     * Get an array of short primitives associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @param defaultValue the default value, which will be returned if the property is not found
     * @return The associated short array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of shorts.
     */
    public short[] getShortArray(String key, short[] defaultValue)
    {
        Object value = getProperty(key);

        short[] array;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            array = defaultValue;
        }
        else if (value instanceof short[])
        {
            array = (short[]) value;
        }
        else if (value instanceof Short[])
        {
            array = ArrayUtils.toPrimitive((Short[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            array = new short[values.size()];

            int i = 0;
            Iterator it = values.iterator();
            while (it.hasNext())
            {
                array[i++] = PropertyConverter.toShort(interpolate(it.next())).shortValue();
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                array = new short[1];
                array[0] = PropertyConverter.toShort(interpolate(value)).shortValue();
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of shorts", e);
            }
        }

        return array;
    }

    /**
     * Get a list of Integer objects associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty list is returned.
     *
     * @param key The configuration key.
     * @return The associated Integer list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of integers.
     */
    public List getIntegerList(String key)
    {
        return getIntegerList(key, new ArrayList());
    }

    /**
     * Get a list of Integer objects associated with the given
     * configuration key. If the key doesn't map to an existing object,
     * the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of Integers.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of integers.
     */
    public List getIntegerList(String key, List defaultValue)
    {
        Object value = getProperty(key);

        List list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value instanceof int[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, ArrayUtils.toObject((int[]) value));
        }
        else if (value instanceof Integer[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, (Integer[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            list = new ArrayList();

            Iterator it = values.iterator();
            while (it.hasNext())
            {
                list.add(PropertyConverter.toInteger(interpolate(it.next())));
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                list = new ArrayList();
                list.add(PropertyConverter.toInteger(interpolate(value)));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of integers", e);
            }
        }

        return list;
    }

    /**
     * Get an array of int primitives associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @return The associated int array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of integers.
     */
    public int[] getIntArray(String key)
    {
        return getIntArray(key, new int[0]);
    }

    /**
     * Get an array of int primitives associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @param defaultValue the default value, which will be returned if the property is not found
     * @return The associated int array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of integers.
     */
    public int[] getIntArray(String key, int[] defaultValue)
    {
        Object value = getProperty(key);

        int[] array;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            array = defaultValue;
        }
        else if (value instanceof int[])
        {
            array = (int[]) value;
        }
        else if (value instanceof Integer[])
        {
            array = ArrayUtils.toPrimitive((Integer[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            array = new int[values.size()];

            int i = 0;
            Iterator it = values.iterator();
            while (it.hasNext())
            {
                array[i++] = PropertyConverter.toInteger(interpolate(it.next())).intValue();
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                array = new int[1];
                array[0] = PropertyConverter.toInteger(interpolate(value)).intValue();
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of integers", e);
            }
        }

        return array;
    }

    /**
     * Get a list of Long objects associated with the given configuration key.
     * If the key doesn't map to an existing object an empty list is returned.
     *
     * @param key The configuration key.
     * @return The associated Long list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of longs.
     */
    public List getLongList(String key)
    {
        return getLongList(key, new ArrayList());
    }

    /**
     * Get a list of Long objects associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of Longs.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of longs.
     */
    public List getLongList(String key, List defaultValue)
    {
        Object value = getProperty(key);

        List list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value instanceof long[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, ArrayUtils.toObject((long[]) value));
        }
        else if (value instanceof Long[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, (Long[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            list = new ArrayList();

            Iterator it = values.iterator();
            while (it.hasNext())
            {
                list.add(PropertyConverter.toLong(interpolate(it.next())));
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                list = new ArrayList();
                list.add(PropertyConverter.toLong(interpolate(value)));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of longs", e);
            }
        }

        return list;
    }

    /**
     * Get an array of long primitives associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @return The associated long array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of longs.
     */
    public long[] getLongArray(String key)
    {
        return getLongArray(key, new long[0]);
    }

    /**
     * Get an array of long primitives associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @param defaultValue the default value, which will be returned if the property is not found
     * @return The associated long array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of longs.
     */
    public long[] getLongArray(String key, long[] defaultValue)
    {
        Object value = getProperty(key);

        long[] array;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            array = defaultValue;
        }
        else if (value instanceof long[])
        {
            array = (long[]) value;
        }
        else if (value instanceof Long[])
        {
            array = ArrayUtils.toPrimitive((Long[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            array = new long[values.size()];

            int i = 0;
            Iterator it = values.iterator();
            while (it.hasNext())
            {
                array[i++] = PropertyConverter.toLong(interpolate(it.next())).longValue();
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                array = new long[1];
                array[0] = PropertyConverter.toLong(interpolate(value)).longValue();
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of longs", e);
            }
        }

        return array;
    }

    /**
     * Get a list of Float objects associated with the given configuration key.
     * If the key doesn't map to an existing object an empty list is returned.
     *
     * @param key The configuration key.
     * @return The associated Float list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of floats.
     */
    public List getFloatList(String key)
    {
        return getFloatList(key, new ArrayList());
    }

    /**
     * Get a list of Float objects associated with the given
     * configuration key. If the key doesn't map to an existing object,
     * the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of Floats.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of floats.
     */
    public List getFloatList(String key, List defaultValue)
    {
        Object value = getProperty(key);

        List list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value instanceof float[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, ArrayUtils.toObject((float[]) value));
        }
        else if (value instanceof Float[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, (Float[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            list = new ArrayList();

            Iterator it = values.iterator();
            while (it.hasNext())
            {
                list.add(PropertyConverter.toFloat(interpolate(it.next())));
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                list = new ArrayList();
                list.add(PropertyConverter.toFloat(interpolate(value)));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of floats", e);
            }
        }

        return list;
    }

    /**
     * Get an array of float primitives associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @return The associated float array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of floats.
     */
    public float[] getFloatArray(String key)
    {
        return getFloatArray(key, new float[0]);
    }

    /**
     * Get an array of float primitives associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @param defaultValue the default value, which will be returned if the property is not found
     * @return The associated float array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of floats.
     */
    public float[] getFloatArray(String key, float[] defaultValue)
    {
        Object value = getProperty(key);

        float[] array;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            array = defaultValue;
        }
        else if (value instanceof float[])
        {
            array = (float[]) value;
        }
        else if (value instanceof Float[])
        {
            array = ArrayUtils.toPrimitive((Float[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            array = new float[values.size()];

            int i = 0;
            Iterator it = values.iterator();
            while (it.hasNext())
            {
                array[i++] = PropertyConverter.toFloat(interpolate(it.next())).floatValue();
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                array = new float[1];
                array[0] = PropertyConverter.toFloat(interpolate(value)).floatValue();
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of floats", e);
            }
        }

        return array;
    }

    /**
     * Get a list of Double objects associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty list is returned.
     *
     * @param key The configuration key.
     * @return The associated Double list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of doubles.
     */
    public List getDoubleList(String key)
    {
        return getDoubleList(key, new ArrayList());
    }

    /**
     * Get a list of Double objects associated with the given
     * configuration key. If the key doesn't map to an existing object,
     * the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of Doubles.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of doubles.
     */
    public List getDoubleList(String key, List defaultValue)
    {
        Object value = getProperty(key);

        List list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value instanceof double[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, ArrayUtils.toObject((double[]) value));
        }
        else if (value instanceof Double[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, (Double[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            list = new ArrayList();

            Iterator it = values.iterator();
            while (it.hasNext())
            {
                list.add(PropertyConverter.toDouble(interpolate(it.next())));
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                list = new ArrayList();
                list.add(PropertyConverter.toDouble(interpolate(value)));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of doubles", e);
            }
        }

        return list;
    }

    /**
     * Get an array of double primitives associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @return The associated double array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of doubles.
     */
    public double[] getDoubleArray(String key)
    {
        return getDoubleArray(key, new double[0]);
    }

    /**
     * Get an array of double primitives associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @param defaultValue the default value, which will be returned if the property is not found
     * @return The associated double array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of doubles.
     */
    public double[] getDoubleArray(String key, double[] defaultValue)
    {
        Object value = getProperty(key);

        double[] array;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            array = defaultValue;
        }
        else if (value instanceof double[])
        {
            array = (double[]) value;
        }
        else if (value instanceof Double[])
        {
            array = ArrayUtils.toPrimitive((Double[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            array = new double[values.size()];

            int i = 0;
            Iterator it = values.iterator();
            while (it.hasNext())
            {
                array[i++] = PropertyConverter.toDouble(interpolate(it.next())).doubleValue();
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                array = new double[1];
                array[0] = PropertyConverter.toDouble(interpolate(value)).doubleValue();
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of doubles", e);
            }
        }

        return array;
    }

    /**
     * Get a list of BigIntegers associated with the given configuration key.
     * If the key doesn't map to an existing object an empty list is returned.
     *
     * @param key The configuration key.
     * @return The associated BigInteger list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of BigIntegers.
     */
    public List getBigIntegerList(String key)
    {
        return getBigIntegerList(key, new ArrayList());
    }

    /**
     * Get a list of BigIntegers associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of BigIntegers.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of BigIntegers.
     */
    public List getBigIntegerList(String key, List defaultValue)
    {
        Object value = getProperty(key);

        List list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value instanceof BigInteger[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, (BigInteger[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            list = new ArrayList();

            Iterator it = values.iterator();
            while (it.hasNext())
            {
                list.add(PropertyConverter.toBigInteger(interpolate(it.next())));
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                list = new ArrayList();
                list.add(PropertyConverter.toBigInteger(interpolate(value)));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of big integers", e);
            }
        }

        return list;
    }

    /**
     * Get an array of BigIntegers associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @return The associated BigInteger array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of BigIntegers.
     */
    public BigInteger[] getBigIntegerArray(String key)
    {
        return getBigIntegerArray(key, new BigInteger[0]);
    }

    /**
     * Get an array of BigIntegers associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @param defaultValue the default value, which will be returned if the property is not found
     * @return The associated BigInteger array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of BigIntegers.
     */
    public BigInteger[] getBigIntegerArray(String key, BigInteger[] defaultValue)
    {
        List list = getBigIntegerList(key);
        if (list.isEmpty())
        {
            return defaultValue;
        }
        else
        {
            return (BigInteger[]) list.toArray(new BigInteger[list.size()]);
        }
    }

    /**
     * Get a list of BigDecimals associated with the given configuration key.
     * If the key doesn't map to an existing object an empty list is returned.
     *
     * @param key The configuration key.
     * @return The associated BigDecimal list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of BigDecimals.
     */
    public List getBigDecimalList(String key)
    {
        return getBigDecimalList(key, new ArrayList());
    }

    /**
     * Get a list of BigDecimals associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of BigDecimals.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of BigDecimals.
     */
    public List getBigDecimalList(String key, List defaultValue)
    {
        Object value = getProperty(key);

        List list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value instanceof BigDecimal[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, (BigDecimal[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            list = new ArrayList();

            Iterator it = values.iterator();
            while (it.hasNext())
            {
                list.add(PropertyConverter.toBigDecimal(interpolate(it.next())));
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                list = new ArrayList();
                list.add(PropertyConverter.toBigDecimal(interpolate(value)));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of big decimals", e);
            }
        }

        return list;
    }

    /**
     * Get an array of BigDecimals associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @return The associated BigDecimal array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of BigDecimals.
     */
    public BigDecimal[] getBigDecimalArray(String key)
    {
        return getBigDecimalArray(key, new BigDecimal[0]);
    }

    /**
     * Get an array of BigDecimals associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @param defaultValue the default value, which will be returned if the property is not found
     * @return The associated BigDecimal array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of BigDecimals.
     */
    public BigDecimal[] getBigDecimalArray(String key, BigDecimal[] defaultValue)
    {
        List list = getBigDecimalList(key);
        if (list.isEmpty())
        {
            return defaultValue;
        }
        else
        {
            return (BigDecimal[]) list.toArray(new BigDecimal[list.size()]);
        }
    }

    /**
     * Get an URL associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated URL.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not an URL.
     */
    public URL getURL(String key)
    {
        return getURL(key, null);
    }

    /**
     * Get an URL associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated URL.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not an URL.
     */
    public URL getURL(String key, URL defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            try
            {
                return PropertyConverter.toURL(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to an URL", e);
            }
        }
    }

    /**
     * Get a list of URLs associated with the given configuration key.
     * If the key doesn't map to an existing object an empty list is returned.
     *
     * @param key The configuration key.
     * @return The associated URL list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of URLs.
     */
    public List getURLList(String key)
    {
        return getURLList(key, new ArrayList());
    }

    /**
     * Get a list of URLs associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of URLs.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of URLs.
     */
    public List getURLList(String key, List defaultValue)
    {
        Object value = getProperty(key);

        List list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value instanceof URL[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, (URL[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            list = new ArrayList();

            Iterator it = values.iterator();
            while (it.hasNext())
            {
                list.add(PropertyConverter.toURL(interpolate(it.next())));
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                list = new ArrayList();
                list.add(PropertyConverter.toURL(interpolate(value)));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of URLs", e);
            }
        }

        return list;
    }

    /**
     * Get an array of URLs associated with the given configuration key.
     * If the key doesn't map to an existing object an empty array is returned.
     *
     * @param key The configuration key.
     * @return The associated URL array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of URLs.
     */
    public URL[] getURLArray(String key)
    {
        return getURLArray(key, new URL[0]);
    }

    /**
     * Get an array of URLs associated with the given configuration key.
     * If the key doesn't map to an existing object an empty array is returned.
     *
     * @param key The configuration key.
     * @param defaultValue the default value, which will be returned if the property is not found
     * @return The associated URL array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of URLs.
     */
    public URL[] getURLArray(String key, URL[] defaultValue)
    {
        List list = getURLList(key);
        if (list.isEmpty())
        {
            return defaultValue;
        }
        else
        {
            return (URL[]) list.toArray(new URL[list.size()]);
        }
    }

    /**
     * Get a Date associated with the given configuration key. If the property
     * is a String, it will be parsed with the format defined by the user in
     * the {@link #DATE_FORMAT_KEY} property, or if it's not defined with the
     * {@link #DEFAULT_DATE_FORMAT} pattern.
     *
     * @param key The configuration key.
     * @return The associated Date.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Date.
     */
    public Date getDate(String key)
    {
        return getDate(key, getDefaultDateFormat());
    }

    /**
     * Get a Date associated with the given configuration key. If the property
     * is a String, it will be parsed with the specified format pattern.
     *
     * @param key    The configuration key.
     * @param format The non-localized {@link java.text.DateFormat} pattern.
     * @return The associated Date
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Date.
     */
    public Date getDate(String key, String format)
    {
        return getDate(key, null, format);
    }

    /**
     * Get a Date associated with the given configuration key. If the property
     * is a String, it will be parsed with the format defined by the user in
     * the {@link #DATE_FORMAT_KEY} property, or if it's not defined with the
     * {@link #DEFAULT_DATE_FORMAT} pattern. If the key doesn't map to an
     * existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated Date.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Date.
     */
    public Date getDate(String key, Date defaultValue)
    {
        return getDate(key, defaultValue, getDefaultDateFormat());
    }

    /**
     * Get a Date associated with the given configuration key. If the property
     * is a String, it will be parsed with the specified format pattern.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @param format       The non-localized {@link java.text.DateFormat} pattern.
     * @return The associated Date.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Date.
     */
    public Date getDate(String key, Date defaultValue, String format)
    {
        Object value = resolveContainerStore(key);

        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            try
            {
                return PropertyConverter.toDate(interpolate(value), format);
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Date", e);
            }
        }
    }

    /**
     * Get a list of Dates associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * format defined by the user in the {@link #DATE_FORMAT_KEY} property,
     * or if it's not defined with the {@link #DEFAULT_DATE_FORMAT} pattern.
     * If the key doesn't map to an existing object an empty list is returned.
     *
     * @param key The configuration key.
     * @return The associated Date list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Dates.
     */
    public List getDateList(String key)
    {
        return getDateList(key, new ArrayList());
    }

    /**
     * Get a list of Dates associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * specified format pattern. If the key doesn't map to an existing object
     * an empty list is returned.
     *
     * @param key    The configuration key.
     * @param format The non-localized {@link java.text.DateFormat} pattern.
     * @return The associated Date list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Dates.
     */
    public List getDateList(String key, String format)
    {
        return getDateList(key, new ArrayList(), format);
    }

    /**
     * Get a list of Dates associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * format defined by the user in the {@link #DATE_FORMAT_KEY} property,
     * or if it's not defined with the {@link #DEFAULT_DATE_FORMAT} pattern.
     * If the key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated Date list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Dates.
     */
    public List getDateList(String key, List defaultValue)
    {
        return getDateList(key, defaultValue, getDefaultDateFormat());
    }

    /**
     * Get a list of Dates associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * specified format pattern. If the key doesn't map to an existing object,
     * the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @param format       The non-localized {@link java.text.DateFormat} pattern.
     * @return The associated Date list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Dates.
     */
    public List getDateList(String key, List defaultValue, String format)
    {
        Object value = getProperty(key);

        List list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value instanceof Date[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, (Date[]) value);
        }
        else if (value instanceof Calendar[])
        {
            list = new ArrayList();
            Calendar[] values = (Calendar[]) value;

            for (int i = 0; i < values.length; i++)
            {
                list.add(values[i].getTime());
            }
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            list = new ArrayList();

            Iterator it = values.iterator();
            while (it.hasNext())
            {
                list.add(PropertyConverter.toDate(interpolate(it.next()), format));
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                list = new ArrayList();
                list.add(PropertyConverter.toDate(interpolate(value), format));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of Dates", e);
            }
        }

        return list;
    }

    /**
     * Get an array of Dates associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * format defined by the user in the {@link #DATE_FORMAT_KEY} property,
     * or if it's not defined with the {@link #DEFAULT_DATE_FORMAT} pattern.
     * If the key doesn't map to an existing object an empty array is returned.
     *
     * @param key The configuration key.
     * @return The associated Date array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Dates.
     */
    public Date[] getDateArray(String key)
    {
        return getDateArray(key, new Date[0]);
    }

    /**
     * Get an array of Dates associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * specified format pattern. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key    The configuration key.
     * @param format The non-localized {@link java.text.DateFormat} pattern.
     * @return The associated Date array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Dates.
     */
    public Date[] getDateArray(String key, String format)
    {
        return getDateArray(key, new Date[0], format);
    }

    /**
     * Get an array of Dates associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * format defined by the user in the {@link #DATE_FORMAT_KEY} property,
     * or if it's not defined with the {@link #DEFAULT_DATE_FORMAT} pattern.
     * If the key doesn't map to an existing object an empty array is returned.
     *
     * @param key The configuration key.
     * @param defaultValue the default value, which will be returned if the property is not found
     * @return The associated Date array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Dates.
     */
    public Date[] getDateArray(String key, Date[] defaultValue)
    {
        return getDateArray(key, defaultValue, getDefaultDateFormat());
    }

    /**
     * Get an array of Dates associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * specified format pattern. If the key doesn't map to an existing object,
     * the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @param format       The non-localized {@link java.text.DateFormat} pattern.
     * @return The associated Date array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Dates.
     */
    public Date[] getDateArray(String key, Date[] defaultValue, String format)
    {
        List list = getDateList(key, format);
        if (list.isEmpty())
        {
            return defaultValue;
        }
        else
        {
            return (Date[]) list.toArray(new Date[list.size()]);
        }
    }

    /**
     * Get a Calendar associated with the given configuration key. If the
     * property is a String, it will be parsed with the format defined by the
     * user in the {@link #DATE_FORMAT_KEY} property, or if it's not defined
     * with the {@link #DEFAULT_DATE_FORMAT} pattern.
     *
     * @param key The configuration key.
     * @return The associated Calendar.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Calendar.
     */
    public Calendar getCalendar(String key)
    {
        return getCalendar(key, getDefaultDateFormat());
    }

    /**
     * Get a Calendar associated with the given configuration key. If the
     * property is a String, it will be parsed with the specified format
     * pattern.
     *
     * @param key    The configuration key.
     * @param format The non-localized {@link java.text.DateFormat} pattern.
     * @return The associated Calendar
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Calendar.
     */
    public Calendar getCalendar(String key, String format)
    {
        return getCalendar(key, null, format);
    }

    /**
     * Get a Calendar associated with the given configuration key. If the
     * property is a String, it will be parsed with the format defined by the
     * user in the {@link #DATE_FORMAT_KEY} property, or if it's not defined
     * with the {@link #DEFAULT_DATE_FORMAT} pattern. If the key doesn't map
     * to an existing object, the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated Calendar.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Calendar.
     */
    public Calendar getCalendar(String key, Calendar defaultValue)
    {
        return getCalendar(key, defaultValue, getDefaultDateFormat());
    }

    /**
     * Get a Calendar associated with the given configuration key. If the
     * property is a String, it will be parsed with the specified format
     * pattern. If the key doesn't map to an existing object, the default
     * value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @param format       The non-localized {@link java.text.DateFormat} pattern.
     * @return The associated Calendar.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Calendar.
     */
    public Calendar getCalendar(String key, Calendar defaultValue, String format)
    {
        Object value = resolveContainerStore(key);

        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            try
            {
                return PropertyConverter.toCalendar(interpolate(value), format);
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Calendar", e);
            }
        }
    }

    /**
     * Get a list of Calendars associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * format defined by the user in the {@link #DATE_FORMAT_KEY} property,
     * or if it's not defined with the {@link #DEFAULT_DATE_FORMAT} pattern.
     * If the key doesn't map to an existing object an empty list is returned.
     *
     * @param key The configuration key.
     * @return The associated Calendar list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Calendars.
     */
    public List getCalendarList(String key)
    {
        return getCalendarList(key, new ArrayList());
    }

    /**
     * Get a list of Calendars associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * specified format pattern. If the key doesn't map to an existing object
     * an empty list is returned.
     *
     * @param key    The configuration key.
     * @param format The non-localized {@link java.text.DateFormat} pattern.
     * @return The associated Calendar list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Calendars.
     */
    public List getCalendarList(String key, String format)
    {
        return getCalendarList(key, new ArrayList(), format);
    }

    /**
     * Get a list of Calendars associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * format defined by the user in the {@link #DATE_FORMAT_KEY} property,
     * or if it's not defined with the {@link #DEFAULT_DATE_FORMAT} pattern.
     * If the key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated Calendar list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Calendars.
     */
    public List getCalendarList(String key, List defaultValue)
    {
        return getCalendarList(key, defaultValue, getDefaultDateFormat());
    }

    /**
     * Get a list of Calendars associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * specified format pattern. If the key doesn't map to an existing object,
     * the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @param format       The non-localized {@link java.text.DateFormat} pattern.
     * @return The associated Calendar list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Calendars.
     */
    public List getCalendarList(String key, List defaultValue, String format)
    {
        Object value = getProperty(key);

        List list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value instanceof Calendar[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, (Calendar[]) value);
        }
        else if (value instanceof Date[])
        {
            list = new ArrayList();
            Date[] values = (Date[]) value;

            for (int i = 0; i < values.length; i++)
            {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(values[i]);
                list.add(calendar);
            }
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            list = new ArrayList();

            Iterator it = values.iterator();
            while (it.hasNext())
            {
                list.add(PropertyConverter.toCalendar(interpolate(it.next()), format));
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                list = new ArrayList();
                list.add(PropertyConverter.toCalendar(interpolate(value), format));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of Calendars", e);
            }
        }

        return list;
    }

    /**
     * Get an array of Calendars associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * format defined by the user in the {@link #DATE_FORMAT_KEY} property,
     * or if it's not defined with the {@link #DEFAULT_DATE_FORMAT} pattern.
     * If the key doesn't map to an existing object an empty array is returned.
     *
     * @param key The configuration key.
     * @return The associated Calendar array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Calendars.
     */
    public Calendar[] getCalendarArray(String key)
    {
        return getCalendarArray(key, new Calendar[0]);
    }

    /**
     * Get an array of Calendars associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * specified format pattern. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key    The configuration key.
     * @param format The non-localized {@link java.text.DateFormat} pattern.
     * @return The associated Calendar array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Calendars.
     */
    public Calendar[] getCalendarArray(String key, String format)
    {
        return getCalendarArray(key, new Calendar[0], format);
    }

    /**
     * Get an array of Calendars associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * format defined by the user in the {@link #DATE_FORMAT_KEY} property,
     * or if it's not defined with the {@link #DEFAULT_DATE_FORMAT} pattern.
     * If the key doesn't map to an existing object an empty array is returned.
     *
     * @param key The configuration key.
     * @param defaultValue the default value, which will be returned if the property is not found
     * @return The associated Calendar array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Calendars.
     */
    public Calendar[] getCalendarArray(String key, Calendar[] defaultValue)
    {
        return getCalendarArray(key, defaultValue, getDefaultDateFormat());
    }

    /**
     * Get an array of Calendars associated with the given configuration key.
     * If the property is a list of Strings, they will be parsed with the
     * specified format pattern. If the key doesn't map to an existing object,
     * the default value is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @param format       The non-localized {@link java.text.DateFormat} pattern.
     * @return The associated Calendar array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Calendars.
     */
    public Calendar[] getCalendarArray(String key, Calendar[] defaultValue, String format)
    {
        List list = getCalendarList(key, format);
        if (list.isEmpty())
        {
            return defaultValue;
        }
        else
        {
            return (Calendar[]) list.toArray(new Calendar[list.size()]);
        }
    }

    /**
     * Returns the date format specified by the user in the DATE_FORMAT_KEY
     * property, or the default format otherwise.
     *
     * @return the default date format
     */
    private String getDefaultDateFormat()
    {
        return getString(DATE_FORMAT_KEY, DEFAULT_DATE_FORMAT);
    }

    /**
     * Get a Locale associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated Locale.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Locale.
     */
    public Locale getLocale(String key)
    {
        return getLocale(key, null);
    }

    /**
     * Get a Locale associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated Locale.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Locale.
     */
    public Locale getLocale(String key, Locale defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            try
            {
                return PropertyConverter.toLocale(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Locale", e);
            }
        }
    }

    /**
     * Get a list of Locales associated with the given configuration key.
     * If the key doesn't map to an existing object an empty list is returned.
     *
     * @param key The configuration key.
     * @return The associated Locale list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Locales.
     */
    public List getLocaleList(String key)
    {
        return getLocaleList(key, new ArrayList());
    }

    /**
     * Get a list of Locales associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of Locales.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Locales.
     */
    public List getLocaleList(String key, List defaultValue)
    {
        Object value = getProperty(key);

        List list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value instanceof Locale[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, (Locale[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            list = new ArrayList();

            Iterator it = values.iterator();
            while (it.hasNext())
            {
                list.add(PropertyConverter.toLocale(interpolate(it.next())));
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                list = new ArrayList();
                list.add(PropertyConverter.toLocale(interpolate(value)));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of Locales", e);
            }
        }

        return list;
    }

    /**
     * Get an array of Locales associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @return The associated Locale array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Locales.
     */
    public Locale[] getLocaleArray(String key)
    {
        return getLocaleArray(key, new Locale[0]);
    }

    /**
     * Get an array of Locales associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @param defaultValue the default value, which will be returned if the property is not found
     * @return The associated Locale array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Locales.
     */
    public Locale[] getLocaleArray(String key, Locale[] defaultValue)
    {
        List list = getLocaleList(key);
        if (list.isEmpty())
        {
            return defaultValue;
        }
        else
        {
            return (Locale[]) list.toArray(new Locale[list.size()]);
        }
    }

    /**
     * Get a Color associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated Color.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Color.
     */
    public Color getColor(String key)
    {
        return getColor(key, null);
    }

    /**
     * Get a Color associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated Color.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Color.
     */
    public Color getColor(String key, Color defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            try
            {
                return PropertyConverter.toColor(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Color", e);
            }
        }
    }

    /**
     * Get a list of Colors associated with the given configuration key.
     * If the key doesn't map to an existing object an empty list is returned.
     *
     * @param key The configuration key.
     * @return The associated Color list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Colors.
     */
    public List getColorList(String key)
    {
        return getColorList(key, new ArrayList());
    }

    /**
     * Get a list of Colors associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of Colors.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Colors.
     */
    public List getColorList(String key, List defaultValue)
    {
        Object value = getProperty(key);

        List list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value instanceof Color[])
        {
            list = new ArrayList();
            CollectionUtils.addAll(list, (Color[]) value);
        }
        else if (value instanceof Collection)
        {
            Collection values = (Collection) value;
            list = new ArrayList();

            Iterator it = values.iterator();
            while (it.hasNext())
            {
                list.add(PropertyConverter.toColor(interpolate(it.next())));
            }
        }
        else
        {
            try
            {
                // attempt to convert a single value
                list = new ArrayList();
                list.add(PropertyConverter.toColor(interpolate(value)));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a list of Colors", e);
            }
        }

        return list;
    }

    /**
     * Get an array of Colors associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @return The associated Color array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Colors.
     */
    public Color[] getColorArray(String key)
    {
        return getColorArray(key, new Color[0]);
    }

    /**
     * Get an array of Colors associated with the given
     * configuration key. If the key doesn't map to an existing object
     * an empty array is returned.
     *
     * @param key The configuration key.
     * @param defaultValue the default value, which will be returned if the property is not found
     * @return The associated Color array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of Colors.
     */
    public Color[] getColorArray(String key, Color[] defaultValue)
    {
        List list = getColorList(key);
        if (list.isEmpty())
        {
            return defaultValue;
        }
        else
        {
            return (Color[]) list.toArray(new Color[list.size()]);
        }
    }

}
