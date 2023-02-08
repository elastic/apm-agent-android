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
package co.elastic.apm.android.sdk.internal.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class InstrumentationTest {

    @Test
    public void whenConfigurationIsntAvailable_fail() {
        try {
            Instrumentation features = Instrumentation.builder().build();
            features.getConfiguration(SimpleInstrumentationConfig.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("No configuration found for 'co.elastic.apm.android.sdk.internal.configuration.InstrumentationTest$SimpleInstrumentationConfig'", e.getMessage());
        }
    }

    @Test
    public void whenConfigurationIsAvailable_provideIt() {
        Instrumentation features = Instrumentation.builder().register(new SimpleInstrumentationConfig()).build();
        InstrumentationConfig configuration = features.getConfiguration(SimpleInstrumentationConfig.class);

        assertTrue(configuration.isEnabled());
    }

    @Test
    public void checkIfFeatureIsEnabled_statically() {
        Instrumentation.builder().register(new SimpleInstrumentationConfig()).build();

        assertTrue(Instrumentation.isEnabled(SimpleInstrumentationConfig.class));
    }

    @Test
    public void whenTryingToRegisterSameFeatureTypeMoreThanOnce_fail() {
        try {
            Instrumentation.builder().register(new SimpleInstrumentationConfig())
                    .register(new SimpleInstrumentationConfig());
            fail();
        } catch (IllegalStateException e) {
            assertEquals("The feature 'co.elastic.apm.android.sdk.internal.configuration.InstrumentationTest$SimpleInstrumentationConfig' is already registered", e.getMessage());
        }
    }

    @Test
    public void whenStaticGetterIsCalled_returnAvailableInstance() {
        Instrumentation features = Instrumentation.builder().build();

        assertEquals(features, Instrumentation.get());
    }

    private static class SimpleInstrumentationConfig extends InstrumentationConfig {

        @Override
        protected boolean enabled() {
            return true;
        }
    }
}