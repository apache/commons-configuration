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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.lang.BooleanUtils;

/**
 * Abstract configuration class. Provide basic functionality but does not
 * store any data. If you want to write your own Configuration class
 * then you should implement only abstract methods from this class.
 *
 * @author <a href="mailto:ksh@scand.com">Konstantin Shaposhnikov</a>
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: AbstractConfiguration.java,v 1.16 2004/06/24 14:01:03 ebourg Exp $
 */
public abstract class AbstractConfiguration implements Configuration
{
    /** start token */
    protected static final String START_TOKEN = "${";

    /** end token */
    protected static final String END_TOKEN = "}";

    /** The property delimiter used while parsing (a comma). */
    protected static final char DELIMITER = ',';

    /** how big the initial arraylist for splitting up name value pairs */
    private static final int INITIAL_LIST_SIZE = 2;

    /**
     * {@inheritDoc}
     */
    public void addProperty(String key, Object token)
    {
        if (token instanceof String)
        {
            Iterator it = split((String) token).iterator();
            while (it.hasNext())
            {
                addPropertyDirect(key, it.next());
            }
        }
        else if (token instanceof Collection)
        {
            Iterator it = ((Collection) token).iterator();
            while (it.hasNext())
            {
                addProperty(key, it.next());
            }
        }
        else
        {
            addPropertyDirect(key, token);
        }
    }

    /**
     * Read property. Should return <code>null</code> if the key doesn't
     * map to an existing object.
     *
     * @param key key to use for mapping
     *
     * @return object associated with the given configuration key.
     */
    protected abstract Object getPropertyDirect(String key);

    /**
     * Adds a key/value pair to the Configuration. Override this method to
     * provide write acces to underlying Configuration store.
     *
     * @param key key to use for mapping
     * @param obj object to store
     */
    protected abstract void addPropertyDirect(String key, Object obj);

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
     * Recursive handler for multple levels of interpolation.
     *
     * When called the first time, priorVariables should be null.
     *
     * @param base string with the ${key} variables
     * @param priorVariables serves two purposes: to allow checking for
     * loops, and creating a meaningful exception message should a loop
     * occur.  It's 0'th element will be set to the value of base from
     * the first call.  All subsequent interpolated variables are added
     * afterward.
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
        String variable = null;
        StringBuffer result = new StringBuffer();

        // FIXME: we should probably allow the escaping of the start token
        while (((begin = base.indexOf(START_TOKEN, prec + END_TOKEN.length()))
            > -1)
            && ((end = base.indexOf(END_TOKEN, begin)) > -1))
        {
            result.append(base.substring(prec + END_TOKEN.length(), begin));
            variable = base.substring(begin + START_TOKEN.length(), end);

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

                throw new IllegalStateException(
                    "infinite loop in property interpolation of "
                        + initialBase
                        + ": "
                        + priorVariableSb.toString());
            }
            // otherwise, add this variable to the interpolation list.
            else
            {
                priorVariables.add(variable);
            }

            //QUESTION: getProperty or getPropertyDirect
            Object value = getProperty(variable);
            if (value != null)
            {
                result.append(interpolateHelper(value.toString(),
                    priorVariables));

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
     * Returns a List of Strings built from the supplied String. Splits up CSV
     * lists. If no commas are in the String, simply returns a List with the
     * String as its first element.
     *
     * @param token The String to tokenize
     *
     * @return A List of Strings
     */
    protected List split(String token)
    {
        List list = new ArrayList(INITIAL_LIST_SIZE);

        if (token.indexOf(DELIMITER) > 0)
        {
            PropertiesTokenizer tokenizer = new PropertiesTokenizer(token);

            while (tokenizer.hasMoreTokens())
            {
                list.add(tokenizer.nextToken());
            }
        }
        else
        {
            list.add(token);
        }

        //
        // We keep the sequence of the keys here and
        // we also keep it in the Container. So the
        // Keys are added to the store in the sequence that
        // is given in the properties
        return list;
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
        addProperty(key, value); // QUESTION: or addPropertyDirect?
    }

    /**
     * {@inheritDoc}
     */
    public  abstract void clearProperty(String key);

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
                boolean matching = false;

                if (obj instanceof String)
                {
                    String key = (String) obj;
                    matching = key.startsWith(prefix + ".") || key.equals(prefix);
                }

                return matching;
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
     * <code>Properties</code> object.  Ignored if <code>null</code>.
     *
     * @return The associated properties if key is found.
     *
     * @throws ConversionException is thrown if the key maps to an
     *         object that is not a String/List of Strings.
     *
     * @throws IllegalArgumentException if one of the tokens is
     *         malformed (does not contain an equals sign).
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
                throw new IllegalArgumentException(
                    '\'' + token + "' does not contain an equals sign");
            }
        }
        return props;
    }

    /**
     * {@inheritDoc}
     */
    public Object getProperty(String key)
    {
        // first, try to get from the 'user value' store
        Object o = getPropertyDirect(key);


        //
        // We must never give a Container Object out. So if the
        // Return Value is a Container, we fix it up to be a
        // List
        //
        if (o instanceof Container)
        {
            o = ((Container) o).asList();
        }
        return o;
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
            throw new NoSuchElementException(
                '\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean getBoolean(String key, boolean defaultValue)
    {
        return getBoolean(key, new Boolean(defaultValue)).booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    public Boolean getBoolean(String key, Boolean defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        else if (value instanceof String)
        {
            Boolean b = BooleanUtils.toBooleanObject((String) value);
            if (b == null)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Boolean object");
            }
            return b;
        }
        else if (value == null)
        {
            return defaultValue;
        }
        else
        {
            throw new ConversionException(
                '\'' + key + "' doesn't map to a Boolean object");
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
            throw new NoSuchElementException(
                '\'' + key + " doesn't map to an existing object");
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

        if (value instanceof Byte)
        {
            return (Byte) value;
        }
        else if (value instanceof String)
        {
            try
            {
                Byte b = new Byte((String) value);
                return b;
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Byte object", e);
            }
        }
        else if (value == null)
        {
            return defaultValue;
        }
        else
        {
            throw new ConversionException('\'' + key + "' doesn't map to a Byte object");
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
            throw new NoSuchElementException(
                '\'' + key + "' doesn't map to an existing object");
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

        if (value instanceof Double)
        {
            return (Double) value;
        }
        else if (value instanceof String)
        {
            try
            {
                Double d = new Double((String) value);
                return d;
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Double object", e);
            }
        }
        else if (value == null)
        {
            return defaultValue;
        }
        else
        {
            throw new ConversionException('\'' + key + "' doesn't map to a Double object");
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
            throw new NoSuchElementException(
                '\'' + key + "' doesn't map to an existing object");
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

        if (value instanceof Float)
        {
            return (Float) value;
        }
        else if (value instanceof String)
        {
            try
            {
                Float f = new Float((String) value);
                return f;
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Float object", e);
            }
        }
        else if (value == null)
        {
           return defaultValue;
        }
        else
        {
            throw new ConversionException('\'' + key + "' doesn't map to a Float object");
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
            throw new NoSuchElementException(
                '\'' + key + "' doesn't map to an existing object");
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

        if (value instanceof Integer)
        {
            return (Integer) value;
        }
        else if (value instanceof String)
        {
            try
            {
                Integer i = new Integer((String) value);
                return i;
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Integer object", e);
            }
        }
        else if (value == null)
        {
            return defaultValue;
        }
        else
        {
            throw new ConversionException('\'' + key + "' doesn't map to a Integer object");
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
            throw new NoSuchElementException(
                '\'' + key + "' doesn't map to an existing object");
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

        if (value instanceof Long)
        {
            return (Long) value;
        }
        else if (value instanceof String)
        {
            try
            {
                Long l = new Long((String) value);
                return l;
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Long object", e);
            }
        }
        else if (value == null)
        {
            return defaultValue;
        }
        else
        {
            throw new ConversionException('\'' + key + "' doesn't map to a Long object");
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
            throw new NoSuchElementException(
                '\'' + key + "' doesn't map to an existing object");
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

        if (value instanceof Short)
        {
            return (Short) value;
        }
        else if (value instanceof String)
        {
            try
            {
                Short s = new Short((String) value);
                return s;
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a Short object", e);
            }
        }
        else if (value == null)
        {
            return defaultValue;
        }
        else
        {
            throw new ConversionException('\'' + key + "' doesn't map to a Short object");
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
        else
        {
            throw new NoSuchElementException(
                '\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value instanceof BigDecimal)
        {
            return (BigDecimal) value;
        }
        else if (value instanceof String)
        {
            try
            {
                BigDecimal number = new BigDecimal((String) value);
                return number;
            }
            catch (Exception e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a BigDecimal object", e);
            }
        }
        else if (value == null)
        {
            return defaultValue;
        }
        else
        {
            throw new ConversionException('\'' + key + "' doesn't map to a BigDecimal object");
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
        else
        {
            throw new NoSuchElementException(
                '\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * {@inheritDoc}
     */
    public BigInteger getBigInteger(String key, BigInteger defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value instanceof BigInteger)
        {
            return (BigInteger) value;
        }
        else if (value instanceof String)
        {
            try
            {
                BigInteger number = new BigInteger((String) value);
                return number;
            }
            catch (Exception e)
            {
                throw new ConversionException('\'' + key + "' doesn't map to a BigDecimal object", e);
            }
        }
        else if (value == null)
        {
            return defaultValue;
        }
        else
        {
            throw new ConversionException(
                '\'' + key + "' doesn't map to a BigDecimal object");
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
        else
        {
            throw new NoSuchElementException(
                '\'' + key + "' doesn't map to an existing object");
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
            throw new ConversionException(
                '\'' + key + "' doesn't map to a String object");
        }
    }

    /**
     * {@inheritDoc}
     */
    public String[] getStringArray(String key)
    {
        Object value = getPropertyDirect(key);

        String[] tokens;

        if (value instanceof String)
        {
            tokens = new String[1];

            tokens[0] = interpolate((String) value);
        }
        else if (value instanceof Container)
        {
            tokens = new String[((Container) value).size()];

            for (int i = 0; i < tokens.length; i++)
            {
                tokens[i] = interpolate((String) ((Container) value).get(i));
            }
        }
        else if (value == null)
        {
            tokens = new String[0];
        }
        else
        {
            throw new ConversionException(
                '\'' + key + "' doesn't map to a String/List object");
        }
        return tokens;
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
        Object value = getPropertyDirect(key);
        List list = null;

        if (value instanceof String)
        {
            list = new ArrayList(1);
            list.add(value);
        }
        else if (value instanceof Container)
        {
            list = ((Container) value).asList();
        }
        else if (value instanceof List)
        {
            list = (List) value;
        }
        else if (value == null)
        {
            list = defaultValue;
        }
        else
        {
            throw new ConversionException(
                '\''
                    + key
                    + "' doesn't map to a List object: "
                    + value
                    + ", a "
                    + value.getClass().getName());
        }
        return list;
    }

    /**
     * Returns an object from the store described by the key.
     * If the value is a Container object, replace it with the
     * first object in the container
     *
     * @param key The property key.
     *
     * @return value Value, transparently resolving a possible
     *         Container dependency.
     */
    private Object resolveContainerStore(String key)
    {
        Object value = getPropertyDirect(key);
        if (value != null && value instanceof Container)
        {
            value = ((Container) value).get(0);
        }
        return value;
    }

    /**
     * This class divides into tokens a property value.  Token
     * separator is "," but commas into the property value are escaped
     * using the backslash in front.
     */
    static class PropertiesTokenizer extends StringTokenizer
    {
        /**
         * Constructor.
         *
         * @param string A String.
         */
        public PropertiesTokenizer(String string)
        {
            super(string, String.valueOf(DELIMITER));
        }

        /**
         * Get next token.
         *
         * @return A String.
         */
        public String nextToken()
        {
            StringBuffer buffer = new StringBuffer();

            while (hasMoreTokens())
            {
                String token = super.nextToken();
                if (token.endsWith("\\"))
                {
                    buffer.append(token.substring(0, token.length() - 1));
                    buffer.append(DELIMITER);
                }
                else
                {
                    buffer.append(token);
                    break;
                }
            }
            return buffer.toString().trim();
        }
    } // class PropertiesTokenizer

    /**
     * Private Wrapper class for List, so we can distinguish between
     * List objects and our container
     */
    static class Container
    {
        /** We're wrapping a List object (A List) */
        private List list;

        /**
         * Constructor
         */
        public Container()
        {
            list = new ArrayList(INITIAL_LIST_SIZE);
        }

        /**
         * Add an Object to the Container
         *
         * @param o The Object
         */
        public void add(Object o)
        {
            list.add(o);
        }

        /**
         * Returns the current size of the Container
         *
         * @return The Number of elements in the container
         */
        public int size()
        {
            return list.size();
        }

        /**
         * Returns the Element at an index
         *
         * @param index The Index
         * @return The element at that index
         */
        public Object get(int index)
        {
            return list.get(index);
        }

        /**
         * Returns an Iterator over the container objects
         *
         * @return An Iterator
         */
        public Iterator iterator()
        {
            return list.iterator();
        }

        /**
         * Returns the Elements of the Container as a List. This is not the
         * internal list element but a shallow copy of the internal list. You
         * may modify the returned list without modifying the container.
         *
         * @return A List containing the elements of the Container.
         */
        public List asList()
        {
            return new ArrayList(list);
        }
    }
}
