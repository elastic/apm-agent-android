package co.elastic.otel.android.compilation.tools.plugins.subprojects;

import static co.elastic.otel.android.compilation.tools.embedding.EmbeddingDependenciesPlugin.EMBEDDED_CLASSPATH_NAME;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.tasks.TaskProvider;

import java.util.ArrayList;
import java.util.List;

import co.elastic.otel.android.compilation.tools.extensions.LicensesFinderExtension;
import co.elastic.otel.android.compilation.tools.plugins.BaseNoticePlugin;
import co.elastic.otel.android.compilation.tools.tasks.subprojects.NoticeFilesCollectorTask;
import co.elastic.otel.android.compilation.tools.tasks.subprojects.PomLicensesCollectorTask;

public class BaseSubprojectPlugin extends BaseNoticePlugin {

    private static final String EXTENSION_LICENSES_CONFIG = "licensesConfig";
    protected LicensesFinderExtension licensesConfig;

    @Override
    public void apply(Project project) {
        licensesConfig = project.getExtensions().create(EXTENSION_LICENSES_CONFIG, LicensesFinderExtension.class);
        licensesConfig.manualMappingFile.convention(project.getRootProject().getLayout().getProjectDirectory().file("manual_licenses_map.txt"));
    }

    protected void setUpLicensedDependencies(Project project, TaskProvider<PomLicensesCollectorTask> licensesCollectorProvider) {
        String licensedDependenciesName = CONFIGURATION_LICENSES_COLLECTOR;
        project.getConfigurations().create(licensedDependenciesName, configuration -> {
            configuration.setCanBeConsumed(true);
            configuration.setCanBeResolved(false);
            configuration.attributes(attrs ->
                    attrs.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LIBRARY_ELEMENT_LICENSED_DEPENDENCIES)));
        });

        project.getArtifacts().add(licensedDependenciesName, licensesCollectorProvider.flatMap(PomLicensesCollectorTask::getLicensesFound),
                configurablePublishArtifact -> configurablePublishArtifact.builtBy(licensesCollectorProvider));
    }

    protected void setUpNoticeFilesProvider(Project project, TaskProvider<NoticeFilesCollectorTask> noticesCollectorProvider) {
        String noticeFilesName = CONFIGURATION_NOTICES_COLLECTOR;
        project.getConfigurations().create(noticeFilesName, configuration -> {
            configuration.setCanBeConsumed(true);
            configuration.setCanBeResolved(false);
            configuration.attributes(attrs ->
                    attrs.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LIBRARY_ELEMENT_NOTICE_FILES)));
        });

        project.getArtifacts().add(noticeFilesName, noticesCollectorProvider.flatMap(NoticeFilesCollectorTask::getOutputDir),
                configurablePublishArtifact -> configurablePublishArtifact.builtBy(noticesCollectorProvider));
    }

    protected List<Configuration> getRuntimeConfigurations(Project project, Configuration classpath) {
        List<Configuration> configurations = new ArrayList<>();
        configurations.add(classpath);
        Configuration embeddedClasspath = project.getConfigurations().findByName(EMBEDDED_CLASSPATH_NAME);
        if (embeddedClasspath != null) {
            configurations.add(embeddedClasspath);
        }

        return configurations;
    }
}
