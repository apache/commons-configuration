/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.commons.configuration;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.cactus.ServletTestCase;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestConfigurationFactoryWithJNDI extends ServletTestCase
{
    private File testDigesterFile = new File("conf/testDigesterConfigurationWJNDI.xml");
    private static Log log = LogFactory.getLog(TestConfigurationFactoryWithJNDI.class);

    public void testLoadingWithDigester() throws Exception
    {
        ConfigurationFactory cf = new ConfigurationFactory();
        cf.setConfigurationFileName(testDigesterFile.toString());
        CompositeConfiguration compositeConfiguration = (CompositeConfiguration) cf.getConfiguration();

        assertEquals("Verify how many configs", 4, compositeConfiguration.getNumberOfConfigurations());

        assertEquals(JNDIConfiguration.class, compositeConfiguration.getConfiguration(1).getClass());
        assertEquals(PropertiesConfiguration.class, compositeConfiguration.getConfiguration(2).getClass());
        //assertEquals(DOM4JConfiguration.class, compositeConfiguration.getConfiguration(3).getClass());
        PropertiesConfiguration pc = (PropertiesConfiguration) compositeConfiguration.getConfiguration(2);

        assertNotNull("Make sure we have a fileName:" + pc.getFileName(), pc.getFileName());

        assertTrue("Make sure we have loaded our key", compositeConfiguration.getBoolean("test.boolean"));
        assertEquals("I'm complex!", compositeConfiguration.getProperty("element2.subelement.subsubelement"));

        assertEquals("Make sure the JNDI config overwrites everything else!", "80", compositeConfiguration.getString("test.overwrite"));
    }

    /**
     * Verify the getKeys() method works.
     *
     * @throws Exception
     */
    public void testGetKeys() throws Exception
    {
        ConfigurationFactory cf = new ConfigurationFactory();
        cf.setConfigurationFileName(testDigesterFile.toString());

        Configuration c = cf.getConfiguration();

        List iteratedList = IteratorUtils.toList(c.getKeys());
        assertTrue(iteratedList.contains("test.jndi"));
    }

    /**
     * Test that a simple key works with JNDI
     *
     * @throws Exception
     */
    public void testGetKeysWithString() throws Exception
    {
        String KEY = "test";
        ConfigurationFactory cf = new ConfigurationFactory();
        cf.setConfigurationFileName(testDigesterFile.toString());

        Configuration c = cf.getConfiguration();

        List iteratedList = IteratorUtils.toList(c.getKeys(KEY));

        assertTrue("Size:" + iteratedList.size(), iteratedList.size() > 0);
        assertTrue(iteratedList.contains("test.jndi"));
        for (Iterator i = iteratedList.iterator(); i.hasNext();)
        {
            String foundKey = (String) i.next();
            assertTrue(foundKey.startsWith(KEY));
        }
    }

    /**
     * Verify that if a key is made of multiple parts, we still find
     * the correct JNDI Context.
     *
     * @throws Exception
     */
    public void testGetKeysWithString2() throws Exception
    {
        String KEY = "test.deep";
        ConfigurationFactory cf = new ConfigurationFactory();
        cf.setConfigurationFileName(testDigesterFile.toString());

        Configuration c = cf.getConfiguration();

        List iteratedList = IteratorUtils.toList(c.getKeys(KEY));

        assertTrue("Size:" + iteratedList.size(), iteratedList.size() == 2);
        assertTrue(iteratedList.contains("test.deep.somekey"));
        for (Iterator i = iteratedList.iterator(); i.hasNext();)
        {
            String foundKey = (String) i.next();
            assertTrue(foundKey.startsWith(KEY));
        }
    }

}
