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

package org.apache.commons.configuration.beanutils;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>The <tt>ConfigurationDynaBean</tt> dynamically reads and
 * writes configurations properties from a wrapped
 * configuration-collection
 * {@link org.apache.commons.configuration.Configuration}
 * instance. It also implements a {@link java.util.Map} interface
 * so that it can be used in JSP 2.0 Expression Language
 * expressions.</p>
 *
 * <p>The <code>ConfigurationDynaBean</code> maps nested and mapped
 * properties to the appropriate <code>Configuration</code> subset
 * using the
 * {@link org.apache.commons.configuration.Configuration#subset}
 * method. Similarly, indexed properties reference lists of
 * configuration properties using the
 * {@link org.apache.commons.configuration.Configuration#getList(String)}
 * method. Setting an indexed property always throws an exception.</p>
 *
 * @author <a href="mailto:ricardo.gladwell@btinternet.com">Ricardo Gladwell</a>
 */
public class ConfigurationDynaBean implements DynaBean {

    private final static Log log = LogFactory.getLog(ConfigurationDynaBean.class);

    Configuration configuration;

    public ConfigurationDynaBean(Configuration configuration) {
        if(log.isTraceEnabled()) log.trace("ConfigurationDynaBean("+configuration+")");
        this.configuration = configuration;
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#set(java.lang.String, java.lang.Object)
     */
    public void set(String name, Object value) {
        if (log.isTraceEnabled()) log.trace("set("+name+","+value+")");
        if (value == null) throw new NullPointerException("Error trying to set property to null.");
        if (value instanceof List) {
            List list = (List) value;
            Iterator iterator = list.iterator();
            while(iterator.hasNext())
                configuration.addProperty(name,iterator.next());
        } else if (value instanceof int[]) {
            int[] array = (int[]) value;
            for (int i = 0; i < array.length; i++)
                configuration.addProperty(name,new Integer(array[i]));
        } else if (value instanceof boolean[]) {
            boolean[] array = (boolean[]) value;
            for (int i = 0; i < array.length; i++)
                configuration.addProperty(name,Boolean.valueOf(array[i]));
        } else if (value instanceof char[]) {
            char[] array = (char[]) value;
            for (int i = 0; i < array.length; i++)
                configuration.addProperty(name,new Character(array[i]));
        } else if (value instanceof byte[]) {
            byte[] array = (byte[]) value;
            for (int i = 0; i < array.length; i++)
                configuration.addProperty(name,new Byte(array[i]));
        } else if (value instanceof short[]) {
            short[] array = (short[]) value;
            for (int i = 0; i < array.length; i++)
                configuration.addProperty(name,new Short(array[i]));
        } else if (value instanceof int[]) {
            int[] array = (int[]) value;
            for (int i = 0; i < array.length; i++)
                configuration.addProperty(name,new Integer(array[i]));
        } else if (value instanceof long[]) {
            long[] array = (long[]) value;
            for (int i = 0; i < array.length; i++)
                configuration.addProperty(name,new Long(array[i]));
        } else if (value instanceof float[]) {
            float[] array = (float[]) value;
            for (int i = 0; i < array.length; i++)
                configuration.addProperty(name,new Float(array[i]));
        } else if (value instanceof double[]) {
            double[] array = (double[]) value;
            for (int i = 0; i < array.length; i++)
                configuration.addProperty(name, new Double(array[i]));
        } else if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            for (int i = 0; i < array.length; i++)
                configuration.addProperty(name,array[i]);
        } else
            configuration.setProperty(name, value);
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#get(java.lang.String)
     */
    public Object get(String name) {
        if (log.isTraceEnabled()) log.trace("get("+name+")");
        // get configuration property
        Object result = configuration.getProperty(name);
        if (result == null) {
            // otherwise attempt to create bean from configuration subset
            Configuration subset = configuration.subset(name);
            if (!subset.isEmpty())
                result = new ConfigurationDynaBean(configuration.subset(name));
        }
        if (log.isDebugEnabled()) log.debug(name+"=["+result+"]");
        if (result == null)
            throw new IllegalArgumentException
                ("Property '" + name +"' does not exist.");
        return result;
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#contains(java.lang.String, java.lang.String)
     */
    public boolean contains(String name, String key) {
        Configuration subset = configuration.subset(name);
        if (subset == null)
            throw new IllegalArgumentException
                    ("Mapped property '" + name +"' does not exist.");
        return subset.containsKey(key);
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#get(java.lang.String, int)
     */
    public Object get(String name, int index) {
        try {
            List list = configuration.getList(name);
            if (list.isEmpty())
                throw new IllegalArgumentException
                    ("Indexed property '" + name +"' does not exist.");
            return list.get(index);
        } catch(ConversionException e) {
            throw new IllegalArgumentException("Property '" + name +"' is not indexed.");
        }
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#get(java.lang.String, java.lang.String)
     */
    public Object get(String name, String key) {
        Configuration subset = configuration.subset(name);
        if (subset == null)
            throw new IllegalArgumentException
                    ("Mapped property '" + name +"' does not exist.");
        return subset.getProperty(key);
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#getDynaClass()
     */
    public DynaClass getDynaClass() {
        return new ConfigurationDynaClass(configuration);
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#remove(java.lang.String, java.lang.String)
     */
    public void remove(String name, String key) {
        Configuration subset = configuration.subset(name);
        if (subset == null)
            throw new IllegalArgumentException
                    ("Mapped property '" + name +"' does not exist.");
        subset.setProperty(key, null);
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#set(java.lang.String, int, java.lang.Object)
     */
    public void set(String name, int index, Object value) {
        try {
            List list = configuration.getList(name);
            if (list == null)
                throw new IllegalArgumentException
                    ("Property '" + name +"' does not exist.");
            list.set(index,value);
        } catch(ConversionException e) {
            throw new IllegalArgumentException
                ("Property '" + name +"' is not indexed.");
        }
    }

    /**
     * @see org.apache.commons.beanutils.DynaBean#set(java.lang.String, java.lang.String, java.lang.Object)
     */
    public void set(String name, String key, Object value) {
        configuration.setProperty(name+"."+key, value);
    }

}
