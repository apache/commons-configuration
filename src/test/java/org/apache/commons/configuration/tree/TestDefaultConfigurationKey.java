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
package org.apache.commons.configuration.tree;

import java.util.NoSuchElementException;

import junit.framework.TestCase;

/**
 * Test class for DefaultConfigurationKey.
 *
 * @author Oliver Heger
 * @version $Id$
 */
public class TestDefaultConfigurationKey extends TestCase
{
    /** Constant for a test key. */
    private static final String TESTPROPS = "tables.table(0).fields.field(1)";

    /** Constant for a test attribute key. */
    private static final String TESTATTR = "[@dataType]";

    /** Constant for a complex attribute key. */
    private static final String TESTKEY = TESTPROPS + TESTATTR;

    /** Stores the expression engine of the key to test. */
    DefaultExpressionEngine expressionEngine;

    /** Stores the object to be tested. */
    DefaultConfigurationKey key;

    protected void setUp() throws Exception
    {
        super.setUp();
        expressionEngine = new DefaultExpressionEngine();
        key = new DefaultConfigurationKey(expressionEngine);
    }

    /**
     * Tests setting the expression engine to null. This should not be allowed.
     */
    public void testSetNullExpressionEngine()
    {
        try
        {
            key.setExpressionEngine(null);
            fail("Could set null expression engine!");
        }
        catch (IllegalArgumentException iex)
        {
            // ok
        }
    }

    /**
     * Tests the isAttributeKey() method with several keys.
     */
    public void testIsAttributeKey()
    {
        assertTrue("Attribute key not detected", key.isAttributeKey(TESTATTR));
        assertFalse("Property key considered as attribute", key
                .isAttributeKey(TESTPROPS));
        assertFalse("Null key considered as attribute", key
                .isAttributeKey(null));
    }

    /**
     * Tests if attribute keys are correctly detected if no end markers are set.
     * (In this test case we use the same delimiter for attributes as for simple
     * properties.)
     */
    public void testIsAttributeKeyWithoutEndMarkers()
    {
        expressionEngine.setAttributeEnd(null);
        expressionEngine
                .setAttributeStart(DefaultExpressionEngine.DEFAULT_PROPERTY_DELIMITER);
        assertTrue(
                "Attribute key not detected",
                key
                        .isAttributeKey(DefaultExpressionEngine.DEFAULT_PROPERTY_DELIMITER
                                + "test"));
        assertFalse("Property key considered as attribute key", key
                .isAttributeKey(TESTATTR));
    }

    /**
     * Tests removing leading delimiters.
     */
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
    public void testTrim()
    {
        assertEquals("Key was not trimmed", "test", key.trim(".test."));
        assertEquals("Null key could not be processed", "", key.trim(null));
        assertEquals("Delimiter could not be processed", "", key
                .trim(DefaultExpressionEngine.DEFAULT_PROPERTY_DELIMITER));
    }

    /**
     * Tests appending keys.
     */
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
    public void testAppendDelimiters()
    {
        key.append("key..").append("test").append(".");
        key.append(".more").append("..tests");
        assertEquals("Wrong key", "key...test.more...tests", key.toString());
    }

    /**
     * Tests appending keys that contain delimiters when no escpaped delimiter
     * is defined.
     */
    public void testAppendDelimitersWithoutEscaping()
    {
        expressionEngine.setEscapedDelimiter(null);
        key.append("key.......").append("test").append(".");
        key.append(".more").append("..tests");
        assertEquals("Wrong constructed key", "key.test.more.tests", key
                .toString());
    }

    /**
     * Tests calling append with the escape flag.
     */
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
    public void testConstructAttributeKeyWithoutEndMarkers()
    {
        expressionEngine.setAttributeEnd(null);
        expressionEngine.setAttributeStart(expressionEngine
                .getPropertyDelimiter());
        assertEquals("Wrong attribute key", ".test", key
                .constructAttributeKey("test"));
        assertEquals("Attribute key was incorrectly converted", ".test", key
                .constructAttributeKey(".test"));
    }

    /**
     * Tests appending attribute keys.
     */
    public void testAppendAttribute()
    {
        key.appendAttribute("dataType");
        assertEquals("Attribute key not correctly appended", TESTATTR, key
                .toString());
    }

    /**
     * Tests appending an attribute key that is already decorated-
     */
    public void testAppendDecoratedAttributeKey()
    {
        key.appendAttribute(TESTATTR);
        assertEquals("Decorated attribute key not correctly appended",
                TESTATTR, key.toString());
    }

    /**
     * Tests appending a null attribute key.
     */
    public void testAppendNullAttributeKey()
    {
        key.appendAttribute(null);
        assertEquals("Null attribute key not correctly appended", "", key
                .toString());
    }

    /**
     * Tests appending an index to a key.
     */
    public void testAppendIndex()
    {
        key.append("test").appendIndex(42);
        assertEquals("Index was not correctly appended", "test(42)", key
                .toString());
    }

    /**
     * Tests constructing a complex key by chaining multiple append operations.
     */
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
    public void testEquals()
    {
        DefaultConfigurationKey k1 = new DefaultConfigurationKey(
                expressionEngine, TESTKEY);
        DefaultConfigurationKey k2 = new DefaultConfigurationKey(
                expressionEngine, TESTKEY);
        assertTrue("Keys are not equal", k1.equals(k2));
        assertTrue("Not reflexiv", k2.equals(k1));
        assertEquals("Hash codes not equal", k1.hashCode(), k2.hashCode());
        k2.append("anotherPart");
        assertFalse("Keys considered equal", k1.equals(k2));
        assertFalse("Keys considered equal", k2.equals(k1));
        assertFalse("Key equals null key", k1.equals(null));
        assertTrue("Faild comparison with string", k1.equals(TESTKEY));
    }

    /**
     * Tests determining an attribute key's name.
     */
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
    public void testIterate()
    {
        key.append(TESTKEY);
        DefaultConfigurationKey.KeyIterator it = key.iterator();
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
        catch (NoSuchElementException nex)
        {
            // ok
        }
    }

    /**
     * Tests an iteration where the remove() method is called. This is not
     * supported.
     */
    public void testIterateWithRemove()
    {
        assertFalse(key.iterator().hasNext());
        key.append("simple");
        DefaultConfigurationKey.KeyIterator it = key.iterator();
        assertTrue(it.hasNext());
        assertEquals("simple", it.next());
        try
        {
            it.remove();
            fail("Could remove key component!");
        }
        catch (UnsupportedOperationException uex)
        {
            // ok
        }
    }

    /**
     * Tests iterating over some funny keys.
     */
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
    public void testIterateEscapedDelimiters()
    {
        key.append("my..elem");
        key.append("trailing..dot..");
        key.append(".strange");
        assertEquals("my..elem.trailing..dot...strange", key.toString());
        DefaultConfigurationKey.KeyIterator kit = key.iterator();
        assertEquals("Wrong first part", "my.elem", kit.nextKey());
        assertEquals("Wrong second part", "trailing.dot.", kit.nextKey());
        assertEquals("Wrong third part", "strange", kit.nextKey());
        assertFalse("Too many parts", kit.hasNext());
    }

    /**
     * Tests iterating over keys when a different escaped delimiter is used.
     */
    public void testIterateAlternativeEscapeDelimiter()
    {
        expressionEngine.setEscapedDelimiter("\\.");
        key.append("\\.my\\.elem");
        key.append("trailing\\.dot\\.");
        key.append(".strange");
        assertEquals("\\.my\\.elem.trailing\\.dot\\..strange", key.toString());
        DefaultConfigurationKey.KeyIterator kit = key.iterator();
        assertEquals("Wrong first part", ".my.elem", kit.nextKey());
        assertEquals("Wrong second part", "trailing.dot.", kit.nextKey());
        assertEquals("Wrong third part", "strange", kit.nextKey());
        assertFalse("Too many parts", kit.hasNext());
    }

    /**
     * Tests iterating when no escape delimiter is defined.
     */
    public void testIterateWithoutEscapeDelimiter()
    {
        expressionEngine.setEscapedDelimiter(null);
        key.append("..my..elem.trailing..dot...strange");
        assertEquals("Wrong key", "my..elem.trailing..dot...strange", key
                .toString());
        DefaultConfigurationKey.KeyIterator kit = key.iterator();
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
    public void testIterateWithBrackets()
    {
        key.append("directory.platform(x86).path");
        DefaultConfigurationKey.KeyIterator kit = key.iterator();
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
    public void testAttributeKeyWithIndex()
    {
        key.append(TESTATTR);
        key.appendIndex(0);
        assertEquals("Wrong attribute key with index", TESTATTR + "(0)", key
                .toString());

        DefaultConfigurationKey.KeyIterator it = key.iterator();
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
    public void testIterateAttributeEqualsPropertyDelimiter()
    {
        expressionEngine.setAttributeEnd(null);
        expressionEngine.setAttributeStart(expressionEngine
                .getPropertyDelimiter());
        key.append("this.isa.key");
        DefaultConfigurationKey.KeyIterator kit = key.iterator();
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
}
