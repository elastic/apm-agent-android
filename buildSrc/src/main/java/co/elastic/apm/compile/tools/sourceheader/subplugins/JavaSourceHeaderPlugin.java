package co.elastic.apm.compile.tools.sourceheader.subplugins;

import org.gradle.api.Project;

public class JavaSourceHeaderPlugin extends BaseSourceHeaderPlugin {

    @Override
    public void apply(Project project) {
        super.apply(project);
        project.afterEvaluate(self -> self.getTasks().getByName("classes", task -> task.dependsOn(getSpotlessApply(self))));
    }
}
