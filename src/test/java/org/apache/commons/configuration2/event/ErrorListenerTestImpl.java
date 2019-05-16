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

import static org.junit.Assert.assertEquals;

/**
 * A test event listener implementation for error events.
 *
 */
public class ErrorListenerTestImpl extends
        AbstractEventListenerTestImpl<ConfigurationErrorEvent>
{
    /**
     * Creates a new instance of {@code ErrorListenerTestImpl} and sets the
     * expected event source.
     *
     * @param source the event source (<b>null</b> if the source need not to be
     *        checked)
     */
    public ErrorListenerTestImpl(final Object source)
    {
        super(source);
    }

    /**
     * Checks the next event which has been received by this listener.
     *
     * @param type the expected event type
     * @param opType the event type for the failed operation
     * @param propName the expected property name
     * @param propValue the expected property value
     * @return the exception stored in the next error event
     */
    public Throwable checkEvent(final EventType<?> type, final EventType<?> opType,
            final String propName, final Object propValue)
    {
        final ConfigurationErrorEvent e = nextEvent(type);
        assertEquals("Wrong operation event type", opType,
                e.getErrorOperationType());
        assertEquals("Wrong property name", propName, e.getPropertyName());
        assertEquals("Wrong property value", propValue, e.getPropertyValue());
        return e.getCause();
    }
}
