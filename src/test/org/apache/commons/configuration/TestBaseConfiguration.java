package org.apache.commons.configuration;

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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * Tests some basic functions of the BaseConfiguration class
 *
 * @version $Id: TestBaseConfiguration.java,v 1.8 2004/03/09 15:34:02 epugh Exp $
 */
public class TestBaseConfiguration extends TestCase
{
    protected BaseConfiguration eprop = new BaseConfiguration();

    public void testGetProperty()
    {
        /* should be empty and return null */
        assertEquals("This returns null", eprop.getProperty("foo"), null);

        /* add a real value, and get it two different ways */
        eprop.setProperty("number", "1");
        assertEquals("This returns '1'", eprop.getProperty("number"), "1");
        assertEquals("This returns '1'", eprop.getString("number"), "1");
    }

    public void testGetByte()
    {
        eprop.setProperty("number", "1");
        byte oneB = 1, twoB = 2;
        assertEquals("This returns 1(byte)", eprop.getByte("number"), oneB);
        assertEquals(
            "This returns 1(byte)",
            eprop.getByte("number", twoB),
            oneB);
        assertEquals(
            "This returns 2(default byte)",
            eprop.getByte("numberNotInConfig", twoB),
            twoB);
        assertEquals(
            "This returns 1(Byte)",
            eprop.getByte("number", new Byte("2")),
            new Byte(oneB));
    }

    public void testGetShort()
    {
        eprop.setProperty("numberS", "1");
        short oneS = 1, twoS = 2;
        assertEquals("This returns 1(short)", eprop.getShort("numberS"), oneS);
        assertEquals(
            "This returns 1(short)",
            eprop.getShort("numberS", twoS),
            oneS);
        assertEquals(
            "This returns 2(default short)",
            eprop.getShort("numberNotInConfig", twoS),
            twoS);
        assertEquals(
            "This returns 1(Short)",
            eprop.getShort("numberS", new Short("2")),
            new Short(oneS));
    }

    public void testGetLong()
    {
        eprop.setProperty("numberL", "1");
        long oneL = 1, twoL = 2;
        assertEquals("This returns 1(long)", eprop.getLong("numberL"), oneL);
        assertEquals(
            "This returns 1(long)",
            eprop.getLong("numberL", twoL),
            oneL);
        assertEquals(
            "This returns 2(default long)",
            eprop.getLong("numberNotInConfig", twoL),
            twoL);
        assertEquals(
            "This returns 1(Long)",
            eprop.getLong("numberL", new Long("2")),
            new Long(oneL));
    }

    public void testGetFloat()
    {
        eprop.setProperty("numberF", "1.0");
        float oneF = 1, twoF = 2;
        assertEquals(
            "This returns 1(float)",
            eprop.getFloat("numberF"),
            oneF,
            0);
        assertEquals(
            "This returns 1(float)",
            eprop.getFloat("numberF", twoF),
            oneF,
            0);
        assertEquals(
            "This returns 2(default float)",
            eprop.getFloat("numberNotInConfig", twoF),
            twoF,
            0);
        assertEquals(
            "This returns 1(Float)",
            eprop.getFloat("numberF", new Float("2")),
            new Float(oneF));
    }

    public void testGetDouble()
    {
        eprop.setProperty("numberD", "1.0");
        double oneD = 1, twoD = 2;
        assertEquals(
            "This returns 1(double)",
            eprop.getDouble("numberD"),
            oneD,
            0);
        assertEquals(
            "This returns 1(double)",
            eprop.getDouble("numberD", twoD),
            oneD,
            0);
        assertEquals(
            "This returns 2(default double)",
            eprop.getDouble("numberNotInConfig", twoD),
            twoD,
            0);
        assertEquals(
            "This returns 1(Double)",
            eprop.getDouble("numberD", new Double("2")),
            new Double(oneD));
    }

    public void testGetBigDecimal()
    {
        eprop.setProperty("numberBigD", "123.456");
        BigDecimal number = new BigDecimal("123.456");
        BigDecimal defaultValue = new BigDecimal("654.321");

        assertEquals("Existing key", number, eprop.getBigDecimal("numberBigD"));
        assertEquals(
            "Existing key with default value",
            number,
            eprop.getBigDecimal("numberBigD", defaultValue));
        assertEquals(
            "Missing key with default value",
            defaultValue,
            eprop.getBigDecimal("numberNotInConfig", defaultValue));
    }

    public void testGetBigInteger()
    {
        eprop.setProperty("numberBigI", "1234567890");
        BigInteger number = new BigInteger("1234567890");
        BigInteger defaultValue = new BigInteger("654321");

        assertEquals("Existing key", number, eprop.getBigInteger("numberBigI"));
        assertEquals(
            "Existing key with default value",
            number,
            eprop.getBigInteger("numberBigI", defaultValue));
        assertEquals(
            "Missing key with default value",
            defaultValue,
            eprop.getBigInteger("numberNotInConfig", defaultValue));
    }

    public void testGetBoolean()
    {
        eprop.setProperty("boolA", Boolean.TRUE);
        boolean boolT = true, boolF = false;
        assertEquals("This returns true", eprop.getBoolean("boolA"), boolT);
        assertEquals(
            "This returns true, not the default",
            eprop.getBoolean("boolA", boolF),
            boolT);
        assertEquals(
            "This returns false(default)",
            eprop.getBoolean("boolNotInConfig", boolF),
            boolF);
        assertEquals(
            "This returns true(Boolean)",
            eprop.getBoolean("boolA", new Boolean(boolF)),
            new Boolean(boolT));
    }

    public void testGetList()
    {
        eprop.addProperty("number", "1");
        eprop.addProperty("number", "2");
        List list = eprop.getList("number");
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
            eprop.getString("number");
        }
        catch (NoSuchElementException nsse)
        {
            fail("Should return a string");
        }
    }

    public void testCommaSeparatedString()
    {
        String prop = "hey, that's a test";
        eprop.setProperty("prop.string", prop);
        try
        {
            eprop.getList("prop.string");
        }
        catch (NoSuchElementException nsse)
        {
            fail("Should return a list");
        }

        String prop2 = "hey\\, that's a test";
        eprop.clearProperty("prop.string");
        eprop.setProperty("prop.string", prop2);
        try
        {
            eprop.getString("prop.string");
        }
        catch (NoSuchElementException nsse)
        {
            fail("Should return a list");
        }

    }

    public void testPropertyAccess()
    {
        eprop.clearProperty("prop.properties");
        eprop.setProperty("prop.properties", "");
        assertEquals(
            "This returns an empty Properties object",
            eprop.getProperties("prop.properties"),
            new Properties());
        eprop.clearProperty("prop.properties");
        eprop.setProperty("prop.properties", "foo=bar, baz=moo, seal=clubber");

        Properties p = new Properties();
        p.setProperty("foo", "bar");
        p.setProperty("baz", "moo");
        p.setProperty("seal", "clubber");
        assertEquals(
            "This returns a filled in Properties object",
            eprop.getProperties("prop.properties"),
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
        eprop.setProperty("prop.string", prop2);
        eprop.setProperty("property.string", "hello");

        Configuration subEprop = eprop.subset("prop");

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
        
        subEprop = eprop.subset("prop.");
        it = subEprop.getKeys();
        assertFalse(it.hasNext());
    }

    public void testInterpolation() throws Exception
    {
        eprop.setProperty("applicationRoot", "/home/applicationRoot");
        eprop.setProperty("db", "${applicationRoot}/db/hypersonic");
        String unInterpolatedValue = "${applicationRoot2}/db/hypersonic";
        eprop.setProperty("dbFailedInterpolate", unInterpolatedValue);
        String dbProp = "/home/applicationRoot/db/hypersonic";

        //construct a new config, using eprop as the defaults config for it.
        BaseConfiguration superProp = eprop;

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
        eprop.setProperty("test.base-level", "/base-level");
        eprop.setProperty("test.first-level", "${test.base-level}/first-level");
        eprop.setProperty(
            "test.second-level",
            "${test.first-level}/second-level");
        eprop.setProperty(
            "test.third-level",
            "${test.second-level}/third-level");

        String expectedValue =
            "/base-level/first-level/second-level/third-level";

        assertEquals(eprop.getString("test.third-level"), expectedValue);
    }

    public void testInterpolationLoop() throws Exception
    {
        eprop.setProperty("test.a", "${test.b}");
        eprop.setProperty("test.b", "${test.a}");

        try
        {
            eprop.getString("test.a");
        }
        catch (IllegalStateException e)
        {
            return;
        }

        fail("IllegalStateException should have been thrown for looped property references");
    }

    
}
