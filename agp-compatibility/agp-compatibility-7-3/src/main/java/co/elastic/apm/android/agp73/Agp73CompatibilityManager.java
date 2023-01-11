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
package co.elastic.apm.android.agp73;

import org.gradle.api.Action;
import org.gradle.api.Project;

import co.elastic.apm.android.agp.api.AgpCompatibilityManager;
import co.elastic.apm.android.agp.api.tools.ClasspathProvider;
import co.elastic.apm.android.agp.api.usecase.ApmInfoUseCase;
import co.elastic.apm.android.agp73.tools.ClasspathProvider73;
import co.elastic.apm.android.agp73.usecase.apminfo.ApmInfoUseCase73;

public class Agp73CompatibilityManager extends AgpCompatibilityManager {

    protected ClasspathProvider73 classpathProvider;

    protected Agp73CompatibilityManager(Project project) {
        super(project);
    }

    @Override
    public ApmInfoUseCase getApmInfoUseCase(Action<ApmInfoUseCase.Parameters> config) {
        return createUseCase(ApmInfoUseCase73.class, ApmInfoUseCase.Parameters.class, config);
    }

    @Override
    public ClasspathProvider getClasspathProvider() {
        if (classpathProvider == null) {
            classpathProvider = new ClasspathProvider73();
        }
        return classpathProvider;
    }
}
