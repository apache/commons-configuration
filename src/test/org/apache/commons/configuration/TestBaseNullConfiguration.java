/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import junit.framework.TestCase;
import junitx.framework.ObjectAssert;

/**
 * Tests some basic functions of the BaseConfiguration class. Missing keys might
 * return null.
 *
 * @version $Id: TestBaseNullConfiguration.java,v 1.4 2005/01/03 16:35:04 ebourg Exp $
 */
public class TestBaseNullConfiguration extends TestCase
{
    protected BaseConfiguration config = null;

    protected static Class missingElementException = NoSuchElementException.class;
    protected static Class incompatibleElementException = ConversionException.class;

    protected void setUp() throws Exception
    {
        config = new BaseConfiguration();
        config.setThrowExceptionOnMissing(false);
    }

    public void testThrowExceptionOnMissing()
    {
        assertFalse("Throw Exception Property is set!", config.isThrowExceptionOnMissing());
    }

    public void testGetProperty()
    {
        /* should be empty and return null */
        assertEquals("This returns null", config.getProperty("foo"), null);

        /* add a real value, and get it two different ways */
        config.setProperty("number", "1");
        assertEquals("This returns '1'", config.getProperty("number"), "1");
        assertEquals("This returns '1'", config.getString("number"), "1");
    }

    public void testGetByte()
    {
        config.setProperty("number", "1");
        byte oneB = 1;
        byte twoB = 2;
        assertEquals("This returns 1(byte)", oneB, config.getByte("number"));
        assertEquals("This returns 1(byte)", oneB, config.getByte("number", twoB));
        assertEquals("This returns 2(default byte)", twoB, config.getByte("numberNotInConfig", twoB));
        assertEquals("This returns 1(Byte)", new Byte(oneB), config.getByte("number", new Byte("2")));

        // missing key without default value
        Throwable t = null;
        try {
            config.getByte("numberNotInConfig");
        } catch (Throwable T) {
            t = T;
        }
        assertNotNull("No exception thrown for missing keys", t);
        ObjectAssert.assertInstanceOf("Exception thrown for missing keys", missingElementException, t);

        // existing key with an incompatible value
        config.setProperty("test.empty", "");
        t = null;
        try {
            config.getByte("test.empty");
        } catch (Throwable T) {
            t = T;
        }
        assertNotNull("No exception thrown for incompatible values", t);
        ObjectAssert.assertInstanceOf("Exception thrown for incompatible values", incompatibleElementException, t);
    }

    public void testGetShort()
    {
        config.setProperty("numberS", "1");
        short oneS = 1;
        short twoS = 2;
        assertEquals("This returns 1(short)", oneS, config.getShort("numberS"));
        assertEquals("This returns 1(short)", oneS, config.getShort("numberS", twoS));
        assertEquals("This returns 2(default short)", twoS, config.getShort("numberNotInConfig", twoS));
        assertEquals("This returns 1(Short)", new Short(oneS), config.getShort("numberS", new Short("2")));

        // missing key without default value
        Throwable t = null;
        try {
            config.getShort("numberNotInConfig");
        } catch (Throwable T) {
            t = T;
        }
        assertNotNull("No exception thrown for missing keys", t);
        ObjectAssert.assertInstanceOf("Exception thrown for missing keys", missingElementException, t);

        // existing key with an incompatible value
        config.setProperty("test.empty", "");
        t = null;
        try {
            config.getShort("test.empty");
        } catch (Throwable T) {
            t = T;
        }
        assertNotNull("No exception thrown for incompatible values", t);
        ObjectAssert.assertInstanceOf("Exception thrown for incompatible values", incompatibleElementException, t);
    }

    public void testGetLong()
    {
        config.setProperty("numberL", "1");
        long oneL = 1;
        long twoL = 2;
        assertEquals("This returns 1(long)", oneL, config.getLong("numberL"));
        assertEquals("This returns 1(long)", oneL, config.getLong("numberL", twoL));
        assertEquals("This returns 2(default long)", twoL, config.getLong("numberNotInConfig", twoL));
        assertEquals("This returns 1(Long)", new Long(oneL), config.getLong("numberL", new Long("2")));

        // missing key without default value
        Throwable t = null;
        try {
            config.getLong("numberNotInConfig");
        } catch (Throwable T) {
            t = T;
        }
        assertNotNull("No exception thrown for missing keys", t);
        ObjectAssert.assertInstanceOf("Exception thrown for missing keys", missingElementException, t);

        // existing key with an incompatible value
        config.setProperty("test.empty", "");
        t = null;
        try {
            config.getLong("test.empty");
        } catch (Throwable T) {
            t = T;
        }
        assertNotNull("No exception thrown for incompatible values", t);
        ObjectAssert.assertInstanceOf("Exception thrown for incompatible values", incompatibleElementException, t);
    }

    public void testGetFloat()
    {
        config.setProperty("numberF", "1.0");
        float oneF = 1;
        float twoF = 2;
        assertEquals("This returns 1(float)", oneF, config.getFloat("numberF"), 0);
        assertEquals("This returns 1(float)", oneF, config.getFloat("numberF", twoF), 0);
        assertEquals("This returns 2(default float)", twoF, config.getFloat("numberNotInConfig", twoF), 0);
        assertEquals("This returns 1(Float)", new Float(oneF), config.getFloat("numberF", new Float("2")));

        // missing key without default value
        Throwable t = null;
        try {
            config.getFloat("numberNotInConfig");
        } catch (Throwable T) {
            t = T;
        }
        assertNotNull("No exception thrown for missing keys", t);
        ObjectAssert.assertInstanceOf("Exception thrown for missing keys", missingElementException, t);

        // existing key with an incompatible value
        config.setProperty("test.empty", "");
        t = null;
        try {
            config.getFloat("test.empty");
        } catch (Throwable T) {
            t = T;
        }
        assertNotNull("No exception thrown for incompatible values", t);
        ObjectAssert.assertInstanceOf("Exception thrown for incompatible values", incompatibleElementException, t);
    }

    public void testGetDouble()
    {
        config.setProperty("numberD", "1.0");
        double oneD = 1;
        double twoD = 2;
        assertEquals("This returns 1(double)", oneD, config.getDouble("numberD"), 0);
        assertEquals("This returns 1(double)", oneD, config.getDouble("numberD", twoD), 0);
        assertEquals("This returns 2(default double)", twoD, config.getDouble("numberNotInConfig", twoD), 0);
        assertEquals("This returns 1(Double)", new Double(oneD), config.getDouble("numberD", new Double("2")));

        // missing key without default value
        Throwable t = null;
        try {
            config.getDouble("numberNotInConfig");
        } catch (Throwable T) {
            t = T;
        }
        assertNotNull("No exception thrown for missing keys", t);
        ObjectAssert.assertInstanceOf("Exception thrown for missing keys", missingElementException, t);

        // existing key with an incompatible value
        config.setProperty("test.empty", "");
        t = null;
        try {
            config.getDouble("test.empty");
        } catch (Throwable T) {
            t = T;
        }
        assertNotNull("No exception thrown for incompatible values", t);
        ObjectAssert.assertInstanceOf("Exception thrown for incompatible values", incompatibleElementException, t);
    }

    public void testGetBigDecimal()
    {
        config.setProperty("numberBigD", "123.456");
        BigDecimal number = new BigDecimal("123.456");
        BigDecimal defaultValue = new BigDecimal("654.321");

        assertEquals("Existing key", number, config.getBigDecimal("numberBigD"));
        assertEquals("Existing key with default value", number, config.getBigDecimal("numberBigD", defaultValue));
        assertEquals("Missing key with default value", defaultValue, config.getBigDecimal("numberNotInConfig", defaultValue));

        // missing key without default value
                assertEquals("Missing Key is not null!", null, config.getBigDecimal("numberNotInConfig"));

        // existing key with an incompatible value
        config.setProperty("test.empty", "");
        Throwable t = null;
        try {
            config.getBigDecimal("test.empty");
        } catch (Throwable T) {
            t = T;
        }
        assertNotNull("No exception thrown for incompatible values", t);
        ObjectAssert.assertInstanceOf("Exception thrown for incompatible values", incompatibleElementException, t);
    }

    public void testGetBigInteger()
    {
        config.setProperty("numberBigI", "1234567890");
        BigInteger number = new BigInteger("1234567890");
        BigInteger defaultValue = new BigInteger("654321");

        assertEquals("Existing key", number, config.getBigInteger("numberBigI"));
        assertEquals("Existing key with default value", number, config.getBigInteger("numberBigI", defaultValue));
        assertEquals("Missing key with default value", defaultValue, config.getBigInteger("numberNotInConfig", defaultValue));

        // missing key without default value
                assertEquals("Missing Key is not null!", null, config.getBigInteger("numberNotInConfig"));

        // existing key with an incompatible value
        config.setProperty("test.empty", "");
        Throwable t = null;
        try {
            config.getBigInteger("test.empty");
        } catch (Throwable T) {
            t = T;
        }
        assertNotNull("No exception thrown for incompatible values", t);
        ObjectAssert.assertInstanceOf("Exception thrown for incompatible values", incompatibleElementException, t);
    }


    public void testGetString()
    {
        config.setProperty("testString", "The quick brown fox");
        String string = new String("The quick brown fox");
        String defaultValue = new String("jumps over the lazy dog");

        assertEquals("Existing key", string, config.getString("testString"));
        assertEquals("Existing key with default value", string, config.getString("testString", defaultValue));
        assertEquals("Missing key with default value", defaultValue, config.getString("stringNotInConfig", defaultValue));

        // missing key without default value
                assertEquals("Missing Key is not null!", null, config.getString("stringNotInConfig"));

    }

    public void testGetBoolean()
    {
        config.setProperty("boolA", Boolean.TRUE);
        boolean boolT = true, boolF = false;
        assertEquals("This returns true", boolT, config.getBoolean("boolA"));
        assertEquals("This returns true, not the default", boolT, config.getBoolean("boolA", boolF));
        assertEquals("This returns false(default)", boolF, config.getBoolean("boolNotInConfig", boolF));
        assertEquals("This returns true(Boolean)", new Boolean(boolT), config.getBoolean("boolA", new Boolean(boolF)));

        // missing key without default value
        Throwable t = null;
        try {
            config.getBoolean("numberNotInConfig");
        } catch (Throwable T) {
            t = T;
        }
        assertNotNull("No exception thrown for missing keys", t);
        ObjectAssert.assertInstanceOf("Exception thrown for missing keys", missingElementException, t);

        // existing key with an incompatible value
        config.setProperty("test.empty", "");
        t = null;
        try {
            config.getBoolean("test.empty");
        } catch (Throwable T) {
            t = T;
        }
        assertNotNull("No exception thrown for incompatible values", t);
        ObjectAssert.assertInstanceOf("Exception thrown for incompatible values", incompatibleElementException, t);
    }

    public void testGetList()
    {
        config.addProperty("number", "1");
        config.addProperty("number", "2");
        List list = config.getList("number");
        assertNotNull("The list is null", list);
        assertEquals("List size", 2, list.size());
        assertTrue("The number 1 is missing from the list", list.contains("1"));
        assertTrue("The number 2 is missing from the list", list.contains("2"));

        /*
         *  now test dan's new fix where we get the first scalar
         *  when we access a list valued property
         */
        try
        {
            config.getString("number");
        }
        catch (NoSuchElementException nsse)
        {
            fail("Should return a string");
        }
    }

    public void testCommaSeparatedString()
    {
        String prop = "hey, that's a test";
        config.setProperty("prop.string", prop);
        try
        {
            config.getList("prop.string");
        }
        catch (NoSuchElementException nsse)
        {
            fail("Should return a list");
        }

        String prop2 = "hey\\, that's a test";
        config.clearProperty("prop.string");
        config.setProperty("prop.string", prop2);
        try
        {
            config.getString("prop.string");
        }
        catch (NoSuchElementException nsse)
        {
            fail("Should return a list");
        }

    }

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

        Properties p = new Properties();
        p.setProperty("foo", "bar");
        p.setProperty("baz", "moo");
        p.setProperty("seal", "clubber");
        assertEquals(
            "This returns a filled in Properties object",
            config.getProperties("prop.properties"),
            p);
    }

    public void testSubset()
    {
        /*
         * test subset : assure we don't reprocess the data elements
         * when generating the subset
         */

        String prop = "hey, that's a test";
        String prop2 = "hey\\, that's a test";
        config.setProperty("prop.string", prop2);
        config.setProperty("property.string", "hello");

        Configuration subEprop = config.subset("prop");

        assertEquals(
            "Returns the full string",
            prop,
            subEprop.getString("string"));
        try
        {
            subEprop.getString("string");
        }
        catch (NoSuchElementException nsse)
        {
            fail("Should return a string");
        }
        try
        {
            subEprop.getList("string");
        }
        catch (NoSuchElementException nsse)
        {
            fail("Should return a list");
        }

        Iterator it = subEprop.getKeys();
        it.next();
        assertFalse(it.hasNext());

        subEprop = config.subset("prop.");
        it = subEprop.getKeys();
        assertFalse(it.hasNext());
    }

    public void testInterpolation() throws Exception
    {
        config.setProperty("applicationRoot", "/home/applicationRoot");
        config.setProperty("db", "${applicationRoot}/db/hypersonic");
        String unInterpolatedValue = "${applicationRoot2}/db/hypersonic";
        config.setProperty("dbFailedInterpolate", unInterpolatedValue);
        String dbProp = "/home/applicationRoot/db/hypersonic";

        //construct a new config, using config as the defaults config for it.
        BaseConfiguration superProp = config;

        assertEquals(
            "Checking interpolated variable",dbProp,
            superProp.getString("db"));
        assertEquals(
            "lookup fails, leave variable as is",
            superProp.getString("dbFailedInterpolate"),
            unInterpolatedValue);

        superProp.setProperty("arrayInt", "${applicationRoot}/1");
        String[] arrayInt = superProp.getStringArray("arrayInt");
        assertEquals(
            "check first entry was interpolated",
            "/home/applicationRoot/1",
            arrayInt[0]);
    }

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

        String expectedValue =
            "/base-level/first-level/second-level/third-level";

        assertEquals(config.getString("test.third-level"), expectedValue);
    }

    public void testInterpolationLoop() throws Exception
    {
        config.setProperty("test.a", "${test.b}");
        config.setProperty("test.b", "${test.a}");

        try
        {
            config.getString("test.a");
        }
        catch (IllegalStateException e)
        {
            return;
        }

        fail("IllegalStateException should have been thrown for looped property references");
    }

}

