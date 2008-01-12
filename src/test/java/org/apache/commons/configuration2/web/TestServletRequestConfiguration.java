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

import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletRequest;

import com.mockobjects.servlet.MockHttpServletRequest;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.TestAbstractConfiguration;
import org.apache.commons.configuration2.web.ServletRequestConfiguration;
import org.apache.commons.lang.StringUtils;

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
        ((BaseConfiguration) configuration).setListDelimiter('\0');
        configuration.setProperty("key1", "value1");
        configuration.setProperty("key2", "value2");
        configuration.addProperty("list", "value1");
        configuration.addProperty("list", "value2");
        configuration.addProperty("listesc", "value1\\,value2");

        return createConfiguration(configuration);
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

    /**
     * Returns a new servlet request configuration that is backed by the passed
     * in configuration.
     *
     * @param base the configuration with the underlying values
     * @return the servlet request configuration
     */
    private ServletRequestConfiguration createConfiguration(
            final Configuration base)
    {
        ServletRequest request = new MockHttpServletRequest()
        {
            public String[] getParameterValues(String key)
            {
                return base.getStringArray(key);
            }

            public Enumeration getParameterNames()
            {
                return new IteratorEnumeration(base.getKeys());
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

    /**
     * Tests a list with elements that contain an escaped list delimiter.
     */
    public void testListWithEscapedElements()
    {
        String[] values =
        { "test1", "test2\\,test3", "test4\\,test5" };
        final String listKey = "test.list";
        BaseConfiguration config = new BaseConfiguration();
        config.setListDelimiter('\0');
        config.addProperty(listKey, values);
        assertEquals("Wrong number of list elements", values.length, config
                .getList(listKey).size());
        Configuration c = createConfiguration(config);
        List v = c.getList(listKey);
        assertEquals("Wrong number of elements in list", values.length, v
                .size());
        for (int i = 0; i < values.length; i++)
        {
            assertEquals("Wrong value at index " + i, StringUtils.replace(
                    values[i], "\\", StringUtils.EMPTY), v.get(i));
        }
    }
}
