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
package co.elastic.apm.android.agp.api.usecase;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import co.elastic.apm.android.agp.api.usecase.base.AgpUseCase;

public abstract class ApmInfoUseCase extends AgpUseCase<ApmInfoUseCase.Parameters> {

    public abstract static class Parameters implements AgpUseCase.Parameters {
        @Input
        public abstract Property<String> getServiceName();

        @Input
        public abstract Property<String> getServerUrl();

        @Input
        public abstract Property<String> getServiceVersion();

        @Optional
        @Input
        public abstract Property<String> getSecretToken();
    }
}
