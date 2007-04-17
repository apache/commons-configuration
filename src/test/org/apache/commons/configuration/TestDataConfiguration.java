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

package org.apache.commons.configuration;

import java.awt.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;
import junitx.framework.ArrayAssert;
import junitx.framework.ListAssert;

/**
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class TestDataConfiguration extends TestCase
{
    private DataConfiguration conf;

    protected void setUp() throws Exception
    {
        conf = new DataConfiguration(new BaseConfiguration());

        // empty value
        conf.addProperty("empty", "");

        // lists of boolean
        conf.addProperty("boolean.list1", "true");
        conf.addProperty("boolean.list1", "false");
        conf.addProperty("boolean.list2", "true, false");
        conf.addProperty("boolean.list3", Boolean.TRUE);
        conf.addProperty("boolean.list3", Boolean.FALSE);
        conf.addPropertyDirect("boolean.list4", new Boolean[] { Boolean.TRUE, Boolean.FALSE });
        conf.addPropertyDirect("boolean.list5", new boolean[] { true, false });
        List booleans = new ArrayList();
        booleans.add(Boolean.TRUE);
        booleans.add(Boolean.FALSE);
        conf.addProperty("boolean.list6", booleans);
        conf.addProperty("boolean.string", "true");
        conf.addProperty("boolean.object", Boolean.TRUE);
        conf.addProperty("boolean.list.interpolated", "${boolean.string},false");

        // lists of bytes
        conf.addProperty("byte.list1", "1");
        conf.addProperty("byte.list1", "2");
        conf.addProperty("byte.list2", "1, 2");
        conf.addProperty("byte.list3", new Byte("1"));
        conf.addProperty("byte.list3", new Byte("2"));
        conf.addPropertyDirect("byte.list4", new Byte[] { new Byte("1"), new Byte("2") });
        conf.addPropertyDirect("byte.list5", new byte[] { 1, 2 });
        List bytes = new ArrayList();
        bytes.add(new Byte("1"));
        bytes.add(new Byte("2"));
        conf.addProperty("byte.list6", bytes);
        conf.addProperty("byte.string", "1");
        conf.addProperty("byte.object", new Byte("1"));
        conf.addProperty("byte.list.interpolated", "${byte.string},2");

        // lists of shorts
        conf.addProperty("short.list1", "1");
        conf.addProperty("short.list1", "2");
        conf.addProperty("short.list2", "1, 2");
        conf.addProperty("short.list3", new Short("1"));
        conf.addProperty("short.list3", new Short("2"));
        conf.addPropertyDirect("short.list4", new Short[] { new Short("1"), new Short("2") });
        conf.addPropertyDirect("short.list5", new short[] { 1, 2 });
        List shorts = new ArrayList();
        shorts.add(new Short("1"));
        shorts.add(new Short("2"));
        conf.addProperty("short.list6", shorts);
        conf.addProperty("short.string", "1");
        conf.addProperty("short.object", new Short("1"));
        conf.addProperty("short.list.interpolated", "${short.string},2");

        // lists of integers
        conf.addProperty("integer.list1", "1");
        conf.addProperty("integer.list1", "2");
        conf.addProperty("integer.list2", "1, 2");
        conf.addProperty("integer.list3", new Integer("1"));
        conf.addProperty("integer.list3", new Integer("2"));
        conf.addPropertyDirect("integer.list4", new Integer[] { new Integer("1"), new Integer("2") });
        conf.addPropertyDirect("integer.list5", new int[] { 1, 2 });
        List integers = new ArrayList();
        integers.add(new Integer("1"));
        integers.add(new Integer("2"));
        conf.addProperty("integer.list6", integers);
        conf.addProperty("integer.string", "1");
        conf.addProperty("integer.object", new Integer("1"));
        conf.addProperty("integer.list.interpolated", "${integer.string},2");

        // lists of longs
        conf.addProperty("long.list1", "1");
        conf.addProperty("long.list1", "2");
        conf.addProperty("long.list2", "1, 2");
        conf.addProperty("long.list3", new Long("1"));
        conf.addProperty("long.list3", new Long("2"));
        conf.addPropertyDirect("long.list4", new Long[] { new Long("1"), new Long("2") });
        conf.addPropertyDirect("long.list5", new long[] { 1, 2 });
        List longs = new ArrayList();
        longs.add(new Long("1"));
        longs.add(new Long("2"));
        conf.addProperty("long.list6", longs);
        conf.addProperty("long.string", "1");
        conf.addProperty("long.object", new Long("1"));
        conf.addProperty("long.list.interpolated", "${long.string},2");

        // lists of floats
        conf.addProperty("float.list1", "1");
        conf.addProperty("float.list1", "2");
        conf.addProperty("float.list2", "1, 2");
        conf.addProperty("float.list3", new Float("1"));
        conf.addProperty("float.list3", new Float("2"));
        conf.addPropertyDirect("float.list4", new Float[] { new Float("1"), new Float("2") });
        conf.addPropertyDirect("float.list5", new float[] { 1, 2 });
        List floats = new ArrayList();
        floats.add(new Float("1"));
        floats.add(new Float("2"));
        conf.addProperty("float.list6", floats);
        conf.addProperty("float.string", "1");
        conf.addProperty("float.object", new Float("1"));
        conf.addProperty("float.list.interpolated", "${float.string},2");

        // lists of doubles
        conf.addProperty("double.list1", "1");
        conf.addProperty("double.list1", "2");
        conf.addProperty("double.list2", "1, 2");
        conf.addProperty("double.list3", new Double("1"));
        conf.addProperty("double.list3", new Double("2"));
        conf.addPropertyDirect("double.list4", new Double[] { new Double("1"), new Double("2") });
        conf.addPropertyDirect("double.list5", new double[] { 1, 2 });
        List doubles = new ArrayList();
        doubles.add(new Double("1"));
        doubles.add(new Double("2"));
        conf.addProperty("double.list6", doubles);
        conf.addProperty("double.string", "1");
        conf.addProperty("double.object", new Double("1"));
        conf.addProperty("double.list.interpolated", "${double.string},2");

        // lists of big integers
        conf.addProperty("biginteger.list1", "1");
        conf.addProperty("biginteger.list1", "2");
        conf.addProperty("biginteger.list2", "1, 2");
        conf.addProperty("biginteger.list3", new BigInteger("1"));
        conf.addProperty("biginteger.list3", new BigInteger("2"));
        conf.addPropertyDirect("biginteger.list4", new BigInteger[] { new BigInteger("1"), new BigInteger("2") });
        List bigintegers = new ArrayList();
        bigintegers.add(new BigInteger("1"));
        bigintegers.add(new BigInteger("2"));
        conf.addProperty("biginteger.list6", bigintegers);
        conf.addProperty("biginteger.string", "1");
        conf.addProperty("biginteger.object", new BigInteger("1"));
        conf.addProperty("biginteger.list.interpolated", "${biginteger.string},2");

        // lists of big decimals
        conf.addProperty("bigdecimal.list1", "1");
        conf.addProperty("bigdecimal.list1", "2");
        conf.addProperty("bigdecimal.list2", "1, 2");
        conf.addProperty("bigdecimal.list3", new BigDecimal("1"));
        conf.addProperty("bigdecimal.list3", new BigDecimal("2"));
        conf.addPropertyDirect("bigdecimal.list4", new BigDecimal[] { new BigDecimal("1"), new BigDecimal("2") });
        List bigdecimals = new ArrayList();
        bigdecimals.add(new BigDecimal("1"));
        bigdecimals.add(new BigDecimal("2"));
        conf.addProperty("bigdecimal.list6", bigdecimals);
        conf.addProperty("bigdecimal.string", "1");
        conf.addProperty("bigdecimal.object", new BigDecimal("1"));
        conf.addProperty("bigdecimal.list.interpolated", "${bigdecimal.string},2");

        // URLs
        String url1 = "http://jakarta.apache.org";
        String url2 = "http://www.apache.org";
        conf.addProperty("url.string", url1);
        conf.addProperty("url.string.interpolated", "${url.string}");
        conf.addProperty("url.object", new URL(url1));
        conf.addProperty("url.list1", url1);
        conf.addProperty("url.list1", url2);
        conf.addProperty("url.list2", url1 + ", " + url2);
        conf.addProperty("url.list3", new URL(url1));
        conf.addProperty("url.list3", new URL(url2));
        conf.addPropertyDirect("url.list4", new URL[] { new URL(url1), new URL(url2) });
        List urls = new ArrayList();
        urls.add(new URL(url1));
        urls.add(new URL(url2));
        conf.addProperty("url.list6", urls);
        conf.addProperty("url.list.interpolated", "${url.string}," + url2);

        // Locales
        conf.addProperty("locale.string", "fr");
        conf.addProperty("locale.string.interpolated", "${locale.string}");
        conf.addProperty("locale.object", Locale.FRENCH);
        conf.addProperty("locale.list1", "fr");
        conf.addProperty("locale.list1", "de");
        conf.addProperty("locale.list2", "fr, de");
        conf.addProperty("locale.list3", Locale.FRENCH);
        conf.addProperty("locale.list3", Locale.GERMAN);
        conf.addPropertyDirect("locale.list4", new Locale[] { Locale.FRENCH, Locale.GERMAN });
        List locales = new ArrayList();
        locales.add(Locale.FRENCH);
        locales.add(Locale.GERMAN);
        conf.addProperty("locale.list6", locales);
        conf.addProperty("locale.list.interpolated", "${locale.string},de");

        // Colors
        String color1 = "FF0000";
        String color2 = "0000FF";
        conf.addProperty("color.string", color1);
        conf.addProperty("color.string.interpolated", "${color.string}");
        conf.addProperty("color.object", Color.red);
        conf.addProperty("color.list1", color1);
        conf.addProperty("color.list1", color2);
        conf.addProperty("color.list2", color1 + ", " + color2);
        conf.addProperty("color.list3", Color.red);
        conf.addProperty("color.list3", Color.blue);
        conf.addPropertyDirect("color.list4", new Color[] { Color.red, Color.blue });
        List colors = new ArrayList();
        colors.add(Color.red);
        colors.add(Color.blue);
        conf.addProperty("color.list6", colors);
        conf.addProperty("color.list.interpolated", "${color.string}," + color2);

        // Dates & Calendars
        String pattern = "yyyy-MM-dd";
        DateFormat format = new SimpleDateFormat(pattern);
        conf.setProperty(DataConfiguration.DATE_FORMAT_KEY, pattern);

        Date date1 = format.parse("2004-01-01");
        Date date2 = format.parse("2004-12-31");
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        conf.addProperty("date.string", "2004-01-01");
        conf.addProperty("date.string.interpolated", "${date.string}");
        conf.addProperty("date.object", date1);
        conf.addProperty("date.list1", "2004-01-01");
        conf.addProperty("date.list1", "2004-12-31");
        conf.addProperty("date.list2", "2004-01-01, 2004-12-31");
        conf.addProperty("date.list3", date1);
        conf.addProperty("date.list3", date2);
        conf.addPropertyDirect("date.list4", new Date[] { date1, date2 });
        conf.addPropertyDirect("date.list5", new Calendar[] { calendar1, calendar2 });
        List dates = new ArrayList();
        dates.add(date1);
        dates.add(date2);
        conf.addProperty("date.list6", dates);
        conf.addProperty("date.list.interpolated", "${date.string},2004-12-31");

        conf.addProperty("calendar.string", "2004-01-01");
        conf.addProperty("calendar.string.interpolated", "${calendar.string}");
        conf.addProperty("calendar.object", calendar1);
        conf.addProperty("calendar.list1", "2004-01-01");
        conf.addProperty("calendar.list1", "2004-12-31");
        conf.addProperty("calendar.list2", "2004-01-01, 2004-12-31");
        conf.addProperty("calendar.list3", calendar1);
        conf.addProperty("calendar.list3", calendar2);
        conf.addPropertyDirect("calendar.list4", new Calendar[] { calendar1, calendar2 });
        conf.addPropertyDirect("calendar.list5", new Date[] { date1, date2 });
        List calendars = new ArrayList();
        calendars.add(date1);
        calendars.add(date2);
        conf.addProperty("calendar.list6", calendars);
        conf.addProperty("calendar.list.interpolated", "${calendar.string},2004-12-31");
    }

    public void testGetBooleanArray()
    {
        // missing list
        boolean[] defaultValue = new boolean[] { false, true };
        ArrayAssert.assertEquals(defaultValue, conf.getBooleanArray("boolean.list", defaultValue));

        boolean[] expected = new boolean[] { true, false };

        // list of strings
        ArrayAssert.assertEquals(expected, conf.getBooleanArray("boolean.list1"));

        // list of strings, comma separated
        ArrayAssert.assertEquals(expected, conf.getBooleanArray("boolean.list2"));

        // list of Boolean objects
        ArrayAssert.assertEquals(expected, conf.getBooleanArray("boolean.list3"));

        // array of Boolean objects
        ArrayAssert.assertEquals(expected, conf.getBooleanArray("boolean.list4"));

        // array of boolean primitives
        ArrayAssert.assertEquals(expected, conf.getBooleanArray("boolean.list5"));

        // list of Boolean objects
        ArrayAssert.assertEquals(expected, conf.getBooleanArray("boolean.list6"));

        // list of interpolated values
        ArrayAssert.assertEquals(expected, conf.getBooleanArray("boolean.list.interpolated"));

        // single boolean values
        ArrayAssert.assertEquals(new boolean[] { true }, conf.getBooleanArray("boolean.string"));
        ArrayAssert.assertEquals(new boolean[] { true }, conf.getBooleanArray("boolean.object"));

        // empty array
        ArrayAssert.assertEquals(new boolean[] { }, conf.getBooleanArray("empty"));
    }

    public void testGetBooleanList()
    {
        // missing list
        ListAssert.assertEquals(null, conf.getBooleanList("boolean.list", null));

        List expected = new ArrayList();
        expected.add(Boolean.TRUE);
        expected.add(Boolean.FALSE);

        // list of strings
        ListAssert.assertEquals(expected, conf.getBooleanList("boolean.list1"));

        // list of strings, comma separated
        ListAssert.assertEquals(expected, conf.getBooleanList("boolean.list2"));

        // list of Boolean objects
        ListAssert.assertEquals(expected, conf.getBooleanList("boolean.list3"));

        // array of Boolean objects
        ListAssert.assertEquals(expected, conf.getBooleanList("boolean.list4"));

        // array of boolean primitives
        ListAssert.assertEquals(expected, conf.getBooleanList("boolean.list5"));

        // list of Boolean objects
        ListAssert.assertEquals(expected, conf.getBooleanList("boolean.list6"));

        // list of interpolated values
        ListAssert.assertEquals(expected, conf.getBooleanList("boolean.list.interpolated"));

        // single boolean values
        expected = new ArrayList();
        expected.add(Boolean.TRUE);
        ListAssert.assertEquals(expected, conf.getBooleanList("boolean.string"));
        ListAssert.assertEquals(expected, conf.getBooleanList("boolean.object"));

        // empty list
        ListAssert.assertEquals(new ArrayList(), conf.getBooleanList("empty"));
    }

    public void testGetByteArray()
    {
        // missing list
        byte[] defaultValue = new byte[] { 1, 2};
        ArrayAssert.assertEquals(defaultValue, conf.getByteArray("byte.list", defaultValue));

        byte[] expected = new byte[] { 1, 2 };

        // list of strings
        ArrayAssert.assertEquals(expected, conf.getByteArray("byte.list1"));

        // list of strings, comma separated
        ArrayAssert.assertEquals(expected, conf.getByteArray("byte.list2"));

        // list of Byte objects
        ArrayAssert.assertEquals(expected, conf.getByteArray("byte.list3"));

        // array of Byte objects
        ArrayAssert.assertEquals(expected, conf.getByteArray("byte.list4"));

        // array of byte primitives
        ArrayAssert.assertEquals(expected, conf.getByteArray("byte.list5"));

        // list of Byte objects
        ArrayAssert.assertEquals(expected, conf.getByteArray("byte.list6"));

        // list of interpolated values
        ArrayAssert.assertEquals(expected, conf.getByteArray("byte.list.interpolated"));

        // single byte values
        ArrayAssert.assertEquals(new byte[] { 1 }, conf.getByteArray("byte.string"));
        ArrayAssert.assertEquals(new byte[] { 1 }, conf.getByteArray("byte.object"));

        // empty array
        ArrayAssert.assertEquals(new byte[] { }, conf.getByteArray("empty"));
    }

    public void testGetByteList()
    {
        // missing list
        ListAssert.assertEquals(null, conf.getByteList("byte.list", null));

        List expected = new ArrayList();
        expected.add(new Byte("1"));
        expected.add(new Byte("2"));

        // list of strings
        ListAssert.assertEquals(expected, conf.getByteList("byte.list1"));

        // list of strings, comma separated
        ListAssert.assertEquals(expected, conf.getByteList("byte.list2"));

        // list of Byte objects
        ListAssert.assertEquals(expected, conf.getByteList("byte.list3"));

        // array of Byte objects
        ListAssert.assertEquals(expected, conf.getByteList("byte.list4"));

        // array of byte primitives
        ListAssert.assertEquals(expected, conf.getByteList("byte.list5"));

        // list of Byte objects
        ListAssert.assertEquals(expected, conf.getByteList("byte.list6"));

        // list of interpolated values
        ListAssert.assertEquals(expected, conf.getByteList("byte.list.interpolated"));

        // single byte values
        expected = new ArrayList();
        expected.add(new Byte("1"));
        ListAssert.assertEquals(expected, conf.getByteList("byte.string"));
        ListAssert.assertEquals(expected, conf.getByteList("byte.object"));

        // empty list
        ListAssert.assertEquals(new ArrayList(), conf.getByteList("empty"));
    }

    public void testGetShortArray()
    {
        // missing list
        short[] defaultValue = new short[] { 2, 1};
        ArrayAssert.assertEquals(defaultValue, conf.getShortArray("short.list", defaultValue));

        short[] expected = new short[] { 1, 2 };

        // list of strings
        ArrayAssert.assertEquals(expected, conf.getShortArray("short.list1"));

        // list of strings, comma separated
        ArrayAssert.assertEquals(expected, conf.getShortArray("short.list2"));

        // list of Byte objects
        ArrayAssert.assertEquals(expected, conf.getShortArray("short.list3"));

        // array of Byte objects
        ArrayAssert.assertEquals(expected, conf.getShortArray("short.list4"));

        // array of byte primitives
        ArrayAssert.assertEquals(expected, conf.getShortArray("short.list5"));

        // list of Byte objects
        ArrayAssert.assertEquals(expected, conf.getShortArray("short.list6"));

        // list of interpolated values
        ArrayAssert.assertEquals(expected, conf.getShortArray("short.list.interpolated"));

        // single byte values
        ArrayAssert.assertEquals(new short[] { 1 }, conf.getShortArray("short.string"));
        ArrayAssert.assertEquals(new short[] { 1 }, conf.getShortArray("short.object"));

        // empty array
        ArrayAssert.assertEquals(new short[] { }, conf.getShortArray("empty"));
    }

    public void testGetShortList()
    {
        // missing list
        ListAssert.assertEquals(null, conf.getShortList("short.list", null));

        List expected = new ArrayList();
        expected.add(new Short("1"));
        expected.add(new Short("2"));

        // list of strings
        ListAssert.assertEquals(expected, conf.getShortList("short.list1"));

        // list of strings, comma separated
        ListAssert.assertEquals(expected, conf.getShortList("short.list2"));

        // list of Short objects
        ListAssert.assertEquals(expected, conf.getShortList("short.list3"));

        // array of Short objects
        ListAssert.assertEquals(expected, conf.getShortList("short.list4"));

        // array of short primitives
        ListAssert.assertEquals(expected, conf.getShortList("short.list5"));

        // list of Short objects
        ListAssert.assertEquals(expected, conf.getShortList("short.list6"));

        // list of interpolated values
        ListAssert.assertEquals(expected, conf.getShortList("short.list.interpolated"));

        // single short values
        expected = new ArrayList();
        expected.add(new Short("1"));
        ListAssert.assertEquals(expected, conf.getShortList("short.string"));
        ListAssert.assertEquals(expected, conf.getShortList("short.object"));

        // empty list
        ListAssert.assertEquals(new ArrayList(), conf.getShortList("empty"));
    }

    public void testGetIntegerArray()
    {
        // missing list
        int[] defaultValue = new int[] { 2, 1};
        ArrayAssert.assertEquals(defaultValue, conf.getIntArray("integer.list", defaultValue));

        int[] expected = new int[] { 1, 2 };

        // list of strings
        ArrayAssert.assertEquals(expected, conf.getIntArray("integer.list1"));

        // list of strings, comma separated
        ArrayAssert.assertEquals(expected, conf.getIntArray("integer.list2"));

        // list of Integer objects
        ArrayAssert.assertEquals(expected, conf.getIntArray("integer.list3"));

        // array of Integer objects
        ArrayAssert.assertEquals(expected, conf.getIntArray("integer.list4"));

        // array of int primitives
        ArrayAssert.assertEquals(expected, conf.getIntArray("integer.list5"));

        // list of Integer objects
        ArrayAssert.assertEquals(expected, conf.getIntArray("integer.list6"));

        // list of interpolated values
        ArrayAssert.assertEquals(expected, conf.getIntArray("integer.list.interpolated"));

        // single int values
        ArrayAssert.assertEquals(new int[] { 1 }, conf.getIntArray("integer.string"));
        ArrayAssert.assertEquals(new int[] { 1 }, conf.getIntArray("integer.object"));

        // empty array
        ArrayAssert.assertEquals(new int[] { }, conf.getIntArray("empty"));
    }

    public void testGetIntegerList()
    {
        // missing list
        ListAssert.assertEquals(null, conf.getIntegerList("integer.list", null));

        List expected = new ArrayList();
        expected.add(new Integer("1"));
        expected.add(new Integer("2"));

        // list of strings
        ListAssert.assertEquals(expected, conf.getIntegerList("integer.list1"));

        // list of strings, comma separated
        ListAssert.assertEquals(expected, conf.getIntegerList("integer.list2"));

        // list of Integer objects
        ListAssert.assertEquals(expected, conf.getIntegerList("integer.list3"));

        // array of Integer objects
        ListAssert.assertEquals(expected, conf.getIntegerList("integer.list4"));

        // array of int primitives
        ListAssert.assertEquals(expected, conf.getIntegerList("integer.list5"));

        // list of Integer objects
        ListAssert.assertEquals(expected, conf.getIntegerList("integer.list6"));

        // list of interpolated values
        ListAssert.assertEquals(expected, conf.getIntegerList("integer.list.interpolated"));

        // single int values
        expected = new ArrayList();
        expected.add(new Integer("1"));
        ListAssert.assertEquals(expected, conf.getIntegerList("integer.string"));
        ListAssert.assertEquals(expected, conf.getIntegerList("integer.object"));

        // empty list
        ListAssert.assertEquals(new ArrayList(), conf.getIntegerList("empty"));
    }

    public void testGetLongArray()
    {
        // missing list
        long[] defaultValue = new long[] { 2, 1};
        ArrayAssert.assertEquals(defaultValue, conf.getLongArray("long.list", defaultValue));

        long[] expected = new long[] { 1, 2 };

        // list of strings
        ArrayAssert.assertEquals(expected, conf.getLongArray("long.list1"));

        // list of strings, comma separated
        ArrayAssert.assertEquals(expected, conf.getLongArray("long.list2"));

        // list of Long objects
        ArrayAssert.assertEquals(expected, conf.getLongArray("long.list3"));

        // array of Long objects
        ArrayAssert.assertEquals(expected, conf.getLongArray("long.list4"));

        // array of long primitives
        ArrayAssert.assertEquals(expected, conf.getLongArray("long.list5"));

        // list of Long objects
        ArrayAssert.assertEquals(expected, conf.getLongArray("long.list6"));

        // list of interpolated values
        ArrayAssert.assertEquals(expected, conf.getLongArray("long.list.interpolated"));

        // single long values
        ArrayAssert.assertEquals(new long[] { 1 }, conf.getLongArray("long.string"));
        ArrayAssert.assertEquals(new long[] { 1 }, conf.getLongArray("long.object"));

        // empty array
        ArrayAssert.assertEquals(new long[] { }, conf.getLongArray("empty"));
    }

    public void testGetLongList()
    {
        // missing list
        ListAssert.assertEquals(null, conf.getLongList("long.list", null));

        List expected = new ArrayList();
        expected.add(new Long("1"));
        expected.add(new Long("2"));

        // list of strings
        ListAssert.assertEquals(expected, conf.getLongList("long.list1"));

        // list of strings, comma separated
        ListAssert.assertEquals(expected, conf.getLongList("long.list2"));

        // list of Long objects
        ListAssert.assertEquals(expected, conf.getLongList("long.list3"));

        // array of Long objects
        ListAssert.assertEquals(expected, conf.getLongList("long.list4"));

        // array of long primitives
        ListAssert.assertEquals(expected, conf.getLongList("long.list5"));

        // list of Long objects
        ListAssert.assertEquals(expected, conf.getLongList("long.list6"));

        // list of interpolated values
        ListAssert.assertEquals(expected, conf.getLongList("long.list.interpolated"));

        // single long values
        expected = new ArrayList();
        expected.add(new Long("1"));
        ListAssert.assertEquals(expected, conf.getLongList("long.string"));
        ListAssert.assertEquals(expected, conf.getLongList("long.object"));

        // empty list
        ListAssert.assertEquals(new ArrayList(), conf.getLongList("empty"));
    }

    public void testGetFloatArray()
    {
        // missing list
        float[] defaultValue = new float[] { 2, 1};
        ArrayAssert.assertEquals(defaultValue, conf.getFloatArray("float.list", defaultValue), 0);

        float[] expected = new float[] { 1, 2 };

        // list of strings
        ArrayAssert.assertEquals(expected, conf.getFloatArray("float.list1"), 0);

        // list of strings, comma separated
        ArrayAssert.assertEquals(expected, conf.getFloatArray("float.list2"), 0);

        // list of Float objects
        ArrayAssert.assertEquals(expected, conf.getFloatArray("float.list3"), 0);

        // array of Float objects
        ArrayAssert.assertEquals(expected, conf.getFloatArray("float.list4"), 0);

        // array of float primitives
        ArrayAssert.assertEquals(expected, conf.getFloatArray("float.list5"), 0);

        // list of Float objects
        ArrayAssert.assertEquals(expected, conf.getFloatArray("float.list6"), 0);

        // list of interpolated values
        ArrayAssert.assertEquals(expected, conf.getFloatArray("float.list.interpolated"), 0);

        // single float values
        ArrayAssert.assertEquals(new float[] { 1 }, conf.getFloatArray("float.string"), 0);
        ArrayAssert.assertEquals(new float[] { 1 }, conf.getFloatArray("float.object"), 0);

        // empty array
        ArrayAssert.assertEquals(new float[] { }, conf.getFloatArray("empty"), 0);
    }

    public void testGetFloatList()
    {
        // missing list
        ListAssert.assertEquals(null, conf.getFloatList("float.list", null));

        List expected = new ArrayList();
        expected.add(new Float("1"));
        expected.add(new Float("2"));

        // list of strings
        ListAssert.assertEquals(expected, conf.getFloatList("float.list1"));

        // list of strings, comma separated
        ListAssert.assertEquals(expected, conf.getFloatList("float.list2"));

        // list of Float objects
        ListAssert.assertEquals(expected, conf.getFloatList("float.list3"));

        // array of Float objects
        ListAssert.assertEquals(expected, conf.getFloatList("float.list4"));

        // array of float primitives
        ListAssert.assertEquals(expected, conf.getFloatList("float.list5"));

        // list of Float objects
        ListAssert.assertEquals(expected, conf.getFloatList("float.list6"));

        // list of interpolated values
        ListAssert.assertEquals(expected, conf.getFloatList("float.list.interpolated"));

        // single float values
        expected = new ArrayList();
        expected.add(new Float("1"));
        ListAssert.assertEquals(expected, conf.getFloatList("float.string"));
        ListAssert.assertEquals(expected, conf.getFloatList("float.object"));

        // empty list
        ListAssert.assertEquals(new ArrayList(), conf.getFloatList("empty"));
    }

    public void testGetDoubleArray()
    {
        // missing list
        double[] defaultValue = new double[] { 2, 1 };
        ArrayAssert.assertEquals(defaultValue, conf.getDoubleArray("double.list", defaultValue), 0);

        double[] expected = new double[] { 1, 2 };

        // list of strings
        ArrayAssert.assertEquals(expected, conf.getDoubleArray("double.list1"), 0);

        // list of strings, comma separated
        ArrayAssert.assertEquals(expected, conf.getDoubleArray("double.list2"), 0);

        // list of Double objects
        ArrayAssert.assertEquals(expected, conf.getDoubleArray("double.list3"), 0);

        // array of Double objects
        ArrayAssert.assertEquals(expected, conf.getDoubleArray("double.list4"), 0);

        // array of double primitives
        ArrayAssert.assertEquals(expected, conf.getDoubleArray("double.list5"), 0);

        // list of Double objects
        ArrayAssert.assertEquals(expected, conf.getDoubleArray("double.list6"), 0);

        // list of interpolated values
        ArrayAssert.assertEquals(expected, conf.getDoubleArray("double.list.interpolated"), 0);

        // single double values
        ArrayAssert.assertEquals(new double[] { 1 }, conf.getDoubleArray("double.string"), 0);
        ArrayAssert.assertEquals(new double[] { 1 }, conf.getDoubleArray("double.object"), 0);

        // empty array
        ArrayAssert.assertEquals(new double[] { }, conf.getDoubleArray("empty"), 0);
    }

    public void testGetDoubleList()
    {
        // missing list
        ListAssert.assertEquals(null, conf.getDoubleList("double.list", null));

        List expected = new ArrayList();
        expected.add(new Double("1"));
        expected.add(new Double("2"));

        // list of strings
        ListAssert.assertEquals(expected, conf.getDoubleList("double.list1"));

        // list of strings, comma separated
        ListAssert.assertEquals(expected, conf.getDoubleList("double.list2"));

        // list of Double objects
        ListAssert.assertEquals(expected, conf.getDoubleList("double.list3"));

        // array of Double objects
        ListAssert.assertEquals(expected, conf.getDoubleList("double.list4"));

        // array of double primitives
        ListAssert.assertEquals(expected, conf.getDoubleList("double.list5"));

        // list of Double objects
        ListAssert.assertEquals(expected, conf.getDoubleList("double.list6"));

        // list of interpolated values
        ListAssert.assertEquals(expected, conf.getDoubleList("double.list.interpolated"));

        // single double values
        expected = new ArrayList();
        expected.add(new Double("1"));
        ListAssert.assertEquals(expected, conf.getDoubleList("double.string"));
        ListAssert.assertEquals(expected, conf.getDoubleList("double.object"));

        // empty list
        ListAssert.assertEquals(new ArrayList(), conf.getDoubleList("empty"));
    }

    public void testGetBigIntegerArray()
    {
        // missing list
        BigInteger[] defaultValue = new BigInteger[] { new BigInteger("2"), new BigInteger("1") };
        ArrayAssert.assertEquals(defaultValue, conf.getBigIntegerArray("biginteger.list", defaultValue));

        BigInteger[] expected = new BigInteger[] { new BigInteger("1"), new BigInteger("2") };

        // list of strings
        ArrayAssert.assertEquals(expected, conf.getBigIntegerArray("biginteger.list1"));

        // list of strings, comma separated
        ArrayAssert.assertEquals(expected, conf.getBigIntegerArray("biginteger.list2"));

        // list of BigInteger objects
        ArrayAssert.assertEquals(expected, conf.getBigIntegerArray("biginteger.list3"));

        // array of BigInteger objects
        ArrayAssert.assertEquals(expected, conf.getBigIntegerArray("biginteger.list4"));

        // list of BigInteger objects
        ArrayAssert.assertEquals(expected, conf.getBigIntegerArray("biginteger.list6"));

        // list of interpolated values
        ArrayAssert.assertEquals(expected, conf.getBigIntegerArray("biginteger.list.interpolated"));

        // single BigInteger values
        ArrayAssert.assertEquals(new BigInteger[] { new BigInteger("1") }, conf.getBigIntegerArray("biginteger.string"));
        ArrayAssert.assertEquals(new BigInteger[] { new BigInteger("1") }, conf.getBigIntegerArray("biginteger.object"));

        // empty array
        ArrayAssert.assertEquals(new BigInteger[] { }, conf.getBigIntegerArray("empty"));
    }

    public void testGetBigIntegerList()
    {
        // missing list
        ListAssert.assertEquals(null, conf.getBigIntegerList("biginteger.list", null));

        List expected = new ArrayList();
        expected.add(new BigInteger("1"));
        expected.add(new BigInteger("2"));

        // list of strings
        ListAssert.assertEquals(expected, conf.getBigIntegerList("biginteger.list1"));

        // list of strings, comma separated
        ListAssert.assertEquals(expected, conf.getBigIntegerList("biginteger.list2"));

        // list of BigInteger objects
        ListAssert.assertEquals(expected, conf.getBigIntegerList("biginteger.list3"));

        // array of BigInteger objects
        ListAssert.assertEquals(expected, conf.getBigIntegerList("biginteger.list4"));

        // list of BigInteger objects
        ListAssert.assertEquals(expected, conf.getBigIntegerList("biginteger.list6"));

        // list of interpolated values
        ListAssert.assertEquals(expected, conf.getBigIntegerList("biginteger.list.interpolated"));

        // single BigInteger values
        expected = new ArrayList();
        expected.add(new BigInteger("1"));
        ListAssert.assertEquals(expected, conf.getBigIntegerList("biginteger.string"));
        ListAssert.assertEquals(expected, conf.getBigIntegerList("biginteger.object"));

        // empty list
        ListAssert.assertEquals(new ArrayList(), conf.getBigIntegerList("empty"));
    }

    public void testGetBigDecimalArray()
    {
        // missing list
        BigDecimal[] defaultValue = new BigDecimal[] { new BigDecimal("2"), new BigDecimal("1") };
        ArrayAssert.assertEquals(defaultValue, conf.getBigDecimalArray("bigdecimal.list", defaultValue));

        BigDecimal[] expected = new BigDecimal[] { new BigDecimal("1"), new BigDecimal("2") };

        // list of strings
        ArrayAssert.assertEquals(expected, conf.getBigDecimalArray("bigdecimal.list1"));

        // list of strings, comma separated
        ArrayAssert.assertEquals(expected, conf.getBigDecimalArray("bigdecimal.list2"));

        // list of BigDecimal objects
        ArrayAssert.assertEquals(expected, conf.getBigDecimalArray("bigdecimal.list3"));

        // array of BigDecimal objects
        ArrayAssert.assertEquals(expected, conf.getBigDecimalArray("bigdecimal.list4"));

        // list of BigDecimal objects
        ArrayAssert.assertEquals(expected, conf.getBigDecimalArray("bigdecimal.list6"));

        // list of interpolated values
        ArrayAssert.assertEquals(expected, conf.getBigDecimalArray("bigdecimal.list.interpolated"));

        // single BigDecimal values
        ArrayAssert.assertEquals(new BigDecimal[] { new BigDecimal("1") }, conf.getBigDecimalArray("bigdecimal.string"));
        ArrayAssert.assertEquals(new BigDecimal[] { new BigDecimal("1") }, conf.getBigDecimalArray("bigdecimal.object"));

        // empty array
        ArrayAssert.assertEquals(new BigDecimal[] { }, conf.getBigDecimalArray("empty"));
    }

    public void testGetBigDecimalList()
    {
        // missing list
        ListAssert.assertEquals(null, conf.getBigDecimalList("bigdecimal.list", null));

        List expected = new ArrayList();
        expected.add(new BigDecimal("1"));
        expected.add(new BigDecimal("2"));

        // list of strings
        ListAssert.assertEquals(expected, conf.getBigDecimalList("bigdecimal.list1"));

        // list of strings, comma separated
        ListAssert.assertEquals(expected, conf.getBigDecimalList("bigdecimal.list2"));

        // list of BigDecimal objects
        ListAssert.assertEquals(expected, conf.getBigDecimalList("bigdecimal.list3"));

        // array of BigDecimal objects
        ListAssert.assertEquals(expected, conf.getBigDecimalList("bigdecimal.list4"));

        // list of BigDecimal objects
        ListAssert.assertEquals(expected, conf.getBigDecimalList("bigdecimal.list6"));

        // list of interpolated values
        ListAssert.assertEquals(expected, conf.getBigDecimalList("bigdecimal.list.interpolated"));

        // single BigDecimal values
        expected = new ArrayList();
        expected.add(new BigDecimal("1"));
        ListAssert.assertEquals(expected, conf.getBigDecimalList("bigdecimal.string"));
        ListAssert.assertEquals(expected, conf.getBigDecimalList("bigdecimal.object"));

        // empty list
        ListAssert.assertEquals(new ArrayList(), conf.getBigDecimalList("empty"));
    }

    public void testGetURL() throws Exception
    {
        // missing URL
        URL defaultValue = new URL("http://www.google.com");
        assertEquals(defaultValue, conf.getURL("url", defaultValue));

        URL expected = new URL("http://jakarta.apache.org");

        // URL string
        assertEquals(expected, conf.getURL("url.string"));

        // URL object
        assertEquals(expected, conf.getURL("url.object"));

        // interpolated value
        assertEquals(expected, conf.getURL("url.string.interpolated"));
    }

    public void testGetURLArray() throws Exception
    {
        // missing list
        URL[] defaultValue = new URL[] { new URL("http://www.apache.org"), new URL("http://jakarta.apache.org") };
        ArrayAssert.assertEquals(defaultValue, conf.getURLArray("url.list", defaultValue));

        URL[] expected = new URL[] { new URL("http://jakarta.apache.org"), new URL("http://www.apache.org") };

        // list of strings
        ArrayAssert.assertEquals(expected, conf.getURLArray("url.list1"));

        // list of strings, comma separated
        ArrayAssert.assertEquals(expected, conf.getURLArray("url.list2"));

        // list of URL objects
        ArrayAssert.assertEquals(expected, conf.getURLArray("url.list3"));

        // array of URL objects
        ArrayAssert.assertEquals(expected, conf.getURLArray("url.list4"));

        // list of URL objects
        ArrayAssert.assertEquals(expected, conf.getURLArray("url.list6"));

        // list of interpolated values
        ArrayAssert.assertEquals(expected, conf.getURLArray("url.list.interpolated"));

        // single URL values
        ArrayAssert.assertEquals(new URL[] { new URL("http://jakarta.apache.org") }, conf.getURLArray("url.string"));
        ArrayAssert.assertEquals(new URL[] { new URL("http://jakarta.apache.org") }, conf.getURLArray("url.object"));

        // empty array
        ArrayAssert.assertEquals(new URL[] { }, conf.getURLArray("empty"));
    }

    public void testGetURLList() throws Exception
    {
        // missing list
        ListAssert.assertEquals(null, conf.getURLList("url.list", null));

        List expected = new ArrayList();
        expected.add(new URL("http://jakarta.apache.org"));
        expected.add(new URL("http://www.apache.org"));

        // list of strings
        ListAssert.assertEquals(expected, conf.getURLList("url.list1"));

        // list of strings, comma separated
        ListAssert.assertEquals(expected, conf.getURLList("url.list2"));

        // list of URL objects
        ListAssert.assertEquals(expected, conf.getURLList("url.list3"));

        // array of URL objects
        ListAssert.assertEquals(expected, conf.getURLList("url.list4"));

        // list of URL objects
        ListAssert.assertEquals(expected, conf.getURLList("url.list6"));

        // list of interpolated values
        ListAssert.assertEquals(expected, conf.getURLList("url.list.interpolated"));

        // single URL values
        expected = new ArrayList();
        expected.add(new URL("http://jakarta.apache.org"));
        ListAssert.assertEquals(expected, conf.getURLList("url.string"));
        ListAssert.assertEquals(expected, conf.getURLList("url.object"));

        // empty list
        ListAssert.assertEquals(new ArrayList(), conf.getURLList("empty"));
    }

    public void testGetLocale()
    {
        // language
        conf.setProperty("locale", "fr");
        assertEquals("language", new Locale("fr", ""), conf.getLocale("locale"));

        // language + variant
        conf.setProperty("locale", "fr__POSIX");
        assertEquals("language + variant", new Locale("fr", "", "POSIX"), conf.getLocale("locale"));

        // country
        conf.setProperty("locale", "_FR");
        assertEquals("country", new Locale("", "FR"), conf.getLocale("locale"));

        // country + variant
        conf.setProperty("locale", "_FR_WIN");
        assertEquals("country + variant", new Locale("", "FR", "WIN"), conf.getLocale("locale"));

        // language + country
        conf.setProperty("locale", "fr_FR");
        assertEquals("language + country", new Locale("fr", "FR"), conf.getLocale("locale"));

        // language + country + variant
        conf.setProperty("locale", "fr_FR_MAC");
        assertEquals("language + country + variant", new Locale("fr", "FR", "MAC"), conf.getLocale("locale"));

        // default value
        conf.setProperty("locale", "fr");
        assertEquals("Existing key with default value", Locale.FRENCH, conf.getLocale("locale", Locale.GERMAN));
        assertEquals("Missing key with default value", Locale.GERMAN, conf.getLocale("localeNotInConfig", Locale.GERMAN));

        // interpolated value
        assertEquals(Locale.FRENCH, conf.getLocale("locale.string.interpolated"));
    }

    public void testGetLocaleArray() throws Exception
    {
        // missing list
        Locale[] defaultValue = new Locale[] { Locale.GERMAN, Locale.FRENCH };
        ArrayAssert.assertEquals(defaultValue, conf.getLocaleArray("locale.list", defaultValue));

        Locale[] expected = new Locale[] { Locale.FRENCH, Locale.GERMAN };

        // list of strings
        ArrayAssert.assertEquals(expected, conf.getLocaleArray("locale.list1"));

        // list of strings, comma separated
        ArrayAssert.assertEquals(expected, conf.getLocaleArray("locale.list2"));

        // list of Locale objects
        ArrayAssert.assertEquals(expected, conf.getLocaleArray("locale.list3"));

        // array of Locale objects
        ArrayAssert.assertEquals(expected, conf.getLocaleArray("locale.list4"));

        // list of Locale objects
        ArrayAssert.assertEquals(expected, conf.getLocaleArray("locale.list6"));

        // list of interpolated values
        ArrayAssert.assertEquals(expected, conf.getLocaleArray("locale.list.interpolated"));

        // single Locale values
        ArrayAssert.assertEquals(new Locale[] { Locale.FRENCH }, conf.getLocaleArray("locale.string"));
        ArrayAssert.assertEquals(new Locale[] { Locale.FRENCH }, conf.getLocaleArray("locale.object"));

        // empty array
        ArrayAssert.assertEquals(new Locale[] { }, conf.getLocaleArray("empty"));
    }

    public void testGetLocaleList() throws Exception
    {
        // missing list
        ListAssert.assertEquals(null, conf.getLocaleList("locale.list", null));

        List expected = new ArrayList();
        expected.add(Locale.FRENCH);
        expected.add(Locale.GERMAN);

        // list of strings
        ListAssert.assertEquals(expected, conf.getLocaleList("locale.list1"));

        // list of strings, comma separated
        ListAssert.assertEquals(expected, conf.getLocaleList("locale.list2"));

        // list of Locale objects
        ListAssert.assertEquals(expected, conf.getLocaleList("locale.list3"));

        // array of Locale objects
        ListAssert.assertEquals(expected, conf.getLocaleList("locale.list4"));

        // list of Locale objects
        ListAssert.assertEquals(expected, conf.getLocaleList("locale.list6"));

        // list of interpolated values
        ListAssert.assertEquals(expected, conf.getLocaleList("locale.list.interpolated"));

        // single Locale values
        expected = new ArrayList();
        expected.add(Locale.FRENCH);
        ListAssert.assertEquals(expected, conf.getLocaleList("locale.string"));
        ListAssert.assertEquals(expected, conf.getLocaleList("locale.object"));

        // empty list
        ListAssert.assertEquals(new ArrayList(), conf.getLocaleList("empty"));
    }

    public void testGetColor()
    {
        // RRGGBB
        conf.setProperty("color", "FF0000");
        assertEquals("color", Color.red, conf.getColor("color"));

        // #RRGGBB
        conf.setProperty("color", "#00FF00");
        assertEquals("color", Color.green, conf.getColor("color"));

        // #RRGGBBAA
        conf.setProperty("color", "#01030507");
        Color color = conf.getColor("color");
        assertNotNull("null color", color);
        assertEquals("red",   1, color.getRed());
        assertEquals("green", 3, color.getGreen());
        assertEquals("blue",  5, color.getBlue());
        assertEquals("alpha", 7, color.getAlpha());

        // interpolated value
        assertEquals(Color.red, conf.getColor("color.string.interpolated"));
    }

    public void testGetColorArray() throws Exception
    {
        // missing list
        Color[] defaultValue = new Color[] { Color.red, Color.blue };
        ArrayAssert.assertEquals(defaultValue, conf.getColorArray("color.list", defaultValue));

        Color[] expected = new Color[] { Color.red, Color.blue };

        // list of strings
        ArrayAssert.assertEquals(expected, conf.getColorArray("color.list1"));

        // list of strings, comma separated
        ArrayAssert.assertEquals(expected, conf.getColorArray("color.list2"));

        // list of Color objects
        ArrayAssert.assertEquals(expected, conf.getColorArray("color.list3"));

        // array of Color objects
        ArrayAssert.assertEquals(expected, conf.getColorArray("color.list4"));

        // list of Color objects
        ArrayAssert.assertEquals(expected, conf.getColorArray("color.list6"));

        // list of interpolated values
        ArrayAssert.assertEquals(expected, conf.getColorArray("color.list.interpolated"));

        // single Color values
        ArrayAssert.assertEquals(new Color[] { Color.red }, conf.getColorArray("color.string"));
        ArrayAssert.assertEquals(new Color[] { Color.red }, conf.getColorArray("color.object"));

        // empty array
        ArrayAssert.assertEquals(new Color[] { }, conf.getColorArray("empty"));
    }

    public void testGetColorList() throws Exception
    {
        // missing list
        ListAssert.assertEquals(null, conf.getColorList("color.list", null));

        List expected = new ArrayList();
        expected.add(Color.red);
        expected.add(Color.blue);

        // list of strings
        ListAssert.assertEquals(expected, conf.getColorList("color.list1"));

        // list of strings, comma separated
        ListAssert.assertEquals(expected, conf.getColorList("color.list2"));

        // list of Color objects
        ListAssert.assertEquals(expected, conf.getColorList("color.list3"));

        // array of Color objects
        ListAssert.assertEquals(expected, conf.getColorList("color.list4"));

        // list of Color objects
        ListAssert.assertEquals(expected, conf.getColorList("color.list6"));

        // list of interpolated values
        ListAssert.assertEquals(expected, conf.getColorList("color.list.interpolated"));

        // single Color values
        expected = new ArrayList();
        expected.add(Color.red);
        ListAssert.assertEquals(expected, conf.getColorList("color.string"));
        ListAssert.assertEquals(expected, conf.getColorList("color.object"));

        // empty list
        ListAssert.assertEquals(new ArrayList(), conf.getColorList("empty"));
    }

    public void testGetDate() throws Exception
    {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        // missing Date
        Date defaultValue = new Date();
        assertEquals(defaultValue, conf.getDate("date", defaultValue));

        Date expected = format.parse("2004-01-01");

        // Date string
        assertEquals(expected, conf.getDate("date.string"));

        // Date object
        assertEquals(expected, conf.getDate("date.object"));

        // Calendar object
        assertEquals(expected, conf.getDate("calendar.object"));

        // interpolated value
        assertEquals(expected, conf.getDate("date.string.interpolated"));
    }

    public void testGetDateArray() throws Exception
    {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = format.parse("2004-01-01");
        Date date2 = format.parse("2004-12-31");

        // missing list
        Date[] defaultValue = new Date[] { date2, date1 };
        ArrayAssert.assertEquals(defaultValue, conf.getDateArray("date.list", defaultValue));

        Date[] expected = new Date[] { date1, date2 };

        // list of strings
        ArrayAssert.assertEquals(expected, conf.getDateArray("date.list1"));

        // list of strings, comma separated
        ArrayAssert.assertEquals(expected, conf.getDateArray("date.list2"));

        // list of Date objects
        ArrayAssert.assertEquals(expected, conf.getDateArray("date.list3"));

        // array of Date objects
        ArrayAssert.assertEquals(expected, conf.getDateArray("date.list4"));

        // list of Calendar objects
        ArrayAssert.assertEquals(expected, conf.getDateArray("date.list5"));

        // list of Date objects
        ArrayAssert.assertEquals(expected, conf.getDateArray("date.list6"));

        // list of interpolated values
        ArrayAssert.assertEquals(expected, conf.getDateArray("date.list.interpolated"));

        // single Date values
        ArrayAssert.assertEquals(new Date[] { date1 }, conf.getDateArray("date.string"));
        ArrayAssert.assertEquals(new Date[] { date1 }, conf.getDateArray("date.object"));

        // empty array
        ArrayAssert.assertEquals(new Date[] { }, conf.getDateArray("empty"));
    }

    public void testGetDateArrayWithFormat() throws Exception
    {
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        Date date1 = format.parse("01/01/2004");
        Date date2 = format.parse("12/31/2004");
        Date[] expected = new Date[]
        { date1, date2 };

        conf.addProperty("date.format", "01/01/2004");
        conf.addProperty("date.format", "12/31/2004");
        ArrayAssert.assertEquals("Wrong dates with format", expected, conf
                .getDateArray("date.format", "MM/dd/yyyy"));
    }

    public void testGetDateList() throws Exception
    {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = format.parse("2004-01-01");
        Date date2 = format.parse("2004-12-31");

        // missing list
        ListAssert.assertEquals(null, conf.getDateList("date.list", (List) null));

        List expected = new ArrayList();
        expected.add(date1);
        expected.add(date2);

        // list of strings
        ListAssert.assertEquals(expected, conf.getDateList("date.list1"));

        // list of strings, comma separated
        ListAssert.assertEquals(expected, conf.getDateList("date.list2"));

        // list of Date objects
        ListAssert.assertEquals(expected, conf.getDateList("date.list3"));

        // array of Date objects
        ListAssert.assertEquals(expected, conf.getDateList("date.list4"));

        // list of Calendar objects
        ListAssert.assertEquals(expected, conf.getDateList("date.list5"));

        // list of Date objects
        ListAssert.assertEquals(expected, conf.getDateList("date.list6"));

        // list of interpolated values
        ListAssert.assertEquals(expected, conf.getDateList("date.list.interpolated"));

        // single Date values
        expected = new ArrayList();
        expected.add(date1);
        ListAssert.assertEquals(expected, conf.getDateList("date.string"));
        ListAssert.assertEquals(expected, conf.getDateList("date.object"));

        // empty list
        ListAssert.assertEquals(new ArrayList(), conf.getDateList("empty"));
    }

    public void testGetCalendar() throws Exception
    {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        // missing Date
        Calendar defaultValue = Calendar.getInstance();
        defaultValue.setTime(new Date());
        assertEquals(defaultValue, conf.getCalendar("calendar", defaultValue));

        Calendar expected = Calendar.getInstance();
        expected.setTime(format.parse("2004-01-01"));

        // Calendar string
        assertEquals(expected, conf.getCalendar("calendar.string"));

        // Calendar object
        assertEquals(expected, conf.getCalendar("calendar.object"));

        // Date object
        assertEquals(expected, conf.getCalendar("date.object"));

        // interpolated value
        assertEquals(expected, conf.getCalendar("calendar.string.interpolated"));
    }


    public void testGetCalendarArray() throws Exception
    {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = format.parse("2004-01-01");
        Date date2 = format.parse("2004-12-31");
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        // missing list
        Calendar[] defaultValue = new Calendar[] { calendar2, calendar1 };
        ArrayAssert.assertEquals(defaultValue, conf.getCalendarArray("calendar.list", defaultValue));

        Calendar[] expected = new Calendar[] { calendar1, calendar2 };

        // list of strings
        ArrayAssert.assertEquals(expected, conf.getCalendarArray("calendar.list1"));

        // list of strings, comma separated
        ArrayAssert.assertEquals(expected, conf.getCalendarArray("calendar.list2"));

        // list of Calendar objects
        ArrayAssert.assertEquals(expected, conf.getCalendarArray("calendar.list3"));

        // array of Calendar objects
        ArrayAssert.assertEquals(expected, conf.getCalendarArray("calendar.list4"));

        // list of Date objects
        ArrayAssert.assertEquals(expected, conf.getCalendarArray("calendar.list5"));

        // list of Calendar objects
        ArrayAssert.assertEquals(expected, conf.getCalendarArray("calendar.list6"));

        // list of interpolated values
        ArrayAssert.assertEquals(expected, conf.getCalendarArray("calendar.list.interpolated"));

        // single Calendar values
        ArrayAssert.assertEquals(new Calendar[] { calendar1 }, conf.getCalendarArray("calendar.string"));
        ArrayAssert.assertEquals(new Calendar[] { calendar1 }, conf.getCalendarArray("calendar.object"));

        // empty array
        ArrayAssert.assertEquals(new Calendar[] { }, conf.getCalendarArray("empty"));
    }

    public void testGetCalendarList() throws Exception
    {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = format.parse("2004-01-01");
        Date date2 = format.parse("2004-12-31");
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        // missing list
        ListAssert.assertEquals(null, conf.getCalendarList("calendar.list", (List) null));

        List expected = new ArrayList();
        expected.add(calendar1);
        expected.add(calendar2);

        // list of strings
        ListAssert.assertEquals(expected, conf.getCalendarList("calendar.list1"));

        // list of strings, comma separated
        ListAssert.assertEquals(expected, conf.getCalendarList("calendar.list2"));

        // list of Calendar objects
        ListAssert.assertEquals(expected, conf.getCalendarList("calendar.list3"));

        // array of Calendar objects
        ListAssert.assertEquals(expected, conf.getCalendarList("calendar.list4"));

        // list of Date objects
        ListAssert.assertEquals(expected, conf.getCalendarList("calendar.list5"));

        // list of Calendar objects
        ListAssert.assertEquals(expected, conf.getCalendarList("calendar.list6"));

        // list of interpolated values
        ListAssert.assertEquals(expected, conf.getCalendarList("calendar.list.interpolated"));

        // single Calendar values
        expected = new ArrayList();
        expected.add(calendar1);
        ListAssert.assertEquals(expected, conf.getCalendarList("date.string"));
        ListAssert.assertEquals(expected, conf.getCalendarList("date.object"));

        // empty list
        ListAssert.assertEquals(new ArrayList(), conf.getCalendarList("empty"));
    }

    public void testConversionException()
    {
        conf.addProperty("key1", new Object());
        conf.addProperty("key2", "xxxxxx");

        try
        {
            conf.getBooleanArray("key1");
            fail("getBooleanArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getBooleanArray("key2");
            fail("getBooleanArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getBooleanList("key1");
            fail("getBooleanList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getBooleanList("key2");
            fail("getBooleanList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getByteArray("key1");
            fail("getByteArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getByteArray("key2");
            fail("getByteArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getByteList("key1");
            fail("getByteList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getByteList("key2");
            fail("getByteList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getShortArray("key1");
            fail("getShortArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getShortArray("key2");
            fail("getShortArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getShortList("key1");
            fail("getShortList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getShortList("key2");
            fail("getShortList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getIntArray("key1");
            fail("getIntArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getIntArray("key2");
            fail("getIntArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getIntegerList("key1");
            fail("getIntegerList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getIntegerList("key2");
            fail("getIntegerList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getLongArray("key1");
            fail("getLongArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getLongArray("key2");
            fail("getLongArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getLongList("key1");
            fail("getLongList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getLongList("key2");
            fail("getLongList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getFloatArray("key1");
            fail("getFloatArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getFloatArray("key2");
            fail("getFloatArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getFloatList("key1");
            fail("getFloatList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getFloatList("key2");
            fail("getFloatList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getDoubleArray("key1");
            fail("getDoubleArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getDoubleArray("key2");
            fail("getDoubleArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getDoubleList("key1");
            fail("getDoubleList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getDoubleList("key2");
            fail("getDoubleList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getBigIntegerArray("key1");
            fail("getBigIntegerArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getBigIntegerArray("key2");
            fail("getBigIntegerArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getBigIntegerList("key1");
            fail("getBigIntegerList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getBigIntegerList("key2");
            fail("getBigIntegerList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getBigDecimalArray("key1");
            fail("getBigDecimalArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getBigDecimalArray("key2");
            fail("getBigDecimalArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getBigDecimalList("key1");
            fail("getBigDecimalList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getBigDecimalList("key2");
            fail("getBigDecimalList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getURLArray("key1");
            fail("getURLArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getURLArray("key2");
            fail("getURLArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getURLList("key1");
            fail("getURLList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getURLList("key2");
            fail("getURLList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getLocaleArray("key1");
            fail("getLocaleArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getLocaleArray("key2");
            fail("getLocaleArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getLocaleList("key1");
            fail("getLocaleList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getLocaleList("key2");
            fail("getLocaleList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getColorArray("key1");
            fail("getColorArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getColorArray("key2");
            fail("getColorArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getColorList("key1");
            fail("getColorList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getColorList("key2");
            fail("getColorList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getDateArray("key1");
            fail("getDateArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getDateArray("key2");
            fail("getDateArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getDateList("key1");
            fail("getDateList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getDateList("key2");
            fail("getDateList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getCalendarArray("key1");
            fail("getCalendarArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getCalendarArray("key2");
            fail("getCalendarArray didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getCalendarList("key1");
            fail("getCalendarList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }

        try
        {
            conf.getCalendarList("key2");
            fail("getCalendarList didn't throw a ConversionException");
        }
        catch (ConversionException e)
        {
            // expected
        }
    }
}
