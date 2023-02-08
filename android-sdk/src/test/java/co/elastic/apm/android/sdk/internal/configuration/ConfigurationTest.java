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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConfigurationTest {

    @Test
    public void whenParentConfigurationIsNotAvailable_provideLocalEnableInfo() {
        assertTrue(new SimpleConfiguration(null, true).isEnabled());
    }

    @Test
    public void whenParentConfigurationIsAvailable_andEnabled_provideLocalEnableInfo() {
        Configurations.builder().register(new ParentConfiguration(true)).build();

        assertFalse(new SimpleConfiguration(ParentConfiguration.class, false).isEnabled());
    }

    @Test
    public void whenParentConfigurationIsAvailable_andNotEnabled_provideParentEnableInfo() {
        Configurations.builder().register(new ParentConfiguration(false)).build();

        assertFalse(new SimpleConfiguration(ParentConfiguration.class, true).isEnabled());
    }

    private static class SimpleConfiguration extends Configuration {
        private final Class<? extends Configuration> parentConfigClass;
        private final boolean enabled;

        private SimpleConfiguration(Class<? extends Configuration> parentConfigClass, boolean enabled) {
            this.parentConfigClass = parentConfigClass;
            this.enabled = enabled;
        }

        @Override
        protected Class<? extends Configuration> getParentConfigurationType() {
            return parentConfigClass;
        }

        @Override
        protected boolean enabled() {
            return enabled;
        }
    }

    private static class ParentConfiguration extends Configuration {
        private final boolean enabled;

        private ParentConfiguration(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        protected boolean enabled() {
            return enabled;
        }
    }
}