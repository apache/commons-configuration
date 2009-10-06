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
package org.apache.commons.configuration2.fs;

import java.net.URL;

/**
 * <p>
 * Definition of an interface for locating configuration sources.
 * </p>
 * <p>
 * Configuration data is typically retrieved from various locations, e.g. from
 * the local file system, from a jar archive in the class path, or even from a
 * URL over the network. This interface provides an easy yet flexible way of
 * dealing with all these different locations.
 * </p>
 * <p>
 * A {@code Locator} simply provides a URL to the data to be loaded (or
 * written). Configuration sources that are capable of dealing with locators
 * interpret these URLs with the help of the {@link FileSystem} classes and open
 * corresponding streams for the I/O operations to be performed.
 * </p>
 * <p>
 * There are already some default {@code Locator} implementations for typical
 * use cases. For special requirements it should not be too complicated to
 * create a custom implementation of this interface. Here an arbitrary complex
 * search strategy for configuration data can be defined.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public interface Locator
{
    /**
     * Returns a {@code URL} to the configuration data pointed by this {@code
     * Locator}. When a configuration source is to be loaded this method is
     * called with the argument <b>false</b>. Analogous, for writing a
     * configuration source, {@code getURL()} is called with the parameter
     * <b>true</b>. It is up to a concrete implementation whether it
     * distinguishes between input and output URLs and which algorithm it uses
     * for locating the actual data. The configuration sources just use the URLs
     * returned by this method for reading and writing their data. No additional
     * checks are performed on these URLs. An implementation should never return
     * <b>null</b>. In case of an error a runtime exception - preferably a
     * {@code ConfigurationRuntimeException} - should be thrown.
     *
     * @param output <b>true</b> if the URL is to be used for writing data;
     *        <b>false</b> if it is to be used for reading data
     * @return the {@code URL} pointing to configuration data
     */
    URL getURL(boolean output);
}
