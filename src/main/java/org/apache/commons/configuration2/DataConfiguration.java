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

import java.awt.Color;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.apache.commons.configuration2.convert.ConversionHandler;
import org.apache.commons.configuration2.convert.DefaultConversionHandler;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.lang3.StringUtils;

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
 *   <li>{@code javax.mail.internet.InternetAddress} (requires Javamail in the classpath)</li>
 *   <li>{@link java.lang.Enum} (Java 5 enumeration types)</li>
 * </ul>
 *
 * Lists and arrays are available for all types.<br>
 * Note that this class is only a thin wrapper over functionality already
 * provided by {@link AbstractConfiguration}. Basically, the generic
 * {@code get()}, and {@code getCollection()} methods are
 * used to actually perform data conversions.
 *
 * <p><strong>Example</strong></p>
 *
 * Configuration file {@code config.properties}:
 * <pre>
 * title.color = #0000FF
 * remote.host = 192.168.0.53
 * default.locales = fr,en,de
 * email.contact = ebourg@apache.org, tester@test.org
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
 * <p><strong>Dates</strong></p>
 *
 * Date objects are expected to be formatted with the pattern {@code yyyy-MM-dd HH:mm:ss}.
 * This default format can be changed by specifying another format in the
 * getters, or by putting a date format in the configuration under the key
 * {@code org.apache.commons.configuration.format.date}. Alternatively, the
 * date format can also be specified via the {@code ConversionHandler} used
 * by a configuration instance:
 *
 * <pre>
 * DefaultConversionHandler handler = new DefaultConversionHandler();
 * handler.setDateFormat("mm/dd/yyyy");
 * config.setConversionHandler(handler);
 * </pre>
 *
 * @author <a href="ebourg@apache.org">Emmanuel Bourg</a>
 * @version $Id$
 * @since 1.1
 */
public class DataConfiguration extends AbstractConfiguration
{
    /** The key of the property storing the user defined date format. */
    public static final String DATE_FORMAT_KEY = "org.apache.commons.configuration.format.date";

    /** The default format for dates. */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** Stores temporary date formats. */
    private static final ThreadLocal<String> TEMP_DATE_FORMAT = new ThreadLocal<>();

    /** Stores the wrapped configuration.*/
    private final Configuration configuration;

    /** A special conversion handler object used by this configuration. */
    private final ConversionHandler dataConversionHandler;

    /**
     * Creates a new instance of {@code DataConfiguration} and sets the
     * wrapped configuration.
     *
     * @param configuration the wrapped configuration
     */
    public DataConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
        dataConversionHandler = new DataConversionHandler();
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

    /**
     * {@inheritDoc} This implementation returns the special conversion handler
     * used by this configuration instance.
     */
    @Override
    public ConversionHandler getConversionHandler()
    {
        return dataConversionHandler;
    }

    @Override
    protected Object getPropertyInternal(String key)
    {
        return configuration.getProperty(key);
    }

    @Override
    protected void addPropertyInternal(String key, Object obj)
    {
        configuration.addProperty(key, obj);
    }

    @Override
    protected void addPropertyDirect(String key, Object value)
    {
        if (configuration instanceof AbstractConfiguration)
        {
            ((AbstractConfiguration) configuration).addPropertyDirect(key, value);
        }
        else
        {
            configuration.addProperty(key, value);
        }
    }

    @Override
    protected boolean isEmptyInternal()
    {
        return configuration.isEmpty();
    }

    @Override
    protected boolean containsKeyInternal(String key)
    {
        return configuration.containsKey(key);
    }

    @Override
    protected void clearPropertyDirect(String key)
    {
        configuration.clearProperty(key);
    }

    @Override
    protected void setPropertyInternal(String key, Object value)
    {
        configuration.setProperty(key, value);
    }

    @Override
    protected Iterator<String> getKeysInternal()
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
        return get(boolean[].class, key, defaultValue);
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
        return get(byte[].class, key, defaultValue);
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
        return get(short[].class, key, defaultValue);
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
        return get(int[].class, key, defaultValue);
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
        return get(long[].class, key, defaultValue);
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
        return get(float[].class, key, defaultValue);
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
        return get(double[].class, key, defaultValue);
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
        return get(BigInteger[].class, key, defaultValue);
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
        return get(BigDecimal[].class, key, defaultValue);
    }

    /**
     * Get an URI associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated URI.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not an URI.
     */
    public URI getURI(String key)
    {
        return get(URI.class, key);
    }

    /**
     * Get an URI associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     * @return The associated URI.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not an URI.
     */
    public URI getURI(String key, URI defaultValue)
    {
        return get(URI.class, key, defaultValue);
    }

    /**
     * Get an array of URIs associated with the given configuration key.
     * If the key doesn't map to an existing object an empty array is returned.
     *
     * @param key The configuration key.
     * @return The associated URI array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of URIs.
     */
    public URI[] getURIArray(String key)
    {
        return getURIArray(key, new URI[0]);
    }

    /**
     * Get an array of URIs associated with the given configuration key.
     * If the key doesn't map to an existing object an empty array is returned.
     *
     * @param key The configuration key.
     * @param defaultValue the default value, which will be returned if the property is not found
     * @return The associated URI array if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of URIs.
     */
    public URI[] getURIArray(String key, URI[] defaultValue)
    {
        return get(URI[].class, key, defaultValue);
    }

    /**
     * Get a list of URIs associated with the given configuration key.
     * If the key doesn't map to an existing object an empty list is returned.
     *
     * @param key The configuration key.
     * @return The associated URI list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of URIs.
     */
    public List<URI> getURIList(String key)
    {
        return getURIList(key, new ArrayList<URI>());
    }

    /**
     * Get a list of URIs associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value is
     * returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of URIs.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a list of URIs.
     */
    public List<URI> getURIList(String key, List<URI> defaultValue)
    {
        return getList(URI.class, key, defaultValue);
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
        return get(URL[].class, key, defaultValue);
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
        return getDate(key, defaultValue, null);
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
        TEMP_DATE_FORMAT.set(format);
        try
        {
            return get(Date.class, key, defaultValue);
        }
        finally
        {
            TEMP_DATE_FORMAT.remove();
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
        return getDateList(key, defaultValue, null);
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
        TEMP_DATE_FORMAT.set(format);
        try
        {
            return getList(Date.class, key, defaultValue);
        }
        finally
        {
            TEMP_DATE_FORMAT.remove();
        }
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
        return getDateArray(key, defaultValue, null);
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
        TEMP_DATE_FORMAT.set(format);
        try
        {
            return get(Date[].class, key, defaultValue);
        }
        finally
        {
            TEMP_DATE_FORMAT.remove();
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
        return getCalendar(key, defaultValue, null);
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
        TEMP_DATE_FORMAT.set(format);
        try
        {
            return get(Calendar.class, key, defaultValue);
        }
        finally
        {
            TEMP_DATE_FORMAT.remove();
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
        return getCalendarList(key, defaultValue, null);
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
        TEMP_DATE_FORMAT.set(format);
        try
        {
            return getList(Calendar.class, key, defaultValue);
        }
        finally
        {
            TEMP_DATE_FORMAT.remove();
        }
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
        return getCalendarArray(key, defaultValue, null);
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
        TEMP_DATE_FORMAT.set(format);
        try
        {
            return get(Calendar[].class, key, defaultValue);
        }
        finally
        {
            TEMP_DATE_FORMAT.remove();
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
        return get(Locale[].class, key, defaultValue);
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
        return get(Color[].class, key, defaultValue);
    }

    /**
     * Returns the original conversion handler set for this configuration. If
     * this is not a {@code DefaultConversionHandler}, result is <b>null</b>.
     *
     * @return the original conversion handler or <b>null</b>
     */
    private DefaultConversionHandler getOriginalConversionHandler()
    {
        ConversionHandler handler = super.getConversionHandler();
        return (DefaultConversionHandler) ((handler instanceof DefaultConversionHandler) ? handler
                : null);
    }

    /**
     * A specialized {@code ConversionHandler} implementation which allows
     * overriding the date format pattern. This class takes care that the format
     * pattern can be defined as a property of the wrapped configuration or
     * temporarily passed when calling a conversion method.
     */
    private class DataConversionHandler extends DefaultConversionHandler
    {
        /**
         * {@inheritDoc} This implementation checks for a defined data format in
         * the following order:
         * <ul>
         * <li>If a temporary date format is set for the current call, it is
         * used.</li>
         * <li>If a date format is specified in this configuration using the
         * {@code DATE_FORMAT_KEY} property, it is used.</li>
         * <li>Otherwise, the date format set for the original conversion
         * handler is used if available.</li>
         * </ul>
         */
        @Override
        public String getDateFormat()
        {
            if (StringUtils.isNotEmpty(TEMP_DATE_FORMAT.get()))
            {
                return TEMP_DATE_FORMAT.get();
            }
            if (containsKey(DATE_FORMAT_KEY))
            {
                return getDefaultDateFormat();
            }

            DefaultConversionHandler orgHandler =
                    getOriginalConversionHandler();
            return (orgHandler != null) ? orgHandler.getDateFormat() : null;
        }
    }
}
