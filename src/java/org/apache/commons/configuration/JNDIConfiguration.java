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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This Configuration class allows you to interface with a JNDI datasource.
 * A JNDIConfiguration is read-only, write operations will throw an
 * UnsupportedOperationException. The clear operations are supported but the
 * underlying JNDI data source is not changed.
 *
 * @version $Id: JNDIConfiguration.java,v 1.11 2004/04/28 22:58:58 epugh Exp $
 */
public class JNDIConfiguration extends AbstractConfiguration
{
    private static Log log = LogFactory.getLog(JNDIConfiguration.class);

    /** The prefix of the context. */
    private String prefix;

    /** The JNDI context. */
    private Context context;

    /** The Set of keys that have been virtually cleared. */
    private Set clearedProperties = new HashSet();

    /**
     * JNDIConfigurations can not be added to.
     *
     * @param key The Key to add the property to.
     * @param token The Value to add.
     */
    public void addProperty(String key, Object token)
    {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    /**
     * This method recursive traverse the JNDI tree, looking for Context objects.
     * When it finds them, it traverses them as well.  Otherwise it just adds the
     * values to the list of keys found.
     *
     * @param keys All the keys that have been found.
     * @param enum An enumeration of all the elements found at a specific context
     * @param key What key we are building on.
     * @throws NamingException If JNDI has an issue.
     */
    private void recursiveGetKeys(List keys, Context parentContext, String key) throws NamingException
    {
        NamingEnumeration enum = parentContext.list("");
        while (enum.hasMoreElements())
        {
            Object o = enum.next();
            
            NameClassPair nameClassPair = (NameClassPair) o;
            StringBuffer newKey = new StringBuffer();
            newKey.append(key);
            if (newKey.length() > 0)
            {
                newKey.append(".");
            }
            newKey.append(nameClassPair.getName());
            if (parentContext.lookup(nameClassPair.getName()) instanceof Context)
            {
                Context context = (Context) parentContext.lookup(nameClassPair.getName());
                recursiveGetKeys(keys, context, newKey.toString());
            }
            else
            {
                if (!keys.contains(newKey.toString()))
                {
                    keys.add(newKey.toString());
                }
            }
        }
    }

    /**
     * Get the list of the keys contained in the configuration repository.
     *
     * @return An Iterator.
     */
    public Iterator getKeys()
    {
        return getKeys("");
    }

    /**
     * Get the list of the keys contained in the configuration
     * repository that match a passed in beginning pattern.
     *
     * @param key the key pattern to match on.
     * @return An Iterator.
     */
    public Iterator getKeys(String key)
    {
        List keys = new ArrayList();
        try
        {
            String[] splitKeys = StringUtils.split(key, ".");
            for (int i = 0; i < splitKeys.length; i++)
            {
                keys.add(splitKeys[i]);
            }

            Context context = null;

            if (keys.isEmpty())
            {
                context = getContext();
            }
            else
            {
                context = getStartingContextPoint(keys, getContext(),getContext().list(""));
            }

            if (context != null)
            {
                recursiveGetKeys(keys, context, key);
            }
        }
        catch (NamingException ne)
        {
            log.warn(ne);
        }

        return keys.iterator();
    }

    /**
     * Because JNDI is based on a tree configuration, we need to filter down the
     * tree, till we find the Context specified by the key to start from.
     * Otherwise return null.
     *
     * @param The key (or name) of the Context we are looking to start from.
     * @return The context at that key's location in the JNDI tree, or null if not found
     * @throws NamingException if JNDI has an issue
     */
    private Context getStartingContextPoint(List keys, Context parentContext, NamingEnumeration enum) throws NamingException
    {
        String keyToSearchFor = (String) keys.get(0);
        log.debug("Key to search for is " + keyToSearchFor);
        while (enum.hasMoreElements())
        {            
            NameClassPair nameClassPair = (NameClassPair) enum.next();
            Object o = parentContext.lookup(nameClassPair.getName());
            log.debug(
                "Binding for name: "
                    + nameClassPair.getName()
                    + ", object:"
                    + parentContext.lookup(nameClassPair.getName())
                    + ", class:"
                    + nameClassPair.getClassName());
            if (o instanceof Context
                && nameClassPair.getName().equals(keyToSearchFor))
            {
                keys.remove(0);
                Context c = (Context) o;
                if (!keys.isEmpty())
                {
                    return getStartingContextPoint(keys,c, c.list(""));
                }
                else
                {
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * Get a list of properties associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated properties if key is found.
     * @throws ClassCastException is thrown if the key maps to an object that is not a String/List.
     * @throws IllegalArgumentException if one of the tokens is malformed (does not contain an equals sign).
     */
    public Properties getProperties(String key)
    {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    public boolean isEmpty()
    {
        try
        {
            NamingEnumeration enum = getContext().list("");
            return !enum.hasMore();
        }
        catch (NamingException ne)
        {
            log.warn(ne);
            return true;
        }
    }

    /**
     * Gets a property from the configuration.
     *
     * @param key property to retrieve
     * @return value as object. Will return user value if exists,
     *          if not then default value if exists, otherwise null
     */
    public Object getProperty(String key)
    {
        return getPropertyDirect(key);
    }

    /**
     * Set a property, this will replace any previously set values. Set values
     * is implicitly a call to clearProperty(key), addProperty(key,value).
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, Object value)
    {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    /**
     * Clear a property in the configuration.  Just marks it as cleared,
     * doesn't change the underlying JNDI data source.
     *
     * @param key the key to remove along with corresponding value.
     */
    public void clearProperty(String key)
    {
        clearedProperties.add(key);
    }

    /**
     * Check if the configuration contains the key, or the key has been removed.
     */
    public boolean containsKey(String key)
    {
        if (clearedProperties.contains(key))
        {
            return false;
        }
        key = StringUtils.replace(key, ".", "/");
        try
        {
        	// throws a NamingException if JNDI doesn't contain the key.
            getContext().lookup(key);
            return true;
        }
        catch (NamingException ne)
        {
            return false;
        }
    }

    /**
     * @return String
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * Sets the prefix.
     *
     * @param prefix The prefix to set
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    protected Object getPropertyDirect(String key)
    {
        if (clearedProperties.contains(key))
        {
            return null;
        }

        try
        {
            key = StringUtils.replace(key, ".", "/");
            return getContext().lookup(key);
        }
        catch (NoSuchElementException nsse)
        {
            return null;
        }
        catch (NamingException ne)
        {
            ne.printStackTrace();
            return null;
        }
    }

    protected void addPropertyDirect(String key, Object obj) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    private Context getContext() throws NamingException
    {
        if (context == null)
        {
            Context initCtx = new InitialContext();
            context = (Context) initCtx.lookup(getPrefix());
        }
        return context;
    }
}
