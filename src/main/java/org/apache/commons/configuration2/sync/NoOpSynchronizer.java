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
 * An implementation of the {@code Synchronizer} interface which does not
 * perform any synchronization.
 * </p>
 * <p>
 * This class is the option of choice for applications that do not access
 * configuration concurrently. All methods required by the {@code Synchronizer}
 * interface are just empty dummies. Therefore, this class does not have any
 * synchronization overhead. Of course, configurations using this
 * {@code Synchronizer} implementation are not thread-safe!
 * </p>
 * <p>
 * Implementation note: This class is an enumeration because only a single
 * instance needs to exist. This instance can be shared between arbitrary
 * configurations.
 * </p>
 *
 * @since 2.0
 */
public enum NoOpSynchronizer implements Synchronizer
{
    /** The single shared instance of this class. */
    INSTANCE;

    @Override
    public void beginRead()
    {
    }

    @Override
    public void endRead()
    {
    }

    @Override
    public void beginWrite()
    {
    }

    @Override
    public void endWrite()
    {
    }
}
