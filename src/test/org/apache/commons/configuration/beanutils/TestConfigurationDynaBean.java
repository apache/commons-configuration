/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.apache.commons.configuration.beanutils;

import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.configuration.BaseConfiguration;

/**
 * <p>Test Case for the <code>ConfigurationDynaBean</code> implementation class.
 * These tests were based on the ones in <code>BasicDynaBeanTestCase</code>
 * because the two classes provide similar levels of functionality.</p>
 *
 * @author <a href="mailto:ricardo.gladwell@btinternet.com">Ricardo Gladwell</a>
 * @version $Revision: 1.2 $
 */
public class TestConfigurationDynaBean extends TestCase {

    /**
     * The basic test bean for each test.
     */
    protected ConfigurationDynaBean bean = null;

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
            "stringProperty"
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
            "This is a string"
    };

    int[] intArray = { 0, 10, 20, 30, 40 };
    String[] stringArray = { "String 0", "String 1", "String 2", "String 3", "String 4" };

    /**
     * Construct a new instance of this test case.
     * @param name Name of the test case
     */
    public TestConfigurationDynaBean(String name) {
        super(name);
    }

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        BaseConfiguration configuration = new BaseConfiguration();

        for(int i = 0; i < properties.length ; i++)
            configuration.setProperty(properties[i], values[i]);

        for(int a = 0; a < intArray.length ; a++)
            configuration.addProperty("intIndexed",new Integer(intArray[a]));

        for(int a = 0; a < stringArray.length ; a++)
            configuration.addProperty("stringIndexed",stringArray[a]);

        List list = new ArrayList();
        for(int i = 0 ; i < stringArray.length ; i++)
            list.add(stringArray[i]);
        configuration.addProperty("listIndexed", list);

        bean = new ConfigurationDynaBean(configuration);

        bean.set("intArray", intArray);
        bean.set("stringArray", stringArray);
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        bean = null;
    }

    /**
     * Corner cases on getDynaProperty invalid arguments.
     */
    public void testGetDescriptorArguments() {

        try {
            DynaProperty descriptor =
                    bean.getDynaClass().getDynaProperty("unknown");
            assertNull("Unknown property descriptor should be null",
                    descriptor);
        } catch (Throwable t) {
            fail("Threw " + t + " instead of returning null");
        }

        try {
            bean.getDynaClass().getDynaProperty(null);
            fail("Should throw IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException e) {
            ; // Expected response
        } catch(AssertionFailedError e) {
            ; // ignore other failed responses
        } catch(Throwable t) {
            fail("Threw '" + t + "' instead of 'IllegalArgumentException'");
        }
    }

    /**
     * Positive getDynaProperty on property <code>booleanProperty</code>.
     */
    public void testGetDescriptorBoolean() {
        testGetDescriptorBase("booleanProperty", Boolean.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>doubleProperty</code>.
     */
    public void testGetDescriptorDouble() {
        testGetDescriptorBase("doubleProperty", Double.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>floatProperty</code>.
     */
    public void testGetDescriptorFloat() {
        testGetDescriptorBase("floatProperty", Float.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>intProperty</code>.
     */
    public void testGetDescriptorInt() {
        testGetDescriptorBase("intProperty", Integer.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>longProperty</code>.
     */
    public void testGetDescriptorLong() {
        testGetDescriptorBase("longProperty", Long.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>booleanSecond</code>
     * that uses an "is" method as the getter.
     */
    public void testGetDescriptorSecond() {
        testGetDescriptorBase("booleanSecond", Boolean.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>shortProperty</code>.
     */
    public void testGetDescriptorShort() {
        testGetDescriptorBase("shortProperty", Short.TYPE);
    }

    /**
     * Positive getDynaProperty on property <code>stringProperty</code>.
     */
    public void testGetDescriptorString() {
        testGetDescriptorBase("stringProperty", String.class);
    }

    /**
     * Positive test for getDynaPropertys().  Each property name
     * listed in <code>properties</code> should be returned exactly once.
     */
    public void testGetDescriptors() {
        DynaProperty pd[] = bean.getDynaClass().getDynaProperties();
        assertNotNull("Got descriptors", pd);
        int count[] = new int[properties.length];
        for (int i = 0; i < pd.length; i++) {
            String name = pd[i].getName();
            for (int j = 0; j < properties.length; j++) {
                if (name.equals(properties[j]))
                    count[j]++;
            }
        }
        for (int j = 0; j < properties.length; j++) {
            if (count[j] < 0)
                fail("Missing property " + properties[j]);
            else if (count[j] > 1)
                fail("Duplicate property " + properties[j]);
        }
    }

    /**
     * Corner cases on getIndexedProperty invalid arguments.
     */
    public void testGetIndexedArguments() {
        try {
            bean.get("intArray", -1);
        } catch (IndexOutOfBoundsException e) {
            return; // Expected response
        } catch (Throwable t) {
            fail("Threw '" + t + "' instead of 'IndexOutOfBoundsException'");
            return;
        }
        fail("Should throw IndexOutOfBoundsException");
    }

    /**
     * Positive and negative tests on getIndexedProperty valid arguments.
     */
    public void testGetIndexedValues() {
        Object value = null;
        for (int i = 0; i < 5; i++) {

            try {
                value = bean.get("intArray", i);
            } catch (Throwable t) {
                fail("intArray " + i + " threw " + t);
            }

            assertNotNull("intArray index " + i + " did not return value.", value);
            assertTrue("intArray index " + i + " did not return Integer.", (value instanceof Integer));
            assertEquals("intArray " + i + " returned incorrect value.", i * 10, ((Integer) value).intValue());

            try {
                value = bean.get("intIndexed", i);
            } catch (Throwable t) {
                fail("intIndexed index " + i + " threw " + t);
            }

            assertNotNull("intIndexed index " + i + "returned value " + i, value);
            assertTrue("intIndexed index " + i + "returned Integer " + i,
                    value instanceof Integer);
            assertEquals("intIndexed index " + i + "returned correct " + i, i * 10,
                    ((Integer) value).intValue());

            try {
                value = bean.get("listIndexed", i);
            } catch (Throwable t) {
                fail("listIndexed index " + i + " threw " + t);
            }

            assertNotNull("listIndexed index " + i + "returned value " + i, value);
            assertTrue("list index " + i + "returned String " + i,
                    value instanceof String);
            assertEquals("listIndexed index " + i + "returned correct " + i,
                    "String " + i, (String) value);

            try {
                value = bean.get("stringArray", i);
            } catch (Throwable t) {
                fail("stringArray index " + i + " threw " + t);
            }

            assertNotNull("stringArray index " + i + " returnde null.", value);
            assertFalse("stringArray index " + i + " returned array instead of String.",
                    value.getClass().isArray());
            assertTrue("stringArray index " + i + " returned "
                    + value.getClass().getName() + "=["+value+"]"
                    + "  instead of String.",
                    value instanceof String);
            assertEquals("stringArray returned correct " + i,
                    "String " + i, (String) value);

            try {
                value = bean.get("stringIndexed", i);
            } catch (Throwable t) {
                fail("stringIndexed " + i + " threw " + t);
            }

            assertNotNull("stringIndexed returned value " + i, value);
            assertTrue("stringIndexed returned String " + i,
                    value instanceof String);
            assertEquals("stringIndexed returned correct " + i,
                    "String " + i, (String) value);
        }
    }

    /**
     * Corner cases on getMappedProperty invalid arguments.
     */
    public void testGetMappedArguments() {
        try {
            Object value = bean.get("mappedProperty", "unknown");
            assertNull("Should not return a value", value);
        } catch (Throwable t) {
            fail("Threw " + t + " instead of returning null");
        }
    }

    /**
     * Positive and negative tests on getMappedProperty valid arguments.
     */
    public void testGetMappedValues() {
        Object value = null;

        try {
            value = bean.get("mappedProperty", "key1");
            assertEquals("Can find first value", "First Value", value);
        } catch (Throwable t) {
            fail("Finding first value threw " + t);
        }

        try {
            value = bean.get("mappedProperty", "key2");
            assertEquals("Can find second value", "Second Value", value);
        } catch (Throwable t) {
            fail("Finding second value threw " + t);
        }

        try {
            value = bean.get("mappedProperty", "key3");
            assertNotNull("Cannot find third value", value);
        } catch (Throwable t) {
            fail("Finding third value threw " + t);
        }
    }

    /**
     * Corner cases on getSimpleProperty invalid arguments.
     */
    public void testGetSimpleArguments() {
        try {
            bean.get(null);
        } catch (IllegalArgumentException e) {
            return; // Expected response
        } catch (Throwable t) {
            fail("Threw " + t + " instead of IllegalArgumentException");
        }
        fail("Should throw IllegalArgumentException");
    }

    /**
     * Test getSimpleProperty on a boolean property.
     */
    public void testGetSimpleBoolean() {

        try {
            Object value = bean.get("booleanProperty");
            assertNotNull("Got a value", value);
            assertTrue("Got correct type", (value instanceof Boolean));
            assertTrue("Got correct value",
                    ((Boolean) value).booleanValue() == true);
        } catch (Throwable e) {
            fail("Exception: " + e);
        }

    }

    /**
     * Test getSimpleProperty on a double property.
     */
    public void testGetSimpleDouble() {

        try {
            Object value = bean.get("doubleProperty");
            assertNotNull("Got a value", value);
            assertTrue("Got correct type", (value instanceof Double));
            assertEquals("Got correct value",
                    ((Double) value).doubleValue(),
                    (double) Double.MAX_VALUE,
                    (double) 0.005);
        } catch (Throwable t) {
            fail("Exception: " + t);
        }

    }

    /**
     * Test getSimpleProperty on a float property.
     */
    public void testGetSimpleFloat() {

        try {
            Object value = bean.get("floatProperty");
            assertNotNull("Got a value", value);
            assertTrue("Got correct type", (value instanceof Float));
            assertEquals("Got correct value",
                    ((Float) value).floatValue(),
                    Float.MAX_VALUE,
                    (float) 0.005);
        } catch (Throwable t) {
            fail("Exception: " + t);
        }

    }

    /**
     * Test getSimpleProperty on a int property.
     */
    public void testGetSimpleInt() {

        try {
            Object value = bean.get("intProperty");
            assertNotNull("Failed to get value", value);
            assertTrue("Incorrect type", (value instanceof Integer));
            assertEquals("Incorrect value",
                    ((Integer) value).intValue(),
                    Integer.MAX_VALUE);
        } catch (Throwable t) {
            fail("Exception: " + t);
        }

    }

    /**
     * Test getSimpleProperty on a long property.
     */
    public void testGetSimpleLong() {

        try {
            Object value = bean.get("longProperty");
            assertNotNull("Got a value", value);
            assertTrue("Returned incorrect type", (value instanceof Long));
            assertEquals("Returned value of Incorrect value",
                    ((Long) value).longValue(),
                    Long.MAX_VALUE);
        } catch (Throwable t) {
            fail("Exception: " + t);
        }

    }

    /**
     * Test getSimpleProperty on a short property.
     */
    public void testGetSimpleShort() {

        try {
            Object value = bean.get("shortProperty");
            assertNotNull("Got a value", value);
            assertTrue("Got correct type", (value instanceof Short));
            assertEquals("Got correct value",
                    ((Short) value).shortValue(),
                    Short.MAX_VALUE);
        } catch (Throwable t) {
            fail("Exception: " + t);
        }

    }

    /**
     * Test getSimpleProperty on a String property.
     */
    public void testGetSimpleString() {

        try {
            Object value = bean.get("stringProperty");
            assertNotNull("Got a value", value);
            assertTrue("Got correct type", (value instanceof String));
            assertEquals("Got correct value",
                    (String) value,
                    "This is a string");
        } catch (Throwable t) {
            fail("Exception: " + t);
        }

    }

    /**
     * Test <code>contains()</code> method for mapped properties.
     */
    public void testMappedContains() {
        try {
            assertTrue("Can't see first key", bean.contains("mappedProperty", "key1"));
        } catch (Exception e) {
            fail("Exception: " + e);
        }

        try {
            assertTrue("Can see unknown key",
                    !bean.contains("mappedProperty", "Unknown Key"));
        } catch (Throwable t) {
            fail("Exception: " + t);
        }

    }

    /**
     * Test <code>remove()</code> method for mapped properties.
     */
    public void testMappedRemove() {

        try {
            assertTrue("Can see first key",
                    bean.contains("mappedProperty", "key1"));
            bean.remove("mappedProperty", "key1");
            assertTrue("Can not see first key",
                    !bean.contains("mappedProperty", "key1"));
        } catch (Throwable t) {
            fail("Exception: " + t);
        }

        try {
            assertTrue("Can not see unknown key",
                    !bean.contains("mappedProperty", "key4"));
            bean.remove("mappedProperty", "key4");
            assertTrue("Can not see unknown key",
                    !bean.contains("mappedProperty", "key4"));
        } catch (Throwable t) {
            fail("Exception: " + t);
        }

    }

    /**
     * Corner cases on setIndexedProperty invalid arguments.
     */
    public void testSetIndexedArguments() {
        try {
            bean.set("intArray", -1, new Integer(0));
         } catch (IndexOutOfBoundsException e) {
            return; // Expected response
        } catch (Throwable t) {
            fail("Threw " + t + " instead of IndexOutOfBoundsException");
        }
        fail("Should throw IndexOutOfBoundsException");
    }

    /**
     * Positive and negative tests on setIndexedProperty valid arguments.
     */
    public void testSetIndexedValues() {
        Object value = null;

        try {
            bean.set("intArray", 0, new Integer(1));
            value = (Integer) bean.get("intArray", 0);
        } catch (Throwable t) {
            fail("Threw " + t);
        }

        assertNotNull("Returned new value 0", value);
        assertTrue("Returned Integer new value 0",
                value instanceof Integer);
        assertEquals("Returned correct new value 0", 1,
                ((Integer) value).intValue());

        try {
            bean.set("intIndexed", 1, new Integer(11));
            value = (Integer) bean.get("intIndexed", 1);
        } catch (Throwable t) {
            fail("Threw " + t);
        }

        assertNotNull("Returned new value 1", value);
        assertTrue("Returned Integer new value 1",
                value instanceof Integer);
        assertEquals("Returned correct new value 1", 11,
                ((Integer) value).intValue());

        try {
            bean.set("listIndexed", 2, "New Value 2");
            value = (String) bean.get("listIndexed", 2);
        } catch (Throwable t) {
            fail("Threw " + t);
        }

        assertNotNull("Returned new value 2", value);
        assertTrue("Returned String new value 2",
                value instanceof String);
        assertEquals("Returned correct new value 2", "New Value 2",
                (String) value);

        try {
            bean.set("stringArray", 3, "New Value 3");
            value = (String) bean.get("stringArray", 3);
        } catch (Throwable t) {
            fail("Threw " + t);
        }

        assertNotNull("Returned new value 3", value);
        assertTrue("Returned String new value 3",
                value instanceof String);
        assertEquals("Returned correct new value 3", "New Value 3",
                (String) value);

        try {
            bean.set("stringIndexed", 4, "New Value 4");
            value = (String) bean.get("stringIndexed", 4);
        } catch (Throwable t) {
            fail("Threw " + t);
        }
        assertNotNull("Returned new value 4", value);
        assertTrue("Returned String new value 4",
                value instanceof String);
        assertEquals("Returned correct new value 4", "New Value 4",
                (String) value);
    }

    /**
     * Positive and negative tests on setMappedProperty valid arguments.
     */
    public void testSetMappedValues() {

        try {
            bean.set("mappedProperty", "First Key", "New First Value");
            assertEquals("Can replace old value",
                    "New First Value",
                    (String) bean.get("mappedProperty", "First Key"));
        } catch (Throwable t) {
            fail("Finding fourth value threw " + t);
        }

        try {
            bean.set("mappedProperty", "Fourth Key", "Fourth Value");
            assertEquals("Can set new value",
                    "Fourth Value",
                    (String) bean.get("mappedProperty", "Fourth Key"));
        } catch (Throwable t) {
            fail("Finding fourth value threw " + t);
        }

    }

    /**
     * Test setSimpleProperty on a boolean property.
     */
    public void testSetSimpleBoolean() {

        try {
            boolean oldValue =
                    ((Boolean) bean.get("booleanProperty")).booleanValue();
            boolean newValue = !oldValue;
            bean.set("booleanProperty", new Boolean(newValue));
            assertTrue("Matched new value",
                    newValue ==
                    ((Boolean) bean.get("booleanProperty")).booleanValue());
        } catch (Throwable e) {
            fail("Exception: " + e);
        }

    }

    /**
     * Test setSimpleProperty on a double property.
     */
    public void testSetSimpleDouble() {

        try {
            double oldValue =
                    ((Double) bean.get("doubleProperty")).doubleValue();
            double newValue = oldValue + 1.0;
            bean.set("doubleProperty", new Double(newValue));
            assertEquals("Matched new value",
                    newValue,
                    ((Double) bean.get("doubleProperty")).doubleValue(),
                    (double) 0.005);
        } catch (Throwable e) {
            fail("Exception: " + e);
        }

    }

    /**
     * Test setSimpleProperty on a float property.
     */
    public void testSetSimpleFloat() {

        try {
            float oldValue =
                    ((Float) bean.get("floatProperty")).floatValue();
            float newValue = oldValue + (float) 1.0;
            bean.set("floatProperty", new Float(newValue));
            assertEquals("Matched new value",
                    newValue,
                    ((Float) bean.get("floatProperty")).floatValue(),
                    (float) 0.005);
        } catch (Throwable e) {
            fail("Exception: " + e);
        }

    }

    /**
     * Test setSimpleProperty on a int property.
     */
    public void testSetSimpleInt() {

        try {
            int oldValue =
                    ((Integer) bean.get("intProperty")).intValue();
            int newValue = oldValue + 1;
            bean.set("intProperty", new Integer(newValue));
            assertEquals("Matched new value",
                    newValue,
                    ((Integer) bean.get("intProperty")).intValue());
        } catch (Throwable e) {
            fail("Exception: " + e);
        }

    }

    /**
     * Test setSimpleProperty on a long property.
     */
    public void testSetSimpleLong() {

        try {
            long oldValue =
                    ((Long) bean.get("longProperty")).longValue();
            long newValue = oldValue + 1;
            bean.set("longProperty", new Long(newValue));
            assertEquals("Matched new value",
                    newValue,
                    ((Long) bean.get("longProperty")).longValue());
        } catch (Throwable e) {
            fail("Exception: " + e);
        }

    }

    /**
     * Test setSimpleProperty on a short property.
     */
    public void testSetSimpleShort() {

        try {
            short oldValue =
                    ((Short) bean.get("shortProperty")).shortValue();
            short newValue = (short) (oldValue + 1);
            bean.set("shortProperty", new Short(newValue));
            assertEquals("Matched new value",
                    newValue,
                    ((Short) bean.get("shortProperty")).shortValue());
        } catch (Throwable e) {
            fail("Exception: " + e);
        }

    }

    /**
     * Test setSimpleProperty on a String property.
     */
    public void testSetSimpleString() {

        try {
            String oldValue = (String) bean.get("stringProperty");
            String newValue = oldValue + " Extra Value";
            bean.set("stringProperty", newValue);
            assertEquals("Matched new value",
                    newValue,
                    (String) bean.get("stringProperty"));
        } catch (Throwable e) {
            fail("Exception: " + e);
        }

    }

    /**
     * Tests set on a null value: should throw NPE.
     */
    public void testAddNullPropertyValue() {
        try {
            bean.set("nullProperty", null);
        } catch(NullPointerException e) {
            return;
        } catch(Throwable t) {
            fail("Threw " + t + " instead of NullPointerException");
            return;
        }
        fail("Should have thrown NullPointerException");
    }
    /**
     * Test the retrieval of a non-existent property.
     */
    public void testGetNonExistentProperty() {
        try {
            Object value = bean.get("nonexistProperty");
        } catch (IllegalArgumentException e) {
        	return;
        } catch(Exception e) {
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
    protected void testGetDescriptorBase(String name, Class type) {
        DynaProperty descriptor = null;
        try {
            descriptor = bean.getDynaClass().getDynaProperty(name);
        } catch (Throwable t) {
            fail("Threw an exception: " + t);
        }
        assertNotNull("Failed to get descriptor", descriptor);
        assertEquals("Got incorrect type", type, descriptor.getType());
    }

}
