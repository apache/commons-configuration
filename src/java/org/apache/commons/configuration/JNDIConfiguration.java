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
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @version $Id: JNDIConfiguration.java,v 1.24 2004/12/04 15:54:55 oheger Exp $
 */
public class JNDIConfiguration extends AbstractConfiguration
{
    /** Logger. */
    private static Log log = LogFactory.getLog(JNDIConfiguration.class);

    /** The prefix of the context. */
    private String prefix;

    /** The initial JNDI context. */
    private Context context;

    /** The base JNDI context. */
    private Context baseContext;

    /** The Set of keys that have been virtually cleared. */
    private Set clearedProperties = new HashSet();

    /**
     * Creates a JNDIConfiguration using the default initial context as the
     * root of the properties.
     *
     * @throws NamingException thrown if an error occurs when initializing the default context
     */
    public JNDIConfiguration() throws NamingException
    {
        this((String) null);
    }

    /**
     * Creates a JNDIConfiguration using the default initial context, shifted
     * with the specified prefix, as the root of the properties.
     *
     * @param prefix
     *
     * @throws NamingException thrown if an error occurs when initializing the default context
     */
    public JNDIConfiguration(String prefix) throws NamingException
    {
        this(new InitialContext(), prefix);
    }

    /**
     * Creates a JNDIConfiguration using the specified initial context as the
     * root of the properties.
     *
     * @param context the initial context
     */
    public JNDIConfiguration(Context context)
    {
        this(context, null);
    }

    /**
     * Creates a JNDIConfiguration using the specified initial context shifted
     * by the specified prefix as the root of the properties.
     *
     * @param context the initial context
     * @param prefix
     */
    public JNDIConfiguration(Context context, String prefix)
    {
        this.context = context;
        this.prefix = prefix;
    }

    /**
     * This method recursive traverse the JNDI tree, looking for Context objects.
     * When it finds them, it traverses them as well.  Otherwise it just adds the
     * values to the list of keys found.
     *
     * @param keys All the keys that have been found.
     * @param context The parent context
     * @param prefix What prefix we are building on.
     * @throws NamingException If JNDI has an issue.
     */
    private void recursiveGetKeys(Set keys, Context context, String prefix) throws NamingException
    {
        NamingEnumeration elements = null;

        try
        {
            elements = context.list("");

            // iterates through the context's elements
            while (elements.hasMore())
            {
                NameClassPair nameClassPair = (NameClassPair) elements.next();
                String name = nameClassPair.getName();
                Object object = context.lookup(name);

                // build the key
                StringBuffer key = new StringBuffer();
                key.append(prefix);
                if (key.length() > 0)
                {
                    key.append(".");
                }
                key.append(name);

                if (object instanceof Context)
                {
                    // add the keys of the sub context
                    Context subcontext = (Context) object;
                    recursiveGetKeys(keys, subcontext, key.toString());
                }
                else
                {
                    // add the key
                    keys.add(key.toString());
                }
            }
        }
        finally
        {
            // close the enumeration
            if (elements != null)
            {
                elements.close();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Iterator getKeys()
    {
        return getKeys("");
    }

    /**
     * {@inheritDoc}
     */
    public Iterator getKeys(String prefix)
    {
        // build the path
        String[] splitPath = StringUtils.split(prefix, ".");

        List path = new ArrayList();

        for (int i = 0; i < splitPath.length; i++)
        {
            path.add(splitPath[i]);
        }

        try
        {
            // find the context matching the specified path
            Context context = getContext(path, getBaseContext());

            // return all the keys under the context found
            Set keys = new HashSet();
            if (context != null)
            {
                recursiveGetKeys(keys, context, prefix);
            }
            else if (containsKey(prefix))
            {
                // add the prefix if it matches exactly a property key
                keys.add(prefix);
            }

            return keys.iterator();
        }
        catch (NamingException e)
        {
            log.error(e.getMessage(), e);
            return new ArrayList().iterator();
        }
    }

    /**
     * Because JNDI is based on a tree configuration, we need to filter down the
     * tree, till we find the Context specified by the key to start from.
     * Otherwise return null.
     *
     * @param path     the path of keys to traverse in order to find the context
     * @param context  the context to start from
     * @return The context at that key's location in the JNDI tree, or null if not found
     * @throws NamingException if JNDI has an issue
     */
    private Context getContext(List path, Context context) throws NamingException
    {
        // return the current context if the path is empty
        if (path == null || path.isEmpty())
        {
            return context;
        }

        String key = (String) path.get(0);

        // search a context matching the key in the context's elements
        NamingEnumeration elements = null;

        try
        {
            elements = context.list("");
            while (elements.hasMore())
            {
                NameClassPair nameClassPair = (NameClassPair) elements.next();
                String name = nameClassPair.getName();
                Object object = context.lookup(name);

                if (object instanceof Context && name.equals(key))
                {
                    Context subcontext = (Context) object;

                    // recursive search in the sub context
                    return getContext(path.subList(1, path.size()), subcontext);
                }
            }
        }
        finally
        {
            if (elements != null)
            {
                elements.close();
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty()
    {
        try
        {
            NamingEnumeration enumeration = null;

            try
            {
                enumeration = getBaseContext().list("");
                return !enumeration.hasMore();
            }
            finally
            {
                // close the enumeration
                if (enumeration != null)
                {
                    enumeration.close();
                }
            }
        }
        catch (NamingException e)
        {
            log.error(e.getMessage(), e);
            return true;
        }
    }

    /**
     * <p><strong>This operation is not supported and will throw an
     * UnsupportedOperationException.</strong></p>
     *
     * @throws UnsupportedOperationException
     */
    public void setProperty(String key, Object value)
    {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    /**
     * {@inheritDoc}
     */
    public void clearProperty(String key)
    {
        clearedProperties.add(key);
    }

    /**
     * {@inheritDoc}
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
            getBaseContext().lookup(key);
            return true;
        }
        catch (NamingException e)
        {
            log.error(e.getMessage(), e);
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

        // clear the previous baseContext
        baseContext = null;
    }

    /**
     * {@inheritDoc}
     */
    public Object getProperty(String key)
    {
        if (clearedProperties.contains(key))
        {
            return null;
        }

        try
        {
            key = StringUtils.replace(key, ".", "/");
            return getBaseContext().lookup(key);
        }
        catch (NamingException e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * <p><strong>This operation is not supported and will throw an
     * UnsupportedOperationException.</strong></p>
     *
     * @throws UnsupportedOperationException
     */
    protected void addPropertyDirect(String key, Object obj)
    {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    /**
     * Return the base context with the prefix applied.
     */
    public Context getBaseContext() throws NamingException
    {
        if (baseContext == null)
        {
            baseContext = (Context) getContext().lookup(prefix == null ? "" : prefix);
        }

        return baseContext;
    }

    /**
     * Return the initial context used by this configuration. This context is
     * independent of the prefix specified.
     */
    public Context getContext()
    {
        return context;
    }

    /**
     * Set the initial context of the configuration.
     */
    public void setContext(Context context)
    {
        // forget the removed properties
        clearedProperties.clear();

        // change the context
        this.context = context;
    }
}
