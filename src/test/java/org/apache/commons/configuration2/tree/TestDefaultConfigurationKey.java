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
 *
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
        assertEquals(TESTKEY, key.toString(), "Wrong key");
    }

    /**
     * Tests appending attribute keys.
     */
    @Test
    public void testAppendAttribute() {
        key.appendAttribute("dataType");
        assertEquals(TESTATTR, key.toString(), "Attribute key not correctly appended");
    }

    /**
     * Tests constructing a complex key by chaining multiple append operations.
     */
    @Test
    public void testAppendComplexKey() {
        key.append("tables").append("table.").appendIndex(0);
        key.append("fields.").append("field").appendIndex(1);
        key.appendAttribute("dataType");
        assertEquals(TESTKEY, key.toString(), "Wrong complex key");
    }

    /**
     * Tests appending an attribute key that is already decorated-
     */
    @Test
    public void testAppendDecoratedAttributeKey() {
        key.appendAttribute(TESTATTR);
        assertEquals(TESTATTR, key.toString(), "Decorated attribute key not correctly appended");
    }

    /**
     * Tests appending keys that contain delimiters.
     */
    @Test
    public void testAppendDelimiters() {
        key.append("key..").append("test").append(".");
        key.append(".more").append("..tests");
        assertEquals("key...test.more...tests", key.toString(), "Wrong key");
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
        assertEquals("key.test.more.tests", key.toString(), "Wrong constructed key");
    }

    /**
     * Tests appending an index to a key.
     */
    @Test
    public void testAppendIndex() {
        key.append("test").appendIndex(42);
        assertEquals("test(42)", key.toString(), "Index was not correctly appended");
    }

    /**
     * Tests appending a null attribute key.
     */
    @Test
    public void testAppendNullAttributeKey() {
        key.appendAttribute(null);
        assertEquals("", key.toString(), "Null attribute key not correctly appended");
    }

    /**
     * Tests calling append with the escape flag.
     */
    @Test
    public void testAppendWithEscapeFlag() {
        key.append(".key.test.", true);
        key.append(".more").append(".tests", true);
        assertEquals("..key..test...more...tests", key.toString(), "Wrong constructed key");
    }

    /**
     * Tests iterating over an attribute key that has an index.
     */
    @Test
    public void testAttributeKeyWithIndex() {
        key.append(TESTATTR);
        key.appendIndex(0);
        assertEquals(TESTATTR + "(0)", key.toString(), "Wrong attribute key with index");

        final DefaultConfigurationKey.KeyIterator it = key.iterator();
        assertTrue(it.hasNext(), "No first element");
        it.next();
        assertTrue(it.hasIndex(), "Index not found");
        assertEquals(0, it.getIndex(), "Incorrect index");
        assertTrue(it.isAttribute(), "Attribute not found");
        assertEquals("dataType", it.currentKey(false), "Wrong plain key");
        assertEquals(TESTATTR, it.currentKey(true), "Wrong decorated key");
    }

    /**
     * Tests determining an attribute key's name.
     */
    @Test
    public void testAttributeName() {
        assertEquals("test", key.attributeName("test"), "Plain key not detected");
        assertEquals("dataType", key.attributeName(TESTATTR), "Attribute markers not stripped");
        assertNull(key.attributeName(null), "Null key not processed");
    }

    /**
     * Tests whether common key parts can be extracted.
     */
    @Test
    public void testCommonKey() {
        final DefaultConfigurationKey k1 = key(TESTKEY);
        DefaultConfigurationKey k2 = key("tables.table(0).name");
        DefaultConfigurationKey kc = k1.commonKey(k2);
        assertEquals(key("tables.table(0)"), kc, "Wrong common key (1)");
        assertEquals(kc, k2.commonKey(k1), "Not symmetric");

        k2 = key("tables.table(1).fields.field(1)");
        kc = k1.commonKey(k2);
        assertEquals(key("tables"), kc, "Wrong common key (2)");

        k2 = key("completely.different.key");
        kc = k1.commonKey(k2);
        assertEquals(0, kc.length(), "Got a common key for different keys");

        kc = k1.commonKey(key);
        assertEquals(0, kc.length(), "Got a common key for empty key");

        kc = k1.commonKey(k1);
        assertEquals(kc, k1, "Wrong result for reflexiv invocation");
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
        assertEquals(TESTATTR, key.constructAttributeKey("dataType"), "Wrong attribute key");
        assertEquals(TESTATTR, key.constructAttributeKey(TESTATTR), "Attribute key was incorrectly converted");
        assertEquals("", key.constructAttributeKey(null), "Null key could not be processed");
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
        assertEquals(".test", key.constructAttributeKey("test"), "Wrong attribute key");
        assertEquals(".test", key.constructAttributeKey(".test"), "Attribute key was incorrectly converted");
    }

    /**
     * Tests the differenceKey() method.
     */
    @Test
    public void testDifferenceKey() {
        final DefaultConfigurationKey k1 = key(TESTKEY);
        DefaultConfigurationKey k2 = key("tables.table(0).name");
        DefaultConfigurationKey kd = k1.differenceKey(k2);
        assertEquals("name", kd.toString(), "Wrong difference (1)");

        k2 = key("tables.table(1).fields.field(1)");
        kd = k1.differenceKey(k2);
        assertEquals("table(1).fields.field(1)", kd.toString(), "Wrong difference (2)");

        k2 = key("completely.different.key");
        kd = k1.differenceKey(k2);
        assertEquals(k2, kd, "Wrong difference (3)");
    }

    /**
     * Tests differenceKey() on the same object.
     */
    @Test
    public void testDifferenceKeySame() {
        final DefaultConfigurationKey k1 = key(TESTKEY);
        final DefaultConfigurationKey kd = k1.differenceKey(k1);
        assertEquals(0, kd.length(), "Got difference for same keys");
    }

    /**
     * Tests comparing configuration keys.
     */
    @Test
    public void testEquals() {
        final DefaultConfigurationKey k1 = key(TESTKEY);
        assertEquals(k1, k1, "Key not equal to itself");
        final DefaultConfigurationKey k2 = key(TESTKEY);
        assertEquals(k1, k2, "Keys are not equal");
        assertEquals(k2, k1, "Not reflexiv");
        assertEquals(k1.hashCode(), k2.hashCode(), "Hash codes not equal");
        k2.append("anotherPart");
        assertNotEquals(k1, k2, "Keys considered equal");
        assertNotEquals(k2, k1, "Keys considered equal (2)");
        assertNotEquals(null, k1, "Key equals null key");
        assertNotEquals(TESTKEY, k1, "Equal with string");
    }

    /**
     * Tests the isAttributeKey() method with several keys.
     */
    @Test
    public void testIsAttributeKey() {
        assertTrue(key.isAttributeKey(TESTATTR), "Attribute key not detected");
        assertFalse(key.isAttributeKey(TESTPROPS), "Property key considered as attribute");
        assertFalse(key.isAttributeKey(null), "Null key considered as attribute");
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
        assertTrue(key.isAttributeKey(DefaultExpressionEngineSymbols.DEFAULT_PROPERTY_DELIMITER + "test"), "Attribute key not detected");
        assertFalse(key.isAttributeKey(TESTATTR), "Property key considered as attribute key");
    }

    /**
     * Tests to iterate over a simple key.
     */
    @Test
    public void testIterate() {
        key.append(TESTKEY);
        final DefaultConfigurationKey.KeyIterator it = key.iterator();
        assertTrue(it.hasNext(), "No key parts");
        assertEquals("tables", it.nextKey(), "Wrong key part");
        assertEquals("table", it.nextKey(), "Wrong key part");
        assertTrue(it.hasIndex(), "No index found");
        assertEquals(0, it.getIndex(), "Wrong index");
        assertEquals("fields", it.nextKey(), "Wrong key part");
        assertFalse(it.hasIndex(), "Found an index");
        assertEquals("field", it.nextKey(true), "Wrong key part");
        assertEquals(1, it.getIndex(), "Wrong index");
        assertFalse(it.isAttribute(), "Found an attribute");
        assertEquals("field", it.currentKey(true), "Wrong current key");
        assertEquals("dataType", it.nextKey(), "Wrong key part");
        assertEquals("[@dataType]", it.currentKey(true), "Wrong decorated key part");
        assertTrue(it.isAttribute(), "Attribute not found");
        assertFalse(it.hasNext(), "Too many key parts");
        assertThrows(NoSuchElementException.class, it::next, "Could iterate over the iteration's end!");
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
        assertEquals(".my.elem", kit.nextKey(), "Wrong first part");
        assertEquals("trailing.dot.", kit.nextKey(), "Wrong second part");
        assertEquals("strange", kit.nextKey(), "Wrong third part");
        assertFalse(kit.hasNext(), "Too many parts");
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
        assertEquals("this", kit.next(), "Wrong first key part");
        assertFalse(kit.isAttribute(), "First part is an attribute");
        assertTrue(kit.isPropertyKey(), "First part is not a property key");
        assertEquals("isa", kit.next(), "Wrong second key part");
        assertFalse(kit.isAttribute(), "Second part is an attribute");
        assertTrue(kit.isPropertyKey(), "Second part is not a property key");
        assertEquals("key", kit.next(), "Wrong third key part");
        assertTrue(kit.isAttribute(), "Third part is not an attribute");
        assertTrue(kit.isPropertyKey(), "Third part is not a property key");
        assertEquals("key", kit.currentKey(true), "Wrong decorated key part");
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
        assertEquals("my.elem", kit.nextKey(), "Wrong first part");
        assertEquals("trailing.dot.", kit.nextKey(), "Wrong second part");
        assertEquals("strange", kit.nextKey(), "Wrong third part");
        assertFalse(kit.hasNext(), "Too many parts");
    }

    /**
     * Tests iterating over some funny keys.
     */
    @Test
    public void testIterateStrangeKeys() {
        key = new DefaultConfigurationKey(expressionEngine, "key.");
        DefaultConfigurationKey.KeyIterator it = key.iterator();
        assertTrue(it.hasNext(), "Too few key parts");
        assertEquals("key", it.next(), "Wrong key part");
        assertFalse(it.hasNext(), "Too many key parts");

        key = new DefaultConfigurationKey(expressionEngine, ".");
        it = key.iterator();
        assertFalse(it.hasNext(), "Simple delimiter key has more parts");

        key = new DefaultConfigurationKey(expressionEngine, "key().index()undefined(0).test");
        it = key.iterator();
        assertEquals("key()", it.next(), "Wrong first part");
        assertFalse(it.hasIndex(), "Index detected in first part");
        assertEquals("index()undefined", it.nextKey(false), "Wrong second part");
        assertTrue(it.hasIndex(), "No index detected in second part");
        assertEquals(0, it.getIndex(), "Wrong index value");
    }

    /**
     * Tests whether a key with brackets in it can be iterated over.
     */
    @Test
    public void testIterateWithBrackets() {
        key.append("directory.platform(x86).path");
        final DefaultConfigurationKey.KeyIterator kit = key.iterator();
        String part = kit.nextKey();
        assertEquals("directory", part, "Wrong part 1");
        assertFalse(kit.hasIndex(), "Has index 1");
        part = kit.nextKey();
        assertEquals("platform(x86)", part, "Wrong part 2");
        assertFalse(kit.hasIndex(), "Has index 2");
        part = kit.nextKey();
        assertEquals("path", part, "Wrong part 3");
        assertFalse(kit.hasIndex(), "Has index 3");
        assertFalse(kit.hasNext(), "Too many elements");
    }

    /**
     * Tests iterating when no escape delimiter is defined.
     */
    @Test
    public void testIterateWithoutEscapeDelimiter() {
        expressionEngine = new DefaultExpressionEngine(symbols().setEscapedDelimiter(null).create());
        key = new DefaultConfigurationKey(expressionEngine);
        key.append("..my..elem.trailing..dot...strange");
        assertEquals("my..elem.trailing..dot...strange", key.toString(), "Wrong key");
        final DefaultConfigurationKey.KeyIterator kit = key.iterator();
        final String[] parts = {"my", "elem", "trailing", "dot", "strange"};
        for (int i = 0; i < parts.length; i++) {
            assertEquals(parts[i], kit.next(), "Wrong key part " + i);
        }
        assertFalse(kit.hasNext(), "Too many parts");
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
        assertEquals(TESTPROPS.length(), key.length(), "Wrong length");
        key.appendAttribute("dataType");
        assertEquals(TESTKEY.length(), key.length(), "Wrong length");
        key.setLength(TESTPROPS.length());
        assertEquals(TESTPROPS.length(), key.length(), "Wrong length after shortening");
        assertEquals(TESTPROPS, key.toString(), "Wrong resulting key");
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
        assertEquals("test", key.trim(".test."), "Key was not trimmed");
        assertEquals("", key.trim(null), "Null key could not be processed");
        assertEquals("", key.trim(DefaultExpressionEngineSymbols.DEFAULT_PROPERTY_DELIMITER), "Delimiter could not be processed");
    }

    /**
     * Tests removing leading delimiters.
     */
    @Test
    public void testTrimLeft() {
        assertEquals("test.", key.trimLeft(".test."), "Key was not left trimmed");
        assertEquals("..test.", key.trimLeft("..test."), "Too much left trimming");
    }

    /**
     * Tests removing trailing delimiters.
     */
    @Test
    public void testTrimRight() {
        assertEquals(".test", key.trimRight(".test."), "Key was not right trimmed");
        assertEquals(".test..", key.trimRight(".test.."), "Too much right trimming");
    }
}
