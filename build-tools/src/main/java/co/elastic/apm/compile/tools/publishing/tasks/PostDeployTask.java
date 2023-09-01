package co.elastic.apm.compile.tools.publishing.tasks;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostDeployTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(VersionUtility.class);
    private static final Pattern COMMENTED_NEXT_RELEASE_PATTERN = Pattern.compile("/{4}\\s+(\\$\\{next_release_notes}[\\s\\S]+)/{4}\\s?");

    @TaskAction
    public void execute() {
        File gradlePropertiesFile = getGradlePropertiesFile();
        Properties properties = getProperties(gradlePropertiesFile);

        String currentVersion = properties.getProperty("version");
        String releaseTag = "v" + currentVersion;
        setGitTag(releaseTag);
        setGitHubRelease(currentVersion, releaseTag);

        String newVersion = VersionUtility.bumpMinorVersion(currentVersion);

        updateNextVersion(gradlePropertiesFile, properties, newVersion);
        updateChangelog(currentVersion);

        publishChanges();
        log("Finished the post deploy task successfully");
    }

    private void publishChanges() {
        log("Committing changes");
        runCommand("git commit -a -m \"Preparing for the next release\"");
        log("Pushing changes");
        runCommand("git push");
    }

    private void setGitTag(String version) {
        log("Setting git tag to: " + version);
        runCommand("git tag " + version);
        runCommand("git push --tags");
    }

    private void setGitHubRelease(String version, String releaseTag) {
        log("Setting GitHub release to: " + releaseTag);
        String title = "Release " + version;
        String notes = "[Release Notes for " + version + "](https://www.elastic.co/guide/en/apm/agent/android/current/release-notes-0.x.html#release-notes-" + version + ")";
        runCommand("gh release create " + releaseTag + " --title " + title + " --notes " + notes);
    }

    private void updateNextVersion(File gradlePropertiesFile, Properties properties, String newVersion) {
        log("Updating version to: " + newVersion);
        properties.setProperty("version", newVersion);
        saveProperties(properties, gradlePropertiesFile);
    }

    private void updateChangelog(String newVersion) {
        log("Updating changelog with version: " + newVersion);
        Path changelogPath = getChangelogPath();
        String contents = getContents(changelogPath);
        contents = uncommentNextRelease(contents);
        replaceFileContents(changelogPath, resolvePlaceholders(contents, newVersion));
    }

    private byte[] resolvePlaceholders(String text, String newVersion) {
        Map<String, String> substitutions = new HashMap<>();
        substitutions.put("release_date", new SimpleDateFormat("yyyy/MM/dd", Locale.US).format(new Date()));
        substitutions.put("version", newVersion);
        substitutions.put("next_release_notes", getNewReleaseNotes());
        StringSubstitutor substitutor = new StringSubstitutor(substitutions);
        return substitutor.replace(text).getBytes(StandardCharsets.UTF_8);
    }

    private Path getChangelogPath() {
        File changelog = new File("CHANGELOG.asciidoc");
        return changelog.toPath();
    }

    private String uncommentNextRelease(String contents) {
        Matcher matcher = COMMENTED_NEXT_RELEASE_PATTERN.matcher(contents);
        if (!matcher.find()) {
            return contents;
        }
        return contents.replace(matcher.group(), matcher.group(1));
    }

    private String getContents(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getNewReleaseNotes() {
        try (InputStream is = getClass().getResourceAsStream("/changelog/release_notes_template.txt")) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void replaceFileContents(Path path, byte[] contents) {
        try {
            Files.write(path, contents);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Properties getProperties(File from) {
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(from)) {
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveProperties(Properties properties, File into) {
        try (OutputStream out = new FileOutputStream(into)) {
            properties.store(out, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getGradlePropertiesFile() {
        return getProject().file("gradle.properties");
    }

    private void log(String message) {
        logger.lifecycle(message);
    }

    private void runCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);

        try {
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println(output);
            } else {
                throw new RuntimeException(output.toString());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
