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
package co.elastic.apm.android.agp72.tools;

import com.android.build.api.variant.Variant;
import com.android.build.gradle.internal.component.ComponentCreationConfig;
import com.android.build.gradle.internal.publishing.AndroidArtifacts;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;

import co.elastic.apm.android.agp.api.tools.ClasspathProvider;

public class ClasspathProvider72 implements ClasspathProvider {
    private FileCollection runtimeClasspath;

    @Override
    public FileCollection getRuntimeClasspath(Variant variant) {
        if (runtimeClasspath == null) {
            runtimeClasspath = ((ComponentCreationConfig) variant).getVariantDependencies().getArtifactFileCollection(AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH,
                    AndroidArtifacts.ArtifactScope.ALL,
                    AndroidArtifacts.ArtifactType.CLASSES_JAR);
        }

        return runtimeClasspath;
    }

    @Override
    public Configuration getRuntimeConfiguration(Variant variant) {
        return ((ComponentCreationConfig) variant).getVariantDependencies().getRuntimeClasspath();
    }
}
