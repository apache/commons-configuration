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
 * @author <a href="mailto:oliver.heger@t-online.de">Oliver Heger</a>
 * @version $Id: HierarchicalConfigurationConverter.java,v 1.1 2003/12/23 15:09:05 epugh Exp $
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
