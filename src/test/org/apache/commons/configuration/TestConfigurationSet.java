package org.apache.commons.configuration;

import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author rgladwel
 */
public class TestConfigurationSet extends TestCase {

    ConfigurationSet set;

    String[] properties = {
            "booleanProperty",
            "doubleProperty",
            "floatProperty",
            "intProperty",
            "longProperty",
            "shortProperty",
            "stringProperty"
    };

    Object[] values = {
            Boolean.TRUE,
            new Double(Double.MAX_VALUE),
            new Float(Float.MAX_VALUE),
            new Integer(Integer.MAX_VALUE),
            new Long(Long.MAX_VALUE),
            new Short(Short.MAX_VALUE),
            "This is a string"
    };

    /**
     * Construct a new instance of this test case.
     * @param name Name of the test case
     */
    public TestConfigurationSet(String name)
    {
        super(name);
    }

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception
    {
        BaseConfiguration configuration = new BaseConfiguration();
        for(int i = 0; i < properties.length ; i++)
            configuration.setProperty(properties[i], values[i]);
        set = new ConfigurationSet(configuration);
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite()
    {
        return (new TestSuite(TestConfigurationSet.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown()
    {
        set = null;
    }

    public void testSize() {
        assertEquals("Entry set does not match properties size.", properties.length, set.size());
    }

    /**
     * Class under test for Iterator iterator()
     */
    public void testIterator() {
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Object object = iterator.next();
            assertTrue("Entry set iterator did not return EntrySet object, returned "
                    + object.getClass().getName(), object instanceof Map.Entry);
            Map.Entry entry = (Map.Entry) object;
            boolean found = false;
            for(int i = 0; i < properties.length; i++) {
                if(entry.getKey().equals(properties[i])) {
                    assertEquals("Incorrect value for property " +
                            properties[i],values[i],entry.getValue());
                    found = true;
                }
            }
            assertTrue("Could not find property " + entry.getKey(),found);
            iterator.remove();
        }
        assertTrue("Iterator failed to remove all properties.",set.isEmpty());
    }

}
