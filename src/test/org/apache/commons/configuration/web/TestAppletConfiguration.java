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

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.TestAbstractConfiguration;

import java.applet.Applet;
import java.util.Properties;

/**
 * Test case for the {@link AppletConfiguration} class.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 1.1 $, $Date: 2004/10/14 09:54:35 $
 */
public class TestAppletConfiguration extends TestAbstractConfiguration
{
    protected AbstractConfiguration getConfiguration()
    {
        final Properties parameters = new Properties();
        parameters.setProperty("key1", "value1");
        parameters.setProperty("key2", "value2");

        Applet applet = new Applet()
        {
            public String getParameter(String key)
            {
                return parameters.getProperty(key);
            }

            public String[][] getParameterInfo()
            {
                return new String[][]
                {
                    {"key1", "String", ""},
                    {"key2", "String", ""}
                };
            }
        };

        return new AppletConfiguration(applet);
    }

    protected AbstractConfiguration getEmptyConfiguration()
    {
        return new AppletConfiguration(new Applet());
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
