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
package org.apache.commons.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.configuration.event.ConfigurationErrorEvent;
import org.apache.commons.configuration.event.ConfigurationErrorListener;

/**
 * An implementation of the {@code ConfigurationErrorListener} interface
 * that can be used in unit tests. This implementation just records received
 * events and allows to test whether expected errors occurred.
 *
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 */
public class ConfigurationErrorListenerImpl implements
        ConfigurationErrorListener
{
    /** Stores the last received error event. */
    private ConfigurationErrorEvent event;

    /** Stores the number of calls to configurationError(). */
    private int errorCount;

    /**
     * An error event is received. Updates the internal counter and stores the
     * event.
     *
     * @param event the error event
     */
    @Override
    public void configurationError(ConfigurationErrorEvent event)
    {
        this.event = event;
        errorCount++;
    }

    /**
     * Returns the last received error event.
     *
     * @return the last error event (may be <b>null</b>)
     */
    public ConfigurationErrorEvent getLastEvent()
    {
        return event;
    }

    /**
     * Returns the number of received error events.
     *
     * @return the number of error events
     */
    public int getErrorCount()
    {
        return errorCount;
    }

    /**
     * Checks whether no error event was received.
     */
    public void verify()
    {
        assertEquals("Error events received", 0, errorCount);
    }

    /**
     * Checks whether an expected error event was received. This is a
     * convenience method for checking whether exactly one event of a certain
     * type was received.
     *
     * @param type the type of the event
     * @param propName the name of the property
     * @param propValue the value of the property
     */
    public void verify(int type, String propName, Object propValue)
    {
        assertEquals("Wrong number of error events", 1, errorCount);
        assertEquals("Wrong event type", type, event.getType());
        assertTrue("Wrong property name", (propName == null) ? event
                .getPropertyName() == null : propName.equals(event
                .getPropertyName()));
        assertTrue("Wrong property value", (propValue == null) ? event
                .getPropertyValue() == null : propValue.equals(event
                .getPropertyValue()));
    }
}
