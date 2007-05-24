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

package org.apache.commons.configuration;

import java.util.Iterator;

import junit.framework.Assert;

/**
 * Assertions on configurations for the unit tests.
 * 
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class ConfigurationAssert
{
    public static void assertEquals(Configuration expected, Configuration actual)
    {
        // check that the actual configuration contains all the properties of the expected configuration
        for (Iterator it = expected.getKeys(); it.hasNext();)
        {
            String key = (String) it.next();
            Assert.assertTrue("The actual configuration doesn't contain the expected key '" + key + "'", actual.containsKey(key));
            Assert.assertEquals("Value of the '" + key + "' property", expected.getProperty(key), actual.getProperty(key));
        }

        // check that the actual configuration has no extra properties
        for (Iterator it = actual.getKeys(); it.hasNext();)
        {
            String key = (String) it.next();
            Assert.assertTrue("The actual configuration contains an extra key '" + key + "'", expected.containsKey(key));
        }
    }
}
