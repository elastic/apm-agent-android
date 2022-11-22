/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.apm.android.plugin.testutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.elastic.apm.android.plugin.testutils.buildgradle.BuildFileBuilder;
import com.elastic.apm.android.plugin.testutils.buildgradle.SettingsGradleBuilder;
import com.elastic.apm.android.plugin.testutils.buildgradle.block.impl.ElasticBlockBuilder;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.testkit.runner.UnexpectedBuildFailure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import co.elastic.apm.android.plugin.testutils.buildgradle.BuildFileBuilder;
import co.elastic.apm.android.plugin.testutils.buildgradle.block.impl.ElasticBlockBuilder;
import co.elastic.apm.android.plugin.testutils.buildgradle.block.impl.settings.SettingsGradleBuilder;

import java.util.Properties;

public abstract class BaseFunctionalTest {
    protected File buildFile;
    protected File androidManifest;
    protected File gradleProperties;
    private final BuildFileBuilder buildFileBuilder;
    private final SettingsGradleBuilder settingsGradleBuilder;
    private BuildResult latestResult;

    protected BaseFunctionalTest() {
        buildFileBuilder = new BuildFileBuilder(getAndroidCompileSdk(), getAndroidAppId(), "1.0");
        buildFileBuilder.addRepository("mavenCentral()");
        buildFileBuilder.addRepository("google()");
        settingsGradleBuilder = new SettingsGradleBuilder();

        settingsGradleBuilder.getBuildscriptBlockBuilder().addRepository("gradlePluginPortal()");
        settingsGradleBuilder.getBuildscriptBlockBuilder().addRepository("mavenCentral()");
        settingsGradleBuilder.getBuildscriptBlockBuilder().addRepository("google()");

        File metadataFile = Paths.get("build", "pluginUnderTestMetadata", "plugin-under-test-metadata.properties").toFile();
        try (InputStream metadataStream = new FileInputStream(metadataFile)) {
            Properties properties = new Properties();
            properties.load(metadataStream);
            String localClasspath = properties.getProperty("implementation-classpath");
            String[] classpath = localClasspath.split(File.pathSeparator);
            for (String path : classpath) {
                settingsGradleBuilder.getBuildscriptBlockBuilder().addDependency(String.format("classpath files('%s')", path));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        settingsGradleBuilder.getBuildscriptBlockBuilder().addDependency(String.format("classpath 'com.android.tools.build:gradle:%s'", getAndroidGradlePluginVersion()));
    }

    protected abstract File getProjectDir();

    protected abstract String getAndroidGradlePluginVersion();

    protected void addPlugin(String id) {
        buildFileBuilder.addPlugin(id);
    }

    protected ElasticBlockBuilder getDefaultElasticBlockBuilder() {
        return buildFileBuilder.getElasticBlockBuilder();
    }

    protected void setUpProject() {
        buildFile = createBuildFile();
        androidManifest = createAndroidManifest();
        gradleProperties = createGradleProperties();
        createGradleSettings();
    }

    private File createBuildFile() {
        File file = new File(getProjectDir(), "build.gradle");
        FileUtils.write(file, buildFileBuilder.build());
        return file;
    }

    private File createAndroidManifest() {
        File file = new File(getProjectDir(), "src/main/AndroidManifest.xml");
        ensureExistingParentDir(file);
        FileUtils.write(file, "<manifest/>");

        return file;
    }

    private File createGradleProperties() {
        File file = new File(getProjectDir(), "gradle.properties");
        ensureExistingParentDir(file);
        FileUtils.write(file, "android.useAndroidX=true");

        return file;
    }

    private File createGradleSettings() {
        File file = new File(getProjectDir(), "settings.gradle");
        ensureExistingParentDir(file);
        FileUtils.write(file, settingsGradleBuilder.build());

        return file;
    }

    private void ensureExistingParentDir(File file) {
        if (!file.getParentFile().exists()) {
            boolean created = file.getParentFile().mkdirs();
            if (!created) {
                throw new RuntimeException("Could not create parent dir for " + file);
            }
        }
    }

    protected int getAndroidCompileSdk() {
        return 32;
    }

    protected String getAndroidAppId() {
        return "com.example.app";
    }

    protected void verifyTaskIsSuccessful(String taskName) {
        assertEquals(TaskOutcome.SUCCESS, latestResult.task(taskName).getOutcome());
    }

    protected void verifyOutputContains(String text) {
        try {
            assertTrue(latestResult.getOutput().contains(text));
        } catch (AssertionError e) {
            System.out.println("Build output: " + latestResult.getOutput());
            throw e;
        }
    }

    protected File getBuildDirFile(String path) {
        return new File(getProjectDir(), "build/" + path);
    }

    protected void runFailedGradle(String command) {
        latestResult = getRunner(command).buildAndFail();
    }

    protected void runGradle(String command) {
        try {
            latestResult = getRunner(command).build();
        } catch (UnexpectedBuildFailure e) {
            System.out.println("Build file:\n");
            System.out.println(FileUtils.read(buildFile));
            throw new RuntimeException(e);
        }
    }

    private GradleRunner getRunner(String command) {
        try {
            command += " --include-build " + Paths.get("..").toFile().getCanonicalPath();
            List<String> commands = new ArrayList<>(Arrays.asList(command.split("\\s+")));
            commands.add("--stacktrace");
            return GradleRunner.create()
                    .withPluginClasspath()
                    .withProjectDir(getProjectDir())
                    .withArguments(commands);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
