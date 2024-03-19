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

package org.apache.commons.configuration2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.builder.XMLBuilderParametersImpl;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.event.ConfigurationErrorEvent;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventSource;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.sync.NoOpSynchronizer;
import org.apache.commons.configuration2.tree.DefaultExpressionEngine;
import org.apache.commons.configuration2.tree.DefaultExpressionEngineSymbols;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the ConfigurationUtils class
 */
public class TestConfigurationUtils {
    /**
     * A test Synchronizer implementation which can be cloned.
     */
    private static final class CloneableSynchronizer extends NonCloneableSynchronizer implements Cloneable {
        /** A flag whether clone() was called. */
        private final boolean cloned;

        /**
         * Creates a new instance of {@code CloneableSynchronizer} and sets the clone flag.
         *
         * @param clone the clone flag
         */
        public CloneableSynchronizer(final boolean clone) {
            cloned = clone;
        }

        @Override
        public Object clone() {
            return new CloneableSynchronizer(true);
        }

        /**
         * Returns a flag whether this object was cloned.
         *
         * @return the clone flag
         */
        public boolean isCloned() {
            return cloned;
        }
    }

    /**
     * A test Synchronizer implementation which cannot be cloned.
     */
    private static class NonCloneableSynchronizer extends SynchronizerTestImpl {
    }

    /** Constant for the name of a class to be loaded. */
    private static final String CLS_NAME = "org.apache.commons.configuration2.PropertiesConfiguration";

    /** Stores the CCL. */
    private ClassLoader ccl;

    @BeforeEach
    public void setUp() throws Exception {
        ccl = Thread.currentThread().getContextClassLoader();
    }

    @AfterEach
    public void tearDown() throws Exception {
        Thread.currentThread().setContextClassLoader(ccl);
    }

    @Test
    public void testAppend() {
        // create the source configuration
        final Configuration conf1 = new BaseConfiguration();
        conf1.addProperty("key1", "value1");
        conf1.addProperty("key2", "value2");

        // create the target configuration
        final Configuration conf2 = new BaseConfiguration();
        conf2.addProperty("key1", "value3");
        conf2.addProperty("key2", "value4");

        // append the source configuration to the target configuration
        ConfigurationUtils.append(conf1, conf2);

        List<Object> expected = new ArrayList<>();
        expected.add("value3");
        expected.add("value1");
        assertEquals(expected, conf2.getList("key1"));

        expected = new ArrayList<>();
        expected.add("value4");
        expected.add("value2");
        assertEquals(expected, conf2.getList("key2"));
    }

    /**
     * Tests asEventSource() if an exception is expected.
     */
    @Test
    public void testAsEventSourceNonSupportedEx() {
        assertThrows(ConfigurationRuntimeException.class, () -> ConfigurationUtils.asEventSource(this, false));
    }

    /**
     * Tests asEventSource() if the passed in object implements this interface.
     */
    @Test
    public void testAsEventSourceSupported() {
        final XMLConfiguration src = new XMLConfiguration();
        assertSame(src, ConfigurationUtils.asEventSource(src, true));
    }

    /**
     * Tests asEventSource() if a mock object has to be returned.
     */
    @Test
    public void testAsEventSourceUnsupportedMock() {
        @SuppressWarnings("unchecked")
        final EventListener<ConfigurationEvent> cl = mock(EventListener.class);
        final EventSource source = ConfigurationUtils.asEventSource(this, true);
        source.addEventListener(ConfigurationEvent.ANY, cl);
        assertFalse(source.removeEventListener(ConfigurationEvent.ANY, cl));
        source.addEventListener(ConfigurationEvent.ANY, null);
    }

    /**
     * Tests cloning a configuration that supports this operation.
     */
    @Test
    public void testCloneConfiguration() {
        final BaseHierarchicalConfiguration conf = new BaseHierarchicalConfiguration();
        conf.addProperty("test", "yes");
        final BaseHierarchicalConfiguration copy = (BaseHierarchicalConfiguration) ConfigurationUtils.cloneConfiguration(conf);
        assertNotSame(conf, copy);
        assertEquals("yes", copy.getString("test"));
    }

    /**
     * Tests cloning a configuration that does not support this operation. This should cause an exception.
     */
    @Test
    public void testCloneConfigurationNotSupported() {
        final Configuration myNonCloneableConfig = new NonCloneableConfiguration();
        assertThrows(ConfigurationRuntimeException.class, () -> ConfigurationUtils.cloneConfiguration(myNonCloneableConfig));
    }

    /**
     * Tests cloning a <b>null</b> configuration.
     */
    @Test
    public void testCloneConfigurationNull() {
        assertNull(ConfigurationUtils.cloneConfiguration(null));
    }

    /**
     * Tests whether errors are handled correctly by cloneIfPossible().
     */
    @Test
    public void testCloneIfPossibleError() {
        final XMLBuilderParametersImpl params = new XMLBuilderParametersImpl() {
            @Override
            public XMLBuilderParametersImpl clone() {
                throw new ConfigurationRuntimeException();
            }
        };
        assertSame(params, ConfigurationUtils.cloneIfPossible(params));
    }

    /**
     * Tests cloneIfPossible() if the passed in object does not support cloning.
     */
    @Test
    public void testCloneIfPossibleNotSupported() {
        final Long value = 20130116221714L;
        assertSame(value, ConfigurationUtils.cloneIfPossible(value));
    }

    /**
     * Tests whether cloneIfPossible() can handle null parameters.
     */
    @Test
    public void testCloneIfPossibleNull() {
        assertNull(ConfigurationUtils.cloneIfPossible(null));
    }

    /**
     * Tests whether an object can be cloned which supports cloning.
     */
    @Test
    public void testCloneIfPossibleSupported() {
        final XMLBuilderParametersImpl params = new XMLBuilderParametersImpl();
        params.setPublicID("testID");
        params.setSchemaValidation(true);
        final XMLBuilderParametersImpl clone = (XMLBuilderParametersImpl) ConfigurationUtils.cloneIfPossible(params);
        assertNotSame(params, clone);
        final Map<String, Object> map = clone.getParameters();
        for (final Map.Entry<String, Object> e : params.getParameters().entrySet()) {
            if (!e.getKey().startsWith("config-")) {
                assertEquals(e.getValue(), map.get(e.getKey()), "Wrong value for field " + e.getKey());
            }
        }
    }

    /**
     * Tests whether a Synchronizer can be cloned using its clone() method.
     */
    @Test
    public void testCloneSynchronizerClone() {
        final CloneableSynchronizer sync = new CloneableSynchronizer(false);
        final CloneableSynchronizer sync2 = (CloneableSynchronizer) ConfigurationUtils.cloneSynchronizer(sync);
        assertTrue(sync2.isCloned());
    }

    /**
     * Tests cloneSynchronizer() if the argument cannot be cloned.
     */
    @Test
    public void testCloneSynchronizerFailed() {
        final NonCloneableSynchronizer synchronizer = new NonCloneableSynchronizer();
        assertThrows(ConfigurationRuntimeException.class, () -> ConfigurationUtils.cloneSynchronizer(synchronizer));
    }

    /**
     * Tests whether a new Synchronizer can be created using reflection.
     */
    @Test
    public void testCloneSynchronizerNewInstance() {
        final SynchronizerTestImpl sync = new SynchronizerTestImpl();
        final SynchronizerTestImpl sync2 = (SynchronizerTestImpl) ConfigurationUtils.cloneSynchronizer(sync);
        assertNotNull(sync2);
        assertNotSame(sync, sync2);
    }

    /**
     * Tests whether the NoOpSyhnchronizer can be cloned.
     */
    @Test
    public void testCloneSynchronizerNoOp() {
        assertSame(NoOpSynchronizer.INSTANCE, ConfigurationUtils.cloneSynchronizer(NoOpSynchronizer.INSTANCE));
    }

    /**
     * Tries to clone a null Synchronizer.
     */
    @Test
    public void testCloneSynchronizerNull() {
        assertThrows(IllegalArgumentException.class, () -> ConfigurationUtils.cloneSynchronizer(null));
    }

    /**
     * Tests converting a configuration into a hierarchical one that is already hierarchical.
     */
    @Test
    public void testConvertHierarchicalToHierarchical() {
        final Configuration conf = new BaseHierarchicalConfiguration();
        conf.addProperty("test", "yes");
        assertSame(conf, ConfigurationUtils.convertToHierarchical(conf));
    }

    /**
     * Tests converting an already hierarchical configuration using an expression engine. The new engine should be set.
     */
    @Test
    public void testConvertHierarchicalToHierarchicalEngine() {
        final BaseHierarchicalConfiguration hc = new BaseHierarchicalConfiguration();
        final ExpressionEngine engine = new DefaultExpressionEngine(DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS);
        assertSame(hc, ConfigurationUtils.convertToHierarchical(hc, engine));
        assertSame(engine, hc.getExpressionEngine());
    }

    /**
     * Tests converting an already hierarchical configuration using a null expression engine. In this case the expression
     * engine of the configuration should not be touched.
     */
    @Test
    public void testConvertHierarchicalToHierarchicalNullEngine() {
        final BaseHierarchicalConfiguration hc = new BaseHierarchicalConfiguration();
        final ExpressionEngine engine = new DefaultExpressionEngine(DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS);
        hc.setExpressionEngine(engine);
        assertSame(hc, ConfigurationUtils.convertToHierarchical(hc, null));
        assertSame(engine, hc.getExpressionEngine());
    }

    /**
     * Tests converting a null configuration to a hierarchical one. The result should be null, too.
     */
    @Test
    public void testConvertNullToHierarchical() {
        assertNull(ConfigurationUtils.convertToHierarchical(null));
    }

    /**
     * Tests converting a configuration into a hierarchical one.
     */
    @Test
    public void testConvertToHierarchical() {
        final Configuration conf = new BaseConfiguration();
        for (int i = 0; i < 10; i++) {
            conf.addProperty("test" + i, "value" + i);
            conf.addProperty("test.list", "item" + i);
        }

        final BaseHierarchicalConfiguration hc = (BaseHierarchicalConfiguration) ConfigurationUtils.convertToHierarchical(conf);
        for (final Iterator<String> it = conf.getKeys(); it.hasNext();) {
            final String key = it.next();
            assertEquals(conf.getProperty(key), hc.getProperty(key), "Wrong value for key " + key);
        }
    }

    /**
     * Tests converting a configuration into a hierarchical one if some of its properties contain escaped list delimiter
     * characters.
     */
    @Test
    public void testConvertToHierarchicalDelimiters() {
        final BaseConfiguration conf = new BaseConfiguration();
        conf.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        conf.addProperty("test.key", "1\\,2\\,3");
        assertEquals("1,2,3", conf.getString("test.key"));
        final HierarchicalConfiguration<?> hc = ConfigurationUtils.convertToHierarchical(conf);
        assertEquals("1,2,3", hc.getString("test.key"));
    }

    /**
     * Tests converting a configuration to a hierarchical one using a specific expression engine.
     */
    @Test
    public void testConvertToHierarchicalEngine() {
        final Configuration conf = new BaseConfiguration();
        conf.addProperty("test(a)", Boolean.TRUE);
        conf.addProperty("test(b)", Boolean.FALSE);
        final DefaultExpressionEngine engine = new DefaultExpressionEngine(
            new DefaultExpressionEngineSymbols.Builder(DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS).setIndexStart("[").setIndexEnd("]").create());
        final HierarchicalConfiguration<?> hc = ConfigurationUtils.convertToHierarchical(conf, engine);
        assertTrue(hc.getBoolean("test(a)"));
        assertFalse(hc.getBoolean("test(b)"));
    }

    /**
     * Tests converting a configuration to a hierarchical one that contains a property with multiple values. This test is
     * related to CONFIGURATION-346.
     */
    @Test
    public void testConvertToHierarchicalMultiValues() {
        final BaseConfiguration config = new BaseConfiguration();
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        config.addProperty("test", "1,2,3");
        final HierarchicalConfiguration<?> hc = ConfigurationUtils.convertToHierarchical(config);
        assertEquals(1, hc.getInt("test(0)"));
        assertEquals(2, hc.getInt("test(1)"));
        assertEquals(3, hc.getInt("test(2)"));
    }

    /**
     * Tests that the structure of the resulting hierarchical configuration does not depend on the order of properties in
     * the source configuration. This test is related to CONFIGURATION-604.
     */
    @Test
    public void testConvertToHierarchicalOrderOfProperties() {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.addProperty("x.y.z", true);
        config.addProperty("x.y", true);
        @SuppressWarnings("unchecked")
        final HierarchicalConfiguration<ImmutableNode> hc = (HierarchicalConfiguration<ImmutableNode>) ConfigurationUtils.convertToHierarchical(config);
        final ImmutableNode rootNode = hc.getNodeModel().getNodeHandler().getRootNode();
        final ImmutableNode nodeX = rootNode.getChildren().get(0);
        assertEquals(1, nodeX.getChildren().size());
    }

    @Test
    public void testCopy() {
        // create the source configuration
        final Configuration conf1 = new BaseConfiguration();
        conf1.addProperty("key1", "value1");
        conf1.addProperty("key2", "value2");

        // create the target configuration
        final Configuration conf2 = new BaseConfiguration();
        conf2.addProperty("key1", "value3");
        conf2.addProperty("key2", "value4");

        // copy the source configuration into the target configuration
        ConfigurationUtils.copy(conf1, conf2);

        assertEquals("value1", conf2.getProperty("key1"));
        assertEquals("value2", conf2.getProperty("key2"));
    }

    /**
     * Tests whether runtime exceptions can be enabled.
     */
    @Test
    public void testEnableRuntimeExceptions() {
        final PropertiesConfiguration config = new PropertiesConfiguration() {
            @Override
            protected void addPropertyDirect(final String key, final Object value) {
                // always simulate an exception
                fireError(ConfigurationErrorEvent.WRITE, ConfigurationEvent.ADD_PROPERTY, key, value, new RuntimeException("A faked exception!"));
            }
        };
        config.clearErrorListeners();
        ConfigurationUtils.enableRuntimeExceptions(config);
        assertThrows(ConfigurationRuntimeException.class, () -> config.addProperty("test", "testValue"));
    }

    /**
     * Tries to enable runtime exceptions for a configuration that does not inherit from EventSource. This should cause an
     * exception.
     */
    @Test
    public void testEnableRuntimeExceptionsInvalid() {
        final Configuration c = mock(Configuration.class);
        assertThrows(IllegalArgumentException.class, () -> ConfigurationUtils.enableRuntimeExceptions(c));
    }

    /**
     * Tries to enable runtime exceptions for a null configuration. This should cause an exception.
     */
    @Test
    public void testEnableRuntimeExceptionsNull() {
        assertThrows(IllegalArgumentException.class, () -> ConfigurationUtils.enableRuntimeExceptions(null));
    }

    /**
     * Tests whether a class can be loaded if it is not found by the CCL.
     */
    @Test
    public void testLoadClassCCLNotFound() throws ClassNotFoundException {
        Thread.currentThread().setContextClassLoader(new ClassLoader() {
            @Override
            public Class<?> loadClass(final String name) throws ClassNotFoundException {
                throw new ClassNotFoundException(name);
            }
        });
        assertEquals(CLS_NAME, ConfigurationUtils.loadClass(CLS_NAME).getName());
    }

    /**
     * Tests whether a class can be loaded if there is no CCL.
     */
    @Test
    public void testLoadClassCCLNull() throws ClassNotFoundException {
        Thread.currentThread().setContextClassLoader(null);
        assertEquals(CLS_NAME, ConfigurationUtils.loadClass(CLS_NAME).getName());
    }

    /**
     * Tests whether a class can be loaded from CCL.
     */
    @Test
    public void testLoadClassFromCCL() throws ClassNotFoundException {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        assertEquals(CLS_NAME, ConfigurationUtils.loadClass(CLS_NAME).getName());
    }

    /**
     * Tests loadClassNoEx() if the class can be resolved.
     */
    @Test
    public void testLoadClassNoExFound() {
        assertEquals(CLS_NAME, ConfigurationUtils.loadClassNoEx(CLS_NAME).getName());
    }

    /**
     * Tests loadClassNoEx() if the class cannot be resolved.
     */
    @Test
    public void testLoadClassNoExNotFound() {
        assertThrows(ConfigurationRuntimeException.class, () -> ConfigurationUtils.loadClassNoEx("a non existing class!"));
    }

    /**
     * Tests the behavior of loadClass() for a non-existing class.
     */
    @Test
    public void testLoadClassNotFound() {
        assertThrows(ClassNotFoundException.class, () -> ConfigurationUtils.loadClass("a non existing class!"));
    }

    @Test
    public void testToString() {
        final Configuration config = new BaseConfiguration();
        final String lineSeparator = System.lineSeparator();

        assertEquals("", ConfigurationUtils.toString(config));

        config.setProperty("one", "1");
        assertEquals("one=1", ConfigurationUtils.toString(config));

        config.setProperty("two", "2");
        assertEquals("one=1" + lineSeparator + "two=2", ConfigurationUtils.toString(config));

        config.clearProperty("one");
        assertEquals("two=2", ConfigurationUtils.toString(config));

        config.setProperty("one", "1");
        assertEquals("two=2" + lineSeparator + "one=1", ConfigurationUtils.toString(config));
    }
}
