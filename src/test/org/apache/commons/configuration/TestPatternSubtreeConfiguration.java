package org.apache.commons.configuration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

import java.io.File;

/**
 * Unit test for simple MultiConfigurationTest.
 */
public class TestPatternSubtreeConfiguration extends TestCase
{
    private static String CONFIG_FILE = "target/test-classes/testPatternSubtreeConfig.xml";
    private static String PATTERN = "BusinessClient[@name='${sys:Id}']";
    private XMLConfiguration conf;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestPatternSubtreeConfiguration( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() throws Exception
    {
        return new TestSuite(TestPatternSubtreeConfiguration.class );
    }

    protected void setUp() throws Exception
    {
        conf = new XMLConfiguration();
        conf.setFile(new File(CONFIG_FILE));
        conf.load();
    }

    /**
     * Rigourous Test :-)
     */
    public void testMultiConfiguration()
    {
        //set up a reloading strategy
        FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
        strategy.setRefreshDelay(10000);

        PatternSubtreeConfigurationWrapper config = new PatternSubtreeConfigurationWrapper(this.conf, PATTERN);
        config.setReloadingStrategy(strategy);
        config.setExpressionEngine(new XPathExpressionEngine());

        System.setProperty("Id", "1001");
        assertTrue(config.getInt("rowsPerPage") == 15);

        System.setProperty("Id", "1002");
        assertTrue(config.getInt("rowsPerPage") == 25);

        System.setProperty("Id", "1003");
        assertTrue(config.getInt("rowsPerPage") == 35);
    }
}