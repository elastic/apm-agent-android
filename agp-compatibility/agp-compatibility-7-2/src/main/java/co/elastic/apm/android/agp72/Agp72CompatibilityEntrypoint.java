package co.elastic.apm.android.agp72;

import com.android.build.api.AndroidPluginVersion;

import org.gradle.api.Project;

import co.elastic.apm.android.agp.api.AgpCompatibilityEntrypoint;
import co.elastic.apm.android.agp.api.AgpCompatibilityManager;
import co.elastic.apm.android.agp.api.CurrentVersion;

public class Agp72CompatibilityEntrypoint implements AgpCompatibilityEntrypoint {

    @Override
    public String getDescription() {
        return "Agp compatible for 7.2+";
    }

    @Override
    public boolean isCompatible(CurrentVersion currentVersion) {
        if (currentVersion.isLowerThan(new AndroidPluginVersion(7, 2))) {
            return false;
        }
        return currentVersion.isLowerThan(new AndroidPluginVersion(7, 3));
    }

    @Override
    public AgpCompatibilityManager provideCompatibilityManager(Project project) {
        return new Agp72CompatibilityManager(project);
    }
}
