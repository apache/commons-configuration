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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
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
import org.junit.Before;
import org.junit.Test;

import junitx.framework.ListAssert;

/**
 * Tests some basic functions of the BaseConfiguration class. Missing keys will
 * throw Exceptions
 *
 */
public class TestBaseConfiguration
{
    /** Constant for the number key.*/
    static final String KEY_NUMBER = "number";

    protected BaseConfiguration config = null;

    protected static Class<?> missingElementException = NoSuchElementException.class;
    protected static Class<?> incompatibleElementException = ConversionException.class;

    @Before
    public void setUp() throws Exception
    {
        config = new BaseConfiguration();
        config.setThrowExceptionOnMissing(true);
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
    }

    @Test
    public void testThrowExceptionOnMissing()
    {
        assertTrue("Throw Exception Property is not set!", config.isThrowExceptionOnMissing());
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

    @Test(expected = NoSuchElementException.class)
    public void testGetBigDecimalUnknown()
    {
        config.getBigDecimal("numberNotInConfig");
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

    @Test(expected = NoSuchElementException.class)
    public void testGetBigIntegerUnknown()
    {
        config.getBigInteger("numberNotInConfig");
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
        final String string = "The quick brown fox";
        final String defaultValue = "jumps over the lazy dog";

        assertEquals("Existing key", string, config.getString("testString"));
        assertEquals("Existing key with default value", string, config.getString("testString", defaultValue));
        assertEquals("Missing key with default value", defaultValue, config.getString("stringNotInConfig", defaultValue));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetStringUnknown()
    {
        config.getString("stringNotInConfig");
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

    /**
     * Tests that the first scalar of a list is returned.
     */
    @Test
    public void testGetStringForListValue()
    {
        config.addProperty("number", "1");
        config.addProperty("number", "2");
        assertEquals("Wrong result", "1", config.getString("number"));
    }

    @Test
    public void testGetInterpolatedList()
    {
        config.addProperty("number", "1");
        config.addProperty("array", "${number}");
        config.addProperty("array", "${number}");

        final List<String> list = new ArrayList<>();
        list.add("1");
        list.add("1");

        ListAssert.assertEquals("'array' property", list, config.getList("array"));
    }

    @Test
    public void testGetInterpolatedPrimitives()
    {
        config.addProperty("number", "1");
        config.addProperty("value", "${number}");

        config.addProperty("boolean", "true");
        config.addProperty("booleanValue", "${boolean}");

        // primitive types
        assertEquals("boolean interpolation", true, config.getBoolean("booleanValue"));
        assertEquals("byte interpolation", 1, config.getByte("value"));
        assertEquals("short interpolation", 1, config.getShort("value"));
        assertEquals("int interpolation", 1, config.getInt("value"));
        assertEquals("long interpolation", 1, config.getLong("value"));
        assertEquals("float interpolation", 1, config.getFloat("value"), 0);
        assertEquals("double interpolation", 1, config.getDouble("value"), 0);

        // primitive wrappers
        assertEquals("Boolean interpolation", Boolean.TRUE, config.getBoolean("booleanValue", null));
        assertEquals("Byte interpolation", new Byte("1"), config.getByte("value", null));
        assertEquals("Short interpolation", new Short("1"), config.getShort("value", null));
        assertEquals("Integer interpolation", new Integer("1"), config.getInteger("value", null));
        assertEquals("Long interpolation", new Long("1"), config.getLong("value", null));
        assertEquals("Float interpolation", new Float("1"), config.getFloat("value", null));
        assertEquals("Double interpolation", new Double("1"), config.getDouble("value", null));

        assertEquals("BigInteger interpolation", new BigInteger("1"), config.getBigInteger("value", null));
        assertEquals("BigDecimal interpolation", new BigDecimal("1"), config.getBigDecimal("value", null));
    }

    @Test
    public void testCommaSeparatedString()
    {
        final String prop = "hey, that's a test";
        config.setProperty("prop.string", prop);
        final List<Object> list = config.getList("prop.string");
        assertEquals("Wrong number of list elements", 2, list.size());
        assertEquals("Wrong element 1", "hey", list.get(0));
    }

    @Test
    public void testCommaSeparatedStringEscaped()
    {
        final String prop2 = "hey\\, that's a test";
        config.setProperty("prop.string", prop2);
        assertEquals("Wrong value", "hey, that's a test", config.getString("prop.string"));
    }

    @Test
    public void testAddProperty() throws Exception
    {
        Collection<Object> props = new ArrayList<>();
        props.add("one");
        props.add("two,three,four");
        props.add(new String[] { "5.1", "5.2", "5.3,5.4", "5.5" });
        props.add("six");
        config.addProperty("complex.property", props);

        Object val = config.getProperty("complex.property");
        assertTrue(val instanceof Collection);
        Collection<?> col = (Collection<?>) val;
        assertEquals(10, col.size());

        props = new ArrayList<>();
        props.add("quick");
        props.add("brown");
        props.add("fox,jumps");
        final Object[] data = new Object[] {
                "The", props, "over,the", "lazy", "dog."
        };
        config.setProperty("complex.property", data);
        val = config.getProperty("complex.property");
        assertTrue(val instanceof Collection);
        col = (Collection<?>) val;
        final Iterator<?> it = col.iterator();
        final StringTokenizer tok = new StringTokenizer("The quick brown fox jumps over the lazy dog.", " ");
        while(tok.hasMoreTokens())
        {
            assertTrue(it.hasNext());
            assertEquals(tok.nextToken(), it.next());
        }
        assertFalse(it.hasNext());

        config.setProperty("complex.property", null);
        assertFalse(config.containsKey("complex.property"));
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
    public void testInterpolation()
    {
        InterpolationTestHelper.testInterpolation(config);
    }

    @Test
    public void testMultipleInterpolation()
    {
        InterpolationTestHelper.testMultipleInterpolation(config);
    }

    @Test
    public void testInterpolationLoop()
    {
        InterpolationTestHelper.testInterpolationLoop(config);
    }

    /**
     * Tests interpolation when a subset configuration is involved.
     */
    @Test
    public void testInterpolationSubset()
    {
        InterpolationTestHelper.testInterpolationSubset(config);
    }

    /**
     * Tests interpolation when the referred property is not found.
     */
    @Test
    public void testInterpolationUnknownProperty()
    {
        InterpolationTestHelper.testInterpolationUnknownProperty(config);
    }

    /**
     * Tests interpolation of system properties.
     */
    @Test
    public void testInterpolationSystemProperties()
    {
        InterpolationTestHelper.testInterpolationSystemProperties(config);
    }

    /**
     * Tests interpolation of environment properties.
     */
    @Test
    public void testInterpolationEnvironment()
    {
        InterpolationTestHelper.testInterpolationEnvironment(config);
    }

    /**
     * Tests interpolation of constant values.
     */
    @Test
    public void testInterpolationConstants()
    {
        InterpolationTestHelper.testInterpolationConstants(config);
    }

    /**
     * Tests whether a variable can be escaped, so that it won't be
     * interpolated.
     */
    @Test
    public void testInterpolationEscaped()
    {
        InterpolationTestHelper.testInterpolationEscaped(config);
    }

    /**
     * Tests interpolation with localhost values.
     */
    @Test
    public void testInterpolationLocalhost()
    {
        InterpolationTestHelper.testInterpolationLocalhost(config);
    }

    /**
     * Tests accessing and manipulating the interpolator object.
     */
    @Test
    public void testGetInterpolator()
    {
        InterpolationTestHelper.testGetInterpolator(config);
    }

    /**
     * Tests obtaining a configuration with all variables replaced by their
     * actual values.
     */
    @Test
    public void testInterpolatedConfiguration()
    {
        InterpolationTestHelper.testInterpolatedConfiguration(config);
    }

    /**
     * Tests whether a {@code ConfigurationInterpolator} can be set.
     */
    @Test
    public void testSetInterpolator()
    {
        final ConfigurationInterpolator interpolator =
                EasyMock.createMock(ConfigurationInterpolator.class);
        EasyMock.replay(interpolator);
        config.setInterpolator(interpolator);
        assertSame("Interpolator not set", interpolator,
                config.getInterpolator());
    }

    /**
     * Tests whether a {@code ConfigurationInterpolator} can be created and
     * installed.
     */
    @Test
    public void testInstallInterpolator()
    {
        final Lookup prefixLookup = EasyMock.createMock(Lookup.class);
        final Lookup defLookup = EasyMock.createMock(Lookup.class);
        EasyMock.replay(prefixLookup, defLookup);
        final Map<String, Lookup> prefixLookups = new HashMap<>();
        prefixLookups.put("test", prefixLookup);
        final List<Lookup> defLookups = new ArrayList<>();
        defLookups.add(defLookup);
        config.installInterpolator(prefixLookups, defLookups);
        final ConfigurationInterpolator interpolator = config.getInterpolator();
        assertEquals("Wrong prefix lookups", prefixLookups,
                interpolator.getLookups());
        final List<Lookup> defLookups2 = interpolator.getDefaultLookups();
        assertEquals("Wrong number of default lookups", 2, defLookups2.size());
        assertSame("Wrong default lookup 1", defLookup, defLookups2.get(0));
        final String var = "testVariable";
        final Object value = 42;
        config.addProperty(var, value);
        assertEquals("Wrong lookup result", value,
                defLookups2.get(1).lookup(var));
    }

    /**
     * Tests whether property access is possible without a
     * {@code ConfigurationInterpolator}.
     */
    @Test
    public void testNoInterpolator()
    {
        config.setProperty("test", "${value}");
        config.setInterpolator(null);
        assertEquals("Wrong result", "${value}", config.getString("test"));
    }

    @Test
    public void testGetHexadecimalValue()
    {
        config.setProperty("number", "0xFF");
        assertEquals("byte value", (byte) 0xFF, config.getByte("number"));

        config.setProperty("number", "0xFFFF");
        assertEquals("short value", (short) 0xFFFF, config.getShort("number"));

        config.setProperty("number", "0xFFFFFFFF");
        assertEquals("int value", 0xFFFFFFFF, config.getInt("number"));

        config.setProperty("number", "0xFFFFFFFFFFFFFFFF");
        assertEquals("long value", 0xFFFFFFFFFFFFFFFFL, config.getLong("number"));

        assertEquals("long value", 0xFFFFFFFFFFFFFFFFL, config.getBigInteger("number").longValue());
    }

    @Test
    public void testGetBinaryValue()
    {
        config.setProperty("number", "0b11111111");
        assertEquals("byte value", (byte) 0xFF, config.getByte("number"));

        config.setProperty("number", "0b1111111111111111");
        assertEquals("short value", (short) 0xFFFF, config.getShort("number"));

        config.setProperty("number", "0b11111111111111111111111111111111");
        assertEquals("int value", 0xFFFFFFFF, config.getInt("number"));

        config.setProperty("number", "0b1111111111111111111111111111111111111111111111111111111111111111");
        assertEquals("long value", 0xFFFFFFFFFFFFFFFFL, config.getLong("number"));

        assertEquals("long value", 0xFFFFFFFFFFFFFFFFL, config.getBigInteger("number").longValue());
    }

    /**
     * Tests if conversion between number types is possible.
     */
    @Test
    public void testNumberConversions()
    {
        config.setProperty(KEY_NUMBER, new Integer(42));
        assertEquals("Wrong int returned", 42, config.getInt(KEY_NUMBER));
        assertEquals("Wrong long returned", 42L, config.getLong(KEY_NUMBER));
        assertEquals("Wrong byte returned", (byte) 42, config
                .getByte(KEY_NUMBER));
        assertEquals("Wrong float returned", 42.0f,
                config.getFloat(KEY_NUMBER), 0.01f);
        assertEquals("Wrong double returned", 42.0, config
                .getDouble(KEY_NUMBER), 0.001);

        assertEquals("Wrong Long returned", new Long(42L), config.getLong(
                KEY_NUMBER, null));
        assertEquals("Wrong BigInt returned", new BigInteger("42"), config
                .getBigInteger(KEY_NUMBER));
        assertEquals("Wrong DigDecimal returned", new BigDecimal("42"), config
                .getBigDecimal(KEY_NUMBER));
    }

    /**
     * Tests cloning a BaseConfiguration.
     */
    @Test
    public void testClone()
    {
        for (int i = 0; i < 10; i++)
        {
            config.addProperty("key" + i, new Integer(i));
        }
        final BaseConfiguration config2 = (BaseConfiguration) config.clone();

        for (final Iterator<String> it = config.getKeys(); it.hasNext();)
        {
            final String key = it.next();
            assertTrue("Key not found: " + key, config2.containsKey(key));
            assertEquals("Wrong value for key " + key, config.getProperty(key),
                    config2.getProperty(key));
        }
    }

    /**
     * Tests whether a cloned configuration is decoupled from its original.
     */
    @Test
    public void testCloneModify()
    {
        final EventListener<ConfigurationEvent> l = new EventListenerTestImpl(config);
        config.addEventListener(ConfigurationEvent.ANY, l);
        config.addProperty("original", Boolean.TRUE);
        final BaseConfiguration config2 = (BaseConfiguration) config.clone();

        config2.addProperty("clone", Boolean.TRUE);
        assertFalse("New key appears in original", config.containsKey("clone"));
        config2.setProperty("original", Boolean.FALSE);
        assertTrue("Wrong value of original property", config
                .getBoolean("original"));

        assertTrue("Event listener was copied", config2
                .getEventListeners(ConfigurationEvent.ANY).isEmpty());
    }

    /**
     * Tests the clone() method if a list property is involved.
     */
    @Test
    public void testCloneListProperty()
    {
        final String key = "list";
        config.addProperty(key, "value1");
        config.addProperty(key, "value2");
        final BaseConfiguration config2 = (BaseConfiguration) config.clone();
        config2.addProperty(key, "value3");
        assertEquals("Wrong number of original properties", 2, config.getList(
                key).size());
    }

    /**
     * Tests whether interpolation works as expected after cloning.
     */
    @Test
    public void testCloneInterpolation()
    {
        final String keyAnswer = "answer";
        config.addProperty(keyAnswer, "The answer is ${" + KEY_NUMBER + "}.");
        config.addProperty(KEY_NUMBER, 42);
        final BaseConfiguration clone = (BaseConfiguration) config.clone();
        clone.setProperty(KEY_NUMBER, 43);
        assertEquals("Wrong interpolation in original", "The answer is 42.",
                config.getString(keyAnswer));
        assertEquals("Wrong interpolation in clone", "The answer is 43.",
                clone.getString(keyAnswer));
    }

    /**
     * Tests the specific size() implementation.
     */
    @Test
    public void testSize()
    {
        final int count = 16;
        for (int i = 0; i < count; i++)
        {
            config.addProperty("key" + i, "value" + i);
        }
        assertEquals("Wrong size", count, config.size());
    }
}
