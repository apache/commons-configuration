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
package org.apache.commons.configuration2.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for DefaultConfigurationKey.
 */
public class TestDefaultConfigurationKey {
    /** Constant for a test key. */
    private static final String TESTPROPS = "tables.table(0).fields.field(1)";

    /** Constant for a test attribute key. */
    private static final String TESTATTR = "[@dataType]";

    /** Constant for a complex attribute key. */
    private static final String TESTKEY = TESTPROPS + TESTATTR;

    /** Stores the expression engine of the key to test. */
    private DefaultExpressionEngine expressionEngine;

    /** Stores the object to be tested. */
    private DefaultConfigurationKey key;

    /**
     * Helper method to create a key instance with the given content.
     *
     * @param k the key for initialization
     * @return the newly created {@code DefaultConfigurationKey} instance
     */
    private DefaultConfigurationKey key(final String k) {
        return new DefaultConfigurationKey(expressionEngine, k);
    }

    @BeforeEach
    public void setUp() throws Exception {
        expressionEngine = DefaultExpressionEngine.INSTANCE;
        key = new DefaultConfigurationKey(expressionEngine);
    }

    /**
     * Returns a builder for symbols with default property settings.
     *
     * @return the initialized builder object
     */
    private DefaultExpressionEngineSymbols.Builder symbols() {
        return new DefaultExpressionEngineSymbols.Builder(expressionEngine.getSymbols());
    }

    /**
     * Tests appending keys.
     */
    @Test
    public void testAppend() {
        key.append("tables").append("table(0).");
        key.append("fields.").append("field(1)");
        key.append(null).append(TESTATTR);
        assertEquals(TESTKEY, key.toString());
    }

    /**
     * Tests appending attribute keys.
     */
    @Test
    public void testAppendAttribute() {
        key.appendAttribute("dataType");
        assertEquals(TESTATTR, key.toString());
    }

    /**
     * Tests constructing a complex key by chaining multiple append operations.
     */
    @Test
    public void testAppendComplexKey() {
        key.append("tables").append("table.").appendIndex(0);
        key.append("fields.").append("field").appendIndex(1);
        key.appendAttribute("dataType");
        assertEquals(TESTKEY, key.toString());
    }

    /**
     * Tests appending an attribute key that is already decorated-
     */
    @Test
    public void testAppendDecoratedAttributeKey() {
        key.appendAttribute(TESTATTR);
        assertEquals(TESTATTR, key.toString());
    }

    /**
     * Tests appending keys that contain delimiters.
     */
    @Test
    public void testAppendDelimiters() {
        key.append("key..").append("test").append(".");
        key.append(".more").append("..tests");
        assertEquals("key...test.more...tests", key.toString());
    }

    /**
     * Tests appending keys that contain delimiters when no escaped delimiter is defined.
     */
    @Test
    public void testAppendDelimitersWithoutEscaping() {
        expressionEngine = new DefaultExpressionEngine(symbols().setEscapedDelimiter(null).create());
        key = new DefaultConfigurationKey(expressionEngine);
        key.append("key.......").append("test").append(".");
        key.append(".more").append("..tests");
        assertEquals("key.test.more.tests", key.toString());
    }

    /**
     * Tests appending an index to a key.
     */
    @Test
    public void testAppendIndex() {
        key.append("test").appendIndex(42);
        assertEquals("test(42)", key.toString());
    }

    /**
     * Tests appending a null attribute key.
     */
    @Test
    public void testAppendNullAttributeKey() {
        key.appendAttribute(null);
        assertEquals("", key.toString());
    }

    /**
     * Tests calling append with the escape flag.
     */
    @Test
    public void testAppendWithEscapeFlag() {
        key.append(".key.test.", true);
        key.append(".more").append(".tests", true);
        assertEquals("..key..test...more...tests", key.toString());
    }

    /**
     * Tests iterating over an attribute key that has an index.
     */
    @Test
    public void testAttributeKeyWithIndex() {
        key.append(TESTATTR);
        key.appendIndex(0);
        assertEquals(TESTATTR + "(0)", key.toString());

        final DefaultConfigurationKey.KeyIterator it = key.iterator();
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasIndex());
        assertEquals(0, it.getIndex());
        assertTrue(it.isAttribute());
        assertEquals("dataType", it.currentKey(false));
        assertEquals(TESTATTR, it.currentKey(true));
    }

    /**
     * Tests determining an attribute key's name.
     */
    @Test
    public void testAttributeName() {
        assertEquals("test", key.attributeName("test"));
        assertEquals("dataType", key.attributeName(TESTATTR));
        assertNull(key.attributeName(null));
    }

    /**
     * Tests whether common key parts can be extracted.
     */
    @Test
    public void testCommonKey() {
        final DefaultConfigurationKey k1 = key(TESTKEY);
        DefaultConfigurationKey k2 = key("tables.table(0).name");
        DefaultConfigurationKey kc = k1.commonKey(k2);
        assertEquals(key("tables.table(0)"), kc);
        assertEquals(kc, k2.commonKey(k1));

        k2 = key("tables.table(1).fields.field(1)");
        kc = k1.commonKey(k2);
        assertEquals(key("tables"), kc);

        k2 = key("completely.different.key");
        kc = k1.commonKey(k2);
        assertEquals(0, kc.length());

        kc = k1.commonKey(key);
        assertEquals(0, kc.length());

        kc = k1.commonKey(k1);
        assertEquals(kc, k1);
    }

    /**
     * Tries to call commonKey() with null input.
     */
    @Test
    public void testCommonKeyNull() {
        assertThrows(IllegalArgumentException.class, () -> key.commonKey(null));
    }

    /**
     * Tests constructing keys for attributes.
     */
    @Test
    public void testConstructAttributeKey() {
        assertEquals(TESTATTR, key.constructAttributeKey("dataType"));
        assertEquals(TESTATTR, key.constructAttributeKey(TESTATTR));
        assertEquals("", key.constructAttributeKey(null));
    }

    /**
     * Tests constructing attribute keys when no end markers are defined. In this test case we use the property delimiter as
     * attribute prefix.
     */
    @Test
    public void testConstructAttributeKeyWithoutEndMarkers() {
        final DefaultExpressionEngineSymbols symbols = symbols().setAttributeEnd(null).setAttributeStart(expressionEngine.getSymbols().getPropertyDelimiter())
            .create();
        expressionEngine = new DefaultExpressionEngine(symbols);
        key = new DefaultConfigurationKey(expressionEngine);
        assertEquals(".test", key.constructAttributeKey("test"));
        assertEquals(".test", key.constructAttributeKey(".test"));
    }

    /**
     * Tests the differenceKey() method.
     */
    @Test
    public void testDifferenceKey() {
        final DefaultConfigurationKey k1 = key(TESTKEY);
        DefaultConfigurationKey k2 = key("tables.table(0).name");
        DefaultConfigurationKey kd = k1.differenceKey(k2);
        assertEquals("name", kd.toString());

        k2 = key("tables.table(1).fields.field(1)");
        kd = k1.differenceKey(k2);
        assertEquals("table(1).fields.field(1)", kd.toString());

        k2 = key("completely.different.key");
        kd = k1.differenceKey(k2);
        assertEquals(k2, kd);
    }

    /**
     * Tests differenceKey() on the same object.
     */
    @Test
    public void testDifferenceKeySame() {
        final DefaultConfigurationKey k1 = key(TESTKEY);
        final DefaultConfigurationKey kd = k1.differenceKey(k1);
        assertEquals(0, kd.length());
    }

    /**
     * Tests comparing configuration keys.
     */
    @Test
    public void testEquals() {
        final DefaultConfigurationKey k1 = key(TESTKEY);
        assertEquals(k1, k1);
        final DefaultConfigurationKey k2 = key(TESTKEY);
        assertEquals(k1, k2);
        assertEquals(k2, k1);
        assertEquals(k1.hashCode(), k2.hashCode());
        k2.append("anotherPart");
        assertNotEquals(k1, k2);
        assertNotEquals(k2, k1);
        assertNotEquals(null, k1);
        assertNotEquals(TESTKEY, k1);
    }

    /**
     * Tests the isAttributeKey() method with several keys.
     */
    @Test
    public void testIsAttributeKey() {
        assertTrue(key.isAttributeKey(TESTATTR));
        assertFalse(key.isAttributeKey(TESTPROPS));
        assertFalse(key.isAttributeKey(null));
    }

    /**
     * Tests if attribute keys are correctly detected if no end markers are set. (In this test case we use the same
     * delimiter for attributes as for simple properties.)
     */
    @Test
    public void testIsAttributeKeyWithoutEndMarkers() {
        final DefaultExpressionEngineSymbols symbols = symbols().setAttributeEnd(null)
            .setAttributeStart(DefaultExpressionEngineSymbols.DEFAULT_PROPERTY_DELIMITER).create();
        expressionEngine = new DefaultExpressionEngine(symbols);
        key = new DefaultConfigurationKey(expressionEngine);
        assertTrue(key.isAttributeKey(DefaultExpressionEngineSymbols.DEFAULT_PROPERTY_DELIMITER + "test"));
        assertFalse(key.isAttributeKey(TESTATTR));
    }

    /**
     * Tests to iterate over a simple key.
     */
    @Test
    public void testIterate() {
        key.append(TESTKEY);
        final DefaultConfigurationKey.KeyIterator it = key.iterator();
        assertTrue(it.hasNext());
        assertEquals("tables", it.nextKey());
        assertEquals("table", it.nextKey());
        assertTrue(it.hasIndex());
        assertEquals(0, it.getIndex());
        assertEquals("fields", it.nextKey());
        assertFalse(it.hasIndex());
        assertEquals("field", it.nextKey(true));
        assertEquals(1, it.getIndex());
        assertFalse(it.isAttribute());
        assertEquals("field", it.currentKey(true));
        assertEquals("dataType", it.nextKey());
        assertEquals("[@dataType]", it.currentKey(true));
        assertTrue(it.isAttribute());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    /**
     * Tests iterating over keys when a different escaped delimiter is used.
     */
    @Test
    public void testIterateAlternativeEscapeDelimiter() {
        expressionEngine = new DefaultExpressionEngine(symbols().setEscapedDelimiter("\\.").create());
        key = new DefaultConfigurationKey(expressionEngine);
        key.append("\\.my\\.elem");
        key.append("trailing\\.dot\\.");
        key.append(".strange");
        assertEquals("\\.my\\.elem.trailing\\.dot\\..strange", key.toString());
        final DefaultConfigurationKey.KeyIterator kit = key.iterator();
        assertEquals(".my.elem", kit.nextKey());
        assertEquals("trailing.dot.", kit.nextKey());
        assertEquals("strange", kit.nextKey());
        assertFalse(kit.hasNext());
    }

    /**
     * Tests iteration when the attribute markers equals the property delimiter.
     */
    @Test
    public void testIterateAttributeEqualsPropertyDelimiter() {
        expressionEngine = new DefaultExpressionEngine(
            symbols().setAttributeEnd(null).setAttributeStart(DefaultExpressionEngineSymbols.DEFAULT_PROPERTY_DELIMITER).create());
        key = new DefaultConfigurationKey(expressionEngine);
        key.append("this.isa.key");
        final DefaultConfigurationKey.KeyIterator kit = key.iterator();
        assertEquals("this", kit.next());
        assertFalse(kit.isAttribute());
        assertTrue(kit.isPropertyKey());
        assertEquals("isa", kit.next());
        assertFalse(kit.isAttribute());
        assertTrue(kit.isPropertyKey());
        assertEquals("key", kit.next());
        assertTrue(kit.isAttribute());
        assertTrue(kit.isPropertyKey());
        assertEquals("key", kit.currentKey(true));
    }

    /**
     * Tests iterating over keys with escaped delimiters.
     */
    @Test
    public void testIterateEscapedDelimiters() {
        key.append("my..elem");
        key.append("trailing..dot..");
        key.append(".strange");
        assertEquals("my..elem.trailing..dot...strange", key.toString());
        final DefaultConfigurationKey.KeyIterator kit = key.iterator();
        assertEquals("my.elem", kit.nextKey());
        assertEquals("trailing.dot.", kit.nextKey());
        assertEquals("strange", kit.nextKey());
        assertFalse(kit.hasNext());
    }

    /**
     * Tests iterating over some funny keys.
     */
    @Test
    public void testIterateStrangeKeys() {
        key = new DefaultConfigurationKey(expressionEngine, "key.");
        DefaultConfigurationKey.KeyIterator it = key.iterator();
        assertTrue(it.hasNext());
        assertEquals("key", it.next());
        assertFalse(it.hasNext());

        key = new DefaultConfigurationKey(expressionEngine, ".");
        it = key.iterator();
        assertFalse(it.hasNext());

        key = new DefaultConfigurationKey(expressionEngine, "key().index()undefined(0).test");
        it = key.iterator();
        assertEquals("key()", it.next());
        assertFalse(it.hasIndex());
        assertEquals("index()undefined", it.nextKey(false));
        assertTrue(it.hasIndex());
        assertEquals(0, it.getIndex());
    }

    /**
     * Tests whether a key with brackets in it can be iterated over.
     */
    @Test
    public void testIterateWithBrackets() {
        key.append("directory.platform(x86).path");
        final DefaultConfigurationKey.KeyIterator kit = key.iterator();
        String part = kit.nextKey();
        assertEquals("directory", part);
        assertFalse(kit.hasIndex());
        part = kit.nextKey();
        assertEquals("platform(x86)", part);
        assertFalse(kit.hasIndex());
        part = kit.nextKey();
        assertEquals("path", part);
        assertFalse(kit.hasIndex());
        assertFalse(kit.hasNext());
    }

    /**
     * Tests iterating when no escape delimiter is defined.
     */
    @Test
    public void testIterateWithoutEscapeDelimiter() {
        expressionEngine = new DefaultExpressionEngine(symbols().setEscapedDelimiter(null).create());
        key = new DefaultConfigurationKey(expressionEngine);
        key.append("..my..elem.trailing..dot...strange");
        assertEquals("my..elem.trailing..dot...strange", key.toString());
        final DefaultConfigurationKey.KeyIterator kit = key.iterator();
        final String[] parts = {"my", "elem", "trailing", "dot", "strange"};
        for (int i = 0; i < parts.length; i++) {
            assertEquals(parts[i], kit.next(), "Wrong key part " + i);
        }
        assertFalse(kit.hasNext());
    }

    /**
     * Tests an iteration where the remove() method is called. This is not supported.
     */
    @Test
    public void testIterateWithRemove() {
        assertFalse(key.iterator().hasNext());
        key.append("simple");
        final DefaultConfigurationKey.KeyIterator it = key.iterator();
        assertTrue(it.hasNext());
        assertEquals("simple", it.next());
        assertThrows(UnsupportedOperationException.class, it::remove);
    }

    /**
     * Tests getting and setting the key's length.
     */
    @Test
    public void testLength() {
        key.append(TESTPROPS);
        assertEquals(TESTPROPS.length(), key.length());
        key.appendAttribute("dataType");
        assertEquals(TESTKEY.length(), key.length());
        key.setLength(TESTPROPS.length());
        assertEquals(TESTPROPS.length(), key.length());
        assertEquals(TESTPROPS, key.toString());
    }

    /**
     * Tests setting the expression engine to null. This should not be allowed.
     */
    @Test
    public void testSetNullExpressionEngine() {
        assertThrows(IllegalArgumentException.class, () -> new DefaultConfigurationKey(null));
    }

    /**
     * Tests removing delimiters.
     */
    @Test
    public void testTrim() {
        assertEquals("test", key.trim(".test."));
        assertEquals("", key.trim(null));
        assertEquals("", key.trim(DefaultExpressionEngineSymbols.DEFAULT_PROPERTY_DELIMITER));
    }

    /**
     * Tests removing leading delimiters.
     */
    @Test
    public void testTrimLeft() {
        assertEquals("test.", key.trimLeft(".test."));
        assertEquals("..test.", key.trimLeft("..test."));
    }

    /**
     * Tests removing trailing delimiters.
     */
    @Test
    public void testTrimRight() {
        assertEquals(".test", key.trimRight(".test."));
        assertEquals(".test..", key.trimRight(".test.."));
    }
}
