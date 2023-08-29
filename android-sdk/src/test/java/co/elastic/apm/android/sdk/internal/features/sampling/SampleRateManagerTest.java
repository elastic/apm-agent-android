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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import co.elastic.apm.android.sdk.internal.configuration.impl.SampleRateConfiguration;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;
import co.elastic.apm.android.sdk.internal.utilities.NumberTools;

public class SampleRateManagerTest {
    private SampleRateConfiguration sampleRateConfiguration;
    private NumberTools numberTools;
    private PreferencesService preferencesService;
    private SampleRateManager sampleRateManager;
    private static final String ENABLE_SIGNAL_EXPORTING_KEY = "sampling_exporting_enabled";

    @Before
    public void setUp() {
        sampleRateConfiguration = mock(SampleRateConfiguration.class);
        doReturn(1.0).when(sampleRateConfiguration).getSampleRate();
        numberTools = mock(NumberTools.class);
        doReturn(1.0).when(numberTools).random();
        preferencesService = createPreferencesService(-1);
        sampleRateManager = new SampleRateManager(sampleRateConfiguration, numberTools, preferencesService);
    }

    @Test
    public void whenSampleRateIs_1_allowSignalExporting() {
        assertTrue(sampleRateManager.allowSignalExporting());
        verify(preferencesService).store(ENABLE_SIGNAL_EXPORTING_KEY, 1);
    }

    @Test
    public void whenPreferencesFlagIsAvailable_initializePolicyFromIt() {
        PreferencesService preferencesService = createPreferencesService(0);
        preferencesService.retrieveInt(ENABLE_SIGNAL_EXPORTING_KEY, -1);
        SampleRateManager sampleRateManager = new SampleRateManager(sampleRateConfiguration, numberTools, preferencesService);

        assertFalse(sampleRateManager.allowSignalExporting());
        verify(preferencesService, never()).store(anyString(), anyInt());
    }

    @Test
    public void whenSampleRateIs_0_disableSignalExporting() {
        doReturn(0.0).when(sampleRateConfiguration).getSampleRate();
        doReturn(0.0).when(numberTools).random();

        // No changes until now.
        assertTrue(sampleRateManager.allowSignalExporting());

        // Reevaluate the sample rate.
        sampleRateManager.onSessionIdChanged("");

        assertFalse(sampleRateManager.allowSignalExporting());
    }

    @Test
    public void whenSampleRateIsInBetween_0_and_1_enableSignalExportingBasedOnRandomPercentile() {
        assertTrue(sampleRateManager.allowSignalExporting());

        doReturn(0.6).when(sampleRateConfiguration).getSampleRate();

        // Anything above the sample rate won't enable it
        doReturn(0.61).when(numberTools).random();
        sampleRateManager.onSessionIdChanged("");
        assertFalse(sampleRateManager.allowSignalExporting());

        // The same value or lower will enable it:
        doReturn(0.6).when(numberTools).random();
        sampleRateManager.onSessionIdChanged("");
        assertTrue(sampleRateManager.allowSignalExporting());

        doReturn(0.1).when(numberTools).random();
        sampleRateManager.onSessionIdChanged("");
        assertTrue(sampleRateManager.allowSignalExporting());
    }

    @Test
    public void whenSampleRateIsReevaluated_to_enable_storeResultInPreferences() {
        clearInvocations(preferencesService);
        doReturn(0.5).when(sampleRateConfiguration).getSampleRate();
        doReturn(0.1).when(numberTools).random();

        sampleRateManager.onSessionIdChanged("");

        verify(preferencesService).store(ENABLE_SIGNAL_EXPORTING_KEY, 1);
    }

    @Test
    public void whenSampleRateIsReevaluated_to_disable_storeResultInPreferences() {
        clearInvocations(preferencesService);
        doReturn(0.5).when(sampleRateConfiguration).getSampleRate();
        doReturn(0.6).when(numberTools).random();

        sampleRateManager.onSessionIdChanged("");

        verify(preferencesService).store(ENABLE_SIGNAL_EXPORTING_KEY, 0);
    }

    private PreferencesService createPreferencesService(int enabledFlag) {
        PreferencesService mock = mock(PreferencesService.class);
        doReturn(enabledFlag).when(mock).retrieveInt(ENABLE_SIGNAL_EXPORTING_KEY, -1);
        return mock;
    }
}