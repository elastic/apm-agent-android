package co.elastic.otel.android.compilation.tools.sourceheader.subplugins;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

public class BaseSourceHeaderPlugin implements Plugin<Project> {
    protected SpotlessExtension spotlessExtension;

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(SpotlessPlugin.class);
        spotlessExtension = project.getExtensions().getByType(SpotlessExtension.class);

        spotlessExtension.java(javaExtension -> javaExtension.licenseHeader(getLicenseHeader()));
    }

    protected String getLicenseHeader() {
        return "/*\n" +
                " * Licensed to Elasticsearch B.V. under one or more contributor\n" +
                " * license agreements. See the NOTICE file distributed with\n" +
                " * this work for additional information regarding copyright\n" +
                " * ownership. Elasticsearch B.V. licenses this file to you under\n" +
                " * the Apache License, Version 2.0 (the \"License\"); you may\n" +
                " * not use this file except in compliance with the License.\n" +
                " * You may obtain a copy of the License at\n" +
                " *\n" +
                " *\thttp://www.apache.org/licenses/LICENSE-2.0\n" +
                " *\n" +
                " * Unless required by applicable law or agreed to in writing,\n" +
                " * software distributed under the License is distributed on an\n" +
                " * \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n" +
                " * KIND, either express or implied.  See the License for the\n" +
                " * specific language governing permissions and limitations\n" +
                " * under the License.\n" +
                " */\n";
    }

    protected TaskProvider<Task> getSpotlessApply(Project project) {
        return project.getTasks().named("spotlessApply");
    }
}
