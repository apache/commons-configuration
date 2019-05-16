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
package org.apache.commons.configuration2;

/**
 * <p>
 * An interface for decoding encoded values from a configuration source.
 * </p>
 * <p>
 * Using this interface it is possible to store encoded or encrypted values in a
 * configuration file. An implementing object can be assigned to a configuration
 * object. The {@code getEncodedString()} method of the
 * {@link ImmutableConfiguration} interface makes use of this instance to decode
 * the value read from the configuration file before it is passed to the caller.
 * </p>
 * <p>
 * By providing custom implementations of this interface an application can add
 * support for different kinds of encoded strings in configuration files.
 * </p>
 *
 * @since 2.0
 */
public interface ConfigurationDecoder
{
    /**
     * Decodes the specified string. This method is called with a string in
     * encoded form read from a configuration file. An implementation has to be
     * perform an appropriate decoding and return the result. This result is
     * passed to the calling application; so it should be in a readable form.
     *
     * @param s the string to be decoded (not <b>null</b>)
     * @return the decoded string
     */
    String decode(String s);
}
