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
package org.apache.commons.configuration.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationRuntimeException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.beanutils.BeanDeclaration;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test class for {@code BasicConfigurationBuilder}.
 *
 * @version $Id$
 */
public class TestBasicConfigurationBuilder
{
    /**
     * Tries to create an instance without a result class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoClass()
    {
        Class<Configuration> cls = null;
        new BasicConfigurationBuilder<Configuration>(cls);
    }

    /**
     * Creates a map with test initialization parameters.
     *
     * @return the map with parameters
     */
    private static Map<String, Object> createTestParameters()
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("throwExceptionOnMissing", Boolean.TRUE);
        params.put("delimiterParsingDisabled", Boolean.TRUE);
        params.put("listDelimiter", Character.valueOf('.'));
        return params;
    }

    /**
     * Tests whether initialization parameters can be passed to the constructor.
     */
    @Test
    public void testInitWithParameters()
    {
        Map<String, Object> params = createTestParameters();
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class, params);
        Map<String, Object> params2 =
                new HashMap<String, Object>(builder.getParameters());
        assertEquals("Wrong parameters", createTestParameters(), params2);
    }

    /**
     * Tests whether a copy of the passed in parameters is created.
     */
    @Test
    public void testInitWithParametersDefensiveCopy()
    {
        Map<String, Object> params = createTestParameters();
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class, params);
        params.put("anotherParameter", "value");
        Map<String, Object> params2 =
                new HashMap<String, Object>(builder.getParameters());
        assertEquals("Wrong parameters", createTestParameters(), params2);
    }

    /**
     * Tests whether null parameters are handled correctly.
     */
    @Test
    public void testInitWithParametersNull()
    {
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class, null);
        assertTrue("Got parameters", builder.getParameters().isEmpty());
    }

    /**
     * Tests that the map with parameters cannot be modified.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetParametersModify()
    {
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class, createTestParameters());
        builder.getParameters().clear();
    }

    /**
     * Tests whether parameters can be set using the configure() method.
     */
    @Test
    public void testConfigure()
    {
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class)
                        .configure(new BasicBuilderParameters()
                                .setDelimiterParsingDisabled(true)
                                .setListDelimiter('.')
                                .setThrowExceptionOnMissing(true));
        Map<String, Object> params2 =
                new HashMap<String, Object>(builder.getParameters());
        assertEquals("Wrong parameters", createTestParameters(), params2);
    }

    /**
     * Tests whether new parameters can be set to replace existing ones.
     */
    @Test
    public void testSetParameters()
    {
        Map<String, Object> params1 = new HashMap<String, Object>();
        params1.put("someParameter", "value");
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class, params1);
        assertSame("Wrong result", builder,
                builder.setParameters(createTestParameters()));
        Map<String, Object> params2 =
                new HashMap<String, Object>(builder.getParameters());
        assertEquals("Wrong parameters", createTestParameters(), params2);
    }

    /**
     * Tests whether additional parameters can be added.
     */
    @Test
    public void testAddParameters()
    {
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class, createTestParameters());
        Map<String, Object> params = createTestParameters();
        params.put("anotherParameter", "value");
        assertSame("Wrong result", builder, builder.addParameters(params));
        Map<String, Object> params2 = builder.getParameters();
        assertTrue("No original parameters",
                params2.keySet().containsAll(createTestParameters().keySet()));
        assertEquals("Additional parameter not found", "value",
                params2.get("anotherParameter"));
    }

    /**
     * Tests whether null parameters are handled correctly by addParameters().
     */
    @Test
    public void testAddParametersNull()
    {
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class, createTestParameters());
        Map<String, Object> params = builder.getParameters();
        builder.addParameters(null);
        assertEquals("Parameters changed", params, builder.getParameters());
    }

    /**
     * Tests whether all parameters can be reset.
     */
    @Test
    public void testResetParameters()
    {
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class, createTestParameters());
        builder.resetParameters();
        assertTrue("Still got parameters", builder.getParameters().isEmpty());
    }

    /**
     * Tests whether the builder can create a correctly initialized
     * configuration object.
     */
    @Test
    public void testGetConfiguration() throws ConfigurationException
    {
        PropertiesConfiguration config =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class).configure(
                        new BasicBuilderParameters().setListDelimiter('*')
                                .setThrowExceptionOnMissing(true))
                        .getConfiguration();
        assertTrue("Delimiter parsing not disabled",
                config.isDelimiterParsingDisabled());
        assertTrue("Wrong exception flag", config.isThrowExceptionOnMissing());
        assertEquals("Wrong list delimiter", '*', config.getListDelimiter());
    }

    /**
     * Tests whether the builder can be accessed by multiple threads and that
     * only a single result object is produced.
     */
    @Test
    public void testGetConfigurationConcurrently() throws Exception
    {
        final int threadCount = 32;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        ConfigurationBuilder<?> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class);
        AccessBuilderThread[] threads = new AccessBuilderThread[threadCount];
        for (int i = 0; i < threadCount; i++)
        {
            threads[i] = new AccessBuilderThread(startLatch, endLatch, builder);
            threads[i].start();
        }
        startLatch.countDown();
        assertTrue("Timeout", endLatch.await(5, TimeUnit.SECONDS));
        Set<Object> results = new HashSet<Object>();
        for (AccessBuilderThread t : threads)
        {
            results.add(t.result);
        }
        assertEquals("Wrong number of result objects", 1, results.size());
    }

    /**
     * Tests whether a reset of the result object can be performed.
     */
    @Test
    public void testResetResult() throws ConfigurationException
    {
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class, createTestParameters());
        PropertiesConfiguration config = builder.getConfiguration();
        builder.resetResult();
        PropertiesConfiguration config2 = builder.getConfiguration();
        assertNotSame("No new result", config, config2);
        assertTrue("Wrong property", config2.isThrowExceptionOnMissing());
    }

    /**
     * Tests a full reset of the builder.
     */
    @Test
    public void testReset() throws ConfigurationException
    {
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class, createTestParameters());
        PropertiesConfiguration config = builder.getConfiguration();
        builder.reset();
        PropertiesConfiguration config2 = builder.getConfiguration();
        assertNotSame("No new result", config, config2);
        assertFalse("Parameters not reset", config2.isThrowExceptionOnMissing());
    }

    /**
     * Tests whether a check for the correct bean class is made.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testGetResultDeclarationInvalidBeanClass()
            throws ConfigurationException
    {
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class, createTestParameters())
                {
                    @Override
                    protected BeanDeclaration createResultDeclaration(
                            Map<String, Object> params)
                    {
                        BeanDeclaration decl =
                                EasyMock.createMock(BeanDeclaration.class);
                        EasyMock.expect(decl.getBeanClassName())
                                .andReturn(Object.class.getName()).anyTimes();
                        EasyMock.replay(decl);
                        return decl;
                    }
                };
        builder.getConfiguration();
    }

    /**
     * A test thread class for testing whether the builder's result object can
     * be requested concurrently.
     */
    private static class AccessBuilderThread extends Thread
    {
        /** A latch for controlling the start of the thread. */
        private final CountDownLatch startLatch;

        /** A latch for controlling the end of the thread. */
        private final CountDownLatch endLatch;

        /** The builder to be accessed. */
        private final ConfigurationBuilder<?> builder;

        /** The result object obtained from the builder. */
        private volatile Object result;

        /**
         * Creates a new instance of {@code AccessBuilderThread}.
         *
         * @param lstart the latch for controlling the thread start
         * @param lend the latch for controlling the thread end
         * @param bldr the builder to be tested
         */
        public AccessBuilderThread(CountDownLatch lstart, CountDownLatch lend,
                ConfigurationBuilder<?> bldr)
        {
            startLatch = lstart;
            endLatch = lend;
            builder = bldr;
        }

        @Override
        public void run()
        {
            try
            {
                startLatch.await();
                result = builder.getConfiguration();
            }
            catch (Exception ex)
            {
                result = ex;
            }
            finally
            {
                endLatch.countDown();
            }
        }
    }
}
