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
package com.elastic.apm.android.plugin.testutils.buildgradle;

import com.elastic.apm.android.plugin.testutils.buildgradle.block.impl.ElasticBlockBuilder;
import com.elastic.apm.android.plugin.testutils.buildgradle.block.impl.PluginBlockBuilder;
import com.elastic.apm.android.plugin.testutils.buildgradle.block.impl.RepositoriesBlockBuilder;
import com.elastic.apm.android.plugin.testutils.buildgradle.block.impl.android.AndroidBlockBuilder;

public class BuildFileBuilder {
    private final PluginBlockBuilder pluginBlockBuilder = new PluginBlockBuilder();
    private final RepositoriesBlockBuilder repositoriesBlockBuilder = new RepositoriesBlockBuilder();
    private final AndroidBlockBuilder androidBlockBuilder;
    private final ElasticBlockBuilder elasticBlockBuilder = new ElasticBlockBuilder();

    public BuildFileBuilder(int androidCompileSdk, String applicationId, String versionName) {
        androidBlockBuilder = new AndroidBlockBuilder(androidCompileSdk, applicationId, versionName);
    }

    public BuildFileBuilder addPlugin(String pluginId) {
        pluginBlockBuilder.addPlugin(pluginId);
        return this;
    }

    public BuildFileBuilder addRepository(String repo) {
        repositoriesBlockBuilder.addRepo(repo);
        return this;
    }

    public AndroidBlockBuilder getAndroidBlockBuilder() {
        return androidBlockBuilder;
    }

    public ElasticBlockBuilder getElasticBlockBuilder() {
        return elasticBlockBuilder;
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append(pluginBlockBuilder.build());
        addNewLine(builder);
        builder.append(androidBlockBuilder.build());
        addNewLine(builder);
        builder.append(repositoriesBlockBuilder.build());
        addNewLine(builder);
        builder.append(elasticBlockBuilder.build());

        return builder.toString();
    }

    private void addNewLine(StringBuilder builder) {
        builder.append("\n");
    }
}
