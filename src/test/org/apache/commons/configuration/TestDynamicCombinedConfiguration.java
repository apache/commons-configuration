package org.apache.commons.configuration;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 *
 */
public class TestDynamicCombinedConfiguration extends TestCase
{
    private static String PATTERN ="${sys:Id}";
    private static String PATTERN1 = "target/test-classes/testMultiConfiguration_${sys:Id}.xml";
    private static String DEFAULT_FILE = "target/test-classes/testMultiConfiguration_default.xml";

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestDynamicCombinedConfiguration( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestDynamicCombinedConfiguration.class );
    }

    public void testConfiguration() throws Exception
    {
        DynamicCombinedConfiguration config = new DynamicCombinedConfiguration();
        config.setKeyPattern(PATTERN);
        MultiFileHierarchicalConfiguration multi = new MultiFileHierarchicalConfiguration(PATTERN1);
        config.addConfiguration(multi, "Multi");
        XMLConfiguration xml = new XMLConfiguration(DEFAULT_FILE);
        config.addConfiguration(xml, "Default");

        verify("1001", config, 15);
        verify("1002", config, 25);
        verify("1003", config, 35);
        verify("1004", config, 50);
    }

    private void verify(String key, DynamicCombinedConfiguration config, int rows)
    {
        System.setProperty("Id", key);
        assertTrue(config.getInt("rowsPerPage") == rows);
    }
}
