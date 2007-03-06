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

package org.apache.commons.configuration.web;

import java.util.Enumeration;
import java.util.Properties;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import com.mockobjects.servlet.MockServletConfig;
import com.mockobjects.servlet.MockServletContext;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.TestAbstractConfiguration;

/**
 * Test case for the {@link ServletContextConfiguration} class.
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class TestServletContextConfiguration extends TestAbstractConfiguration
{
    protected AbstractConfiguration getConfiguration()
    {
        final Properties parameters = new Properties();
        parameters.setProperty("key1", "value1");
        parameters.setProperty("key2", "value2");
        parameters.setProperty("list", "value1, value2");
        parameters.setProperty("listesc", "value1\\,value2");

        // create a servlet context
        ServletContext context = new MockServletContext()
        {
            public String getInitParameter(String key)
            {
                return parameters.getProperty(key);
            }

            public Enumeration getInitParameterNames()
            {
                return parameters.keys();
            }
        };

        // create a servlet config
        final MockServletConfig config = new MockServletConfig();
        config.setServletContext(context);

        // create a servlet
        Servlet servlet = new HttpServlet()
        {
            public ServletConfig getServletConfig()
            {
                return config;
            }
        };

        return new ServletContextConfiguration(servlet);
    }

    protected AbstractConfiguration getEmptyConfiguration()
    {
        // create a servlet context
        ServletContext context = new MockServletContext()
        {
            public Enumeration getInitParameterNames()
            {
                return new Properties().keys();
            }
        };

        return new ServletContextConfiguration(context);
    }

    public void testAddPropertyDirect()
    {
        try
        {
            super.testAddPropertyDirect();
            fail("addPropertyDirect should throw an UnsupportedException");
        }
        catch (UnsupportedOperationException e)
        {
            // ok
        }
    }

    public void testClearProperty()
    {
        try
        {
            super.testClearProperty();
            fail("testClearProperty should throw an UnsupportedException");
        }
        catch (UnsupportedOperationException e)
        {
            // ok
        }
    }

}
