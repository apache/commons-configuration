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

import java.applet.Applet;
import java.util.Iterator;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.commons.configuration.AbstractConfiguration;

/**
 * A configuration wrapper to read applet parameters. This configuration is
 * read only, adding or removing a property will throw an
 * UnsupportedOperationException.
 *
 * @author <a href="mailto:ebourg@apache.org">Emmanuel Bourg</a>
 * @version $Revision: 1.2 $, $Date: 2004/10/14 15:54:06 $
 * @since 1.1
 */
public class AppletConfiguration extends AbstractConfiguration
{
    protected Applet applet;

    /**
     * Create an AppletConfiguration using the initialization parameters of
     * the specified Applet.
     *
     * @param applet
     */
    public AppletConfiguration(Applet applet)
    {
        this.applet = applet;
    }

    protected Object getPropertyDirect(String key)
    {
        return applet.getParameter(key);
    }

    protected void addPropertyDirect(String key, Object obj)
    {
        throw new UnsupportedOperationException("Read only configuration");
    }

    public boolean isEmpty()
    {
        return !getKeys().hasNext();
    }

    public boolean containsKey(String key)
    {
        return getPropertyDirect(key) != null;
    }

    public void clearProperty(String key)
    {
        throw new UnsupportedOperationException("Read only configuration");
    }

    public Iterator getKeys()
    {
        String[][] paramsInfo = applet.getParameterInfo();
        String[] keys = new String[paramsInfo != null ? paramsInfo.length : 0];
        for (int i = 0; i < keys.length; i++)
        {
            keys[i] = paramsInfo[i][0];
        }

        return new ArrayIterator(keys);
    }
}
