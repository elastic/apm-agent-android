package co.elastic.otel.android.compilation.tools.publishing;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.util.internal.VersionNumber;

import java.time.Duration;
import java.util.regex.Pattern;

import co.elastic.otel.android.compilation.tools.NoticeProviderPlugin;
import co.elastic.otel.android.compilation.tools.plugins.RootNoticeProviderPlugin;
import co.elastic.otel.android.compilation.tools.publishing.tasks.PostDeployTask;
import co.elastic.otel.android.compilation.tools.sourceheader.ApmSourceHeaderPlugin;
import io.github.gradlenexus.publishplugin.NexusPublishExtension;
import io.github.gradlenexus.publishplugin.NexusPublishPlugin;
import io.github.gradlenexus.publishplugin.NexusRepositoryContainer;

public class ApmPublisherRootPlugin implements Plugin<Project> {

    private final static String PROPERTY_VERSION_OVERRIDE = "version_override";

    @Override
    public void apply(Project project) {
        configureVersion(project);
        applyRootPlugins(project.getPlugins());
        addPostDeployTask(project);
        configureMavenCentral(project);
        project.subprojects(subproject -> {
            if (!subproject.getName().equals("agp-compatibility")) {
                applySubprojectPlugins(subproject.getPlugins());
                project.getDependencies().add("noticeProducer", subproject);
            }
        });
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

    private void addPostDeployTask(Project project) {
        project.getTasks().register("postDeploy", PostDeployTask.class);
    }

    private void applySubprojectPlugins(PluginContainer subprojectPlugins) {
        subprojectPlugins.apply(ApmSourceHeaderPlugin.class);
        subprojectPlugins.apply(NoticeProviderPlugin.class);
        subprojectPlugins.apply(ApmPublisherPlugin.class);
    }

    private void applyRootPlugins(PluginContainer plugins) {
        plugins.apply(RootNoticeProviderPlugin.class);
        plugins.apply(NexusPublishPlugin.class);
    }

    private void configureMavenCentral(Project project) {
        NexusPublishExtension nexusPublishExtension = project.getExtensions().getByType(NexusPublishExtension.class);
        nexusPublishExtension.repositories(NexusRepositoryContainer::sonatype);
        nexusPublishExtension.getClientTimeout().set(Duration.ofMinutes(10));
        nexusPublishExtension.getConnectTimeout().set(Duration.ofMinutes(10));
        nexusPublishExtension.transitionCheckOptions(transitionCheckOptions -> {
            transitionCheckOptions.getMaxRetries().set(200);
            transitionCheckOptions.getDelayBetween().set(Duration.ofSeconds(15));
        });
    }
}
