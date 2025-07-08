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
package co.elastic.otel.android.internal.opamp.connectivity.http

import co.elastic.otel.android.internal.opamp.tools.SystemTime
import io.mockk.every
import io.mockk.mockk
import java.time.Duration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RetryAfterParserTest {

    @Test
    fun verifyParsing() {
        val systemTime: SystemTime = mockk()
        val currentTimeMillis = 1577836800000L // Wed, 01 Jan 2020 00:00:00 GMT
        every {
            systemTime.currentTimeMillis
        }.returns(currentTimeMillis)

        val parser = RetryAfterParser(systemTime)

        assertThat(parser.tryParse("123")).get().isEqualTo(Duration.ofSeconds(123))
        assertThat(parser.tryParse("Wed, 01 Jan 2020 01:00:00 GMT"))
            .get()
            .isEqualTo(Duration.ofHours(1))

        // Check when provided time is older than the current one
        assertThat(parser.tryParse("Tue, 31 Dec 2019 23:00:00 GMT")).isNotPresent()
    }
}
