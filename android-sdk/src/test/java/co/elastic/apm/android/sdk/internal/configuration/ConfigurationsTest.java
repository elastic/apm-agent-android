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

public class ConfigurationsTest {

    @Test
    public void whenConfigurationIsntAvailable_fail() {
        try {
            Configurations configurations = Configurations.builder().buildAndRegisterGlobal();
            configurations.getConfiguration(SimpleConfiguration.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("No configuration found for 'co.elastic.apm.android.sdk.internal.configuration.ConfigurationsTest$SimpleConfiguration'", e.getMessage());
        }
    }

    @Test
    public void whenConfigurationIsAvailable_provideIt() {
        Configurations configurations = Configurations.builder().register(new SimpleConfiguration()).buildAndRegisterGlobal();
        Configuration configuration = configurations.getConfiguration(SimpleConfiguration.class);

        assertTrue(configuration.isEnabled());
    }

    @Test
    public void checkIfConfigurationIsEnabled_statically() {
        Configurations.builder().register(new SimpleConfiguration()).buildAndRegisterGlobal();

        assertTrue(Configurations.isEnabled(SimpleConfiguration.class));
    }

    @Test
    public void whenTryingToRegisterSameConfigurationTypeMoreThanOnce_fail() {
        try {
            Configurations.builder().register(new SimpleConfiguration())
                    .register(new SimpleConfiguration());
            fail();
        } catch (IllegalStateException e) {
            assertEquals("The configuration 'co.elastic.apm.android.sdk.internal.configuration.ConfigurationsTest$SimpleConfiguration' is already registered", e.getMessage());
        }
    }

    @Test
    public void whenStaticGetterIsCalled_returnAvailableInstance() {
        Configurations configurations = Configurations.builder().buildAndRegisterGlobal();

        assertEquals(configurations, Configurations.get());
    }

    private static class SimpleConfiguration extends Configuration {

        @Override
        protected boolean enabled() {
            return true;
        }
    }
}