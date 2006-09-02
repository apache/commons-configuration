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
 * A simple event listener interface for configuration observers.
 * </p>
 * <p>
 * This interface can be implemented by classes that are interested in
 * &quot;raw&quot; events caused by configuration objects. Each manipulation on
 * a configuration object will generate such an event. There is only a single
 * method that is invoked when an event occurs.
 * </p>
 *
 * @author <a
 * href="http://jakarta.apache.org/commons/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 * @since 1.3
 */
public interface ConfigurationListener
{
    /**
     * Notifies this listener about a manipulation on a monitored configuration
     * object.
     *
     * @param event the event describing the manipulation
     */
    void configurationChanged(ConfigurationEvent event);
}
