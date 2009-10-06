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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.flat.BaseConfiguration;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import junitx.framework.ObjectAssert;

/**
 * <p>Test Case for the <code>ConfigurationDynaBean</code> implementation class.
 * These tests were based on the ones in <code>BasicDynaBeanTestCase</code>
 * because the two classes provide similar levels of functionality.</p>
 *
 * @author <a href="mailto:ricardo.gladwell@btinternet.com">Ricardo Gladwell</a>
 * @version $Revision$
 */
public class TestConfigurationDynaBean extends TestCase
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
    private String[] properties = {
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

    private Object[] values = {
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

    private int[] intArray = {0, 10, 20, 30, 40};
    private boolean[] booleanArray = {true, false, true, false, true};
    private char[] charArray = {'a', 'b', 'c', 'd', 'e'};
    private byte[] byteArray = {0, 10, 20, 30, 40};
    private long[] longArray = {0, 10, 20, 30, 40};
    private short[] shortArray = {0, 10, 20, 30, 40};
    private float[] floatArray = {0, 10, 20, 30, 40};
    private double[] doubleArray = {0.0, 10.0, 20.0, 30.0, 40.0};
    private String[] stringArray = {"String 0", "String 1", "String 2", "String 3", "String 4"};


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception
    {
        Configuration configuration = createConfiguration();

        for (int i = 0; i < properties.length; i++)
        {
            configuration.setProperty(properties[i], values[i]);
        }

        for (int a = 0; a < intArray.length; a++)
        {
            configuration.addProperty("intIndexed", new Integer(intArray[a]));
        }

        for (int a = 0; a < stringArray.length; a++)
        {
            configuration.addProperty("stringIndexed", stringArray[a]);
        }

        List list = new ArrayList();
        for (int i = 0; i < stringArray.length; i++)
        {
            list.add(stringArray[i]);
        }
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
    public void testGetDescriptorArguments()
    {
        DynaProperty descriptor = bean.getDynaClass().getDynaProperty("unknown");
        assertNull("Unknown property descriptor should be null", descriptor);

        try
        {
            bean.getDynaClass().getDynaProperty(null);
            fail("Should throw IllegalArgumentException");
        }
        catch (java.lang.IllegalArgumentException e)
        {
            // Expected response
        }
        catch (AssertionFailedError e)
        {
            // ignore other failed responses
        }
        catch (Throwable t)
        {
            fail("Threw '" + t + "' instead of 'IllegalArgumentException'");
        }
    }

    /**
     * Positive getDynaProperty on property <code>booleanProperty</code>.
     */
    public void testGetDescriptorBoolean()
    {
        testGetDescriptorBase("booleanProperty", Boolean.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>doubleProperty</code>.
     */
    public void testGetDescriptorDouble()
    {
        testGetDescriptorBase("doubleProperty", Double.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>floatProperty</code>.
     */
    public void testGetDescriptorFloat()
    {
        testGetDescriptorBase("floatProperty", Float.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>intProperty</code>.
     */
    public void testGetDescriptorInt()
    {
        testGetDescriptorBase("intProperty", Integer.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>longProperty</code>.
     */
    public void testGetDescriptorLong()
    {
        testGetDescriptorBase("longProperty", Long.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>booleanSecond</code>
     * that uses an "is" method as the getter.
     */
    public void testGetDescriptorSecond()
    {
        testGetDescriptorBase("booleanSecond", Boolean.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>shortProperty</code>.
     */
    public void testGetDescriptorShort()
    {
        testGetDescriptorBase("shortProperty", Short.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>stringProperty</code>.
     */
    public void testGetDescriptorString()
    {
        testGetDescriptorBase("stringProperty", String.class);
    }

    /**
     * Positive test for getDynaPropertys().  Each property name
     * listed in <code>properties</code> should be returned exactly once.
     */
    public void testGetDescriptors()
    {
        DynaProperty pd[] = bean.getDynaClass().getDynaProperties();
        assertNotNull("Got descriptors", pd);
        int count[] = new int[properties.length];
        for (int i = 0; i < pd.length; i++)
        {
            String name = pd[i].getName();
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
    public void testGetIndexedArguments()
    {
        try
        {
            bean.get("intArray", -1);
        }
        catch (IndexOutOfBoundsException e)
        {
            return; // Expected response
        }
        catch (Throwable t)
        {
            fail("Threw '" + t + "' instead of 'IndexOutOfBoundsException'");
            return;
        }

        fail("Should throw IndexOutOfBoundsException");
    }

    /**
     * Positive and negative tests on getIndexedProperty valid arguments.
     */
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
            assertEquals("listIndexed index " + i + "returned correct " + i, "String " + i, (String) value);

            value = bean.get("stringArray", i);

            assertNotNull("stringArray index " + i + " returnde null.", value);
            assertFalse("stringArray index " + i + " returned array instead of String.", value.getClass().isArray());
            ObjectAssert.assertInstanceOf("stringArray index " + i, String.class, value);
            assertEquals("stringArray returned correct " + i, "String " + i, (String) value);

            value = bean.get("stringIndexed", i);

            assertNotNull("stringIndexed returned value " + i, value);
            ObjectAssert.assertInstanceOf("stringIndexed", String.class, value);
            assertEquals("stringIndexed returned correct " + i, "String " + i, (String) value);
        }
    }

    /**
     * Corner cases on getMappedProperty invalid arguments.
     */
    public void testGetMappedArguments()
    {
        try
        {
            Object value = bean.get("mappedProperty", "unknown");
            assertNull("Should not return a value", value);
        }
        catch (Throwable t)
        {
            fail("Threw " + t + " instead of returning null");
        }
    }

    /**
     * Positive and negative tests on getMappedProperty valid arguments.
     */
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
    public void testGetSimpleArguments()
    {
        try
        {
            bean.get("a non existing property");
        }
        catch (IllegalArgumentException e)
        {
            return; // Expected response
        }
        catch (Throwable t)
        {
            fail("Threw " + t + " instead of IllegalArgumentException");
        }
        fail("Should throw IllegalArgumentException");
    }

    /**
     * Test getSimpleProperty on a boolean property.
     */
    public void testGetSimpleBoolean()
    {
        Object value = bean.get("booleanProperty");
        assertNotNull("Got a value", value);
        ObjectAssert.assertInstanceOf("Got correct type", Boolean.class, value);
        assertTrue("Got correct value", ((Boolean) value).booleanValue());
    }

    /**
     * Test getSimpleProperty on a double property.
     */
    public void testGetSimpleDouble()
    {
        Object value = bean.get("doubleProperty");
        assertNotNull("Got a value", value);
        ObjectAssert.assertInstanceOf("Got correct type", Double.class, value);
        assertEquals("Got correct value", ((Double) value).doubleValue(), Double.MAX_VALUE, 0.005);
    }

    /**
     * Test getSimpleProperty on a float property.
     */
    public void testGetSimpleFloat()
    {
        Object value = bean.get("floatProperty");
        assertNotNull("Got a value", value);
        ObjectAssert.assertInstanceOf("Got correct type", Float.class, value);
        assertEquals("Got correct value", ((Float) value).floatValue(), Float.MAX_VALUE, 0.005f);
    }

    /**
     * Test getSimpleProperty on a int property.
     */
    public void testGetSimpleInt()
    {
        Object value = bean.get("intProperty");
        assertNotNull("Failed to get value", value);
        ObjectAssert.assertInstanceOf("Incorrect type", Integer.class, value);
        assertEquals("Incorrect value", ((Integer) value).intValue(), Integer.MAX_VALUE);
    }

    /**
     * Test getSimpleProperty on a long property.
     */
    public void testGetSimpleLong()
    {
        Object value = bean.get("longProperty");
        assertNotNull("Got a value", value);
        ObjectAssert.assertInstanceOf("Returned incorrect type", Long.class, value);
        assertEquals("Returned value of Incorrect value", ((Long) value).longValue(), Long.MAX_VALUE);
    }

    /**
     * Test getSimpleProperty on a short property.
     */
    public void testGetSimpleShort()
    {
        Object value = bean.get("shortProperty");
        assertNotNull("Got a value", value);
        ObjectAssert.assertInstanceOf("Got correct type", Short.class, value);
        assertEquals("Got correct value", ((Short) value).shortValue(), Short.MAX_VALUE);
    }

    /**
     * Test getSimpleProperty on a String property.
     */
    public void testGetSimpleString()
    {
        Object value = bean.get("stringProperty");
        assertNotNull("Got a value", value);
        ObjectAssert.assertInstanceOf("Got correct type", String.class, value);
        assertEquals("Got correct value", (String) value, "This is a string");
    }

    /**
     * Test <code>contains()</code> method for mapped properties.
     */
    public void testMappedContains()
    {
        assertTrue("Can't see first key", bean.contains("mappedProperty", "key1"));
        assertTrue("Can see unknown key", !bean.contains("mappedProperty", "Unknown Key"));
    }

    /**
     * Test <code>remove()</code> method for mapped properties.
     */
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
    public void testSetIndexedArguments()
    {
        try
        {
            bean.set("intArray", -1, new Integer(0));
        }
        catch (IndexOutOfBoundsException e)
        {
            return; // Expected response
        }
        catch (Throwable t)
        {
            fail("Threw " + t + " instead of IndexOutOfBoundsException");
        }

        fail("Should throw IndexOutOfBoundsException");
    }

    /**
     * Positive and negative tests on setIndexedProperty valid arguments.
     */
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
        assertEquals("Returned correct new value 2", "New Value 2", (String) value);


        bean.set("stringArray", 3, "New Value 3");
        value = bean.get("stringArray", 3);

        assertNotNull("Returned new value 3", value);
        ObjectAssert.assertInstanceOf("Returned String new value 3", String.class,  value);
        assertEquals("Returned correct new value 3", "New Value 3", (String) value);


        bean.set("stringIndexed", 4, "New Value 4");
        value = bean.get("stringIndexed", 4);

        assertNotNull("Returned new value 4", value);
        ObjectAssert.assertInstanceOf("Returned String new value 4", String.class,  value);
        assertEquals("Returned correct new value 4", "New Value 4", (String) value);
    }

    /**
     * Test the modification of a configuration property stored internally as an array.
     */
    public void testSetArrayValue()
    {
        MapConfiguration configuration = new MapConfiguration(new HashMap());
        configuration.getMap().put("objectArray", new Object[] {"value1", "value2", "value3"});

        ConfigurationDynaBean bean = new ConfigurationDynaBean(configuration);

        bean.set("objectArray", 1, "New Value 1");
        Object value = bean.get("objectArray", 1);

        assertNotNull("Returned new value 1", value);
        ObjectAssert.assertInstanceOf("Returned String new value 1", String.class,  value);
        assertEquals("Returned correct new value 1", "New Value 1", (String) value);
    }

    /**
     * Positive and negative tests on setMappedProperty valid arguments.
     */
    public void testSetMappedValues()
    {
        bean.set("mappedProperty", "First Key", "New First Value");
        assertEquals("Can replace old value", "New First Value", (String) bean.get("mappedProperty", "First Key"));

        bean.set("mappedProperty", "Fourth Key", "Fourth Value");
        assertEquals("Can set new value", "Fourth Value", (String) bean.get("mappedProperty", "Fourth Key"));
    }

    /**
     * Test setSimpleProperty on a boolean property.
     */
    public void testSetSimpleBoolean()
    {
        boolean oldValue = ((Boolean) bean.get("booleanProperty")).booleanValue();
        boolean newValue = !oldValue;
        bean.set("booleanProperty", new Boolean(newValue));
        assertTrue("Matched new value", newValue == ((Boolean) bean.get("booleanProperty")).booleanValue());
    }

    /**
     * Test setSimpleProperty on a double property.
     */
    public void testSetSimpleDouble()
    {
        double oldValue = ((Double) bean.get("doubleProperty")).doubleValue();
        double newValue = oldValue + 1.0;
        bean.set("doubleProperty", new Double(newValue));
        assertEquals("Matched new value", newValue, ((Double) bean.get("doubleProperty")).doubleValue(), 0.005);
    }

    /**
     * Test setSimpleProperty on a float property.
     */
    public void testSetSimpleFloat()
    {
        float oldValue = ((Float) bean.get("floatProperty")).floatValue();
        float newValue = oldValue + (float) 1.0;
        bean.set("floatProperty", new Float(newValue));
        assertEquals("Matched new value", newValue, ((Float) bean.get("floatProperty")).floatValue(), 0.005f);
    }

    /**
     * Test setSimpleProperty on a int property.
     */
    public void testSetSimpleInt()
    {
        int oldValue = ((Integer) bean.get("intProperty")).intValue();
        int newValue = oldValue + 1;
        bean.set("intProperty", new Integer(newValue));
        assertEquals("Matched new value", newValue, ((Integer) bean.get("intProperty")).intValue());
    }

    /**
     * Test setSimpleProperty on a long property.
     */
    public void testSetSimpleLong()
    {
        long oldValue = ((Long) bean.get("longProperty")).longValue();
        long newValue = oldValue + 1;
        bean.set("longProperty", new Long(newValue));
        assertEquals("Matched new value", newValue, ((Long) bean.get("longProperty")).longValue());
    }

    /**
     * Test setSimpleProperty on a short property.
     */
    public void testSetSimpleShort()
    {
        short oldValue = ((Short) bean.get("shortProperty")).shortValue();
        short newValue = (short) (oldValue + 1);
        bean.set("shortProperty", new Short(newValue));
        assertEquals("Matched new value", newValue, ((Short) bean.get("shortProperty")).shortValue());
    }

    /**
     * Test setSimpleProperty on a String property.
     */
    public void testSetSimpleString()
    {
        String oldValue = (String) bean.get("stringProperty");
        String newValue = oldValue + " Extra Value";
        bean.set("stringProperty", newValue);
        assertEquals("Matched new value", newValue, (String) bean.get("stringProperty"));
    }

    /**
     * Tests set on a null value: should throw NPE.
     */
    public void testAddNullPropertyValue()
    {
        try
        {
            bean.set("nullProperty", null);
        }
        catch (NullPointerException e)
        {
            return;
        }
        catch (Throwable t)
        {
            fail("Threw " + t + " instead of NullPointerException");
            return;
        }
        fail("Should have thrown NullPointerException");
    }

    /**
     * Test the retrieval of a non-existent property.
     */
    public void testGetNonExistentProperty()
    {
        try
        {
            bean.get("nonexistProperty");
        }
        catch (IllegalArgumentException e)
        {
            return;
        }
        catch (Exception e)
        {
            fail("Threw '" + e + "' instead of java.lang.IllegalArgumentException");
        }

        fail("Get non-existent property failed to throw java.lang.IllegalArgumentException");
    }

    /**
     * Base for testGetDescriptorXxxxx() series of tests.
     *
     * @param name Name of the property to be retrieved
     * @param type Expected class type of this property
     */
    protected void testGetDescriptorBase(String name, Class type)
    {
        DynaProperty descriptor = bean.getDynaClass().getDynaProperty(name);

        assertNotNull("Failed to get descriptor", descriptor);
        assertEquals("Got incorrect type", type, descriptor.getType());
    }

    /**
     * Tests if accessing a non-indexed property using the index
     * get method throws an IllegalArgumentException as it
     * should.
     */
    public void testNonIndexedPropeties()
    {
        ConfigurationDynaBean nested = (ConfigurationDynaBean) bean.get("mappedProperty");

        String value = (String) nested.get("key1");
        assertEquals("Can find first value", "First Value", value);

        nested.set("key1", "undefined");
        assertEquals("Incorrect value returned", "undefined", bean.get("mappedProperty.key1"));
    }

    /**
     * Tests if accessing a non-indexed property using the index
     * get method throws an IllegalArgumentException as it
     * should.
     */
    public void testNestedPropeties()
    {
        try
        {
            bean.get("booleanProperty", 0);
        }
        catch (IllegalArgumentException e)
        {
            return;
        }
        catch (Throwable t)
        {
            fail("Threw " + t + " instead of IllegalArgumentException");
            return;
        }

        fail("Should have thrown IllegalArgumentException");

        try
        {
            bean.set("booleanProperty", 0, Boolean.TRUE);
        }
        catch (IllegalArgumentException e)
        {
            return;
        }
        catch (Throwable t)
        {
            fail("Threw " + t + " instead of IllegalArgumentException");
            return;
        }

        fail("Should have thrown IllegalArgumentException");
    }


}
