package co.elastic.otel.android.compilation.tools.sourceheader.subplugins;

import org.gradle.api.Project;

public class AndroidSourceHeaderPlugin extends BaseSourceHeaderPlugin {

    @Override
    public void apply(Project project) {
        super.apply(project);
        spotlessExtension.java(javaExtension -> javaExtension.target("src/*/java/**/*.java"));
        project.afterEvaluate(self -> self.getTasks().getByName("preBuild", task -> task.dependsOn(getSpotlessApply(self))));
    }
}
