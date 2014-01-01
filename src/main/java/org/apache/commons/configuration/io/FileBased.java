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
package org.apache.commons.configuration.io;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.configuration.ex.ConfigurationException;

/**
 * <p>
 * Definition of an interface to be implemented by objects which know how to
 * read and write themselves from or to a character stream.
 * </p>
 * <p>
 * This interface is implemented by special implementations of the
 * {@code Configuration} interface which are associated with a file. It demands
 * only basic methods for doing I/O based on character stream objects. Based on
 * these methods it is possible to implement other methods which operate on
 * files, file names, URLs, etc.
 * </p>
 *
 * @version $Id$
 */
public interface FileBased
{
    /**
     * Reads the content of this object from the given reader.
     *
     * @param in the reader
     * @throws IOException if an I/O error occurs
     * @throws ConfigurationException if a non-I/O related problem occurs, e.g.
     *         the data read does not have the expected format
     */
    void read(Reader in) throws ConfigurationException, IOException;

    /**
     * Writes the content of this object to the given writer.
     *
     * @param out the writer
     * @throws IOException if an I/O error occurs
     * @throws ConfigurationException if a non-I/O related problem occurs, e.g.
     *         the data read does not have the expected format
     */
    void write(Writer out) throws ConfigurationException, IOException;
}
