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

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileSystem;

/**
 * <p>
 * Definition of a properties interface for parameters of file-based configurations.
 * </p>
 * <p>
 * This interface defines a set of properties which can be used to specify the
 * location of a configuration source.
 * </p>
 *
 * @param <T> the type of the result of all set methods for method chaining
 */
public interface FileBasedBuilderProperties<T>
{
    /**
     * Sets the refresh delay for reloading support
     *
     * @param reloadingRefreshDelay the refresh delay (in milliseconds)
     * @return a reference to this object for method chaining
     */
    T setReloadingRefreshDelay(Long reloadingRefreshDelay);

    /**
     * Sets the factory for creating {@code ReloadingDetector} objects. With
     * this method a custom factory for reloading detectors can be installed.
     * Per default, a factory creating {@code FileHandlerReloadingDetector}
     * objects is used.
     *
     * @param factory the {@code ReloadingDetectorFactory}
     * @return a reference to this object for method chaining
     */
    T setReloadingDetectorFactory(ReloadingDetectorFactory factory);

    /**
     * Sets the location of the associated {@code FileHandler} as a {@code File}
     * object.
     *
     * @param file the {@code File} location
     * @return a reference to this object for method chaining
     */
    T setFile(File file);

    /**
     * Sets the location of the associated {@code FileHandler} as a {@code URL}
     * object.
     *
     * @param url the {@code URL} location
     * @return a reference to this object for method chaining
     */
    T setURL(URL url);

    /**
     * Sets the location of the associated {@code FileHandler} as an absolute
     * file path.
     *
     * @param path the path location
     * @return a reference to this object for method chaining
     */
    T setPath(String path);

    /**
     * Sets the file name of the associated {@code FileHandler}.
     *
     * @param name the file name
     * @return a reference to this object for method chaining
     */
    T setFileName(String name);

    /**
     * Sets the base path of the associated {@code FileHandler}.
     *
     * @param path the base path
     * @return a reference to this object for method chaining
     */
    T setBasePath(String path);

    /**
     * Sets the {@code FileSystem} of the associated {@code FileHandler}.
     *
     * @param fs the {@code FileSystem}
     * @return a reference to this object for method chaining
     */
    T setFileSystem(FileSystem fs);

    /**
     * Sets the {@code FileLocationStrategy} for resolving the referenced file.
     *
     * @param strategy the {@code FileLocationStrategy}
     * @return a reference to this object for method chaining
     */
    T setLocationStrategy(FileLocationStrategy strategy);

    /**
     * Sets the encoding of the associated {@code FileHandler}.
     *
     * @param enc the encoding
     * @return a reference to this object for method chaining
     */
    T setEncoding(String enc);
}
