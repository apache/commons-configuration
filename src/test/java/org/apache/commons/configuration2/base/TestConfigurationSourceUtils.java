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
package org.apache.commons.configuration2.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.easymock.EasyMock;

/**
 * Test class for {@link ConfigurationSourceUtils}.
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public class TestConfigurationSourceUtils extends TestCase
{
    /** Constant for a property key. */
    private static final String KEY = "propertyKey";

    /**
     * Tests the isEmpty() method if it is implemented by the source.
     */
    public void testIsEmptyImplemented()
    {
        ConfigurationSource source = EasyMock
                .createMock(ConfigurationSource.class);
        EasyMock.expect(source.isEmpty()).andReturn(Boolean.FALSE);
        EasyMock.replay(source);
        assertFalse("Wrong result of isEmpty()", ConfigurationSourceUtils
                .isEmpty(source));
        EasyMock.verify(source);
    }

    /**
     * Tests the isEmpty() method if it is not implemented by the source.
     */
    public void testIsEmptyNotImplemented()
    {
        ConfigurationSource source = EasyMock
                .createMock(ConfigurationSource.class);
        Iterator<?> it = EasyMock.createMock(Iterator.class);
        EasyMock.expect(source.isEmpty()).andThrow(
                new UnsupportedOperationException());
        source.getKeys();
        EasyMock.expectLastCall().andReturn(it);
        EasyMock.expect(it.hasNext()).andReturn(Boolean.TRUE);
        EasyMock.replay(it, source);
        assertFalse("Wrong result of isEmpty()", ConfigurationSourceUtils
                .isEmpty(source));
        EasyMock.verify(it, source);
    }

    /**
     * Tests isEmpty() for a null source. This should throw an exception.
     */
    public void testIsEmptyNull()
    {
        try
        {
            ConfigurationSourceUtils.isEmpty(null);
            fail("No exception for null source!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests the valueCount() implementation if this method is implemented by
     * the source.
     */
    public void testValueCountImplemented()
    {
        ConfigurationSource source = EasyMock
                .createMock(ConfigurationSource.class);
        final int count = 4;
        EasyMock.expect(source.valueCount(KEY)).andReturn(count);
        EasyMock.replay(source);
        assertEquals("Wrong value count", count, ConfigurationSourceUtils
                .valueCount(source, KEY));
        EasyMock.verify(source);
    }

    /**
     * Tests the valueCount() implementation if this method is not implemented
     * by the source and the property cannot be found.
     */
    public void testValueCountNotImplementedNonExisting()
    {
        ConfigurationSource source = EasyMock
                .createMock(ConfigurationSource.class);
        EasyMock.expect(source.valueCount(KEY)).andThrow(
                new UnsupportedOperationException());
        EasyMock.expect(source.getProperty(KEY)).andReturn(null);
        EasyMock.replay(source);
        assertEquals("Wrong value count", 0, ConfigurationSourceUtils
                .valueCount(source, KEY));
        EasyMock.verify(source);
    }

    /**
     * Tests the valueCount() implementation if this method is not implemented
     * by the source and the property has a single value.
     */
    public void testValueCountNotImplementedSingleValue()
    {
        ConfigurationSource source = EasyMock
                .createMock(ConfigurationSource.class);
        EasyMock.expect(source.valueCount(KEY)).andThrow(
                new UnsupportedOperationException());
        EasyMock.expect(source.getProperty(KEY)).andReturn(this);
        EasyMock.replay(source);
        assertEquals("Wrong value count", 1, ConfigurationSourceUtils
                .valueCount(source, KEY));
        EasyMock.verify(source);
    }

    /**
     * Tests the valueCount() implementation if this method is not implemented
     * by the source and the property is a collection.
     */
    public void testValueCountNotImplementedCollection()
    {
        ConfigurationSource source = EasyMock
                .createMock(ConfigurationSource.class);
        EasyMock.expect(source.valueCount(KEY)).andThrow(
                new UnsupportedOperationException());
        final int count = 5;
        Collection<Object> values = new ArrayList<Object>(count);
        for (int i = 0; i < count; i++)
        {
            values.add(KEY + i);
        }
        EasyMock.expect(source.getProperty(KEY)).andReturn(values);
        EasyMock.replay(source);
        assertEquals("Wrong value count", count, ConfigurationSourceUtils
                .valueCount(source, KEY));
        EasyMock.verify(source);
    }

    /**
     * Tests the valueCount() implementation if a null source is passed in. This
     * should cause an exception.
     */
    public void testValueCountNull()
    {
        try
        {
            ConfigurationSourceUtils.valueCount(null, KEY);
            fail("No exception for null source!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests the size() implementation if this method is implemented by the
     * source.
     */
    public void testSizeImplemented()
    {
        ConfigurationSource source = EasyMock
                .createMock(ConfigurationSource.class);
        final int size = 256;
        EasyMock.expect(source.size()).andReturn(size);
        EasyMock.replay(source);
        assertEquals("Wrong size", size, ConfigurationSourceUtils.size(source));
        EasyMock.verify(source);
    }

    /**
     * Tests the size() implementation if this method is not implemented by the
     * source.
     */
    public void testSizeNotImplemented()
    {
        ConfigurationSource source = EasyMock
                .createMock(ConfigurationSource.class);
        final int size = 128;
        EasyMock.expect(source.size()).andThrow(
                new UnsupportedOperationException());
        Collection<String> keys = new ArrayList<String>(size);
        for (int i = 0; i < size; i++)
        {
            keys.add(KEY + i);
        }
        EasyMock.expect(source.getKeys()).andReturn(keys.iterator());
        for (String k : keys)
        {
            EasyMock.expect(source.valueCount(k)).andReturn(1);
        }
        EasyMock.replay(source);
        assertEquals("Wrong size", size, ConfigurationSourceUtils.size(source));
        EasyMock.verify(source);
    }

    /**
     * Tests the size() implementation if a null source is passed in. This
     * should cause an exception.
     */
    public void testSizeNull()
    {
        try
        {
            ConfigurationSourceUtils.size(null);
            fail("No exception for null source!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }
}
