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
package co.elastic.apm.android.sdk.internal.otel

import co.elastic.apm.android.sdk.internal.opentelemetry.tools.ElasticClock
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider
import co.elastic.apm.android.sdk.internal.time.ntp.SntpClient
import co.elastic.apm.android.sdk.testutils.BaseTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

internal class ElasticClockTest : BaseTest() {
    @MockK
    lateinit var sntpClient: SntpClient

    @MockK
    lateinit var systemTimeProvider: SystemTimeProvider
    private lateinit var elasticClock: ElasticClock

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        elasticClock = ElasticClock(sntpClient, systemTimeProvider)
    }

    @Test
    fun `Return system nano time`() {
        val nanoTime: Long = 123
        every { systemTimeProvider.nanoTime }.returns(nanoTime)

        assertThat(elasticClock.nanoTime()).isEqualTo(nanoTime)
    }

    @Test
    fun `Return current time plus initial time offset`() {
        val systemTimeMillis = 12345L
        val systemTimeNanos = TimeUnit.MILLISECONDS.toNanos(systemTimeMillis)
        every { systemTimeProvider.currentTimeMillis }.returns(systemTimeMillis)

        assertThat(elasticClock.now()).isEqualTo(systemTimeNanos)
    }

    @Test
    fun `Use network time offset when available`() {
        val systemTimeMillis: Long = 12345
        val offsetTime: Long = 5
        val expectedTime = TimeUnit.MILLISECONDS.toNanos(systemTimeMillis + offsetTime)
        every { systemTimeProvider.currentTimeMillis }.returns(systemTimeMillis)
        every { sntpClient.fetchTimeOffset(any()) }.returns(
            SntpClient.Response.Success(offsetTime)
        )

        // Fetch time offset
        elasticClock.runTask()

        assertThat(elasticClock.now()).isEqualTo(expectedTime)
    }
}