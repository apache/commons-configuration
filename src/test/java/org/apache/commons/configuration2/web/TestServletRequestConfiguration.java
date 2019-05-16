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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationMap;
import org.apache.commons.configuration2.TestAbstractConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.junit.Test;

import com.mockobjects.servlet.MockHttpServletRequest;

/**
 * Test case for the {@link ServletRequestConfiguration} class.
 *
 * @author Emmanuel Bourg
 */
public class TestServletRequestConfiguration extends TestAbstractConfiguration
{
    @Override
    protected AbstractConfiguration getConfiguration()
    {
        final Configuration configuration = new BaseConfiguration();
        configuration.setProperty("key1", "value1");
        configuration.setProperty("key2", "value2");
        configuration.addProperty("list", "value1");
        configuration.addProperty("list", "value2");
        configuration.addProperty("listesc", "value1\\,value2");

        return createConfiguration(configuration);
    }

    @Override
    protected AbstractConfiguration getEmptyConfiguration()
    {
        final ServletRequest request = new MockHttpServletRequest()
        {
            @Override
            public String getParameter(final String key)
            {
                return null;
            }

            @Override
            public Map<?, ?> getParameterMap()
            {
                return new HashMap<>();
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
    private ServletRequestConfiguration createConfiguration(final Configuration base)
    {
        final ServletRequest request = new MockHttpServletRequest()
        {
            @Override
            public String[] getParameterValues(final String key)
            {
                return base.getStringArray(key);
            }

            @Override
            public Map<?, ?> getParameterMap()
            {
                return new ConfigurationMap(base);
            }
        };

        final ServletRequestConfiguration config = new ServletRequestConfiguration(request);
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        return config;
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

    /**
     * Tests a list with elements that contain an escaped list delimiter.
     */
    @Test
    public void testListWithEscapedElements()
    {
        final String[] values = { "test1", "test2\\,test3", "test4\\,test5" };
        final String listKey = "test.list";

        final BaseConfiguration config = new BaseConfiguration();
        config.addProperty(listKey, values);

        assertEquals("Wrong number of list elements", values.length, config.getList(listKey).size());

        final Configuration c = createConfiguration(config);
        final List<?> v = c.getList(listKey);

        assertEquals("Wrong number of elements in list", values.length, v.size());

        for (int i = 0; i < values.length; i++)
        {
            assertEquals("Wrong value at index " + i, values[i].replaceAll("\\\\", ""), v.get(i));
        }
    }
}
