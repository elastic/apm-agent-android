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
package com.elastic.apm.android.plugin.testutils.buildgradle.block.impl;

import com.elastic.apm.android.plugin.testutils.buildgradle.block.BlockBuilder;

import java.util.HashMap;
import java.util.Map;

public class DependencyByModuleSubstitutionBlockBuilder implements BlockBuilder {
    private final Map<String, String> dependencyToModuleName = new HashMap<>();

    public void addSubstitution(String dependency, String moduleName) {
        dependencyToModuleName.put(dependency, moduleName);
    }

    @Override
    public String build() {
        if (dependencyToModuleName.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("configurations.all {");
        addNewLine(builder);
        builder.append("resolutionStrategy.dependencySubstitution {");
        for (String original : dependencyToModuleName.keySet()) {
            addNewLine(builder);
            String replacement = dependencyToModuleName.get(original);
            builder.append("substitute module('").append(original).append("') using project('").append(replacement).append("')");
        }
        addNewLine(builder);
        builder.append("}");
        addNewLine(builder);
        builder.append("}");
        return builder.toString();
    }

    private void addNewLine(StringBuilder builder) {
        builder.append("\n");
    }
}
