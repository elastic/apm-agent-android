package co.elastic.apm.compile.tools;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import co.elastic.apm.compile.tools.tasks.PomLicensesCollector;

public class ApmProjectPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().register("dependenciesLicencesFinder", PomLicensesCollector.class, task -> {
            task.getRuntimeDependencies().set(project.getConfigurations().getByName("runtimeClasspath"));
            task.getLicensesFound().set(project.getLayout().getBuildDirectory().file(task.getName() + "/licenses.txt"));
        });
    }
}
