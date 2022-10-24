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
package co.elastic.apm.android.agp73.usecase.apminfo;

import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.Variant;

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import co.elastic.apm.android.agp.api.usecase.ApmInfoUseCase;
import co.elastic.apm.android.agp72.usecase.apminfo.ApmInfoGeneratorTask;

public abstract class ApmInfoUseCase73 extends ApmInfoUseCase {

    @Override
    public void execute() {
        Project project = getProject().get();
        AndroidComponentsExtension<?, ?, Variant> componentsExtension = project.getExtensions().getByType(AndroidComponentsExtension.class);
        Parameters parameters = getParameters().get();

        componentsExtension.onVariants(componentsExtension.selector().all(), variant -> {
            attachTaskToVariant(variant, ApmInfoGeneratorTask.create(project, parameters, variant));
        });
    }

    protected void attachTaskToVariant(Variant variant, TaskProvider<ApmInfoGeneratorTask> taskProvider) {
        variant.getSources().getAssets().addGeneratedSourceDirectory(taskProvider, ApmInfoGeneratorTask::getOutputDir);
    }

}
