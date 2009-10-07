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
package org.apache.commons.configuration2.base;

import java.io.Reader;
import java.io.Writer;

import org.apache.commons.configuration2.ConfigurationException;

/**
 * <p>
 * Definition of an interface for {@code ConfigurationSource} implementations
 * that read or write their data from or to streams.
 * </p>
 * <p>
 * This interface is used by the default implementation of the
 * {@link LocatorSupport} interface: The various methods for loading and saving
 * configuration data are all reduced to a load operation from a {@code Reader}
 * and a save operation to a {@code Writer} respective. So concrete file-based
 * configuration sources can focus on the implementation of these two methods
 * while the whole functionality of managing a default {@code Locator} or
 * providing various load and save methods is handled by a specialized
 * component.
 * </p>
 *
 * @author Commons Configuration team
 * @version $Id$
 */
public interface StreamBasedSource
{
    /**
     * Loads data for this configuration source from the specified {@code
     * Reader}. Note: The caller is responsible for closing the reader.
     *
     * @param reader the {@code Reader} to load the data from
     * @throws ConfigurationException if an I/O error occurs
     */
    void load(Reader reader) throws ConfigurationException;

    /**
     * Saves the data of this configuration source to the specified {@code
     * Writer}. Note: The caller is responsible for closing the reader.
     *
     * @param writer the {@code Writer} for storing the data
     * @throws ConfigurationException if an I/O error occurs
     */
    void save(Writer writer) throws ConfigurationException;
}
