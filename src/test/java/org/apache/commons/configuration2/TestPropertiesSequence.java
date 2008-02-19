package org.apache.commons.configuration2;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import junit.framework.TestCase;

/**
 * Test that the configuration factory returns keys in the same
 * sequence as the properties configurator
 *
 * @version $Id$
 */
public class TestPropertiesSequence extends TestCase
{

    public void testConfigurationValuesInSameOrderFromFile() throws Exception
    {
        String simpleConfigurationFile = ConfigurationAssert.getTestFile("testSequence.properties").getAbsolutePath();
        String compositeConfigurationFile = ConfigurationAssert.getTestFile("testSequenceDigester.xml").getAbsolutePath();

        Configuration simpleConfiguration = new PropertiesConfiguration(simpleConfigurationFile);

        DefaultConfigurationBuilder configurationBuilder = new DefaultConfigurationBuilder();
        configurationBuilder.setFileName(compositeConfigurationFile);
        Configuration compositeConfiguration = configurationBuilder.getConfiguration();

        Configuration a = simpleConfiguration.subset("prefix");
        Configuration b = compositeConfiguration.subset("prefix");

        List<String> keysSimpleConfiguration = keyList(a);
        List<String> keysCompositeConfiguration = keyList(b);

        assertTrue("Size:" + keysSimpleConfiguration.size(), keysSimpleConfiguration.size() > 0);
        assertEquals(keysSimpleConfiguration.size(), keysCompositeConfiguration.size());

        for (int i = 0; i < keysSimpleConfiguration.size(); i++)
        {
            assertEquals(keysSimpleConfiguration.get(i), keysCompositeConfiguration.get(i));
        }
    }

    public void testConfigurationValuesInSameOrderWithManualAdd() throws Exception
    {
        String simpleConfigurationFile = ConfigurationAssert.getTestFile("testSequence.properties").getAbsolutePath();
        String compositeConfigurationFile = ConfigurationAssert.getTestFile("testSequenceDigester.xml").getAbsolutePath();

        Configuration simpleConfiguration = new PropertiesConfiguration(simpleConfigurationFile);

        DefaultConfigurationBuilder configurationBuilder = new DefaultConfigurationBuilder();
        configurationBuilder.setFileName(compositeConfigurationFile);
        Configuration compositeConfiguration = configurationBuilder.getConfiguration();

        simpleConfiguration.setProperty("prefix.Co.test", Boolean.TRUE);
        simpleConfiguration.setProperty("prefix.Av.test", Boolean.TRUE);

        compositeConfiguration.setProperty("prefix.Co.test", Boolean.TRUE);
        compositeConfiguration.setProperty("prefix.Av.test", Boolean.TRUE);

        Configuration a = simpleConfiguration.subset("prefix");
        Configuration b = compositeConfiguration.subset("prefix");

        List<String> keysSimpleConfiguration = keyList(a);
        List<String> keysCompositeConfiguration = keyList(b);

        assertTrue("Size:" + keysSimpleConfiguration.size(), keysSimpleConfiguration.size() > 0);
        assertEquals(keysSimpleConfiguration.size(), keysCompositeConfiguration.size());

        for (int i = 0; i < keysSimpleConfiguration.size(); i++)
        {
            assertEquals(keysSimpleConfiguration.get(i), keysCompositeConfiguration.get(i));
        }
    }

    public void testMappingInSameOrder() throws Exception
    {
        String simpleConfigurationFile = ConfigurationAssert.getTestFile("testSequence.properties").getAbsolutePath();
        String compositeConfigurationFile = ConfigurationAssert.getTestFile("testSequenceDigester.xml").getAbsolutePath();

        Configuration simpleConfiguration = new PropertiesConfiguration(simpleConfigurationFile);

        DefaultConfigurationBuilder configurationBuilder = new DefaultConfigurationBuilder();
        configurationBuilder.setFileName(compositeConfigurationFile);
        Configuration compositeConfiguration = configurationBuilder.getConfiguration();

        Configuration mapping = new BaseConfiguration();
        Configuration mapping2 = new BaseConfiguration();

        for (Iterator<String> keys = simpleConfiguration.getKeys(); keys.hasNext();)
        {
            String key = keys.next();
            String[] keyParts = StringUtils.split(key, ".");

            if ((keyParts.length == 3) && keyParts[0].equals("prefix") && keyParts[2].equals("postfix"))
            {
                String serviceKey = keyParts[1];

                if (!mapping.containsKey(serviceKey))
                {
                    mapping.setProperty(serviceKey, simpleConfiguration.getString(key));
                }
            }
        }

        for (Iterator<String> keys = compositeConfiguration.getKeys(); keys.hasNext();)
        {
            String key = keys.next();
            String[] keyParts = StringUtils.split(key, ".");

            if ((keyParts.length == 3) && keyParts[0].equals("prefix") && keyParts[2].equals("postfix"))
            {
                String serviceKey = keyParts[1];

                if (!mapping2.containsKey(serviceKey))
                {
                    mapping2.setProperty(serviceKey, compositeConfiguration.getString(key));
                }
            }
        }
    }

    /**
     * Returns a list with the keys of the specified configuration.
     *
     * @param config the configuration
     * @return a list with the keys of this configuration
     */
    private static List<String> keyList(Configuration config)
    {
        List<String> keys = new ArrayList<String>();
        for (Iterator<String> it = config.getKeys(); it.hasNext();)
        {
            keys.add(it.next());
        }
        return keys;
    }
}
