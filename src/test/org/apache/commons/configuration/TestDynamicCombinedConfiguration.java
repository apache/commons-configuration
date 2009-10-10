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

package org.apache.commons.configuration;

import java.io.File;

import junit.framework.TestCase;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.text.StrLookup;

public class TestDynamicCombinedConfiguration extends TestCase
{
    private static String PATTERN ="${sys:Id}";
    private static String PATTERN1 = "target/test-classes/testMultiConfiguration_${sys:Id}.xml";
    private static String DEFAULT_FILE = "target/test-classes/testMultiConfiguration_default.xml";
    private static final File MULTI_TENENT_FILE = new File(
            "conf/testMultiTenentConfigurationBuilder4.xml");

    public void testConfiguration() throws Exception
    {
        DynamicCombinedConfiguration config = new DynamicCombinedConfiguration();
        XPathExpressionEngine engine = new XPathExpressionEngine();
        config.setExpressionEngine(engine);
        config.setKeyPattern(PATTERN);
        config.setDelimiterParsingDisabled(true);
        MultiFileHierarchicalConfiguration multi = new MultiFileHierarchicalConfiguration(PATTERN1);
        multi.setExpressionEngine(engine);
        config.addConfiguration(multi, "Multi");
        XMLConfiguration xml = new XMLConfiguration();
        xml.setExpressionEngine(engine);
        xml.setDelimiterParsingDisabled(true);
        xml.setFile(new File(DEFAULT_FILE));
        xml.load();
        config.addConfiguration(xml, "Default");

        verify("1001", config, 15);
        verify("1002", config, 25);
        verify("1003", config, 35);
        verify("1004", config, 50);
        assertEquals("a,b,c", config.getString("split/list3/@values"));
        assertEquals(0, config.getMaxIndex("split/list3/@values"));
        assertEquals("a\\,b\\,c", config.getString("split/list4/@values"));
        assertEquals("a,b,c", config.getString("split/list1"));
        assertEquals(0, config.getMaxIndex("split/list1"));
        assertEquals("a\\,b\\,c", config.getString("split/list2"));
    }

    public void testConcurrentGetAndReload() throws Exception
    {
        final int threadCount = 5;
        final int loopCount = 500;
        System.getProperties().remove("Id");
        DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder();
        factory.setFile(MULTI_TENENT_FILE);
        CombinedConfiguration config = factory.getConfiguration(true);

        assertEquals(config.getString("rowsPerPage"), "50");
        Thread testThreads[] = new Thread[threadCount];
        int failures[] = new int[threadCount];

        for (int i = 0; i < testThreads.length; ++i)
        {
            testThreads[i] = new ReloadThread(config, failures, i, loopCount, false, null, "50");
            testThreads[i].start();
        }

        int totalFailures = 0;
        for (int i = 0; i < testThreads.length; ++i)
        {
            testThreads[i].join();
            totalFailures += failures[i];
        }
        assertTrue(totalFailures + " failures Occurred", totalFailures == 0);
    }

    public void testConcurrentGetAndReload2() throws Exception
    {
        final int threadCount = 5;
        final int loopCount = 500;
        System.getProperties().remove("Id");
        DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder();
        factory.setFile(MULTI_TENENT_FILE);
        CombinedConfiguration config = factory.getConfiguration(true);

        assertEquals(config.getString("rowsPerPage"), "50");

        Thread testThreads[] = new Thread[threadCount];
        int failures[] = new int[threadCount];
        System.setProperty("Id", "2002");
        assertEquals(config.getString("rowsPerPage"), "25");
        for (int i = 0; i < testThreads.length; ++i)
        {
            testThreads[i] = new ReloadThread(config, failures, i, loopCount, false, null, "25");
            testThreads[i].start();
        }

        int totalFailures = 0;
        for (int i = 0; i < testThreads.length; ++i)
        {
            testThreads[i].join();
            totalFailures += failures[i];
        }
        System.getProperties().remove("Id");
        assertTrue(totalFailures + " failures Occurred", totalFailures == 0);
    }

    public void testConcurrentGetAndReloadMultipleClients() throws Exception
    {
        final int threadCount = 5;
        final int loopCount = 500;
        System.getProperties().remove("Id");
        DefaultConfigurationBuilder factory = new DefaultConfigurationBuilder();
        factory.setFile(MULTI_TENENT_FILE);
        CombinedConfiguration config = factory.getConfiguration(true);

        assertEquals(config.getString("rowsPerPage"), "50");

        Thread testThreads[] = new Thread[threadCount];
        int failures[] = new int[threadCount];
        String[] ids = new String[] {null, "2002", "3001", "3002", "3003"};
        String[] expected = new String[] {"50", "25", "15", "25", "50"};
        for (int i = 0; i < testThreads.length; ++i)
        {
            testThreads[i] = new ReloadThread(config, failures, i, loopCount, true, ids[i], expected[i]);
            testThreads[i].start();
        }

        int totalFailures = 0;
        for (int i = 0; i < testThreads.length; ++i)
        {
            testThreads[i].join();
            totalFailures += failures[i];
        }
        System.getProperties().remove("Id");
        if (totalFailures != 0)
        {
            System.out.println("Failures:");
            for (int i = 0; i < testThreads.length; ++i)
            {
                System.out.println("Thread " + i + " " + failures[i]);
            }
        }
        assertTrue(totalFailures + " failures Occurred", totalFailures == 0);
    }

    private class ReloadThread extends Thread
    {
        CombinedConfiguration combined;
        int[] failures;
        int index;
        int count;
        String expected;
        String id;
        boolean useId;

        ReloadThread(CombinedConfiguration config, int[] failures, int index, int count,
                     boolean useId, String id, String expected)
        {
            combined = config;
            this.failures = failures;
            this.index = index;
            this.count = count;
            this.expected = expected;
            this.id = id;
            this.useId = useId;
        }
        public void run()
        {
            failures[index] = 0;

            if (useId)
            {
                ThreadLookup.setId(id);
            }
            for (int i = 0; i < count; i++)
            {
                try
                {
                    String value = combined.getString("rowsPerPage", null);
                    if (value == null || !value.equals(expected))
                    {
                        ++failures[index];
                    }
                }
                catch (Exception ex)
                {
                    ++failures[index];
                }
            }
        }
    }

    private void verify(String key, DynamicCombinedConfiguration config, int rows)
    {
        System.setProperty("Id", key);
        assertTrue(config.getInt("rowsPerPage") == rows);
    }

    public static class ThreadLookup extends StrLookup
    {
        private static ThreadLocal id = new ThreadLocal();



        public ThreadLookup()
        {

        }

        public static void setId(String value)
        {
            id.set(value);
        }

        public String lookup(String key)
        {
            if (key == null || !key.equals("Id"))
            {
                return null;
            }
            String value = System.getProperty("Id");
            if (value != null)
            {
                return value;
            }
            return (String)id.get();

        }
    }
}
