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

import androidx.annotation.NonNull;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.configuration.Configuration;
import co.elastic.apm.android.sdk.internal.configuration.ConfigurationOption;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.configuration.OptionsRegistry;

public abstract class Instrumentation extends Configuration {
    private final ConfigurationOption<Boolean> isEnabled;

    public Instrumentation(boolean enabled) {
        isEnabled = createBooleanOption(getEnabledKeyName(), enabled);
    }

    @Override
    protected void visitOptions(OptionsRegistry options) {
        super.visitOptions(options);
        options.register(isEnabled);
    }

    protected abstract String getEnabledKeyName();

    static <T extends Instrumentation> void runWhenEnabled(Class<T> type, Function<T> onEnabled) {
        if (isEnabled(type)) {
            onEnabled.onInstrumentationReady(Configurations.get(type));
        }
    }

    static boolean isEnabled(Class<? extends Instrumentation> instrumentationClass) {
        if (!Configurations.isInitialized()) {
            Elog.getLogger().info("Configurations has not been initialized");
            return false;
        }
        Instrumentation instrumentation = Configurations.get(instrumentationClass);
        if (instrumentation == null) {
            Elog.getLogger().info("The requested Configuration was not found");
            return false;
        }

        return instrumentation.isEnabled();
    }

    public final boolean isEnabled() {
        if (groupIsNotEnabled()) {
            return false;
        }
        return enabled();
    }

    private boolean groupIsNotEnabled() {
        Class<? extends Instrumentation> groupType = getGroup().getType();
        return groupType != null && getClass() != groupType && !Configurations.get(groupType).isEnabled();
    }

    protected boolean enabled() {
        return isEnabled.get();
    }

    @NonNull
    protected Group getGroup() {
        return Groups.NONE;
    }

    public interface Group {
        Class<? extends Instrumentation> getType();
    }

    public enum Groups implements Group {
        NONE(InstrumentationConfiguration.class);

        private final Class<? extends Instrumentation> type;

        Groups(Class<? extends Instrumentation> type) {
            this.type = type;
        }

        @Override
        public Class<? extends Instrumentation> getType() {
            return type;
        }
    }

    @FunctionalInterface
    public interface Function<T extends Instrumentation> {
        void onInstrumentationReady(T instrumentation);
    }
}
