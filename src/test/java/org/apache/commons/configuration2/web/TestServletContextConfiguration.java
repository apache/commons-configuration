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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.TestAbstractConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

/**
 * Test case for the {@link ServletContextConfiguration} class.
 */
public class TestServletContextConfiguration extends TestAbstractConfiguration {

    @Override
    protected AbstractConfiguration getConfiguration() {
        final Properties parameters = new Properties();
        parameters.setProperty("key1", "value1");
        parameters.setProperty("key2", "value2");
        parameters.setProperty("list", "value1, value2");
        parameters.setProperty("listesc", "value1\\,value2");

        // create a servlet context
        final ServletContext context = mockServletConfig(parameters);

        // create a servlet config
        final ServletConfig config = mock(ServletConfig.class);
        when(config.getServletContext()).thenReturn(context);

        // create a servlet
        final Servlet servlet = new HttpServlet() {
            /**
             * Serial version UID.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public ServletConfig getServletConfig() {
                return config;
            }
        };

        final ServletContextConfiguration resultConfig = new ServletContextConfiguration(servlet);
        resultConfig.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        return resultConfig;
    }

    @Override
    protected AbstractConfiguration getEmptyConfiguration() {
        // create a servlet context
        final ServletContext context = mockServletConfig(new Properties());

        return new ServletContextConfiguration(context);
    }

    /**
     * Creates a mocked {@link ServletConfig}.
     *
     * @param parameters the init parameters to use
     * @return The created mock
     */
    private ServletContext mockServletConfig(final Properties parameters) {
        final ServletContext context = mock(ServletContext.class);
        when(context.getInitParameterNames()).thenAnswer(invocation -> parameters.keys());
        when(context.getInitParameter(ArgumentMatchers.any())).thenAnswer(invocation -> {
            final String name = invocation.getArgument(0, String.class);
            return parameters.getProperty(name);
        });
        return context;
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
}
