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
package org.apache.commons.configuration.reloading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * <p>
 * A specialized {@code ReloadingController} implementation which manages an
 * arbitrary number of other {@code ReloadingController} objects.
 * </p>
 * <p>
 * This class can be used to handle multiple simple controllers for reload
 * operations as a single object. As a usage example consider a combined
 * configuration containing a number of configuration sources of which some
 * support reloading. In this scenario all {@code ReloadingController} instances
 * for the reloading-enabled sources can be added to a
 * {@code CombinedReloadingController}. Then by triggering the combined
 * controller a reload check is performed on all child sources.
 * </p>
 * <p>
 * This class is a typical implementation of the <em>composite pattern</em>. An
 * instance is constructed with a collection of sub {@code ReloadingController}
 * objects. Its operations are implemented by delegating to all child
 * controllers.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public class CombinedReloadingController extends ReloadingController
{
    /**
     * Creates a new instance of {@code CombinedReloadingController} and
     * initializes it with the {@code ReloadingController} objects to be
     * managed.
     *
     * @param subCtrls the collection with sub {@code ReloadingController}s
     *        (must not be <b>null</b> or contain <b>null</b> entries)
     * @throws IllegalArgumentException if the passed in collection is
     *         <b>null</b> or contains <b>null</b> entries
     */
    public CombinedReloadingController(
            Collection<? extends ReloadingController> subCtrls)
    {
        super(createDetector(subCtrls));
    }

    /**
     * Returns a (unmodifiable) collection with the sub controllers managed by
     * this combined controller.
     *
     * @return a collection with sub controllers
     */
    public Collection<ReloadingController> getSubControllers()
    {
        return ((MultiReloadingControllerDetector) getDetector())
                .getControllers();
    }

    /**
     * Creates a specialized detector object which manages the passed in sub
     * controllers. The collection with controllers is also checked for
     * validity.
     *
     * @param subCtrls the collection with sub controllers
     * @return the {@code ReloadingDetector} to be used by the combined
     *         controller
     * @throws IllegalArgumentException if the passed in collection is
     *         <b>null</b> or contains <b>null</b> entries
     */
    private static ReloadingDetector createDetector(
            Collection<? extends ReloadingController> subCtrls)
    {
        if (subCtrls == null)
        {
            throw new IllegalArgumentException(
                    "Collection with sub controllers must not be null!");
        }
        Collection<ReloadingController> ctrls =
                new ArrayList<ReloadingController>(subCtrls);
        for (ReloadingController rc : ctrls)
        {
            if (rc == null)
            {
                throw new IllegalArgumentException(
                        "Collection with sub controllers contains a null entry!");
            }
        }

        return new MultiReloadingControllerDetector(
                Collections.unmodifiableCollection(ctrls));
    }

    /**
     * A specialized implementation of the {@code ReloadingDetector} interface
     * which operates on a collection of {@code ReloadingController} objects.
     * The methods defined by the {@code ReloadingDetector} interface are
     * delegated to the managed controllers.
     */
    private static class MultiReloadingControllerDetector implements
            ReloadingDetector
    {
        /** Stores the managed sub controllers. */
        private final Collection<ReloadingController> controllers;

        /**
         * Creates a new instance of {@code MultiReloadingControllerDetector}
         * and sets the managed controllers.
         *
         * @param ctrls a collection with the managed controllers
         */
        public MultiReloadingControllerDetector(
                Collection<ReloadingController> ctrls)
        {
            controllers = ctrls;
        }

        /**
         * {@inheritDoc} This implementation delegates to the managed
         * controllers. If one of them returns <b>true</b> from its check
         * method, iteration is aborted, and result is <b>true</b>.
         */
        public boolean isReloadingRequired()
        {
            for (ReloadingController rc : getControllers())
            {
                if (rc.checkForReloading(null) || rc.isInReloadingState())
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * {@inheritDoc} This implementation resets the reloading state on all
         * managed controllers.
         */
        public void reloadingPerformed()
        {
            for (ReloadingController rc : getControllers())
            {
                rc.resetReloadingState();
            }
        }

        /**
         * Returns the collection with managed sub controllers.
         *
         * @return the controllers the sub controllers
         */
        Collection<ReloadingController> getControllers()
        {
            return controllers;
        }
    }
}
