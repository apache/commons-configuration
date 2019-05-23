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

/**
 * <p>
 * A specialized parameters class for INI configuration.
 * </p>
 * <p>
 * This parameters class defines some properties which allow customizing the
 * parsing and writing of INI documents.
 * </p>
 * <p>
 * This class is not thread-safe. It is intended that an instance is constructed
 * and initialized by a single thread during configuration of a
 * {@code ConfigurationBuilder}.
 * </p>
 *
 * @since 2.2
 */
public class INIBuilderParametersImpl extends HierarchicalBuilderParametersImpl
        implements INIBuilderProperties<INIBuilderParametersImpl>
{
    /** The key for the separatorUsedInINIOutput property. */
    private static final String PROP_SEPARATOR_USED_IN_INI_OUTPUT
        = "separatorUsedInOutput";

    /** The key for the separatorUsedInInput property. */
    private static final String PROP_SEPARATOR_USED_IN_INI_INPUT
        = "separatorUsedInInput";

    /** The key for the commentLeadingCharsUsedInInput property. */
    private static final String PROP_COMMENT_LEADING_SEPARATOR_USED_IN_INI_INPUT
        = "commentLeadingCharsUsedInInput";

    @Override
    public void inheritFrom(final Map<String, ?> source)
    {
        super.inheritFrom(source);
        copyPropertiesFrom(source, PROP_SEPARATOR_USED_IN_INI_OUTPUT);
        copyPropertiesFrom(source, PROP_SEPARATOR_USED_IN_INI_INPUT);
        copyPropertiesFrom(source, PROP_COMMENT_LEADING_SEPARATOR_USED_IN_INI_INPUT);
    }

    @Override
    public INIBuilderParametersImpl setSeparatorUsedInOutput(final String separator)
    {
        storeProperty(PROP_SEPARATOR_USED_IN_INI_OUTPUT, separator);
        return this;
    }

    @Override
    public INIBuilderParametersImpl setSeparatorUsedInInput(final String separator)
    {
        storeProperty(PROP_SEPARATOR_USED_IN_INI_INPUT, separator);
        return this;
    }

    @Override
    public INIBuilderParametersImpl setCommentLeadingCharsUsedInInput(final String separator)
    {
        storeProperty(PROP_COMMENT_LEADING_SEPARATOR_USED_IN_INI_INPUT, separator);
        return this;
    }
}
