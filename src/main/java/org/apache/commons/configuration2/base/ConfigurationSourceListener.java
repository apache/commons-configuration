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
package org.apache.commons.configuration2.base;

import java.util.EventListener;

/**
 * <p>
 * An event listener interface to be implemented by observers of
 * {@link ConfigurationSource} objects.
 * </p>
 * <p>
 * This interface defines a callback method that is invoked on each manipulation
 * of a {@link ConfigurationSource}. It allows interested components to keep
 * track on the changes of a {@link ConfigurationSource}. The exact nature of
 * the change can be determined by inspecting the
 * {@link ConfigurationSourceEvent} object passed to the event listener method.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 * @see ConfigurationSourceEvent
 */
public interface ConfigurationSourceListener extends EventListener
{
    /**
     * Notifies this listener about a change on a monitored
     * {@link ConfigurationSource} object.
     *
     * @param event the event describing the change
     */
    void configurationSourceChanged(ConfigurationSourceEvent event);
}
