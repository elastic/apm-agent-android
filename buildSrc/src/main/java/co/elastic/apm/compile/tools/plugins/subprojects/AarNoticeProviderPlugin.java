package co.elastic.apm.compile.tools.plugins.subprojects;

import com.android.build.api.component.impl.ComponentImpl;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.Variant;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.tasks.TaskProvider;

import java.util.List;

import co.elastic.apm.compile.tools.extensions.AndroidApmExtension;
import co.elastic.apm.compile.tools.tasks.CreateDependenciesListTask;
import co.elastic.apm.compile.tools.tasks.CreateNoticeTask;
import co.elastic.apm.compile.tools.tasks.NoticeMergerTask;
import co.elastic.apm.compile.tools.tasks.subprojects.CopySingleFileTask;
import co.elastic.apm.compile.tools.tasks.subprojects.DependenciesHasherTask;
import co.elastic.apm.compile.tools.tasks.subprojects.NoticeFilesCollectorTask;
import co.elastic.apm.compile.tools.tasks.subprojects.PomLicensesCollectorTask;
import co.elastic.apm.compile.tools.utils.Constants;

public class AarNoticeProviderPlugin extends BaseSubprojectPlugin {

    @SuppressWarnings("unchecked")
    @Override
    public void apply(Project project) {
        super.apply(project);
        AndroidApmExtension apmExtension = project.getExtensions().create("androidNotice", AndroidApmExtension.class);

        AndroidComponentsExtension<?, ?, Variant> componentsExtension = project.getExtensions().getByType(AndroidComponentsExtension.class);
        componentsExtension.onVariants(componentsExtension.selector().all(), variant -> {
            ComponentImpl component = (ComponentImpl) variant;
            Configuration runtimeClasspath = component.getVariantDependencies().getRuntimeClasspath();
            Configuration apmToolsClasspath = wrapConfiguration(project, variant, runtimeClasspath);
            List<Configuration> runtimeConfigs = getRuntimeConfigurations(project, apmToolsClasspath);
            TaskProvider<DependenciesHasherTask> dependenciesHasher = project.getTasks().register(variant.getName() + "DependenciesHasher", DependenciesHasherTask.class, task -> {
                task.getRuntimeDependencies().set(runtimeConfigs);
                task.getOutputFile().set(project.getLayout().getBuildDirectory().file(task.getName() + "/" + "dependencies_hash.txt"));
            });
            TaskProvider<PomLicensesCollectorTask> pomLicensesFinder = project.getTasks().register(variant.getName() + "DependenciesLicencesFinder", PomLicensesCollectorTask.class, task -> {
                task.getRuntimeDependencies().set(runtimeConfigs);
                task.getLicensesFound().set(project.getLayout().getBuildDirectory().file(task.getName() + "/licenses.txt"));
                task.getManualLicenseMapping().set(licensesConfig.manualMappingFile);
            });
            TaskProvider<NoticeFilesCollectorTask> noticeCollector = project.getTasks().register(variant.getName() + "NoticeFilesCollector", NoticeFilesCollectorTask.class, task -> {
                task.getRuntimeDependencies().set(runtimeConfigs);
                task.getOutputDir().set(project.getLayout().getBuildDirectory().dir(task.getName()));
            });
            TaskProvider<NoticeMergerTask> noticeFilesMerger = project.getTasks().register(variant.getName() + "NoticeFilesMerger", NoticeMergerTask.class, task -> {
                task.getNoticeFilesDir().set(noticeCollector.flatMap(NoticeFilesCollectorTask::getOutputDir));
                task.getOutputFile().set(project.getLayout().getBuildDirectory().file(task.getName() + "/" + "mergedNotice.txt"));
            });
            TaskProvider<CreateDependenciesListTask> licensesDependencies = project.getTasks().register(variant.getName() + "CreateDependenciesList", CreateDependenciesListTask.class, task -> {
                task.getLicensesFound().set(pomLicensesFinder.flatMap(PomLicensesCollectorTask::getLicensesFound));
                task.getOutputFile().set(project.getLayout().getBuildDirectory().file(task.getName() + "/" + "licensed_dependencies.txt"));
            });
            TaskProvider<CreateNoticeTask> createNotice = project.getTasks().register(variant.getName() + StringUtils.capitalize(TASK_CREATE_NOTICE_FILE_NAME), CreateNoticeTask.class, task -> {
                task.getMergedNoticeFiles().from(noticeFilesMerger.flatMap(NoticeMergerTask::getOutputFile));
                task.getLicensedDependencies().set(licensesDependencies.flatMap(CreateDependenciesListTask::getOutputFile));
                task.getFoundLicensesIds().set(pomLicensesFinder.flatMap(PomLicensesCollectorTask::getLicensesFound));
                task.getDependenciesHashFile().set(dependenciesHasher.flatMap(DependenciesHasherTask::getOutputFile));
                task.getOutputFile().set(project.getLayout().getBuildDirectory().file(task.getName() + "/" + "notice_file.txt"));
            });
            if (apmExtension.variantName.get().equals(variant.getName())) {
                project.getTasks().register(TASK_CREATE_NOTICE_FILE_NAME, CopySingleFileTask.class, task -> {
                    task.getInputFile().set(createNotice.flatMap(CreateNoticeTask::getOutputFile));
                    task.getOutputFile().set(project.getLayout().getProjectDirectory().file("src/main/resources/META-INF/NOTICE"));
                });
                setUpLicensedDependencies(project, pomLicensesFinder);
                setUpNoticeFilesProvider(project, noticeCollector);
            }
        });
    }

    private Configuration wrapConfiguration(Project project, Variant variant, Configuration configuration) {
        return project.getConfigurations().create(variant.getName() + "ApmToolsClasspath", wrapper -> {
            wrapper.setCanBeConsumed(false);
            wrapper.setCanBeResolved(true);
            wrapper.extendsFrom(configuration);
            copyAttributes(configuration.getAttributes(), wrapper.getAttributes());
            wrapper.attributes(attributeContainer -> {
                attributeContainer.attribute(Constants.ARTIFACT_TYPE_ATTR, "android-classes");
            });
        });
    }

    @SuppressWarnings("unchecked")
    private void copyAttributes(AttributeContainer from, AttributeContainer to) {
        for (Attribute<?> attribute : from.keySet()) {
            Object value = from.getAttribute(attribute);
            to.attribute((Attribute<Object>) attribute, value);
        }
    }
}
