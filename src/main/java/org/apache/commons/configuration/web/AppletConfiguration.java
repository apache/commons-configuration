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

import java.applet.Applet;
import java.util.Iterator;

import org.apache.commons.collections.iterators.ArrayIterator;

/**
 * A configuration wrapper to read applet parameters. This configuration is
 * read only, adding or removing a property will throw an
 * UnsupportedOperationException.
 *
 * @author <a href="mailto:ebourg@apache.org">Emmanuel Bourg</a>
 * @version $Revision$, $Date$
 * @since 1.1
 */
public class AppletConfiguration extends BaseWebConfiguration
{
    /** Stores the wrapped applet.*/
    protected Applet applet;

    /**
     * Create an AppletConfiguration using the initialization parameters of
     * the specified Applet.
     *
     * @param applet the applet
     */
    public AppletConfiguration(Applet applet)
    {
        this.applet = applet;
    }

    public Object getProperty(String key)
    {
        return handleDelimiters(applet.getParameter(key));
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
