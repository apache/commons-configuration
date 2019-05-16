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
package org.apache.commons.configuration2.tree;

/**
 * <p>
 * A specialized version of the {@code NodeModelSupport} interface which allows
 * querying an {@link InMemoryNodeModel}.
 * </p>
 * <p>
 * This interface is needed by some special node model implementations used by
 * in-memory configurations. Such implementations require the extended
 * capabilities of an {@code InMemoryNodeModel}.
 * </p>
 *
 * @since 2.0
 */
public interface InMemoryNodeModelSupport extends
        NodeModelSupport<ImmutableNode>
{
    /**
     * {@inheritDoc} This variant specializes the return type to
     * {@code InMemoryNodeModel}.
     */
    @Override
    InMemoryNodeModel getNodeModel();
}
