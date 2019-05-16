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
package org.apache.commons.configuration2;

/**
 * <p>
 * Definition of an interface to be implemented by {@code Configuration}
 * implementations which support a special initialization method.
 * </p>
 * <p>
 * This interface is mainly evaluated by <em>configuration builder</em>
 * implementations: If a newly created configuration instance implements this
 * interface, the builder calls the {@code initialize()} method. This gives
 * {@code Configuration} classes the opportunity to perform additional
 * initializations after all properties passed to the builder have been set.
 * </p>
 * <p>
 * Another use case for this interface is to perform initializations directly
 * which otherwise would have been done lazily. Lazy initializations can be
 * problematic regarding thread-safety. If in contrast a configuration instance
 * has been fully initialized when it is returned from the builder, it may be
 * used with a {@code NoOpSynchronizer} if it is not modified.
 * </p>
 *
 * @since 2.0
 */
public interface Initializable
{
    /**
     * Initializes this object. A concrete implementation can use this method to
     * perform arbitrary initialization. Typically, this method is invoked by a
     * <em>configuration builder</em>. In this case, the builder's lock is held,
     * so that all member fields set by this method are safely published.
     */
    void initialize();
}
