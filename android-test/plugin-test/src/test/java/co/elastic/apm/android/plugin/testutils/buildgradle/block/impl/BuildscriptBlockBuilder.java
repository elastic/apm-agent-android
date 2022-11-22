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
package co.elastic.apm.android.plugin.testutils.buildgradle.block.impl;

import co.elastic.apm.android.plugin.testutils.buildgradle.block.BlockBuilder;

public class BuildscriptBlockBuilder implements BlockBuilder {
    private final RepositoriesBlockBuilder repositoriesBlockBuilder = new RepositoriesBlockBuilder();
    private final DependenciesBlockBuilder dependenciesBlockBuilder = new DependenciesBlockBuilder();

    public void addRepository(String repository) {
        repositoriesBlockBuilder.addRepo(repository);
    }

    public void addDependency(String dependency) {
        dependenciesBlockBuilder.addDependencyLine(dependency);
    }

    @Override
    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append("buildscript {");
        addNewLine(builder);
        builder.append(repositoriesBlockBuilder.build());
        addNewLine(builder);
        builder.append(dependenciesBlockBuilder.build());
        addNewLine(builder);
        builder.append("}");

        return builder.toString();
    }

    private void addNewLine(StringBuilder builder) {
        builder.append("\n");
    }
}
