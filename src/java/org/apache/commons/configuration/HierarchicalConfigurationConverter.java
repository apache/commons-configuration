package org.apache.commons.configuration;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * <p>A base class for converters that transform a normal configuration
 * object into a hierarchical configuration.</p>
 * <p>This class provides a default mechanism for iterating over the keys in a
 * configuration and to throw corresponding element start and end events. By
 * handling these events a hierarchy can be constructed that is equivalent to
 * the keys in the original configuration.</p>
 * <p>Concrete sub classes will implement event handlers that generate SAX
 * events for XML processing or construct a
 * <code>HierarchicalConfiguration</code> root node. All in all with this class
 * it is possible to treat a default configuration as if it was a hierarchical
 * configuration, which can be sometimes useful.</p>
 * @see HierarchicalConfiguration 
 *
 * @version $Id: HierarchicalConfigurationConverter.java,v 1.2 2004/02/27 17:41:35 epugh Exp $
 */
abstract class HierarchicalConfigurationConverter
{
    /**
     * Processes the specified configuration object. This method implements
     * the iteration over the configuration's keys. All defined keys are
     * translated into a set of element start and end events represented by
     * calls to the <code>elementStart()</code> and
     * <code>elementEnd()</code> methods.
     * @param config the configuration to be processed
     */
    public void process(Configuration config)
    {
        if (config != null)
        {
            ConfigurationKey keyEmpty = new ConfigurationKey();
            ConfigurationKey keyLast = keyEmpty;

            for (Iterator it = config.getKeys(); it.hasNext();)
            {
                String key = (String) it.next();
                ConfigurationKey keyAct = new ConfigurationKey(key);
                closeElements(keyLast, keyAct);
                String elem = openElements(keyLast, keyAct);
                fireValue(elem, config.getProperty(key));
                keyLast = keyAct;
            } /* for */

            closeElements(keyLast, keyEmpty); // close all open
        }
    }

    /**
     * An event handler method that is called when an element starts.
     * Concrete sub classes must implement it to perform a proper event
     * handling. 
     * @param name the name of the new element
     * @param value the element's value; can be <b>null</b> if the element
     * does not have any value
     */
    protected abstract void elementStart(String name, Object value);

    /**
     * An event handler method that is called when an element ends. For each
     * call of <code>elementStart()</code> there will be a corresponding call
     * of this method. Concrete sub classes must implement it to perform a
     * proper event handling.
     * @param name the name of the ending element
     */
    protected abstract void elementEnd(String name);

    /**
     * Fires all necessary element end events for the specified keys. This
     * method is called for each key obtained from the configuration to be
     * converted. It calculates the common part of the actual and the last
     * processed key and thus determines how many elements must be
     * closed.
     * @param keyLast the last processed key
     * @param keyAct the actual key
     */
    protected void closeElements(
        ConfigurationKey keyLast,
        ConfigurationKey keyAct)
    {
        ConfigurationKey keyDiff = keyAct.differenceKey(keyLast);
        Iterator it = reverseIterator(keyDiff);
        if (it.hasNext())
        {
            // Skip first because it has already been closed by fireValue()
            it.next();
        } /* if */

        while (it.hasNext())
        {
            elementEnd((String) it.next());
        } /* while */
    }

    /**
     * Helper method for determining a reverse iterator for the specified key.
     * This implementation returns an iterator that returns the parts of the
     * given key in reverse order, ignoring indices.
     * @param key the key
     * @return a reverse iterator for the parts of this key
     */
    protected Iterator reverseIterator(ConfigurationKey key)
    {
        List list = new ArrayList();
        for (ConfigurationKey.KeyIterator it = key.iterator(); it.hasNext();)
        {
            list.add(it.nextKey());
        } /* for */

        Collections.reverse(list);
        return list.iterator();
    }

    /**
     * Fires all necessary element start events for the specified key. This
     * method is called for each key obtained from the configuration to be
     * converted. It ensures that all elements "between" the last key and the
     * actual key are opened.
     * @param keyLast the last processed key
     * @param keyAct the actual key
     * @return the name of the last element on the path
     */
    protected String openElements(
        ConfigurationKey keyLast,
        ConfigurationKey keyAct)
    {
        ConfigurationKey.KeyIterator it =
            keyLast.differenceKey(keyAct).iterator();

        for (it.nextKey(); it.hasNext(); it.nextKey())
        {
            elementStart(it.currentKey(), null);
        } /* for */

        return it.currentKey();
    }

    /**
     * Fires all necessary element start events with the actual element values.
     * This method is called for each key obtained from the configuration to be
     * processed with the last part of the key as argument. The value can be
     * either a single value or a collection.
     * @param name the name of the actual element
     * @param value the element's value
     */
    protected void fireValue(String name, Object value)
    {
        if (value != null && value instanceof Collection)
        {
            for (Iterator it = ((Collection) value).iterator(); it.hasNext();)
            {
                fireValue(name, it.next());
            } /* for */
        } /* if */

        else
        {
            elementStart(name, value);
            elementEnd(name);
        } /* else */
    }
}
