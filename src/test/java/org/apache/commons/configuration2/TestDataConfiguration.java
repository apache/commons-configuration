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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.apache.commons.configuration2.convert.DefaultConversionHandler;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConversionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.mail.internet.InternetAddress;

/**
 */
public class TestDataConfiguration {
    /** Constant for the date pattern used by tests. */
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * Returns the expected test date.
     *
     * @return the expected test date
     * @throws ParseException if the date cannot be parsed
     */
    private static Date expectedDate() throws ParseException {
        final DateFormat format = new SimpleDateFormat(DATE_PATTERN);
        return format.parse("2004-01-01");
    }

    /** The test instance. */
    private DataConfiguration conf;

    @BeforeEach
    public void setUp() throws Exception {
        final BaseConfiguration baseConfig = new BaseConfiguration();
        baseConfig.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        conf = new DataConfiguration(baseConfig);

        // empty value
        conf.addProperty("empty", "");

        // lists of boolean
        conf.addProperty("boolean.list1", "true");
        conf.addProperty("boolean.list1", "false");
        conf.addProperty("boolean.list2", "true, false");
        conf.addProperty("boolean.list3", Boolean.TRUE);
        conf.addProperty("boolean.list3", Boolean.FALSE);
        conf.addPropertyDirect("boolean.list4", new Boolean[] {Boolean.TRUE, Boolean.FALSE});
        conf.addPropertyDirect("boolean.list5", new boolean[] {true, false});
        final List<Object> booleans = new ArrayList<>();
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
        conf.addProperty("byte.list3", Byte.valueOf("1"));
        conf.addProperty("byte.list3", Byte.valueOf("2"));
        conf.addPropertyDirect("byte.list4", new Byte[] {Byte.valueOf("1"), Byte.valueOf("2")});
        conf.addPropertyDirect("byte.list5", new byte[] {1, 2});
        final List<Object> bytes = new ArrayList<>();
        bytes.add(Byte.valueOf("1"));
        bytes.add(Byte.valueOf("2"));
        conf.addProperty("byte.list6", bytes);
        conf.addProperty("byte.string", "1");
        conf.addProperty("byte.object", Byte.valueOf("1"));
        conf.addProperty("byte.list.interpolated", "${byte.string},2");

        // lists of shorts
        conf.addProperty("short.list1", "1");
        conf.addProperty("short.list1", "2");
        conf.addProperty("short.list2", "1, 2");
        conf.addProperty("short.list3", Short.valueOf("1"));
        conf.addProperty("short.list3", Short.valueOf("2"));
        conf.addPropertyDirect("short.list4", new Short[] {Short.valueOf("1"), Short.valueOf("2")});
        conf.addPropertyDirect("short.list5", new short[] {1, 2});
        final List<Object> shorts = new ArrayList<>();
        shorts.add(Short.valueOf("1"));
        shorts.add(Short.valueOf("2"));
        conf.addProperty("short.list6", shorts);
        conf.addProperty("short.string", "1");
        conf.addProperty("short.object", Short.valueOf("1"));
        conf.addProperty("short.list.interpolated", "${short.string},2");

        // lists of integers
        conf.addProperty("integer.list1", "1");
        conf.addProperty("integer.list1", "2");
        conf.addProperty("integer.list2", "1, 2");
        conf.addProperty("integer.list3", Integer.valueOf("1"));
        conf.addProperty("integer.list3", Integer.valueOf("2"));
        conf.addPropertyDirect("integer.list4", new Integer[] {Integer.valueOf("1"), Integer.valueOf("2")});
        conf.addPropertyDirect("integer.list5", new int[] {1, 2});
        final List<Object> integers = new ArrayList<>();
        integers.add(Integer.valueOf("1"));
        integers.add(Integer.valueOf("2"));
        conf.addProperty("integer.list6", integers);
        conf.addProperty("integer.string", "1");
        conf.addProperty("integer.object", Integer.valueOf("1"));
        conf.addProperty("integer.list.interpolated", "${integer.string},2");

        // lists of longs
        conf.addProperty("long.list1", "1");
        conf.addProperty("long.list1", "2");
        conf.addProperty("long.list2", "1, 2");
        conf.addProperty("long.list3", Long.valueOf("1"));
        conf.addProperty("long.list3", Long.valueOf("2"));
        conf.addPropertyDirect("long.list4", new Long[] {Long.valueOf("1"), Long.valueOf("2")});
        conf.addPropertyDirect("long.list5", new long[] {1, 2});
        final List<Object> longs = new ArrayList<>();
        longs.add(Long.valueOf("1"));
        longs.add(Long.valueOf("2"));
        conf.addProperty("long.list6", longs);
        conf.addProperty("long.string", "1");
        conf.addProperty("long.object", Long.valueOf("1"));
        conf.addProperty("long.list.interpolated", "${long.string},2");

        // lists of floats
        conf.addProperty("float.list1", "1");
        conf.addProperty("float.list1", "2");
        conf.addProperty("float.list2", "1, 2");
        conf.addProperty("float.list3", Float.valueOf("1"));
        conf.addProperty("float.list3", Float.valueOf("2"));
        conf.addPropertyDirect("float.list4", new Float[] {Float.valueOf("1"), Float.valueOf("2")});
        conf.addPropertyDirect("float.list5", new float[] {1, 2});
        final List<Object> floats = new ArrayList<>();
        floats.add(Float.valueOf("1"));
        floats.add(Float.valueOf("2"));
        conf.addProperty("float.list6", floats);
        conf.addProperty("float.string", "1");
        conf.addProperty("float.object", Float.valueOf("1"));
        conf.addProperty("float.list.interpolated", "${float.string},2");

        // lists of doubles
        conf.addProperty("double.list1", "1");
        conf.addProperty("double.list1", "2");
        conf.addProperty("double.list2", "1, 2");
        conf.addProperty("double.list3", Double.valueOf("1"));
        conf.addProperty("double.list3", Double.valueOf("2"));
        conf.addPropertyDirect("double.list4", new Double[] {Double.valueOf("1"), Double.valueOf("2")});
        conf.addPropertyDirect("double.list5", new double[] {1, 2});
        final List<Object> doubles = new ArrayList<>();
        doubles.add(Double.valueOf("1"));
        doubles.add(Double.valueOf("2"));
        conf.addProperty("double.list6", doubles);
        conf.addProperty("double.string", "1");
        conf.addProperty("double.object", Double.valueOf("1"));
        conf.addProperty("double.list.interpolated", "${double.string},2");

        // lists of big integers
        conf.addProperty("biginteger.list1", "1");
        conf.addProperty("biginteger.list1", "2");
        conf.addProperty("biginteger.list2", "1, 2");
        conf.addProperty("biginteger.list3", new BigInteger("1"));
        conf.addProperty("biginteger.list3", new BigInteger("2"));
        conf.addPropertyDirect("biginteger.list4", new BigInteger[] {new BigInteger("1"), new BigInteger("2")});
        final List<Object> bigintegers = new ArrayList<>();
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
        conf.addPropertyDirect("bigdecimal.list4", new BigDecimal[] {new BigDecimal("1"), new BigDecimal("2")});
        final List<Object> bigdecimals = new ArrayList<>();
        bigdecimals.add(new BigDecimal("1"));
        bigdecimals.add(new BigDecimal("2"));
        conf.addProperty("bigdecimal.list6", bigdecimals);
        conf.addProperty("bigdecimal.string", "1");
        conf.addProperty("bigdecimal.object", new BigDecimal("1"));
        conf.addProperty("bigdecimal.list.interpolated", "${bigdecimal.string},2");

        // URIs
        final String uri1 = "http://jakarta.apache.org";
        final String uri2 = "http://www.apache.org";
        conf.addProperty("uri.string", uri1);
        conf.addProperty("uri.string.interpolated", "${uri.string}");
        conf.addProperty("uri.object", new URI(uri1));
        conf.addProperty("uri.list1", uri1);
        conf.addProperty("uri.list1", uri2);
        conf.addProperty("uri.list2", uri1 + ", " + uri2);
        conf.addProperty("uri.list3", new URI(uri1));
        conf.addProperty("uri.list3", new URI(uri2));
        conf.addPropertyDirect("uri.list4", new URI[] {new URI(uri1), new URI(uri2)});
        final List<Object> uris = new ArrayList<>();
        uris.add(new URI(uri1));
        uris.add(new URI(uri2));
        conf.addProperty("uri.list6", uris);
        conf.addProperty("uri.list.interpolated", "${uri.string}," + uri2);

        // URLs
        final String url1 = "http://jakarta.apache.org";
        final String url2 = "http://www.apache.org";
        conf.addProperty("url.string", url1);
        conf.addProperty("url.string.interpolated", "${url.string}");
        conf.addProperty("url.object", new URL(url1));
        conf.addProperty("url.list1", url1);
        conf.addProperty("url.list1", url2);
        conf.addProperty("url.list2", url1 + ", " + url2);
        conf.addProperty("url.list3", new URL(url1));
        conf.addProperty("url.list3", new URL(url2));
        conf.addPropertyDirect("url.list4", new URL[] {new URL(url1), new URL(url2)});
        final List<Object> urls = new ArrayList<>();
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
        conf.addPropertyDirect("locale.list4", new Locale[] {Locale.FRENCH, Locale.GERMAN});
        final List<Object> locales = new ArrayList<>();
        locales.add(Locale.FRENCH);
        locales.add(Locale.GERMAN);
        conf.addProperty("locale.list6", locales);
        conf.addProperty("locale.list.interpolated", "${locale.string},de");

        // Colors
        final String color1 = "FF0000";
        final String color2 = "0000FF";
        conf.addProperty("color.string", color1);
        conf.addProperty("color.string.interpolated", "${color.string}");
        conf.addProperty("color.object", Color.red);
        conf.addProperty("color.list1", color1);
        conf.addProperty("color.list1", color2);
        conf.addProperty("color.list2", color1 + ", " + color2);
        conf.addProperty("color.list3", Color.red);
        conf.addProperty("color.list3", Color.blue);
        conf.addPropertyDirect("color.list4", new Color[] {Color.red, Color.blue});
        final List<Object> colors = new ArrayList<>();
        colors.add(Color.red);
        colors.add(Color.blue);
        conf.addProperty("color.list6", colors);
        conf.addProperty("color.list.interpolated", "${color.string}," + color2);

        // Dates & Calendars
        final String pattern = DATE_PATTERN;
        final DateFormat format = new SimpleDateFormat(pattern);
        conf.setProperty(DataConfiguration.DATE_FORMAT_KEY, pattern);

        final Date date1 = format.parse("2004-01-01");
        final Date date2 = format.parse("2004-12-31");
        final Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        final Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        conf.addProperty("date.string", "2004-01-01");
        conf.addProperty("date.string.interpolated", "${date.string}");
        conf.addProperty("date.object", date1);
        conf.addProperty("date.list1", "2004-01-01");
        conf.addProperty("date.list1", "2004-12-31");
        conf.addProperty("date.list2", "2004-01-01, 2004-12-31");
        conf.addProperty("date.list3", date1);
        conf.addProperty("date.list3", date2);
        conf.addPropertyDirect("date.list4", new Date[] {date1, date2});
        conf.addPropertyDirect("date.list5", new Calendar[] {calendar1, calendar2});
        final List<Object> dates = new ArrayList<>();
        dates.add(date1);
        dates.add(date2);
        conf.addProperty("date.list6", dates);
        conf.addProperty("date.list.interpolated", "${date.string},2004-12-31");
        conf.addPropertyDirect("date.list7", new String[] {"2004-01-01", "2004-12-31"});

        conf.addProperty("calendar.string", "2004-01-01");
        conf.addProperty("calendar.string.interpolated", "${calendar.string}");
        conf.addProperty("calendar.object", calendar1);
        conf.addProperty("calendar.list1", "2004-01-01");
        conf.addProperty("calendar.list1", "2004-12-31");
        conf.addProperty("calendar.list2", "2004-01-01, 2004-12-31");
        conf.addProperty("calendar.list3", calendar1);
        conf.addProperty("calendar.list3", calendar2);
        conf.addPropertyDirect("calendar.list4", new Calendar[] {calendar1, calendar2});
        conf.addPropertyDirect("calendar.list5", new Date[] {date1, date2});
        final List<Object> calendars = new ArrayList<>();
        calendars.add(date1);
        calendars.add(date2);
        conf.addProperty("calendar.list6", calendars);
        conf.addProperty("calendar.list.interpolated", "${calendar.string},2004-12-31");
        conf.addPropertyDirect("calendar.list7", new String[] {"2004-01-01", "2004-12-31"});

        // host address
        conf.addProperty("ip.string", "127.0.0.1");
        conf.addProperty("ip.string.interpolated", "${ip.string}");
        conf.addProperty("ip.object", InetAddress.getByName("127.0.0.1"));

        // email address
        conf.addProperty("email.string", "dev@test.org");
        conf.addProperty("email.string.interpolated", "${email.string}");
        conf.addProperty("email.object", new InternetAddress("dev@test.org"));
    }

    /**
     * Tests whether properties can be cleared.
     */
    @Test
    public void testClearProperty() {
        final String key = "test.property";
        conf.addProperty(key, "someValue");
        conf.clearProperty(key);
        assertFalse(conf.containsKey(key));
    }

    /**
     * Tests the implementation of clearPropertyDirect().
     */
    @Test
    public void testClearPropertyDirect() {
        final String key = "test.property";
        conf.addProperty(key, "someValue");
        conf.clearPropertyDirect(key);
        assertFalse(conf.containsKey(key));
    }

    /**
     * Tests clearPropertyDirect() if the wrapped configuration does not extend AbstractConfiguration.
     */
    @Test
    public void testClearPropertyDirectNoAbstractConf() {
        final Configuration wrapped = mock(Configuration.class);
        final String key = "test.property";
        conf = new DataConfiguration(wrapped);
        conf.clearPropertyDirect(key);

        verify(wrapped).clearProperty(key);
        verifyNoMoreInteractions(wrapped);
    }

    @Test
    public void testContainsKey() {
        final Configuration baseconf = new BaseConfiguration();
        final DataConfiguration conf = new DataConfiguration(baseconf);

        assertFalse(conf.containsKey("foo"));

        baseconf.setProperty("foo", "bar");

        assertTrue(conf.containsKey("foo"));
    }

    @Test
    public void testConversionException() throws Exception {
        conf.addProperty("key1", new Object());
        conf.addProperty("key2", "xxxxxx");

        assertThrows(ConversionException.class, () -> conf.getBooleanArray("key1"));
        assertThrows(ConversionException.class, () -> conf.getBooleanArray("key2"));

        assertThrows(ConversionException.class, () -> conf.getBooleanList("key1"));
        assertThrows(ConversionException.class, () -> conf.getBooleanList("key2"));

        assertThrows(ConversionException.class, () -> conf.getByteArray("key1"));
        assertThrows(ConversionException.class, () -> conf.getByteArray("key2"));

        assertThrows(ConversionException.class, () -> conf.getByteList("key1"));
        assertThrows(ConversionException.class, () -> conf.getByteList("key2"));

        assertThrows(ConversionException.class, () -> conf.getShortArray("key1"));
        assertThrows(ConversionException.class, () -> conf.getShortArray("key2"));

        assertThrows(ConversionException.class, () -> conf.getShortList("key1"));
        assertThrows(ConversionException.class, () -> conf.getShortList("key2"));

        assertThrows(ConversionException.class, () -> conf.getIntArray("key1"));
        assertThrows(ConversionException.class, () -> conf.getIntArray("key2"));

        assertThrows(ConversionException.class, () -> conf.getIntegerList("key1"));
        assertThrows(ConversionException.class, () -> conf.getIntegerList("key2"));

        assertThrows(ConversionException.class, () -> conf.getLongArray("key1"));
        assertThrows(ConversionException.class, () -> conf.getLongArray("key2"));

        assertThrows(ConversionException.class, () -> conf.getLongList("key1"));
        assertThrows(ConversionException.class, () -> conf.getLongList("key2"));

        assertThrows(ConversionException.class, () -> conf.getFloatArray("key1"));
        assertThrows(ConversionException.class, () -> conf.getFloatArray("key2"));

        assertThrows(ConversionException.class, () -> conf.getFloatList("key1"));
        assertThrows(ConversionException.class, () -> conf.getFloatList("key2"));

        assertThrows(ConversionException.class, () -> conf.getDoubleArray("key1"));
        assertThrows(ConversionException.class, () -> conf.getDoubleArray("key2"));

        assertThrows(ConversionException.class, () -> conf.getDoubleList("key1"));
        assertThrows(ConversionException.class, () -> conf.getDoubleList("key2"));

        assertThrows(ConversionException.class, () -> conf.getBigIntegerArray("key1"));
        assertThrows(ConversionException.class, () -> conf.getBigIntegerArray("key2"));

        assertThrows(ConversionException.class, () -> conf.getBigIntegerList("key1"));
        assertThrows(ConversionException.class, () -> conf.getBigIntegerList("key2"));

        assertThrows(ConversionException.class, () -> conf.getBigDecimalArray("key1"));
        assertThrows(ConversionException.class, () -> conf.getBigDecimalArray("key2"));

        assertThrows(ConversionException.class, () -> conf.getBigDecimalList("key1"));
        assertThrows(ConversionException.class, () -> conf.getBigDecimalList("key2"));

        assertThrows(ConversionException.class, () -> conf.getURLArray("key1"));
        assertThrows(ConversionException.class, () -> conf.getURLArray("key2"));

        assertThrows(ConversionException.class, () -> conf.getURLList("key1"));
        assertThrows(ConversionException.class, () -> conf.getURLList("key2"));

        assertThrows(ConversionException.class, () -> conf.getLocaleArray("key1"));
        assertThrows(ConversionException.class, () -> conf.getLocaleArray("key2"));

        assertThrows(ConversionException.class, () -> conf.getLocaleList("key1"));
        assertThrows(ConversionException.class, () -> conf.getLocaleList("key2"));

        assertThrows(ConversionException.class, () -> conf.getColorArray("key1"));
        assertThrows(ConversionException.class, () -> conf.getColorArray("key2"));

        assertThrows(ConversionException.class, () -> conf.getColorList("key1"));
        assertThrows(ConversionException.class, () -> conf.getColorList("key2"));

        assertThrows(ConversionException.class, () -> conf.getDateArray("key1"));
        assertThrows(ConversionException.class, () -> conf.getDateArray("key2"));

        assertThrows(ConversionException.class, () -> conf.getDate("key1", DATE_PATTERN));
        assertThrows(ConversionException.class, () -> conf.getDate("key2", DATE_PATTERN));

        assertThrows(ConversionException.class, () -> conf.getDateList("key1"));
        assertThrows(ConversionException.class, () -> conf.getDateList("key2"));

        assertThrows(ConversionException.class, () -> conf.getCalendar("key1", DATE_PATTERN));
        assertThrows(ConversionException.class, () -> conf.getCalendar("key2", DATE_PATTERN));

        assertThrows(ConversionException.class, () -> conf.getCalendarArray("key1"));
        assertThrows(ConversionException.class, () -> conf.getCalendarArray("key2"));

        assertThrows(ConversionException.class, () -> conf.getCalendarList("key1"));
        assertThrows(ConversionException.class, () -> conf.getCalendarList("key2"));

        assertThrows(ConversionException.class, () -> conf.get(InetAddress.class, "key1"));

        assertThrows(ConversionException.class, () -> conf.get(InternetAddress.class, "key1"));
    }

    /**
     * Tests that the cause of a conversion exception is kept.
     */
    @Test
    public void testConversionExceptionCause() {
        final ConversionException cex = assertThrows(ConversionException.class, () -> conf.get(Integer.TYPE, "uri.string"));
        assertInstanceOf(NumberFormatException.class, cex.getCause());
    }

    @Test
    public void testGetArrayInvalidDefaultType() {
        assertThrows(IllegalArgumentException.class, () -> conf.getArray(Boolean.class, "unknownkey", new URL[] {}));
    }

    @Test
    public void testGetBigDecimalArray() {
        // missing list
        final BigDecimal[] defaultValue = {new BigDecimal("2"), new BigDecimal("1")};
        assertArrayEquals(defaultValue, conf.getBigDecimalArray("bigdecimal.list", defaultValue));

        final BigDecimal[] expected = {new BigDecimal("1"), new BigDecimal("2")};

        // list of strings
        assertArrayEquals(expected, conf.getBigDecimalArray("bigdecimal.list1"));

        // list of strings, comma separated
        assertArrayEquals(expected, conf.getBigDecimalArray("bigdecimal.list2"));

        // list of BigDecimal objects
        assertArrayEquals(expected, conf.getBigDecimalArray("bigdecimal.list3"));

        // array of BigDecimal objects
        assertArrayEquals(expected, conf.getBigDecimalArray("bigdecimal.list4"));

        // list of BigDecimal objects
        assertArrayEquals(expected, conf.getBigDecimalArray("bigdecimal.list6"));

        // list of interpolated values
        assertArrayEquals(expected, conf.getBigDecimalArray("bigdecimal.list.interpolated"));

        // single BigDecimal values
        assertArrayEquals(new BigDecimal[] {new BigDecimal("1")}, conf.getBigDecimalArray("bigdecimal.string"));
        assertArrayEquals(new BigDecimal[] {new BigDecimal("1")}, conf.getBigDecimalArray("bigdecimal.object"));

        // empty array
        assertArrayEquals(new BigDecimal[] {}, conf.getBigDecimalArray("empty"));
    }

    @Test
    public void testGetBigDecimalList() {
        // missing list
        assertNull(conf.getBigDecimalList("bigdecimal.list", null));

        List<Object> expected = new ArrayList<>();
        expected.add(new BigDecimal("1"));
        expected.add(new BigDecimal("2"));

        // list of strings
        assertEquals(expected, conf.getBigDecimalList("bigdecimal.list1"));

        // list of strings, comma separated
        assertEquals(expected, conf.getBigDecimalList("bigdecimal.list2"));

        // list of BigDecimal objects
        assertEquals(expected, conf.getBigDecimalList("bigdecimal.list3"));

        // array of BigDecimal objects
        assertEquals(expected, conf.getBigDecimalList("bigdecimal.list4"));

        // list of BigDecimal objects
        assertEquals(expected, conf.getBigDecimalList("bigdecimal.list6"));

        // list of interpolated values
        assertEquals(expected, conf.getBigDecimalList("bigdecimal.list.interpolated"));

        // single BigDecimal values
        expected = new ArrayList<>();
        expected.add(new BigDecimal("1"));
        assertEquals(expected, conf.getBigDecimalList("bigdecimal.string"));
        assertEquals(expected, conf.getBigDecimalList("bigdecimal.object"));

        // empty list
        assertEquals(new ArrayList<>(), conf.getBigDecimalList("empty"));
    }

    @Test
    public void testGetBigIntegerArray() {
        // missing list
        final BigInteger[] defaultValue = {new BigInteger("2"), new BigInteger("1")};
        assertArrayEquals(defaultValue, conf.getBigIntegerArray("biginteger.list", defaultValue));

        final BigInteger[] expected = {new BigInteger("1"), new BigInteger("2")};

        // list of strings
        assertArrayEquals(expected, conf.getBigIntegerArray("biginteger.list1"));

        // list of strings, comma separated
        assertArrayEquals(expected, conf.getBigIntegerArray("biginteger.list2"));

        // list of BigInteger objects
        assertArrayEquals(expected, conf.getBigIntegerArray("biginteger.list3"));

        // array of BigInteger objects
        assertArrayEquals(expected, conf.getBigIntegerArray("biginteger.list4"));

        // list of BigInteger objects
        assertArrayEquals(expected, conf.getBigIntegerArray("biginteger.list6"));

        // list of interpolated values
        assertArrayEquals(expected, conf.getBigIntegerArray("biginteger.list.interpolated"));

        // single BigInteger values
        assertArrayEquals(new BigInteger[] {new BigInteger("1")}, conf.getBigIntegerArray("biginteger.string"));
        assertArrayEquals(new BigInteger[] {new BigInteger("1")}, conf.getBigIntegerArray("biginteger.object"));

        // empty array
        assertArrayEquals(new BigInteger[] {}, conf.getBigIntegerArray("empty"));
    }

    @Test
    public void testGetBigIntegerList() {
        // missing list
        final List<BigInteger> bigIntegerList = conf.getBigIntegerList("biginteger.list", null);
        assertNull(bigIntegerList);

        List<Object> expected = new ArrayList<>();
        expected.add(new BigInteger("1"));
        expected.add(new BigInteger("2"));

        // list of strings
        assertEquals(expected, conf.getBigIntegerList("biginteger.list1"));

        // list of strings, comma separated
        assertEquals(expected, conf.getBigIntegerList("biginteger.list2"));

        // list of BigInteger objects
        assertEquals(expected, conf.getBigIntegerList("biginteger.list3"));

        // array of BigInteger objects
        assertEquals(expected, conf.getBigIntegerList("biginteger.list4"));

        // list of BigInteger objects
        assertEquals(expected, conf.getBigIntegerList("biginteger.list6"));

        // list of interpolated values
        assertEquals(expected, conf.getBigIntegerList("biginteger.list.interpolated"));

        // single BigInteger values
        expected = new ArrayList<>();
        expected.add(new BigInteger("1"));
        assertEquals(expected, conf.getBigIntegerList("biginteger.string"));
        assertEquals(expected, conf.getBigIntegerList("biginteger.object"));

        // empty list
        assertEquals(new ArrayList<>(), conf.getBigIntegerList("empty"));
    }

    @Test
    public void testGetBooleanArray() {
        // missing list
        final boolean[] defaultValue = {false, true};
        assertArrayEquals(defaultValue, conf.getBooleanArray("boolean.list", defaultValue));

        final boolean[] expected = {true, false};

        // list of strings
        assertArrayEquals(expected, conf.getBooleanArray("boolean.list1"));

        // list of strings, comma separated
        assertArrayEquals(expected, conf.getBooleanArray("boolean.list2"));

        // list of Boolean objects
        assertArrayEquals(expected, conf.getBooleanArray("boolean.list3"));

        // array of Boolean objects
        assertArrayEquals(expected, conf.getBooleanArray("boolean.list4"));

        // array of boolean primitives
        assertArrayEquals(expected, conf.getBooleanArray("boolean.list5"));

        // list of Boolean objects
        assertArrayEquals(expected, conf.getBooleanArray("boolean.list6"));

        // list of interpolated values
        assertArrayEquals(expected, conf.getBooleanArray("boolean.list.interpolated"));

        // single boolean values
        assertArrayEquals(new boolean[] {true}, conf.getBooleanArray("boolean.string"));
        assertArrayEquals(new boolean[] {true}, conf.getBooleanArray("boolean.object"));

        // empty array
        assertArrayEquals(new boolean[] {}, conf.getBooleanArray("empty"));
    }

    @Test
    public void testGetBooleanList() {
        // missing list
        assertNull(conf.getBooleanList("boolean.list", null));

        List<Object> expected = new ArrayList<>();
        expected.add(Boolean.TRUE);
        expected.add(Boolean.FALSE);

        // list of strings
        assertEquals(expected, conf.getBooleanList("boolean.list1"));

        // list of strings, comma separated
        assertEquals(expected, conf.getBooleanList("boolean.list2"));

        // list of Boolean objects
        assertEquals(expected, conf.getBooleanList("boolean.list3"));

        // array of Boolean objects
        assertEquals(expected, conf.getBooleanList("boolean.list4"));

        // array of boolean primitives
        assertEquals(expected, conf.getBooleanList("boolean.list5"));

        // list of Boolean objects
        assertEquals(expected, conf.getBooleanList("boolean.list6"));

        // list of interpolated values
        assertEquals(expected, conf.getBooleanList("boolean.list.interpolated"));

        // single boolean values
        expected = new ArrayList<>();
        expected.add(Boolean.TRUE);
        assertEquals(expected, conf.getBooleanList("boolean.string"));
        assertEquals(expected, conf.getBooleanList("boolean.object"));

        // empty list
        assertEquals(new ArrayList<>(), conf.getBooleanList("empty"));
    }

    @Test
    public void testGetByteArray() {
        // missing list
        final byte[] defaultValue = {1, 2};
        assertArrayEquals(defaultValue, conf.getByteArray("byte.list", defaultValue));

        final byte[] expected = {1, 2};

        // list of strings
        assertArrayEquals(expected, conf.getByteArray("byte.list1"));

        // list of strings, comma separated
        assertArrayEquals(expected, conf.getByteArray("byte.list2"));

        // list of Byte objects
        assertArrayEquals(expected, conf.getByteArray("byte.list3"));

        // array of Byte objects
        assertArrayEquals(expected, conf.getByteArray("byte.list4"));

        // array of byte primitives
        assertArrayEquals(expected, conf.getByteArray("byte.list5"));

        // list of Byte objects
        assertArrayEquals(expected, conf.getByteArray("byte.list6"));

        // list of interpolated values
        assertArrayEquals(expected, conf.getByteArray("byte.list.interpolated"));

        // single byte values
        assertArrayEquals(new byte[] {1}, conf.getByteArray("byte.string"));
        assertArrayEquals(new byte[] {1}, conf.getByteArray("byte.object"));

        // empty array
        assertArrayEquals(new byte[] {}, conf.getByteArray("empty"));
    }

    @Test
    public void testGetByteList() {
        // missing list
        assertNull(conf.getByteList("byte.list", null));

        List<Object> expected = new ArrayList<>();
        expected.add(Byte.valueOf("1"));
        expected.add(Byte.valueOf("2"));

        // list of strings
        assertEquals(expected, conf.getByteList("byte.list1"));

        // list of strings, comma separated
        assertEquals(expected, conf.getByteList("byte.list2"));

        // list of Byte objects
        assertEquals(expected, conf.getByteList("byte.list3"));

        // array of Byte objects
        assertEquals(expected, conf.getByteList("byte.list4"));

        // array of byte primitives
        assertEquals(expected, conf.getByteList("byte.list5"));

        // list of Byte objects
        assertEquals(expected, conf.getByteList("byte.list6"));

        // list of interpolated values
        assertEquals(expected, conf.getByteList("byte.list.interpolated"));

        // single byte values
        expected = new ArrayList<>();
        expected.add(Byte.valueOf("1"));
        assertEquals(expected, conf.getByteList("byte.string"));
        assertEquals(expected, conf.getByteList("byte.object"));

        // empty list
        assertEquals(new ArrayList<>(), conf.getByteList("empty"));
    }

    @Test
    public void testGetCalendar() throws Exception {
        final DateFormat format = new SimpleDateFormat(DATE_PATTERN);

        // missing Date
        final Calendar defaultValue = Calendar.getInstance();
        defaultValue.setTime(new Date());
        assertEquals(conf.getCalendar("calendar", defaultValue), defaultValue);
        assertNull(conf.getCalendar("unknownkey", DATE_PATTERN));

        conf.setThrowExceptionOnMissing(true);

        assertThrows(NoSuchElementException.class, () -> conf.getCalendar("unknownkey", DATE_PATTERN));

        final Calendar expected = Calendar.getInstance();
        expected.setTime(format.parse("2004-01-01"));

        // Calendar string
        assertEquals(expected, conf.getCalendar("calendar.string"));
        assertEquals(conf.getCalendar("calendar.string", DATE_PATTERN), expected);

        // Calendar object
        assertEquals(expected, conf.getCalendar("calendar.object"));

        // Date object
        assertEquals(expected, conf.getCalendar("date.object"));

        // interpolated value
        assertEquals(expected, conf.getCalendar("calendar.string.interpolated"));
    }

    @Test
    public void testGetCalendarArray() throws Exception {
        final DateFormat format = new SimpleDateFormat(DATE_PATTERN);
        final Date date1 = format.parse("2004-01-01");
        final Date date2 = format.parse("2004-12-31");
        final Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        final Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        // missing list
        final Calendar[] defaultValue = {calendar2, calendar1};
        assertArrayEquals(defaultValue, conf.getCalendarArray("calendar.list", defaultValue));

        final Calendar[] expected = {calendar1, calendar2};

        // list of strings
        assertArrayEquals(expected, conf.getCalendarArray("calendar.list1"));

        // list of strings, comma separated
        assertArrayEquals(expected, conf.getCalendarArray("calendar.list2"));

        // list of Calendar objects
        assertArrayEquals(expected, conf.getCalendarArray("calendar.list3"));

        // array of Calendar objects
        assertArrayEquals(expected, conf.getCalendarArray("calendar.list4"));

        // list of Date objects
        assertArrayEquals(expected, conf.getCalendarArray("calendar.list5"));

        // list of Calendar objects
        assertArrayEquals(expected, conf.getCalendarArray("calendar.list6"));

        // list of interpolated values
        assertArrayEquals(expected, conf.getCalendarArray("calendar.list.interpolated"));

        // single Calendar values
        assertArrayEquals(new Calendar[] {calendar1}, conf.getCalendarArray("calendar.string"));
        assertArrayEquals(new Calendar[] {calendar1}, conf.getCalendarArray("calendar.object"));

        // empty array
        assertArrayEquals(new Calendar[] {}, conf.getCalendarArray("empty"));
    }

    @Test
    public void testGetCalendarArrayWithFormat() throws Exception {
        final DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        final Date date1 = format.parse("01/01/2004");
        final Date date2 = format.parse("12/31/2004");

        final Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        final Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        final Calendar[] expected = {calendar1, calendar2};

        conf.addProperty("calendar.format", "01/01/2004");
        conf.addProperty("calendar.format", "12/31/2004");
        assertArrayEquals(expected, conf.getCalendarArray("calendar.format", "MM/dd/yyyy"));
    }

    @Test
    public void testGetCalendarList() throws Exception {
        final DateFormat format = new SimpleDateFormat(DATE_PATTERN);
        final Date date1 = format.parse("2004-01-01");
        final Date date2 = format.parse("2004-12-31");
        final Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        final Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);

        // missing list
        final List<Calendar> nullList = null;
        assertNull(conf.getCalendarList("calendar.list", nullList));

        List<Object> expected = new ArrayList<>();
        expected.add(calendar1);
        expected.add(calendar2);

        // list of strings
        assertEquals(expected, conf.getCalendarList("calendar.list1"));
        assertEquals(conf.getList(Calendar.class, "calendar.list1"), expected);

        // list of strings, comma separated
        assertEquals(expected, conf.getCalendarList("calendar.list2"));

        // list of Calendar objects
        assertEquals(expected, conf.getCalendarList("calendar.list3"));

        // array of Calendar objects
        assertEquals(expected, conf.getCalendarList("calendar.list4"));

        // list of Date objects
        assertEquals(expected, conf.getCalendarList("calendar.list5"));

        // list of Calendar objects
        assertEquals(expected, conf.getCalendarList("calendar.list6"));

        // array of strings
        assertEquals(conf.getList(Calendar.class, "calendar.list7"), expected);

        // list of interpolated values
        assertEquals(expected, conf.getCalendarList("calendar.list.interpolated"));

        // single Calendar values
        expected = new ArrayList<>();
        expected.add(calendar1);
        assertEquals(expected, conf.getCalendarList("date.string"));
        assertEquals(expected, conf.getCalendarList("date.object"));

        // empty list
        assertEquals(new ArrayList<>(), conf.getCalendarList("empty"));
    }

    @Test
    public void testGetColor() {
        // RRGGBB
        conf.setProperty("color", "FF0000");
        assertEquals(Color.red, conf.getColor("color"));

        // #RRGGBB
        conf.setProperty("color", "#00FF00");
        assertEquals(Color.green, conf.getColor("color"));

        // #RRGGBBAA
        conf.setProperty("color", "#01030507");
        final Color color = conf.getColor("color");
        assertNotNull(color);
        assertEquals(1, color.getRed());
        assertEquals(3, color.getGreen());
        assertEquals(5, color.getBlue());
        assertEquals(7, color.getAlpha());

        // interpolated value
        assertEquals(Color.red, conf.getColor("color.string.interpolated"));

        // default value
        assertEquals(Color.cyan, conf.getColor("unknownkey", Color.cyan));
    }

    @Test
    public void testGetColorArray() throws Exception {
        // missing list
        final Color[] defaultValue = {Color.red, Color.blue};
        assertArrayEquals(defaultValue, conf.getColorArray("color.list", defaultValue));

        final Color[] expected = {Color.red, Color.blue};

        // list of strings
        assertArrayEquals(expected, conf.getColorArray("color.list1"));

        // list of strings, comma separated
        assertArrayEquals(expected, conf.getColorArray("color.list2"));

        // list of Color objects
        assertArrayEquals(expected, conf.getColorArray("color.list3"));

        // array of Color objects
        assertArrayEquals(expected, conf.getColorArray("color.list4"));

        // list of Color objects
        assertArrayEquals(expected, conf.getColorArray("color.list6"));

        // list of interpolated values
        assertArrayEquals(expected, conf.getColorArray("color.list.interpolated"));

        // single Color values
        assertArrayEquals(new Color[] {Color.red}, conf.getColorArray("color.string"));
        assertArrayEquals(new Color[] {Color.red}, conf.getColorArray("color.object"));

        // empty array
        assertArrayEquals(new Color[] {}, conf.getColorArray("empty"));
    }

    @Test
    public void testGetColorList() throws Exception {
        // missing list
        assertNull(conf.getColorList("color.list", null));

        List<Object> expected = new ArrayList<>();
        expected.add(Color.red);
        expected.add(Color.blue);

        // list of strings
        assertEquals(expected, conf.getColorList("color.list1"));

        // list of strings, comma separated
        assertEquals(expected, conf.getColorList("color.list2"));

        // list of Color objects
        assertEquals(expected, conf.getColorList("color.list3"));

        // array of Color objects
        assertEquals(expected, conf.getColorList("color.list4"));

        // list of Color objects
        assertEquals(expected, conf.getColorList("color.list6"));

        // list of interpolated values
        assertEquals(expected, conf.getColorList("color.list.interpolated"));

        // single Color values
        expected = new ArrayList<>();
        expected.add(Color.red);
        assertEquals(expected, conf.getColorList("color.string"));
        assertEquals(expected, conf.getColorList("color.object"));

        // empty list
        assertEquals(new ArrayList<>(), conf.getColorList("empty"));
    }

    @Test
    public void testGetConfiguration() {
        final Configuration baseconf = new BaseConfiguration();
        final DataConfiguration conf = new DataConfiguration(baseconf);

        assertEquals(baseconf, conf.getConfiguration());
    }

    @Test
    public void testGetDate() throws Exception {
        final Date expected = expectedDate();

        // missing Date
        final Date defaultValue = new Date();
        assertEquals(conf.getDate("date", defaultValue), defaultValue);
        assertNull(conf.getDate("unknownkey", DATE_PATTERN));

        conf.setThrowExceptionOnMissing(true);

        assertThrows(NoSuchElementException.class, () -> conf.getDate("unknownkey", DATE_PATTERN));

        // Date string
        assertEquals(expected, conf.getDate("date.string"));
        assertEquals(conf.getDate("date.string", DATE_PATTERN), expected);

        // Date object
        assertEquals(expected, conf.getDate("date.object"));

        // Calendar object
        assertEquals(expected, conf.getDate("calendar.object"));

        // interpolated value
        assertEquals(expected, conf.getDate("date.string.interpolated"));
    }

    @Test
    public void testGetDateArray() throws Exception {
        final DateFormat format = new SimpleDateFormat(DATE_PATTERN);
        final Date date1 = format.parse("2004-01-01");
        final Date date2 = format.parse("2004-12-31");

        // missing list
        final Date[] defaultValue = {date2, date1};
        assertArrayEquals(defaultValue, conf.getDateArray("date.list", defaultValue));

        final Date[] expected = {date1, date2};

        // list of strings
        assertArrayEquals(expected, conf.getDateArray("date.list1"));

        // list of strings, comma separated
        assertArrayEquals(expected, conf.getDateArray("date.list2"));

        // list of Date objects
        assertArrayEquals(expected, conf.getDateArray("date.list3"));

        // array of Date objects
        assertArrayEquals(expected, conf.getDateArray("date.list4"));

        // list of Calendar objects
        assertArrayEquals(expected, conf.getDateArray("date.list5"));

        // list of Date objects
        assertArrayEquals(expected, conf.getDateArray("date.list6"));

        // list of interpolated values
        assertArrayEquals(expected, conf.getDateArray("date.list.interpolated"));

        // single Date values
        assertArrayEquals(new Date[] {date1}, conf.getDateArray("date.string"));
        assertArrayEquals(new Date[] {date1}, conf.getDateArray("date.object"));

        // empty array
        assertArrayEquals(new Date[] {}, conf.getDateArray("empty"));
    }

    @Test
    public void testGetDateArrayWithFormat() throws Exception {
        final DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        final Date date1 = format.parse("01/01/2004");
        final Date date2 = format.parse("12/31/2004");
        final Date[] expected = {date1, date2};

        conf.addProperty("date.format", "01/01/2004");
        conf.addProperty("date.format", "12/31/2004");
        assertArrayEquals(expected, conf.getDateArray("date.format", "MM/dd/yyyy"));
    }

    @Test
    public void testGetDateList() throws Exception {
        final DateFormat format = new SimpleDateFormat(DATE_PATTERN);
        final Date date1 = format.parse("2004-01-01");
        final Date date2 = format.parse("2004-12-31");

        // missing list
        final List<Date> nullList = null;
        assertNull(conf.getDateList("date.list", nullList));

        List<Object> expected = new ArrayList<>();
        expected.add(date1);
        expected.add(date2);

        // list of strings
        assertEquals(expected, conf.getDateList("date.list1"));
        assertEquals(conf.getList(Date.class, "date.list1"), expected);

        // list of strings, comma separated
        assertEquals(expected, conf.getDateList("date.list2"));

        // list of Date objects
        assertEquals(expected, conf.getDateList("date.list3"));

        // array of Date objects
        assertEquals(expected, conf.getDateList("date.list4"));

        // list of Calendar objects
        assertEquals(expected, conf.getDateList("date.list5"));

        // list of Date objects
        assertEquals(expected, conf.getDateList("date.list6"));

        // array of strings
        assertEquals(conf.getList(Date.class, "date.list7"), expected);

        // list of interpolated values
        assertEquals(expected, conf.getDateList("date.list.interpolated"));

        // single Date values
        expected = new ArrayList<>();
        expected.add(date1);
        assertEquals(expected, conf.getDateList("date.string"));
        assertEquals(expected, conf.getDateList("date.object"));

        // empty list
        assertEquals(new ArrayList<>(), conf.getDateList("empty"));
    }

    /**
     * Tests a conversion to a Date if no property is set with the date format, and the format is specified in the
     * conversion handler.
     */
    @Test
    public void testGetDateNoFormatPropertyConversionHandler() throws Exception {
        conf.clearProperty(DataConfiguration.DATE_FORMAT_KEY);
        final DefaultConversionHandler handler = new DefaultConversionHandler();
        handler.setDateFormat(DATE_PATTERN);
        conf.setConversionHandler(handler);
        assertEquals(expectedDate(), conf.getDate("date.string"));
    }

    /**
     * Tests a conversion to a Date if no property is set with the date format, and the format is directly passed in.
     */
    @Test
    public void testGetDateNoFormatPropertyDirectlySpecified() throws Exception {
        conf.clearProperty(DataConfiguration.DATE_FORMAT_KEY);
        assertEquals(expectedDate(), conf.getDate("date.string", DATE_PATTERN));
    }

    @Test
    public void testGetDoubleArray() {
        // missing list
        final double[] defaultValue = {2, 1};
        assertArrayEquals(defaultValue, conf.getDoubleArray("double.list", defaultValue), 0);

        final double[] expected = {1, 2};

        // list of strings
        assertArrayEquals(expected, conf.getDoubleArray("double.list1"), 0);

        // list of strings, comma separated
        assertArrayEquals(expected, conf.getDoubleArray("double.list2"), 0);

        // list of Double objects
        assertArrayEquals(expected, conf.getDoubleArray("double.list3"), 0);

        // array of Double objects
        assertArrayEquals(expected, conf.getDoubleArray("double.list4"), 0);

        // array of double primitives
        assertArrayEquals(expected, conf.getDoubleArray("double.list5"), 0);

        // list of Double objects
        assertArrayEquals(expected, conf.getDoubleArray("double.list6"), 0);

        // list of interpolated values
        assertArrayEquals(expected, conf.getDoubleArray("double.list.interpolated"), 0);

        // single double values
        assertArrayEquals(new double[] {1}, conf.getDoubleArray("double.string"), 0);
        assertArrayEquals(new double[] {1}, conf.getDoubleArray("double.object"), 0);

        // empty array
        assertArrayEquals(new double[] {}, conf.getDoubleArray("empty"), 0);
    }

    @Test
    public void testGetDoubleList() {
        // missing list
        assertNull(conf.getDoubleList("double.list", null));

        List<Object> expected = new ArrayList<>();
        expected.add(Double.valueOf("1"));
        expected.add(Double.valueOf("2"));

        // list of strings
        assertEquals(expected, conf.getDoubleList("double.list1"));

        // list of strings, comma separated
        assertEquals(expected, conf.getDoubleList("double.list2"));

        // list of Double objects
        assertEquals(expected, conf.getDoubleList("double.list3"));

        // array of Double objects
        assertEquals(expected, conf.getDoubleList("double.list4"));

        // array of double primitives
        assertEquals(expected, conf.getDoubleList("double.list5"));

        // list of Double objects
        assertEquals(expected, conf.getDoubleList("double.list6"));

        // list of interpolated values
        assertEquals(expected, conf.getDoubleList("double.list.interpolated"));

        // single double values
        expected = new ArrayList<>();
        expected.add(Double.valueOf("1"));
        assertEquals(expected, conf.getDoubleList("double.string"));
        assertEquals(expected, conf.getDoubleList("double.object"));

        // empty list
        assertEquals(new ArrayList<>(), conf.getDoubleList("empty"));
    }

    @Test
    public void testGetFloatArray() {
        // missing list
        final float[] defaultValue = {2, 1};
        assertArrayEquals(defaultValue, conf.getFloatArray("float.list", defaultValue), 0);

        final float[] expected = {1, 2};

        // list of strings
        assertArrayEquals(expected, conf.getFloatArray("float.list1"), 0);

        // list of strings, comma separated
        assertArrayEquals(expected, conf.getFloatArray("float.list2"), 0);

        // list of Float objects
        assertArrayEquals(expected, conf.getFloatArray("float.list3"), 0);

        // array of Float objects
        assertArrayEquals(expected, conf.getFloatArray("float.list4"), 0);

        // array of float primitives
        assertArrayEquals(expected, conf.getFloatArray("float.list5"), 0);

        // list of Float objects
        assertArrayEquals(expected, conf.getFloatArray("float.list6"), 0);

        // list of interpolated values
        assertArrayEquals(expected, conf.getFloatArray("float.list.interpolated"), 0);

        // single float values
        assertArrayEquals(new float[] {1}, conf.getFloatArray("float.string"), 0);
        assertArrayEquals(new float[] {1}, conf.getFloatArray("float.object"), 0);

        // empty array
        assertArrayEquals(new float[] {}, conf.getFloatArray("empty"), 0);
    }

    @Test
    public void testGetFloatList() {
        // missing list
        assertNull(conf.getFloatList("float.list", null));

        List<Object> expected = new ArrayList<>();
        expected.add(Float.valueOf("1"));
        expected.add(Float.valueOf("2"));

        // list of strings
        assertEquals(expected, conf.getFloatList("float.list1"));

        // list of strings, comma separated
        assertEquals(expected, conf.getFloatList("float.list2"));

        // list of Float objects
        assertEquals(expected, conf.getFloatList("float.list3"));

        // array of Float objects
        assertEquals(expected, conf.getFloatList("float.list4"));

        // array of float primitives
        assertEquals(expected, conf.getFloatList("float.list5"));

        // list of Float objects
        assertEquals(expected, conf.getFloatList("float.list6"));

        // list of interpolated values
        assertEquals(expected, conf.getFloatList("float.list.interpolated"));

        // single float values
        expected = new ArrayList<>();
        expected.add(Float.valueOf("1"));
        assertEquals(expected, conf.getFloatList("float.string"));
        assertEquals(expected, conf.getFloatList("float.object"));

        // empty list
        assertEquals(new ArrayList<>(), conf.getFloatList("empty"));
    }

    @Test
    public void testGetInetAddress() throws Exception {
        final InetAddress expected = InetAddress.getByName("127.0.0.1");

        // address as string
        assertEquals(conf.get(InetAddress.class, "ip.string"), expected);

        // address object
        assertEquals(conf.get(InetAddress.class, "ip.object"), expected);

        // interpolated value
        assertEquals(conf.get(InetAddress.class, "ip.string.interpolated"), expected);
    }

    @Test
    public void testGetInetAddressInvalidType() {
        conf.setProperty("ip.unknownhost", "foo");
        assertThrows(ConversionException.class, () -> conf.get(InetAddress.class, "ip.unknownhost"));
    }

    @Test
    public void testGetIntegerArray() {
        // missing list
        final int[] defaultValue = {2, 1};
        assertArrayEquals(conf.getIntArray("integer.list", defaultValue), defaultValue);

        final int[] expected = {1, 2};

        // list of strings
        assertArrayEquals(expected, conf.getIntArray("integer.list1"));

        // list of strings, comma separated
        assertArrayEquals(expected, conf.getIntArray("integer.list2"));

        // list of Integer objects
        assertArrayEquals(expected, conf.getIntArray("integer.list3"));

        // array of Integer objects
        assertArrayEquals(expected, conf.getIntArray("integer.list4"));

        // array of int primitives
        assertArrayEquals(expected, conf.getIntArray("integer.list5"));

        // list of Integer objects
        assertArrayEquals(expected, conf.getIntArray("integer.list6"));

        // list of interpolated values
        assertArrayEquals(expected, conf.getIntArray("integer.list.interpolated"));

        // single int values
        assertArrayEquals(new int[] {1}, conf.getIntArray("integer.string"));
        assertArrayEquals(new int[] {1}, conf.getIntArray("integer.object"));

        // empty array
        assertArrayEquals(new int[] {}, conf.getIntArray("empty"));
    }

    @Test
    public void testGetIntegerList() {
        // missing list
        assertNull(conf.getIntegerList("integer.list", null));

        List<Object> expected = new ArrayList<>();
        expected.add(Integer.valueOf("1"));
        expected.add(Integer.valueOf("2"));

        // list of strings
        assertEquals(expected, conf.getIntegerList("integer.list1"));

        // list of strings, comma separated
        assertEquals(expected, conf.getIntegerList("integer.list2"));

        // list of Integer objects
        assertEquals(expected, conf.getIntegerList("integer.list3"));

        // array of Integer objects
        assertEquals(expected, conf.getIntegerList("integer.list4"));

        // array of int primitives
        assertEquals(expected, conf.getIntegerList("integer.list5"));

        // list of Integer objects
        assertEquals(expected, conf.getIntegerList("integer.list6"));

        // list of interpolated values
        assertEquals(expected, conf.getIntegerList("integer.list.interpolated"));

        // single int values
        expected = new ArrayList<>();
        expected.add(Integer.valueOf("1"));
        assertEquals(expected, conf.getIntegerList("integer.string"));
        assertEquals(expected, conf.getIntegerList("integer.object"));

        // empty list
        assertEquals(new ArrayList<>(), conf.getIntegerList("empty"));
    }

    @Test
    public void testGetInternetAddress() throws Exception {
        final Object expected = new InternetAddress("dev@test.org");

        // address as string
        assertEquals(conf.get(expected.getClass(), "email.string"), expected);

        // address object
        assertEquals(conf.get(expected.getClass(), "email.object"), expected);

        // interpolated value
        assertEquals(conf.get(expected.getClass(), "email.string.interpolated"), expected);

        conf.setProperty("email.invalid", "dev@test@org");
        assertThrows(ConversionException.class, () -> conf.get(expected.getClass(), "email.invalid"));
    }

    @Test
    public void testGetInternetAddressInvalidType() throws Exception {
        final Object expected = new InternetAddress("dev@test.org");
        conf.setProperty("email.invalid", "dev@test@org");
        assertThrows(ConversionException.class, () -> conf.get(expected.getClass(), "email.invalid"));
    }

    @Test
    public void testGetInvalidType() {
        assertThrows(ConversionException.class, () -> conf.get(Boolean.class, "url.object", null));
    }

    @Test
    public void testGetKeys() {
        final Configuration baseconf = new BaseConfiguration();
        final DataConfiguration conf = new DataConfiguration(baseconf);

        baseconf.setProperty("foo", "bar");

        final Iterator<String> it = conf.getKeys();
        assertTrue(it.hasNext());
        assertEquals("foo", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testGetLocale() {
        // language
        conf.setProperty("locale", "fr");
        assertEquals(new Locale("fr", ""), conf.getLocale("locale"));

        // language + variant
        conf.setProperty("locale", "fr__POSIX");
        assertEquals(new Locale("fr", "", "POSIX"), conf.getLocale("locale"));

        // country
        conf.setProperty("locale", "_FR");
        assertEquals(new Locale("", "FR"), conf.getLocale("locale"));

        // country + variant
        conf.setProperty("locale", "_FR_WIN");
        assertEquals(new Locale("", "FR", "WIN"), conf.getLocale("locale"));

        // language + country
        conf.setProperty("locale", "fr_FR");
        assertEquals(new Locale("fr", "FR"), conf.getLocale("locale"));

        // language + country + variant
        conf.setProperty("locale", "fr_FR_MAC");
        assertEquals(new Locale("fr", "FR", "MAC"), conf.getLocale("locale"));

        // default value
        conf.setProperty("locale", "fr");
        assertEquals(Locale.FRENCH, conf.getLocale("locale", Locale.GERMAN));
        assertEquals(Locale.GERMAN, conf.getLocale("localeNotInConfig", Locale.GERMAN));

        // interpolated value
        assertEquals(Locale.FRENCH, conf.getLocale("locale.string.interpolated"));
    }

    @Test
    public void testGetLocaleArray() throws Exception {
        // missing list
        final Locale[] defaultValue = {Locale.GERMAN, Locale.FRENCH};
        assertArrayEquals(conf.getLocaleArray("locale.list", defaultValue), defaultValue);

        final Locale[] expected = {Locale.FRENCH, Locale.GERMAN};

        // list of strings
        assertArrayEquals(expected, conf.getLocaleArray("locale.list1"));

        // list of strings, comma separated
        assertArrayEquals(expected, conf.getLocaleArray("locale.list2"));

        // list of Locale objects
        assertArrayEquals(expected, conf.getLocaleArray("locale.list3"));

        // array of Locale objects
        assertArrayEquals(expected, conf.getLocaleArray("locale.list4"));

        // list of Locale objects
        assertArrayEquals(expected, conf.getLocaleArray("locale.list6"));

        // list of interpolated values
        assertArrayEquals(expected, conf.getLocaleArray("locale.list.interpolated"));

        // single Locale values
        assertArrayEquals(new Locale[] {Locale.FRENCH}, conf.getLocaleArray("locale.string"));
        assertArrayEquals(new Locale[] {Locale.FRENCH}, conf.getLocaleArray("locale.object"));

        // empty array
        assertArrayEquals(new Locale[] {}, conf.getLocaleArray("empty"));
    }

    @Test
    public void testGetLocaleList() throws Exception {
        // missing list
        assertNull(conf.getLocaleList("locale.list", null));

        List<Object> expected = new ArrayList<>();
        expected.add(Locale.FRENCH);
        expected.add(Locale.GERMAN);

        // list of strings
        assertEquals(expected, conf.getLocaleList("locale.list1"));

        // list of strings, comma separated
        assertEquals(expected, conf.getLocaleList("locale.list2"));

        // list of Locale objects
        assertEquals(expected, conf.getLocaleList("locale.list3"));

        // array of Locale objects
        assertEquals(expected, conf.getLocaleList("locale.list4"));

        // list of Locale objects
        assertEquals(expected, conf.getLocaleList("locale.list6"));

        // list of interpolated values
        assertEquals(expected, conf.getLocaleList("locale.list.interpolated"));

        // single Locale values
        expected = new ArrayList<>();
        expected.add(Locale.FRENCH);
        assertEquals(expected, conf.getLocaleList("locale.string"));
        assertEquals(expected, conf.getLocaleList("locale.object"));

        // empty list
        assertEquals(new ArrayList<>(), conf.getLocaleList("empty"));
    }

    @Test
    public void testGetLongArray() {
        // missing list
        final long[] defaultValue = {2, 1};
        assertArrayEquals(conf.getLongArray("long.list", defaultValue), defaultValue);

        final long[] expected = {1, 2};

        // list of strings
        assertArrayEquals(expected, conf.getLongArray("long.list1"));

        // list of strings, comma separated
        assertArrayEquals(expected, conf.getLongArray("long.list2"));

        // list of Long objects
        assertArrayEquals(expected, conf.getLongArray("long.list3"));

        // array of Long objects
        assertArrayEquals(expected, conf.getLongArray("long.list4"));

        // array of long primitives
        assertArrayEquals(expected, conf.getLongArray("long.list5"));

        // list of Long objects
        assertArrayEquals(expected, conf.getLongArray("long.list6"));

        // list of interpolated values
        assertArrayEquals(expected, conf.getLongArray("long.list.interpolated"));

        // single long values
        assertArrayEquals(new long[] {1}, conf.getLongArray("long.string"));
        assertArrayEquals(new long[] {1}, conf.getLongArray("long.object"));

        // empty array
        assertArrayEquals(new long[] {}, conf.getLongArray("empty"));
    }

    @Test
    public void testGetLongList() {
        // missing list
        assertNull(conf.getLongList("long.list", null));

        List<Object> expected = new ArrayList<>();
        expected.add(Long.valueOf("1"));
        expected.add(Long.valueOf("2"));

        // list of strings
        assertEquals(expected, conf.getLongList("long.list1"));

        // list of strings, comma separated
        assertEquals(expected, conf.getLongList("long.list2"));

        // list of Long objects
        assertEquals(expected, conf.getLongList("long.list3"));

        // array of Long objects
        assertEquals(expected, conf.getLongList("long.list4"));

        // array of long primitives
        assertEquals(expected, conf.getLongList("long.list5"));

        // list of Long objects
        assertEquals(expected, conf.getLongList("long.list6"));

        // list of interpolated values
        assertEquals(expected, conf.getLongList("long.list.interpolated"));

        // single long values
        expected = new ArrayList<>();
        expected.add(Long.valueOf("1"));
        assertEquals(expected, conf.getLongList("long.string"));
        assertEquals(expected, conf.getLongList("long.object"));

        // empty list
        assertEquals(new ArrayList<>(), conf.getLongList("empty"));
    }

    @Test
    public void testGetPrimitiveArrayInvalidType() {
        assertThrows(ConversionException.class, () -> conf.getArray(Boolean.TYPE, "calendar.list4"));
    }

    /**
     * Tests whether a string property can be obtained through get() if no type conversion is required.
     */
    @Test
    public void testGetPropertyWithoutConversion() {
        final String key = "test.str";
        final String value = "someTestValue";
        conf.addProperty(key, value);
        assertEquals(value, conf.get(String.class, key));
    }

    @Test
    public void testGetShortArray() {
        // missing list
        final short[] defaultValue = {2, 1};
        assertArrayEquals(conf.getShortArray("short.list", defaultValue), defaultValue);

        final short[] expected = {1, 2};

        // list of strings
        assertArrayEquals(expected, conf.getShortArray("short.list1"));

        // list of strings, comma separated
        assertArrayEquals(expected, conf.getShortArray("short.list2"));

        // list of Byte objects
        assertArrayEquals(expected, conf.getShortArray("short.list3"));

        // array of Byte objects
        assertArrayEquals(expected, conf.getShortArray("short.list4"));

        // array of byte primitives
        assertArrayEquals(expected, conf.getShortArray("short.list5"));

        // list of Byte objects
        assertArrayEquals(expected, conf.getShortArray("short.list6"));

        // list of interpolated values
        assertArrayEquals(expected, conf.getShortArray("short.list.interpolated"));

        // single byte values
        assertArrayEquals(new short[] {1}, conf.getShortArray("short.string"));
        assertArrayEquals(new short[] {1}, conf.getShortArray("short.object"));

        // empty array
        assertArrayEquals(new short[] {}, conf.getShortArray("empty"));
    }

    @Test
    public void testGetShortList() {
        // missing list
        assertNull(conf.getShortList("short.list", null));

        List<Object> expected = new ArrayList<>();
        expected.add(Short.valueOf("1"));
        expected.add(Short.valueOf("2"));

        // list of strings
        assertEquals(expected, conf.getShortList("short.list1"));

        // list of strings, comma separated
        assertEquals(expected, conf.getShortList("short.list2"));

        // list of Short objects
        assertEquals(expected, conf.getShortList("short.list3"));

        // array of Short objects
        assertEquals(expected, conf.getShortList("short.list4"));

        // array of short primitives
        assertEquals(expected, conf.getShortList("short.list5"));

        // list of Short objects
        assertEquals(expected, conf.getShortList("short.list6"));

        // list of interpolated values
        assertEquals(expected, conf.getShortList("short.list.interpolated"));

        // single short values
        expected = new ArrayList<>();
        expected.add(Short.valueOf("1"));
        assertEquals(expected, conf.getShortList("short.string"));
        assertEquals(expected, conf.getShortList("short.object"));

        // empty list
        assertEquals(new ArrayList<>(), conf.getShortList("empty"));
    }

    @Test
    public void testGetUnknown() {
        assertNull(conf.get(Object.class, "unknownkey"));
    }

    @Test
    public void testGetUnknownException() {
        conf.setThrowExceptionOnMissing(true);
        assertThrows(NoSuchElementException.class, () -> conf.get(Object.class, "unknownkey"));
    }

    @Test
    public void testGetURI() throws Exception {
        // missing URI
        final URI defaultValue = new URI("http://www.google.com");
        assertEquals(conf.getURI("url", defaultValue), defaultValue);

        final URI expected = new URI("http://jakarta.apache.org");

        // URI string
        assertEquals(expected, conf.getURI("uri.string"));

        // URI object
        assertEquals(expected, conf.getURI("uri.object"));

        // interpolated value
        assertEquals(expected, conf.getURI("uri.string.interpolated"));
    }

    @Test
    public void testGetURIArray() throws Exception {
        // missing list
        final URI[] defaultValue = {new URI("http://www.apache.org"), new URI("http://jakarta.apache.org")};
        assertArrayEquals(conf.getURIArray("url.list", defaultValue), defaultValue);

        final URI[] expected = {new URI("http://jakarta.apache.org"), new URI("http://www.apache.org")};

        // list of strings
        assertArrayEquals(expected, conf.getURIArray("uri.list1"));

        // list of strings, comma separated
        assertArrayEquals(expected, conf.getURIArray("uri.list2"));

        // list of URI objects
        assertArrayEquals(expected, conf.getURIArray("uri.list3"));

        // array of URI objects
        assertArrayEquals(expected, conf.getURIArray("uri.list4"));

        // list of URI objects
        assertArrayEquals(expected, conf.getURIArray("uri.list6"));

        // list of interpolated values
        assertArrayEquals(expected, conf.getURIArray("uri.list.interpolated"));

        // single URI values
        assertArrayEquals(new URI[] {new URI("http://jakarta.apache.org")}, conf.getURIArray("uri.string"));
        assertArrayEquals(new URI[] {new URI("http://jakarta.apache.org")}, conf.getURIArray("uri.object"));

        // empty array
        assertArrayEquals(new URI[] {}, conf.getURIArray("empty"));
    }

    @Test
    public void testGetURIList() throws Exception {
        // missing list
        assertNull(conf.getURIList("uri.list", null));

        List<Object> expected = new ArrayList<>();
        expected.add(new URI("http://jakarta.apache.org"));
        expected.add(new URI("http://www.apache.org"));

        // list of strings
        assertEquals(expected, conf.getURIList("uri.list1"));

        // list of strings, comma separated
        assertEquals(expected, conf.getURIList("uri.list2"));

        // list of URI objects
        assertEquals(expected, conf.getURIList("uri.list3"));

        // array of URI objects
        assertEquals(expected, conf.getURIList("uri.list4"));

        // list of URI objects
        assertEquals(expected, conf.getURIList("uri.list6"));

        // list of interpolated values
        assertEquals(expected, conf.getURIList("uri.list.interpolated"));

        // single URI values
        expected = new ArrayList<>();
        expected.add(new URI("http://jakarta.apache.org"));
        assertEquals(expected, conf.getURIList("uri.string"));
        assertEquals(expected, conf.getURIList("uri.object"));

        // empty list
        assertEquals(new ArrayList<>(), conf.getURIList("empty"));
    }

    @Test
    public void testGetURL() throws Exception {
        // missing URL
        final URL defaultValue = new URL("http://www.google.com");
        assertEquals(conf.getURL("url", defaultValue), defaultValue);

        final URL expected = new URL("http://jakarta.apache.org");

        // URL string
        assertEquals(expected, conf.getURL("url.string"));

        // URL object
        assertEquals(expected, conf.getURL("url.object"));

        // interpolated value
        assertEquals(expected, conf.getURL("url.string.interpolated"));
    }

    @Test
    public void testGetURLArray() throws Exception {
        // missing list
        final URL[] defaultValue = {new URL("http://www.apache.org"), new URL("http://jakarta.apache.org")};
        assertArrayEquals(conf.getURLArray("url.list", defaultValue), defaultValue);

        final URL[] expected = {new URL("http://jakarta.apache.org"), new URL("http://www.apache.org")};

        // list of strings
        assertArrayEquals(expected, conf.getURLArray("url.list1"));

        // list of strings, comma separated
        assertArrayEquals(expected, conf.getURLArray("url.list2"));

        // list of URL objects
        assertArrayEquals(expected, conf.getURLArray("url.list3"));

        // array of URL objects
        assertArrayEquals(expected, conf.getURLArray("url.list4"));

        // list of URL objects
        assertArrayEquals(expected, conf.getURLArray("url.list6"));

        // list of interpolated values
        assertArrayEquals(expected, conf.getURLArray("url.list.interpolated"));

        // single URL values
        assertArrayEquals(new URL[] {new URL("http://jakarta.apache.org")}, conf.getURLArray("url.string"));
        assertArrayEquals(new URL[] {new URL("http://jakarta.apache.org")}, conf.getURLArray("url.object"));

        // empty array
        assertArrayEquals(new URL[] {}, conf.getURLArray("empty"));
    }

    @Test
    public void testGetURLList() throws Exception {
        // missing list
        assertNull(conf.getURLList("url.list", null));

        List<Object> expected = new ArrayList<>();
        expected.add(new URL("http://jakarta.apache.org"));
        expected.add(new URL("http://www.apache.org"));

        // list of strings
        assertEquals(expected, conf.getURLList("url.list1"));

        // list of strings, comma separated
        assertEquals(expected, conf.getURLList("url.list2"));

        // list of URL objects
        assertEquals(expected, conf.getURLList("url.list3"));

        // array of URL objects
        assertEquals(expected, conf.getURLList("url.list4"));

        // list of URL objects
        assertEquals(expected, conf.getURLList("url.list6"));

        // list of interpolated values
        assertEquals(expected, conf.getURLList("url.list.interpolated"));

        // single URL values
        expected = new ArrayList<>();
        expected.add(new URL("http://jakarta.apache.org"));
        assertEquals(expected, conf.getURLList("url.string"));
        assertEquals(expected, conf.getURLList("url.object"));

        // empty list
        assertEquals(new ArrayList<>(), conf.getURLList("empty"));
    }

    @Test
    public void testIsEmpty() {
        final Configuration baseconf = new BaseConfiguration();
        final DataConfiguration conf = new DataConfiguration(baseconf);

        assertTrue(conf.isEmpty());
        baseconf.setProperty("foo", "bar");
        assertFalse(conf.isEmpty());
    }

    @Test
    public void testNullConfiguration() {
        assertThrows(NullPointerException.class, () -> new DataConfiguration(null));
    }
}
