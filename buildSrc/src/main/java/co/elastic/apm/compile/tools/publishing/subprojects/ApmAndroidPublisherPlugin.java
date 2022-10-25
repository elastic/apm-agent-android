package co.elastic.apm.compile.tools.publishing.subprojects;

import com.android.build.api.dsl.LibraryExtension;

import org.gradle.api.Project;

public class ApmAndroidPublisherPlugin extends BaseApmPublisherPlugin {

    @Override
    public void apply(Project project) {
        LibraryExtension androidExtension = project.getExtensions().getByType(LibraryExtension.class);
        String componentName = "release";
        androidExtension.getPublishing().singleVariant(componentName);

        addMavenPublication(project, componentName);
    }
}
