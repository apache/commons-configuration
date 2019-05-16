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
package org.apache.commons.configuration2.sync;

/**
 * <p>
 * An enumeration used by {@link SynchronizerSupport} to specify how an object
 * is locked.
 * </p>
 * <p>
 * The {@code SynchronizerSupport} interface allows locking an object. This can
 * be done in different ways controlling the level of concurrency still possible
 * with the object. One of the constants defined here can be passed in when
 * calling the {@code lock()} method of a {@code SynchronizerSupport} object.
 * (Note that at the end of the day it is up to a concrete implementation of
 * {@link Synchronizer} how these lock modes are interpreted.)
 * </p>
 *
 * @since 2.0
 */
public enum LockMode
{
    /**
     * Lock mode <em>READ</em>. The object is accessed in a read-only manner.
     * Typically, this means that other read locks can be granted while
     * concurrent writes are not permitted.
     */
    READ,

    /**
     * Lock mode <em>WRITE</em>. The object is updated. This usually means that
     * this object is exclusively locked. Attempts of other readers or writers
     * will block until the current update operation is complete.
     */
    WRITE
}
