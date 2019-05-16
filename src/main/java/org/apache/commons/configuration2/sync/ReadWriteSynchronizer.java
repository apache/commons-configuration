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

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>
 * A special implementation of {@code Synchronizer} based on the JDK's
 * {@code ReentrantReadWriteLock} class.
 * </p>
 * <p>
 * This class manages a {@code ReadWriteLock} object internally. The methods of
 * the {@code Synchronizer} interface are delegated to this lock. So this class
 * behaves in the same way as documented for {@code ReentrantReadWriteLock}.
 * </p>
 * <p>
 * Using this {@code Synchronizer} implementation is appropriate to make
 * configuration objects thread-safe. This means that multiple threads can read
 * configuration data in parallel; if one thread wants to update the
 * configuration, this happens with an exclusive lock.
 * </p>
 *
 * @since 2.0
 */
public class ReadWriteSynchronizer implements Synchronizer
{
    /** The lock object used by this Synchronizer. */
    private final ReadWriteLock lock;

    /**
     * Creates a new instance of {@code ReadWriteSynchronizer} and initializes
     * it with the given lock object. This constructor can be used to pass a
     * lock object which has been configured externally. If the lock object is
     * <b>null</b>, a default lock object is created.
     *
     * @param l the lock object to be used (can be <b>null</b>)
     */
    public ReadWriteSynchronizer(final ReadWriteLock l)
    {
        lock = (l != null) ? l : createDefaultLock();
    }

    /**
     * Creates a new instance of {@code ReadWriteSynchronizer} and initializes
     * it with a lock object of type {@code ReentrantReadWriteLock}.
     */
    public ReadWriteSynchronizer()
    {
        this(null);
    }

    @Override
    public void beginRead()
    {
        lock.readLock().lock();
    }

    @Override
    public void endRead()
    {
        lock.readLock().unlock();
    }

    @Override
    public void beginWrite()
    {
        lock.writeLock().lock();
    }

    @Override
    public void endWrite()
    {
        lock.writeLock().unlock();
    }

    /**
     * Returns a new default lock object which is used if no lock is passed to
     * the constructor.
     *
     * @return the new default lock object
     */
    private static ReadWriteLock createDefaultLock()
    {
        return new ReentrantReadWriteLock();
    }
}
