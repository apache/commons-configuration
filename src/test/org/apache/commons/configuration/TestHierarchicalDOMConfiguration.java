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
 
import java.io.File;
import java.util.Collection;

import junit.framework.TestCase;

/**
 * Test class for HierarchicalDOM4JConfiguration,
 *
 * @version $Id: TestHierarchicalDOMConfiguration.java,v 1.1 2004/04/01 18:43:03 epugh Exp $
 */
public class TestHierarchicalDOMConfiguration extends TestCase
{
    private static final String TEST_DIR = "conf";
    
    private static final String TEST_FILENAME = "testHierarchicalDOM4JConfiguration.xml";
    
    private static final String TEST_FILE = TEST_DIR + File.separator + TEST_FILENAME;
    
    private HierarchicalDOMConfiguration config;
    
    protected void setUp() throws Exception
    {
        config = new HierarchicalDOMConfiguration();
    }

    private void configTest(HierarchicalDOMConfiguration config)
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
}
