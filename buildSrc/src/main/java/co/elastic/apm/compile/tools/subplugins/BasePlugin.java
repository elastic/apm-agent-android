package co.elastic.apm.compile.tools.subplugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import co.elastic.apm.compile.tools.extensions.LicensesFinderExtension;

public class BasePlugin implements Plugin<Project> {

    private static final String EXTENSION_LICENSES_CONFIG = "licensesConfig";
    protected LicensesFinderExtension licensesConfig;

    @Override
    public void apply(Project project) {
        licensesConfig = project.getExtensions().create(EXTENSION_LICENSES_CONFIG, LicensesFinderExtension.class);
    }
}
