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
package co.elastic.apm.android.sdk.connectivity.opentelemetry;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.sdk.connectivity.ExportProtocol;
import co.elastic.apm.android.sdk.internal.configuration.impl.ConnectivityConfiguration;
import co.elastic.apm.android.sdk.testutils.providers.SimpleProvider;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class DefaultSignalConfigurationTest {

    private MockWebServer server;
    private ConnectivityConfiguration connectivityConfiguration;
    private DefaultSignalConfiguration signalConfiguration;

    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        server.enqueue(new MockResponse());

        connectivityConfiguration = mock();
        doReturn("http://" + server.getHostName() + ":" + server.getPort()).when(connectivityConfiguration).getEndpoint();
        signalConfiguration = new DefaultSignalConfiguration(SimpleProvider.create(connectivityConfiguration));
    }

    @Test
    public void testSpansHttpEndpoint() throws InterruptedException {
        doReturn(ExportProtocol.HTTP).when(connectivityConfiguration).getExportProtocol();
        TestSpanData spanData = getTestSpanData();

        signalConfiguration.provideSpanExporter().export(Collections.singleton(spanData)).join(1, TimeUnit.SECONDS);

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/v1/traces", recordedRequest.getPath());
    }

    @Test
    public void testMetricsHttpEndpoint() throws InterruptedException {
        doReturn(ExportProtocol.HTTP).when(connectivityConfiguration).getExportProtocol();
        MetricData metricData = getTestMetricData();

        signalConfiguration.provideMetricExporter().export(Collections.singleton(metricData)).join(1, TimeUnit.SECONDS);

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/v1/metrics", recordedRequest.getPath());
    }

    @Test
    public void testLogRecordsHttpEndpoint() throws InterruptedException {
        doReturn(ExportProtocol.HTTP).when(connectivityConfiguration).getExportProtocol();
        TestLogRecordData logRecordData = TestLogRecordData.builder().setBody("Log body").build();

        signalConfiguration.provideLogExporter().export(Collections.singleton(logRecordData)).join(1, TimeUnit.SECONDS);

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("/v1/logs", recordedRequest.getPath());
    }

    private static TestSpanData getTestSpanData() {
        return TestSpanData.builder().setName("Some name")
                .setStartEpochNanos(1000)
                .setEndEpochNanos(1100)
                .setHasEnded(true)
                .setStatus(StatusData.ok())
                .setKind(SpanKind.CLIENT)
                .build();
    }

    private static MetricData getTestMetricData() {
        return ImmutableMetricData.createLongSum(Resource.empty(),
                InstrumentationScopeInfo.empty(),
                "A Metric",
                "A description",
                "m",
                ImmutableSumData.empty());
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }
}