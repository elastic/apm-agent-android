package co.elastic.apm.compile.tools.notice.plugins.subprojects;

import org.gradle.api.Project;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.tasks.TaskProvider;

import co.elastic.apm.compile.tools.notice.extensions.LicensesFinderExtension;
import co.elastic.apm.compile.tools.notice.plugins.BasePlugin;
import co.elastic.apm.compile.tools.notice.tasks.subprojects.NoticeFilesCollectorTask;
import co.elastic.apm.compile.tools.notice.tasks.subprojects.PomLicensesCollectorTask;

public class BaseSubprojectPlugin extends BasePlugin {

    private static final String EXTENSION_LICENSES_CONFIG = "licensesConfig";
    protected LicensesFinderExtension licensesConfig;

    @Override
    public void apply(Project project) {
        licensesConfig = project.getExtensions().create(EXTENSION_LICENSES_CONFIG, LicensesFinderExtension.class);
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
}
