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
package org.apache.commons.configuration2;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration2.ConfigurationMap;
import org.apache.commons.configuration2.flat.BaseConfiguration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author rgladwel
 */
public class TestConfigurationSet extends TestCase {

    ConfigurationMap.ConfigurationSet set;

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
    public TestConfigurationSet(String name)
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
        set = new ConfigurationMap.ConfigurationSet(configuration);
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite()
    {
        return (new TestSuite(TestConfigurationSet.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown()
    {
        set = null;
    }

    public void testSize() {
        assertEquals("Entry set does not match properties size.", properties.length, set.size());
    }

    /**
     * Class under test for Iterator iterator()
     */
    public void testIterator() {
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Object object = iterator.next();
            assertTrue("Entry set iterator did not return EntrySet object, returned "
                    + object.getClass().getName(), object instanceof Map.Entry);
            Map.Entry entry = (Map.Entry) object;
            boolean found = false;
            for(int i = 0; i < properties.length; i++) {
                if(entry.getKey().equals(properties[i])) {
                    assertEquals("Incorrect value for property " +
                            properties[i],values[i],entry.getValue());
                    found = true;
                }
            }
            assertTrue("Could not find property " + entry.getKey(),found);
            iterator.remove();
        }
        assertTrue("Iterator failed to remove all properties.",set.isEmpty());
    }

}
