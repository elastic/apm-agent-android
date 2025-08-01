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
package co.elastic.otel.android.internal.opamp.request.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import co.elastic.otel.android.internal.opamp.connectivity.http.HttpErrorException;
import co.elastic.otel.android.internal.opamp.connectivity.http.HttpSender;
import co.elastic.otel.android.internal.opamp.connectivity.http.RetryAfterParser;
import co.elastic.otel.android.internal.opamp.request.Request;
import co.elastic.otel.android.internal.opamp.request.delay.PeriodicDelay;
import co.elastic.otel.android.internal.opamp.response.Response;
import opamp.proto.AgentToServer;
import opamp.proto.RetryInfo;
import opamp.proto.ServerErrorResponse;
import opamp.proto.ServerErrorResponseType;
import opamp.proto.ServerToAgent;

@ExtendWith(MockitoExtension.class)
class HttpRequestServiceTest {

    private static final Duration REGULAR_DELAY = Duration.ofSeconds(1);
    private static final Duration RETRY_DELAY = Duration.ofSeconds(5);

    @Mock
    private RequestService.Callback callback;
    private TestScheduler scheduler;
    private TestHttpSender requestSender;
    private PeriodicDelay periodicRequestDelay;
    private PeriodicDelayWithSuggestion periodicRetryDelay;
    private int requestSize = -1;
    private HttpRequestService httpRequestService;

    @BeforeEach
    void setUp() {
        requestSender = new TestHttpSender();
        periodicRequestDelay = createPeriodicDelay(REGULAR_DELAY);
        periodicRetryDelay = createPeriodicDelayWithSuggestionSupport(RETRY_DELAY);
        scheduler = new TestScheduler();
        httpRequestService =
                new HttpRequestService(
                        requestSender,
                        scheduler.getMockService(),
                        periodicRequestDelay,
                        periodicRetryDelay,
                        RetryAfterParser.getInstance());
        httpRequestService.start(callback, this::createRequest);
    }

    @AfterEach
    void tearDown() {
        httpRequestService.stop();
        verify(scheduler.getMockService()).shutdown();
    }

    @Test
    void verifyStart_scheduledFirstTask() {
        TestScheduler.Task firstTask = assertAndGetSingleCurrentTask();
        assertThat(firstTask.getDelay()).isEqualTo(REGULAR_DELAY);

        // Verify initial task creates next one
        scheduler.clearTasks();
        requestSender.enqueueResponse(createSuccessfulResponse(new ServerToAgent.Builder().build()));
        firstTask.run();

        assertThat(scheduler.getScheduledTasks()).hasSize(1);

        // Check on-demand requests don't create subsequent tasks
        requestSender.enqueueResponse(createSuccessfulResponse(new ServerToAgent.Builder().build()));
        httpRequestService.sendRequest();

        assertThat(scheduler.getScheduledTasks()).hasSize(1);
    }

    @Test
    void verifySendingRequest_happyPath() {
        ServerToAgent serverToAgent = new ServerToAgent.Builder().build();
        HttpSender.Response httpResponse = createSuccessfulResponse(serverToAgent);
        requestSender.enqueueResponse(httpResponse);

        httpRequestService.sendRequest();

        verifySingleRequestSent();
        verifyRequestSuccessCallback(serverToAgent);
        verify(callback).onConnectionSuccess();
    }

    @Test
    void verifyWhenSendingOnDemandRequest_andDelayChanges() {
        // Initial state
        assertThat(assertAndGetSingleCurrentTask().getDelay()).isEqualTo(REGULAR_DELAY);

        // Trigger delay strategy change
        requestSender.enqueueResponse(createFailedResponse(503));
        httpRequestService.sendRequest();

        // Expected state
        assertThat(assertAndGetSingleCurrentTask().getDelay()).isEqualTo(RETRY_DELAY);
    }

    @Test
    void verifySendingRequest_whenTheresAParsingError() {
        HttpSender.Response httpResponse = createSuccessfulResponse(new byte[]{1, 2, 3});
        requestSender.enqueueResponse(httpResponse);

        httpRequestService.sendRequest();

        verifySingleRequestSent();
        verify(callback).onConnectionFailed(any());
    }

    @Test
    void verifySendingRequest_whenThereIsAnExecutionError()
            throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<HttpSender.Response> future = mock();
        requestSender.enqueueResponseFuture(future);
        Exception myException = mock();
        doThrow(new ExecutionException(myException)).when(future).get(30, TimeUnit.SECONDS);

        httpRequestService.sendRequest();

        verifySingleRequestSent();
        verify(callback).onConnectionFailed(myException);
    }

    @Test
    void verifySendingRequest_whenThereIsAnInterruptedException()
            throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<HttpSender.Response> future = mock();
        requestSender.enqueueResponseFuture(future);
        InterruptedException myException = mock();
        doThrow(myException).when(future).get(30, TimeUnit.SECONDS);

        httpRequestService.sendRequest();

        verifySingleRequestSent();
        verify(callback).onConnectionFailed(myException);
    }

    @Test
    void verifySendingRequest_whenThereIsAGenericHttpError() {
        requestSender.enqueueResponse(createFailedResponse(500));

        httpRequestService.sendRequest();

        verifySingleRequestSent();
        verifyRequestFailedCallback(500);
    }

    @Test
    void verifySendingRequest_whenThereIsATooManyRequestsError() {
        verifyRetryDelayOnError(createFailedResponse(429), RETRY_DELAY);
    }

    @Test
    void verifySendingRequest_whenThereIsATooManyRequestsError_withSuggestedDelay() {
        HttpSender.Response response = createFailedResponse(429);
        when(response.getHeader("Retry-After")).thenReturn("5");

        verifyRetryDelayOnError(response, Duration.ofSeconds(5));
    }

    @Test
    void verifySendingRequest_whenServerProvidesRetryInfo() {
        long nanosecondsToWaitForRetry = 1000;
        ServerErrorResponse errorResponse =
                new ServerErrorResponse.Builder()
                        .type(ServerErrorResponseType.ServerErrorResponseType_Unavailable)
                        .retry_info(
                                new RetryInfo.Builder().retry_after_nanoseconds(nanosecondsToWaitForRetry).build())
                        .build();
        ServerToAgent serverToAgent = new ServerToAgent.Builder().error_response(errorResponse).build();
        HttpSender.Response response = createSuccessfulResponse(serverToAgent);

        verifyRetryDelayOnError(response, Duration.ofNanos(nanosecondsToWaitForRetry));
    }

    @Test
    void verifySendingRequest_whenServerIsUnavailable() {
        ServerErrorResponse errorResponse =
                new ServerErrorResponse.Builder()
                        .type(ServerErrorResponseType.ServerErrorResponseType_Unavailable)
                        .build();
        ServerToAgent serverToAgent = new ServerToAgent.Builder().error_response(errorResponse).build();
        HttpSender.Response response = createSuccessfulResponse(serverToAgent);

        verifyRetryDelayOnError(response, RETRY_DELAY);
    }

    @Test
    void verifySendingRequest_whenThereIsAServiceUnavailableError() {
        verifyRetryDelayOnError(createFailedResponse(503), RETRY_DELAY);
    }

    @Test
    void verifySendingRequest_whenThereIsAServiceUnavailableError_withSuggestedDelay() {
        HttpSender.Response response = createFailedResponse(503);
        when(response.getHeader("Retry-After")).thenReturn("2");

        verifyRetryDelayOnError(response, Duration.ofSeconds(2));
    }

    @Test
    void verifySendingRequest_duringRegularMode() {
        requestSender.enqueueResponse(createSuccessfulResponse(new ServerToAgent.Builder().build()));

        httpRequestService.sendRequest();

        verifySingleRequestSent();
    }

    private void verifyRetryDelayOnError(
            HttpSender.Response errorResponse, Duration expectedRetryDelay) {
        requestSender.enqueueResponse(errorResponse);
        TestScheduler.Task previousTask = assertAndGetSingleCurrentTask();

        previousTask.run();

        verifySingleRequestSent();
        verify(periodicRetryDelay).reset();
        verify(callback).onRequestFailed(any());
        TestScheduler.Task retryTask = assertAndGetSingleCurrentTask();
        assertThat(retryTask.getDelay()).isEqualTo(expectedRetryDelay);

        // Retry with another error
        clearInvocations(callback);
        scheduler.clearTasks();
        requestSender.enqueueResponse(createFailedResponse(500));
        retryTask.run();

        verifySingleRequestSent();
        verify(callback).onRequestFailed(any());
        TestScheduler.Task retryTask2 = assertAndGetSingleCurrentTask();
        assertThat(retryTask2.getDelay()).isEqualTo(expectedRetryDelay);

        // Retry with a success
        clearInvocations(callback);
        scheduler.clearTasks();
        ServerToAgent serverToAgent = new ServerToAgent.Builder().build();
        requestSender.enqueueResponse(createSuccessfulResponse(serverToAgent));
        retryTask2.run();

        verify(periodicRequestDelay).reset();
        verifySingleRequestSent();
        verifyRequestSuccessCallback(serverToAgent);
        assertThat(assertAndGetSingleCurrentTask().getDelay()).isEqualTo(REGULAR_DELAY);
    }

    private Request createRequest() {
        AgentToServer agentToServer = new AgentToServer.Builder().sequence_num(10).build();
        requestSize = agentToServer.encodeByteString().size();
        return Request.create(agentToServer);
    }

    private TestScheduler.Task assertAndGetSingleCurrentTask() {
        List<TestScheduler.Task> scheduledTasks = scheduler.getScheduledTasks();
        assertThat(scheduledTasks).hasSize(1);
        return scheduledTasks.get(0);
    }

    private void verifySingleRequestSent() {
        List<TestHttpSender.RequestParams> requests = requestSender.getRequests(1);
        assertThat(requests.get(0).contentLength).isEqualTo(requestSize);
    }

    private void verifyRequestSuccessCallback(ServerToAgent serverToAgent) {
        verify(callback).onRequestSuccess(Response.create(serverToAgent));
    }

    private void verifyRequestFailedCallback(int errorCode) {
        ArgumentCaptor<HttpErrorException> captor = ArgumentCaptor.forClass(HttpErrorException.class);
        verify(callback).onRequestFailed(captor.capture());
        assertThat(captor.getValue().getErrorCode()).isEqualTo(errorCode);
        assertThat(captor.getValue().getMessage()).isEqualTo("Error message");
    }

    private static HttpSender.Response createSuccessfulResponse(ServerToAgent serverToAgent) {
        return createSuccessfulResponse(serverToAgent.encodeByteString().toByteArray());
    }

    private static HttpSender.Response createSuccessfulResponse(byte[] serverToAgent) {
        HttpSender.Response response = mock();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serverToAgent);
        when(response.statusCode()).thenReturn(200);
        when(response.bodyInputStream()).thenReturn(byteArrayInputStream);
        return response;
    }

    private static HttpSender.Response createFailedResponse(int statusCode) {
        HttpSender.Response response = mock();
        when(response.statusCode()).thenReturn(statusCode);
        when(response.statusMessage()).thenReturn("Error message");
        return response;
    }

    private static PeriodicDelay createPeriodicDelay(Duration delay) {
        PeriodicDelay mock = mock();
        when(mock.getNextDelay()).thenReturn(delay);
        return mock;
    }

    private static PeriodicDelayWithSuggestion createPeriodicDelayWithSuggestionSupport(
            Duration delay) {
        return spy(new PeriodicDelayWithSuggestion(delay));
    }

    private static class TestHttpSender implements HttpSender {
        private final List<RequestParams> requests = new ArrayList<>();

        @SuppressWarnings("JdkObsolete")
        private final Queue<CompletableFuture<Response>> responses = new LinkedList<>();

        @Override
        public CompletableFuture<HttpSender.Response> send(HttpSender.BodyWriter writer, int contentLength) {
            requests.add(new RequestParams(contentLength));
            CompletableFuture<HttpSender.Response> response = null;
            try {
                response = responses.remove();
            } catch (NoSuchElementException e) {
                fail("Unwanted triggered request");
            }
            return response;
        }

        public void enqueueResponse(HttpSender.Response response) {
            enqueueResponseFuture(CompletableFuture.completedFuture(response));
        }

        public void enqueueResponseFuture(CompletableFuture<Response> future) {
            responses.add(future);
        }

        public List<RequestParams> getRequests(int size) {
            assertThat(requests).hasSize(size);
            List<RequestParams> immutableRequests =
                    Collections.unmodifiableList(new ArrayList<>(requests));
            requests.clear();
            return immutableRequests;
        }

        private static class RequestParams {
            public final int contentLength;

            private RequestParams(int contentLength) {
                this.contentLength = contentLength;
            }
        }
    }
}
