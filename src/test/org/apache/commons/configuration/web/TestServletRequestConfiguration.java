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

import com.mockobjects.servlet.MockHttpServletRequest;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.TestAbstractConfiguration;

import javax.servlet.ServletRequest;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Test case for the {@link ServletRequestConfiguration} class.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 1.1 $, $Date: 2004/10/14 09:54:35 $
 */
public class TestServletRequestConfiguration extends TestAbstractConfiguration
{
    protected AbstractConfiguration getConfiguration()
    {
        final Properties parameters = new Properties();
        parameters.setProperty("key1", "value1");
        parameters.setProperty("key2", "value2");

        ServletRequest request = new MockHttpServletRequest()
        {
            public String getParameter(String key)
            {
                return parameters.getProperty(key);
            }

            public Enumeration getParameterNames()
            {
                return parameters.keys();
            }
        };

        return new ServletRequestConfiguration(request);
    }

    protected AbstractConfiguration getEmptyConfiguration()
    {
        final Properties parameters = new Properties();

        ServletRequest request = new MockHttpServletRequest()
        {
            public String getParameter(String key)
            {
                return null;
            }

            public Enumeration getParameterNames()
            {
                return parameters.keys();
            }
        };

        return new ServletRequestConfiguration(request);
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
