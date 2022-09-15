package co.elastic.apm.compile.tools.subplugins;

import com.android.build.api.component.impl.ComponentImpl;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.Variant;

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import co.elastic.apm.compile.tools.tasks.CreateDependenciesListTask;
import co.elastic.apm.compile.tools.tasks.NoticeFilesCollectorTask;
import co.elastic.apm.compile.tools.tasks.NoticeMergerTask;
import co.elastic.apm.compile.tools.tasks.PomLicensesCollectorTask;

public class AarApmCompilerPlugin extends BasePlugin {

    @SuppressWarnings("unchecked")
    @Override
    public void apply(Project project) {
        super.apply(project);
        AndroidComponentsExtension<?, ?, Variant> componentsExtension = project.getExtensions().getByType(AndroidComponentsExtension.class);
        componentsExtension.onVariants(componentsExtension.selector().all(), variant -> {
            ComponentImpl component = (ComponentImpl) variant;
            TaskProvider<PomLicensesCollectorTask> pomLicensesFinder = project.getTasks().register(variant.getName() + "DependenciesLicencesFinder", PomLicensesCollectorTask.class, task -> {
                task.getRuntimeDependencies().set(component.getVariantDependencies().getRuntimeClasspath());
                task.getLicensesFound().set(project.getLayout().getBuildDirectory().file(task.getName() + "/licenses.txt"));
                task.getManualLicenseMapping().set(licensesConfig.manualMappingFile);
            });
            TaskProvider<NoticeFilesCollectorTask> noticeCollector = project.getTasks().register(variant.getName() + "NoticeFilesCollector", NoticeFilesCollectorTask.class, task -> {
                task.getRuntimeDependencies().set(component.getVariantDependencies().getRuntimeClasspath());
                task.getOutputDir().set(project.getLayout().getBuildDirectory().dir(task.getName()));
            });
            project.getTasks().register(variant.getName() + "NoticeFilesMerger", NoticeMergerTask.class, task -> {
                task.getNoticeFilesDir().set(noticeCollector.flatMap(NoticeFilesCollectorTask::getOutputDir));
                task.getOutputFile().set(project.getLayout().getBuildDirectory().file(task.getName() + "/" + "mergedNotice.txt"));
            });
            project.getTasks().register(variant.getName() + "CreateDependenciesList", CreateDependenciesListTask.class, task -> {
                task.getLicensesFound().set(pomLicensesFinder.flatMap(PomLicensesCollectorTask::getLicensesFound));
                task.getOutputFile().set(project.getLayout().getBuildDirectory().file(task.getName() + "/" + "licensed_dependencies.txt"));
            });
        });
    }
}
