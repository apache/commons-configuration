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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.commons.configuration.ex.ConversionException;

/**
 * <p>The main interface for accessing configuration data in a read-only fashion.</p>
 * <p>
 * The major part of the methods defined in this interface deals with accessing
 * properties of various data types. There is a generic {@code getProperty()}
 * method, which returns the value of the queried property in its raw data
 * type. Other getter methods try to convert this raw data type into a specific
 * data type. If this fails, a {@code ConversionException} will be thrown.</p>
 * <p>For most of the property getter methods an overloaded version exists that
 * allows to specify a default value, which will be returned if the queried
 * property cannot be found in the configuration. The behavior of the methods
 * that do not take a default value in case of a missing property is not defined
 * by this interface and depends on a concrete implementation. E.g. the
 * {@link AbstractConfiguration} class, which is the base class
 * of most configuration implementations provided by this package, per default
 * returns <b>null</b> if a property is not found, but provides the
 * {@link AbstractConfiguration#setThrowExceptionOnMissing(boolean)
 * setThrowExceptionOnMissing()}
 * method, with which it can be configured to throw a {@code NoSuchElementException}
 * exception in that case. (Note that getter methods for primitive types in
 * {@code AbstractConfiguration} always throw an exception for missing
 * properties because there is no way of overloading the return value.)</p>
 *
 * @version $Id$
 * @since 2.0
 */
public interface ImmutableConfiguration
{
    /**
     * Check if the configuration is empty.
     *
     * @return {@code true} if the configuration contains no property,
     *         {@code false} otherwise.
     */
    boolean isEmpty();

    /**
     * Check if the configuration contains the specified key.
     *
     * @param key the key whose presence in this configuration is to be tested
     *
     * @return {@code true} if the configuration contains a value for this
     *         key, {@code false} otherwise
     */
    boolean containsKey(String key);

    /**
     * Gets a property from the configuration. This is the most basic get
     * method for retrieving values of properties. In a typical implementation
     * of the {@code Configuration} interface the other get methods (that
     * return specific data types) will internally make use of this method. On
     * this level variable substitution is not yet performed. The returned
     * object is an internal representation of the property value for the passed
     * in key. It is owned by the {@code Configuration} object. So a caller
     * should not modify this object. It cannot be guaranteed that this object
     * will stay constant over time (i.e. further update operations on the
     * configuration may change its internal state).
     *
     * @param key property to retrieve
     * @return the value to which this configuration maps the specified key, or
     *         null if the configuration contains no mapping for this key.
     */
    Object getProperty(String key);

    /**
     * Get the list of the keys contained in the configuration that match the
     * specified prefix. For instance, if the configuration contains the
     * following keys:<br>
     * {@code db.user, db.pwd, db.url, window.xpos, window.ypos},<br>
     * an invocation of {@code getKeys("db");}<br>
     * will return the keys below:<br>
     * {@code db.user, db.pwd, db.url}.<br>
     * Note that the prefix itself is included in the result set if there is a
     * matching key. The exact behavior - how the prefix is actually
     * interpreted - depends on a concrete implementation.
     *
     * @param prefix The prefix to test against.
     * @return An Iterator of keys that match the prefix.
     * @see #getKeys()
     */
    Iterator<String> getKeys(String prefix);

    /**
     * Get the list of the keys contained in the configuration. The returned
     * iterator can be used to obtain all defined keys. Note that the exact
     * behavior of the iterator's {@code remove()} method is specific to
     * a concrete implementation. It <em>may</em> remove the corresponding
     * property from the configuration, but this is not guaranteed. In any case
     * it is no replacement for calling
     * {@link #clearProperty(String)} for this property. So it is
     * highly recommended to avoid using the iterator's {@code remove()}
     * method.
     *
     * @return An Iterator.
     */
    Iterator<String> getKeys();

    /**
     * Get a list of properties associated with the given configuration key.
     * This method expects the given key to have an arbitrary number of String
     * values, each of which is of the form {@code key=value}. These
     * strings are split at the equals sign, and the key parts will become
     * keys of the returned {@code Properties} object, the value parts
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
    Boolean getBoolean(String key, Boolean defaultValue);

    /**
     * Get a byte associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated byte.
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
     */
    Short getShort(String key, Short defaultValue);

    /**
     * Get a {@link BigDecimal} associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated BigDecimal if key is found and has valid format
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
     * Get a List of the values associated with the given configuration key.
     * This method is different from the generic {@code getList()} method in
     * that it does not recursively obtain all values stored for the specified
     * property key. Rather, only the first level of the hierarchy is processed.
     * So the resulting list may contain complex objects like arrays or
     * collections - depending on the storage structure used by a concrete
     * subclass. If the key doesn't map to an existing object, an empty List is
     * returned.
     *
     * @param key The configuration key.
     * @return The associated List.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a List.
     */
    List<Object> getList(String key);

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
     * @see #getList(Class, String, List)
     */
    List<Object> getList(String key, List<Object> defaultValue);

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
     * @since 2.0
     */
    <T> T get(Class<T> cls, String key);

    /**
     * Get an object of the specified type associated with the given
     * configuration key using a default value. If the key doesn't map to an
     * existing object, the default value is returned.
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
     * @since 2.0
     */
    <T> T get(Class<T> cls, String key, T defaultValue);

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
     * @since 2.0
     */
    Object getArray(Class<?> cls, String key);

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
     * @since 2.0
     */
    Object getArray(Class<?> cls, String key, Object defaultValue);

    /**
     * Get a list of typed objects associated with the given configuration key
     * returning an empty list if the key doesn't map to an existing object.
     *
     * @param <T> the type expected for the elements of the list
     * @param cls the class expected for the elements of the list
     * @param key The configuration key.
     * @return The associated list if the key is found.
     *
     * @throws ConversionException is thrown if the key maps to an object that
     *     is not compatible with a list of the specified class.
     *
     * @since 2.0
     */
    <T> List<T> getList(Class<T> cls, String key);

    /**
     * Get a list of typed objects associated with the given configuration key
     * returning the specified default value if the key doesn't map to an
     * existing object. This method recursively retrieves all values stored
     * for the passed in key, i.e. if one of these values is again a complex
     * object like an array or a collection (which may be the case for some
     * concrete subclasses), all values are extracted and added to the
     * resulting list - performing a type conversion if necessary.
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
     * @since 2.0
     */
    <T> List<T> getList(Class<T> cls, String key, List<T> defaultValue);

    /**
     * Get a collection of typed objects associated with the given configuration
     * key. This method works like
     * {@link #getCollection(Class, String, Collection, Collection)} passing in
     * <b>null</b> as default value.
     *
     * @param <T> the element type of the result list
     * @param cls the the element class of the result list
     * @param key the configuration key
     * @param target the target collection (may be <b>null</b>)
     * @return the collection to which data was added
     * @throws ConversionException if the conversion is not possible
     * @since 2.0
     */
    <T> Collection<T> getCollection(Class<T> cls, String key,
            Collection<T> target);

    /**
     * Get a collection of typed objects associated with the given configuration
     * key using the values in the specified default collection if the key does
     * not map to an existing object. This method is similar to
     * {@code getList()}, however, it allows specifying a target collection.
     * Results are added to this collection. This is useful if the data
     * retrieved should be added to a specific kind of collection, e.g. a set to
     * remove duplicates. The return value is as follows:
     * <ul>
     * <li>If the key does not map to an existing object and the default value
     * is <b>null</b>, the method returns <b>null</b>.</li>
     * <li>If the target collection is not <b>null</b> and data has been added
     * (either from the resolved property value or from the default collection),
     * the target collection is returned.</li>
     * <li>If the target collection is <b>null</b> and data has been added
     * (either from the resolved property value or from the default collection),
     * return value is the target collection created by this method.</li>
     * </ul>
     *
     * @param <T> the element type of the result list
     * @param cls the the element class of the result list
     * @param key the configuration key
     * @param target the target collection (may be <b>null</b>)
     * @param defaultValue the default value (may be <b>null</b>)
     * @return the collection to which data was added
     * @throws ConversionException if the conversion is not possible
     * @since 2.0
     */
    <T> Collection<T> getCollection(Class<T> cls, String key,
            Collection<T> target, Collection<T> defaultValue);

    /**
     * Return a decorator immutable Configuration containing every key from the current
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
     * the immutable Configuration returned by {@code subset("prefix")} will contain
     * the properties:
     *
     * <pre>
     *    number = 1
     *    string = Apache
     *    = Jakarta</pre>
     *
     * (The key for the value "Jakarta" is an empty string)
     *
     * @param prefix The prefix used to select the properties.
     * @return a subset immutable configuration
     */
    ImmutableConfiguration immutableSubset(String prefix);
}
