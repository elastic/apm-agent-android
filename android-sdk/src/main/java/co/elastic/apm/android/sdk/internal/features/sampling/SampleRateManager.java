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
package co.elastic.apm.android.sdk.internal.features.sampling;

import co.elastic.apm.android.sdk.internal.configuration.impl.SampleRateConfiguration;
import co.elastic.apm.android.sdk.internal.features.sampling.filters.SampleLogRecordFilter;
import co.elastic.apm.android.sdk.internal.features.sampling.filters.SampleMetricFilter;
import co.elastic.apm.android.sdk.internal.features.sampling.filters.SampleSpanFilter;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;
import co.elastic.apm.android.sdk.internal.utilities.NumberTools;
import co.elastic.apm.android.sdk.logs.tools.LogFilter;
import co.elastic.apm.android.sdk.metrics.tools.MetricFilter;
import co.elastic.apm.android.sdk.session.SessionObserver;
import co.elastic.apm.android.sdk.traces.tools.SpanFilter;

public final class SampleRateManager implements SessionObserver, SamplingPolicy {
    public final SpanFilter spanFilter;
    public final MetricFilter metricFilter;
    public final LogFilter logFilter;
    private final SampleRateConfiguration sampleRateConfiguration;
    private final NumberTools numberTools;
    private final PreferencesService preferencesService;
    private boolean allowSignalExporting = false;
    private static final String ENABLE_SIGNAL_EXPORTING_KEY = "sampling_exporting_enabled";

    SampleRateManager(SampleRateConfiguration sampleRateConfiguration, NumberTools numberTools,
                      PreferencesService preferencesService) {
        this.sampleRateConfiguration = sampleRateConfiguration;
        this.numberTools = numberTools;
        this.preferencesService = preferencesService;
        this.spanFilter = new SampleSpanFilter(this);
        this.metricFilter = new SampleMetricFilter(this);
        this.logFilter = new SampleLogRecordFilter(this);
        setUpInitialPolicy();
    }

    private void setUpInitialPolicy() {
        int enabledFlag = preferencesService.retrieveInt(ENABLE_SIGNAL_EXPORTING_KEY, -1);
        if (enabledFlag == -1) {
            evaluateSampleRate();
        } else {
            allowSignalExporting = enabledFlag == 1;
        }
    }

    @Override
    public void onSessionIdChanged(String newId) {
        evaluateSampleRate();
    }

    @Override
    public boolean allowSignalExporting() {
        return allowSignalExporting;
    }

    private void evaluateSampleRate() {
        boolean enable = shouldEnableSignalExporting();
        preferencesService.store(ENABLE_SIGNAL_EXPORTING_KEY, enable ? 1 : 0);
        allowSignalExporting = enable;
    }

    private boolean shouldEnableSignalExporting() {
        if (sampleRateConfiguration.getSampleRate() == 0) {
            return false;
        }
        return numberTools.random() <= sampleRateConfiguration.getSampleRate();
    }
}
