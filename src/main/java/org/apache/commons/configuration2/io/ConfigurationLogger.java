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

package org.apache.commons.configuration2.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.NoOpLog;

/**
 * <p>
 * A class providing basic logging capabilities.
 * </p>
 * <p>
 * When reading configuration files in complex scenarios having log output is
 * useful for diagnostic purposes. Therefore, <em>Commons Configuration</em>
 * produces some logging output. As concrete projects have different
 * requirements on the amount and detail of logging, there is a way of
 * configuring logging: All classes derived from
 * {@link org.apache.commons.configuration2.AbstractConfiguration}
 * can be assigned a logger which is then used for all log statements generated.
 * </p>
 * <p>
 * Allowing a logger object to be passed to a configuration creates a direct
 * dependency to a concrete logging framework in the configuration API. This
 * would make it impossible to switch to an alternative logging framework
 * without breaking backwards compatibility. To avoid this, the
 * {@code ConfigurationLogger} class is introduced. It is a minimum abstraction
 * over a logging framework offering only very basic logging capabilities. The
 * methods defined in this class are used by configuration implementations to
 * produce their logging statements. Client applications can create specialized
 * instances and pass them to configuration objects without having to deal with
 * a concrete logging framework. It is even possible to create a subclass that
 * uses a completely different logging framework.
 * </p>
 *
 * @since 2.0
 */
public class ConfigurationLogger
{
    /** The internal logger. */
    private final Log log;

    /**
     * Creates a new instance of {@code ConfigurationLogger} that uses the
     * specified logger name.
     *
     * @param loggerName the logger name (must not be <b>null</b>)
     * @throws IllegalArgumentException if the logger name is <b>null</b>
     */
    public ConfigurationLogger(final String loggerName)
    {
        this(createLoggerForName(loggerName));
    }

    /**
     * Creates a new instance of {@code ConfigurationLogger} that uses a logger
     * whose name is derived from the provided class.
     *
     * @param logCls the class whose name is to be used for logging (must not be
     *        <b>null</b>)
     * @throws IllegalArgumentException if the logger class is <b>null</b>
     */
    public ConfigurationLogger(final Class<?> logCls)
    {
        this(createLoggerForClass(logCls));
    }

    /**
     * Creates a new, uninitialized instance of {@code ConfigurationLogger}.
     * This constructor can be used by derived classes that implement their own
     * specific logging mechanism. Such classes must override all methods
     * because the default implementations do not work in this uninitialized
     * state.
     */
    protected ConfigurationLogger()
    {
        this((Log) null);
    }

    /**
     * Creates a new instance of {@code ConfigurationLogger} which wraps the
     * specified logger.
     *
     * @param wrapped the logger to be wrapped
     */
    ConfigurationLogger(final Log wrapped)
    {
        log = wrapped;
    }

    /**
     * Creates a new dummy logger which produces no output. If such a logger is
     * passed to a configuration object, logging is effectively disabled.
     *
     * @return the new dummy logger
     */
    public static ConfigurationLogger newDummyLogger()
    {
        return new ConfigurationLogger(new NoOpLog());
    }

    /**
     * Returns a flag whether logging on debug level is enabled.
     *
     * @return <b>true</b> if debug logging is enabled, <b>false</b> otherwise
     */
    public boolean isDebugEnabled()
    {
        return getLog().isDebugEnabled();
    }

    /**
     * Logs the specified message on debug level.
     *
     * @param msg the message to be logged
     */
    public void debug(final String msg)
    {
        getLog().debug(msg);
    }

    /**
     * Returns a flag whether logging on info level is enabled.
     *
     * @return <b>true</b> if debug logging is enabled, <b>false</b> otherwise
     */
    public boolean isInfoEnabled()
    {
        return getLog().isInfoEnabled();
    }

    /**
     * Logs the specified message on info level.
     *
     * @param msg the message to be logged
     */
    public void info(final String msg)
    {
        getLog().info(msg);
    }

    /**
     * Logs the specified message on warn level.
     *
     * @param msg the message to be logged
     */
    public void warn(final String msg)
    {
        getLog().warn(msg);
    }

    /**
     * Logs the specified exception on warn level.
     *
     * @param msg the message to be logged
     * @param ex the exception to be logged
     */
    public void warn(final String msg, final Throwable ex)
    {
        getLog().warn(msg, ex);
    }

    /**
     * Logs the specified message on error level.
     *
     * @param msg the message to be logged
     */
    public void error(final String msg)
    {
        getLog().error(msg);
    }

    /**
     * Logs the specified exception on error level.
     *
     * @param msg the message to be logged
     * @param ex the exception to be logged
     */
    public void error(final String msg, final Throwable ex)
    {
        getLog().error(msg, ex);
    }

    /**
     * Returns the internal logger.
     *
     * @return the internal logger
     */
    Log getLog()
    {
        return log;
    }

    /**
     * Creates an internal logger for the given name. Throws an exception if the
     * name is undefined.
     *
     * @param name the name of the logger
     * @return the logger object
     * @throws IllegalArgumentException if the logger name is undefined
     */
    private static Log createLoggerForName(final String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Logger name must not be null!");
        }
        return LogFactory.getLog(name);
    }

    /**
     * Creates an internal logger for the given class. Throws an exception if
     * the class is undefined.
     *
     * @param cls the logger class
     * @return the logger object
     * @throws IllegalArgumentException if the logger class is undefined
     */
    private static Log createLoggerForClass(final Class<?> cls)
    {
        if (cls == null)
        {
            throw new IllegalArgumentException(
                    "Logger class must not be null!");
        }
        return LogFactory.getLog(cls);
    }
}
