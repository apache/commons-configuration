/*
 * Copyright 2004 The Apache Software Foundation.
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

import java.util.Iterator;

/**
 * Abstract TestCase for implementations of {@link AbstractConfiguration}.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 1.1 $, $Date: 2004/10/14 09:54:35 $
 */
public abstract class TestAbstractConfiguration extends TestCase
{
    /**
     * Return an abstract configuration with 2 key/value pairs:<br>
     * <pre>
     * key1 = value1
     * key2 = value2
     * </pre>
     */
    protected abstract AbstractConfiguration getConfiguration();

    /**
     * Return an empty configuration.
     */
    protected abstract AbstractConfiguration getEmptyConfiguration();

    public void testGetPropertyDirect()
    {
        AbstractConfiguration config = getConfiguration();
        assertEquals("key1", "value1", config.getPropertyDirect("key1"));
        assertEquals("key2", "value2", config.getPropertyDirect("key2"));
        assertNull("key3", config.getPropertyDirect("key3"));
    }

    public void testAddPropertyDirect()
    {
        AbstractConfiguration config = getConfiguration();
        config.addPropertyDirect("key3", "value3");
        assertEquals("key3", "value3", config.getPropertyDirect("key3"));
    }

    public void testIsEmpty()
    {
        AbstractConfiguration config = getConfiguration();
        assertFalse("the configuration is empty", config.isEmpty());
        assertTrue("the configuration is not empty", getEmptyConfiguration().isEmpty());
    }

    public void testContainsKey()
    {
        AbstractConfiguration config = getConfiguration();
        assertTrue("key1 not found", config.containsKey("key1"));
        assertFalse("key3 found", config.containsKey("key3"));
    }

    public void testClearProperty()
    {
        AbstractConfiguration config = getConfiguration();
        config.clearProperty("key2");
        assertFalse("key2 not cleared", config.containsKey("key2"));
    }

    public void testGetKeys()
    {
        AbstractConfiguration config = getConfiguration();
        Iterator keys = config.getKeys();

        assertNotNull("null iterator", keys);
        String k = keys.next() + ":" + keys.next();
        assertTrue("elements", "key1:key2".equals(k) | "key2:key1".equals(k));
        assertFalse("too many elements", keys.hasNext());

        keys = getEmptyConfiguration().getKeys();
        assertNotNull("null iterator", keys);
        assertFalse("too many elements", keys.hasNext());
    }

}
