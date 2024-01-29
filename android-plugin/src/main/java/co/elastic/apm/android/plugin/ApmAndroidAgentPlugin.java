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
package co.elastic.apm.android.plugin;

import com.android.build.api.instrumentation.InstrumentationScope;
import com.android.build.api.variant.ApplicationAndroidComponentsExtension;
import com.android.build.api.variant.ApplicationVariant;
import com.android.build.api.variant.SourceDirectories;
import com.android.build.gradle.BaseExtension;

import net.bytebuddy.build.gradle.android.ByteBuddyAndroidPlugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolveException;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.plugin.extensions.ElasticApmExtension;
import co.elastic.apm.android.plugin.instrumentation.ElasticLocalInstrumentationFactory;
import co.elastic.apm.android.plugin.logging.GradleLoggerFactory;
import co.elastic.apm.android.plugin.tasks.ApmInfoGeneratorTask;
import co.elastic.apm.android.plugin.tasks.tools.ClasspathProvider;
import co.elastic.apm.generated.BuildConfig;
import kotlin.Unit;

class ApmAndroidAgentPlugin implements Plugin<Project> {

    private Project project;
    private BaseExtension androidExtension;
    private ElasticApmExtension defaultExtension;
    private ClasspathProvider classpathProvider;

    @Override
    public void apply(Project project) {
        this.project = project;
        Elog.init(new GradleLoggerFactory());
        androidExtension = project.getExtensions().getByType(BaseExtension.class);
        classpathProvider = new ClasspathProvider();
        initializeElasticExtension(project);
        addBytebuddyPlugin();
        addSdkDependency();
        addInstrumentationDependency();
        addTasks();
    }

    private void initializeElasticExtension(Project project) {
        defaultExtension = project.getExtensions().create("elasticApm", ElasticApmExtension.class);
        defaultExtension.getServiceName().convention(project.provider(() -> androidExtension.getDefaultConfig().getApplicationId()));
        defaultExtension.getServiceVersion().convention(project.provider(() -> androidExtension.getDefaultConfig().getVersionName()));
    }

    private void addBytebuddyPlugin() {
        project.getPluginManager().apply(ByteBuddyAndroidPlugin.class);
    }

    private void addSdkDependency() {
        project.getDependencies().add("implementation", BuildConfig.SDK_DEPENDENCY_URI);
        if (kotlinPluginFound()) {
            project.getDependencies().add("implementation", BuildConfig.SDK_KTX_DEPENDENCY_URI);
        }
    }

    private boolean kotlinPluginFound() {
        return project.getExtensions().findByName("kotlin") != null;
    }

    private void addInstrumentationDependency() {
        project.getDependencies().add("implementation", BuildConfig.OTEL_OKHTTP_LIBRARY_URI);
        project.getDependencies().add("byteBuddy", BuildConfig.OTEL_OKHTTP_AGENT_URI);
    }

    private void addTasks() {
        ExtensionContainer extensions = project.getExtensions();
        ApplicationAndroidComponentsExtension extension = extensions.getByType(ApplicationAndroidComponentsExtension.class);

        extension.onVariants(extension.selector().all(), this::enhanceVariant);
    }

    private void enhanceVariant(ApplicationVariant applicationVariant) {
        addLocalRemapping(applicationVariant);
        addApmInfoGenerator(applicationVariant);
    }

    private void addLocalRemapping(ApplicationVariant applicationVariant) {
        applicationVariant.getInstrumentation().transformClassesWith(ElasticLocalInstrumentationFactory.class, InstrumentationScope.PROJECT, none -> Unit.INSTANCE);
    }

    private void addApmInfoGenerator(ApplicationVariant variant) {
        String variantName = variant.getName();
        TaskProvider<ApmInfoGeneratorTask> taskProvider = project.getTasks().register(variantName + "GenerateApmInfo", ApmInfoGeneratorTask.class);
        taskProvider.configure(apmInfoGenerator -> {
            apmInfoGenerator.getServiceName().set(defaultExtension.getServiceName());
            apmInfoGenerator.getServiceVersion().set(defaultExtension.getServiceVersion());
            apmInfoGenerator.getServerUrl().set(defaultExtension.getServerUrl());
            apmInfoGenerator.getSecretToken().set(defaultExtension.getSecretToken());
            apmInfoGenerator.getApiKey().set(defaultExtension.getApiKey());
            apmInfoGenerator.getVariantName().set(variantName);
            apmInfoGenerator.getOutputDir().set(project.getLayout().getBuildDirectory().dir(apmInfoGenerator.getName()));
            apmInfoGenerator.getOkHttpVersion().set(getOkhttpVersion(project, classpathProvider.getRuntimeConfiguration(variant)));
        });
        SourceDirectories.Layered assets = variant.getSources().getAssets();
        if (assets != null) {
            assets.addGeneratedSourceDirectory(taskProvider, ApmInfoGeneratorTask::getOutputDir);
        } else {
            Elog.getLogger().warn("Could not attach ApmInfoGeneratorTask");
        }
    }

    private static Provider<String> getOkhttpVersion(Project project, Configuration runtimeConfiguration) {
        return project.provider(() -> {
            ResolvedConfiguration resolvedConfiguration = runtimeConfiguration.getResolvedConfiguration();
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