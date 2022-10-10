package co.elastic.apm.compile.tools.sourceheader.subplugins;

import org.gradle.api.Project;

public class AndroidSourceHeaderPlugin extends BaseSourceHeaderPlugin {

    @Override
    public void apply(Project project) {
        super.apply(project);
        spotlessExtension.java(javaExtension -> javaExtension.target("src/*/java/**/*.java"));
    }
}
