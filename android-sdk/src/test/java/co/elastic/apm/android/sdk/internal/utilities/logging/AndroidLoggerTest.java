/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.apm.android.sdk.internal.utilities.logging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import co.elastic.apm.android.sdk.configuration.logging.LogLevel;
import co.elastic.apm.android.sdk.configuration.logging.LoggingPolicy;

public class AndroidLoggerTest {

    @Test
    public void checkTraceLogger() {
        AndroidLogger logger = getLogger(LogLevel.TRACE);

        assertTrue(logger.isTraceEnabled());
        assertTrue(logger.isDebugEnabled());
        assertTrue(logger.isInfoEnabled());
        assertTrue(logger.isWarnEnabled());
        assertTrue(logger.isErrorEnabled());
    }

    @Test
    public void checkDebugLogger() {
        AndroidLogger logger = getLogger(LogLevel.DEBUG);

        assertFalse(logger.isTraceEnabled());
        assertTrue(logger.isDebugEnabled());
        assertTrue(logger.isInfoEnabled());
        assertTrue(logger.isWarnEnabled());
        assertTrue(logger.isErrorEnabled());
    }

    @Test
    public void checkInfoLogger() {
        AndroidLogger logger = getLogger(LogLevel.INFO);

        assertFalse(logger.isTraceEnabled());
        assertFalse(logger.isDebugEnabled());
        assertTrue(logger.isInfoEnabled());
        assertTrue(logger.isWarnEnabled());
        assertTrue(logger.isErrorEnabled());
    }

    @Test
    public void checkWarnLogger() {
        AndroidLogger logger = getLogger(LogLevel.WARN);

        assertFalse(logger.isTraceEnabled());
        assertFalse(logger.isDebugEnabled());
        assertFalse(logger.isInfoEnabled());
        assertTrue(logger.isWarnEnabled());
        assertTrue(logger.isErrorEnabled());
    }

    @Test
    public void checkErrorLogger() {
        AndroidLogger logger = getLogger(LogLevel.ERROR);

        assertFalse(logger.isTraceEnabled());
        assertFalse(logger.isDebugEnabled());
        assertFalse(logger.isInfoEnabled());
        assertFalse(logger.isWarnEnabled());
        assertTrue(logger.isErrorEnabled());
    }

    @Test
    public void checkDisabledPolicy() {
        AndroidLogger logger = getLoggerWithDisabledPolicy();

        assertFalse(logger.isTraceEnabled());
        assertFalse(logger.isDebugEnabled());
        assertFalse(logger.isInfoEnabled());
        assertFalse(logger.isWarnEnabled());
        assertFalse(logger.isErrorEnabled());
    }

    private AndroidLogger getLogger(LogLevel minimumLevel) {
        return new AndroidLogger("TAG", LoggingPolicy.create(true, minimumLevel));
    }

    private AndroidLogger getLoggerWithDisabledPolicy() {
        return new AndroidLogger("TAG", LoggingPolicy.create(false, LogLevel.TRACE));
    }
}