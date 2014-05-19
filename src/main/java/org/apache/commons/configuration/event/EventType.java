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
 * A class representing an event type.
 * </p>
 * <p>
 * The events produced by <em>Commons Configuration</em> all have a specific
 * type. The event type can be used to determine the meaning of a specific
 * event. It also acts as filter criterion when event listeners are registered.
 * The listener is then called only for events of this type or derived types.
 * The events in this library form a natural hierarchy with base types and more
 * specialized types. By specifying an appropriate event type at listener
 * registration time, it can be determined on a fine-granular basis which events
 * are propagated to the listener.
 * </p>
 * <p>
 * Note: Users familiar with JavaFX probably recognize this approach to event
 * handling. It allows for generic event listener interfaces and a natural
 * selection of events to be processed.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 * @param <T> the event associated with this type
 */
public class EventType<T>
{
    /** Constant for the format used by toString(). */
    private static final String FMT_TO_STRING = "%s [ %s ]";

    /** Stores the super type of this type. */
    private final EventType<? super T> superType;

    /** A name for this event type. */
    private final String name;

    /**
     * Creates a new instance of {@code EventType} and initializes it with the
     * super type and a type name. If no super type is specified, this is the
     * root event type.
     *
     * @param superEventType the super event type
     * @param typeName the name of this event type
     */
    public EventType(EventType<? super T> superEventType, String typeName)
    {
        superType = superEventType;
        name = typeName;
    }

    /**
     * Returns the super event type. Result is <b>null</b> for the root event
     * type.
     *
     * @return the super event type
     */
    public EventType<? super T> getSuperType()
    {
        return superType;
    }

    /**
     * Returns the name of this event type. The name has no specific semantic
     * meaning. It is just used for debugging purposes and also part of the
     * string representation of this event type.
     *
     * @return the event type name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns a string representation for this object. This method is mainly
     * overridden for debugging purposes. The returned string contains the name
     * of this event type.
     *
     * @return a string for this object
     */
    @Override
    public String toString()
    {
        return String.format(FMT_TO_STRING, getClass().getSimpleName(),
                getName());
    }
}
