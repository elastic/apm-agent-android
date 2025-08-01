package co.elastic.otel.android.compilation.tools.publishing;

import static co.elastic.otel.android.compilation.tools.publishing.PublishingUtils.setArtifactId;
import static co.elastic.otel.android.compilation.tools.publishing.PublishingUtils.setGroupId;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.util.internal.VersionNumber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.elastic.otel.android.compilation.tools.NoticeProviderPlugin;
import co.elastic.otel.android.compilation.tools.plugins.RootNoticeProviderPlugin;
import co.elastic.otel.android.compilation.tools.sourceheader.ApmSourceHeaderPlugin;
import kotlinx.validation.ApiValidationExtension;
import kotlinx.validation.BinaryCompatibilityValidatorPlugin;

public class ApmPublisherRootPlugin implements Plugin<Project> {

    private final static String PROPERTY_VERSION_OVERRIDE = "version_override";
    private final static Pattern INSTRUMENTATION_PROJECT_PATTERN = Pattern.compile(":instrumentation:([^:]+):([^:]+)$");

    @Override
    public void apply(Project project) {
        configureVersion(project);
        applyRootPlugins(project.getPlugins());
        project.subprojects(subproject -> {
            String path = subproject.getPath();
            if (!path.startsWith(":internal-tools")) {
                if (path.startsWith(":instrumentation:")) {
                    setGroupId(subproject, subproject.getGroup() + ".instrumentation");
                }
                Matcher instrumentationMatcher = INSTRUMENTATION_PROJECT_PATTERN.matcher(path);
                if (instrumentationMatcher.matches()) {
                    setArtifactId(subproject, instrumentationMatcher.group(1) + "-" + instrumentationMatcher.group(2));
                    subproject.setGroup(subproject.getGroup() + "." + instrumentationMatcher.group(1));
                }
                if ("true".equals(subproject.findProperty("elastic.experimental"))) {
                    subproject.setVersion(subproject.getVersion() + "-alpha");
                }

                subproject.getPluginManager().withPlugin("java-library", appliedPlugin -> configureProject(project, subproject));
                subproject.getPluginManager().withPlugin("com.android.library", appliedPlugin -> configureProject(project, subproject));
            }
        });
    }

    private void configureProject(Project project, Project subproject) {
        applySubprojectPlugins(subproject.getPlugins());
        project.getDependencies().add("noticeProducer", subproject);
        configureBinaryValidator(subproject);
    }

    private static void configureBinaryValidator(Project subproject) {
        ApiValidationExtension binaryValidatorExtension = subproject.getExtensions().getByType(ApiValidationExtension.class);
        binaryValidatorExtension.getNonPublicMarkers().add("co.elastic.otel.android.common.internal.annotations.InternalApi");
        binaryValidatorExtension.setApiDumpDirectory("metadata");
    }

    private void configureVersion(Project project) {
        String versionOverride = getVersionOverride(project);
        if (versionOverride != null) {
            validateVersionOverrideFormatting(versionOverride);
            validateVersionPatchChangeOnly(project.getVersion().toString(), versionOverride);
            System.out.println("Overriding version with: '" + versionOverride + "'");
            project.setVersion(versionOverride);
            project.subprojects(subproject -> subproject.setVersion(versionOverride));
        }
    }

    private static void validateVersionPatchChangeOnly(String currentVersion, String versionOverride) {
        VersionNumber comparableVersion = VersionNumber.parse(currentVersion);
        VersionNumber comparableVersionOverride = VersionNumber.parse(versionOverride);
        if (comparableVersionOverride.getMajor() > comparableVersion.getMajor() || comparableVersionOverride.getMinor() > comparableVersion.getMinor()) {
            throw new IllegalArgumentException(String.format("The version override, '%s', cannot provide greater major or minor numbers than the existing version from the gradle.properties file: '%s'.", versionOverride, currentVersion));
        }
    }

    private static void validateVersionOverrideFormatting(String versionOverride) {
        Pattern semverPattern = Pattern.compile("\\d+\\.\\d+\\.\\d+");
        if (!semverPattern.matcher(versionOverride).matches()) {
            throw new IllegalArgumentException(String.format("The provided version override, '%s', does not have a valid format.", versionOverride));
        }
    }

    private String getVersionOverride(Project project) {
        if (!project.hasProperty(PROPERTY_VERSION_OVERRIDE)) {
            return null;
        }
        String property = (String) project.property(PROPERTY_VERSION_OVERRIDE);
        if (property == null) {
            return null;
        }
        property = property.trim();
        if (property.isEmpty()) {
            return null;
        }
        return property;
    }

    private void applySubprojectPlugins(PluginContainer subprojectPlugins) {
        subprojectPlugins.apply(BinaryCompatibilityValidatorPlugin.class);
        subprojectPlugins.apply(ApmSourceHeaderPlugin.class);
        subprojectPlugins.apply(NoticeProviderPlugin.class);
        subprojectPlugins.apply(ApmPublisherPlugin.class);
    }

    private void applyRootPlugins(PluginContainer plugins) {
        plugins.apply(RootNoticeProviderPlugin.class);
    }
}
