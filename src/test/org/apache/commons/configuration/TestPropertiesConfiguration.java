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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Test for loading and saving properties files.
 *
 * @version $Id: TestPropertiesConfiguration.java,v 1.7 2004/06/15 10:12:29 ebourg Exp $
 */
public class TestPropertiesConfiguration extends TestBasePropertiesConfiguration
{
    /** The File that we test with */
    private String testProperties = new File("conf/test.properties").getAbsolutePath();

    private String testBasePath = new File("conf").getAbsolutePath();
    private String testBasePath2 = new File("conf").getAbsoluteFile().getParentFile().getAbsolutePath();
    private File testSavePropertiesFile = new File("target/testsave.properties");

    protected void setUp() throws Exception
    {
        conf = new PropertiesConfiguration(testProperties);
    }

    public void testSave() throws Exception
    {
    	// remove the file previously saved if necessary
        if(testSavePropertiesFile.exists()){
    		assertTrue(testSavePropertiesFile.delete());
    	}

        // add an array of strings to the configuration
    	conf.addProperty("string", "value1");
        List list = new ArrayList();
        for (int i = 1; i < 5; i++)
        {
            list.add("value" + i);
        }
        conf.addProperty("array", list);

        // save the configuration
        conf.save(testSavePropertiesFile.getAbsolutePath());

        // read the configuration and compare the properties
        PropertiesConfiguration checkConfig = new PropertiesConfiguration(testSavePropertiesFile.getAbsolutePath());
        for (Iterator i = conf.getKeys(); i.hasNext();) {
        	String key = (String) i.next();
        	assertTrue("The saved configuration doesn't contain the key '" + key + "'", checkConfig.containsKey(key));
        	assertEquals("Value of the '" + key + "' property", conf.getProperty(key), checkConfig.getProperty(key));
        }
    }

    public void testLoadViaProperty() throws Exception
    {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setFileName(testProperties);
        pc.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));
    }

    public void testLoadViaPropertyWithBasePath() throws Exception
    {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setBasePath(testBasePath);
        pc.setFileName("test.properties");
        pc.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));
    }

    public void testLoadViaPropertyWithBasePath2() throws Exception
    {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setBasePath(testBasePath2);
        pc.setFileName("conf/test.properties");
        pc.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));

        pc = new PropertiesConfiguration();
        pc.setBasePath(testBasePath2);
        pc.setFileName("conf/test.properties");
        pc.load();

        assertTrue("Make sure we have multiple keys", pc.getBoolean("test.boolean"));
    }

    public void testGetStringWithEscapedChars()
    {
        String property = conf.getString("test.unescape");
        assertEquals("String with escaped characters", "This \n string \t contains \" escaped \\ characters", property);
    }

    public void testGetStringWithEscapedComma()
    {
        String property = conf.getString("test.unescape.list-separator");
        assertEquals("String with an escaped list separator", "This string contains , an escaped list separator", property);
    }

    public void testUnescapeJava()
    {
        assertEquals("test\\,test", PropertiesConfiguration.unescapeJava("test\\,test"));
    }
}
