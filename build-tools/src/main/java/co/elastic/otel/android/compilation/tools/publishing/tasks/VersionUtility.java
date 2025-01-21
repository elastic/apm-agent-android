package co.elastic.otel.android.compilation.tools.publishing.tasks;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionUtility {
    private static final Logger logger = Logging.getLogger(VersionUtility.class);
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)[\\d.]+");

    public static String bumpMinorVersion(String version) {
        log("Bumping minor version for: " + version);
        Matcher versionMatcher = VERSION_PATTERN.matcher(version);
        if (versionMatcher.find()) {
            int currentMinorVersion = Integer.parseInt(versionMatcher.group(2));
            log("Current minor version is: " + currentMinorVersion);
            String newVersion = versionMatcher.group(1) + "." + (currentMinorVersion + 1) + ".0";
            log("The new version is: " + newVersion);
            return newVersion;
        } else {
            throw new IllegalArgumentException("Could not find minor version in: " + version);
        }
    }

    private static void log(String message) {
        logger.lifecycle(message);
    }
}
