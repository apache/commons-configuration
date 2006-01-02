/*
 * Copyright 2004-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration.web;

import java.util.Iterator;
import java.util.List;
import javax.servlet.FilterConfig;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.configuration.PropertyConverter;

/**
 * A configuration wrapper around a {@link FilterConfig}. This configuration is
 * read only, adding or removing a property will throw an
 * UnsupportedOperationException.
 *
 * @author <a href="mailto:ebourg@apache.org">Emmanuel Bourg</a>
 * @version $Revision$, $Date$
 * @since 1.1
 */
public class ServletFilterConfiguration extends BaseWebConfiguration
{
    /** Stores the wrapped filter config.*/
    protected FilterConfig config;

    /**
     * Create a ServletFilterConfiguration using the filter initialization parameters.
     *
     * @param config the filter configuration
     */
    public ServletFilterConfiguration(FilterConfig config)
    {
        this.config = config;
    }

    public Object getProperty(String key)
    {
        Object value = config.getInitParameter(key);
        if (!isDelimiterParsingDisabled())
        {
            List list = PropertyConverter.split((String) value, getListDelimiter());
            value = list.size() > 1 ? list : value;
        }

        return value;
    }

    public Iterator getKeys()
    {
        return new EnumerationIterator(config.getInitParameterNames());
    }
}
