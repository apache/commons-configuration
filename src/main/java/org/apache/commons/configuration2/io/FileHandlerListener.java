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
package org.apache.commons.configuration2.io;

/**
 * <p>
 * A listener interface for receiving notifications about updates of a
 * {@code FileHandler}.
 * </p>
 * <p>
 * Objects implementing this interface are notified when properties of a
 * {@code FileHandler} change or when a load or save operation is performed.
 * This can be useful for various use cases, e.g. when monitoring file-based
 * configurations.
 * </p>
 *
 * @since 2.0
 */
public interface FileHandlerListener
{
    /**
     * Notification that the associated file is about to be loaded. This method
     * is called immediately before the load operation.
     *
     * @param handler the file handler
     */
    void loading(FileHandler handler);

    /**
     * Notification that the associated file has been loaded. This method is
     * called directly after the load operation.
     *
     * @param handler the file handler
     */
    void loaded(FileHandler handler);

    /**
     * Notification that the associated file is about to be saved. This method
     * is called immediately before the save operation.
     *
     * @param handler the file handler
     */
    void saving(FileHandler handler);

    /**
     * Notification that the associated file has been saved. This method is
     * called directly after the save operation.
     *
     * @param handler the file handler
     */
    void saved(FileHandler handler);

    /**
     * Notification that a property of the monitored {@code FileHandler} has
     * changed.
     *
     * @param handler the file handler
     */
    void locationChanged(FileHandler handler);
}
