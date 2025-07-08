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

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import co.elastic.otel.android.internal.opamp.connectivity.http.HttpErrorException;
import co.elastic.otel.android.internal.opamp.connectivity.http.HttpSender;
import co.elastic.otel.android.internal.opamp.connectivity.http.RetryAfterParser;
import co.elastic.otel.android.internal.opamp.request.Request;
import co.elastic.otel.android.internal.opamp.request.delay.AcceptsDelaySuggestion;
import co.elastic.otel.android.internal.opamp.request.delay.PeriodicDelay;
import co.elastic.otel.android.internal.opamp.response.OpampServerResponseException;
import co.elastic.otel.android.internal.opamp.response.Response;
import opamp.proto.AgentToServer;
import opamp.proto.ServerErrorResponse;
import opamp.proto.ServerErrorResponseType;
import opamp.proto.ServerToAgent;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class HttpRequestService implements RequestService {
    private final HttpSender requestSender;
    // must be a single threaded executor, the code in this class relies on requests being processed
    // serially
    private final ScheduledExecutorService executorService;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean hasStopped = new AtomicBoolean(false);
    private final ConnectionStatus connectionStatus;
    private final AtomicReference<ScheduledFuture<?>> scheduledTask = new AtomicReference<>();
    private final RetryAfterParser retryAfterParser;
    @Nullable
    private Callback callback;
    @Nullable
    private Supplier<Request> requestSupplier;
    public static final PeriodicDelay DEFAULT_DELAY_BETWEEN_REQUESTS =
            PeriodicDelay.ofFixedDuration(Duration.ofSeconds(30));

    /**
     * Creates an {@link HttpRequestService}.
     *
     * @param requestSender The HTTP sender implementation.
     */
    public static HttpRequestService create(HttpSender requestSender) {
        return create(requestSender, DEFAULT_DELAY_BETWEEN_REQUESTS, DEFAULT_DELAY_BETWEEN_REQUESTS);
    }

    /**
     * Creates an {@link HttpRequestService}.
     *
     * @param requestSender        The HTTP sender implementation.
     * @param periodicRequestDelay The time to wait between requests in general.
     * @param periodicRetryDelay   The time to wait between retries.
     */
    public static HttpRequestService create(
            HttpSender requestSender,
            PeriodicDelay periodicRequestDelay,
            PeriodicDelay periodicRetryDelay) {
        return new HttpRequestService(
                requestSender,
                Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory()),
                periodicRequestDelay,
                periodicRetryDelay,
                RetryAfterParser.getInstance());
    }

    HttpRequestService(
            HttpSender requestSender,
            ScheduledExecutorService executorService,
            PeriodicDelay periodicRequestDelay,
            PeriodicDelay periodicRetryDelay,
            RetryAfterParser retryAfterParser) {
        this.requestSender = requestSender;
        this.executorService = executorService;
        this.retryAfterParser = retryAfterParser;
        this.connectionStatus = new ConnectionStatus(periodicRequestDelay, periodicRetryDelay);
    }

    @Override
    public void start(Callback callback, Supplier<Request> requestSupplier) {
        if (hasStopped.get()) {
            throw new IllegalStateException("HttpRequestService cannot start after it has been stopped.");
        }
        if (isRunning.compareAndSet(false, true)) {
            this.callback = callback;
            this.requestSupplier = requestSupplier;
            scheduleNextExecution();
        } else {
            throw new IllegalStateException("HttpRequestService is already running");
        }
    }

    @Override
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            hasStopped.set(true);
            executorService.shutdown();
        }
    }

    @Override
    public void sendRequest() {
        if (!isRunning.get()) {
            throw new IllegalStateException("HttpRequestService is not running");
        }

        executorService.execute(
                () -> {
                    // cancel the already scheduled task, a new one is created after current request is
                    // processed
                    ScheduledFuture<?> scheduledFuture = scheduledTask.get();
                    if (scheduledFuture != null) {
                        scheduledFuture.cancel(false);
                    }
                    sendAndScheduleNext();
                });
    }

    private void sendAndScheduleNext() {
        doSendRequest();
        scheduleNextExecution();
    }

    private void scheduleNextExecution() {
        scheduledTask.set(
                executorService.schedule(
                        this::sendAndScheduleNext,
                        connectionStatus.getNextDelay().toNanos(),
                        TimeUnit.NANOSECONDS));
    }

    private void doSendRequest() {
        AgentToServer agentToServer = Objects.requireNonNull(requestSupplier).get().getAgentToServer();

        byte[] data = agentToServer.encodeByteString().toByteArray();
        CompletableFuture<HttpSender.Response> future =
                requestSender.send(outputStream -> outputStream.write(data), data.length);
        try (HttpSender.Response response = future.get(30, TimeUnit.SECONDS)) {
            getCallback().onConnectionSuccess();
            if (isSuccessful(response)) {
                handleHttpSuccess(
                        Response.create(ServerToAgent.ADAPTER.decode(response.bodyInputStream())));
            } else {
                handleHttpError(response);
            }
        } catch (IOException | InterruptedException | TimeoutException e) {
            getCallback().onConnectionFailed(e);
        } catch (ExecutionException e) {
            if (e.getCause() != null) {
                getCallback().onConnectionFailed(e.getCause());
            } else {
                getCallback().onConnectionFailed(e);
            }
        }
    }

    private void handleHttpError(HttpSender.Response response) {
        int errorCode = response.statusCode();
        getCallback().onRequestFailed(new HttpErrorException(errorCode, response.statusMessage()));

        if (errorCode == 503 || errorCode == 429) {
            String retryAfterHeader = response.getHeader("Retry-After");
            Duration retryAfter = null;
            if (retryAfterHeader != null) {
                Optional<Duration> duration = retryAfterParser.tryParse(retryAfterHeader);
                if (duration.isPresent()) {
                    retryAfter = duration.get();
                }
            }
            connectionStatus.retryAfter(retryAfter);
        }
    }

    private static boolean isSuccessful(HttpSender.Response response) {
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    private void handleHttpSuccess(Response response) {
        connectionStatus.success();
        ServerToAgent serverToAgent = response.getServerToAgent();

        if (serverToAgent.error_response != null) {
            handleErrorResponse(serverToAgent.error_response);
        } else {
            getCallback().onRequestSuccess(response);
        }
    }

    private void handleErrorResponse(ServerErrorResponse errorResponse) {
        if (errorResponse.type.equals(ServerErrorResponseType.ServerErrorResponseType_Unavailable)) {
            Duration retryAfter = null;
            if (errorResponse.retry_info != null) {
                retryAfter = Duration.ofNanos(errorResponse.retry_info.retry_after_nanoseconds);
            }
            connectionStatus.retryAfter(retryAfter);
        }
        getCallback().onRequestFailed(new OpampServerResponseException(errorResponse,
                errorResponse.error_message));
    }

    private Callback getCallback() {
        return Objects.requireNonNull(callback);
    }

    // this class is only used from a single threaded ScheduledExecutorService, hence no
    // synchronization is needed
    private static class ConnectionStatus {
        private final PeriodicDelay periodicRequestDelay;
        private final PeriodicDelay periodicRetryDelay;

        private boolean retrying;
        private PeriodicDelay currentDelay;

        ConnectionStatus(PeriodicDelay periodicRequestDelay, PeriodicDelay periodicRetryDelay) {
            this.periodicRequestDelay = periodicRequestDelay;
            this.periodicRetryDelay = periodicRetryDelay;
            currentDelay = periodicRequestDelay;
        }

        void success() {
            // after successful request transition from retry to regular delay
            if (retrying) {
                retrying = false;
                periodicRequestDelay.reset();
                currentDelay = periodicRequestDelay;
            }
        }

        void retryAfter(@Nullable Duration retryAfter) {
            // after failed request transition from regular to retry delay
            if (!retrying) {
                retrying = true;
                periodicRetryDelay.reset();
                currentDelay = periodicRetryDelay;
                if (retryAfter != null && periodicRetryDelay instanceof AcceptsDelaySuggestion) {
                    ((AcceptsDelaySuggestion) periodicRetryDelay).suggestDelay(retryAfter);
                }
            }
        }

        Duration getNextDelay() {
            return currentDelay.getNextDelay();
        }
    }
}
