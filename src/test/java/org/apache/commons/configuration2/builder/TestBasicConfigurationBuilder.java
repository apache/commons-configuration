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
package org.apache.commons.configuration2.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.Initializable;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.beanutils.BeanCreationContext;
import org.apache.commons.configuration2.beanutils.BeanDeclaration;
import org.apache.commons.configuration2.beanutils.BeanFactory;
import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.configuration2.beanutils.DefaultBeanFactory;
import org.apache.commons.configuration2.beanutils.XMLBeanDeclaration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.event.ConfigurationErrorEvent;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.ErrorListenerTestImpl;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventListenerRegistrationData;
import org.apache.commons.configuration2.event.EventListenerTestImpl;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.reloading.ReloadingController;
import org.apache.commons.configuration2.reloading.ReloadingDetector;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for {@code BasicConfigurationBuilder}.
 *
 * @version $Id$
 */
public class TestBasicConfigurationBuilder
{
    /** A test list delimiter handler. */
    private static ListDelimiterHandler listHandler;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        listHandler = new DefaultListDelimiterHandler(';');
    }

    /**
     * Tries to create an instance without a result class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNoClass()
    {
        new BasicConfigurationBuilder<Configuration>(null);
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
        params.put("listDelimiterHandler", listHandler);
        return params;
    }

    /**
     * Tests whether initialization parameters can be passed to the constructor.
     */
    @Test
    public void testInitWithParameters()
    {
        Map<String, Object> params = createTestParameters();
        BasicConfigurationBuilder<Configuration> builder =
                new BasicConfigurationBuilder<Configuration>(
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
                                .setListDelimiterHandler(listHandler)
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
                        new BasicBuilderParameters().setListDelimiterHandler(
                                listHandler).setThrowExceptionOnMissing(true))
                        .getConfiguration();
        assertTrue("Wrong exception flag", config.isThrowExceptionOnMissing());
        assertEquals("Wrong list delimiter handler", listHandler,
                config.getListDelimiterHandler());
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
                        return new XMLBeanDeclaration(
                                new BaseHierarchicalConfiguration(), "bean",
                                true, Object.class.getName());
                    }
                };
        builder.getConfiguration();
    }

    /**
     * Creates a mock for an event listener.
     *
     * @return the event listener mock
     */
    private static EventListener<ConfigurationEvent> createEventListener()
    {
        @SuppressWarnings("unchecked")
        EventListener<ConfigurationEvent> listener =
                EasyMock.createMock(EventListener.class);
        return listener;
    }

    /**
     * Tests whether configuration listeners can be added.
     */
    @Test
    public void testAddConfigurationListener() throws ConfigurationException
    {
        EventListener<ConfigurationEvent> l1 = createEventListener();
        EventListener<ConfigurationEvent> l2 = createEventListener();
        EasyMock.replay(l1, l2);
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class);
        builder.addEventListener(ConfigurationEvent.ANY, l1);
        PropertiesConfiguration config = builder.getConfiguration();
        builder.addEventListener(ConfigurationEvent.ANY, l2);
        Collection<EventListener<? super ConfigurationEvent>> listeners =
                config.getEventListeners(ConfigurationEvent.ANY);
        assertTrue("Listener 1 not registered", listeners.contains(l1));
        assertTrue("Listener 2 not registered", listeners.contains(l2));
    }

    /**
     * Tests whether configuration listeners can be removed.
     */
    @Test
    public void testRemoveConfigurationListener() throws ConfigurationException
    {
        EventListener<ConfigurationEvent> l1 = createEventListener();
        EventListener<ConfigurationEvent> l2 = createEventListener();
        EasyMock.replay(l1, l2);
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class);
        builder.addEventListener(ConfigurationEvent.ANY_HIERARCHICAL,
                l1);
        builder.addEventListener(ConfigurationEvent.ANY, l2);
        assertTrue("Wrong result",
                builder.removeEventListener(ConfigurationEvent.ANY, l2));
        PropertiesConfiguration config = builder.getConfiguration();
        assertFalse("Removed listener was registered", config
                .getEventListeners(ConfigurationEvent.ANY).contains(l2));
        assertTrue("Listener not registered",
                config.getEventListeners(ConfigurationEvent.ANY_HIERARCHICAL)
                        .contains(l1));
        builder.removeEventListener(
                ConfigurationEvent.ANY_HIERARCHICAL, l1);
        assertFalse("Listener still registered",
                config.getEventListeners(ConfigurationEvent.ANY_HIERARCHICAL)
                        .contains(l1));
    }

    /**
     * Tests whether event listeners can be copied to another builder.
     */
    @Test
    public void testCopyEventListeners() throws ConfigurationException
    {
        EventListener<ConfigurationEvent> l1 = createEventListener();
        EventListener<ConfigurationEvent> l2 = createEventListener();
        EventListener<ConfigurationErrorEvent> l3 = new ErrorListenerTestImpl(null);
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class);
        builder.addEventListener(ConfigurationEvent.ANY, l1);
        builder.addEventListener(ConfigurationEvent.ANY_HIERARCHICAL, l2);
        builder.addEventListener(ConfigurationErrorEvent.ANY, l3);
        BasicConfigurationBuilder<XMLConfiguration> builder2 =
                new BasicConfigurationBuilder<XMLConfiguration>(
                        XMLConfiguration.class);
        builder.copyEventListeners(builder2);
        XMLConfiguration config = builder2.getConfiguration();
        Collection<EventListener<? super ConfigurationEvent>> listeners =
                config.getEventListeners(ConfigurationEvent.ANY);
        assertEquals("Wrong number of listeners", 1, listeners.size());
        assertTrue("Wrong listener", listeners.contains(l1));
        listeners =
                config.getEventListeners(ConfigurationEvent.ANY_HIERARCHICAL);
        assertEquals("Wrong number of listeners for hierarchical", 2,
                listeners.size());
        assertTrue("Listener 1 not found", listeners.contains(l1));
        assertTrue("Listener 2 not found", listeners.contains(l2));
        Collection<EventListener<? super ConfigurationErrorEvent>> errListeners =
                config.getEventListeners(ConfigurationErrorEvent.ANY);
        assertEquals("Wrong number of error listeners", 1, errListeners.size());
        assertTrue("Wrong error listener", errListeners.contains(l3));
    }

    /**
     * Tests whether configuration listeners can be defined via the configure()
     * method.
     */
    @Test
    public void testEventListenerConfiguration() throws ConfigurationException
    {
        EventListenerTestImpl listener1 = new EventListenerTestImpl(null);
        EventListenerRegistrationData<ConfigurationErrorEvent> regData =
                new EventListenerRegistrationData<ConfigurationErrorEvent>(
                        ConfigurationErrorEvent.WRITE,
                        new ErrorListenerTestImpl(null));
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class)
                        .configure(new EventListenerParameters()
                                .addEventListener(ConfigurationEvent.ANY,
                                        listener1).addEventListener(regData));
        PropertiesConfiguration config = builder.getConfiguration();
        assertTrue("Configuration listener not found", config
                .getEventListeners(ConfigurationEvent.ANY).contains(listener1));
        assertTrue(
                "Error listener not found",
                config.getEventListeners(regData.getEventType()).contains(
                        regData.getListener()));
    }

    /**
     * Tests whether configuration listeners are removed from the managed
     * configuration when the builder's result object is reset.
     */
    @Test
    public void testRemoveConfigurationListenersOnReset()
            throws ConfigurationException
    {
        EventListenerTestImpl listener = new EventListenerTestImpl(null);
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class)
                        .configure(new EventListenerParameters()
                                .addEventListener(ConfigurationEvent.ANY,
                                        listener));
        PropertiesConfiguration config = builder.getConfiguration();
        builder.resetResult();
        config.addProperty("foo", "bar");
        listener.done();
    }

    /**
     * Tests whether parameters starting with a reserved prefix are filtered out
     * before result objects are initialized.
     */
    @Test
    public void testReservedParameter() throws ConfigurationException
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("throwExceptionOnMissing", Boolean.TRUE);
        params.put("config-test", "a test");
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class, params);
        PropertiesConfiguration config = builder.getConfiguration();
        assertTrue("Flag not set", config.isThrowExceptionOnMissing());
    }

    /**
     * Tests an exception during configuration initialization if the
     * allowFailOnInit flag is false.
     */
    @Test(expected = ConfigurationException.class)
    public void testInitializationErrorNotAllowed()
            throws ConfigurationException
    {
        BasicConfigurationBuilderInitFailImpl builder =
                new BasicConfigurationBuilderInitFailImpl(false);
        builder.getConfiguration();
    }

    /**
     * Tests an exception during configuration initialization if the
     * allowFailOnInit flag is true.
     */
    @Test
    public void testInitializationErrorAllowed() throws ConfigurationException
    {
        BasicConfigurationBuilderInitFailImpl builder =
                new BasicConfigurationBuilderInitFailImpl(true);
        PropertiesConfiguration config = builder.getConfiguration();
        assertTrue("Got data", config.isEmpty());
    }

    /**
     * Tests whether a configuration implementing {@code Initializable} is
     * correctly handled.
     */
    @Test
    public void testInitializableCalled() throws ConfigurationException
    {
        BasicConfigurationBuilder<InitializableConfiguration> builder =
                new BasicConfigurationBuilder<InitializableConfiguration>(
                        InitializableConfiguration.class);
        builder.configure(new BasicBuilderParameters()
                .setThrowExceptionOnMissing(true));
        InitializableConfiguration config = builder.getConfiguration();
        assertEquals("Property not correctly initialized",
                "Initialized with flag true", config.getInitProperty());
    }

    /**
     * Tests whether a configured BeanHelper is used for result creation.
     */
    @Test
    public void testBeanHelperInConfiguration() throws ConfigurationException
    {
        final Set<Class<?>> classesPassedToFactory = new HashSet<Class<?>>();
        BeanFactory factory = new DefaultBeanFactory()
        {
            @Override
            public Object createBean(BeanCreationContext bcc) throws Exception
            {
                classesPassedToFactory.add(bcc.getBeanClass());
                return super.createBean(bcc);
            }
        };
        BeanHelper helper = new BeanHelper(factory);
        BasicConfigurationBuilder<PropertiesConfiguration> builder =
                new BasicConfigurationBuilder<PropertiesConfiguration>(
                        PropertiesConfiguration.class);
        builder.configure(new BasicBuilderParameters().setBeanHelper(helper));
        PropertiesConfiguration config = builder.getConfiguration();
        assertTrue("BeanFactory was not used correctly",
                classesPassedToFactory.contains(config.getClass()));
    }

    /**
     * Tests whether a builder can be connected to a reloading controller.
     */
    @Test
    public void testConnectToReloadingController()
            throws ConfigurationException
    {
        ReloadingDetector detector =
                EasyMock.createNiceMock(ReloadingDetector.class);
        EasyMock.expect(detector.isReloadingRequired()).andReturn(Boolean.TRUE);
        EasyMock.replay(detector);
        ReloadingController controller = new ReloadingController(detector);
        BasicConfigurationBuilder<Configuration> builder =
                new BasicConfigurationBuilder<Configuration>(
                        PropertiesConfiguration.class);
        Configuration configuration = builder.getConfiguration();

        builder.connectToReloadingController(controller);
        controller.checkForReloading(null);
        assertTrue("Not in reloading state", controller.isInReloadingState());
        assertNotSame("No new configuration created", configuration,
                builder.getConfiguration());
        assertFalse("Still in reloading state", controller.isInReloadingState());
    }

    /**
     * Tries to connect to a null reloading controller.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConnectToReloadingControllerNull()
    {
        BasicConfigurationBuilder<Configuration> builder =
                new BasicConfigurationBuilder<Configuration>(
                        PropertiesConfiguration.class);
        builder.connectToReloadingController(null);
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

    /**
     * A builder test implementation which allows checking exception handling
     * when creating new configuration objects.
     */
    private static class BasicConfigurationBuilderInitFailImpl extends
            BasicConfigurationBuilder<PropertiesConfiguration>
    {
        public BasicConfigurationBuilderInitFailImpl(boolean allowFailOnInit)
        {
            super(PropertiesConfiguration.class, null, allowFailOnInit);
        }

        /**
         * {@inheritDoc} This implementation only throws an exception.
         */
        @Override
        protected void initResultInstance(PropertiesConfiguration obj)
                throws ConfigurationException
        {
            throw new ConfigurationException("Initialization test exception!");
        }
    }

    /**
     * A test configuration implementation which also implements Initializable.
     */
    public static class InitializableConfiguration extends BaseConfiguration
            implements Initializable
    {
        /** A property which is initialized if the builder works as expected. */
        private String initProperty;

        /**
         * Sets the value of the initProperty member based on other flag values.
         * This tests whether the method is called after other properties have
         * been set.
         */
        @Override
        public void initialize()
        {
            initProperty =
                    "Initialized with flag " + isThrowExceptionOnMissing();
        }

        public String getInitProperty()
        {
            return initProperty;
        }
    }
}
