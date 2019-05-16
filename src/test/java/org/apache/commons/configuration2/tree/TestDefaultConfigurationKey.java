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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for DefaultConfigurationKey.
 *
 * @author Oliver Heger
 */
public class TestDefaultConfigurationKey
{
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

    @Before
    public void setUp() throws Exception
    {
        expressionEngine = DefaultExpressionEngine.INSTANCE;
        key = new DefaultConfigurationKey(expressionEngine);
    }

    /**
     * Helper method to create a key instance with the given content.
     *
     * @param k the key for initialization
     * @return the newly created {@code DefaultConfigurationKey} instance
     */
    private DefaultConfigurationKey key(final String k)
    {
        return new DefaultConfigurationKey(expressionEngine, k);
    }

    /**
     * Tests setting the expression engine to null. This should not be allowed.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetNullExpressionEngine()
    {
        new DefaultConfigurationKey(null);
    }

    /**
     * Tests the isAttributeKey() method with several keys.
     */
    @Test
    public void testIsAttributeKey()
    {
        assertTrue("Attribute key not detected", key.isAttributeKey(TESTATTR));
        assertFalse("Property key considered as attribute", key
                .isAttributeKey(TESTPROPS));
        assertFalse("Null key considered as attribute", key
                .isAttributeKey(null));
    }

    /**
     * Returns a builder for symbols with default property settings.
     *
     * @return the initialized builder object
     */
    private DefaultExpressionEngineSymbols.Builder symbols()
    {
        return new DefaultExpressionEngineSymbols.Builder(
                expressionEngine.getSymbols());
    }

    /**
     * Tests if attribute keys are correctly detected if no end markers are set.
     * (In this test case we use the same delimiter for attributes as for simple
     * properties.)
     */
    @Test
    public void testIsAttributeKeyWithoutEndMarkers()
    {
        final DefaultExpressionEngineSymbols symbols =
                symbols()
                        .setAttributeEnd(null)
                        .setAttributeStart(
                                DefaultExpressionEngineSymbols.DEFAULT_PROPERTY_DELIMITER)
                        .create();
        expressionEngine = new DefaultExpressionEngine(symbols);
        key = new DefaultConfigurationKey(expressionEngine);
        assertTrue(
                "Attribute key not detected",
                key.isAttributeKey(DefaultExpressionEngineSymbols.DEFAULT_PROPERTY_DELIMITER
                        + "test"));
        assertFalse("Property key considered as attribute key",
                key.isAttributeKey(TESTATTR));
    }

    /**
     * Tests removing leading delimiters.
     */
    @Test
    public void testTrimLeft()
    {
        assertEquals("Key was not left trimmed", "test.", key
                .trimLeft(".test."));
        assertEquals("Too much left trimming", "..test.", key
                .trimLeft("..test."));
    }

    /**
     * Tests removing trailing delimiters.
     */
    @Test
    public void testTrimRight()
    {
        assertEquals("Key was not right trimmed", ".test", key
                .trimRight(".test."));
        assertEquals("Too much right trimming", ".test..", key
                .trimRight(".test.."));
    }

    /**
     * Tests removing delimiters.
     */
    @Test
    public void testTrim()
    {
        assertEquals("Key was not trimmed", "test", key.trim(".test."));
        assertEquals("Null key could not be processed", "", key.trim(null));
        assertEquals("Delimiter could not be processed", "", key
                .trim(DefaultExpressionEngineSymbols.DEFAULT_PROPERTY_DELIMITER));
    }

    /**
     * Tests appending keys.
     */
    @Test
    public void testAppend()
    {
        key.append("tables").append("table(0).");
        key.append("fields.").append("field(1)");
        key.append(null).append(TESTATTR);
        assertEquals("Wrong key", TESTKEY, key.toString());
    }

    /**
     * Tests appending keys that contain delimiters.
     */
    @Test
    public void testAppendDelimiters()
    {
        key.append("key..").append("test").append(".");
        key.append(".more").append("..tests");
        assertEquals("Wrong key", "key...test.more...tests", key.toString());
    }

    /**
     * Tests appending keys that contain delimiters when no escaped delimiter
     * is defined.
     */
    @Test
    public void testAppendDelimitersWithoutEscaping()
    {
        expressionEngine =
                new DefaultExpressionEngine(symbols().setEscapedDelimiter(null)
                        .create());
        key = new DefaultConfigurationKey(expressionEngine);
        key.append("key.......").append("test").append(".");
        key.append(".more").append("..tests");
        assertEquals("Wrong constructed key", "key.test.more.tests", key
                .toString());
    }

    /**
     * Tests calling append with the escape flag.
     */
    @Test
    public void testAppendWithEscapeFlag()
    {
        key.append(".key.test.", true);
        key.append(".more").append(".tests", true);
        assertEquals("Wrong constructed key", "..key..test...more...tests", key
                .toString());
    }

    /**
     * Tests constructing keys for attributes.
     */
    @Test
    public void testConstructAttributeKey()
    {
        assertEquals("Wrong attribute key", TESTATTR, key
                .constructAttributeKey("dataType"));
        assertEquals("Attribute key was incorrectly converted", TESTATTR, key
                .constructAttributeKey(TESTATTR));
        assertEquals("Null key could not be processed", "", key
                .constructAttributeKey(null));
    }

    /**
     * Tests constructing attribute keys when no end markers are defined. In
     * this test case we use the property delimiter as attribute prefix.
     */
    @Test
    public void testConstructAttributeKeyWithoutEndMarkers()
    {
        final DefaultExpressionEngineSymbols symbols =
                symbols()
                        .setAttributeEnd(null)
                        .setAttributeStart(
                                expressionEngine.getSymbols()
                                        .getPropertyDelimiter()).create();
        expressionEngine = new DefaultExpressionEngine(symbols);
        key = new DefaultConfigurationKey(expressionEngine);
        assertEquals("Wrong attribute key", ".test", key
                .constructAttributeKey("test"));
        assertEquals("Attribute key was incorrectly converted", ".test", key
                .constructAttributeKey(".test"));
    }

    /**
     * Tests appending attribute keys.
     */
    @Test
    public void testAppendAttribute()
    {
        key.appendAttribute("dataType");
        assertEquals("Attribute key not correctly appended", TESTATTR, key
                .toString());
    }

    /**
     * Tests appending an attribute key that is already decorated-
     */
    @Test
    public void testAppendDecoratedAttributeKey()
    {
        key.appendAttribute(TESTATTR);
        assertEquals("Decorated attribute key not correctly appended",
                TESTATTR, key.toString());
    }

    /**
     * Tests appending a null attribute key.
     */
    @Test
    public void testAppendNullAttributeKey()
    {
        key.appendAttribute(null);
        assertEquals("Null attribute key not correctly appended", "", key
                .toString());
    }

    /**
     * Tests appending an index to a key.
     */
    @Test
    public void testAppendIndex()
    {
        key.append("test").appendIndex(42);
        assertEquals("Index was not correctly appended", "test(42)", key
                .toString());
    }

    /**
     * Tests constructing a complex key by chaining multiple append operations.
     */
    @Test
    public void testAppendComplexKey()
    {
        key.append("tables").append("table.").appendIndex(0);
        key.append("fields.").append("field").appendIndex(1);
        key.appendAttribute("dataType");
        assertEquals("Wrong complex key", TESTKEY, key.toString());
    }

    /**
     * Tests getting and setting the key's length.
     */
    @Test
    public void testLength()
    {
        key.append(TESTPROPS);
        assertEquals("Wrong length", TESTPROPS.length(), key.length());
        key.appendAttribute("dataType");
        assertEquals("Wrong length", TESTKEY.length(), key.length());
        key.setLength(TESTPROPS.length());
        assertEquals("Wrong length after shortening", TESTPROPS.length(), key
                .length());
        assertEquals("Wrong resulting key", TESTPROPS, key.toString());
    }

    /**
     * Tests comparing configuration keys.
     */
    @Test
    public void testEquals()
    {
        final DefaultConfigurationKey k1 = key(TESTKEY);
        assertTrue("Key not equal to itself", k1.equals(k1));
        final DefaultConfigurationKey k2 = key(TESTKEY);
        assertTrue("Keys are not equal", k1.equals(k2));
        assertTrue("Not reflexiv", k2.equals(k1));
        assertEquals("Hash codes not equal", k1.hashCode(), k2.hashCode());
        k2.append("anotherPart");
        assertFalse("Keys considered equal", k1.equals(k2));
        assertFalse("Keys considered equal (2)", k2.equals(k1));
        assertFalse("Key equals null key", k1.equals(null));
        assertFalse("Equal with string", k1.equals(TESTKEY));
    }

    /**
     * Tests determining an attribute key's name.
     */
    @Test
    public void testAttributeName()
    {
        assertEquals("Plain key not detected", "test", key
                .attributeName("test"));
        assertEquals("Attribute markers not stripped", "dataType", key
                .attributeName(TESTATTR));
        assertNull("Null key not processed", key.attributeName(null));
    }

    /**
     * Tests to iterate over a simple key.
     */
    @Test
    public void testIterate()
    {
        key.append(TESTKEY);
        final DefaultConfigurationKey.KeyIterator it = key.iterator();
        assertTrue("No key parts", it.hasNext());
        assertEquals("Wrong key part", "tables", it.nextKey());
        assertEquals("Wrong key part", "table", it.nextKey());
        assertTrue("No index found", it.hasIndex());
        assertEquals("Wrong index", 0, it.getIndex());
        assertEquals("Wrong key part", "fields", it.nextKey());
        assertFalse("Found an index", it.hasIndex());
        assertEquals("Wrong key part", "field", it.nextKey(true));
        assertEquals("Wrong index", 1, it.getIndex());
        assertFalse("Found an attribute", it.isAttribute());
        assertEquals("Wrong current key", "field", it.currentKey(true));
        assertEquals("Wrong key part", "dataType", it.nextKey());
        assertEquals("Wrong decorated key part", "[@dataType]", it
                .currentKey(true));
        assertTrue("Attribute not found", it.isAttribute());
        assertFalse("Too many key parts", it.hasNext());
        try
        {
            it.next();
            fail("Could iterate over the iteration's end!");
        }
        catch (final NoSuchElementException nex)
        {
            // ok
        }
    }

    /**
     * Tests an iteration where the remove() method is called. This is not
     * supported.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testIterateWithRemove()
    {
        assertFalse(key.iterator().hasNext());
        key.append("simple");
        final DefaultConfigurationKey.KeyIterator it = key.iterator();
        assertTrue(it.hasNext());
        assertEquals("simple", it.next());
        it.remove();
    }

    /**
     * Tests iterating over some funny keys.
     */
    @Test
    public void testIterateStrangeKeys()
    {
        key = new DefaultConfigurationKey(expressionEngine, "key.");
        DefaultConfigurationKey.KeyIterator it = key.iterator();
        assertTrue("Too few key parts", it.hasNext());
        assertEquals("Wrong key part", "key", it.next());
        assertFalse("Too many key parts", it.hasNext());

        key = new DefaultConfigurationKey(expressionEngine, ".");
        it = key.iterator();
        assertFalse("Simple delimiter key has more parts", it.hasNext());

        key = new DefaultConfigurationKey(expressionEngine,
                "key().index()undefined(0).test");
        it = key.iterator();
        assertEquals("Wrong first part", "key()", it.next());
        assertFalse("Index detected in first part", it.hasIndex());
        assertEquals("Wrong second part", "index()undefined", it.nextKey(false));
        assertTrue("No index detected in second part", it.hasIndex());
        assertEquals("Wrong index value", 0, it.getIndex());
    }

    /**
     * Tests iterating over keys with escaped delimiters.
     */
    @Test
    public void testIterateEscapedDelimiters()
    {
        key.append("my..elem");
        key.append("trailing..dot..");
        key.append(".strange");
        assertEquals("my..elem.trailing..dot...strange", key.toString());
        final DefaultConfigurationKey.KeyIterator kit = key.iterator();
        assertEquals("Wrong first part", "my.elem", kit.nextKey());
        assertEquals("Wrong second part", "trailing.dot.", kit.nextKey());
        assertEquals("Wrong third part", "strange", kit.nextKey());
        assertFalse("Too many parts", kit.hasNext());
    }

    /**
     * Tests iterating over keys when a different escaped delimiter is used.
     */
    @Test
    public void testIterateAlternativeEscapeDelimiter()
    {
        expressionEngine =
                new DefaultExpressionEngine(symbols()
                        .setEscapedDelimiter("\\.").create());
        key = new DefaultConfigurationKey(expressionEngine);
        key.append("\\.my\\.elem");
        key.append("trailing\\.dot\\.");
        key.append(".strange");
        assertEquals("\\.my\\.elem.trailing\\.dot\\..strange", key.toString());
        final DefaultConfigurationKey.KeyIterator kit = key.iterator();
        assertEquals("Wrong first part", ".my.elem", kit.nextKey());
        assertEquals("Wrong second part", "trailing.dot.", kit.nextKey());
        assertEquals("Wrong third part", "strange", kit.nextKey());
        assertFalse("Too many parts", kit.hasNext());
    }

    /**
     * Tests iterating when no escape delimiter is defined.
     */
    @Test
    public void testIterateWithoutEscapeDelimiter()
    {
        expressionEngine =
                new DefaultExpressionEngine(symbols()
                        .setEscapedDelimiter(null).create());
        key = new DefaultConfigurationKey(expressionEngine);
        key.append("..my..elem.trailing..dot...strange");
        assertEquals("Wrong key", "my..elem.trailing..dot...strange", key
                .toString());
        final DefaultConfigurationKey.KeyIterator kit = key.iterator();
        final String[] parts =
        { "my", "elem", "trailing", "dot", "strange"};
        for (int i = 0; i < parts.length; i++)
        {
            assertEquals("Wrong key part " + i, parts[i], kit.next());
        }
        assertFalse("Too many parts", kit.hasNext());
    }

    /**
     * Tests whether a key with brackets in it can be iterated over.
     */
    @Test
    public void testIterateWithBrackets()
    {
        key.append("directory.platform(x86).path");
        final DefaultConfigurationKey.KeyIterator kit = key.iterator();
        String part = kit.nextKey();
        assertEquals("Wrong part 1", "directory", part);
        assertFalse("Has index 1", kit.hasIndex());
        part = kit.nextKey();
        assertEquals("Wrong part 2", "platform(x86)", part);
        assertFalse("Has index 2", kit.hasIndex());
        part = kit.nextKey();
        assertEquals("Wrong part 3", "path", part);
        assertFalse("Has index 3", kit.hasIndex());
        assertFalse("Too many elements", kit.hasNext());
    }

    /**
     * Tests iterating over an attribute key that has an index.
     */
    @Test
    public void testAttributeKeyWithIndex()
    {
        key.append(TESTATTR);
        key.appendIndex(0);
        assertEquals("Wrong attribute key with index", TESTATTR + "(0)", key
                .toString());

        final DefaultConfigurationKey.KeyIterator it = key.iterator();
        assertTrue("No first element", it.hasNext());
        it.next();
        assertTrue("Index not found", it.hasIndex());
        assertEquals("Incorrect index", 0, it.getIndex());
        assertTrue("Attribute not found", it.isAttribute());
        assertEquals("Wrong plain key", "dataType", it.currentKey(false));
        assertEquals("Wrong decorated key", TESTATTR, it.currentKey(true));
    }

    /**
     * Tests iteration when the attribute markers equals the property delimiter.
     */
    @Test
    public void testIterateAttributeEqualsPropertyDelimiter()
    {
        expressionEngine =
                new DefaultExpressionEngine(
                        symbols()
                                .setAttributeEnd(null)
                                .setAttributeStart(
                                        DefaultExpressionEngineSymbols.DEFAULT_PROPERTY_DELIMITER)
                                .create());
        key = new DefaultConfigurationKey(expressionEngine);
        key.append("this.isa.key");
        final DefaultConfigurationKey.KeyIterator kit = key.iterator();
        assertEquals("Wrong first key part", "this", kit.next());
        assertFalse("First part is an attribute", kit.isAttribute());
        assertTrue("First part is not a property key", kit.isPropertyKey());
        assertEquals("Wrong second key part", "isa", kit.next());
        assertFalse("Second part is an attribute", kit.isAttribute());
        assertTrue("Second part is not a property key", kit.isPropertyKey());
        assertEquals("Wrong third key part", "key", kit.next());
        assertTrue("Third part is not an attribute", kit.isAttribute());
        assertTrue("Third part is not a property key", kit.isPropertyKey());
        assertEquals("Wrong decorated key part", "key", kit.currentKey(true));
    }

    /**
     * Tests whether common key parts can be extracted.
     */
    @Test
    public void testCommonKey()
    {
        final DefaultConfigurationKey k1 = key(TESTKEY);
        DefaultConfigurationKey k2 = key("tables.table(0).name");
        DefaultConfigurationKey kc = k1.commonKey(k2);
        assertEquals("Wrong common key (1)", key("tables.table(0)"), kc);
        assertEquals("Not symmetric", kc, k2.commonKey(k1));

        k2 = key("tables.table(1).fields.field(1)");
        kc = k1.commonKey(k2);
        assertEquals("Wrong common key (2)", key("tables"), kc);

        k2 = key("completely.different.key");
        kc = k1.commonKey(k2);
        assertEquals("Got a common key for different keys", 0, kc.length());

        kc = k1.commonKey(key);
        assertEquals("Got a common key for empty key", 0, kc.length());

        kc = k1.commonKey(k1);
        assertEquals("Wrong result for reflexiv invocation", kc, k1);
    }

    /**
     * Tries to call commonKey() with null input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCommonKeyNull()
    {
        key.commonKey(null);
    }

    /**
     * Tests differenceKey() on the same object.
     */
    @Test
    public void testDifferenceKeySame()
    {
        final DefaultConfigurationKey k1 = key(TESTKEY);
        final DefaultConfigurationKey kd = k1.differenceKey(k1);
        assertEquals("Got difference for same keys", 0, kd.length());
    }

    /**
     * Tests the differenceKey() method.
     */
    @Test
    public void testDifferenceKey()
    {
        final DefaultConfigurationKey k1 = key(TESTKEY);
        DefaultConfigurationKey k2 = key("tables.table(0).name");
        DefaultConfigurationKey kd = k1.differenceKey(k2);
        assertEquals("Wrong difference (1)", "name", kd.toString());

        k2 = key("tables.table(1).fields.field(1)");
        kd = k1.differenceKey(k2);
        assertEquals("Wrong difference (2)", "table(1).fields.field(1)", kd.toString());

        k2 = key("completely.different.key");
        kd = k1.differenceKey(k2);
        assertEquals("Wrong difference (3)", k2, kd);
    }
}
