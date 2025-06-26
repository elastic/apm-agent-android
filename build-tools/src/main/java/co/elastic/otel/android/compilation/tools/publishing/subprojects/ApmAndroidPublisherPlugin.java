package co.elastic.otel.android.compilation.tools.publishing.subprojects;

import com.android.build.api.dsl.LibraryExtension;

import kotlin.Unit;

public class ApmAndroidPublisherPlugin extends BaseApmPublisherPlugin {

    @Override
    protected void onApply() {
        LibraryExtension androidExtension = project.getExtensions().getByType(LibraryExtension.class);
        String componentName = "release";
        androidExtension.getPublishing().singleVariant(componentName, librarySingleVariant -> {
            if (isRelease()) {
                librarySingleVariant.withJavadocJar();
                librarySingleVariant.withSourcesJar();
            }
            return Unit.INSTANCE;
        });

        project.afterEvaluate(self -> {
            addMavenPublication(componentName);
            enableMavenCentralPublishing();
        });
    }
}
