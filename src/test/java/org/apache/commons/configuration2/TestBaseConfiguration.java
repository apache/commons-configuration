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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventListenerTestImpl;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.Lookup;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests some basic functions of the BaseConfiguration class. Missing keys will throw Exceptions
 *
 */
public class TestBaseConfiguration {
    /** Constant for the number key. */
    static final String KEY_NUMBER = "number";

    protected static Class<?> missingElementException = NoSuchElementException.class;
    protected static Class<?> incompatibleElementException = ConversionException.class;
    protected BaseConfiguration config;

    @BeforeEach
    public void setUp() throws Exception {
        config = new BaseConfiguration();
        config.setThrowExceptionOnMissing(true);
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
    }

    @Test
    public void testAddProperty() throws Exception {
        Collection<Object> props = new ArrayList<>();
        props.add("one");
        props.add("two,three,four");
        props.add(new String[] {"5.1", "5.2", "5.3,5.4", "5.5"});
        props.add("six");
        config.addProperty("complex.property", props);

        Object val = config.getProperty("complex.property");
        assertInstanceOf(Collection.class, val);
        Collection<?> col = (Collection<?>) val;
        assertEquals(10, col.size());

        props = new ArrayList<>();
        props.add("quick");
        props.add("brown");
        props.add("fox,jumps");
        final Object[] data = {"The", props, "over,the", "lazy", "dog."};
        config.setProperty("complex.property", data);
        val = config.getProperty("complex.property");
        assertInstanceOf(Collection.class, val);
        col = (Collection<?>) val;
        final Iterator<?> it = col.iterator();
        final StringTokenizer tok = new StringTokenizer("The quick brown fox jumps over the lazy dog.", " ");
        while (tok.hasMoreTokens()) {
            assertTrue(it.hasNext());
            assertEquals(tok.nextToken(), it.next());
        }
        assertFalse(it.hasNext());

        config.setProperty("complex.property", null);
        assertFalse(config.containsKey("complex.property"));
    }

    /**
     * Tests cloning a BaseConfiguration.
     */
    @Test
    public void testClone() {
        for (int i = 0; i < 10; i++) {
            config.addProperty("key" + i, Integer.valueOf(i));
        }
        final BaseConfiguration config2 = (BaseConfiguration) config.clone();

        for (final Iterator<String> it = config.getKeys(); it.hasNext();) {
            final String key = it.next();
            assertTrue(config2.containsKey(key), "Key not found: " + key);
            assertEquals(config.getProperty(key), config2.getProperty(key), "Wrong value for key " + key);
        }
    }

    /**
     * Tests whether interpolation works as expected after cloning.
     */
    @Test
    public void testCloneInterpolation() {
        final String keyAnswer = "answer";
        config.addProperty(keyAnswer, "The answer is ${" + KEY_NUMBER + "}.");
        config.addProperty(KEY_NUMBER, 42);
        final BaseConfiguration clone = (BaseConfiguration) config.clone();
        clone.setProperty(KEY_NUMBER, 43);
        assertEquals("The answer is 42.", config.getString(keyAnswer), "Wrong interpolation in original");
        assertEquals("The answer is 43.", clone.getString(keyAnswer), "Wrong interpolation in clone");
    }

    /**
     * Tests the clone() method if a list property is involved.
     */
    @Test
    public void testCloneListProperty() {
        final String key = "list";
        config.addProperty(key, "value1");
        config.addProperty(key, "value2");
        final BaseConfiguration config2 = (BaseConfiguration) config.clone();
        config2.addProperty(key, "value3");
        assertEquals(2, config.getList(key).size(), "Wrong number of original properties");
    }

    /**
     * Tests whether a cloned configuration is decoupled from its original.
     */
    @Test
    public void testCloneModify() {
        final EventListener<ConfigurationEvent> l = new EventListenerTestImpl(config);
        config.addEventListener(ConfigurationEvent.ANY, l);
        config.addProperty("original", Boolean.TRUE);
        final BaseConfiguration config2 = (BaseConfiguration) config.clone();

        config2.addProperty("clone", Boolean.TRUE);
        assertFalse(config.containsKey("clone"), "New key appears in original");
        config2.setProperty("original", Boolean.FALSE);
        assertTrue(config.getBoolean("original"), "Wrong value of original property");

        assertTrue(config2.getEventListeners(ConfigurationEvent.ANY).isEmpty(), "Event listener was copied");
    }

    @Test
    public void testCommaSeparatedString() {
        final String prop = "hey, that's a test";
        config.setProperty("prop.string", prop);
        final List<Object> list = config.getList("prop.string");
        assertEquals(2, list.size(), "Wrong number of list elements");
        assertEquals("hey", list.get(0), "Wrong element 1");
    }

    @Test
    public void testCommaSeparatedStringEscaped() {
        final String prop2 = "hey\\, that's a test";
        config.setProperty("prop.string", prop2);
        assertEquals("hey, that's a test", config.getString("prop.string"), "Wrong value");
    }

    @Test
    public void testGetBigDecimal() {
        config.setProperty("numberBigD", "123.456");
        final BigDecimal number = new BigDecimal("123.456");
        final BigDecimal defaultValue = new BigDecimal("654.321");

        assertEquals(number, config.getBigDecimal("numberBigD"), "Existing key");
        assertEquals(number, config.getBigDecimal("numberBigD", defaultValue), "Existing key with default value");
        assertEquals(defaultValue, config.getBigDecimal("numberNotInConfig", defaultValue), "Missing key with default value");
    }

    @Test
    public void testGetBigDecimalIncompatibleType() {
        config.setProperty("test.empty", "");
        assertThrows(ConversionException.class, () -> config.getBigDecimal("test.empty"));
    }

    @Test
    public void testGetBigDecimalUnknown() {
        assertThrows(NoSuchElementException.class, () -> config.getBigDecimal("numberNotInConfig"));
    }

    @Test
    public void testGetBigInteger() {
        config.setProperty("numberBigI", "1234567890");
        final BigInteger number = new BigInteger("1234567890");
        final BigInteger defaultValue = new BigInteger("654321");

        assertEquals(number, config.getBigInteger("numberBigI"), "Existing key");
        assertEquals(number, config.getBigInteger("numberBigI", defaultValue), "Existing key with default value");
        assertEquals(defaultValue, config.getBigInteger("numberNotInConfig", defaultValue), "Missing key with default value");
    }

    @Test
    public void testGetBigIntegerIncompatibleType() {
        config.setProperty("test.empty", "");
        assertThrows(ConversionException.class, () -> config.getBigInteger("test.empty"));
    }

    @Test
    public void testGetBigIntegerUnknown() {
        assertThrows(NoSuchElementException.class, () -> config.getBigInteger("numberNotInConfig"));
    }

    @Test
    public void testGetBinaryValue() {
        config.setProperty("number", "0b11111111");
        assertEquals((byte) 0xFF, config.getByte("number"), "byte value");

        config.setProperty("number", "0b1111111111111111");
        assertEquals((short) 0xFFFF, config.getShort("number"), "short value");

        config.setProperty("number", "0b11111111111111111111111111111111");
        assertEquals(0xFFFFFFFF, config.getInt("number"), "int value");

        config.setProperty("number", "0b1111111111111111111111111111111111111111111111111111111111111111");
        assertEquals(0xFFFFFFFFFFFFFFFFL, config.getLong("number"), "long value");

        assertEquals(0xFFFFFFFFFFFFFFFFL, config.getBigInteger("number").longValue(), "long value");
    }

    @Test
    public void testGetBoolean() {
        config.setProperty("boolA", Boolean.TRUE);
        final boolean boolT = true, boolF = false;
        assertEquals(boolT, config.getBoolean("boolA"), "This returns true");
        assertEquals(boolT, config.getBoolean("boolA", boolF), "This returns true, not the default");
        assertEquals(boolF, config.getBoolean("boolNotInConfig", boolF), "This returns false(default)");
        assertEquals(Boolean.valueOf(boolT), config.getBoolean("boolA", Boolean.valueOf(boolF)), "This returns true(Boolean)");
    }

    @Test
    public void testGetBooleanIncompatibleType() {
        config.setProperty("test.empty", "");
        assertThrows(ConversionException.class, () -> config.getBoolean("test.empty"));
    }

    @Test
    public void testGetBooleanUnknown() {
        assertThrows(NoSuchElementException.class, () -> config.getBoolean("numberNotInConfig"));
    }

    @Test
    public void testGetByte() {
        config.setProperty("number", "1");
        final byte oneB = 1;
        final byte twoB = 2;
        assertEquals(oneB, config.getByte("number"), "This returns 1(byte)");
        assertEquals(oneB, config.getByte("number", twoB), "This returns 1(byte)");
        assertEquals(twoB, config.getByte("numberNotInConfig", twoB), "This returns 2(default byte)");
        assertEquals(Byte.valueOf(oneB), config.getByte("number", Byte.valueOf("2")), "This returns 1(Byte)");
    }

    @Test
    public void testGetByteIncompatibleType() {
        config.setProperty("test.empty", "");
        assertThrows(ConversionException.class, () -> config.getByte("test.empty"));
    }

    @Test
    public void testGetByteUnknown() {
        assertThrows(NoSuchElementException.class, () -> config.getByte("numberNotInConfig"));
    }

    @Test
    public void testGetDouble() {
        config.setProperty("numberD", "1.0");
        final double oneD = 1;
        final double twoD = 2;
        assertEquals(oneD, config.getDouble("numberD"), 0, "This returns 1(double)");
        assertEquals(oneD, config.getDouble("numberD", twoD), 0, "This returns 1(double)");
        assertEquals(twoD, config.getDouble("numberNotInConfig", twoD), 0, "This returns 2(default double)");
        assertEquals(Double.valueOf(oneD), config.getDouble("numberD", Double.valueOf("2")), "This returns 1(Double)");
    }

    @Test
    public void testGetDoubleIncompatibleType() {
        config.setProperty("test.empty", "");
        assertThrows(ConversionException.class, () -> config.getDouble("test.empty"));
    }

    @Test
    public void testGetDoubleUnknown() {
        assertThrows(NoSuchElementException.class, () -> config.getDouble("numberNotInConfig"));
    }

    @Test
    public void testGetDuration() {
        final Duration d = Duration.ofSeconds(1);
        config.setProperty("durationD", d.toString());
        final Duration oneD = Duration.ofSeconds(1);
        final Duration twoD = Duration.ofSeconds(2);
        assertEquals(oneD, config.getDuration("durationD"), "This returns 1(Duration)");
        assertEquals(oneD, config.getDuration("durationD", twoD), "This returns 1(Duration)");
        assertEquals(twoD, config.getDuration("numberNotInConfig", twoD), "This returns 2(default Duration)");
        assertEquals(oneD, config.getDuration("durationD", twoD), "This returns 1(Duration)");
    }

    @Test
    public void testGetDurationIncompatibleType() {
        config.setProperty("test.empty", "");
        assertThrows(ConversionException.class, () -> config.getDuration("test.empty"));
    }

    @Test
    public void testGetDurationUnknown() {
        assertThrows(NoSuchElementException.class, () -> config.getDuration("numberNotInConfig"));
    }

    @Test
    public void testGetEnum() {
        config.setProperty("testEnum", EnumFixture.SMALLTALK.name());
        config.setProperty("testBadEnum", "This is not an enum value.");
        final EnumFixture enum1 = EnumFixture.SMALLTALK;
        final EnumFixture defaultValue = EnumFixture.JAVA;
        //
        assertEquals(enum1, config.getEnum("testEnum", EnumFixture.class), "Existing key");
        assertEquals(enum1, config.getEnum("testEnum", EnumFixture.class, defaultValue), "Existing key with default value");
        assertEquals(defaultValue, config.getEnum("stringNotInConfig", EnumFixture.class, defaultValue), "Missing key with default value");
        //
        assertThrows(ConversionException.class, () -> config.getEnum("testBadEnum", EnumFixture.class));
        //
        assertThrows(ConversionException.class, () -> config.getEnum("testBadEnum", EnumFixture.class, defaultValue));
    }

    @Test
    public void testGetFloat() {
        config.setProperty("numberF", "1.0");
        final float oneF = 1;
        final float twoF = 2;
        assertEquals(oneF, config.getFloat("numberF"), 0, "This returns 1(float)");
        assertEquals(oneF, config.getFloat("numberF", twoF), 0, "This returns 1(float)");
        assertEquals(twoF, config.getFloat("numberNotInConfig", twoF), 0, "This returns 2(default float)");
        assertEquals(Float.valueOf(oneF), config.getFloat("numberF", Float.valueOf("2")), "This returns 1(Float)");
    }

    @Test
    public void testGetFloatIncompatibleType() {
        config.setProperty("test.empty", "");
        assertThrows(ConversionException.class, () -> config.getFloat("test.empty"));
    }

    @Test
    public void testGetFloatUnknown() {
        assertThrows(NoSuchElementException.class, () -> config.getFloat("numberNotInConfig"));
    }

    @Test
    public void testGetHexadecimalValue() {
        config.setProperty("number", "0xFF");
        assertEquals((byte) 0xFF, config.getByte("number"), "byte value");

        config.setProperty("number", "0xFFFF");
        assertEquals((short) 0xFFFF, config.getShort("number"), "short value");

        config.setProperty("number", "0xFFFFFFFF");
        assertEquals(0xFFFFFFFF, config.getInt("number"), "int value");

        config.setProperty("number", "0xFFFFFFFFFFFFFFFF");
        assertEquals(0xFFFFFFFFFFFFFFFFL, config.getLong("number"), "long value");

        assertEquals(0xFFFFFFFFFFFFFFFFL, config.getBigInteger("number").longValue(), "long value");
    }

    @Test
    public void testGetInterpolatedList() {
        config.addProperty("number", "1");
        config.addProperty("array", "${number}");
        config.addProperty("array", "${number}");

        final List<String> list = new ArrayList<>();
        list.add("1");
        list.add("1");

        assertEquals(list, config.getList("array"), "'array' property");
    }

    @Test
    public void testGetInterpolatedPrimitives() {
        config.addProperty("number", "1");
        config.addProperty("value", "${number}");

        config.addProperty("boolean", "true");
        config.addProperty("booleanValue", "${boolean}");

        // primitive types
        assertTrue(config.getBoolean("booleanValue"), "boolean interpolation");
        assertEquals(1, config.getByte("value"), "byte interpolation");
        assertEquals(1, config.getShort("value"), "short interpolation");
        assertEquals(1, config.getInt("value"), "int interpolation");
        assertEquals(1, config.getLong("value"), "long interpolation");
        assertEquals(1, config.getFloat("value"), 0, "float interpolation");
        assertEquals(1, config.getDouble("value"), 0, "double interpolation");

        // primitive wrappers
        assertEquals(Boolean.TRUE, config.getBoolean("booleanValue", null), "Boolean interpolation");
        assertEquals(Byte.valueOf("1"), config.getByte("value", null), "Byte interpolation");
        assertEquals(Short.valueOf("1"), config.getShort("value", null), "Short interpolation");
        assertEquals(Integer.valueOf("1"), config.getInteger("value", null), "Integer interpolation");
        assertEquals(Long.valueOf("1"), config.getLong("value", null), "Long interpolation");
        assertEquals(Float.valueOf("1"), config.getFloat("value", null), "Float interpolation");
        assertEquals(Double.valueOf("1"), config.getDouble("value", null), "Double interpolation");

        assertEquals(new BigInteger("1"), config.getBigInteger("value", null), "BigInteger interpolation");
        assertEquals(new BigDecimal("1"), config.getBigDecimal("value", null), "BigDecimal interpolation");
    }

    /**
     * Tests accessing and manipulating the interpolator object.
     */
    @Test
    public void testGetInterpolator() {
        InterpolationTestHelper.testGetInterpolator(config);
    }

    @Test
    public void testGetList() {
        config.addProperty("number", "1");
        config.addProperty("number", "2");
        final List<Object> list = config.getList("number");
        assertNotNull(list, "The list is null");
        assertEquals(2, list.size(), "List size");
        assertTrue(list.contains("1"), "The number 1 is missing from the list");
        assertTrue(list.contains("2"), "The number 2 is missing from the list");
    }

    @Test
    public void testGetLong() {
        config.setProperty("numberL", "1");
        final long oneL = 1;
        final long twoL = 2;
        assertEquals(oneL, config.getLong("numberL"), "This returns 1(long)");
        assertEquals(oneL, config.getLong("numberL", twoL), "This returns 1(long)");
        assertEquals(twoL, config.getLong("numberNotInConfig", twoL), "This returns 2(default long)");
        assertEquals(Long.valueOf(oneL), config.getLong("numberL", Long.valueOf("2")), "This returns 1(Long)");
    }

    @Test
    public void testGetLongIncompatibleTypes() {
        config.setProperty("test.empty", "");
        assertThrows(ConversionException.class, () -> config.getLong("test.empty"));
    }

    @Test
    public void testGetLongUnknown() {
        assertThrows(NoSuchElementException.class, () -> config.getLong("numberNotInConfig"));
    }

    @Test
    public void testGetProperty() {
        /* should be empty and return null */
        assertNull(config.getProperty("foo"), "This returns null");

        /* add a real value, and get it two different ways */
        config.setProperty("number", "1");
        assertEquals(config.getProperty("number"), "1", "This returns '1'");
        assertEquals(config.getString("number"), "1", "This returns '1'");
    }

    @Test
    public void testGetShort() {
        config.setProperty("numberS", "1");
        final short oneS = 1;
        final short twoS = 2;
        assertEquals(oneS, config.getShort("numberS"), "This returns 1(short)");
        assertEquals(oneS, config.getShort("numberS", twoS), "This returns 1(short)");
        assertEquals(twoS, config.getShort("numberNotInConfig", twoS), "This returns 2(default short)");
        assertEquals(Short.valueOf(oneS), config.getShort("numberS", Short.valueOf("2")), "This returns 1(Short)");
    }

    @Test
    public void testGetShortIncompatibleType() {
        config.setProperty("test.empty", "");
        assertThrows(ConversionException.class, () -> config.getShort("test.empty"));
    }

    @Test
    public void testGetShortUnknown() {
        assertThrows(NoSuchElementException.class, () -> config.getShort("numberNotInConfig"));
    }

    @Test
    public void testGetString() {
        config.setProperty("testString", "The quick brown fox");
        final String string = "The quick brown fox";
        final String defaultValue = "jumps over the lazy dog";

        assertEquals(string, config.getString("testString"), "Existing key");
        assertEquals(string, config.getString("testString", defaultValue), "Existing key with default value");
        assertEquals(defaultValue, config.getString("stringNotInConfig", defaultValue), "Missing key with default value");
    }

    /**
     * Tests that the first scalar of a list is returned.
     */
    @Test
    public void testGetStringForListValue() {
        config.addProperty("number", "1");
        config.addProperty("number", "2");
        assertEquals("1", config.getString("number"), "Wrong result");
    }

    @Test
    public void testGetStringUnknown() {
        assertThrows(NoSuchElementException.class, () -> config.getString("stringNotInConfig"));
    }

    /**
     * Tests whether a {@code ConfigurationInterpolator} can be created and installed.
     */
    @Test
    public void testInstallInterpolator() {
        final Lookup prefixLookup = EasyMock.createMock(Lookup.class);
        final Lookup defLookup = EasyMock.createMock(Lookup.class);
        EasyMock.replay(prefixLookup, defLookup);
        final Map<String, Lookup> prefixLookups = new HashMap<>();
        prefixLookups.put("test", prefixLookup);
        final List<Lookup> defLookups = new ArrayList<>();
        defLookups.add(defLookup);
        config.installInterpolator(prefixLookups, defLookups);
        final ConfigurationInterpolator interpolator = config.getInterpolator();
        assertEquals(prefixLookups, interpolator.getLookups(), "Wrong prefix lookups");
        final List<Lookup> defLookups2 = interpolator.getDefaultLookups();
        assertEquals(2, defLookups2.size(), "Wrong number of default lookups");
        assertSame(defLookup, defLookups2.get(0), "Wrong default lookup 1");
        final String var = "testVariable";
        final Object value = 42;
        config.addProperty(var, value);
        assertEquals(value, defLookups2.get(1).lookup(var), "Wrong lookup result");
    }

    /**
     * Tests obtaining a configuration with all variables replaced by their actual values.
     */
    @Test
    public void testInterpolatedConfiguration() {
        InterpolationTestHelper.testInterpolatedConfiguration(config);
    }

    @Test
    public void testInterpolation() {
        InterpolationTestHelper.testInterpolation(config);
    }

    /**
     * Tests interpolation of constant values.
     */
    @Test
    public void testInterpolationConstants() {
        InterpolationTestHelper.testInterpolationConstants(config);
    }

    /**
     * Tests interpolation of environment properties.
     */
    @Test
    public void testInterpolationEnvironment() {
        InterpolationTestHelper.testInterpolationEnvironment(config);
    }

    /**
     * Tests whether a variable can be escaped, so that it won't be interpolated.
     */
    @Test
    public void testInterpolationEscaped() {
        InterpolationTestHelper.testInterpolationEscaped(config);
    }

    /**
     * Tests interpolation with localhost values.
     */
    @Test
    public void testInterpolationLocalhost() {
        InterpolationTestHelper.testInterpolationLocalhost(config);
    }

    @Test
    public void testInterpolationLoop() {
        InterpolationTestHelper.testInterpolationLoop(config);
    }

    /**
     * Tests interpolation when a subset configuration is involved.
     */
    @Test
    public void testInterpolationSubset() {
        InterpolationTestHelper.testInterpolationSubset(config);
    }

    /**
     * Tests interpolation of system properties.
     */
    @Test
    public void testInterpolationSystemProperties() {
        InterpolationTestHelper.testInterpolationSystemProperties(config);
    }

    /**
     * Tests interpolation when the referred property is not found.
     */
    @Test
    public void testInterpolationUnknownProperty() {
        InterpolationTestHelper.testInterpolationUnknownProperty(config);
    }

    @Test
    public void testMultipleInterpolation() {
        InterpolationTestHelper.testMultipleInterpolation(config);
    }

    /**
     * Tests whether property access is possible without a {@code ConfigurationInterpolator}.
     */
    @Test
    public void testNoInterpolator() {
        config.setProperty("test", "${value}");
        config.setInterpolator(null);
        assertEquals("${value}", config.getString("test"), "Wrong result");
    }

    /**
     * Tests if conversion between number types is possible.
     */
    @Test
    public void testNumberConversions() {
        config.setProperty(KEY_NUMBER, Integer.valueOf(42));
        assertEquals(42, config.getInt(KEY_NUMBER), "Wrong int returned");
        assertEquals(42L, config.getLong(KEY_NUMBER), "Wrong long returned");
        assertEquals((byte) 42, config.getByte(KEY_NUMBER), "Wrong byte returned");
        assertEquals(42.0f, config.getFloat(KEY_NUMBER), 0.01f, "Wrong float returned");
        assertEquals(42.0, config.getDouble(KEY_NUMBER), 0.001, "Wrong double returned");

        assertEquals(Long.valueOf(42L), config.getLong(KEY_NUMBER, null), "Wrong Long returned");
        assertEquals(new BigInteger("42"), config.getBigInteger(KEY_NUMBER), "Wrong BigInt returned");
        assertEquals(new BigDecimal("42.0"), config.getBigDecimal(KEY_NUMBER), "Wrong DigDecimal returned");
    }

    @Test
    public void testPropertyAccess() {
        config.clearProperty("prop.properties");
        config.setProperty("prop.properties", "");
        assertEquals(config.getProperties("prop.properties"), new Properties(), "This returns an empty Properties object");
        config.clearProperty("prop.properties");
        config.setProperty("prop.properties", "foo=bar, baz=moo, seal=clubber");

        final Properties p = new Properties();
        p.setProperty("foo", "bar");
        p.setProperty("baz", "moo");
        p.setProperty("seal", "clubber");
        assertEquals(config.getProperties("prop.properties"), p, "This returns a filled in Properties object");
    }

    /**
     * Tests whether a {@code ConfigurationInterpolator} can be set.
     */
    @Test
    public void testSetInterpolator() {
        final ConfigurationInterpolator interpolator = EasyMock.createMock(ConfigurationInterpolator.class);
        EasyMock.replay(interpolator);
        config.setInterpolator(interpolator);
        assertSame(interpolator, config.getInterpolator(), "Interpolator not set");
    }

    /**
     * Tests the specific size() implementation.
     */
    @Test
    public void testSize() {
        final int count = 16;
        for (int i = 0; i < count; i++) {
            config.addProperty("key" + i, "value" + i);
        }
        assertEquals(count, config.size(), "Wrong size");
    }

    @Test
    public void testSubset() {
        /*
         * test subset : assure we don't reprocess the data elements when generating the subset
         */

        final String prop = "hey, that's a test";
        final String prop2 = "hey\\, that's a test";
        config.setProperty("prop.string", prop2);
        config.setProperty("property.string", "hello");

        Configuration subEprop = config.subset("prop");

        assertEquals(prop, subEprop.getString("string"), "Returns the full string");
        assertEquals(1, subEprop.getList("string").size(), "Wrong list size");

        Iterator<String> it = subEprop.getKeys();
        it.next();
        assertFalse(it.hasNext());

        subEprop = config.subset("prop.");
        it = subEprop.getKeys();
        assertFalse(it.hasNext());
    }

    @Test
    public void testThrowExceptionOnMissing() {
        assertTrue(config.isThrowExceptionOnMissing(), "Throw Exception Property is not set!");
    }
}
