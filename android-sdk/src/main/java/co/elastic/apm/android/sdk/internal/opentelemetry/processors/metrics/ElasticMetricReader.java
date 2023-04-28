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
package co.elastic.apm.android.sdk.internal.opentelemetry.processors.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.api.filter.Filter;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.impl.AllInstrumentationConfiguration;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.export.MetricProducer;

public final class ElasticMetricReader implements MetricReader {
    private final MetricReader wrapped;
    private Filter<MetricData> filter = Filter.noop();

    public ElasticMetricReader(MetricReader wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void register(CollectionRegistration registration) {
        if (registration instanceof MetricProducer) {
            wrapped.register(new ElasticMetricProducer((MetricProducer) registration, filter));
        } else {
            wrapped.register(registration);
        }
    }

    @Override
    public CompletableResultCode forceFlush() {
        return wrapped.forceFlush();
    }

    @Override
    public CompletableResultCode shutdown() {
        return wrapped.shutdown();
    }

    @Override
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
        return wrapped.getAggregationTemporality(instrumentType);
    }

    private static class ElasticMetricProducer implements MetricProducer {
        private final MetricProducer wrapped;
        private final Filter<MetricData> filter;

        private ElasticMetricProducer(MetricProducer wrapped, Filter<MetricData> filter) {
            this.wrapped = wrapped;
            this.filter = filter;
        }

        @Override
        public Collection<MetricData> collectAllMetrics() {
            if (!Configurations.get(AllInstrumentationConfiguration.class).isEnabled()) {
                Elog.getLogger().debug("Ignoring all metrics");
                return Collections.emptyList();
            }
            Collection<MetricData> originalMetrics = wrapped.collectAllMetrics();
            List<MetricData> filteredMetrics = new ArrayList<>();

            for (MetricData metric : originalMetrics) {
                if (filter.shouldInclude(metric)) {
                    filteredMetrics.add(metric);
                }
            }

            return filteredMetrics;
        }
    }

    public void setFilter(Filter<MetricData> filter) {
        this.filter = filter;
    }
}
