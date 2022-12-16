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
package co.elastic.apm.android.sdk.traces.otel.processor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.traces.session.SessionIdProvider;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

public class ElasticSpanProcessor implements SpanProcessor {
    private final SpanProcessor original;
    private final Set<ExclusionRule> rules = new HashSet<>();
    private final SessionIdProvider sessionIdProvider;
    private static final AttributeKey<String> SESSION_ID_ATTRIBUTE_KEY = AttributeKey.stringKey("session.id");
    private static final AttributeKey<String> TRANSACTION_TYPE_ATTRIBUTE_KEY = AttributeKey.stringKey("type");
    private static final String TRANSACTION_TYPE_VALUE = "mobile";

    public void addAllExclusionRules(Collection<? extends ExclusionRule> rules) {
        this.rules.addAll(rules);
    }

    public ElasticSpanProcessor(SpanProcessor original) {
        this.original = original;
        sessionIdProvider = ElasticApmAgent.get().configuration.sessionIdProvider;
    }

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        span.setAttribute(SESSION_ID_ATTRIBUTE_KEY, getSessionId());
        span.setAttribute(TRANSACTION_TYPE_ATTRIBUTE_KEY, TRANSACTION_TYPE_VALUE);
        span.setStatus(StatusCode.OK);
        original.onStart(parentContext, span);
    }

    private synchronized String getSessionId() {
        return sessionIdProvider.getSessionId();
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {
        if (shouldExclude(span)) {
            return;
        }
        original.onEnd(span);
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }

    private boolean shouldExclude(ReadableSpan span) {
        for (ExclusionRule rule : rules) {
            if (rule.exclude(span)) {
                return true;
            }
        }
        return false;
    }

    public interface ExclusionRule {
        boolean exclude(ReadableSpan span);
    }
}
