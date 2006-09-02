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

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.TestAbstractConfiguration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Test case for the {@link ServletFilterConfiguration} class.
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class TestServletFilterConfiguration extends TestAbstractConfiguration
{
    protected AbstractConfiguration getConfiguration()
    {
        MockFilterConfig config = new MockFilterConfig();
        config.setInitParameter("key1", "value1");
        config.setInitParameter("key2", "value2");
        config.setInitParameter("list", "value1, value2");

        return new ServletFilterConfiguration(config);
    }

    protected AbstractConfiguration getEmptyConfiguration()
    {
        return new ServletFilterConfiguration(new MockFilterConfig());
    }

    private class MockFilterConfig implements FilterConfig
    {
        private Properties parameters = new Properties();

        public String getFilterName()
        {
            return null;
        }

        public ServletContext getServletContext()
        {
            return null;
        }

        public String getInitParameter(String key)
        {
            return parameters.getProperty(key);
        }

        public Enumeration getInitParameterNames()
        {
            return parameters.keys();
        }

        public void setInitParameter(String key, String value)
        {
            parameters.setProperty(key, value);
        }
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
