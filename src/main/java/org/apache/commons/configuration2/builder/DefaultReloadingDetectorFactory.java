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
package org.apache.commons.configuration2.builder;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.reloading.FileHandlerReloadingDetector;
import org.apache.commons.configuration2.reloading.ReloadingDetector;

/**
 * <p>
 * A default implementation of the {@code ReloadingDetectorFactory} interface.
 * </p>
 * <p>
 * This factory creates objects of type {@link FileHandlerReloadingDetector}.
 * Instances have no state and can be shared between multiple builders.
 * </p>
 *
 * @since 2.0
 */
public class DefaultReloadingDetectorFactory implements
        ReloadingDetectorFactory
{
    @Override
    public ReloadingDetector createReloadingDetector(final FileHandler handler,
            final FileBasedBuilderParametersImpl params)
            throws ConfigurationException
    {
        final Long refreshDelay = params.getReloadingRefreshDelay();

        final FileHandlerReloadingDetector fileHandlerReloadingDetector =
                (refreshDelay != null) ? new FileHandlerReloadingDetector(
                handler, refreshDelay) : new FileHandlerReloadingDetector(
                handler);

        fileHandlerReloadingDetector.refresh();

        return fileHandlerReloadingDetector;
    }
}
