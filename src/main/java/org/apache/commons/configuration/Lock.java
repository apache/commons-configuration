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

/**
 * <p>
 * A simple class acting as lock.
 * </p>
 * <p>
 * Instances of this class are used by some configuration classes to synchronize
 * themselves.
 * </p>
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @since 1.7
 * @version $Id$
 */
public class Lock
{
    /** A string used internally to synchronize counter updates. */
    private static String counterLock = "Lock";

    /** A counter for generating unique instance IDs. */
    private static int counter;

    /** The name of this lock. */
    private final String name;

    /** The unique ID of this lock instance. */
    private final int instanceId;

    /**
     * Creates a new instance of {@code Lock} with the specified name.
     *
     * @param name the name of this lock
     */
    public Lock(String name)
    {
        this.name = name;
        synchronized (counterLock)
        {
            instanceId = ++counter;
        }
    }

    /**
     * Returns the name of this lock.
     *
     * @return the name of this lock
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns a string representation of this object. This implementation
     * returns a string which contains the lock name and the instance ID.
     *
     * @return a string for this object
     */
    @Override
    public String toString()
    {
        return "Lock: " + name + " id = " + instanceId + ": "
                + super.toString();
    }
}
