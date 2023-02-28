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
package co.elastic.apm.android.sdk.instrumentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import org.junit.After;
import org.junit.Test;

import co.elastic.apm.android.sdk.internal.configuration.Configurations;

public class InstrumentationTest {

    @After
    public void tearDown() {
        Configurations.resetForTest();
    }

    @Test
    public void whenInstrumentationIsAvailable_provideIt() {
        Configurations configurations = Configurations.builder().register(new SimpleInstrumentation()).buildAndRegisterGlobal();
        Instrumentation instrumentation = configurations.getConfiguration(SimpleInstrumentation.class);

        assertTrue(instrumentation.isEnabled());
    }

    @Test
    public void checkIfInstrumentationIsEnabled_statically() {
        Configurations.builder().register(new SimpleInstrumentation()).buildAndRegisterGlobal();

        assertTrue(Instrumentation.isEnabled(SimpleInstrumentation.class));
    }

    @Test
    public void whenNotInitialized_returnNotEnabled_statically() {
        assertFalse(Instrumentation.isEnabled(SimpleInstrumentation.class));
    }

    @Test
    public void whenConfigNotFound_returnNotEnabled_statically() {
        assertFalse(Instrumentation.isEnabled(SimpleInstrumentation.class));
    }

    @Test
    public void whenParentInstrumentationIsNotAvailable_provideLocalEnableInfo() {
        assertTrue(new SimpleInstrumentation(null, true).isEnabled());
    }

    @Test
    public void whenParentInstrumentationIsAvailable_andEnabled_provideLocalEnableInfo() {
        Configurations.builder().register(new ParentInstrumentation(true)).buildAndRegisterGlobal();

        assertFalse(new SimpleInstrumentation(ParentInstrumentation.class, false).isEnabled());
    }

    @Test
    public void whenParentInstrumentationIsAvailable_andNotEnabled_provideParentEnableInfo() {
        Configurations.builder().register(new ParentInstrumentation(false)).buildAndRegisterGlobal();

        assertFalse(new SimpleInstrumentation(ParentInstrumentation.class, true).isEnabled());
    }

    @Test
    public void whenParentInstrumentationIsSelf_provideSelfEnableInfo() {
        Configurations.builder().register(new SimpleInstrumentation(SimpleInstrumentation.class, true)).buildAndRegisterGlobal();

        assertTrue(Instrumentation.isEnabled(SimpleInstrumentation.class));
    }

    private static class SimpleInstrumentation extends Instrumentation {
        private final Class<? extends Instrumentation> parentConfigClass;
        private final boolean enabled;

        private SimpleInstrumentation(Class<? extends Instrumentation> parentConfigClass, boolean enabled) {
            this.parentConfigClass = parentConfigClass;
            this.enabled = enabled;
        }

        private SimpleInstrumentation() {
            this(null, true);
        }

        @NonNull
        @Override
        protected Group getGroup() {
            return () -> parentConfigClass;
        }

        @Override
        protected boolean enabled() {
            return enabled;
        }
    }

    private static class ParentInstrumentation extends Instrumentation {
        private final boolean enabled;

        private ParentInstrumentation(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        protected boolean enabled() {
            return enabled;
        }

        @NonNull
        @Override
        protected Group getGroup() {
            return () -> null;
        }
    }
}