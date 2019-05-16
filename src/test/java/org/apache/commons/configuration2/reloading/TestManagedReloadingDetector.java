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

package org.apache.commons.configuration2.reloading;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for the ManagedReloadingDetector class.
 *
 * @author Nicolas De loof
 */
public class TestManagedReloadingDetector
{
    /** The instance to be tested. */
    private ManagedReloadingDetector strategy;

    @Before
    public void setUp() throws Exception
    {
        strategy = new ManagedReloadingDetector();
    }

    /**
     * Tests the result of isReloadingRequired() for a newly created instance.
     */
    @Test
    public void testReloadingRequiredInitial()
    {
        assertFalse("Wrong result", strategy.isReloadingRequired());
    }

    /**
     * Tests the refresh() method.
     */
    @Test
    public void testRefresh()
    {
        strategy.refresh();
        assertTrue("Reloading request not detected",
                strategy.isReloadingRequired());
        assertTrue("Reloading state not permanent",
                strategy.isReloadingRequired());
    }

    /**
     * Tests whether the reloading state can be reset again.
     */
    @Test
    public void testReloadingPerformed()
    {
        strategy.refresh();
        strategy.reloadingPerformed();
        assertFalse("Reloading state not reset", strategy.isReloadingRequired());
    }
}
