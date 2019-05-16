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

package org.apache.commons.configuration2.ex;


/**
 * A configuration related runtime exception.
 *
 * @since 1.0
 *
 * @author Emmanuel Bourg
 */
public class ConfigurationRuntimeException extends RuntimeException
{
    /**
     * The serial version ID.
     */
    private static final long serialVersionUID = -7838702245512140996L;

    /**
     * Constructs a new {@code ConfigurationRuntimeException} without
     * specified detail message.
     */
    public ConfigurationRuntimeException()
    {
        super();
    }

    /**
     * Constructs a new {@code ConfigurationRuntimeException} with
     * specified detail message.
     *
     * @param message  the error message
     */
    public ConfigurationRuntimeException(final String message)
    {
        super(message);
    }

    /**
     * Constructs a new {@code ConfigurationRuntimeException} with
     * specified detail message using {@link String#format(String,Object...)}.
     *
     * @param message  the error message
     * @param args arguments to the error message
     * @see String#format(String,Object...)
     */
    public ConfigurationRuntimeException(final String message, final Object... args)
    {
        super(String.format(message, args));
    }

    /**
     * Constructs a new {@code ConfigurationRuntimeException} with
     * specified nested {@code Throwable}.
     *
     * @param cause  the exception or error that caused this exception to be thrown
     */
    public ConfigurationRuntimeException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructs a new {@code ConfigurationRuntimeException} with
     * specified detail message and nested {@code Throwable}.
     *
     * @param message  the error message
     * @param cause    the exception or error that caused this exception to be thrown
     */
    public ConfigurationRuntimeException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
