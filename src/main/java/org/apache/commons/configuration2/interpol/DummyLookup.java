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

/**
 * <p>
 * A simple dummy {@code Lookup} implementation.
 * </p>
 * <p>
 * This implementation always returns <b>null</b> for a passed in variable
 * indicating that it cannot resolve that variable. This is useful for instance
 * as an application of the <em>null object</em> pattern.
 * </p>
 * <p>
 * This class does not define any state, therefore a single instance can be
 * shared. To enforce usage of only a single instance this class is actually an
 * enumeration.
 * </p>
 *
 * @since 2.0
 */
public enum DummyLookup implements Lookup
{
    /** The single instance of this class. */
    INSTANCE;

    /**
     * {@inheritDoc} This implementation always returns <b>null</b>.
     */
    @Override
    public Object lookup(final String variable)
    {
        return null;
    }
}
