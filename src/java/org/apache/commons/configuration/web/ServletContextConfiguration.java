/*
 * Copyright 2004 The Apache Software Foundation.
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

package org.apache.commons.configuration.web;

import java.util.Iterator;
import java.util.List;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.PropertyConverter;

/**
 * A configuration wrapper to read the initialization parameters of a servlet
 * context. This configuration is read only, adding or removing a property will
 * throw an UnsupportedOperationException.
 *
 * @author <a href="mailto:ebourg@apache.org">Emmanuel Bourg</a>
 * @version $Revision: 1.4 $, $Date$
 * @since 1.1
 */
public class ServletContextConfiguration extends AbstractConfiguration
{
    protected ServletContext context;

    /**
     * Create a ServletContextConfiguration using the context of
     * the specified servlet.
     *
     * @param servlet the servlet
     */
    public ServletContextConfiguration(Servlet servlet)
    {
        this.context = servlet.getServletConfig().getServletContext();
    }

    /**
     * Create a ServletContextConfiguration using the servlet context
     * initialization parameters.
     *
     * @param context the servlet context
     */
    public ServletContextConfiguration(ServletContext context)
    {
        this.context = context;
    }

    public Object getProperty(String key)
    {
        Object value = context.getInitParameter(key);
        List list = PropertyConverter.split((String) value, getDelimiter());

        return list.size() > 1 ? list : value;
    }

    /**
     * <p><strong>This operation is not supported and will throw an
     * UnsupportedOperationException.</strong></p>
     *
     * @throws UnsupportedOperationException
     */
    protected void addPropertyDirect(String key, Object obj)
    {
        throw new UnsupportedOperationException("Read only configuration");
    }

    public boolean isEmpty()
    {
        return !getKeys().hasNext();
    }

    public boolean containsKey(String key)
    {
        return getProperty(key) != null;
    }

    /**
     * <p><strong>This operation is not supported and will throw an
     * UnsupportedOperationException.</strong></p>
     *
     * @throws UnsupportedOperationException
     */
    public void clearProperty(String key)
    {
        throw new UnsupportedOperationException("Read only configuration");
    }

    public Iterator getKeys()
    {
        return new EnumerationIterator(context.getInitParameterNames());
    }
}
