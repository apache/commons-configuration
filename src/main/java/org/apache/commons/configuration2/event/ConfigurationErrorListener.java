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
package org.apache.commons.configuration2.event;

/**
 * <p>
 * An event listener interface to be implemented by observers that are
 * interested in internal errors caused by processing of configuration
 * properties.
 * </p>
 * <p>
 * Some configuration classes use an underlying storage where each access of a
 * property can cause an exception. In earlier versions of this library such
 * exceptions were typically ignored. By implementing this interface and
 * registering at a configuration object as an error listener it is now possible
 * for clients to receive notifications about those internal problems.
 * </p>
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 * @since 1.4
 * @see ConfigurationErrorEvent
 */
public interface ConfigurationErrorListener
{
    /**
     * Notifies this listener that in an observed configuration an error
     * occurred. All information available about this error, including the
     * causing <code>Throwable</code> object, can be obtained from the passed
     * in event object.
     *
     * @param event the event object with information about the error
     */
    void configurationError(ConfigurationErrorEvent event);
}
