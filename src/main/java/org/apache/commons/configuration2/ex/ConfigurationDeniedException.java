/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration2.ex;

/**
 * Thrown when an application only grants specific configurations for elements like URL schemes and hosts.
 *
 * @since 2.15.0
 */
public class ConfigurationDeniedException extends ConfigurationRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ConfigurationDeniedException} with specified detail message using {@link String#format(String,Object...)}.
     *
     * @param message the error message.
     * @param args    arguments to the error message.
     * @see String#format(String,Object...)
     */
    public ConfigurationDeniedException(final String message, final Object... args) {
        super(message, args);
    }

    /**
     * Constructs a new {@code ConfigurationDeniedException} with specified detail message using {@link String#format(String,Object...)} and cause.
     *
     * @param cause the cause.
     * @param message the error message.
     * @param args    arguments to the error message.
     * @see String#format(String,Object...)
     */
    public ConfigurationDeniedException(final Throwable cause, final String message, final Object... args) {
        super(cause, message, args);
    }
}
