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
package co.elastic.apm.android.plugin.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import co.elastic.apm.android.common.ApmInfo;

public abstract class ApmInfoGenerator extends DefaultTask {

    @Input
    public abstract Property<String> getVariantName();

    @Input
    public abstract Property<String> getServiceName();

    @Input
    public abstract Property<String> getServiceVersion();

    @Optional
    @Input
    public abstract Property<String> getOkHttpVersion();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void execute() {
        File propertiesFile = new File(getOutputDir().get().getAsFile(), ApmInfo.ASSET_FILE_NAME);
        Properties properties = new Properties();
        properties.put(ApmInfo.KEY_SERVICE_NAME, getServiceName().get());
        properties.put(ApmInfo.KEY_SERVICE_VERSION, getServiceVersion().get());
        properties.put(ApmInfo.KEY_SERVICE_ENVIRONMENT, getVariantName().get());
        String okhttpVersion = getOkHttpVersion().getOrNull();
        if (okhttpVersion != null) {
            properties.put(ApmInfo.KEY_SCOPE_OKHTTP_VERSION, okhttpVersion);
        }

        try (OutputStream outputStream = new FileOutputStream(propertiesFile)) {
            properties.store(outputStream, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
