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
package com.elastic.apm.android.plugin;

import com.elastic.apm.android.plugin.testutils.BaseFunctionalTest;

import org.gradle.testkit.runner.BuildResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

public class ApmAndroidAgentPluginTest extends BaseFunctionalTest {

    @Rule
    public TemporaryFolder projectTemporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        addPlugin("com.android.application");
        addPlugin("co.elastic.apm.android");
    }

    @Test
    public void apmInfoGenerator_verifyDefaultValues() {
        createBuildFile();
        BuildResult result = runGradle("assembleDebug");
    }

    @Override
    protected File getProjectDir() {
        return projectTemporaryFolder.getRoot();
    }
}
