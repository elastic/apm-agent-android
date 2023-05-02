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
package co.elastic.apm.android.sdk.internal.features.lifecycle;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import co.elastic.apm.android.sdk.logs.ElasticLoggers;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.logs.Logger;

public class ElasticProcessLifecycleObserver implements DefaultLifecycleObserver {
    private final Logger lifecycleLogger;

    @VisibleForTesting
    public ElasticProcessLifecycleObserver(Logger lifecycleLogger) {
        this.lifecycleLogger = lifecycleLogger;
    }

    public ElasticProcessLifecycleObserver() {
        this(ElasticLoggers.lifecycleReporter());
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        emitLifecycleState("created");
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        emitLifecycleState("started");
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        emitLifecycleState("resumed");
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        emitLifecycleState("paused");
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        emitLifecycleState("stopped");
    }

    private void emitLifecycleState(String value) {
        lifecycleLogger.eventBuilder("lifecycle")
                .setAttribute(AttributeKey.stringKey("lifecycle.state"), value)
                .emit();
    }
}
