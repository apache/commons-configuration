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
package org.apache.commons.configuration;

import org.apache.commons.configuration.tree.ExpressionEngine;

/**
 * <p>
 * An interface for immutable hierarchical configurations.
 * </p>
 * <p>
 * There are some sources of configuration data that cannot be stored very well
 * in a flat configuration object (like {@link BaseConfiguration}) because then
 * their structure is lost. A prominent example are XML documents.
 * </p>
 * <p>
 * This interface extends the basic {@link ImmutableConfiguration} interface by
 * structured access to configuration properties. An {@link ExpressionEngine} is
 * used to evaluate complex property keys and to map them to nodes of a
 * tree-like structure.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public interface ImmutableHierarchicalConfiguration extends ImmutableConfiguration
{
    /**
     * Returns the expression engine used by this configuration. This method
     * will never return <b>null</b>; if no specific expression engine was set,
     * the default expression engine will be returned.
     *
     * @return the current expression engine
     */
    ExpressionEngine getExpressionEngine();

    /**
     * Returns the maximum defined index for the given key. This is useful if
     * there are multiple values for this key. They can then be addressed
     * separately by specifying indices from 0 to the return value of this
     * method.
     *
     * @param key the key to be checked
     * @return the maximum defined index for this key
     */
    int getMaxIndex(String key);
}
