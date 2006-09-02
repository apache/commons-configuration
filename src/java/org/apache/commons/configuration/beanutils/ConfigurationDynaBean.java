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

package org.apache.commons.configuration.beanutils;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationMap;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The <tt>ConfigurationDynaBean</tt> dynamically reads and writes
 * configurations properties from a wrapped configuration-collection
 * {@link org.apache.commons.configuration.Configuration} instance. It also
 * implements a {@link java.util.Map} interface so that it can be used in
 * JSP 2.0 Expression Language expressions.
 *
 * <p>The <code>ConfigurationDynaBean</code> maps nested and mapped properties
 * to the appropriate <code>Configuration</code> subset using the
 * {@link org.apache.commons.configuration.Configuration#subset}
 * method. Similarly, indexed properties reference lists of configuration
 * properties using the
 * {@link org.apache.commons.configuration.Configuration#getList(String)}
 * method. Setting an indexed property always throws an exception.</p>
 *
 * @author <a href="mailto:ricardo.gladwell@btinternet.com">Ricardo Gladwell</a>
 * @version $Revision$, $Date$
 * @since 1.0-rc1
 */
public class ConfigurationDynaBean extends ConfigurationMap implements DynaBean
{
    /** The logger.*/
    private static Log log = LogFactory.getLog(ConfigurationDynaBean.class);

    /**
     * Creates a new instance of <code>ConfigurationDynaBean</code> and sets
     * the configuration this bean is associated with.
     * @param configuration the configuration
     */
    public ConfigurationDynaBean(Configuration configuration)
    {
        super(configuration);
        if (log.isTraceEnabled())
        {
            log.trace("ConfigurationDynaBean(" + configuration + ")");
        }
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#set(java.lang.String, java.lang.Object)
     */
    public void set(String name, Object value)
    {
        if (log.isTraceEnabled())
        {
            log.trace("set(" + name + "," + value + ")");
        }

        if (value == null)
        {
            throw new NullPointerException("Error trying to set property to null.");
        }

        if (value instanceof List)
        {
            List list = (List) value;
            Iterator iterator = list.iterator();
            while (iterator.hasNext())
            {
                getConfiguration().addProperty(name, iterator.next());
            }
        }
        else if (value instanceof int[])
        {
            int[] array = (int[]) value;
            for (int i = 0; i < array.length; i++)
            {
                getConfiguration().addProperty(name, new Integer(array[i]));
            }
        }
        else if (value instanceof boolean[])
        {
            boolean[] array = (boolean[]) value;
            for (int i = 0; i < array.length; i++)
            {
                getConfiguration().addProperty(name, BooleanUtils.toBooleanObject(array[i]));
            }
        }
        else if (value instanceof char[])
        {
            char[] array = (char[]) value;
            for (int i = 0; i < array.length; i++)
            {
                getConfiguration().addProperty(name, new Character(array[i]));
            }
        }
        else if (value instanceof byte[])
        {
            byte[] array = (byte[]) value;
            for (int i = 0; i < array.length; i++)
            {
                getConfiguration().addProperty(name, new Byte(array[i]));
            }
        }
        else if (value instanceof short[])
        {
            short[] array = (short[]) value;
            for (int i = 0; i < array.length; i++)
            {
                getConfiguration().addProperty(name, new Short(array[i]));
            }
        }
        else if (value instanceof long[])
        {
            long[] array = (long[]) value;
            for (int i = 0; i < array.length; i++)
            {
                getConfiguration().addProperty(name, new Long(array[i]));
            }
        }
        else if (value instanceof float[])
        {
            float[] array = (float[]) value;
            for (int i = 0; i < array.length; i++)
            {
                getConfiguration().addProperty(name, new Float(array[i]));
            }
        }
        else if (value instanceof double[])
        {
            double[] array = (double[]) value;
            for (int i = 0; i < array.length; i++)
            {
                getConfiguration().addProperty(name, new Double(array[i]));
            }
        }
        else if (value instanceof Object[])
        {
            Object[] array = (Object[]) value;
            for (int i = 0; i < array.length; i++)
            {
                getConfiguration().addProperty(name, array[i]);
            }
        }
        else
        {
            getConfiguration().setProperty(name, value);
        }
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#get(java.lang.String)
     */
    public Object get(String name)
    {
        if (log.isTraceEnabled())
        {
            log.trace("get(" + name + ")");
        }

        // get configuration property
        Object result = getConfiguration().getProperty(name);
        if (result == null)
        {
            // otherwise attempt to create bean from configuration subset
            Configuration subset = getConfiguration().subset(name);
            if (!subset.isEmpty())
            {
                result = new ConfigurationDynaBean(getConfiguration().subset(name));
            }
        }

        if (log.isDebugEnabled())
        {
            log.debug(name + "=[" + result + "]");
        }

        if (result == null)
        {
            throw new IllegalArgumentException("Property '" + name + "' does not exist.");
        }
        return result;
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#contains(java.lang.String, java.lang.String)
     */
    public boolean contains(String name, String key)
    {
        Configuration subset = getConfiguration().subset(name);
        if (subset == null)
        {
            throw new IllegalArgumentException("Mapped property '" + name + "' does not exist.");
        }

        return subset.containsKey(key);
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#get(java.lang.String, int)
     */
    public Object get(String name, int index)
    {
        try
        {
            List list = getConfiguration().getList(name);
            if (list.isEmpty())
            {
                throw new IllegalArgumentException("Indexed property '" + name + "' does not exist.");
            }

            return list.get(index);
        }
        catch (ConversionException e)
        {
            throw new IllegalArgumentException("Property '" + name + "' is not indexed.");
        }
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#get(java.lang.String, java.lang.String)
     */
    public Object get(String name, String key)
    {
        Configuration subset = getConfiguration().subset(name);
        if (subset == null)
        {
            throw new IllegalArgumentException("Mapped property '" + name + "' does not exist.");
        }

        return subset.getProperty(key);
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#getDynaClass()
     */
    public DynaClass getDynaClass()
    {
        return new ConfigurationDynaClass(getConfiguration());
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#remove(java.lang.String, java.lang.String)
     */
    public void remove(String name, String key)
    {
        Configuration subset = getConfiguration().subset(name);
        if (subset == null)
        {
            throw new IllegalArgumentException("Mapped property '" + name + "' does not exist.");
        }
        subset.setProperty(key, null);
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#set(java.lang.String, int, java.lang.Object)
     */
    public void set(String name, int index, Object value)
    {
        try
        {
            Object property = getConfiguration().getProperty(name);

            if (property == null)
            {
                throw new IllegalArgumentException("Property '" + name + "' does not exist.");
            }
            else if (property instanceof List)
            {
                List list = (List) property;
                list.set(index, value);
            }
            else if (property.getClass().isArray())
            {
                Object[] array = (Object[]) property;
                array[index] = value;
            }
            else if (index == 0)
            {
                getConfiguration().setProperty(name, value);
            }
            else
            {
                throw new IllegalArgumentException("Property '" + name + "' is not indexed.");
            }
        }
        catch (ConversionException e)
        {
            throw new IllegalArgumentException("Property '" + name + "' is not indexed.");
        }
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#set(java.lang.String, java.lang.String, java.lang.Object)
     */
    public void set(String name, String key, Object value)
    {
        getConfiguration().setProperty(name + "." + key, value);
    }

}
