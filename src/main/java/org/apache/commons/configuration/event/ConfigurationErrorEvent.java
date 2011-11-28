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
 * An event class that is used for reporting errors that occurred while
 * processing configuration properties.
 * </p>
 * <p>
 * Some configuration implementations (e.g.
 * {@link org.apache.commons.configuration.DatabaseConfiguration}
 * or {@link org.apache.commons.configuration.JNDIConfiguration}
 * use an underlying storage that can throw an exception on each property
 * access. In earlier versions of this library such exceptions were logged and
 * then silently ignored. This makes it impossible for a client to find out that
 * something went wrong.
 * </p>
 * <p>
 * To give clients better control over the handling of errors that occur during
 * access of a configuration object a new event listener mechanism specific for
 * exceptions is introduced: Clients can register itself at a configuration
 * object as an <em>error listener</em> and are then notified about all
 * internal errors related to the source configuration object.
 * </p>
 * <p>
 * By inheriting from {@code ConfigurationEvent} this event class
 * supports all properties that describe an operation on a configuration
 * instance. In addition a {@code Throwable} object is available
 * representing the occurred error. The event's type determines the operation
 * that caused the error. Note that depending on the event type and the occurred
 * exception not all of the other properties (e.g. name of the affected property
 * or its value) may be available.
 * </p>
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 * @since 1.4
 * @see ConfigurationEvent
 */
public class ConfigurationErrorEvent extends ConfigurationEvent
{
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = -7433184493062648409L;

    /** Stores the exception that caused this event. */
    private Throwable cause;

    /**
     * Creates a new instance of {@code ConfigurationErrorEvent} and
     * initializes it.
     *
     * @param source the event source
     * @param type the event's type
     * @param propertyName the name of the affected property
     * @param propertyValue the value of the affected property
     * @param cause the exception object that caused this event
     */
    public ConfigurationErrorEvent(Object source, int type,
            String propertyName, Object propertyValue, Throwable cause)
    {
        super(source, type, propertyName, propertyValue, true);
        this.cause = cause;
    }

    /**
     * Returns the cause of this error event. This is the {@code Throwable}
     * object that caused this event to be fired.
     *
     * @return the cause of this error event
     */
    public Throwable getCause()
    {
        return cause;
    }
}
