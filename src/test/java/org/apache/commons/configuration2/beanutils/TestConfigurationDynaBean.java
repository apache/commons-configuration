/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration2.beanutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import junitx.framework.ObjectAssert;

import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>Test Case for the <code>ConfigurationDynaBean</code> implementation class.
 * These tests were based on the ones in <code>BasicDynaBeanTestCase</code>
 * because the two classes provide similar levels of functionality.</p>
 *
 * @author <a href="mailto:ricardo.gladwell@btinternet.com">Ricardo Gladwell</a>
 */
public class TestConfigurationDynaBean
{
    /**
     * The basic test bean for each test.
     */
    private ConfigurationDynaBean bean;

    /**
     * The set of property names we expect to have returned when calling
     * <code>getDynaProperties()</code>.  You should update this list
     * when new properties are added to TestBean.
     */
    String[] properties = {
            "booleanProperty",
            "booleanSecond",
            "doubleProperty",
            "floatProperty",
            "intProperty",
            "longProperty",
            "mappedProperty.key1",
            "mappedProperty.key2",
            "mappedProperty.key3",
            "mappedIntProperty.key1",
            "shortProperty",
            "stringProperty",
            "byteProperty",
            "charProperty"
    };

    Object[] values = {
            Boolean.TRUE,
            Boolean.TRUE,
            new Double(Double.MAX_VALUE),
            new Float(Float.MAX_VALUE),
            new Integer(Integer.MAX_VALUE),
            new Long(Long.MAX_VALUE),
            "First Value",
            "Second Value",
            "Third Value",
            new Integer(Integer.MAX_VALUE),
            new Short(Short.MAX_VALUE),
            "This is a string",
            new Byte(Byte.MAX_VALUE),
            new Character(Character.MAX_VALUE)
    };

    int[] intArray = {0, 10, 20, 30, 40};
    boolean[] booleanArray = {true, false, true, false, true};
    char[] charArray = {'a', 'b', 'c', 'd', 'e'};
    byte[] byteArray = {0, 10, 20, 30, 40};
    long[] longArray = {0, 10, 20, 30, 40};
    short[] shortArray = {0, 10, 20, 30, 40};
    float[] floatArray = {0, 10, 20, 30, 40};
    double[] doubleArray = {0.0, 10.0, 20.0, 30.0, 40.0};
    String[] stringArray = {"String 0", "String 1", "String 2", "String 3", "String 4"};


    /**
     * Set up instance variables required by this test case.
     */
    @Before
    public void setUp() throws Exception
    {
        final Configuration configuration = createConfiguration();

        for (int i = 0; i < properties.length; i++)
        {
            configuration.setProperty(properties[i], values[i]);
        }

        for (final int element : intArray) {
            configuration.addProperty("intIndexed", new Integer(element));
        }

        for (final String element : stringArray) {
            configuration.addProperty("stringIndexed", element);
        }

        final List<String> list = Arrays.asList(stringArray);
        configuration.addProperty("listIndexed", list);

        bean = new ConfigurationDynaBean(configuration);

        bean.set("listIndexed", list);
        bean.set("intArray", intArray);
        bean.set("booleanArray", booleanArray);
        bean.set("charArray", charArray);
        bean.set("longArray", longArray);
        bean.set("shortArray", shortArray);
        bean.set("floatArray", floatArray);
        bean.set("doubleArray", doubleArray);
        bean.set("byteArray", byteArray);
        bean.set("stringArray", stringArray);
    }

    /**
     * Creates the underlying configuration object for the dyna bean.
     * @return the underlying configuration object
     */
    protected Configuration createConfiguration()
    {
        return new BaseConfiguration();
    }

    /**
     * Corner cases on getDynaProperty invalid arguments.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetDescriptorArguments()
    {
        final DynaProperty descriptor = bean.getDynaClass().getDynaProperty("unknown");
        assertNull("Unknown property descriptor should be null", descriptor);
        bean.getDynaClass().getDynaProperty(null);
    }

    /**
     * Positive getDynaProperty on property <code>booleanProperty</code>.
     */
    @Test
    public void testGetDescriptorBoolean()
    {
        testGetDescriptorBase("booleanProperty", Boolean.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>doubleProperty</code>.
     */
    @Test
    public void testGetDescriptorDouble()
    {
        testGetDescriptorBase("doubleProperty", Double.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>floatProperty</code>.
     */
    @Test
    public void testGetDescriptorFloat()
    {
        testGetDescriptorBase("floatProperty", Float.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>intProperty</code>.
     */
    @Test
    public void testGetDescriptorInt()
    {
        testGetDescriptorBase("intProperty", Integer.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>longProperty</code>.
     */
    @Test
    public void testGetDescriptorLong()
    {
        testGetDescriptorBase("longProperty", Long.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>booleanSecond</code>
     * that uses an "is" method as the getter.
     */
    @Test
    public void testGetDescriptorSecond()
    {
        testGetDescriptorBase("booleanSecond", Boolean.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>shortProperty</code>.
     */
    @Test
    public void testGetDescriptorShort()
    {
        testGetDescriptorBase("shortProperty", Short.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>stringProperty</code>.
     */
    @Test
    public void testGetDescriptorString()
    {
        testGetDescriptorBase("stringProperty", String.class);
    }

    /**
     * Positive test for getDynaPropertys().  Each property name
     * listed in <code>properties</code> should be returned exactly once.
     */
    @Test
    public void testGetDescriptors()
    {
        final DynaProperty pd[] = bean.getDynaClass().getDynaProperties();
        assertNotNull("Got descriptors", pd);
        final int count[] = new int[properties.length];
        for (final DynaProperty element : pd) {
            final String name = element.getName();
            for (int j = 0; j < properties.length; j++)
            {
                if (name.equals(properties[j]))
                {
                    count[j]++;
                }
            }
        }

        for (int j = 0; j < properties.length; j++)
        {
            if (count[j] < 0)
            {
                fail("Missing property " + properties[j]);
            }
            else if (count[j] > 1)
            {
                fail("Duplicate property " + properties[j]);
            }
        }
    }

    /**
     * Corner cases on getIndexedProperty invalid arguments.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetIndexedArguments()
    {
        bean.get("intArray", -1);
    }

    /**
     * Positive and negative tests on getIndexedProperty valid arguments.
     */
    @Test
    public void testGetIndexedValues()
    {
        for (int i = 0; i < 5; i++)
        {
            Object value = bean.get("intArray", i);

            assertNotNull("intArray index " + i + " did not return value.", value);
            ObjectAssert.assertInstanceOf("intArray index " + i, Integer.class, value);
            assertEquals("intArray " + i + " returned incorrect value.", i * 10, ((Integer) value).intValue());

            value = bean.get("intIndexed", i);

            assertNotNull("intIndexed index " + i + "returned value " + i, value);
            ObjectAssert.assertInstanceOf("intIndexed index " + i, Integer.class, value);
            assertEquals("intIndexed index " + i + "returned correct " + i, i * 10, ((Integer) value).intValue());

            value = bean.get("listIndexed", i);

            assertNotNull("listIndexed index " + i + "returned value " + i, value);
            ObjectAssert.assertInstanceOf("list index " + i, String.class, value);
            assertEquals("listIndexed index " + i + "returned correct " + i, "String " + i, value);

            value = bean.get("stringArray", i);

            assertNotNull("stringArray index " + i + " returnde null.", value);
            assertFalse("stringArray index " + i + " returned array instead of String.", value.getClass().isArray());
            ObjectAssert.assertInstanceOf("stringArray index " + i, String.class, value);
            assertEquals("stringArray returned correct " + i, "String " + i, value);

            value = bean.get("stringIndexed", i);

            assertNotNull("stringIndexed returned value " + i, value);
            ObjectAssert.assertInstanceOf("stringIndexed", String.class, value);
            assertEquals("stringIndexed returned correct " + i, "String " + i, value);
        }
    }

    /**
     * Corner cases on getMappedProperty invalid arguments.
     */
    @Test
    public void testGetMappedArguments()
    {
        try
        {
            final Object value = bean.get("mappedProperty", "unknown");
            assertNull("Should not return a value", value);
        }
        catch (final Throwable t)
        {
            fail("Threw " + t + " instead of returning null");
        }
    }

    /**
     * Positive and negative tests on getMappedProperty valid arguments.
     */
    @Test
    public void testGetMappedValues()
    {
        Object value = bean.get("mappedProperty", "key1");
        assertEquals("Can find first value", "First Value", value);

        value = bean.get("mappedProperty", "key2");
        assertEquals("Can find second value", "Second Value", value);

        value = bean.get("mappedProperty", "key3");
        assertNotNull("Cannot find third value", value);
    }

    /**
     * Corner cases on getSimpleProperty invalid arguments.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetSimpleArguments()
    {
        bean.get("a non existing property");
    }

    /**
     * Test getSimpleProperty on a boolean property.
     */
    @Test
    public void testGetSimpleBoolean()
    {
        final Object value = bean.get("booleanProperty");
        assertNotNull("Got a value", value);
        ObjectAssert.assertInstanceOf("Got correct type", Boolean.class, value);
        assertTrue("Got correct value", ((Boolean) value).booleanValue());
    }

    /**
     * Test getSimpleProperty on a double property.
     */
    @Test
    public void testGetSimpleDouble()
    {
        final Object value = bean.get("doubleProperty");
        assertNotNull("Got a value", value);
        ObjectAssert.assertInstanceOf("Got correct type", Double.class, value);
        assertEquals("Got correct value", ((Double) value).doubleValue(), Double.MAX_VALUE, 0.005);
    }

    /**
     * Test getSimpleProperty on a float property.
     */
    @Test
    public void testGetSimpleFloat()
    {
        final Object value = bean.get("floatProperty");
        assertNotNull("Got a value", value);
        ObjectAssert.assertInstanceOf("Got correct type", Float.class, value);
        assertEquals("Got correct value", ((Float) value).floatValue(), Float.MAX_VALUE, 0.005f);
    }

    /**
     * Test getSimpleProperty on a int property.
     */
    @Test
    public void testGetSimpleInt()
    {
        final Object value = bean.get("intProperty");
        assertNotNull("Failed to get value", value);
        ObjectAssert.assertInstanceOf("Incorrect type", Integer.class, value);
        assertEquals("Incorrect value", ((Integer) value).intValue(), Integer.MAX_VALUE);
    }

    /**
     * Test getSimpleProperty on a long property.
     */
    @Test
    public void testGetSimpleLong()
    {
        final Object value = bean.get("longProperty");
        assertNotNull("Got a value", value);
        ObjectAssert.assertInstanceOf("Returned incorrect type", Long.class, value);
        assertEquals("Returned value of Incorrect value", ((Long) value).longValue(), Long.MAX_VALUE);
    }

    /**
     * Test getSimpleProperty on a short property.
     */
    @Test
    public void testGetSimpleShort()
    {
        final Object value = bean.get("shortProperty");
        assertNotNull("Got a value", value);
        ObjectAssert.assertInstanceOf("Got correct type", Short.class, value);
        assertEquals("Got correct value", ((Short) value).shortValue(), Short.MAX_VALUE);
    }

    /**
     * Test getSimpleProperty on a String property.
     */
    @Test
    public void testGetSimpleString()
    {
        final Object value = bean.get("stringProperty");
        assertNotNull("Got a value", value);
        ObjectAssert.assertInstanceOf("Got correct type", String.class, value);
        assertEquals("Got correct value", value, "This is a string");
    }

    /**
     * Test <code>contains()</code> method for mapped properties.
     */
    @Test
    public void testMappedContains()
    {
        assertTrue("Can't see first key", bean.contains("mappedProperty", "key1"));
        assertTrue("Can see unknown key", !bean.contains("mappedProperty", "Unknown Key"));
    }

    /**
     * Test <code>remove()</code> method for mapped properties.
     */
    @Test
    public void testMappedRemove()
    {
        assertTrue("Can see first key", bean.contains("mappedProperty", "key1"));
        bean.remove("mappedProperty", "key1");
        assertTrue("Can not see first key", !bean.contains("mappedProperty", "key1"));

        assertTrue("Can not see unknown key", !bean.contains("mappedProperty", "key4"));
        bean.remove("mappedProperty", "key4");
        assertTrue("Can not see unknown key", !bean.contains("mappedProperty", "key4"));
    }

    /**
     * Corner cases on setIndexedProperty invalid arguments.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetIndexedArguments()
    {
        bean.set("intArray", -1, new Integer(0));
    }

    /**
     * Positive and negative tests on setIndexedProperty valid arguments.
     */
    @Test
    public void testSetIndexedValues()
    {
        bean.set("intArray", 0, new Integer(1));
        Object value = bean.get("intArray", 0);

        assertNotNull("Returned new value 0", value);
        ObjectAssert.assertInstanceOf("Returned Integer new value 0", Integer.class,  value);
        assertEquals("Returned correct new value 0", 1, ((Integer) value).intValue());


        bean.set("intIndexed", 1, new Integer(11));
        value = bean.get("intIndexed", 1);

        assertNotNull("Returned new value 1", value);
        ObjectAssert.assertInstanceOf("Returned Integer new value 1", Integer.class,  value);
        assertEquals("Returned correct new value 1", 11, ((Integer) value).intValue());


        bean.set("listIndexed", 2, "New Value 2");
        value = bean.get("listIndexed", 2);

        assertNotNull("Returned new value 2", value);
        ObjectAssert.assertInstanceOf("Returned String new value 2", String.class,  value);
        assertEquals("Returned correct new value 2", "New Value 2", value);


        bean.set("stringArray", 3, "New Value 3");
        value = bean.get("stringArray", 3);

        assertNotNull("Returned new value 3", value);
        ObjectAssert.assertInstanceOf("Returned String new value 3", String.class,  value);
        assertEquals("Returned correct new value 3", "New Value 3", value);


        bean.set("stringIndexed", 4, "New Value 4");
        value = bean.get("stringIndexed", 4);

        assertNotNull("Returned new value 4", value);
        ObjectAssert.assertInstanceOf("Returned String new value 4", String.class,  value);
        assertEquals("Returned correct new value 4", "New Value 4", value);
    }

    /**
     * Test the modification of a configuration property stored internally as an array.
     */
    @Test
    public void testSetArrayValue()
    {
        final MapConfiguration configuration = new MapConfiguration(new HashMap<String, Object>());
        configuration.getMap().put("objectArray", new Object[] {"value1", "value2", "value3"});

        final ConfigurationDynaBean bean = new ConfigurationDynaBean(configuration);

        bean.set("objectArray", 1, "New Value 1");
        final Object value = bean.get("objectArray", 1);

        assertNotNull("Returned new value 1", value);
        ObjectAssert.assertInstanceOf("Returned String new value 1", String.class,  value);
        assertEquals("Returned correct new value 1", "New Value 1", value);
    }

    /**
     * Positive and negative tests on setMappedProperty valid arguments.
     */
    @Test
    public void testSetMappedValues()
    {
        bean.set("mappedProperty", "First Key", "New First Value");
        assertEquals("Can replace old value", "New First Value", bean.get("mappedProperty", "First Key"));

        bean.set("mappedProperty", "Fourth Key", "Fourth Value");
        assertEquals("Can set new value", "Fourth Value", bean.get("mappedProperty", "Fourth Key"));
    }

    /**
     * Test setSimpleProperty on a boolean property.
     */
    @Test
    public void testSetSimpleBoolean()
    {
        final boolean oldValue = ((Boolean) bean.get("booleanProperty")).booleanValue();
        final boolean newValue = !oldValue;
        bean.set("booleanProperty", new Boolean(newValue));
        assertTrue("Matched new value", newValue == ((Boolean) bean.get("booleanProperty")).booleanValue());
    }

    /**
     * Test setSimpleProperty on a double property.
     */
    @Test
    public void testSetSimpleDouble()
    {
        final double oldValue = ((Double) bean.get("doubleProperty")).doubleValue();
        final double newValue = oldValue + 1.0;
        bean.set("doubleProperty", new Double(newValue));
        assertEquals("Matched new value", newValue, ((Double) bean.get("doubleProperty")).doubleValue(), 0.005);
    }

    /**
     * Test setSimpleProperty on a float property.
     */
    @Test
    public void testSetSimpleFloat()
    {
        final float oldValue = ((Float) bean.get("floatProperty")).floatValue();
        final float newValue = oldValue + (float) 1.0;
        bean.set("floatProperty", new Float(newValue));
        assertEquals("Matched new value", newValue, ((Float) bean.get("floatProperty")).floatValue(), 0.005f);
    }

    /**
     * Test setSimpleProperty on a int property.
     */
    @Test
    public void testSetSimpleInt()
    {
        final int oldValue = ((Integer) bean.get("intProperty")).intValue();
        final int newValue = oldValue + 1;
        bean.set("intProperty", new Integer(newValue));
        assertEquals("Matched new value", newValue, ((Integer) bean.get("intProperty")).intValue());
    }

    /**
     * Test setSimpleProperty on a long property.
     */
    @Test
    public void testSetSimpleLong()
    {
        final long oldValue = ((Long) bean.get("longProperty")).longValue();
        final long newValue = oldValue + 1;
        bean.set("longProperty", new Long(newValue));
        assertEquals("Matched new value", newValue, ((Long) bean.get("longProperty")).longValue());
    }

    /**
     * Test setSimpleProperty on a short property.
     */
    @Test
    public void testSetSimpleShort()
    {
        final short oldValue = ((Short) bean.get("shortProperty")).shortValue();
        final short newValue = (short) (oldValue + 1);
        bean.set("shortProperty", new Short(newValue));
        assertEquals("Matched new value", newValue, ((Short) bean.get("shortProperty")).shortValue());
    }

    /**
     * Test setSimpleProperty on a String property.
     */
    @Test
    public void testSetSimpleString()
    {
        final String oldValue = (String) bean.get("stringProperty");
        final String newValue = oldValue + " Extra Value";
        bean.set("stringProperty", newValue);
        assertEquals("Matched new value", newValue, bean.get("stringProperty"));
    }

    /**
     * Tests set on a null value: should throw NPE.
     */
    @Test(expected = NullPointerException.class)
    public void testAddNullPropertyValue()
    {
        bean.set("nullProperty", null);
    }

    /**
     * Test the retrieval of a non-existent property.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetNonExistentProperty()
    {
        bean.get("nonexistProperty");
    }

    /**
     * Base for testGetDescriptorXxxxx() series of tests.
     *
     * @param name Name of the property to be retrieved
     * @param type Expected class type of this property
     */
    protected void testGetDescriptorBase(final String name, final Class<?> type)
    {
        final DynaProperty descriptor = bean.getDynaClass().getDynaProperty(name);

        assertNotNull("Failed to get descriptor", descriptor);
        assertEquals("Got incorrect type", type, descriptor.getType());
    }

    /**
     * Tests whether nested properties can be accessed.
     */
    @Test
    public void testNestedPropeties()
    {
        final ConfigurationDynaBean nested = (ConfigurationDynaBean) bean.get("mappedProperty");

        final String value = (String) nested.get("key1");
        assertEquals("Can find first value", "First Value", value);

        nested.set("key1", "undefined");
        assertEquals("Incorrect value returned", "undefined", bean.get("mappedProperty.key1"));
    }

    /**
     * Tests if reading a non-indexed property using the index
     * get method throws an IllegalArgumentException as it
     * should.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetNonIndexedProperties()
    {
        bean.get("booleanProperty", 0);
    }

    /**
     * Tests whether accessing a non-indexed string property using the index get
     * method causes an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetIndexedString()
    {
        bean.set("stringProp", "value");
        bean.get("stringProp", 0);
    }

    /**
     * Tests whether an indexed access to a non-existing property causes an
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetIndexedNonExisting()
    {
        bean.get("Non existing property", 0);
    }

    /**
     * Tests if writing a non-indexed property using the index
     * set method with an index &gt; 0 throws an IllegalArgumentException as it
     * should.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetNonIndexedProperties()
    {
        bean.set("booleanProperty", 1, Boolean.TRUE);
    }
}
