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

import org.apache.commons.configuration.reloading.Reloadable;

/**
 * <p>A base class for hierarchical configurations with specific reloading
 * requirements.</p>
 * <p>This class manages a lock object which can be used for synchronization.</p>
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @since 1.7
 * @version $Id$
 */
public class HierarchicalReloadableConfiguration extends HierarchicalConfiguration
    implements Reloadable
{
    /** Constant for the name used for the lock object. */
    private static final String LOCK_NAME = "HierarchicalReloadableConfigurationLock";

    /** The lock object used by this instance. */
    private final Object reloadLock;

    /**
     * Creates a new instance of <code>HierarchicalReloadableConfiguration</code>.
     */
    public HierarchicalReloadableConfiguration()
    {
        super();
        reloadLock = new Lock(LOCK_NAME);
    }

    /**
     * Creates a new instance of
     * <code>HierarchicalReloadableConfiguration</code> and initializes it with
     * the given lock object.
     *
     * @param lock the lock object
     */
    public HierarchicalReloadableConfiguration(Object lock)
    {
        super();
        reloadLock = lock == null ? new Lock(LOCK_NAME) : lock;
    }

    /**
     * Creates a new instance of <code>HierarchicalConfiguration</code> and
     * copies all data contained in the specified configuration into the new
     * one.
     *
     * @param c the configuration that is to be copied (if <b>null</b>, this
     * constructor will behave like the standard constructor)
     */
    public HierarchicalReloadableConfiguration(HierarchicalConfiguration c)
    {
        super(c);
        reloadLock = new Lock(LOCK_NAME);
    }

    public Object getReloadLock()
    {
        return reloadLock;
    }
}
