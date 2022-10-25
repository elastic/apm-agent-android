package co.elastic.apm.compile.tools.plugins;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.tasks.TaskProvider;

import co.elastic.apm.compile.tools.tasks.CreateDependenciesListTask;
import co.elastic.apm.compile.tools.tasks.CreateNoticeTask;
import co.elastic.apm.compile.tools.tasks.NoticeMergerTask;
import co.elastic.apm.compile.tools.tasks.rootproject.SubprojectNoticesCollectorTask;
import co.elastic.apm.compile.tools.tasks.rootproject.SubprojectPomLicensesMergerTask;

public class RootNoticeProviderPlugin extends BaseNoticePlugin {

    @Override
    public void apply(Project project) {
        Configuration bucket = project.getConfigurations().create("noticeProducer", configuration -> {
            configuration.setCanBeConsumed(false);
            configuration.setCanBeResolved(false);
        });
        Configuration licensedDependencies = getLicensedDependencies(project, bucket);
        Configuration noticeFiles = getNoticeFiles(project, bucket);

        TaskProvider<SubprojectPomLicensesMergerTask> pomLicenses = project.getTasks().register("mergeSubprojectLicensedDependencies", SubprojectPomLicensesMergerTask.class, task -> {
            task.getSubprojectLicensedDependenciesFiles().from(licensedDependencies);
            task.getMergedSubprojectLicensedDependencies().set(project.getLayout().getBuildDirectory().file(task.getName() + "/merged_dependencies.txt"));
        });

        TaskProvider<SubprojectNoticesCollectorTask> noticeFilesCollector = project.getTasks().register("mergeSubprojectNoticeFiles", SubprojectNoticesCollectorTask.class, task -> {
            task.getSubprojectNoticeFiles().from(noticeFiles);
            task.getMergedSubprojectNoticeFiles().set(project.getLayout().getBuildDirectory().dir(task.getName()));
        });

        TaskProvider<NoticeMergerTask> noticeFilesMerger = project.getTasks().register("noticeFilesMerger", NoticeMergerTask.class, task -> {
            task.getNoticeFilesDir().set(noticeFilesCollector.flatMap(SubprojectNoticesCollectorTask::getMergedSubprojectNoticeFiles));
            task.getOutputFile().set(project.getLayout().getBuildDirectory().file(task.getName() + "/" + "mergedNotice.txt"));
        });

        TaskProvider<CreateDependenciesListTask> licensesDependencies = project.getTasks().register("createDependenciesList", CreateDependenciesListTask.class, task -> {
            task.getLicensesFound().set(pomLicenses.flatMap(SubprojectPomLicensesMergerTask::getMergedSubprojectLicensedDependencies));
            task.getOutputFile().set(project.getLayout().getBuildDirectory().file(task.getName() + "/" + "licensed_dependencies.txt"));
        });

        project.getTasks().register(TASK_CREATE_NOTICE_FILE_NAME, CreateNoticeTask.class, task -> {
            task.getMergedNoticeFiles().from(noticeFilesMerger.flatMap(NoticeMergerTask::getOutputFile));
            task.getLicensedDependencies().set(licensesDependencies.flatMap(CreateDependenciesListTask::getOutputFile));
            task.getFoundLicensesIds().set(pomLicenses.flatMap(SubprojectPomLicensesMergerTask::getMergedSubprojectLicensedDependencies));
            task.getOutputFile().set(project.getLayout().getProjectDirectory().file("NOTICE"));
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
