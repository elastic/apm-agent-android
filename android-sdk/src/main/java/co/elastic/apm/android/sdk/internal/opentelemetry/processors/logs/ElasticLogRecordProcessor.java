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
package co.elastic.apm.android.sdk.internal.opentelemetry.processors.logs;

import org.slf4j.Logger;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.attributes.AttributesCreator;
import co.elastic.apm.android.sdk.attributes.AttributesVisitor;
import co.elastic.apm.android.sdk.instrumentation.Instrumentation;
import co.elastic.apm.android.sdk.instrumentation.Instrumentations;
import co.elastic.apm.android.sdk.internal.api.filter.Filter;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.impl.AllInstrumentationConfiguration;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.data.LogRecordData;

public final class ElasticLogRecordProcessor implements LogRecordProcessor {
    private final LogRecordProcessor original;
    private final AttributesVisitor commonAttributesVisitor;
    private final Logger logger = Elog.getLogger();
    private Filter<LogRecordData> filter = Filter.noop();

    public ElasticLogRecordProcessor(LogRecordProcessor original, AttributesVisitor commonAttributesVisitor) {
        this.original = original;
        this.commonAttributesVisitor = commonAttributesVisitor;
    }

    @Override
    public void onEmit(Context context, ReadWriteLogRecord logRecord) {
        if (!Instrumentation.isEnabled(AllInstrumentationConfiguration.class)) {
            Elog.getLogger().debug("Ignoring all log records");
            return;
        }
        if (!filter.shouldInclude(logRecord.toLogRecordData())) {
            logger.debug("Excluding log record: {}", logRecord);
            return;
        }
        setAllAttributes(logRecord, AttributesCreator.from(commonAttributesVisitor).create());
        original.onEmit(context, logRecord);
    }

    private void setAllAttributes(ReadWriteLogRecord logRecord, Attributes attributes) {
        attributes.forEach((attributeKey, value) ->
                logRecord.setAttribute((AttributeKey<Object>) attributeKey, value));
    }

    @Override
    public CompletableResultCode shutdown() {
        return original.shutdown();
    }

    @Override
    public CompletableResultCode forceFlush() {
        return original.forceFlush();
    }

    @Override
    public void close() {
        original.close();
    }

    public void setFilter(Filter<LogRecordData> filter) {
        if (filter == null) {
            return;
        }
        this.filter = filter;
    }
}
