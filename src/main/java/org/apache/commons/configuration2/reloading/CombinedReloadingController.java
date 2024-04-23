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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * <p>
 * A specialized {@code ReloadingController} implementation which manages an arbitrary number of other
 * {@code ReloadingController} objects.
 * </p>
 * <p>
 * This class can be used to handle multiple simple controllers for reload operations as a single object. As a usage
 * example consider a combined configuration containing a number of configuration sources of which some support
 * reloading. In this scenario all {@code ReloadingController} instances for the reloading-enabled sources can be added
 * to a {@code CombinedReloadingController}. Then by triggering the combined controller a reload check is performed on
 * all child sources.
 * </p>
 * <p>
 * This class is a typical implementation of the <em>composite pattern</em>. An instance is constructed with a
 * collection of sub {@code ReloadingController} objects. Its operations are implemented by delegating to all child
 * controllers.
 * </p>
 * <p>
 * This class expects the managed controller objects to be passed to the constructor. From this list a defensive copy is
 * created so that it cannot be changed later on. Derived classes can override the {@link #getSubControllers()} method
 * if they need another way to handle child controllers (e.g. a more dynamic way). However, they are then responsible to
 * ensure a safe access to this list in a multi-threaded environment.
 * </p>
 *
 * @since 2.0
 */
public class CombinedReloadingController extends ReloadingController {
    /**
     * A specialized implementation of the {@code ReloadingDetector} interface which operates on a collection of
     * {@code ReloadingController} objects. The methods defined by the {@code ReloadingDetector} interface are delegated to
     * the managed controllers.
     */
    private static final class MultiReloadingControllerDetector implements ReloadingDetector {
        /** A reference to the owning combined reloading controller. */
        private final CombinedReloadingController owner;

        /**
         * Creates a new instance of {@code MultiReloadingControllerDetector}.
         *
         * @param owner the owner
         */
        public MultiReloadingControllerDetector(final CombinedReloadingController owner) {
            this.owner = owner;
        }

        /**
         * {@inheritDoc} This implementation delegates to the managed controllers. For all of them the
         * {@code checkForReloading()} method is called, giving them the chance to trigger a reload if necessary. If one of
         * these calls returns <b>true</b>, the result of this method is <b>true</b>, otherwise <b>false</b>.
         */
        @Override
        public boolean isReloadingRequired() {
            return owner.getSubControllers().stream().reduce(false, (b, rc) -> b | rc.checkForReloading(null), (t, u) -> t | u);
        }

        /**
         * {@inheritDoc} This implementation resets the reloading state on all managed controllers.
         */
        @Override
        public void reloadingPerformed() {
            owner.getSubControllers().forEach(ReloadingController::resetReloadingState);
        }
    }

    /** Constant for a dummy reloading detector. */
    private static final ReloadingDetector DUMMY = new MultiReloadingControllerDetector(null);

    /**
     * Checks the collection with the passed in sub controllers and creates a defensive copy.
     *
     * @param subCtrls the collection with sub controllers
     * @return a copy of the collection to be stored in the newly created instance
     * @throws IllegalArgumentException if the passed in collection is <b>null</b> or contains <b>null</b> entries
     */
    private static Collection<ReloadingController> checkManagedControllers(final Collection<? extends ReloadingController> subCtrls) {
        if (subCtrls == null) {
            throw new IllegalArgumentException("Collection with sub controllers must not be null!");
        }
        final Collection<ReloadingController> ctrls = new ArrayList<>(subCtrls);
        if (ctrls.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Collection with sub controllers contains a null entry!");
        }

        return Collections.unmodifiableCollection(ctrls);
    }

    /** The collection with managed reloading controllers. */
    private final Collection<ReloadingController> controllers;

    /** The reloading detector used by this instance. */
    private final ReloadingDetector detector;

    /**
     * Creates a new instance of {@code CombinedReloadingController} and initializes it with the {@code ReloadingController}
     * objects to be managed.
     *
     * @param subCtrls the collection with sub {@code ReloadingController}s (must not be <b>null</b> or contain <b>null</b>
     *        entries)
     * @throws IllegalArgumentException if the passed in collection is <b>null</b> or contains <b>null</b> entries
     */
    public CombinedReloadingController(final Collection<? extends ReloadingController> subCtrls) {
        super(DUMMY);
        controllers = checkManagedControllers(subCtrls);
        detector = new MultiReloadingControllerDetector(this);
    }

    /**
     * {@inheritDoc} This implementation returns a special reloading detector which operates on all managed controllers.
     */
    @Override
    public ReloadingDetector getDetector() {
        return detector;
    }

    /**
     * Gets a (unmodifiable) collection with the sub controllers managed by this combined controller.
     *
     * @return a collection with sub controllers
     */
    public Collection<ReloadingController> getSubControllers() {
        return controllers;
    }

    /**
     * Resets the reloading state of all managed sub controllers unconditionally. This method is intended to be called after
     * the creation of an instance. It may be the case that some of the sub controllers are already in reloading state, so
     * their state is out of sync with this controller's global reloading state. This method ensures that the reloading
     * state of all sub controllers is reset.
     */
    public void resetInitialReloadingState() {
        getDetector().reloadingPerformed();
    }
}
