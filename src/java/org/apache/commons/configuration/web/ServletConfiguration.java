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
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.configuration.AbstractConfiguration;

/**
 * A configuration wrapper around a {@link ServletConfig}. This configuration
 * is read only, adding or removing a property will throw an
 * UnsupportedOperationException.
 *
 * @author <a href="mailto:ebourg@apache.org">Emmanuel Bourg</a>
 * @version $Revision: 1.3 $, $Date: 2004/10/21 18:42:09 $
 * @since 1.1
 */
public class ServletConfiguration extends AbstractConfiguration
{
    protected ServletConfig config;

    /**
     * Create a ServletConfiguration using the initialization parameter of
     * the specified servlet.
     *
     * @param servlet the servlet
     */
    public ServletConfiguration(Servlet servlet)
    {
        this(servlet.getServletConfig());
    }

    /**
     * Create a ServletConfiguration using the servlet initialization parameters.
     *
     * @param config the servlet configuration
     */
    public ServletConfiguration(ServletConfig config)
    {
        this.config = config;
    }

    protected Object getPropertyDirect(String key)
    {
        return config.getInitParameter(key);
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
        return getPropertyDirect(key) != null;
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
        return new EnumerationIterator(config.getInitParameterNames());
    }

}
