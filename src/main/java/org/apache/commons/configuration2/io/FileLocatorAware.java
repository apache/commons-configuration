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

/**
 * <p>
 * Definition of an interface to be implemented by {@link FileBased} objects
 * which need access to the current {@link FileLocator}.
 * </p>
 * <p>
 * When loading or saving a {@code FileBased} object using {@code FileHandler}
 * the handler eventually invokes the {@code read()} or {@code write()} methods
 * passing in a reader or writer. For some implementations this may not be
 * sufficient because they need information about the current location. For
 * instance, a concrete {@code FileBased} implementation may have to resolve
 * other data sources based on relative file names which have to be interpreted
 * in the context of the current file location.
 * </p>
 * <p>
 * To deal with such scenarios, affected implementations can choose to implement
 * this interface. They are then passed the current location to the file being
 * accessed before their {@code read()} or {@code write()} method is called.
 * </p>
 *
 * @since 2.0
 */
public interface FileLocatorAware
{
    /**
     * Passes the current {@code FileLocator} to this object. Note that this
     * {@code FileLocator} object is only temporarily valid for the following
     * invocation of {@code read()} or {@code write(}. Depending on the state of
     * the {@code FileHandler} and which of its methods was called, the object
     * may not be fully initialized. For instance, if the {@code FileHandler}'s
     * {@code load(InputStream)} method was called, no file information is
     * available, and all methods of the {@code FileLocator} will return
     * <b>null</b>.
     *
     * @param locator the current {@code FileLocator}
     */
    void initFileLocator(FileLocator locator);
}
