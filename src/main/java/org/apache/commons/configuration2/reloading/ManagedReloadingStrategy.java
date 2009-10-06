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

package org.apache.commons.configuration2.reloading;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.configuration2.FileConfiguration;

/**
 * A strategy to reload configuration based on management requests. Designed for
 * JMX management.
 *
 * @author Nicolas De loof
 */
public class ManagedReloadingStrategy implements ReloadingStrategy, ManagedReloadingStrategyMBean
{
    /** The logger. */
    private Log log = LogFactory.getLog(getClass().getName());

    /** Stores a reference to the associated configuration. */
    private FileConfiguration configuration;

    /** A flag whether a reload is required. */
    private boolean reloadingRequired;

    /**
     * @see org.apache.commons.configuration2.reloading.ReloadingStrategy#init()
     */
    public void init()
    {
    }

    /**
     * @see org.apache.commons.configuration2.reloading.ReloadingStrategy#reloadingPerformed()
     */
    public void reloadingPerformed()
    {
        reloadingRequired = false;
    }

    /**
     * Checks whether reloading is required. This implementation checks whether
     * the <code>refresh()</code> method has been invokded.
     *
     * @return a flag whether reloading is required
     * @see org.apache.commons.configuration2.reloading.ReloadingStrategy#reloadingRequired()
     */
    public boolean reloadingRequired()
    {
        return reloadingRequired;
    }

    /**
     * Sets the associated configuration.
     *
     * @param configuration the associated configuration
     */
    public void setConfiguration(FileConfiguration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Tells this strategy that the monitored configuration file should be
     * refreshed. This method will typically be called from outside (through an
     * exposed MBean) on behalf of an administrator.
     *
     * @see org.apache.commons.configuration2.reloading.ManagedReloadingStrategyMBean#refresh()
     */
    public void refresh()
    {
        log.info("Reloading configuration.");
        this.reloadingRequired = true;
        // force reloading
        configuration.isEmpty();
    }
}
