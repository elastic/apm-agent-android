package com.elastic.apm.android.plugin.testutils.buildgradle.block.impl;

import com.elastic.apm.android.plugin.testutils.buildgradle.block.BlockBuilder;

import java.util.HashMap;
import java.util.Map;

public class ElasticBlockBuilder implements BlockBuilder {
    private final Map<String, String> parameters = new HashMap<>();

    public void setServiceName(String serviceName) {
        parameters.put("serviceName", serviceName);
    }

    public void setServiceVersion(String serviceVersion) {
        parameters.put("serviceVersion", serviceVersion);
    }

    public void setServerUrl(String serverUrl) {
        parameters.put("serverUrl", serverUrl);
    }

    public void setServerToken(String serverToken) {
        parameters.put("serverToken", serverToken);
    }

    @Override
    public String build() {
        if (nothingSet()) {
            return build();
        }
        StringBuilder builder = new StringBuilder();
        builder.append("elasticApm {");
        for (String parameterName : parameters.keySet()) {
            String parameterValue = parameters.get(parameterName);
            addNewLine(builder);
            builder.append(parameterName).append(" '").append(parameterValue).append("'");
        }
        addNewLine(builder);
        builder.append("}");

        return builder.toString();
    }

    private void addNewLine(StringBuilder builder) {
        builder.append("\n");
    }

    private boolean nothingSet() {
        return parameters.isEmpty();
    }
}
