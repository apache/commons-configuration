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
package org.apache.commons.configuration.builder;

import java.util.EventListener;

import org.apache.commons.configuration.Configuration;

/**
 * <p>
 * A listener interface for receiving notifications when the status of a
 * {@link ConfigurationBuilder} is changed.
 * </p>
 * <p>
 * There are use cases when it is of interest to monitor the status of a
 * {@code ConfigurationBuilder}. Especially, if the builder creates new result
 * objects dynamically, it may be important to know when it was reset because it
 * will then return a new result object. This could also mean that the
 * {@code Configuration} object managed by the builder has changed.
 * </p>
 * <p>
 * This interface defines a single method which gets called when the builder was
 * reset. A concrete implementation could react on this notification for
 * instance by querying a new result object or by marking a reference to the
 * current {@code Configuration} as stale.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public interface BuilderListener extends EventListener
{
    /**
     * Notifies this listener that a reset was performed on the specified
     * {@code ConfigurationBuilder}.
     *
     * @param builder the builder
     */
    void builderReset(ConfigurationBuilder<? extends Configuration> builder);
}
