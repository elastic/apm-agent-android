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

public class FeaturesTest {

    @Test
    public void whenConfigurationIsntAvailable_fail() {
        try {
            Features features = Features.builder().build();
            features.getConfiguration(SimpleFeature.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("No configuration found for 'co.elastic.apm.android.sdk.internal.configuration.FeaturesTest$SimpleFeature'", e.getMessage());
        }
    }

    @Test
    public void whenConfigurationIsAvailable_provideIt() {
        Features features = Features.builder().register(new SimpleFeature()).build();
        FeatureConfiguration configuration = features.getConfiguration(SimpleFeature.class);

        assertTrue(configuration.isEnabled());
    }

    @Test
    public void checkIfFeatureIsEnabled_statically() {
        Features.builder().register(new SimpleFeature()).build();

        assertTrue(Features.isEnabled(SimpleFeature.class));
    }

    @Test
    public void whenTryingToRegisterSameFeatureTypeMoreThanOnce_fail() {
        try {
            Features.builder().register(new SimpleFeature())
                    .register(new SimpleFeature());
            fail();
        } catch (IllegalStateException e) {
            assertEquals("The feature 'co.elastic.apm.android.sdk.internal.configuration.FeaturesTest$SimpleFeature' is already registered", e.getMessage());
        }
    }

    @Test
    public void whenStaticGetterIsCalled_returnAvailableInstance() {
        Features features = Features.builder().build();

        assertEquals(features, Features.get());
    }

    private static class SimpleFeature extends FeatureConfiguration {

        @Override
        protected boolean enabled() {
            return true;
        }
    }
}