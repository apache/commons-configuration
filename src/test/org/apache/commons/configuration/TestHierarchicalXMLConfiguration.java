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
 
import junit.framework.TestCase;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

/**
 * Test class for HierarchicalXMLConfiguration,
 *
 * @author Emmanuel Bourg
 * @author Mark Woodman
 * @version $Id: TestHierarchicalXMLConfiguration.java,v 1.2 2004/09/06 13:12:04 epugh Exp $
 */
public class TestHierarchicalXMLConfiguration extends TestCase
{
    /** Test resources directory. */
    private static final String TEST_DIR = "conf";

    /** Test file #1 **/
    private static final String TEST_FILENAME = "testHierarchicalXMLConfiguration.xml";

    /** Test file #2 **/
    private static final String TEST_FILENAME2 = "testHierarchicalXMLConfiguration2.xml";

    /** Test file path #1 **/
    private static final String TEST_FILE = TEST_DIR + File.separator + TEST_FILENAME;

    /** Test file path #2 **/
    private static final String TEST_FILE2 = TEST_DIR + File.separator + TEST_FILENAME2;

    /** Instance config used for tests. */
    private HierarchicalXMLConfiguration config;

    /** Fixture setup. */
    protected void setUp() throws Exception
    {
        config = new HierarchicalXMLConfiguration();
    }

    private void configTest(HierarchicalXMLConfiguration config)
    {
        assertEquals(1, config.getMaxIndex("tables.table"));
        assertEquals("system", config.getProperty("tables.table(0)[@tableType]"));
        assertEquals("application", config.getProperty("tables.table(1)[@tableType]"));
        
        assertEquals("users", config.getProperty("tables.table(0).name"));
        assertEquals("documents", config.getProperty("tables.table(1).name"));
        
        Object prop = config.getProperty("tables.table.fields.field.name");
        assertTrue(prop instanceof Collection);
        assertEquals(10, ((Collection) prop).size());
        
        prop = config.getProperty("tables.table(0).fields.field.type");
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection) prop).size());
        
        prop = config.getProperty("tables.table(1).fields.field.type");
        assertTrue(prop instanceof Collection);
        assertEquals(5, ((Collection) prop).size());
    }
    
    public void testGetProperty() throws Exception
    {
        config.setFileName(TEST_FILE);
        config.load();

        configTest(config);
    }
    
    public void testLoadURL() throws Exception
    {
        config.load(new File(TEST_FILE).getAbsoluteFile().toURL());
        configTest(config);
    }
    
    public void testLoadBasePath1() throws Exception
    {
        config.setBasePath(TEST_DIR);
        config.setFileName(TEST_FILENAME);
        config.load();
        configTest(config);
    }
    
    public void testLoadBasePath2() throws Exception
    {
        config.setBasePath(new File(TEST_FILE).getAbsoluteFile().toURL().toString());
        config.setFileName(TEST_FILENAME);
        config.load();
        configTest(config);
    }

    /**
     * Ensure various node types are correctly processed in config.
     * @throws Exception
     */
    public void testXmlNodeTypes() throws Exception
    {
        // Number of keys expected from test configuration file
        final int KEY_COUNT = 5;

        // Load the configuration file
        config.load(new File(TEST_FILE2).getAbsoluteFile().toURL());

        // Validate comment in element ignored
        assertEquals("Comment in element must not change element value.", "Case1Text", config.getString("case1"));

        // Validate sibling comment ignored
        assertEquals("Comment as sibling must not change element value.", "Case2Text", config.getString("case2.child"));

        // Validate comment ignored, CDATA processed
        assertEquals("Comment and use of CDATA must not change element value.", "Case3Text", config.getString("case3"));

        // Validate comment and processing instruction ignored
        assertEquals("Comment and use of PI must not change element value.", "Case4Text", config.getString("case4"));

        // Validate comment ignored in parent attribute
        assertEquals("Comment must not change attribute node value.", "Case5Text", config.getString("case5[@attr]"));

        // Validate non-text nodes haven't snuck in as keys
        Iterator iter = config.getKeys();
        int count = 0;
        while (iter.hasNext())
        {
            iter.next();
            count++;
        }
        assertEquals("Config must contain only " + KEY_COUNT + " keys.", KEY_COUNT, count);
    }

}
