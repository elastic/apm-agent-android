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
package co.elastic.apm.android.sdk.traces.session;

import androidx.annotation.NonNull;

/**
 * Provides an identifier for all the {@link io.opentelemetry.api.trace.Span}s created during a
 * period of time. The idea of a session is to provide a context that covers many transactions
 * that a user did in order to fulfil their needs using an application. For most apps, a session
 * could start when the user opens the app, and end when the user closes the app, or when the
 * app is forced to get closed due to an unexpected error. But for other apps, such as a ticketing
 * app for a queue in a bank for example, the app will always be open, but a session might start when
 * a person starts the process to get a new ticket, and end when the ticket is printed.
 */
public interface SessionIdProvider {
    @NonNull
    String getSessionId();
}
