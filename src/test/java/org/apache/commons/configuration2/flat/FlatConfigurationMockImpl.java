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
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

/**
 * A mock implementation of a flat configuration. This mock class is used for
 * testing whether properties are correctly accessed. Most methods are simply
 * dummy implementations.
 *
 * @author <a href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
class FlatConfigurationMockImpl extends AbstractFlatConfiguration implements Cloneable
{
    /** Constant for the name of the test property. */
    public static final String NAME = "testFlatNode";

    /** A list with data for expected getProperty() invocations. */
    private List<ExpectedData<Object>> getPropertyInvocations;

    /** A list with data about expected getMaxIndex() invocations. */
    private List<ExpectedData<Integer>> getMaxIndexInvocations;

    /** A list with the keys. */
    List<String> keyList;

    /** Stores the value of the test property. */
    Object property;

    /** Stores the expected index. */
    int expectedIndex;

    /** A flag whether an add property operation is expected. */
    boolean expectAdd;

    /** A flag whether clearProperty() was called. */
    boolean clearProperty;

    /**
     * Creates a new instance of <code>FlatConfigurationMockImpl</code>.
     */
    public FlatConfigurationMockImpl()
    {
        getMaxIndexInvocations = new LinkedList<ExpectedData<Integer>>();
    }

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

    /**
     * Returns the keys stored in this configuration. This implementation
     * returns an iterator of the key list.
     *
     * @return the keys stored in this configuration
     */
    public Iterator<String> getKeys()
    {
        return keyList.iterator();
    }

    /**
     * Informs this object that a getProperty() call is expected.
     *
     * @param key the key
     * @param value the value to return
     */
    public void expectGetProperty(String key, Object value)
    {
        if (getPropertyInvocations == null)
        {
            getPropertyInvocations = new LinkedList<ExpectedData<Object>>();
        }
        getPropertyInvocations.add(new ExpectedData<Object>(key, value));
    }

    /**
     * Returns the value of a property. If expected getProperty() invocations
     * exist, this call must match the current element of this list. Otherwise
     * the test property is returned.
     */
    public Object getProperty(String key)
    {
        if (getPropertyInvocations != null)
        {
            if (getPropertyInvocations.isEmpty())
            {
                throw new IllegalStateException(
                        "No getProperty() invocation expected!");
            }
            ExpectedData<Object> ed = getPropertyInvocations.remove(0);
            return ed.checkAndReturn(key);
        }

        Assert.assertEquals("Wrong property key", NAME, key);
        return property;
    }

    /**
     * Informs this object that a getMaxIndex() call is expected.
     *
     * @param property the affected property
     * @param value its maximum index
     */
    public void expectGetMaxIndex(String property, int value)
    {
        getMaxIndexInvocations.add(new ExpectedData<Integer>(property, value));
    }

    /**
     * Returns the maximum index of the specified property. This value must have
     * been set before using <code>expectGetMaxIndex()</code>.
     *
     * @param key the key
     * @return the maximum value index
     */
    @Override
    public int getMaxIndex(String key)
    {
        if (getMaxIndexInvocations.isEmpty())
        {
            throw new IllegalStateException(
                    "No getMaxIndex() invocation expected!");
        }
        return getMaxIndexInvocations.remove(0).checkAndReturn(key);
    }

    public boolean isEmpty()
    {
        return false;
    }

    /**
     * A simple data class for storing information about expected method
     * invocations. Tests can tell an instance, which method invocations they
     * expect. This information is stored in instances of this class. Thus it is
     * available for later verification.
     *
     * @param <T> the type of the data stored in an instance
     */
    private static class ExpectedData<T>
    {
        /** The name of the affected property. */
        String propertyName;

        /** The corresponding data. */
        T data;

        /**
         * Creates a new instance of <code>ExpectedData</code>.
         *
         * @param n the property name
         * @param d the data
         */
        public ExpectedData(String n, T d)
        {
            propertyName = n;
            data = d;
        }

        /**
         * Checks the specified key and returns the stored data.
         *
         * @param key the actual key
         * @return the data
         */
        public T checkAndReturn(String key)
        {
            Assert.assertEquals("Wrong key", propertyName, key);
            return data;
        }
    }
}
