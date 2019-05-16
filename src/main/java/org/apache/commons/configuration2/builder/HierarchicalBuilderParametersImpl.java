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
package org.apache.commons.configuration2.builder;

import java.util.Map;

import org.apache.commons.configuration2.tree.ExpressionEngine;

/**
 * <p>
 * A specialized parameters object for hierarchical configurations.
 * </p>
 * <p>
 * This class defines special properties for hierarchical configurations.
 * Because most hierarchical configurations are file-based configurations this
 * class extends {@code FileBasedBuilderParametersImpl}.
 * </p>
 *
 * @since 2.0
 */
public class HierarchicalBuilderParametersImpl extends
        FileBasedBuilderParametersImpl implements
        HierarchicalBuilderProperties<HierarchicalBuilderParametersImpl>
{
    /** Constant for the expression engine property. */
    private static final String PROP_EXPRESSION_ENGINE = "expressionEngine";

    /**
     * {@inheritDoc} This implementation copies some more properties defined by
     * this class.
     */
    @Override
    public void inheritFrom(final Map<String, ?> source)
    {
        super.inheritFrom(source);
        copyPropertiesFrom(source, PROP_EXPRESSION_ENGINE);
    }

    /**
     * {@inheritDoc} This implementation stores the expression engine in the
     * internal parameters map.
     */
    @Override
    public HierarchicalBuilderParametersImpl setExpressionEngine(
            final ExpressionEngine engine)
    {
        storeProperty(PROP_EXPRESSION_ENGINE, engine);
        return this;
    }
}
