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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConversionException;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests some basic functions of the BaseConfiguration class. Missing keys might
 * return null.
 *
 */
public class TestBaseNullConfiguration
{
    protected BaseConfiguration config;

    @Before
    public void setUp() throws Exception
    {
        config = new BaseConfiguration();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        config.setThrowExceptionOnMissing(false);
    }

    @Test
    public void testThrowExceptionOnMissing()
    {
        assertFalse("Throw Exception Property is set!", config.isThrowExceptionOnMissing());
    }

    @Test
    public void testGetProperty()
    {
        /* should be empty and return null */
        assertEquals("This returns null", config.getProperty("foo"), null);

        /* add a real value, and get it two different ways */
        config.setProperty("number", "1");
        assertEquals("This returns '1'", config.getProperty("number"), "1");
        assertEquals("This returns '1'", config.getString("number"), "1");
    }

    @Test
    public void testGetByte()
    {
        config.setProperty("number", "1");
        final byte oneB = 1;
        final byte twoB = 2;
        assertEquals("This returns 1(byte)", oneB, config.getByte("number"));
        assertEquals("This returns 1(byte)", oneB, config.getByte("number", twoB));
        assertEquals("This returns 2(default byte)", twoB, config.getByte("numberNotInConfig", twoB));
        assertEquals("This returns 1(Byte)", new Byte(oneB), config.getByte("number", new Byte("2")));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetByteUnknown()
    {
        config.getByte("numberNotInConfig");
    }

    @Test(expected = ConversionException.class)
    public void testGetByteIncompatibleType()
    {
        config.setProperty("test.empty", "");
        config.getByte("test.empty");
    }

    @Test
    public void testGetShort()
    {
        config.setProperty("numberS", "1");
        final short oneS = 1;
        final short twoS = 2;
        assertEquals("This returns 1(short)", oneS, config.getShort("numberS"));
        assertEquals("This returns 1(short)", oneS, config.getShort("numberS", twoS));
        assertEquals("This returns 2(default short)", twoS, config.getShort("numberNotInConfig", twoS));
        assertEquals("This returns 1(Short)", new Short(oneS), config.getShort("numberS", new Short("2")));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetShortUnknown()
    {
        config.getShort("numberNotInConfig");
    }

    @Test(expected = ConversionException.class)
    public void testGetShortIncompatibleType()
    {
        config.setProperty("test.empty", "");
        config.getShort("test.empty");
    }

    @Test
    public void testGetLong()
    {
        config.setProperty("numberL", "1");
        final long oneL = 1;
        final long twoL = 2;
        assertEquals("This returns 1(long)", oneL, config.getLong("numberL"));
        assertEquals("This returns 1(long)", oneL, config.getLong("numberL", twoL));
        assertEquals("This returns 2(default long)", twoL, config.getLong("numberNotInConfig", twoL));
        assertEquals("This returns 1(Long)", new Long(oneL), config.getLong("numberL", new Long("2")));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetLongUnknown()
    {
        config.getLong("numberNotInConfig");
    }

    @Test(expected = ConversionException.class)
    public void testGetLongIncompatibleTypes()
    {
        config.setProperty("test.empty", "");
        config.getLong("test.empty");
    }

    @Test
    public void testGetFloat()
    {
        config.setProperty("numberF", "1.0");
        final float oneF = 1;
        final float twoF = 2;
        assertEquals("This returns 1(float)", oneF, config.getFloat("numberF"), 0);
        assertEquals("This returns 1(float)", oneF, config.getFloat("numberF", twoF), 0);
        assertEquals("This returns 2(default float)", twoF, config.getFloat("numberNotInConfig", twoF), 0);
        assertEquals("This returns 1(Float)", new Float(oneF), config.getFloat("numberF", new Float("2")));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetFloatUnknown()
    {
        config.getFloat("numberNotInConfig");
    }

    @Test(expected = ConversionException.class)
    public void testGetFloatIncompatibleType()
    {
        config.setProperty("test.empty", "");
        config.getFloat("test.empty");
    }

    @Test
    public void testGetDouble()
    {
        config.setProperty("numberD", "1.0");
        final double oneD = 1;
        final double twoD = 2;
        assertEquals("This returns 1(double)", oneD, config.getDouble("numberD"), 0);
        assertEquals("This returns 1(double)", oneD, config.getDouble("numberD", twoD), 0);
        assertEquals("This returns 2(default double)", twoD, config.getDouble("numberNotInConfig", twoD), 0);
        assertEquals("This returns 1(Double)", new Double(oneD), config.getDouble("numberD", new Double("2")));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetDoubleUnknown()
    {
        config.getDouble("numberNotInConfig");
    }

    @Test(expected = ConversionException.class)
    public void testGetDoubleIncompatibleType()
    {
        config.setProperty("test.empty", "");
        config.getDouble("test.empty");
    }

    @Test
    public void testGetBigDecimal()
    {
        config.setProperty("numberBigD", "123.456");
        final BigDecimal number = new BigDecimal("123.456");
        final BigDecimal defaultValue = new BigDecimal("654.321");

        assertEquals("Existing key", number, config.getBigDecimal("numberBigD"));
        assertEquals("Existing key with default value", number, config.getBigDecimal("numberBigD", defaultValue));
        assertEquals("Missing key with default value", defaultValue, config.getBigDecimal("numberNotInConfig", defaultValue));
    }

    @Test
    public void testGetBigDecimalUnknown()
    {
        assertNull("Missing Key is not null!", config.getBigDecimal("numberNotInConfig"));
    }

    @Test(expected = ConversionException.class)
    public void testGetBigDecimalIncompatibleType()
    {
        config.setProperty("test.empty", "");
        config.getBigDecimal("test.empty");
    }

    @Test
    public void testGetBigInteger()
    {
        config.setProperty("numberBigI", "1234567890");
        final BigInteger number = new BigInteger("1234567890");
        final BigInteger defaultValue = new BigInteger("654321");

        assertEquals("Existing key", number, config.getBigInteger("numberBigI"));
        assertEquals("Existing key with default value", number, config.getBigInteger("numberBigI", defaultValue));
        assertEquals("Missing key with default value", defaultValue, config.getBigInteger("numberNotInConfig", defaultValue));
    }

    @Test
    public void testGetBigIntegerUnknown()
    {
        assertNull("Missing Key is not null!", config.getBigInteger("numberNotInConfig"));
    }

    @Test(expected = ConversionException.class)
    public void testGetBigIntegerIncompatibleType()
    {
        config.setProperty("test.empty", "");
        config.getBigInteger("test.empty");
    }


    @Test
    public void testGetString()
    {
        config.setProperty("testString", "The quick brown fox");
        final String string = new String("The quick brown fox");
        final String defaultValue = new String("jumps over the lazy dog");

        assertEquals("Existing key", string, config.getString("testString"));
        assertEquals("Existing key with default value", string, config.getString("testString", defaultValue));
        assertEquals("Missing key with default value", defaultValue, config.getString("stringNotInConfig", defaultValue));
    }

    @Test
    public void testGetStringUnknown()
    {
        assertNull("Missing Key is not null!", config.getString("stringNotInConfig"));
    }

    @Test
    public void testGetBoolean()
    {
        config.setProperty("boolA", Boolean.TRUE);
        final boolean boolT = true, boolF = false;
        assertEquals("This returns true", boolT, config.getBoolean("boolA"));
        assertEquals("This returns true, not the default", boolT, config.getBoolean("boolA", boolF));
        assertEquals("This returns false(default)", boolF, config.getBoolean("boolNotInConfig", boolF));
        assertEquals("This returns true(Boolean)", new Boolean(boolT), config.getBoolean("boolA", new Boolean(boolF)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetBooleanUnknown()
    {
        config.getBoolean("numberNotInConfig");
    }

    @Test(expected = ConversionException.class)
    public void testGetBooleanIncompatibleType()
    {
        config.setProperty("test.empty", "");
        config.getBoolean("test.empty");
    }

    @Test
    public void testGetList()
    {
        config.addProperty("number", "1");
        config.addProperty("number", "2");
        final List<Object> list = config.getList("number");
        assertNotNull("The list is null", list);
        assertEquals("List size", 2, list.size());
        assertTrue("The number 1 is missing from the list", list.contains("1"));
        assertTrue("The number 2 is missing from the list", list.contains("2"));
    }

    @Test
    public void testGetListAsScalar()
    {
        config.addProperty("number", "1");
        config.addProperty("number", "2");
        assertEquals("Wrong value", "1", config.getString("number"));
    }

    @Test
    public void testCommaSeparatedString()
    {
        final String prop = "hey, that's a test";
        config.setProperty("prop.string", prop);
        final List<Object> list = config.getList("prop.string");
        assertEquals("Wrong number of elements", 2, list.size());
        assertEquals("Wrong element 1", "hey", list.get(0));
    }

    @Test
    public void testCommaSeparatedStringEscaped()
    {
        final String prop2 = "hey\\, that's a test";
        config.clearProperty("prop.string");
        config.setProperty("prop.string", prop2);
        assertEquals("Wrong value", "hey, that's a test", config.getString("prop.string"));
    }

    @Test
    public void testPropertyAccess()
    {
        config.clearProperty("prop.properties");
        config.setProperty("prop.properties", "");
        assertEquals(
            "This returns an empty Properties object",
            config.getProperties("prop.properties"),
            new Properties());
        config.clearProperty("prop.properties");
        config.setProperty("prop.properties", "foo=bar, baz=moo, seal=clubber");

        final Properties p = new Properties();
        p.setProperty("foo", "bar");
        p.setProperty("baz", "moo");
        p.setProperty("seal", "clubber");
        assertEquals(
            "This returns a filled in Properties object",
            config.getProperties("prop.properties"),
            p);
    }

    @Test
    public void testSubset()
    {
        /*
         * test subset : assure we don't reprocess the data elements
         * when generating the subset
         */

        final String prop = "hey, that's a test";
        final String prop2 = "hey\\, that's a test";
        config.setProperty("prop.string", prop2);
        config.setProperty("property.string", "hello");

        Configuration subEprop = config.subset("prop");

        assertEquals(
            "Returns the full string",
            prop,
            subEprop.getString("string"));
        assertEquals("Wrong list size", 1, subEprop.getList("string").size());

        Iterator<String> it = subEprop.getKeys();
        it.next();
        assertFalse(it.hasNext());

        subEprop = config.subset("prop.");
        it = subEprop.getKeys();
        assertFalse(it.hasNext());
    }

    @Test
    public void testInterpolation() throws Exception
    {
        config.setProperty("applicationRoot", "/home/applicationRoot");
        config.setProperty("db", "${applicationRoot}/db/hypersonic");
        final String unInterpolatedValue = "${applicationRoot2}/db/hypersonic";
        config.setProperty("dbFailedInterpolate", unInterpolatedValue);
        final String dbProp = "/home/applicationRoot/db/hypersonic";

        //construct a new config, using config as the defaults config for it.
        final BaseConfiguration superProp = config;

        assertEquals(
            "Checking interpolated variable",dbProp,
            superProp.getString("db"));
        assertEquals(
            "lookup fails, leave variable as is",
            superProp.getString("dbFailedInterpolate"),
            unInterpolatedValue);

        superProp.setProperty("arrayInt", "${applicationRoot}/1");
        final String[] arrayInt = superProp.getStringArray("arrayInt");
        assertEquals(
            "check first entry was interpolated",
            "/home/applicationRoot/1",
            arrayInt[0]);
    }

    @Test
    public void testMultipleInterpolation() throws Exception
    {
        config.setProperty("test.base-level", "/base-level");
        config.setProperty("test.first-level", "${test.base-level}/first-level");
        config.setProperty(
            "test.second-level",
            "${test.first-level}/second-level");
        config.setProperty(
            "test.third-level",
            "${test.second-level}/third-level");

        final String expectedValue =
            "/base-level/first-level/second-level/third-level";

        assertEquals(config.getString("test.third-level"), expectedValue);
    }

    @Test(expected = IllegalStateException.class)
    public void testInterpolationLoop() throws Exception
    {
        config.setProperty("test.a", "${test.b}");
        config.setProperty("test.b", "${test.a}");
        config.getString("test.a");
    }
}

