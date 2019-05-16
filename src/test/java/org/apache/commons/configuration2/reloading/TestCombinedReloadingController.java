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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Test;

/**
 * Test class for {@code CombinedReloadingController}.
 *
 */
public class TestCombinedReloadingController
{
    /** An array with mock objects for the sub controllers. */
    private ReloadingController[] subControllers;

    /**
     * Creates an array with mock objects for sub controllers.
     */
    private void initSubControllers()
    {
        subControllers = new ReloadingController[3];
        for (int i = 0; i < subControllers.length; i++)
        {
            subControllers[i] = EasyMock.createMock(ReloadingController.class);
        }
    }

    /**
     * Replays the mocks for the sub controllers.
     */
    private void replaySubControllers()
    {
        EasyMock.replay((Object[]) subControllers);
    }

    /**
     * Verifies the mocks for the sub controllers.
     */
    private void verifySubSontrollers()
    {
        EasyMock.verify((Object[]) subControllers);
    }

    /**
     * Creates a test instance with default settings.
     *
     * @return the test instance
     */
    private CombinedReloadingController setUpController()
    {
        initSubControllers();
        final List<ReloadingController> lstCtrls =
                new ArrayList<>(
                        Arrays.asList(subControllers));
        final CombinedReloadingController result =
                new CombinedReloadingController(lstCtrls);
        // check whether a defensive copy is created
        lstCtrls.clear();
        return result;
    }

    /**
     * Tries to create an instance without a collection.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNull()
    {
        new CombinedReloadingController(null);
    }

    /**
     * Tries to create an instance with a collection containing a null entry.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInitNullEntries()
    {
        initSubControllers();
        final Collection<ReloadingController> ctrls =
                new ArrayList<>(
                        Arrays.asList(subControllers));
        ctrls.add(null);
        new CombinedReloadingController(ctrls);
    }

    /**
     * Tests a check for a reloading operation which results in true.
     */
    @Test
    public void testCheckForReloadingTrue()
    {
        final CombinedReloadingController ctrl = setUpController();
        EasyMock.expect(subControllers[0].checkForReloading(null)).andReturn(
                Boolean.FALSE);
        EasyMock.expect(subControllers[1].checkForReloading(null)).andReturn(
                Boolean.TRUE);
        EasyMock.expect(subControllers[2].checkForReloading(null)).andReturn(
                Boolean.FALSE);
        replaySubControllers();
        assertTrue("Wrong result", ctrl.checkForReloading("someData"));
        verifySubSontrollers();
    }

    /**
     * Tests a check for a reloading operation which results in false.
     */
    @Test
    public void testCheckForReloadingFalse()
    {
        final CombinedReloadingController ctrl = setUpController();
        for (final ReloadingController rc : subControllers)
        {
            EasyMock.expect(rc.checkForReloading(null))
                    .andReturn(Boolean.FALSE);
        }
        replaySubControllers();
        assertFalse("Wrong result", ctrl.checkForReloading("someParam"));
        verifySubSontrollers();
    }

    /**
     * Tests whether the reloading state can be reset.
     */
    @Test
    public void testResetReloadingState()
    {
        final CombinedReloadingController ctrl = setUpController();
        EasyMock.expect(subControllers[0].checkForReloading(null)).andReturn(
                Boolean.TRUE);
        EasyMock.expect(subControllers[1].checkForReloading(null)).andReturn(
                Boolean.FALSE);
        EasyMock.expect(subControllers[2].checkForReloading(null)).andReturn(
                Boolean.FALSE);
        for (final ReloadingController rc : subControllers)
        {
            rc.resetReloadingState();
        }
        replaySubControllers();
        ctrl.checkForReloading(null);
        ctrl.resetReloadingState();
        verifySubSontrollers();
    }

    /**
     * Tests whether the sub controller's reloading state can be reset
     * unconditionally.
     */
    @Test
    public void testResetInitialReloadingState()
    {
        final CombinedReloadingController ctrl = setUpController();
        for (final ReloadingController rc : subControllers)
        {
            rc.resetReloadingState();
        }
        replaySubControllers();
        ctrl.resetInitialReloadingState();
        verifySubSontrollers();
    }

    /**
     * Tests whether the sub controllers can be accessed.
     */
    @Test
    public void testGetSubControllers()
    {
        final CombinedReloadingController ctrl = setUpController();
        replaySubControllers();
        final Collection<ReloadingController> subs = ctrl.getSubControllers();
        assertEquals("Wrong number of sub controllers", subControllers.length,
                subs.size());
        assertTrue("Wrong sub controllers",
                subs.containsAll(Arrays.asList(subControllers)));
    }

    /**
     * Tests that the list of sub controllers cannot be manipulated.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetSubControllersModify()
    {
        final Collection<ReloadingController> subs =
                setUpController().getSubControllers();
        subs.clear();
    }
}
