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
package co.elastic.otel.android.internal.opamp.impl.recipe.appenders;

import java.util.function.Supplier;

import opamp.proto.AgentToServer;
import opamp.proto.EffectiveConfig;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class EffectiveConfigAppender implements AgentToServerAppender {
    private final Supplier<EffectiveConfig> effectiveConfig;

    public static EffectiveConfigAppender create(Supplier<EffectiveConfig> effectiveConfig) {
        return new EffectiveConfigAppender(effectiveConfig);
    }

    private EffectiveConfigAppender(Supplier<EffectiveConfig> effectiveConfig) {
        this.effectiveConfig = effectiveConfig;
    }

    @Override
    public void appendTo(AgentToServer.Builder builder) {
        builder.effective_config(effectiveConfig.get());
    }
}
