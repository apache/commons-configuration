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
package org.apache.commons.configuration2.builder.combined;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils2.DynaClass;
import org.apache.commons.beanutils2.LazyDynaBean;
import org.apache.commons.beanutils2.PropertyUtils;
import org.apache.commons.configuration2.builder.BasicBuilderParameters;
import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code MultiWrapDynaBean} and {@code MultiWrapDynaClass}.
 */
public class TestMultiWrapDynaBean {
    /**
     * A simple test bean class used as wrapped bean.
     */
    public static class WrappedBeanTestImpl {
        /** Stores mapped properties. */
        private final Map<String, String> mapProperties = new HashMap<>();

        /** Stores indexed properties. */
        private final int[] indexedValues = new int[8];

        /** A simple property. */
        private String text;

        public int getIndexedProperty(final int idx) {
            return indexedValues[idx];
        }

        public String getMapProperty(final String key) {
            return mapProperties.get(key);
        }

        public String getText() {
            return text;
        }

        public void setIndexedProperty(final int idx, final int value) {
            indexedValues[idx] = value;
        }

        public void setMapProperty(final String key, final String value) {
            mapProperties.put(key, value);
        }

        public void setText(final String text) {
            this.text = text;
        }
    }

    /** Constant for a mapped property. */
    private static final String MAPPED_PROPERTY = "testMappedProperty";

    /** A test wrapped bean. */
    private BasicBuilderParameters params;

    /** Another test wrapped bean. */
    private WrappedBeanTestImpl wrapBean;

    /** A wrapped DynaBean. */
    private LazyDynaBean wrapDynaBean;

    /**
     * Creates a new test object with a list of wrapped beans.
     *
     * @param withDynaBean a flag whether also a DynaBean should be added to the wrapped beans
     * @return the test bean
     */
    private MultiWrapDynaBean createBean(final boolean withDynaBean) {
        params = new BasicBuilderParameters();
        wrapBean = new WrappedBeanTestImpl();
        final Collection<Object> beans = new ArrayList<>();
        beans.add(params);
        beans.add(wrapBean);
        if (withDynaBean) {
            wrapDynaBean = new LazyDynaBean();
            wrapDynaBean.set(MAPPED_PROPERTY, "someKey", "somValue");
            beans.add(wrapDynaBean);
        }
        return new MultiWrapDynaBean(beans);
    }

    /**
     * Tests the contains() implementation. This operation is not available.
     */
    @Test
    public void testContains() {
        final MultiWrapDynaBean bean = createBean(false);
        assertThrows(UnsupportedOperationException.class, () -> bean.contains(MAPPED_PROPERTY, "someKey"));
    }

    /**
     * Tests whether the class of bean can be queried.
     */
    @Test
    public void testGetDynaClass() {
        final DynaClass cls = createBean(false).getDynaClass();
        assertNotNull(cls.getDynaProperty("throwExceptionOnMissing"));
        assertNotNull(cls.getDynaProperty("text"));
    }

    /**
     * Checks the name of the DynaClass.
     */
    @Test
    public void testGetDynaClassName() {
        assertNull(createBean(false).getDynaClass().getName());
    }

    /**
     * Tries to create a new instance of the DynaClass. This is not possible.
     */
    @Test
    public void testGetDynaClassNewInstance() {
        final DynaClass dynaClass = createBean(false).getDynaClass();
        assertThrows(UnsupportedOperationException.class, dynaClass::newInstance);
    }

    /**
     * Tests whether an indexed property can be read.
     */
    @Test
    public void testGetIndexedProperty() throws Exception {
        final MultiWrapDynaBean bean = createBean(false);
        wrapBean.setIndexedProperty(3, 20121117);
        assertEquals(20121117, PropertyUtils.getIndexedProperty(bean, "indexedProperty", 3));
    }

    /**
     * Tests whether a map property can be read.
     */
    @Test
    public void testGetMappedProperty() throws Exception {
        final MultiWrapDynaBean bean = createBean(true);
        final String key = "testKey";
        final String value = "Hello World";
        wrapDynaBean.set(MAPPED_PROPERTY, key, value);
        assertEquals(value, PropertyUtils.getMappedProperty(bean, MAPPED_PROPERTY, key));
    }

    /**
     * Tries to access an unknown property.
     */
    @Test
    public void testGetPropertyUnknown() {
        final MultiWrapDynaBean bean = createBean(false);
        assertThrows(IllegalArgumentException.class, () -> bean.get("unknown property"));
    }

    /**
     * Tests whether a simple property can be read.
     */
    @Test
    public void testGetSimpleProperty() throws Exception {
        final MultiWrapDynaBean bean = createBean(false);
        final String text = "testText";
        wrapBean.setText(text);
        assertEquals(text, PropertyUtils.getProperty(bean, "text"));
    }

    /**
     * Tests that the order of properties is relevant when adding beans to a MultiWrapDynaBean.
     */
    @Test
    public void testOrderOfProperties() throws Exception {
        final Collection<Object> beans = new ArrayList<>();
        params = new BasicBuilderParameters();
        beans.add(params);
        beans.add(new FileBasedBuilderParametersImpl());
        for (int i = 0; i < 32; i++) {
            beans.add(new BasicBuilderParameters());
        }
        final MultiWrapDynaBean bean = new MultiWrapDynaBean(beans);
        final ListDelimiterHandler listHandler = new DefaultListDelimiterHandler('+');
        PropertyUtils.setProperty(bean, "throwExceptionOnMissing", Boolean.TRUE);
        PropertyUtils.setProperty(bean, "listDelimiterHandler", listHandler);
        final Map<String, Object> map = params.getParameters();
        assertEquals(Boolean.TRUE, map.get("throwExceptionOnMissing"));
        assertEquals(listHandler, map.get("listDelimiterHandler"));
    }

    /**
     * Tests the remove() implementation. This operation is not available.
     */
    @Test
    public void testRemove() {
        final MultiWrapDynaBean bean = createBean(false);
        assertThrows(UnsupportedOperationException.class, () -> bean.remove(MAPPED_PROPERTY, "someKey"));
    }

    /**
     * Tests whether an indexed property can be set.
     */
    @Test
    public void testSetIndexedProperty() throws Exception {
        PropertyUtils.setIndexedProperty(createBean(false), "indexedProperty", 1, 42);
        assertEquals(42, wrapBean.getIndexedProperty(1));
    }

    /**
     * Tests whether a map property can be set.
     */
    @Test
    public void testSetMappedProperty() throws Exception {
        final MultiWrapDynaBean bean = createBean(true);
        final String key = "testKey";
        final String text = "Hello World";
        PropertyUtils.setMappedProperty(bean, MAPPED_PROPERTY, key, text);
        assertEquals(text, wrapDynaBean.get(MAPPED_PROPERTY, key));
    }

    /**
     * Tests whether a simple property can be set.
     */
    @Test
    public void testSetSimpleProperty() throws Exception {
        PropertyUtils.setProperty(createBean(false), "throwExceptionOnMissing", Boolean.TRUE);
        assertEquals(Boolean.TRUE, params.getParameters().get("throwExceptionOnMissing"));
    }
}
