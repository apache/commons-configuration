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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.lang.text.StrLookup;

/**
 * Test case for the {@link SubsetConfiguration} class.
 *
 * @author Emmanuel Bourg
 * @version $Id$
 */
public class TestSubsetConfiguration extends TestCase
{
    static final String TEST_DIR = ConfigurationAssert.TEST_DIR_NAME;
    static final String TEST_FILE = "testDigesterConfiguration2.xml";

    public void testGetProperty()
    {
        Configuration conf = new BaseConfiguration();
        conf.setProperty("test.key1", "value1");
        conf.setProperty("testing.key2", "value1");

        Configuration subset = new SubsetConfiguration(conf, "test", ".");
        assertFalse("the subset is empty", subset.isEmpty());
        assertTrue("'key1' not found in the subset", subset.containsKey("key1"));
        assertFalse("'ng.key2' found in the subset", subset.containsKey("ng.key2"));
    }

    public void testSetProperty()
    {
        Configuration conf = new BaseConfiguration();
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

    public void testGetParentKey()
    {
        // subset with delimiter
        SubsetConfiguration subset = new SubsetConfiguration(null, "prefix", ".");
        assertEquals("parent key for \"key\"", "prefix.key", subset.getParentKey("key"));
        assertEquals("parent key for \"\"", "prefix", subset.getParentKey(""));

        // subset without delimiter
        subset = new SubsetConfiguration(null, "prefix", null);
        assertEquals("parent key for \"key\"", "prefixkey", subset.getParentKey("key"));
        assertEquals("parent key for \"\"", "prefix", subset.getParentKey(""));
    }

    public void testGetChildKey()
    {
        // subset with delimiter
        SubsetConfiguration subset = new SubsetConfiguration(null, "prefix", ".");
        assertEquals("parent key for \"prefixkey\"", "key", subset.getChildKey("prefix.key"));
        assertEquals("parent key for \"prefix\"", "", subset.getChildKey("prefix"));

        // subset without delimiter
        subset = new SubsetConfiguration(null, "prefix", null);
        assertEquals("parent key for \"prefixkey\"", "key", subset.getChildKey("prefixkey"));
        assertEquals("parent key for \"prefix\"", "", subset.getChildKey("prefix"));
    }

    public void testGetKeys()
    {
        Configuration conf = new BaseConfiguration();
        conf.setProperty("test", "value0");
        conf.setProperty("test.key1", "value1");
        conf.setProperty("testing.key2", "value1");

        Configuration subset = new SubsetConfiguration(conf, "test", ".");

        Iterator it = subset.getKeys();
        assertEquals("1st key", "", it.next());
        assertEquals("2nd key", "key1", it.next());
        assertFalse("too many elements", it.hasNext());
    }

    public void testGetKeysWithPrefix()
    {
        Configuration conf = new BaseConfiguration();
        conf.setProperty("test.abc", "value0");
        conf.setProperty("test.abc.key1", "value1");
        conf.setProperty("test.abcdef.key2", "value1");

        Configuration subset = new SubsetConfiguration(conf, "test", ".");

        Iterator it = subset.getKeys("abc");
        assertEquals("1st key", "abc", it.next());
        assertEquals("2nd key", "abc.key1", it.next());
        assertFalse("too many elements", it.hasNext());
    }

    public void testGetList()
    {
        Configuration conf = new BaseConfiguration();
        conf.setProperty("test.abc", "value0,value1");
        conf.addProperty("test.abc", "value3");

        Configuration subset = new SubsetConfiguration(conf, "test", ".");
        List list = subset.getList("abc", new ArrayList());
        assertEquals(3, list.size());
    }

    public void testGetParent()
    {
        Configuration conf = new BaseConfiguration();
        SubsetConfiguration subset = new SubsetConfiguration(conf, "prefix", ".");

        assertEquals("parent", conf, subset.getParent());
    }

    public void testGetPrefix()
    {
        Configuration conf = new BaseConfiguration();
        SubsetConfiguration subset = new SubsetConfiguration(conf, "prefix", ".");

        assertEquals("prefix", "prefix", subset.getPrefix());
    }

    public void testSetPrefix()
    {
        Configuration conf = new BaseConfiguration();
        SubsetConfiguration subset = new SubsetConfiguration(conf, null, ".");
        subset.setPrefix("prefix");

        assertEquals("prefix", "prefix", subset.getPrefix());
    }

    public void testThrowExceptionOnMissing()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.setThrowExceptionOnMissing(true);

        SubsetConfiguration subset = new SubsetConfiguration(config, "prefix");

        try
        {
            subset.getString("foo");
            fail("NoSuchElementException expected");
        }
        catch (NoSuchElementException e)
        {
            // expected
        }

        config.setThrowExceptionOnMissing(false);
        assertNull(subset.getString("foo"));


        subset.setThrowExceptionOnMissing(true);
        try
        {
            config.getString("foo");
            fail("NoSuchElementException expected");
        }
        catch (NoSuchElementException e)
        {
            // expected
        }
    }

    public void testNested() throws Exception
    {
        ConfigurationFactory factory = new ConfigurationFactory();
        File src = new File(new File(TEST_DIR), TEST_FILE);
        factory.setConfigurationURL(src.toURL());
        Configuration config = factory.getConfiguration();
        Configuration subConf = config.subset("tables.table(0)");
        assertTrue(subConf.getKeys().hasNext());
        Configuration subSubConf = subConf.subset("fields.field(1)");
        Iterator itKeys = subSubConf.getKeys();
        Set keys = new HashSet();
        keys.add("name");
        keys.add("type");
        while(itKeys.hasNext())
        {
            String k = (String) itKeys.next();
            assertTrue(keys.contains(k));
            keys.remove(k);
        }
        assertTrue(keys.isEmpty());
    }

    public void testClear()
    {
        Configuration config = new BaseConfiguration();
        config.setProperty("test.key1", "value1");
        config.setProperty("testing.key2", "value1");

        Configuration subset = config.subset("test");
        subset.clear();

        assertTrue("the subset is not empty", subset.isEmpty());
        assertFalse("the parent configuration is empty", config.isEmpty());
    }

    public void testSetListDelimiter()
    {
        BaseConfiguration config = new BaseConfiguration();
        Configuration subset = config.subset("prefix");
        config.setListDelimiter('/');
        subset.addProperty("list", "a/b/c");
        assertEquals("Wrong size of list", 3, config.getList("prefix.list")
                .size());

        ((AbstractConfiguration) subset).setListDelimiter(';');
        subset.addProperty("list2", "a;b;c");
        assertEquals("Wrong size of list2", 3, config.getList("prefix.list2")
                .size());
    }

    public void testGetListDelimiter()
    {
        BaseConfiguration config = new BaseConfiguration();
        AbstractConfiguration subset = (AbstractConfiguration) config
                .subset("prefix");
        config.setListDelimiter('/');
        assertEquals("Wrong list delimiter in subset", '/', subset
                .getListDelimiter());
        subset.setListDelimiter(';');
        assertEquals("Wrong list delimiter in parent", ';', config
                .getListDelimiter());
    }

    public void testSetDelimiterParsingDisabled()
    {
        BaseConfiguration config = new BaseConfiguration();
        Configuration subset = config.subset("prefix");
        config.setDelimiterParsingDisabled(true);
        subset.addProperty("list", "a,b,c");
        assertEquals("Wrong value of property", "a,b,c", config
                .getString("prefix.list"));

        ((AbstractConfiguration) subset).setDelimiterParsingDisabled(false);
        subset.addProperty("list2", "a,b,c");
        assertEquals("Wrong size of list2", 3, config.getList("prefix.list2")
                .size());
    }

    public void testIsDelimiterParsingDisabled()
    {
        BaseConfiguration config = new BaseConfiguration();
        AbstractConfiguration subset = (AbstractConfiguration) config
                .subset("prefix");
        config.setDelimiterParsingDisabled(true);
        assertTrue("Wrong value of list parsing flag in subset", subset
                .isDelimiterParsingDisabled());
        subset.setDelimiterParsingDisabled(false);
        assertFalse("Wrong value of list parsing flag in parent", config
                .isDelimiterParsingDisabled());
    }

    /**
     * Tests manipulating the interpolator.
     */
    public void testInterpolator()
    {
        BaseConfiguration config = new BaseConfiguration();
        AbstractConfiguration subset = (AbstractConfiguration) config
                .subset("prefix");
        InterpolationTestHelper.testGetInterpolator(subset);
    }

    public void testLocalLookupsInInterpolatorAreInherited() {
        BaseConfiguration config = new BaseConfiguration();
        ConfigurationInterpolator interpolator = config.getInterpolator();
        interpolator.registerLookup("brackets", new StrLookup(){

            public String lookup(String key) {
                return "(" + key +")";
            }

        });
        config.setProperty("prefix.var", "${brackets:x}");
        AbstractConfiguration subset = (AbstractConfiguration) config
                .subset("prefix");
        assertEquals("Local lookup was not inherited", "(x)", subset
                .getString("var", ""));
    }

    public void testInterpolationForKeysOfTheParent() {
        BaseConfiguration config = new BaseConfiguration();
        config.setProperty("test", "junit");
        config.setProperty("prefix.key", "${test}");
        AbstractConfiguration subset = (AbstractConfiguration) config
                .subset("prefix");
        assertEquals("Interpolation does not resolve parent keys", "junit",
                subset.getString("key", ""));
    }
}
