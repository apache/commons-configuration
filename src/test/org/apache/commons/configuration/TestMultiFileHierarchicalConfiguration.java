package org.apache.commons.configuration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

/**
 * Unit test for simple MultiConfigurationTest.
 */
public class TestMultiFileHierarchicalConfiguration
    extends TestCase
{
    private static String PATTERN1 = "target/test-classes/testMultiConfiguration_${sys:Id}.xml";
    
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestMultiFileHierarchicalConfiguration( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestMultiFileHierarchicalConfiguration.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testMultiConfiguration()
    {
        //set up a reloading strategy
        FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
        strategy.setRefreshDelay(10000);
        
        MultiFileHierarchicalConfiguration config = new MultiFileHierarchicalConfiguration(PATTERN1);
        config.setReloadingStrategy(strategy);

        System.setProperty("Id", "1001");
        assertTrue(config.getInt("rowsPerPage") == 15);

        System.setProperty("Id", "1002");
        assertTrue(config.getInt("rowsPerPage") == 25);

        System.setProperty("Id", "1003");
        assertTrue(config.getInt("rowsPerPage") == 35);
    }
}
