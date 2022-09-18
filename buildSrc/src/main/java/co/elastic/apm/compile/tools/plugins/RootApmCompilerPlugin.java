package co.elastic.apm.compile.tools.plugins;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.LibraryElements;

import co.elastic.apm.compile.tools.tasks.rootproject.SubprojectLicensesMergerTask;
import co.elastic.apm.compile.tools.tasks.rootproject.SubprojectNoticesCollectorTask;

public class RootApmCompilerPlugin extends BasePlugin {

    @Override
    public void apply(Project project) {
        Configuration bucket = project.getConfigurations().create("noticeProducer", configuration -> {
            configuration.setCanBeConsumed(false);
            configuration.setCanBeResolved(false);
        });
        Configuration licensedDependencies = getLicensedDependencies(project, bucket);
        Configuration noticeFiles = getNoticeFiles(project, bucket);

        project.getTasks().register("mergeSubprojectLicensedDependencies", SubprojectLicensesMergerTask.class, task -> {
            task.getSubprojectLicensedDependenciesFiles().from(licensedDependencies);
            task.getMergedSubprojectLicensedDependencies().set(project.getLayout().getBuildDirectory().file(task.getName() + "/merged_dependencies.txt"));
        });

        project.getTasks().register("mergeSubprojectNoticeFiles", SubprojectNoticesCollectorTask.class, task -> {
            task.getSubprojectNoticeFiles().from(noticeFiles);
            task.getMergedSubprojectNoticeFiles().set(project.getLayout().getBuildDirectory().dir(task.getName()));
        });
    }

    private Configuration getLicensedDependencies(Project project, Configuration bucket) {
        return project.getConfigurations().create(CONFIGURATION_LICENSES_COLLECTOR + "Classpath", configuration -> {
            configuration.setCanBeResolved(true);
            configuration.setCanBeConsumed(false);
            configuration.extendsFrom(bucket);
            configuration.attributes(attributes ->
                    attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LIBRARY_ELEMENT_LICENSED_DEPENDENCIES)));
        });
    }

    private Configuration getNoticeFiles(Project project, Configuration bucket) {
        return project.getConfigurations().create(CONFIGURATION_NOTICES_COLLECTOR + "Classpath", configuration -> {
            configuration.setCanBeResolved(true);
            configuration.setCanBeConsumed(false);
            configuration.extendsFrom(bucket);
            configuration.attributes(attributes ->
                    attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LIBRARY_ELEMENT_NOTICE_FILES)));
        });
    }
}
