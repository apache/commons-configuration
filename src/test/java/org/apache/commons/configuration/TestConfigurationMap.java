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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author <a href="mailto:ricardo.gladwell@btinternet.com">Ricardo Gladwell</a>
 */
public class TestConfigurationMap extends TestCase
{

    ConfigurationMap map;

    String[] properties = {
            "booleanProperty",
            "doubleProperty",
            "floatProperty",
            "intProperty",
            "longProperty",
            "shortProperty",
            "stringProperty"
    };
    
    Object[] values = {
            Boolean.TRUE,
            new Double(Double.MAX_VALUE),
            new Float(Float.MAX_VALUE),
            new Integer(Integer.MAX_VALUE),
            new Long(Long.MAX_VALUE),
            new Short(Short.MAX_VALUE),
            "This is a string"
    };

    /**
     * Construct a new instance of this test case.
     * @param name Name of the test case
     */
    public TestConfigurationMap(String name)
    {
        super(name);
    }

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception
    {
        BaseConfiguration configuration = new BaseConfiguration();
        for(int i = 0; i < properties.length ; i++)
            configuration.setProperty(properties[i], values[i]);
        map = new ConfigurationMap(configuration);
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite()
    {
        return (new TestSuite(TestConfigurationMap.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown()
    {
        map = null;
    }

    /**
     * Class under test for Object put(Object, Object)
     */
    public void testPut()
    {
        for(int i = 0; i < properties.length; i++) {
            Object object = map.put(properties[i], values[i]);
            assertNotNull("Returned null from put.",object);
            assertEquals("Returned wrong result.",values[i],object);
            object = map.get(properties[i]);
            assertNotNull("Returned null from get.",object);
            assertEquals("Returned wrong result.",values[i],object);
        }
    }

}
