package co.elastic.apm.compile.tools.subplugins;

import org.gradle.api.Project;

import co.elastic.apm.compile.tools.tasks.PomLicensesCollectorTask;

public class JarApmCompilerPlugin extends BasePlugin {

    @Override
    public void apply(Project project) {
        super.apply(project);
        project.getTasks().register("dependenciesLicencesFinder", PomLicensesCollectorTask.class, task -> {
            task.getRuntimeDependencies().set(project.getConfigurations().getByName("runtimeClasspath"));
            task.getLicensesFound().set(project.getLayout().getBuildDirectory().file(task.getName() + "/licenses.txt"));
            task.getManualLicenseMapping().set(licensesConfig.manualMappingFile);
        });
    }
}
