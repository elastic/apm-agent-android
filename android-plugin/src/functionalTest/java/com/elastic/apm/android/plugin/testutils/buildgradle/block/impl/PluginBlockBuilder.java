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

import java.util.ArrayList;
import java.util.List;

public class PluginBlockBuilder implements BlockBuilder {
    private final List<Plugin> plugins = new ArrayList<>();

    @Override
    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append("plugins {");
        for (Plugin plugin : plugins) {
            builder.append("\n");
            builder.append("id '");
            builder.append(plugin.id);
            builder.append("'");
            String version = plugin.version;
            if (version != null) {
                builder.append(" version '");
                builder.append(version);
                builder.append("'");
            }
        }
        builder.append("\n");
        builder.append("}");
        return builder.toString();
    }

    public void addPlugin(String pluginId, String pluginVersion) {
        if (pluginId == null) {
            throw new NullPointerException();
        }
        plugins.add(new Plugin(pluginId, pluginVersion));
    }

    private static class Plugin {
        private final String id;
        private final String version;

        private Plugin(String id, String version) {
            this.id = id;
            this.version = version;
        }
    }
}
