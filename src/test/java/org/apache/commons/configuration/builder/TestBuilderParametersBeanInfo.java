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
package org.apache.commons.configuration.builder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Test class for {@code BuilderParametersBeanInfo}.
 *
 * @version $Id$
 */
public class TestBuilderParametersBeanInfo
{
    /**
     * Tests whether additional BeanInfo for all properties is available.
     */
    @Test
    public void testFindAllProperties() throws IntrospectionException
    {
        BeanInfo info = Introspector.getBeanInfo(ParametersBeanTestImpl.class);
        Map<String, PropertyDescriptor> properties =
                new HashMap<String, PropertyDescriptor>();
        for (PropertyDescriptor pd : info.getPropertyDescriptors())
        {
            properties.put(pd.getName(), pd);
        }
        PropertyDescriptor pd = fetchDescriptor(properties, "intProperty");
        assertNotNull("No read method", pd.getReadMethod());
        pd = fetchDescriptor(properties, "logger");
        assertNull("Got a read method for logger", pd.getReadMethod());
        fetchDescriptor(properties, "throwExceptionOnMissing");
        pd = fetchDescriptor(properties, "fluentProperty");
        assertNull("Got a read method for fluentProperty", pd.getReadMethod());
        pd = fetchDescriptor(properties, "fluentPropertyWithGet");
        assertNotNull("No read method for fluent property", pd.getReadMethod());
        assertNotNull("No write method for fluent property", pd.getWriteMethod());
    }

    /**
     * Returns the descriptor for the property with the given name from the map
     * or fails if property is unknown.
     *
     * @param properties the map with property descriptors
     * @param name the name of the property
     * @return the descriptor for this property
     */
    private static PropertyDescriptor fetchDescriptor(
            Map<String, PropertyDescriptor> properties, String name)
    {
        assertTrue("Property not found: " + name, properties.containsKey(name));
        return properties.get(name);
    }
}
