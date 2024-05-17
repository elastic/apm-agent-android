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
package co.elastic.apm.android.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.events.EventBuilder;
import io.opentelemetry.api.incubator.events.GlobalEventLoggerProvider;

public final class ElasticEvents {
    private static final AttributeKey<String> EVENT_DOMAIN = AttributeKey.stringKey("event.domain");

    public static EventBuilder crashReporter() {
        return builder("CrashReport", "crash");
    }

    public static EventBuilder lifecycleReporter() {
        return builder("ApplicationLifecycle", "lifecycle");
    }

    public static EventBuilder builder(String instrumentationScopeName, String eventName) {
        return GlobalEventLoggerProvider.get().eventLoggerBuilder(instrumentationScopeName)
                .build().builder(eventName)
                .setAttributes(Attributes.of(EVENT_DOMAIN, "device"));
    }
}
