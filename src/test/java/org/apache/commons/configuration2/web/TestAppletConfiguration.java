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

import static org.junit.Assert.fail;

import java.applet.Applet;
import java.util.Properties;

import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.TestAbstractConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for the {@link AppletConfiguration} class.
 *
 * @author Emmanuel Bourg
 */
public class TestAppletConfiguration extends TestAbstractConfiguration
{
    /** A flag whether tests with an applet can be run. */
    boolean supportsApplet;

    /**
     * Initializes the tests. This implementation checks whether an applet can
     * be used. Some environments, which do not support a GUI, don't allow
     * creating an <code>Applet</code> instance. If we are in such an
     * environment, some tests need to behave differently or be completely
     * dropped.
     */
    @Before
    public void setUp() throws Exception
    {
        try
        {
            new Applet();
            supportsApplet = true;
        }
        catch (final Exception ex)
        {
            // cannot use applets
            supportsApplet = false;
        }
    }

    @Override
    protected AbstractConfiguration getConfiguration()
    {
        AbstractConfiguration config;
        final Properties parameters = new Properties();
        parameters.setProperty("key1", "value1");
        parameters.setProperty("key2", "value2");
        parameters.setProperty("list", "value1, value2");
        parameters.setProperty("listesc", "value1\\,value2");

        if (supportsApplet)
        {
            final Applet applet = new Applet()
            {
                /**
                 * Serial version UID.
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public String getParameter(final String key)
                {
                    return parameters.getProperty(key);
                }

                @Override
                public String[][] getParameterInfo()
                {
                    return new String[][]
                    {
                    { "key1", "String", "" },
                    { "key2", "String", "" },
                    { "list", "String[]", "" },
                    { "listesc", "String", "" } };
                }
            };

            config = new AppletConfiguration(applet);
        }
        else
        {
            config = new MapConfiguration(parameters);
        }

        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        return config;
    }

    @Override
    protected AbstractConfiguration getEmptyConfiguration()
    {
        if (supportsApplet)
        {
            return new AppletConfiguration(new Applet());
        }
        return new BaseConfiguration();
    }

    @Override
    @Test
    public void testAddPropertyDirect()
    {
        if (supportsApplet)
        {
            try
            {
                super.testAddPropertyDirect();
                fail("addPropertyDirect should throw an UnsupportedException");
            }
            catch (final UnsupportedOperationException e)
            {
                // ok
            }
        }
    }

    @Override
    @Test
    public void testClearProperty()
    {
        if (supportsApplet)
        {
            try
            {
                super.testClearProperty();
                fail("testClearProperty should throw an UnsupportedException");
            }
            catch (final UnsupportedOperationException e)
            {
                // ok
            }
        }
    }
}
