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
package org.apache.commons.configuration.event;

/**
 * <p>
 * An interface for configuration implementations which support registration of
 * event listeners.
 * </p>
 * <p>
 * Through the methods provided by this interface it is possible to register and
 * remove event listeners for configuration events. Both configuration change
 * listeners and error listeners are supported.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public interface EventSource
{
    /**
     * Adds a configuration listener to this object.
     *
     * @param l the listener to add
     */
    void addConfigurationListener(ConfigurationListener l);

    /**
     * Removes the specified event listener so that it does not receive any
     * further events caused by this object.
     *
     * @param l the listener to be removed
     * @return a flag whether the event listener was found
     */
    boolean removeConfigurationListener(ConfigurationListener l);

    /**
     * Adds a new configuration error listener to this object. This listener
     * will then be notified about internal problems.
     *
     * @param l the listener to register (must not be <b>null</b>)
     * @since 1.4
     */
    void addErrorListener(ConfigurationErrorListener l);

    /**
     * Removes the specified error listener so that it does not receive any
     * further events caused by this object.
     *
     * @param l the listener to remove
     * @return a flag whether the listener could be found and removed
     * @since 1.4
     */
    boolean removeErrorListener(ConfigurationErrorListener l);
}
