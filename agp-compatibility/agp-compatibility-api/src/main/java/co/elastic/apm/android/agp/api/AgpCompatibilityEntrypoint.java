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
package co.elastic.apm.android.agp.api;

import com.android.build.api.AndroidPluginVersion;
import com.android.build.api.variant.AndroidComponentsExtension;

import org.gradle.api.Project;

import java.util.ServiceLoader;

import co.elastic.apm.android.common.internal.logging.Elog;

public interface AgpCompatibilityEntrypoint {
    String getDescription();

    boolean isCompatible(CurrentVersion currentVersion);

    AgpCompatibilityManager provideCompatibilityManager(Project project);

    static AgpCompatibilityManager findCompatibleManager(Project project) {
        ServiceLoader<AgpCompatibilityEntrypoint> entrypoints = ServiceLoader.load(AgpCompatibilityEntrypoint.class);
        if (!entrypoints.iterator().hasNext()) {
            throw new IllegalStateException("No implementations found for " + AgpCompatibilityEntrypoint.class.getName());
        }

        AndroidPluginVersion currentVersion = project.getExtensions().findByType(AndroidComponentsExtension.class).getPluginVersion();
        CurrentVersion comparable = new CurrentVersion(currentVersion);

        for (AgpCompatibilityEntrypoint entrypoint : entrypoints) {
            if (entrypoint.isCompatible(comparable)) {
                Elog.getLogger().debug("Found AGP compatible entrypoint with description: '{}'", entrypoint.getDescription());
                return entrypoint.provideCompatibilityManager(project);
            }
        }

        throw new UnsupportedOperationException("Could not find compatibility manager for " + currentVersion);
    }
}
