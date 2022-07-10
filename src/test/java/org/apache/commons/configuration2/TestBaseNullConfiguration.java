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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConversionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests some basic functions of the BaseConfiguration class. Missing keys might return null.
 *
 */
public class TestBaseNullConfiguration {
    protected BaseConfiguration config;

    @BeforeEach
    public void setUp() throws Exception {
        config = new BaseConfiguration();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        config.setThrowExceptionOnMissing(false);
    }

    @Test
    public void testCommaSeparatedString() {
        final String prop = "hey, that's a test";
        config.setProperty("prop.string", prop);
        final List<Object> list = config.getList("prop.string");
        assertEquals(2, list.size(), "Wrong number of elements");
        assertEquals("hey", list.get(0), "Wrong element 1");
    }

    @Test
    public void testCommaSeparatedStringEscaped() {
        final String prop2 = "hey\\, that's a test";
        config.clearProperty("prop.string");
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
        assertNull(config.getBigDecimal("numberNotInConfig"), "Missing Key is not null!");
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
        assertNull(config.getBigInteger("numberNotInConfig"), "Missing Key is not null!");
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
    public void testGetListAsScalar() {
        config.addProperty("number", "1");
        config.addProperty("number", "2");
        assertEquals("1", config.getString("number"), "Wrong value");
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
        assertEquals("1", config.getProperty("number"), "This returns '1'");
        assertEquals("1", config.getString("number"), "This returns '1'");
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

    @Test
    public void testGetStringUnknown() {
        assertNull(config.getString("stringNotInConfig"), "Missing Key is not null!");
    }

    @Test
    public void testInterpolation() throws Exception {
        config.setProperty("applicationRoot", "/home/applicationRoot");
        config.setProperty("db", "${applicationRoot}/db/hypersonic");
        final String unInterpolatedValue = "${applicationRoot2}/db/hypersonic";
        config.setProperty("dbFailedInterpolate", unInterpolatedValue);
        final String dbProp = "/home/applicationRoot/db/hypersonic";

        // construct a new config, using config as the defaults config for it.
        final BaseConfiguration superProp = config;

        assertEquals(dbProp, superProp.getString("db"), "Checking interpolated variable");
        assertEquals(unInterpolatedValue, superProp.getString("dbFailedInterpolate"), "lookup fails, leave variable as is");

        superProp.setProperty("arrayInt", "${applicationRoot}/1");
        final String[] arrayInt = superProp.getStringArray("arrayInt");
        assertEquals("/home/applicationRoot/1", arrayInt[0], "check first entry was interpolated");
    }

    @Test
    public void testInterpolationLoop() throws Exception {
        config.setProperty("test.a", "${test.b}");
        config.setProperty("test.b", "${test.a}");
        assertThrows(IllegalStateException.class, () -> config.getString("test.a"));
    }

    @Test
    public void testMultipleInterpolation() throws Exception {
        config.setProperty("test.base-level", "/base-level");
        config.setProperty("test.first-level", "${test.base-level}/first-level");
        config.setProperty("test.second-level", "${test.first-level}/second-level");
        config.setProperty("test.third-level", "${test.second-level}/third-level");

        final String expectedValue = "/base-level/first-level/second-level/third-level";

        assertEquals(config.getString("test.third-level"), expectedValue);
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
        assertFalse(config.isThrowExceptionOnMissing(), "Throw Exception Property is set!");
    }
}
