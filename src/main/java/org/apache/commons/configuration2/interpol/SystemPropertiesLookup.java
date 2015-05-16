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
package org.apache.commons.configuration2.interpol;

import org.apache.commons.lang3.text.StrLookup;

/**
 * <p>
 * A specialized implementation of the {@code Lookup} interface that allows
 * access to system properties.
 * </p>
 * <p>
 * This implementation relies on {@code StrLookup.systemPropertiesLookup()} from
 * the Commons Lang project to resolve system properties. It can be used for
 * referencing system properties in configuration files in an easy way, for
 * instance:
 * </p>
 *
 * <pre>
 * current.user = ${sys:user.name}
 * </pre>
 *
 * <p>
 * {@code SystemPropertiesLookup} is one of the standard lookups that is
 * registered per default for each configuration.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public class SystemPropertiesLookup implements Lookup
{
    /** The underlying StrLookup object. */
    private final StrLookup<String> sysLookup = StrLookup
            .systemPropertiesLookup();

    @Override
    public Object lookup(String variable)
    {
        return sysLookup.lookup(variable);
    }
}
