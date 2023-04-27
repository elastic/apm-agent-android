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
package co.elastic.apm.android.sdk.internal.opentelemetry.processors.spans;

import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.attributes.AttributesCreator;
import co.elastic.apm.android.sdk.attributes.AttributesVisitor;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

public final class ElasticSpanProcessor implements SpanProcessor {
    private final SpanProcessor original;
    private final AttributesVisitor commonAttributesVisitor;
    private final Set<ExclusionRule> rules = new HashSet<>();
    private final Logger logger = Elog.getLogger();
    private static final AttributeKey<String> TRANSACTION_TYPE_ATTRIBUTE_KEY = AttributeKey.stringKey("type");
    private static final String TRANSACTION_TYPE_VALUE = "mobile";

    public void addAllExclusionRules(Collection<? extends ExclusionRule> rules) {
        this.rules.addAll(rules);
    }

    public ElasticSpanProcessor(SpanProcessor original, AttributesVisitor commonAttributesVisitor) {
        this.original = original;
        this.commonAttributesVisitor = commonAttributesVisitor;
    }

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        span.setAllAttributes(AttributesCreator.from(commonAttributesVisitor).create());
        span.setAttribute(TRANSACTION_TYPE_ATTRIBUTE_KEY, TRANSACTION_TYPE_VALUE);
        span.setStatus(StatusCode.OK);
        logger.debug("Starting span: '{}', within context: '{}'", span, parentContext);
        original.onStart(parentContext, span);
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {
        if (shouldExclude(span)) {
            logger.debug("Excluding span: {}", span);
            return;
        }
        logger.debug("Ending span: {}", span);
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
