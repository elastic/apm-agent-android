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

import java.util.function.Supplier;

import co.elastic.otel.android.internal.opamp.OpampClient;
import co.elastic.otel.android.internal.opamp.request.Request;
import co.elastic.otel.android.internal.opamp.response.Response;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
/**
 * Handles the network connectivity in general, its implementation can choose what protocol to use
 * (HTTP or WebSocket) and should provide the necessary configurations options depending on the
 * case. There are 2 implementations ready to use, {@link HttpRequestService}, for using HTTP, and
 * {@link WebSocketRequestService} for using WebSocket. The {@link OpampClient} must not be aware of
 * the specific implementation it uses as it can expect the same behavior from either.
 */
public interface RequestService {

    /**
     * Starts the service. The actions done in this method depend on the implementation. For HTTP this
     * is where the periodic poll task should get started, whereas for WebSocket this is where the
     * connectivity is started.
     *
     * @param callback        This is the only way that the service can communicate back to the {@link
     *                        OpampClient} implementation.
     * @param requestSupplier This supplier must be queried every time a new request is about to be
     *                        sent.
     */
    void start(Callback callback, Supplier<Request> requestSupplier);

    /**
     * Triggers a new request send.
     */
    void sendRequest();

    /**
     * Clears the service for good. No further calls to {@link #sendRequest()} can be made after this
     * method is called.
     */
    void stop();

    /**
     * Allows the service to talk back to the {@link OpampClient} implementation.
     */
    interface Callback {
        /**
         * For WebSocket implementations, this is called when the connection is established. For HTTP
         * implementations, this is called on every HTTP request that ends successfully.
         */
        void onConnectionSuccess();

        /**
         * For WebSocket implementations, this is called when the connection cannot be made or is lost.
         * For HTTP implementations, this is called on every HTTP request that cannot get a response.
         *
         * @param throwable The detailed error.
         */
        void onConnectionFailed(Throwable throwable);

        /**
         * For WebSocket implementations, this is called every time there's a new message from the
         * server. For HTTP implementations, this is called when a successful HTTP request is finished
         * with a valid server to agent response body.
         *
         * @param response The server to agent message.
         */
        void onRequestSuccess(Response response);

        /**
         * For both HTTP and WebSocket implementations, this is called when an attempt at sending a
         * message fails.
         *
         * @param throwable The detailed error.
         */
        void onRequestFailed(Throwable throwable);
    }
}
