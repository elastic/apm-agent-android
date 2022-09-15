package co.elastic.apm.compile.tools.subplugins;

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import co.elastic.apm.compile.tools.tasks.CreateDependenciesListTask;
import co.elastic.apm.compile.tools.tasks.NoticeFilesCollectorTask;
import co.elastic.apm.compile.tools.tasks.NoticeMergerTask;
import co.elastic.apm.compile.tools.tasks.PomLicensesCollectorTask;

public class JarApmCompilerPlugin extends BasePlugin {

    @Override
    public void apply(Project project) {
        super.apply(project);
        TaskProvider<PomLicensesCollectorTask> pomLicensesFinder = project.getTasks().register("dependenciesLicencesFinder", PomLicensesCollectorTask.class, task -> {
            task.getRuntimeDependencies().set(project.getConfigurations().getByName("runtimeClasspath"));
            task.getLicensesFound().set(project.getLayout().getBuildDirectory().file(task.getName() + "/licenses.txt"));
            task.getManualLicenseMapping().set(licensesConfig.manualMappingFile);
        });

        TaskProvider<NoticeFilesCollectorTask> noticeCollector = project.getTasks().register("noticeFilesCollector", NoticeFilesCollectorTask.class, task -> {
            task.getRuntimeDependencies().set(project.getConfigurations().getByName("runtimeClasspath"));
            task.getOutputDir().set(project.getLayout().getBuildDirectory().dir(task.getName()));
        });

        project.getTasks().register("noticeFilesMerger", NoticeMergerTask.class, task -> {
            task.getNoticeFilesDir().set(noticeCollector.flatMap(NoticeFilesCollectorTask::getOutputDir));
            task.getOutputFile().set(project.getLayout().getBuildDirectory().file(task.getName() + "/" + "mergedNotice.txt"));
        });

        project.getTasks().register("createDependenciesList", CreateDependenciesListTask.class, task -> {
            task.getLicensesFound().set(pomLicensesFinder.flatMap(PomLicensesCollectorTask::getLicensesFound));
            task.getOutputFile().set(project.getLayout().getBuildDirectory().file(task.getName() + "/" + "licensed_dependencies.txt"));
        });
    }
}
