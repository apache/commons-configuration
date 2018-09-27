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

import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Priority;
import org.apache.log4j.Level;
import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.ConsoleAppender;

/**
 * Configures logging for tests.
 *
 * When running with Maven do -Dmaven.surefire.debug="LogLevel=level" to set the
 * Log Level to the desired value.
 */
public class Logging extends Log4JLogger
{
    private static final long serialVersionUID = 8619242753795694874L;

    /**
     * The fully qualified name of the Log4JLogger class.
     */
    private static final String FQCN = Logging.class.getName();

    private static Priority traceLevel; // TODO should this be Level?

    static
    {
        // Releases of log4j1.2 >= 1.2.12 have Priority.TRACE available, earlier
        // versions do not. If TRACE is not available, then we have to map
        // calls to Log.trace(...) onto the DEBUG level.

        try
        {
            traceLevel = (Priority) Level.class.getDeclaredField("TRACE").get(null);
        }
        catch (final Exception ex)
        {
            // ok, trace not available
            traceLevel = Level.DEBUG;
        }

        final String level = System.getProperty("LogLevel");
        if (level != null)
        {
            final org.apache.log4j.Logger log = org.apache.log4j.Logger.getRootLogger();
            log.setLevel(Level.toLevel(level));
            final Appender appender = new ConsoleAppender(new PatternLayout("%p %l - %m%n"), ConsoleAppender.SYSTEM_OUT);
            log.addAppender(appender);
        }
    }

    public Logging()
    {
        super();
    }


    /**
     * Base constructor.
     */
    public Logging(final String name)
    {
        super(name);
    }

    /**
     * For use with a log4j factory.
     */
    public Logging(final org.apache.log4j.Logger logger)
    {
        super(logger);
    }

    // ---------------------------------------------------------
    // Implementation
    //
    // Note that in the methods below the Priority class is used to define
    // levels even though the Level class is supported in 1.2. This is done
    // so that at compile time the call definitely resolves to a call to
    // a method that takes a Priority rather than one that takes a Level.
    //
    // The Category class (and hence its subclass Logging) in version 1.2 only
    // has methods that take Priority objects. The Category class (and hence
    // Logging class) in version 1.3 has methods that take both Priority and
    // Level objects. This means that if we use Level here, and compile
    // against log4j 1.3 then calls would be bound to the versions of
    // methods taking Level objects and then would fail to run against
    // version 1.2 of log4j.
    // ---------------------------------------------------------


    /**
     * Logs a message with <code>org.apache.log4j.Priority.TRACE</code>.
     * When using a log4j version that does not support the <code>TRACE</code>
     * level, the message will be logged at the <code>DEBUG</code> level.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#trace(Object)
     */
    @Override
    public void trace(final Object message)
    {
        getLogger().log(FQCN, traceLevel, message, null);
    }


    /**
     * Logs a message with <code>org.apache.log4j.Priority.TRACE</code>.
     * When using a log4j version that does not support the <code>TRACE</code>
     * level, the message will be logged at the <code>DEBUG</code> level.
     *
     * @param message to log
     * @param t       log this cause
     * @see org.apache.commons.logging.Log#trace(Object, Throwable)
     */
    @Override
    public void trace(final Object message, final Throwable t)
    {
        getLogger().log(FQCN, traceLevel, message, t);
    }


    /**
     * Logs a message with <code>org.apache.log4j.Priority.DEBUG</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#debug(Object)
     */
    @Override
    public void debug(final Object message)
    {
        getLogger().log(FQCN, Level.DEBUG, message, null);
    }

    /**
     * Logs a message with <code>org.apache.log4j.Priority.DEBUG</code>.
     *
     * @param message to log
     * @param t       log this cause
     * @see org.apache.commons.logging.Log#debug(Object, Throwable)
     */
    @Override
    public void debug(final Object message, final Throwable t)
    {
        getLogger().log(FQCN, Level.DEBUG, message, t);
    }


    /**
     * Logs a message with <code>org.apache.log4j.Priority.INFO</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#info(Object)
     */
    @Override
    public void info(final Object message)
    {
        getLogger().log(FQCN, Level.INFO, message, null);
    }


    /**
     * Logs a message with <code>org.apache.log4j.Priority.INFO</code>.
     *
     * @param message to log
     * @param t       log this cause
     * @see org.apache.commons.logging.Log#info(Object, Throwable)
     */
    @Override
    public void info(final Object message, final Throwable t)
    {
        getLogger().log(FQCN, Level.INFO, message, t);
    }


    /**
     * Logs a message with <code>org.apache.log4j.Priority.WARN</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#warn(Object)
     */
    @Override
    public void warn(final Object message)
    {
        getLogger().log(FQCN, Level.WARN, message, null);
    }


    /**
     * Logs a message with <code>org.apache.log4j.Priority.WARN</code>.
     *
     * @param message to log
     * @param t       log this cause
     * @see org.apache.commons.logging.Log#warn(Object, Throwable)
     */
    @Override
    public void warn(final Object message, final Throwable t)
    {
        getLogger().log(FQCN, Level.WARN, message, t);
    }


    /**
     * Logs a message with <code>org.apache.log4j.Priority.ERROR</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#error(Object)
     */
    @Override
    public void error(final Object message)
    {
        getLogger().log(FQCN, Level.ERROR, message, null);
    }


    /**
     * Logs a message with <code>org.apache.log4j.Priority.ERROR</code>.
     *
     * @param message to log
     * @param t       log this cause
     * @see org.apache.commons.logging.Log#error(Object, Throwable)
     */
    @Override
    public void error(final Object message, final Throwable t)
    {
        getLogger().log(FQCN, Level.ERROR, message, t);
    }


    /**
     * Logs a message with <code>org.apache.log4j.Priority.FATAL</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#fatal(Object)
     */
    @Override
    public void fatal(final Object message)
    {
        getLogger().log(FQCN, Level.FATAL, message, null);
    }


    /**
     * Logs a message with <code>org.apache.log4j.Priority.FATAL</code>.
     *
     * @param message to log
     * @param t       log this cause
     * @see org.apache.commons.logging.Log#fatal(Object, Throwable)
     */
    @Override
    public void fatal(final Object message, final Throwable t)
    {
        getLogger().log(FQCN, Level.FATAL, message, t);
    }

}
