/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.lang.BooleanUtils;

/**
 * Abstract configuration class. Provide basic functionality but does not store
 * any data. If you want to write your own Configuration class then you should
 * implement only abstract methods from this class.
 *
 * @author <a href="mailto:ksh@scand.com">Konstantin Shaposhnikov </a>
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger </a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen </a>
 * @version $Id: AbstractConfiguration.java,v 1.29 2004/12/02 22:05:52 ebourg
 * Exp $
 */
public abstract class AbstractConfiguration implements Configuration
{
    /** start token */
    protected static final String START_TOKEN = "${";

    /** end token */
    protected static final String END_TOKEN = "}";

    /** The default value for listDelimiter */
    private static char defaultListDelimiter = ',';

    /** Delimiter used to convert single values to lists */
    private char listDelimiter = defaultListDelimiter;

    /**
     * When set to true the given configuration delimiter will not be used
     * while parsing for this configuration.
     */
    private boolean delimiterParsingDisabled = false;

    /**
     * Whether the configuration should throw NoSuchElementExceptions or simply
     * return null when a property does not exist. Defaults to return null.
     */
    private boolean throwExceptionOnMissing;

    /**
     * For configurations extending AbstractConfiguration, allow them to change
     * the listDelimiter from the default comma (","). This value will be used
     * only when creating new configurations. Those already created will not be
     * affected by this change
     *
     * @param delimiter The new listDelimiter
     */
    public static void setDefaultListDelimiter(char delimiter)
    {
        AbstractConfiguration.defaultListDelimiter = delimiter;
    }

    /**
     * @deprecated Use AbstractConfiguration.setDefaultListDelimiter(char)
     * instead
     * @param delimiter
     */
    public static void setDelimiter(char delimiter)
    {
        setDefaultListDelimiter(delimiter);
    }

    /**
     * Retrieve the current delimiter. By default this is a comma (",").
     *
     * @return The delimiter in use
     */
    public static char getDefaultListDelimiter()
    {
        return AbstractConfiguration.defaultListDelimiter;
    }

    /**
     * @deprecated Use AbstractConfiguration.getDefaultListDelimiter() instead
     */
    public static char getDelimiter()
    {
        return getDefaultListDelimiter();
    }

    /**
     * Change the list delimiter for this configuration.
     *
     * Note: this change will only be effective for new parsings. If you
     * want it to take effect for all loaded properties use the no arg constructor
     * and call this method before setting the source.
     *
     * @param listDelimiter The new listDelimiter
     */
    public void setListDelimiter(char listDelimiter)
    {
        this.listDelimiter = listDelimiter;
    }

    /**
     * Retrieve the delimiter for this configuration. The default
     * is the value of defaultListDelimiter.
     *
     * @return The listDelimiter in use
     */
    public char getListDelimiter()
    {
        return listDelimiter;
    }

    /**
     * Determine if this configuration is using delimiters when parsing
     * property values to convert them to lists of values. Defaults to false
     * @return true if delimiters are not being used
     */
    public boolean isDelimiterParsingDisabled()
    {
        return delimiterParsingDisabled;
    }

    /**
     * Set whether this configuration should use delimiters when parsing
     * property values to convert them to lists of values. By default delimiter
     * parsing is enabled
     *
     * Note: this change will only be effective for new parsings. If you
     * want it to take effect for all loaded properties use the no arg constructor
     * and call this method before setting source.
     * @param delimiterParsingDisabled
     */
    public void setDelimiterParsingDisabled(boolean delimiterParsingDisabled)
    {
        this.delimiterParsingDisabled = delimiterParsingDisabled;
    }

    /**
     * Allows to set the <code>throwExceptionOnMissing</code> flag. This
     * flag controls the behavior of property getter methods that return
     * objects if the requested property is missing. If the flag is set to
     * <b>false</b> (which is the default value), these methods will return
     * <b>null</b>. If set to <b>true</b>, they will throw a
     * <code>NoSuchElementException</code> exception. Note that getter methods
     * for primitive data types are not affected by this flag.
     *
     * @param throwExceptionOnMissing The new value for the property
     */
    public void setThrowExceptionOnMissing(boolean throwExceptionOnMissing)
    {
        this.throwExceptionOnMissing = throwExceptionOnMissing;
    }

    /**
     * Returns true if missing values throw Exceptions.
     *
     * @return true if missing values throw Exceptions
     */
    public boolean isThrowExceptionOnMissing()
    {
        return throwExceptionOnMissing;
    }

    /**
     * {@inheritDoc}
     */
    public void addProperty(String key, Object value)
    {
        if (!isDelimiterParsingDisabled())
        {
            Iterator it = PropertyConverter.toIterator(value, getListDelimiter());
            while (it.hasNext())
            {
                addPropertyDirect(key, it.next());
            }
        }
        else
        {
            addPropertyDirect(key, value);
        }
    }

    /**
     * Adds a key/value pair to the Configuration. Override this method to
     * provide write acces to underlying Configuration store.
     *
     * @param key key to use for mapping
     * @param value object to store
     */
    protected abstract void addPropertyDirect(String key, Object value);

    /**
     * interpolate key names to handle ${key} stuff
     *
     * @param base string to interpolate
     *
     * @return returns the key name with the ${key} substituted
     */
    protected String interpolate(String base)
    {
        return interpolateHelper(base, null);
    }

    /**
     * Returns the interpolated value. Non String values are returned without change.
     *
     * @param value the value to interpolate
     *
     * @return returns the value with variables substituted
     */
    protected Object interpolate(Object value)
    {
        if (value instanceof String)
        {
            return interpolate((String) value);
        }
        else
        {
            return value;
        }
    }

    /**
     * Recursive handler for multple levels of interpolation.
     *
     * When called the first time, priorVariables should be null.
     *
     * @param base string with the ${key} variables
     * @param priorVariables serves two purposes: to allow checking for loops,
     * and creating a meaningful exception message should a loop occur. It's
     * 0'th element will be set to the value of base from the first call. All
     * subsequent interpolated variables are added afterward.
     *
     * @return the string with the interpolation taken care of
     */
    protected String interpolateHelper(String base, List priorVariables)
    {
        if (base == null)
        {
            return null;
        }

        // on the first call initialize priorVariables
        // and add base as the first element
        if (priorVariables == null)
        {
            priorVariables = new ArrayList();
            priorVariables.add(base);
        }

        int begin = -1;
        int end = -1;
        int prec = 0 - END_TOKEN.length();
        StringBuffer result = new StringBuffer();

        // FIXME: we should probably allow the escaping of the start token
        while (((begin = base.indexOf(START_TOKEN, prec + END_TOKEN.length())) > -1)
                && ((end = base.indexOf(END_TOKEN, begin)) > -1))
        {
            result.append(base.substring(prec + END_TOKEN.length(), begin));
            String variable = base.substring(begin + START_TOKEN.length(), end);

            // if we've got a loop, create a useful exception message and throw
            if (priorVariables.contains(variable))
            {
                String initialBase = priorVariables.remove(0).toString();
                priorVariables.add(variable);
                StringBuffer priorVariableSb = new StringBuffer();

                // create a nice trace of interpolated variables like so:
                // var1->var2->var3
                for (Iterator it = priorVariables.iterator(); it.hasNext();)
                {
                    priorVariableSb.append(it.next());
                    if (it.hasNext())
                    {
                        priorVariableSb.append("->");
                    }
                }

                throw new IllegalStateException("infinite loop in property interpolation of " + initialBase + ": "
                        + priorVariableSb.toString());
            }
            // otherwise, add this variable to the interpolation list.
            else
            {
                priorVariables.add(variable);
            }

            Object value = resolveContainerStore(variable);
            if (value != null)
            {
                result.append(interpolateHelper(value.toString(), priorVariables));

                // pop the interpolated variable off the stack
                // this maintains priorVariables correctness for
                // properties with multiple interpolations, e.g.
                // prop.name=${some.other.prop1}/blahblah/${some.other.prop2}
                priorVariables.remove(priorVariables.size() - 1);
            }
            else
            {
                //variable not defined - so put it back in the value
                result.append(START_TOKEN).append(variable).append(END_TOKEN);
            }

            prec = end;
        }
        result.append(base.substring(prec + END_TOKEN.length(), base.length()));
        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    public Configuration subset(String prefix)
    {
        return new SubsetConfiguration(this, prefix, ".");
    }

    /**
     * {@inheritDoc}
     */
    public abstract boolean isEmpty();

    /**
     * {@inheritDoc}
     */
    public abstract boolean containsKey(String key);

    /**
     * {@inheritDoc}
     */
    public void setProperty(String key, Object value)
    {
        clearProperty(key);
        addProperty(key, value);
    }

    /**
     * {@inheritDoc}
     */
    public abstract void clearProperty(String key);

    /**
     * {@inheritDoc}
     */
    public void clear()
    {
        Iterator it = getKeys();
        while (it.hasNext())
        {
            String key = (String) it.next();
            it.remove();

            if (containsKey(key))
            {
                // workaround for Iterators that do not remove the property on calling remove()
                clearProperty(key);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public abstract Iterator getKeys();

    /**
     * {@inheritDoc}
     */
    public Iterator getKeys(final String prefix)
    {
        return new FilterIterator(getKeys(), new Predicate()
        {
            public boolean evaluate(Object obj)
            {
                String key = (String) obj;
                return key.startsWith(prefix + ".") || key.equals(prefix);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public Properties getProperties(String key)
    {
        return getProperties(key, null);
    }

    /**
     * Get a list of properties associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaults Any default values for the returned
     * <code>Properties</code> object. Ignored if <code>null</code>.
     *
     * @return The associated properties if key is found.
     *
     * @throws ConversionException is thrown if the key maps to an object that
     * is not a String/List of Strings.
     *
     * @throws IllegalArgumentException if one of the tokens is malformed (does
     * not contain an equals sign).
     */
    public Properties getProperties(String key, Properties defaults)
    {
        /*
         * Grab an array of the tokens for this key.
         */
        String[] tokens = getStringArray(key);

        /*
         * Each token is of the form 'key=value'.
         */
        Properties props = defaults == null ? new Properties() : new Properties(defaults);
        for (int i = 0; i < tokens.length; i++)
        {
            String token = tokens[i];
            int equalSign = token.indexOf('=');
            if (equalSign > 0)
            {
                String pkey = token.substring(0, equalSign).trim();
                String pvalue = token.substring(equalSign + 1).trim();
                props.put(pkey, pvalue);
            }
            else if (tokens.length == 1 && "".equals(token))
            {
                // Semantically equivalent to an empty Properties
                // object.
                break;
            }
            else
            {
                throw new IllegalArgumentException('\'' + token + "' does not contain an equals sign");
            }
        }
        return props;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getBoolean(String key)
    {
        Boolean b = getBoolean(key, null);
        if (b != null)
        {
            return b.booleanValue();
        }
        else
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean getBoolean(String key, boolean defaultValue)
    {
        return getBoolean(key, BooleanUtils.toBooleanObject(defaultValue)).booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    public Boolean getBoolean(String key, Boolean defaultValue)
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
                return PropertyConverter.toBoolean(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Boolean object", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public byte getByte(String key)
    {
        Byte b = getByte(key, null);
        if (b != null)
        {
            return b.byteValue();
        }
        else
        {
            throw new NoSuchElementException('\'' + key + " doesn't map to an existing object");
        }
    }

    /**
     * {@inheritDoc}
     */
    public byte getByte(String key, byte defaultValue)
    {
        return getByte(key, new Byte(defaultValue)).byteValue();
    }

    /**
     * {@inheritDoc}
     */
    public Byte getByte(String key, Byte defaultValue)
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
                return PropertyConverter.toByte(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Byte object", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public double getDouble(String key)
    {
        Double d = getDouble(key, null);
        if (d != null)
        {
            return d.doubleValue();
        }
        else
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * {@inheritDoc}
     */
    public double getDouble(String key, double defaultValue)
    {
        return getDouble(key, new Double(defaultValue)).doubleValue();
    }

    /**
     * {@inheritDoc}
     */
    public Double getDouble(String key, Double defaultValue)
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
                return PropertyConverter.toDouble(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Double object", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public float getFloat(String key)
    {
        Float f = getFloat(key, null);
        if (f != null)
        {
            return f.floatValue();
        }
        else
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * {@inheritDoc}
     */
    public float getFloat(String key, float defaultValue)
    {
        return getFloat(key, new Float(defaultValue)).floatValue();
    }

    /**
     * {@inheritDoc}
     */
    public Float getFloat(String key, Float defaultValue)
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
                return PropertyConverter.toFloat(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Float object", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getInt(String key)
    {
        Integer i = getInteger(key, null);
        if (i != null)
        {
            return i.intValue();
        }
        else
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getInt(String key, int defaultValue)
    {
        Integer i = getInteger(key, null);

        if (i == null)
        {
            return defaultValue;
        }

        return i.intValue();
    }

    /**
     * {@inheritDoc}
     */
    public Integer getInteger(String key, Integer defaultValue)
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
                return PropertyConverter.toInteger(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to an Integer object", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public long getLong(String key)
    {
        Long l = getLong(key, null);
        if (l != null)
        {
            return l.longValue();
        }
        else
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * {@inheritDoc}
     */
    public long getLong(String key, long defaultValue)
    {
        return getLong(key, new Long(defaultValue)).longValue();
    }

    /**
     * {@inheritDoc}
     */
    public Long getLong(String key, Long defaultValue)
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
                return PropertyConverter.toLong(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Long object", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public short getShort(String key)
    {
        Short s = getShort(key, null);
        if (s != null)
        {
            return s.shortValue();
        }
        else
        {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * {@inheritDoc}
     */
    public short getShort(String key, short defaultValue)
    {
        return getShort(key, new Short(defaultValue)).shortValue();
    }

    /**
     * {@inheritDoc}
     */
    public Short getShort(String key, Short defaultValue)
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
                return PropertyConverter.toShort(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Short object", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(String key)
    {
        BigDecimal number = getBigDecimal(key, null);
        if (number != null)
        {
            return number;
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
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue)
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
                return PropertyConverter.toBigDecimal(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a BigDecimal object", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public BigInteger getBigInteger(String key)
    {
        BigInteger number = getBigInteger(key, null);
        if (number != null)
        {
            return number;
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
     * {@inheritDoc}
     */
    public BigInteger getBigInteger(String key, BigInteger defaultValue)
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
                return PropertyConverter.toBigInteger(interpolate(value));
            }
            catch (ConversionException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a BigDecimal object", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getString(String key)
    {
        String s = getString(key, null);
        if (s != null)
        {
            return s;
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
     * {@inheritDoc}
     */
    public String getString(String key, String defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value instanceof String)
        {
            return interpolate((String) value);
        }
        else if (value == null)
        {
            return interpolate(defaultValue);
        }
        else
        {
            throw new ConversionException('\'' + key + "' doesn't map to a String object");
        }
    }

    /**
     * {@inheritDoc}
     */
    public String[] getStringArray(String key)
    {
        Object value = getProperty(key);

        String[] array;

        if (value instanceof String)
        {
            array = new String[1];

            array[0] = interpolate((String) value);
        }
        else if (value instanceof List)
        {
            List list = (List) value;
            array = new String[list.size()];

            for (int i = 0; i < array.length; i++)
            {
                array[i] = interpolate((String) list.get(i));
            }
        }
        else if (value == null)
        {
            array = new String[0];
        }
        else
        {
            throw new ConversionException('\'' + key + "' doesn't map to a String/List object");
        }
        return array;
    }

    /**
     * {@inheritDoc}
     */
    public List getList(String key)
    {
        return getList(key, new ArrayList());
    }

    /**
     * {@inheritDoc}
     */
    public List getList(String key, List defaultValue)
    {
        Object value = getProperty(key);
        List list;

        if (value instanceof String)
        {
            list = new ArrayList(1);
            list.add(interpolate((String) value));
        }
        else if (value instanceof List)
        {
            list = new ArrayList();
            List l = (List) value;

            // add the interpolated elements in the new list
            Iterator it = l.iterator();
            while (it.hasNext())
            {
                list.add(interpolate(it.next()));
            }

        }
        else if (value == null)
        {
            list = defaultValue;
        }
        else
        {
            throw new ConversionException('\'' + key + "' doesn't map to a List object: " + value + ", a "
                    + value.getClass().getName());
        }
        return list;
    }

    /**
     * Returns an object from the store described by the key. If the value is a
     * List object, replace it with the first object in the list.
     *
     * @param key The property key.
     *
     * @return value Value, transparently resolving a possible List dependency.
     */
    protected Object resolveContainerStore(String key)
    {
        Object value = getProperty(key);
        if (value != null)
        {
            if (value instanceof List)
            {
                List list = (List) value;
                value = list.isEmpty() ? null : list.get(0);
            }
            else if (value instanceof Object[])
            {
                Object[] array = (Object[]) value;
                value = array.length == 0 ? null : array[0];
            }
            else if (value instanceof boolean[])
            {
                boolean[] array = (boolean[]) value;
                value = array.length == 0 ? null : new Boolean(array[0]);
            }
            else if (value instanceof byte[])
            {
                byte[] array = (byte[]) value;
                value = array.length == 0 ? null : new Byte(array[0]);
            }
            else if (value instanceof short[])
            {
                short[] array = (short[]) value;
                value = array.length == 0 ? null : new Short(array[0]);
            }
            else if (value instanceof int[])
            {
                int[] array = (int[]) value;
                value = array.length == 0 ? null : new Integer(array[0]);
            }
            else if (value instanceof long[])
            {
                long[] array = (long[]) value;
                value = array.length == 0 ? null : new Long(array[0]);
            }
            else if (value instanceof float[])
            {
                float[] array = (float[]) value;
                value = array.length == 0 ? null : new Float(array[0]);
            }
            else if (value instanceof double[])
            {
                double[] array = (double[]) value;
                value = array.length == 0 ? null : new Double(array[0]);
            }
        }

        return value;
    }

}
