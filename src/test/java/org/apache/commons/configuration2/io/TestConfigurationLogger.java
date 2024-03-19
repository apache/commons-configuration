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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.commons.logging.impl.NoOpLog;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@code ConfigurationLogger}.
 */
public class TestConfigurationLogger {
    /** Constant for a message to be logged. */
    private static final String MSG = "Interesting log output";

    /**
     * Tests the logger set per default.
     */
    @Test
    public void testAbstractConfigurationDefaultLogger() {
        final AbstractConfiguration config = new BaseConfiguration();
        assertInstanceOf(NoOpLog.class, config.getLogger().getLog());
    }

    /**
     * Tests whether the logger can be set.
     */
    @Test
    public void testAbstractConfigurationSetLogger() {
        final ConfigurationLogger logger = new ConfigurationLogger(getClass());
        final AbstractConfiguration config = new BaseConfiguration();

        config.setLogger(logger);
        assertSame(logger, config.getLogger());
    }

    /**
     * Tests that the logger can be disabled by setting it to null.
     */
    @Test
    public void testAbstractConfigurationSetLoggerNull() {
        final AbstractConfiguration config = new BaseConfiguration();
        config.setLogger(new ConfigurationLogger(getClass()));

        config.setLogger(null);
        assertInstanceOf(NoOpLog.class, config.getLogger().getLog());
    }

    /**
     * Tests whether debug logging is possible.
     */
    @Test
    public void testDebug() {
        final Log log = mock(Log.class);
        final ConfigurationLogger logger = new ConfigurationLogger(log);

        logger.debug(MSG);

        verify(log).debug(MSG);
        verifyNoMoreInteractions(log);
    }

    /**
     * Tests whether a dummy logger can be created.
     */
    @Test
    public void testDummyLogger() {
        final ConfigurationLogger logger = ConfigurationLogger.newDummyLogger();

        assertInstanceOf(NoOpLog.class, logger.getLog());
    }

    /**
     * Tests whether error logging is possible.
     */
    @Test
    public void testError() {
        final Log log = mock(Log.class);
        final ConfigurationLogger logger = new ConfigurationLogger(log);

        logger.error(MSG);

        verify(log).error(MSG);
        verifyNoMoreInteractions(log);
    }

    /**
     * Tests whether an exception can be logged on error level.
     */
    @Test
    public void testErrorWithException() {
        final Log log = mock(Log.class);
        final Throwable ex = new Exception("Test exception");
        final ConfigurationLogger logger = new ConfigurationLogger(log);

        logger.error(MSG, ex);

        verify(log).error(MSG, ex);
        verifyNoMoreInteractions(log);
    }

    /**
     * Tests whether info logging is possible.
     */
    @Test
    public void testInfo() {
        final Log log = mock(Log.class);
        final ConfigurationLogger logger = new ConfigurationLogger(log);

        logger.info(MSG);

        verify(log).info(MSG);
        verifyNoMoreInteractions(log);
    }

    /**
     * Tries to create an instance without passing a logger class.
     */
    @Test
    public void testInitNoLoggerClass() {
        assertThrows(IllegalArgumentException.class, () -> new ConfigurationLogger((Class<?>) null));
    }

    /**
     * Tries to create an instance without passing in a logger name.
     */
    @Test
    public void testInitNoLoggerName() {
        assertThrows(IllegalArgumentException.class, () -> new ConfigurationLogger((String) null));
    }

    /**
     * Tests whether a correct internal logger is created.
     */
    @Test
    public void testInitWithLoggerSpec() {
        final ConfigurationLogger logger1 = new ConfigurationLogger(getClass().getName());
        final ConfigurationLogger logger2 = new ConfigurationLogger(getClass());

        assertNotNull(logger1.getLog());
        if (logger1.getLog() instanceof Log4JLogger) {
            assertEquals(logger1.getLog(), logger2.getLog());
        } else {
            // TODO assert what for the Slf4j adapter?
        }
    }

    /**
     * Tests whether the debug status can be queried.
     */
    @Test
    public void testIsDebugEnabled() {
        final Log log = mock(Log.class);

        when(log.isDebugEnabled()).thenReturn(Boolean.TRUE);

        final ConfigurationLogger logger = new ConfigurationLogger(log);

        assertTrue(logger.isDebugEnabled());

        verify(log).isDebugEnabled();
        verifyNoMoreInteractions(log);
    }

    /**
     * Tests whether the info status can be queried.
     */
    @Test
    public void testIsInfoEnabled() {
        final Log log = mock(Log.class);

        when(log.isInfoEnabled()).thenReturn(Boolean.FALSE);

        final ConfigurationLogger logger = new ConfigurationLogger(log);

        assertFalse(logger.isInfoEnabled());

        verify(log).isInfoEnabled();
        verifyNoMoreInteractions(log);
    }

    /**
     * Tests that a derived class can be created for a logger.
     */
    @Test
    public void testSubClass() {
        final StringBuilder buf = new StringBuilder();
        final ConfigurationLogger logger = new ConfigurationLogger() {
            @Override
            public void info(final String msg) {
                buf.append(msg);
            }
        };

        assertNull(logger.getLog());
        logger.info(MSG);
        assertEquals(MSG, buf.toString());
    }

    /**
     * Tests whether warn logging is possible.
     */
    @Test
    public void testWarn() {
        final Log log = mock(Log.class);
        final ConfigurationLogger logger = new ConfigurationLogger(log);

        logger.warn(MSG);

        verify(log).warn(MSG);
        verifyNoMoreInteractions(log);
    }

    /**
     * Tests whether an exception can be logged on warn level.
     */
    @Test
    public void testWarnWithException() {
        final Log log = mock(Log.class);
        final Throwable ex = new Exception("Test exception");
        final ConfigurationLogger logger = new ConfigurationLogger(log);

        logger.warn(MSG, ex);

        verify(log).warn(MSG, ex);
        verifyNoMoreInteractions(log);
    }
}
