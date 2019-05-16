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
 * Definition of an interface for objects that can be associated with a
 * {@link Synchronizer}.
 * </p>
 * <p>
 * This interface defines methods for querying and setting the
 * {@code Synchronizer}. In addition, it is possible to lock the object for a
 * certain operation. This is useful if some complex operations are to be
 * performed on the {@code SynchronizerSupport} object in an atomic way.
 * </p>
 * <p>
 * Note that the actual effect of these methods depends on the concrete
 * {@code Synchronizer} implementation in use! If only a dummy
 * {@code Synchronizer} is involved (which is appropriate if objects are only
 * accessed by a single thread), locking an object does not really prohibit
 * concurrent access.
 * </p>
 *
 * @since 2.0
 */
public interface SynchronizerSupport
{
    /**
     * Returns the {@code Synchronizer} used by this object. An implementation
     * must not return <b>null</b>. If no {@code Synchronizer} has been set so
     * far, a meaningful default {@code Synchronizer} has to be returned.
     *
     * @return the {@code Synchronizer} used by this object
     */
    Synchronizer getSynchronizer();

    /**
     * Sets the {@code Synchronizer} to be used by this object. Calling this
     * method and setting an appropriate {@code Synchronizer} determines whether
     * this object can be accessed in a thread-safe way or not. The argument may
     * be <b>null</b>; in this case an implementation should switch to a default
     * {@code Synchronizer}.
     *
     * @param sync the {@code Synchronizer} for this object
     */
    void setSynchronizer(Synchronizer sync);

    /**
     * Locks this object for the specified mode. This call may block until this
     * object is released from other lock operations. When it returns the caller
     * can access the object in a way compatible to the specified
     * {@code LockMode}. When done the {@code unlock()} must be called with the
     * same {@code LockMode} argument. In practice, a <b>try</b>-<b>finally</b>
     * construct should be used as in the following example:
     *
     * <pre>
     * SynchronizerSupport syncSupport = ...;
     * syncSupport.lock(LockMode.READ);
     * try
     * {
     *     // read access to syncSupport
     * }
     * finally
     * {
     *     syncSupport.unlock(LockMode.READ);
     * }
     * </pre>
     *
     * <em>Note:</em> Always use this method for obtaining a lock rather than
     * accessing the object's {@link Synchronizer} directly. An implementation
     * may perform additional actions which are not executed when only
     * interacting with the {@code Synchronizer}.
     *
     * @param mode the {@code LockMode}
     */
    void lock(LockMode mode);

    /**
     * Releases a lock of this object that was obtained using the
     * {@link #lock(LockMode)} method. This method must always be called
     * pair-wise with {@code lock()}. The argument must match to the one passed
     * to the corresponding {@code lock()} call; otherwise, the behavior of the
     * {@link Synchronizer} is unspecified.
     *
     * @param mode the {@code LockMode}
     */
    void unlock(LockMode mode);
}
