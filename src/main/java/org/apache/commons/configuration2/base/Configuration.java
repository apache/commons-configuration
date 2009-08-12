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
package org.apache.commons.configuration2.base;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.ConversionException;
import org.apache.commons.configuration2.expr.ExpressionEngine;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.lang.text.StrSubstitutor;

/**
 * <p>
 * The main Configuration interface.
 * </p>
 * <p>
 * This interface allows accessing and manipulating a configuration object. The
 * major part of the methods defined in this interface deals with accessing
 * properties of various data types. There is a generic {@code getProperty()}
 * method, which returns the value of the queried property in its raw data type.
 * Other getter methods try to convert this raw data type into a specific data
 * type. If this fails, a {@link ConversionException} will be thrown.
 * </p>
 * <p>
 * For most of the property getter methods an overloaded version exists that
 * allows to specify a default value, which will be returned if the queried
 * property cannot be found in the configuration. The behavior of the methods
 * that do not take a default value in case of a missing property can be defined
 * using the {@link #setThrowExceptionOnMissing(boolean)} method: invoking this
 * method with the parameter set to <b>true</b> causes get methods to throw an
 * exception if a property cannot be resolved. Otherwise, they simply return
 * <b>null</b> (this is the default behavior). Note that getter methods for
 * primitive types always throw an exception for missing properties because
 * there is no way of overloading the return value.
 * </p>
 * <p>
 * With the {@link #addProperty(String, Object)} and
 * {@link #setProperty(String, Object)} methods new properties can be added to a
 * configuration or the values of properties can be changed. With
 * {@link #clearProperty(String)} a property can be removed. Other methods allow
 * iterating over the contained properties or the creation of a subset
 * configuration.
 * </p>
 * <p>
 * Because many sources for configuration properties are hierarchical in nature
 * this interface also provides special support for such sources. With the
 * {@link #setExpressionEngine(ExpressionEngine)} method an
 * {@link ExpressionEngine} can be set which is used for interpreting the
 * property keys and mapping them to hierarchical node structures. The type of
 * the nodes a configuration deals with is reflected by the generics parameter
 * of this interface.
 * </p>
 *
 * @param <T> the type of configuration nodes used by this configuration
 * @author Commons Configuration team
 * @version $Id$
 */
public interface Configuration<T>
{
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
     * @return <code>true</code> if the configuration contains a value for this
     *         key, <code>false</code> otherwise
     */
    boolean containsKey(String key);

    /**
     * Add a property to the configuration. If it already exists then the value
     * stated here will be added to the configuration entry. For example, if the
     * property:
     *
     * <pre>
     * resource.loader = file
     * </pre>
     *
     * is already present in the configuration and you call
     *
     * <pre>
     * addProperty(&quot;resource.loader&quot;, &quot;classpath&quot;)
     * </pre>
     *
     * Then you will end up with a List like the following:
     *
     * <pre>
     * [&quot;file&quot;, &quot;classpath&quot;]
     * </pre>
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
     * Gets a property from the configuration. This is the most basic get method
     * for retrieving values of properties. In a typical implementation of the
     * <code>Configuration</code> interface the other get methods (that return
     * specific data types) will internally make use of this method. On this
     * level variable substitution is not yet performed. The returned object is
     * an internal representation of the property value for the passed in key.
     * It is owned by the <code>Configuration</code> object. So a caller should
     * not modify this object. It cannot be guaranteed that this object will
     * stay constant over time (i.e. further update operations on the
     * configuration may change its internal state).
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
     * @see #getKeys()
     */
    Iterator<String> getKeys(String prefix);

    /**
     * Get the list of the keys contained in the configuration. The returned
     * iterator can be used to obtain all defined keys. Note that the exact
     * behavior of the iterator's <code>remove()</code> method is specific to a
     * concrete implementation. It <em>may</em> remove the corresponding
     * property from the configuration, but this is not guaranteed. In any case
     * it is no replacement for calling
     * <code>{@link #clearProperty(String)}</code> for this property. So it is
     * highly recommended to avoid using the iterator's <code>remove()</code>
     * method.
     *
     * @return An Iterator.
     */
    Iterator<String> getKeys();

    /**
     * Get a boolean associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated boolean.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Boolean.
     */
    boolean getBoolean(String key);

    /**
     * Get a boolean associated with the given configuration key. If the key
     * doesn't map to an existing object, the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated boolean.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Boolean.
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Get a {@link Boolean} associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated boolean if key is found and has valid format,
     *         default value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Boolean.
     */
    Boolean getBoolean(String key, Boolean defaultValue);

    /**
     * Get a byte associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated byte.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Byte.
     */
    byte getByte(String key);

    /**
     * Get a byte associated with the given configuration key. If the key
     * doesn't map to an existing object, the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated byte.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Byte.
     */
    byte getByte(String key, byte defaultValue);

    /**
     * Get a {@link Byte} associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated byte if key is found and has valid format, default
     *         value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Byte.
     */
    Byte getByte(String key, Byte defaultValue);

    /**
     * Get a double associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated double.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Double.
     */
    double getDouble(String key);

    /**
     * Get a double associated with the given configuration key. If the key
     * doesn't map to an existing object, the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated double.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Double.
     */
    double getDouble(String key, double defaultValue);

    /**
     * Get a {@link Double} associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated double if key is found and has valid format,
     *         default value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Double.
     */
    Double getDouble(String key, Double defaultValue);

    /**
     * Get a float associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated float.
     * @throws ConversionException is thrown if the key maps to an object that
     *         is not a Float.
     */
    float getFloat(String key);

    /**
     * Get a float associated with the given configuration key. If the key
     * doesn't map to an existing object, the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated float.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Float.
     */
    float getFloat(String key, float defaultValue);

    /**
     * Get a {@link Float} associated with the given configuration key. If the
     * key doesn't map to an existing object, the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated float if key is found and has valid format,
     *         default value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Float.
     */
    Float getFloat(String key, Float defaultValue);

    /**
     * Get a int associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated int.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Integer.
     */
    int getInt(String key);

    /**
     * Get a int associated with the given configuration key. If the key doesn't
     * map to an existing object, the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated int.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Integer.
     */
    int getInt(String key, int defaultValue);

    /**
     * Get an {@link Integer} associated with the given configuration key. If
     * the key doesn't map to an existing object, the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated int if key is found and has valid format, default
     *         value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Integer.
     */
    Integer getInteger(String key, Integer defaultValue);

    /**
     * Get a long associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated long.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Long.
     */
    long getLong(String key);

    /**
     * Get a long associated with the given configuration key. If the key
     * doesn't map to an existing object, the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated long.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Long.
     */
    long getLong(String key, long defaultValue);

    /**
     * Get a {@link Long} associated with the given configuration key. If the
     * key doesn't map to an existing object, the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated long if key is found and has valid format, default
     *         value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Long.
     */
    Long getLong(String key, Long defaultValue);

    /**
     * Get a short associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated short.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Short.
     */
    short getShort(String key);

    /**
     * Get a short associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated short.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Short.
     */
    short getShort(String key, short defaultValue);

    /**
     * Get a {@link Short} associated with the given configuration key. If the
     * key doesn't map to an existing object, the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated short if key is found and has valid format,
     *         default value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a Short.
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
     * Get a {@link BigDecimal} associated with the given configuration key. If
     * the key doesn't map to an existing object, the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated BigDecimal if key is found and has valid format,
     *         default value otherwise.
     */
    BigDecimal getBigDecimal(String key, BigDecimal defaultValue);

    /**
     * Get a {@link BigInteger} associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated BigInteger if key is found and has valid format
     */
    BigInteger getBigInteger(String key);

    /**
     * Get a {@link BigInteger} associated with the given configuration key. If
     * the key doesn't map to an existing object, the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated BigInteger if key is found and has valid format,
     *         default value otherwise.
     */
    BigInteger getBigInteger(String key, BigInteger defaultValue);

    /**
     * Get a string associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated string.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a String.
     */
    String getString(String key);

    /**
     * Get a string associated with the given configuration key. If the key
     * doesn't map to an existing object, the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated string if key is found and has valid format,
     *         default value otherwise.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a String.
     */
    String getString(String key, String defaultValue);

    /**
     * Get an array of strings associated with the given configuration key. If
     * the key doesn't map to an existing object an empty array is returned
     *
     * @param key The configuration key.
     * @return The associated string array if key is found.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a String/List of Strings.
     */
    String[] getStringArray(String key);

    /**
     * Get a List of strings associated with the given configuration key. If the
     * key doesn't map to an existing object an empty List is returned.
     *
     * @param key The configuration key.
     * @param <E> the type of the elements in the list
     * @return The associated List.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a List.
     */
    <E> List<E> getList(String key);

    /**
     * Get a List of strings associated with the given configuration key. If the
     * key doesn't map to an existing object, the default value is returned.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @param <E> the type of the elements in the list
     * @return The associated List of strings.
     * @throws ConversionException is thrown if the key maps to an object that is
     *         not a List.
     */
    <E> List<E> getList(String key, List<E> defaultValue);

    /**
     * Change the list delimiter for this configuration. Note: this change will
     * only be effective for new parsings. If you want it to take effect for all
     * loaded properties use the no arg constructor and call this method before
     * setting the source.
     *
     * @param listDelimiter The new listDelimiter
     */
    void setListDelimiter(char listDelimiter);

    /**
     * Retrieve the delimiter for this configuration. The default is the value
     * of defaultListDelimiter.
     *
     * @return The listDelimiter in use
     */
    char getListDelimiter();

    /**
     * Determine if this configuration is using delimiters when parsing property
     * values to convert them to lists of values. Defaults to false
     *
     * @return true if delimiters are not being used
     */
    boolean isDelimiterParsingDisabled();

    /**
     * Set whether this configuration should use delimiters when parsing
     * property values to convert them to lists of values. By default delimiter
     * parsing is enabled Note: this change will only be effective for new
     * parsings. If you want it to take effect for all loaded properties use the
     * no arg constructor and call this method before setting source.
     *
     * @param delimiterParsingDisabled a flag whether delimiter parsing should
     *        be disabled
     */
    void setDelimiterParsingDisabled(boolean delimiterParsingDisabled);

    /**
     * Allows to set the <code>throwExceptionOnMissing</code> flag. This flag
     * controls the behavior of property getter methods that return objects if
     * the requested property is missing. If the flag is set to <b>false</b>
     * (which is the default value), these methods will return <b>null</b>. If
     * set to <b>true</b>, they will throw a <code>NoSuchElementException</code>
     * exception. Note that getter methods for primitive data types are not
     * affected by this flag.
     *
     * @param throwExceptionOnMissing The new value for the property
     */
    void setThrowExceptionOnMissing(boolean throwExceptionOnMissing);

    /**
     * Returns true if missing values throw Exceptions.
     *
     * @return true if missing values throw Exceptions
     */
    boolean isThrowExceptionOnMissing();

    /**
     * Returns the object that is responsible for variable interpolation.
     *
     * @return the object responsible for variable interpolation
     * @since 1.4
     */
    StrSubstitutor getSubstitutor();

    /**
     * Returns the <code>ConfigurationInterpolator</code> object that manages
     * the lookup objects for resolving variables. <em>Note:</em> If this object
     * is manipulated (e.g. new lookup objects added), synchronisation has to be
     * manually ensured. Because <code>ConfigurationInterpolator</code> is not
     * thread-safe concurrent access to properties of this configuration
     * instance (which causes the interpolator to be invoked) may cause race
     * conditions.
     *
     * @return the <code>ConfigurationInterpolator</code> associated with this
     *         configuration
     * @since 1.4
     */
    ConfigurationInterpolator getInterpolator();

    /**
     * Returns the expression engine used by this configuration. This method
     * will never return <b>null</b>; if no specific expression engine was set,
     * the default expression engine will be returned.
     *
     * @return the current expression engine
     */
    ExpressionEngine getExpressionEngine();

    /**
     * Sets the expression engine to be used by this configuration. All property
     * keys this configuration has to deal with will be interpreted by this
     * engine.
     *
     * @param expressionEngine the new expression engine; can be <b>null</b>,
     *        then the default expression engine will be used
     */
    void setExpressionEngine(ExpressionEngine expressionEngine);

    /**
     * <p>
     * Returns a hierarchical sub configuration object that wraps the
     * configuration node specified by the given key. This method provides an
     * easy means of accessing sub trees of a hierarchical configuration. In the
     * returned configuration the sub tree can directly be accessed, it becomes
     * the root node of this configuration. Because of this the passed in key
     * must select exactly one configuration node; otherwise an
     * <code>IllegalArgumentException</code> will be thrown.
     * </p>
     * <p>
     * The difference between this method and the
     * <code>{@link #subset(String)}</code> method is that <code>subset()</code>
     * supports arbitrary subsets of configuration nodes while
     * <code>configurationAt()</code> only returns a single sub tree. Please
     * refer to the documentation of the <code>{@link SubConfiguration}</code>
     * class to obtain further information about sub configurations and when
     * they should be used.
     * </p>
     * <p>
     * With the <code>supportUpdate</code> flag the behavior of the returned
     * <code>SubConfiguration</code> regarding updates of its parent
     * configuration can be determined. A sub configuration operates on the same
     * nodes as its parent, so changes at one configuration are normally
     * directly visible for the other configuration. There are however changes
     * of the parent configuration, which are not recognized by the sub
     * configuration per default. An example for this is a reload operation (for
     * file-based configurations): Here the complete node set of the parent
     * configuration is replaced, but the sub configuration still references the
     * old nodes. If such changes should be detected by the sub configuration,
     * the <code>supportUpdates</code> flag must be set to <b>true</b>. This
     * causes the sub configuration to reevaluate the key used for its creation
     * each time it is accessed. This guarantees that the sub configuration
     * always stays in sync with its key, even if the parent configuration's
     * data significantly changes. If such a change makes the key invalid -
     * because it now no longer points to exactly one node -, the sub
     * configuration is not reconstructed, but keeps its old data. It is then
     * quasi detached from its parent.
     * </p>
     *
     * @param key the key that selects the sub tree
     * @param supportUpdates a flag whether the returned sub configuration
     *        should be able to handle updates of its parent
     * @return a hierarchical configuration that contains this sub tree
     * @see SubConfiguration
     */
    Configuration<T> configurationAt(String key, boolean supportUpdates);

    /**
     * Returns a hierarchical sub configuration for the node specified by the
     * given key. This is a short form for <code>configurationAt(key,
     * <b>false</b>)</code>.
     *
     * @param key the key that selects the sub tree
     * @return a hierarchical configuration that contains this sub tree
     * @see SubConfiguration
     */
    Configuration<T> configurationAt(String key);

    /**
     * Returns a list of sub configurations for all configuration nodes selected
     * by the given key. This method will evaluate the passed in key (using the
     * current <code>ExpressionEngine</code>) and then create a
     * <code>{@link SubConfiguration}</code> for each returned node (like
     * <code>{@link #configurationAt(String)}</code> ). This is especially
     * useful when dealing with list-like structures. As an example consider the
     * configuration that contains data about database tables and their fields.
     * If you need access to all fields of a certain table, you can simply do
     *
     * <pre>
     * List&lt;SubConfiguration&lt;T&gt;&gt; fields = config.configurationsAt(&quot;tables.table(0).fields.field&quot;);
     * for(SubConfiguration sub : fields)
     * {
     *     // now the children and attributes of the field node can be
     *     // directly accessed
     *     String fieldName = sub.getString(&quot;name&quot;);
     *     String fieldType = sub.getString(&quot;type&quot;);
     *     ...
     * </pre>
     *
     * This method also supports a <code>supportUpdates</code> parameter for
     * making the sub configurations returned aware of structural changes in the
     * parent configuration. Refer to the documentation of
     * <code>{@link #configurationAt(String, boolean)}</code> for more details
     * about the effect of this flag.
     *
     * @param key the key for selecting the desired nodes
     * @param supportUpdates a flag whether the returned sub configurations
     *        should be able to handle updates of its parent
     * @return a list with hierarchical configuration objects; each
     *         configuration represents one of the nodes selected by the passed
     *         in key
     */
    List<Configuration<T>> configurationsAt(String key,
            boolean supportUpdates);

    /**
     * Returns a list of sub configurations for all configuration nodes selected
     * by the given key that are not aware of structural updates of their
     * parent. This is a short form for
     * <code>configurationsAt(key, <b>false</b>)</code>.
     *
     * @param key the key for selecting the desired nodes
     * @return a list with hierarchical configuration objects; each
     *         configuration represents one of the nodes selected by the passed
     *         in key
     */
    List<Configuration<T>> configurationsAt(String key);

    /**
     * Removes all values of the property with the given name and of keys that
     * start with this name. So if there is a property with the key
     * &quot;foo&quot; and a property with the key &quot;foo.bar&quot;, a call
     * of <code>clearTree("foo")</code> would remove both properties.
     *
     * @param key the key of the property to be removed
     */
    void clearTree(String key);

    /**
     * Returns the maximum defined index for the given key. This is useful if
     * there are multiple values for this key. They can then be addressed
     * separately by specifying indices from 0 to the return value of this
     * method.
     *
     * @param key the key to be checked
     * @return the maximum defined index for this key
     */
    int getMaxIndex(String key);

    /**
     * Returns the {@code HierarchicalConfigurationSource} used by this
     * configuration. This object contains the actual data. Clients may use this
     * reference to access configuration data directly in its raw form, but then
     * many of the convenience features offered by a {@code Configuration}
     * implementation are not available.
     *
     * @return the underlying {@code HierarchicalConfigurationSource}
     */
    HierarchicalConfigurationSource<T> getConfigurationSource();
}
