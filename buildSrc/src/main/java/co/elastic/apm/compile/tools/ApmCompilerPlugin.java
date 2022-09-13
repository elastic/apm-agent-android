package co.elastic.apm.compile.tools;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import co.elastic.apm.compile.tools.extensions.LicensesFinderExtension;
import co.elastic.apm.compile.tools.tasks.PomLicensesCollector;

public class ApmCompilerPlugin implements Plugin<Project> {
    private static final String EXTENSION_LICENSES_CONFIG = "licensesConfig";

    @Override
    public void apply(Project project) {
        LicensesFinderExtension licensesConfig = project.getExtensions().create(EXTENSION_LICENSES_CONFIG, LicensesFinderExtension.class);

        project.getTasks().register("dependenciesLicencesFinder", PomLicensesCollector.class, task -> {
            task.getRuntimeDependencies().set(project.getConfigurations().getByName("runtimeClasspath"));
            task.getLicensesFound().set(project.getLayout().getBuildDirectory().file(task.getName() + "/licenses.txt"));
            task.getManualLicenseMapping().set(licensesConfig.manualMappingFile);
        });
    }
}
