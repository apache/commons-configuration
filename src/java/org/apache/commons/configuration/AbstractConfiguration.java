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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Abstract configuration class. Provide basic functionality but does not
 * store any data. If you want to write your own Configuration class
 * then you should implement only abstract methods from this class.
 *
 * @author <a href="mailto:ksh@scand.com">Konstantin Shaposhnikov</a>
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: AbstractConfiguration.java,v 1.1 2003/12/23 15:09:05 epugh Exp $
 */
public abstract class AbstractConfiguration implements Configuration
{
    /** how big the initial arraylist for splitting up name value pairs */
    private static final int INITIAL_LIST_SIZE = 2;

    /**
     * stores the configuration key-value pairs
     */
    protected Configuration defaults = null;

    /** start token */
    protected static final String START_TOKEN = "${";
    /** end token */
    protected static final String END_TOKEN = "}";

    /**
     * Empty constructor.
     */
    public AbstractConfiguration()
    {
    }

    /**
     * Creates an empty AbstractConfiguration object with
     * a Super-Object which is queries for every key.
     *
     * @param defaults Configuration defaults to use if key not in file
     */
    public AbstractConfiguration(Configuration defaults)
    {
        this();
        this.defaults = defaults;
    }

    /**
     * Add a property to the configuration. If it already exists then the value
     * stated here will be added to the configuration entry. For example, if
     *
     * resource.loader = file
     *
     * is already present in the configuration and you
     *
     * addProperty("resource.loader", "classpath")
     *
     * Then you will end up with a Vector like the following:
     *
     * ["file", "classpath"]
     *
     * @param key The Key to add the property to.
     * @param token The Value to add.
     */
    public void addProperty(String key, Object token)
    {
        if (token instanceof String)
        {
            for(Iterator it = processString((String) token).iterator();
            it.hasNext();)
            {
                addPropertyDirect(key, it.next());
            }
        }
        else if (token instanceof Collection)
        {
            for (Iterator it = ((Collection) token).iterator(); it.hasNext();)
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
        return (interpolateHelper(base, null));
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
            else if (defaults != null && defaults.getString(variable,
                null) != null)
            {
                result.append(defaults.getString(variable));
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
     * Returns a Vector of Strings built from the supplied
     * String. Splits up CSV lists. If no commas are in the
     * String, simply returns a Vector with the String as its
     * first element
     *
     * @param token The String to tokenize
     *
     * @return A List of Strings
     */
    protected List processString(String token)
    {
        List retList = new ArrayList(INITIAL_LIST_SIZE);

        if (token.indexOf(PropertiesTokenizer.DELIMITER) > 0)
        {
            PropertiesTokenizer tokenizer =
                new PropertiesTokenizer((String) token);

            while (tokenizer.hasMoreTokens())
            {
                String value = tokenizer.nextToken();
                retList.add(value);
            }
        }
        else
        {
            retList.add(token);
        }

        //
        // We keep the sequence of the keys here and
        // we also keep it in the Container. So the
        // Keys are added to the store in the sequence that
        // is given in the properties
        return retList;
    }


    /**
     * Test whether the string represent by value maps to a boolean
     * value or not. We will allow <code>true</code>, <code>on</code>,
     * and <code>yes</code> for a <code>true</code> boolean value, and
     * <code>false</code>, <code>off</code>, and <code>no</code> for
     * <code>false</code> boolean values. Case of value to test for
     * boolean status is ignored.
     *
     * @param value The value to test for boolean state.
     * @return <code>true</code> or <code>false</code> if the supplied
     * text maps to a boolean value, or <code>null</code> otherwise.
     */
    protected final Boolean testBoolean(String value)
    {
        String s = value.toLowerCase();

        if (s.equals("true") || s.equals("on") || s.equals("yes"))
        {
            return Boolean.TRUE;
        }
        else if (s.equals("false") || s.equals("off") || s.equals("no"))
        {
            return Boolean.FALSE;
        }
        else
        {
            return null;
        }
    }

    /**
     * Create an BaseConfiguration object that is a subset
     * of this one.
     *
     * @param prefix prefix string for keys
     *
     * @return subset of configuration if there is keys, that match
     * given prefix, or <code>null</code> if there is no such keys.
     */
    public Configuration subset(String prefix)
    {
        BaseConfiguration c = new BaseConfiguration();
        Iterator keys = this.getKeys();
        boolean validSubset = false;

        while (keys.hasNext())
        {
            Object key = keys.next();

            if (key instanceof String && ((String) key).startsWith(prefix))
            {
                if (!validSubset)
                {
                    validSubset = true;
                }

                String newKey = null;

                /*
                 * Check to make sure that c.subset(prefix) doesn't blow up when
                 * there is only a single property with the key prefix. This is
                 * not a useful subset but it is a valid subset.
                 */
                if (((String) key).length() == prefix.length())
                {
                    newKey = prefix;
                }
                else
                {
                    newKey = ((String) key).substring(prefix.length() + 1);
                }

                /*
                 * use addPropertyDirect() - this will plug the data as is into
                 * the Map, but will also do the right thing re key accounting
                 *
                 * QUESTION: getProperty or getPropertyDirect
                 */
                Object value = getProperty((String) key);
                if (value instanceof String)
                {
                    c.addPropertyDirect(newKey, interpolate((String) value));
                }
                else
                {
                    c.addProperty(newKey, value);
                }
            }
        }

        if (validSubset)
        {
            return c;
        }
        else
        {
            return null;
        }
    }

    /**
     * Check if the configuration is empty
     *
     * @return <code>true</code> if Configuration is empty,
     * <code>false</code> otherwise.
     */
    public abstract boolean isEmpty();

    /**
     * check if the configuration contains the key
     *
     * @param key the configuration key
     *
     * @return <code>true</code> if Configuration contain given key,
     * <code>false</code> otherwise.
     */
    public abstract boolean containsKey(String key);

    /**
     * Set a property, this will replace any previously
     * set values. Set values is implicitly a call
     * to clearProperty(key), addProperty(key,value).
     *
     * @param key the configuration key
     * @param value the property value
     */
    public void setProperty(String key, Object value)
    {
        clearProperty(key);
        addProperty(key, value); // QUESTION: or addPropertyDirect?
    }

    /**
     * Clear a property in the configuration.
     *
     * @param key the key to remove along with corresponding value.
     */
    public  abstract void clearProperty(String key);

    /**
     * Get the list of the keys contained in the configuration
     * repository.
     *
     * @return An Iterator.
     */
    public abstract Iterator getKeys();

    /**
     * Get the list of the keys contained in the configuration
     * repository that match the specified prefix.
     *
     * @param prefix The prefix to test against.
     *
     * @return An Iterator of keys that match the prefix.
     */
    public Iterator getKeys(String prefix)
    {
        Iterator keys = getKeys();
        ArrayList matchingKeys = new ArrayList();

        while (keys.hasNext())
        {
            Object key = keys.next();

            if (key instanceof String && ((String) key).startsWith(prefix))
            {
                matchingKeys.add(key);
            }
        }
        return matchingKeys.iterator();
    }

    /**
     * Get a list of properties associated with the given
     * configuration key.
     *
     * @param key The configuration key.
     *
     * @return The associated properties if key is found.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a String/Vector.
     * @throws IllegalArgumentException if one of the tokens is
     * malformed (does not contain an equals sign).
     *
     * @see #getProperties(String, Properties)
     */
    public Properties getProperties(String key)
    {
        return getProperties(key, null);
    }

    /**
     * Get a list of properties associated with the given
     * configuration key.
     *
     * @param key The configuration key.
     * @param defaults Any default values for the returned
     * <code>Properties</code> object.  Ignored if <code>null</code>.
     *
     * @return The associated properties if key is found.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a String/Vector of Strings.
     * @throws IllegalArgumentException if one of the tokens is
     * malformed (does not contain an equals sign).
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
        Properties props =
            (defaults == null ? new Properties() : new Properties(defaults));
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
     *  Gets a property from the configuration.
     *
     *  @param key property to retrieve
     *  @return value as object. Will return user value if exists,
     *          if not then default value if exists, otherwise null
     */
    public Object getProperty(String key)
    {
        // first, try to get from the 'user value' store
        Object o = getPropertyDirect(key);

        if (o == null)
        {
            // if there isn't a value there, get it from the defaults if we have
            // them
            if (defaults != null)
            {
                o = defaults.getProperty(key);
            }
        }

        //
        // We must never give a Container Object out. So if the
        // Return Value is a Container, we fix it up to be a
        // Vector
        //
        if (o instanceof Container)
        {
            o = ((Container) o).asVector();
        }
        return o;
   }

    /**
     * Get a boolean associated with the given configuration key.
     *
     * @param key The configuration key.
     *
     * @return The associated boolean.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Boolean.
     */
    public boolean getBoolean(String key)
    {
        Boolean b = getBoolean(key, (Boolean) null);
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
     * Get a boolean associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     *
     * @return The associated boolean.
     *
     * @throws ClassCastException is thrown if the key maps to an
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
     *
     * @return The associated boolean if key is found and has valid
     * format, default value otherwise.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Boolean.
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
            return testBoolean((String) value);
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getBoolean(key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }
        else
        {
            throw new ClassCastException(
                '\'' + key + "' doesn't map to a Boolean object");
        }
    }

    /**
     * Get a byte associated with the given configuration key.
     *
     * @param key The configuration key.
     *
     * @return The associated byte.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Byte.
     * @throws NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
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
     * Get a byte associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     *
     * @return The associated byte.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Byte.
     * @throws NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public byte getByte(String key, byte defaultValue)
    {
        return getByte(key, new Byte(defaultValue)).byteValue();
    }

    /**
     * Get a byte associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     *
     * @return The associated byte if key is found and has valid format, default
     *         value otherwise.
     *
     * @throws ClassCastException is thrown if the key maps to an object that
     *            is not a Byte.
     * @throws NumberFormatException is thrown if the value mapped by the key
     *            has not a valid number format.
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
            Byte b = new Byte((String) value);
            return b;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getByte(key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }
        else
        {
            throw new ClassCastException(
                '\'' + key + "' doesn't map to a Byte object");
        }
    }

    /**
     * Get a double associated with the given configuration key.
     *
     * @param key The configuration key.
     *
     * @return The associated double.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Double.
     * @throws NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
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
     * Get a double associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     *
     * @return The associated double.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Double.
     * @throws NumberFormatException is thrown if the value mapped
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
     *
     * @return The associated double if key is found and has valid
     * format, default value otherwise.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Double.
     * @throws NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
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
            Double d = new Double((String) value);
            return d;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getDouble(key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }
        else
        {
            throw new ClassCastException(
                '\'' + key + "' doesn't map to a Double object");
        }
    }

    /**
     * Get a float associated with the given configuration key.
     *
     * @param key The configuration key.
     *
     * @return The associated float.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Float.
     * @throws NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
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
     * Get a float associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     *
     * @return The associated float.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Float.
     * @throws NumberFormatException is thrown if the value mapped
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
     *
     * @return The associated float if key is found and has valid
     * format, default value otherwise.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Float.
     * @throws NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
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
            Float f = new Float((String) value);
            return f;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getFloat(key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }
        else
        {
            throw new ClassCastException(
                '\'' + key + "' doesn't map to a Float object");
        }
    }

    /**
     * Get a int associated with the given configuration key.
     *
     * @param key The configuration key.
     *
     * @return The associated int.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Integer.
     * @throws NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
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
     * Get a int associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     *
     * @return The associated int.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Integer.
     * @throws NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
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
     * Get a int associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     *
     * @return The associated int if key is found and has valid format, default
     *         value otherwise.
     *
     * @throws ClassCastException is thrown if the key maps to an object that
     *         is not a Integer.
     * @throws NumberFormatException is thrown if the value mapped by the key
     *         has not a valid number format.
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
            Integer i = new Integer((String) value);
            return i;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getInteger(key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }
        else
        {
            throw new ClassCastException(
                '\'' + key + "' doesn't map to a Integer object");
        }
    }

    /**
     * Get a long associated with the given configuration key.
     *
     * @param key The configuration key.
     *
     * @return The associated long.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Long.
     * @throws NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
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
     * Get a long associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     *
     * @return The associated long.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Long.
     * @throws NumberFormatException is thrown if the value mapped
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
     *
     * @return The associated long if key is found and has valid
     * format, default value otherwise.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Long.
     * @throws NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
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
            Long l = new Long((String) value);
            return l;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getLong(key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }
        else
        {
            throw new ClassCastException(
                '\'' + key + "' doesn't map to a Long object");
        }
    }

    /**
     * Get a short associated with the given configuration key.
     *
     * @param key The configuration key.
     *
     * @return The associated short.
     *
     * @throws NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Short.
     * @throws NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
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
     * Get a short associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     *
     * @return The associated short.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Short.
     * @throws NumberFormatException is thrown if the value mapped
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
     *
     * @return The associated short if key is found and has valid
     * format, default value otherwise.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Short.
     * @throws NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
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
            Short s = new Short((String) value);
            return s;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getShort(key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }
        else
        {
            throw new ClassCastException(
                '\'' + key + "' doesn't map to a Short object");
        }
    }

    /**
     * Get a string associated with the given configuration key.
     *
     * @param key The configuration key.
     *
     * @return The associated string.
     *
     * @throws ClassCastException is thrown if the key maps to an object that
     *            is not a String.
     * @throws NoSuchElementException is thrown if the key doesn't
     *         map to an existing object.
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
     * Get a string associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     *
     * @return The associated string if key is found, default value otherwise.
     *
     * @throws ClassCastException is thrown if the key maps to an object that
     *            is not a String.
     */
    public String getString(String key, String defaultValue)
    {
        Object value = resolveContainerStore(key);

        if (value instanceof String)
        {
            return (String) interpolate((String) value);
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return interpolate(defaults.getString(key, defaultValue));
            }
            else
            {
                return interpolate(defaultValue);
            }
        }
        else
        {
            throw new ClassCastException(
                '\'' + key + "' doesn't map to a String object");
        }
    }

    /**
     * Get an array of strings associated with the given configuration
     * key.
     *
     * @param key The configuration key.
     *
     * @return The associated string array if key is found.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a String/Vector of Strings.
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
            if (defaults != null)
            {
                tokens = defaults.getStringArray(key);
            }
            else
            {
                tokens = new String[0];
            }
        }
        else
        {
            throw new ClassCastException(
                '\'' + key + "' doesn't map to a String/Vector object");
        }
        return tokens;
    }

    /**
     * Get a Vector of strings associated with the given configuration key.
     *
     * @param key The configuration key.
     *
     * @return The associated Vector.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Vector.
     * @throws NoSuchElementException is thrown if the key doesn't
     *         map to an existing object.
     */
    public Vector getVector(String key)
    {
        Vector v = getVector(key, null);
        if (v != null)
        {
            return v;
        }
        else
        {
            throw new NoSuchElementException(
                '\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * Get a Vector of strings associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     *
     * @return The associated Vector.
     *
     * @throws ClassCastException is thrown if the key maps to an
     * object that is not a Vector.
     */
    public Vector getVector(String key, Vector defaultValue)
    {
        Object value = getPropertyDirect(key);
        Vector v = null;

        if (value instanceof String)
        {
            v = new Vector(1);
            v.addElement((String) value);
        }
        else if (value instanceof Container)
        {
            v = ((Container) value).asVector();
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                v = defaults.getVector(key, defaultValue);
            }
            else
            {
                v = ((defaultValue == null) ? new Vector() : defaultValue);
            }
        }
        else
        {
            throw new ClassCastException(
                '\''
                    + key
                    + "' doesn't map to a Vector object: "
                    + value
                    + ", a "
                    + value.getClass().getName());
        }
        return v;
    }

    /**
     * Returns an object from the store described by the key.
     * If the value is a Container object, replace it with the
     * first object in the container
     *
     * @param key The property key.
     *
     * @return value Value, transparently resolving a possible
     *               Container dependency.
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
    class PropertiesTokenizer extends StringTokenizer
    {
        /** The property delimiter used while parsing (a comma). */
        static final String DELIMITER = ",";

        /**
         * Constructor.
         *
         * @param string A String.
         */
        public PropertiesTokenizer(String string)
        {
            super(string, DELIMITER);
        }

        /**
         * Check whether the object has more tokens.
         *
         * @return True if the object has more tokens.
         */
        public boolean hasMoreTokens()
        {
            return super.hasMoreTokens();
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
     * Private Wrapper class for Vector, so we can distinguish between
     * Vector objects and our container
     */
    static class Container
    {
        /** We're wrapping a List object (A vector) */
        private List l = null;

        /**
         * C'tor
         */
        public Container()
        {
            l = new Vector(INITIAL_LIST_SIZE);
        }

        /**
         * Add an Object to the Container
         *
         * @param o The Object
         */
        public void add(Object o)
        {
            l.add(o);
        }

        /**
         * Returns the current size of the Container
         *
         * @return The Number of elements in the container
         */
        public int size()
        {
            return l.size();
        }

        /**
         * Returns the Element at an index
         *
         * @param index The Index
         * @return The element at that index
         */
        public Object get(int index)
        {
            return l.get(index);
        }

        /**
         * Returns an Iterator over the container objects
         *
         * @return An Iterator
         */
        public Iterator iterator()
        {
            return l.iterator();
        }

        /**
         * Returns the Elements of the Container as
         * a Vector. This is not the internal vector
         * element but a shallow copy of the internal
         * list. You may modify the returned list without
         * modifying the container.
         *
         * @return A Vector containing the elements of the Container.
         */
        public Vector asVector()
        {
            Vector v = new Vector(l.size());

            for (Iterator it = l.iterator(); it.hasNext();)
            {
                v.add(it.next());
            }
            return v;
        }
    }
}
