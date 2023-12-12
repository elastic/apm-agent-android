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
package co.elastic.apm.android.sdk.configuration.logging.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import co.elastic.apm.android.sdk.configuration.logging.LogLevel;
import co.elastic.apm.android.sdk.testutils.providers.SimpleProvider;

public class DefaultLoggingPolicyTest {

    @Test
    public void isEnabled_true_whenAgentIsInitialized() {
        assertTrue(getInstance(true).isEnabled());
        assertTrue(getInstance(false).isEnabled());
    }

    @Test
    public void isEnabled_false_whenAgentNotInitialized() {
        assertFalse(getInstance(true, false).isEnabled());
        assertFalse(getInstance(false, false).isEnabled());
    }

    @Test
    public void getMinimumLevel_onDebuggableApp() {
        assertEquals(LogLevel.DEBUG, getInstance(true).getMinimumLevel());
    }

    @Test
    public void getMinimumLevel_onNonDebuggableApp() {
        assertEquals(LogLevel.INFO, getInstance(false).getMinimumLevel());
    }

    private static DefaultLoggingPolicy getInstance(boolean appIsDebuggable) {
        return getInstance(appIsDebuggable, true);
    }

    private static DefaultLoggingPolicy getInstance(boolean appIsDebuggable, boolean agentIsInitialized) {
        return new DefaultLoggingPolicy(SimpleProvider.create(appIsDebuggable), SimpleProvider.create(agentIsInitialized));
    }
}