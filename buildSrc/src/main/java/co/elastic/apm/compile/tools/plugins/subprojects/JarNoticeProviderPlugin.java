package co.elastic.apm.compile.tools.plugins.subprojects;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskProvider;

import java.util.List;

import co.elastic.apm.compile.tools.tasks.CreateDependenciesListTask;
import co.elastic.apm.compile.tools.tasks.CreateNoticeTask;
import co.elastic.apm.compile.tools.tasks.NoticeMergerTask;
import co.elastic.apm.compile.tools.tasks.subprojects.NoticeFilesCollectorTask;
import co.elastic.apm.compile.tools.tasks.subprojects.PomLicensesCollectorTask;

public class JarNoticeProviderPlugin extends BaseSubprojectPlugin {

    @Override
    public void apply(Project project) {
        super.apply(project);
        List<Configuration> runtimeClasspath = getRuntimeConfigurations(project, project.getConfigurations().getByName("runtimeClasspath"));
        TaskProvider<PomLicensesCollectorTask> pomLicensesFinder = project.getTasks().register("dependenciesLicencesFinder", PomLicensesCollectorTask.class, task -> {
            task.getRuntimeDependencies().set(runtimeClasspath);
            task.getLicensesFound().set(project.getLayout().getBuildDirectory().file(task.getName() + "/licenses.txt"));
            task.getManualLicenseMapping().set(licensesConfig.manualMappingFile);
        });

        TaskProvider<NoticeFilesCollectorTask> noticeCollector = project.getTasks().register("noticeFilesCollector", NoticeFilesCollectorTask.class, task -> {
            task.getRuntimeDependencies().set(runtimeClasspath);
            task.getOutputDir().set(project.getLayout().getBuildDirectory().dir(task.getName()));
        });

        TaskProvider<NoticeMergerTask> noticeFilesMerger = project.getTasks().register("noticeFilesMerger", NoticeMergerTask.class, task -> {
            task.getNoticeFilesDir().set(noticeCollector.flatMap(NoticeFilesCollectorTask::getOutputDir));
            task.getOutputFile().set(project.getLayout().getBuildDirectory().file(task.getName() + "/" + "mergedNotice.txt"));
        });

        TaskProvider<CreateDependenciesListTask> licensesDependencies = project.getTasks().register("createDependenciesList", CreateDependenciesListTask.class, task -> {
            task.getLicensesFound().set(pomLicensesFinder.flatMap(PomLicensesCollectorTask::getLicensesFound));
            task.getOutputFile().set(project.getLayout().getBuildDirectory().file(task.getName() + "/" + "licensed_dependencies.txt"));
        });

        project.getTasks().register(TASK_CREATE_NOTICE_FILE_NAME, CreateNoticeTask.class, task -> {
            task.getMergedNoticeFiles().from(noticeFilesMerger.flatMap(NoticeMergerTask::getOutputFile));
            task.getLicensedDependencies().set(licensesDependencies.flatMap(CreateDependenciesListTask::getOutputFile));
            task.getFoundLicensesIds().set(pomLicensesFinder.flatMap(PomLicensesCollectorTask::getLicensesFound));
            task.getOutputFile().set(project.getLayout().getProjectDirectory().file("src/main/resources/META-INF/NOTICE"));
        });

        setUpLicensedDependencies(project, pomLicensesFinder);
        setUpNoticeFilesProvider(project, noticeCollector);
    }
}
