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
package co.elastic.apm.android.sdk.internal.otel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.sdk.internal.opentelemetry.tools.ElasticClock;
import co.elastic.apm.android.sdk.internal.time.SystemTimeProvider;
import co.elastic.apm.android.sdk.internal.time.ntp.TrueTimeWrapper;
import co.elastic.apm.android.sdk.testutils.BaseTest;

@RunWith(MockitoJUnitRunner.class)
public class ElasticClockTest extends BaseTest {

    @Mock
    public TrueTimeWrapper trueTimeWrapper;

    @Mock
    public SystemTimeProvider systemTimeProvider;
    private ElasticClock elasticClock;

    @Before
    public void setUp() {
        elasticClock = new ElasticClock(trueTimeWrapper, systemTimeProvider);
    }

    @Test
    public void whenProvidingNanoTime_returnSystemNanoTime() {
        long nanoTime = 123;
        doReturn(nanoTime).when(systemTimeProvider).getNanoTime();

        assertEquals(nanoTime, elasticClock.nanoTime());
        verify(systemTimeProvider).getNanoTime();
    }

    @Test
    public void whenProvidingNow_withTrueTimeNotInitialized_returnSystemCurrentTimeInNanos() {
        long systemTimeMillis = 12345;
        long systemTimeNanos = TimeUnit.MILLISECONDS.toNanos(systemTimeMillis);
        doReturn(systemTimeMillis).when(systemTimeProvider).getCurrentTimeMillis();
        doReturn(false).when(trueTimeWrapper).isInitialized();

        assertEquals(systemTimeNanos, elasticClock.now());
    }

    @Test
    public void whenProvidingNow_withNoTrueTimeAvailable_returnSystemCurrentTimeInNanos() {
        long systemTimeMillis = 12345;
        long systemTimeNanos = TimeUnit.MILLISECONDS.toNanos(systemTimeMillis);
        doReturn(systemTimeMillis).when(systemTimeProvider).getCurrentTimeMillis();
        doReturn(true).when(trueTimeWrapper).isInitialized();
        doThrow(IllegalStateException.class).when(trueTimeWrapper).now();

        assertEquals(systemTimeNanos, elasticClock.now());
    }

    @Test
    public void whenProvidingNow_withTrueTimeAvailable_returnTrueTimeInNanos() {
        long trueTimeMillis = 12345;
        Date trueTimeNow = new Date(trueTimeMillis);
        long trueTimeNanos = TimeUnit.MILLISECONDS.toNanos(trueTimeMillis);
        doReturn(true).when(trueTimeWrapper).isInitialized();
        doReturn(trueTimeNow).when(trueTimeWrapper).now();

        assertEquals(trueTimeNanos, elasticClock.now());
    }
}