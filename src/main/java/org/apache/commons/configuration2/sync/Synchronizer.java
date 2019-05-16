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
 * An interface controlling synchronization of configuration instances.
 * </p>
 * <p>
 * Each {@code Configuration} object derived from
 * {@link org.apache.commons.configuration2.AbstractConfiguration AbstractConfiguration}
 * has an associated {@code Synchronizer} object. Before an operation on the
 * configuration is performed (e.g. a property read or an update), the
 * {@code Synchronizer} is invoked. Depending on the concrete implementation of
 * the {@code Synchronizer} used, the configuration can be made thread-safe.
 * </p>
 * <p>
 * Whether a configuration has to be thread-safe or not is a matter of a
 * concrete use case. For instance, an application that just reads some
 * configuration settings on startup does need a thread-safe configuration
 * implementation. A configuration in contrast which is shared between multiple
 * components and updated concurrently should better be thread-safe. In order to
 * satisfy both kinds of use cases, the support for thread-safety has been
 * extracted out of the configuration implementation and refactored into this
 * {@code Synchronizer} interface. By assigning different {@code Synchronizer}
 * implementations to a configuration instance, the instance's support for
 * concurrent access can be adapted to the concrete use case.
 * </p>
 * <p>
 * The methods defined by this interface are similar to a <em>read-write
 * lock</em>. The {@code Synchronizer} is notified when read or write operations
 * start and end. A concrete implementation can then apply a specific policy to
 * decide when threads need to block or when access to the configuration for the
 * desired operation is granted.
 * </p>
 *
 * @since 2.0
 */
public interface Synchronizer
{
    /**
     * Notifies this {@code Synchronizer} that the current thread is going to
     * start a read operation on the managed configuration. This call can block
     * if a concrete implementation decides that the thread has to wait until a
     * specific condition is fulfilled.
     */
    void beginRead();

    /**
     * Notifies this {@code Synchronizer} that the current thread has finished
     * its read operation. This may cause other waiting threads to be granted
     * access to the managed configuration.
     */
    void endRead();

    /**
     * Notifies this {@code Synchronizer} that the current thread is going to
     * start a write operation on the managed configuration. This call may
     * block. For instance, a concrete implementation may suspend the thread
     * until all read operations currently active are finished,
     */
    void beginWrite();

    /**
     * Notifies this {@code Synchronizer} that the current thread has finished
     * its write operation. This may cause other waiting threads to be granted
     * access to the managed configuration.
     */
    void endWrite();
}
