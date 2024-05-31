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

package org.apache.commons.configuration2.spring;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.env.EnumerablePropertySource;

/**
 * Allow use of Apache Commons Configuration Objects as Spring PropertySources
 */
public class ConfigurationPropertySource extends EnumerablePropertySource<Configuration> {

    protected ConfigurationPropertySource(final String name) {
        super(name);
    }

    public ConfigurationPropertySource(final String name, final Configuration source) {
        super(name, source);
    }

    @Override
    public Object getProperty(final String name) {
        if (source.getProperty(name) != null) {
            final String[] propValue = source.getStringArray(name);
            if (propValue == null || propValue.length == 0) {
                return "";
            } else if (propValue.length == 1) {
                return propValue[0];
            } else {
                return propValue;
            }
        } else {
            return null;
        }
    }

    @Override
    public String[] getPropertyNames() {
        final List<String> keys = new ArrayList<>();
        source.getKeys().forEachRemaining(keys::add);
        return keys.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }
}
