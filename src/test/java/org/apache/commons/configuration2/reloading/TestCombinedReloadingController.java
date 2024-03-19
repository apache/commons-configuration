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
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@code CombinedReloadingController}.
 */
public class TestCombinedReloadingController {
    /** An array with mock objects for the sub controllers. */
    private ReloadingController[] subControllers;

    /**
     * Creates an array with mock objects for sub controllers.
     */
    private void initSubControllers() {
        subControllers = new ReloadingController[3];
        for (int i = 0; i < subControllers.length; i++) {
            subControllers[i] = mock(ReloadingController.class);
        }
    }

    /**
     * Creates a test instance with default settings.
     *
     * @return the test instance
     */
    private CombinedReloadingController setUpController() {
        initSubControllers();
        final List<ReloadingController> lstCtrls = new ArrayList<>(Arrays.asList(subControllers));
        final CombinedReloadingController result = new CombinedReloadingController(lstCtrls);
        // check whether a defensive copy is created
        lstCtrls.clear();
        return result;
    }

    /**
     * Tests a check for a reloading operation which results in false.
     */
    @Test
    public void testCheckForReloadingFalse() {
        final CombinedReloadingController ctrl = setUpController();

        for (final ReloadingController rc : subControllers) {
            when(rc.checkForReloading(null)).thenReturn(Boolean.FALSE);
        }

        assertFalse(ctrl.checkForReloading("someParam"));

        for (final ReloadingController rc : subControllers) {
            verify(rc).checkForReloading(null);
            verifyNoMoreInteractions(rc);
        }
    }

    /**
     * Tests a check for a reloading operation which results in true.
     */
    @Test
    public void testCheckForReloadingTrue() {
        final CombinedReloadingController ctrl = setUpController();

        when(subControllers[0].checkForReloading(null)).thenReturn(Boolean.FALSE);
        when(subControllers[1].checkForReloading(null)).thenReturn(Boolean.TRUE);
        when(subControllers[2].checkForReloading(null)).thenReturn(Boolean.FALSE);

        assertTrue(ctrl.checkForReloading("someData"));

        for (final ReloadingController rc : subControllers) {
            verify(rc).checkForReloading(null);
            verifyNoMoreInteractions(rc);
        }
    }

    /**
     * Tests whether the sub controllers can be accessed.
     */
    @Test
    public void testGetSubControllers() {
        final CombinedReloadingController ctrl = setUpController();
        final Collection<ReloadingController> subs = ctrl.getSubControllers();
        assertIterableEquals(Arrays.asList(subControllers), subs);
    }

    /**
     * Tests that the list of sub controllers cannot be manipulated.
     */
    @Test
    public void testGetSubControllersModify() {
        final Collection<ReloadingController> subs = setUpController().getSubControllers();
        assertThrows(UnsupportedOperationException.class, subs::clear);
    }

    /**
     * Tries to create an instance without a collection.
     */
    @Test
    public void testInitNull() {
        assertThrows(IllegalArgumentException.class, () -> new CombinedReloadingController(null));
    }

    /**
     * Tries to create an instance with a collection containing a null entry.
     */
    @Test
    public void testInitNullEntries() {
        initSubControllers();
        final Collection<ReloadingController> ctrls = new ArrayList<>(Arrays.asList(subControllers));
        ctrls.add(null);
        assertThrows(IllegalArgumentException.class, () -> new CombinedReloadingController(ctrls));
    }

    /**
     * Tests whether the sub controller's reloading state can be reset unconditionally.
     */
    @Test
    public void testResetInitialReloadingState() {
        final CombinedReloadingController ctrl = setUpController();
        ctrl.resetInitialReloadingState();

        for (final ReloadingController rc : subControllers) {
            verify(rc).resetReloadingState();
            verifyNoMoreInteractions(rc);
        }
    }

    /**
     * Tests whether the reloading state can be reset.
     */
    @Test
    public void testResetReloadingState() {
        final CombinedReloadingController ctrl = setUpController();

        when(subControllers[0].checkForReloading(null)).thenReturn(Boolean.TRUE);
        when(subControllers[1].checkForReloading(null)).thenReturn(Boolean.FALSE);
        when(subControllers[2].checkForReloading(null)).thenReturn(Boolean.FALSE);

        ctrl.checkForReloading(null);
        ctrl.resetReloadingState();

        for (final ReloadingController rc : subControllers) {
            verify(rc).checkForReloading(null);
            verify(rc).resetReloadingState();
            verifyNoMoreInteractions(rc);
        }
    }
}
