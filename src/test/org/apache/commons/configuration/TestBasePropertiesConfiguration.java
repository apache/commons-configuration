package org.apache.commons.configuration;

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

import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

/**
 * A base class for testing {@link
 * org.apache.commons.configuration.BasePropertiesConfiguration}
 * extensions.
 *
 * @version $Id: TestBasePropertiesConfiguration.java,v 1.5 2004/08/16 22:16:31 henning Exp $
 */
public abstract class TestBasePropertiesConfiguration extends TestCase
{
    protected BasePropertiesConfiguration conf;

    /**
     * Assigns the {@link #conf} field to a {@link
     * org.apache.commons.configuration.BasePropertiesConfiguration}
     * sub-class.
     */
    protected abstract void setUp() throws Exception;

    public void testLoad() throws Exception
    {
        String loaded = conf.getString("configuration.loaded");
        assertEquals("true", loaded);
    }

    /**
     * Tests that empty properties are treated as the empty string
     * (rather than as null).
     */
    public void testEmpty() throws Exception
    {
        String empty = conf.getString("test.empty");
        assertNotNull(empty);
        assertEquals("", empty);
    }

    /**
     * Tests that references to other properties work
     */
    public void testReference() throws Exception
    {
		assertEquals("baseextra", conf.getString("base.reference"));
	}

    /**
     * test if includes properties get loaded too
     */
    public void testLoadInclude() throws Exception
    {
        String loaded = conf.getString("include.loaded");
        assertEquals("true", loaded);
    }

    /**
     * Tests <code>List</code> parsing.
     */
    public void testList() throws Exception
    {
        List packages = conf.getList("packages");
        // we should get 3 packages here
        assertEquals(3, packages.size());
    }

    /**
     * Tests <code>Vector</code> parsing.
     */
    public void testVector() throws Exception
    {
        Vector packages = conf.getVector("packages");
        // we should get 3 packages here
        assertEquals(3, packages.size());
    }
}
