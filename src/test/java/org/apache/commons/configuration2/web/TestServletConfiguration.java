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

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.TestAbstractConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.junit.Test;

import com.mockobjects.servlet.MockServletConfig;

/**
 * Test case for the {@link ServletConfiguration} class.
 *
 * @author Emmanuel Bourg
 */
public class TestServletConfiguration extends TestAbstractConfiguration
{
    @Override
    protected AbstractConfiguration getConfiguration()
    {
        final MockServletConfig config = new MockServletConfig();
        config.setInitParameter("key1", "value1");
        config.setInitParameter("key2", "value2");
        config.setInitParameter("list", "value1, value2");
        config.setInitParameter("listesc", "value1\\,value2");

        final Servlet servlet = new HttpServlet() {
            /**
             * Serial version UID.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public ServletConfig getServletConfig()
            {
                return config;
            }
        };

        final ServletConfiguration servletConfiguration = new ServletConfiguration(servlet);
        servletConfiguration.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        return servletConfiguration;
    }

    @Override
    protected AbstractConfiguration getEmptyConfiguration()
    {
        return new ServletConfiguration(new MockServletConfig());
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testAddPropertyDirect()
    {
        super.testAddPropertyDirect();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testClearProperty()
    {
       super.testClearProperty();
    }
}
