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
package co.elastic.otel.android.internal.opamp.response;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

import opamp.proto.AgentRemoteConfig;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@AutoValue
public abstract class MessageData {
    @Nullable
    public abstract AgentRemoteConfig getRemoteConfig();

    public static Builder builder() {
        return new AutoValue_MessageData.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setRemoteConfig(AgentRemoteConfig remoteConfig);

        public abstract MessageData build();
    }
}
