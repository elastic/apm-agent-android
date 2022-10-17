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
package com.elastic.apm.android.plugin.testutils;

import com.elastic.apm.android.plugin.testutils.buildgradle.BuildFileBuilder;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.UnexpectedBuildFailure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseFunctionalTest {
    protected File buildFile;
    protected File androidManifest;
    private final BuildFileBuilder buildFileBuilder;

    protected BaseFunctionalTest() {
        buildFileBuilder = new BuildFileBuilder(getAndroidCompileSdk(), getAndroidAppId());
    }

    protected abstract File getProjectDir();

    protected void addPlugin(String id) {
        addPlugin(id, null);
    }

    protected void addPlugin(String id, String version) {
        buildFileBuilder.addPlugin(id, version);
    }

    protected void setUpProject() {
        buildFile = createBuildFile();
        androidManifest = createAndroidManifest();
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

    protected BuildResult runGradle(String command) {
        try {
            command += " --include-build " + Paths.get("..").toFile().getCanonicalPath();
            List<String> commands = new ArrayList<>(Arrays.asList(command.split("\\s+")));
            return GradleRunner.create()
                    .withPluginClasspath()
                    .withProjectDir(getProjectDir())
                    .withArguments(commands)
                    .build();
        } catch (UnexpectedBuildFailure | IOException e) {
            System.out.println("Build file:\n");
            System.out.println(FileUtils.read(buildFile));
            throw new RuntimeException(e);
        }
    }
}
