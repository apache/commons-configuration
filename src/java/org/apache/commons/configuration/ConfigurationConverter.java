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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.List;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;


/**
 * Configuration converter. <br>
 * Helper class to convert between Configuration, ExtendedProperties and
 * standard Properties.
 *
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @version $Id: ConfigurationConverter.java,v 1.2 2003/12/24 14:28:22 epugh Exp $
 */
public class ConfigurationConverter
{
    /**
     * Convert a ExtendedProperties class into a Configuration class.
     *
     * @param ep ExtendedProperties object to convert
     * @return Configuration created from the ExtendedProperties
     */
    public static Configuration getConfiguration(ExtendedProperties ep)
    {
        Configuration config = (Configuration) new BaseConfiguration();
        for (Iterator i = ep.getKeys(); i.hasNext();)
        {
            String key = (String) i.next();
            config.setProperty(key, ep.getProperty(key));
        }
        return config;
    }

    /**
     * Convert a standard properties class into a configuration class.
     *
     * @param p properties object to convert
     * @return Configuration configuration created from the Properties
     */
    public static Configuration getConfiguration(Properties p)
    {
        Configuration config = (Configuration) new BaseConfiguration();
        for (Enumeration e = p.keys(); e.hasMoreElements();)
        {
            String key = (String) e.nextElement();
            config.setProperty(key, p.getProperty(key));
        }
        return config;
    }

    /**
     * Convert a Configuration class into a ExtendedProperties class.
     *
     * @param c Configuration object to convert
     * @return ExtendedProperties created from the Configuration
     */
    public static ExtendedProperties getExtendedProperties(Configuration c)
    {
        ExtendedProperties props = new ExtendedProperties();
        for (Iterator i = c.getKeys(); i.hasNext();)
        {
            String key = (String) i.next();
            Object property = c.getProperty(key);

            // turn lists into vectors
            if (property instanceof List)
            {
                property = new Vector((List) property);
            }

            props.setProperty(key, property);
        }
        return props;
    }

    /**
     * Convert a Configuration class into a Properties class. Multvalue keys
     * will be collapsed by {@link Configuration#getString}.
     *
     * @param c Configuration object to convert
     * @return Properties created from the Configuration
     */
    public static Properties getProperties(Configuration c)
    {
        Properties props = new Properties();

        Iterator iter = c.getKeys();

        while (iter.hasNext())
        {
            String key = (String) iter.next();
            props.setProperty(key, c.getString(key));
        }

        return props;
    }
}
