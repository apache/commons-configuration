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
 * An event class that is used for reporting errors that occurred while
 * processing configuration properties.
 * </p>
 * <p>
 * Some configuration implementations (e.g.
 * {@link org.apache.commons.configuration2.DatabaseConfiguration} or
 * {@link org.apache.commons.configuration2.JNDIConfiguration} use an underlying
 * storage that can throw an exception on each property access. In earlier
 * versions of this library such exceptions were logged and then silently
 * ignored. This makes it impossible for a client to find out that something
 * went wrong.
 * </p>
 * <p>
 * To give clients better control over the handling of errors that might occur
 * while interacting with a configuration object, a specialized error event type
 * is introduced. Clients can register as listeners of this event type at a
 * configuration object and are then notified about all internal errors related
 * to the source configuration object.
 * </p>
 * <p>
 * This class defines similar properties to the {@link ConfigurationEvent}
 * class. This makes it possible to find out which operation was performed on a
 * configuration causing this error event. In addition, a {@code Throwable}
 * object is available representing the occurred error. Note that depending on
 * the event type and the occurred exception not all of the other properties
 * (e.g. name of the affected property or its value) may be available.
 * </p>
 *
 * @since 1.4
 * @see ConfigurationEvent
 */
public class ConfigurationErrorEvent extends Event
{
    /**
     * Constant for the common event type for all error events. Specific types
     * for error events use this type as super type.
     *
     * @since 2.0
     */
    public static final EventType<ConfigurationErrorEvent> ANY =
            new EventType<>(Event.ANY, "ERROR");

    /**
     * Constant for the event type indicating a read error. Errors of this type
     * are generated if the underlying data store throws an exception when
     * reading a property.
     *
     * @since 2.0
     */
    public static final EventType<ConfigurationErrorEvent> READ =
            new EventType<>(ANY, "READ_ERROR");

    /**
     * Constant for the event type indicating a write error. Errors of this type
     * are generate if the underlying data store throws an exception when
     * updating data.
     *
     * @since 2.0
     */
    public static final EventType<ConfigurationErrorEvent> WRITE =
            new EventType<>(ANY, "WRITE_ERROR");

    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 20140712L;

    /** The event type of the operation which caused this error. */
    private final EventType<?> errorOperationType;

    /** Stores the property name. */
    private final String propertyName;

    /** Stores the property value. */
    private final Object propertyValue;

    /** Stores the exception that caused this event. */
    private final Throwable cause;

    /**
     * Creates a new instance of {@code ConfigurationErrorEvent} and sets all
     * its properties.
     *
     * @param source the event source
     * @param eventType the type of this event
     * @param operationType the event type of the operation causing this error
     * @param propName the name of the affected property
     * @param propValue the value of the affected property
     * @param cause the exception object that caused this event
     */
    public ConfigurationErrorEvent(final Object source,
            final EventType<? extends ConfigurationErrorEvent> eventType,
            final EventType<?> operationType, final String propName, final Object propValue,
            final Throwable cause)
    {
        super(source, eventType);
        errorOperationType = operationType;
        propertyName = propName;
        propertyValue = propValue;
        this.cause = cause;
    }

    /**
     * Returns the {@code EventType} of the operation which caused this error.
     *
     * @return the event type of the operation causing this error
     */
    public EventType<?> getErrorOperationType()
    {
        return errorOperationType;
    }

    /**
     * Returns the name of the property that was accessed when this error
     * occurred.
     *
     * @return the property name related to this error (may be <b>null</b>)
     */
    public String getPropertyName()
    {
        return propertyName;
    }

    /**
     * Returns the value of the property that was accessed when this error
     * occurred.
     *
     * @return the property value related this error (may be <b>null</b>)
     */
    public Object getPropertyValue()
    {
        return propertyValue;
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
