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

/**
 * <p>
 * An interface to be implemented by objects which can detect whether a reload
 * operation is required.
 * </p>
 * <p>
 * This interface is used by a {@link ReloadingController} object. When a
 * reloading check is to be performed, it is delegated to a concrete
 * implementation. The implementation decides whether (specific) criteria for a
 * reload are fulfilled, so that the controller can react accordingly.
 * </p>
 * <p>
 * This interface does not define how a check for a reload is performed. This is
 * completely up to a concrete implementation. There is just one method for
 * executing the check and one method to notify the {@code ReloadingDetector}
 * that the reload actually happened; this method can be used to reset the
 * internal state so that the conditions for the next reload can be detected.
 * </p>
 * <p>
 * When used together with {@code ReloadingController} an implementation does
 * not have to be thread-safe. The controller takes care for synchronization so
 * that an instance is accessed by a single thread only.
 * </p>
 *
 * @since 2.0
 */
public interface ReloadingDetector
{
    /**
     * Checks whether all criteria for a reload operation are fulfilled. This
     * method is called by external components to find out when reloading should
     * take place.
     *
     * @return <b>true</b> if a reload operation should be performed,
     *         <b>false</b> otherwise
     */
    boolean isReloadingRequired();

    /**
     * Notifies this object that a reload operation has been performed. This
     * method is called after {@code reloadingRequired()} has returned
     * <b>true</b>. It can be used to reset internal state in order to detect
     * the next reload operation.
     */
    void reloadingPerformed();
}
