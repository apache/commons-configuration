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
 *
 */
public class HierarchicalReloadableConfiguration extends HierarchicalConfiguration
    implements Reloadable
{
    private final Object reloadLock;

    private static final String LOCK_NAME = "HierarchicalReloadableConfigurationLock";

    /**
     * Creates a new instance of <code>HierarchicalReloadableConfiguration</code>.
     */
    public HierarchicalReloadableConfiguration()
    {
        super();
        reloadLock = new Lock(LOCK_NAME);
    }

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
     * @since 1.4
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
