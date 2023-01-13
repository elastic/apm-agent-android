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
package co.elastic.apm.android.agp.api;

import org.gradle.api.Action;
import org.gradle.api.Project;

import co.elastic.apm.android.agp.api.tools.ClasspathProvider;
import co.elastic.apm.android.agp.api.usecase.ApmInfoUseCase;
import co.elastic.apm.android.agp.api.usecase.base.AgpUseCase;

public abstract class AgpCompatibilityManager {
    protected final Project project;

    protected AgpCompatibilityManager(Project project) {
        this.project = project;
    }

    protected <P extends AgpUseCase.Parameters, T extends AgpUseCase<P>> T createUseCase(Class<T> useCaseClass, Class<P> parametersClass, Action<P> config) {
        T useCase = project.getObjects().newInstance(useCaseClass);
        P parameters = project.getObjects().newInstance(parametersClass);
        if (config != null) {
            config.execute(parameters);
        }
        useCase.getParameters().set(parameters);
        useCase.getProject().set(project);
        return useCase;
    }

    public abstract ApmInfoUseCase getApmInfoUseCase(Action<ApmInfoUseCase.Parameters> config);

    public abstract ClasspathProvider getClasspathProvider();
}
