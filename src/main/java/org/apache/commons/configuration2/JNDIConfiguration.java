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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration2.event.ConfigurationErrorEvent;
import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.apache.commons.lang3.StringUtils;

/**
 * This Configuration class allows you to interface with a JNDI datasource.
 * A JNDIConfiguration is read-only, write operations will throw an
 * UnsupportedOperationException. The clear operations are supported but the
 * underlying JNDI data source is not changed.
 *
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 */
public class JNDIConfiguration extends AbstractConfiguration
{
    /** The prefix of the context. */
    private String prefix;

    /** The initial JNDI context. */
    private Context context;

    /** The base JNDI context. */
    private Context baseContext;

    /** The Set of keys that have been virtually cleared. */
    private final Set<String> clearedProperties = new HashSet<>();

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
     * @param prefix the prefix
     *
     * @throws NamingException thrown if an error occurs when initializing the default context
     */
    public JNDIConfiguration(final String prefix) throws NamingException
    {
        this(new InitialContext(), prefix);
    }

    /**
     * Creates a JNDIConfiguration using the specified initial context as the
     * root of the properties.
     *
     * @param context the initial context
     */
    public JNDIConfiguration(final Context context)
    {
        this(context, null);
    }

    /**
     * Creates a JNDIConfiguration using the specified initial context shifted
     * by the specified prefix as the root of the properties.
     *
     * @param context the initial context
     * @param prefix the prefix
     */
    public JNDIConfiguration(final Context context, final String prefix)
    {
        this.context = context;
        this.prefix = prefix;
        initLogger(new ConfigurationLogger(JNDIConfiguration.class));
        addErrorLogListener();
    }

    /**
     * This method recursive traverse the JNDI tree, looking for Context objects.
     * When it finds them, it traverses them as well.  Otherwise it just adds the
     * values to the list of keys found.
     *
     * @param keys All the keys that have been found.
     * @param context The parent context
     * @param prefix What prefix we are building on.
     * @param processedCtx a set with the so far processed objects
     * @throws NamingException If JNDI has an issue.
     */
    private void recursiveGetKeys(final Set<String> keys, final Context context, final String prefix,
            final Set<Context> processedCtx) throws NamingException
    {
        processedCtx.add(context);
        NamingEnumeration<NameClassPair> elements = null;

        try
        {
            elements = context.list("");

            // iterates through the context's elements
            while (elements.hasMore())
            {
                final NameClassPair nameClassPair = elements.next();
                final String name = nameClassPair.getName();
                final Object object = context.lookup(name);

                // build the key
                final StringBuilder key = new StringBuilder();
                key.append(prefix);
                if (key.length() > 0)
                {
                    key.append(".");
                }
                key.append(name);

                if (object instanceof Context)
                {
                    // add the keys of the sub context
                    final Context subcontext = (Context) object;
                    if (!processedCtx.contains(subcontext))
                    {
                        recursiveGetKeys(keys, subcontext, key.toString(),
                                processedCtx);
                    }
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
     * Returns an iterator with all property keys stored in this configuration.
     *
     * @return an iterator with all keys
     */
    @Override
    protected Iterator<String> getKeysInternal()
    {
        return getKeysInternal("");
    }

    /**
     * Returns an iterator with all property keys starting with the given
     * prefix.
     *
     * @param prefix the prefix
     * @return an iterator with the selected keys
     */
    @Override
    protected Iterator<String> getKeysInternal(final String prefix)
    {
        // build the path
        final String[] splitPath = StringUtils.split(prefix, ".");

        final List<String> path = Arrays.asList(splitPath);

        try
        {
            // find the context matching the specified path
            final Context context = getContext(path, getBaseContext());

            // return all the keys under the context found
            final Set<String> keys = new HashSet<>();
            if (context != null)
            {
                recursiveGetKeys(keys, context, prefix, new HashSet<Context>());
            }
            else if (containsKey(prefix))
            {
                // add the prefix if it matches exactly a property key
                keys.add(prefix);
            }

            return keys.iterator();
        }
        catch (final NameNotFoundException e)
        {
            // expected exception, no need to log it
            return new ArrayList<String>().iterator();
        }
        catch (final NamingException e)
        {
            fireError(ConfigurationErrorEvent.READ,
                    ConfigurationErrorEvent.READ, null, null, e);
            return new ArrayList<String>().iterator();
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
    private Context getContext(final List<String> path, final Context context) throws NamingException
    {
        // return the current context if the path is empty
        if (path == null || path.isEmpty())
        {
            return context;
        }

        final String key = path.get(0);

        // search a context matching the key in the context's elements
        NamingEnumeration<NameClassPair> elements = null;

        try
        {
            elements = context.list("");
            while (elements.hasMore())
            {
                final NameClassPair nameClassPair = elements.next();
                final String name = nameClassPair.getName();
                final Object object = context.lookup(name);

                if (object instanceof Context && name.equals(key))
                {
                    final Context subcontext = (Context) object;

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
     * Returns a flag whether this configuration is empty.
     *
     * @return the empty flag
     */
    @Override
    protected boolean isEmptyInternal()
    {
        try
        {
            NamingEnumeration<NameClassPair> enumeration = null;

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
        catch (final NamingException e)
        {
            fireError(ConfigurationErrorEvent.READ,
                    ConfigurationErrorEvent.READ, null, null, e);
            return true;
        }
    }

    /**
     * <p><strong>This operation is not supported and will throw an
     * UnsupportedOperationException.</strong></p>
     *
     * @param key the key
     * @param value the value
     * @throws UnsupportedOperationException always thrown as this method is not supported
     */
    @Override
    protected void setPropertyInternal(final String key, final Object value)
    {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    /**
     * Removes the specified property.
     *
     * @param key the key of the property to remove
     */
    @Override
    protected void clearPropertyDirect(final String key)
    {
        clearedProperties.add(key);
    }

    /**
     * Checks whether the specified key is contained in this configuration.
     *
     * @param key the key to check
     * @return a flag whether this key is stored in this configuration
     */
    @Override
    protected boolean containsKeyInternal(String key)
    {
        if (clearedProperties.contains(key))
        {
            return false;
        }
        key = key.replaceAll("\\.", "/");
        try
        {
            // throws a NamingException if JNDI doesn't contain the key.
            getBaseContext().lookup(key);
            return true;
        }
        catch (final NameNotFoundException e)
        {
            // expected exception, no need to log it
            return false;
        }
        catch (final NamingException e)
        {
            fireError(ConfigurationErrorEvent.READ,
                    ConfigurationErrorEvent.READ, key, null, e);
            return false;
        }
    }

    /**
     * Returns the prefix.
     * @return the prefix
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
    public void setPrefix(final String prefix)
    {
        this.prefix = prefix;

        // clear the previous baseContext
        baseContext = null;
    }

    /**
     * Returns the value of the specified property.
     *
     * @param key the key of the property
     * @return the value of this property
     */
    @Override
    protected Object getPropertyInternal(String key)
    {
        if (clearedProperties.contains(key))
        {
            return null;
        }

        try
        {
            key = key.replaceAll("\\.", "/");
            return getBaseContext().lookup(key);
        }
        catch (final NameNotFoundException e)
        {
            // expected exception, no need to log it
            return null;
        }
        catch (final NotContextException nctxex)
        {
            // expected exception, no need to log it
            return null;
        }
        catch (final NamingException e)
        {
            fireError(ConfigurationErrorEvent.READ,
                    ConfigurationErrorEvent.READ, key, null, e);
            return null;
        }
    }

    /**
     * <p><strong>This operation is not supported and will throw an
     * UnsupportedOperationException.</strong></p>
     *
     * @param key the key
     * @param obj the value
     * @throws UnsupportedOperationException always thrown as this method is not supported
     */
    @Override
    protected void addPropertyDirect(final String key, final Object obj)
    {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    /**
     * Return the base context with the prefix applied.
     *
     * @return the base context
     * @throws NamingException if an error occurs
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
     *
     * @return the initial context
     */
    public Context getContext()
    {
        return context;
    }

    /**
     * Set the initial context of the configuration.
     *
     * @param context the context
     */
    public void setContext(final Context context)
    {
        // forget the removed properties
        clearedProperties.clear();

        // change the context
        this.context = context;
    }
}
