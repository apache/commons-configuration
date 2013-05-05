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
import java.io.Serializable;
import java.lang.reflect.Array;
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
import java.util.NoSuchElementException;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Decorator providing additional getters for any Configuration. This extended
 * Configuration supports more types:
 * <ul>
 *   <li>{@link java.net.URL}</li>
 *   <li>{@link java.util.Locale}</li>
 *   <li>{@link java.util.Date}</li>
 *   <li>{@link java.util.Calendar}</li>
 *   <li>{@link java.awt.Color}</li>
 *   <li>{@link java.net.InetAddress}</li>
 *   <li>{@link javax.mail.internet.InternetAddress} (requires Javamail in the classpath)</li>
 *   <li>{@link java.lang.Enum} (Java 5 enumeration types)</li>
 * </ul>
 *
 * Lists and arrays are available for all types.
 *
 * <h4>Example</h4>
 *
 * Configuration file <tt>config.properties</tt>:
 * <pre>
 * title.color = #0000FF
 * remote.host = 192.168.0.53
 * default.locales = fr,en,de
 * email.contact = ebourg@apache.org, oheger@apache.org
 * </pre>
 *
 * Usage:
 *
 * <pre>
 * DataConfiguration config = new DataConfiguration(new PropertiesConfiguration("config.properties"));
 *
 * // retrieve a property using a specialized getter
 * Color color = config.getColor("title.color");
 *
 * // retrieve a property using a generic getter
 * InetAddress host = (InetAddress) config.get(InetAddress.class, "remote.host");
 * Locale[] locales = (Locale[]) config.getArray(Locale.class, "default.locales");
 * List contacts = config.getList(InternetAddress.class, "email.contact");
 * </pre>
 *
 * <h4>Dates</h4>
 *
 * Date objects are expected to be formatted with the pattern <tt>yyyy-MM-dd HH:mm:ss</tt>.
 * This default format can be changed by specifying another format in the
 * getters, or by putting a date format in the configuration under the key
 * <tt>org.apache.commons.configuration.format.date</tt>.
 *
 * @author <a href="ebourg@apache.org">Emmanuel Bourg</a>
 * @version $Id$
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
     * Creates a new instance of {@code DataConfiguration} and sets the
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

    @Override
    protected void addPropertyDirect(String key, Object obj)
    {
        if (configuration instanceof AbstractConfiguration)
        {
            ((AbstractConfiguration) configuration).addPropertyDirect(key, obj);
        }
        else
        {
            configuration.addProperty(key, obj);
        }
    }

    public boolean isEmpty()
    {
        return configuration.isEmpty();
    }

    public boolean containsKey(String key)
    {
        return configuration.containsKey(key);
    }

    @Override
    public void clearProperty(String key)
    {
        configuration.clearProperty(key);
    }

    @Override
    protected void clearPropertyDirect(String key)
    {
        if (configuration instanceof AbstractConfiguration)
        {
            ((AbstractConfiguration) configuration).clearPropertyDirect(key);
        }
        else
        {
            configuration.clearProperty(key);
        }
    }

    @Override
    public void setProperty(String key, Object value)
    {
        configuration.setProperty(key, value);
    }

    public Iterator<String> getKeys()
    {
        return configuration.getKeys();
    }

    /**
     * Get an object of the specified type associated with the given
     * configuration key. If the key doesn't map to an existing object, the
     * method returns null unless {@link #isThrowExceptionOnMissing()} is set
     * to <tt>true</tt>.
     *
     * @param <T> the target type of the value
     * @param cls the target class of the value
     * @param key the key of the value
     *
     * @return the value of the requested type for the key
     *
     * @throws NoSuchElementException if the key doesn't map to an existing
     *     object and <tt>throwExceptionOnMissing=true</tt>
     * @throws ConversionException if the value is not compatible with the requested type
     *
     * @since 1.5
     */
    public <T> T get(Class<T> cls, String key)
    {
        T value = get(cls, key, null);
        if (value != null)
        {
            return value;
        }
        else if (isThrowExceptionOnMissing())
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
        else
        {
            return null;
        }
    }

    /**
     * Get an object of the specified type associated with the given
     * configuration key. If the key doesn't map to an existing object, the
     * default value is returned.
     *
     * @param <T>          the target type of the value
     * @param cls          the target class of the value
     * @param key          the key of the value
     * @param defaultValue the default value
     *
     * @return the value of the requested type for the key
     *
     * @throws ConversionException if the value is not compatible with the requested type
     *
     * @since 1.5
     */
    public <T> T get(Class<T> cls, String key, T defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value == null)
        {
            return defaultValue;
        }

        if (Date.class.equals(cls) || Calendar.class.equals(cls))
        {
            return convert(cls, key, interpolate(value), new String[] {getDefaultDateFormat()});
        }
        else
        {
            return convert(cls, key, interpolate(value), null);
        }
    }

    /**
     * Get a list of typed objects associated with the given configuration key.
     * If the key doesn't map to an existing object, an empty list is returned.
     *
     * @param <T> the type expected for the elements of the list
     * @param cls the class expected for the elements of the list
     * @param key The configuration key.
     * @return The associated list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an object that
     *     is not compatible with a list of the specified class.
     *
     * @since 1.5
     */
    public <T> List<T> getList(Class<T> cls, String key)
    {
        return getList(cls, key, new ArrayList<T>());
    }

    /**
     * Get a list of typed objects associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param <T>          the type expected for the elements of the list
     * @param cls          the class expected for the elements of the list
     * @param key          the configuration key.
     * @param defaultValue the default value.
     * @return The associated List.
     *
     * @throws ConversionException is thrown if the key maps to an object that
     *     is not compatible with a list of the specified class.
     *
     * @since 1.5
     */
    public <T> List<T> getList(Class<T> cls, String key, List<T> defaultValue)
    {
        Object value = getProperty(key);
        Class<?> valueClass = value != null ? value.getClass() : null;

        List<T> list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            // the value is null or is an empty string
            list = defaultValue;
        }
        else
        {
            list = new ArrayList<T>();

            Object[] params = null;
            if (cls.equals(Date.class) || cls.equals(Calendar.class))
            {
                params = new Object[] {getDefaultDateFormat()};
            }

            if (valueClass.isArray())
            {
                // get the class of the objects contained in the array
                Class<?> arrayType = valueClass.getComponentType();
                int length = Array.getLength(value);

                if (arrayType.equals(cls)
                        || (arrayType.isPrimitive() && cls.equals(ClassUtils.primitiveToWrapper(arrayType))))
                {
                    // the value is an array of the specified type, or an array
                    // of the primitive type derived from the specified type
                    for (int i = 0; i < length; i++)
                    {
                        list.add(cls.cast(Array.get(value, i)));
                    }
                }
                else
                {
                    // attempt to convert the elements of the array
                    for (int i = 0; i < length; i++)
                    {
                        list.add(convert(cls, key, interpolate(Array.get(value, i)), params));
                    }
                }
            }
            else if (value instanceof Collection)
            {
                Collection<?> values = (Collection<?>) value;

                for (Object o : values)
                {
                    list.add(convert(cls, key, interpolate(o), params));
                }
            }
            else
            {
                // attempt to convert a single value
                list.add(convert(cls, key, interpolate(value), params));
            }
        }

        return list;
    }

    /**
     * Get an array of typed objects associated with the given configuration key.
     * If the key doesn't map to an existing object, an empty list is returned.
     *
     * @param cls the type expected for the elements of the array
     * @param key The configuration key.
     * @return The associated array if the key is found, and the value compatible with the type specified.
     *
     * @throws ConversionException is thrown if the key maps to an object that
     *     is not compatible with a list of the specified class.
     *
     * @since 1.5
     */
    public Object getArray(Class<?> cls, String key)
    {
        return getArray(cls, key, Array.newInstance(cls, 0));
    }

    /**
     * Get an array of typed objects associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value is returned.
     *
     * @param cls          the type expected for the elements of the array
     * @param key          the configuration key.
     * @param defaultValue the default value
     * @return The associated array if the key is found, and the value compatible with the type specified.
     *
     * @throws ConversionException is thrown if the key maps to an object that
     *     is not compatible with an array of the specified class.
     * @throws IllegalArgumentException if the default value is not an array of the specified type
     *
     * @since 1.5
     */
    public Object getArray(Class<?> cls, String key, Object defaultValue)
    {
        // check the type of the default value
        if (defaultValue != null
                && (!defaultValue.getClass().isArray() || !cls
                        .isAssignableFrom(defaultValue.getClass()
                                .getComponentType())))
        {
            throw new IllegalArgumentException(
                    "The type of the default value (" + defaultValue.getClass()
                            + ")" + " is not an array of the specified class ("
                            + cls + ")");
        }

        if (cls.isPrimitive())
        {
            return getPrimitiveArray(cls, key, defaultValue);
        }

        List<?> list = getList(cls, key);
        if (list.isEmpty())
        {
            return defaultValue;
        }
        else
        {
            return list.toArray((Object[]) Array.newInstance(cls, list.size()));
        }
    }

    /**
     * Get an array of primitive values associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value is returned.
     *
     * @param cls          the primitive type expected for the elements of the array
     * @param key          the configuration key.
     * @param defaultValue the default value
     * @return The associated array if the key is found, and the value compatible with the type specified.
     *
     * @throws ConversionException is thrown if the key maps to an object that
     *     is not compatible with an array of the specified class.
     *
     * @since 1.5
     */
    private Object getPrimitiveArray(Class<?> cls, String key, Object defaultValue)
    {
        Object value = getProperty(key);
        Class<?> valueClass = value != null ? value.getClass() : null;

        Object array;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            // the value is null or is an empty string
            array = defaultValue;
        }
        else
        {
            if (valueClass.isArray())
            {
                // get the class of the objects contained in the array
                Class<?> arrayType = valueClass.getComponentType();
                int length = Array.getLength(value);

                if (arrayType.equals(cls))
                {
                    // the value is an array of the same primitive type
                    array = value;
                }
                else if (arrayType.equals(ClassUtils.primitiveToWrapper(cls)))
                {
                    // the value is an array of the wrapper type derived from the specified primitive type
                    array = Array.newInstance(cls, length);

                    for (int i = 0; i < length; i++)
                    {
                        Array.set(array, i, Array.get(value, i));
                    }
                }
                else
                {
                    throw new ConversionException('\'' + key + "' (" + arrayType + ")"
                            + " doesn't map to a compatible array of " + cls);
                }
            }
            else if (value instanceof Collection)
            {
                Collection<?> values = (Collection<?>) value;

                array = Array.newInstance(cls, values.size());

                int i = 0;
                for (Object o : values)
                {
                    // This is safe because PropertyConverter can handle
                    // conversion to wrapper classes correctly.
                    @SuppressWarnings("unchecked")
                    Object convertedValue = convert(ClassUtils.primitiveToWrapper(cls), key, interpolate(o), null);
                    Array.set(array, i++, convertedValue);
                }
            }
            else
            {
                // attempt to convert a single value
                // This is safe because PropertyConverter can handle
                // conversion to wrapper classes correctly.
                @SuppressWarnings("unchecked")
                Object convertedValue = convert(ClassUtils.primitiveToWrapper(cls), key, interpolate(value), null);

                // create an array of one element
                array = Array.newInstance(cls, 1);
                Array.set(array, 0, convertedValue);
            }
        }

        return array;
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
    public List<Boolean> getBooleanList(String key)
    {
        return getBooleanList(key, new ArrayList<Boolean>());
    }

    /**
     * Get a list of Boolean objects associated with the given
     * configuration key. If the key doesn't map to an existing object,
     * the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of Booleans.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of booleans.
     */
    public List<Boolean> getBooleanList(String key, List<Boolean> defaultValue)
    {
         return getList(Boolean.class, key, defaultValue);
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
        return (boolean[]) getArray(Boolean.TYPE, key);
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
        return (boolean[]) getArray(Boolean.TYPE, key, defaultValue);
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
    public List<Byte> getByteList(String key)
    {
        return getByteList(key, new ArrayList<Byte>());
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
    public List<Byte> getByteList(String key, List<Byte> defaultValue)
    {
        return getList(Byte.class, key, defaultValue);
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
        return (byte[]) getArray(Byte.TYPE, key, defaultValue);
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
    public List<Short> getShortList(String key)
    {
        return getShortList(key, new ArrayList<Short>());
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
    public List<Short> getShortList(String key, List<Short> defaultValue)
    {
        return getList(Short.class, key, defaultValue);
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
        return (short[]) getArray(Short.TYPE, key, defaultValue);
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
    public List<Integer> getIntegerList(String key)
    {
        return getIntegerList(key, new ArrayList<Integer>());
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
    public List<Integer> getIntegerList(String key, List<Integer> defaultValue)
    {
        return getList(Integer.class, key, defaultValue);
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
        return (int[]) getArray(Integer.TYPE, key, defaultValue);
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
    public List<Long> getLongList(String key)
    {
        return getLongList(key, new ArrayList<Long>());
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
    public List<Long> getLongList(String key, List<Long> defaultValue)
    {
        return getList(Long.class, key, defaultValue);
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
        return (long[]) getArray(Long.TYPE, key, defaultValue);
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
    public List<Float> getFloatList(String key)
    {
        return getFloatList(key, new ArrayList<Float>());
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
    public List<Float> getFloatList(String key, List<Float> defaultValue)
    {
        return getList(Float.class, key, defaultValue);
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
        return (float[]) getArray(Float.TYPE, key, defaultValue);
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
    public List<Double> getDoubleList(String key)
    {
        return getDoubleList(key, new ArrayList<Double>());
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
    public List<Double> getDoubleList(String key, List<Double> defaultValue)
    {
        return getList(Double.class, key, defaultValue);
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
        return (double[]) getArray(Double.TYPE, key, defaultValue);
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
    public List<BigInteger> getBigIntegerList(String key)
    {
        return getBigIntegerList(key, new ArrayList<BigInteger>());
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
    public List<BigInteger> getBigIntegerList(String key, List<BigInteger> defaultValue)
    {
        return getList(BigInteger.class, key, defaultValue);
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
        return (BigInteger[]) getArray(BigInteger.class, key, defaultValue);
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
    public List<BigDecimal> getBigDecimalList(String key)
    {
        return getBigDecimalList(key, new ArrayList<BigDecimal>());
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
    public List<BigDecimal> getBigDecimalList(String key, List<BigDecimal> defaultValue)
    {
        return getList(BigDecimal.class, key, defaultValue);
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
        return (BigDecimal[]) getArray(BigDecimal.class, key, defaultValue);
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
        return get(URL.class, key);
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
        return get(URL.class, key, defaultValue);
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
    public List<URL> getURLList(String key)
    {
        return getURLList(key, new ArrayList<URL>());
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
    public List<URL> getURLList(String key, List<URL> defaultValue)
    {
        return getList(URL.class, key, defaultValue);
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
        return (URL[]) getArray(URL.class, key, defaultValue);
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
        return get(Date.class, key);
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
        Date value = getDate(key, null, format);
        if (value != null)
        {
            return value;
        }
        else if (isThrowExceptionOnMissing())
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
        else
        {
            return null;
        }
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
    public List<Date> getDateList(String key)
    {
        return getDateList(key, new ArrayList<Date>());
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
    public List<Date> getDateList(String key, String format)
    {
        return getDateList(key, new ArrayList<Date>(), format);
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
    public List<Date> getDateList(String key, List<Date> defaultValue)
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
    public List<Date> getDateList(String key, List<Date> defaultValue, String format)
    {
        Object value = getProperty(key);

        List<Date> list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value.getClass().isArray())
        {
            list = new ArrayList<Date>();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++)
            {
                list.add(convert(Date.class, key, interpolate(Array.get(value, i)), new String[] {format}));
            }
        }
        else if (value instanceof Collection)
        {
            Collection<?> values = (Collection<?>) value;
            list = new ArrayList<Date>();

            for (Object o : values)
            {
                list.add(convert(Date.class, key, interpolate(o), new String[] {format}));
            }
        }
        else
        {
            // attempt to convert a single value
            list = new ArrayList<Date>();
            list.add(convert(Date.class, key, interpolate(value), new String[] {format}));
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
        List<Date> list = getDateList(key, format);
        if (list.isEmpty())
        {
            return defaultValue;
        }
        else
        {
            return list.toArray(new Date[list.size()]);
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
        return get(Calendar.class, key);
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
        Calendar value = getCalendar(key, null, format);
        if (value != null)
        {
            return value;
        }
        else if (isThrowExceptionOnMissing())
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
        else
        {
            return null;
        }
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
    public List<Calendar> getCalendarList(String key)
    {
        return getCalendarList(key, new ArrayList<Calendar>());
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
    public List<Calendar> getCalendarList(String key, String format)
    {
        return getCalendarList(key, new ArrayList<Calendar>(), format);
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
    public List<Calendar> getCalendarList(String key, List<Calendar> defaultValue)
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
    public List<Calendar> getCalendarList(String key, List<Calendar> defaultValue, String format)
    {
        Object value = getProperty(key);

        List<Calendar> list;

        if (value == null || (value instanceof String && StringUtils.isEmpty((String) value)))
        {
            list = defaultValue;
        }
        else if (value.getClass().isArray())
        {
            list = new ArrayList<Calendar>();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++)
            {
                list.add(convert(Calendar.class, key, interpolate(Array.get(value, i)), new String[] {format}));
            }
        }
        else if (value instanceof Collection)
        {
            Collection<?> values = (Collection<?>) value;
            list = new ArrayList<Calendar>();

            for (Object o : values)
            {
                list.add(convert(Calendar.class, key, interpolate(o), new String[] {format}));
            }
        }
        else
        {
            // attempt to convert a single value
            list = new ArrayList<Calendar>();
            list.add(convert(Calendar.class, key, interpolate(value), new String[] {format}));
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
        List<Calendar> list = getCalendarList(key, format);
        if (list.isEmpty())
        {
            return defaultValue;
        }
        else
        {
            return list.toArray(new Calendar[list.size()]);
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
        return get(Locale.class, key);
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
        return get(Locale.class, key, defaultValue);
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
    public List<Locale> getLocaleList(String key)
    {
        return getLocaleList(key, new ArrayList<Locale>());
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
    public List<Locale> getLocaleList(String key, List<Locale> defaultValue)
    {
        return getList(Locale.class, key, defaultValue);
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
        return (Locale[]) getArray(Locale.class, key, defaultValue);
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
        return get(Color.class, key);
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
        return get(Color.class, key, defaultValue);
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
    public List<Color> getColorList(String key)
    {
        return getColorList(key, new ArrayList<Color>());
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
    public List<Color> getColorList(String key, List<Color> defaultValue)
    {
        return getList(Color.class, key, defaultValue);
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
        return (Color[]) getArray(Color.class, key, defaultValue);
    }

    /**
     * Helper method for performing a type conversion using the
     * {@code PropertyConverter} class.
     *
     * @param <T> the target type of the conversion
     * @param cls the target class of the conversion
     * @param key the configuration key
     * @param value the value to be converted
     * @param params additional parameters
     * @throws ConversionException if the value is not compatible with the
     *         requested type
     */
    private static <T> T convert(Class<T> cls, String key, Object value,
            Object[] params)
    {
        try
        {
            Object result = PropertyConverter.to(cls, value, params);
            // Will not throw a ClassCastException because PropertyConverter
            // would have thrown a ConversionException if conversion had failed.
            return cls.cast(result);
        }
        catch (ConversionException e)
        {
            throw new ConversionException('\'' + key + "' doesn't map to a "
                    + cls, e);
        }
    }
}
