/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Test case for the {@link SubsetConfiguration} class.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 1.2 $, $Date: 2004/05/04 22:27:10 $
 */
public class TestSubsetConfiguration extends TestCase {

    public void testGetProperty() {
        BaseConfiguration conf = new BaseConfiguration();
        conf.setProperty("test.key1", "value1");
        conf.setProperty("testing.key2", "value1");

        Configuration subset = new SubsetConfiguration(conf, "test", ".");
        assertFalse("the subset is empty", subset.isEmpty());
        assertTrue("'key1' not found in the subset", subset.containsKey("key1"));
        assertFalse("'ng.key2' found in the subset", subset.containsKey("ng.key2"));
    }

    public void testSetProperty() {
        BaseConfiguration conf = new BaseConfiguration();
        Configuration subset = new SubsetConfiguration(conf, "test", ".");

        // set a property in the subset and check the parent
        subset.setProperty("key1", "value1");
        assertEquals("key1 in the subset configuration", "value1", subset.getProperty("key1"));
        assertEquals("test.key1 in the parent configuration", "value1", conf.getProperty("test.key1"));

        // set a property in the parent and check in the subset
        conf.setProperty("test.key2", "value2");
        assertEquals("test.key2 in the parent configuration", "value2", conf.getProperty("test.key2"));
        assertEquals("key2 in the subset configuration", "value2", subset.getProperty("key2"));
    }

    public void testGetParentKey() {
        // subset with delimiter
        SubsetConfiguration subset = new SubsetConfiguration(null, "prefix", ".");
        assertEquals("parent key for \"key\"", "prefix.key", subset.getParentKey("key"));
        assertEquals("parent key for \"\"", "prefix", subset.getParentKey(""));

        // subset without delimiter
        subset = new SubsetConfiguration(null, "prefix", null);
        assertEquals("parent key for \"key\"", "prefixkey", subset.getParentKey("key"));
        assertEquals("parent key for \"\"", "prefix", subset.getParentKey(""));
    }

    public void testGetChildKey() {
        // subset with delimiter
        SubsetConfiguration subset = new SubsetConfiguration(null, "prefix", ".");
        assertEquals("parent key for \"prefixkey\"", "key", subset.getChildKey("prefix.key"));
        assertEquals("parent key for \"prefix\"", "", subset.getChildKey("prefix"));

        // subset without delimiter
        subset = new SubsetConfiguration(null, "prefix", null);
        assertEquals("parent key for \"prefixkey\"", "key", subset.getChildKey("prefixkey"));
        assertEquals("parent key for \"prefix\"", "", subset.getChildKey("prefix"));
    }

    public void testGetKeys() {
        BaseConfiguration conf = new BaseConfiguration();
        conf.setProperty("test", "value0");
        conf.setProperty("test.key1", "value1");
        conf.setProperty("testing.key2", "value1");

        Configuration subset = new SubsetConfiguration(conf, "test", ".");

        Iterator it = subset.getKeys();
        assertEquals("1st key", "", it.next());
        assertEquals("2nd key", "key1", it.next());
        assertFalse("too many elements", it.hasNext());
    }

    public void testGetKeysWithPrefix() {
        BaseConfiguration conf = new BaseConfiguration();
        conf.setProperty("test.abc", "value0");
        conf.setProperty("test.abc.key1", "value1");
        conf.setProperty("test.abcdef.key2", "value1");

        Configuration subset = new SubsetConfiguration(conf, "test", ".");

        Iterator it = subset.getKeys("abc");
        assertEquals("1st key", "abc", it.next());
        assertEquals("2nd key", "abc.key1", it.next());
        assertFalse("too many elements", it.hasNext());
    }
    
    public void testGetList() {
        BaseConfiguration conf = new BaseConfiguration();
        conf.setProperty("test.abc", "value0,value1");
        conf.addProperty("test.abc","value3");

        Configuration subset = new SubsetConfiguration(conf, "test", ".");
        List list = subset.getList("abc", new ArrayList());
        assertEquals(3, list.size());
    }
}
