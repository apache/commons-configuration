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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.configuration2.ex.ConversionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link ImmutableConfiguration} default methods.
 */
public class TestDefaultImmutableConfiguration {

    /** Tests default methods. This class MUST NOT override the default methods! */
    private static class MapImmutableConfiguration implements ImmutableConfiguration {

        Map<String, Object> map = new HashMap<>();

        @Override
        public boolean containsKey(final String key) {
            // Super is not a default method.
            return false;
        }

        @Override
        public <T> T get(final Class<T> cls, final String key) {
            // Super is not a default method.
            return null;
        }

        @Override
        public <T> T get(final Class<T> cls, final String key, final T defaultValue) {
            // Super is not a default method.
            return null;
        }

        @Override
        public Object getArray(final Class<?> cls, final String key) {
            // Super is not a default method.
            return null;
        }

        @Override
        public Object getArray(final Class<?> cls, final String key, final Object defaultValue) {
            // Super is not a default method.
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(final String key) {
            // Super is not a default method.
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(final String key, final BigDecimal defaultValue) {
            // Super is not a default method.
            return null;
        }

        @Override
        public BigInteger getBigInteger(final String key) {
            // Super is not a default method.
            return null;
        }

        @Override
        public BigInteger getBigInteger(final String key, final BigInteger defaultValue) {
            // Super is not a default method.
            return null;
        }

        @Override
        public boolean getBoolean(final String key) {
            // Super is not a default method.
            return false;
        }

        @Override
        public boolean getBoolean(final String key, final boolean defaultValue) {
            // Super is not a default method.
            return false;
        }

        @Override
        public Boolean getBoolean(final String key, final Boolean defaultValue) {
            // Super is not a default method.
            return null;
        }

        @Override
        public byte getByte(final String key) {
            // Super is not a default method.
            return 0;
        }

        @Override
        public byte getByte(final String key, final byte defaultValue) {
            // Super is not a default method.
            return 0;
        }

        @Override
        public Byte getByte(final String key, final Byte defaultValue) {
            // Super is not a default method.
            return null;
        }

        @Override
        public <T> Collection<T> getCollection(final Class<T> cls, final String key, final Collection<T> target) {
            // Super is not a default method.
            return null;
        }

        @Override
        public <T> Collection<T> getCollection(final Class<T> cls, final String key, final Collection<T> target, final Collection<T> defaultValue) {
            // Super is not a default method.
            return null;
        }

        @Override
        public double getDouble(final String key) {
            // Super is not a default method.
            return 0;
        }

        @Override
        public double getDouble(final String key, final double defaultValue) {
            // Super is not a default method.
            return 0;
        }

        @Override
        public Double getDouble(final String key, final Double defaultValue) {
            // Super is not a default method.
            return null;
        }

        @Override
        public String getEncodedString(final String key) {
            // Super is not a default method.
            return null;
        }

        @Override
        public String getEncodedString(final String key, final ConfigurationDecoder decoder) {
            // Super is not a default method.
            return null;
        }

        @Override
        public float getFloat(final String key) {
            // Super is not a default method.
            return 0;
        }

        @Override
        public float getFloat(final String key, final float defaultValue) {
            // Super is not a default method.
            return 0;
        }

        @Override
        public Float getFloat(final String key, final Float defaultValue) {
            // Super is not a default method.
            return null;
        }

        @Override
        public int getInt(final String key) {
            // Super is not a default method.
            return 0;
        }

        @Override
        public int getInt(final String key, final int defaultValue) {
            // Super is not a default method.
            return 0;
        }

        @Override
        public Integer getInteger(final String key, final Integer defaultValue) {
            // Super is not a default method.
            return null;
        }

        @Override
        public Iterator<String> getKeys() {
            // Super is not a default method.
            return null;
        }

        @Override
        public Iterator<String> getKeys(final String prefix) {
            // Super is not a default method.
            return null;
        }

        @Override
        public <T> List<T> getList(final Class<T> cls, final String key) {
            // Super is not a default method.
            return null;
        }

        @Override
        public <T> List<T> getList(final Class<T> cls, final String key, final List<T> defaultValue) {
            // Super is not a default method.
            return null;
        }

        @Override
        public List<Object> getList(final String key) {
            // Super is not a default method.
            return null;
        }

        @Override
        public List<Object> getList(final String key, final List<?> defaultValue) {
            // Super is not a default method.
            return null;
        }

        @Override
        public long getLong(final String key) {
            // Super is not a default method.
            return 0;
        }

        @Override
        public long getLong(final String key, final long defaultValue) {
            // Super is not a default method.
            return 0;
        }

        @Override
        public Long getLong(final String key, final Long defaultValue) {
            // Super is not a default method.
            return null;
        }

        @Override
        public Properties getProperties(final String key) {
            // Super is not a default method.
            return null;
        }

        @Override
        public Object getProperty(final String key) {
            // Super is not a default method.
            return map.get(key);
        }

        @Override
        public short getShort(final String key) {
            // Super is not a default method.
            return 0;
        }

        @Override
        public short getShort(final String key, final short defaultValue) {
            // Super is not a default method.
            return 0;
        }

        @Override
        public Short getShort(final String key, final Short defaultValue) {
            // Super is not a default method.
            return null;
        }

        @Override
        public String getString(final String key) {
            return Objects.toString(map.get(key), null);
        }

        @Override
        public String getString(final String key, final String defaultValue) {
            // Super is not a default method.
            return null;
        }

        @Override
        public String[] getStringArray(final String key) {
            // Super is not a default method.
            return null;
        }

        @Override
        public ImmutableConfiguration immutableSubset(final String prefix) {
            // Super is not a default method.
            return null;
        }

        @Override
        public boolean isEmpty() {
            // Super is not a default method.
            return false;
        }

        @Override
        public int size() {
            // Super is not a default method.
            return 0;
        }

    }

    private final MapImmutableConfiguration config = new MapImmutableConfiguration();

    @Before
    @After
    public void clearMap() {
        config.map.clear();
    }

    @Test
    public void testGetDuration() {
        final Duration d = Duration.ofSeconds(1);
        config.map.put("durationD", d.toString());
        final Duration oneD = Duration.ofSeconds(1);
        final Duration twoD = Duration.ofSeconds(2);
        assertEquals("This returns 1(Duration)", oneD, config.getDuration("durationD"));
        assertEquals("This returns 1(Duration)", oneD, config.getDuration("durationD", twoD));
        assertEquals("This returns 2(default Duration)", twoD, config.getDuration("numberNotInConfig", twoD));
        assertEquals("This returns 1(Duration)", oneD, config.getDuration("durationD", twoD));
    }

    @Test(expected = ConversionException.class)
    public void testGetDurationIncompatibleType() {
        config.map.put("test.empty", "");
        config.getDuration("test.empty");
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetDurationUnknown() {
        config.getDuration("numberNotInConfig");
    }

}
