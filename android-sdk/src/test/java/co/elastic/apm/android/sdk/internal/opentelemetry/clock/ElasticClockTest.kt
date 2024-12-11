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
package co.elastic.apm.android.sdk.internal.opentelemetry.clock

import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import co.elastic.apm.android.sdk.internal.time.ntp.SntpClient
import co.elastic.apm.android.sdk.testutils.BaseTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import java.util.concurrent.TimeUnit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ElasticClockTest : BaseTest() {
    @MockK
    lateinit var sntpClient: SntpClient

    @MockK
    lateinit var systemTimeProvider: SystemTimeProvider

    private lateinit var elasticClock: ElasticClock

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { systemTimeProvider.getCurrentTimeMillis() }.returns(INITIAL_CURRENT_TIME)
        every { systemTimeProvider.getElapsedRealTime() }.returns(INITIAL_ELAPSED_TIME)
        elasticClock = ElasticClock(sntpClient, systemTimeProvider)
    }

    @Test
    fun `Return system nano time`() {
        val nanoTime: Long = 123
        every { systemTimeProvider.getNanoTime() }.returns(nanoTime)

        assertThat(elasticClock.nanoTime()).isEqualTo(nanoTime)
    }

    @Test
    fun `Return current time as now when network one isn't available`() {
        val delta = 100L
        val elapsedTime = INITIAL_ELAPSED_TIME + delta
        val currentTimeMillis = INITIAL_CURRENT_TIME + delta
        every { systemTimeProvider.getElapsedRealTime() }.returns(elapsedTime)

        assertThat(elasticClock.now()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(currentTimeMillis))
    }

    @Test
    fun `Use network time offset to calculate now, when available`() {
        val elapsedTime = 123L
        val serverOffset = 1_000L
        val expectedOffset = serverOffset + TIME_REFERENCE
        val expectedTime = TimeUnit.MILLISECONDS.toNanos(elapsedTime + expectedOffset)
        every { systemTimeProvider.getElapsedRealTime() }.returns(elapsedTime)
        every { sntpClient.fetchTimeOffset(elapsedTime + TIME_REFERENCE) }.returns(
            SntpClient.Response.Success(serverOffset)
        )

        // Fetch time offset
        elasticClock.sync()

        assertThat(elasticClock.now()).isEqualTo(expectedTime)
    }

    companion object {
        private const val TIME_REFERENCE = 1577836800000L
        private const val INITIAL_CURRENT_TIME = 1_000_000_000L
        private const val INITIAL_ELAPSED_TIME = 1_000L
    }
}