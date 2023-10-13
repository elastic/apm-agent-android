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
package co.elastic.apm.android.plugin.tasks.tools;

import com.android.build.api.variant.Variant;

import org.gradle.api.Action;
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.file.FileCollection;

public class ClasspathProvider implements Action<ArtifactView.ViewConfiguration> {

    private static final Attribute<String> ARTIFACT_TYPE_ATTR = Attribute.of("artifactType", String.class);
    private FileCollection runtimeClasspath;

    public FileCollection getRuntimeClasspath(Variant variant) {
        if (runtimeClasspath == null) {
            runtimeClasspath = findClasspath(variant);
        }

        return runtimeClasspath;
    }

    public Configuration getRuntimeConfiguration(Variant variant) {
        return variant.getRuntimeConfiguration();
    }

    private FileCollection findClasspath(Variant variant) {
        return getRuntimeConfiguration(variant).getIncoming()
                .artifactView(this)
                .getArtifacts()
                .getArtifactFiles();
    }

    @Override
    public void execute(ArtifactView.ViewConfiguration configuration) {
        configuration.setLenient(false);
        configuration.getAttributes().attribute(ARTIFACT_TYPE_ATTR, "android-classes-jar");
    }
}
