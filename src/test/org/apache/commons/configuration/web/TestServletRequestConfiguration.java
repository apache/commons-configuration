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

import java.util.Enumeration;
import javax.servlet.ServletRequest;

import com.mockobjects.servlet.MockHttpServletRequest;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.TestAbstractConfiguration;

/**
 * Test case for the {@link ServletRequestConfiguration} class.
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class TestServletRequestConfiguration extends TestAbstractConfiguration
{
    protected AbstractConfiguration getConfiguration()
    {
        final Configuration configuration = new BaseConfiguration();
        configuration.setProperty("key1", "value1");
        configuration.setProperty("key2", "value2");
        configuration.addProperty("list", "value1");
        configuration.addProperty("list", "value2");

        ServletRequest request = new MockHttpServletRequest()
        {
            public String[] getParameterValues(String key)
            {
                return configuration.getStringArray(key);
            }

            public Enumeration getParameterNames()
            {
                return new IteratorEnumeration(configuration.getKeys());
            }
        };

        return new ServletRequestConfiguration(request);
    }

    protected AbstractConfiguration getEmptyConfiguration()
    {
        final Configuration configuration = new BaseConfiguration();

        ServletRequest request = new MockHttpServletRequest()
        {
            public String getParameter(String key)
            {
                return null;
            }

            public Enumeration getParameterNames()
            {
                return new IteratorEnumeration(configuration.getKeys());
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
