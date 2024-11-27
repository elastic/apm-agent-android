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
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class ElapsedTimeClockTest {

    @MockK
    lateinit var sntpClient: SntpClient

    @MockK
    lateinit var systemTimeProvider: SystemTimeProvider

    private lateinit var elapsedTimeClock: ElapsedTimeClock

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        elapsedTimeClock = ElapsedTimeClock(sntpClient, systemTimeProvider)
    }

    @Test
    fun `Return system nano time`() {
        val nanoTime: Long = 123
        every { systemTimeProvider.nanoTime }.returns(nanoTime)

        assertThat(elapsedTimeClock.nanoTime()).isEqualTo(nanoTime)
    }

    @Test
    fun `Use network time offset when available`() {
        val elapsedTime = 123L
        val serverOffset = 1_000L
        val expectedOffset = serverOffset + TIME_REFERENCE
        val expectedTime = TimeUnit.MILLISECONDS.toNanos(elapsedTime + expectedOffset)
        every { systemTimeProvider.elapsedRealTime }.returns(elapsedTime)
        every { sntpClient.fetchTimeOffset(elapsedTime + TIME_REFERENCE) }.returns(
            SntpClient.Response.Success(serverOffset)
        )

        // Fetch time offset
        elapsedTimeClock.runTask()

        assertThat(elapsedTimeClock.now()).isEqualTo(expectedTime)
    }

    companion object {
        private const val TIME_REFERENCE = 1577836800000L
    }
}