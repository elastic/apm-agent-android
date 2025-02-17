package co.elastic.otel.android.compilation.tools.publishing.tasks;

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
import java.io.PrintStream;
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

        String newVersion = VersionUtility.bumpMinorVersion(currentVersion);

        updateNextVersion(gradlePropertiesFile, properties, newVersion);
        updateChangelog(currentVersion);
        updateSetupDoc(currentVersion);

        createPullRequestWithChanges(currentVersion);
        setGitHubRelease(currentVersion, releaseTag);
        log("Finished the post deploy task successfully");
    }

    private void createPullRequestWithChanges(String version) {
        log("Create new branch");
        String title = "Release " + version;
        String newBranch = "post-release/" + version;
        runCommand("git checkout -b " + newBranch);
        log("Committing changes");
        runCommand("git commit -a -m \"Preparing for the next release\"");
        log("Pushing changes");
        runCommand("git push origin " + newBranch);
        runCommand("gh pr create --title \"" + title + "\" --body \"" + title + "\" --base main --reviewer elastic/apm-agent-android --repo elastic/apm-agent-android");
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
        runCommand("gh release create " + releaseTag + " --title \"" + title + "\" --notes \"" + notes + "\"");
    }

    private void updateNextVersion(File gradlePropertiesFile, Properties properties, String newVersion) {
        log("Updating version to: " + newVersion);
        properties.setProperty("version", newVersion);
        saveProperties(properties, gradlePropertiesFile);
    }

    private void updateChangelog(String newVersion) {
        log("Updating changelog with version: " + newVersion);
        Path changelogPath = getProjectPath("CHANGELOG.asciidoc");
        String contents = getContents(changelogPath);
        contents = uncommentNextRelease(contents);
        replaceFileContents(changelogPath, resolvePlaceholders(contents, newVersion));
    }

    private void updateSetupDoc(String currentVersion) {
        log("Updating setup doc to show version: " + currentVersion);
        Path setupDocPath = getProjectPath("docs/setup.asciidoc");
        String contents = getContents(setupDocPath);
        Pattern versionMentionsPattern = Pattern.compile("(?<=co\\.elastic\\.apm\\.android/)\\d+\\.\\d+\\.\\d+|(?<=co\\.elastic\\.apm:agent-sdk:)\\d+\\.\\d+\\.\\d+|(?<=id\\s\"co\\.elastic\\.apm\\.android\"\\sversion\\s\")\\d+\\.\\d+\\.\\d+");
        Matcher versionMentionMatcher = versionMentionsPattern.matcher(contents);

        String setupDocWithNewVersion = versionMentionMatcher.replaceAll(currentVersion);
        replaceFileContents(setupDocPath, setupDocWithNewVersion.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] resolvePlaceholders(String text, String newVersion) {
        Map<String, String> substitutions = new HashMap<>();
        substitutions.put("release_date", new SimpleDateFormat("yyyy/MM/dd", Locale.US).format(new Date()));
        substitutions.put("version", newVersion);
        substitutions.put("next_release_notes", getNewReleaseNotes());
        StringSubstitutor substitutor = new StringSubstitutor(substitutions);
        return substitutor.replace(text).getBytes(StandardCharsets.UTF_8);
    }

    private Path getProjectPath(String relativePath) {
        File changelog = new File(relativePath);
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
        processBuilder.command("bash", "-c", scapeSpecialChars(command));

        try {
            Process process = processBuilder.start();
            BufferedReader successReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            new OutputPrinter(successReader, System.out).start();
            new OutputPrinter(errorReader, System.err).start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Error running command: " + command);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String scapeSpecialChars(String command) {
        return command.replaceAll("([()])", "\\\\$1");
    }

    private static class OutputPrinter implements Runnable {
        private final BufferedReader reader;
        private final PrintStream output;

        private OutputPrinter(BufferedReader reader, PrintStream output) {
            this.reader = reader;
            this.output = output;
        }

        public void start() {
            new Thread(this).start();
        }

        @Override
        public void run() {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.println(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
