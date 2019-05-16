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

package org.apache.commons.configuration2.web;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

/**
 * A configuration wrapper to read the initialization parameters of a servlet
 * context. This configuration is read only, adding or removing a property will
 * throw an UnsupportedOperationException.
 *
 * @author <a href="mailto:ebourg@apache.org">Emmanuel Bourg</a>
 * @since 1.1
 */
public class ServletContextConfiguration extends BaseWebConfiguration
{
    /** Stores the wrapped servlet context.*/
    protected ServletContext context;

    /**
     * Create a ServletContextConfiguration using the context of
     * the specified servlet.
     *
     * @param servlet the servlet
     */
    public ServletContextConfiguration(final Servlet servlet)
    {
        this.context = servlet.getServletConfig().getServletContext();
    }

    /**
     * Create a ServletContextConfiguration using the servlet context
     * initialization parameters.
     *
     * @param context the servlet context
     */
    public ServletContextConfiguration(final ServletContext context)
    {
        this.context = context;
    }

    @Override
    protected Object getPropertyInternal(final String key)
    {
        return handleDelimiters(context.getInitParameter(key));
    }

    @Override
    protected Iterator<String> getKeysInternal()
    {
        // According to the documentation of getInitParameterNames() the
        // enumeration is of type String.
        final Enumeration<String> en = context.getInitParameterNames();
        return Collections.list(en).iterator();
    }
}
