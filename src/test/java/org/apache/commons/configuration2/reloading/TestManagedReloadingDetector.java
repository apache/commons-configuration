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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test case for the ManagedReloadingDetector class.
 */
public class TestManagedReloadingDetector {
    /** The instance to be tested. */
    private ManagedReloadingDetector strategy;

    @BeforeEach
    public void setUp() throws Exception {
        strategy = new ManagedReloadingDetector();
    }

    /**
     * Tests the refresh() method.
     */
    @Test
    public void testRefresh() {
        strategy.refresh();
        assertTrue(strategy.isReloadingRequired());
        assertTrue(strategy.isReloadingRequired());
    }

    /**
     * Tests whether the reloading state can be reset again.
     */
    @Test
    public void testReloadingPerformed() {
        strategy.refresh();
        strategy.reloadingPerformed();
        assertFalse(strategy.isReloadingRequired());
    }

    /**
     * Tests the result of isReloadingRequired() for a newly created instance.
     */
    @Test
    public void testReloadingRequiredInitial() {
        assertFalse(strategy.isReloadingRequired());
    }
}
