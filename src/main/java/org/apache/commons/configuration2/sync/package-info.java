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

/**
 * <p>
 * A package containing interfaces and classes related to synchronization of
 * configurations.
 * </p>
 * <p>
 * Whether a configuration object has to be thread-safe or not is application-specific.
 * Therefore, this library allows an application to adapt configuration objects to
 * their requirements regarding thread-safety by assigning them so-called
 * {@code Synchronizer} objects. A configuration invokes its
 * {@code Synchronizer} every time it is accessed (in read or write mode).
 * If configurations are not accessed concurrently by multiple threads, a simple
 * dummy {@code Synchronizer} can be used - this is also the default
 * setting. To ensure thread-safety, a fully functional implementation has to be
 * set.
 * </p>
 *
 */
package org.apache.commons.configuration2.sync;
