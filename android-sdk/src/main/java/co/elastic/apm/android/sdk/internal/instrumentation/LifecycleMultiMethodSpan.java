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
package co.elastic.apm.android.sdk.internal.instrumentation;

import android.app.Activity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.otel.SpanUtilities;
import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

public class LifecycleMultiMethodSpan {
    private static final String ROOT_SPAN_SUFFIX = " - View appearing";

    public static SpanWithScope onMethodEnter(String ownerName, String methodName, ElasticTracer tracer) {
        Elog.getLogger().debug("Entering lifecycle method '{}' in '{}'", methodName, ownerName);
        ensureRootSpanIsCreated(ownerName, tracer);
        SpanBuilder spanBuilder = tracer.spanBuilder(methodName);
        Span span = spanBuilder.startSpan();
        Scope scope = span.makeCurrent();

        return new SpanWithScope(span, scope);
    }

    public static void onMethodExit(Object owner, SpanWithScope spanWithScope, Throwable thrown, boolean endRoot) {
        Elog.getLogger().debug("Exiting lifecycle method from {} - endRoot: {}, thrown: {}", owner, endRoot, thrown);
        endMethodSpan(spanWithScope, thrown);

        Span rootSpan = Span.current();
        if (endRoot || thrown != null) {
            trySetActivityTitleAsRootSpanName(owner, rootSpan);
            endRootSpanAndCleanUp(rootSpan);
        }
    }

    private static void trySetActivityTitleAsRootSpanName(Object owner, Span rootSpan) {
        if (owner instanceof Activity) {
            CharSequence activityTitle = ((Activity) owner).getTitle();
            if (activityTitle != null && !activityTitle.toString().isEmpty()) {
                rootSpan.updateName(activityTitle + ROOT_SPAN_SUFFIX);
            }
        }
    }

    private static void endMethodSpan(SpanWithScope spanWithScope, Throwable thrown) {
        Span span = spanWithScope.span;
        Scope scope = spanWithScope.scope;
        if (thrown != null) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(thrown);
        }
        span.end();
        scope.close();
    }

    private static void endRootSpanAndCleanUp(Span rootSpan) {
        rootSpan.end();
        Context.root().makeCurrent();
    }

    private static void ensureRootSpanIsCreated(String ownerName, ElasticTracer tracer) {
        if (SpanUtilities.runningSpanNotFound()) {
            Elog.getLogger().debug("Creating root span for {}", ownerName);
            SpanBuilder spanBuilder = tracer.spanBuilder(ownerName + ROOT_SPAN_SUFFIX);
            Span rootSpan = spanBuilder.startSpan();
            rootSpan.makeCurrent();
        }
    }

    public static class SpanWithScope {
        public final Span span;
        public final Scope scope;

        public SpanWithScope(Span span, Scope scope) {
            this.span = span;
            this.scope = scope;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface LastMethod {

    }
}
