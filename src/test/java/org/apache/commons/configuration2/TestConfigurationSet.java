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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author rgladwel
 */
public class TestConfigurationSet {

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
     * Set up instance variables required by this test case.
     */
    @Before
    public void setUp() throws Exception
    {
        final BaseConfiguration configuration = new BaseConfiguration();
        for(int i = 0; i < properties.length ; i++) {
            configuration.setProperty(properties[i], values[i]);
        }
        set = new ConfigurationMap.ConfigurationSet(configuration);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @After
    public void tearDown()
    {
        set = null;
    }

    @Test
    public void testSize() {
        assertEquals("Entry set does not match properties size.", properties.length, set.size());
    }

    /**
     * Class under test for Iterator iterator()
     */
    @Test
    public void testIterator() {
        final Iterator<Map.Entry<Object, Object>> iterator = set.iterator();
        while(iterator.hasNext()) {
            final Map.Entry<Object, Object> entry = iterator.next();
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
