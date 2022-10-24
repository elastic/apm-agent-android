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
package co.elastic.apm.android.agp72.usecase.apminfo;

import com.android.build.api.component.impl.ComponentImpl;
import com.android.build.api.variant.Variant;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolveException;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import co.elastic.apm.android.agp.api.usecase.ApmInfoUseCase;
import co.elastic.apm.android.common.ApmInfo;

public abstract class ApmInfoGeneratorTask extends DefaultTask {

    @Input
    public abstract Property<String> getVariantName();

    @Input
    public abstract Property<String> getServiceName();

    @Input
    public abstract Property<String> getServerUrl();

    @Input
    public abstract Property<String> getServiceVersion();

    @Optional
    @Input
    public abstract Property<String> getServerToken();

    @Optional
    @Input
    public abstract Property<String> getOkHttpVersion();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void execute() {
        File propertiesFile = new File(getOutputDir().get().getAsFile(), ApmInfo.ASSET_FILE_NAME);
        Properties properties = new Properties();
        properties.put(ApmInfo.KEY_SERVICE_NAME, getServiceName().get());
        properties.put(ApmInfo.KEY_SERVICE_VERSION, getServiceVersion().get());
        properties.put(ApmInfo.KEY_SERVICE_ENVIRONMENT, getVariantName().get());
        properties.put(ApmInfo.KEY_SERVER_URL, getServerUrl().get());
        if (getServerToken().isPresent()) {
            properties.put(ApmInfo.KEY_SERVER_TOKEN, getServerToken().get());
        }
        String okhttpVersion = getOkHttpVersion().getOrNull();
        if (okhttpVersion != null) {
            properties.put(ApmInfo.KEY_SCOPE_OKHTTP_VERSION, okhttpVersion);
        }

        try (OutputStream outputStream = new FileOutputStream(propertiesFile)) {
            properties.store(outputStream, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TaskProvider<ApmInfoGeneratorTask> create(Project project, ApmInfoUseCase.Parameters parameters, Variant variant) {
        String variantName = variant.getName();
        TaskProvider<ApmInfoGeneratorTask> taskProvider = project.getTasks().register(variantName + "GenerateApmInfo", ApmInfoGeneratorTask.class);
        taskProvider.configure(apmInfoGenerator -> {
            apmInfoGenerator.getServiceName().set(parameters.getServiceName());
            apmInfoGenerator.getServiceVersion().set(parameters.getServiceVersion());
            apmInfoGenerator.getServerUrl().set(parameters.getServerUrl());
            apmInfoGenerator.getVariantName().set(variantName);
            apmInfoGenerator.getOutputDir().set(project.getLayout().getBuildDirectory().dir(apmInfoGenerator.getName()));
            apmInfoGenerator.getOkHttpVersion().set(getOkhttpVersion(project, variant));
        });

        return taskProvider;
    }

    private static Provider<String> getOkhttpVersion(Project project, Variant variant) {
        ComponentImpl component = (ComponentImpl) variant;
        return project.provider(() -> {
            Configuration runtimeClasspath = component.getVariantDependencies().getRuntimeClasspath();
            ResolvedConfiguration resolvedConfiguration = runtimeClasspath.getResolvedConfiguration();
            try {
                for (ResolvedArtifact artifact : resolvedConfiguration.getResolvedArtifacts()) {
                    ModuleVersionIdentifier identifier = artifact.getModuleVersion().getId();
                    if (identifier.getGroup().equals("com.squareup.okhttp3") && identifier.getName().equals("okhttp")) {
                        return identifier.getVersion();
                    }
                }
            } catch (ResolveException ignored) {
            }
            return null;
        });
    }
}
