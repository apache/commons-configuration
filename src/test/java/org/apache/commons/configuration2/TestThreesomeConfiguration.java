package org.apache.commons.configuration2;

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

import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;

import junit.framework.TestCase;

/**
 * A base class for testing {@link
 * org.apache.commons.configuration2.BasePropertiesConfiguration}
 * extensions.
 *
 * @version $Id$
 */
public class TestThreesomeConfiguration extends TestCase
{
    protected Configuration conf = null;

    protected void setUp() throws Exception
    {
        conf = new PropertiesConfiguration("threesome.properties");
    }
    
    /**
     * Tests <code>List</code> parsing.
     */
    public void testList1() throws Exception
    {
        List packages = conf.getList("test.threesome.one");
        // we should get 3 packages here
        assertEquals(3, packages.size());
    }

    /**
     * Tests <code>List</code> parsing.
     */
    public void testList2() throws Exception
    {
        List packages = conf.getList("test.threesome.two");
        // we should get 3 packages here
        assertEquals(3, packages.size());
    }

    /**
     * Tests <code>List</code> parsing.
     */
    public void testList3() throws Exception
    {
        List packages = conf.getList("test.threesome.three");
        // we should get 3 packages here
        assertEquals(3, packages.size());
    }

}
