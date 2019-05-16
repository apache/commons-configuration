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
 * Test class for {@code NodeHandlerDecorator}. This class uses the abstract
 * base class for NodeHandler tests to verify that all methods defined by the
 * {@code NodeHandler} interface are correctly delegated to the wrapped handler.
 *
 */
public class TestNodeHandlerDecorator extends AbstractImmutableNodeHandlerTest
{
    /**
     * {@inheritDoc} This implementation returns a {@code NodeHandlerDecorator}
     * which wraps a {@code TreeData} object acting as the actual node handler.
     */
    @Override
    protected NodeHandler<ImmutableNode> createHandler(final ImmutableNode root)
    {
        final InMemoryNodeModel model = new InMemoryNodeModel(root);
        return new NodeHandlerDecorator<ImmutableNode>()
        {
            @Override
            protected NodeHandler<ImmutableNode> getDecoratedNodeHandler()
            {
                return model.getNodeHandler();
            }
        };
    }
}
