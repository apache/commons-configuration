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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationMap;
import org.apache.commons.configuration2.TestAbstractConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

/**
 * Test case for the {@link ServletRequestConfiguration} class.
 */
public class TestServletRequestConfiguration extends TestAbstractConfiguration {
    /**
     * Returns a new servlet request configuration that is backed by the passed in configuration.
     *
     * @param base the configuration with the underlying values
     * @return the servlet request configuration
     */
    private ServletRequestConfiguration createConfiguration(final Configuration base) {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenAnswer(invocation -> new ConfigurationMap(base));
        when(request.getParameterValues(ArgumentMatchers.any())).thenAnswer(invocation -> {
            final String key = invocation.getArgument(0, String.class);
            return base.getStringArray(key);
        });

        final ServletRequestConfiguration config = new ServletRequestConfiguration(request);
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        return config;
    }

    @Override
    protected AbstractConfiguration getConfiguration() {
        final Configuration configuration = new BaseConfiguration();
        configuration.setProperty("key1", "value1");
        configuration.setProperty("key2", "value2");
        configuration.addProperty("list", "value1");
        configuration.addProperty("list", "value2");
        configuration.addProperty("listesc", "value1\\,value2");

        return createConfiguration(configuration);
    }

    @Override
    protected AbstractConfiguration getEmptyConfiguration() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(ArgumentMatchers.any())).thenReturn(null);
        when(request.getParameterMap()).thenAnswer(invocation -> new HashMap<>());

        return new ServletRequestConfiguration(request);
    }

    @Override
    @Test
    public void testAddPropertyDirect() {
        assertThrows(UnsupportedOperationException.class, super::testAddPropertyDirect);
    }

    @Override
    @Test
    public void testClearProperty() {
        assertThrows(UnsupportedOperationException.class, super::testClearProperty);
    }

    /**
     * Tests a list with elements that contain an escaped list delimiter.
     */
    @Test
    public void testListWithEscapedElements() {
        final String[] values = {"test1", "test2\\,test3", "test4\\,test5"};
        final String listKey = "test.list";

        final BaseConfiguration config = new BaseConfiguration();
        config.addProperty(listKey, values);

        assertEquals(values.length, config.getList(listKey).size());

        final Configuration c = createConfiguration(config);
        final List<?> v = c.getList(listKey);

        final List<String> expected = new ArrayList<>();
        for (final String value : values) {
            expected.add(value.replace("\\", ""));
        }
        assertEquals(expected, v);
    }
}
