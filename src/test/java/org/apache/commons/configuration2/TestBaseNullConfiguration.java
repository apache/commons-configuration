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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
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
        assertEquals(Arrays.asList("hey", "that's a test"), list);
    }

    @Test
    public void testCommaSeparatedStringEscaped() {
        final String prop2 = "hey\\, that's a test";
        config.clearProperty("prop.string");
        config.setProperty("prop.string", prop2);
        assertEquals("hey, that's a test", config.getString("prop.string"));
    }

    @Test
    public void testGetBigDecimal() {
        config.setProperty("numberBigD", "123.456");
        final BigDecimal number = new BigDecimal("123.456");
        final BigDecimal defaultValue = new BigDecimal("654.321");

        assertEquals(number, config.getBigDecimal("numberBigD"));
        assertEquals(number, config.getBigDecimal("numberBigD", defaultValue));
        assertEquals(defaultValue, config.getBigDecimal("numberNotInConfig", defaultValue));
    }

    @Test
    public void testGetBigDecimalIncompatibleType() {
        config.setProperty("test.empty", "");
        assertThrows(ConversionException.class, () -> config.getBigDecimal("test.empty"));
    }

    @Test
    public void testGetBigDecimalUnknown() {
        assertNull(config.getBigDecimal("numberNotInConfig"));
    }

    @Test
    public void testGetBigInteger() {
        config.setProperty("numberBigI", "1234567890");
        final BigInteger number = new BigInteger("1234567890");
        final BigInteger defaultValue = new BigInteger("654321");

        assertEquals(number, config.getBigInteger("numberBigI"));
        assertEquals(number, config.getBigInteger("numberBigI", defaultValue));
        assertEquals(defaultValue, config.getBigInteger("numberNotInConfig", defaultValue));
    }

    @Test
    public void testGetBigIntegerIncompatibleType() {
        config.setProperty("test.empty", "");
        assertThrows(ConversionException.class, () -> config.getBigInteger("test.empty"));
    }

    @Test
    public void testGetBigIntegerUnknown() {
        assertNull(config.getBigInteger("numberNotInConfig"));
    }

    @Test
    public void testGetBoolean() {
        config.setProperty("boolA", Boolean.TRUE);
        final boolean boolT = true, boolF = false;
        assertEquals(boolT, config.getBoolean("boolA"));
        assertEquals(boolT, config.getBoolean("boolA", boolF));
        assertEquals(boolF, config.getBoolean("boolNotInConfig", boolF));
        assertEquals(Boolean.valueOf(boolT), config.getBoolean("boolA", Boolean.valueOf(boolF)));
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
        assertEquals(oneB, config.getByte("number"));
        assertEquals(oneB, config.getByte("number", twoB));
        assertEquals(twoB, config.getByte("numberNotInConfig", twoB));
        assertEquals(Byte.valueOf(oneB), config.getByte("number", Byte.valueOf("2")));
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
        assertEquals(oneD, config.getDouble("numberD"), 0);
        assertEquals(oneD, config.getDouble("numberD", twoD), 0);
        assertEquals(twoD, config.getDouble("numberNotInConfig", twoD), 0);
        assertEquals(Double.valueOf(oneD), config.getDouble("numberD", Double.valueOf("2")));
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
        assertEquals(oneF, config.getFloat("numberF"), 0);
        assertEquals(oneF, config.getFloat("numberF", twoF), 0);
        assertEquals(twoF, config.getFloat("numberNotInConfig", twoF), 0);
        assertEquals(Float.valueOf(oneF), config.getFloat("numberF", Float.valueOf("2")));
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
        assertEquals(Arrays.asList("1", "2"), list);
    }

    @Test
    public void testGetListAsScalar() {
        config.addProperty("number", "1");
        config.addProperty("number", "2");
        assertEquals("1", config.getString("number"));
    }

    @Test
    public void testGetLong() {
        config.setProperty("numberL", "1");
        final long oneL = 1;
        final long twoL = 2;
        assertEquals(oneL, config.getLong("numberL"));
        assertEquals(oneL, config.getLong("numberL", twoL));
        assertEquals(twoL, config.getLong("numberNotInConfig", twoL));
        assertEquals(Long.valueOf(oneL), config.getLong("numberL", Long.valueOf("2")));
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
        assertNull(config.getProperty("foo"));

        /* add a real value, and get it two different ways */
        config.setProperty("number", "1");
        assertEquals("1", config.getProperty("number"));
        assertEquals("1", config.getString("number"));
    }

    @Test
    public void testGetShort() {
        config.setProperty("numberS", "1");
        final short oneS = 1;
        final short twoS = 2;
        assertEquals(oneS, config.getShort("numberS"));
        assertEquals(oneS, config.getShort("numberS", twoS));
        assertEquals(twoS, config.getShort("numberNotInConfig", twoS));
        assertEquals(Short.valueOf(oneS), config.getShort("numberS", Short.valueOf("2")));
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

        assertEquals(string, config.getString("testString"));
        assertEquals(string, config.getString("testString", defaultValue));
        assertEquals(defaultValue, config.getString("stringNotInConfig", defaultValue));
    }

    @Test
    public void testGetStringUnknown() {
        assertNull(config.getString("stringNotInConfig"));
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

        assertEquals(dbProp, superProp.getString("db"));
        assertEquals(unInterpolatedValue, superProp.getString("dbFailedInterpolate"));

        superProp.setProperty("arrayInt", "${applicationRoot}/1");
        final String[] arrayInt = superProp.getStringArray("arrayInt");
        assertEquals("/home/applicationRoot/1", arrayInt[0]);
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

        assertEquals(expectedValue, config.getString("test.third-level"));
    }

    @Test
    public void testPropertyAccess() {
        config.clearProperty("prop.properties");
        config.setProperty("prop.properties", "");
        assertEquals(new Properties(), config.getProperties("prop.properties"));
        config.clearProperty("prop.properties");
        config.setProperty("prop.properties", "foo=bar, baz=moo, seal=clubber");

        final Properties p = new Properties();
        p.setProperty("foo", "bar");
        p.setProperty("baz", "moo");
        p.setProperty("seal", "clubber");
        assertEquals(p, config.getProperties("prop.properties"));
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

        assertEquals(prop, subEprop.getString("string"));
        assertEquals(1, subEprop.getList("string").size());

        Iterator<String> it = subEprop.getKeys();
        it.next();
        assertFalse(it.hasNext());

        subEprop = config.subset("prop.");
        it = subEprop.getKeys();
        assertFalse(it.hasNext());
    }

    @Test
    public void testThrowExceptionOnMissing() {
        assertFalse(config.isThrowExceptionOnMissing());
    }
}
