package co.elastic.apm.android.agp.api;

import com.android.build.api.AndroidPluginVersion;

public class CurrentVersion {
    private final AndroidPluginVersion current;

    public CurrentVersion(AndroidPluginVersion current) {
        this.current = current;
    }

    public boolean isEqualTo(AndroidPluginVersion other) {
        return current.compareTo(other) == 0;
    }

    public boolean isGreaterThan(AndroidPluginVersion other) {
        return current.compareTo(other) > 0;
    }

    public boolean isLowerThan(AndroidPluginVersion other) {
        return current.compareTo(other) < 0;
    }
}
