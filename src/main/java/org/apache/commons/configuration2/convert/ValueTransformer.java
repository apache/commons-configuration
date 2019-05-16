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
package org.apache.commons.configuration2.convert;


/**
 * <p>
 * Definition of an interface used by {@link ListDelimiterHandler} to perform
 * additional transformations on behalf of a configuration when a property value
 * is escaped.
 * </p>
 * <p>
 * Some {@code Configuration} implementations require a special encoding of
 * their property values before they get written on disk. In some
 * constellations, e.g. when a property with multiple values is to be forced on
 * a single line, this encoding has to be done together with the escaping of
 * list delimiter characters - which is in the responsibility of
 * {@link ListDelimiterHandler}.
 * </p>
 * <p>
 * In order to allow a proper collaboration between the parties involved, this
 * interface was introduced. A configuration object provides an implementation
 * of {@code ValueTransformer} and passes it to the {@code ListDelimiterHandler}
 * when escaping of properties is needed. The delimiter handler can then call
 * back to perform the additional encoding as its pleasure.
 * </p>
 *
 * @since 2.0
 */
public interface ValueTransformer
{
    /**
     * Performs an arbitrary encoding of the passed in value object. This method
     * is called by a {@link ListDelimiterHandler} implementation before or
     * after list delimiters have been escaped.
     *
     * @param value the property value to be transformed
     * @return the transformed property value
     */
    Object transformValue(Object value);
}
