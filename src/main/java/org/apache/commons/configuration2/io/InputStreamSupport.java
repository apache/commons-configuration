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
package org.apache.commons.configuration2.io;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * <p>
 * Definition of an interface to be implemented by objects which support reading
 * from an input stream.
 * </p>
 * <p>
 * When reading data using a {@link FileHandler} per default a reader is used as
 * defined by the {@link FileBased#read(java.io.Reader)} method. For some
 * configuration formats it is necessary to directly read binary data. In order
 * to achieve this, a {@link FileBased} object can also implement this
 * interface. It defines an additional {@code read()} method expecting an
 * {@code InputStream} as argument. If the {@code FileHandler} detects that its
 * associated {@code FileBased} object implements this interface, it passes the
 * input stream directly rather than transforming it to a reader.
 * </p>
 *
 * @since 2.0
 */
public interface InputStreamSupport
{
    /**
     * Reads the content of this object from the specified {@code InputStream}.
     *
     * @param in the input stream
     * @throws ConfigurationException if a non-I/O related problem occurs, e.g.
     *         the data read does not have the expected format
     * @throws IOException if an I/O error occurs
     */
    void read(InputStream in) throws ConfigurationException, IOException;
}
