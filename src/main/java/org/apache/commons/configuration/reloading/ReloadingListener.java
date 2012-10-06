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
package org.apache.commons.configuration.reloading;

import java.util.EventListener;

/**
 * <p>
 * An event listener interface to be implemented by objects that want to be
 * notified about reloading events.
 * </p>
 * <p>
 * Objects implementing this interface can be registered at a
 * {@link ReloadingController}. They are then notified whenever a check of the
 * controller detects that a reload operation should be performed. This can be
 * useful for instance to find out that configuration settings might have
 * changed.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public interface ReloadingListener extends EventListener
{
    /**
     * Notifies this listener that a {@link ReloadingController} detected the
     * necessity to reload data. More details are available through the passed
     * in {@code ReloadingEvent} object.
     *
     * @param event a {@code ReloadingEvent} with details
     */
    void reloadingRequired(ReloadingEvent event);
}
