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

package org.apache.commons.configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Configuration interface.
 *
 * @version $Id$
 */
public interface Configuration
{
    /**
     * Return a decorator Configuration containing every key from the current
     * Configuration that starts with the specified prefix. The prefix is
     * removed from the keys in the subset. For example, if the configuration
     * contains the following properties:
     *
     * <pre>
     *    prefix.number = 1
     *    prefix.string = Apache
     *    prefixed.foo = bar
     *    prefix = Jakarta</pre>
     *
     * the Configuration returned by <code>subset("prefix")</code> will contain
     * the properties:
     *
     * <pre>
     *    number = 1
     *    string = Apache
     *    = Jakarta</pre>
     *
     * (The key for the value "Jakarta" is an empty string)
     * <p>
     * Since the subset is a decorator and not a modified copy of the initial
     * Configuration, any change made to the subset is available to the
     * Configuration, and reciprocally.
     *
     * @param prefix The prefix used to select the properties.
     *
     * @see SubsetConfiguration
     */
    Configuration subset(String prefix);

    /**
     * Check if the configuration is empty.
     *
     * @return <code>true</code> if the configuration contains no property,
     *         <code>false</code> otherwise.
     */
    boolean isEmpty();

    /**
     * Check if the configuration contains the specified key.
     *
     * @param key the key whose presence in this configuration is to be tested
     *
     * @return <code>true</code> if the configuration contains a value for this
     *         key, <code>false</code> otherwise
     */
    boolean containsKey(String key);

    /**
     * Add a property to the configuration. If it already exists then the value
     * stated here will be added to the configuration entry. For example, if
     * the property:
     *
     * <pre>resource.loader = file</pre>
     *
     * is already present in the configuration and you call
     *
     * <pre>addProperty("resource.loader", "classpath")</pre>
     *
     * Then you will end up with a List like the following:
     *
     * <pre>["file", "classpath"]</pre>
     *
     * @param key The key to add the property to.
     * @param value The value to add.
     */
    void addProperty(String key, Object value);

    /**
     * Set a property, this will replace any previously set values. Set values
     * is implicitly a call to clearProperty(key), addProperty(key, value).
     *
     * @param key The key of the property to change
     * @param value The new value
     */
    void setProperty(String key, Object value);

    /**
     * Remove a property from the configuration.
     *
     * @param key the key to remove along with corresponding value.
     */
    void clearProperty(String key);

    /**
     * Remove all properties from the configuration.
     */
    void clear();

    /**
     * Gets a property from the configuration.
     *
     * @param key property to retrieve
     * @return the value to which this configuration maps the specified key, or
     *         null if the configuration contains no mapping for this key.
     */
    Object getProperty(String key);

    /**
     * Get the list of the keys contained in the configuration that match the
     * specified prefix.
     *
     * @param prefix The prefix to test against.
     * @return An Iterator of keys that match the prefix.
     */
    Iterator getKeys(String prefix);

    /**
     * Get the list of the keys contained in the configuration.
     *
     * @return An Iterator.
     */
    Iterator getKeys();

    /**
     * Get a list of properties associated with the given configuration key.
     * This method expects the given key to have an arbitrary number of String
     * values, each of which is of the form <code>key=value</code>. These
     * strings are splitted at the equals sign, and the key parts will become
     * keys of the returned <code>Properties</code> object, the value parts
     * become values.
     *
     * @param key The configuration key.
     * @return The associated properties if key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a String/List.
     *
     * @throws IllegalArgumentException if one of the tokens is
     *         malformed (does not contain an equals sign).
     */
    Properties getProperties(String key);

    /**
     * Get a boolean associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated boolean.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     *         map to an existing object.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Boolean.
     */
    boolean getBoolean(String key);

    /**
     * Get a boolean associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated boolean.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Boolean.
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Get a {@link Boolean} associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated boolean if key is found and has valid
     *         format, default value otherwise.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Boolean.
     */
    Boolean getBoolean(String key, Boolean defaultValue) throws NoClassDefFoundError;

    /**
     * Get a byte associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated byte.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     *         map to an existing object.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Byte.
     */
    byte getByte(String key);

    /**
     * Get a byte associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated byte.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Byte.
     */
    byte getByte(String key, byte defaultValue);

    /**
     * Get a {@link Byte} associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated byte if key is found and has valid format, default
     *         value otherwise.
     *
     * @throws ConversionException is thrown if the key maps to an object that
     *         is not a Byte.
     */
    Byte getByte(String key, Byte defaultValue);

    /**
     * Get a double associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated double.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     *         map to an existing object.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Double.
     */
    double getDouble(String key);

    /**
     * Get a double associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated double.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Double.
     */
    double getDouble(String key, double defaultValue);

    /**
     * Get a {@link Double} associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated double if key is found and has valid
     *         format, default value otherwise.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Double.
     */
    Double getDouble(String key, Double defaultValue);

    /**
     * Get a float associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated float.
     * @throws NoSuchElementException is thrown if the key doesn't
     *         map to an existing object.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Float.
     */
    float getFloat(String key);

    /**
     * Get a float associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated float.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Float.
     */
    float getFloat(String key, float defaultValue);

    /**
     * Get a {@link Float} associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated float if key is found and has valid
     *         format, default value otherwise.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Float.
     */
    Float getFloat(String key, Float defaultValue);

    /**
     * Get a int associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated int.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     *         map to an existing object.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Integer.
     */
    int getInt(String key);

    /**
     * Get a int associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated int.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Integer.
     */
    int getInt(String key, int defaultValue);

    /**
     * Get an {@link Integer} associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated int if key is found and has valid format, default
     *         value otherwise.
     *
     * @throws ConversionException is thrown if the key maps to an object that
     *         is not a Integer.
     */
    Integer getInteger(String key, Integer defaultValue);

    /**
     * Get a long associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated long.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     *         map to an existing object.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Long.
     */
    long getLong(String key);

    /**
     * Get a long associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated long.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Long.
     */
    long getLong(String key, long defaultValue);

    /**
     * Get a {@link Long} associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated long if key is found and has valid
     * format, default value otherwise.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Long.
     */
    Long getLong(String key, Long defaultValue);

    /**
     * Get a short associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated short.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     *         map to an existing object.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Short.
     */
    short getShort(String key);

    /**
     * Get a short associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated short.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Short.
     */
    short getShort(String key, short defaultValue);

    /**
     * Get a {@link Short} associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated short if key is found and has valid
     *         format, default value otherwise.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a Short.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     *         map to an existing object.
     */
    Short getShort(String key, Short defaultValue);

    /**
     * Get a {@link BigDecimal} associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated BigDecimal if key is found and has valid format
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     *         map to an existing object.
     */
    BigDecimal getBigDecimal(String key);

    /**
     * Get a {@link BigDecimal} associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     *
     * @return The associated BigDecimal if key is found and has valid
     *         format, default value otherwise.
     */
    BigDecimal getBigDecimal(String key, BigDecimal defaultValue);

    /**
     * Get a {@link BigInteger} associated with the given configuration key.
     *
     * @param key The configuration key.
     *
     * @return The associated BigInteger if key is found and has valid format
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     *         map to an existing object.
     */
    BigInteger getBigInteger(String key);

    /**
     * Get a {@link BigInteger} associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key          The configuration key.
     * @param defaultValue The default value.
     *
     * @return The associated BigInteger if key is found and has valid
     *         format, default value otherwise.
     */
    BigInteger getBigInteger(String key, BigInteger defaultValue);

    /**
     * Get a string associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated string.
     *
     * @throws ConversionException is thrown if the key maps to an object that
     *         is not a String.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     *         map to an existing object.
     */
    String getString(String key);

    /**
     * Get a string associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated string if key is found and has valid
     *         format, default value otherwise.
     *
     * @throws ConversionException is thrown if the key maps to an object that
     *         is not a String.
     */
    String getString(String key, String defaultValue);

    /**
     * Get an array of strings associated with the given configuration key.
     * If the key doesn't map to an existing object an empty array is returned
     *
     * @param key The configuration key.
     * @return The associated string array if key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a String/List of Strings.
     */
    String[] getStringArray(String key);

    /**
     * Get a List of strings associated with the given configuration key.
     * If the key doesn't map to an existing object an empty List is returned.
     *
     * @param key The configuration key.
     * @return The associated List.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a List.
     */
    List getList(String key);

    /**
     * Get a List of strings associated with the given configuration key.
     * If the key doesn't map to an existing object, the default value
     * is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated List of strings.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a List.
     */
    List getList(String key, List defaultValue);
}
