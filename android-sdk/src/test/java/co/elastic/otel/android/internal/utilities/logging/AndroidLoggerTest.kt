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
package co.elastic.otel.android.internal.utilities.logging

import co.elastic.otel.android.logging.LogLevel
import co.elastic.otel.android.logging.LoggingPolicy
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AndroidLoggerTest {

    @Test
    fun checkTraceLogger() {
        val logger = getLogger(LogLevel.TRACE)

        assertThat(logger.isTraceEnabled).isTrue()
        assertThat(logger.isDebugEnabled).isTrue()
        assertThat(logger.isInfoEnabled).isTrue()
        assertThat(logger.isWarnEnabled).isTrue()
        assertThat(logger.isErrorEnabled).isTrue()
    }

    @Test
    fun checkDebugLogger() {
        val logger = getLogger(LogLevel.DEBUG)

        assertThat(logger.isTraceEnabled).isFalse()
        assertThat(logger.isDebugEnabled).isTrue()
        assertThat(logger.isInfoEnabled).isTrue()
        assertThat(logger.isWarnEnabled).isTrue()
        assertThat(logger.isErrorEnabled).isTrue()
    }

    @Test
    fun checkInfoLogger() {
        val logger = getLogger(LogLevel.INFO)

        assertThat(logger.isTraceEnabled).isFalse()
        assertThat(logger.isDebugEnabled).isFalse()
        assertThat(logger.isInfoEnabled).isTrue()
        assertThat(logger.isWarnEnabled).isTrue()
        assertThat(logger.isErrorEnabled).isTrue()
    }

    @Test
    fun checkWarnLogger() {
        val logger = getLogger(LogLevel.WARN)

        assertThat(logger.isTraceEnabled).isFalse()
        assertThat(logger.isDebugEnabled).isFalse()
        assertThat(logger.isInfoEnabled).isFalse()
        assertThat(logger.isWarnEnabled).isTrue()
        assertThat(logger.isErrorEnabled).isTrue()
    }

    @Test
    fun checkErrorLogger() {
        val logger = getLogger(LogLevel.ERROR)

        assertThat(logger.isTraceEnabled).isFalse()
        assertThat(logger.isDebugEnabled).isFalse()
        assertThat(logger.isInfoEnabled).isFalse()
        assertThat(logger.isWarnEnabled).isFalse()
        assertThat(logger.isErrorEnabled).isTrue()
    }

    @Test
    fun checkDisabledPolicy() {
        val logger = loggerWithDisabledPolicy

        assertThat(logger.isTraceEnabled).isFalse()
        assertThat(logger.isDebugEnabled).isFalse()
        assertThat(logger.isInfoEnabled).isFalse()
        assertThat(logger.isWarnEnabled).isFalse()
        assertThat(logger.isErrorEnabled).isFalse()
    }

    private fun getLogger(minimumLevel: LogLevel): AndroidLogger {
        return AndroidLogger("TAG", LoggingPolicy.enabled(minimumLevel))
    }

    private val loggerWithDisabledPolicy: AndroidLogger
        get() = AndroidLogger("TAG", LoggingPolicy.disabled())
}