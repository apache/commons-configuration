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
import org.apache.commons.configuration2.reloading.ReloadingDetector;

/**
 * <p>
 * Definition of an interface for objects which can create a
 * {@link ReloadingDetector}.
 * </p>
 * <p>
 * This interface is used by {@link ReloadingFileBasedConfigurationBuilder} to
 * create detector objects for configuration sources supporting reloading.
 * </p>
 *
 * @since 2.0
 */
public interface ReloadingDetectorFactory
{
    /**
     * Creates a new {@code ReloadingDetector} object based on the passed in
     * parameters. The {@code FileHandler} points to the file to be monitored.
     * (It may be different from the {@code FileHandler} managed by the
     * parameters object.) The {@code FileBasedBuilderParametersImpl} object may
     * contain additional information for configuring the detector, e.g. a
     * refresh delay.
     *
     * @param handler the handler of the file to be monitored
     * @param params parameters related to file-based configurations
     * @return the newly created {@code ReloadingDetector}
     * @throws ConfigurationException if an error occurs
     */
    ReloadingDetector createReloadingDetector(FileHandler handler,
            FileBasedBuilderParametersImpl params)
            throws ConfigurationException;
}
