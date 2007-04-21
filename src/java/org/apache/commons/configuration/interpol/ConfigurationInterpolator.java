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
package org.apache.commons.configuration.interpol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.text.StrLookup;

/**
 * <p>
 * A class that handles interpolation (variable substitution) for configuration
 * objects.
 * </p>
 * <p>
 * Each instance of <code>AbstractConfiguration</code> is associated with an
 * object of this class. All interpolation tasks are delegated to this object.
 * </p>
 * <p>
 * <code>ConfigurationInterpolator</code> works together with the
 * <code>StrSubstitutor</code> class from <a
 * href="http://jakarta.apache.org/commons/lang">Commons Lang</a>. By extending
 * <code>StrLookup</code> it is able to provide values for variables that
 * appear in expressions.
 * </p>
 * <p>
 * The basic idea of this class is that it can maintain a set of primitive
 * <code>StrLookup</code> objects, each of which is identified by a special
 * prefix. The variables to be processed have the form
 * <code>${prefix:name}</code>. <code>ConfigurationInterpolator</code> will
 * extract the prefix and determine, which primitive lookup object is registered
 * for it. Then the name of the variable is passed to this object to obtain the
 * actual value. It is also possible to define a default lookup object, which
 * will be used for variables that do not have a prefix or that cannot be
 * resolved by their associated lookup object.
 * </p>
 * <p>
 * When a new instance of this class is created it is initialized with a default
 * set of primitive lookup objects. This set can be customized using the static
 * methods <code>registerGlobalLookup()</code> and
 * <code>deregisterGlobalLookup()</code>. Per default it contains the
 * following standard lookup objects:
 * </p>
 * <p>
 * <table border="1">
 * <tr>
 * <th>Prefix</th>
 * <th>Lookup object</th>
 * </tr>
 * <tr>
 * <td valign="top">sys</td>
 * <td>With this prefix a lookup object is associated that is able to resolve
 * system properties.</td>
 * </tr>
 * <tr>
 * <td valign="top">const</td>
 * <td>The <code>const</code> prefix indicates that a variable is to be
 * interpreted as a constant member field of a class (i.e. a field with the
 * <b>static final</b> modifiers). The name of the variable must be of the form
 * <code>&lt;full qualified class name&gt;.&lt;field name&gt;</code>, e.g.
 * <code>org.apache.commons.configuration.interpol.ConfigurationInterpolator.PREFIX_CONSTANTS
 * </code>.</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * After an instance has been created the current set of lookup objects can be
 * modified using the <code>registerLookup()</code> and
 * <code>deregisterLookup()</code> methods. The default lookup object (that is
 * invoked for variables without a prefix) can be set with the
 * <code>setDefaultLookup()</code> method. (If a
 * <code>ConfigurationInterpolator</code> instance is created by a
 * configuration object, this lookup points to the configuration itself, so that
 * variables are resolved using the configuration's properties. This ensures
 * backward compatibility to earlier version of Commons Configuration.)
 * </p>
 * <p>
 * Implementation node: Instances of this class are not thread-safe related to
 * modifications of their current set of registered lookup objects. It is
 * intended that each instance is associated with a single
 * <code>Configuration</code> object and used for its interpolation tasks.
 * </p>
 *
 * @version $Id$
 * @since 1.4
 * @author <a
 * href="http://jakarta.apache.org/commons/configuration/team-list.html">Commons
 * Configuration team</a>
 */
public class ConfigurationInterpolator extends StrLookup
{
    /**
     * Constant for the prefix of the standard lookup object for resolving
     * system properties.
     */
    public static final String PREFIX_SYSPROPERTIES = "sys";

    /**
     * Constant for the prefix of the standard lookup object for resolving
     * constant values.
     */
    public static final String PREFIX_CONSTANTS = "const";

    /** Constant for the prefix separator. */
    private static final char PREFIX_SEPARATOR = ':';

    /** A map with the globally registered lookup objects. */
    private static Map globalLookups;

    /** A map with the locally registered lookup objects. */
    private Map localLookups;

    /** Stores the default lookup object. */
    private StrLookup defaultLookup;

    /**
     * Creates a new instance of <code>ConfigurationInterpolator</code>.
     */
    public ConfigurationInterpolator()
    {
        synchronized (globalLookups)
        {
            localLookups = new HashMap(globalLookups);
        }
    }

    /**
     * Registers the given lookup object for the specified prefix globally. This
     * means that all instances that are created later will use this lookup
     * object for this prefix. If for this prefix a lookup object is already
     * registered, the new lookup object will replace the old one. Note that the
     * lookup objects registered here will be shared between multiple clients.
     * So they should be thread-safe.
     *
     * @param prefix the variable prefix (must not be <b>null</b>)
     * @param lookup the lookup object to be used for this prefix (must not be
     * <b>null</b>)
     */
    public static void registerGlobalLookup(String prefix, StrLookup lookup)
    {
        if (prefix == null)
        {
            throw new IllegalArgumentException(
                    "Prefix for lookup object must not be null!");
        }
        if (lookup == null)
        {
            throw new IllegalArgumentException(
                    "Lookup object must not be null!");
        }
        synchronized (globalLookups)
        {
            globalLookups.put(prefix, lookup);
        }
    }

    /**
     * Deregisters the global lookup object for the specified prefix. This means
     * that this lookup object won't be available for later created instances
     * any more. For already existing instances this operation does not have any
     * impact.
     *
     * @param prefix the variable prefix
     * @return a flag whether for this prefix a lookup object had been
     * registered
     */
    public static boolean deregisterGlobalLookup(String prefix)
    {
        synchronized (globalLookups)
        {
            return globalLookups.remove(prefix) != null;
        }
    }

    /**
     * Registers the given lookup object for the specified prefix at this
     * instance. From now on this lookup object will be used for variables that
     * have the specified prefix.
     *
     * @param prefix the variable prefix (must not be <b>null</b>)
     * @param lookup the lookup object to be used for this prefix (must not be
     * <b>null</b>)
     */
    public void registerLookup(String prefix, StrLookup lookup)
    {
        if (prefix == null)
        {
            throw new IllegalArgumentException(
                    "Prefix for lookup object must not be null!");
        }
        if (lookup == null)
        {
            throw new IllegalArgumentException(
                    "Lookup object must not be null!");
        }
        localLookups.put(prefix, lookup);
    }

    /**
     * Deregisters the lookup object for the specified prefix at this instance.
     * It will be removed from this instance.
     *
     * @param prefix the variable prefix
     * @return a flag whether for this prefix a lookup object had been
     * registered
     */
    public boolean deregisterLookup(String prefix)
    {
        return localLookups.remove(prefix) != null;
    }

    /**
     * Returns a set with the prefixes, for which lookup objects are registered
     * at this instance. This means that variables with these prefixes can be
     * processed.
     *
     * @return a set with the registered variable prefixes
     */
    public Set prefixSet()
    {
        return localLookups.keySet();
    }

    /**
     * Returns the default lookup object.
     *
     * @return the default lookup object
     */
    public StrLookup getDefaultLookup()
    {
        return defaultLookup;
    }

    /**
     * Sets the default lookup object. This lookup object will be used for all
     * variables without a special prefix. If it is set to <b>null</b>, such
     * variables won't be processed.
     *
     * @param defaultLookup the new default lookup object
     */
    public void setDefaultLookup(StrLookup defaultLookup)
    {
        this.defaultLookup = defaultLookup;
    }

    /**
     * Resolves the specified variable. This implementation will try to extract
     * a variable prefix from the given variable name (the first colon (':') is
     * used as prefix separator). It then passes the name of the variable with
     * the prefix stripped to the lookup object registered for this prefix. If
     * no prefix can be found or if the associated lookup object cannot resolve
     * this variable, the default lookup object will be used.
     *
     * @param var the name of the variable whose value is to be looked up
     * @return the value of this variable or <b>null</b> if it cannot be
     * resolved
     */
    public String lookup(String var)
    {
        if (var == null)
        {
            return null;
        }
        
        int prefixPos = var.indexOf(PREFIX_SEPARATOR);
        if (prefixPos >= 0)
        {
            String prefix = var.substring(0, prefixPos);
            String name = var.substring(prefixPos + 1);
            String value = fetchLookupForPrefix(prefix).lookup(name);
            if (value != null) 
            {
                return value;
            }
        }
        return fetchNoPrefixLookup().lookup(var);
    }

    /**
     * Returns the lookup object to be used for variables without a prefix. This
     * implementation will check whether a default lookup object was set. If
     * this is the case, it will be returned. Otherwise a <b>null</b> lookup
     * object will be returned.
     *
     * @return the lookup object to be used for variables without a prefix
     */
    protected StrLookup fetchNoPrefixLookup()
    {
        return (getDefaultLookup() != null) ? getDefaultLookup() : StrLookup
                .noneLookup();
    }

    /**
     * Obtains the lookup object for the specified prefix. This method is called
     * by the <code>lookup()</code> method. This implementation will check
     * whether a lookup object is registered for the given prefix. If not, a
     * <b>null</b> lookup object will be returned.
     *
     * @param prefix the prefix
     * @return the lookup object to be used for this prefix
     */
    protected StrLookup fetchLookupForPrefix(String prefix)
    {
        StrLookup lookup = (StrLookup) localLookups.get(prefix);
        if (lookup == null)
        {
            lookup = StrLookup.noneLookup();
        }
        return lookup;
    }

    // static initializer, sets up the map with the standard lookups
    static
    {
        globalLookups = new HashMap();
        globalLookups.put(PREFIX_SYSPROPERTIES, StrLookup
                .systemPropertiesLookup());
        globalLookups.put(PREFIX_CONSTANTS, new ConstantLookup());
    }
}
