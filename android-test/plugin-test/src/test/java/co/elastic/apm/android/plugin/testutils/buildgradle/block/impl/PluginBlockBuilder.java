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

import java.util.ArrayList;
import java.util.List;

import co.elastic.apm.android.plugin.testutils.buildgradle.block.BlockBuilder;

public class PluginBlockBuilder implements BlockBuilder {
    private final List<Plugin> plugins = new ArrayList<>();

    @Override
    public String build() {
        StringBuilder builder = new StringBuilder();
        for (Plugin plugin : plugins) {
            builder.append("\n");
            builder.append("apply plugin: '");
            builder.append(plugin.id);
            builder.append("'");
        }
        builder.append("\n");
        return builder.toString();
    }

    public void addPlugin(String pluginId) {
        if (pluginId == null) {
            throw new NullPointerException();
        }
        plugins.add(new Plugin(pluginId));
    }

    private static class Plugin {
        private final String id;

        private Plugin(String id) {
            this.id = id;
        }
    }
}
