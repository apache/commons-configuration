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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.combined.CombinedConfigurationBuilder;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * Test that the configuration factory returns keys in the same
 * sequence as the properties configurator
 *
 */
public class TestPropertiesSequence
{
    @Test
    public void testConfigurationValuesInSameOrderFromFile() throws Exception
    {
        final String simpleConfigurationFile = ConfigurationAssert.getTestFile("testSequence.properties").getAbsolutePath();
        final String compositeConfigurationFile = ConfigurationAssert.getTestFile("testSequenceDigester.xml").getAbsolutePath();

        final PropertiesConfiguration simpleConfiguration = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(simpleConfiguration);
        handler.setFileName(simpleConfigurationFile);
        handler.load();

        final CombinedConfigurationBuilder builder = new CombinedConfigurationBuilder();
        builder.configure(new FileBasedBuilderParametersImpl().setFileName(compositeConfigurationFile));
        final Configuration compositeConfiguration = builder.getConfiguration();

        final Configuration a = simpleConfiguration.subset("prefix");
        final Configuration b = compositeConfiguration.subset("prefix");

        final List<String> keysSimpleConfiguration = ConfigurationAssert.keysToList(a);
        final List<String> keysCompositeConfiguration = ConfigurationAssert.keysToList(b);

        assertTrue("Size:" + keysSimpleConfiguration.size(), keysSimpleConfiguration.size() > 0);
        assertEquals(keysSimpleConfiguration.size(), keysCompositeConfiguration.size());

        for (int i = 0; i < keysSimpleConfiguration.size(); i++)
        {
            assertEquals(keysSimpleConfiguration.get(i), keysCompositeConfiguration.get(i));
        }
    }

    @Test
    public void testConfigurationValuesInSameOrderWithManualAdd() throws Exception
    {
        final String simpleConfigurationFile = ConfigurationAssert.getTestFile("testSequence.properties").getAbsolutePath();
        final String compositeConfigurationFile = ConfigurationAssert.getTestFile("testSequenceDigester.xml").getAbsolutePath();

        final PropertiesConfiguration simpleConfiguration = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(simpleConfiguration);
        handler.setFileName(simpleConfigurationFile);
        handler.load();

        final CombinedConfigurationBuilder builder = new CombinedConfigurationBuilder();
        builder.configure(new FileBasedBuilderParametersImpl().setFileName(compositeConfigurationFile));
        final Configuration compositeConfiguration = builder.getConfiguration();

        simpleConfiguration.setProperty("prefix.Co.test", Boolean.TRUE);
        simpleConfiguration.setProperty("prefix.Av.test", Boolean.TRUE);

        compositeConfiguration.setProperty("prefix.Co.test", Boolean.TRUE);
        compositeConfiguration.setProperty("prefix.Av.test", Boolean.TRUE);

        final Configuration a = simpleConfiguration.subset("prefix");
        final Configuration b = compositeConfiguration.subset("prefix");

        final List<String> keysSimpleConfiguration = ConfigurationAssert.keysToList(a);
        final List<String> keysCompositeConfiguration = ConfigurationAssert.keysToList(b);

        assertTrue("Size:" + keysSimpleConfiguration.size(), keysSimpleConfiguration.size() > 0);
        assertEquals(keysSimpleConfiguration.size(), keysCompositeConfiguration.size());

        for (int i = 0; i < keysSimpleConfiguration.size(); i++)
        {
            assertEquals(keysSimpleConfiguration.get(i), keysCompositeConfiguration.get(i));
        }
    }

    @Test
    public void testMappingInSameOrder() throws Exception
    {
        final String simpleConfigurationFile = ConfigurationAssert.getTestFile("testSequence.properties").getAbsolutePath();
        final String compositeConfigurationFile = ConfigurationAssert.getTestFile("testSequenceDigester.xml").getAbsolutePath();

        final PropertiesConfiguration simpleConfiguration = new PropertiesConfiguration();
        final FileHandler handler = new FileHandler(simpleConfiguration);
        handler.setFileName(simpleConfigurationFile);
        handler.load();

        final CombinedConfigurationBuilder builder = new CombinedConfigurationBuilder();
        builder.configure(new FileBasedBuilderParametersImpl().setFileName(compositeConfigurationFile));
        final Configuration compositeConfiguration = builder.getConfiguration();

        final Configuration mapping = new BaseConfiguration();
        final Configuration mapping2 = new BaseConfiguration();

        for (final Iterator<String> keys = simpleConfiguration.getKeys(); keys.hasNext();)
        {
            final String key = keys.next();
            final String[] keyParts = StringUtils.split(key, ".");

            if ((keyParts.length == 3) && keyParts[0].equals("prefix") && keyParts[2].equals("postfix"))
            {
                final String serviceKey = keyParts[1];

                if (!mapping.containsKey(serviceKey))
                {
                    mapping.setProperty(serviceKey, simpleConfiguration.getString(key));
                }
            }
        }

        for (final Iterator<String> keys = compositeConfiguration.getKeys(); keys.hasNext();)
        {
            final String key = keys.next();
            final String[] keyParts = StringUtils.split(key, ".");

            if ((keyParts.length == 3) && keyParts[0].equals("prefix") && keyParts[2].equals("postfix"))
            {
                final String serviceKey = keyParts[1];

                if (!mapping2.containsKey(serviceKey))
                {
                    mapping2.setProperty(serviceKey, compositeConfiguration.getString(key));
                }
            }
        }
    }
}
