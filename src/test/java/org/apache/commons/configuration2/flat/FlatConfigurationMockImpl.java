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
package org.apache.commons.configuration2.flat;

import java.util.Iterator;

/**
 * A mock implementation of a flat configuration. This mock class is used
 * for testing whether properties are correctly accessed. Most methods are
 * simply dummy implementations.
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
class FlatConfigurationMockImpl extends AbstractFlatConfiguration
{
    /** Constant for the name of the test property. */
    public static final String NAME = "testFlatNode";

    /** Stores the value of the test property. */
    Object property;

    /** Stores the expected index. */
    int expectedIndex;

    /** A flag whether an add property operation is expected. */
    boolean expectAdd;

    /** A flag whether clearProperty() was called. */
    boolean clearProperty;

    @Override
    public boolean clearPropertyValueDirect(String key, int index)
    {
        TestFlatNodes.assertEquals("Wrong property key", NAME, key);
        clearProperty = true;
        expectedIndex = index;
        return true;
    }

    @Override
    public void clearPropertyDirect(String key)
    {
        clearPropertyValue(key, FlatNode.INDEX_UNDEFINED);
    }

    @Override
    public void setPropertyValueDirect(String key, int index, Object value)
    {
        TestFlatNodes.assertFalse("Add operation expected", expectAdd);
        TestFlatNodes.assertEquals("Wrong property key", NAME, key);
        TestFlatNodes.assertEquals("Wrong index", expectedIndex, index);
        property = value;
    }

    @Override
    public void setProperty(String key, Object value)
    {
        setPropertyValue(key, FlatNode.INDEX_UNDEFINED, value);
    }

    @Override
    protected void addPropertyDirect(String key, Object value)
    {
        TestFlatNodes.assertTrue("Set operation expected", expectAdd);
        TestFlatNodes.assertEquals("Wrong property key", NAME, key);
        property = value;
    }

    public boolean containsKey(String key)
    {
        return false;
    }

    public Iterator<String> getKeys()
    {
        return null;
    }

    public Object getProperty(String key)
    {
        TestFlatNodes.assertEquals("Wrong property key", NAME, key);
        return property;
    }

    public boolean isEmpty()
    {
        return false;
    }
}
